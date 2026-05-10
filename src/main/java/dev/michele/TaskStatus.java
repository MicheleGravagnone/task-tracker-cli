package dev.michele;

public enum TaskStatus {
    TODO, IN_PROGRESS, DONE;

    public static TaskStatus from(String str) {
        return switch(str.toLowerCase()) {
            case "todo"        -> TODO;
            case "in-progress" -> IN_PROGRESS;
            case "done"        -> DONE;
            default            -> throw new IllegalArgumentException("Undefined status" + str);
        };
    }

    @Override
    public String toString() {
        return switch(this) {
            case TODO        -> "todo";
            case IN_PROGRESS -> "in-progress";
            case DONE        -> "done";
        };
    }
    
}
