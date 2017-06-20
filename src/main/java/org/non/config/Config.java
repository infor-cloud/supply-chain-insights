package org.non.config;

import java.util.List;

public class Config {
	private List<Org> orgs;
	private List<ChannelDetails> channelDetails;
	
	public void setOrgs(List<Org>orgs){
		this.orgs=orgs;
	}
	
	public List<Org> getOrgs(){
		return this.orgs;
	}
	
	public void setChannelDetails(List<ChannelDetails> channelDetails){
		this.channelDetails=channelDetails;
	}
	
	public List<ChannelDetails> getChannelDetails(){
		return this.channelDetails;
	}
}
