package com.sap.refapps.espm.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.cloudfoundry.com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.cloud.cloudfoundry.com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.cloud.cloudfoundry.com.fasterxml.jackson.databind.DeserializationFeature;
import org.springframework.cloud.cloudfoundry.com.fasterxml.jackson.databind.JsonNode;
import org.springframework.cloud.cloudfoundry.com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.sap.cloud.sdk.cloudplatform.connectivity.DefaultHttpDestination;
import com.sap.cloud.sdk.cloudplatform.connectivity.Destination;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationLoader;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationLoaderChain;
import com.sap.cloud.sdk.cloudplatform.connectivity.ScpCfDestinationLoader;
import com.sap.cloud.security.xsuaa.client.ClientCredentials;
import com.sap.cloud.security.xsuaa.client.DefaultOAuth2TokenService;
import com.sap.cloud.security.xsuaa.client.OAuth2TokenResponse;
import com.sap.cloud.security.xsuaa.client.XsuaaDefaultEndpoints;
import com.sap.cloud.security.xsuaa.tokenflows.TokenFlowException;
import com.sap.cloud.security.xsuaa.tokenflows.XsuaaTokenFlows;
import com.sap.cloud.servicesdk.xbem.extension.sapcp.jms.MessagingServiceJmsConnectionFactory;
import com.sap.refapps.espm.model.SalesOrder;
import com.sap.refapps.espm.model.SalesOrderRepository;
import com.sap.refapps.espm.model.Tax;
import com.sap.refapps.espm.util.ResilienceHandler;
import com.sap.refapps.espm.util.SalesOrderLifecycleStatusEnum;
import com.sap.refapps.espm.util.SalesOrderLifecycleStatusNameEnum;

/**
 * Implementation class for the sales order service, deployable in cloud
 * environment.
 *
 */
@Profile("cloud")
@Service
public class CloudSalesOrderService extends AbstractSalesOrderService {

	private static final Logger logger = LoggerFactory.getLogger(CloudSalesOrderService.class);

	@Value("${tax.destinationName}")
	private String taxDestination;

	private String taxUri;

	private final ObjectMapper mapper = new ObjectMapper();

	private HashMap<String, String> taxUrlCache = new HashMap<>(1);

	private final HttpHeaders headers = new HttpHeaders();

	private final String taxEndPointSuffix = "/tax.svc/api/v1/calculate/tax?amount=";

	private final static String DESTINATION_NAME = "tax";

	@Autowired(required = false)
	private MessagingServiceJmsConnectionFactory jmsConnectionFactory;

	/**
	 * @param salesOrderRepository
	 * @param rest
	 */
	@Autowired
	public CloudSalesOrderService(final SalesOrderRepository salesOrderRepository, final RestTemplate rest,
			final ResilienceHandler resilienceHandler) {
		super(salesOrderRepository, rest, resilienceHandler);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	@Override
	public void insert(final SalesOrder salesOrder)
			throws JsonProcessingException, UnsupportedEncodingException, JMSException {

		final BigDecimal netAmount;
		Date now = new Date();
		DateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");

		// create MathContext object with 4 precision
		MathContext mc = new MathContext(15);
		final Tax tax = getTax(salesOrder.getGrossAmount());
		netAmount = tax.getTaxAmount().add(salesOrder.getGrossAmount(), mc);
		salesOrder.setLifecycleStatus(SalesOrderLifecycleStatusEnum.N.toString());
		salesOrder.setLifecycleStatusName(SalesOrderLifecycleStatusNameEnum.N.toString());
		salesOrder.setNetAmount(netAmount);
		salesOrder.setTaxAmount(tax.getTaxAmount());
		salesOrder.setQuantityUnit("EA");
		salesOrder.setCreatedAt(dateFormat.format(now));

		final ObjectMapper mapper = new ObjectMapper();
		final String salesOrderString = mapper.writeValueAsString(salesOrder);

		sendMessage(salesOrderString);
	}

	@Override
	public Tax supplyTax(BigDecimal amount) {

		Tax tax;
		taxUri = taxUrlCache.get("TAX_URI");

		if (taxUri == null) {
			taxUri = getTaxUri();
			taxUrlCache.put("TAX_URI", taxUri);
		}

		if (taxUri == "") {
			logger.info("Calling fall back Tax calculation as Tax destination is not found");
			tax = resilienceHandler.applyResiliencePatterns(amount);

		} else {
			URI uri = URI.create(taxUri + amount);
			HttpEntity entity = new HttpEntity(evaluateTokenAndAuthorizeClient());

			try {
				// If Tax URI does not work check again in the destination if the URL in
				// destination has changed.
				taxUri = getTaxUri();
				taxUrlCache.put("TAX_URI", taxUri);
				uri = URI.create(taxUri + amount);
				ResponseEntity<Tax> responseEntity = restTemplate.exchange(uri, HttpMethod.GET, entity, Tax.class);
				tax = responseEntity.getBody();

			} catch (HttpClientErrorException e) {
				logger.info("Retrying to connect to the tax service...");
				uri = URI.create(taxUri + amount);
				ResponseEntity<Tax> responseEntity = restTemplate.exchange(uri, HttpMethod.GET, entity, Tax.class);
				tax = responseEntity.getBody();
			}
		}
		logger.info("Tax service endpoint is {}", taxUri);
		logger.info("Tax service is called to calculate tax for amount : {}", amount);
		logger.info("Tax amount is : {}", tax.getTaxAmount());

		return tax;
	}

	private HttpHeaders evaluateTokenAndAuthorizeClient() {

		OAuth2TokenResponse clientCredentialsTokenResponse = null;
		try {
			// client credentials flow
			final DestinationService configuration = getDestinationServiceDetails("xsuaa");
			String clientSecret = configuration.clientsecret;
			String clientId = configuration.clientid;
			String url = configuration.url;
			final XsuaaDefaultEndpoints xsuaaDefaultEndpoints = new XsuaaDefaultEndpoints(url);
			final ClientCredentials clientCredentials = new ClientCredentials(clientId, clientSecret);
			final DefaultOAuth2TokenService defaultOAuth2TokenService = new DefaultOAuth2TokenService();

			// XsuaaTokenFlows are used to get access to token flow builders which inturn is
			// used for getting a technical user token.
			XsuaaTokenFlows xsuaaTokenFlows = new XsuaaTokenFlows(defaultOAuth2TokenService, xsuaaDefaultEndpoints,
					clientCredentials);
			clientCredentialsTokenResponse = xsuaaTokenFlows.clientCredentialsTokenFlow().execute();
		} catch (TokenFlowException e) {
			logger.error("Couldn't get client credentials token: {}", e.getMessage());
		} catch (IOException e) {
			logger.error("No proper XSUAA Service available: {}", e.getMessage());
		} catch (Exception e) {
			logger.error("Please contact the administrator for details: {}", e.getMessage());
		}

		String appToken = clientCredentialsTokenResponse.getAccessToken();
		headers.set("Authorization", "Bearer " + appToken);

		return headers;
	}

	private String getTaxUri() {

		String taxUrl = getTaxUrlFromDestinationService() + taxEndPointSuffix;
		logger.info("***********Tax microservice endpoint is {}********", taxUrl);
		return taxUrl;
	}

	private String getTaxUrlFromDestinationService() {

		try {
			DestinationLoader customLoaderChain = DestinationLoaderChain.builder(new ScpCfDestinationLoader()).build();
			DestinationAccessor.setLoader(customLoaderChain);
			Destination destination = DestinationAccessor.getDestination(DESTINATION_NAME);

			final DefaultHttpDestination destination2 = destination.asHttp().decorate(DefaultHttpDestination::new);

			if (destination2.getUri() != null) {
				return destination2.getUri().toString();
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}

		return "";
	}

	private DestinationService getDestinationServiceDetails(String node) throws IOException {
		final String destinationService = System.getenv("VCAP_SERVICES");
		final JsonNode root = mapper.readTree(destinationService);
		final JsonNode destinations = root.get(node).get(0).get("credentials");
		final DestinationService destination = mapper.treeToValue(destinations, DestinationService.class);
		return destination;
	}

	private synchronized void sendMessage(final String messageString) throws JMSException {

		final Connection connection = jmsConnectionFactory.createConnection();
		connection.start();
		final Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		final Queue queue = session.createQueue("queue:" + System.getenv("QUEUE_NAME"));
		final MessageProducer messageProducer = session.createProducer(queue);
		TextMessage message = session.createTextMessage(messageString);
		messageProducer.send(message);
		session.close();
		connection.close();

	}

}

@JsonIgnoreProperties(ignoreUnknown = true)
class DestinationService {

	public String clientid;
	public String clientsecret;
	public String uri;
	public String url;

}
