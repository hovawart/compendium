package com.nobodyelses.data.router;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ClientInfo;
import org.restlet.data.Status;
import org.restlet.engine.header.Header;
import org.restlet.security.Enroler;
import org.restlet.security.User;
import org.restlet.util.Series;

import com.google.appengine.api.NamespaceManager;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.maintainer.data.provider.DataProviderFactory;
import com.maintainer.data.provider.datastore.DatastoreDataProvider;
import com.maintainer.util.Utils;
import com.nobodyelses.data.model.Role;

public class MyCookieAuthenticator extends com.maintainer.data.security.MyCookieAuthenticator {
    private static final Logger log = Logger.getLogger(MyCookieAuthenticator.class.getName());

    public MyCookieAuthenticator(final Context context, final boolean optional, final String realm, final byte[] encryptSecretKey) {
        super(context, optional, realm, encryptSecretKey);
    }

    @Override
    protected boolean authenticate(Request request, Response response) {
        boolean authenticate = super.authenticate(request, response);

        if (!authenticate) {
            final ChallengeResponse cr = new ChallengeResponse(
                    getScheme(),
                    "guest",
                    "guest"
            );

            // Set the guest user
            User user = new User("guest", "guest");
            ClientInfo clientInfo = request.getClientInfo();
            clientInfo.setUser(user);

            // Set guest roles
            Enroler enroler = getEnroler();
            enroler.enrole(clientInfo);

            request.setChallengeResponse(cr);
            response.setStatus(Status.SUCCESS_OK);

            final Series<Header> headers = (Series<Header>) request.getAttributes().get("org.restlet.http.headers");
            final String connection = headers==null?null:headers.getFirstValue("Nobodyelses-Data-Connection");

            if (connection != null) {
                NamespaceManager.set(connection);
            } else {
                // Temporarily set to the system namespace
                try {
                    String id = com.nobodyelses.data.utils.Utils.getSystemUser().getKey().getId().toString();
                    NamespaceManager.set(id);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return true;
    }

    @Override
    protected int authenticated(Request request, Response response) {
        // TODO Auto-generated method stub
        return super.authenticated(request, response);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    protected List getUserRoles(final Request request, final User user) {
        try {
            // Why isn't this using the user passed in?
            final com.nobodyelses.data.model.User user2 = (com.nobodyelses.data.model.User) Utils.getUser(request);
            final Key datastoreKey = DatastoreDataProvider.createDatastoreKey(user2.getKey());

            final com.google.appengine.api.datastore.Query q = new com.google.appengine.api.datastore.Query(Role.class.getName());
            q.setFilter(new FilterPredicate("users", FilterOperator.EQUAL, datastoreKey));
            final PreparedQuery p = DatastoreServiceFactory.getDatastoreService().prepare(q);
            final List<Entity> list = p.asList(FetchOptions.Builder.withDefaults());

            final List<Role> result = new ArrayList<Role>();
            final DatastoreDataProvider<Role> roles = (DatastoreDataProvider<Role>) DataProviderFactory.instance().getDataProvider(Role.class);
            for (final Entity entity : list) {
                final Role fromEntity = roles.fromEntity(Role.class, entity);
                result.add(fromEntity);
            }

            return result;
        } catch (final Exception e) {
            return Collections.emptyList();
        }
    }
}
