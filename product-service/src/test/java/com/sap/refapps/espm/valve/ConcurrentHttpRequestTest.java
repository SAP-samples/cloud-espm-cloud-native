package com.sap.refapps.espm.valve;

import com.sap.refapps.espm.Application;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Stream;

import static java.util.Collections.nCopies;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class ConcurrentHttpRequestTest {

	@Autowired
	private TestRestTemplate restTemplate;

	Map<HttpStatus, Long> getCountPerHttpStatus(List<ResponseEntity<String>> responseList) {
		Map<HttpStatus, Long> defaultCountPerStatus = Stream.of(HttpStatus.values())
				.collect(toMap(identity(), e -> 0L));
		responseList.stream().map(ResponseEntity::getStatusCode).collect(groupingBy(identity(), counting()))
				.forEach((code, count) -> defaultCountPerStatus.merge(code, count, Long::max));

		return defaultCountPerStatus;
	}

	public List<ResponseEntity<String>> sendConcurrentHttpRequests(int numberOfRequests, int numberOfThreads,
			String endpoint) {
		try {
			ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
			List<Callable<ResponseEntity<String>>> requests = nCopies(numberOfRequests,
					() -> sendHttpRequest(endpoint));
			List<Future<ResponseEntity<String>>> responseFutures = executorService.invokeAll(requests);

			executorService.shutdown();
			executorService.awaitTermination(5, TimeUnit.SECONDS);

			List<ResponseEntity<String>> responseList = new LinkedList<>();
			for (Future<ResponseEntity<String>> responseFuture : responseFutures) {
				responseList.add(responseFuture.get());
			}
			return responseList;
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	private ResponseEntity<String> sendHttpRequest(String endpoint) {
		HttpHeaders httpHeaders = new HttpHeaders();
		HttpEntity<String> httpRequestEntity = new HttpEntity<>("", httpHeaders);
		return restTemplate.exchange(endpoint, HttpMethod.GET, httpRequestEntity, String.class);
	}

}
