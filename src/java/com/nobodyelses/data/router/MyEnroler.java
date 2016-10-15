package com.nobodyelses.data.router;

import java.util.List;

import org.restlet.data.ClientInfo;
import org.restlet.security.Role;

import com.google.appengine.api.NamespaceManager;
import com.nobodyelses.data.model.User;

public class MyEnroler extends com.maintainer.data.security.MyEnroler {

    public MyEnroler(final String applicationName) {
        super(applicationName);
    }

    @Override
    public void enrole(final ClientInfo clientInfo) {
        final List<Role> roles = clientInfo.getRoles();
        roles.add(new Role("login.POST", "Login"));

        ThreadLocalInfo info = ThreadLocalInfo.getInfo();
        User user = info.getUser();
        if (user != null) {
            roles.add(new Role("**.**", "Any resource, any method."));
            NamespaceManager.set(user.getKey().getId().toString());
        } else {
            roles.add(new Role("**.GET", "Any resource, get method."));
        }
    }
}
