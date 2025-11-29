# CPH 插件 JetBrains IDE 使用指南

本指南介绍如何在 JetBrains IDE（如 CLion、IntelliJ IDEA、PyCharm 等）中安装和使用竞赛编程助手（CPH）插件。

## 目录

- [安装](#安装)
- [快速入门](#快速入门)
- [配合 Competitive Companion 使用](#配合-competitive-companion-使用)
- [使用本地题目](#使用本地题目)
- [键盘快捷键](#键盘快捷键)
- [功能特性](#功能特性)
- [配置设置](#配置设置)
- [提交到 Codeforces](#提交到-codeforces)
- [常见问题](#常见问题)

## 安装

### 从磁盘安装（推荐）

1. 从 [Releases 页面](https://github.com/Aurxs/cph_clion/releases) 下载最新的插件 ZIP 文件
2. 打开你的 JetBrains IDE（CLion、IntelliJ IDEA 等）
3. 进入 **Settings/Preferences** → **Plugins**
4. 点击 **⚙️（齿轮图标）** → **Install Plugin from Disk...**
5. 选择下载的 ZIP 文件
6. 按提示重启 IDE

### 从源码构建

1. 确保已安装 JDK 17+
2. 克隆仓库：
   ```bash
   git clone https://github.com/Aurxs/cph_clion.git
   cd cph_clion/clion-plugin
   ```
3. 构建插件：
   ```bash
   ./gradlew buildPlugin
   ```
4. 插件 ZIP 文件将在 `build/distributions/` 目录下
5. 使用上述「从磁盘安装」方法安装

## 快速入门

安装完成后，你会在 IDE 右侧看到新的 **CPH Judge** 工具窗口。

1. **打开项目文件夹**
2. **创建或打开**源代码文件（C++、C、Python、Java 等）
3. **编写解决方案**或使用 Competitive Companion 获取题目
4. **运行测试用例**使用快捷键或菜单选项

## 配合 Competitive Companion 使用

[Competitive Companion](https://github.com/jmerle/competitive-companion) 是一个浏览器扩展，可自动从竞赛编程网站获取题目数据和测试用例。

### 设置步骤

1. 在浏览器中安装 Competitive Companion：
   - [Chrome](https://chrome.google.com/webstore/detail/competitive-companion/cjnmckjndlpiamhfimnnjmnckgghkjbl)
   - [Firefox](https://addons.mozilla.org/en-US/firefox/addon/competitive-companion/)

2. 确保装有 CPH 插件的 JetBrains IDE 正在运行

3. 访问任意支持的题目页面（Codeforces、AtCoder、洛谷 等）

4. 点击浏览器工具栏中的绿色 **+** 按钮

5. 题目将自动在 IDE 中创建，测试用例自动加载

### 支持的平台

- Codeforces
- AtCoder
- LeetCode
- CodeChef
- 洛谷 (Luogu)
- HackerRank
- TopCoder
- 以及更多！

## 使用本地题目

1. 打开或创建支持语言的源代码文件
2. 按 **运行测试用例** 快捷键或使用菜单
3. CPH Judge 面板将打开
4. 点击 **添加测试用例** 添加你自己的测试用例
5. 输入测试输入和期望输出
6. 运行测试用例

## 键盘快捷键

### Windows/Linux

| 操作 | 快捷键 |
|------|--------|
| 运行所有测试用例 | <kbd>Ctrl</kbd>+<kbd>Alt</kbd>+<kbd>B</kbd> |
| 提交到 Codeforces | <kbd>Ctrl</kbd>+<kbd>Alt</kbd>+<kbd>S</kbd> |
| 编辑测试用例 | <kbd>Ctrl</kbd>+<kbd>Alt</kbd>+<kbd>E</kbd> |

### macOS

| 操作 | 快捷键 |
|------|--------|
| 运行所有测试用例 | <kbd>⌘</kbd>+<kbd>⌥</kbd>+<kbd>B</kbd> |
| 提交到 Codeforces | <kbd>⌘</kbd>+<kbd>⌥</kbd>+<kbd>S</kbd> |
| 编辑测试用例 | <kbd>⌘</kbd>+<kbd>⌥</kbd>+<kbd>E</kbd> |

你也可以通过以下方式访问这些操作：
- **Run** 菜单 → CPH 操作
- **Tools** 菜单 → CPH 子菜单
- 在编辑器中右键 → Run Testcases

## 功能特性

### 自动编译

插件在运行测试用例前自动编译代码：
- **C/C++**：使用 g++/gcc，支持自定义编译选项
- **Java**：使用 javac
- **Rust**：使用 rustc
- **Go**：使用 go build
- **Python/Ruby/JavaScript**：无需编译（解释型语言）

### 测试用例评判

对于每个测试用例，插件显示：
- ✅ **通过 (Pass)**：输出与期望结果匹配
- ❌ **失败 (Fail)**：输出与期望不符
- ⚠️ **错误 (Error)**：运行时错误或崩溃
- ⏱️ **超时 (Timeout)**：执行超过时间限制

### 输出比较

插件比较你的输出与期望输出：
- 忽略末尾空白字符
- 规范化行结束符（跨平台兼容）
- 逐行比较便于调试

## 配置设置

打开 **Settings/Preferences** → **Tools** → **CPH Settings** 进行配置：

### 通用设置

- **保存位置 (Save Location)**：保存编译后二进制文件和测试用例文件的位置
- **超时时间 (Timeout)**：每个测试用例的最大执行时间（默认：3000ms）
- **成功时隐藏 stderr**：如果编译成功则不显示 stderr
- **默认语言**：Competitive Companion 新题目的默认语言

### 编译器设置

对于每种语言，你可以配置：
- **编译器命令**：编译器可执行文件（如 g++、clang++）
- **编译器参数**：额外的编译选项（如 -O2、-std=c++17）
- **提交编译器**：Codeforces 提交时使用的编译器

### C++ 配置示例

```
编译器命令: g++
编译器参数: -O2 -std=c++17 -Wall -Wextra
```

macOS 使用 Homebrew：
```
编译器命令: /usr/local/bin/g++-13
编译器参数: -O2 -std=c++17
```

## 提交到 Codeforces

插件支持通过 [cph-submit](https://github.com/agrawal-d/cph-submit) 浏览器扩展提交解决方案到 Codeforces。

### 设置步骤

1. 安装 [cph-submit](https://github.com/agrawal-d/cph-submit) 浏览器扩展
2. 确保你已在浏览器中登录 Codeforces
3. 打开从 Codeforces 获取的题目
4. 按提交快捷键或使用菜单

### 提交流程

1. 按 <kbd>Ctrl</kbd>+<kbd>Alt</kbd>+<kbd>S</kbd>（Windows/Linux）或 <kbd>⌘</kbd>+<kbd>⌥</kbd>+<kbd>S</kbd>（macOS）
2. 在浏览器中点击 cph-submit 按钮
3. 解决方案将自动提交

## 常见问题

### 插件没有从 Competitive Companion 接收题目

1. 确保装有 CPH 插件的 IDE 正在运行
2. 检查防火墙没有阻止 27121 端口
3. 尝试重启 IDE 和浏览器

### macOS 上的编译错误

如果出现「command not found」错误：
1. 确保已安装编译器（通过 Homebrew 或 Xcode 命令行工具）
2. 在设置中配置编译器的完整路径

使用 Homebrew 安装 C++：
```bash
brew install gcc
```
然后将编译器命令设置为 `/usr/local/bin/g++-13`（或你的版本）

对于 Apple Silicon Mac (M1/M2/M3)：
```bash
brew install gcc
```
编译器路径通常是 `/opt/homebrew/bin/g++-13`

### macOS 上的二进制权限问题

如果运行编译后的二进制文件时出现「permission denied」：
1. 插件应该自动设置可执行权限
2. 如果问题持续，检查保存位置是否有写权限

### 测试用例不加载

1. 确保题目文件与源文件有相同的基本名称
2. 检查设置中的保存位置
3. 尝试创建新的本地题目

## 支持的语言

| 语言 | 文件扩展名 | 需要编译 |
|------|-----------|----------|
| C++ | .cpp, .cc, .cxx | 是 |
| C | .c | 是 |
| Python | .py | 否 |
| Java | .java | 是 |
| JavaScript | .js | 否 |
| Rust | .rs | 是 |
| Go | .go | 是 |
| Ruby | .rb | 否 |
| Haskell | .hs | 是 |
| C# | .cs | 是 |

## 获取帮助

如果遇到问题或有建议：
- 在 [GitHub](https://github.com/Aurxs/cph_clion/issues) 上提交 issue
- 查看现有 issue 寻找解决方案
- 报告 bug 时请包含 IDE 版本、操作系统和插件版本
