package org.gtnexus.blockchain.channel;

import static java.lang.String.format;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.hyperledger.fabric.sdk.BlockEvent;
import org.hyperledger.fabric.sdk.BlockEvent.TransactionEvent;
import org.hyperledger.fabric.sdk.ChaincodeID;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.EventHub;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.Orderer;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.TransactionEventException;

import org.junit.Test;
import org.non.api.code.HyperledgerAPI;
import org.non.api.code.HyperledgerTestAPI;
import org.non.api.code.HLConfigHelper;
import org.non.config.ChannelDetails;
import org.non.config.HLConfiguration;
import org.non.config.Organization;

public class ChannelUtilTest_AddPeers {

	private String CHAIN_CODE_NAME = "myCC_go";
	private String CHAIN_CODE_PATH = "main/go/chaincode/myCC";
	private String CHAIN_CODE_VERSION = "1";

	private String ORG_NAME_GTN = "GTN";
	private String ORG_NAME_ELEMICA = "Elemica";
	private String ORG_NAME_DNB = "Dun&BradStreet";
	protected static String TESTUSER_1_NAME = "User1";
	protected static String TEST_ADMIN_NAME = "admin";

	private int TRANSACTIONWAITTIME = 140000;

	@Test
	public void createChannelTest() throws InvalidArgumentException {
		
		HFClient client = HLConfigHelper.getHyperledgerFabricClient();
		HLConfiguration config = HLConfigHelper.getHyperledgerFabricConfig();
		
		ChannelDetails channelDetails = config.getChannelDetails("ch1");
		try {
			/* Take only org GTN to construct the channel */
			Organization thisOrg = config.getOrgDetailsByName(ORG_NAME_GTN);
			client.setUserContext(thisOrg.getPeerAdmin());
			
			/* Take ordererDetails to create object of orderers */
			List<Orderer> orderers = thisOrg.getOrderer();
			
			/* Take peerDetails to create object of peers */
			List<Peer> peers=thisOrg.getPeers();
			
			/*Take eventHubDetails to create object of eventHubs*/
			List<EventHub> eventHubs = thisOrg.getEventHub();
			
			//////////////////////////////////////////////////////////////////////////////////////////////////
			/* Construct Channel by org GTN*/
			
			Channel ch1 = HyperledgerTestAPI.constructChannelwithOneOrg("ch1", client, thisOrg.getPeerAdmin(), orderers, peers,
					eventHubs, channelDetails.getTransactionFilePath());

			/* ChainCode Configuration */
			ChaincodeID chaincodeID = ChaincodeID.newBuilder().setName(CHAIN_CODE_NAME).setVersion(CHAIN_CODE_VERSION)
					.setPath(CHAIN_CODE_PATH).build();
			
			/* Install Chaincode on peers of GTN*/
			peers = thisOrg.getPeers();
			HyperledgerAPI.installChaincode(client, thisOrg.getPeerAdmin(), peers, chaincodeID);
			
			
			/////////////////////////////////////////////////////////////////////////////////////////////////
			//Then bring Elemica and DnB onto channel
			//
			
			/* Add peers onto channel and install Chaincode */
			List<Organization> newChannelOrgs = Arrays.asList(config.getOrgDetailsByName(ORG_NAME_ELEMICA), config.getOrgDetailsByName(ORG_NAME_DNB));
			for (Organization org : newChannelOrgs) {
				orderers = org.getOrderer();
				peers = org.getPeers();
				eventHubs = org.getEventHub();
				
				HyperledgerTestAPI.addPeersOntoChannel(ch1, client, org.getPeerAdmin(), orderers, peers, eventHubs);
				HyperledgerAPI.installChaincode(client, org.getPeerAdmin(), peers, chaincodeID);
			
			}

			/* Run the channel by using Hyperledger API */

			////////////////////////////////////////////////
			HyperledgerTestAPI.showPeersOnChannel(ch1);

			String sampleKey = "Zara";
			String sampleRecord = "Name: Zara, ID: 000-000-999";

			CompletableFuture<TransactionEvent> initFuture = HyperledgerAPI.initiateChaincode(client, chaincodeID, ch1,
					"scripts/chaincodeendorsementpolicy.yaml");
			initFuture.thenApply(transactionEvent -> {
				assertTrue(transactionEvent.isValid());
				out("Finished instantiate transaction with transaction id %s", transactionEvent.getTransactionID());

				try {
					CompletableFuture<TransactionEvent> invokeFuture = HyperledgerAPI.invoke(
							new String[] { "add", sampleKey, sampleRecord }, config.getOrgDetailsByName(ORG_NAME_GTN).getUserByName(TESTUSER_1_NAME), client, chaincodeID, ch1);
					invokeFuture.thenApply(invokeTransactionEvent -> {
						assertTrue(invokeTransactionEvent.isValid());
						out("Finished transaction with transaction id %s", invokeTransactionEvent.getTransactionID());
						String payload = null;
						try {
							Organization org=config.getOrgDetailsByName(ORG_NAME_DNB);
							String[] args = {"query", sampleKey};
							payload = HyperledgerAPI.query(args, org.getUserByName(TESTUSER_1_NAME),
									client, chaincodeID, ch1,org.getPeers());
						} catch (Exception e) {
							e.printStackTrace();
						}
						out("Query payload of %s returned %s", sampleKey, payload);
						return null;
					}).get(TRANSACTIONWAITTIME, TimeUnit.SECONDS);

				} catch (Exception e) {
					if (e instanceof TransactionEventException) {
						BlockEvent.TransactionEvent te = ((TransactionEventException) e).getTransactionEvent();
						if (te != null) {
							fail(format("Transaction with txid %s failed. %s", te.getTransactionID(), e.getMessage()));
						}
						fail(format("Test failed with %s exception %s", e.getClass().getName(), e.getMessage()));
					} else {
						out("Caught an exception running chain %s", ch1.getName());
						e.printStackTrace();
						fail("Test failed with error : " + e.getMessage());
					}
				}
				return null;

			}).get(TRANSACTIONWAITTIME, TimeUnit.SECONDS);

			out("That's all!");

		} catch (Exception e) {
			e.printStackTrace();

			fail(e.getMessage());
		}
	}
	
	
	static void out(String format, Object... args) {
		System.err.flush();
		System.out.flush();
		System.out.println(format(format, args));
		System.err.flush();
		System.out.flush();
	}
	
}
