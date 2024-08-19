package com.sap.refapps.espm.config;

import com.sap.cloud.security.client.HttpClientFactory;
import com.sap.cloud.security.config.ClientCredentials;
import com.sap.cloud.security.config.OAuth2ServiceConfiguration;
import com.sap.cloud.security.config.OAuth2ServiceConfigurationBuilder;
import com.sap.cloud.security.config.Service;
import com.sap.cloud.security.xsuaa.client.DefaultOAuth2TokenService;
import com.sap.cloud.security.xsuaa.client.OAuth2TokenResponse;
import com.sap.cloud.security.xsuaa.client.XsuaaDefaultEndpoints;
import com.sap.cloud.security.xsuaa.tokenflows.ClientCredentialsTokenFlow;
import com.sap.cloud.security.xsuaa.tokenflows.XsuaaTokenFlows;
import com.sap.cloud.servicesdk.xbem.core.MessagingServiceFactory;
import com.sap.cloud.servicesdk.xbem.core.exception.MessagingException;
import com.sap.cloud.servicesdk.xbem.core.impl.MessagingServiceFactoryCreator;
import com.sap.cloud.servicesdk.xbem.extension.sapcp.jms.MessagingServiceJmsConnectionFactory;
import com.sap.cloud.servicesdk.xbem.extension.sapcp.jms.MessagingServiceJmsSettings;
import com.sap.refapps.espm.exception.ConflictException;
import com.sap.refapps.espm.exception.EmsResponseErrorHandler;
import com.sap.refapps.espm.exception.NotFoundException;
import com.sap.refapps.espm.exception.UnauthorizedException;

import java.util.Arrays;
import java.util.Collections;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import io.pivotal.cfenv.core.CfCredentials;
import io.pivotal.cfenv.core.CfEnv;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


@Profile("cloud")
@Configuration
public class EnterpriseMessagingConfig {

	private static final Logger logger = LoggerFactory.getLogger(EnterpriseMessagingConfig.class);

	@Bean
    public MessagingServiceFactory getMessagingServiceFactory() {
        CfEnv cfEnv = new CfEnv();
        CfCredentials cfCredentials = cfEnv.findCredentialsByName("espm-em");
        Map<String, Object> credentials = cfCredentials.getMap();
        if (credentials == null) {
            throw new IllegalStateException("Unable to create the MessagingService.");
        }
        return MessagingServiceFactoryCreator.createFactoryFromCredentials(credentials);
    }

	@Bean
	public MessagingServiceJmsConnectionFactory getMessagingServiceJmsConnectionFactory(
			MessagingServiceFactory messagingServiceFactory) throws JsonProcessingException, IOException {
		try {
			/*
			 * The settings object is preset with default values (see JavaDoc)
			 * and can be adjusted. The settings aren't required and depend on
			 * the use-case. Note: a connection will be closed after an idle
			 * time of 5 minutes.
			 */

			logger.info("Checking for queue");
			checkQueue();
			MessagingServiceJmsSettings settings = new MessagingServiceJmsSettings();
			settings.setFailoverMaxReconnectAttempts(3); // use -1 for unlimited attempts
			settings.setFailoverInitialReconnectDelay(3000);
			settings.setFailoverReconnectDelay(3000);
			// settings.
			return messagingServiceFactory.createConnectionFactory(MessagingServiceJmsConnectionFactory.class,
					settings);
		} catch (MessagingException e) {
			throw new IllegalStateException("Unable to create the Connection Factory", e);
		}
	}

	/**
	 * Check whether the queue with queueName exist, if not create a new queue with
	 * queueName.
	 * 
	 * @throws JsonProcessingException
	 * @throws IOException
	 */
	public void checkQueue() throws JsonProcessingException, IOException {
		final ObjectMapper mapper = new ObjectMapper();

		final String vcap = System.getenv("VCAP_SERVICES");

		final JsonNode root = mapper.readTree(vcap);
		final JsonNode credentials = root.get("enterprise-messaging").get(0).get("credentials");
		final JsonNode oauth = credentials.get("management").get(0).get("oa2");
		final EnterpriseMessagingService emManagement = mapper.treeToValue(oauth,
				EnterpriseMessagingService.class);
		final String url = credentials.get("management").get(0)
				.get("uri").textValue();

		final String managementUrl = "/hub/rest/api/v1/management/messaging";
		final String CLIENT_SECRET = emManagement.clientsecret;
		final String CLIENT_ID = emManagement.clientid;
		String tokenurl=emManagement.tokenendpoint;
		final String TOKEN_ENDPOINT = tokenurl.substring(0, tokenurl.lastIndexOf("/oauth/token"));;
		ClientCredentials identity = new ClientCredentials(CLIENT_ID, CLIENT_SECRET);
		


		        OAuth2TokenResponse tokenResponse = null;
				OAuth2ServiceConfiguration config=OAuth2ServiceConfigurationBuilder.forService(Service.XSUAA)
                .withClientId(CLIENT_ID)
                .withClientSecret(CLIENT_SECRET)
                .withUrl(TOKEN_ENDPOINT)
                .build();      

	 XsuaaTokenFlows tokenFlows = new XsuaaTokenFlows(new DefaultOAuth2TokenService(HttpClientFactory.create(identity)),
                new XsuaaDefaultEndpoints(config), identity);


		ClientCredentialsTokenFlow clientCredentialsTokenFlow = tokenFlows.clientCredentialsTokenFlow();

		tokenResponse = clientCredentialsTokenFlow.execute();
		String accessToken = tokenResponse.getAccessToken();
        

		

		RestTemplate restTemplate = new RestTemplate();
HttpHeaders headers = new HttpHeaders();
headers.add("Authorization", "Bearer " + accessToken);  
  headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
HttpEntity<String> httpEntity = new HttpEntity<String>( headers);


// restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);



		restTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory(url + managementUrl));
		restTemplate.setErrorHandler(new EmsResponseErrorHandler());
 
		final String queueName = System.getenv("QUEUE_NAME");
		final String PATH_QUEUE = "/queues/{queueName}";

		try {

			String queue=  restTemplate.exchange(PATH_QUEUE, HttpMethod.GET, httpEntity, String.class,queueName).toString();
			queue=queue.replace("<200 OK OK,","");
			queue=queue.replace(">","");

			final JsonNode fetchedQueue = mapper.readTree(queue);
			
			logger.info(fetchedQueue.get("name") + " already exists");
		} catch (NotFoundException e) {
			logger.info(e.getMessage());
			logger.info("Queue is not available..!!! creating queue");
		
headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));


restTemplate.exchange(PATH_QUEUE, HttpMethod.PUT, httpEntity, String.class,queueName).toString();


		} catch (UnauthorizedException e) {
			logger.error(e.getMessage());
		} catch (ConflictException e) {
			logger.error(e.getMessage());
		}

	}

	

}

@JsonIgnoreProperties(ignoreUnknown = true)
class EnterpriseMessagingService {

	public String clientid;
	public String clientsecret;
	public String granttype;
	public String tokenendpoint;

}
