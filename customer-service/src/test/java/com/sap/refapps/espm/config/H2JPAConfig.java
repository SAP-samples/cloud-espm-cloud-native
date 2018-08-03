package com.sap.refapps.espm.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * This is the config class of 
 * embeddded H2 DB
 *
 */
@Configuration
@Profile("test")
@EnableJpaRepositories(basePackages = "com.sap.refapps.espm")
@PropertySource("classpath:test-db.properties")
@EnableTransactionManagement
public class H2JPAConfig {

}
