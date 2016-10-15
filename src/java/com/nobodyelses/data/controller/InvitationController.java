package com.nobodyelses.data.controller;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import com.nobodyelses.data.router.InvitationServlet;
import com.maintainer.data.controller.Resource;
import com.maintainer.data.provider.DataProvider;
import com.maintainer.data.provider.DataProviderFactory;
import com.maintainer.data.provider.Query;
import com.maintainer.util.Utils;
import com.nobodyelses.data.model.User;

public class InvitationController extends ServerResource {

    @SuppressWarnings("unchecked")
    @Post
    public Representation invite(final Representation rep) throws Exception {
        try {
            final ArrayList<Resource> resources = Utils.getResources(getRequest());
            final Resource resource = resources.get(0);

            final String hash = resource.getProperty();

            if (Utils.isEmpty(hash)) throw new Exception("A hash must be provided.");

            final DataProvider<User> users = (DataProvider<User>) DataProviderFactory.instance().getDataProvider(User.class);
            final Query q = new Query(User.class);
            q.filter("hash", hash);
            final List<User> find = users.find(q);
            if (find.isEmpty()) throw new Exception("Invitation does not exists.");

            final User user = find.get(0);

            if (user.isInvited()) {
                throw new Exception(MessageFormat.format("User {0} already invited.", user.getUsername()));
            }

            InvitationServlet.sendInvitation(user);

            final Date date = Utils.now();

            user.setInvited(true);
            user.setInvitedDate(date);
            users.put(user);

            final Map<String, Object> map = new LinkedHashMap<String, Object>();
            map.put("username", user.getUsername());
            map.put("hash", hash);
            map.put("invited", true);
            map.put("date", date);

            final Response response = getResponse();
            response.setStatus(Status.SUCCESS_OK);

            final String json = Utils.getGsonPretty().toJson(map);
            return new JsonRepresentation(json);
        } catch (final Exception e) {
            final Response response = getResponse();
            response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return new StringRepresentation(e.getMessage());
        }
    }
}
