package com.sap.refapps.espm.config;

import com.sap.cloud.servicesdk.xbem.core.MessagingService;
import com.sap.cloud.servicesdk.xbem.core.MessagingServiceFactory;
import com.sap.cloud.servicesdk.xbem.core.exception.MessagingException;
import com.sap.cloud.servicesdk.xbem.core.impl.MessagingServiceFactoryCreator;
import com.sap.cloud.servicesdk.xbem.extension.sapcp.jms.MessagingServiceJmsConnectionFactory;
import com.sap.cloud.servicesdk.xbem.extension.sapcp.jms.MessagingServiceJmsSettings;
import com.sap.refapps.espm.exception.ConflictException;
import com.sap.refapps.espm.exception.EmsResponseErrorHandler;
import com.sap.refapps.espm.exception.NotFoundException;
import com.sap.refapps.espm.exception.UnauthorizedException;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.Cloud;
import org.springframework.cloud.CloudFactory;

import org.springframework.cloud.service.ServiceConnectorConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.client.DefaultOAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.web.client.RestClientException;
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
		ServiceConnectorConfig config = null; // currently there are no configurations for the MessagingService
												// supported
		Cloud cloud = new CloudFactory().getCloud();
		// get the MessagingService via the service connector
		MessagingService messagingService = cloud.getSingletonServiceConnector(MessagingService.class, config);
		if (messagingService == null) {
			throw new IllegalStateException("Unable to create the MessagingService.");
		}
		return MessagingServiceFactoryCreator.createFactory(messagingService);
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
			settings.setMaxReconnectAttempts(3); // use -1 for unlimited attempts
			settings.setInitialReconnectDelay(3000);
			settings.setReconnectDelay(3000);
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

		DefaultOAuth2ClientContext clientContext = new DefaultOAuth2ClientContext();

		RestTemplate restTemplate = new OAuth2RestTemplate(getClientCredentialsResourceDetails(emManagement));

		restTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory(url + managementUrl));
		restTemplate.setErrorHandler(new EmsResponseErrorHandler());

		final String queueName = System.getenv("QUEUE_NAME");
		final String PATH_QUEUE = "/queues/{queueName}";

		try {
			final String managementPath = "/hub/rest/api/v1/management/messaging/queues/";
			logger.info("GET " + url + managementPath + queueName);
			String queue = restTemplate.getForObject(PATH_QUEUE, String.class, queueName).toString();
			final JsonNode fetchedQueue = mapper.readTree(queue);
			logger.info(fetchedQueue.get("name") + " already exists");
		} catch (NotFoundException e) {
			logger.info(e.getMessage());
			logger.info("Queue is not available..!!! creating queue");
			createQueue(restTemplate, PATH_QUEUE, queueName);
		} catch (UnauthorizedException e) {
			logger.error(e.getMessage());
		} catch (ConflictException e) {
			logger.error(e.getMessage());
		}

	}

	/**
	 * Creating a queue
	 * 
	 * @param restTemplate
	 * @param PATH_QUEUE
	 * @param queueName
	 */
	private void createQueue(RestTemplate restTemplate, final String PATH_QUEUE, final String queueName) {
		try {

			restTemplate.put(PATH_QUEUE, null, queueName);
		} catch (RestClientException e) {
			logger.info(e.getMessage());

		}
	}

	/**
	 * Retrieving client credentials to access the api
	 * 
	 * @param emManagement
	 * @return
	 */
	private ClientCredentialsResourceDetails getClientCredentialsResourceDetails(
			EnterpriseMessagingService emManagement) {

		// service key credentials
		final String CLIENT_CREDENTIALS = emManagement.granttype;
		final String CLIENT_SECRET = emManagement.clientsecret;
		final String CLIENT_ID = emManagement.clientid;
		final String TOKEN_ENDPOINT = emManagement.tokenendpoint;

		ClientCredentialsResourceDetails resourceDetails = new ClientCredentialsResourceDetails();
		resourceDetails.setAccessTokenUri(TOKEN_ENDPOINT);
		resourceDetails.setClientId(CLIENT_ID);
		resourceDetails.setClientSecret(CLIENT_SECRET);
		resourceDetails.setGrantType(CLIENT_CREDENTIALS);

		logger.error("Setting ResourceDetails");

		return resourceDetails;

	}

}

@JsonIgnoreProperties(ignoreUnknown = true)
class EnterpriseMessagingService {

	public String clientid;
	public String clientsecret;
	public String granttype;
	public String tokenendpoint;

}