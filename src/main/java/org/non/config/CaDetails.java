package org.non.config;

import java.util.Properties;

import org.hyperledger.fabric_ca.sdk.HFCAClient;

public class CaDetails {
	private String containerName;
	private String location;
	private Properties caProperties= null;
	private HFCAClient caClient;

	
	public void setLocation(String location){
		this.location=location;
	}
	
	public String getLocation(){
		return this.location;
	}
	
	public void setContainerName(String containerName){
		this.containerName=containerName;
	}
	
	public String getContainerName(){
		return this.containerName;
	}
	
	public HFCAClient getCAClient() {

        return caClient;
    }

    public void setCAClient(HFCAClient caClient) {

        this.caClient = caClient;
    }
    
    public void setCAProperties(Properties CAProperties) {
        this.caProperties = CAProperties;
    }

    public Properties getCAProperties() {
        return caProperties;
    }



	

}
