package com.nobodyelses.data.controller;

import java.util.List;

import com.nobodyelses.data.utils.Utils;
import com.maintainer.data.provider.DataProvider;
import com.maintainer.data.provider.Key;
import com.maintainer.data.provider.Query;
import com.nobodyelses.data.model.User;

@SuppressWarnings("rawtypes")
public class SystemAndUserGenericController extends UserGenericController {
    @SuppressWarnings({ "unchecked" })
    @Override
    protected List find(DataProvider dataProvider, Query query) throws Exception {
        List list = super.find(dataProvider, query);

        User user = getUser();
        Key systemUserKey = Utils.getSystemUserKey();

        if (!user.getKey().equals(systemUserKey)) {
            query.setParent(systemUserKey);
            List list2 = super.find(dataProvider, query);
            list.addAll(list2);
        }

        return list;
    }
}
