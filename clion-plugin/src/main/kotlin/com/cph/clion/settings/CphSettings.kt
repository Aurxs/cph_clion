package com.cph.clion.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

/**
 * Persistent settings for CPH plugin.
 * Settings are stored in the IDE configuration directory.
 */
@State(
    name = "CphSettings",
    storages = [Storage("cph-settings.xml")]
)
class CphSettings : PersistentStateComponent<CphSettings.State> {

    data class State(
        // General settings
        var saveLocation: String = "",
        var timeout: Int = 3000,
        var hideStderrWhenCompiledOK: Boolean = true,
        var ignoreStderr: Boolean = false,
        var defaultLanguage: String = "cpp",
        var autoShowJudge: Boolean = true,

        // C settings
        var cCommand: String = "gcc",
        var cArgs: String = "",
        var cOutputArg: String = "-o",

        // C++ settings
        var cppCommand: String = "g++",
        var cppArgs: String = "",
        var cppOutputArg: String = "-o",

        // Python settings
        var pythonCommand: String = "python3",
        var pythonArgs: String = "",

        // Java settings
        var javaCommand: String = "javac",
        var javaArgs: String = "",

        // Rust settings
        var rustCommand: String = "rustc",
        var rustArgs: String = "",

        // Go settings
        var goCommand: String = "go",
        var goArgs: String = "",

        // Ruby settings
        var rubyCommand: String = "ruby",
        var rubyArgs: String = "",

        // JavaScript settings
        var jsCommand: String = "node",
        var jsArgs: String = "",

        // Haskell settings
        var haskellCommand: String = "ghc",
        var haskellArgs: String = "",

        // C# settings
        var csharpCommand: String = "dotnet",
        var csharpArgs: String = "",

        // Short naming preferences
        var useShortCodeforcesName: Boolean = false,
        var useShortLuoguName: Boolean = false,
        var useShortAtCoderName: Boolean = false,

        // Template file location
        var defaultLanguageTemplateFileLocation: String = "",

        // Menu choices order
        var menuChoices: String = "cpp java js python c rust ruby csharp go haskell"
    )

    private var state = State()

    override fun getState(): State = state

    override fun loadState(state: State) {
        this.state = state
    }

    companion object {
        fun getInstance(): CphSettings {
            return ApplicationManager.getApplication().getService(CphSettings::class.java)
        }
    }

    fun getArgsForLanguage(extension: String): List<String> {
        val argsString = when (extension.lowercase()) {
            "c" -> state.cArgs
            "cpp", "cc", "cxx" -> state.cppArgs
            "py" -> state.pythonArgs
            "java" -> state.javaArgs
            "rs" -> state.rustArgs
            "go" -> state.goArgs
            "rb" -> state.rubyArgs
            "js" -> state.jsArgs
            "hs" -> state.haskellArgs
            "cs" -> state.csharpArgs
            else -> ""
        }
        return argsString.split(" ").filter { it.isNotEmpty() }
    }

    fun getCommandForLanguage(extension: String): String {
        return when (extension.lowercase()) {
            "c" -> state.cCommand
            "cpp", "cc", "cxx" -> state.cppCommand
            "py" -> state.pythonCommand
            "java" -> state.javaCommand
            "rs" -> state.rustCommand
            "go" -> state.goCommand
            "rb" -> state.rubyCommand
            "js" -> state.jsCommand
            "hs" -> state.haskellCommand
            "cs" -> state.csharpCommand
            else -> ""
        }
    }

    fun getOutputArgForLanguage(extension: String): String {
        return when (extension.lowercase()) {
            "c" -> state.cOutputArg
            "cpp", "cc", "cxx" -> state.cppOutputArg
            else -> "-o"
        }
    }
}
