package com.sap.refapps.espm.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.Cloud;
import org.springframework.cloud.CloudException;
import org.springframework.cloud.CloudFactory;
import org.springframework.context.*;
import org.springframework.core.env.ConfigurableEnvironment;

public class WorkerContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

	private static final Logger logger = LoggerFactory.getLogger(WorkerContextInitializer.class);

	@Override
	public void initialize(ConfigurableApplicationContext applicationContext) {
		ConfigurableEnvironment applicationEnvironment = applicationContext.getEnvironment();
		Cloud cloud = getCloud();
		if (cloud != null) {
			logger.info("**********Initializing the application context for cloud env**********");
			applicationEnvironment.setActiveProfiles("cloud");

		} else {
			logger.info("**********Initializing the application context for local env**********");
			applicationEnvironment.setActiveProfiles("local");
		}

	}

	private Cloud getCloud() {
		try {
			CloudFactory cloudFactory = new CloudFactory();
			return cloudFactory.getCloud();
		} catch (CloudException ce) {
			logger.error("no suitable cloud found");
			return null;
		}
	}

}
