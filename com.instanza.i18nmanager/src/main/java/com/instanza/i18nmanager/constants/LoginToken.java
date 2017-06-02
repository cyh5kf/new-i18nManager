package com.instanza.i18nmanager.constants;

/**
 * Created by luanhaipeng on 16/12/12.
 */
public class LoginToken {

    private static final String tokenConst = "20161212134944";

    public static String getLoginToken() {
        return tokenConst;
    }

    public static boolean checkLoginToken(String token) {
        return tokenConst.equals(token);

//        return true;
    }
}
