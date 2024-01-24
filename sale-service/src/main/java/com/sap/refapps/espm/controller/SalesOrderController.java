package com.sap.refapps.espm.controller;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.UUID;

import jakarta.jms.JMSException;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.RestTemplate;

import com.sap.cloud.security.xsuaa.token.SpringSecurityContext;
import com.sap.cloud.security.xsuaa.token.Token;
import com.sap.cloud.security.xsuaa.tokenflows.TokenFlowException;
import com.sap.refapps.espm.model.SalesOrder;
import com.sap.refapps.espm.service.SalesOrderService;

/**
 * This class is a controller class of sales service which is responsible for
 * handling all endpoints.
 *
 */
@RestController
@RequestMapping("sale.svc/api/v1/salesOrders")
public class SalesOrderController {
	@Value("${product.service}")
	private String productServiceEndPoint;
	protected static final String V1_PATH = "/v1/salesOrders";
	private static final Logger logger = LoggerFactory.getLogger(SalesOrderController.class);

	private final HttpHeaders headers = new HttpHeaders();

	@Autowired
	private SalesOrderService salesOrderService;

	private SalesOrder salesorder;

	@Autowired
	private Environment environment;
	private RequestCallback requestCallback;

	/**
	 * It creates a sales order
	 * 
	 * @param salesOrder
	 * @return ResponseEntity<String> message
	 */
	@PostMapping
	public ResponseEntity<String> createSalesOrder(@RequestBody final SalesOrder salesOrder)
			throws UnsupportedEncodingException, JMSException, JsonProcessingException {
		String soId = UUID.randomUUID().toString();
		salesOrder.setSalesOrderId(soId);

		if (Arrays.stream(environment.getActiveProfiles()).anyMatch(env -> env.equalsIgnoreCase("local"))) {
			if (!salesOrderService.insert(salesOrder, "local"))
				return errorMessage("Service is currently unavailable", HttpStatus.SERVICE_UNAVAILABLE);
		} else if (Arrays.stream(environment.getActiveProfiles()).anyMatch(env -> env.equalsIgnoreCase("cloud"))) {
			salesOrderService.insert(salesOrder);
		}

		return new ResponseEntity<>("Sales Order with ID " + soId + " created", HttpStatus.ACCEPTED);
	}

	/**
	 * update sales order
	 * 
	 * @param salesOrderId, status
	 * @return ResponseEntity<String> message
	 * @throws JSONException
	 */
	@PutMapping("/{salesOrderId}/{statusCode}")
	@ResponseBody
	public ResponseEntity<String> updateSalesOrder(@PathVariable("salesOrderId") final String salesOrderId,
			@PathVariable("statusCode") String statusCode, @RequestBody String note,
			RequestEntity<String> requestEntity) throws JSONException, IllegalArgumentException, TokenFlowException {
		if (salesOrderService.getById(salesOrderId) != null) {

			final String productId = salesOrderService.getById(salesOrderId).getProductId();
			final BigDecimal quantity = salesOrderService.getById(salesOrderId).getQuantity();
			if (statusCode.equalsIgnoreCase("S")) {
				try {
					if (Arrays.stream(environment.getActiveProfiles()).anyMatch(env -> env.equalsIgnoreCase("cloud"))) {
						Token jwtToken = SpringSecurityContext.getToken();
						String appToken = jwtToken.getAppToken();
						headers.set("Authorization", "Bearer " + appToken);
					}
					headers.set("Content-Type", "application/json");
					RestTemplate restTemplate = new RestTemplate();
					// creation of payload as json object from input
					String quanEdit = "-" + quantity;
					BigDecimal addQuan = new BigDecimal(quanEdit);
					JSONObject updatedStock = new JSONObject();
					updatedStock.put("quantity", addQuan);
					updatedStock.put("productId", productId);
					logger.info("updated stock:", updatedStock);
					final String S_PATH = geProductServiceUri() + productId;
					logger.info("product URL:", S_PATH);
					HttpEntity<String> request = new HttpEntity<String>(updatedStock.toString(), headers);

					// call product service
					ResponseEntity<String> response = restTemplate.exchange(S_PATH, HttpMethod.PUT, request,
							String.class);
					response.getStatusCode();
					logger.info("status code:", response.getStatusCode());
					logger.info("get response value:", response.getStatusCode().value());
					String responseBody = response.getBody();
					logger.info("response body for success", responseBody);
					if (response.getStatusCode().value() == HttpStatus.OK.value()) {
						salesOrderService.updateStatus(salesOrderId, statusCode, note);
					} else if (response.getStatusCode().value() == HttpStatus.NO_CONTENT.value()) {
						statusCode = "C";
						note = "Out of Stock";
						salesOrderService.updateStatus(salesOrderId, statusCode, note);
						return errorMessage("Out of stock" + productId, HttpStatus.NO_CONTENT);
					}
				}

				catch (Exception e) {
					return errorMessage(e.getMessage() + " " + productId, HttpStatus.BAD_REQUEST);
				}
			} else if (statusCode.equalsIgnoreCase("R")) {
				statusCode = "R";
				note = "Rejected by Retailer";
				salesOrderService.updateStatus(salesOrderId, statusCode, note);
			}
			return new ResponseEntity<>("Sales Order with ID " + salesOrderId + " updated", HttpStatus.OK);

		} else {

			return errorMessage("SalesOrder not found", HttpStatus.NOT_FOUND);
		}
	}

	/**
	 * Returns list of sales orders based on customer email.
	 * 
	 * @param customerEmail
	 * @return list of sales order
	 */
	@GetMapping("/email/{customerEmail}")
	public ResponseEntity<Iterable<SalesOrder>> getSalesOrdersByCustomerEmail(
			@PathVariable("customerEmail") final String customerEmail) {

		final Iterable<SalesOrder> salesOrders = salesOrderService.getByEmail(customerEmail);
		if (salesOrders.iterator().hasNext())
			return new ResponseEntity<>(salesOrders, HttpStatus.OK);
		return errorMessage("Customer with email Address " + customerEmail
				+ " not found or customer does not have any sales orders", HttpStatus.NOT_FOUND);
	}

	/**
	 * Returns a sales order based on sales order id.
	 * 
	 * @param salesOrderId
	 * @return sales order
	 */
	@GetMapping("/{salesOrderId}")
	public ResponseEntity<SalesOrder> getSalesOrderById(@PathVariable("salesOrderId") final String salesOrderId) {

		final SalesOrder salesOrders = salesOrderService.getById(salesOrderId);
		if (salesOrders != null)
			return new ResponseEntity<>(salesOrders, HttpStatus.OK);
		return errorMessage("Sales order not found", HttpStatus.NOT_FOUND);
	}

	/**
	 * Returns all sales orders.
	 * 
	 * @return list of sales order
	 */
	@GetMapping
	public ResponseEntity<Iterable<SalesOrder>> getAllSalesOrders() {
		final Iterable<SalesOrder> salesOrders = salesOrderService.getAll();
		return new ResponseEntity<>(salesOrders, HttpStatus.OK);

	}

	/**
	 * It is used to print the error message.
	 * 
	 * @param message
	 * @param status
	 * @return ResponseEntity of status,headers and body
	 */
	private ResponseEntity errorMessage(String message, HttpStatus status) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(org.springframework.http.MediaType.TEXT_PLAIN);
		return ResponseEntity.status(status).headers(headers).body(message);
	}

	private String geProductServiceUri() {
		String prodUrl = this.environment.getProperty("PROD_SERVICE") + "/product.svc/api/v1/stocks/";

		final String productserviceUri = Arrays.stream(environment.getActiveProfiles())
				.anyMatch(env -> (env.equalsIgnoreCase("cloud"))) ? prodUrl : productServiceEndPoint;
		logger.info("***********Productservice end point used in Sales is {}********", productserviceUri);

		return productserviceUri;
	}

}
