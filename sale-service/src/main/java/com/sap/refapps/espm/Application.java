package com.sap.refapps.espm;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import com.sap.refapps.espm.config.SaleApplicationContextInitializer;

/**
 * This is the main class of the Spring Boot
 * Application and the entry point to the 
 * application.
 *
 */
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
public class Application {

    public static void main(String[] args) {
    	new SpringApplicationBuilder(Application.class)
		.initializers(new SaleApplicationContextInitializer())
		.run(args);

    }

    /**
     * @param builder
     * @return RestTemplate
     */
    @Bean
    public RestTemplate rest(RestTemplateBuilder builder) {
      return builder.build();
    }
    
}
