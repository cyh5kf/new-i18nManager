package com.instanza.i18nmanager.utils;

import java.lang.reflect.Field;
import java.util.Map;


public class ObjectUtils {


    /**
     * 将Map转成java对象
     *
     * @param instance
     * @param map
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public static <T> T merge(T instance, Map<String, ?> map) throws InstantiationException, IllegalAccessException {

        Field[] fields = ReflectUtils.getObjectFields(instance.getClass(), ReflectUtils.FIELD_FILTER_ONLY_BASIC);


        for (Field f : fields) {

            f.setAccessible(true);

            String fieldName = f.getName();

            Object value = map.get(fieldName);
            if (value != null) {
                f.set(instance, value);
            }
        }

        return instance;
    }


}
