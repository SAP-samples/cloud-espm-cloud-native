package com.sap.refapps.espm.service;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.sap.refapps.espm.model.Product;
import com.sap.refapps.espm.model.ProductWithStock;
import com.sap.refapps.espm.model.Stock;
import com.sap.refapps.espm.repository.ProductRepository;
import com.sap.refapps.espm.repository.StockRepository;

/**
 * This is the implementation class for the product service
 *
 */
@Service
public class ProductServiceImpl implements ProductService {

	private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);
	@Autowired
	private ProductRepository productRepository;
	@Autowired
	private StockRepository stockRepository;

	@Override
	public Iterable<Product> getAllProducts() throws DataAccessException {

		Iterable<Product> products;
		try {
			products = productRepository.findAll();
		} catch (DataAccessException d) {
			logger.info("Retrying to connect to the database...");
			throw new DataAccessException("") {
			};
		}
		return products;
	}

	@Override
	public Product getProductById(String productId) {
		return productRepository.findProductById(productId);
	}

	@Override
	public Stock getStockByProductId(String productId) {
		return stockRepository.findStockByProductId(productId);
	}

	@Override
	public Iterable<ProductWithStock> getStockForAllProducts() {
		Iterable<ProductWithStock> stocks;
		stocks = stockRepository.findStocksForAllProducts();
		return stocks;
	}

	@Override
	public int checkAndUpdateStock(Stock stock) {
		BigDecimal quantityToUpdate = stock.getQuantity();
		Stock s = stockRepository.findStockByProductId(stock.getProductId());
		BigDecimal qyantityBeforeUpdate = s.getQuantity();
		BigDecimal quantityAfterUpdate = qyantityBeforeUpdate.add(quantityToUpdate);
		// check for maximum stock
		if (quantityAfterUpdate.intValueExact() > 1000) {
			return 1;
		}
		// check for minimum stock
		if (quantityAfterUpdate.intValueExact() < 0) {
			return 2;
		}
		stock.setQuantity(quantityAfterUpdate);
		stockRepository.save(stock);
		return 0;
	}

	@Override
	public boolean isProductExists(String productId) {
		return productRepository.existsById(productId);
	}

	@Override
	public Product saveProduct(Product product) {
		return productRepository.save(product);
	}

	@Override
	public Stock saveStock(Stock stock) {
		return stockRepository.save(stock);
	}

	@Override
	public void saveProduct(List<Product> products) {
		productRepository.saveAll(products);
	}

	@Override
	public void saveStock(List<Stock> stocks) {
		stockRepository.saveAll(stocks);
	}

	@Override
	public void loadProduct(String location) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		TypeReference<List<Product>> typeReference = new TypeReference<List<Product>>() {
		};
		InputStream inputStream = null;
		try {
			inputStream = TypeReference.class.getResourceAsStream(location);
			List<Product> listOfProducts = mapper.readValue(inputStream, typeReference);
			saveProduct(listOfProducts);
		} catch (IOException e) {
			logger.error("loading of product data failed");
			throw e;
		}
	}

	@Override
	public void loadStock(String location) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		TypeReference<List<Stock>> typeReference = new TypeReference<List<Stock>>() {
		};
		InputStream inputStream = null;
		try {
			inputStream = TypeReference.class.getResourceAsStream(location);
			List<Stock> listOfStocks = mapper.readValue(inputStream, typeReference);
			saveStock(listOfStocks);
		} catch (IOException e) {
			logger.error("loading of stock data failed");
			throw e;
		}
	}

}
