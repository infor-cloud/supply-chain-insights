package org.non.config;

public class Peers {
	private String name;
	private String containerName;
	private String location;
	
	public void setName(String name){
		this.name=name;
	}
	
	public String getName(){
		return this.name;
	}
	
	public void setContainerName(String container_name){
		this.containerName=container_name;
	}
	
	public String getContainerName(){
		return this.containerName;
	}
	
	public void setLocation(String location){
		this.location=location;
	}
	
	public String getLocation(){
		return this.location;
	}
	

}
