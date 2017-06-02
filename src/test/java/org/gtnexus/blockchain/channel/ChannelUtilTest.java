package org.gtnexus.blockchain.channel;

import static org.junit.Assert.assertTrue;


import org.hyperledger.fabric.sdk.Chain;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.junit.Test;
import org.non.BaseTestCase;

public class ChannelUtilTest extends BaseTestCase{

	@Test
	public void createChannelTest() throws InvalidArgumentException{
		HFClient client = getHyperLedgetFabricClient();
		Chain channel1 = client.newChain("Channel1");
		assertTrue(client.getChain("Channel1").getName().equals("Channel1"));
		//
	}
	// create a private channel and transaction by member org
	
	//  create a private channel and transaction by non member org
	
	// create a public channel and member org
}	
