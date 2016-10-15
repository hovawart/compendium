package com.nobodyelses.data.controller;

import org.restlet.Request;
import org.restlet.representation.Representation;

public class TicketsController extends NoIdGenericController {
    @Override
    protected Representation getItems(Request request) throws Exception {
        return super.getItems(request);
    }

    @Override
    public Representation postItem(Representation rep) throws Exception {
        return super.postItem(rep);
    }

    @Override
    public Representation putItem(Representation rep) throws Exception {
        return super.putItem(rep);
    }

    @Override
    public Representation deleteItem() throws Exception {
        return super.deleteItem();
    }
}
