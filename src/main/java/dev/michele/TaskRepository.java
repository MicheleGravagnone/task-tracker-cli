package dev.michele;

import java.io.*;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;

public class TaskRepository {
    
    private static final String FILE = "tasks.json";

    public List<Task> loadAll() {
        Path path = Path.of(FILE);
        if (!Files.exists(path)) return new ArrayList<>();

        try {
            String json = Files.readString(path);
            return parseJson(json);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read " + FILE, e);
        }
    }

    public void saveAll(List<Task> tasks) {
        String json = toJson(tasks);
        try {
            Files.writeString(Path.of(FILE), json);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write" + FILE, e);
        }
    }

    private String toJson(List<Task> tasks) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < tasks.size(); i++) {
            Task t = tasks.get(i);
            sb.append("  {\n");
            sb.append("    \"id\": ").append(t.getId()).append(",\n");
            sb.append("    \"description\": \"").append(escape(t.getDescription())).append("\",\n");
            sb.append("    \"status\": \"").append(t.getStatus()).append("\",\n");
            sb.append("    \"createdAt\": \"").append(t.getCreatedAt()).append("\",\n");
            sb.append("    \"updatedAt\": \"").append(t.getUpdatedAt()).append("\"\n");
            sb.append("  }");
            if (i < tasks.size() - 1) sb.append(",");
            sb.append("\n");
        }

        return sb.toString();
    }

    private String escape(String str) {
        return str.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private List<Task> parseJson(String json) {
        List<Task> tasks = new ArrayList<>();

        String[] objects = json.split("\\{");
        for (String obj : objects) {
            if (!obj.contains("\"id\"")) continue;
            int id = Integer.parseInt(field(obj, "id"));
            String desc = field(obj, "description");
            TaskStatus ts = TaskStatus.from(field(obj, "status"));
            Instant ca = Instant.parse(field(obj, "createdAt"));
            Instant ua = Instant.parse(field(obj, "updatedAt"));
            tasks.add(new Task(id, desc, ts, ca, ua));
        }

        return tasks;
    }

    private String field(String obj, String name) {

    }
}
