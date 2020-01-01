package com.sap.refapps.espm.config;

import java.io.IOException;
import java.net.URISyntaxException;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * This class is the Rabbit config class 
 * for message queue
 *
 */
@Configuration
@Profile("local")
public class RabbitConfig {

	@Value("${rabbit.queueName}")
	private String queueName;
	
	@Value("${local.rabbit.amqpUrl}")
	private String amqpUrl;

	@Value("${local.rabbit.user}")
	private String rabbitUserName;

	@Value("${local.rabbit.password}")
	private String rabbitPassword;

	@Bean
	public Queue queue(){
		return new Queue(queueName);
	}

	/**
	 * Returns the rabbit admin.
	 * 
	 * @param connectionFactory
	 * @return RabbitAdmin
	 */
	@Bean
	RabbitAdmin rabbitAdmin(final ConnectionFactory connectionFactory) {
		return new RabbitAdmin(connectionFactory);
	}


	/**
	 * Returns the rabbit template.
	 * 
	 * @param rabbitConnectionFactory
	 * @return RabbitTemplate
	 */
	@Bean
	RabbitTemplate rabbitTemplateSettings(ConnectionFactory rabbitConnectionFactory){
		RabbitTemplate template = new RabbitTemplate(rabbitConnectionFactory);
		template.setMessageConverter(new Jackson2JsonMessageConverter());
		return template;

	}
	
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
