package org.non.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Identifier {
	
	@JsonProperty
	private String schemeName;
	
	@JsonProperty
	private String id;
	
	@Override
	public String toString(){
		return "\n Scheme Name: " + schemeName +
			   "\n Id: " + id;
	}
}
