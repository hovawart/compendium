package com.nobodyelses.data.router;

import com.nobodyelses.data.model.User;

public class ThreadLocalInfo {
    private static final ThreadLocal<ThreadLocalInfo> info = new ThreadLocal<ThreadLocalInfo>() {
        @Override
        protected ThreadLocalInfo initialValue() {
            return new ThreadLocalInfo();
        }
    };

    private User user;

    public void setUser(final User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public static ThreadLocalInfo getInfo() {
        final ThreadLocalInfo callerInfo = info.get();
        if (callerInfo == null) {
            throw new IllegalStateException("No info.");
        }
        return callerInfo;
    }

    public static ThreadLocalInfo setInfo(final User user) {
        final ThreadLocalInfo callerInfo = getThreadInfo();
        callerInfo.user = user;
        return callerInfo;
    }

    private static ThreadLocalInfo getThreadInfo() {
        ThreadLocalInfo callerInfo = info.get();
        if (callerInfo == null) {
            callerInfo = new ThreadLocalInfo();
            info.set(callerInfo);
        }
        return callerInfo;
    }

    public static void clearInfo() {
        info.set(null);
    }

}
