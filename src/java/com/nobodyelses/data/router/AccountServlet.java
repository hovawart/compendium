package com.nobodyelses.data.router;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.nobodyelses.data.utils.Utils;
import com.nobodyelses.data.model.User;

@SuppressWarnings("serial")
public class AccountServlet extends SecureHttpServlet {
    @Override
    protected void get(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        User user = getUser();
        user.setAccountNumber(user.getKey().getId().toString());
        String json = Utils.getGson().toJson(user);
        sendJsonResponse(resp, json);
    }
}
