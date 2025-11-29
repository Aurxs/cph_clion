package com.cph.clion.ui

import com.cph.clion.services.ProblemService
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

/**
 * Factory for creating the CPH Judge tool window.
 */
class CphToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val judgePanel = CphJudgePanel(project)
        val content = ContentFactory.getInstance().createContent(judgePanel, "Results", false)
        toolWindow.contentManager.addContent(content)

        // Listen for file editor changes to update the problem
        project.messageBus.connect().subscribe(
            FileEditorManagerListener.FILE_EDITOR_MANAGER,
            object : FileEditorManagerListener {
                override fun fileOpened(source: FileEditorManager, file: VirtualFile) {
                    updateProblemForFile(project, judgePanel, file)
                }

                override fun selectionChanged(event: FileEditorManagerListener.FileEditorManagerEvent) {
                    event.newFile?.let { file ->
                        updateProblemForFile(project, judgePanel, file)
                    }
                }
            }
        )

        // Load problem for the currently open file
        FileEditorManager.getInstance(project).selectedTextEditor?.let { editor ->
            val file = editor.document.let {
                com.intellij.openapi.fileEditor.FileDocumentManager.getInstance().getFile(it)
            }
            file?.let { updateProblemForFile(project, judgePanel, it) }
        }
    }

    private fun updateProblemForFile(project: Project, panel: CphJudgePanel, file: VirtualFile) {
        val problemService = ProblemService.getInstance(project)
        if (problemService.isSupportedFile(file)) {
            val problem = problemService.loadProblem(file.path)
            panel.setProblem(problem)
        } else {
            panel.setProblem(null)
        }
    }
}
