import AjaxUtils from '../utils/AjaxUtils';


export const queryI18nItemList = function (paramMap) {//这玩意直接透传到数据库层.
    return AjaxUtils.doPostRequest('/i18nitem/queryItem',paramMap);
};

export const getI18nLanguagesRequest = function () {
    return AjaxUtils.doGetRequest('/i18nitem/getLanguages');
};


export const addI18nItemRequest = function (data) {
    return AjaxUtils.doPostRequest('/i18nitem/addItem',data);
};


export const updateI18nItemByIdRequest = function (data) {
    return AjaxUtils.doPostRequest('/i18nitem/updateItemById',data);
};


export const getImportI18nUploadURL = function () {
    //后台需要langName/projects/file三个参数
    return AjaxUtils.getRequestURL('/i18nitem/importUpload');//返回一个URL字符串
};


export const getBatchImportI18nUploadURL = function () {
    //后台需要projects/file两个参数
    return AjaxUtils.getRequestURL('/i18nitem/batchImportUpload');//返回一个URL字符串
};


export const getExcelImportI18nUploadURL = function () {
    //后台需要projects/file两个参数
    return AjaxUtils.getRequestURL('/i18nitem/batchImportUploadByExcel');//返回一个URL字符串
};


export const deleteI18nItemRequest = function (id) {
    return AjaxUtils.doGetRequest('/i18nitem/deleteItem?id='+id);
};


export const updateAndroidKeyRequest = function (data) {
    return AjaxUtils.doPostRequest('/i18nitem/updateAndroidKey',data);
};


export const findOutErrorItemsRequest = function (projectId,langCodeList) {
    return AjaxUtils.doGetRequest(`/i18nitem/findOutErrorItems?projectId=${projectId}&langCodeList=${langCodeList}`);
};


export const exportProjectI18nResource = function(projectId,langCode,format,options){
    var optionsJSON = JSON.stringify(options);
    optionsJSON = encodeURIComponent(optionsJSON);

    var url = AjaxUtils.getRequestURL(`/i18nitem/exportProjectI18nResource?projectId=${projectId}&langCode=${langCode}&format=${format}&options=${optionsJSON}`);
    window.open(url);
    return Promise.resolve();
};


export const exportProjectAllI18nResource = function(projectId,options){
    var optionsJSON = JSON.stringify(options);
    optionsJSON = encodeURIComponent(optionsJSON);

    var url = AjaxUtils.getRequestURL(`/i18nitem/exportProjectAllI18nResource?projectId=${projectId}&options=${optionsJSON}`);
    window.open(url);
    return Promise.resolve();
};

export const exportProjectJSI18nResource = function(projectId){
    var url = AjaxUtils.getRequestURL(`/i18nitem/exportProjectAllI18nResource?projectId=${projectId}&exportType=js`);
    window.open(url);
    return Promise.resolve();
};

window.i18nItemApi = {
    updateAndroidKeyRequest:updateAndroidKeyRequest,
    findOutErrorItemsRequest:findOutErrorItemsRequest,
    exportProjectJSI18nResource:exportProjectJSI18nResource
};