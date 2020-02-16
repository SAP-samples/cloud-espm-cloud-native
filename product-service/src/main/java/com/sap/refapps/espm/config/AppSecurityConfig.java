package com.sap.refapps.espm.config;

import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.DELETE;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.Jwt;

import com.sap.cloud.security.xsuaa.XsuaaServiceConfiguration;
import com.sap.cloud.security.xsuaa.token.TokenAuthenticationConverter;

@Configuration
@Profile("cloud")
@EnableWebSecurity(debug = true)

// Avoid using (debug = true) in productive code

public class AppSecurityConfig extends WebSecurityConfigurerAdapter {
	@Autowired
	XsuaaServiceConfiguration xsuaaServiceConfiguration;

	// configure Spring Security, demand authentication and specific scopes
	@Override
	public void configure(HttpSecurity http) throws Exception {

		http.csrf().disable();
		http.sessionManagement()
				// session is created by approuter
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
				// demand specific scopes depending on intended request
				.authorizeRequests()
				// enable OAuth2 checks
				.antMatchers(GET, "/product.svc/api/v1/products/**").permitAll()
				.antMatchers(GET, "/product.svc/api/v1/stocks/**").authenticated()
				.antMatchers(PUT, "/product.svc/api/v1/stocks/**").hasAuthority("Update").anyRequest().denyAll()

				.and().oauth2ResourceServer().jwt().jwtAuthenticationConverter(getJwtAuthoritiesConverter());
	}

	/**
	 * Customizes how GrantedAuthority are derived from a Jwt
	 *
	 * @returns jwt converter
	 */
	Converter<Jwt, AbstractAuthenticationToken> getJwtAuthoritiesConverter() {
		TokenAuthenticationConverter converter = new TokenAuthenticationConverter(xsuaaServiceConfiguration);
		converter.setLocalScopeAsAuthorities(true);
		return converter;
	}

}
