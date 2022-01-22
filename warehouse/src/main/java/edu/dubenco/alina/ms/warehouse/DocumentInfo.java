package edu.dubenco.alina.ms.warehouse;

import java.util.Date;
import java.util.List;

/**
 * This class is used in REST APIs to receive and transmit information about a document 
 * (either 'products supply' or 'products reservation').
 * 
 * @author Alina Dubenco
 *
 */
public class DocumentInfo {
	private long id;
	private int number;
	private Date date;
	private List<InputOutputInfo> inputOutputs;
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
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
	public List<InputOutputInfo> getInputOutputs() {
		return inputOutputs;
	}
	public void setInputOutputs(List<InputOutputInfo> inputOutputs) {
		this.inputOutputs = inputOutputs;
	}

}
