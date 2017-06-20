package org.non.api.model;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.QueryParam;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

@JsonAutoDetect(fieldVisibility = Visibility.ANY)
public class TradingPartner {
	@QueryParam("name")
	private String name;
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getWebsiteURI() {
		return websiteURI;
	}

	public void setWebsiteURI(String websiteURI) {
		this.websiteURI = websiteURI;
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

	public String getContactName() {
		return contactName;
	}

	public void setContactName(String contactName) {
		this.contactName = contactName;
	}

	public String getContactEmail() {
		return contactEmail;
	}

	public void setContactEmail(String contactEmail) {
		this.contactEmail = contactEmail;
	}

	public String getContactTelephone() {
		return contactTelephone;
	}

	public void setContactTelephone(String contactTelephone) {
		this.contactTelephone = contactTelephone;
	}

	public String getIds() {
		return ids;
	}

	public void setIds(String ids) {
		this.ids = ids;
	}

	public List<Identifier> getIdList() {
		return idList;
	}

	public void setIdList(List<Identifier> idList) {
		this.idList = idList;
	}

	@QueryParam("websiteURI")
	private String websiteURI;
	@QueryParam("streetName")
	private String streetName;
	@QueryParam("cityName")
	private String cityName;
	@QueryParam("postalZone")
	private String postalZone;
	@QueryParam("contactName")
	private String contactName;
	@QueryParam("contactEmail")
	private String contactEmail;
	@QueryParam("contactTelephone")
	private String contactTelephone;
	@QueryParam("identifier")
	private String ids;
	List<Identifier> idList;
	
	
	public void init(){
		ObjectMapper mapper = new ObjectMapper(); 
		mapper.getTypeFactory();
		
		try {
			idList = mapper.readValue(ids, TypeFactory.defaultInstance().constructCollectionType(List.class, Identifier.class));
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(idList.size());
		for (Identifier i : idList){
			System.out.println(i);
		}
	}
	
	public String toString(){
		
		
		String result = "Trading Partner:" + "\n name=" + name + 
				"\n WebsiteURI: " + websiteURI + 
				"\n Street Name:" + streetName +
				"\n City Name: " + cityName + 
				"\n Postal Zone: " + postalZone +
				"\n Contact Name: " + contactName + 
				"\n Contact Email: " + contactEmail + 
				"\n Contact Telephone: " + contactTelephone +
				"\n Identifiers: " + ids;
		for (Identifier i : idList){
			result += "\n Identifier" + i;
		}
		
		return result;
	}
	
	public String toJSONString(){
		ObjectMapper mapper = new ObjectMapper();
		String result = null;
		try {
			result = mapper.writeValueAsString(this);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	
	
}