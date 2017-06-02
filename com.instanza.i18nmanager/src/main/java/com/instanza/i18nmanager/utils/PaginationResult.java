package com.instanza.i18nmanager.utils;

import java.util.List;

/**
 * Created by luanhaipeng on 16/12/8.
 */
public class PaginationResult <T>{
    private List<T> data;
    private int total;

    public PaginationResult() {
    }

    public PaginationResult(List<T> data, int total) {
        this.data = data;
        this.total = total;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
}
