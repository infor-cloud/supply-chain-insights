package org.gtnexus.blockchain.channel;

import static java.lang.String.format;
import static org.junit.Assert.*;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.hyperledger.fabric.sdk.BlockEvent;
import org.hyperledger.fabric.sdk.BlockEvent.TransactionEvent;
import org.hyperledger.fabric.sdk.ChaincodeID;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.Orderer;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.TransactionEventException;
import org.junit.Ignore;
//import org.hyperledger.fabric.sdk.helper.Channel;
import org.junit.Test;
//import org.non.BaseTestCase;
import org.non.api.code.HyperledgerAPI;
import org.non.api.code.HyperledgerTestAPI;
import org.non.config.ChannelDetails;
import org.non.config.HLConfiguration;
import org.non.config.OrdererDetails;
import org.non.config.Organization;
import org.non.api.code.HLConfigHelper;

public class ChannelUtilTest{

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
		List<Organization> channelorgs = config.getOrgsOnChannel("ch1");

		try {
			Organization thisOrg = channelorgs.get(0);
			List<OrdererDetails> ordererDetails = thisOrg.getOrderer();
			client.setUserContext(thisOrg.getPeerAdmin());
			Orderer thisOrderer = client.newOrderer(ordererDetails.get(0).getName(),
					ordererDetails.get(0).getLocation(), config.getOrdererProperties(ordererDetails.get(0).getName()));

			/* Construct Channel */
			Channel ch1 = HyperledgerTestAPI.constructChannel("ch1", client, channelorgs, thisOrderer,
					channelDetails.getTransactionFilePath(), config);

			/* ChainCode Configuration */
			ChaincodeID chaincodeID = ChaincodeID.newBuilder().setName(CHAIN_CODE_NAME).setVersion(CHAIN_CODE_VERSION)
					.setPath(CHAIN_CODE_PATH).build();

			/* Install Chaincode */
			for (Organization org : channelorgs) {
				HyperledgerAPI.installChaincode(client, org.getPeerAdmin(), HLConfigHelper.getPeers(org.getPeer(), client, config), chaincodeID);
			}

			/* Run the test for knowing how to use the API. */

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
	
	@Test
    public void createNoPeerChannel(){
        HFClient client = HLConfigHelper.getHyperledgerFabricClient();
        try {
            Channel channel = client.newChannel("No_Peer_Channel");
            Collection<Peer> peers = channel.getPeers();
            assertEquals(peers.size(), 0);
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        }
    }

    @Test @Ignore
    public void createChannelWithDynamicPeer(){
        HFClient client = HLConfigHelper.getHyperledgerFabricClient();
        try {
            Channel channel = client.newChannel("Dynamic_Channel");
            Collection<Peer> peers = channel.getPeers();
            assertEquals(peers.size(), 0);
            
            
        } catch (InvalidArgumentException e) {
           
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
