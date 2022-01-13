package com.xxxxxxh.lib;

public class MessageEvent {

    public final Object[] message;

    public MessageEvent(Object... message) {
        this.message = message;
    }

    public Object[] getMessage() {
        return message;
    }
}
