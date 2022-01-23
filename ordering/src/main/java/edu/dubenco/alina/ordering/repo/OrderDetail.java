package edu.dubenco.alina.ordering.repo;

import java.math.BigDecimal;

import org.springframework.data.couchbase.core.mapping.Field;

/**
 * This class holds detailed information about one orderred product. <br/>
 * It is used to hold one record of Order details.
 * 
 * @author Alina Dubenco
 *
 */
public class OrderDetail {
	@Field private long productId;
	@Field private BigDecimal price;
	@Field private int quantity;
	
	public long getProductId() {
		return productId;
	}
	public void setProductId(long productId) {
		this.productId = productId;
	}
	public BigDecimal getPrice() {
		return price;
	}
	public void setPrice(BigDecimal price) {
		this.price = price;
	}
	public int getQuantity() {
		return quantity;
	}
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

}
