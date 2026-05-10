package dev.michele;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

public class TaskService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final TaskRepository repo = new TaskRepository();

    public void add(String description, String dueDateStr) {
        List<Task> tasks = repo.loadAll();
        int nextId = tasks.stream().mapToInt(Task::getId).max().orElse(0) + 1;
        Task task = new Task(nextId, description);
        if (dueDateStr != null) task.setDueDate(parseDate(dueDateStr));
        tasks.add(task);
        repo.saveAll(tasks);
        Terminal.success("Task added (ID: " + nextId + ")  " + description);
        if (task.getDueDate() != null)
            Terminal.dim("Due: " + task.getDueDate());
    }

    public void update(int id, String description) {
        List<Task> tasks = repo.loadAll();
        findById(tasks, id).setDescription(description);
        repo.saveAll(tasks);
        Terminal.success("Task " + id + " updated.");
    }

    public void setDue(int id, String dueDateStr) {
        List<Task> tasks = repo.loadAll();
        findById(tasks, id).setDueDate(parseDate(dueDateStr));
        repo.saveAll(tasks);
        Terminal.success("Due date set to " + dueDateStr + " for task " + id + ".");
    }

    public void delete(int id) {
        List<Task> tasks = repo.loadAll();
        if (!tasks.removeIf(t -> t.getId() == id))
            throw new IllegalArgumentException("Task not found: " + id);
        repo.saveAll(tasks);
        Terminal.success("Task " + id + " deleted.");
    }

    public void markInProgress(int id) { setStatus(id, TaskStatus.IN_PROGRESS); }
    public void markDone(int id)       { setStatus(id, TaskStatus.DONE); }

    private void setStatus(int id, TaskStatus status) {
        List<Task> tasks = repo.loadAll();
        findById(tasks, id).setStatus(status);
        repo.saveAll(tasks);
        Terminal.success("Task " + id + " marked as " + status + ".");
    }

    public void list(TaskStatus filter) {
        List<Task> all    = repo.loadAll();
        List<Task> result = filter == null
            ? all
            : all.stream().filter(t -> t.getStatus() == filter).collect(Collectors.toList());

        String title = filter == null ? "All tasks" : "Tasks · " + filter;
        Terminal.header(title);
        Terminal.statSummary(all);
        Terminal.taskTable(result);
    }

    public void search(String query) {
        String q = query.toLowerCase();
        List<Task> all    = repo.loadAll();
        List<Task> result = all.stream()
            .filter(t -> t.getDescription().toLowerCase().contains(q))
            .collect(Collectors.toList());

        Terminal.header("Search · \"" + query + "\"");
        Terminal.statSummary(all);
        Terminal.taskTable(result);
    }

    public void show(int id) {
        List<Task> tasks = repo.loadAll();
        Task task = findById(tasks, id);
        Terminal.taskDetail(task);
    }

    private Task findById(List<Task> tasks, int id) {
        return tasks.stream()
            .filter(t -> t.getId() == id)
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Task not found: " + id));
    }

    private LocalDate parseDate(String s) {
        try {
            return LocalDate.parse(s, DATE_FMT);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                "Invalid date format: \"" + s + "\"  — expected yyyy-MM-dd  e.g. 2025-05-20");
        }
    }
}