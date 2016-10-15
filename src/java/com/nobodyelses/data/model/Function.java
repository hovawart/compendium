package com.nobodyelses.data.model;

import com.maintainer.data.model.EntityImpl;
import com.maintainer.data.model.Resource;

@SuppressWarnings("serial")
@Resource(name="functions")
public class Function extends EntityImpl implements com.maintainer.data.security.model.Function {
    private String path;
    private String permission;

    public void setPath(final String path) {
        this.path = path;
    }

    @Override
    public String getPath() {
        return path;
    }

    public void setPermission(final String permission) {
        this.permission = permission;
    }

    @Override
    public String getPermission() {
        return permission;
    }

    @Override
    public String toString(){
       return "Function : " + path + "\n"
            + "permission: " + permission;
    }
}