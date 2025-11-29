package com.cph.clion.actions

import com.cph.clion.models.CphSubmitResponse
import com.cph.clion.models.LanguageExtensions
import com.cph.clion.services.CompanionServerService
import com.cph.clion.services.ProblemService
import com.cph.clion.settings.CphSettings
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.fileEditor.FileDocumentManager
import java.io.File

/**
 * Action to submit the current solution to Codeforces.
 * Works with the cph-submit browser extension.
 * Bound to Ctrl+Alt+S by default.
 */
class SubmitToCodeforcesAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val file = FileDocumentManager.getInstance().getFile(editor.document) ?: return
        val extension = file.extension ?: return

        if (!LanguageExtensions.isSupported(extension)) {
            showNotification(project, "Unsupported file type: $extension", NotificationType.ERROR)
            return
        }

        // Save all documents
        FileDocumentManager.getInstance().saveAllDocuments()

        val srcPath = file.path
        val problemService = ProblemService.getInstance(project)

        val problem = problemService.loadProblem(srcPath)
        if (problem == null) {
            showNotification(project, "No problem found for this file. Load a problem first.", NotificationType.ERROR)
            return
        }

        if (!problem.url.contains("codeforces.com")) {
            showNotification(project, "This problem is not from Codeforces.", NotificationType.WARNING)
            return
        }

        // Get source code
        val sourceCode = File(srcPath).readText()

        // Get language ID for submission
        val settings = CphSettings.getInstance()
        val languageId = getLanguageId(extension, settings)

        if (languageId == -1) {
            showNotification(project, "Unknown language for submission.", NotificationType.ERROR)
            return
        }

        // Extract problem name from URL
        val problemName = extractProblemName(problem.url) ?: problem.name

        // Store the response for cph-submit extension
        val response = CphSubmitResponse(
            empty = false,
            url = problem.url,
            problemName = problemName,
            sourceCode = sourceCode,
            languageId = languageId
        )

        CompanionServerService.getInstance().storeSubmitResponse(response)

        showNotification(
            project,
            "Ready to submit! Click the cph-submit extension button in your browser.",
            NotificationType.INFORMATION
        )
    }

    override fun update(e: AnActionEvent) {
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
        val extension = file?.extension
        e.presentation.isEnabled = extension != null && LanguageExtensions.isSupported(extension)
    }

    private fun getLanguageId(extension: String, settings: CphSettings): Int {
        val compiler = when (extension.lowercase()) {
            "cpp", "cc", "cxx" -> settings.state.cppSubmissionCompiler
            "c" -> settings.state.cSubmissionCompiler
            "py" -> settings.state.pythonSubmissionCompiler
            "java" -> settings.state.javaSubmissionCompiler
            "rs" -> settings.state.rustSubmissionCompiler
            "go" -> settings.state.goSubmissionCompiler
            else -> return -1
        }
        return settings.getCompilerId(compiler)
    }

    private fun extractProblemName(url: String): String? {
        // Match contest and problem ID from URL like /contest/123/problem/A
        val regex = Regex("codeforces\\.com/(contest|problemset)/(\\d+)/problem/(\\w+)")
        val match = regex.find(url)
        return if (match != null) {
            "${match.groupValues[2]}${match.groupValues[3]}"
        } else null
    }

    private fun showNotification(project: com.intellij.openapi.project.Project, message: String, type: NotificationType) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("CPH Notifications")
            .createNotification(message, type)
            .notify(project)
    }
}
