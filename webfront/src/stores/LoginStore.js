
const LOCAL_STORAGE_KEY = 'loginUserInfo';

/**
 * 登录用户的信息,是全局性信息.单独存储.
 * 界面各自的数据,直接使用State存储
 */
class LoginStore {

    constructor(props) {
        var json = localStorage.getItem(LOCAL_STORAGE_KEY);
        if(json) {
            this.loginUserInfo = JSON.parse(json);
        }else {
            this.loginUserInfo = null;
        }
    }

    getLoginUserInfo(){
        return this.loginUserInfo;
    }

    setLoginUserInfo(loginUserInfo){
        var json = JSON.stringify(loginUserInfo);
        localStorage.setItem(LOCAL_STORAGE_KEY,json);
        this.loginUserInfo = loginUserInfo;
    }
    
}

export default new LoginStore();