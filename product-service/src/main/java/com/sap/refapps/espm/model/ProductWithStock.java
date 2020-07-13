package com.sap.refapps.espm.model;

import java.math.BigDecimal;

/**
 * This is the entity to get both product,stock information together
 *
 */
public class ProductWithStock {

	private String productId;
	private String name;
	private String shortDescription;
	private BigDecimal price;
	private String pictureUrl;
	private BigDecimal quantity;

	public ProductWithStock(String productId, String name, String shortDescription, BigDecimal price, String pictureUrl,
			BigDecimal quantity) {
		this.productId = productId;
		this.name = name;
		this.shortDescription = shortDescription;
		this.price = price;
		this.pictureUrl = pictureUrl;
		this.quantity = quantity;
	}

	public BigDecimal getQuantity() {
		return quantity;
	}

	public void setQuantity(BigDecimal quantity) {
		this.quantity = quantity;
	}

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

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public String getPictureUrl() {
		return pictureUrl;
	}

	public void setPictureUrl(String pictureUrl) {
		this.pictureUrl = pictureUrl;
	}

	@Override
	public String toString() {
		return "ProductWithStock [productId=" + productId + ", name=" + name + ", shortDescription=" + shortDescription
				+ ", price=" + price + ", pictureUrl=" + pictureUrl + ", quantity=" + quantity + "]";
	}

}
