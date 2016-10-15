package com.nobodyelses.data.model;

import com.maintainer.data.model.Autocreate;
import com.maintainer.data.model.EntityImpl;
import com.maintainer.data.model.NotIndexed;

@SuppressWarnings("serial")
public class UserEntityImpl extends EntityImpl {
    @NotIndexed
    @Autocreate(readonly=true)
    private User user;

    public void setUser(final User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}
