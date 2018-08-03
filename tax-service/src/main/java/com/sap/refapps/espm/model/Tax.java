package com.sap.refapps.espm.model;

import java.math.BigDecimal;

/**
 * This is the model class for Tax
 *
 */
public class Tax {

	private BigDecimal taxAmount;
	private double taxPercentage;
	
	public BigDecimal getTaxAmount() {
		return taxAmount;
	}
	public void setTaxAmount(BigDecimal taxAmount) {
		this.taxAmount = taxAmount;
	}
	public double getTaxPercentage() {
		return taxPercentage;
	}
	public void setTaxPercentage(double taxPercentage) {
		this.taxPercentage = taxPercentage;
	}
	
	
}
