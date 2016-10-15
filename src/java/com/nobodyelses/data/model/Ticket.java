package com.nobodyelses.data.model;

import java.util.Date;

import com.maintainer.data.model.Autocreate;
import com.maintainer.data.model.EntityImpl;
import com.maintainer.data.model.NotIndexed;
import com.maintainer.data.model.Resource;

@Resource(name="tickets")
public class Ticket extends EntityImpl {
    @NotIndexed
    private String summary;

    @NotIndexed
    private String description;

    private String source;

    @Autocreate(create=true, update=false, delete=false)
    private TicketStatus status;

    @Autocreate(create=true, update=false, delete=false)
    private TicketPriority priority;

    @Autocreate(create=true, update=false, delete=false)
    private TicketResolution resolution;

    @Autocreate(create=true, update=false, delete=false)
    private TicketCategory category;

    private Date created;

    @Override
    public void setCreated(final Date created) {
        this.created = created;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
}
