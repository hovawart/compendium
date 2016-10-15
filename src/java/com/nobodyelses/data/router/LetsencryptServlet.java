package com.nobodyelses.data.router;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LetsencryptServlet extends HttpServlet {

    public static final Map<String, String> challenges = new HashMap<String, String>();

    static {
        challenges.put("pY7mAjUEJs30aBW6kb0ycJOJ_VDXLgseVLxh24OiQwQ",
                "pY7mAjUEJs30aBW6kb0ycJOJ_VDXLgseVLxh24OiQwQ.ekJ-frnyFAhEBhFBGSGC6L_hCt59MMWw9PdqpubZ9j4");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!req.getRequestURI().startsWith("/.well-known/acme-challenge/")) {
            resp.sendError(404);
            return;
        }
        String id = req.getRequestURI().substring("/.well-known/acme-challenge/".length());
        if (!challenges.containsKey(id)) {
            resp.sendError(404);
            return;
        }
        resp.setContentType("text/plain");
        resp.getOutputStream().print(challenges.get(id));
    }
}