package com.sap.refapps.espm;

import java.io.IOException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import com.sap.refapps.espm.config.WorkerContextInitializer;
import com.sap.refapps.espm.listner.EMListner;

/**
 * This is the spring boot application class for worker.
 *
 */
@SpringBootApplication
public class Worker implements CommandLineRunner {

	@Autowired
	ApplicationContext appContext;

	@Autowired
	Environment environment;

	@Autowired(required = false)
	@Qualifier("EMListner")
	EMListner emListner;

	@Value("${worker.retry.initial}")
	private Long initialValue;

	@Value("${worker.retry.initial}")
	private Long value = initialValue;

	@Value("${worker.retry.multiplier}")
	private Long multiplier;

	@Value("${worker.retry.maxVal}")
	private Long maxVal;

	public static void main(String[] args) {
		new SpringApplicationBuilder(Worker.class).initializers(new WorkerContextInitializer()).run(args);

	}

	/**
	 * This method is used to process messages from queue.
	 * 
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */

	@Override
	public void run(String... args) throws Exception {
		if (Arrays.stream(environment.getActiveProfiles()).anyMatch(env -> (env.equalsIgnoreCase("cloud")))) {
			emListner.receive();
		}
	}

}
