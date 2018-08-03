package com.sap.refapps.espm.service;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.cloudfoundry.com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.cloud.cloudfoundry.com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.sap.refapps.espm.model.Product;
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

	/* (non-Javadoc)
	 * @see com.sap.refapps.espm.service.ProductService#getAllProducts()
	 */
	public Iterable<Product> getAllProducts() throws DataAccessException {

		Iterable<Product> products = productRepository.findAll();
		return products;
	}

	/* (non-Javadoc)
	 * @see com.sap.refapps.espm.service.ProductService#getProductById(java.lang.String)
	 */
	@Override
	public Product getProductById(String productId) {
		return productRepository.findProductById(productId);
	}

	/* (non-Javadoc)
	 * @see com.sap.refapps.espm.service.ProductService#getStockByProductId(java.lang.String)
	 */
	@Override
	public Stock getStockByProductId(String productId) {
		return stockRepository.findStockByProductId(productId);
	}

	/* (non-Javadoc)
	 * @see com.sap.refapps.espm.service.ProductService#checkAndUpdateStock(com.sap.refapps.espm.model.Stock)
	 */
	@Override
	public int checkAndUpdateStock(Stock stock) {
		BigDecimal quantityToUpdate = stock.getQuantity();
		BigDecimal qyantityBeforeUpdate = stockRepository.findStockByProductId(stock.getProductId()).getQuantity();
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

	/* (non-Javadoc)
	 * @see com.sap.refapps.espm.service.ProductService#isProductExists(java.lang.String)
	 */
	@Override
	public boolean isProductExists(String productId) {
		return productRepository.existsById(productId);
	}

	/* (non-Javadoc)
	 * @see com.sap.refapps.espm.service.ProductService#saveProduct(com.sap.refapps.espm.model.Product)
	 */
	@Override
	public Product saveProduct(Product product) {
		return productRepository.save(product);
	}

	/* (non-Javadoc)
	 * @see com.sap.refapps.espm.service.ProductService#saveStock(com.sap.refapps.espm.model.Stock)
	 */
	@Override
	public Stock saveStock(Stock stock) {
		return stockRepository.save(stock);
	}

	/* (non-Javadoc)
	 * @see com.sap.refapps.espm.service.ProductService#saveProduct(java.util.List)
	 */
	@Override
	public void saveProduct(List<Product> products) {
		productRepository.saveAll(products);
	}

	/* (non-Javadoc)
	 * @see com.sap.refapps.espm.service.ProductService#saveStock(java.util.List)
	 */
	@Override
	public void saveStock(List<Stock> stocks) {
		stockRepository.saveAll(stocks);
	}

	/* (non-Javadoc)
	 * @see com.sap.refapps.espm.service.ProductService#loadProduct(java.lang.String)
	 */
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
		} /*finally {
			try {
				inputStream.close();
			} catch (IOException e) {
				logger.info(e.getMessage());
			}
		}*/
	}

	/* (non-Javadoc)
	 * @see com.sap.refapps.espm.service.ProductService#loadStock(java.lang.String)
	 */
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
		} /*finally {
			try {
				inputStream.close();
			} catch (IOException e) {
				logger.info(e.getMessage());
			}
		}*/
	}

}
