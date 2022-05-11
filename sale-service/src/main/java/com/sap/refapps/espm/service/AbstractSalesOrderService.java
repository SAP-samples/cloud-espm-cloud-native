package com.sap.refapps.espm.service;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.Optional;

import javax.jms.JMSException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.web.client.RestTemplate;

import com.sap.cloud.security.xsuaa.tokenflows.XsuaaTokenFlows;
import com.sap.refapps.espm.model.SalesOrder;
import com.sap.refapps.espm.model.SalesOrderRepository;
import com.sap.refapps.espm.model.Tax;
import com.sap.refapps.espm.util.ResilienceHandler;
import com.sap.refapps.espm.util.SalesOrderLifecycleStatusEnum;
import com.sap.refapps.espm.util.SalesOrderLifecycleStatusNameEnum;

/**
 * The objective of this class is to offer common attributes and behaviors
 * (variables and methods)
 * to its concrete subclasses with default values and implementations. It also
 * provide abstraction
 * to some of the behaviors which need specific implementation by the
 * subclasses.
 * 
 * @author C5258401
 *
 */
public abstract class AbstractSalesOrderService implements SalesOrderService {

	private static final Logger logger = LoggerFactory.getLogger(AbstractSalesOrderService.class);

	private final SalesOrderRepository salesOrderRepository;

	private Iterable<SalesOrder> salesOrder;

	protected final ResilienceHandler resilienceHandler;

	protected final RestTemplate restTemplate;

	/**
	 * @param salesOrderRepository
	 * @param restTemplate
	 * @param resilienceHandler
	 */
	public AbstractSalesOrderService(final SalesOrderRepository salesOrderRepository, final RestTemplate restTemplate,
			final ResilienceHandler resilienceHandler) {
		this.salesOrderRepository = salesOrderRepository;
		this.restTemplate = restTemplate;
		this.resilienceHandler = resilienceHandler;
	}

	@Override
	public void insert(final SalesOrder salesOrder)
			throws JsonProcessingException, UnsupportedEncodingException, JMSException {
		// TODO nothing
	}

	@Override
	public boolean insert(SalesOrder salesOrder, String profile) {
		return false;
	}

	@Override
	public Iterable<SalesOrder> getAll() {
		salesOrder = salesOrderRepository.getAllSalesOrders();
		return salesOrder;
	}

	@Override
	public Iterable<SalesOrder> getByEmail(String customerEmail) {
		salesOrder = salesOrderRepository.getAllSalesOrderForCustomer(customerEmail);
		return salesOrder;
	}

	@Override
	public SalesOrder getById(String salesOrderId) {
		return salesOrderRepository.findSalesOrderById(salesOrderId);
	}

	@Override
	public void updateStatus(String salesOrderId, String lifecyleStatus, String note) {
		Optional<SalesOrder> optionalSalesOrder = salesOrderRepository.findById(salesOrderId);

		if (optionalSalesOrder.isPresent()) {
			SalesOrder salesOrder = optionalSalesOrder.get();
			salesOrder.setNote(note);
			salesOrder.setLifecycleStatus(SalesOrderLifecycleStatusEnum.valueOf(lifecyleStatus).toString());
			salesOrder.setLifecycleStatusName(SalesOrderLifecycleStatusNameEnum.valueOf(lifecyleStatus).toString());
			salesOrderRepository.save(salesOrder);
		}

	}

	/**
	 * @param amount
	 * @return
	 */
	protected Tax getTax(BigDecimal amount) {
		logger.info("applying resilience patterns before calling the tax-service");
		return resilienceHandler.applyResiliencePatterns(amount);
	}

	/**
	 * @param amount
	 * @return
	 */
	public abstract Tax supplyTax(BigDecimal amount);

}
