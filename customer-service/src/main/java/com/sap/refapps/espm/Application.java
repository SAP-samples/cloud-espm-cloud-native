package com.sap.refapps.espm;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.core.env.Environment;
import org.springframework.retry.annotation.EnableRetry;

import com.sap.refapps.espm.config.CustomerApplicationContextInitializer;
import com.sap.refapps.espm.service.CustomerService;

/**
 * This is the main class of the Spring Boot
 * Application and the entry point to the 
 * application.
 *
 */
@EnableRetry
@SpringBootApplication
public class Application implements CommandLineRunner {

	private static final String CUSTOMER_DATA_LOCATION = "/customer.json";
	
	@Autowired
	private Environment environment;
	
	@Autowired
	private CustomerService customerService;
	
	/**
	 * This is the main method.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		new SpringApplicationBuilder(Application.class)
		.initializers(new CustomerApplicationContextInitializer()).run(args);
	}

	/* (non-Javadoc)
	 * @see org.springframework.boot.CommandLineRunner#run(java.lang.String[])
	 * 
	 * This method is used to trigger the mock data load 
	 * into the database from json files. And this method triggers
	 * after the main method call.
	 */
	@Override
	public void run(String... args) throws Exception {
		if (Arrays.stream(environment.getActiveProfiles())
				.anyMatch(env -> (env.equalsIgnoreCase("local") || env.equalsIgnoreCase("cloud")))) {
			customerService.loadCustomer(CUSTOMER_DATA_LOCATION);
		}
	}

}