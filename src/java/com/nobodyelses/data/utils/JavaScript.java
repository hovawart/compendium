package com.nobodyelses.data.utils;

import java.util.List;
import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeJavaArray;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;

public class JavaScript {
    @SuppressWarnings("unchecked")
    public static Object toJavaScript(final Object o, final Scriptable scope) {
        if (o == null) {
            return null;
        }

        final Class<?> clazz = o.getClass();

        if (Map.class.isAssignableFrom(clazz)  && !JavaScriptMap.class.isAssignableFrom(clazz)) {
            return new JavaScriptMap (scope, (Map<String, Object>) o);
        }

        if (List.class.isAssignableFrom(clazz) && !JavaScriptList.class.isAssignableFrom(clazz)) {
            return new JavaScriptList (scope, (List<Object>) o);
        }

        return Context.javaToJS (o, scope);
    }

    public static class JavaScriptList extends NativeJavaArray {

        private static final long serialVersionUID = 1483539454201596942L;

        private List<Object> list = null;

        public JavaScriptList(final Scriptable scope, final List<Object> list) {
            super(scope, list.toArray());
            this.list = list;
        }

        public List<Object> getList() {
            return list;
        }

        @Override
        public boolean has(final int index, final Scriptable scope) {
            return super.has(index, scope) || (index >= 0 && index < getList().size());
        }

        @Override
        public Object get(final String id, final Scriptable start) {
            if (id.equals("length")) {
                return getList().size();
            }
            return super.get(id,  start);
        }

        @Override
        public Object get(final int index, final Scriptable scope) {
            final List<Object> list = getList();

            if (index >= 0 && index < list.size()) {
                return toJavaScript(list.get(index), scope);
            }

            return Scriptable.NOT_FOUND;
        }

        @Override
        public void put(final int index, final Scriptable scope, final Object value) {
            final int max = index + 1;
            final List<Object> list = getList();

            if (max > list.size()) {
                for (int i = list.size(); i < index; i++) {
                    list.add(i, null);
                }

                list.add(index, value);
            } else {
                list.set(index, value);
            }
        }


        @Override
        public void delete (final int index) {
            getList().remove(index);
        }

        @Override
        public Object[] getIds() {
            final List<Object> list = getList();
            final Integer[] ids = new Integer[list.size()];

            for (int i = 0; i < ids.length; i++) {
                ids[i] = new Integer(i);
            }

            return ids;
        }
    }

    public static class JavaScriptMap extends NativeJavaObject {

        private static final long serialVersionUID = 7703148826316390477L;

        private Map<String, Object> map = null;

        public JavaScriptMap(final Scriptable scope, final Map<String, Object> map) {
            super(scope, map, null);

            if (map == null) {
                throw new RuntimeException ("Map cannot be null.");
            }

            this.map = map;
        }

        public Map<String, Object> getMap() {
            return map;
        }

        @Override
        public boolean has(final String key, final Scriptable scope) {
            return super.has(key, scope) || getMap().containsKey (key);
        }

        @Override
        public Object get(final String key, final Scriptable scope) {
            System.out.println("looking for " + key);
            if (super.has(key, scope)) {
                System.out.println("super has " + key);
                return super.get(key, scope);
            }
            final Object object = toJavaScript(getMap().get(key), scope);
            if (object == null) {
                System.out.println("we don't have " + key);
            }
            return object;
        }

        @Override
        public Object get(final int index, final Scriptable scope) {
            return NOT_FOUND;
        }

        @Override
        public void put(final String key, final Scriptable scope, Object value) {
            if (value != null && NativeJavaObject.class.isAssignableFrom(value.getClass())) {
                value = ((NativeJavaObject) value).unwrap();
            }
            getMap().put(key, value);
        }

        @Override
        public void put(final int index, final Scriptable scope, final Object value) {}

        @Override
        public void delete(final String key) {
            getMap().remove(key);
        }

        @Override
        public void delete(final int index) {}

        @Override
        public Object[] getIds() {
            return getMap().keySet().toArray();
        }
    }
}
