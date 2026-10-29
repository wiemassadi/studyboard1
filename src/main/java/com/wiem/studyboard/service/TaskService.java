package com.wiem.studyboard.service;

import com.wiem.studyboard.model.Task;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Business logic: manages the task list in memory.
 * Data is lost when the container restarts (no database, to keep deployment simple).
 */
public class TaskService {

    public static final List<String> CATEGORIES = List.of("Course", "Lab", "Project", "Exam");
    public static final int MAX_TITLE_LENGTH = 80;

    /** Pending tasks first, then by due date, then by id. */
    private static final Comparator<Task> ORDER = Comparator
            .comparing(Task::done)
            .thenComparing(Task::dueDate, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparingInt(Task::id);

    private static final TaskService SHARED = withSampleData();

    private final List<Task> tasks = new ArrayList<>();
    private int nextId = 1;

    /** Instance shared by all pages of the application. */
    public static TaskService shared() {
        return SHARED;
    }

    public static TaskService withSampleData() {
        TaskService service = new TaskService();
        LocalDate today = LocalDate.now();
        service.add("Review chapter 3 of Networks", "Course", today.plusDays(2));
        service.add("Finish the Docker lab", "Lab", today.plusDays(4));
        service.add("Prepare the CI/CD project demo", "Project", today.plusDays(7));
        return service;
    }

    public synchronized Task add(String title, String category, LocalDate dueDate) {
        String cleanTitle = title == null ? "" : title.trim();
        if (cleanTitle.isEmpty()) {
            throw new IllegalArgumentException("Title is required.");
        }
        if (cleanTitle.length() > MAX_TITLE_LENGTH) {
            throw new IllegalArgumentException(
                    "Title must not exceed " + MAX_TITLE_LENGTH + " characters.");
        }
        if (!CATEGORIES.contains(category)) {
            throw new IllegalArgumentException("Unknown category.");
        }
        Task task = new Task(nextId++, cleanTitle, category, dueDate, false);
        tasks.add(task);
        return task;
    }

    /** Flips a task's state. Returns false if the id does not exist. */
    public synchronized boolean toggle(int id) {
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).id() == id) {
                tasks.set(i, tasks.get(i).toggled());
                return true;
            }
        }
        return false;
    }

    /** Deletes a task. Returns false if the id does not exist. */
    public synchronized boolean delete(int id) {
        return tasks.removeIf(task -> task.id() == id);
    }

    public synchronized List<Task> all() {
        return tasks.stream().sorted(ORDER).toList();
    }

    /** Filters by category; a null or unknown category returns all tasks. */
    public synchronized List<Task> byCategory(String category) {
        if (category == null || !CATEGORIES.contains(category)) {
            return all();
        }
        return tasks.stream().filter(task -> task.category().equals(category)).sorted(ORDER).toList();
    }

    public synchronized Stats stats() {
        int done = (int) tasks.stream().filter(Task::done).count();
        return new Stats(tasks.size(), done);
    }

    public record Stats(int total, int done) {
        public int remaining() {
            return total - done;
        }

        public int percent() {
            return total == 0 ? 0 : Math.round(100f * done / total);
        }
    }
}
