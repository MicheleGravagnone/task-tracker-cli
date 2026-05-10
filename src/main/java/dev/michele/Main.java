package dev.michele;

public class Main {

    public static void main(String[] args) {
        if (args.length == 0) { Terminal.help(); return; }

        TaskService service = new TaskService();

        try {
            switch (args[0]) {
                case "add"              -> { require(args, 2, "add <description>");
                                             service.add(args[1]); }
                case "update"           -> { require(args, 3, "update <id> <description>");
                                             service.update(parseInt(args[1]), args[2]); }
                case "delete"           -> { require(args, 2, "delete <id>");
                                             service.delete(parseInt(args[1])); }
                case "mark-in-progress" -> { require(args, 2, "mark-in-progress <id>");
                                             service.markInProgress(parseInt(args[1])); }
                case "mark-done"        -> { require(args, 2, "mark-done <id>");
                                             service.markDone(parseInt(args[1])); }
                case "list"             -> service.list(args.length > 1
                                             ? TaskStatus.from(args[1]) : null);
                default                 -> { Terminal.error("Unknown command: " + args[0]);
                                             Terminal.blank();
                                             Terminal.help(); }
            }
        } catch (IllegalArgumentException e) {
            Terminal.error(e.getMessage());
        } catch (RuntimeException e) {
            Terminal.error("Unexpected error: " + e.getMessage());
        }
    }

    private static void require(String[] args, int min, String usage) {
        if (args.length < min)
            throw new IllegalArgumentException("Usage: task-cli " + usage);
    }

    private static int parseInt(String s) {
        try { return Integer.parseInt(s); }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException("Expected a number, got: " + s);
        }
    }
}