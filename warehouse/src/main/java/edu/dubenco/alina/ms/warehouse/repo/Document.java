package edu.dubenco.alina.ms.warehouse.repo;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

/**
 * This class holds information about a document - either 'products supply' or 'products reservation'
 * 
 * @author Alina Dubenco
 *
 */
@Entity
public class Document {
	private @Id @GeneratedValue Long id;
	private int number;
	private Date date;
	private boolean confirmed;
	
	@OneToMany(mappedBy="document", fetch = FetchType.EAGER)
	private List<InputOutput> inputOutputs;

	public Document() {}
	
	public Document(int number, Date date, boolean confirmed) {
		super();
		this.number = number;
		this.date = date;
		this.confirmed = confirmed;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public boolean isConfirmed() {
		return confirmed;
	}

	public void setConfirmed(boolean confirmed) {
		this.confirmed = confirmed;
	}

	public List<InputOutput> getInputOutputs() {
		return inputOutputs;
	}

	public void setInputOutputs(List<InputOutput> inputOutputs) {
		this.inputOutputs = inputOutputs;
	}
	
}
