package edu.dubenco.alina.ordering.repo;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.couchbase.core.mapping.Document;
import org.springframework.data.couchbase.core.mapping.Field;
import org.springframework.data.couchbase.core.mapping.id.IdAttribute;

/**
 * This class holds information about a purchase order. <br/>
 * 
 * @author Alina Dubenco
 *
 */
@Document
public class Order {
	@IdAttribute private int id;
	@Field private String user;
	@Field private String custName;
	@Field private String address;
	@Field private String city;
	@Field private List<OrderDetail> details;
	@Field private BigDecimal amount;
	@Field private BigDecimal taxAmount;
	@Field private BigDecimal deliveryAmount;
	
	public List<OrderDetail> getDetails() {
		return details;
	}
	public void setDetails(List<OrderDetail> details) {
		this.details = details;
	}
	public BigDecimal getAmount() {
		return amount;
	}
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
	public BigDecimal getTaxAmount() {
		return taxAmount;
	}
	public void setTaxAmount(BigDecimal taxAmount) {
		this.taxAmount = taxAmount;
	}
	public BigDecimal getDeliveryAmount() {
		return deliveryAmount;
	}
	public void setDeliveryAmount(BigDecimal deliveryAmount) {
		this.deliveryAmount = deliveryAmount;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public String getCustName() {
		return custName;
	}
	public void setCustName(String custName) {
		this.custName = custName;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
}
