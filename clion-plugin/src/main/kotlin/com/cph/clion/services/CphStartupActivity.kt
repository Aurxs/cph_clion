package com.cph.clion.services

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

/**
 * Startup activity that runs when a project is opened.
 * Initializes the Companion server and sets up problem handling.
 */
class CphStartupActivity : ProjectActivity {

    override suspend fun execute(project: Project) {
        // Start the Companion server
        val companionServer = CompanionServerService.getInstance()
        companionServer.startServer()

        // Set up problem handling callback
        val problemService = ProblemService.getInstance(project)
        companionServer.setOnProblemReceived { problem ->
            problemService.handleNewProblem(problem)
        }
    }
}
