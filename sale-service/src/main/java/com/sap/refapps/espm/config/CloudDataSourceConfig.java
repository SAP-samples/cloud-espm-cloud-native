package com.sap.refapps.espm.config;

import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.cloud.config.java.AbstractCloudConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

/**
 * This is the cloud database configuration class which reads
 * the database properties automatically from the application
 * environment i.e VCAP_SERVICES based on the service bound
 * to the application.
 *
 */
@Configuration
@Profile("cloud")
public class CloudDataSourceConfig extends AbstractCloudConfig  {


	/**
	 * Returns the datasource based on DB service
	 * bound to the application.
	 * 
	 * @return datasource
	 */
	@Bean
	public DataSource dataSource() {
		return cloud().getSingletonServiceConnector(DataSource.class, null);
	}

	
	/**
	 * Returns the connection factory for message queue
	 * 
	 * @param rabbitProperties
	 * @return CachingConnectionFactory
	 */
	@Bean
	public ConnectionFactory msgProviderConnectionFactory(RabbitProperties rabbitProperties) {
		CachingConnectionFactory factory = (CachingConnectionFactory) (connectionFactory().rabbitConnectionFactory());
		factory.setPublisherConfirms(true);
		return factory;
	}





	
}
