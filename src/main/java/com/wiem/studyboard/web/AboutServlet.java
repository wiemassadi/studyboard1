package com.wiem.studyboard.web;

import com.wiem.studyboard.AppInfo;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * "Deployment" page: proves which version is running and where.
 * After a git push, the build number and the container name change.
 */
@WebServlet(urlPatterns = "/about")
public class AboutServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String build = AppInfo.build();
        String buildLabel = "local".equals(build) ? "local (built outside Jenkins)" : "#" + build + " (Jenkins)";

        StringBuilder body = new StringBuilder();
        body.append("<p class=\"lead\">This page shows which version of the application is responding. ")
                .append("After every <code>git push</code>, the pipeline builds a new one: ")
                .append("the build number and the container change.</p>\n")
                .append("<table class=\"info\">\n")
                .append(row("Version", AppInfo.version()))
                .append(row("Build", buildLabel))
                .append(row("Built on", AppInfo.builtAt()))
                .append(row("Container (host)", AppInfo.hostname()))
                .append(row("Java", System.getProperty("java.version")))
                .append(row("Server time",
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))))
                .append("</table>\n");

        resp.setContentType("text/html;charset=UTF-8");
        resp.getWriter().write(Layout.page(req.getContextPath(), "about", "Deployment", body.toString()));
    }

    private static String row(String label, String value) {
        return "<tr><th scope=\"row\">" + Layout.escape(label) + "</th><td>"
                + Layout.escape(value) + "</td></tr>\n";
    }
}
