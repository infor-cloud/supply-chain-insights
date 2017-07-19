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
//import org.non.BaseTestCase;
import org.non.api.code.HyperledgerAPI;
import org.non.api.code.HyperledgerTestAPI;
import org.non.api.code.HLConfigHelper;
import org.non.config.ChannelDetails;
import org.non.config.HLConfiguration;
import org.non.config.NonUser;
import org.non.config.OrdererDetails;
import org.non.config.Org;
import org.non.config.PeerDetails;

public class ChannelUtilTest_AddPeers {

	private String CHAIN_CODE_NAME = "myCC_go";
	private String CHAIN_CODE_PATH = "github.com/myCC";
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
			Org thisOrg = config.getOrgDetailsByName(ORG_NAME_GTN);
			client.setUserContext(thisOrg.getPeerAdmin());
			
			/* Take ordererDetails to create object of orderers */
			List<Orderer> orderers = HLConfigHelper.getOrderers(thisOrg.getOrderer(), client, config);
			
			/* Take peerDetails to create object of peers */
			List<Peer> peers = HLConfigHelper.getPeers(thisOrg.getPeer(), client, config);
			
			/*Take eventHubDetails to create object of eventHubs*/
			List<EventHub> eventHubs = HLConfigHelper.getEventHubs(thisOrg.getEventHub(), thisOrg.getEventHubNames(), client, config);
			
			//////////////////////////////////////////////////////////////////////////////////////////////////
			/* Construct Channel by org GTN*/
			
			Channel ch1 = HyperledgerTestAPI.constructChannelwithOneOrg("ch1", client, thisOrg.getPeerAdmin(), orderers, peers,
					eventHubs, channelDetails.getTransactionFilePath());

			/* ChainCode Configuration */
			ChaincodeID chaincodeID = ChaincodeID.newBuilder().setName(CHAIN_CODE_NAME).setVersion(CHAIN_CODE_VERSION)
					.setPath(CHAIN_CODE_PATH).build();
			
			/* Install Chaincode on peers of GTN*/
			peers = HLConfigHelper.getPeers(thisOrg.getPeer(), client, config);
			HyperledgerAPI.installChaincode(client, thisOrg.getPeerAdmin(), peers, chaincodeID);
			
			
			/////////////////////////////////////////////////////////////////////////////////////////////////
			//Then bring Elemica and DnB onto channel
			//
			
			/* Add peers onto channel and install Chaincode */
			List<Org> newChannelOrgs = Arrays.asList(config.getOrgDetailsByName(ORG_NAME_ELEMICA), config.getOrgDetailsByName(ORG_NAME_DNB));
			for (Org org : newChannelOrgs) {
				orderers = HLConfigHelper.getOrderers(org.getOrderer(), client, config);
				peers = HLConfigHelper.getPeers(org.getPeer(), client, config);
				eventHubs = HLConfigHelper.getEventHubs(org.getEventHub(), org.getEventHubNames(), client, config);
				
				HyperledgerTestAPI.addPeersOntoChannel(ch1, client, org.getPeerAdmin(), orderers, peers, eventHubs);
				HyperledgerAPI.installChaincode(client, org.getPeerAdmin(), peers, chaincodeID);
				//runChannelhelper.addPeersFromOrgOntoChannel(ch1, org.getPeer(), org.getPeerAdmin(), chaincodeID, client, config);
			
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
//							List<Peer> orgPeers = HLConfigHelper.getPeers(config.getOrgDetailsByName(ORG_NAME_DNB).getPeer(), client, config);
							payload = HyperledgerAPI.query(config.getOrgDetailsByName(ORG_NAME_DNB).getUserByName(TESTUSER_1_NAME),
									client, chaincodeID, ch1, sampleKey);
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
