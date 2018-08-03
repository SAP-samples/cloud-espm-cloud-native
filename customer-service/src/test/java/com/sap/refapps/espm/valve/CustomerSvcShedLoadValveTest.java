package com.sap.refapps.espm.valve;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;

/**
 * This class is used to test the customer shed load valve.
 *
 */
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
		"classpath:CustomerSvcShedLoadValveTestData.sql" })
public class CustomerSvcShedLoadValveTest extends ConcurrentHttpRequestTest {
	@Value("${max.requests}")
	private int maxRequests;

	@Test
	public void shedLoadWithValve() {

		int requestsToSend = maxRequests * 2;
		List<ResponseEntity<String>> responseList = sendConcurrentHttpRequests(requestsToSend, requestsToSend,
				"/customer.svc/api/v1/customers/paul.burke@itelo1.info");

		Map<HttpStatus, Long> responseCodeCounts = getCountPerHttpStatus(responseList);
		System.out.println("========PRINT RESPONSE MAP============");
		System.out.println(responseCodeCounts);
		assertThat(responseCodeCounts.get(HttpStatus.OK)).isGreaterThanOrEqualTo(maxRequests);
		assertThat(responseCodeCounts.get(HttpStatus.INTERNAL_SERVER_ERROR)).isEqualTo(0);
		assertThat(responseCodeCounts.get(HttpStatus.SERVICE_UNAVAILABLE)).isGreaterThan(0);
	}
}
