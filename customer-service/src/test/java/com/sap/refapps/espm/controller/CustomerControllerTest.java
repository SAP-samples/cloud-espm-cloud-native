package com.sap.refapps.espm.controller;

import static org.hamcrest.Matchers.is;
import static org.springframework.http.MediaType.TEXT_PLAIN;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.refapps.espm.model.Cart;
import com.sap.refapps.espm.model.Customer;

/**
 * This is the Customer Controller Test class
 *
 */
@ActiveProfiles(profiles = "test")
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ComponentScan({ "com.sap.refapps.espm" })
@SpringBootTest
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = { "classpath:CustomerControllerTestData.sql" })
public class CustomerControllerTest {

    @Autowired
    private WebApplicationContext context;
    private MockMvc mockMvc;
    private MockHttpServletRequestBuilder requestBuilder;
    private String objectToJson;

    private final String EMAIL_ADDRESS = "viola.gains@itelo1.info";
    private final String LOCATION = "location";
    private final String CUSTOMER_ID = "1000000001";
    private final String INVALID_CUSTOMER_ID = "1990000001";
    private final String CUSTOMER_URL = CustomerController.API + CustomerController.API_CUSTOMER;
    private final String CUSTOMER_URL_VALID = CustomerController.API + CustomerController.API_CUSTOMER + EMAIL_ADDRESS;
    private final String CUSTOMER_URL_INVALID = CustomerController.API + CustomerController.API_CUSTOMER
            + "invalid@test.com";
    private final String CART_URL = CustomerController.API + CustomerController.API_CUSTOMER + CUSTOMER_ID
            + CustomerController.API_CART;
    private final String INVALID_CUSTOMER_CART_URL = CustomerController.API + CustomerController.API_CUSTOMER
            + INVALID_CUSTOMER_ID + CustomerController.API_CART;

    private static final String POST_CUSTOMER_JSON_PAYLOAD = "{\"emailAddress\": \"new_customer@test.com\", \"phoneNumber\": \"0123456789\", \"firstName\": \"new\", "
            + "\"lastName\": \"customer\", \"dateOfBirth\": \"19900911\", \"city\": \"Bang, KR\", \"postalCode\": \"112233\", \"street\": \"100ft Road\", "
            + "\"houseNumber\": \"123\", \"country\": \"IN\"}";

    private static final String CUSTOMER_JSON = "{\"customerId\":\"1000000001\",\"emailAddress\":\"viola.gains@itelo1.info\","
            + "\"phoneNumber\":\"1029384756\",\"firstName\":\"Viola\",\"lastName\":\"Gains\",\"dateOfBirth\":\"19801231\","
            + "\"city\":\"Antioch, Illinois\",\"postalCode\":\"60002\",\"street\":\"Spring Garden Street\",\"houseNumber\":\"143\",\"country\":\"US\"}";

    @BeforeEach
    public void create() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    /**
     * It is used to test the addCustomer()
     * 
     * @throws Exception
     */
    @Test
    public void testAddCustomer() throws Exception {
        requestBuilder = buildPostRequest(CUSTOMER_URL);
        mockMvc.perform(requestBuilder
                .contentType(MediaType.APPLICATION_JSON)
                .content(POST_CUSTOMER_JSON_PAYLOAD))
                .andExpect(status().isCreated());
    }

    /**
     * It is used to test the getCustomerByEmailAddress() by providing valid email
     * id.
     * 
     * @throws Exception
     */
    @Test
    public void testGetCustomerByValidEmailId() throws Exception {
        requestBuilder = buildGetRequest(CUSTOMER_URL_VALID);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(CUSTOMER_JSON));
    }

    /**
     * It is used to test the getCustomerByEmailAddress() by providing invalid email
     * id.
     * 
     * @throws Exception
     */
    @Test
    public void testGetCustomerByInvalidEmailId() throws Exception {
        requestBuilder = buildGetRequest(CUSTOMER_URL_INVALID);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isNotFound());
    }

    /**
     * It is used to test the addCart() with valid content.
     * 
     * @throws Exception
     */
    @Test
    public void testCreateCart() throws Exception {
        requestBuilder = buildPostRequest(CART_URL);
        mockMvc.perform(requestBuilder)
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    /**
     * It is used to test the addCart() with valid content.
     * 
     * @throws Exception
     */
    @Test
    public void testCreateCartWithInvalidCustomer() throws Exception {
        requestBuilder = buildPostRequest(INVALID_CUSTOMER_CART_URL);
        mockMvc.perform(requestBuilder)
                        .andExpect(status().isNotFound());
    }

    /**
     * It is used to test the addCart() with no content.
     * 
     * @throws Exception
     */
    @Test
    public void testCreateCartWithNoContent() throws Exception {
        mockMvc.perform(post(CART_URL, CUSTOMER_ID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    /**
     * It is used to test the getCartByCustomerId()
     * 
     * @throws Exception
     */
    @Test
    public void testGetAllCarts() throws Exception {
        requestBuilder = buildPostRequest(CART_URL);
        mockMvc.perform(requestBuilder)
                    .andExpect(status().isCreated()).andReturn().getResponse();

        requestBuilder = buildGetRequest(CART_URL);
        mockMvc.perform(requestBuilder).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[0]customer.customerId", is(CUSTOMER_ID)));
    }

    /**
     * It is used to test the updateCart() with valid Item Id.
     * 
     * @throws Exception
     */
    @Test
    public void testUpdateCartWithValidItemId() throws Exception {
        requestBuilder = buildPostRequest(CART_URL);

        MockHttpServletResponse response = mockMvc.perform(requestBuilder)
                                                    .andExpect(status().isCreated()).andReturn()
                                                    .getResponse();

        Cart cart = convertJsonContent(response, Cart.class);
        cart.setCheckOutStatus(true);

        String itemId = getIdFromLocation(response.getHeader(LOCATION));
        requestBuilder = buildPutRequest(CART_URL + itemId, cart);
        mockMvc.perform(requestBuilder.contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(TEXT_PLAIN))
                    .andExpect(content().string("Cart updated for item id : " + itemId));
    }

    /**
     * It is used to test the updateCart() with invalid Item Id.
     * 
     * @throws Exception
     */
    @Test
    public void testUpdateCartWithInvalidItemId() throws Exception {
        Cart cart = new Cart();
        cart.setCheckOutStatus(true);
        cart.setQuantityUnit(new BigDecimal("7.000"));
        requestBuilder = buildPutRequest(CART_URL + "000000000C5", cart);
        cart.setName("Notebook Basic 15");

        objectToJson = convertObjectToJson(cart);
        mockMvc.perform(requestBuilder.contentType(MediaType.APPLICATION_JSON).content(objectToJson))
                .andExpect(status().isNotFound());
    }

    /**
     * It is used to test the deleteCartById() with valid Item Id.
     * 
     * @throws Exception
     */
    @Test
    public void testDeleteCartByValidItemId() throws Exception {
        String itemId = performPostAndGetId();
        requestBuilder = buildDeleteRequest(CART_URL + itemId);
        mockMvc.perform(requestBuilder).andExpect(status().isOk())
                .andExpect(content().string("Cart is deleted with item id : " + itemId));
    }

    /**
     * It is used to test the deleteCartById() with invalid Item Id.
     * 
     * @throws Exception
     */
    @Test
    public void testDeleteCartByInvalidItemId() throws Exception {
        final String ID = "000000000C5";
        requestBuilder = buildDeleteRequest(CART_URL + ID);
        mockMvc.perform(requestBuilder).andExpect(status().isNotFound());
    }

    /**
     * This method is used to build Mock GET request.
     * 
     * @param path
     * @return MockHttpServletRequestBuilder
     */
    private MockHttpServletRequestBuilder buildGetRequest(final String path) {
        return get(path);
    }

    /**
     * This method is used to build mock POST request.
     * 
     * @param path
     * @return MockHttpServletRequestBuilder
     * @throws JsonProcessingException
     */
    private MockHttpServletRequestBuilder buildPostRequest(final String path) throws JsonProcessingException {

        Customer customer = new Customer();
        customer.setCustomerId("1000000001");
        customer.setEmailAddress("customer.service@test.com");
        customer.setPhoneNumber("9888058880");
        customer.setFirstName("Anil");
        customer.setLastName("Kumar");
        customer.setDateOfBirth("01-01-90");
        customer.setCity("Bangalore");
        customer.setPostalCode("560034");
        customer.setStreet("JNC");
        customer.setHouseNumber("34");
        customer.setCountry("IND");

        Cart cart = new Cart();
        cart.setProductId("P2");
        cart.setQuantityUnit(new BigDecimal("8.000"));
        cart.setName("Notebook Basic 17");
        cart.setCheckOutStatus(false);
        cart.setCustomer(customer);

        // post the Cart object as a JSON entity in the request body
        objectToJson = convertObjectToJson(cart);
        return post(path).content(objectToJson).contentType(MediaType.APPLICATION_JSON);
    }

    /**
     * This method is used to build mock PUT request.
     * 
     * @param path
     * @param cart
     * @return MockHttpServletRequestBuilder
     * @throws JsonProcessingException
     */
    private MockHttpServletRequestBuilder buildPutRequest(String path, Cart cart) throws JsonProcessingException {
        objectToJson = convertObjectToJson(cart);
        return put(path).content(objectToJson).contentType(MediaType.APPLICATION_JSON);
    }

    /**
     * This method is used to build mock DELETE request.
     * 
     * @param path
     * @return MockHttpServletRequestBuilder
     */
    private MockHttpServletRequestBuilder buildDeleteRequest(final String path) {
        return delete(path);
    }

    /**
     * This method is used to perform a post request to get the response header
     * 'LOCATION' which is being passed to getIdFromLocation() to parse the ID from
     * it and return it back.
     * 
     * @return (string) id
     * @throws Exception
     */
    public String performPostAndGetId() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(buildPostRequest(CART_URL)).andExpect(status().isCreated())
                .andReturn().getResponse();

        return getIdFromLocation(response.getHeader(LOCATION));
    }

    /**
     * This method is useed to parse the id from the URL provided.
     * 
     * @param location
     * @return (string) id
     */
    private String getIdFromLocation(String location) {
        return location.substring(location.lastIndexOf('/') + 1);
    }

    /**
     * To convert an object to a JSON string
     * 
     * @param object
     * @return String
     * @throws JsonProcessingException
     */
    private String convertObjectToJson(Object object) throws JsonProcessingException {
        String jsonString = new ObjectMapper().writeValueAsString(object);
        return jsonString;
    }

    /**
     * To convert JSOn string to object.
     * 
     * @param response
     * @param clazz
     * @return object
     * @throws IOException
     */
    private <T> T convertJsonContent(MockHttpServletResponse response, Class<T> clazz) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        String contentString = response.getContentAsString();
        return objectMapper.readValue(contentString, clazz);
    }

}
