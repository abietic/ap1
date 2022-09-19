package com.abietic.ap1.configuration;

import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.Http11Nio2Protocol;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;

@Component
public class WebServerConfiguration implements WebServerFactoryCustomizer<ConfigurableWebServerFactory>{

    @Override
    public void customize(ConfigurableWebServerFactory factory) {
        // 由于http1.1中默认使用长连接进行request和response的处理，但是req和respo不是持续不断发生的，在没有发生的空闲时间，在长连接上使线程阻塞性能不好，因此使用nio
        ((TomcatServletWebServerFactory)factory).setProtocol("org.apache.coyote.http11.Http11Nio2Protocol");
        ((TomcatServletWebServerFactory)factory).addConnectorCustomizers(new TomcatConnectorCustomizer() {

            @Override
            public void customize(Connector connector) {
                Http11Nio2Protocol protocol = (Http11Nio2Protocol)connector.getProtocolHandler();

                protocol.setKeepAliveTimeout(30000); // 连接保持30秒
                protocol.setMaxKeepAliveRequests(10000); // 同时10000个请求数

                
            }
            
        });
    }
    
}
