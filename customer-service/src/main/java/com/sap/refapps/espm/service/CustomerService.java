package com.sap.refapps.espm.service;

import java.io.IOException;
import java.util.List;

import org.eclipse.persistence.exceptions.DatabaseException;
import org.postgresql.util.PSQLException;
import org.springframework.dao.DataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

import com.sap.db.jdbc.exceptions.SQLIntegrityConstraintViolationExceptionSapDB;
import com.sap.refapps.espm.model.Cart;
import com.sap.refapps.espm.model.Customer;

/**
 * This interface defines all the methods
 * for the customer and cart service.
 *
 */
public interface CustomerService {

	/**
	 * It saves item into the cart.
	 * 
	 * @param cart
	 * @return cart
	 */
	Cart saveCart(Cart cart);

	/**
	 * Returns list of items in the cart.
	 * 
	 * @param customerId
	 * @return list of items in the cart
	 */
	Iterable<Cart> getCartByCustomerId(String customerId);

	/**
	 * Returns customer based on email id.
	 * Retry pattern is implemented to ensure that
	 * if DB is not reachable due to any network issue,
	 * the service does not break. 
	 * 
	 * @param emailAddress
	 * @return customer
	 * @throws DataAccessException
	 */
	@Retryable(value = { DataAccessException.class }, backoff = @Backoff(delay = 2000), maxAttempts = 2)
	Customer getCustomerByEmailAddress(String emailAddress) throws DataAccessException;
	
	@Retryable(value = { DataAccessException.class }, backoff = @Backoff(delay = 2000), maxAttempts = 2)
	Customer getCustomerById(String customerId) throws DataAccessException;

	/**
	 * It deletes the item based on item id.
	 * 
	 * @param itemId
	 */
	void deleteCart(String itemId);

	/**
	 * It returns true if the item is present
	 * in the cart
	 * 
	 * @param itemId
	 * @return true if the item is present
	 */
	boolean cartItemExists(String itemId);

	/**
	 * It creates a customer.
	 * 
	 * @param customer
	 * @return customer
	 */
	Customer saveCustomer(Customer customer);

	/**
	 * It creates a list of customers.
	 * 
	 * @param listOfCustomers
	 */
	void saveCustomer(List<Customer> listOfCustomers);

	/**
	 * It loads customer mock data from the json file.
	 * 
	 * @param filePath
	 * @throws IOException
	 */
	void loadCustomer(String filePath) throws IOException;
}
