package com.nobodyelses.data.router;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.engine.header.Header;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Template;
import org.restlet.util.Series;

import com.maintainer.data.model.MapEntityImpl;
import com.maintainer.data.model.MyClass;
import com.maintainer.data.model.Resource;
import com.maintainer.data.provider.DataProvider;
import com.maintainer.data.provider.datastore.MapDatastoreDataProvider;
import com.maintainer.data.router.RouteMap;
import com.maintainer.data.router.WebSwitch;
import com.maintainer.data.security.MyCookieAuthenticator;
import com.maintainer.util.Utils;
import com.nobodyelses.data.controller.UsersController;
import com.nobodyelses.data.dataprovider.MyClassDatastoreDataProvider;
import com.nobodyelses.data.dataprovider.UserDataProvider;
import com.nobodyelses.data.model.Ticket2;
import com.nobodyelses.data.model.User;

//This is a test comment.
public class MyResourceRouter extends WebSwitch {
    private static Logger log = Logger.getLogger(MyResourceRouter.class.getName());
    private static Map<Class<?>, DataProvider<?>> dataProviders;

    @Override
    protected void fillRoutes(final RouteMap routes) {
        routes.put("/users", UsersController.class, Template.MODE_STARTS_WITH);
    }

    @Override
    protected void registerDataProviders(final Map<Class<?>, DataProvider<?>> dataProviders) {
        if (MyResourceRouter.dataProviders == null) {
            dataProviders.putAll(getDataProviders());
        }

        dataProviders.put(MyClass.class, new MyClassDatastoreDataProvider());
        dataProviders.put(MapEntityImpl.class, new MapDatastoreDataProvider<>());

        super.registerDataProviders(dataProviders);
    }

    public static boolean hasDataProviders() {
        return MyResourceRouter.dataProviders != null;
    }

    public static Map<Class<?>, DataProvider<?>> getDataProviders() {
        if (dataProviders == null) {
            dataProviders = new HashMap<Class<?>, DataProvider<?>>();
            dataProviders.put(User.class, new UserDataProvider());
        }
        return dataProviders;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void handle(final Request request, final Response response) {
        final String path = request.getResourceRef().getPath();
        log.warning(MessageFormat.format("MyResourceRouter.handle start: {0}", path));
        final Series<Header> headers = (Series<Header>) request.getAttributes().get("org.restlet.http.headers");
        final String cookie = headers==null?null:headers.getFirstValue("Cookie");
        if (cookie != null) {
            final int length = cookie.split(",").length;

            if (length > 0) {
                log.warning("Multiple credential cookies detected.");
                headers.removeAll("Cookie");
                headers.set("Cookie", cookie);
            }
        }

        final Class<? extends ServerResource> target = Utils.getTargetServerResource(this, request);
        final Resource annotation = target.getAnnotation(Resource.class);

        if (annotation != null && !annotation.secured()) {
            // bypasses security.
            try {
                final ServerResource controller = target.newInstance();
                controller.setRequest(request);
                controller.setResponse(response);
                controller.handle();
                return;
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
        super.handle(request, response);
        log.warning(MessageFormat.format("MyResourceRouter.handle done: {0}", path));
    }

    @Override
    public void initializeAppServerLogging() {
        // logging is handled by app engine.
    }

    @Override
    protected MyCookieAuthenticator getCookieAuthenticator(final Context context) {
        return new com.nobodyelses.data.router.MyCookieAuthenticator(context, false, "My cookie realm", "MyExtraSecretKey".getBytes());
    }

    @Override
    protected MyEnroler getEnroler(final String applicationName) {
        return new MyEnroler(applicationName);
    }

    @Override
    protected MyVerifier getVerifier() {
        final MyVerifier verifier = new MyVerifier();
        verifier.setApplication(this);
        return verifier;
    }
}
