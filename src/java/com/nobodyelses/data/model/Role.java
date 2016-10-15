package com.nobodyelses.data.model;

import java.util.ArrayList;
import java.util.List;

import com.maintainer.data.model.Autocreate;
import com.maintainer.data.model.EntityImpl;
import com.maintainer.data.model.Resource;

@SuppressWarnings("serial")
@Resource(name="roles")
public class Role extends EntityImpl implements com.maintainer.data.security.model.Role {
    private String name;

    @Autocreate(create=false, update=false, delete=false)
    private List<Function> functions;

    @Autocreate(create=false, update=false, delete=false)
    private List<User> users;

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public List getFunctions() {
        if (functions == null) functions = new ArrayList<Function>();
        return functions;
    }

    @Override
    public String getName() {
        return name;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public List getUsers() {
        if (users == null) users = new ArrayList<User>();
        return users;
    }

    @Override
    public String toString() {
        String functions = null;
        if (this.functions != null) {
            functions = this.functions.toString();
        } else {
            functions = "[]";
        }

        return
            new StringBuilder()
            .append("Role: ").append(name).append('\n').append("Functions: ").append(functions).toString();
    }

    public void setName(final String name) {
        this.name = name;
    }

    @SuppressWarnings("unchecked")
    public void addUser(final User user) {
        getUsers().add(user);
    }

    @SuppressWarnings("unchecked")
    public void addFunction(final Function function) {
        getFunctions().add(function);
    }
}