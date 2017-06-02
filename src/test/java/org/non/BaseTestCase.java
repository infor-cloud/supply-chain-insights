package org.non;

import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.exception.CryptoException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.non.config.HLConfiguration;

public class BaseTestCase {

	private static HFClient client;
	
	@Test
	public void test(){
		assert(true);
	}
	
	@BeforeClass
	public void buildRegistry(){
		HLConfiguration config = new HLConfiguration();
	//	config.load(path);
	}
	
	
	@Before
	public void setUp(){
		
		 try {
			client = HFClient.createNewInstance(); 
			client.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
			//client.setUserContext();
		} catch (CryptoException e) {

		} catch (InvalidArgumentException e) {
		
		}
	}
	
	public static HFClient getHyperLedgetFabricClient(){
		return client;
	}
}
