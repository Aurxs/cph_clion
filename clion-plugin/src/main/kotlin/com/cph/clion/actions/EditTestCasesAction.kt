package com.cph.clion.actions

import com.cph.clion.models.LanguageExtensions
import com.cph.clion.services.ProblemService
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.wm.ToolWindowManager

/**
 * Action to create or edit test cases for the current file.
 */
class EditTestCasesAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val file = FileDocumentManager.getInstance().getFile(editor.document) ?: return
        val extension = file.extension ?: return

        if (!LanguageExtensions.isSupported(extension)) {
            showNotification(project, "Unsupported file type: $extension", NotificationType.ERROR)
            return
        }

        val srcPath = file.path
        val problemService = ProblemService.getInstance(project)

        // Load or create problem
        var problem = problemService.loadProblem(srcPath)
        if (problem == null) {
            problem = problemService.createLocalProblem(srcPath)
            showNotification(project, "Created new local problem with one test case.", NotificationType.INFORMATION)
        }

        // Show the tool window
        val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("CPH Judge")
        toolWindow?.show()
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
