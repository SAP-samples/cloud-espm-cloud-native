package com.sap.refapps.espm.controller;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.cloudfoundry.com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.HttpEntity;
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
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;

import com.sap.cloud.security.xsuaa.client.OAuth2TokenResponse;
import com.sap.cloud.security.xsuaa.token.SpringSecurityContext;
import com.sap.cloud.security.xsuaa.token.Token;
import com.sap.cloud.security.xsuaa.client.ClientCredentials;
import com.sap.cloud.security.xsuaa.client.DefaultOAuth2TokenService;
import com.sap.cloud.security.xsuaa.client.XsuaaDefaultEndpoints;
import com.sap.cloud.security.xsuaa.tokenflows.TokenFlowException;
import com.sap.cloud.security.xsuaa.tokenflows.XsuaaTokenFlows;
import com.sap.refapps.espm.model.SalesOrder;
import com.sap.refapps.espm.model.Tax;
import javax.jms.JMSException;
import com.sap.refapps.espm.service.SalesOrderService;
import com.sap.refapps.espm.service.SalesOrderServiceImpl;

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
	 * @param salesOrderId,
	 *            status
	 * @return ResponseEntity<String> message
	 * @throws JSONException
	 */
	@PutMapping("/{salesOrderId}/{statusCode}")
	@ResponseBody
	public ResponseEntity<String> updateSalesOrder(@PathVariable("salesOrderId") final String salesOrderId,
			@PathVariable("statusCode") final String statusCode, @RequestBody final String note,
			RequestEntity<String> requestEntity) throws JSONException, IllegalArgumentException, TokenFlowException {
		if (salesOrderService.getById(salesOrderId) != null) {
			if (Arrays.stream(environment.getActiveProfiles()).anyMatch(env -> env.equalsIgnoreCase("local"))) {
				return errorMessage("Service is currently unavailable", HttpStatus.SERVICE_UNAVAILABLE);
			} else if (Arrays.stream(environment.getActiveProfiles()).anyMatch(env -> env.equalsIgnoreCase("cloud"))) {
				final String productId = salesOrderService.getById(salesOrderId).getProductId();
				final BigDecimal quantity = salesOrderService.getById(salesOrderId).getQuantity();
				if(statusCode.equalsIgnoreCase("S")){
					try{
						Token jwtToken = SpringSecurityContext.getToken();
						String appToken = jwtToken.getAppToken();
						headers.set("Authorization", "Bearer " + appToken);
						headers.set("Content-Type", "application/json");
						RestTemplate restTemplate = new RestTemplate();
						// creation of payload as json object from input
						String quanEdit = "-" + quantity;
						System.out.println("quanEdit "+quanEdit + "quantity "+quantity);
						BigDecimal addQuan = new BigDecimal(quanEdit);
						JSONObject updatedStock = new JSONObject();
						updatedStock.put("quantity", addQuan);
						updatedStock.put("productId", productId);
						final String S_PATH = geProductServiceUri() + productId;
						System.out.println("s_path " + S_PATH);
						HttpEntity<String> request = new HttpEntity<String>(updatedStock.toString(), headers);
						// call product service
						restTemplate.put(S_PATH, request); 	
					}catch(Exception e){
						return errorMessage(e.getMessage() + " " + productId,
								HttpStatus.BAD_REQUEST);
					}
				}
				salesOrderService.updateStatus(salesOrderId, statusCode, note);
				return new ResponseEntity<>("Sales Order with ID " + salesOrderId + " updated", HttpStatus.OK);
			}
		} else {
			
			return errorMessage("SalesOrder not found", HttpStatus.NOT_FOUND);
		}
		return errorMessage("Stock not found", HttpStatus.NOT_FOUND);
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
		String prodUrl = this.environment.getProperty("PROD_SERVICE")+"/product.svc/api/v1/stocks/";
		logger.info("***********Sale microservice endpoint is {}********",prodUrl);
		final String productserviceUri = Arrays.stream(environment.getActiveProfiles())
				.anyMatch(env -> (env.equalsIgnoreCase("cloud"))) ? prodUrl
						: productServiceEndPoint;

		return productserviceUri;
	}

}
