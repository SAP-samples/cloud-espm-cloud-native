package com.sap.refapps.espm.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.cloudfoundry.com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.cloud.cloudfoundry.com.fasterxml.jackson.databind.ObjectMapper;
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

	/* (non-Javadoc)
	 * @see com.sap.refapps.espm.service.CustomerService#save(com.sap.refapps.espm.model.Cart)
	 */
	@Override
	public Cart saveCart(Cart cart) {
		return cartRepository.save(cart);
	}

	/* (non-Javadoc)
	 * @see com.sap.refapps.espm.service.CustomerService#getAll(java.lang.String)
	 */
	@Override
	public Iterable<Cart> getCartByCustomerId(String customerId) {
		return cartRepository.findCartsByCustomerId(customerId);
	}

	/* (non-Javadoc)
	 * @see com.sap.refapps.espm.service.CustomerService#getCustomer(java.lang.String)
	 */
	@Override
	public Customer getCustomerByEmailAddress(String emailAddress) throws DataAccessException {
		Customer customer = customerRepository.findCustomerByEmailAddress(emailAddress);
		return customer;
	}
	
	@Override
	public Customer getCustomerById(String customerId) throws DataAccessException {
		Customer customer = customerRepository.findCustomerById(customerId);
		return customer;
	}

	/* (non-Javadoc)
	 * @see com.sap.refapps.espm.service.CustomerService#delete(java.lang.String)
	 */
	@Override
	public void deleteCart(String itemId) {
		cartRepository.deleteById(itemId);
	}

	/* (non-Javadoc)
	 * @see com.sap.refapps.espm.service.CustomerService#exists(java.lang.String)
	 */
	@Override
	public boolean cartItemExists(String itemId) {
		return cartRepository.existsById(itemId);
	}

	/* (non-Javadoc)
	 * @see com.sap.refapps.espm.service.CustomerService#saveCustomer(com.sap.refapps.espm.model.Customer)
	 */
	@Override
	public Customer saveCustomer(Customer customer) {
		return customerRepository.save(customer);
	}

	/* (non-Javadoc)
	 * @see com.sap.refapps.espm.service.CustomerService#saveCustomer(java.util.List)
	 */
	@Override
	public void saveCustomer(List<Customer> listOfCustomers) {
		customerRepository.saveAll(listOfCustomers);
	}

	/* (non-Javadoc)
	 * @see com.sap.refapps.espm.service.CustomerService#loadCustomer(java.lang.String)
	 */
	@Override
	public void loadCustomer(String filePath) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		TypeReference<List<Customer>> typeReference = new TypeReference<List<Customer>>() {
		};
		InputStream inputStream = null;
		try {
			inputStream = TypeReference.class.getResourceAsStream(filePath);
			List<Customer> listOfCustomers = mapper.readValue(inputStream, typeReference);
			saveCustomer(listOfCustomers);
		} catch (IOException e) {
			logger.error("loading of customer data failed");
			throw e;
		} /*finally {
			try {
				inputStream.close();
			} catch (IOException  | NullPointerException e) {
				logger.info(e.getMessage());
			}
		}*/
	}

}
