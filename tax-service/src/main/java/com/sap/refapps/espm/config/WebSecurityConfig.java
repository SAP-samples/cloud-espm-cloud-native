package com.sap.refapps.espm.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.Jwt;

import com.sap.cloud.security.xsuaa.XsuaaServiceConfiguration;
import com.sap.cloud.security.xsuaa.token.TokenAuthenticationConverter;

@Configuration
@Profile("cloud")
public class WebSecurityConfig  extends WebSecurityConfigurerAdapter {
	@Autowired
	XsuaaServiceConfiguration xsuaaServiceConfiguration;

	// configure Spring Security, demand authentication and specific scopes
	@Override
	public void configure(HttpSecurity http) throws Exception {

       http
           .sessionManagement()
           // session is created by approuter
           .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
           .and()
               // demand specific scopes depending on intended request
               .authorizeRequests()
               .anyRequest().authenticated().and().oauth2ResourceServer().jwt().jwtAuthenticationConverter(getJwtAuthoritiesConverter());
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
