package com.instanza.i18nmanager.service;

import com.instanza.i18nmanager.constants.LangCodeMap;
import com.instanza.i18nmanager.entity.I18nItem;
import com.instanza.i18nmanager.service.model.I18nImportExcelMO;
import com.instanza.i18nmanager.service.model.I18nImportFormatMO;
import com.instanza.i18nmanager.utils.KeyValuePair;
import com.instanza.i18nmanager.utils.MessageException;
import com.instanza.i18nmanager.utils.ParseStringFiles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Created by luanhaipeng on 16/12/7.
 */
@Service
public class I18nImportService {


    @Autowired
    private I18nItemService i18nItemService;


    //批量导入
    public void doBatchImportFile(String projects, List<I18nImportFormatMO> files) throws Exception {
        Map<String,Map<String, String>> values = toBatchImportValuesMap(files);

        Set<Map.Entry<String, Map<String, String>>> entries = values.entrySet();
        for (Map.Entry<String, Map<String, String>> entry:entries){

            String sourceKey = entry.getKey();
            Map<String, String> entryValues = entry.getValue();

            this.importOneItem(sourceKey,projects,entryValues);
        }

    }


    //批量导入
    public void doBatchImportFile(String projects, I18nImportExcelMO excelMO) throws Exception {

        List<Map<String, String>> rowList = excelMO.getRowList();
        for (Map<String, String> entryValues:rowList){

            String sourceKey = entryValues.get("source_key");

            if (!StringUtils.isEmpty(entryValues)){

                entryValues.put("android_key",sourceKey);
                entryValues.put("ios_key",sourceKey);

                this.importOneItem(sourceKey,projects,entryValues);
            }
            else {
                System.err.println("doBatchImportFile:: source_key is Empty");
            }

        }
    }



    private void importOneItem(String sourceKey,String projects,Map<String, String> entryValues) throws Exception {
        I18nItem i18nItem = i18nItemService.getI18nItemBySourceKey(sourceKey);
        if (i18nItem!=null) {
            //update
            String updateProjects = mergeProjects(i18nItem.getProjects(),projects);
            i18nItemService.updateI18nItemValue(updateProjects, sourceKey, entryValues);
        } else {
            //insert
            i18nItemService.insertI18nItem(projects,sourceKey, entryValues);
        }
    }




    private Map<String,Map<String, String>> toBatchImportValuesMap(List<I18nImportFormatMO> files) {

        //key-->source_key  value:Object
        Map<String, Map<String, String>> result = new HashMap<>();

        for (I18nImportFormatMO mo : files) {


            String langName = mo.getLangName();

            String langCode = LangCodeMap.getCodeByName(langName);

            List<KeyValuePair> keyValuePairList = mo.getKeyValuePairList();

            for (KeyValuePair keyValuePair : keyValuePairList) {

                String sourceKey = keyValuePair.getKey();

                Map<String, String> objectMap = result.get(sourceKey);

                if (objectMap == null) {
                    objectMap = new HashMap<>();
                    objectMap.put("source_key", sourceKey);
                    objectMap.put("android_key", sourceKey);
                    objectMap.put("ios_key", sourceKey);
                    result.put(sourceKey, objectMap);
                }

                String value = keyValuePair.getValue();
                objectMap.put("value_" + langCode, value);
            }

        }

        return result;
    }






    public void doImportFile(String languageName,String projects, MultipartFile file) throws MessageException,Exception {
        List<KeyValuePair> pairList = doParseFile(file);
        doImportKeyValuePair(languageName,projects, pairList);
    }

    private List<KeyValuePair> doParseFile(MultipartFile file) throws Exception {
        InputStream is = file.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        return ParseStringFiles.doParse(br);
    }


    private void doImportKeyValuePair(String languageName, String projects,List<KeyValuePair> pairList) throws MessageException,Exception {

        checkDuplicateKey(pairList);

        String langCode = LangCodeMap.getCodeByName(languageName);

        for (KeyValuePair pair : pairList) {
             I18nItem i18nItem = i18nItemService.getI18nItemBySourceKey(pair.getKey());
            if (i18nItem!=null) {
                //update
                String updateProjects = mergeProjects(i18nItem.getProjects(),projects);
                i18nItemService.updateI18nItemValue(langCode,updateProjects, pair.getKey(), pair.getValue());
            } else {
                //insert
                i18nItemService.insertI18nItem(langCode, projects,pair.getKey(), pair.getValue());
            }
        }

    }

    private String mergeProjects(String projects1, String projects2) {

        Set<String> stringSet = new HashSet<>();

        if (!StringUtils.isEmpty(projects1)) {
            String[] mm1 = projects1.split("#");
            if (mm1 != null && mm1.length > 0) {
                List<String> mm11 = Arrays.asList(mm1);
                stringSet.addAll(mm11);
            }
        }


        if (!StringUtils.isEmpty(projects2)) {
            String[] mm2 = projects2.split("#");
            if (mm2 != null && mm2.length > 0) {
                List<String> mm22 = Arrays.asList(mm2);
                stringSet.addAll(mm22);
            }
        }


        if (stringSet.size() > 0) {
            StringBuffer resultString = new StringBuffer("#");
            for (String s : stringSet) {
                if (s != null && !s.isEmpty()) {
                    resultString.append(s);
                    resultString.append("#");
                }
            }
            return resultString.toString();
        }
        return "";

    }


    private void checkDuplicateKey(List<KeyValuePair> pairList) throws MessageException {
        Map<String, Boolean> map = new HashMap<>();

        StringBuffer DuplicateKeyString = new StringBuffer();

        List<String> DuplicateKeyList = new ArrayList<>();

        boolean hasDuplicateKey = false;

        for (KeyValuePair p : pairList) {
            Boolean m = map.get(p.getKey());
            if (m != null) {
                DuplicateKeyString.append(p.getKey());
                DuplicateKeyString.append("#");

                DuplicateKeyList.add(p.getKey());

                hasDuplicateKey = true;
            }

            map.put(p.getKey(),true);
        }


        if (hasDuplicateKey){
            throw new MessageException("DuplicateKey", DuplicateKeyList);
        }

    }


}
