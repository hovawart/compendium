package com.nobodyelses.data.router;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.memcache.MemcacheService;
import com.nobodyelses.data.utils.Utils;
import com.maintainer.data.provider.DataProvider;
import com.maintainer.data.provider.DataProviderFactory;
import com.maintainer.data.provider.Query;
import com.maintainer.data.provider.datastore.MyMemcacheServiceFactory;
import com.nobodyelses.data.model.User;

public class RegisterServlet extends HttpServlet {

    @SuppressWarnings("unchecked")
    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        try {
            String username = req.getParameter("username");
            if (Utils.isEmpty(username)) throw new Exception("An email address must be provided.");
            if (!Utils.isValidEmailAddress(username)) throw new Exception("Email address must be a valid.");

            final String password = req.getParameter("password");
            if (Utils.isEmpty(password)) throw new Exception("A password must be provided.");

            final String confirmPassword = req.getParameter("confirmPassword");
            if (!password.equals(confirmPassword)) throw new Exception("Passwords must match.");

            final String hash = req.getParameter("hash");
            if (hash == null) throw new Exception("An invitation reference number is required.");

            final DataProvider<User> users = (DataProvider<User>) DataProviderFactory.instance().getDataProvider(User.class);
            Query q = new Query(User.class);
            q.filter("hash", hash);
            List<User> find = users.find(q);
            if (find.isEmpty()) throw new Exception("Invitation does not exists.");

            final User user = find.get(0);

            username = username.toLowerCase();

            q = new Query(User.class);
            q.filter("username", username);
            find = users.find(q);
            if (!find.isEmpty()) {
                final User existing = find.get(0);
                if (!user.getKey().equals(existing.getKey())) {
                    throw new Exception("Username already exists. Please try another.");
                }
            }

            final Date date = com.maintainer.util.Utils.now();

            user.setUsername(username);
            user.setPassword(com.maintainer.util.Utils.encrypt(password));
            user.setHash(null);
            user.setRegisteredDate(date);

            final User post = users.put(user);
            final MemcacheService cache = MyMemcacheServiceFactory.getMemcacheService();
            cache.delete(username);

            final String credentials = Utils.formatRestletCredentials(username, password.toCharArray());
            resp.addHeader("Set-Cookie", "Credentials=" + credentials);

            final String userIdCookie = Utils.getUserIdCookie(post.getKey().toString());
            resp.addHeader("Set-Cookie", userIdCookie);

            resp.setStatus(SecureHttpServlet.SUCCESS_OK);
            resp.setContentType(SecureHttpServlet.APPLICATION_JSON);
            resp.getWriter().write("{\"welcome\":\"" + username + "\"}");
        } catch (final Exception e) {
            SecureHttpServlet.sendError(resp, e);
        }
    }
}
