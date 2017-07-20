package org.non.config;

import java.util.List;

public class NetworkConfig {
	private List<Organization> orgs;
	private List<ChannelDetails> channelDetails;
	
	public void setOrgs(List<Organization>orgs){
		this.orgs=orgs;
	}
	
	public List<Organization> getOrgs(){
		return this.orgs;
	}
	
	public void setChannelDetails(List<ChannelDetails> channelDetails){
		this.channelDetails=channelDetails;
	}
	
	public List<ChannelDetails> getChannelDetails(){
		return this.channelDetails;
	}
}
