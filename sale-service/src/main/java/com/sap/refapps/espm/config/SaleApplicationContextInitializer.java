package com.sap.refapps.espm.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import io.pivotal.cfenv.core.CfEnv;

/**
 * This is the application context initializer class
 * used to activate the profile based on environment.
 *
 */
public class SaleApplicationContextInitializer
implements ApplicationContextInitializer<ConfigurableApplicationContext> {

	private static final Logger logger = LoggerFactory.getLogger(SaleApplicationContextInitializer.class);

	/* (non-Javadoc)
	 * @see org.springframework.context.ApplicationContextInitializer#initialize(org.springframework.context.ConfigurableApplicationContext)
	 */
	@Override
	public void initialize(ConfigurableApplicationContext applicationContext) {
		var applicationEnvironment = applicationContext.getEnvironment();
		var cfEnv = new CfEnv();
		
		if (cfEnv.isInCf()) {
			logger.info("**********Initializing the application context for cloud env**********");
			applicationEnvironment.setActiveProfiles("cloud");

		} else {
			logger.info("**********Initializing the application context for local env**********");
			applicationEnvironment.setActiveProfiles("local");
		}

	}

}
