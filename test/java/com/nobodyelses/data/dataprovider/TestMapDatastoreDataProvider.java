package com.nobodyelses.data.dataprovider;

import java.util.List;

import org.junit.Test;

import com.google.gson.Gson;
import com.maintainer.data.model.EntityBase;
import com.maintainer.data.model.MapEntityImpl;
import com.maintainer.data.model.MyClass;
import com.maintainer.data.model.MyField;
import com.maintainer.data.model.ThreadLocalInfo;
import com.maintainer.data.provider.DataProvider;
import com.maintainer.data.provider.DataProviderFactory;
import com.maintainer.data.provider.datastore.DatastoreDataProvider;
import com.maintainer.data.provider.datastore.MapDatastoreDataProvider;
import com.maintainer.util.Utils;
import com.nobodyelses.data.LocalServiceTest;
import com.nobodyelses.data.model.Ticket;
import com.nobodyelses.data.model.Ticket2;
import com.nobodyelses.data.model.TicketStatus;

public class TestMapDatastoreDataProvider extends LocalServiceTest {

    @Test
    public void test() throws Exception {
        DataProviderFactory.instance().register(Ticket2.class, new MapDatastoreDataProvider<>());

        DatastoreDataProvider<EntityBase> datastoreDataProvider = new DatastoreDataProvider<>();

        Ticket ticket = new Ticket();
        ticket.setSummary("This is a test ticket");
        TicketStatus status = new TicketStatus();
        status.setName("Open");
        ticket.setStatus(status);
        datastoreDataProvider.post(status);
        datastoreDataProvider.post(ticket);

        ticket.setKey(null);
        ticket.setId(null);
        ticket.setIdentity(null);

        Gson gson = Utils.getGsonPretty();
        String json = gson.toJson(ticket);
        System.out.println(json);

        MapDatastoreDataProvider<MapEntityImpl> mapDatastoreDataProvider = new MapDatastoreDataProvider<MapEntityImpl>();
        Ticket2 ticket2 = (Ticket2) mapDatastoreDataProvider.fromJson(Ticket2.class, json);
        mapDatastoreDataProvider.post(ticket2);
        assertNotNull(ticket2.getKey());

        List<MapEntityImpl> all = mapDatastoreDataProvider.getAll(Ticket2.class);
        assertNotNull(all);

        String json2 = gson.toJson(all);
        System.out.println(json2);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testCircularReference() throws Exception {
        DataProviderFactory factory = DataProviderFactory.instance();
        factory.register(MapEntityImpl.class, new MapDatastoreDataProvider<>());

        DataProvider provider = factory.getDefaultDataProvider();
        DataProvider mapProvider = factory.getDataProvider(MapEntityImpl.class);

        MyClass class1 = new MyClass();
        class1.setName("Class1");
        class1.setRoute("class1");

        MyField field1 = new MyField("name", String.class);
        class1.addField(field1);

        MyField field2 = new MyField("ref", MapEntityImpl.class);
        class1.addField(field2);

        provider.post(class1);

        assertNotNull(class1.getKey());

        ThreadLocalInfo.getInfo().setPath("/data/class1");

        MapEntityImpl map1 = new MapEntityImpl();
        map1.setMyClass(class1);
        map1.put("name", "Test1");
        mapProvider.post(map1);
        assertNotNull(map1.getKey());

        MapEntityImpl map2 = new MapEntityImpl();
        map2.setMyClass(class1);
        map2.put("name", "Test2");
        mapProvider.post(map2);
        assertNotNull(map2.getKey());

        map1.set("ref", map2);

//        ObjectMapper mapper = new ObjectMapper();
//        String json = mapper.writeValueAsString(map1);
//        System.out.println(json);

//        mapProvider.put(map1);
//
//        map2.set("ref", map1);
//        mapProvider.put(map2);
    }
}
