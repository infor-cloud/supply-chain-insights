package org.non.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Connection {
	@JsonProperty
	private String buyer;
	@JsonProperty
	private String seller;
	@JsonProperty
	private String status;
	@JsonProperty
	private String businessProcess;
	
	public Connection (String cp1, String cp2, String status, String businessProcess){
		
		buyer = cp1;
		seller = cp2;
		this.status = status;
		this.businessProcess = businessProcess;	
		
	}
	
}
	
