package com.java.plm.MyWebApp.model;

import org.springframework.stereotype.Component;

@Component
public class Historylist {
	private String timestamp;
	private String discrepancies;

	public String getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	public String getDiscrepancies() {
		return discrepancies;
	}
	public void setDiscrepancies(String discrepancies) {
		this.discrepancies = discrepancies;
	}




}
