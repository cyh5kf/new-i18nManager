import React from 'react';
import {message} from 'antd';
import {getProjectListRequest,addProjectRequest,updateProjectRequest} from '../../api/I18nProjectApi';
import {getI18nLanguagesRequest,exportProjectI18nResource,exportProjectAllI18nResource,exportProjectJSI18nResource} from '../../api/I18nItemApi';
import {toMutiValueString} from '../../utils/StringUtils';
import ProjectsView from './ProjectsView';

export default class ProjectsComposer extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            isOpenDialog_ModifyOrAddProjectDialog: false,
            dialogData_ModifyOrAddProjectDialog:null,

            isOpenDialog_ExportProjectResourceDialog: false,
            dialogData_ExportProjectResourceDialog:null,//{}

            languageMap: {},//getCodeNameMap
            projectList: []
        };
    }


    componentWillMount() {
        Promise.all([getI18nLanguagesRequest(), getProjectListRequest()]).then(([response1,response2])=> {
            var languageMap = response1.data.codeNameMap || {};
            var projectList = response2.data || [];
            this.setState({projectList: projectList, languageMap: languageMap});
        })
    }

    refreshProjectList=()=>{
        return getProjectListRequest().then((response)=>{
            var projectList = response.data || [];
            this.setState({projectList: projectList});
            return response;
        });
    };

    handleToggleDialog = (dialogName, isShow, data)=> {
        var newState = {};
        newState["isOpenDialog_" + dialogName] = isShow;
        newState["dialogData_" + dialogName] = data;
        this.setState(newState);
    };


    handleModifyOrAddProject = (values, dialogData,finished, dialogName)=>{


        var languages = values.languages;
        var type = values.type;
        var name = values.name;
        languages = toMutiValueString(languages);

        var newData = {
            name:name,
            type:type,
            languages:languages
        };

        var promiseHandler = null;
        if(dialogData){
            newData.id = dialogData.id;
            promiseHandler = updateProjectRequest(newData);
        }else {
            promiseHandler =  addProjectRequest(newData);
        }

        promiseHandler.then((response)=>{
            this.refreshProjectList().then(()=>{
                finished();
            });
            message.success("Save Project Successfully");
        },()=>{
            finished();
        });

    };


    handleExportProjectI18n=(values, dialogData,finished)=>{

        var projectId,langCode,format;

        langCode = values.langCode;
        projectId = dialogData.id;
        format = values.type;
        if(dialogData.type!=='Mobile'){
            format = dialogData.type;
        }

        var options = {
            valueReplaceSource:"",
            valueReplaceTarget:""
        };
        exportProjectI18nResource(projectId,langCode,format,options);

        finished();
    };


    handleExportProjectAllI18nResource=(row)=>{
        var projectId = row.id;
        exportProjectAllI18nResource(projectId);
    };

    handleExportProjectJSI18nResource=(row)=>{
        var projectId = row.id;
        exportProjectJSI18nResource(projectId);
    };


    render() {
        return (
            <ProjectsView actions={this} store={this.state}/>
        );
    }

}
