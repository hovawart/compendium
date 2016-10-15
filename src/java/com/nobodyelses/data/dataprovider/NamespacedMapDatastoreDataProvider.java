package com.nobodyelses.data.dataprovider;

import java.util.List;

import com.google.appengine.api.NamespaceManager;

import com.maintainer.data.model.ThreadLocalInfo;
import com.maintainer.data.model.MapEntityImpl;
import com.maintainer.data.provider.Query;
import com.maintainer.data.provider.datastore.MapDatastoreDataProvider;

import com.nobodyelses.data.model.User;

public class NamespacedMapDatastoreDataProvider<T extends MapEntityImpl> extends MapDatastoreDataProvider<T> {

    private void setNamespace() {
        User user = (User) ThreadLocalInfo.getInfo().getUser();
        String namespace = user.getKey().getId().toString();
        NamespaceManager.set(namespace);
    }

    @SuppressWarnings("unchecked")
    @Override
    public T get(final com.maintainer.data.provider.Key key) throws Exception {
        String oldNamespace = NamespaceManager.get();
        setNamespace();
        try {
            return super.get(key);
        } finally {
            NamespaceManager.set(oldNamespace);
        }
    }

    @Override
    public List<T> getAll(final Class<?> kind) throws Exception {
        String oldNamespace = NamespaceManager.get();
        setNamespace();
        try {
            return super.getAll(kind);
        } finally {
            NamespaceManager.set(oldNamespace);
        }
    }

    @Override
    public T post(final T target) throws Exception {
        String oldNamespace = NamespaceManager.get();
        setNamespace();
        try {
            return super.post(target);
        } finally {
            NamespaceManager.set(oldNamespace);
        }
    }

    @Override
    public T put(T target) throws Exception {
        String oldNamespace = NamespaceManager.get();
        setNamespace();
        try {
            return super.put(target);
        } finally {
            NamespaceManager.set(oldNamespace);
        }
    }

    @Override
    public com.maintainer.data.provider.Key delete(final com.maintainer.data.provider.Key key) throws Exception {
        String oldNamespace = NamespaceManager.get();
        setNamespace();
        try {
            return super.delete(key);
        } finally {
            NamespaceManager.set(oldNamespace);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<T> find(final Query query) throws Exception {
        String oldNamespace = NamespaceManager.get();
        setNamespace();
        try {
            return super.find(query);
        } finally {
            NamespaceManager.set(oldNamespace);
        }
    }
}
