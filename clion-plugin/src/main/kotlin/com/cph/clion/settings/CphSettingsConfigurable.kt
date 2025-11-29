package com.cph.clion.settings

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurableProvider
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.ui.components.JBCheckBox
import com.intellij.util.ui.FormBuilder
import javax.swing.*

/**
 * Settings UI configurable for CPH plugin.
 * Provides a form for configuring all CPH settings.
 */
class CphSettingsConfigurable : Configurable {

    private var settingsPanel: JPanel? = null
    
    // General settings
    private val saveLocationField = JBTextField()
    private val timeoutField = JBTextField()
    private val hideStderrCheckbox = JBCheckBox("Hide stderr when compilation succeeds")
    private val ignoreStderrCheckbox = JBCheckBox("Ignore stderr for testcase results")
    private val autoShowJudgeCheckbox = JBCheckBox("Auto-show judge panel when opening problem file")
    
    private val defaultLanguageCombo = ComboBox(arrayOf(
        "cpp", "c", "python", "java", "rust", "go", "ruby", "js", "haskell", "csharp"
    ))

    // C/C++ settings
    private val cCommandField = JBTextField()
    private val cArgsField = JBTextField()
    private val cppCommandField = JBTextField()
    private val cppArgsField = JBTextField()

    // Python settings
    private val pythonCommandField = JBTextField()
    private val pythonArgsField = JBTextField()

    // Java settings
    private val javaCommandField = JBTextField()
    private val javaArgsField = JBTextField()

    // Rust settings
    private val rustCommandField = JBTextField()
    private val rustArgsField = JBTextField()

    // Go settings
    private val goCommandField = JBTextField()
    private val goArgsField = JBTextField()

    // Short name preferences
    private val useShortCodeforcesNameCheckbox = JBCheckBox("Use short Codeforces problem names (e.g., 144C)")
    private val useShortLuoguNameCheckbox = JBCheckBox("Use short Luogu problem names (e.g., P1145)")
    private val useShortAtCoderNameCheckbox = JBCheckBox("Use short AtCoder problem names (e.g., abc123a)")

    // Template file location
    private val templateLocationField = JBTextField()

    override fun getDisplayName(): String = "CPH Settings"

    override fun createComponent(): JComponent {
        val settings = CphSettings.getInstance().state

        // Initialize fields with current values
        saveLocationField.text = settings.saveLocation
        timeoutField.text = settings.timeout.toString()
        hideStderrCheckbox.isSelected = settings.hideStderrWhenCompiledOK
        ignoreStderrCheckbox.isSelected = settings.ignoreStderr
        autoShowJudgeCheckbox.isSelected = settings.autoShowJudge
        defaultLanguageCombo.selectedItem = settings.defaultLanguage

        cCommandField.text = settings.cCommand
        cArgsField.text = settings.cArgs
        cppCommandField.text = settings.cppCommand
        cppArgsField.text = settings.cppArgs

        pythonCommandField.text = settings.pythonCommand
        pythonArgsField.text = settings.pythonArgs

        javaCommandField.text = settings.javaCommand
        javaArgsField.text = settings.javaArgs

        rustCommandField.text = settings.rustCommand
        rustArgsField.text = settings.rustArgs

        goCommandField.text = settings.goCommand
        goArgsField.text = settings.goArgs

        useShortCodeforcesNameCheckbox.isSelected = settings.useShortCodeforcesName
        useShortLuoguNameCheckbox.isSelected = settings.useShortLuoguName
        useShortAtCoderNameCheckbox.isSelected = settings.useShortAtCoderName

        templateLocationField.text = settings.defaultLanguageTemplateFileLocation

        settingsPanel = FormBuilder.createFormBuilder()
            // General settings
            .addSeparator()
            .addComponent(JBLabel("<html><b>General Settings</b></html>"))
            .addLabeledComponent("Save Location:", saveLocationField)
            .addTooltip("Location where generated .prob and binary files will be saved. Leave empty for source file directory.")
            .addLabeledComponent("Timeout (ms):", timeoutField)
            .addTooltip("Maximum time in milliseconds for each testcase execution.")
            .addLabeledComponent("Default Language:", defaultLanguageCombo)
            .addComponent(hideStderrCheckbox)
            .addComponent(ignoreStderrCheckbox)
            .addComponent(autoShowJudgeCheckbox)

            // Short name preferences
            .addSeparator()
            .addComponent(JBLabel("<html><b>Problem Naming</b></html>"))
            .addComponent(useShortCodeforcesNameCheckbox)
            .addComponent(useShortLuoguNameCheckbox)
            .addComponent(useShortAtCoderNameCheckbox)

            // Template
            .addSeparator()
            .addComponent(JBLabel("<html><b>Template</b></html>"))
            .addLabeledComponent("Template File Location:", templateLocationField)
            .addTooltip("Path to template file for new problems. Use CLASS_NAME placeholder for Java class names.")

            // C Settings
            .addSeparator()
            .addComponent(JBLabel("<html><b>C Settings</b></html>"))
            .addLabeledComponent("C Compiler Command:", cCommandField)
            .addLabeledComponent("C Compiler Args:", cArgsField)

            // C++ Settings
            .addSeparator()
            .addComponent(JBLabel("<html><b>C++ Settings</b></html>"))
            .addLabeledComponent("C++ Compiler Command:", cppCommandField)
            .addLabeledComponent("C++ Compiler Args:", cppArgsField)

            // Python Settings
            .addSeparator()
            .addComponent(JBLabel("<html><b>Python Settings</b></html>"))
            .addLabeledComponent("Python Command:", pythonCommandField)
            .addLabeledComponent("Python Args:", pythonArgsField)

            // Java Settings
            .addSeparator()
            .addComponent(JBLabel("<html><b>Java Settings</b></html>"))
            .addLabeledComponent("Java Compiler Command:", javaCommandField)
            .addLabeledComponent("Java Compiler Args:", javaArgsField)

            // Rust Settings
            .addSeparator()
            .addComponent(JBLabel("<html><b>Rust Settings</b></html>"))
            .addLabeledComponent("Rust Compiler Command:", rustCommandField)
            .addLabeledComponent("Rust Compiler Args:", rustArgsField)

            // Go Settings
            .addSeparator()
            .addComponent(JBLabel("<html><b>Go Settings</b></html>"))
            .addLabeledComponent("Go Command:", goCommandField)
            .addLabeledComponent("Go Args:", goArgsField)

            .addComponentFillVertically(JPanel(), 0)
            .panel

        return JScrollPane(settingsPanel).apply {
            border = null
        }
    }

    override fun isModified(): Boolean {
        val settings = CphSettings.getInstance().state
        return saveLocationField.text != settings.saveLocation ||
                timeoutField.text != settings.timeout.toString() ||
                hideStderrCheckbox.isSelected != settings.hideStderrWhenCompiledOK ||
                ignoreStderrCheckbox.isSelected != settings.ignoreStderr ||
                autoShowJudgeCheckbox.isSelected != settings.autoShowJudge ||
                defaultLanguageCombo.selectedItem != settings.defaultLanguage ||
                cCommandField.text != settings.cCommand ||
                cArgsField.text != settings.cArgs ||
                cppCommandField.text != settings.cppCommand ||
                cppArgsField.text != settings.cppArgs ||
                pythonCommandField.text != settings.pythonCommand ||
                pythonArgsField.text != settings.pythonArgs ||
                javaCommandField.text != settings.javaCommand ||
                javaArgsField.text != settings.javaArgs ||
                rustCommandField.text != settings.rustCommand ||
                rustArgsField.text != settings.rustArgs ||
                goCommandField.text != settings.goCommand ||
                goArgsField.text != settings.goArgs ||
                useShortCodeforcesNameCheckbox.isSelected != settings.useShortCodeforcesName ||
                useShortLuoguNameCheckbox.isSelected != settings.useShortLuoguName ||
                useShortAtCoderNameCheckbox.isSelected != settings.useShortAtCoderName ||
                templateLocationField.text != settings.defaultLanguageTemplateFileLocation
    }

    override fun apply() {
        val settings = CphSettings.getInstance().state
        settings.saveLocation = saveLocationField.text
        settings.timeout = timeoutField.text.toIntOrNull() ?: 3000
        settings.hideStderrWhenCompiledOK = hideStderrCheckbox.isSelected
        settings.ignoreStderr = ignoreStderrCheckbox.isSelected
        settings.autoShowJudge = autoShowJudgeCheckbox.isSelected
        settings.defaultLanguage = defaultLanguageCombo.selectedItem as String

        settings.cCommand = cCommandField.text
        settings.cArgs = cArgsField.text
        settings.cppCommand = cppCommandField.text
        settings.cppArgs = cppArgsField.text

        settings.pythonCommand = pythonCommandField.text
        settings.pythonArgs = pythonArgsField.text

        settings.javaCommand = javaCommandField.text
        settings.javaArgs = javaArgsField.text

        settings.rustCommand = rustCommandField.text
        settings.rustArgs = rustArgsField.text

        settings.goCommand = goCommandField.text
        settings.goArgs = goArgsField.text

        settings.useShortCodeforcesName = useShortCodeforcesNameCheckbox.isSelected
        settings.useShortLuoguName = useShortLuoguNameCheckbox.isSelected
        settings.useShortAtCoderName = useShortAtCoderNameCheckbox.isSelected

        settings.defaultLanguageTemplateFileLocation = templateLocationField.text
    }

    override fun reset() {
        val settings = CphSettings.getInstance().state
        saveLocationField.text = settings.saveLocation
        timeoutField.text = settings.timeout.toString()
        hideStderrCheckbox.isSelected = settings.hideStderrWhenCompiledOK
        ignoreStderrCheckbox.isSelected = settings.ignoreStderr
        autoShowJudgeCheckbox.isSelected = settings.autoShowJudge
        defaultLanguageCombo.selectedItem = settings.defaultLanguage

        cCommandField.text = settings.cCommand
        cArgsField.text = settings.cArgs
        cppCommandField.text = settings.cppCommand
        cppArgsField.text = settings.cppArgs

        pythonCommandField.text = settings.pythonCommand
        pythonArgsField.text = settings.pythonArgs

        javaCommandField.text = settings.javaCommand
        javaArgsField.text = settings.javaArgs

        rustCommandField.text = settings.rustCommand
        rustArgsField.text = settings.rustArgs

        goCommandField.text = settings.goCommand
        goArgsField.text = settings.goArgs

        useShortCodeforcesNameCheckbox.isSelected = settings.useShortCodeforcesName
        useShortLuoguNameCheckbox.isSelected = settings.useShortLuoguName
        useShortAtCoderNameCheckbox.isSelected = settings.useShortAtCoderName

        templateLocationField.text = settings.defaultLanguageTemplateFileLocation
    }
}
