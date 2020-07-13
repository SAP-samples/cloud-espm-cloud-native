package com.sap.refapps.espm.controller;

import java.net.URI;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.sap.refapps.espm.model.Cart;
import com.sap.refapps.espm.model.Customer;
import com.sap.refapps.espm.service.CustomerService;

/**
 * This class is a controller class of customer service which is responsible for
 * handling all endpoints.
 *
 */
@RestController
@RequestMapping(path = CustomerController.API)
public class CustomerController {

	private static final Logger logger = LoggerFactory.getLogger(CustomerController.class);
	protected static final String API = "/customer.svc/api/v1";
	protected static final String API_CUSTOMER = "/customers/";
	protected static final String API_CART = "/carts/";

	private final CustomerService customerService;

	@Autowired
	public CustomerController(final CustomerService customerservice) {
		this.customerService = customerservice;
	}
	
	/**
	 * To add a new customer
	 * 
	 * @param customer
	 * @param uriComponentsBuilder
	 * @return
	 */
	@PostMapping(CustomerController.API_CUSTOMER)
	public ResponseEntity<Customer> addCustomer(@RequestBody final Customer customer, UriComponentsBuilder uriComponentsBuilder) {

		customerService.saveCustomer(customer);
		URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
				.buildAndExpand(customer.getCustomerId()).toUri();
		return ResponseEntity.created(uri).body(customer);
	}

	/**
	 * To get customer by email id
	 * 
	 * @param emailAddress
	 * @return customer if email id is valid
	 * @throws InterruptedException 
	 */
	@GetMapping(CustomerController.API_CUSTOMER + "{emailId}")
	public ResponseEntity<?> getCustomerByEmailAddress(@PathVariable("emailId") final String emailAddress) throws InterruptedException {

		final Customer customer;
		try {
			customer = customerService.getCustomerByEmailAddress(emailAddress);
			//To slow it down and get effects of rate limiting
			  Thread.sleep(1000);
			if (customer != null)
				return new ResponseEntity<Customer>(customer, HttpStatus.OK);
			return new ResponseEntity<String>("Customer not found", HttpStatus.NOT_FOUND);
		} catch (DataAccessException e) {
			logger.error("Database is down");
			return errorMessage("Database service is temporarily down. Please try again later",
					HttpStatus.SERVICE_UNAVAILABLE);
		}

	}

	/**
	 * To add a new item to the cart for a customer
	 * 
	 * @param cart
	 * @return cart object
	 * @throws URISyntaxException
	 */

	@PostMapping(CustomerController.API_CUSTOMER + "{customerId}" + CustomerController.API_CART)
	public ResponseEntity<Cart> addCart(@PathVariable(value = "customerId") final String customerId,
			@RequestBody final Cart cart, UriComponentsBuilder uriComponentsBuilder) throws URISyntaxException {
		
		try  {
			final String UUID = java.util.UUID.randomUUID().toString();
			cart.setItemId(UUID);
			Customer customer = customerService.getCustomerById(customerId);
			if (customer==null)
				return errorMessage("Customer with id: " + customerId + " is not found.", HttpStatus.NOT_FOUND);
			cart.setCustomer(customer);
			customerService.saveCart(cart);
			logger.debug("Created:: " + cart);
			UriComponents uriComponents = uriComponentsBuilder
					.path(CustomerController.API_CUSTOMER + "{customerId}" + CustomerController.API_CART + "{id}")
					.buildAndExpand(customerId, UUID);
			return ResponseEntity.created(new URI(uriComponents.getPath())).body(cart);
			
		} catch (DataAccessException e) {
			logger.error("Database is down");
			return errorMessage("Database service is temporarily down. Please try again later",
					HttpStatus.SERVICE_UNAVAILABLE);
		}
		
	}

	/**
	 * To fetch all the carts of a customer
	 * 
	 * @param customerId
	 * @return list of items in cart
	 */
	@GetMapping(CustomerController.API_CUSTOMER + "{customerId}" + CustomerController.API_CART)
	public ResponseEntity<Iterable<Cart>> getCartByCustomerId(@PathVariable("customerId") final String customerId) {
		final Iterable<Cart> carts = customerService.getCartByCustomerId(customerId);
		if (carts == null) {
			return errorMessage("Customer with id: " + customerId + " has no carts created or customer id does not exist.", HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(carts, HttpStatus.OK);
	}

	/**
	 * To update cart of a customer
	 * 
	 * @param cart
	 * @param itemId
	 * @return confirmation message
	 */
	@PutMapping(CustomerController.API_CUSTOMER + "{customerId}" + CustomerController.API_CART + "{item_id}")
	public ResponseEntity<?> updateCart(@RequestBody final Cart cart,
			@PathVariable("customerId") final String customerId, @PathVariable("item_id") final String itemId) {
		
		try {
			
			Customer customer = customerService.getCustomerById(customerId);
			if (customer==null)
				return errorMessage("Customer with id: " + customerId + " is not found.", HttpStatus.NOT_FOUND);
			if (!customerService.cartItemExists(itemId))
				return errorMessage("Item with id : " + itemId + " is not found.", HttpStatus.NOT_FOUND);
			cart.setCustomer(customer);
			cart.setItemId(itemId);
			customerService.saveCart(cart);
			HttpHeaders httpHeaders = new HttpHeaders();
			httpHeaders.setContentType(org.springframework.http.MediaType.TEXT_PLAIN);
			return new ResponseEntity<>("Cart updated for item id : " + itemId, httpHeaders, HttpStatus.OK);
			
		} catch (DataAccessException e) {
			logger.error("Database is down");
			return errorMessage("Database service is temporarily down. Please try again later",
					HttpStatus.SERVICE_UNAVAILABLE);
		}
		
	}

	/**
	 * To delete a cart of a customer
	 * 
	 * @param itemId
	 * @return confirmation message
	 */
	@DeleteMapping(CustomerController.API_CUSTOMER + "{customerId}" + CustomerController.API_CART + "{item_Id}")
	public ResponseEntity<?> deleteCartById(@PathVariable("item_Id") final String itemId) {
		if (!customerService.cartItemExists(itemId))
			return errorMessage("Item with id : " + itemId + " is not found.", HttpStatus.NOT_FOUND);
		customerService.deleteCart(itemId);
		return new ResponseEntity<>("Cart is deleted with item id : " + itemId, HttpStatus.OK);
	}

	/**
	 * @param message
	 * @param status
	 * @return ResponseEntity with HTTP status,headers and body
	 */
	private ResponseEntity errorMessage(String message, HttpStatus status) {
		return ResponseEntity.status(status).body(message);
	}
	
}