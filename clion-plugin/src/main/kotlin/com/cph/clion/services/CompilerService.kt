package com.cph.clion.services

import com.cph.clion.models.Language
import com.cph.clion.models.LanguageExtensions
import com.cph.clion.settings.CphSettings
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessAdapter
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessOutputTypes
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.Key
import java.io.File
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 * Service for compiling source code files.
 */
object CompilerService {

    private val logger = Logger.getInstance(CompilerService::class.java)
    
    // Scheduler for timeout handling
    private val scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(1) { r ->
        Thread(r, "CPH-CompilerTimeout").apply { isDaemon = true }
    }

    data class CompilationResult(
        val success: Boolean,
        val output: String,
        val errorOutput: String,
        val exitCode: Int
    )

    /**
     * Compile the source file and return the result.
     * Returns the path to the compiled binary, or null if compilation failed.
     */
    fun compile(srcPath: String): CompletableFuture<CompilationResult> {
        val future = CompletableFuture<CompilationResult>()
        val srcFile = File(srcPath)
        val extension = srcFile.extension

        // Check if compilation should be skipped
        if (LanguageExtensions.shouldSkipCompile(extension)) {
            future.complete(CompilationResult(
                success = true,
                output = "Compilation skipped for interpreted language",
                errorOutput = "",
                exitCode = 0
            ))
            return future
        }

        val language = getLanguage(srcPath)
        if (language == null) {
            future.complete(CompilationResult(
                success = false,
                output = "",
                errorOutput = "Unsupported file extension: $extension",
                exitCode = -1
            ))
            return future
        }

        val binPath = getBinSaveLocation(srcPath)
        val flags = getCompilerFlags(language, srcPath, binPath)

        logger.info("Compiling $srcPath with: ${language.compiler} ${flags.joinToString(" ")}")

        try {
            val commandLine = GeneralCommandLine(language.compiler)
                .withParameters(flags)
                .withWorkDirectory(srcFile.parent)
                .withCharset(Charsets.UTF_8)

            val stdout = StringBuilder()
            val stderr = StringBuilder()

            val processHandler = OSProcessHandler(commandLine)
            processHandler.addProcessListener(object : ProcessAdapter() {
                override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
                    if (outputType === ProcessOutputTypes.STDOUT) {
                        stdout.append(event.text)
                    } else if (outputType === ProcessOutputTypes.STDERR) {
                        stderr.append(event.text)
                    }
                }

                override fun processTerminated(event: ProcessEvent) {
                    val success = event.exitCode == 0
                    future.complete(CompilationResult(
                        success = success,
                        output = stdout.toString(),
                        errorOutput = stderr.toString(),
                        exitCode = event.exitCode
                    ))
                }
            })

            processHandler.startNotify()

            // Set a timeout for compilation using scheduler
            scheduler.schedule({
                if (!future.isDone) {
                    processHandler.destroyProcess()
                    future.complete(CompilationResult(
                        success = false,
                        output = "",
                        errorOutput = "Compilation timed out",
                        exitCode = -1
                    ))
                }
            }, 60, TimeUnit.SECONDS)

        } catch (e: Exception) {
            logger.error("Compilation error", e)
            future.complete(CompilationResult(
                success = false,
                output = "",
                errorOutput = "Failed to start compiler: ${e.message}",
                exitCode = -1
            ))
        }

        return future
    }

    /**
     * Get the Language configuration for a source file.
     */
    fun getLanguage(srcPath: String): Language? {
        val extension = File(srcPath).extension
        val langName = LanguageExtensions.getLanguageName(extension) ?: return null
        val settings = CphSettings.getInstance()

        return Language(
            name = langName,
            compiler = settings.getCommandForLanguage(extension),
            args = settings.getArgsForLanguage(extension),
            skipCompile = LanguageExtensions.shouldSkipCompile(extension)
        )
    }

    /**
     * Get the path where the compiled binary should be saved.
     */
    fun getBinSaveLocation(srcPath: String): String {
        val srcFile = File(srcPath)
        val extension = srcFile.extension
        val settings = CphSettings.getInstance().state

        if (LanguageExtensions.shouldSkipCompile(extension)) {
            return srcPath
        }

        val binExtension = when {
            extension == "java" -> ".class"
            System.getProperty("os.name").lowercase().contains("windows") -> ".exe"
            else -> ".bin"
        }

        val binFileName = srcFile.nameWithoutExtension + binExtension
        val saveLocation = settings.saveLocation

        return if (saveLocation.isNotEmpty() && File(saveLocation).exists()) {
            File(saveLocation, binFileName).absolutePath
        } else {
            File(srcFile.parent, binFileName).absolutePath
        }
    }

    /**
     * Get compiler flags for a source file.
     */
    private fun getCompilerFlags(language: Language, srcPath: String, binPath: String): List<String> {
        val settings = CphSettings.getInstance()
        val args = language.args.filter { it.isNotEmpty() }.toMutableList()

        return when (language.name) {
            "cpp", "cc", "cxx" -> {
                listOf(srcPath, settings.getOutputArgForLanguage("cpp"), binPath) + 
                    args + listOf("-D", "DEBUG", "-D", "CPH")
            }
            "c" -> {
                listOf(srcPath, settings.getOutputArgForLanguage("c"), binPath) + args
            }
            "rust" -> {
                listOf(srcPath, "-o", binPath) + args
            }
            "go" -> {
                listOf("build", "-o", binPath, srcPath) + args
            }
            "java" -> {
                val binDir = File(binPath).parent
                listOf(srcPath, "-d", binDir) + args
            }
            "haskell" -> {
                listOf(srcPath, "-o", binPath, "-no-keep-hi-files", "-no-keep-o-files") + args
            }
            else -> listOf(srcPath) + args
        }
    }

    /**
     * Check if a file needs to be recompiled.
     * Returns true if the source file is newer than the binary.
     */
    fun needsRecompilation(srcPath: String): Boolean {
        val extension = File(srcPath).extension
        if (LanguageExtensions.shouldSkipCompile(extension)) {
            return false
        }

        val srcFile = File(srcPath)
        val binFile = File(getBinSaveLocation(srcPath))

        return !binFile.exists() || srcFile.lastModified() > binFile.lastModified()
    }
}
