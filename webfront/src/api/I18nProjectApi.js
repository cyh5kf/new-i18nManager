import AjaxUtils from '../utils/AjaxUtils';


//获取数据
export const getProjectListRequest = function () {
    return AjaxUtils.doGetRequest('/i18nproject/getProjectList');
};



export const addProjectRequest = function (data) {
    return AjaxUtils.doPostRequest('/i18nproject/addProject',data);
};


export const updateProjectRequest = function (data) {
    return AjaxUtils.doPostRequest('/i18nproject/updateProject',data);
};