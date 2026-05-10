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

    int getId() {
        return this.id;
    }

    String getDescription() {
        return this.description;
    }

    void setDescription(String description) {
        this.description = description;
    }

    String getStatus() {
        return this.status.toString();
    }

    void setStatus(String str) {
        this.status = TaskStatus.from(str);
    }
    
}
