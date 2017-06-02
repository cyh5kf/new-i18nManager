import React from 'react';
import {Card,Input,Table,Button,Modal} from 'antd';
import {Link} from 'react-router';
import _ from 'underscore';
import {parseMultiValueString} from '../../utils/StringUtils';
import {dateFormat} from '../../utils/DateFormatUtils';
import './OperationView.less';


export default class OperationView extends React.Component {



    doUpdateAndroidKey=()=> {
        var {actions,store} = this.props;
        var textarea = this.refs['textarea'];
        var v = textarea.value;
        v = v.trim();
        var vList = v.split('\n');
        var vTList = vList.map(function (v0) {
            return v0.trim();
        });

        var pairMap = {};
        var chongfuIOSKey = [];
        vTList.forEach(function(v0){
            v0 = v0.trim();
            var mm = v0.split(/[\t\s]/);
            var mmLen = mm.length;
            var mmFill = [];
            for(var i = 0 ; i<mmLen;i++){
                var mi = mm[i];
                if(mi){
                    mmFill.push(mi);
                }
            }

            if(mmFill[0] && mmFill[1]){
                if(pairMap[mmFill[1]]){
                    chongfuIOSKey.push(mmFill[1]);
                    console.error("重复的iosKey",mmFill[1]);
                }
                pairMap[mmFill[1]] = mmFill[0];
            }else {
                console.error(mmFill[0],mmFill[1]);
            }
        });


        console.log(pairMap);
        window.OperationView_pairMap = pairMap;
        var pairKeys = Object.keys(pairMap);
        var pairKeysLength = pairKeys.length;

        Modal.confirm({
            title: `去掉重复的检测到${pairKeysLength}个项//总共有${vTList.length}项`,
            content: <div>发现重复的IOS Key : <br/>{chongfuIOSKey.map(function(m){
                return <p>{m}</p>
            })}</div>,
            okText: '确认更新Android',
            cancelText: '取消',
            onOk:function(){
                actions.doUpdateAndroidKeyRequest(pairMap);
            }
        });

    };

    render() {
        var {actions,store} = this.props;

        return (

            <div className="OperationView">
                <Card>
                    <textarea ref="textarea" className="textarea"></textarea>
                    <div className="clear20"></div>
                    <Button type="primary" onClick={this.doUpdateAndroidKey}>Update AndroidKey</Button>
                </Card>
            </div>
        );
    }
}
