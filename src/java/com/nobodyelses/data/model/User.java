package com.nobodyelses.data.model;

import java.util.Date;

import com.maintainer.data.model.Autocreate;
import com.maintainer.data.model.NotIndexed;
import com.maintainer.data.model.NotStored;
import com.maintainer.data.model.Resource;

@SuppressWarnings("serial")
@Resource(name="users")
@Autocreate(update=false, delete=false)
public class User extends com.maintainer.data.model.User {
    @NotStored
    private String accountNumber;
    private String hash;
    @NotIndexed
    private boolean invited;
    @NotIndexed
    private Date invitedDate;
    @NotIndexed
    private Date registeredDate;

    protected User() {
        super();
    }

    public User(final String username, final String password) {
        super(username, password);
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setHash(final String hash) {
        this.hash = hash;
    }

    public String getHash() {
        return hash;
    }

    public void setInvited(final boolean invited) {
        this.invited = invited;
    }

    public boolean isInvited() {
        return invited;
    }

    public void setInvitedDate(final Date date) {
        this.invitedDate = date;
    }

    public Date getInvitedDate() {
        return invitedDate;
    }

    public void setRegisteredDate(final Date date) {
        this.registeredDate = date;
    }

    public Date getRegisteredDate() {
        return registeredDate;
    }
}
