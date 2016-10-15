package com.nobodyelses.data.controller;

import org.restlet.Response;
import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import com.maintainer.data.provider.datastore.MyMemcacheServiceFactory;
import com.maintainer.data.model.User;
import com.maintainer.data.provider.DataProvider;
import com.maintainer.data.provider.DataProviderFactory;
import com.maintainer.util.Utils;

public class ChangePasswordController extends ServerResource {
    @SuppressWarnings("unchecked")
    @Post
    public Representation changePassword(final Representation rep) throws Exception {
        try {
            final User user = Utils.getUser(getRequest());
            if (user == null) throw new Exception("No logged in user.");

            final Form form = new Form(getRequest().getEntity());

            final String oldPassword = form.getFirstValue("oldpassword");

            final boolean validatePassword = Utils.validatePassword(new String(oldPassword), user.getPassword());
            if (!validatePassword) throw new Exception("Password incorrect.");

            final String newPassword = form.getFirstValue("newpassword");
            final String confirmPassword = form.getFirstValue("confirmpassword");
            if (newPassword == null || confirmPassword == null || !newPassword.equals(confirmPassword)) throw new Exception("Password incorrect.");

            final String encrypt = Utils.encrypt(newPassword);
            user.setPassword(encrypt);

            final DataProvider<User> users = (DataProvider<User>) DataProviderFactory.instance().getDataProvider(com.nobodyelses.data.model.User.class);
            users.put(user);

            final boolean delete = MyMemcacheServiceFactory.getMemcacheService().delete(user.getUsername());

            final Response response = getResponse();
            final String credentials = com.nobodyelses.data.utils.Utils.formatRestletCredentials(user.getUsername(), newPassword.toCharArray());
            response.getCookieSettings().set("Credentials", credentials);
            response.setStatus(Status.SUCCESS_OK);

            final String message = "{\"welcome\": \"" + user.getUsername() + "\"}";
            return new JsonRepresentation(message);
        } catch (final Exception e) {
            getResponse().setStatus(Status.CLIENT_ERROR_UNAUTHORIZED, e.getMessage());
            return null;
        }
    }
}
