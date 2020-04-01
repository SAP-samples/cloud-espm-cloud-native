package com.sap.refapps.espm;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import com.sap.refapps.espm.config.TaxApplicationContextInitializer;
import com.sap.refapps.espm.service.TaxService;

/**
 * This is the spring boot application
 * class and acts as a entry point to the 
 * tax service application.
 *
 */
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@EnableAutoConfiguration(exclude = {org.springframework.boot.autoconfigure.gson.GsonAutoConfiguration.class})
public class Application extends SpringBootServletInitializer implements CommandLineRunner {

	@Autowired
	private TaxService taxService;

    /**
     * This is main method.
     * 
     * @param args
     */
    public static void main(String ... args){
    	new SpringApplicationBuilder(Application.class)
		.initializers(new TaxApplicationContextInitializer())
		.run(args);
    }

	/* (non-Javadoc)
	 * @see org.springframework.boot.CommandLineRunner#run(java.lang.String[])
	 */
	public void run(String... arg0) throws Exception {
		
		 taxService.clearTaxSlabs();
		 taxService.setTaxSlabs(SLAB1, SLAB2, SLAB3, SLAB4);
		
	}
	
	//initialize default tax %age
	 static final double SLAB1 = 0; //tax percentage => price(0 - 499)
	 static final double SLAB2 = 5; //tax percentage => price(500-999)
	 static final double SLAB3 = 10; //tax percentage => price(1000-1999)
	 static final double SLAB4 = 15; //tax percentage => price(2000 and above)
	 

}