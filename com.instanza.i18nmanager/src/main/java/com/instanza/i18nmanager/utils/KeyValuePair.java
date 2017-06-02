package com.instanza.i18nmanager.utils;

/**
 * Created by luanhaipeng on 16/12/7.
 */
public class KeyValuePair {
    private String key;
    private String value;

    public KeyValuePair(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public KeyValuePair() {
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        if(value==null){
            return "";
        }
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
