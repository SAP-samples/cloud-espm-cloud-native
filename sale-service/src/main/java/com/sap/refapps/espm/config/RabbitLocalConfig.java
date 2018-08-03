package com.sap.refapps.espm.config;

import java.io.IOException;
import java.net.URISyntaxException;

import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * This class is the Rabbit config class 
 * for message queue
 *
 */
@Profile("local")
@Configuration
public class RabbitLocalConfig {

	@Value("${local.rabbit.amqpUrl}")
	private String amqpUrl;

	@Value("${local.rabbit.user}")
	private String rabbitUserName;

	@Value("${local.rabbit.password}")
	private String rabbitPassword;

	/**
	 * Returns the connection factory for message queue
	 * 
	 * @param rabbitProperties
	 * @return CachingConnectionFactory
	 */
	@Bean
	public ConnectionFactory connectionFactory() throws IOException, URISyntaxException {

		CachingConnectionFactory connectionFactory = new CachingConnectionFactory(amqpUrl);
		connectionFactory.setUsername(rabbitUserName);
		connectionFactory.setPassword(rabbitPassword);
		connectionFactory.setChannelCacheSize(100);
		connectionFactory.setPublisherConfirms(true);
		return connectionFactory;
	}

}
