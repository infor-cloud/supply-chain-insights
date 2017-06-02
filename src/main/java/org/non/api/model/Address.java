package org.non.api;

public class Address {

	private String streetName;
	private String cityName;
	private String postalZone;
	
	public Address(){
		// Jackson deserialization
	}
	
	public Address(String streetName, String cityName, String postalZone){
		this.streetName = streetName;
		this.cityName = cityName;
		this.postalZone = postalZone;
	}
	
	public String getStreetName() {
		return streetName;
	}
	public void setStreetName(String streetName) {
		this.streetName = streetName;
	}
	public String getCityName() {
		return cityName;
	}
	public void setCityName(String cityName) {
		this.cityName = cityName;
	}
	public String getPostalZone() {
		return postalZone;
	}
	public void setPostalZone(String postalZone) {
		this.postalZone = postalZone;
	}
	
	
}
