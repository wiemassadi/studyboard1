package com.wiem.studyboard.web;

import com.wiem.studyboard.AppInfo;
import java.util.List;

/**
 * Template shared by all pages: side navigation, title, build info.
 * Each page only provides its own content (the "body").
 */
public final class Layout {

    private record NavItem(String key, String path, String label) {
    }

    private static final List<NavItem> NAV = List.of(
            new NavItem("home", "/home", "Home"),
            new NavItem("tasks", "/tasks", "My tasks"),
            new NavItem("about", "/about", "Deployment"));

    private Layout() {
    }

    /** Neutralises special HTML characters to prevent XSS attacks. */
    public static String escape(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(value.length() + 16);
        for (char c : value.toCharArray()) {
            switch (c) {
                case '&' -> sb.append("&amp;");
                case '<' -> sb.append("&lt;");
                case '>' -> sb.append("&gt;");
                case '"' -> sb.append("&quot;");
                case '\'' -> sb.append("&#39;");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }

    public static String page(String contextPath, String active, String title, String body) {
        String ctx = escape(contextPath);
        StringBuilder html = new StringBuilder(4096);
        html.append("<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n")
                .append("<meta charset=\"UTF-8\">\n")
                .append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n")
                .append("<title>").append(escape(title)).append(" | StudyBoard</title>\n")
                .append("<link rel=\"stylesheet\" href=\"").append(ctx).append("/css/style.css\">\n")
                .append("</head>\n<body>\n")
                .append("<aside class=\"sidebar\">\n")
                .append("<a class=\"brand\" href=\"").append(ctx).append("/home\">StudyBoard</a>\n")
                .append("<nav aria-label=\"Main navigation\">\n");
        for (NavItem item : NAV) {
            boolean current = item.key().equals(active);
            html.append("<a href=\"").append(ctx).append(item.path()).append("\"")
                    .append(current ? " class=\"active\" aria-current=\"page\"" : "")
                    .append(">").append(item.label()).append("</a>\n");
        }
        html.append("</nav>\n")
                .append("<p class=\"build\">Version ").append(escape(AppInfo.version()))
                .append("<br>Build ").append(escape(AppInfo.build())).append("</p>\n")
                .append("</aside>\n<main>\n<h1>").append(escape(title)).append("</h1>\n")
                .append(body)
                .append("\n</main>\n</body>\n</html>\n");
        return html.toString();
    }
}
