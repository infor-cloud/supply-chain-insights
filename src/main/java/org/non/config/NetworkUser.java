package org.non.config;

import java.util.Set;

import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.User;

import io.netty.util.internal.StringUtil;

public class NetworkUser implements User{
	private String name;
	private String hostName;
	private Set<String> roles;
	private String account;//never set
    private String affiliation;//never set
    private String enrollmentSecret;
    private String mspID;
    private Enrollment enrollment = null; //need access in test env.

	
	public void setName(String name){
		this.name=name;
	}
	
	public String getName(){
		return this.name;
	}
	
	public void setHostName(String hostName){
		this.hostName=hostName;
	}
	
	public String getHostName(){
		return this.hostName;
	}
	
	
	@Override
	public String getAccount() {
		return this.account;
	}

	@Override
	public String getAffiliation() {
		return this.affiliation;
	}

	@Override
	public Enrollment getEnrollment() {
		return this.enrollment;
	}

	@Override
	public String getMspId() {
		return this.mspID;
	}

	@Override
	public Set<String> getRoles() {
		return this.roles;
	}
	
	public void setRoles(Set<String> roles) {

        this.roles = roles;        
    }

	
    public void setAccount(String account) {
        this.account = account;
   
    }
    
    public void setAffiliation(String affiliation) {
        this.affiliation = affiliation;
    }
    
    public String getEnrollmentSecret() {
        return enrollmentSecret;
    }

    public void setEnrollmentSecret(String enrollmentSecret) {
        this.enrollmentSecret = enrollmentSecret;

    }


    public void setEnrollment(Enrollment enrollment) {
        this.enrollment = enrollment;
       
    }

    public void setMPSID(String mspID) {
        this.mspID = mspID;

    }
    
    public boolean isEnrolled() {
        return this.enrollment != null;
    }

    
    public boolean isRegistered() {
        return !StringUtil.isNullOrEmpty(enrollmentSecret);
    }






}
