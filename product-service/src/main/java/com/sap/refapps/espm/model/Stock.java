package com.sap.refapps.espm.model;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * This is the Stock entity class 
 * which defines the data model for stock.
 *
 */
@Entity
@Table(name = "ESPM_STOCK")
public class Stock {
	@Id
	@Column(length = 10, name = "PRODUCT_ID", unique = true)
	private String productId;

	@Column(name = "QUANTITY", precision = 13, scale = 3, nullable = false)
	private BigDecimal quantity;

	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}

	public BigDecimal getQuantity() {
		return quantity;
	}

	public void setQuantity(BigDecimal quantity) {
		this.quantity = quantity;
	}

}
