package com.sap.refapps.espm.config;


import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
@Profile("local")
public class LocalDataSourceConfig {

	@Value("${jdbc.sql.driverClassName}")
	private String driverClassName;

	@Value("${jdbc.sql.url}")
	private String url;

	@Value("${jdbc.sql.username}")
	private String userName;

	@Value("${jdbc.sql.password}")
	private String password;

	@Bean
	public DataSource localDataSource() {

		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName(driverClassName);
		dataSource.setUrl(url);
		dataSource.setUsername(userName);
		dataSource.setPassword(password);
		return dataSource;
	}

	@Value("${local.rabbit.amqpUrl}")
	private String amqpUrl;

	@Value("${local.rabbit.user}")
	private String rabbitUserName;

	@Value("${local.rabbit.password}")
	private String rabbitPassword;




}


