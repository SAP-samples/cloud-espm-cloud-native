package com.sap.refapps.espm.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.sap.refapps.espm.model.Customer;

/**
 * This is the customer repository interface 
 * which is responsible for communicating with database.
 *
 */
@Repository
public interface CustomerRepository extends CrudRepository<Customer, String> {

	/**
	 * Returns customer based on email id.
	 * 
	 * @param emailAddress
	 * @return customer
	 */
	@Query(value = "SELECT * FROM ESPM_CUSTOMER WHERE EMAIL_ADDRESS = ?1", nativeQuery = true)
	Customer findCustomerByEmailAddress(String emailAddress);

	@Query(value = "SELECT * FROM ESPM_CUSTOMER WHERE CUSTOMER_ID = ?1", nativeQuery = true)
	Customer findCustomerById(String customerId);
}
