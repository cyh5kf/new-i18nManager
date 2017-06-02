import React from 'react';
import {Card,Input,Table,Upload,message,Icon,Checkbox,Select,Modal,Tabs} from 'antd';
import {getImportI18nUploadURL,getBatchImportI18nUploadURL,getExcelImportI18nUploadURL} from '../../api/I18nItemApi';
import {toMutiValueString} from '../../utils/StringUtils';
const Dragger = Upload.Dragger;
const CheckboxGroup = Checkbox.Group;
const Option = Select.Option;
const OptGroup = Select.OptGroup;
const TabPane = Tabs.TabPane;

import I18nImportProcessDialog from './I18nImportProcessDialog';
import './I18nImportView.less';


export default class I18nImportView extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            selectLanguage_currentValue: null,
            checkboxGroupValue: []
        };
    }

    getExcelUploadProps=()=>{
        var that = this;

        const props = {
            name: 'file',
            multiple: true,
            showUploadList: true,
            beforeUpload: function (info) {
                var {actions} = that.props;
                actions.handleToggleDialog("I18nImportProcessDialog", true);
                console.log("beforeUpload", info);
                return true;
            },
            data: function () {
                var projectsArray = that.state.checkboxGroupValue;
                var projects = toMutiValueString(projectsArray);
                return {
                    projects: projects
                }
            },
            action: getExcelImportI18nUploadURL(),
            onChange(info) {
                const {actions} = that.props;
                const status = info.file.status;
                if (status !== 'uploading') {
                    console.log("uploading", info.file, info.fileList);
                }
                if (status === 'done') {
                    console.log("uploading done", info.file, info.fileList);
                    actions.handleToggleDialog("I18nImportProcessDialog", false);

                    var response = info.file.response || {};
                    var respMessage = response.message;

                    if ("DuplicateKey" === respMessage) {
                        //上传失败
                        var respData = response.data || [];
                        Modal.error({
                            title: 'Duplicate Key !',
                            content: (
                                <div className="DuplicateKeyItemList">
                                    {
                                        _.map(respData, function (d) {
                                            return <div className="DuplicateKeyItem">{d}</div>
                                        })
                                    }
                                </div>
                            )
                        });

                    } else {
                        message.success(`${info.file.name} file uploaded successfully.`);
                    }
                } else if (status === 'error') {
                    actions.handleToggleDialog("I18nImportProcessDialog", false);
                    console.log("uploading error", info.file, info.fileList);
                    message.error(`${info.file.name} file upload failed.`);
                }
            }
        };

        return props;
    };

    getBatchUploadProps=()=>{
        var that = this;

        const props = {
            name: 'file',
            multiple: true,
            showUploadList: true,
            beforeUpload: function (info) {
                var {actions} = that.props;
                actions.handleToggleDialog("I18nImportProcessDialog", true);
                console.log("beforeUpload", info);
                return true;
            },
            data: function () {
                var projectsArray = that.state.checkboxGroupValue;
                var projects = toMutiValueString(projectsArray);
                return {
                    projects: projects
                }
            },
            action: getBatchImportI18nUploadURL(),
            onChange(info) {
                const {actions} = that.props;
                const status = info.file.status;
                if (status !== 'uploading') {
                    console.log("uploading", info.file, info.fileList);
                }
                if (status === 'done') {
                    console.log("uploading done", info.file, info.fileList);
                    actions.handleToggleDialog("I18nImportProcessDialog", false);

                    var response = info.file.response || {};
                    var respMessage = response.message;

                    if ("DuplicateKey" === respMessage) {
                        //上传失败
                        var respData = response.data || [];
                        Modal.error({
                            title: 'Duplicate Key !',
                            content: (
                                <div className="DuplicateKeyItemList">
                                    {
                                        _.map(respData, function (d) {
                                            return <div className="DuplicateKeyItem">{d}</div>
                                        })
                                    }
                                </div>
                            )
                        });

                    } else {
                        message.success(`${info.file.name} file uploaded successfully.`);
                    }
                } else if (status === 'error') {
                    actions.handleToggleDialog("I18nImportProcessDialog", false);
                    console.log("uploading error", info.file, info.fileList);
                    message.error(`${info.file.name} file upload failed.`);
                }
            }
        };

        return props;
    };

    getUploadProps = ()=> {
        var that = this;

        const props = {
            name: 'file',
            multiple: true,
            showUploadList: true,
            beforeUpload: function (info) {
                var {actions} = that.props;
                actions.handleToggleDialog("I18nImportProcessDialog", true);
                console.log("beforeUpload", info);
                return true;
            },
            data: function () {
                var {languageCodes,languageMap} = that.props.store;
                var langCode = that.state.selectLanguage_currentValue;
                var langName = languageMap[langCode];
                var projectsArray = that.state.checkboxGroupValue;
                var projects = toMutiValueString(projectsArray);
                return {
                    projects: projects,
                    langName: langName
                }
            },
            action: getImportI18nUploadURL(),
            onChange(info) {
                const {actions} = that.props;
                const status = info.file.status;
                if (status !== 'uploading') {
                    console.log("uploading", info.file, info.fileList);
                }
                if (status === 'done') {
                    console.log("uploading done", info.file, info.fileList);
                    actions.handleToggleDialog("I18nImportProcessDialog", false);

                    var response = info.file.response || {};
                    var respMessage = response.message;

                    if ("DuplicateKey" === respMessage) {
                        //上传失败
                        var respData = response.data || [];
                        Modal.error({
                            title: 'Duplicate Key !',
                            content: (
                                <div className="DuplicateKeyItemList">
                                    {
                                        _.map(respData, function (d) {
                                            return <div className="DuplicateKeyItem">{d}</div>
                                        })
                                    }
                                </div>
                            )
                        });

                    } else {
                        message.success(`${info.file.name} file uploaded successfully.`);
                    }
                } else if (status === 'error') {
                    actions.handleToggleDialog("I18nImportProcessDialog", false);
                    console.log("uploading error", info.file, info.fileList);
                    message.error(`${info.file.name} file upload failed.`);
                }
            }
        };

        return props;
    };

    getProjectCheckboxOptions = ()=> {

        var {projectList} = this.props.store;

        var result = [];

        _.forEach(projectList, function (p) {
            result.push({
                label: p.name,
                value: p.id
            });
        });
        return result;
    };


    onChangeSelectSearchType = (v)=> {
        this.setState({selectLanguage_currentValue: v});
    };


    renderLanguageOptions = ()=> {
        var {languageCodes,languageMap} = this.props.store;

        var options = _.map(languageCodes, function (c) {
            var languageName = languageMap[c];
            return <Option key={c} value={c}>{languageName}</Option>
        });

        //var defaultValue = languageCodes[0] || '';
        return (
            <Select
                style={{ width: 150 }}
                showSearch={false}
                onChange={this.onChangeSelectSearchType}>
                {options}
            </Select>
        );
    };


    onChangeCheckboxGroup = (checkboxGroupValue)=> {
        this.setState({checkboxGroupValue: checkboxGroupValue});
    };

    render() {
        var {actions,store} = this.props;
        var {projectList} = this.props.store;
        var uploadProps = this.getUploadProps();
        var batchUploadProps = this.getBatchUploadProps();
        var excelUploadProps = this.getExcelUploadProps();

        var projectCheckboxOptions = this.getProjectCheckboxOptions();
        var selectLanguage_currentValue = this.state.selectLanguage_currentValue;
        var isBatchUploadDraggerDisabled = !this.state.checkboxGroupValue || this.state.checkboxGroupValue.length === 0;
        var isExcelUploadDraggerDisabled = !this.state.checkboxGroupValue || this.state.checkboxGroupValue.length === 0;
        var isUploadDraggerDisabled = !selectLanguage_currentValue || !this.state.checkboxGroupValue || this.state.checkboxGroupValue.length === 0;
        return (
            <div className="I18nImportView">
                <Card>
                    <Tabs defaultActiveKey="1">



                        <TabPane tab="Batch Import By A Zip File" key="1">
                            <div>
                                <div className="form">
                                    <div className="row">
                                        <div className="clear20"></div>
                                        <div className="title">Import to Projects :</div>
                                        <div className="content">
                                            <CheckboxGroup options={projectCheckboxOptions}
                                                           value={this.state.checkboxGroupValue}
                                                           onChange={this.onChangeCheckboxGroup}/>
                                        </div>
                                        <div className="clear20"></div>
                                        <div className="clear20"></div>
                                    </div>

                                </div>


                                <Dragger {...batchUploadProps} disabled={isBatchUploadDraggerDisabled}>
                                    <p className="ant-upload-drag-icon">
                                        <Icon type="inbox"/>
                                    </p>
                                    <p className="ant-upload-text">Click or drag <span style={{color:'red'}}>.zip</span> file to this area to upload</p>
                                    <p className="ant-upload-hint">Support for a single or bulk upload. Strictly
                                        prohibit
                                        from
                                        uploading company data or other band files</p>
                                </Dragger>
                            </div>
                        </TabPane>


                        <TabPane tab="Batch Import By Excel" key="2">
                            <div>
                                <div className="form">
                                    <div className="row">
                                        <div className="clear20"></div>
                                        <div className="title">Import to Projects :</div>
                                        <div className="content">
                                            <CheckboxGroup options={projectCheckboxOptions}
                                                           value={this.state.checkboxGroupValue}
                                                           onChange={this.onChangeCheckboxGroup}/>
                                        </div>
                                        <div className="clear20"></div>
                                        <div className="clear20"></div>
                                    </div>

                                </div>


                                <Dragger {...excelUploadProps} disabled={isExcelUploadDraggerDisabled}>
                                    <p className="ant-upload-drag-icon">
                                        <Icon type="inbox"/>
                                    </p>
                                    <p className="ant-upload-text">Click or drag <span style={{color:'red'}}>.xls</span> file to this area to upload</p>
                                    <p className="ant-upload-hint">Support for a single or bulk upload. Strictly
                                        prohibit
                                        from
                                        uploading company data or other band files</p>
                                </Dragger>
                            </div>
                        </TabPane>



                        <TabPane tab="Import Single Files" key="3">
                            <div>
                                <div className="form">

                                    <div className="row">
                                        <div className="clear20"></div>
                                        <div className="title">Source Language :</div>
                                        <div className="content">
                                            {this.renderLanguageOptions()}
                                        </div>
                                        <div className="clear20"></div>
                                    </div>

                                    <div className="row">
                                        <div className="title">Import to Projects :</div>
                                        <div className="content">
                                            <CheckboxGroup options={projectCheckboxOptions}
                                                           value={this.state.checkboxGroupValue}
                                                           onChange={this.onChangeCheckboxGroup}/>
                                        </div>
                                        <div className="clear20"></div>
                                        <div className="clear20"></div>
                                    </div>


                                </div>


                                <Dragger {...uploadProps} disabled={isUploadDraggerDisabled}>
                                    <p className="ant-upload-drag-icon">
                                        <Icon type="inbox"/>
                                    </p>
                                    <p className="ant-upload-text">Click or drag <span style={{color:'red'}}>.string</span> file to this area to upload</p>
                                    <p className="ant-upload-hint">Support for a single or bulk upload. Strictly
                                        prohibit
                                        from
                                        uploading company data or other band files</p>
                                </Dragger>
                            </div>
                        </TabPane>

                    </Tabs>

                </Card>
                <I18nImportProcessDialog store={store} actions={actions}/>

            </div>

        );
    }
}
