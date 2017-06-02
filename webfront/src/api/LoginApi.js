import axios from 'axios';
import AjaxUtils from '../utils/AjaxUtils';
import LoginStore from '../stores/LoginStore';

const doGetRequest = function(url){
    url =  window.G_REST_URL_PREFIX + "/i18nmanager/login" + url;
    return axios({ method: 'get', url: url});
};

//登录
export const loginRequest = function ({email,password}) {

    email = encodeURIComponent(email);
    password = encodeURIComponent(password);

    return doGetRequest('/doLogin?email=' + email + '&passwd=' + password).then(function (d) {
        if (d.status == 200) {
            LoginStore.setLoginUserInfo(d.data);
        }else {
            LoginStore.setLoginUserInfo(null);
        }
        return d;
    });

};


function delCookie(name)
{
    document.cookie= name + "= ";
}

//退出
export const logoutRequest = function () {

    delCookie("token");

    return doGetRequest('/doLogout').then(function (d) {
        LoginStore.setLoginUserInfo(null);
        return d;
    }).catch(function(){
        LoginStore.setLoginUserInfo(null);
    });
};
