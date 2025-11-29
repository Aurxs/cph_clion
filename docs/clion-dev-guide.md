# CLion Plugin Development Guide

This document contains the developer guide for the CPH CLion/IntelliJ plugin.

## Project Structure

```
clion-plugin/
├── build.gradle.kts        # Gradle build configuration
├── settings.gradle.kts     # Gradle settings
├── gradle/wrapper/         # Gradle wrapper
├── src/main/
│   ├── kotlin/com/cph/clion/
│   │   ├── actions/        # IDE Actions (run tests, submit, etc.)
│   │   ├── models/         # Data models (Problem, TestCase, etc.)
│   │   ├── services/       # Core services (Compiler, Judge, etc.)
│   │   ├── settings/       # Plugin settings
│   │   └── ui/             # UI components (Tool Window, Panels)
│   └── resources/
│       ├── META-INF/plugin.xml  # Plugin configuration
│       └── icons/           # Plugin icons
```

## Building

### Prerequisites

- JDK 17 or higher
- Gradle 8.5+ (included via wrapper)

### Build Commands

```bash
# Build the plugin
./gradlew buildPlugin

# Run in a sandbox IDE instance
./gradlew runIde

# Clean build artifacts
./gradlew clean
```

The built plugin will be in `build/distributions/`.

## Architecture

### Services

1. **CompanionServerService**: HTTP server listening on port 27121 for Competitive Companion browser extension. Application-level service.

2. **ProblemService**: Project-level service for managing problems - loading, saving, creating local problems.

3. **CompilerService**: Compiles source code using configured compilers.

4. **JudgeService**: Runs test cases against compiled programs and compares output.

### Data Models

- **Problem**: Competitive programming problem with metadata and test cases
- **TestCase**: Individual test case with input and expected output
- **RunResult**: Result of running a test case

### UI Components

- **CphJudgePanel**: Main panel displaying test cases and results
- **CphToolWindowFactory**: Factory for creating the tool window

### Actions

- **RunTestCasesAction**: Run all test cases (Ctrl+Alt+B)
- **SubmitToCodeforcesAction**: Submit to Codeforces (Ctrl+Alt+S)
- **EditTestCasesAction**: Create/edit test cases

## Adding New Features

### Adding a New Language

1. Update `LanguageExtensions` in `Models.kt` to add the extension mapping
2. Add compiler settings in `CphSettings.kt`
3. Add compilation logic in `CompilerService.kt`
4. Add execution logic in `JudgeService.kt`

### Adding a New Action

1. Create a new class extending `AnAction` in `actions/`
2. Register the action in `plugin.xml`
3. Add keyboard shortcuts if needed

## Testing

Currently, the plugin needs to be tested manually using `./gradlew runIde`. Unit tests can be added using the IntelliJ Platform testing framework.

## Publishing

1. Build the plugin: `./gradlew buildPlugin`
2. The ZIP file in `build/distributions/` can be installed via:
   - Settings → Plugins → ⚙️ → Install Plugin from Disk...

For JetBrains Marketplace publishing, you'll need to:
1. Create an account on JetBrains Marketplace
2. Set up signing keys
3. Configure `signPlugin` and `publishPlugin` tasks in build.gradle.kts
