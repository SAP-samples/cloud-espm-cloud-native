package com.sap.refapps.espm.service;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import javax.jms.JMSException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.cloudfoundry.com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.cloud.cloudfoundry.com.fasterxml.jackson.databind.DeserializationFeature;
import org.springframework.cloud.cloudfoundry.com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

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
@Profile("local")
@Service
public class LocalSalesOrderService implements SalesOrderService {

	private final SalesOrderRepository salesOrderRepository;

	private final RestTemplate restTemplate;

	private static final Logger logger = LoggerFactory.getLogger(CloudSalesOrderService.class);

	@Value("${tax.service}")
	private String taxServiceEndPoint;

	private Iterable<SalesOrder> salesOrder;

	private String taxUri;

	private final ObjectMapper mapper = new ObjectMapper();
	
	@Autowired(required = false)
	private QueueDispatcherService queueDispatcherService;

	final static String DESTINATION_PATH = "/destination-configuration/v1/destinations/";

	/**
	 * @param salesOrderRepository
	 * @param rest
	 */
	@Autowired
	public LocalSalesOrderService(final SalesOrderRepository salesOrderRepository, final RestTemplate rest) {
		this.salesOrderRepository = salesOrderRepository;
		this.restTemplate = rest;
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	@Override
	public void insert(final SalesOrder salesOrder)
			throws JsonProcessingException, UnsupportedEncodingException, JMSException {}
	
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
		logger.info("Tax service is down. So a default tax will be set to the amount : {}", amount);
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
	 * This method returns a desired Tax(taxAmount, taxPercentage) value when the TaxService is up. 
	 * If the TaxService is down, it applies a combination of following fault tolerance patterns 
	 * in a sequence: TimeLimiter, CircuitBreaker and Retry using a Callable. Furthermore, if the 
	 * service is still down, it recovers from the exception by calling a fallback method using
	 * Try monad from the Vavr library. 
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
		
		//Executing the decorated callable and recovering from any exception by calling the fallback method
		Try<Tax> result = Try.ofCallable(callable).recover(throwable -> taxServiceFallback(amount));
		return result.get();
	}
	
	/**
	 * Creating a circuitbreaker using custom configuration
	 * 
	 * @return
	 */
	private CircuitBreaker configureCircuitBreaker() {
		CircuitBreakerConfig circuitBreakerConfig = 
				CircuitBreakerConfig.custom()
									.failureRateThreshold(20)
									.waitDurationInOpenState(Duration.ofMillis(3000))
									.ringBufferSizeInClosedState(10)
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
		TimeLimiterConfig timeLimiterConfig = 
				TimeLimiterConfig.custom()
								.timeoutDuration(Duration.ofMillis(1000))
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
		RetryConfig retryConfig = 
				RetryConfig.custom()
							.maxAttempts(3)
							.waitDuration(Duration.ofMillis(5000)).build();
		Retry retry = Retry.of("taxservice", retryConfig);
		return retry;
	}

	/**
	 * @param amount
	 * @return
	 */
	private Tax supplyTax(BigDecimal amount) {

		Tax tax;
		taxUri = taxServiceEndPoint;
		
		try {
			URI uri = URI.create(taxUri + amount);
			tax = restTemplate.getForObject(uri, Tax.class);

		} catch (ResourceAccessException e) {
    		logger.info("Retrying to connect to the TaxService...");
    		taxUri = taxServiceEndPoint;
			URI uri = URI.create(taxUri + amount);
    		tax = restTemplate.getForObject(uri, Tax.class);
    		
		}
		
		logger.info("Tax service endpoint is {}", taxUri);
		logger.info("Tax service is called to calculate tax for amount : {}", amount);
		logger.info("Tax amount is : {}", tax.getTaxAmount());

		return tax;
	}

}
