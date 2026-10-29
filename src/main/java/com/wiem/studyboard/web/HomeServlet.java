package com.wiem.studyboard.web;

import com.wiem.studyboard.model.Task;
import com.wiem.studyboard.service.TaskService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/** Home page: overall progress and next tasks. */
@WebServlet(urlPatterns = "/home")
public class HomeServlet extends HttpServlet {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        TaskService service = TaskService.shared();
        TaskService.Stats stats = service.stats();
        String ctx = req.getContextPath();

        StringBuilder body = new StringBuilder();
        body.append("<section class=\"overview\">\n")
                .append("<div class=\"ring\" style=\"--pct:").append(stats.percent())
                .append("\" role=\"img\" aria-label=\"").append(stats.percent())
                .append("% of tasks completed\"><span class=\"ring-value\">")
                .append(stats.percent()).append("%</span></div>\n")
                .append("<div class=\"overview-text\">")
                .append("<p class=\"lead\">").append(stats.done()).append(" of ")
                .append(stats.total()).append(" tasks completed.</p>")
                .append("<p>").append(stats.remaining()).append(" remaining.</p>")
                .append("<a class=\"button\" href=\"").append(Layout.escape(ctx))
                .append("/tasks\">View my tasks</a></div>\n</section>\n");

        body.append("<h2>Up next</h2>\n");
        List<Task> pending = service.all().stream().filter(task -> !task.done()).limit(3).toList();
        if (pending.isEmpty()) {
            body.append("<p class=\"empty\">Everything is done. Add a task in \"My tasks\".</p>\n");
        } else {
            body.append("<ul class=\"task-list\">\n");
            for (Task task : pending) {
                body.append("<li class=\"task\"><div class=\"task-main\"><span class=\"task-title\">")
                        .append(Layout.escape(task.title())).append("</span><span class=\"task-meta\">")
                        .append(Layout.escape(task.category()));
                if (task.dueDate() != null) {
                    body.append(", due ").append(DATE.format(task.dueDate()));
                }
                body.append("</span></div></li>\n");
            }
            body.append("</ul>\n");
        }

        resp.setContentType("text/html;charset=UTF-8");
        resp.getWriter().write(Layout.page(ctx, "home", "Dashboard", body.toString()));
    }
}
