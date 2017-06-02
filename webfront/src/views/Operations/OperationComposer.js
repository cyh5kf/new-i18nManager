import React from 'react';
import {message} from 'antd';
import {getProjectListRequest,addProjectRequest,updateProjectRequest} from '../../api/I18nProjectApi';
import {updateAndroidKeyRequest} from '../../api/I18nItemApi';
import {toMutiValueString} from '../../utils/StringUtils';
import OperationView from './OperationView';

export default class OperationComposer extends React.Component {

    constructor(props) {
        super(props);
        this.state = {

        };
    }

    doUpdateAndroidKeyRequest=(data)=>{
        updateAndroidKeyRequest(data).then(function(d){
            console.log(d);
            message.success(JSON.stringify(d));
        });
    };

    render() {
        return (
            <OperationView actions={this} store={this.state}/>
        );
    }

}
