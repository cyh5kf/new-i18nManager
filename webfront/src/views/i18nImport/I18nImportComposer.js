import React from 'react';
import {message} from 'antd';
import I18nImportView from './I18nImportView';
import {getProjectListRequest} from '../../api/I18nProjectApi';
import {getI18nLanguagesRequest,} from '../../api/I18nItemApi';
import {toMutiValueString} from '../../utils/StringUtils';


export default class I18nImportComposer extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            isOpenDialog_I18nImportProcessDialog: false,
            dialogData_I18nImportProcessDialog: null,
            languageCodes:[],//所有的语言的Code值
            languageMap: {},//getCodeNameMap
            projectList: []
        };
    }

    componentWillMount() {
        Promise.all([getI18nLanguagesRequest(), getProjectListRequest()]).then(([response1,response2])=> {
            var languageMap = response1.data.codeNameMap || {};
            var languageCodes = response1.data.codes || {};
            var projectList = response2.data || [];
            this.setState({projectList: projectList, languageMap: languageMap,languageCodes:languageCodes});
        })
    }

    handleToggleDialog = (dialogName, isShow, data)=> {
        var newState = {};
        newState["isOpenDialog_" + dialogName] = isShow;
        newState["dialogData_" + dialogName] = data;
        this.setState(newState);
    };

    render() {
        return (
            <I18nImportView actions={this} store={this.state}/>
        );
    }

}
