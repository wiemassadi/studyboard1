package com.wiem.studyboard.web;

import com.wiem.studyboard.model.Task;
import com.wiem.studyboard.service.TaskService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;

/**
 * "My tasks" page: list, category filter, add, change state, delete.
 * After each action (POST) we redirect to the page (GET) to avoid a double submission
 * if the user reloads the page.
 */
@WebServlet(urlPatterns = "/tasks")
public class TasksServlet extends HttpServlet {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String ctx = req.getContextPath();
        String category = req.getParameter("category");
        String error = req.getParameter("error");
        boolean filtered = category != null && TaskService.CATEGORIES.contains(category);
        List<Task> tasks = TaskService.shared().byCategory(category);

        StringBuilder body = new StringBuilder();
        if (error != null && !error.isBlank()) {
            body.append("<p class=\"error\" role=\"alert\">").append(Layout.escape(error)).append("</p>\n");
        }
        body.append(addForm(ctx));
        body.append(filters(ctx, filtered ? category : null));
        if (tasks.isEmpty()) {
            body.append("<p class=\"empty\">No tasks here. Add one with the form above.</p>\n");
        } else {
            body.append("<ul class=\"task-list\">\n");
            for (Task task : tasks) {
                body.append(row(ctx, task));
            }
            body.append("</ul>\n");
        }

        resp.setContentType("text/html;charset=UTF-8");
        resp.getWriter().write(Layout.page(ctx, "tasks", "My tasks", body.toString()));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String ctx = req.getContextPath();
        TaskService service = TaskService.shared();
        String action = req.getParameter("action");
        try {
            if ("add".equals(action)) {
                service.add(req.getParameter("title"), req.getParameter("category"),
                        parseDate(req.getParameter("due")));
            } else if ("toggle".equals(action)) {
                if (!service.toggle(parseId(req.getParameter("id")))) {
                    throw new IllegalArgumentException("Task not found.");
                }
            } else if ("delete".equals(action)) {
                if (!service.delete(parseId(req.getParameter("id")))) {
                    throw new IllegalArgumentException("Task not found.");
                }
            } else {
                throw new IllegalArgumentException("Unknown action.");
            }
            resp.sendRedirect(ctx + "/tasks");
        } catch (IllegalArgumentException e) {
            resp.sendRedirect(ctx + "/tasks?error="
                    + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8));
        }
    }

    private static LocalDate parseDate(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(raw);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("The date is invalid.");
        }
    }

    private static int parseId(String raw) {
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Task not found.");
        }
    }

    private static String addForm(String ctx) {
        StringBuilder sb = new StringBuilder();
        sb.append("<form class=\"add-form\" method=\"post\" action=\"").append(Layout.escape(ctx))
                .append("/tasks\">\n")
                .append("<input type=\"hidden\" name=\"action\" value=\"add\">\n")
                .append("<label>Title<input type=\"text\" name=\"title\" maxlength=\"")
                .append(TaskService.MAX_TITLE_LENGTH).append("\" required></label>\n")
                .append("<label>Category<select name=\"category\">");
        for (String category : TaskService.CATEGORIES) {
            sb.append("<option>").append(Layout.escape(category)).append("</option>");
        }
        sb.append("</select></label>\n")
                .append("<label>Due date<input type=\"date\" name=\"due\"></label>\n")
                .append("<button type=\"submit\">Add</button>\n</form>\n");
        return sb.toString();
    }

    private static String filters(String ctx, String active) {
        StringBuilder sb = new StringBuilder("<nav class=\"filters\" aria-label=\"Filter by category\">");
        sb.append(chip(ctx + "/tasks", "All", active == null));
        for (String category : TaskService.CATEGORIES) {
            sb.append(chip(ctx + "/tasks?category=" + URLEncoder.encode(category, StandardCharsets.UTF_8),
                    category, category.equals(active)));
        }
        return sb.append("</nav>\n").toString();
    }

    private static String chip(String href, String label, boolean active) {
        return "<a href=\"" + Layout.escape(href) + "\""
                + (active ? " class=\"active\" aria-current=\"true\"" : "")
                + ">" + Layout.escape(label) + "</a>";
    }

    private static String row(String ctx, Task task) {
        String action = Layout.escape(ctx) + "/tasks";
        StringBuilder sb = new StringBuilder();
        sb.append("<li class=\"task").append(task.done() ? " done" : "").append("\">\n")
                .append("<div class=\"task-main\"><span class=\"task-title\">")
                .append(Layout.escape(task.title())).append("</span><span class=\"task-meta\">")
                .append(Layout.escape(task.category()));
        if (task.dueDate() != null) {
            sb.append(", due ").append(DATE.format(task.dueDate()));
        }
        sb.append("</span></div>\n")
                .append(actionForm(action, "toggle", task.id(), task.done() ? "Reopen" : "Complete", "secondary"))
                .append(actionForm(action, "delete", task.id(), "Delete", "danger"))
                .append("</li>\n");
        return sb.toString();
    }

    private static String actionForm(String action, String name, int id, String label, String css) {
        return "<form method=\"post\" action=\"" + action + "\">"
                + "<input type=\"hidden\" name=\"action\" value=\"" + name + "\">"
                + "<input type=\"hidden\" name=\"id\" value=\"" + id + "\">"
                + "<button type=\"submit\" class=\"" + css + "\">" + label + "</button></form>\n";
    }
}
