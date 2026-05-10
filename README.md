# task-tracker-cli

A command-line task manager built in Java 26 with a rich terminal interface, amber-and-slate color palette, due date tracking, and zero external dependencies. All persistence, JSON parsing, and terminal rendering is implemented from scratch.

---

## Features

- Add, update, and delete tasks from the command line
- Mark tasks as todo, in-progress, or done
- Set due dates with automatic overdue detection and day countdown
- Search tasks by description
- Full detail view for individual tasks
- Live stat summary above every task list (done, active, todo, overdue)
- Amber and slate color palette with ANSI rendering that degrades gracefully in unsupported terminals
- UTF-8 output enforced at the JVM level for consistent rendering on Windows
- Hand-written JSON serializer and parser with no external libraries of any kind
- Single self-contained JAR invoked via a thin shell wrapper

---

## Requirements

| Tool | Version |
|---|---|
| Java JDK | 26+ |
| Apache Maven | 3.9+ |

Recommended terminal on Windows: **Windows Terminal**.

---

## Installation

**Clone and build:**

```bash
git clone https://github.com/your-username/task-tracker-cli.git
cd task-tracker-cli
mvn package
```

**Register the command (Windows):**

```powershell
$projectDir = (Get-Location).Path
$current = [Environment]::GetEnvironmentVariable("Path", "User")
[Environment]::SetEnvironmentVariable("Path", "$current;$projectDir", "User")
```

Close and reopen PowerShell. You can now run `task-cli` from any directory.

**Register the command (Linux / macOS):**

```bash
chmod +x task-cli
mkdir -p ~/.local/bin
ln -sf "$(pwd)/task-cli" ~/.local/bin/task-cli
```

`chmod +x` is a one-time step that marks the script as executable.
You do not need this on Windows — `task-cli.cmd` is recognized automatically.

---

## Usage

```
task-cli <command> [arguments]
```

Running `task-cli` with no arguments prints the help screen.

### Commands

| Command | Description |
|---|---|
| `add "<description>" [--due yyyy-MM-dd]` | Add a new task with an optional due date |
| `update <id> "<description>"` | Update a task's description |
| `due <id> <yyyy-MM-dd>` | Set or update the due date on an existing task |
| `mark-in-progress <id>` | Mark a task as in progress |
| `mark-done <id>` | Mark a task as done |
| `delete <id>` | Delete a task permanently |
| `list` | List all tasks |
| `list todo` | List only pending tasks |
| `list in-progress` | List only active tasks |
| `list done` | List only completed tasks |
| `search <query>` | Search tasks by description (case-insensitive) |
| `show <id>` | Show full detail for a single task |

### Examples

```bash
task-cli add "Write integration tests" --due 2026-05-20
task-cli add "Fix the login bug"
task-cli mark-in-progress 2
task-cli list
task-cli search "login"
task-cli show 2
task-cli mark-done 2
task-cli delete 1
```

---

## Project Structure

```
task-tracker-cli/
├── pom.xml                     Maven build configuration
├── task-cli.cmd                Command wrapper (Windows)
├── task-cli                    Command wrapper (Linux / macOS)
└── src/main/java/dev/michele/
    ├── Main.java               Entry point and argument dispatch
    ├── Task.java               Task data model with due date support
    ├── TaskStatus.java         Enum: TODO, IN_PROGRESS, DONE
    ├── TaskRepository.java     Hand-written JSON serializer and parser
    ├── TaskService.java        Business logic layer
    └── Terminal.java           ANSI rendering, table drawing, color palette
```

---

## Architecture

The project follows a strict three-layer architecture with no layer reaching across its boundary.

```
CLI layer        Main.java
                   | parses positional arguments, dispatches commands
                   v
Service layer    TaskService.java
                   | applies business rules, calls Terminal for output
                   v
Storage layer    TaskRepository.java
                   | reads and writes tasks.json over UTF-8
                   v
                 tasks.json
```

**Main.java** reads positional arguments and routes each command to the correct service method. All error handling is centralized here.

**TaskService.java** contains all business logic: ID allocation, status transitions, due date validation, search filtering, and stat aggregation. It has no knowledge of JSON or ANSI codes.

**TaskRepository.java** is the only class that touches the filesystem. JSON serialization and parsing are implemented by hand using string manipulation and character-level scanning — no external libraries.

**Terminal.java** owns all output. It holds the ANSI escape code constants, draws the boxed five-column table, renders the stat summary line, and provides the detail view. The `c()` method wraps every escape code and returns an empty string when ANSI is not supported, so all output degrades cleanly to plain text.

---

## Data Storage

Tasks are stored as a JSON array in `tasks.json` in the working directory. The file is created on the first `add` command and is plain UTF-8 text.

```json
[
  {
    "id": 1,
    "description": "Write integration tests",
    "status": "in-progress",
    "createdAt": "2026-05-10T14:32:00Z",
    "updatedAt": "2026-05-10T15:01:00Z",
    "dueDate": "2026-05-20"
  },
  {
    "id": 2,
    "description": "Fix the login bug",
    "status": "done",
    "createdAt": "2026-05-10T14:35:00Z",
    "updatedAt": "2026-05-10T16:00:00Z",
    "dueDate": null
  }
]
```

The `dueDate` field is backwards compatible. Files written by earlier versions of the tool that do not contain the field are read without errors and the due date is treated as unset.

---

## Terminal Compatibility

| Terminal | Colors | Box drawing |
|---|---|---|
| Windows Terminal | Yes | Yes |
| PowerShell (in Windows Terminal) | Yes | Yes |
| VS Code integrated terminal | Yes | Yes |
| IntelliJ IDEA terminal | Yes | Yes |
| macOS Terminal | Yes | Yes |
| Linux (GNOME Terminal, Konsole) | Yes | Yes |
| Classic cmd.exe | No | Partial |

---

## Building

```bash
# Standard build
mvn package

# Clean build
mvn clean package

# Quiet build (suppresses plugin output)
mvn -q package
```

The wrapper scripts check for the JAR on every invocation and rebuild automatically if it is missing, so running any `task-cli` command after a source change is sufficient to trigger a rebuild.

---

## Troubleshooting

**`task-cli` is not recognized**
The project directory is not on your PATH. Re-run the registration step and open a new terminal.

**Box-drawing characters appear as garbage**
Switch to Windows Terminal and ensure `chcp 65001` is present in `task-cli.cmd`. The JVM flags `-Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8` in the wrapper enforce UTF-8 at the output stream level.

**Colors do not appear**
You are running in classic CMD. Switch to Windows Terminal or PowerShell. On Linux and macOS, verify that `$TERM` is set to a value such as `xterm-256color`.

**Tasks disappear between sessions**
The file is created in the current working directory. Always run `task-cli` from the same directory, or set a fixed path by editing the `FILE` constant in `TaskRepository.java` to an absolute path.

**`task-cli` says "Permission denied" on Linux or macOS**
The execute bit is not set on the wrapper script. Run `chmod +x task-cli`
from the project root and try again.