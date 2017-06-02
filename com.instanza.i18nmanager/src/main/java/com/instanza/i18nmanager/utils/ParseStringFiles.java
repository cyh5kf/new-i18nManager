package com.instanza.i18nmanager.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by luanhaipeng on 16/12/7.
 */
public class ParseStringFiles {


    public static List<KeyValuePair> doParse(BufferedReader bufferedReader) throws Exception {

        List<String> lineData = new ArrayList<>();
        String line = null;
        while ((line = bufferedReader.readLine()) != null) {
            lineData.add(line);
        }

        List<KeyValuePair> result = parseI18NFromContent(lineData);

        return result;

    }


    public static List<KeyValuePair> parseI18NFromContent(List<String> lines) {
        if (null == lines || 0 == lines.size()) {
            return null;
        }

        List<KeyValuePair> i18nList = new ArrayList<>();

        if (null == lines) {
            return null;
        }

        int linesSize = lines.size();

        for (int i = 0; i < linesSize; i++) {
            String line = lines.get(i);
            if (line != null) {
                line = line.trim();
                if (line.startsWith("\"")) {
                    if (!line.endsWith("\";")) {
                        int lastIdx = line.lastIndexOf("//");
                        if (lastIdx != -1) {
                            line = line.substring(0, lastIdx).trim();
                        } else {
                            lastIdx = line.lastIndexOf("/*");
                            if (lastIdx != -1) {
                                line = line.substring(0, lastIdx).trim();
                            }
                        }
                    }
                    if (!line.endsWith("\";")) {
                        System.out.println("Error: " + i + ":" + line);
                        for (int j = i + 1; j < linesSize; j++) {
                            String xline = lines.get(j);
                            if (xline != null) {
                                xline = xline.trim();
                                if (xline.startsWith("\"")) {
                                    System.out.println("Error: " + j + ": " + xline);
                                }
                                line += "\r\n" + xline;
                                if (xline.endsWith("\";")) {
                                    i = j;
                                    break;
                                }
                            }
                        }
                    }
                    String separator = "\" = \"";
                    int idx = line.indexOf(separator);
                    if (-1 == idx) {
                        separator = "\"=\"";
                    }
                    idx = line.indexOf(separator);
                    if (idx != -1) {
                        String key = line.substring(1, idx);
                        String value = line.substring(idx + separator.length(), line.length() - 2);

                        i18nList.add(new KeyValuePair(key, value));
                    }
                }
            }
        }
        return i18nList;
    }

    private static KeyValuePair doParseLine(String line, String nextLine) {


        if (line != null) {
            line = line.trim();
            if (line.startsWith("\"")) {
                if (!line.endsWith("\";")) {
                    int lastIdx = line.lastIndexOf("//");
                    if (lastIdx != -1) {
                        line = line.substring(0, lastIdx).trim();
                    } else {
                        lastIdx = line.lastIndexOf("/*");
                        if (lastIdx != -1) {
                            line = line.substring(0, lastIdx).trim();
                        }
                    }
                }

                if (!line.endsWith("\";")) {
                    line = line + nextLine;
                }

                String separator = "\" = \"";
                int idx = line.indexOf(separator);
                if (-1 == idx) {
                    separator = "\"=\"";
                }
                idx = line.indexOf(separator);
                if (idx != -1) {
                    String key = line.substring(1, idx);
                    String value = line.substring(idx + separator.length(), line.length() - 2);

                    return new KeyValuePair(key, value);
                }
            }
        }


        return null;
    }

//
//    public static void main(String[] args) throws Exception {
//        String s = "/Users/luanhaipeng/Downloads/i18n_import_source_files/Arabic_Localizable_en__3_.strings";
//        File f = new File(s);
//
//        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(s)));
//
//        List<KeyValuePair> result = doParse(br);
//
//        System.out.println(result.size());
//    }
}
