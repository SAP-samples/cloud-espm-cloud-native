package com.sap.refapps.espm.model;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * This is the SalesOrder entity class used to define the data model for sales
 * order.
 *
 */
@Entity
@Table(name = "ESPM_SALES_ORDER")
public class SalesOrder implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "SALES_ORDER_ID", length = 50, unique = true)
	private String salesOrderId;

	@Column(name = "CUSTOMER_EMAIL", nullable = false)
	private String customerEmail;

	@Column(name = "PRODUCT_ID", nullable = false)
	private String productId;

	@Column(name = "CURRENCY_CODE", length = 5, nullable = false)
	private String currencyCode;

	@Column(name = "GROSS_AMOUNT", precision = 15, scale = 3, nullable = false)
	private BigDecimal grossAmount;

	@Column(name = "NET_AMOUNT", precision = 15, scale = 3, nullable = false)
	private BigDecimal netAmount;

	@Column(name = "TAX_AMOUNT", precision = 15, scale = 3, nullable = false)
	private BigDecimal taxAmount;

	@Column(length = 1, name = "LIFECYCLE_STATUS")
	private String lifecycleStatus;

	@Column(name = "LIFECYCLE_STATUS_NAME")
	private String lifecycleStatusName;

	@Column(name = "QUANTITY", precision = 15, scale = 3, nullable = false)
	private BigDecimal quantity;

	@Column(length = 3, name = "QUANTITY_UNIT")
	private String quantityUnit;

	@Column(name = "DELIVERY_DATE")
	private String deliveryDate;

	@Column(name = "CREATED_AT")
	private String createdAt;
	
	@Column(name = "NOTE")
	private String note;
	
	@Column(name = "PRODUCT_NAME", nullable = false)
    private String productName;

	public String getSalesOrderId() {
		return salesOrderId;
	}

	public void setSalesOrderId(String salesOrderId) {
		this.salesOrderId = salesOrderId;
	}

	public String getCustomerEmail() {
		return customerEmail;
	}

	public void setCustomerEmail(String customerEmail) {
		this.customerEmail = customerEmail;
	}

	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}

	public String getCurrencyCode() {
		return currencyCode;
	}

	public void setCurrencyCode(String currencyCode) {
		this.currencyCode = currencyCode;
	}

	public BigDecimal getGrossAmount() {
		return grossAmount;
	}

	public void setGrossAmount(BigDecimal grossAmount) {
		this.grossAmount = grossAmount;
	}

	public BigDecimal getNetAmount() {
		return netAmount;
	}

	public void setNetAmount(BigDecimal netAmount) {
		this.netAmount = netAmount;
	}

	public BigDecimal getTaxAmount() {
		return taxAmount;
	}

	public void setTaxAmount(BigDecimal taxAmount) {
		this.taxAmount = taxAmount;
	}

	public String getLifecycleStatus() {
		return lifecycleStatus;
	}

	public void setLifecycleStatus(String lifecycleStatus) {
		this.lifecycleStatus = lifecycleStatus;
	}

	public String getLifecycleStatusName() {
		return lifecycleStatusName;
	}

	public void setLifecycleStatusName(String lifecycleStatusName) {
		this.lifecycleStatusName = lifecycleStatusName;
	}

	public BigDecimal getQuantity() {
		return quantity;
	}

	public void setQuantity(BigDecimal quantity) {
		this.quantity = quantity;
	}

	public String getQuantityUnit() {
		return quantityUnit;
	}

	public void setQuantityUnit(String quantityUnit) {
		this.quantityUnit = quantityUnit;
	}

	public String getDeliveryDate() {
		return deliveryDate;
	}

	public void setDeliveryDate(String deliveryDate) {
		this.deliveryDate = deliveryDate;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}
	
	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}
	
	public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }
}
