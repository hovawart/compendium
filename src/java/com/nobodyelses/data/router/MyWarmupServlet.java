package com.nobodyelses.data.router;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.maintainer.util.Utils;

public class MyWarmupServlet extends HttpServlet {
    private static final long serialVersionUID = -8675608138755240355L;
    private static final Logger log = Logger.getLogger(MyWarmupServlet.class.getName());

    @Override
    public void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        Utils.setDateSerializationFormat(new SimpleDateFormat());
    }
}
