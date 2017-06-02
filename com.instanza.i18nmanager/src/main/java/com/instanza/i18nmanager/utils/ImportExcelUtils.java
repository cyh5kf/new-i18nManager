package com.instanza.i18nmanager.utils;

import com.instanza.i18nmanager.constants.LangCodeMap;
import com.instanza.i18nmanager.service.model.I18nImportExcelMO;
import org.apache.poi.ss.usermodel.*;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.*;

/**
 * Created by luanhaipeng on 16/12/19.
 */
public class ImportExcelUtils {
    public static I18nImportExcelMO parse(MultipartFile file) throws Exception {

        if (!isFileType(file.getOriginalFilename())) {
            throw new MessageException("文件不合法，请上传Excel文件");
        }

        InputStream inputStream = file.getInputStream();
        Workbook workbook = WorkbookFactory.create(inputStream);
        Sheet sheet = workbook.getSheetAt(workbook.getFirstVisibleTab());
        Row row = sheet.getRow(sheet.getFirstRowNum());

        Map<Integer, String> headMap = processSheetHead(row);

        int rowSize = sheet.getLastRowNum();
        Collection<Row> rowList = new ArrayList<Row>();

        for (int r = 1 + sheet.getFirstRowNum(); r <= rowSize; r++) {
            rowList.add(sheet.getRow(r));
        }

        return toI18nImportExcelMO(headMap, rowList);

    }


    private static I18nImportExcelMO toI18nImportExcelMO(Map<Integer, String> headMap, Collection<Row> rowList) throws MessageException {

        Map<Integer, String> fieldNameIndex = toFieldNameIndex(headMap);

        I18nImportExcelMO result = new I18nImportExcelMO();

        int columnSize = headMap.size();

        for (Row row : rowList) {
            if (row != null) {
                Map<String, String> rowItem = parseRow(columnSize, row, fieldNameIndex);
                if (rowItem!=null && !rowItem.isEmpty()){
                    result.getRowList().add(rowItem);
                }
            }
        }

        return result;
    }


    private static Map<Integer, String> toFieldNameIndex(Map<Integer, String> headMap) throws MessageException {
        Map<Integer, String> result = new HashMap<>();

        Set<Map.Entry<Integer, String>> entrySet = headMap.entrySet();
        for (Map.Entry<Integer, String> entry : entrySet) {

            Integer index = entry.getKey();
            String text = entry.getValue();
            if ("Key".equalsIgnoreCase(text)) {
                result.put(index, "source_key");
            } else {

                String langCode = LangCodeMap.getCodeByName(text);
                if (!StringUtils.isEmpty(langCode)) {
                    result.put(index, "value_" + langCode);
                }else {
                    throw new MessageException("列名" + text + "，不是正确的列，请检查");
                }

            }
        }

        if (!result.containsValue("source_key")){
            throw new MessageException("Key 列不存在");
        }


        return result;

    }


    private static Map<String, String> parseRow(int columnSize ,Row row, Map<Integer, String> fieldNameIndex) {

        Map<String, String> result = new HashMap<>();

        for (int c = 0; c < columnSize; c++) {

            Cell cell = row.getCell(c);

            String cellString = getStringValue(cell);
            String fieldName = fieldNameIndex.get(c);

            if (!StringUtils.isEmpty(cellString) && !StringUtils.isEmpty(fieldName)) {
                result.put(fieldName, cellString);
            }

        }

        return result;
    }


    private static boolean isFileType(String fileName) {
        if (fileName.endsWith(".xls") || fileName.endsWith(".xlsx")) {
            return true;
        }
        return false;
    }

    private static String getStringValue(Cell cell) {
        if (cell == null) {
            return null;
        }

        String value = "";
        int cellType = cell.getCellType();
        if (cellType == Cell.CELL_TYPE_FORMULA) {
            cellType = cell.getCachedFormulaResultType();
        }

        switch (cellType) {
            case Cell.CELL_TYPE_BLANK:
                value = "";
                break;
            case Cell.CELL_TYPE_STRING:
                value = cell.getRichStringCellValue().getString();
                break;
            case Cell.CELL_TYPE_NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    value = cell.getDateCellValue().toString();
                } else {
                    value = "" + cell.getNumericCellValue();
                }
                break;
            case Cell.CELL_TYPE_BOOLEAN:
                value = "" + cell.getBooleanCellValue();
                break;
            default:
                break;
        }
        return value.trim();
    }

    private static Map<Integer, String> processSheetHead(Row row) {

        Map<Integer, String> headMap = new LinkedHashMap<>();
        if (row == null) {
            return headMap;
        }


        int columnSize = row.getLastCellNum();

        for (int c = 0; c < columnSize; c++) {
            Cell cell = row.getCell(c);
            String headString = getStringValue(cell);
            if (StringUtils.isEmpty(headString))
                continue;
            if (headString.startsWith("#"))
                continue;

            headMap.put(c, headString);
        }
        return headMap;
    }

}
