package org.non.config;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.hyperledger.fabric.sdk.EventHub;
import org.hyperledger.fabric.sdk.Orderer;
import org.hyperledger.fabric.sdk.Peer;

public class Organization {
	private String name;
	private String domain;
	private MspDetails msp;
	private List<PeerDetails> peerDetails;
	private List<Peer> peers;	
	private List<AnchorPeerDetails> anchorPeers;
	private List<OrdererDetails> ordererDetails;
	private Map<String,String> eventHubMap;
	private List<Orderer> orderer;
	private List<EventHub> eventHub;
	private CaDetails ca;
	private List<NetworkUser> users;
	private NetworkUser admin;
	private NetworkUser peerAdmin;
	
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
	
	public void setPeerDetails(List<PeerDetails>peer){
		this.peerDetails=peer;
	}
	
	public List<PeerDetails> getPeerDetails(){
		return this.peerDetails;
	}
	
	public void setAnchorPeer(List<AnchorPeerDetails> anchorPeers){
		this.anchorPeers=anchorPeers;
	}
	
	public List<AnchorPeerDetails> getAnchorPeers(){
		return this.anchorPeers;
	}
	
	public void setOrdererDetails(List<OrdererDetails> orderer){
		this.ordererDetails=orderer;
	}
	
	public List<OrdererDetails> getOrdererDetails(){
		return this.ordererDetails;
	}
	
	public void setEventHubMap(Map<String,String> eventHub){
		this.eventHubMap=eventHub;
	}
	
	public Map<String,String> getEventHubMap(){
		return this.eventHubMap;
	}
	
	public Set<String> getEventHubNames(){
		return Collections.unmodifiableSet(eventHubMap.keySet());
	}
	
	public Collection<String> getEventHubLocations() {
        return Collections.unmodifiableCollection(eventHubMap.values());
    }
	
	
	public void setCA(CaDetails ca){
		this.ca=ca;
	}
	
	public CaDetails getCA(){
		return this.ca;
	}
	
	public void setUsers(List<NetworkUser>users){
		this.users=users;
	}
	
	public List<NetworkUser> getUsers(){
		return this.users;
	}
	
	public NetworkUser getAdmin() {
        return admin;
    }

    public void setAdmin(NetworkUser admin) {
        this.admin = admin;
    }

	
	public NetworkUser getUserByName(String userName){
		NetworkUser user=null;
		try
		{
			user=this.users.stream().filter(p->p.getName().equals(userName)).collect(Collectors.<NetworkUser> toList()).get(0);
		}
		catch(Exception e){
			user=null;
		}
		return user;
	}
	
	public void addUser(NetworkUser user) {
        this.users.add(user);
    }

	public void setPeerAdmin(NetworkUser peerAdmin) {
        this.peerAdmin = peerAdmin;
    }

	public NetworkUser getPeerAdmin(){
		return this.peerAdmin;
	}
	
	public List<Peer> getPeers() {
		return peers;
	}

	public void setPeers(List<Peer> peers) {
		this.peers = peers;
	}

	public List<Orderer> getOrderer() {
		return orderer;
	}

	public void setOrderer(List<Orderer> orderer) {
		this.orderer = orderer;
	}

	public List<EventHub> getEventHub() {
		return eventHub;
	}

	public void setEventHub(List<EventHub> eventHub) {
		this.eventHub = eventHub;
	}

	
	

}
