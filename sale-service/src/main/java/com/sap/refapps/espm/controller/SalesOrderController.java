package com.sap.refapps.espm.controller;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.cloudfoundry.com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sap.refapps.espm.model.SalesOrder;
import com.sap.refapps.espm.model.Tax;
import com.sap.refapps.espm.service.SalesOrderService;

import javax.jms.JMSException;

/**
 * This class is a controller class of sales service which is responsible for
 * handling all endpoints.
 *
 */
@RestController
@RequestMapping("sale.svc/api/v1/salesOrders")
public class SalesOrderController {

	protected static final String V1_PATH = "/v1/salesOrders";

	@Autowired
	private SalesOrderService salesOrderService;
	
	@Autowired
	private Environment environment;

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
		
		if(Arrays.stream(environment.getActiveProfiles()).anyMatch(env -> env.equalsIgnoreCase("local"))) {
			if(!salesOrderService.insert(salesOrder, "local"))
				return errorMessage("Service is currently unavailable",HttpStatus.SERVICE_UNAVAILABLE);
		}
		else if(Arrays.stream(environment.getActiveProfiles()).anyMatch(env -> env.equalsIgnoreCase("cloud"))) {
			salesOrderService.insert(salesOrder);
		}
		
		return new ResponseEntity<>("Sales Order with ID " + soId + " created", HttpStatus.ACCEPTED);
	}

	/**
	 * update sales order
	 * 
	 * @param salesOrderId, status
	 * @return ResponseEntity<String> message
	 */
	@PutMapping("/{salesOrderId}/{statusCode}")
	public ResponseEntity<String> updateSalesOrder(@PathVariable("salesOrderId") final String salesOrderId,
			@PathVariable("statusCode") final String statusCode, @RequestBody final String note) {
		if (salesOrderService.getById(salesOrderId) != null) {
			salesOrderService.updateStatus(salesOrderId, statusCode, note);
			return new ResponseEntity<>("Sales Order with ID " + salesOrderId + " updated", HttpStatus.OK);
		} else {
			return errorMessage("SalesOrder " + salesOrderId + " not found", HttpStatus.NOT_FOUND);
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

}
