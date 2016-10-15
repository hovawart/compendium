package com.nobodyelses.data.router;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.nobodyelses.data.utils.Utils;
import com.maintainer.data.provider.DataProvider;
import com.maintainer.data.provider.DataProviderFactory;
import com.maintainer.data.provider.datastore.MyMemcacheServiceFactory;
import com.nobodyelses.data.model.User;

@SuppressWarnings("serial")
public class ChangePasswordServlet extends SecureHttpServlet {

    @SuppressWarnings("unchecked")
    @Override
    protected void post(final HttpServletRequest req, final HttpServletResponse resp) throws Exception {
        final User user = getUser();
        if (user == null) throw new Exception("No logged in user.");

        final String oldPassword = req.getParameter("oldpassword");

        final boolean validatePassword = com.maintainer.util.Utils.validatePassword(oldPassword, user.getPassword());
        if (!validatePassword) throw new Exception("Password incorrect.");

        final String newPassword = req.getParameter("newpassword");
        final String confirmPassword = req.getParameter("confirmpassword");
        if (newPassword == null || confirmPassword == null || !newPassword.equals(confirmPassword)) throw new Exception("Password incorrect.");

        final String encrypt = com.maintainer.util.Utils.encrypt(newPassword);
        user.setPassword(encrypt);

        final DataProvider<User> users = (DataProvider<User>) DataProviderFactory.instance().getDataProvider(com.nobodyelses.data.model.User.class);
        users.put(user);

        final boolean delete = MyMemcacheServiceFactory.getMemcacheService().delete(user.getUsername());

        final String username = user.getUsername();

        final String credentials = Utils.formatRestletCredentials(username, newPassword.toCharArray());
        resp.addHeader("Set-Cookie", "Credentials=" + credentials);

        final String userIdCookie = Utils.getUserIdCookie(user.getKey().toString());
        resp.addHeader("Set-Cookie", userIdCookie);


        resp.setStatus(SecureHttpServlet.SUCCESS_OK);
        resp.setContentType(SecureHttpServlet.APPLICATION_JSON);
        resp.getWriter().write("{\"welcome\":\"" + username + "\"}");
    }
}
