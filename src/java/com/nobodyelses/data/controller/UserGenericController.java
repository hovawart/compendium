package com.nobodyelses.data.controller;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.restlet.Request;
import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.ext.servlet.ServletUtils;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;

import com.maintainer.data.controller.Resource;
import com.maintainer.data.model.EntityImpl;
import com.maintainer.data.model.MyField;
import com.maintainer.data.provider.Key;
import com.maintainer.data.provider.Query;
import com.maintainer.data.provider.datastore.MyMemcacheServiceFactory;
import com.maintainer.util.Utils;
import com.nobodyelses.data.model.User;

public class UserGenericController<T extends EntityImpl> extends NoIdGenericController<T> {
    private User user;

    @Override
    public Representation handle() {
        try {
            checkMachine(getRequest());
        } catch (Exception e) {
            getResponse().setStatus(Status.CLIENT_ERROR_UNAUTHORIZED, e.getMessage());
            return new StringRepresentation(e.getMessage());
        }
        return super.handle();
    }

    protected void checkMachine(Request request) throws Exception {
        HttpServletRequest req = ServletUtils.getRequest(request);

        final Map<String, Cookie> mappedCookies = new HashMap<String, Cookie>();
        final Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (final Cookie cookie : cookies) {
                mappedCookies.put(cookie.getName(), cookie);
            }

            final Cookie cookie = mappedCookies.get("Machine");
            if (cookie != null) {
                throw new Exception("Machines cannot access data.");
            }
        }
    }

    @Override
    protected Query addParametersToQuery(Request request, Resource resource, Query query) throws Exception {
        User user = getUser();
        Query query2 = super.addParametersToQuery(request, resource, query);
        query2.setParent(user.getKey());
        return query2;
    }

    @Override
    protected void prePost(final T target) throws Exception {
        final User user = getUser();
        checkUser(target, user);

        com.nobodyelses.data.utils.Utils.setUser(target, user);
        com.nobodyelses.data.utils.Utils.setParent(target, user);

        super.prePost(target);
    }

    @Override
    protected void postPost(T obj) throws Exception {
        MyMemcacheServiceFactory.getMemcacheService().clearAll();
    }

    @Override
    protected void prePut(final T target) throws Exception {
        final User user = getUser();
        checkUser(target, user);

        com.nobodyelses.data.utils.Utils.setUser(target, user);
        com.nobodyelses.data.utils.Utils.setParent(target, user);

        super.prePut(target);
    }

    @Override
    protected void postPut(T obj) throws Exception {
        MyMemcacheServiceFactory.getMemcacheService().clearAll();
    }

    protected void checkUser(final T target, final User user) throws Exception {
        final User targetUser = getUser(target);
        if (targetUser != null && !targetUser.getKey().equals(user.getKey())) {
            throw new SecurityException("Unauthorized access.");
        }
    }

    @Override
    protected void preDelete(final T target) throws Exception {
        final User user = getUser();
        checkUser(target, user);

        com.nobodyelses.data.utils.Utils.setUser(target, user);
        com.nobodyelses.data.utils.Utils.setParent(target, user);

        super.preDelete(target);
    }

    @Override
    protected void postDelete(T obj) throws Exception {
        MyMemcacheServiceFactory.getMemcacheService().clearAll();
    }

    private User getUser(final T target) throws Exception {
        final Key key = target.getKey();
        if (key == null) return null;
        final Key parent = key.getParent();
        final User user = com.nobodyelses.data.utils.Utils.getUser(parent);
        return user;
    }

    protected User getUser() throws Exception {
        if (user == null) {
            user = (User) Utils.getUser(getRequest());
        }
        return user;
    }

    @Override
    protected Object get(final Request request) throws Exception {
        final Object o = super.get(request);
        if (isNoUser()) {
            stripUsers(o);
        }
        return o;
    }

    @Override
    protected String toJson(final List list) {
        if (isNoUser()) {
            return Utils.getGsonPretty().toJson(list);
        }
        return com.nobodyelses.data.utils.Utils.getGson().toJson(list);
    }

    @Override
    protected String toJson(final Object entity) {
        if (isNoUser()) {
            return Utils.getGsonPretty().toJson(entity);
        }
        return com.nobodyelses.data.utils.Utils.getGson().toJson(entity);
    }

    protected boolean isNoUser() {
        final Form query = getQuery();
        final String nouser = query.getFirstValue(":nouser", "false");
        final boolean b = Boolean.parseBoolean(nouser);
        return b;
    }

    protected void stripUsers(Object o1) throws Exception {
        if (o1 == null) return;

        final Class<? extends Object> clazz = o1.getClass();
        if (Map.class.isAssignableFrom(clazz)) {
            o1 = ((Map) o1).values();
        }

        if (Collection.class.isAssignableFrom(clazz)) {
            final Iterator iterator = ((Collection)o1).iterator();
            while (iterator.hasNext()) {
                final Object o2 = iterator.next();
                stripUsers(o2);
            }
        } else if (EntityImpl.class.isAssignableFrom(clazz)){
            final MyField field = Utils.getField(o1, "user");
            field.setAccessible(true);
            if (field != null) {
                field.set(o1, null);
            }

            final Field[] fields = clazz.getDeclaredFields();
            for (final Field f : fields) {
                f.setAccessible(true);
                final Object o2 = f.get(o1);
                stripUsers(o2);
            }
        }
    }

}
