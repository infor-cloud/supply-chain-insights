package org.non.api.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonAutoDetect(fieldVisibility = Visibility.ANY)
public class Connection {
	public String getComp1() {
		return comp1;
	}

	public void setComp1(String comp1) {
		this.comp1 = comp1;
	}

	public String getComp2() {
		return comp2;
	}

	public void setComp2(String comp2) {
		this.comp2 = comp2;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getBusinessProcess() {
		return businessProcess;
	}

	public void setBusinessProcess(String businessProcess) {
		this.businessProcess = businessProcess;
	}

	private String comp1;
	private String comp2;
	private String status;
	private String businessProcess;
	
	public Connection(String c1, String c2, String stat, String proc){
		comp1 = c1;
		comp2 = c2;
		status = stat;
		businessProcess = proc;
	}
	
	public String toString(){
		return "Company 1: " + comp1 + 
				"\nCompany2: " + comp2 + 
				"\nStatus: " + status + 
				"\nBusiness Process: " + businessProcess;
	}
	
	public String toJSONString(){
		ObjectMapper mapper = new ObjectMapper();
		String result = null;
		try {
			result = mapper.writeValueAsString(this);
		} catch (JsonProcessingException e) {
			System.out.println(e.getMessage());
		}
		return result;
	}
	
}
	
