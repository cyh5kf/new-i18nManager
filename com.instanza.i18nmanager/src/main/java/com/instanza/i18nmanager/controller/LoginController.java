package com.instanza.i18nmanager.controller;


import com.instanza.i18nmanager.constants.LoginToken;
import com.instanza.i18nmanager.utils.MessageException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping(value = "/i18nmanager/login")
public class LoginController {


    @RequestMapping(value = "doLogin", method = RequestMethod.GET)
    public ResponseEntity<?> doLogin(@RequestParam("email") String email,
                                   @RequestParam("passwd") String passwd, HttpServletRequest request) throws MessageException {

        if ("i18nadmin".equals(email) && "helloi18n".equals(passwd)){

            HttpHeaders head = new HttpHeaders();
            head.add("Set-Cookie", "token=" + LoginToken.getLoginToken() + "; Path=/; max-age=604800");

            HashMap<String,String> userInfo = new HashMap<>();

            userInfo.put("username","i18nadmin");

            return new ResponseEntity<>(userInfo, head, HttpStatus.OK);
        }

        throw new MessageException("error");
    }

    @RequestMapping(value = "doLogout", method = RequestMethod.GET)
    public ResponseEntity<?> doLogout() {
        return new ResponseEntity<String>(HttpStatus.OK);
    }



    @RequestMapping(value = "checkUser", method = RequestMethod.GET)
    public ResponseEntity<?> checkUser(@CookieValue("operator_id") long operator_id) {
        return new ResponseEntity<String>(HttpStatus.OK);
    }


}
