package com.sap.refapps.espm.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.sap.refapps.espm.model.ProductWithStock;
import com.sap.refapps.espm.model.Stock;

/**
 * This is the stock repository interface 
 * which is responsible for communicating with database.
 *
 */
public interface StockRepository extends CrudRepository<Stock, String>{

	/** Returns stock based on product id.
	 * 
	 * @param productId
	 * @return stock
	 */
	@Query(value = "SELECT * FROM ESPM_STOCK WHERE PRODUCT_ID = ?1", nativeQuery = true)
	Stock findStockByProductId(@Param("PRODUCT_ID") String productId);
	
	@Query(value = "SELECT new com.sap.refapps.espm.model.ProductWithStock(prod.productId, prod.name, prod.shortDescription, prod.price, prod.pictureUrl, stock.quantity)" + 
			"FROM Product prod INNER JOIN Stock stock on prod.productId = stock.productId")
	Iterable<ProductWithStock> findStocksForAllProducts();
	
}
