package com.sap.refapps.espm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;

@Configuration
@Profile("local")
@EnableWebSecurity
public class DefaultSecurityConfiguration  {

	@Bean
	public DefaultSecurityFilterChain configure(HttpSecurity http) throws Exception {
		http.csrf().disable().authorizeHttpRequests().anyRequest().permitAll();
		return http.build();

	}
}
