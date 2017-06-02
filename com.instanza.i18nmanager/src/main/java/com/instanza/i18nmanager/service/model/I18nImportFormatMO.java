package com.instanza.i18nmanager.service.model;

import com.instanza.i18nmanager.utils.KeyValuePair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by luanhaipeng on 16/12/14.
 */
public class I18nImportFormatMO {

    private String langName;
    private String fileName;
    private List<KeyValuePair> keyValuePairList;

    public I18nImportFormatMO(String langName, String fileName, List<KeyValuePair> keyValuePairList) {
        this.langName = langName;
        this.fileName = fileName;
        this.keyValuePairList = keyValuePairList;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getLangName() {
        return langName;
    }

    public void setLangName(String langName) {
        this.langName = langName;
    }

    public List<KeyValuePair> getKeyValuePairList() {
        if(keyValuePairList==null){
            return new ArrayList<>();
        }
        return keyValuePairList;
    }

    public void setKeyValuePairList(List<KeyValuePair> keyValuePairList) {
        this.keyValuePairList = keyValuePairList;
    }
}
