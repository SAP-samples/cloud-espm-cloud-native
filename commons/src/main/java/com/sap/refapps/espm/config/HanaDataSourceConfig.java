package com.sap.refapps.espm.config;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 * This is the cloud database configuration class which reads the database
 * properties automatically from the application environment i.e VCAP_SERVICES
 * based on the service bound to the application.
 *
 */
@Configuration
@Profile("cloud")
@EnableConfigurationProperties
@ConfigurationProperties("vcap.services.espm-hana-db.credentials")
public class HanaDataSourceConfig {

	private String driver;
	private String host;
	private String password;
	private String port;
	private String schema;
	private String url;
	private String user;
	private String hdi_password;
	private String hdi_user;

	public String getDriver() {
		return driver;
	}

	public void setDriver(String driver) {
		this.driver = driver;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getSchema() {
		return schema;
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getHdi_password() {
		return hdi_password;
	}

	public void setHdi_password(String hdi_password) {
		this.hdi_password = hdi_password;
	}

	public String getHdi_user() {
		return hdi_user;
	}

	public void setHdi_user(String hdi_user) {
		this.hdi_user = hdi_user;
	}

	/**
	 * Returns cloud hana datasource
	 * 
	 * @return datasource
	 */
	@Bean
	public DataSource cloudHanaDataSource() {
		final Logger logger = LoggerFactory.getLogger(HanaDataSourceConfig.class);
		DriverManagerDataSource dataSource = null;

		if (driver != null) {
			dataSource = new DriverManagerDataSource();
			dataSource.setDriverClassName(driver);
			dataSource.setUrl(url);
			dataSource.setUsername(user);
			dataSource.setPassword(password);

		} else {

			logger.info("No database driver found. Please check if you have bound to hana instance,"
					+ " the name of the service instance created."
					+ " The expected name is 'espm-hana-db'. Edit the name in HanaDataSourceConfig class if"
					+ " you would like to use another name. ");
			// dataSource when null gives a clearer error msg than when initialised to new.
		}
		return dataSource;
	}
}