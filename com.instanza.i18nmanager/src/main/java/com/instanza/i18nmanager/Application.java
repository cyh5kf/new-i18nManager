package com.instanza.i18nmanager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

@EnableAutoConfiguration
@SpringBootApplication
@ServletComponentScan
public class Application extends SpringBootServletInitializer implements EmbeddedServletContainerCustomizer {


    @Autowired
    private Environment environment ;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }


    public void customize(ConfigurableEmbeddedServletContainer container) {

        String contextPath = environment.getProperty("i18nManager.contextPath");
        if(!StringUtils.isEmpty(contextPath)){
            container.setContextPath(contextPath);
        }


        String serverPort = environment.getProperty("i18nManager.port");
        if(!StringUtils.isEmpty(serverPort)){
            int serverPortInt = Integer.parseInt(serverPort);
            container.setPort(serverPortInt);
        }else {
            container.setPort(9001);
        }

    }
}


