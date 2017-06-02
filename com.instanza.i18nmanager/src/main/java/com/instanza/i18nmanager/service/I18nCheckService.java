package com.instanza.i18nmanager.service;

import com.instanza.i18nmanager.constants.LangCodeMap;
import com.instanza.i18nmanager.entity.I18nItem;
import com.instanza.i18nmanager.mapper.I18nItemMapper;
import com.instanza.i18nmanager.service.model.I18nCheckErrorVO;
import com.instanza.i18nmanager.utils.ReflectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by luanhaipeng on 16/12/15.
 */
@Service
public class I18nCheckService {

    @Autowired
    private I18nItemMapper i18nItemMapper;


    public List<I18nCheckErrorVO> checkOutErrorItems(Long projectId, List<String> langCodeList,boolean onlyError) throws Exception {


        Map<String, Object> queryCondition = new HashMap<>();
        queryCondition.put("projects", "#" + projectId + "#");
        queryCondition.put("limitStart", 0);
        queryCondition.put("limitSize", 10000000);
        List<I18nItem> itemList = i18nItemMapper.queryItemList(queryCondition);

        List<I18nCheckErrorVO> result = new ArrayList<>();

        for (I18nItem i18n:itemList){


            I18nCheckErrorVO vo = new I18nCheckErrorVO();
            vo.setSourceKey(i18n.getSource_key());


            for (String langCode : langCodeList) {

                String fieldName = "value_" + langCode;

                String value = (String) ReflectUtils.getFieldValue(i18n, fieldName);


                String langName = LangCodeMap.getNameByCode(langCode);

                if(StringUtils.isEmpty(value) || value.trim().isEmpty()){
                    vo.setError(true);
                    vo.getEmptyValueLangName().add(langName);
                }else {
                    vo.getFillValueLangName().add(langName);
                }

            }

            if (onlyError){
                if (vo.isError()){
                    result.add(vo);
                }
            }else {
                result.add(vo);
            }
        }


        return result;
    }

}
