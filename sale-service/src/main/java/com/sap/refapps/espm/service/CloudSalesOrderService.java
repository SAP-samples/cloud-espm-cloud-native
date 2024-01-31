package com.sap.refapps.espm.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Optional;
import jakarta.jms.Connection;
import jakarta.jms.JMSException;
import jakarta.jms.MessageProducer;
import jakarta.jms.Queue;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.cloud.security.xsuaa.client.OAuth2TokenResponse;
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

	private static final String DESTINATION = "destination";
	private static final String TOKEN_ENDPOINT = "/oauth/token";
	private static final String AUTHORIZATION = "Authorization";
	private static final String CONTENT_TYPE = "Content-Type";
	private static final String GRANT_TYPE = "grant_type=";
	private static final String CLIENT_CREDENTIALS = "client_credentials";
	private static final String UTF8 = "UTF-8";
	private static final String ACCESS_TOKEN = "access_token";
	private static final String DESTINATION_ENDPOINT = "/destination-configuration/v1/destinations/";
	private static final String DESTINATION_CONFIG = "destinationConfiguration";
	
	private String accessToken;
	
	private DestinationService destination;

	@Autowired(required = false)
	private MessagingServiceJmsConnectionFactory jmsConnectionFactory;
	
	@Autowired
	private XsuaaTokenFlows xsuaaTokenFlows;

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
			clientCredentialsTokenResponse = xsuaaTokenFlows.clientCredentialsTokenFlow().execute();
		} catch (TokenFlowException e) {
			logger.error("Couldn't get client credentials token: {}", e.getMessage());
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

		Optional<String> destinationUrl = Optional.empty();
		try {
			Optional<String> destinationConfigDetails = getDestinationConfiguration();
			ObjectMapper mapper = new ObjectMapper();
			JsonNode vcap = mapper.readTree(destinationConfigDetails.get());
			JsonNode destinationConfiguration = vcap.get(DESTINATION_CONFIG);
			destinationUrl = Optional.of(destinationConfiguration.get("URL").asText());
		} catch(IOException io) {
			logger.error(io.getMessage());
		} catch (Exception e) {
			logger.error(e.getMessage());
		}

		return destinationUrl.get();
	}
	
	/**
	 * Sends a request to get the destination configuration details by authorizing user with a bearer token
	 * and returns the details.
	 * 
	 * @return
	 * @throws IOException
	 */
	private Optional<String> getDestinationConfiguration() throws IOException {
		
		Optional<String> destinationConfig = Optional.empty();
		destination = getDestinationServiceDetails(DESTINATION);
		final String destinationRequestUrl =  destination.uri + DESTINATION_ENDPOINT + taxDestination;
		logger.info("Fetching the destination configuration attributes using the request url {}", destinationRequestUrl);
		
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpGet httpGet = new HttpGet(destinationRequestUrl);
		httpGet.setHeader(HttpHeaders.CONTENT_TYPE,"application/json");
		logger.info("Authorizing user with bearer token...");
		httpGet.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken());
		
		HttpResponse response = httpClient.execute(httpGet);
		BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), UTF8));
		destinationConfig = Optional.of(br.readLine());
		
		return destinationConfig;
	}
	
	/**
	 * Reads the access token content returned by the api authorizeAndConfigureNewToken(), 
	 * fetches the access token and returns it.
	 * 
	 * @return String
	 */
	private String getAccessToken() {
		
		Optional<String> accessTokenContent = Optional.empty();
		try {
			accessTokenContent = authorizeAndConfigureNewToken();
			if(accessTokenContent.isPresent()) {
				ObjectMapper mapper = new ObjectMapper();
				TypeReference<HashMap<String, String>> typeRef = new TypeReference<HashMap<String,String>>() {};
				HashMap<String, String> hashMap = mapper.readValue(accessTokenContent.get(), typeRef);
				accessToken = hashMap.get(ACCESS_TOKEN);
			}
		} catch (JsonParseException e) {
			logger.error(e.getMessage());
		} catch (JsonMappingException e) {
			logger.error(e.getMessage());
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		return accessToken;
	}
	
	/**
	 * 
	 * Authorizes user and configures a new access token using client credentials as the grant type.
	 * Returns the complete content of an access token.
	 * 
	 * @return
	 * @throws JsonProcessingException
	 * @throws IOException
	 */
	private Optional<String> authorizeAndConfigureNewToken() throws JsonProcessingException, IOException {
		
		destination = getDestinationServiceDetails(DESTINATION);
		Optional<String> tokenContent = Optional.empty();
		final String tokenURL =  destination.url + TOKEN_ENDPOINT;
		
		// authentication credentials encoding
		String base64Credentials = Base64.getEncoder().encodeToString(
				(destination.clientid + ":" + destination.clientsecret).getBytes());
		
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpPost httpPost = new HttpPost(tokenURL);
		logger.info("Generates new token using client credentials...");
		httpPost.addHeader(AUTHORIZATION, "Basic " + base64Credentials);
		httpPost.addHeader(CONTENT_TYPE, "application/x-www-form-urlencoded");
		
		StringEntity input = new StringEntity(GRANT_TYPE + CLIENT_CREDENTIALS);
		httpPost.setEntity(input);
		
		//send and retrieve response
		HttpResponse response = httpClient.execute(httpPost);
		BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), UTF8));
		tokenContent = Optional.of(br.readLine());
		
		return tokenContent;
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
