package com.sap.refapps.espm.model;

import java.math.BigDecimal;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * This is the Cart entity class 
 * which defines the data model for cart.
 *
 */
@Entity
@Table(name = "ESPM_CART")
public class Cart {

	@Id
	@Column(name = "ITEM_ID", length = 50, unique = true)
	private String itemId;

	@Column(name = "PRODUCT_ID", length = 10)
	private String productId;

	@Column(name = "QUANTITY", precision = 13, scale = 3, nullable = false)
	private BigDecimal quantityUnit;

	@Column(name = "CHECK_OUT_STATUS")
	private Boolean checkOutStatus;

	@ManyToOne(fetch = FetchType.EAGER, cascade = { CascadeType.PERSIST })
	@JoinColumn(name = "CUSTOMER_ID")
	private Customer customer;
	
	@Column(name = "PRODUCT_NAME", nullable = false)
	private String name;
    
   public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getItemId() {
		return itemId;
	}

	public void setItemId(String itemId) {
		this.itemId = itemId;
	}

	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}

	public BigDecimal getQuantityUnit() {
		return quantityUnit;
	}

	public void setQuantityUnit(BigDecimal quantityUnit) {
		this.quantityUnit = quantityUnit;
	}

	public Boolean getCheckOutStatus() {
		return checkOutStatus;
	}

	public void setCheckOutStatus(Boolean checkOutStatus) {
		this.checkOutStatus = checkOutStatus;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

}
