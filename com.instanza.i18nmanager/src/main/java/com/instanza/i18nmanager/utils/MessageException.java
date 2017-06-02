package com.instanza.i18nmanager.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by luanhaipeng on 16/12/7.
 */
public class MessageException extends Exception {
    private String message;

    private Object data;

    public MessageException(String message) {
        super(message);
        this.message = message;
    }

    public MessageException(String message,Object data) {
        super(message);
        this.message = message;
        this.data = data;
    }

    public MessageException() {
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Map<String,Object> toDescMap(){
        Map<String,Object> map = new HashMap<>();
        map.put("message",this.message);
        map.put("data",this.data);
        return map;
    }
}
