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

        // Codeforces submission compilers
        var cppSubmissionCompiler: String = "GNU G++17 7.3.0",
        var cSubmissionCompiler: String = "GNU GCC C11 5.1.0",
        var pythonSubmissionCompiler: String = "PyPy 3.10 (7.3.15, 64bit)",
        var javaSubmissionCompiler: String = "Java 11.0.6",
        var rustSubmissionCompiler: String = "Rust 1.75.0 (2021)",
        var goSubmissionCompiler: String = "Go 1.22.2",

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

    // Compiler ID mappings for Codeforces
    val compilerToId = mapOf(
        "GNU G++17 7.3.0" to 54,
        "GNU G++14 6.4.0" to 50,
        "GNU G++11 5.1.0" to 42,
        "GNU G++17 9.2.0 (64 bit, msys 2)" to 61,
        "GNU G++20 13.2 (64 bit, winlibs)" to 89,
        "GNU G++23 14.2 (64 bit, msys2)" to 91,
        "Microsoft Visual C++ 2017" to 59,
        "Microsoft Visual C++ 2010" to 2,
        "Clang++17 Diagnostics" to 52,
        "C# 8, .NET Core 3.1" to 65,
        "C# 10, .NET SDK 6.0" to 79,
        "C# Mono 6.8" to 9,
        "Java 21 64bit" to 87,
        "Java 11.0.6" to 60,
        "Java 8 32bit" to 36,
        "Node.js 15.8.0 (64bit)" to 55,
        "JavaScript V8 4.8.0" to 34,
        "PyPy 2.7.13 (7.3.0)" to 40,
        "PyPy 3.6.9 (7.3.0)" to 41,
        "PyPy 3.10 (7.3.15, 64bit)" to 70,
        "Python 3.13.2" to 31,
        "Python 2.7.18" to 7,
        "Ruby 3.2.2" to 67,
        "GNU GCC C11 5.1.0" to 43,
        "Go 1.22.2" to 32,
        "Rust 1.75.0 (2021)" to 75,
        "Haskell GHC 8.10.1" to 12
    )

    fun getCompilerId(compiler: String): Int {
        return compilerToId[compiler] ?: -1
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
