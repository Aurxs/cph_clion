package com.cph.clion.models

/**
 * Represents a single test case with input and expected output.
 */
data class TestCase(
    val id: Long = System.currentTimeMillis(),
    val input: String = "",
    val output: String = ""
)

/**
 * Represents a competitive programming problem.
 * This matches the format used by Competitive Companion browser extension.
 */
data class Problem(
    val name: String = "",
    val url: String = "",
    val interactive: Boolean = false,
    val memoryLimit: Int = 256,
    val timeLimit: Int = 2000,
    val group: String = "",
    val tests: MutableList<TestCase> = mutableListOf(),
    var srcPath: String = "",
    val local: Boolean = false
)

/**
 * Result of running a single test case.
 */
data class RunResult(
    val id: Long,
    val pass: Boolean?,
    val stdout: String,
    val stderr: String,
    val exitCode: Int?,
    val signal: String?,
    val time: Long,
    val timeout: Boolean
)

/**
 * Represents a test case with its run result.
 */
data class Case(
    val id: Long,
    val testcase: TestCase,
    var result: RunResult? = null
)

/**
 * Language configuration for compilation and execution.
 */
data class Language(
    val name: String,
    val compiler: String,
    val args: List<String>,
    val skipCompile: Boolean
)

/**
 * Supported file extensions and their language mappings.
 */
object LanguageExtensions {
    val extensions = mapOf(
        "c" to "c",
        "cpp" to "cpp",
        "cc" to "cpp",
        "cxx" to "cpp",
        "cs" to "csharp",
        "py" to "python",
        "rb" to "ruby",
        "rs" to "rust",
        "java" to "java",
        "js" to "js",
        "go" to "go",
        "hs" to "haskell"
    )

    val supportedExtensions = listOf(
        "py", "cpp", "cc", "cxx", "rs", "c", "java", "js", "go", "hs", "rb", "cs"
    )

    val skipCompile = listOf("py", "js", "rb")

    fun isSupported(extension: String): Boolean {
        return supportedExtensions.contains(extension.lowercase())
    }

    fun getLanguageName(extension: String): String? {
        return extensions[extension.lowercase()]
    }

    fun shouldSkipCompile(extension: String): Boolean {
        return skipCompile.contains(extension.lowercase())
    }
}
