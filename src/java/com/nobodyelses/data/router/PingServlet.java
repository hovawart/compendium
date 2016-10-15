package com.nobodyelses.data.router;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.nobodyelses.data.model.Ping;
import com.nobodyelses.data.model.User;
import com.maintainer.util.Utils;

@SuppressWarnings("serial")
public class PingServlet extends SecureHttpServlet {

    @Override
    protected void get(final HttpServletRequest req, final HttpServletResponse resp) throws Exception {
        final User user = getUser();
        if (user != null) {
            boolean isUser = getCookieIdentifier(req, "Credentials") != null;

            final Ping welcome = new Ping(user.getUsername(), isUser);

            final Gson gson = Utils.getGson();
            final String json = gson.toJson(welcome);
            sendJsonResponse(resp, json);
        }
    }
}
