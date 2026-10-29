package com.wiem.studyboard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.wiem.studyboard.model.Task;
import com.wiem.studyboard.service.TaskService;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class TaskServiceTest {

    @Test
    void addTrimsTitleAndAssignsIncreasingIds() {
        TaskService service = new TaskService();
        Task first = service.add("  Review Docker  ", "Lab", null);
        Task second = service.add("Read the course notes", "Course", LocalDate.of(2027, 1, 10));

        assertEquals("Review Docker", first.title());
        assertEquals(1, first.id());
        assertEquals(2, second.id());
        assertFalse(first.done());
    }

    @Test
    void addRejectsBlankTitle() {
        TaskService service = new TaskService();
        assertThrows(IllegalArgumentException.class, () -> service.add("   ", "Course", null));
        assertThrows(IllegalArgumentException.class, () -> service.add(null, "Course", null));
    }

    @Test
    void addRejectsTooLongTitle() {
        TaskService service = new TaskService();
        String tooLong = "x".repeat(TaskService.MAX_TITLE_LENGTH + 1);
        assertThrows(IllegalArgumentException.class, () -> service.add(tooLong, "Course", null));
    }

    @Test
    void addRejectsUnknownCategory() {
        TaskService service = new TaskService();
        assertThrows(IllegalArgumentException.class, () -> service.add("Task", "Unknown", null));
    }

    @Test
    void toggleFlipsStateAndReportsUnknownId() {
        TaskService service = new TaskService();
        Task task = service.add("Task", "Course", null);

        assertTrue(service.toggle(task.id()));
        assertTrue(service.all().get(0).done());
        assertTrue(service.toggle(task.id()));
        assertFalse(service.all().get(0).done());
        assertFalse(service.toggle(999));
    }

    @Test
    void deleteRemovesTask() {
        TaskService service = new TaskService();
        Task task = service.add("Task", "Course", null);

        assertTrue(service.delete(task.id()));
        assertTrue(service.all().isEmpty());
        assertFalse(service.delete(task.id()));
    }

    @Test
    void statsComputesPercentage() {
        TaskService service = new TaskService();
        assertEquals(0, service.stats().percent());

        service.add("A", "Course", null);
        service.add("B", "Course", null);
        service.add("C", "Lab", null);
        Task last = service.add("D", "Project", null);
        service.toggle(last.id());

        TaskService.Stats stats = service.stats();
        assertEquals(4, stats.total());
        assertEquals(1, stats.done());
        assertEquals(3, stats.remaining());
        assertEquals(25, stats.percent());
    }

    @Test
    void allListsPendingTasksBeforeDoneOnesAndSortsByDueDate() {
        TaskService service = new TaskService();
        Task late = service.add("Late", "Course", LocalDate.of(2027, 3, 1));
        Task soon = service.add("Soon", "Course", LocalDate.of(2027, 1, 1));
        Task finished = service.add("Finished", "Course", LocalDate.of(2026, 12, 1));
        service.toggle(finished.id());

        List<Task> ordered = service.all();
        assertEquals(soon.id(), ordered.get(0).id());
        assertEquals(late.id(), ordered.get(1).id());
        assertEquals(finished.id(), ordered.get(2).id());
    }

    @Test
    void byCategoryFiltersAndFallsBackToAll() {
        TaskService service = new TaskService();
        service.add("Course 1", "Course", null);
        service.add("Lab 1", "Lab", null);

        assertEquals(1, service.byCategory("Lab").size());
        assertEquals(2, service.byCategory(null).size());
        assertEquals(2, service.byCategory("Unknown").size());
    }
}
