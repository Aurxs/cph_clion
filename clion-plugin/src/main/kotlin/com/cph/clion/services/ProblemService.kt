package com.cph.clion.services

import com.cph.clion.models.LanguageExtensions
import com.cph.clion.models.Problem
import com.cph.clion.models.TestCase
import com.cph.clion.settings.CphSettings
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import java.io.File
import java.net.URL

/**
 * Project-level service for managing competitive programming problems.
 * Handles problem creation, saving, loading, and file management.
 */
@Service(Service.Level.PROJECT)
class ProblemService(private val project: Project) {

    private val logger = Logger.getInstance(ProblemService::class.java)
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    // Current active problem
    var currentProblem: Problem? = null
        private set

    companion object {
        const val PROBLEM_FILE_EXTENSION = ".prob"

        fun getInstance(project: Project): ProblemService {
            return project.getService(ProblemService::class.java)
        }
    }

    /**
     * Handle a new problem received from Competitive Companion.
     * Creates the source file and saves the problem metadata.
     */
    fun handleNewProblem(problem: Problem) {
        val settings = CphSettings.getInstance().state
        val basePath = project.basePath ?: run {
            showNotification("Please open a project folder first.", NotificationType.ERROR)
            return
        }

        // Determine file extension based on default language
        val extension = getExtensionForLanguage(settings.defaultLanguage)
        val fileName = generateProblemFileName(problem, extension, settings)
        val srcPath = File(basePath, fileName).absolutePath

        problem.srcPath = srcPath

        // Assign IDs to test cases
        problem.tests.forEachIndexed { index, testCase ->
            if (testCase.id == 0L) {
                problem.tests[index] = testCase.copy(id = System.currentTimeMillis() + index)
            }
        }

        // Create the source file if it doesn't exist
        val srcFile = File(srcPath)
        if (!srcFile.exists()) {
            val templateContent = getTemplateContent(extension, fileName, settings)
            srcFile.writeText(templateContent)
        }

        // Save problem metadata
        saveProblem(srcPath, problem)

        // Open the file in the editor
        val virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(srcPath)
        if (virtualFile != null) {
            ApplicationManager.getApplication().invokeLater {
                FileEditorManager.getInstance(project).openFile(virtualFile, true)
            }
        }

        currentProblem = problem
        showNotification("Problem loaded: ${problem.name}", NotificationType.INFORMATION)
    }

    /**
     * Get the file extension for a language name.
     */
    private fun getExtensionForLanguage(language: String): String {
        return when (language.lowercase()) {
            "c" -> "c"
            "cpp" -> "cpp"
            "python" -> "py"
            "java" -> "java"
            "rust" -> "rs"
            "go" -> "go"
            "ruby" -> "rb"
            "js", "javascript" -> "js"
            "haskell" -> "hs"
            "csharp" -> "cs"
            else -> "cpp"
        }
    }

    /**
     * Generate the problem file name based on settings.
     */
    private fun generateProblemFileName(
        problem: Problem, 
        extension: String, 
        settings: CphSettings.State
    ): String {
        val url = try { URL(problem.url) } catch (e: Exception) { null }
        
        // Check for short naming preferences
        if (url != null) {
            if (settings.useShortCodeforcesName && isCodeforcesUrl(url)) {
                val problemName = extractCodeforcesProblemName(problem.url)
                if (problemName != null) return "$problemName.$extension"
            }
            if (settings.useShortLuoguName && isLuoguUrl(url)) {
                val match = Regex("problem/(\\w+)").find(problem.url)
                match?.groupValues?.get(1)?.let { return "$it.$extension" }
            }
            if (settings.useShortAtCoderName && isAtCoderUrl(url)) {
                val match = Regex("tasks/(\\w+)_(\\w+)").find(problem.url)
                if (match != null) {
                    return "${match.groupValues[1]}${match.groupValues[2]}.$extension"
                }
            }
        }

        // Default: use problem name with underscores
        val safeName = problem.name.replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
            .replace(Regex("_+"), "_")
            .trim('_')
        return "$safeName.$extension"
    }

    /**
     * Get template content for a new file.
     */
    private fun getTemplateContent(
        extension: String, 
        fileName: String, 
        settings: CphSettings.State
    ): String {
        val templateLocation = settings.defaultLanguageTemplateFileLocation
        if (templateLocation.isNotEmpty()) {
            val templateFile = File(templateLocation)
            if (templateFile.exists()) {
                var content = templateFile.readText()
                if (extension == "java") {
                    val className = fileName.substringBefore(".")
                    content = content.replace("CLASS_NAME", className)
                }
                return content
            }
        }
        return getDefaultTemplate(extension, fileName)
    }

    /**
     * Get default template for a language.
     */
    private fun getDefaultTemplate(extension: String, fileName: String): String {
        return when (extension) {
            "cpp" -> """
                #include <bits/stdc++.h>
                using namespace std;
                
                int main() {
                    ios::sync_with_stdio(false);
                    cin.tie(nullptr);
                    
                    return 0;
                }
            """.trimIndent()
            "c" -> """
                #include <stdio.h>
                
                int main() {
                    
                    return 0;
                }
            """.trimIndent()
            "java" -> {
                val className = fileName.substringBefore(".")
                """
                import java.util.*;
                import java.io.*;
                
                public class $className {
                    public static void main(String[] args) {
                        Scanner sc = new Scanner(System.in);
                        
                    }
                }
                """.trimIndent()
            }
            "py" -> """
                import sys
                input = sys.stdin.readline
                
                def main():
                    pass
                
                if __name__ == "__main__":
                    main()
            """.trimIndent()
            "rs" -> """
                use std::io::{self, BufRead, Write};
                
                fn main() {
                    let stdin = io::stdin();
                    let stdout = io::stdout();
                    let mut out = io::BufWriter::new(stdout.lock());
                    
                }
            """.trimIndent()
            "go" -> """
                package main
                
                import (
                    "bufio"
                    "fmt"
                    "os"
                )
                
                func main() {
                    reader := bufio.NewReader(os.Stdin)
                    _ = reader
                }
            """.trimIndent()
            else -> ""
        }
    }

    /**
     * Save problem to a .prob file.
     */
    fun saveProblem(srcPath: String, problem: Problem) {
        val probPath = getProblemFilePath(srcPath)
        val probFile = File(probPath)
        probFile.parentFile?.mkdirs()
        probFile.writeText(gson.toJson(problem))
        logger.info("Saved problem to: $probPath")
    }

    /**
     * Load problem from a .prob file.
     */
    fun loadProblem(srcPath: String): Problem? {
        val probPath = getProblemFilePath(srcPath)
        val probFile = File(probPath)
        if (!probFile.exists()) {
            return null
        }
        return try {
            val problem = gson.fromJson(probFile.readText(), Problem::class.java)
            currentProblem = problem
            problem
        } catch (e: Exception) {
            logger.error("Error loading problem from $probPath", e)
            null
        }
    }

    /**
     * Get the path to the .prob file for a source file.
     */
    fun getProblemFilePath(srcPath: String): String {
        val settings = CphSettings.getInstance().state
        val srcFile = File(srcPath)
        val probFileName = srcFile.nameWithoutExtension + PROBLEM_FILE_EXTENSION

        return if (settings.saveLocation.isNotEmpty() && File(settings.saveLocation).exists()) {
            File(File(settings.saveLocation, ".cph"), probFileName).absolutePath
        } else {
            File(File(srcFile.parent, ".cph"), probFileName).absolutePath
        }
    }

    /**
     * Delete the .prob file for a source file.
     */
    fun deleteProblemFile(srcPath: String) {
        val probPath = getProblemFilePath(srcPath)
        val probFile = File(probPath)
        if (probFile.exists()) {
            probFile.delete()
            // Clean up empty .cph directory
            val cphDir = probFile.parentFile
            if (cphDir?.listFiles()?.isEmpty() == true) {
                cphDir.delete()
            }
        }
        if (currentProblem?.srcPath == srcPath) {
            currentProblem = null
        }
    }

    /**
     * Create a local problem for a source file that doesn't have one.
     */
    fun createLocalProblem(srcPath: String): Problem {
        val srcFile = File(srcPath)
        val problem = Problem(
            name = "Local: ${srcFile.nameWithoutExtension}",
            url = srcPath,
            tests = mutableListOf(
                TestCase(
                    id = System.currentTimeMillis(),
                    input = "",
                    output = ""
                )
            ),
            srcPath = srcPath,
            local = true
        )
        saveProblem(srcPath, problem)
        currentProblem = problem
        return problem
    }

    /**
     * Get the problem for the currently active editor file.
     */
    fun getProblemForActiveFile(): Problem? {
        val editor = FileEditorManager.getInstance(project).selectedTextEditor ?: return null
        val virtualFile = FileDocumentManager.getInstance().getFile(editor.document) ?: return null
        return loadProblem(virtualFile.path)
    }

    /**
     * Check if a file extension is supported.
     */
    fun isSupportedFile(virtualFile: VirtualFile): Boolean {
        val extension = virtualFile.extension ?: return false
        return LanguageExtensions.isSupported(extension)
    }

    // URL helpers
    private fun isCodeforcesUrl(url: URL): Boolean {
        return url.host.contains("codeforces.com")
    }

    private fun isLuoguUrl(url: URL): Boolean {
        return url.host.contains("luogu.com.cn")
    }

    private fun isAtCoderUrl(url: URL): Boolean {
        return url.host == "atcoder.jp"
    }

    private fun extractCodeforcesProblemName(url: String): String? {
        // Match contest and problem ID from URL like /contest/123/problem/A
        val regex = Regex("codeforces\\.com/(contest|problemset)/(\\d+)/problem/(\\w+)")
        val match = regex.find(url)
        return if (match != null) {
            "${match.groupValues[2]}${match.groupValues[3]}"
        } else null
    }

    private fun showNotification(message: String, type: NotificationType) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("CPH Notifications")
            .createNotification(message, type)
            .notify(project)
    }
}
