package com.sap.refapps.espm.service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.sap.refapps.espm.model.SalesOrder;
import com.sap.refapps.espm.model.SalesOrderRepository;
import com.sap.refapps.espm.model.Tax;
import com.sap.refapps.espm.util.ResilienceHandler;
import com.sap.refapps.espm.util.SalesOrderLifecycleStatusEnum;
import com.sap.refapps.espm.util.SalesOrderLifecycleStatusNameEnum;

/**
 * Implementation class for the sales order service, deployable in local environment.
 *
 */
@Profile("local")
@Service
public class LocalSalesOrderService extends AbstractSalesOrderService {

	private static final Logger logger = LoggerFactory.getLogger(CloudSalesOrderService.class);

	@Value("${tax.service}")
	private String taxUri;
	
	@Autowired(required = false)
	private QueueDispatcherService queueDispatcherService;

	/**
	 * @param salesOrderRepository
	 * @param restTemplate
	 */
	@Autowired
	public LocalSalesOrderService(final SalesOrderRepository salesOrderRepository, final RestTemplate restTemplate,
			final ResilienceHandler resilienceHandler) {
		super(salesOrderRepository, restTemplate, resilienceHandler);
	}
	
	@Override
	public boolean insert(SalesOrder salesOrder, String profile) {
		
		final BigDecimal netAmount;
		Date now = new Date();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		// create MathContext object with 4 precision
		MathContext mc = new MathContext(15);
		final Tax tax = getTax(salesOrder.getGrossAmount());
		netAmount = tax.getTaxAmount().add(salesOrder.getGrossAmount(), mc);
		salesOrder.setLifecycleStatus(SalesOrderLifecycleStatusEnum.N.toString());
		salesOrder.setLifecycleStatusName(SalesOrderLifecycleStatusNameEnum.N.toString());
		salesOrder.setNetAmount(netAmount);
		salesOrder.setTaxAmount(tax.getTaxAmount());
		salesOrder.setQuantityUnit("EA");
		salesOrder.setCreatedAt(dateFormat.format(now));
		return queueDispatcherService.dispatch(salesOrder, salesOrder.getSalesOrderId());
	}

	@Override
	public Tax supplyTax(BigDecimal amount) {

		Tax tax;
		try {
			URI uri = URI.create(taxUri + amount);
			tax = restTemplate.getForObject(uri, Tax.class);

		} catch (ResourceAccessException e) {
    		logger.info("Retrying to connect to the TaxService...");
			URI uri = URI.create(taxUri + amount);
    		tax = restTemplate.getForObject(uri, Tax.class);
    		
		}
		
		logger.info("Tax service endpoint is {}", taxUri);
		logger.info("Tax service is called to calculate tax for amount : {}", amount);
		logger.info("Tax amount is : {}", tax.getTaxAmount());

		return tax;
	}

}
