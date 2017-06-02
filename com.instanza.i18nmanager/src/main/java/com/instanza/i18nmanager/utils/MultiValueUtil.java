package com.instanza.i18nmanager.utils;

import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by luanhaipeng on 16/12/15.
 */
public class MultiValueUtil {
    public static List<String> parseMultiValue(String multiValueString) {

        if (StringUtils.isEmpty(multiValueString)){
            return new ArrayList<>();
        }

        List<String> result = new ArrayList<>();

        String[] arr1 = multiValueString.split("#");
        for (String obj :arr1){
            if (!StringUtils.isEmpty(obj)){
                result.add(obj);
            }
        }

        return result;
    }
}
