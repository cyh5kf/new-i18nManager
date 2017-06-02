package com.instanza.i18nmanager.filter;


import com.instanza.i18nmanager.constants.LoginToken;
import org.springframework.http.HttpStatus;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


@WebFilter(filterName = "UserLoginI18nApiFilter", urlPatterns = "/i18nmanager/api/*")
public class UserLoginI18nApiFilter implements Filter {

    @Override
    public void init(FilterConfig config) throws ServletException {
//        System.out.println("过滤器初始化");
    }


    @Override
    public void doFilter(ServletRequest request1, ServletResponse response1,
                         FilterChain chain) throws IOException, ServletException {
//        System.out.println("执行过滤操作");

        HttpServletRequest request = (HttpServletRequest)request1;
        HttpServletResponse response = (HttpServletResponse)response1;
        // token校验
        Map<String,String> cookieMap = new HashMap<String,String>();
        Cookie[] cookies = request.getCookies();
        if(cookies != null){
            for(Cookie cookie : cookies){
                cookieMap.put(cookie.getName(), cookie.getValue());
            }
        }

        String token = cookieMap.get("token");

        if(LoginToken.checkLoginToken(token)){
            chain.doFilter(request, response);
        }else {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
        }

    }

    @Override
    public void destroy() {
//        System.out.println("过滤器销毁");
    }
}