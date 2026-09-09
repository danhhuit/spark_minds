package com.sparkminds.library.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Uses the NIO2 connector to avoid Windows selector loopback failures. */
@Configuration
public class TomcatServerConfig {

  @Bean
  public WebServerFactoryCustomizer<TomcatServletWebServerFactory> tomcatProtocolCustomizer(
      @Value("${app.server.tomcat-protocol}") String protocol) {
    return factory -> factory.setProtocol(protocol);
  }
}
