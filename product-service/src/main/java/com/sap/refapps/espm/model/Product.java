package com.sap.refapps.espm.model;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * This is the Product entity class which defines the data model for product.
 *
 */
@Entity
@Table(name = "ESPM_PRODUCT")
public class Product {

	@Id
	@Column(length = 10, name = "PRODUCT_ID", unique = true)
	private String productId;

	@Column(name = "PRODUCT_NAME", nullable = false)
	private String name;

	@Column(name = "SHORT_DESCRIPTION", nullable = false)
	private String shortDescription;

	@Column(length = 40, name = "PRODUCT_CATEGORY", nullable = false)
	private String category;

	@Column(name = "WEIGHT", precision = 13, scale = 3, nullable = false)
	private BigDecimal weight;

	@Column(length = 3, name = "WEIGHT_UNIT", nullable = false)
	private String weightUnit;

	@Column(name = "PRICE", precision = 23, scale = 3, nullable = false)
	private BigDecimal price;

	@Column(name = "CURRENCY_CODE", length = 5, nullable = false)
	private String currencyCode;

	@Column(name = "DIMENSION_WIDTH", precision = 13, scale = 4)
	private BigDecimal dimensionWidth;

	@Column(name = "DIMENSION_DEPTH", precision = 13, scale = 4)
	private BigDecimal dimensionDepth;

	@Column(name = "DIMENSION_HEIGHT", precision = 13, scale = 4)
	private BigDecimal dimensionHeight;

	@Column(name = "DIMENSION_UNIT", length = 3)
	private String dimensionUnit;

	@Column(name = "PICTURE_URL", length = 255)
	private String pictureUrl;

	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getShortDescription() {
		return shortDescription;
	}

	public void setShortDescription(String shortDescription) {
		this.shortDescription = shortDescription;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public BigDecimal getWeight() {
		return weight;
	}

	public void setWeight(BigDecimal weight) {
		this.weight = weight;
	}

	public String getWeightUnit() {
		return weightUnit;
	}

	public void setWeightUnit(String weightUnit) {
		this.weightUnit = weightUnit;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public String getCurrencyCode() {
		return currencyCode;
	}

	public void setCurrencyCode(String currencyCode) {
		this.currencyCode = currencyCode;
	}

	public BigDecimal getDimensionWidth() {
		return dimensionWidth;
	}

	public void setDimensionWidth(BigDecimal dimensionWidth) {
		this.dimensionWidth = dimensionWidth;
	}

	public BigDecimal getDimensionDepth() {
		return dimensionDepth;
	}

	public void setDimensionDepth(BigDecimal dimensionDepth) {
		this.dimensionDepth = dimensionDepth;
	}

	public BigDecimal getDimensionHeight() {
		return dimensionHeight;
	}

	public void setDimensionHeight(BigDecimal dimensionHeight) {
		this.dimensionHeight = dimensionHeight;
	}

	public String getDimensionUnit() {
		return dimensionUnit;
	}

	public void setDimensionUnit(String dimensionUnit) {
		this.dimensionUnit = dimensionUnit;
	}

	public String getPictureUrl() {
		return pictureUrl;
	}

	public void setPictureUrl(String pictureUrl) {
		this.pictureUrl = pictureUrl;
	}

}
