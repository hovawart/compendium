package com.nobodyelses.data.dataprovider;

import java.util.Arrays;
import java.util.List;

import com.google.appengine.api.memcache.MemcacheService;
import com.maintainer.data.provider.Filter;
import com.maintainer.data.provider.Query;
import com.maintainer.data.provider.datastore.DatastoreDataProvider;
import com.maintainer.data.provider.datastore.MyMemcacheServiceFactory;
import com.nobodyelses.data.model.User;

public class UserDataProvider extends DatastoreDataProvider<User> {

    @Override
    public List<User> find(final Query query) throws Exception {
        final MemcacheService memcacheService = MyMemcacheServiceFactory.getMemcacheService();

        final List<Filter> filters = query.getFilters();
        if (filters.size() == 1) {
            final Filter filter = filters.get(0);
            if ("username".equals(filter.getField())) {
                final String username = (String) filter.getValue();
                final User user = (User) memcacheService.get(username);
                if (user != null) {
                    return Arrays.asList(user);
                }
            }
        }

        final List<User> list = super.find(query);
        for (final User user : list) {
            memcacheService.put(user.getUsername(), user);
        }

        return list;
    }
}
