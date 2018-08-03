package com.sap.refapps.espm.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This class is the Rabbit config class 
 * for message queue
 *
 */
@Configuration
public class RabbitConfig {

	@Value("${rabbit.queueName}")
	private String queueName;

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
	
}
