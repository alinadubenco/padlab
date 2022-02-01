package edu.dubenco.alina.ms.warehouse.repo;

import java.math.BigDecimal;
import java.util.Objects;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.springframework.data.annotation.Version;

/**
 * This class holds information about a product.
 * 
 * @author Alina Dubenco
 *
 */
@Entity
public class Product {

	@Id @GeneratedValue 
	private Long id;
	
	private String name;
	private String description;
	private String category;
	private BigDecimal price;

	private String characteristics;
	
	@Version
	private long version;
	
	@Transient
	private int quantity;

	public Product() {
	}

	public Product(String name, String role) {

		this.name = name;
		this.description = role;
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	@Override
	public String toString() {
		return "Product{" + "id=" + this.id + ", name='" + this.name + "\'}'";
	}

	public String getCharacteristics() {
		return characteristics;
	}

	public void setCharacteristics(String characteristics) {
		this.characteristics = characteristics;
	}

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}

	@Override
	public boolean equals(Object o) {

		if (this == o)
			return true;
		if (!(o instanceof Product))
			return false;
		Product employee = (Product) o;
		return Objects.equals(this.id, employee.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.id);
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

}