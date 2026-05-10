package dev.michele;

import java.time.Instant;

public class Task {
    private int id;
    private String description;
    private TaskStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    public Task(int id, String description) {
        this.id = id;
        this.description = description;
        this.status = TaskStatus.TODO;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public Task(int id, String description, TaskStatus status, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getId() {
        return this.id;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return this.status.toString();
    }

    public void setStatus(TaskStatus t) {
        this.status = t;
    }

    public void setStatus(String str) {
        this.status = TaskStatus.from(str);
    }

    public Instant getCreatedAt() {
        return this.createdAt;
    }

    public Instant getUpdatedAt() {
        return this.updatedAt;
    }

    @Override
    public String toString() {
        return String.format("[%d] %-12s %s (created: %s)", id, "[" + status + "]", description, createdAt);
    }
    
}
