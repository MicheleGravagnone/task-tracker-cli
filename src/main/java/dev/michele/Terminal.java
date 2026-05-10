package dev.michele;

public final class Terminal {
    static {
        enableWindowsAnsi();
    }

    private static void enableWindowsAnsi() {
        String os = System.getProperty("os.name", "").toLowerCase();
        if (!os.contains("win")) return;
        try {
            var kernel32Class = Class.forName("com.sun.jna.platform.win32.Kernel32");
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
        boolean isIde = System.getenv("IDEA_INITIAL_DIRECTORY") != null
                     || System.getenv("TERMINAL_EMULATOR") != null;
        COLOR = !isWindows || hasModernTerminal || isIde;
    }

    private static final String RESET  = "\033[0m";
    private static final String BOLD   = "\033[1m";
    private static final String DIM    = "\033[2m";

    private static final String FG_WHITE   = "\033[97m";
    private static final String FG_CYAN    = "\033[96m";
    private static final String FG_GREEN   = "\033[92m";
    private static final String FG_YELLOW  = "\033[93m";
    private static final String FG_RED     = "\033[91m";
    private static final String FG_BLUE    = "\033[94m";
    private static final String FG_MAGENTA = "\033[95m";
    private static final String FG_GRAY    = "\033[90m";

    private static final String BG_DARK    = "\033[48;5;235m";

    private static final String TL = "╭", TR = "╮", BL = "╰", BR = "╯";
    private static final String H  = "─", V  = "│";
    private static final String ML = "├", MR = "┤", MC = "┼", MT = "┬", MB = "┴";

    public static void banner() {
        System.out.println();
        System.out.println(FG_CYAN + BOLD +
            "  ████████╗ █████╗ ███████╗██╗  ██╗" + RESET);
        System.out.println(FG_CYAN + BOLD +
            "     ██╔══╝██╔══██╗██╔════╝██║ ██╔╝" + RESET);
        System.out.println(FG_CYAN + BOLD +
            "     ██║   ███████║███████╗█████╔╝ " + RESET);
        System.out.println(FG_CYAN + BOLD +
            "     ██║   ██╔══██║╚════██║██╔═██╗ " + RESET);
        System.out.println(FG_CYAN + BOLD +
            "     ██║   ██║  ██║███████║██║  ██╗" + RESET);
        System.out.println(FG_GRAY +
            "  Task Tracker CLI  ·  dev.michele" + RESET);
        System.out.println();
    }

    public static void header(String title) {
        int w = 48;
        String bar = H.repeat(w - 2);
        System.out.println(FG_CYAN + TL + bar + TR + RESET);
        String padded = pad(" " + title, w - 2);
        System.out.println(FG_CYAN + V + RESET + BOLD + FG_WHITE + padded + RESET + FG_CYAN + V + RESET);
        System.out.println(FG_CYAN + BL + bar + BR + RESET);
    }

    public static void success(String msg) {
        System.out.println(FG_GREEN + BOLD + "  ✓ " + RESET + FG_GREEN + msg + RESET);
    }

    public static void error(String msg) {
        System.out.println(FG_RED + BOLD + "  ✗ " + RESET + FG_RED + msg + RESET);
    }

    public static void info(String msg) {
        System.out.println(FG_BLUE + "  → " + RESET + msg);
    }

    public static void warn(String msg) {
        System.out.println(FG_YELLOW + "  ⚠ " + msg + RESET);
    }

    public static void dim(String msg) {
        System.out.println(FG_GRAY + DIM + "    " + msg + RESET);
    }

    public static void blank() {
        System.out.println();
    }

    public static void taskTable(java.util.List<Task> tasks) {
        if (tasks.isEmpty()) {
            warn("No tasks found.");
            return;
        }

        int wId   = 4;
        int wSt   = 13;
        int wDesc = 34;
        int wDate = 16;


        tableRow(TL, MT, MT, MT, TR, wId, wSt, wDesc, wDate);
        System.out.printf(FG_CYAN + V + RESET
            + " " + BOLD + FG_WHITE + "%-" + wId   + "s" + RESET
            + FG_CYAN + " " + V + RESET
            + " " + BOLD + FG_WHITE + "%-" + wSt   + "s" + RESET
            + FG_CYAN + " " + V + RESET
            + " " + BOLD + FG_WHITE + "%-" + wDesc + "s" + RESET
            + FG_CYAN + " " + V + RESET
            + " " + BOLD + FG_WHITE + "%-" + wDate + "s" + RESET
            + " " + FG_CYAN + V + RESET + "%n",
            "ID", "Status", "Description", "Updated");
        tableRow(ML, MC, MC, MC, MR, wId, wSt, wDesc, wDate);

        for (Task t : tasks) {
            String[] stColor = statusStyle(t.getStatus());
            String desc = truncate(t.getDescription(), wDesc);
            String date = formatInstant(t.getUpdatedAt());
            System.out.printf(FG_CYAN + V + RESET
                + " " + FG_MAGENTA + "%-" + wId + "s" + RESET
                + FG_CYAN + " " + V + RESET
                + " " + stColor[0] + "%-" + wSt + "s" + RESET
                + FG_CYAN + " " + V + RESET
                + " " + FG_WHITE + "%-" + wDesc + "s" + RESET
                + FG_CYAN + " " + V + RESET
                + " " + FG_GRAY + "%-" + wDate + "s" + RESET
                + " " + FG_CYAN + V + RESET + "%n",
                t.getId(), stColor[1] + t.getStatus(), desc, date);
        }

        tableRow(BL, MB, MB, MB, BR, wId, wSt, wDesc, wDate);
        blank();
        dim(tasks.size() + " task(s) shown");
        blank();
    }

    public static void help() {
        banner();
        header("Commands");
        blank();
        helpCmd("add <description>",         "Add a new task");
        helpCmd("update <id> <description>", "Update a task's description");
        helpCmd("delete <id>",               "Delete a task");
        helpCmd("mark-in-progress <id>",     "Mark a task as in progress");
        helpCmd("mark-done <id>",            "Mark a task as done");
        helpCmd("list",                      "List all tasks");
        helpCmd("list done",                 "List completed tasks");
        helpCmd("list todo",                 "List pending tasks");
        helpCmd("list in-progress",          "List in-progress tasks");
        blank();
        dim("Tasks are stored in ./tasks.json");
        blank();
    }

    private static void helpCmd(String cmd, String desc) {
        System.out.printf("  " + FG_CYAN + BOLD + "%-34s" + RESET + FG_GRAY + "%s" + RESET + "%n",
            cmd, desc);
    }

    private static void tableRow(String l, String lm, String rm, String mm, String r,
                                  int w1, int w2, int w3, int w4) {
        System.out.println(FG_CYAN
            + l + H.repeat(w1 + 2)
            + lm + H.repeat(w2 + 2)
            + mm + H.repeat(w3 + 2)
            + rm + H.repeat(w4 + 2)
            + r + RESET);
    }

    private static String[] statusStyle(TaskStatus s) {
        return switch (s) {
            case TODO        -> new String[]{ FG_GRAY,   "○ " };
            case IN_PROGRESS -> new String[]{ FG_YELLOW, "◐ " };
            case DONE        -> new String[]{ FG_GREEN,  "● " };
        };
    }

    private static String truncate(String s, int max) {
        if (s.length() <= max) return s;
        return s.substring(0, max - 1) + "…";
    }

    private static String pad(String s, int width) {
        if (s.length() >= width) return s.substring(0, width);
        return s + " ".repeat(width - s.length());
    }

    private static String formatInstant(java.time.Instant i) {
        java.time.LocalDateTime ldt = java.time.LocalDateTime
            .ofInstant(i, java.time.ZoneId.systemDefault());
        return String.format("%02d/%02d %02d:%02d",
            ldt.getDayOfMonth(), ldt.getMonthValue(),
            ldt.getHour(), ldt.getMinute());
    }
}