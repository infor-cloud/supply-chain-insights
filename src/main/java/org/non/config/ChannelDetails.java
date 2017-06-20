package org.non.config;

import java.util.List;

public class ChannelDetails {
	private String name;
	private String transactionFilePath;
	private List<String> orgsOnChannel;
	
	public void setName(String name){
		this.name=name;
	}
	
	public String getName(){
		return this.name;
	}
	
	public void setTransactionFilePath(String transactionFilePath){
		this.transactionFilePath=transactionFilePath;
	}
	
	public String getTransactionFilePath(){
		return this.transactionFilePath;
	}
	
	public void setorgsOnChannel(List<String> orgsOnChannel){
		this.orgsOnChannel=orgsOnChannel;
	}
	
	public List<String> getOrgsOnChannel(){
		return this.orgsOnChannel;
	}
	

}
