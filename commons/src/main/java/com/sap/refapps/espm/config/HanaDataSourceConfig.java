package com.sap.refapps.espm.config;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import io.pivotal.cfenv.jdbc.CfJdbcEnv;

/**
 * This is the cloud database configuration class which reads the database
 * properties automatically from the application environment i.e VCAP_SERVICES
 * based on the service bound to the application.
 *
 */
@Configuration
@Profile("cloud")
public class HanaDataSourceConfig {

	private static final String HANA = "hana";
	
	/**
	 * Returns cloud hana datasource
	 * 
	 * @return datasource
	 */
	@Bean
	public DataSource cloudHanaDataSource() {
		
		var dataSource = new DriverManagerDataSource();
		var cfJdbcEnv = new CfJdbcEnv();
		var hanaCredentials = cfJdbcEnv.findCredentialsByTag(HANA);

		dataSource.setUrl(hanaCredentials.getUri(HANA));
		dataSource.setUsername(hanaCredentials.getUsername());
		dataSource.setPassword(hanaCredentials.getPassword());
		
		return dataSource;
	}
}