package com.sap.refapps.espm.service;

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * This class is used to test the loading 
 * mock data from json files
 *
 */
@ActiveProfiles(profiles = "test")
@ExtendWith(SpringExtension.class)
@SpringBootTest
public class CustomerServiceTest {

	private static final String VALID_FILE_PATH = "/CustomerServiceTest.json";
	private static final String INVALID_FILE_PATH = "/temp/testData.json";

	@Autowired
	private CustomerService customerService;

	/**
	 * This method is used to test the
	 * loadCustomer(path) with valid path.
	 *  
	 * @throws IOException
	 */
	@Test
	public void testLoadCustomer() throws IOException {
		customerService.loadCustomer(VALID_FILE_PATH);
	}

	/**
	 * This method is used to test the
	 * loadCustomer(path) with invalid path.
	 *  
	 * @throws IOException
	 */
	@Test
	public void testLoadCustomerFromInvalidPath() throws IOException {
		Assertions.assertThrows(java.lang.Exception.class, () -> {
			customerService.loadCustomer(INVALID_FILE_PATH);
		  });
	}
}
