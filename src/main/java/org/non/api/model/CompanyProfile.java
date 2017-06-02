package org.non.api;

import java.util.List;

public class CompanyProfile {
	
	private String name;
	private Contact contact;
	private String websiteURI;
	private Address address;
	private List<Identifier> identifier;
	
	public CompanyProfile(){
		// Jackson deserialization	
	}
	
	public CompanyProfile(String name, Contact contact, String websiteURI, Address address,
			List<Identifier> identifier) {
		this.name = name;
		this.contact = contact;
		this.websiteURI = websiteURI;
		this.address = address;
		this.identifier = identifier;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Contact getContact() {
		return contact;
	}
	public void setContact(Contact contact) {
		this.contact = contact;
	}
	public String getWebsiteURI() {
		return websiteURI;
	}
	public void setWebsiteURI(String websiteURI) {
		this.websiteURI = websiteURI;
	}
	public Address getAddress() {
		return address;
	}
	public void setAddress(Address address) {
		this.address = address;
	}
	public List<Identifier> getIdentifier() {
		return identifier;
	}
	public void setIdentifier(List<Identifier> identifier) {
		this.identifier = identifier;
	}
	

}
