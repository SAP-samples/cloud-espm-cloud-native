package com.sap.refapps.espm.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.cloudfoundry.com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.cloud.cloudfoundry.com.fasterxml.jackson.databind.DeserializationFeature;
import org.springframework.cloud.cloudfoundry.com.fasterxml.jackson.databind.JsonNode;
import org.springframework.cloud.cloudfoundry.com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
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

	@Value("${tax.destinationName}")
	private String taxDestination;

	private Iterable<SalesOrder> salesOrder;

	private QueueDispatcherService queueDispatcherService;

	private String taxUri;

    final ObjectMapper mapper = new ObjectMapper();

    private HashMap<String,String> taxUrlCache = new HashMap<>(1);


    final HttpHeaders headers = new HttpHeaders();

    final static String DESTINATION_PATH = "/destination-configuration/v1/destinations/";
	


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
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
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
        taxUri = taxUrlCache.get("TAX_URI");
        if (taxUri == "" || taxUri == null){
            taxUri = getTaxUrlFromDestinationService();
            taxUrlCache.put("TAX_URI",taxUri);
        }


        //If destination service not configured
        if (taxUri == "") {
            taxUri = Arrays.stream(environment.getActiveProfiles())
                    .anyMatch(env -> (env.equalsIgnoreCase("cloud"))) ? this.environment.getProperty("TAX_SERVICE")
                    : taxServiceEndPoint;
            URI uri = URI.create(taxUri + amount);
            tax = this.restTemplate.getForObject(uri, Tax.class);
        }
        else
            try{
                URI uri = URI.create(taxUri + amount);
                tax = restTemplate.getForObject(uri, Tax.class);
            }catch(HttpClientErrorException e){
                logger.info("Retrying");
                taxUri = getTaxUrlFromDestinationService();
                taxUrlCache.put("TAX_URI",taxUri);
                URI uri = URI.create(taxUri + amount);
                tax = restTemplate.getForObject(uri, Tax.class);


            }
        logger.info("Tax service endpoint is {}", taxUri);
        logger.info("Tax service is called to calculate tax for amount : {}", amount);
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

    private String getTaxUrlFromDestinationService(){
        try {
            final DestinationService destination = getDestinationServiceDetails();
            final String accessToken = getOAuthToken(destination);
            headers.set("Authorization","Bearer "+accessToken);
            HttpEntity entity = new HttpEntity(headers);
            final String taxUrl = destination.uri + DESTINATION_PATH + taxDestination ;
            final ResponseEntity<String> response = restTemplate.exchange( taxUrl, HttpMethod.GET,entity,String.class  );
            final JsonNode root = mapper.readTree(response.getBody());
            final String texDestination = root.path("destinationConfiguration").path("URL").asText();
            return texDestination;
        }catch (IOException e){
            logger.error("No proper destination Service available: {}", e.getMessage());

        }
        return "";
    }


    private String getOAuthToken(final DestinationService destination) throws IOException {
        final String auth = destination.clientid+":"+destination.clientsecret;
        final byte[] basicToken = Base64.getEncoder().encode(auth.getBytes());
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization","Basic "+new String(basicToken));
        MultiValueMap<String, String> map= new LinkedMultiValueMap<String, String>();
        map.add("client_id", destination.clientid);
        map.add("grant_type", "client_credentials");
        final HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map, headers);
        ResponseEntity<String> response = restTemplate.postForEntity( destination.url+"/oauth/token", request , String.class );
        final String responseString = response.getBody();
        JsonNode root = mapper.readTree(responseString);
        final String accessToken = root.get("access_token").asText();
        return accessToken;

    }


    private DestinationService getDestinationServiceDetails() throws IOException {
        final String destinationService = System.getenv("VCAP_SERVICES");
        final JsonNode root = mapper.readTree(destinationService);
        final JsonNode destinations = root.get("destination").get(0).get("credentials");
        final DestinationService destination = mapper.treeToValue(destinations,DestinationService.class);
        return  destination;

    }

}

@JsonIgnoreProperties(ignoreUnknown = true)
class DestinationService{

    public String clientid;
    public String clientsecret;
    public String uri;
    public String url;

}
