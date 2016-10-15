package com.nobodyelses.data.router;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.nobodyelses.data.utils.Utils;

@SuppressWarnings("serial")
public class ClipServlet extends MyHttpServlet {
    @Override
    protected void get(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String title = req.getParameter("title");
        String url = req.getParameter("url");

        String template = Utils.getFileAsString("templates/clip.html");
        template = template.replace("{{title}}", title);
        template = template.replace("{{url}}", url);

        byte[] bytes = template.getBytes();
        resp.setContentType("text/html");
        resp.setContentLength(bytes.length);
        resp.getOutputStream().write(bytes);
    }
}
