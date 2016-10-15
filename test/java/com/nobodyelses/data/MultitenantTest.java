package com.nobodyelses.data;

import org.junit.Test;

import com.google.appengine.api.NamespaceManager;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;

public class MultitenantTest extends LocalServiceTest {
    @Test
    public void test() {
        String namespace = NamespaceManager.get();
        NamespaceManager.set("Test");

        try {
            DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();

            Entity customer = new Entity("Customer");
            customer.setProperty("name", "Parent Company");

            Key parentCompanyKey = datastoreService.put(customer);

            String parentCompanyKeyString = parentCompanyKey.toString();
            assertEquals("!Test:Customer(1)", parentCompanyKeyString);

            Entity childCustomer = new Entity("Customer", parentCompanyKey);
            childCustomer.setProperty("name", "Child Company");

            Key childCompanyKey = datastoreService.put(childCustomer);
            String childCompanyKeyString = childCompanyKey.toString();
            assertEquals("!Test:Customer(1)/Customer(2)", childCompanyKeyString);
        } finally {
            NamespaceManager.set(namespace);
        }
    }
}
