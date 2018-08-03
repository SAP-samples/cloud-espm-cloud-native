package com.sap.refapps.espm.repository;

import com.sap.refapps.espm.model.Product;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/**
 * This is the product repository interface 
 * which is responsible for communicating with database.
 *
 */
public interface ProductRepository extends CrudRepository<Product, String>{

	/**
	 * Returns product based on product id.
	 * 
	 * @param productId
	 * @return product
	 */
	@Query(value = "SELECT * FROM ESPM_PRODUCT WHERE PRODUCT_ID = ?1", nativeQuery = true)
	Product findProductById(@Param("PRODUCT_ID") String productId);
}
