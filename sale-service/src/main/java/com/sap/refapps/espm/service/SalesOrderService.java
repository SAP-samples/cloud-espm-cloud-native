package com.sap.refapps.espm.service;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;

import javax.jms.JMSException;

import com.fasterxml.jackson.core.JsonProcessingException;

import com.sap.refapps.espm.model.SalesOrder;
import com.sap.refapps.espm.model.Tax;

/**
 * This interface defines all the methods for the sales order service.
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
	 * Returns a list of sales order based on customer email.
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
	void insert(SalesOrder salesOrder)
			throws JsonProcessingException, UnsupportedEncodingException, JMSException;

	/**
	 * create a sales order for local environment
	 * 
	 * @param salesOrder
	 * @param tax
	 * @param status
	 * @return
	 */
	boolean insert(SalesOrder salesOrder, String profile);

	/**
	 * It updates a sales order status.
	 * 
	 * @param salesOrder
	 * @param tax
	 */
	void updateStatus(String salesOrderId, String lifecyleStatus, String note);

}