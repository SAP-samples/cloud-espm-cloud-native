package com.sap.refapps.espm.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Primary;

/**
 * This is the config class for Rabbit message queue.
 *
 */
@Configuration
@EnableRabbit
public class RabbitConfig {

	@Value("${rabbit.queueName}")
	private String queueName;

	@Value("${rabbit.prefetchCount}")
	private int prefetchCount;

	@Value("${rabbit.consumerCount}")
	private int consumerCount;

	@Autowired
	RabbitTemplate rabbitTemplate;

	/**
	 * @param rabbitConnectionFactory
	 * @return SimpleMessageListenerContainer
	 */
	@Primary
	public SimpleMessageListenerContainer rabbitListener(ConnectionFactory rabbitConnectionFactory) {
		var factory = new SimpleMessageListenerContainer();
		factory.setConnectionFactory(rabbitConnectionFactory);
		factory.setPrefetchCount(prefetchCount);
		factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
		factory.setConcurrentConsumers(consumerCount);
		return factory;

	}

	/**
	 * Returns a queue.
	 * 
	 * @return queue
	 */
	@Bean
	Queue queue() {
		return new Queue(queueName);
	}

	/**
	 * This is the json message converter.
	 * 
	 * Returns 
	 * @return Jackson2JsonMessageConverter
	 */
	@Bean
	public Jackson2JsonMessageConverter converter() {
		return new Jackson2JsonMessageConverter();
	}

	/**
	 * @param rabbitConnectionFactory
	 * @return RabbitAdmin
	 */
	@Bean
	RabbitAdmin rabbitAdmin(ConnectionFactory rabbitConnectionFactory) {
		return new RabbitAdmin(rabbitConnectionFactory);
	}
}