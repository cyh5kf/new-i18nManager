package com.instanza.i18nmanager.utils;

import com.instanza.i18nmanager.constants.LangCodeMap;
import com.instanza.i18nmanager.service.model.I18nImportFormatMO;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * 解压文件
 * Created by luanhaipeng on 16/12/14.
 */
public class ImportZipUtils {


    public static List<I18nImportFormatMO> unzipFile(MultipartFile file) throws Exception {
        InputStream is = file.getInputStream();
        return unzipFile(is);
    }

    private static List<I18nImportFormatMO> unzipFile(InputStream is) throws Exception {

        String tempFileName = "./tmp/upload_zip_" + System.currentTimeMillis() + ".zip";

        String tempFileFolder = "./tmp/";

        File f = new File(tempFileFolder);
        if (!f.exists()) {
            f.mkdirs();
        }

        File file = new File(tempFileName);
        if (!file.exists()) {
            file.createNewFile();
        }


        inputStreamToFile(is, file);


        List<I18nImportFormatMO> result = new ArrayList<>();
        ZipFile zip = new ZipFile(file);


        for (Enumeration entries = zip.entries(); entries.hasMoreElements(); ) {

            ZipEntry entry = (ZipEntry) entries.nextElement();
            String zipEntryName = entry.getName();

            InputStream in = zip.getInputStream(entry);

            I18nImportFormatMO i18nImportFormatMO = toI18nImportFormatMO(zipEntryName, in);
            if(i18nImportFormatMO!=null){
                result.add(i18nImportFormatMO);
            }

            in.close();

        }

        file.delete();

        return result;

    }


    private static void inputStreamToFile(InputStream ins, File file) throws IOException {
        OutputStream os = new FileOutputStream(file);
        int bytesRead = 0;
        byte[] buffer = new byte[8192];
        while ((bytesRead = ins.read(buffer, 0, 8192)) != -1) {
            os.write(buffer, 0, bytesRead);
        }
        os.close();
        ins.close();
    }


    private static I18nImportFormatMO toI18nImportFormatMO(String zipEntryName, InputStream in) throws Exception {

        String langName = parseFileLangName(zipEntryName);
        if (StringUtils.isEmpty(langName)){
            return null;
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        List<KeyValuePair> keyValuePairList = ParseStringFiles.doParse(br);
        if (keyValuePairList==null || keyValuePairList.isEmpty()){
            return null;
        }

        return new I18nImportFormatMO(langName, zipEntryName, keyValuePairList);
    }


    private static String parseFileLangName(String zipEntryName) {
//        String[] nameArr = zipEntryName.split("_");
//        String name = nameArr[0];
//        if (name.startsWith("Chinese")) {
//            name = "Chinese";
//        }
//
//
//        if (name.startsWith("English")) {
//            name = "English";
//        }

        String[] nameArr = zipEntryName.split("/");

        String mm = nameArr[nameArr.length-1];

        List<String> langNames = LangCodeMap.getNames();

        for (String name : langNames){
            if (mm.indexOf(name)==0){
                return name;
            }
        }


        return null;
    }


}
