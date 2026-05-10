package dev.michele;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public final class Terminal {

    static {
        enableWindowsAnsi();
    }

    private static void enableWindowsAnsi() {
        String os = System.getProperty("os.name", "").toLowerCase();
        if (!os.contains("win")) return;
        try {
            Class.forName("com.sun.jna.platform.win32.Kernel32");
        } catch (ClassNotFoundException ignored) {
        }
        System.out.print("\033[0m");
    }

    private static final boolean COLOR;
    static {
        String os = System.getProperty("os.name", "").toLowerCase();
        boolean isWindows = os.contains("win");
        boolean hasModernTerminal =
            System.getenv("WT_SESSION") != null ||
            System.getenv("TERM_PROGRAM") != null ||
            System.getenv("TERM") != null;
        boolean isIde =
            System.getenv("IDEA_INITIAL_DIRECTORY") != null ||
            System.getenv("TERMINAL_EMULATOR") != null;
        COLOR = !isWindows || hasModernTerminal || isIde;
    }

    private static String c(String code) { return COLOR ? code : ""; }

    private static final String RESET  = "\033[0m";
    private static final String BOLD   = "\033[1m";
    private static final String DIM    = "\033[2m";

    private static final String FG_BORDER = "\033[33m";
    private static final String FG_HEADER = "\033[33m";
    private static final String FG_ID     = "\033[93m";
    private static final String FG_WHITE  = "\033[97m";
    private static final String FG_GRAY   = "\033[90m";
    private static final String FG_GREEN  = "\033[92m";
    private static final String FG_YELLOW = "\033[93m";
    private static final String FG_RED    = "\033[91m";
    private static final String FG_BLUE   = "\033[94m";
    private static final String FG_TODO   = "\033[90m";

    private static final String TL = "╭", TR = "╮", BL = "╰", BR = "╯";
    private static final String H  = "─", V  = "│";
    private static final String ML = "├", MR = "┤", MC = "┼", MT = "┬", MB = "┴";

    private static final int wId   = 4;
    private static final int wSt   = 13;
    private static final int wDesc = 30;
    private static final int wDate = 11;
    private static final int wDue  = 14;

    public static void banner() {
        System.out.println();
        System.out.println(c(FG_BORDER) + c(BOLD) + "  ████████╗ █████╗ ███████╗██╗  ██╗" + c(RESET));
        System.out.println(c(FG_BORDER) + c(BOLD) + "     ██╔══╝██╔══██╗██╔════╝██║ ██╔╝" + c(RESET));
        System.out.println(c(FG_BORDER) + c(BOLD) + "     ██║   ███████║███████╗█████╔╝ " + c(RESET));
        System.out.println(c(FG_BORDER) + c(BOLD) + "     ██║   ██╔══██║╚════██║██╔═██╗ " + c(RESET));
        System.out.println(c(FG_BORDER) + c(BOLD) + "     ██║   ██║  ██║███████║██║  ██╗" + c(RESET));
        System.out.println(c(FG_GRAY)              + "  Task Tracker CLI  ·  dev.michele"  + c(RESET));
        System.out.println();
    }

    public static void header(String title) {
        int w = 48;
        System.out.println(c(FG_HEADER) + TL + H.repeat(w - 2) + TR + c(RESET));
        System.out.println(c(FG_HEADER) + V + c(RESET) + c(BOLD) + c(FG_WHITE) + pad(" " + title, w - 2) + c(RESET) + c(FG_HEADER) + V + c(RESET));
        System.out.println(c(FG_HEADER) + BL + H.repeat(w - 2) + BR + c(RESET));
    }

    public static void statSummary(List<Task> all) {
        if (all.isEmpty()) return;
        long done     = all.stream().filter(t -> t.getStatus() == TaskStatus.DONE).count();
        long active   = all.stream().filter(t -> t.getStatus() == TaskStatus.IN_PROGRESS).count();
        long todo     = all.stream().filter(t -> t.getStatus() == TaskStatus.TODO).count();
        long overdue  = all.stream().filter(Task::isOverdue).count();
        long dueToday = all.stream().filter(Task::isDueToday).count();

        StringBuilder sb = new StringBuilder("  ");
        sb.append(c(FG_GREEN) ).append(done  ).append(" done"  ).append(c(RESET));
        sb.append(c(FG_GRAY)  ).append("  ·  ").append(c(RESET));
        sb.append(c(FG_YELLOW)).append(active ).append(" active").append(c(RESET));
        sb.append(c(FG_GRAY)  ).append("  ·  ").append(c(RESET));
        sb.append(c(FG_GRAY)  ).append(todo   ).append(" todo"  ).append(c(RESET));
        if (overdue > 0) {
            sb.append(c(FG_GRAY)).append("  ·  ").append(c(RESET));
            sb.append(c(FG_RED)).append(c(BOLD)).append(overdue).append(" overdue").append(c(RESET));
        }
        if (dueToday > 0) {
            sb.append(c(FG_GRAY)).append("  ·  ").append(c(RESET));
            sb.append(c(FG_YELLOW)).append(dueToday).append(" due today").append(c(RESET));
        }
        System.out.println(sb);
        blank();
    }

    public static void taskTable(List<Task> tasks) {
        if (tasks.isEmpty()) {
            warn("No tasks found.");
            return;
        }

        tableRow5(TL, MT, MT, MT, MT, TR);
        System.out.printf(
            c(FG_BORDER) + V + c(RESET)
            + " " + c(BOLD) + c(FG_WHITE) + "%-" + wId   + "s" + c(RESET)
            + c(FG_BORDER) + " " + V + c(RESET)
            + " " + c(BOLD) + c(FG_WHITE) + "%-" + wSt   + "s" + c(RESET)
            + c(FG_BORDER) + " " + V + c(RESET)
            + " " + c(BOLD) + c(FG_WHITE) + "%-" + wDesc + "s" + c(RESET)
            + c(FG_BORDER) + " " + V + c(RESET)
            + " " + c(BOLD) + c(FG_WHITE) + "%-" + wDate + "s" + c(RESET)
            + c(FG_BORDER) + " " + V + c(RESET)
            + " " + c(BOLD) + c(FG_WHITE) + "%-" + wDue  + "s" + c(RESET)
            + " " + c(FG_BORDER) + V + c(RESET) + "%n",
            "ID", "Status", "Description", "Updated", "Due");
        tableRow5(ML, MC, MC, MC, MC, MR);

        for (Task t : tasks) {
            String[] st      = statusStyle(t.getStatus());
            String   desc    = truncate(t.getDescription(), wDesc);
            String   updated = formatDate(t.getUpdatedAt());
            String   due     = dueDateDisplay(t);
            String   dueCol  = dueDateColor(t);

            System.out.printf(
                c(FG_BORDER) + V + c(RESET)
                + " " + c(FG_ID)    + "%-" + wId   + "s" + c(RESET)
                + c(FG_BORDER) + " " + V + c(RESET)
                + " " + st[0]       + "%-" + wSt   + "s" + c(RESET)
                + c(FG_BORDER) + " " + V + c(RESET)
                + " " + c(FG_WHITE) + "%-" + wDesc + "s" + c(RESET)
                + c(FG_BORDER) + " " + V + c(RESET)
                + " " + c(FG_GRAY)  + "%-" + wDate + "s" + c(RESET)
                + c(FG_BORDER) + " " + V + c(RESET)
                + " " + dueCol      + "%-" + wDue  + "s" + c(RESET)
                + " " + c(FG_BORDER) + V + c(RESET) + "%n",
                t.getId(), st[1], desc, updated, due);
        }

        tableRow5(BL, MB, MB, MB, MB, BR);
        blank();
        dim(tasks.size() + " task(s) shown");
        blank();
    }

    public static void taskDetail(Task t) {
        int w = 52;
        System.out.println();
        System.out.println(c(FG_BORDER) + TL + H.repeat(w - 2) + TR + c(RESET));
        System.out.println(c(FG_BORDER) + V  + c(RESET) + c(BOLD) + c(FG_ID) + pad(" Task #" + t.getId(), w - 2) + c(RESET) + c(FG_BORDER) + V + c(RESET));
        System.out.println(c(FG_BORDER) + ML + H.repeat(w - 2) + MR + c(RESET));

        detailRow(w, "Description", t.getDescription(),               c(FG_WHITE));
        detailRow(w, "Status",      statusLabel(t.getStatus()),        statusColor(t.getStatus()));
        detailRow(w, "Created",     formatDateLong(t.getCreatedAt()),  c(FG_GRAY));
        detailRow(w, "Updated",     formatDateLong(t.getUpdatedAt()),  c(FG_GRAY));

        if (t.getDueDate() != null) {
            detailRow(w, "Due", t.getDueDate() + dueSuffix(t), dueDateColor(t));
        } else {
            detailRow(w, "Due", "not set", c(FG_GRAY));
        }

        System.out.println(c(FG_BORDER) + BL + H.repeat(w - 2) + BR + c(RESET));
        System.out.println();
    }

    public static void success(String msg) {
        System.out.println(c(FG_GREEN)  + c(BOLD) + "  + " + c(RESET) + c(FG_GREEN)  + msg + c(RESET));
    }

    public static void error(String msg) {
        System.out.println(c(FG_RED)    + c(BOLD) + "  x " + c(RESET) + c(FG_RED)    + msg + c(RESET));
    }

    public static void info(String msg) {
        System.out.println(c(FG_BLUE)   + "  > " + c(RESET) + msg);
    }

    public static void warn(String msg) {
        System.out.println(c(FG_YELLOW) + "  ! " + msg + c(RESET));
    }

    public static void dim(String msg) {
        System.out.println(c(FG_GRAY) + c(DIM) + "    " + msg + c(RESET));
    }

    public static void blank() {
        System.out.println();
    }

    public static void help() {
        banner();
        header("Commands");
        blank();
        helpCmd("add <description> [--due yyyy-MM-dd]", "Add a new task");
        helpCmd("update <id> <description>",            "Update description");
        helpCmd("due <id> <yyyy-MM-dd>",                "Set or update due date");
        helpCmd("delete <id>",                          "Delete a task");
        helpCmd("mark-in-progress <id>",                "Mark as in progress");
        helpCmd("mark-done <id>",                       "Mark as done");
        helpCmd("list",                                 "List all tasks");
        helpCmd("list done",                            "List completed tasks");
        helpCmd("list todo",                            "List pending tasks");
        helpCmd("list in-progress",                     "List in-progress tasks");
        helpCmd("search <query>",                       "Search by description");
        helpCmd("show <id>",                            "Show full task detail");
        blank();
        dim("Tasks are stored in ./tasks.json");
        blank();
    }

    public static void clearScreen() {
        try {
            if (System.console() == null && System.getenv("TERM") == null) return;
        } catch (Exception ignored) {}
        System.out.print("\033[2J\033[H");
        System.out.flush();
    }

    private static void helpCmd(String cmd, String desc) {
        System.out.printf("  " + c(FG_BORDER) + c(BOLD) + "%-44s" + c(RESET) + c(FG_GRAY) + "%s" + c(RESET) + "%n", cmd, desc);
    }

    private static void detailRow(int w, String label, String value, String valueColor) {
        int labelW   = 13;
        int valueMax = w - 2 - 2 - labelW;
        String content = c(FG_GRAY) + "  " + pad(label, labelW) + c(RESET)
                       + valueColor + truncate(value, valueMax) + c(RESET);
        int visual = 2 + labelW + Math.min(value.length(), valueMax);
        int fill   = Math.max(0, w - 2 - visual);
        System.out.println(c(FG_BORDER) + V + c(RESET) + content + " ".repeat(fill) + c(FG_BORDER) + V + c(RESET));
    }

    private static void tableRow5(String l, String c1, String c2, String c3, String c4, String r) {
        System.out.println(c(FG_BORDER)
            + l  + H.repeat(wId   + 2)
            + c1 + H.repeat(wSt   + 2)
            + c2 + H.repeat(wDesc + 2)
            + c3 + H.repeat(wDate + 2)
            + c4 + H.repeat(wDue  + 2)
            + r  + c(RESET));
    }

    private static String[] statusStyle(TaskStatus s) {
        return switch (s) {
            case TODO        -> new String[]{ c(FG_TODO),   "- todo"        };
            case IN_PROGRESS -> new String[]{ c(FG_YELLOW), "~ in-progress" };
            case DONE        -> new String[]{ c(FG_GREEN),  "+ done"        };
        };
    }

    private static String statusLabel(TaskStatus s) {
        return switch (s) {
            case TODO        -> "todo";
            case IN_PROGRESS -> "in-progress";
            case DONE        -> "done";
        };
    }

    private static String statusColor(TaskStatus s) {
        return switch (s) {
            case TODO        -> c(FG_TODO);
            case IN_PROGRESS -> c(FG_YELLOW);
            case DONE        -> c(FG_GREEN);
        };
    }

    private static String dueDateDisplay(Task t) {
        if (t.getDueDate() == null) return "-";
        if (t.getStatus() == TaskStatus.DONE) return t.getDueDate().toString();
        if (t.isOverdue()) {
            long days = ChronoUnit.DAYS.between(t.getDueDate(), LocalDate.now());
            return t.getDueDate() + " !" + days + "d";
        }
        if (t.isDueToday()) return "today";
        long days = ChronoUnit.DAYS.between(LocalDate.now(), t.getDueDate());
        return t.getDueDate() + " " + days + "d";
    }

    private static String dueDateColor(Task t) {
        if (t.getDueDate() == null || t.getStatus() == TaskStatus.DONE) return c(FG_GRAY);
        if (t.isOverdue())  return c(FG_RED)    + c(BOLD);
        if (t.isDueToday()) return c(FG_YELLOW) + c(BOLD);
        long days = ChronoUnit.DAYS.between(LocalDate.now(), t.getDueDate());
        if (days <= 3)      return c(FG_YELLOW);
        return c(FG_GRAY);
    }

    private static String dueSuffix(Task t) {
        if (t.getStatus() == TaskStatus.DONE) return "  (completed)";
        if (t.isOverdue()) {
            long d = ChronoUnit.DAYS.between(t.getDueDate(), LocalDate.now());
            return "  (overdue by " + d + " day" + (d == 1 ? "" : "s") + ")";
        }
        if (t.isDueToday()) return "  (due today)";
        long d = ChronoUnit.DAYS.between(LocalDate.now(), t.getDueDate());
        return "  (in " + d + " day" + (d == 1 ? "" : "s") + ")";
    }

    private static String truncate(String s, int max) {
        if (s == null || s.isEmpty()) return "";
        if (s.length() <= max) return s;
        return s.substring(0, max - 1) + "~";
    }

    private static String pad(String s, int width) {
        if (s.length() >= width) return s.substring(0, width);
        return s + " ".repeat(width - s.length());
    }

    private static String formatDate(java.time.Instant i) {
        java.time.LocalDateTime ldt = java.time.LocalDateTime.ofInstant(i, java.time.ZoneId.systemDefault());
        return "%02d/%02d %02d:%02d".formatted(ldt.getDayOfMonth(), ldt.getMonthValue(), ldt.getHour(), ldt.getMinute());
    }

    private static String formatDateLong(java.time.Instant i) {
        java.time.LocalDateTime ldt = java.time.LocalDateTime.ofInstant(i, java.time.ZoneId.systemDefault());
        return "%04d-%02d-%02d %02d:%02d".formatted(ldt.getYear(), ldt.getMonthValue(), ldt.getDayOfMonth(), ldt.getHour(), ldt.getMinute());
    }
}