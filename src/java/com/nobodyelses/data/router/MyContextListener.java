package com.nobodyelses.data.router;

import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.maintainer.data.provider.DataProvider;
import com.maintainer.data.provider.DataProviderFactory;
import com.maintainer.data.provider.datastore.DatastoreDataProvider;

public class MyContextListener implements ServletContextListener {
    private static final Logger log = Logger.getLogger(MyContextListener.class.getName());

    @Override
    public void contextDestroyed(final ServletContextEvent arg0) {

    }

    @Override
    public void contextInitialized(final ServletContextEvent arg0) {
        log.warning("context initializing");
        if (!MyResourceRouter.hasDataProviders()) {
            final DataProviderFactory factory = DataProviderFactory.instance();
            factory.setDefaultDataProvider(new DatastoreDataProvider<>());
            final Map<Class<?>, DataProvider<?>> dataProviders = MyResourceRouter.getDataProviders();
            for (final Entry<Class<?>, DataProvider<?>> e : dataProviders.entrySet()) {
                factory.register(e.getKey(), e.getValue());
            }
        }
    }

}
