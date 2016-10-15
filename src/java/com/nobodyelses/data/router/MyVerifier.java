package com.nobodyelses.data.router;

import java.util.List;

import org.restlet.Request;

import com.google.appengine.api.memcache.MemcacheService;
import com.maintainer.data.provider.DataProvider;
import com.maintainer.data.provider.DataProviderFactory;
import com.maintainer.data.provider.Query;
import com.maintainer.data.provider.datastore.MyMemcacheServiceFactory;
import com.maintainer.util.Utils;
import com.nobodyelses.data.model.User;

public class MyVerifier extends com.maintainer.data.security.MyVerifier {

    @Override
    public int verify(final Request request, final String identifier, final char[] secret) {

        try {
            final User user = getUser(identifier);

            if (user == null) {
                return RESULT_INVALID;
            }

            request.getAttributes().put(Utils._USER_, user);

            boolean validatedPassword = Utils.validatePassword(new String(secret), user.getPassword());
            if (validatedPassword) {
                ThreadLocalInfo.setInfo(user);
                return RESULT_VALID;
            }
        } catch (final Exception e) {
            SecureHttpServlet.recordError(e);
        }

        return RESULT_INVALID;
    }

    @SuppressWarnings("unchecked")
    private User getUser(final String identifier) throws Exception {
        final MemcacheService cache = MyMemcacheServiceFactory.getMemcacheService();
        User user = (User) cache.get(identifier);

        if (user == null) {
            final DataProvider<User> users = (DataProvider<User>) DataProviderFactory.instance().getDataProvider(com.nobodyelses.data.model.User.class);
            final Query q = new Query(User.class);
            q.filter("username", identifier);
            final List<User> list = users.find(q);
            if (!list.isEmpty()) {
                user = list.get(0);
                cache.put(identifier, user);
            }
        }
        return user;
    }
}
