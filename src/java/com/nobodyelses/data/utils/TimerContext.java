package com.nobodyelses.data.utils;

import org.mozilla.javascript.Context;

public class TimerContext extends Context {
    private long startTime;
    private long expires = 1000;

    public void setStartTime(final long startTime) {
        this.startTime = startTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setExpires(final long expires) {
        this.expires = expires;
    }

    public long getExpires() {
        return expires;
    }

    public void startTimer() {
        setStartTime(System.currentTimeMillis());
    }

    public void startTimer(final long expires) {
        setExpires(expires);
        startTimer();
    }
}
