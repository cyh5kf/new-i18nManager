import React from 'react';
import _ from 'underscore';
import {Modal,Button,Form,Input,Radio,Checkbox} from 'antd';
import {parseMultiValueString} from '../../utils/StringUtils';
const FormItem = Form.Item;
const CheckboxGroup = Checkbox.Group;
const RadioGroup = Radio.Group;



function toLanguageOptions(map){
    var result = [];
    var keys = _.keys(map);
    _.forEach(keys,function(k){
        var value = map[k];
        result.push({ label: value, value: k });
    });
    return result;
}


const TheForm = Form.create()(React.createClass({
    getInitialState() {
        var dialogData = this.props.dialogData || {};
        var initialValueLanguages = null;
        var languages = dialogData.languages || "";
        var languagesArray = parseMultiValueString(languages);
        if(languagesArray.length > 0){
            initialValueLanguages = languagesArray;
        }
        return {
            initialValueName:dialogData.name || '',
            initialValueType:dialogData.type || 'Mobile',
            initialValueLanguages:initialValueLanguages || ['en']
        };
    },


    onChangeCheckboxGroup(e){

    },


    onChangeRadioGroup(e){

    },

    render() {
        const { getFieldDecorator } = this.props.form;
        const formItemLayout = {
            labelCol: {span: 6},
            wrapperCol: {span: 18}
        };
        const languageMap = this.props.languageMap;
        const plainOptions = toLanguageOptions(languageMap);
        const dialogData = this.props.dialogData;
        const state = this.state;
        return (
            <Form horizontal>
                <FormItem
                    {...formItemLayout}
                    label="Project Name"
                >
                    {getFieldDecorator('name', {
                        initialValue:state.initialValueName,
                        rules: [{
                            required: true, message: 'Please input your old project name!'
                        }]
                    })(
                        <Input style={{width:350}}/>
                    )}
                </FormItem>
                <FormItem
                    {...formItemLayout}
                    label="Project Type"
                >
                    {getFieldDecorator('type',{
                        initialValue:state.initialValueType
                    })(
                        <RadioGroup>
                            <Radio value="Mobile">Mobile</Radio>
                            <Radio value="Web">Web</Radio>
                            <Radio value="Desktop">Desktop</Radio>
                        </RadioGroup>
                    )}
                </FormItem>
                <FormItem
                    {...formItemLayout}
                    label="Project Languages"
                >
                    {getFieldDecorator('languages',{
                        initialValue:state.initialValueLanguages
                    })(
                        <CheckboxGroup options={plainOptions} />
                    )}
                </FormItem>
            </Form>
        );
    }
}));


//ModifyOrAddProjectDialog
export default class ModifyOrAddProjectDialog extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            loading: false
        };
    }

    handleOk = ()=> {
        var theForm = this.refs['theForm'];
        var {actions,store} = this.props;
        theForm.validateFields((err, values) => {
            if (!err) {
                var dialogData = store.dialogData_ModifyOrAddProjectDialog;
                console.log('Received values of form: ', values);
                this.setState({loading:true});
                actions.handleModifyOrAddProject(values,dialogData,()=>{
                    this.setState({loading:false});
                    actions.handleToggleDialog("ModifyOrAddProjectDialog",false,null);
                },'ModifyOrAddProjectDialog');
            }
        });
    };

    handleCancel = ()=> {
        var {actions} = this.props;
        actions.handleToggleDialog("ModifyOrAddProjectDialog",false,null);
    };


    render() {
        var {actions,store} = this.props;
        var visible = store.isOpenDialog_ModifyOrAddProjectDialog;
        var dialogData = store.dialogData_ModifyOrAddProjectDialog;
        if(!visible){
            return null;
        }

        var title = "Add Project";
        if(dialogData){
            title = "Edit Project";
        }

        var languageMap = store.languageMap;

        return (
            <Modal
                visible={visible}
                title={title}
                width={600}
                onOk={this.handleOk}
                onCancel={this.handleCancel}
                footer={[
                    <Button key="back" type="ghost" size="large" onClick={this.handleCancel}>Cancel</Button>,
                    <Button key="submit" type="primary" size="large" loading={this.state.loading} onClick={this.handleOk}>Submit </Button>
                    ]}>
                <TheForm ref="theForm" dialogData={dialogData} languageMap={languageMap} store={store} actions={actions}></TheForm>
            </Modal>
        );
    }

}
