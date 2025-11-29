package com.cph.clion.services

import com.cph.clion.models.Problem
import com.google.gson.Gson
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.util.concurrent.Executors

/**
 * Service that runs an HTTP server to receive problems from Competitive Companion browser extension.
 * This is an application-level service that runs once per IDE instance.
 */
@Service(Service.Level.APP)
class CompanionServerService {

    private val logger = Logger.getInstance(CompanionServerService::class.java)
    private val gson = Gson()
    private var server: HttpServer? = null

    companion object {
        const val COMPANION_PORT = 27121

        fun getInstance(): CompanionServerService {
            return ApplicationManager.getApplication().getService(CompanionServerService::class.java)
        }
    }

    // Callbacks for problem handling
    private var onProblemReceived: ((Problem) -> Unit)? = null

    fun setOnProblemReceived(callback: (Problem) -> Unit) {
        onProblemReceived = callback
    }

    /**
     * Starts the Competitive Companion HTTP server on port 27121.
     */
    fun startServer() {
        if (server != null) {
            logger.info("Companion server already running")
            return
        }

        try {
            server = HttpServer.create(InetSocketAddress(COMPANION_PORT), 0).apply {
                createContext("/") { exchange ->
                    try {
                        val requestBody = exchange.requestBody.bufferedReader().readText()

                        if (requestBody.isNotEmpty()) {
                            // Handle Competitive Companion problem
                            try {
                                val problem = gson.fromJson(requestBody, Problem::class.java)
                                logger.info("Received problem: ${problem.name}")
                                
                                ApplicationManager.getApplication().invokeLater {
                                    onProblemReceived?.invoke(problem)
                                }

                                exchange.sendResponseHeaders(200, 0)
                                exchange.responseBody.close()
                            } catch (e: Exception) {
                                logger.error("Error parsing problem", e)
                                showNotification("Error parsing problem from Competitive Companion: ${e.message}")
                                exchange.sendResponseHeaders(400, 0)
                                exchange.responseBody.close()
                            }
                        } else {
                            exchange.sendResponseHeaders(200, 0)
                            exchange.responseBody.close()
                        }
                    } catch (e: Exception) {
                        logger.error("Error handling request", e)
                        exchange.sendResponseHeaders(500, 0)
                        exchange.responseBody.close()
                    }
                }
                executor = Executors.newCachedThreadPool()
                start()
            }
            logger.info("Companion server started on port $COMPANION_PORT")
        } catch (e: Exception) {
            logger.error("Failed to start Companion server", e)
            showNotification(
                "Failed to start CPH Companion server on port $COMPANION_PORT. " +
                "Is another IDE instance already running? Error: ${e.message}"
            )
        }
    }

    /**
     * Stops the Companion server.
     */
    fun stopServer() {
        server?.stop(0)
        server = null
        logger.info("Companion server stopped")
    }

    /**
     * Check if the server is running.
     */
    fun isRunning(): Boolean = server != null

    private fun showNotification(message: String) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("CPH Notifications")
            .createNotification(message, NotificationType.WARNING)
            .notify(null)
    }
}
