package com.instanza.i18nmanager.constants;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class LangCodeMap {

    private static final Map<String, String> nameCodeMap = new LinkedHashMap<>();
    private static final Map<String, String> codeNameMap = new LinkedHashMap<>();
    private static final List<String> langNameList = new ArrayList<>();
    private static final List<String> langCodeList = new ArrayList<>();

    private static void init() {

        putMap("English", "en");
        putMap("Chinese", "zh");
        putMap("Arabic", "ar");
        putMap("Spanish", "es");
        putMap("Portuguese", "pt");
        putMap("Russian", "ru");
        putMap("Polish", "pl");
        putMap("Persian", "fa");
        putMap("Malay", "ms");
        putMap("Dutch", "nl");
        putMap("Thai", "th");
        putMap("Turkish", "tr");
        putMap("Ukrainian", "uk");
        putMap("Vietnamese", "vi");
        putMap("French", "fr");
        putMap("German", "de");
        putMap("Italian", "it");
        putMap("Japanese", "ja");
        putMap("Hindi", "hi");
        putMap("Hungarian", "hu");
        putMap("Indonesian", "id");
        putMap("Korean", "ko");
        putMap("Norwegian", "nb");
        putMap("Catalan", "ca");
        putMap("Croatian", "hr");
        putMap("Czech", "cs");
        putMap("Danish", "da");
        putMap("Finnish", "fi");
        putMap("Greek", "el");
        putMap("Hebrew", "he");
        putMap("Romanian", "ro");
        putMap("Slovak", "sk");

    }

    private static void putMap(String name, String code) {
        codeNameMap.put(code, name);
        nameCodeMap.put(name, code);
        langNameList.add(name);
        langCodeList.add(code);
    }

    static {
        init();
    }

    public static List<String> getNames() {
        return langNameList;
    }

    public static List<String> getCodes() {
        return langCodeList;
    }

    public static String getCodeByName(String name) {
        return nameCodeMap.get(name);
    }

    public static String getNameByCode(String code) {
        return codeNameMap.get(code);
    }

    public static Map<String, String> getNameCodeMap() {
        return nameCodeMap;
    }

    public static Map<String, String> getCodeNameMap() {
        return codeNameMap;
    }
}
