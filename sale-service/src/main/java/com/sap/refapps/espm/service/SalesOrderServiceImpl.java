package com.sap.refapps.espm.service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixException;
import com.sap.refapps.espm.model.SalesOrder;
import com.sap.refapps.espm.model.SalesOrderRepository;
import com.sap.refapps.espm.model.Tax;

/**
 * This is the implementation class for the sales order service
 *
 */
@Service
public class SalesOrderServiceImpl implements SalesOrderService {

	private final SalesOrderRepository salesOrderRepository;

	private final RestTemplate restTemplate;

	private static final Logger logger = LoggerFactory.getLogger(SalesOrderServiceImpl.class);

	@Value("${tax.service}")
	private String taxServiceEndPoint;

	private Iterable<SalesOrder> salesOrder;

	private QueueDispatcherService queueDispatcherService;

	private String taxUri;
	


	@Autowired
	private Environment environment;
	
	
	/**
	 * @param salesOrderRepository
	 * @param queueDispatcherService
	 * @param rest
	 */
	@Autowired
	public SalesOrderServiceImpl(final SalesOrderRepository salesOrderRepository,
			final QueueDispatcherService queueDispatcherService, final RestTemplate rest) {
		this.salesOrderRepository = salesOrderRepository;
		this.queueDispatcherService = queueDispatcherService;
		this.restTemplate = rest;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sap.refapps.espm.service.SalesOrderService#insert(com.sap.refapps.espm.
	 * model.SalesOrder, com.sap.refapps.espm.model.Tax)
	 */
	@Override
	public boolean insert(final SalesOrder salesOrder, final Tax tax) {

		final BigDecimal netAmount;
		Date now = new Date();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
				
		// create MathContext object with 4 precision
		MathContext mc = new MathContext(15);
		netAmount = tax.getTaxAmount().add(salesOrder.getGrossAmount(), mc);
		salesOrder.setLifecycleStatus("N");
		salesOrder.setLifecycleStatusName("New");
		salesOrder.setNetAmount(netAmount);
		salesOrder.setTaxAmount(tax.getTaxAmount());
		salesOrder.setQuantityUnit("EA");
		salesOrder.setCreatedAt(dateFormat.format(now));
		return queueDispatcherService.dispatch(salesOrder, salesOrder.getSalesOrderId());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sap.refapps.espm.service.SalesOrderService#getAll()
	 */
	@Override
	public Iterable<SalesOrder> getAll() {
		salesOrder = salesOrderRepository.findAll();
		return salesOrder;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sap.refapps.espm.service.SalesOrderService#getByEmail(java.lang.String)
	 */
	@Override
	public Iterable<SalesOrder> getByEmail(String customerEmail) {
		salesOrder = salesOrderRepository.getAllSalesOrderForCustomer(customerEmail);
		return salesOrder;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sap.refapps.espm.service.SalesOrderService#getById(java.lang.String)
	 */
	@Override
	public SalesOrder getById(String salesOrderId) {
		return salesOrderRepository.findSalesOrderById(salesOrderId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sap.refapps.espm.service.SalesOrderService#getTax(java.math.BigDecimal)
	 */
	@HystrixCommand(fallbackMethod = "taxServiceFallback", raiseHystrixExceptions = HystrixException.RUNTIME_EXCEPTION, commandKey = "taxCommandKey", groupKey = "taxThreadPoolKey")
	public Tax getTax(BigDecimal amount) {
		Tax tax;
		taxUri = Arrays.stream(environment.getActiveProfiles())
				.anyMatch(env -> (env.equalsIgnoreCase("cloud"))) ? this.environment.getProperty("TAX_SERVICE")
				: taxServiceEndPoint;
		logger.info("Tax service endpoint is {}", taxUri);
		logger.info("Tax service is called to calculate tax for amount : {}", amount);
		URI uri = URI.create(taxUri + amount);
		tax = this.restTemplate.getForObject(uri, Tax.class);
		logger.info("Tax amount is : {}", tax.getTaxAmount());
		return tax;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sap.refapps.espm.service.SalesOrderService#taxServiceFallback(java.math.
	 * BigDecimal)
	 */
	public Tax taxServiceFallback(BigDecimal amount) {
		logger.info("Tax service is down. So a default tax will be set to the amount : {}", amount);
		final Tax tax = new Tax();
		tax.setTaxPercentage(00.00);
		tax.setTaxAmount(new BigDecimal(00.00));

		return tax;
	}

}
