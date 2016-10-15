package com.nobodyelses.data.router;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.maintainer.data.provider.datastore.ThreadLocalCache;

public class ServletFilter implements Filter {

    @Override
    public void destroy() {

    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        ThreadLocalCache.clearCache();

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;
        response.setHeader("Access-Control-Allow-Origin", "*");
        if (request.getMethod().equals("OPTIONS")) {
            response.setHeader("Access-Control-Allow-Credentials", "true");
            response.setHeader("Access-Control-Allow-Methods", "POST, GET, HEAD, OPTIONS");
            response.setHeader("Access-Control-Allow-Headers", "Origin, Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers, Nobodyelses-Data-Connection");
            response.setHeader("Access-Control-Max-Age", "84600");
            response.setStatus(200);
        } else {
            chain.doFilter(req, resp);
        }

        ThreadLocalCache.clearCache();
    }

    @Override
    public void init(FilterConfig arg0) throws ServletException {

    }

}
