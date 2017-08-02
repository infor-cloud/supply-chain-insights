package org.non.api.model;

import javax.ws.rs.QueryParam;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

@JsonAutoDetect(fieldVisibility = Visibility.ANY)
public class Connection {
	@QueryParam("comp1")
	private String comp1;
	@QueryParam("comp2")
	private String comp2;
	@QueryParam("status")
	private String status;
	@QueryParam("process")
	private String businessProcess;
	
	public String toString(){
		return "Company 1: " + comp1 + 
				"\nCompany2: " + comp2 + 
				"\nStatus: " + status + 
				"\nBusiness Process: " + businessProcess;
	}
	
}
	
