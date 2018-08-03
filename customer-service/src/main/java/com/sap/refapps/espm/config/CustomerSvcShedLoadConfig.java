package com.sap.refapps.espm.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sap.refapps.espm.valve.ShedLoadSemaphore;
import com.sap.refapps.espm.valve.CustomerShedLoadSemaphoreValve;


/**
 * This is the configuration class for shed load
 *
 */
@Configuration
public class CustomerSvcShedLoadConfig {

	@Autowired
	ShedLoadSemaphore shedLoad;
	/**
	 *  It is used to register the tomcat valve with the tomcat container.
	 *  
	 * @return embeddedTomcat
	 */
	@Bean
	public ServletWebServerFactory servletContainerWithSemaphoreRateLimiterValve() {
		TomcatServletWebServerFactory embeddedTomcat = new TomcatServletWebServerFactory();
		embeddedTomcat.addEngineValves(new CustomerShedLoadSemaphoreValve(shedLoad));
		return embeddedTomcat;
	}

}
