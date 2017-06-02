package com.instanza.i18nmanager.service;

import com.instanza.i18nmanager.entity.I18nItem;
import com.instanza.i18nmanager.mapper.I18nItemMapper;
import com.instanza.i18nmanager.utils.MessageException;
import com.instanza.i18nmanager.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


@Service
public class I18nItemService {

    @Autowired
    private I18nItemMapper i18nItemMapper;


    public boolean hasSourceKey(String sourceKey) {

        Map<String, Object> params = new HashMap<>();
        params.put("source_key", sourceKey);
        int count = i18nItemMapper.queryCountBySourceKey(params);

        if (count > 0) {
            return true;
        }

        return false;
    }


    public void updateI18nItemValue(String langCode, String projects, String sourceKey, String value) {
        Map<String, Object> params = new HashMap<>();
        String valueLanguage = "value_" + langCode;

        params.put("source_key", sourceKey);
        params.put(valueLanguage, value);
        params.put("projects",projects);

        i18nItemMapper.updateItemBySourceKey(params);
    }

    public void insertI18nItem(String langCode,String projects, String sourceKey, String value) throws Exception {
        Map<String, Object> params = new HashMap<>();
        String valueLanguage = "value_" + langCode;

        params.put("source_key", sourceKey);
        params.put("android_key", sourceKey);
        params.put("ios_key", sourceKey);

        params.put(valueLanguage, value);
        params.put("projects",projects);

        i18nItemMapper.addItem(createI18nItem(params));
    }


    private I18nItem createI18nItem(Map<String, ?> params) throws Exception {
        I18nItem entity = new I18nItem();
        entity = ObjectUtils.merge(entity, params);
        return entity;
    }

    public void insertI18nItem(Map<String, Object> data) throws Exception {
        i18nItemMapper.addItem(createI18nItem(data));
    }


    public void updateI18nItemValues(Map<String, Object> data) {
        i18nItemMapper.updateItem(data);
    }

    public I18nItem getI18nItemBySourceKey(String sourceKey) {

        Map<String, Object> params = new HashMap<>();

        params.put("source_key",sourceKey);

        List<I18nItem> result = i18nItemMapper.queryBySourceKey(params);

        if(result!=null && !result.isEmpty()){
            return  result.get(0);
        }

        return null;

    }


    public void updateI18nItemValue(String updateProjects, String sourceKey, Map<String, String> entryValues) {

        Map<String, Object> params = new HashMap<>();
        params.put("source_key", sourceKey);
        params.put("projects",updateProjects);
        params.putAll(entryValues);

        i18nItemMapper.updateItemBySourceKey(params);

    }

    public void insertI18nItem(String projects, String sourceKey, Map<String, String> entryValues) throws Exception {

        I18nItem item = createI18nItem(entryValues);
        item.setProjects(projects);
        item.setSource_key(sourceKey);
        i18nItemMapper.addItem(item);

    }

    public void updateAndroidKey(Map<String, String> data) {
        Set<Map.Entry<String, String>> entrySet = data.entrySet();

        for (Map.Entry<String, String> entry:entrySet){

            String sourceKey = entry.getKey();
            String androidKey = entry.getValue();

            if (!StringUtils.isEmpty(sourceKey) && !StringUtils.isEmpty(androidKey) ){

                Map<String, Object> params = new HashMap<>();
                params.put("source_key", sourceKey);
                params.put("android_key",androidKey);
                i18nItemMapper.updateItemBySourceKey(params);

            }

        }

    }
}
