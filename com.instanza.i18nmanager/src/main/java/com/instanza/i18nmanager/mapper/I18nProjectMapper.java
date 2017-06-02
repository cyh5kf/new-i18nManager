package com.instanza.i18nmanager.mapper;

import com.instanza.i18nmanager.entity.I18nProject;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * Created by luanhaipeng on 16/12/7.
 */

@Mapper
public interface I18nProjectMapper {

    List<I18nProject> queryAll();

    int addProject(I18nProject p);


    int updateProject(@Param("param") Map<String, Object> params);


    int deleteProject(@Param("item_id") long item_id);

    I18nProject queryById(@Param("item_id") long item_id);

}
