package com.sap.refapps.espm.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sap.refapps.espm.model.Product;
import com.sap.refapps.espm.model.ProductWithStock;
import com.sap.refapps.espm.model.Stock;
import com.sap.refapps.espm.service.ProductService;

/**
 * This class is a controller class of product service which is responsible for
 * handling all endpoints.
 *
 */
@RestController
@RequestMapping("/product.svc/api/v1")
public class ProductController {

	private final Logger logger = LoggerFactory.getLogger(ProductController.class);

	@Autowired
	private ProductService productService;

	/**
	 * To get all products
	 * 
	 * @return list of product
	 * @throws InterruptedException
	 */
	@GetMapping("/products")
	public ResponseEntity<Iterable<Product>> getAllProducts() throws InterruptedException {
		final Iterable<Product> products;
		try {
			// To slow it down and get effects of rate limiting
			Thread.sleep(1000);
			products = productService.getAllProducts();
			if (products != null)
				return new ResponseEntity<>(products, HttpStatus.OK);
			return errorMessage("Not found", HttpStatus.NOT_FOUND);
		} catch (DataAccessException e) {
			logger.error("Database is down");
			return errorMessage("Database service is temporarily down. Please try again later",
					HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	/**
	 * To get product by ProductID
	 * 
	 * @param productId
	 * @return a single product
	 */
	@GetMapping("/products/{id}")
	public ResponseEntity<Product> getProductById(@PathVariable(value = "id") final String productId) {
		final Product product = productService.getProductById(productId);
		if (product != null)
			return new ResponseEntity<>(product, HttpStatus.OK);
		return errorMessage(productId + " Not found", HttpStatus.NOT_FOUND);
	}

	/**
	 * To get Stock by ProductID
	 * 
	 * @param productId
	 * @return stock
	 */
	@GetMapping("/stocks/{id}")
	public ResponseEntity<Stock> getStockByProductId(@PathVariable(value = "id") final String productId) {
		final Stock stock = productService.getStockByProductId(productId);
		if (stock != null)
			return new ResponseEntity<>(stock, HttpStatus.OK);
		return errorMessage(productId + " Not found", HttpStatus.NOT_FOUND);
	}

	/**
	 * To get Stock for all products
	 * 
	 * @param productId
	 * @return stock
	 */
	@GetMapping("/stocks")
	public ResponseEntity<Iterable<ProductWithStock>> getStockForAllProducts() {
		final Iterable<ProductWithStock> stocks = productService.getStockForAllProducts();
		return new ResponseEntity<>(stocks, HttpStatus.OK);
	}

	/**
	 * To check and update the stock by productId
	 * 
	 * @param stock
	 * @param productId
	 * @return message confirmation of stock update
	 */
	@PutMapping("/stocks/{id}")
	public ResponseEntity<?> checkAndUpdateStock(@RequestBody Stock stock,
			@PathVariable(value = "id") final String productId) {
		stock.setProductId(productId);
		if (!productService.isProductExists(stock.getProductId())) {
			return errorMessage("Invalid product id : " + stock.getProductId(), HttpStatus.NOT_FOUND);
		}
		if (!productService.isProductExists(productId)) {
			return errorMessage("Invalid product id : " + productId, HttpStatus.NOT_FOUND);
		} else {

			int flag = productService.checkAndUpdateStock(stock);
			if (flag == 1) {
				return errorMessage("Could not update.Maximum stock reached for product id : " + productId,
						HttpStatus.BAD_REQUEST);
			}
			if (flag == 2) {
				return errorMessage("Could not update.Out of stock for product id : " + productId,
						HttpStatus.NO_CONTENT);
			}

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(org.springframework.http.MediaType.TEXT_PLAIN);

			return new ResponseEntity<>("Stock Updated for product id : " + productId, headers, HttpStatus.OK);
		}

	}

	/**
	 * @param message
	 * @param status
	 * @return ResponseEntity with HTTP status,headers and body
	 */
	public static ResponseEntity errorMessage(String message, HttpStatus status) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(org.springframework.http.MediaType.TEXT_PLAIN);

		return ResponseEntity.status(status).headers(headers).body(message);
	}

}
