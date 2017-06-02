import React from 'react';
import {Modal,Button,Form,Input,Transfer,Checkbox,Radio,Select,Row, Col} from 'antd';
import {parseMultiValueString} from '../../utils/StringUtils';
const FormItem = Form.Item;
import _ from 'underscore';

const CheckboxGroup = Checkbox.Group;
const RadioGroup = Radio.Group;


function toProjectOptions(projectList) {
    var result = [];
    _.forEach(projectList, function (p) {
        result.push({label: p.name, value: "" + p.id});
    });
    return result;
}

function toLanguageSelectOptions({languageCodes,languageMap}) {
    return _.map(languageCodes, function (code) {
        var langName = languageMap[code];
        return <Select.Option key={code} value={code}>{langName}</Select.Option>;
    });
}

const TheForm = Form.create()(React.createClass({
    getInitialState() {
        var data = this.props.data;
        var projectArray = [];
        if (data && data.projects) {
            projectArray = parseMultiValueString(data.projects);
        }
        data = data || {};
        return _.extend({}, data, {
            selectLanguage: 'en',
            projectArray: projectArray
        });
    },


    render() {
        const {store} = this.props;
        const { getFieldDecorator } = this.props.form;
        const formItemLayout = {
            labelCol: {span: 6},
            wrapperCol: {span: 18}
        };
        const dialogData = this.props.data;
        const languageMap = this.props.languageMap;
        const projectOptions = toProjectOptions(store.projectList);
        const state = this.state;
        return (
            <Form horizontal>
                <FormItem
                    {...formItemLayout}
                    label="Key"
                >
                    {getFieldDecorator('source_key', {
                        initialValue: (state['source_key'] || ''),
                        rules: [{
                            required: true, message: 'Please input the source key'
                        }]
                    })(
                        <Input style={{width:300}}/>
                    )}
                </FormItem>

                <FormItem
                    {...formItemLayout}
                    label="Android Key"
                >
                    {getFieldDecorator('android_key', {
                        initialValue: (state['android_key'] || '')
                    })(
                        <Input style={{width:300}}/>
                    )}
                </FormItem>

                <FormItem
                    {...formItemLayout}
                    label="IOS Key"
                >
                    {getFieldDecorator('ios_key', {
                        initialValue: (state['ios_key'] || '')
                    })(
                        <Input style={{width:300}}/>
                    )}
                </FormItem>

                <FormItem
                    {...formItemLayout}
                    label="Projects"
                >
                    {getFieldDecorator('projects', {
                        initialValue: state['projectArray']
                    })(
                        <CheckboxGroup options={projectOptions}/>
                    )}
                </FormItem>
            </Form>
        );
    }
}));


export class InputModifyLanguageView extends React.Component {

    constructor(props) {
        super(props);
        var data = this.props.data;
        var projectArray = [];
        if (data && data.projects) {
            projectArray = parseMultiValueString(data.projects);
        }
        data = data || {};
        this.state = _.extend({}, data, {
            selectLanguage: 'en',
            projectArray: projectArray
        });
    }


    handleChangeSelectLanguage = (value)=> {
        this.setState({selectLanguage: value});
    };

    handleChangeLanguageValue = (e)=> {
        var value = (e.target.value);
        var newState = {};
        newState['value_' + this.state.selectLanguage] = value;
        this.setState(newState);
    };

    getCurStateValue = ()=> {
        return this.state;
    };

    render() {

        var that = this;
        var {state,props} = that;
        var {store,actions} = props;
        return (
            <Row className="languageDisplay">
                <Col span={6} style={{"textAlign":"right"}}>
                    Language : &nbsp;
                </Col>
                <Col span={18}>
                    <Select style={{ width: 120 }}
                            value={state.selectLanguage}
                            onChange={this.handleChangeSelectLanguage}>
                        {toLanguageSelectOptions(store)}
                    </Select>
                    <div className="clear10"></div>
                    <Input type="textarea"
                           placeholder=""
                           onChange={this.handleChangeLanguageValue}
                           value={state['value_'+state.selectLanguage] || ''}
                           style={{ width:300 }}
                           autosize={{ minRows: 3, maxRows: 6 }}/>
                </Col>
            </Row>
        );
    }

}


export default class ModifyOrAddI18nItemDialog extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            loading: false
        };
    }

    handleOk = ()=> {
        var theForm = this.refs['theForm'];
        var theLanguageForm = this.refs['theLanguageForm'];
        var {actions,store} = this.props;
        var dialogData = store.dialogData_ModifyOrAddI18nItemDialog;
        var mm = theLanguageForm.getCurStateValue();
        theForm.validateFields((err, values) => {
            if (!err) {
                console.log('Received values of form: ', values);
                this.setState({loading: true});
                var newValues = _.extend({}, mm, values);
                actions.handleModifyOrAddI18nItem(newValues, dialogData, ()=> {
                    this.setState({loading: false});
                }, 'ModifyOrAddI18nItemDialog');
            }
        });

    };

    handleCancel = ()=> {
        var {actions} = this.props;
        actions.handleToggleDialog("ModifyOrAddI18nItemDialog", false, null)
    };


    render() {
        var {store,actions} = this.props;
        var visible = store.isOpenDialog_ModifyOrAddI18nItemDialog;
        var dialogData = store.dialogData_ModifyOrAddI18nItemDialog;
        if (!visible) {
            return null;
        }
        var {languageMap} = store;
        var state = this.state;
        var title = dialogData ? "Edit I18n Item" : "Add I18n Item";

        var submitBtnText = dialogData ? "Update I18n" : "Add I18n";

        return (
            <Modal
                className="ModifyOrAddI18nItemDialog"
                visible={visible}
                title={title}
                onOk={this.handleOk}
                onCancel={this.handleCancel}
                footer={[
                    <Button key="back" type="ghost" size="large" onClick={this.handleCancel}>Cancel</Button>,
                    <Button key="submit" type="primary" size="large" loading={this.state.loading} onClick={this.handleOk}>{submitBtnText}</Button>
                    ]}>

                <TheForm ref="theForm" store={store} data={dialogData} languageMap={languageMap}></TheForm>
                <InputModifyLanguageView ref="theLanguageForm" store={store} data={dialogData}
                                         languageMap={languageMap}/>
            </Modal>
        );
    }

}
