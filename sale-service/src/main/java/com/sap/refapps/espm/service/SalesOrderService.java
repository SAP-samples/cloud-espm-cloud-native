package com.sap.refapps.espm.service;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;

import com.sap.refapps.espm.model.SalesOrder;
import com.sap.refapps.espm.model.Tax;
import org.springframework.cloud.cloudfoundry.com.fasterxml.jackson.core.JsonProcessingException;

import javax.jms.JMSException;


/**
 * This interface defines all the methods
 * for the sales order service.
 *
 */
public interface SalesOrderService {

	/**
	 * Returns all sales order.
	 * 
	 * @return list of sales order
	 */
	Iterable<SalesOrder> getAll();

	/**
	 * Returns a sales order based on sales order id.
	 * 
	 * @param salesOrderId
	 * @return sales order
	 */
	SalesOrder getById(String salesOrderId);

	/**
	 * Returns a list of sales order based on 
	 * customer email.
	 * 
	 * @param customerEmail
	 * @return list of sales order
	 */
	Iterable<SalesOrder> getByEmail(String customerEmail);

	/**
	 * It creates a sales order.
	 * 
	 * @param salesOrder
	 * @param tax
	 * @return true if sales order get inserted successfully
	 */
	void insert(SalesOrder salesOrder, Tax tax) throws JsonProcessingException, UnsupportedEncodingException, JMSException;

	/**
	 * Returns a tax based on amount provided
	 * from an external service.
	 * 
	 * @param amount
	 * @return tax object
	 */
	Tax getTax(BigDecimal amount);

	/**
	 * Returns a tax if the tax service is not
	 * reachable or down. This is the fallback
	 * method for getTax().
	 * 
	 * @param amount
	 * @return tax object
	 */
	Tax taxServiceFallback(BigDecimal amount);

}

