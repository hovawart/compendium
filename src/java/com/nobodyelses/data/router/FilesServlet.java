package com.nobodyelses.data.router;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.maintainer.data.provider.DataProvider;
import com.maintainer.data.provider.DataProviderFactory;
import com.maintainer.data.provider.Key;
import com.nobodyelses.data.model.File;

@SuppressWarnings("serial")
public class FilesServlet extends SecureHttpServlet {

    @SuppressWarnings("unchecked")
    @Override
    protected void get(final HttpServletRequest req, final HttpServletResponse resp) throws Exception {
        String uri = req.getRequestURI();

        String name = uri.substring(7);
        String content = null;

        DataProvider<File> dataProvider = (DataProvider<File>) DataProviderFactory.instance().getDataProvider(File.class);
        Key key = Key.create(File.class, name);
        File file = dataProvider.get(key);
        if (file == null) {
            resp.setStatus(NOT_FOUND);
            content = NOT_FOUND_STRING;
        } else {
            resp.setStatus(SUCCESS_OK);
            content = file.getContent();
        }

        byte[] bytes = content.getBytes();
        resp.setContentType("text/plain");
        resp.setContentLength(bytes.length);
        resp.getOutputStream().write(bytes);
    }
}
