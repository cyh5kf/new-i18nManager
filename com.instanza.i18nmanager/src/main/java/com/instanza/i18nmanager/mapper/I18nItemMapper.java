package com.instanza.i18nmanager.mapper;


import com.instanza.i18nmanager.entity.I18nItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface I18nItemMapper {

    /**
     * 查询
     */
    List<I18nItem> queryItemList(@Param("param") Map<String, Object> paramMap);


    /**
     * 查询数据总量
     */
    int queryItemListCount(@Param("param") Map<String, Object> paramMap);

    /**
     * 查询数据总量
     */
    int queryCountBySourceKey(@Param("param") Map<String, Object> paramMap);

    /**
     * 新增
     */
    int addItem(I18nItem entity);

    /**
     * 更新
     */
    int updateItem(@Param("param") Map<String, Object> paramMap);

    /**
     * 删除
     */
    int deleteItem(@Param("item_id") long item_id);

    void updateItemBySourceKey(@Param("param") Map<String, Object> params);

    /**
     * 以SourceKey查询
     */
    List<I18nItem> queryBySourceKey(@Param("param") Map<String, Object> params);
}
