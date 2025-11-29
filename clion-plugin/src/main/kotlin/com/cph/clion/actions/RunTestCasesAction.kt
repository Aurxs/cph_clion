package com.cph.clion.actions

import com.cph.clion.models.LanguageExtensions
import com.cph.clion.services.CompilerService
import com.cph.clion.services.JudgeService
import com.cph.clion.services.ProblemService
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.wm.ToolWindowManager

/**
 * Action to run all test cases for the current file.
 * Bound to Ctrl+Alt+B by default.
 */
class RunTestCasesAction : AnAction() {

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

        // Load or create problem
        var problem = problemService.loadProblem(srcPath)
        if (problem == null) {
            problem = problemService.createLocalProblem(srcPath)
        }

        // Show the tool window
        val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("CPH Judge")
        toolWindow?.show()

        // Compile and run
        showNotification(project, "Compiling...", NotificationType.INFORMATION)

        CompilerService.compile(srcPath).thenAccept { result ->
            ApplicationManager.getApplication().invokeLater {
                if (!result.success) {
                    showNotification(
                        project,
                        "Compilation failed:\n${result.errorOutput}",
                        NotificationType.ERROR
                    )
                    return@invokeLater
                }

                showNotification(project, "Running ${problem.tests.size} test cases...", NotificationType.INFORMATION)

                JudgeService.runAllTestCases(
                    problem,
                    onProgress = { _ -> },
                    onComplete = { results ->
                        ApplicationManager.getApplication().invokeLater {
                            val passed = results.count { it.pass == true }
                            val total = results.size
                            val status = if (passed == total) "All passed!" else "$passed/$total passed"
                            showNotification(project, status, 
                                if (passed == total) NotificationType.INFORMATION else NotificationType.WARNING
                            )
                        }
                    }
                )
            }
        }
    }

    override fun update(e: AnActionEvent) {
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
        val extension = file?.extension
        e.presentation.isEnabled = extension != null && LanguageExtensions.isSupported(extension)
    }

    private fun showNotification(project: com.intellij.openapi.project.Project, message: String, type: NotificationType) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("CPH Notifications")
            .createNotification(message, type)
            .notify(project)
    }
}
