package com.instanza.i18nmanager.controller;

import com.instanza.i18nmanager.entity.I18nProject;
import com.instanza.i18nmanager.mapper.I18nProjectMapper;
import com.instanza.i18nmanager.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/i18nmanager/api/i18nproject")
public class I18nProjectController {

    @Autowired
    private I18nProjectMapper i18nProjectMapper;

    @RequestMapping(value = "getProjectList")
    public ResponseEntity<?> getProjectList() {
        List<I18nProject> result = i18nProjectMapper.queryAll();
        return ResponseEntity.ok(result);
    }


    @RequestMapping(value = "addProject")
    public ResponseEntity<?> addProject(@RequestBody Map<String, Object> data) {
        I18nProject i18nProject = new I18nProject();
        try {

            i18nProject = ObjectUtils.merge(i18nProject, data);

            i18nProjectMapper.addProject(i18nProject);

        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return ResponseEntity.ok("");
    }


    @RequestMapping(value = "updateProject")
    public ResponseEntity<?> updateProject(@RequestBody Map<String, Object> data) {

        i18nProjectMapper.updateProject(data);

        return ResponseEntity.ok("ok");
    }


}
