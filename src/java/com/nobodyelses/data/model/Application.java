package com.nobodyelses.data.model;

import java.util.List;

import com.maintainer.data.model.Autocreate;
import com.maintainer.data.model.EntityImpl;
import com.maintainer.data.model.Resource;

@SuppressWarnings("serial")
@Resource(name="applications")
@Autocreate(create=true, update=false, delete=false)
public class Application extends EntityImpl implements com.maintainer.data.security.model.Application {

    private String name;

    @Autocreate
    private List<Function> functions;

    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setFunctions(final List<Function> functions) {
        this.functions = functions;
    }

    @Override
    public List getFunctions() {
        return functions;
    }
}
