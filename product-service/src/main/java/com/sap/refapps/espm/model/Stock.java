package com.sap.refapps.espm.model;

import java.math.BigDecimal;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * This is the Stock entity class which defines the data model for stock.
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

	@OneToOne(fetch = FetchType.EAGER, optional = false, cascade = { CascadeType.PERSIST })
	@JoinColumn(name = "product_id", insertable = false, updatable = false)
	private Product product;

	public void setProduct(Product product) {
		this.product = product;
	}

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

	@Override
	public String toString() {
		return "{" + "productId'" + productId + '\'' + ", Quantity'" + quantity + '\'' + '}';
	}

}
