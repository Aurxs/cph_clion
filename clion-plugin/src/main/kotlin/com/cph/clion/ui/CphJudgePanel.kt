package com.cph.clion.ui

import com.cph.clion.models.Problem
import com.cph.clion.models.RunResult
import com.cph.clion.models.TestCase
import com.cph.clion.services.CompilerService
import com.cph.clion.services.JudgeService
import com.cph.clion.services.ProblemService
import com.cph.clion.settings.CphSettings
import com.intellij.icons.AllIcons
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import com.intellij.util.ui.JBUI
import java.awt.*
import javax.swing.*

/**
 * Main panel for the CPH Judge tool window.
 * Displays test cases and their results.
 */
class CphJudgePanel(private val project: Project) : SimpleToolWindowPanel(true, true) {

    private var problem: Problem? = null
    private val testCasePanels = mutableListOf<TestCasePanel>()
    private val mainPanel = JPanel()
    private val statusLabel = JBLabel("No problem loaded")
    private val runAllButton = JButton("Run All", AllIcons.Actions.Execute)
    private val addTestButton = JButton("Add Test", AllIcons.General.Add)
    private val deleteAllButton = JButton("Delete All", AllIcons.Actions.GC)
    private var isCompiling = false
    private var isRunning = false

    init {
        setupUI()
    }

    private fun setupUI() {
        // Toolbar panel
        val toolbarPanel = JPanel(FlowLayout(FlowLayout.LEFT)).apply {
            add(runAllButton)
            add(addTestButton)
            add(deleteAllButton)
            add(statusLabel)
        }

        runAllButton.addActionListener { runAllTests() }
        addTestButton.addActionListener { addTestCase() }
        deleteAllButton.addActionListener { deleteAllTestCases() }

        // Main content panel
        mainPanel.layout = BoxLayout(mainPanel, BoxLayout.Y_AXIS)
        mainPanel.border = JBUI.Borders.empty(10)

        val scrollPane = JBScrollPane(mainPanel)
        scrollPane.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
        scrollPane.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER

        // Layout
        layout = BorderLayout()
        add(toolbarPanel, BorderLayout.NORTH)
        add(scrollPane, BorderLayout.CENTER)

        showNoProblem()
    }

    /**
     * Load and display a problem.
     */
    fun setProblem(problem: Problem?) {
        this.problem = problem
        refreshUI()
    }

    /**
     * Refresh the UI to show current problem state.
     */
    private fun refreshUI() {
        mainPanel.removeAll()
        testCasePanels.clear()

        val currentProblem = problem
        if (currentProblem == null) {
            showNoProblem()
        } else {
            showProblem(currentProblem)
        }

        mainPanel.revalidate()
        mainPanel.repaint()
    }

    private fun showNoProblem() {
        statusLabel.text = "No problem loaded"
        runAllButton.isEnabled = false
        addTestButton.isEnabled = false
        deleteAllButton.isEnabled = false

        val noFilePanel = JPanel(GridBagLayout())
        noFilePanel.add(JBLabel("<html><center>" +
            "<h3>CPH Judge</h3>" +
            "<p>Open a source file to run testcases</p>" +
            "<p>or use Competitive Companion to load a problem</p>" +
            "</center></html>"))
        mainPanel.add(noFilePanel)
    }

    private fun showProblem(problem: Problem) {
        statusLabel.text = problem.name
        runAllButton.isEnabled = true
        addTestButton.isEnabled = true
        deleteAllButton.isEnabled = true

        // Problem info header
        val headerPanel = JPanel(BorderLayout()).apply {
            border = JBUI.Borders.emptyBottom(10)
            add(JBLabel("<html><b>${problem.name}</b></html>"), BorderLayout.WEST)
            if (!problem.local) {
                val urlButton = JButton("Open URL", AllIcons.Ide.External_link_arrow)
                urlButton.addActionListener {
                    java.awt.Desktop.getDesktop().browse(java.net.URI(problem.url))
                }
                add(urlButton, BorderLayout.EAST)
            }
        }
        mainPanel.add(headerPanel)

        // Test cases
        for (testCase in problem.tests) {
            val panel = TestCasePanel(testCase, problem.tests.indexOf(testCase) + 1)
            testCasePanels.add(panel)
            mainPanel.add(panel)
        }
    }

    /**
     * Run all test cases.
     */
    private fun runAllTests() {
        val currentProblem = problem ?: return
        if (isCompiling || isRunning) return

        // Save all documents first
        FileDocumentManager.getInstance().saveAllDocuments()

        isCompiling = true
        updateStatus("Compiling...")

        // Compile first
        CompilerService.compile(currentProblem.srcPath).thenAccept { result ->
            ApplicationManager.getApplication().invokeLater {
                isCompiling = false

                if (!result.success) {
                    updateStatus("Compilation failed")
                    showNotification("Compilation failed: ${result.errorOutput}", NotificationType.ERROR)
                    return@invokeLater
                }

                isRunning = true
                updateStatus("Running tests...")

                // Run all tests
                JudgeService.runAllTestCases(
                    currentProblem,
                    onProgress = { runResult ->
                        ApplicationManager.getApplication().invokeLater {
                            updateTestCaseResult(runResult)
                        }
                    },
                    onComplete = { results ->
                        ApplicationManager.getApplication().invokeLater {
                            isRunning = false
                            val passed = results.count { it.pass == true }
                            updateStatus("Done: $passed/${results.size} passed")
                        }
                    }
                )
            }
        }
    }

    /**
     * Run a single test case.
     */
    fun runSingleTest(testCase: TestCase) {
        val currentProblem = problem ?: return
        if (isCompiling || isRunning) return

        FileDocumentManager.getInstance().saveAllDocuments()

        isCompiling = true
        updateStatus("Compiling...")

        CompilerService.compile(currentProblem.srcPath).thenAccept { result ->
            ApplicationManager.getApplication().invokeLater {
                isCompiling = false

                if (!result.success) {
                    updateStatus("Compilation failed")
                    showNotification("Compilation failed: ${result.errorOutput}", NotificationType.ERROR)
                    return@invokeLater
                }

                updateStatus("Running test...")
                JudgeService.runTestCase(currentProblem.srcPath, testCase) { runResult ->
                    ApplicationManager.getApplication().invokeLater {
                        updateTestCaseResult(runResult)
                        val statusText = when {
                            runResult.pass == true -> "Passed"
                            runResult.pass == false -> if (runResult.timeout) "Timeout" else "Failed"
                            else -> "Error"
                        }
                        updateStatus(statusText)
                    }
                }
            }
        }
    }

    /**
     * Add a new test case.
     */
    private fun addTestCase() {
        val currentProblem = problem ?: return

        val newTestCase = TestCase(
            id = System.currentTimeMillis(),
            input = "",
            output = ""
        )
        currentProblem.tests.add(newTestCase)
        saveProblem()

        val panel = TestCasePanel(newTestCase, currentProblem.tests.size)
        testCasePanels.add(panel)
        mainPanel.add(panel)
        mainPanel.revalidate()
        mainPanel.repaint()
    }

    /**
     * Delete a test case.
     */
    fun deleteTestCase(testCase: TestCase) {
        val currentProblem = problem ?: return

        currentProblem.tests.removeIf { it.id == testCase.id }
        saveProblem()
        refreshUI()
    }

    /**
     * Delete all test cases.
     */
    private fun deleteAllTestCases() {
        val currentProblem = problem ?: return

        val confirm = JOptionPane.showConfirmDialog(
            this,
            "Delete all test cases for this problem?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION
        )

        if (confirm == JOptionPane.YES_OPTION) {
            currentProblem.tests.clear()
            saveProblem()
            refreshUI()
        }
    }

    /**
     * Update a test case's content.
     */
    fun updateTestCase(testCase: TestCase, input: String, output: String) {
        val currentProblem = problem ?: return
        val index = currentProblem.tests.indexOfFirst { it.id == testCase.id }
        if (index >= 0) {
            currentProblem.tests[index] = testCase.copy(input = input, output = output)
            saveProblem()
        }
    }

    /**
     * Update the result display for a test case.
     */
    private fun updateTestCaseResult(result: RunResult) {
        val panel = testCasePanels.find { it.testCase.id == result.id }
        panel?.setResult(result)
    }

    /**
     * Save the current problem.
     */
    private fun saveProblem() {
        val currentProblem = problem ?: return
        ProblemService.getInstance(project).saveProblem(currentProblem.srcPath, currentProblem)
    }

    /**
     * Update status label.
     */
    private fun updateStatus(text: String) {
        statusLabel.text = text
    }

    private fun showNotification(message: String, type: NotificationType) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("CPH Notifications")
            .createNotification(message, type)
            .notify(project)
    }

    /**
     * Panel for a single test case.
     */
    inner class TestCasePanel(val testCase: TestCase, private val index: Int) : JPanel() {

        private val inputArea = JBTextArea(testCase.input, 4, 30)
        private val expectedOutputArea = JBTextArea(testCase.output, 4, 30)
        private val actualOutputArea = JBTextArea(4, 30)
        private val statusLabel = JBLabel()
        private val timeLabel = JBLabel()

        init {
            setupUI()
        }

        private fun setupUI() {
            layout = BorderLayout()
            border = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(JBColor.border()),
                JBUI.Borders.empty(10)
            )

            // Header with buttons
            val headerPanel = JPanel(BorderLayout()).apply {
                add(JBLabel("<html><b>Test Case $index</b></html>"), BorderLayout.WEST)
                
                val buttonsPanel = JPanel(FlowLayout(FlowLayout.RIGHT, 5, 0))
                
                val runButton = JButton(AllIcons.Actions.Execute)
                runButton.toolTipText = "Run this test case"
                runButton.addActionListener { runSingleTest(testCase) }
                buttonsPanel.add(runButton)

                val deleteButton = JButton(AllIcons.Actions.Close)
                deleteButton.toolTipText = "Delete this test case"
                deleteButton.addActionListener { deleteTestCase(testCase) }
                buttonsPanel.add(deleteButton)

                add(buttonsPanel, BorderLayout.EAST)
            }

            // Input/Output panels
            val contentPanel = JPanel(GridLayout(1, 3, 10, 0))

            // Input panel
            val inputPanel = JPanel(BorderLayout()).apply {
                add(JBLabel("Input:"), BorderLayout.NORTH)
                inputArea.lineWrap = true
                inputArea.addFocusListener(object : java.awt.event.FocusAdapter() {
                    override fun focusLost(e: java.awt.event.FocusEvent?) {
                        updateTestCase(testCase, inputArea.text, expectedOutputArea.text)
                    }
                })
                add(JBScrollPane(inputArea), BorderLayout.CENTER)
            }

            // Expected output panel
            val expectedPanel = JPanel(BorderLayout()).apply {
                add(JBLabel("Expected Output:"), BorderLayout.NORTH)
                expectedOutputArea.lineWrap = true
                expectedOutputArea.addFocusListener(object : java.awt.event.FocusAdapter() {
                    override fun focusLost(e: java.awt.event.FocusEvent?) {
                        updateTestCase(testCase, inputArea.text, expectedOutputArea.text)
                    }
                })
                add(JBScrollPane(expectedOutputArea), BorderLayout.CENTER)
            }

            // Actual output panel
            val actualPanel = JPanel(BorderLayout()).apply {
                add(JBLabel("Actual Output:"), BorderLayout.NORTH)
                actualOutputArea.lineWrap = true
                actualOutputArea.isEditable = false
                actualOutputArea.background = JBColor.background()
                add(JBScrollPane(actualOutputArea), BorderLayout.CENTER)
            }

            contentPanel.add(inputPanel)
            contentPanel.add(expectedPanel)
            contentPanel.add(actualPanel)

            // Status bar
            val statusPanel = JPanel(BorderLayout()).apply {
                border = JBUI.Borders.emptyTop(5)
                add(statusLabel, BorderLayout.WEST)
                add(timeLabel, BorderLayout.EAST)
            }

            add(headerPanel, BorderLayout.NORTH)
            add(contentPanel, BorderLayout.CENTER)
            add(statusPanel, BorderLayout.SOUTH)

            maximumSize = Dimension(Int.MAX_VALUE, 250)
        }

        /**
         * Update the display with a test result.
         */
        fun setResult(result: RunResult) {
            actualOutputArea.text = result.stdout
            timeLabel.text = "${result.time}ms"

            when {
                result.timeout -> {
                    statusLabel.text = "⏱ TIMEOUT"
                    statusLabel.foreground = JBColor.ORANGE
                    border = createResultBorder(JBColor.ORANGE)
                }
                result.pass == true -> {
                    statusLabel.text = "✓ PASSED"
                    statusLabel.foreground = JBColor(Color(0, 150, 0), Color(0, 200, 0))
                    border = createResultBorder(JBColor(Color(0, 150, 0), Color(0, 200, 0)))
                }
                result.pass == false -> {
                    statusLabel.text = "✗ WRONG ANSWER"
                    statusLabel.foreground = JBColor.RED
                    border = createResultBorder(JBColor.RED)
                }
                else -> {
                    statusLabel.text = "⚠ ERROR (exit code: ${result.exitCode})"
                    statusLabel.foreground = JBColor.RED
                    border = createResultBorder(JBColor.RED)
                    if (result.stderr.isNotEmpty()) {
                        actualOutputArea.text = "STDERR:\n${result.stderr}\n\nSTDOUT:\n${result.stdout}"
                    }
                }
            }
        }

        private fun createResultBorder(color: JBColor): javax.swing.border.Border {
            return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 2),
                JBUI.Borders.empty(10)
            )
        }
    }
}
