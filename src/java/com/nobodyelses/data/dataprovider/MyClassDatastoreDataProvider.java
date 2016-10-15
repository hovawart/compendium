package com.nobodyelses.data.dataprovider;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.google.appengine.api.NamespaceManager;
import com.maintainer.data.model.MyClass;
import com.maintainer.data.provider.Query;
import com.maintainer.data.provider.datastore.DatastoreDataProvider;
import com.nobodyelses.data.utils.Utils;

public class MyClassDatastoreDataProvider extends DatastoreDataProvider<MyClass> {
    @Override
    public List<MyClass> getAll(Class<?> kind) throws Exception {
        String current = NamespaceManager.get();

        List<MyClass> all = new ArrayList<MyClass>();
        Object id = Utils.getSystemUserKey().getId();
        String system = id.toString();
        NamespaceManager.set(system);
        try {
            all.addAll(super.getAll(kind));
        } catch (Exception e) {}
        NamespaceManager.set(current);
        all.addAll(super.getAll(kind));
        return all;
    }

    @Override
    public List<MyClass> find(Query query) throws Exception {
        String current = NamespaceManager.get();

        Set<MyClass> find = new LinkedHashSet<MyClass>();
        Object id = Utils.getSystemUserKey().getId();
        String system = id.toString();
        NamespaceManager.set(system);
        try {
             find.addAll(super.find(query));
        } catch (Exception e) {}
        NamespaceManager.set(current);
        if (!system.equals(current)) {
            find.addAll(super.find(query));
        }
        return new ArrayList<MyClass>(find);
    }
}
