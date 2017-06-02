package com.instanza.i18nmanager.service.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by luanhaipeng on 16/12/15.
 */
public class I18nCheckErrorVO {
    private boolean error = false;

    private String sourceKey;
    private List<String> fillValueLangName;
    private List<String> emptyValueLangName;

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public String getSourceKey() {
        return sourceKey;
    }

    public void setSourceKey(String sourceKey) {
        this.sourceKey = sourceKey;
    }


    public List<String> getFillValueLangName() {
        if (fillValueLangName==null){
            fillValueLangName = new ArrayList<>();
        }
        return fillValueLangName;
    }

    public void setFillValueLangName(List<String> fillValueLangName) {
        this.fillValueLangName = fillValueLangName;
    }

    public List<String> getEmptyValueLangName() {
        if (emptyValueLangName==null){
            emptyValueLangName = new ArrayList<>();
        }
        return emptyValueLangName;
    }

    public void setEmptyValueLangName(List<String> emptyValueLangName) {
        this.emptyValueLangName = emptyValueLangName;
    }
}
