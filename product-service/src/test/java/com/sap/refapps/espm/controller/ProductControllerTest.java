package com.sap.refapps.espm.controller;

import static org.springframework.http.MediaType.TEXT_PLAIN;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * This is the Customer Controller Test class
 *
 */
@ActiveProfiles(profiles = "test")
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ComponentScan({ "com.sap.refapps.espm" })
@SpringBootTest
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = { "classpath:productTestData.sql",
        "classpath:stockTestData.sql" })
public class ProductControllerTest {

    protected static final String PRODUCT_API = "/product.svc/api/v1/products";
    protected static final String STOCK_API = "/product.svc/api/v1/stocks";

    @Autowired
    WebApplicationContext context;

    private MockMvc mockMvc;
    private MockHttpServletRequestBuilder requestBuilder;

    private final static String PRODUCT_JSON = "{\"productId\":\"2\",\"name\":\"DVD\",\"shortDescription\":\"DVD\",\"category\":\"DVD\",\"weight\":1.0,\"weightUnit\":\"KG\",\"price\":2.0,\"currencyCode\":\"EUR\",\"dimensionWidth\":2.0,\"dimensionDepth\":2.0,\"dimensionHeight\":2.0,\"dimensionUnit\":\"M\",\"pictureUrl\":\"\"}";
    private final static String PRODUCTS_JSON = "[{\"productId\":\"1\",\"name\":\"TV\",\"shortDescription\":\"TV\",\"category\":\"TV\",\"weight\":1.0,\"weightUnit\":\"KG\",\"price\":2.0,\"currencyCode\":\"EUR\",\"dimensionWidth\":2.0,\"dimensionDepth\":2.0,\"dimensionHeight\":2.0,\"dimensionUnit\":\"M\",\"pictureUrl\":\"\"},{\"productId\":\"2\",\"name\":\"DVD\",\"shortDescription\":\"DVD\",\"category\":\"DVD\",\"weight\":1.0,\"weightUnit\":\"KG\",\"price\":2.0,\"currencyCode\":\"EUR\",\"dimensionWidth\":2.0,\"dimensionDepth\":2.0,\"dimensionHeight\":2.0,\"dimensionUnit\":\"M\",\"pictureUrl\":\"\"}]";

    private final static String STOCK_JSON = "{\"productId\":\"2\",\"quantity\":100.0}";
    private final static String UPDATE_STOCK_JSON_VALID_QUANTITY = "{\"productId\":\"2\",\"quantity\":80.0}";
    private final static String UPDATE_STOCK_JSON_MAX_QUANTITY = "{\"productId\":\"2\",\"quantity\":1000.0}";
    private final static String UPDATE_STOCK_JSON_MIN_QUANTITY = "{\"productId\":\"1\",\"quantity\":-11.0}";

    @BeforeEach
    public void create() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    /**
     * It is used to test the getProductById()
     * by providing valid product id.
     * 
     * @throws Exception
     */
    @Test
    public void getProductByValidId() throws Exception {
        requestBuilder = buildGetRequest(PRODUCT_API + "/2");
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(PRODUCT_JSON));
    }

    /**
     * It is used to test GetAllProducts()
     * method.
     * 
     * @throws Exception
     */
    @Test
    public void getAllProducts() throws Exception {
        requestBuilder = buildGetRequest(PRODUCT_API);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(PRODUCTS_JSON));
    }

    /**
     * It is used to test the getProductById()
     * by providing invalid product id.
     * 
     * @throws Exception
     */
    @Test
    public void getProductByInvalidId() throws Exception {
        requestBuilder = buildGetRequest(PRODUCT_API + "/3");
        mockMvc.perform(requestBuilder)
                .andExpect(status().isNotFound());
    }

    /**
     * It is used to test the get stock by valid product
     * id.
     * 
     * @throws Exception
     */
    @Test
    public void getStockByValidProductId() throws Exception {
        requestBuilder = buildGetRequest(STOCK_API + "/2");
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(STOCK_JSON));
    }

    /**
     * It is used to test the get stock
     * by invalid product id.
     * 
     * @throws Exception
     */
    @Test
    public void getStockByInValidProductId() throws Exception {
        requestBuilder = buildGetRequest(STOCK_API + "/3");
        mockMvc.perform(requestBuilder)
                .andExpect(status().isNotFound());
    }

    /**
     * It is used to test the update stock
     * by invalid product id.
     * 
     * @throws Exception
     */
    @Test
    public void updateStockByInValidProductId() throws Exception {
        requestBuilder = buildPutRequest(STOCK_API + "/3");
        mockMvc.perform(requestBuilder
                .contentType(MediaType.APPLICATION_JSON)
                .content(UPDATE_STOCK_JSON_VALID_QUANTITY))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(TEXT_PLAIN))
                .andExpect(content().string("Invalid product id : 3"));
    }

    /**
     * It is used to test the update stock
     * by valid product id and valid quantity.
     * 
     * @throws Exception
     */
    @Test
    public void updateStockByValidProductIdAndValidQuantity() throws Exception {
        requestBuilder = buildPutRequest(STOCK_API + "/2");
        mockMvc.perform(requestBuilder
                .contentType(MediaType.APPLICATION_JSON)
                .content(UPDATE_STOCK_JSON_VALID_QUANTITY))
                .andExpect(status().isOk())
                .andExpect(content().contentType(TEXT_PLAIN))
                .andExpect(content().string("Stock Updated for product id : 2"));
    }

    /**
     * It is used to test the update stock by valid product id
     * and invalid maximum quantity.
     * 
     * @throws Exception
     */
    @Test
    public void updateStockByValidProductIdAndMaxQuantity() throws Exception {
        requestBuilder = buildPutRequest(STOCK_API + "/2");
        mockMvc.perform(requestBuilder
                .contentType(MediaType.APPLICATION_JSON)
                .content(UPDATE_STOCK_JSON_MAX_QUANTITY))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(TEXT_PLAIN))
                .andExpect(content().string("Could not update.Maximum stock reached for product id : 2"));
    }

    /**
     * It is used to test the update stock by
     * valid product id and invalid minimum quantity.
     * 
     * @throws Exception
     */
    @Test
    public void updateStockByValidProductIdAndMinQuantity() throws Exception {
        requestBuilder = buildPutRequest(STOCK_API + "/1");
        mockMvc.perform(requestBuilder
                .contentType(MediaType.APPLICATION_JSON)
                .content(UPDATE_STOCK_JSON_MIN_QUANTITY))
                .andExpect(status().isNoContent())
                .andExpect(content().contentType(TEXT_PLAIN))
                .andExpect(content().string("Could not update.Out of stock for product id : 1"));
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

}
