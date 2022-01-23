package edu.dubenco.alina.ordering;

/**
 * This class is used in REST API as the details part of a {@link DocumentInfo}
 * 
 * @author Alina Dubenco
 *
 */
public class InputOutputInfo {
	private long product;
	private int quantity;
	
	public InputOutputInfo(long product, int quantity) {
		this.product = product;
		this.quantity = quantity;
	}
	public long getProduct() {
		return product;
	}
	public void setProduct(long product) {
		this.product = product;
	}
	public int getQuantity() {
		return quantity;
	}
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
}
