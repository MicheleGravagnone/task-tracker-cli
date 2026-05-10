package dev.michele;

import java.time.Instant;
import java.time.LocalDate;

public class Task {
    private int id;
    private String description;
    private TaskStatus status;
    private Instant createdAt;
    private Instant updatedAt;
    private LocalDate dueDate;

    public Task(int id, String description) {
        this(id, description, TaskStatus.TODO, Instant.now(), Instant.now(), null);
    }

    public Task(int id, String description, TaskStatus status, Instant createdAt, Instant updatedAt, LocalDate dueDate) {
        this.id          = id;
        this.description = description;
        this.status      = status;
        this.createdAt   = createdAt;
        this.updatedAt   = updatedAt;
        this.dueDate     = dueDate;
    }

    public int        getId()          { return id; }
    public String     getDescription() { return description; }
    public TaskStatus getStatus()      { return status; }
    public Instant    getCreatedAt()   { return createdAt; }
    public Instant    getUpdatedAt()   { return updatedAt; }
    public LocalDate  getDueDate()     { return dueDate; }

    public void setDescription(String description) { this.description = description; this.updatedAt = Instant.now(); }
    public void setDueDate(LocalDate d)            { this.dueDate = d;              this.updatedAt = Instant.now(); }

    public void setStatus(TaskStatus t) { this.status = t; this.updatedAt = Instant.now(); }
    public void setStatus(String str)   { this.status = TaskStatus.from(str); this.updatedAt = Instant.now(); }

    public boolean isOverdue() {
        return dueDate != null
            && status != TaskStatus.DONE
            && dueDate.isBefore(LocalDate.now());
    }

    public boolean isDueToday() {
        return dueDate != null
            && status != TaskStatus.DONE
            && dueDate.isEqual(LocalDate.now());
    }

    @Override
    public String toString() {
        return "[%d] %-12s %s (created: %s)".formatted(id, "[" + status + "]", description, createdAt);
    }
}