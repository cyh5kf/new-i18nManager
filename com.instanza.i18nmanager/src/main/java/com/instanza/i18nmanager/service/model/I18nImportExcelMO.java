package com.instanza.i18nmanager.service.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by luanhaipeng on 16/12/19.
 */
public class I18nImportExcelMO {

    private List<Map<String,String>> rowList = new ArrayList<>();


    public List<Map<String, String>> getRowList() {
        return rowList;
    }

    public void setRowList(List<Map<String, String>> rowList) {
        this.rowList = rowList;
    }
}
