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

    private static String c(String code) {
        return COLOR ? code : "";
    }

    private static final String RESET  = "\033[0m";
    private static final String BOLD   = "\033[1m";
    private static final String DIM    = "\033[2m";

    private static final String FG_BORDER  = "\033[33m";
    private static final String FG_HEADER  = "\033[33m";
    private static final String FG_ID      = "\033[93m";
    private static final String FG_WHITE   = "\033[97m";
    private static final String FG_GRAY    = "\033[90m";
    private static final String FG_GREEN   = "\033[92m";
    private static final String FG_YELLOW  = "\033[93m";
    private static final String FG_RED     = "\033[91m";
    private static final String FG_BLUE    = "\033[94m";
    private static final String FG_TODO    = "\033[90m"; 

    private static final String TL = "╭", TR = "╮", BL = "╰", BR = "╯";
    private static final String H  = "─", V  = "│";
    private static final String ML = "├", MR = "┤", MC = "┼", MT = "┬", MB = "┴";

    public static void banner() {
        System.out.println();
        System.out.println(c(FG_BORDER) + c(BOLD) +
            "  ████████╗ █████╗ ███████╗██╗  ██╗" + c(RESET));
        System.out.println(c(FG_BORDER) + c(BOLD) +
            "     ██╔══╝██╔══██╗██╔════╝██║ ██╔╝" + c(RESET));
        System.out.println(c(FG_BORDER) + c(BOLD) +
            "     ██║   ███████║███████╗█████╔╝ " + c(RESET));
        System.out.println(c(FG_BORDER) + c(BOLD) +
            "     ██║   ██╔══██║╚════██║██╔═██╗ " + c(RESET));
        System.out.println(c(FG_BORDER) + c(BOLD) +
            "     ██║   ██║  ██║███████║██║  ██╗" + c(RESET));
        System.out.println(c(FG_GRAY) +
            "  Task Tracker CLI  ·  dev.michele" + c(RESET));
        System.out.println();
    }

    public static void header(String title) {
        int w = 48;
        String bar = H.repeat(w - 2);
        System.out.println(c(FG_HEADER) + TL + bar + TR + c(RESET));
        String padded = pad(" " + title, w - 2);
        System.out.println(c(FG_HEADER) + V + c(RESET) + c(BOLD) + c(FG_WHITE) + padded + c(RESET) + c(FG_HEADER) + V + c(RESET));
        System.out.println(c(FG_HEADER) + BL + bar + BR + c(RESET));
    }

    public static void success(String msg) {
        System.out.println(c(FG_GREEN) + c(BOLD) + "  + " + c(RESET) + c(FG_GREEN) + msg + c(RESET));
    }

    public static void error(String msg) {
        System.out.println(c(FG_RED) + c(BOLD) + "  x " + c(RESET) + c(FG_RED) + msg + c(RESET));
    }

    public static void info(String msg) {
        System.out.println(c(FG_BLUE) + "  > " + c(RESET) + msg);
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
        System.out.printf(c(FG_BORDER) + V + c(RESET)
            + " " + c(BOLD) + c(FG_WHITE) + "%-" + wId   + "s" + c(RESET)
            + c(FG_BORDER) + " " + V + c(RESET)
            + " " + c(BOLD) + c(FG_WHITE) + "%-" + wSt   + "s" + c(RESET)
            + c(FG_BORDER) + " " + V + c(RESET)
            + " " + c(BOLD) + c(FG_WHITE) + "%-" + wDesc + "s" + c(RESET)
            + c(FG_BORDER) + " " + V + c(RESET)
            + " " + c(BOLD) + c(FG_WHITE) + "%-" + wDate + "s" + c(RESET)
            + " " + c(FG_BORDER) + V + c(RESET) + "%n",
            "ID", "Status", "Description", "Updated");
        tableRow(ML, MC, MC, MC, MR, wId, wSt, wDesc, wDate);

        for (Task t : tasks) {
            String[] st = statusStyle(t.getStatus());
            String desc = truncate(t.getDescription(), wDesc);
            String date = formatInstant(t.getUpdatedAt());
            System.out.printf(c(FG_BORDER) + V + c(RESET)
                + " " + c(FG_ID) + "%-" + wId + "s" + c(RESET)
                + c(FG_BORDER) + " " + V + c(RESET)
                + " " + st[0] + "%-" + wSt + "s" + c(RESET)
                + c(FG_BORDER) + " " + V + c(RESET)
                + " " + c(FG_WHITE) + "%-" + wDesc + "s" + c(RESET)
                + c(FG_BORDER) + " " + V + c(RESET)
                + " " + c(FG_GRAY) + "%-" + wDate + "s" + c(RESET)
                + " " + c(FG_BORDER) + V + c(RESET) + "%n",
                t.getId(), st[1], desc, date);
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
        System.out.printf("  " + c(FG_BORDER) + c(BOLD) + "%-34s" + c(RESET) + c(FG_GRAY) + "%s" + c(RESET) + "%n",
            cmd, desc);
    }

    private static void tableRow(String l, String lm, String rm, String mm, String r,
                                  int w1, int w2, int w3, int w4) {
        System.out.println(c(FG_BORDER)
            + l  + H.repeat(w1 + 2)
            + lm + H.repeat(w2 + 2)
            + mm + H.repeat(w3 + 2)
            + rm + H.repeat(w4 + 2)
            + r  + c(RESET));
    }

    private static String[] statusStyle(TaskStatus s) {
        return switch (s) {
            case TODO        -> new String[]{ c(FG_TODO),   "- todo"       };
            case IN_PROGRESS -> new String[]{ c(FG_YELLOW), "~ in-progress" };
            case DONE        -> new String[]{ c(FG_GREEN),  "+ done"       };
        };
    }

    private static String truncate(String s, int max) {
        if (s.length() <= max) return s;
        return s.substring(0, max - 1) + "~";
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