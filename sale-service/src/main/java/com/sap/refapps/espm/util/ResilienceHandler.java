package com.sap.refapps.espm.util;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.sap.refapps.espm.model.Tax;
import com.sap.refapps.espm.service.AbstractSalesOrderService;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.vavr.control.Try;

/**
 * Offers resiliencies for service to service communication to ensure high availability
 * of remote services. Configures and applies some of the useful fault tolerance patterns 
 * e.g retry, time limiter and circuit breaker during the invocation of a remote service. 
 * Also provides fallback methods as a recovery mechanism for failed operations and returns 
 * the default values. Uses Resilience4j library.
 *  
 * @author C5258401
 *
 */

@Component
public class ResilienceHandler {

	private final AbstractSalesOrderService salesOrderService;
	
	private static final Logger logger = LoggerFactory.getLogger(ResilienceHandler.class);
	
	@Autowired
	public ResilienceHandler (@Lazy final AbstractSalesOrderService abstractsalesOrderService) {
		this.salesOrderService = abstractsalesOrderService;
	}
	
	/**
	 * Creating a circuit breaker using custom configuration
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
	 * This method returns a desired Tax(taxAmount, taxPercentage) value when the TaxService is up. 
	 * If the TaxService is down, it applies a combination of following fault tolerance patterns 
	 * in a sequence: TimeLimiter, CircuitBreaker and Retry using a Callable. When all the attempts 
	 * are exhausted it calls a fallback method to recover from failure and offers the default tax value.
	 * 
	 * @param amount
	 * @return
	 */
	public Tax applyResiliencePatterns(BigDecimal amount) {

		CircuitBreaker circuitBreaker = configureCircuitBreaker();
		TimeLimiter timeLimiter = configureTimeLimiter();
		Retry retry = configureRetry();
		
		Supplier<CompletableFuture<Tax>> futureSupplier = () -> CompletableFuture.supplyAsync(() -> salesOrderService.supplyTax(amount));
		Callable<Tax> callable = TimeLimiter.decorateFutureSupplier(timeLimiter, futureSupplier);
		callable = CircuitBreaker.decorateCallable(circuitBreaker, callable);
		callable = Retry.decorateCallable(retry, callable);
		
		//Executing the decorated callable and recovering from any exception by calling the fallback method
		Try<Tax> result = Try.ofCallable(callable).recover(throwable -> taxServiceFallback(amount));
		return result.get();
	}
	
	/**
	 * Fallback method to be called when the tax-service is down and supplies default tax value.
	 *  
	 * @param amount
	 * @return
	 */
	protected Tax taxServiceFallback(BigDecimal amount) {
		logger.info("Tax service is down. So a default tax will be set to the amount : {}", amount);
		final Tax tax = new Tax();
		tax.setTaxPercentage(00.00);
		tax.setTaxAmount(new BigDecimal(00.00));
		return tax;
	}
}
