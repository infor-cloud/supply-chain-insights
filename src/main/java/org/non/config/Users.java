package org.non.config;


public class Users {
	private String name;
	private String hostName;
	private String role;
	
	public void setName(String name){
		this.name=name;
	}
	
	public String getName(){
		return this.name;
	}
	
	public void setHostName(String hostName){
		this.hostName=hostName;
	}
	
	public String getHostName(){
		return this.hostName;
	}
	
	public void setRole(String role){
		this.role=role;
	}
	
	public String getRole(){
		return this.role;
	}


}
