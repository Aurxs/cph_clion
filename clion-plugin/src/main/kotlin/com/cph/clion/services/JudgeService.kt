package com.cph.clion.services

import com.cph.clion.models.*
import com.cph.clion.settings.CphSettings
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessAdapter
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessOutputTypes
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.Key
import java.io.File
import java.io.OutputStreamWriter
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 * Service for running test cases against compiled programs.
 */
object JudgeService {

    private val logger = Logger.getInstance(JudgeService::class.java)

    // Track running processes so they can be killed if needed
    private val runningProcesses = ConcurrentHashMap<Long, OSProcessHandler>()
    
    // Scheduler for timeout handling
    private val scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(1) { r ->
        Thread(r, "CPH-JudgeTimeout").apply { isDaemon = true }
    }

    /**
     * Run a single test case and return the result.
     */
    fun runTestCase(
        srcPath: String,
        testCase: TestCase,
        onComplete: (RunResult) -> Unit
    ) {
        val settings = CphSettings.getInstance().state
        val timeout = settings.timeout.toLong()
        val extension = File(srcPath).extension

        val commandLine = buildCommandLine(srcPath, extension) ?: run {
            onComplete(RunResult(
                id = testCase.id,
                pass = false,
                stdout = "",
                stderr = "Unsupported file extension: $extension",
                exitCode = -1,
                signal = null,
                time = 0,
                timeout = false
            ))
            return
        }

        logger.info("Running testcase ${testCase.id} for $srcPath")

        try {
            val stdout = StringBuilder()
            val stderr = StringBuilder()
            val startTime = System.currentTimeMillis()

            val processHandler = OSProcessHandler(commandLine)
            runningProcesses[testCase.id] = processHandler

            processHandler.addProcessListener(object : ProcessAdapter() {
                override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
                    if (outputType === ProcessOutputTypes.STDOUT) {
                        stdout.append(event.text)
                    } else if (outputType === ProcessOutputTypes.STDERR) {
                        stderr.append(event.text)
                    }
                }

                override fun processTerminated(event: ProcessEvent) {
                    runningProcesses.remove(testCase.id)
                    val elapsed = System.currentTimeMillis() - startTime
                    val isTimedOut = elapsed >= timeout

                    val pass = if (isTimedOut) {
                        false
                    } else if (event.exitCode != 0) {
                        null // Runtime error
                    } else {
                        isResultCorrect(testCase, stdout.toString())
                    }

                    onComplete(RunResult(
                        id = testCase.id,
                        pass = pass,
                        stdout = stdout.toString(),
                        stderr = stderr.toString(),
                        exitCode = event.exitCode,
                        signal = null,
                        time = elapsed,
                        timeout = isTimedOut
                    ))
                }

                override fun startNotified(event: ProcessEvent) {
                    // Write input to stdin
                    processHandler.processInput?.let { inputStream ->
                        try {
                            val writer = OutputStreamWriter(inputStream, Charsets.UTF_8)
                            writer.write(testCase.input)
                            writer.flush()
                            writer.close()
                        } catch (e: Exception) {
                            logger.warn("Error writing to stdin", e)
                        }
                    }
                }
            })

            processHandler.startNotify()

            // Set up timeout using scheduler
            scheduler.schedule({
                val handler = runningProcesses[testCase.id]
                if (handler != null && !handler.isProcessTerminated) {
                    handler.destroyProcess()
                }
            }, timeout, TimeUnit.MILLISECONDS)

        } catch (e: Exception) {
            logger.error("Error running testcase", e)
            runningProcesses.remove(testCase.id)
            onComplete(RunResult(
                id = testCase.id,
                pass = false,
                stdout = "",
                stderr = "Error running testcase: ${e.message}",
                exitCode = -1,
                signal = null,
                time = 0,
                timeout = false
            ))
        }
    }

    /**
     * Run all test cases for a problem.
     */
    fun runAllTestCases(
        problem: Problem,
        onProgress: (RunResult) -> Unit,
        onComplete: (List<RunResult>) -> Unit
    ) {
        if (problem.tests.isEmpty()) {
            onComplete(emptyList())
            return
        }

        val results = mutableListOf<RunResult>()
        val totalTests = problem.tests.size
        var completedTests = 0

        for (testCase in problem.tests) {
            runTestCase(problem.srcPath, testCase) { result ->
                synchronized(results) {
                    results.add(result)
                    completedTests++
                    onProgress(result)

                    if (completedTests == totalTests) {
                        onComplete(results.sortedBy { it.id })
                    }
                }
            }
        }
    }

    /**
     * Kill all running test processes.
     */
    fun killAllRunning() {
        runningProcesses.forEach { (id, handler) ->
            try {
                handler.destroyProcess()
            } catch (e: Exception) {
                logger.warn("Error killing process $id", e)
            }
        }
        runningProcesses.clear()
    }

    /**
     * Build the command line for running a program.
     */
    private fun buildCommandLine(srcPath: String, extension: String): GeneralCommandLine? {
        val settings = CphSettings.getInstance()
        val srcFile = File(srcPath)

        return when (extension.lowercase()) {
            "py" -> {
                GeneralCommandLine(settings.getCommandForLanguage("py"))
                    .withParameters(settings.getArgsForLanguage("py") + listOf(srcPath))
                    .withWorkDirectory(srcFile.parent)
            }
            "rb" -> {
                GeneralCommandLine(settings.getCommandForLanguage("rb"))
                    .withParameters(settings.getArgsForLanguage("rb") + listOf(srcPath))
                    .withWorkDirectory(srcFile.parent)
            }
            "js" -> {
                GeneralCommandLine(settings.getCommandForLanguage("js"))
                    .withParameters(settings.getArgsForLanguage("js") + listOf(srcPath))
                    .withWorkDirectory(srcFile.parent)
            }
            "cpp", "cc", "cxx", "c", "rs", "go", "hs" -> {
                val binPath = CompilerService.getBinSaveLocation(srcPath)
                GeneralCommandLine(binPath)
                    .withWorkDirectory(srcFile.parent)
            }
            "java" -> {
                val className = srcFile.nameWithoutExtension
                val binDir = File(CompilerService.getBinSaveLocation(srcPath)).parent
                GeneralCommandLine("java")
                    .withParameters("-cp", binDir, className)
                    .withWorkDirectory(srcFile.parent)
            }
            else -> null
        }
    }

    /**
     * Judge whether the test case output is correct.
     */
    fun isResultCorrect(testCase: TestCase, stdout: String): Boolean {
        // Normalize line endings
        val expected = testCase.output.replace("\r\n", "\n").trim()
        val result = stdout.replace("\r\n", "\n").trim()

        if (expected.isEmpty() && result.isEmpty()) {
            return true
        }

        val expectedLines = expected.split("\n")
        val resultLines = result.split("\n")

        if (expectedLines.size != resultLines.size) {
            return false
        }

        for (i in expectedLines.indices) {
            if (expectedLines[i].trim() != resultLines[i].trim()) {
                return false
            }
        }

        return true
    }
}
