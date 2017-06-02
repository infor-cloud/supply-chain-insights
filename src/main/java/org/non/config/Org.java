package org.non.config;

import java.util.List;

public class Org {
	private String name;
	private String domain;
	private Msp msp;
	private List<Peers> peer;
	private List<AnchorPeer> anchorPeers;
	private Orderer orderer;
	private List<EventHub> eventHub;
	private CA ca;
	private List<Users> user;
	
	public void setName(String name){
		this.name=name;
	}
	
	public String getName(){
		return this.name;
	}
	
	public void setDomain(String domain){
		this.domain=domain;
	}
	
	public String getDomain(){
		return this.domain;
	}
	
	public void setMsp(Msp msp){
		this.msp=msp;
	}
	
	public Msp getMsp(){
		return this.msp;
	}
	
	public void setPeer(List<Peers>peer){
		this.peer=peer;
	}
	
	public List<Peers> getPeer(){
		return this.peer;
	}
	
	public void setAnchorPeer(List<AnchorPeer> anchorPeers){
		this.anchorPeers=anchorPeers;
	}
	
	public List<AnchorPeer> getAnchorPeers(){
		return this.anchorPeers;
	}
	
	public void setOrderer(Orderer orderer){
		this.orderer=orderer;
	}
	
	public Orderer getOrderer(){
		return this.orderer;
	}
	
	public void setEventHub(List<EventHub> eventHub){
		this.eventHub=eventHub;
	}
	
	public List<EventHub> getEventHub(){
		return this.eventHub;
	}
	
	public void setCA(CA ca){
		this.ca=ca;
	}
	
	public CA getCA(){
		return this.ca;
	}
	
	public void setUser(List<Users>user){
		this.user=user;
	}
	
	public List<Users> getUser(){
		return this.user;
	}
	
	
	

}
