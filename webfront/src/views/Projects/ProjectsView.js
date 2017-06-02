import React from 'react';
import {Card,Input,Table,Button} from 'antd';
import {Link} from 'react-router';
import _ from 'underscore';
import ModifyOrAddProjectDialog from './ModifyOrAddProjectDialog';
import ExportProjectResourceDialog from './ExportProjectResourceDialog';
import {parseMultiValueString} from '../../utils/StringUtils';
import {dateFormat} from '../../utils/DateFormatUtils';
import './ProjectsView.less';



function toLangNameArray(codeArr,languageMap){
    return _.map(codeArr,function(c){
        return languageMap[c] || c;
    });
}
export default class ProjectsView extends React.Component {


    getColumns = ()=> {
        var that = this;
        var columns = [
            {title: 'Project ID', dataIndex: 'id', key: 'id'},
            {title: 'Project Name', dataIndex: 'name', key: 'name'},
            {title: 'Project Type', dataIndex: 'type', key: 'type'},
            {
                title: 'Project Languages', dataIndex: 'languages', key: 'languages', render: function (cell, row) {
                    var {store} = that.props;
                    var codeArr = parseMultiValueString(cell);
                    var languageMap = store.languageMap || {};///getCodeNameMap
                    var nameArray = toLangNameArray(codeArr, languageMap);
                    return <div>{nameArray.join(",")}</div>
                }
            },
            {
                title: 'Created ', dataIndex: 'created', key: 'created', render: function (cell, row) {
                    return <div>{dateFormat(new Date(cell), "yyyy-MM-dd hh:mm:ss")}</div>
                }
            },
            {
                title: 'Operation', dataIndex: 'id', key: 'Operation_id', width:'200px',render: function (cell, row) {
                    var actions = that.props.actions;
                    return(
                        <div>
                            <span className="linkStyle" onClick={()=>{actions.handleToggleDialog('ModifyOrAddProjectDialog',true,row)}}>Edit</span>
                            <span className="width10" />
                            <span className="linkStyle" onClick={()=>{actions.handleToggleDialog('ExportProjectResourceDialog',true,row)}}>Export</span>
                            <span className="width10" />
                            <span className="linkStyle" onClick={()=>{actions.handleExportProjectAllI18nResource(row)}}>ExportAll</span>

                            <br/>
                            <span className="linkStyle" onClick={()=>{actions.handleExportProjectJSI18nResource(row)}}>Export Web</span>
                        </div>
                    );
                }
            }
        ];

        return columns;
    };


    render() {
        var {actions,store} = this.props;
        var columns = this.getColumns();
        var {projectList} =store;
        return (

            <div className="ProjectsView">
                <div className="line1">
                    <div className="floatRight">
                        <Button type="primary" onClick={()=>{actions.handleToggleDialog("ModifyOrAddProjectDialog",true,null)}}>Add Project</Button>
                    </div>
                    <div className="clear20"></div>
                </div>
                <Table dataSource={projectList} columns={columns} pagination={false}/>
                <ModifyOrAddProjectDialog store={store} actions={actions} />
                <ExportProjectResourceDialog store={store} actions={actions} />
            </div>
        );
    }
}
