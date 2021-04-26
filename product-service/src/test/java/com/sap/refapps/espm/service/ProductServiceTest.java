package com.sap.refapps.espm.service;

import java.io.IOException;
import java.io.IOException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;


/**
 * This class is used to test the loading
 * mock data from json files.
 *
 */
@ActiveProfiles(profiles = "test")
@RunWith(SpringRunner.class)
@SpringBootTest
public class ProductServiceTest {

	private static final String PRODUCT_TEST_DATA = "/productTest.json";
	private static final String STOCK_TEST_DATA = "/stockTest.json";
	private static final String INVALID_PATH = "/temp/testData.json";

	@Autowired
	private ProductService service;

	/**
	 * This method is used to test
	 * the loading of product data from 
	 * valid json path.
	 *  
	 * @throws IOException
	 */
	@Test
	public void testLoadProduct() throws IOException {
		service.loadProduct(PRODUCT_TEST_DATA);
	}

	/**
	 * This method is used to test
	 * the loading of product data from
	 * invalid path.
	 * 
	 * @throws IOException
	 */
	@Test(expected = java.lang.Exception.class)
	public void testLoadProductFromInvalidPath() throws IOException {
		service.loadProduct(INVALID_PATH); 
	}

	/**
	 * This method is used to test 
	 * the loading the stock data from
	 * valid path.
	 * 
	 *   @throws IOException
	 */
	@Test
	public void testLoadStock() throws IOException {
		service.loadStock(STOCK_TEST_DATA);
	} 

	/**
	 * This method is used to test 
	 * the loading the stock data from
	 * invalid path.
	 * 
	 * @throws IOException
	 */
	@Test(expected = java.lang.Exception.class)
	public void testLoadStockFromInvalidPath() throws IOException {
		service.loadStock(INVALID_PATH);
	} 
}
