package org.non.web.app.resources;

import static java.lang.String.format;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.hyperledger.fabric.sdk.ChaincodeID;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.EventHub;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.Orderer;
import org.hyperledger.fabric.sdk.Peer;
import org.non.api.code.HyperledgerAPI;
import org.non.api.code.HLConfigHelper;
import org.non.api.model.TradingPartner;
import org.non.config.ChannelDetails;
import org.non.config.HLConfiguration;
import org.non.config.Org;

public class HLConnection {
	private String ORG_NAME_GTN = "GTN";
	private String ORG_NAME_ELEMICA = "Elemica";
	private String ORG_NAME_DNB = "Dun&BradStreet";
	protected static String TESTUSER_1_NAME = "User1";
	protected static String TEST_ADMIN_NAME = "admin";
	
	private String CHAIN_CODE_NAME = "myCC_go";
	private String CHAIN_CODE_PATH = "github.com/myCC";
	private String CHAIN_CODE_VERSION = "1";
	private ChaincodeID chainCodeID;

	
	private static HLConnection hlconnection;
	//private Map<String, Channel> channelMap = new HashMap<String, Channel>();
	private HFClient client;
	private HLConfiguration config;
	
	public HFClient getClient() {
		return client;
	}

	public void setClient(HFClient client) {
		this.client = client;
	}

	public HLConfiguration getConfig() {
		return config;
	}

	public void setConfig(HLConfiguration config) {
		this.config = config;
	}

	private HLConnection(){
		client = HLConfigHelper.getHyperledgerFabricClient();
		config = HLConfigHelper.getHyperledgerFabricConfig();
		ChannelDetails channelDetails = config.getChannelDetails("ch1");
		String channelTxFilePath = channelDetails.getTransactionFilePath();
		chainCodeID = ChaincodeID.newBuilder().setName(CHAIN_CODE_NAME).setVersion(CHAIN_CODE_VERSION)
				.setPath(CHAIN_CODE_PATH).build();
		
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
			
			Channel ch1 = HyperledgerAPI.constructChannelwithOneOrg("ch1", client, thisOrg.getPeerAdmin(), orderers, peers,
					eventHubs, channelTxFilePath);
			

			ChaincodeID chaincodeID = ChaincodeID.newBuilder().setName(CHAIN_CODE_NAME).setVersion(CHAIN_CODE_VERSION)
					.setPath(CHAIN_CODE_PATH).build();
			
			/* Install Chaincode on peers of GTN*/
			peers = HLConfigHelper.getPeers(thisOrg.getPeer(), client, config);
			HyperledgerAPI.installChaincode(client, thisOrg.getPeerAdmin(), peers, chaincodeID);
			HyperledgerAPI.initiateChaincode(client, chaincodeID,
					ch1, "scripts/chaincodeendorsementpolicy.yaml");

			
			out("That's all!");

		} catch (Exception e) {
			e.printStackTrace();

			fail(e.getMessage());
		}
	}
	
	public static HLConnection getInstance() {
        if(hlconnection == null){
            hlconnection = new HLConnection();
        }
        return hlconnection;
    }

	public String createPartner(String orgName, String userName, String channelName, TradingPartner tradingPartner)
			throws Exception {
		Channel ch =  client.getChannel(channelName.toLowerCase());
		String tradeString = tradingPartner.toJSONString();

		if (ch == null) {
			out("No channel exists for channel name: %s", channelName);
			return ("ERROR: No channel exists for channel name " + channelName);
		} else if (tradeString == null) {
			System.out.println("Error parsing the Trading Partner to a string");
			return ("ERROR: Improper input of params");
		}

		else {
			String args[] = { "add", tradingPartner.getName(), tradeString };
			HyperledgerAPI.invoke(args, config.getOrgDetailsByName(orgName).getUserByName(userName), client, chainCodeID, ch);
			return ("SUCCESS");
			
		}
	}

	public String getTradingPartner(String orgName, String userName, String channelName, String compName)
			throws Exception {
		out("Got Request to Query a trading Partner");
		Channel ch = client.getChannel(channelName.toLowerCase());
		if (ch == null) {
			out("No channel exist for channel name: %s", channelName);
			return ("ERROR: No Channel Exists for: " + channelName);
		}

		else {
			out("Chain found for name: %s", channelName);
			
			String result = HyperledgerAPI.query(config.getOrgDetailsByName(orgName).getUserByName(userName), client, chainCodeID, ch, compName);
			if (result.isEmpty())
				return "ERROR: No trading partner exists for: " + compName;

			return result;
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
