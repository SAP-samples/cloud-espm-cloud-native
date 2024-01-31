package com.sap.refapps.espm.config;

import jakarta.servlet.Servlet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.web.ServerProperties.Tomcat;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sap.refapps.espm.valve.ShedLoadSemaphore;
import com.sap.refapps.espm.valve.ProductShedLoadSemaphoreValve;

/**
 * This is the configuration class for shed load
 *
 */
@Configuration
@EnableAutoConfiguration
public class ProductSvcShedLoadConfig {


	@Autowired
	ShedLoadSemaphore shedLoad;

	/**
	 * It is used to register the tomcat valve with the tomcat container.
	 * 
	 * @return embeddedTomcat
	 */
	@Bean
	@ConditionalOnClass({ Servlet.class, Tomcat.class })
	public ServletWebServerFactory servletContainerWithSemaphoreRateLimiterValve() {
		TomcatServletWebServerFactory embeddedTomcat = new TomcatServletWebServerFactory();
		embeddedTomcat.addEngineValves(new ProductShedLoadSemaphoreValve(shedLoad));
		return embeddedTomcat;
	}
}
