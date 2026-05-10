# task-tracker-cli

A command-line task manager written in Java 26 with a rich terminal interface,
interactive keyboard navigation, and zero external dependencies.

---

## Table of Contents

- [Requirements](#requirements)
- [Installation](#installation)
  - [1. Install Java 26](#1-install-java-26)
  - [2. Install Maven](#2-install-maven)
  - [3. Clone the repository](#3-clone-the-repository)
  - [4. Build the project](#4-build-the-project)
  - [5. Register the command](#5-register-the-command)
- [Usage](#usage)
  - [Adding tasks](#adding-tasks)
  - [Listing tasks](#listing-tasks)
  - [Interactive mode](#interactive-mode)
  - [Updating tasks](#updating-tasks)
  - [Changing status](#changing-status)
  - [Deleting tasks](#deleting-tasks)
- [Project Structure](#project-structure)
- [Architecture](#architecture)
- [Data Storage](#data-storage)
- [Terminal Compatibility](#terminal-compatibility)
- [Building from Source](#building-from-source)
- [Troubleshooting](#troubleshooting)

---

## Requirements

| Requirement | Version  | Notes                                      |
|-------------|----------|--------------------------------------------|
| Java JDK    | 26+      | Must be on PATH as `java`                  |
| Apache Maven| 3.9+     | Must be on PATH as `mvn`                   |
| OS          | Windows, Linux, macOS | Windows Terminal recommended on Windows |

---

## Installation

### 1. Install Java 26

Download the JDK from the official OpenJDK builds:

---

### 2. Install Maven

---

### 3. Clone the repository

```bash
git clone https://github.com/your-username/task-tracker-cli.git
cd task-tracker-cli
```

---

### 4. Build the project

From the project root (where `pom.xml` is located):

```bash
mvn package
```

Maven will compile the source files and produce a self-contained JAR at
`target/task-cli.jar`. The first build downloads Maven plugins and may take
a minute; subsequent builds are fast.

To verify the JAR runs correctly before registering the command:

```bash
java -jar target/task-cli.jar
```

You should see the help screen with the list of available commands.

---

### 5. Register the command

This step makes `task-cli` available from any directory.

**Windows:**

Add the project root to your user PATH so that Windows can find `task-cli.cmd`:

```powershell
$projectDir = (Get-Location).Path
$current = [Environment]::GetEnvironmentVariable("Path", "User")
[Environment]::SetEnvironmentVariable("Path", "$current;$projectDir", "User")
```

Close and reopen PowerShell. You can now run `task-cli` from any directory.

**Linux / macOS:**

```bash
chmod +x task-cli
mkdir -p ~/.local/bin
ln -sf "$(pwd)/task-cli" ~/.local/bin/task-cli
```

If `~/.local/bin` is not already on your PATH, add this line to `~/.bashrc`
or `~/.zshrc`:

```bash
export PATH="$HOME/.local/bin:$PATH"
```

Then reload your shell:

```bash
source ~/.bashrc   # or source ~/.zshrc
```

---

## Usage

All commands follow the pattern:

```
task-cli <command> [arguments]
```

Running `task-cli` with no arguments prints the help screen.

---

### Adding tasks

```
task-cli add "<description>"
```

Always quote descriptions that contain spaces.

```
task-cli add "Write the project README"
task-cli add "Fix the login bug"
task-cli add "Review pull request #42"
```

Each task is assigned an integer ID automatically. IDs are never reused.

---

### Listing tasks

Print all tasks as a formatted table:

```
task-cli list
```

Filter by status:

```
task-cli list todo
task-cli list in-progress
task-cli list done
```

---

### Interactive mode

Running `task-cli list` without a filter argument enters interactive mode.
The task grid is rendered in the terminal and you can navigate it with the keyboard.

```
task-cli list
```

Key bindings in interactive mode:

| Key        | Action                          |
|------------|---------------------------------|
| Up arrow   | Move selection up               |
| Down arrow | Move selection down             |
| Enter      | Open action menu for selected task |
| q / Esc   | Exit interactive mode           |

When you press Enter on a task, an action menu appears:

```
  mark done
  mark in-progress
  mark todo
  delete
  cancel
```

Navigate the menu with the arrow keys and confirm with Enter.

---

### Updating tasks

Change the description of an existing task:

```
task-cli update <id> "<new description>"
```

Example:

```
task-cli update 3 "Review pull request #42 and leave comments"
```

---

### Changing status

Mark a task as in progress:

```
task-cli mark-in-progress <id>
```

Mark a task as done:

```
task-cli mark-done <id>
```

Examples:

```
task-cli mark-in-progress 2
task-cli mark-done 2
```

---

### Deleting tasks

```
task-cli delete <id>
```

Example:

```
task-cli delete 5
```

Deletion is immediate and permanent. The ID is not reassigned.

---

## Project Structure

```
task-tracker-cli/
├── pom.xml                     Maven build configuration
├── task-cli                    Shell wrapper (Linux / macOS)
├── task-cli.cmd                Batch wrapper (Windows)
└── src/
    └── main/
        └── java/
            └── dev/
                └── michele/
                    ├── Main.java             Entry point, argument dispatch
                    ├── Task.java             Task data model
                    ├── TaskStatus.java       Enum: TODO, IN_PROGRESS, DONE
                    ├── TaskRepository.java   JSON read and write
                    ├── TaskService.java      Business logic
                    ├── Terminal.java         ANSI rendering, table drawing
                    ├── RawTerminal.java      Raw mode and keystroke input
                    └── InteractiveList.java  Interactive navigation loop
```

---

## Architecture

The project is organized in three layers:

```
CLI layer       Main.java
                  |
                  | parses arguments, dispatches commands
                  v
Service layer   TaskService.java
                  |
                  | loads, mutates, and saves tasks
                  | calls Terminal.java for output
                  v
Storage layer   TaskRepository.java
                  |
                  | reads and writes tasks.json
                  v
                tasks.json
```

**Main.java** is the entry point. It reads the positional arguments, routes to
the correct method in `TaskService`, and handles top-level errors.

**TaskService.java** contains all business logic: computing next IDs, filtering
tasks by status, and calling `Terminal` methods to display results. It has no
knowledge of JSON or terminal escape codes.

**TaskRepository.java** is the only class that touches the filesystem. It
contains a hand-written JSON serializer and parser so no external libraries
are needed. All file I/O uses `StandardCharsets.UTF_8` explicitly.

**Terminal.java** is the rendering layer. It holds all ANSI escape codes, draws
the boxed table, and exposes methods like `success()`, `error()`, `header()`,
and `taskTable()`. Colors degrade gracefully: the `c()` method returns an empty
string when the terminal does not support ANSI.

**RawTerminal.java** enables raw mode so keystrokes are delivered instantly
without line buffering. On Windows it calls PowerShell to toggle `kernel32`
console mode flags. On Unix it calls `stty raw -echo`.

**InteractiveList.java** is the navigation loop. It draws the grid, reads one
keystroke at a time, moves the highlight, and renders the action menu inline.
It redraws only the lines that changed, using ANSI cursor movement, so there
is no screen flicker.

---

## Data Storage

Tasks are stored in a file named `tasks.json` in the directory from which you
run `task-cli`. The file is created automatically on the first `add` command.

Example file:

```json
[
  {
    "id": 1,
    "description": "Write the project README",
    "status": "done",
    "createdAt": "2025-05-10T14:32:00Z",
    "updatedAt": "2025-05-10T15:01:00Z"
  },
  {
    "id": 2,
    "description": "Fix the login bug",
    "status": "in-progress",
    "createdAt": "2025-05-10T14:35:00Z",
    "updatedAt": "2025-05-10T14:35:00Z"
  }
]
```

The file is plain UTF-8 text and can be read or edited in any text editor.
If the file is corrupted or contains invalid JSON, the application will print
an error and exit. To recover, either fix the JSON manually or delete the file
(all tasks will be lost).

---

## Terminal Compatibility

| Terminal                   | Colors | Box drawing | Interactive mode |
|----------------------------|--------|-------------|------------------|
| Windows Terminal (wt.exe)  | Yes    | Yes         | Yes              |
| PowerShell (in WT)         | Yes    | Yes         | Yes              |
| IntelliJ IDEA terminal     | Yes    | Yes         | Yes              |
| VS Code integrated terminal| Yes    | Yes         | Yes              |
| Classic cmd.exe            | No     | Partial     | Limited          |
| macOS Terminal             | Yes    | Yes         | Yes              |
| Linux (gnome-terminal etc.)| Yes    | Yes         | Yes              |

The recommended terminal on Windows is **Windows Terminal**, which is available
from the Microsoft Store or at https://github.com/microsoft/terminal.

If you are running in an environment without ANSI support (classic CMD), colors
are automatically disabled and the table still renders correctly in plain text.

---

## Building from Source

To recompile after changing source files:

```bash
mvn package
```

To skip tests (there are none by default) and suppress most output:

```bash
mvn -q package
```

The wrapper scripts (`task-cli` / `task-cli.cmd`) automatically rebuild the JAR
if it is missing, so you can also just run `task-cli add "test"` after any
source change and the rebuild happens for you.

To clean all compiled output:

```bash
mvn clean
```

To clean and rebuild in one step:

```bash
mvn clean package
```

---

## Troubleshooting

**`task-cli` is not recognized as a command**

The project directory is not on your PATH. Re-run the PATH setup step from the
Installation section and open a new terminal window.

**Box-drawing characters appear as question marks or garbage**

Your terminal is not using UTF-8. On Windows, make sure you are using Windows
Terminal. In classic CMD, `task-cli.cmd` runs `chcp 65001` automatically, but
some very old systems may still have issues. Switch to Windows Terminal.

**Status symbols appear as `?`**

The JVM is using a non-UTF-8 output encoding. The `task-cli.cmd` wrapper passes
`-Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8` to the JVM. If you are running
the JAR directly, add those flags:

```bash
java -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -jar target/task-cli.jar
```

**Colors do not appear**

You are likely running in classic CMD. Switch to Windows Terminal or PowerShell.
On Linux or macOS, make sure the `TERM` environment variable is set:

```bash
echo $TERM
# Should print something like xterm-256color
```

**`mvn` is not recognized**

Maven is not on your PATH. Repeat step 2 of the Installation section and open
a new terminal.

**`java -version` shows the wrong version**

Multiple JDK versions are installed. Set `JAVA_HOME` to the JDK 26 directory
and make sure its `bin` folder appears before other Java installations in PATH.

**The tasks.json file is not found**

`tasks.json` is created in the current working directory when you run the first
`add` command. If your tasks seem to disappear between sessions, check that you
are always running `task-cli` from the same directory, or move to a fixed
directory before running any commands.
