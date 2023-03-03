package com.sap.refapps.espm.controller;

import static org.assertj.core.api.Assertions.assertThat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.MediaType.TEXT_PLAIN;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import com.sap.refapps.espm.config.EmbeddedQpidBrokerConfig;

/** 
 * This is SalesOrder Controller Test class
 * 
 */
@ActiveProfiles(profiles = { "local", "test" })
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@SpringBootTest
@ComponentScan({ "com.sap.refapps.espm" })
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = { "classpath:salesOrderTestData.sql" })
public class SalesOrderControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    EmbeddedQpidBrokerConfig broker;

    private MockMvc mockMvc;
    private MockHttpServletRequestBuilder requestBuilder;

    private final static String SALES_SERVICE_ENDPOINT = "/sale.svc/api/v1/salesOrders";
    private final static String VALID_EMAILID_URL = SALES_SERVICE_ENDPOINT + "/email" + "/testcustomer@gmail.com";
    private final static String INVALID_EMAILID_URL = SALES_SERVICE_ENDPOINT + "/email" + "/customer@gmail.com";
    private final static String VALID_SALESORDER_ID_URL = SALES_SERVICE_ENDPOINT + "/ac4e58d8-60f2-4af2-8f15-548e96729a3f";
    private final static String INVALID_SALESORDER_ID_URL = SALES_SERVICE_ENDPOINT + "/ac4e58d8-60f2-4af2-8f15-548";
    private final static String UPDATE_VALID_SALESORDER_ID_URL = VALID_SALESORDER_ID_URL + "/N";
    private final static String UPDATE_INVALID_SALESORDER_ID_URL = INVALID_SALESORDER_ID_URL + "/N";

    private final static String UPDATE_SALESORDER_JSON_VALID_QUANTITY = "{\"salesOrderId\":\"ac4e58d8-60f2-4af2-8f15-548e96729a3f\",\"quantity\":4}";

    private final static String POST_SALESORDER_JSON_PAYLOAD = "{\"customerEmail\": \"viola.gains@itelo.info\",\"productId\": \"HT-1000\","
            + "\"productName\" :\"Notebook Basic 15\" , \"currencyCode\": \"EUR\", \"grossAmount\":956,\"quantity\":4}";

    private static final String SALESORDERS_JSON = "[{\"salesOrderId\":\"ac4e58d8-60f2-4af2-8f15-548e96729a3f\",\"customerEmail\":\"testcustomer@gmail.com\","
            + "\"productId\":\"HT-1000\",\"currencyCode\":\"EUR\",\"grossAmount\":1500.0,\"netAmount\":1650.0,\"taxAmount\":150.0,\"lifecycleStatus\":\"N\","
            + "\"lifecycleStatusName\":\"New\",\"quantity\":2.0,\"quantityUnit\":\"EA\",\"deliveryDate\":\"2022-09-01\",\"createdAt\":\"2022-09-09\","
            + "\"note\":\"\",\"productName\":\"Notebook Basic 15\"}]";

    private static final String SALESORDER_JSON = "{\"salesOrderId\":\"ac4e58d8-60f2-4af2-8f15-548e96729a3f\",\"customerEmail\":\"testcustomer@gmail.com\","
            + "\"productId\":\"HT-1000\",\"currencyCode\":\"EUR\",\"grossAmount\":1500.0,\"netAmount\":1650.0,\"taxAmount\":150.0,\"lifecycleStatus\":\"N\","
            + "\"lifecycleStatusName\":\"New\",\"quantity\":2.0,\"quantityUnit\":\"EA\",\"deliveryDate\":\"2022-09-01\",\"createdAt\":\"2022-09-09\","
            + "\"note\":\"\",\"productName\":\"Notebook Basic 15\"}";

    @BeforeEach
    public void create() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

    }

    /**
     * It is used to test the GetSalesOrder 
     * by providing valid EmailId.
     * 
     * @throws Exception
     */
    @Test
    public void testGetSalesOrderByValidEmailId() throws Exception {
        requestBuilder = buildGetRequest(VALID_EMAILID_URL);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(SALESORDERS_JSON));
    }

    /**
     * It is used to test the GetSalesOrder 
     * by providing Invalid EmailId.
     * 
     * @throws Exception
     */
    @Test
    public void testGetSalesOrderByInvalidEmailId() throws Exception {
        requestBuilder = buildGetRequest(INVALID_EMAILID_URL);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isNotFound());
    }

    /**
     * It is used to test the getAllSalesOrder
     * 
     * @throws Exception
     */
    @Test
    public void testGetAllSalesOrder() throws Exception {
        requestBuilder = buildGetRequest(SALES_SERVICE_ENDPOINT);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(SALESORDERS_JSON));
    }

    /**
     * It is used to test the getSalesOrder 
     * by providing valid SalesOrder id.
     * 
     * @throws Exception
     */
    @Test
    public void testGetSalesOrderById() throws Exception {
        requestBuilder = buildGetRequest(VALID_SALESORDER_ID_URL);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(SALESORDER_JSON));
    }

    /**
     * It is used to test the getSalesOrder 
     * by providing invalid SalesOrder id.
     * 
     * @throws Exception
     */
    @Test
    public void testGetSalesOrderByInvalidId() throws Exception {
        requestBuilder = buildGetRequest(INVALID_SALESORDER_ID_URL);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isNotFound());
    }

    /**
     * It is used to test the updatesalesOrder 
     * by providing valid salesOrder id.
     * 
     * @throws Exception
     */
    @Test
    public void testUpdateSalesOrderByValidSalesOrderId() throws Exception {
        requestBuilder = buildPutRequest(UPDATE_VALID_SALESORDER_ID_URL);
        mockMvc.perform(requestBuilder
                .contentType(MediaType.APPLICATION_JSON)
                .content(UPDATE_SALESORDER_JSON_VALID_QUANTITY))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(TEXT_PLAIN))
                .andExpect(content().string("Sales Order with ID ac4e58d8-60f2-4af2-8f15-548e96729a3f updated"));
    }

    /**
     * It is used to test the updateSalesOrder 
     * by providing invalid SalesOrder Id.
     * 
     * @throws Exception
     */
    @Test
    public void testUpdateSalesOrderByInValidSalesOrderId() throws Exception {
        requestBuilder = buildPutRequest(UPDATE_INVALID_SALESORDER_ID_URL);
        mockMvc.perform(requestBuilder
                .contentType(MediaType.APPLICATION_JSON)
                .content(UPDATE_SALESORDER_JSON_VALID_QUANTITY))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(TEXT_PLAIN))
                .andExpect(content().string("SalesOrder not found"));
    }

    /**
     * It is used to test the createSalesOrder 
     * with qpid broker running.
     * 
     * @throws Exception
     */
    @Test
    public void testCreateSalesOrder() throws Exception {
        broker.startQpidBroker();
        requestBuilder = buildPostRequest(SALES_SERVICE_ENDPOINT);
        MvcResult result = mockMvc.perform(requestBuilder
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(POST_SALESORDER_JSON_PAYLOAD))
                                    .andExpect(status().isAccepted())
                                    .andExpect(content().contentTypeCompatibleWith(TEXT_PLAIN))
                                    .andReturn();

        assertEquals(202, result.getResponse().getStatus());
        broker.after();
    }

    /**
     * It is used to build mock GET request.
     * 
     * @param path
     * @return MockHttpServletRequestBuilder
     */
    private MockHttpServletRequestBuilder buildGetRequest(final String path) {
        return get(path);
    }

    /**
     * It is used to build mock PUT request.
     * 
     * @param path
     * @return MockHttpServletRequestBuilder
     */
    private MockHttpServletRequestBuilder buildPutRequest(final String path) {
        return put(path);
    }

    /**
     * It is used to build mock POST request.
     * 
     * @param path
     * @return MockHttpServletRequestBuilder
     */
    private MockHttpServletRequestBuilder buildPostRequest(final String path) {
        return post(path);
    }
}
