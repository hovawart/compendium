package com.nobodyelses.data.router;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.restlet.ext.servlet.ServerServlet;

public class MyServerServlet extends ServerServlet {
    private static final Logger log = Logger.getLogger(MyServerServlet.class.getName());

    @Override
    public void init() throws ServletException {
        super.init();
    }

    @Override
    public void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        log.fine("service: " + request.getRequestURL());
        super.service(request, response);
    }
}
