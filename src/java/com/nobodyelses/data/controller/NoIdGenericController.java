package com.nobodyelses.data.controller;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.restlet.Request;
import org.restlet.data.Form;

import com.maintainer.data.controller.GenericController;
import com.maintainer.data.model.EntityImpl;
import com.maintainer.data.model.MyField;
import com.maintainer.util.Utils;

public class NoIdGenericController<T extends EntityImpl> extends GenericController<T> {

    @Override
    protected Object get(final Request request) throws Exception {
        final Object o = super.get(request);
        if (isNoId()) {
            stripIds(o);
        }
        return o;
    }

    @Override
    protected String toJson(final List list) throws Exception {
        if (isNoId()) {
            return Utils.getGsonPretty().toJson(list);
        }
        return super.toJson(list);
    }

    @Override
    protected String toJson(final Object entity) throws Exception {
        if (isNoId()) {
            return Utils.getGsonPretty().toJson(entity);
        }
        return super.toJson(entity);
    }

    protected boolean isNoId() {
        final Form query = getQuery();
        final String noid = query.getFirstValue(":noid", "false");
        final boolean b = Boolean.parseBoolean(noid);
        return b;
    }

    protected void stripIds(Object o1) throws Exception {
        if (o1 == null) return;

        final Class<? extends Object> clazz = o1.getClass();
        if (Map.class.isAssignableFrom(clazz)) {
            o1 = ((Map) o1).values();
        }

        if (Collection.class.isAssignableFrom(clazz)) {
            final Iterator iterator = ((Collection)o1).iterator();
            while (iterator.hasNext()) {
                final Object o2 = iterator.next();
                stripIds(o2);
            }
        } else if (EntityImpl.class.isAssignableFrom(clazz)){
            final MyField field = Utils.getField(o1, "id");
            field.setAccessible(true);
            if (field != null) {
                field.set(o1, null);
            }

            final Field[] fields = clazz.getDeclaredFields();
            for (final Field f : fields) {
                f.setAccessible(true);
                final Object o2 = f.get(o1);
                stripIds(o2);
            }
        }
    }
}
