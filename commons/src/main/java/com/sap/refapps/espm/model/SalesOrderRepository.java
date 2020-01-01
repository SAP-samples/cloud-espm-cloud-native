package com.sap.refapps.espm.model;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * This is the sales order repository interface
 * responsible for communicating with the database.
 *
 */
@Repository
public interface SalesOrderRepository extends CrudRepository<SalesOrder, String> {

	/**
	 * Returns list of sales order based on customer email.
	 * 
	 * @param customerEmail
	 * @return list of sales order
	 */
	@Query(value = "SELECT * FROM ESPM_SALES_ORDER WHERE CUSTOMER_EMAIL = ?1 ORDER BY CREATED_AT DESC", nativeQuery = true)
	Iterable<SalesOrder> getAllSalesOrderForCustomer(String customerEmail);
	
	/**
	 * Returns sales order based on sales order id.
	 * 
	 * @param salesOrderId
	 * @return sales order
	 */
	@Query(value = "SELECT * FROM ESPM_SALES_ORDER WHERE SALES_ORDER_ID = ?1", nativeQuery = true)
	SalesOrder findSalesOrderById(String salesOrderId);
	
	@Query(value = "SELECT * FROM ESPM_SALES_ORDER ORDER BY CREATED_AT DESC", nativeQuery = true)
	Iterable<SalesOrder> getAllSalesOrders();
	
}
