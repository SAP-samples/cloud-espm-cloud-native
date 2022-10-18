package com.sap.refapps.espm.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.sap.refapps.espm.model.Cart;
import com.sap.refapps.espm.model.Customer;
import com.sap.refapps.espm.repository.CartRepository;
import com.sap.refapps.espm.repository.CustomerRepository;

/**
 * This is the customer service implementation class.
 *
 */
@Service
public class CustomerServiceImpl implements CustomerService {

	private static final Logger logger = LoggerFactory.getLogger(CustomerServiceImpl.class);

	@Autowired
	private CustomerRepository customerRepository;

	@Autowired
	private CartRepository cartRepository;

	@Override
	public Cart saveCart(Cart cart) {
		return cartRepository.save(cart);
	}

	@Override
	public Iterable<Cart> getCartByCustomerId(String customerId) {
		return cartRepository.findCartsByCustomerId(customerId);
	}

	@Override
	public Customer getCustomerByEmailAddress(String emailAddress) {
		Customer customer;
		try {
			customer = customerRepository.findCustomerByEmailAddress(emailAddress);
		} catch (DataAccessException p) {
			logger.info("Retrying to connect to the database...");
			throw new DataAccessException("") {
			};
		}

		return customer;
	}

	@Override
	public Customer getCustomerById(String customerId) {
		Customer customer;
		try {
			customer = customerRepository.findCustomerById(customerId);
		} catch (DataAccessException p) {
			logger.info("Retrying to connect to the database...");
			throw new DataAccessException("") {
			};
		}
		return customer;
	}

	@Override
	public void deleteCart(String itemId) {
		cartRepository.deleteById(itemId);
	}

	@Override
	public boolean cartItemExists(String itemId) {
		return cartRepository.existsById(itemId);
	}

	@Override
	public Customer saveCustomer(Customer customer) {
		return customerRepository.save(customer);
	}

	@Override
	public void saveCustomer(List<Customer> listOfCustomers) {
		customerRepository.saveAll(listOfCustomers);
	}

	@Override
	public void loadCustomer(String filePath) throws IOException {
		var mapper = new ObjectMapper();
		var typeReference = new TypeReference<List<Customer>>() {
		};
		InputStream inputStream = null;
		try {
			inputStream = TypeReference.class.getResourceAsStream(filePath);
			var listOfCustomers = mapper.readValue(inputStream, typeReference);
			saveCustomer(listOfCustomers);
		} catch (IOException e) {
			logger.error("loading of customer data failed");
			throw e;
		}
	}

}
