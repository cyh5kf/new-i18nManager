package com.instanza.i18nmanager.controller;


import com.alibaba.fastjson.JSON;
import com.instanza.i18nmanager.constants.LangCodeMap;
import com.instanza.i18nmanager.entity.I18nItem;
import com.instanza.i18nmanager.entity.I18nProject;
import com.instanza.i18nmanager.mapper.I18nItemMapper;
import com.instanza.i18nmanager.mapper.I18nProjectMapper;
import com.instanza.i18nmanager.service.I18nCheckService;
import com.instanza.i18nmanager.service.I18nExportService;
import com.instanza.i18nmanager.service.I18nImportService;
import com.instanza.i18nmanager.service.I18nItemService;
import com.instanza.i18nmanager.service.model.*;
import com.instanza.i18nmanager.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping(value = "/i18nmanager/api/i18nitem")
public class I18nController {


    @Autowired
    private I18nItemService i18nItemService;

    @Autowired
    private I18nImportService i18nImportService;

    @Autowired
    private I18nItemMapper i18nItemMapper;

    @Autowired
    private I18nExportService i18nExportService;

    @Autowired
    private I18nProjectMapper i18nProjectMapper;

    @Autowired
    private I18nCheckService i18nCheckService;


    @RequestMapping(value = "queryItem")
    public ResponseEntity<?> queryItem(@RequestBody Map<String, Object> paramMap) {

        List<I18nItem> itemList = i18nItemMapper.queryItemList(paramMap);
        int total = i18nItemMapper.queryItemListCount(paramMap);

        PaginationResult result = new PaginationResult<>(itemList, total);

        return ResponseEntity.ok(result);
    }


    @RequestMapping(value = "addItem")
    public ResponseEntity<?> addItem(@RequestBody Map<String, Object> data) {
        try {

            String source_key = data.get("source_key").toString();

            if (i18nItemService.hasSourceKey(source_key)) {
                throw new MessageException("KeyIsExist");
            }

            i18nItemService.insertI18nItem(data);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("theKeyIsAlreadyExist", HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok("ok");
    }

    @RequestMapping(value = "deleteItem")
    public ResponseEntity<?> deleteItem(@RequestParam(value = "id") String id) {
        Long idLong = Long.parseLong(id);
        i18nItemMapper.deleteItem(idLong);
        return ResponseEntity.ok("ok");
    }


    @RequestMapping(value = "getLanguages")
    public ResponseEntity<?> getLanguages() {
        Map<String, String> codeNameMap = LangCodeMap.getCodeNameMap();
        List<String> codes = LangCodeMap.getCodes();
        Map<String, Object> map = new HashMap<>();
        map.put("codeNameMap", codeNameMap);
        map.put("codes", codes);
        return ResponseEntity.ok(map);
    }


    //导入数据
    @RequestMapping(value = "importUpload", produces = "text/html;charset=UTF-8")
    public ResponseEntity<?> checkImport(@RequestParam(value = "langName") String langName, @RequestParam(value = "projects") String projects, @RequestParam(value = "file") MultipartFile file) throws Exception {

        //projects :  #5#6#8#

        try {

            i18nImportService.doImportFile(langName, projects, file);
        } catch (MessageException e) {
            e.printStackTrace();
            Map<String, Object> emap = e.toDescMap();

            String ejson = JSON.toJSONString(emap);

            return ResponseEntity.ok(ejson);
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        return ResponseEntity.ok("ok");
    }



    //批量导入数据
    @RequestMapping(value = "batchImportUpload", produces = "text/html;charset=UTF-8")
    public ResponseEntity<?> batchImportUpload(@RequestParam(value = "projects") String projects, @RequestParam(value = "file") MultipartFile file) throws Exception {

        //projects :  #5#6#8#

        try {

            List<I18nImportFormatMO> files = ImportZipUtils.unzipFile(file);

            i18nImportService.doBatchImportFile(projects, files);

        } catch (MessageException e) {
            e.printStackTrace();
            Map<String, Object> emap = e.toDescMap();

            String ejson = JSON.toJSONString(emap);

            return ResponseEntity.ok(ejson);
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        return ResponseEntity.ok("ok");
    }



    //批量导入数据,使用Excel文件
    @RequestMapping(value = "batchImportUploadByExcel", produces = "text/html;charset=UTF-8")
    public ResponseEntity<?> batchImportUploadByExcel(@RequestParam(value = "projects") String projects, @RequestParam(value = "file") MultipartFile file) throws Exception {

        //projects :  #5#6#8#

        try {

            I18nImportExcelMO excelMO = ImportExcelUtils.parse(file);
            System.out.println("Will Import "+excelMO.getRowList().size() + " Items from Excel");
            i18nImportService.doBatchImportFile(projects, excelMO);
            System.out.println("Finished Import "+excelMO.getRowList().size() + " Items from Excel");

        } catch (MessageException e) {
            e.printStackTrace();
            Map<String, Object> emap = e.toDescMap();

            String ejson = JSON.toJSONString(emap);

            return ResponseEntity.ok(ejson);
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        return ResponseEntity.ok("ok");
    }




    //更新修改
    @RequestMapping(value = "updateItemById")
    public ResponseEntity<?> updateI18nItemById(@RequestBody Map<String, Object> data) {
        String id = data.get("id").toString();//肯定有个Id字段

        String source_key = data.get("source_key").toString();

        I18nItem oldI18nItem = i18nItemService.getI18nItemBySourceKey(source_key);
        if (oldI18nItem != null && !oldI18nItem.getId().toString().equals(id)) {

            return new ResponseEntity<>("theKeyIsAlreadyExist", HttpStatus.BAD_REQUEST);
        }

        i18nItemService.updateI18nItemValues(data);
        return ResponseEntity.ok("");
    }


    //导出
    @RequestMapping(value = "exportProjectI18nResource", produces = "application/octet-stream; charset=UTF-8")
    public ResponseEntity<?> exportProjectI18nResource(
//            @RequestParam(value = "options",required = false,defaultValue = "") String options,
            @RequestParam(value = "projectId") String projectId,
            @RequestParam(value = "langCode") String langCode,
            @RequestParam(value = "format") String format, //android , ios ,web
            HttpServletResponse response) throws Exception {

        String options = "";//TODO
        I18nExportOptions i18nExportOptions = null;
        if (!StringUtils.isEmpty(options)){
            i18nExportOptions = JSON.parseObject(options,I18nExportOptions.class);
        }


        Map<String, Object> queryCondition = new HashMap<>();
        queryCondition.put("projects", "#" + projectId + "#");
        queryCondition.put("limitStart", 0);
        queryCondition.put("limitSize", 10000000);

        List<I18nItem> items = i18nItemMapper.queryItemList(queryCondition);

        I18nExportFormatMO mo = i18nExportService.convertExportFormat(items, langCode, format,i18nExportOptions);

        ExportFileUtil.doExportTextFile(response, mo.getFileName(), mo.getContent());

        return ResponseEntity.ok("ok");
    }


    //导出所有
    @RequestMapping(value = "exportProjectAllI18nResource", produces = "application/octet-stream; charset=UTF-8")
    public ResponseEntity<?> exportProjectAllI18nResource(
//            @RequestParam(value = "options",required = false,defaultValue = "") String options,
            @RequestParam(value = "projectId") String projectId,
            @RequestParam(value = "exportType",required = false,defaultValue = "") String exportType,
            HttpServletResponse response) throws Exception {


        Map<String, Object> queryCondition = new HashMap<>();
        queryCondition.put("projects", "#" + projectId + "#");
        queryCondition.put("limitStart", 0);
        queryCondition.put("limitSize", 10000000);
        List<I18nItem> items = i18nItemMapper.queryItemList(queryCondition);


        long projectIdLong = Long.parseLong(projectId);

        I18nProject i18nProject = i18nProjectMapper.queryById(projectIdLong);

        List<I18nExportFormatMO> moList = i18nExportService.convertExportFormatAll(i18nProject, items,exportType);

        String projectName = i18nProject.getName().replaceAll(" ","_");
        ExportFileUtil.doExportTextFileZip(response, moList, "" + projectName +"__i18n_export_" +System.currentTimeMillis() + ".zip");

        return ResponseEntity.ok("ok");
    }




    @RequestMapping(value = "findOutErrorItems")
    public ResponseEntity<?> findOutErrorItems(
            @RequestParam(value = "projectId") Long projectId,
            @RequestParam(value = "langCodeList",required = false,defaultValue = "") String langCodeList
    ) throws Exception {

        List<String> langCodeList1 ;
        if (StringUtils.isEmpty(langCodeList)){
            I18nProject project = i18nProjectMapper.queryById(projectId);
            langCodeList1 = MultiValueUtil.parseMultiValue(project.getLanguages());
        }else {
            langCodeList1 = Arrays.asList(langCodeList.split(","));
        }

        List<I18nCheckErrorVO> result = i18nCheckService.checkOutErrorItems(projectId, langCodeList1,true);
        return ResponseEntity.ok(result);
    }



    @RequestMapping(value = "updateAndroidKey")
    public ResponseEntity<?> updateAndroidKey(@RequestBody Map<String, String> data) throws Exception {
        i18nItemService.updateAndroidKey(data);
        return ResponseEntity.ok("ok");
    }




    @RequestMapping(value = "hello")
    public ResponseEntity<?> hello() {
        return ResponseEntity.ok("hello");
    }
}
