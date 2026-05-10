package dev.michele;

import java.util.List;
import java.util.stream.Collectors;

public class TaskService {
    
    private final TaskRepository repo = new TaskRepository();

    public void add(String description) {
        List<Task> tasks = repo.loadAll();
        int nextId = tasks.stream()
                .mapToInt(Task::getId)
                .max()
                .orElse(0) + 1;
        Task task = new Task(nextId, description);
        tasks.add(task);
        repo.saveAll(tasks);
        Terminal.success("Task added successfully (ID: " + nextId + ")");
    }

    public void update(int id, String description) {
        List<Task> tasks = repo.loadAll();
        Task task = findById(tasks, id);
        task.setDescription(description);
        repo.saveAll(tasks);
        Terminal.success("Task " + id + " updated.");
    }

    public void delete(int id) {
        List<Task> tasks = repo.loadAll();
        boolean removed = tasks.removeIf(t -> t.getId() == id);
        if (!removed) throw new IllegalArgumentException("Task not found: " + id);
        repo.saveAll(tasks);
        Terminal.success("Task " + id + " deleted.");
    }

    public void markInProgress(int id) {
        setStatus(id, TaskStatus.IN_PROGRESS);
    }

    public void markDone(int id) {
        setStatus(id, TaskStatus.DONE);
    }

    private void setStatus(int id, TaskStatus status) {
        List<Task> tasks = repo.loadAll();
        Task task = findById(tasks, id);
        task.setStatus(status);
        repo.saveAll(tasks);
        Terminal.success("Task " + id + " marked as " + status + ".");
    }

    public void list(TaskStatus filter) {
        List<Task> tasks = repo.loadAll();
        List<Task> result = (filter == null)
            ? tasks
            : tasks.stream()
                    .filter(t -> t.getStatus() == filter)
                    .collect(Collectors.toList());

        String title = filter == null ? "All tasks" : "Tasks · " + filter;
        Terminal.header(title);
        Terminal.taskTable(result);
    }

    private Task findById(List<Task> tasks, int id) {
        return tasks.stream()
            .filter(t -> t.getId() == id)
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Task not found: " + id));
    }
}
