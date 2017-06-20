package org.non.config;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Org {
	private String name;
	private String domain;
	private MspDetails msp;
	private List<PeerDetails> peer;
	private List<AnchorPeerDetails> anchorPeers;
	private List<OrdererDetails> orderer;
	private Map<String,String> eventHub;
	private CaDetails ca;
	private List<NonUser> users;
	private NonUser admin;
	private NonUser peerAdmin;
	
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
	
	public void setMsp(MspDetails msp){
		this.msp=msp;
	}
	
	public MspDetails getMsp(){
		return this.msp;
	}
	
	public void setPeer(List<PeerDetails>peer){
		this.peer=peer;
	}
	
	public List<PeerDetails> getPeer(){
		return this.peer;
	}
	
	public void setAnchorPeer(List<AnchorPeerDetails> anchorPeers){
		this.anchorPeers=anchorPeers;
	}
	
	public List<AnchorPeerDetails> getAnchorPeers(){
		return this.anchorPeers;
	}
	
	public void setOrderer(List<OrdererDetails> orderer){
		this.orderer=orderer;
	}
	
	public List<OrdererDetails> getOrderer(){
		return this.orderer;
	}
	
	public void setEventHub(Map<String,String> eventHub){
		this.eventHub=eventHub;
	}
	
	public Map<String,String> getEventHub(){
		return this.eventHub;
	}
	
	public Set<String> getEventHubNames(){
		return Collections.unmodifiableSet(eventHub.keySet());
	}
	
	public Collection<String> getEventHubLocations() {
        return Collections.unmodifiableCollection(eventHub.values());
    }
	
	
	public void setCA(CaDetails ca){
		this.ca=ca;
	}
	
	public CaDetails getCA(){
		return this.ca;
	}
	
	public void setUsers(List<NonUser>users){
		this.users=users;
	}
	
	public List<NonUser> getUsers(){
		return this.users;
	}
	
	public NonUser getAdmin() {
        return admin;
    }

    public void setAdmin(NonUser admin) {
        this.admin = admin;
    }

	
	public NonUser getUserByName(String userName){
		NonUser user=null;
		try
		{
			user=this.users.stream().filter(p->p.getName().equals(userName)).collect(Collectors.<NonUser> toList()).get(0);
		}
		catch(Exception e){
			user=null;
		}
		return user;
	}
	
	public void addUser(NonUser user) {
        this.users.add(user);
    }

	public void setPeerAdmin(NonUser peerAdmin) {
        this.peerAdmin = peerAdmin;
    }

	public NonUser getPeerAdmin(){
		return this.peerAdmin;
	}
	
	
	

}
