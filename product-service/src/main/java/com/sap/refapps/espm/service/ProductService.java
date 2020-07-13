package com.sap.refapps.espm.service;

import java.io.IOException;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

import com.sap.refapps.espm.model.Product;
import com.sap.refapps.espm.model.ProductWithStock;
import com.sap.refapps.espm.model.Stock;

/**
 * This interface defines all the methods
 * for the product service.
 *
 */
public interface ProductService {

	/**
	 * Returns the list of all product.
	 * 
	 * @return list of all product
	 * @throws DataAccessException
	 */
	@Retryable(value = { DataAccessException.class }, backoff = @Backoff(delay = 5000), maxAttempts = 2)
	Iterable<Product> getAllProducts() throws DataAccessException;

	/**
	 * Returns the product object based on product id.
	 * 
	 * @param productId
	 * @return the product object
	 */
	Product getProductById(String productId);

	/**
	 * Returns the stock object based on the product id.
	 * 
	 * @param productId
	 * @return the stock object
	 */
	Stock getStockByProductId(String productId);
		
	Iterable<ProductWithStock> getStockForAllProducts();

	/**
	 * Returns '0' if the stock is updated successfully, 
	 * '1' if the stock unit greater than 1000,
	 * '2' if the stock unit less than 0
	 * 
	 * @param stock
	 * @return value (0 or 1 or 2)
	 */
	int checkAndUpdateStock(Stock stock);

	/**
	 * Returns true if the given productId exists
	 * 
	 * @param productId
	 * @return true if the given productId exists
	 */
	boolean isProductExists(String productId);

	/**
	 * Returns the product object which is being persisted 
	 * into the database
	 * 
	 * @param product
	 * @return the product object
	 */
	Product saveProduct(Product product);

	/**
	 * Returns the stock object which is being persisted
	 * into the database.
	 * 
	 * @param stock
	 * @return the stock object
	 */
	Stock saveStock(Stock stock);

	/**
	 * Inserts list of mock product data into the database 
	 * during application initialization.
	 * 
	 * @param products
	 */
	void saveProduct(List<Product> products);

	/**
	 * Inserts list of mock stock data into the database 
	 * during application initialization.
	 * 
	 * @param stocks
	 */
	void saveStock(List<Stock> stocks);

	/**
	 * Parses product mock data from the 
	 * product.json file
	 * 
	 * @param location
	 * @throws IOException 
	 */
	void loadProduct(String location) throws IOException;
	
	/**
	 * Parses stock mock data from the 
	 * stock.json file
	 * 
	 * @param location
	 * @throws IOException 
	 */
	void loadStock(String location) throws IOException;
}
