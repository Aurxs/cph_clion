# CPH Plugin for JetBrains IDEs - User Guide

This guide explains how to install and use the Competitive Programming Helper (CPH) plugin in JetBrains IDEs like CLion, IntelliJ IDEA, PyCharm, etc.

## Table of Contents

- [Installation](#installation)
- [Getting Started](#getting-started)
- [Using with Competitive Companion](#using-with-competitive-companion)
- [Using with Your Own Problems](#using-with-your-own-problems)
- [Keyboard Shortcuts](#keyboard-shortcuts)
- [Features](#features)
- [Configuration](#configuration)
- [Troubleshooting](#troubleshooting)

## Installation

### Installing from Disk (Recommended)

1. Download the latest plugin ZIP file from the [Releases page](https://github.com/Aurxs/cph_clion/releases)
2. Open your JetBrains IDE (CLion, IntelliJ IDEA, etc.)
3. Go to **Settings/Preferences** → **Plugins**
4. Click the **⚙️ (gear icon)** → **Install Plugin from Disk...**
5. Select the downloaded ZIP file
6. Restart the IDE when prompted

### Building from Source

1. Make sure you have JDK 17+ installed
2. Clone the repository:
   ```bash
   git clone https://github.com/Aurxs/cph_clion.git
   cd cph_clion/clion-plugin
   ```
3. Build the plugin:
   ```bash
   ./gradlew buildPlugin
   ```
4. The plugin ZIP file will be in `build/distributions/`
5. Install it using the "Install Plugin from Disk" method above

## Getting Started

After installation, you'll see a new **CPH Judge** tool window on the right side of your IDE.

1. **Open a project folder** in your IDE
2. **Create or open** a source code file (C++, C, Python, Java, etc.)
3. **Write your solution** or fetch problems using Competitive Companion
4. **Run test cases** using the keyboard shortcut or menu option

## Using with Competitive Companion

[Competitive Companion](https://github.com/jmerle/competitive-companion) is a browser extension that automatically fetches problem data and test cases from competitive programming websites.

### Setup

1. Install Competitive Companion in your browser:
   - [Chrome](https://chrome.google.com/webstore/detail/competitive-companion/cjnmckjndlpiamhfimnnjmnckgghkjbl)
   - [Firefox](https://addons.mozilla.org/en-US/firefox/addon/competitive-companion/)

2. Make sure your JetBrains IDE with CPH plugin is running

3. Visit any supported problem page (Codeforces, AtCoder, LeetCode, etc.)

4. Click the green **+** button in your browser toolbar

5. The problem will be automatically created in your IDE with test cases loaded

### Supported Platforms

- Codeforces
- AtCoder
- LeetCode
- CodeChef
- SPOJ
- HackerRank
- TopCoder
- And many more!

## Using with Your Own Problems

1. Open or create a source code file in a supported language
2. Press the **Run Testcases** shortcut or use the menu
3. The CPH Judge panel will open
4. Click **Add Test Case** to add your own test cases
5. Enter the input and expected output
6. Run the test cases

## Keyboard Shortcuts

### Windows/Linux

| Action | Shortcut |
|--------|----------|
| Run all test cases | <kbd>Ctrl</kbd>+<kbd>Alt</kbd>+<kbd>B</kbd> |
| Edit test cases | <kbd>Ctrl</kbd>+<kbd>Alt</kbd>+<kbd>E</kbd> |

### macOS

| Action | Shortcut |
|--------|----------|
| Run all test cases | <kbd>⌘</kbd>+<kbd>⌥</kbd>+<kbd>B</kbd> |
| Edit test cases | <kbd>⌘</kbd>+<kbd>⌥</kbd>+<kbd>E</kbd> |

You can also access these actions from:
- **Run** menu → CPH actions
- **Tools** menu → CPH submenu
- Right-click in the editor → Run Testcases

## Features

### Automatic Compilation

The plugin automatically compiles your code before running test cases:
- **C/C++**: Uses g++/gcc with customizable flags
- **Java**: Uses javac
- **Rust**: Uses rustc
- **Go**: Uses go build
- **Python/Ruby/JavaScript**: No compilation needed (interpreted)

### Test Case Judging

For each test case, the plugin shows:
- ✅ **Pass**: Output matches expected result
- ❌ **Fail**: Output differs from expected
- ⚠️ **Error**: Runtime error or crash
- ⏱️ **Timeout**: Execution exceeded time limit

### Output Comparison

The plugin compares your output with the expected output:
- Trailing whitespace is ignored
- Line endings are normalized (works across platforms)
- Line-by-line comparison for easy debugging

## Configuration

Open **Settings/Preferences** → **Tools** → **CPH Settings** to configure:

### General Settings

- **Save Location**: Where to save compiled binaries and test case files
- **Timeout**: Maximum execution time for each test case (default: 3000ms)
- **Hide stderr on success**: Don't show stderr if compilation succeeds
- **Default Language**: Language for new problems from Competitive Companion

### Compiler Settings

For each language, you can configure:
- **Compiler Command**: The compiler executable (e.g., g++, clang++)
- **Compiler Arguments**: Additional flags (e.g., -O2, -std=c++17)

### Example C++ Configuration

```
Compiler Command: g++
Compiler Arguments: -O2 -std=c++17 -Wall -Wextra
```

For macOS with Homebrew:
```
Compiler Command: /usr/local/bin/g++-13
Compiler Arguments: -O2 -std=c++17
```

## Troubleshooting

### Plugin not receiving problems from Competitive Companion

1. Make sure the IDE is running with the CPH plugin installed
2. Check that no firewall is blocking port 27121
3. Try restarting both the IDE and browser

### Compilation errors on macOS

If you get "command not found" errors:
1. Make sure you have the compiler installed (e.g., via Homebrew or Xcode Command Line Tools)
2. Configure the full path to the compiler in settings

For C++ with Homebrew:
```bash
brew install gcc
```
Then set compiler command to `/usr/local/bin/g++-13` (or your installed version - check with `brew list gcc`)

For Apple Silicon Macs (M1/M2/M3), the path is typically:
```
/opt/homebrew/bin/g++-13
```
(Replace `13` with your actual GCC version)

### Binary permission issues on macOS

If you get "permission denied" when running the compiled binary:
1. The plugin should automatically set executable permissions
2. If issues persist, check the save location has write permissions

### Test cases not loading

1. Make sure the problem file has the same base name as the source file
2. Check the save location in settings
3. Try creating a new local problem

## Supported Languages

| Language | File Extensions | Compiled |
|----------|-----------------|----------|
| C++ | .cpp, .cc, .cxx | Yes |
| C | .c | Yes |
| Python | .py | No |
| Java | .java | Yes |
| JavaScript | .js | No |
| Rust | .rs | Yes |
| Go | .go | Yes |
| Ruby | .rb | No |
| Haskell | .hs | Yes |
| C# | .cs | Yes |

## Getting Help

If you encounter issues or have suggestions:
- Open an issue on [GitHub](https://github.com/Aurxs/cph_clion/issues)
- Check existing issues for solutions
- Include your IDE version, OS, and plugin version when reporting bugs
