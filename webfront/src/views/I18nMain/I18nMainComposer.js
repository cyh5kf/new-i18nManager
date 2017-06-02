import React from 'react';
import _ from 'underscore';
import {message} from 'antd';
import I18nMainView from './I18nMainView';
import {getProjectListRequest} from '../../api/I18nProjectApi';
import {getI18nLanguagesRequest,queryI18nItemList,deleteI18nItemRequest,addI18nItemRequest,updateI18nItemByIdRequest} from '../../api/I18nItemApi';
import {toMutiValueString} from '../../utils/StringUtils';

const CONST_ShowLanguageColumns = "ShowLanguageColumns";

function getShowLanguageColumns() {
    var d = localStorage.getItem(CONST_ShowLanguageColumns);
    if (d) {
        return JSON.parse(d);
    } else {
        return ['source_key', 'value_en', 'value_zh', 'projects']
    }
}

export default class I18nMainComposer extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            isOpenDialog_ColumnSelectDialog: false,
            dialogData_ColumnSelectDialog: null,

            isOpenDialog_ModifyOrAddI18nItemDialog: false,
            dialogData_ModifyOrAddI18nItemDialog: null,

            languageCodes: [],//所有的语言的Code值
            languageMap: {}, //所有的语言
            projectList: [],  //所有的项目
            queryCondition: {
                limitStart: 0,
                limitSize: 10
            },
            i18nItemTotal: 0,
            i18nItemList: [],
            selectedRowIsSelectAllPage: false,
            selectedRowKeys: [],//表格中显示的行
            selectedColumnKeys: getShowLanguageColumns()//表格中显示的列
        };
    }

    toggleSelectAllPage = (selectedRowIsSelectAllPage)=> {
        this.setState({selectedRowIsSelectAllPage: selectedRowIsSelectAllPage});
    };

    componentWillMount() {
        this.doQueryI18nItemList(this.state.queryCondition);
        Promise.all([getI18nLanguagesRequest(), getProjectListRequest()]).then(([response1,response2])=> {
            var languageMap = response1.data.codeNameMap || {};
            var languageCodes = response1.data.codes || {};
            var projectList = response2.data || [];
            this.setState({projectList: projectList, languageMap: languageMap, languageCodes: languageCodes});
        })
    }

    doQueryI18nItemList = (queryCondition)=> {
        return queryI18nItemList(queryCondition).then((resp)=> {
            var {data,total} = resp.data;
            this.setState({
                i18nItemTotal: total,
                i18nItemList: data
            });
        });
    };


    doQueryWithCondition = (newCondition, clearSelectedRows)=> {


        var queryCondition = this.state.queryCondition;

        if (clearSelectedRows) {
            queryCondition = newCondition;
        } else {
            queryCondition = Object.assign(queryCondition, newCondition);
        }


        queryI18nItemList(queryCondition).then((resp)=> {
            var {data,total} = resp.data;

            if (clearSelectedRows) {
                this.setState({
                    selectedRowIsSelectAllPage: false,
                    selectedRowKeys: [],

                    queryCondition: queryCondition,
                    i18nItemTotal: total,
                    i18nItemList: data
                });
            } else {
                this.setState({
                    queryCondition: queryCondition,
                    i18nItemTotal: total,
                    i18nItemList: data
                });
            }
        });

    };

    doClearSelectedRows = ()=> {
        this.setState({
            selectedRowIsSelectAllPage: false,
            selectedRowKeys: []
        });
    };


    handleSelectI18nRowChange = (selectedRowKeys)=> {
        this.setState({selectedRowKeys: selectedRowKeys});
    };

    handleToggleDialog = (dialogName, isShow, data)=> {
        var newState = {};
        newState["isOpenDialog_" + dialogName] = isShow;
        newState["dialogData_" + dialogName] = data;
        this.setState(newState);
    };

    handleSubmitSelectShowColumns = (values, finished, dialogName)=> {
        this.doClearSelectedRows();

        var json = JSON.stringify(values);
        localStorage.setItem(CONST_ShowLanguageColumns, json);
        this.setState({selectedColumnKeys: values});
        finished();
    };


    handleDeleteI18nItem = ({id})=> {

        this.doClearSelectedRows();

        deleteI18nItemRequest(id).then(()=> {
            this.doQueryI18nItemList(this.state.queryCondition);
        });
    };


    handleModifyOrAddI18nItem = (values, dialogData, finished)=> {

        this.doClearSelectedRows();

        if (_.isArray(values.projects)) {
            var projectString = toMutiValueString(values.projects);
            values.projects = projectString;
        }

        var source_key = (values['source_key']||'').trim();
        var android_key = (values['android_key']||'').trim();
        var ios_key = (values['ios_key']||'').trim();

        values['android_key'] = android_key || source_key;
        values['ios_key'] = ios_key || source_key;
        values['source_key'] = source_key;


        if (dialogData) {
            //修改

            updateI18nItemByIdRequest(values).then(()=> {
                message.success('Update I18n Item Successfully');
                this.doQueryI18nItemList(this.state.queryCondition).then(()=>{
                    finished();
                    this.handleToggleDialog("ModifyOrAddI18nItemDialog", false, null);
                });
            },()=>{
                message.error("The unique key is already exists,please check");
                finished();
            });

        } else {

            //新建
            addI18nItemRequest(values).then(()=> {
                message.success('Add I18n Item Successfully');
                this.doQueryI18nItemList(this.state.queryCondition).then(()=>{
                    finished();
                    this.handleToggleDialog("ModifyOrAddI18nItemDialog", false, null);
                });
            },(resp)=>{
                message.error("The unique key is already exists,please check");
                finished();
            });
        }
    };


    render() {
        return (
            <I18nMainView actions={this} store={this.state}/>
        );
    }

}
