package com.nobodyelses.data.model;

public class Ping {
    private String ping = null;
    private boolean isUser = false;

    public Ping(final String identifier, final boolean isUser) {
        this.ping = identifier;
        this.isUser = isUser;
    }

    public String getPing() {
        return ping;
    }

    public void setPing(String ping) {
        this.ping = ping;
    }

    public boolean isUser() {
        return isUser;
    }

    public void setUser(boolean isUser) {
        this.isUser = isUser;
    }
}
