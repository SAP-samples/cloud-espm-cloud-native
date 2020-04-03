package com.sap.refapps.espm.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

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
import org.springframework.core.env.Environment;
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

import com.sap.cloud.security.xsuaa.client.OAuth2TokenResponse;
import com.sap.cloud.security.xsuaa.tokenflows.TokenFlowException;
import com.sap.cloud.security.xsuaa.tokenflows.XsuaaTokenFlows;
import com.sap.cloud.servicesdk.xbem.extension.sapcp.jms.MessagingServiceJmsConnectionFactory;
import com.sap.refapps.espm.model.SalesOrder;
import com.sap.refapps.espm.model.SalesOrderRepository;
import com.sap.refapps.espm.model.Tax;
import com.sap.refapps.espm.util.SalesOrderLifecycleStatusEnum;
import com.sap.refapps.espm.util.SalesOrderLifecycleStatusNameEnum;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.vavr.control.Try;

/**
 * This is the implementation class for the sales order service
 *
 */
@Profile("cloud")
@Service
public class CloudSalesOrderService implements SalesOrderService {

	private XsuaaTokenFlows tokenFlows;

	private final SalesOrderRepository salesOrderRepository;

	private final RestTemplate restTemplate;

	private static final Logger logger = LoggerFactory.getLogger(CloudSalesOrderService.class);

	@Value("${tax.service}")
	private String taxServiceEndPoint;

	@Value("${tax.destinationName}")
	private String taxDestination;

	private Iterable<SalesOrder> salesOrder;

	private String taxUri;

	private final ObjectMapper mapper = new ObjectMapper();

	private HashMap<String, String> taxUrlCache = new HashMap<>(1);

	private final HttpHeaders headers = new HttpHeaders();

	private final String taxEndPointSuffix = "/api/v1/calculate/tax?amount=";

	@Autowired(required = false)
	private MessagingServiceJmsConnectionFactory factory;

	@Autowired(required = false)
	private QueueDispatcherService queueDispatcherService;

	final static String DESTINATION_PATH = "/destination-configuration/v1/destinations/";

	@Autowired
	private Environment environment;

	/**
	 * @param salesOrderRepository
	 * @param rest
	 */
	@Autowired
	public CloudSalesOrderService(final SalesOrderRepository salesOrderRepository, final RestTemplate rest,
			XsuaaTokenFlows tokenFlows) {
		this.salesOrderRepository = salesOrderRepository;
		this.restTemplate = rest;
		this.tokenFlows = tokenFlows;
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
	public boolean insert(SalesOrder salesOrder, String profile) {

		final BigDecimal netAmount;
		Date now = new Date();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

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
		return queueDispatcherService.dispatch(salesOrder, salesOrder.getSalesOrderId());
	}

	@Override
	public Iterable<SalesOrder> getAll() {
		salesOrder = salesOrderRepository.getAllSalesOrders();
		return salesOrder;
	}

	@Override
	public Iterable<SalesOrder> getByEmail(String customerEmail) {
		salesOrder = salesOrderRepository.getAllSalesOrderForCustomer(customerEmail);
		return salesOrder;
	}

	@Override
	public SalesOrder getById(String salesOrderId) {
		return salesOrderRepository.findSalesOrderById(salesOrderId);
	}

	private Tax getTax(BigDecimal amount) {
		return applyCircuitBreaker(amount);
	}

	private Tax taxServiceFallback(BigDecimal amount) {
		logger.info(
				"Tax service is down or tax destination is not available. So a default tax will be set to the amount : {}",
				amount);
		final Tax tax = new Tax();
		tax.setTaxPercentage(00.00);
		tax.setTaxAmount(new BigDecimal(00.00));
		return tax;
	}

	@Override
	public void updateStatus(String salesOrderId, String lifecyleStatus, String note) {
		Optional<SalesOrder> optionalSalesOrder = salesOrderRepository.findById(salesOrderId);

		if (optionalSalesOrder.isPresent()) {
			SalesOrder salesOrder = optionalSalesOrder.get();
			salesOrder.setNote(note);
			salesOrder.setLifecycleStatus(SalesOrderLifecycleStatusEnum.valueOf(lifecyleStatus).toString());
			salesOrder.setLifecycleStatusName(SalesOrderLifecycleStatusNameEnum.valueOf(lifecyleStatus).toString());
			salesOrderRepository.save(salesOrder);
		}

	}

	/**
	 * This method returns a desired Tax(taxAmount, taxPercentage) value when the
	 * TaxService is up. If the TaxService is down, it applies a combination of
	 * following fault tolerance patterns in a sequence: TimeLimiter, CircuitBreaker
	 * and Retry using a Callable. Furthermore, if the service is still down, it
	 * recovers from the exception by calling a fallback method using Try monad from
	 * the Vavr library.
	 * 
	 * @param amount
	 * @return
	 */
	private Tax applyCircuitBreaker(BigDecimal amount) {

		CircuitBreaker circuitBreaker = configureCircuitBreaker();
		TimeLimiter timeLimiter = configureTimeLimiter();
		Retry retry = configureRetry();

		Supplier<CompletableFuture<Tax>> futureSupplier = () -> CompletableFuture.supplyAsync(() -> supplyTax(amount));
		Callable<Tax> callable = TimeLimiter.decorateFutureSupplier(timeLimiter, futureSupplier);
		callable = CircuitBreaker.decorateCallable(circuitBreaker, callable);
		callable = Retry.decorateCallable(retry, callable);

		// Executing the decorated callable and recovering from any exception by
		// calling the fallback method
		Try<Tax> result = Try.ofCallable(callable).recover(throwable -> taxServiceFallback(amount));
		return result.get();
	}

	/**
	 * Creating a circuitbreaker using custom configuration
	 * 
	 * @return
	 */
	private CircuitBreaker configureCircuitBreaker() {
		CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom().failureRateThreshold(20)
				.waitDurationInOpenState(Duration.ofMillis(3000)).ringBufferSizeInClosedState(10)
				.ringBufferSizeInHalfOpenState(5).build();
		CircuitBreakerRegistry circuitBreakerRegistry = CircuitBreakerRegistry.of(circuitBreakerConfig);
		CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("taxservice");
		return circuitBreaker;
	}

	/**
	 * Creating a TimeLimiter using custom configuration
	 * 
	 * @return
	 */
	private TimeLimiter configureTimeLimiter() {
		TimeLimiterConfig timeLimiterConfig = TimeLimiterConfig.custom().timeoutDuration(Duration.ofMillis(1000))
				.cancelRunningFuture(false).build();
		TimeLimiter timeLimiter = TimeLimiter.of(timeLimiterConfig);
		return timeLimiter;
	}

	/**
	 * Creating a Retry using custom configuration
	 * 
	 * @return
	 */
	private Retry configureRetry() {
		RetryConfig retryConfig = RetryConfig.custom().maxAttempts(3).waitDuration(Duration.ofMillis(5000)).build();
		Retry retry = Retry.of("taxservice", retryConfig);
		return retry;
	}

	/**
	 * @param amount
	 * @return
	 */
	private Tax supplyTax(BigDecimal amount) {

		Tax tax;
		taxUri = taxUrlCache.get("TAX_URI");

		if (taxUri == null) {

			taxUri = getTaxUri();

			taxUrlCache.put("TAX_URI", taxUri);

		}

		if (taxUri == "") {
			logger.info("Calling fall back Tax calculation as Tax destination is not found");
			tax = taxServiceFallback(amount);
		} else {

			URI uri = URI.create(taxUri + amount);
			// tax = this.restTemplate.getForObject(uri, Tax.class);
			OAuth2TokenResponse clientCredentialsTokenResponse = null;
			try {
				clientCredentialsTokenResponse = tokenFlows.clientCredentialsTokenFlow().execute();
			} catch (TokenFlowException e1) {
				logger.error("Couldnt get client credentials token: {}", e1.getMessage());
			}
			String appToken = clientCredentialsTokenResponse.getAccessToken();
			headers.set("Authorization", "Bearer " + appToken);
			HttpEntity entity = new HttpEntity(headers);
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

	private String getTaxUri() {
		String taxUrlDest = getTaxUrlFromDestinationService() + taxEndPointSuffix;
		logger.info("***********Tax microservice endpoint is {}********", taxUrlDest);
		final String taxUri = Arrays.stream(environment.getActiveProfiles())

				.anyMatch(env -> (env.equalsIgnoreCase("cloud"))) ? taxUrlDest

						: taxServiceEndPoint;

		return taxUri;
	}

	private String getTaxUrlFromDestinationService() {
		try {
			final DestinationService destination = getDestinationServiceDetails();
			final String accessToken = getOAuthToken(destination);
			headers.set("Authorization", "Bearer " + accessToken);
			HttpEntity entity = new HttpEntity(headers);
			final String taxUrl = destination.uri + DESTINATION_PATH + taxDestination;
			final ResponseEntity<String> response = restTemplate.exchange(taxUrl, HttpMethod.GET, entity, String.class);
			final JsonNode root = mapper.readTree(response.getBody());
			final String texDestination = root.path("destinationConfiguration").path("URL").asText();
			return texDestination;
		} catch (IOException e) {
			logger.error("No proper destination Service available: {}", e.getMessage());

		}
		return "";
	}

	private String getOAuthToken(final DestinationService destination) throws IOException {
		final String auth = destination.clientid + ":" + destination.clientsecret;
		final byte[] basicToken = Base64.getEncoder().encode(auth.getBytes());
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		headers.set("Authorization", "Basic " + new String(basicToken));
		MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
		map.add("client_id", destination.clientid);
		map.add("grant_type", "client_credentials");
		final HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map,
				headers);
		ResponseEntity<String> response = restTemplate.postForEntity(destination.url + "/oauth/token", request,
				String.class);
		final String responseString = response.getBody();
		JsonNode root = mapper.readTree(responseString);
		final String accessToken = root.get("access_token").asText();
		return accessToken;

	}

	private DestinationService getDestinationServiceDetails() throws IOException {
		final String destinationService = System.getenv("VCAP_SERVICES");
		final JsonNode root = mapper.readTree(destinationService);
		final JsonNode destinations = root.get("destination").get(0).get("credentials");
		final DestinationService destination = mapper.treeToValue(destinations, DestinationService.class);
		return destination;

	}

	private synchronized void sendMessage(final String messageString) throws JMSException {

		final Connection connection = factory.createConnection();
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