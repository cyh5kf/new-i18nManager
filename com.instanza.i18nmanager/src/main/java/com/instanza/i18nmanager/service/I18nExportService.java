package com.instanza.i18nmanager.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.instanza.i18nmanager.constants.LangCodeMap;
import com.instanza.i18nmanager.entity.I18nItem;
import com.instanza.i18nmanager.entity.I18nProject;
import com.instanza.i18nmanager.service.model.I18nExportFormatMO;
import com.instanza.i18nmanager.service.model.I18nExportOptions;
import com.instanza.i18nmanager.utils.ReflectUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class I18nExportService {


    //导出所有
    public List<I18nExportFormatMO> convertExportFormatAll(I18nProject i18nProject, List<I18nItem> items, String exportType) throws Exception {


        List<I18nExportFormatMO> result = new ArrayList<>();


        String languagesString = i18nProject.getLanguages();

        String[] languageArray = languagesString.split("#");

        String projectType = i18nProject.getType();

        for (String langCode : languageArray) {


            if (!StringUtils.isEmpty(langCode) && LangCodeMap.getNameByCode(langCode)!=null) {


                if("js".equals(exportType)){

                    I18nExportFormatMO singleExportMo2 = this.convertExportFormat(items, langCode, "js", null);
                    singleExportMo2.setFileName(toBatchExportName("js",langCode));
                    result.add(singleExportMo2);

                }else {

                    String format = projectType;
                    if ("Mobile".equalsIgnoreCase(projectType)){

                        format = "android";
                        I18nExportFormatMO singleExportMo1 = this.convertExportFormat(items, langCode, format, null);
                        singleExportMo1.setFileName(toBatchExportName(format,langCode));
                        result.add(singleExportMo1);


                        format = "ios";
                        I18nExportFormatMO singleExportMo2 = this.convertExportFormat(items, langCode, format, null);
                        singleExportMo2.setFileName(toBatchExportName(format,langCode));
                        result.add(singleExportMo2);
                    }

                    else {
                        I18nExportFormatMO singleExportMo2 = this.convertExportFormat(items, langCode, format, null);
                        singleExportMo2.setFileName(toBatchExportName(format,langCode));
                        result.add(singleExportMo2);
                    }
                }
            }

        }


        return result;
    }


    private static String toBatchExportName(String format,String langCode){

        //string_uk.xml  Localizable_ja.strings

        String fileName = null;
        if ("android".equalsIgnoreCase(format)) {
            fileName = "string_" + langCode + ".xml";
        } else if ("ios".equalsIgnoreCase(format)) {
            fileName = "Localizable_" + langCode + ".strings";
        } else if ("js".equalsIgnoreCase(format)){
            fileName = langCode + ".js";
        }else {
            fileName = "" + langCode + ".json";
        }

        return fileName;
    }



    //到处单个语言，单个格式
    public I18nExportFormatMO convertExportFormat(List<I18nItem> items, String langCode, String format, I18nExportOptions i18nExportOptions) throws Exception {

        items = applyI18nExportOptions(items,i18nExportOptions);

        String langName = LangCodeMap.getNameByCode(langCode);
        StringBuffer content = null;
        String fileName = null;

        if ("android".equalsIgnoreCase(format)) {
            content = this.getExportAndroidContent(items, langCode);
            fileName = "Android_" + langName + "_" + System.currentTimeMillis() + ".xml";
        } else if ("ios".equalsIgnoreCase(format)) {
            content = this.getExportIOSContent(items, langCode);
            fileName = "ios_" + langName + "_" + System.currentTimeMillis() + ".strings";
        } else if("js".equalsIgnoreCase(format)){
            content = this.getExportJSContent(items, langCode);
            fileName = "Web_" + langName + "_" + System.currentTimeMillis() + ".js";
        }else {
            content = this.getExportWebContent(items, langCode);
            fileName = "Web_" + langName + "_" + System.currentTimeMillis() + ".json";
        }

        return new I18nExportFormatMO(fileName, content.toString().getBytes());

    }


    private List<I18nItem> applyI18nExportOptions(List<I18nItem> items, I18nExportOptions i18nExportOptions) throws Exception {

        if (i18nExportOptions == null) {
            return items;
        }

        if (StringUtils.isEmpty(i18nExportOptions.getValueReplaceSource())) {
            return items;
        }

        List<String> langCodeList = LangCodeMap.getCodes();

        for (I18nItem i18n : items) {

            if (null != i18n) {

                for (String langCode : langCodeList) {

                    String fieldName = "value_" + langCode;

                    String value = (String) ReflectUtils.getFieldValue(i18n, fieldName);

                    if (!StringUtils.isEmpty(value)) {

                        value = value.replaceAll(i18nExportOptions.getValueReplaceSource(), i18nExportOptions.getValueReplaceTarget());

                        ReflectUtils.setFieldValue(i18n, fieldName, value);

                    }
                }
            }

        }

        return items;

    }



    private StringBuffer getExportAndroidContent(List<I18nItem> items, String langCode) throws Exception {
        StringBuffer content = new StringBuffer();
        content.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
        content.append("<resources xmlns:xliff=\"urn:oasis:names:tc:xliff:document:1.2\">\n");


        for (I18nItem i18n : items) {
            if (null == i18n) {
                continue;
            }

            String value = (String) ReflectUtils.getFieldValue(i18n, "value_" + langCode);
            value = (value == null) ? "" : value;

            value = value.replaceAll("&", "&amp;").replaceAll("%@", "%s").replaceAll("\\.\\.\\.", "&#8230;").replaceAll("\'", "\\\\\\\'");
            value = value.replaceAll("<","&lt;");
            value = value.replaceAll(">","&gt;");
            value = value.replace("\\U200E","\\u200E");
            value = value.replace("\\U200F","\\u200F");
            if (value.contains("%")) {
                content.append("\t<string name=\"" + i18n.getAndroid_key() + "\" formatted=\"false\">"
                        + value + "</string>\n");
            } else {
                content.append("\t<string name=\"" + i18n.getAndroid_key() + "\">"
                        + value + "</string>\n");
            }
        }


        content.append("</resources>\n");

        return content;
    }

    private StringBuffer getExportJSContent(List<I18nItem> items, String langCode) throws Exception {

        Map<String, String> map = new HashMap<>();


        for (I18nItem i18n : items) {
            if (null == i18n) {
                continue;
            }


            String value = (String) ReflectUtils.getFieldValue(i18n, "value_" + langCode);

            value = value.replace("\\\"", "\"");

            value = value.replace("\\U200E", "");
            value = value.replace("\\U200F", "");
            map.put(i18n.getIos_key(), value);

        }


        StringBuffer content = new StringBuffer();

        content.append("export default ");

        content.append(JSON.toJSONString(map, SerializerFeature.PrettyFormat));

        content.append(";");

        return content;
    }

    private StringBuffer getExportWebContent(List<I18nItem> items, String langCode) throws Exception {
        StringBuffer content = new StringBuffer();
        content.append("{\n");
        for (I18nItem i18n : items) {
            if (null == i18n) {
                continue;
            }

            String value = (String) ReflectUtils.getFieldValue(i18n, "value_" + langCode);
            value = (value == null) ? "" : value;
            value = value.replace("\n", "");
            value = value.replace("\r", "");
            value = value.replace("\\U200E", "");
            content.append("\t\"" + i18n.getSource_key() + "\"" + " : " + "\"" + value + "\",");
            content.append("\n");
        }

        content.append("}\n");

        return content;
    }

    private StringBuffer getExportIOSContent(List<I18nItem> items, String langCode) throws Exception {

        StringBuffer content = new StringBuffer();
        content.append("{\n");
        for (I18nItem i18n : items) {
            if (null == i18n) {
                continue;
            }

            String value = (String) ReflectUtils.getFieldValue(i18n, "value_" + langCode);
            value = (value == null) ? "" : value;

//			i18n.value = i18n.value.replaceAll("\"", "\\\\\"");
            content.append("\t\"" + i18n.getIos_key() + "\"" + " = " + "\""
                    + value + "\";");
            content.append("\n");
        }

        content.append("}\n");

        return content;
    }

}
