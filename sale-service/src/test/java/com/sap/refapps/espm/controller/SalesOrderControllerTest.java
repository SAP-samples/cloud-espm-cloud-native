package com.sap.refapps.espm.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * This class is used to test the 
 * sales service controller.
 *
 */
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@WebAppConfiguration
@ComponentScan({ "com.sap.refapps.espm" })
@SpringBootTest
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = { "classpath:beforeTestRun.sql" })
public class SalesOrderControllerTest {

	private static final String PATH = "/sale.svc/api/v1/salesOrders";

	private final String SALES_ORDERS_OF_SPECIFIC_CUSTOMER = "[{\"salesOrderId\":\"1\",\"customerEmail\":\"customer@gmail.com\",\"productId\":\"1\",\"currencyCode\":\"DLR\",\"grossAmount\":100.000,\"netAmount\":90.000,\"taxAmount\":10.000,\"lifecycleStatus\":\"1\",\"lifecycleStatusName\":\"\",\"quantity\":1.000,\"quantityUnit\":\"KG\",\"deliveryDate\":\"2018-01-01\",\"createdAt\":\"2018-01-01\"}]";
	private String SALES_ORDER = "{\"salesOrderId\":\"1\",\"customerEmail\":\"customer@gmail.com\",\"productId\":\"1\",\"currencyCode\":\"DLR\",\"grossAmount\":100.000,\"netAmount\":90.000,\"taxAmount\":10.000,\"lifecycleStatus\":\"1\",\"lifecycleStatusName\":\"\",\"quantity\":1.000,\"quantityUnit\":\"KG\",\"deliveryDate\":\"2018-01-01\",\"createdAt\":\"2018-01-01\"}";
	private String ALL_SALES_ORDERS = "[{\"salesOrderId\":\"1\",\"customerEmail\":\"customer@gmail.com\",\"productId\":\"1\",\"currencyCode\":\"DLR\",\"grossAmount\":100.000,\"netAmount\":90.000,\"taxAmount\":10.000,\"lifecycleStatus\":\"1\",\"lifecycleStatusName\":\"\",\"quantity\":1.000,\"quantityUnit\":\"KG\",\"deliveryDate\":\"2018-01-01\",\"createdAt\":\"2018-01-01\"},{\"salesOrderId\":\"2\",\"customerEmail\":\"customer2@gmail.com\",\"productId\":\"1\",\"currencyCode\":\"DLR\",\"grossAmount\":100.000,\"netAmount\":90.000,\"taxAmount\":10.000,\"lifecycleStatus\":\"1\",\"lifecycleStatusName\":\"\",\"quantity\":1.000,\"quantityUnit\":\"KG\",\"deliveryDate\":\"2018-01-01\",\"createdAt\":\"2018-01-01\"}]";
	@Autowired
	WebApplicationContext context;

	private MockMvc mockMvc;

	@Before
	public void create() {
		MockitoAnnotations.initMocks(this);
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
	}

	/**
	 * This method is used to test the get
	 * all sales order by customer email id.
	 * 
	 * @throws Exception
	 */
	@Test
	public void getAllSalesOrderOfCustomer() throws Exception {
		mockMvc.perform(get(PATH + "/email/customer@gmail.com"))
				.andExpect(content().json(SALES_ORDERS_OF_SPECIFIC_CUSTOMER));
	}

	/**
	 * This method is used to test the get all
	 * sales order by invalid customer email id.
	 * 
	 * @throws Exception
	 */
	@Test
	public void getAllSalesOrderOfNonCustomer() throws Exception {
		mockMvc.perform(get(PATH + "/email/invalidCustomer@gmail.com"))
				.andExpect(status().isNotFound());

	}

	/**
	 * This method is used to test get 
	 * all sales orders.
	 * 
	 * @throws Exception
	 */
	@Test
	public void getAllSalesOrders() throws Exception {
		mockMvc.perform(get( PATH ))
				.andExpect(content().json(ALL_SALES_ORDERS));
	}

	/**
	 * This method is used to test to get sales order
	 * by valid sales order id.
	 * 
	 * @throws Exception
	 */
	@Test
	public void getValidSpecificSalesOrder() throws Exception {
		mockMvc.perform(get(PATH + "/1"))
				.andExpect(content().json(SALES_ORDER));
	}

	/**
	 * This method is used to test to get sales order
	 * by valid sales order.
	 * 
	 * @throws Exception
	 */
	@Test
	public void getInvalidSpecificSalesOrder() throws Exception {
		mockMvc.perform(get(PATH + "/3"))
				.andExpect(content().string("Sales order not found"));
	}

}
