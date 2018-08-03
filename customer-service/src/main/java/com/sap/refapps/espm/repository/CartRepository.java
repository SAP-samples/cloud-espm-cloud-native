package com.sap.refapps.espm.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.sap.refapps.espm.model.Cart;

/**
 * This is the cart repository interface 
 * which is responsible for communicating with database.
 *
 */
@Repository
public interface CartRepository extends CrudRepository<Cart, String> {

	/**
	 * Returns list of items in cart based on customer
	 * email id.
	 * 
	 * @param customerId
	 * @return list of items
	 */
	@Query(value = "SELECT * FROM ESPM_CART WHERE CUSTOMER_ID = ?1", nativeQuery = true)
	Iterable<Cart> findCartsByCustomerId(String customerId);

}
