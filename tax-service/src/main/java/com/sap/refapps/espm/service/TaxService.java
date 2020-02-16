package com.sap.refapps.espm.service;

import java.math.BigDecimal;
import java.util.HashMap;

import org.springframework.stereotype.Service;

import com.sap.refapps.espm.model.Tax;

/**
 * This is the service class for Tax
 *
 */
@Service
public class TaxService {

	private BigDecimal taxAmount = null;

	// Create a HashMap
	static HashMap<String, Double> hmap = new HashMap<String, Double>();

	/**
	 * It clears the entries in TaxSLab 
	 */
	public void clearTaxSlabs(){
		hmap.clear();
	}

	/**
	 * This method is used to setting up the tax slab
	 * 
	 * @param slab1
	 * @param slab2
	 * @param slab3
	 * @param slab4
	 */
	public void setTaxSlabs(double slab1, double slab2, double slab3, double slab4){
		hmap.put("SLAB1", slab1);
		hmap.put("SLAB2", slab2);
		hmap.put("SLAB3", slab3);
		hmap.put("SLAB4", slab4);
	}

	/**
	 * This method is used to updating the tax slab.
	 * 
	 * @param slab1
	 * @param slab2
	 * @param slab3
	 * @param slab4
	 */
	public void updateTaxSlabs(double slab1, double slab2, double slab3, double slab4){
		if(slab1 != -1){
			hmap.remove("SLAB1");
			hmap.put("SLAB1", slab1);
		}
		if(slab2 != -1){
			hmap.remove("SLAB2");
			hmap.put("SLAB2", slab2);
		}
		if(slab3 != -1){
			hmap.remove("SLAB3");
			hmap.put("SLAB3", slab3);
		}
		if(slab4 != -1){
			hmap.remove("SLAB4");
			hmap.put("SLAB4", slab4);
		}
	}

	/**
	 * This method is used to calculate the tax amount based on gross amount
	 * and tax slab.
	 * 
	 * @param amount
	 * @param slab
	 * @return (BigDecimal) tax percentage
	 */
	private BigDecimal taxCalculator(BigDecimal amount, double slab) {
		// calculating tax amount based on tax % age
		BigDecimal results = null;
		results = (amount.multiply(BigDecimal.valueOf(slab))).divide(BigDecimal.valueOf(100));
		return results;
	}


	/**
	 * Returns tax based on amount provided.
	 * 
	 * @param amount
	 * @return tax
	 */
	public Tax calculateTaxFromAmount(BigDecimal amount){
		Tax tax = new Tax();
		//if amount is less than 500
		if(amount.compareTo(BigDecimal.valueOf(499.99)) <= 0){
			taxAmount = taxCalculator(amount,hmap.get("SLAB1"));

			tax.setTaxAmount(taxAmount);
			tax.setTaxPercentage(hmap.get("SLAB1"));
		}

		//if amount is greater than equal to 500 and less than equal to 1000

		if(amount.compareTo(BigDecimal.valueOf(500.00)) >= 0 && amount.compareTo(BigDecimal.valueOf(999.99)) <= 0){

			taxAmount = taxCalculator(amount,hmap.get("SLAB2"));

			tax.setTaxAmount(taxAmount);
			tax.setTaxPercentage(hmap.get("SLAB2"));
		}

		//if amount is greater than equal to 500 and less than equal to 1000
		if(amount.compareTo(BigDecimal.valueOf(1000.00)) >= 0 && amount.compareTo(BigDecimal.valueOf(1999.99)) <= 0){
			taxAmount = taxCalculator(amount,hmap.get("SLAB3"));
			tax.setTaxAmount(taxAmount);
			tax.setTaxPercentage(hmap.get("SLAB3"));
		}

		//if amount is greater than 2000 
		if(amount.compareTo(BigDecimal.valueOf(2000.00)) >= 0 ){
			taxAmount = taxCalculator(amount,hmap.get("SLAB4"));
			tax.setTaxAmount(taxAmount);
			tax.setTaxPercentage(hmap.get("SLAB4"));
		}

		return tax;


	}
}
