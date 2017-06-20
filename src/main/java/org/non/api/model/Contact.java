package org.non.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Contact {

	@JsonProperty
	private String name;
	 
	@JsonProperty
	private String email;
	
	@JsonProperty
	private String telephone;
	
	public Contact(String name, String email, String telephone) {
		this.name = name;
		this.email = email;
		this.telephone = telephone;
	}
	
	
}
