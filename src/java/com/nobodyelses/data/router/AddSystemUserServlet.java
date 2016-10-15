package com.nobodyelses.data.router;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.restlet.data.MediaType;
import org.restlet.data.Status;

import com.nobodyelses.data.utils.Utils;
import com.maintainer.data.provider.DataProvider;
import com.maintainer.data.provider.DataProviderFactory;
import com.nobodyelses.data.model.Application;
import com.nobodyelses.data.model.Function;
import com.nobodyelses.data.model.Role;
import com.nobodyelses.data.model.User;

public class AddSystemUserServlet extends HttpServlet {
    private static final long serialVersionUID = 3400744090343212203L;

    @SuppressWarnings("unchecked")
    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        try {
            User user = com.nobodyelses.data.utils.Utils.getSystemUser();
            if (user == null) {
                final DataProvider<User> users = (DataProvider<User>) DataProviderFactory.instance().getDataProvider(User.class);
                user = new User(Utils.SYSTEM_USERNAME, com.maintainer.util.Utils.encrypt("password"));
                users.post(user);
            }

            final DataProvider<?> data = DataProviderFactory.instance().getDefaultDataProvider();
            List<?> list = data.getAll(Application.class);

            Application application = null;
            if (list.isEmpty()) {
                application = new Application();
                application.setName("Basic Application");
                application = ((DataProvider<Application>) DataProviderFactory.instance().getDataProvider(Application.class)).post(application);
            }

            list = data.getAll(Function.class);
            Function function = null;
            if (list.isEmpty()) {
                function = new Function();
                function.setPath("**.**");
                function = ((DataProvider<Function>) DataProviderFactory.instance().getDataProvider(Function.class)).post(function);
            }

            list = data.getAll(Role.class);
            Role role = null;
            if (list.isEmpty()) {
                role = new Role();
                role.setName("Administrator");
                role.addUser(user);
                role.addFunction(function);
                role = ((DataProvider<Role>) DataProviderFactory.instance().getDataProvider(Role.class)).post(role);
            }

            final String key = user.getKey().toString();
            resp.setContentLength(key.length());
            resp.setContentType(MediaType.APPLICATION_JSON.getName());
            resp.setStatus(Status.SUCCESS_OK.getCode());

            resp.getWriter().write(key);
        } catch (final Exception e) {
            SecureHttpServlet.sendError(resp, e);
        }
    }
}
