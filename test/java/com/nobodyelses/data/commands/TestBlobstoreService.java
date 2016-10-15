package com.nobodyelses.data.commands;

import org.junit.Test;

import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.nobodyelses.data.LocalServiceTest;

public class TestBlobstoreService extends LocalServiceTest {

    @Test
    public void test() {
        BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
        String url = blobstoreService.createUploadUrl("/media");
        assertEquals("http://localhost:8080/_ah/upload/agR0ZXN0chsLEhVfX0Jsb2JVcGxvYWRTZXNzaW9uX18YAQw", url);
    }
}
