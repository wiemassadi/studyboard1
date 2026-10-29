package com.wiem.studyboard.model;

import java.time.LocalDate;

/**
 * A study task.
 * The type is immutable: to change it, create a new instance.
 */
public record Task(int id, String title, String category, LocalDate dueDate, boolean done) {

    /** Returns a copy of the task with the "done" state flipped. */
    public Task toggled() {
        return new Task(id, title, category, dueDate, !done);
    }
}
