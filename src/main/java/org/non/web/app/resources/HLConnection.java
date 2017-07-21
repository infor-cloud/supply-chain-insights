package org.non.web.app.resources;

import java.util.List;
//import java.util.stream.Collectors;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.hyperledger.fabric.sdk.ChaincodeID;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.Orderer;
//import org.hyperledger.fabric.sdk.Peer;
import org.non.api.code.HyperledgerAPI;
import org.non.api.code.HLConfigHelper;
import org.non.api.model.TradingPartner;
import org.non.config.ChannelDetails;
import org.non.config.HLConfiguration;
import org.non.config.Organization;

public class HLConnection {
	private String ORG_NAME_GTN = "GTN";
	protected static String TESTUSER_1_NAME = "User1";
	protected static String TEST_ADMIN_NAME = "admin";
	private String CHAIN_CODE_NAME = "myCC_go";
	private String CHAIN_CODE_PATH = "github.com/myCC";
	private String CHAIN_CODE_VERSION = "1";
	private ChaincodeID chainCodeID;
	private static HLConnection hlconnection;
	private HFClient client;
	private HLConfiguration config;
	private static Logger logger = LogManager.getLogger(HLConnection.class);

	// private String ORG_NAME_ELEMICA = "Elemica";
	// private String ORG_NAME_DNB = "Dun&BradStreet";
		

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

	private HLConnection() {
		client = HLConfigHelper.getHyperledgerFabricClient();
		config = HLConfigHelper.getHyperledgerFabricConfig();
		ChannelDetails channelDetails = config.getChannelDetails("ch1");
		List<Organization> channelorgs = config.getOrgsOnChannel("ch1");
		String channelTxFilePath = channelDetails.getTransactionFilePath();
		chainCodeID = ChaincodeID.newBuilder().setName(CHAIN_CODE_NAME).setVersion(CHAIN_CODE_VERSION)
				.setPath(CHAIN_CODE_PATH).build();

		try {
			/* Construct Channel */
			Organization thisOrg = config.getOrgDetailsByName(ORG_NAME_GTN);
			client.setUserContext(thisOrg.getPeerAdmin());
			List<Orderer> orderers = HLConfigHelper.getOrderers(thisOrg.getOrderer(), client, config);
			Channel ch1 = HyperledgerAPI.constructChannel("ch1", client, channelorgs, orderers.get(0),
					channelTxFilePath, config);

			/* ChainCode Configuration */
			ChaincodeID chaincodeID = ChaincodeID.newBuilder().setName(CHAIN_CODE_NAME).setVersion(CHAIN_CODE_VERSION)
					.setPath(CHAIN_CODE_PATH).build();

			/* Install Chaincode on peers */
			for (Organization org : channelorgs) {
				HyperledgerAPI.installChaincode(client, org.getPeerAdmin(),
						HLConfigHelper.getPeers(org.getPeer(), client, config), chaincodeID);
			}

			/* Initiate Chaincode on peers on the channel */
			HyperledgerAPI.initiateChaincode(client, chaincodeID, ch1, "scripts/chaincodeendorsementpolicy.yaml");

			logger.info("That's all!");

		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

	public static HLConnection getInstance() {
		if (hlconnection == null) {
			hlconnection = new HLConnection();
		}
		return hlconnection;
	}

	public String createPartner(String orgName, String userName, String channelName, TradingPartner tradingPartner)
			throws Exception {
		logger.info("Got Request to add a new Trading Partner");
		/* Get the channel specified from the UI */
		Channel ch = client.getChannel(channelName.toLowerCase());
		
		/* Convert the trading partner into something readable by the chaincode */
		String tradeString = tradingPartner.toJSONString();

		/* Make sure the information provided exists for this channel*/
		if (ch == null) {
			logger.info("No channel exists for channel name: %s" + channelName);
			return ("ERROR: No channel exists for channel name " + channelName);
		} else if (tradeString == null) {
			System.out.println("Error parsing the Trading Partner to a string");
			return ("ERROR: Improper input of params");
		}

		else {
			String args[] = { "add", tradingPartner.getName(), tradeString };
			
			/* Send transaction to HyperledgerAPI to properly format the proposal request*/
			HyperledgerAPI.invoke(args, config.getOrgDetailsByName(orgName).getUserByName(userName), client,
					chainCodeID, ch);
			return ("SUCCESS");

		}
	}

	public String getTradingPartner(String orgName, String userName, String channelName, String compName)
			throws Exception {
		logger.info("Got Request to Query a Trading Partner");
		
		/* Get the channel specified from the UI*/
		Channel ch = client.getChannel(channelName.toLowerCase());
		if (ch == null) {
			logger.info("No channel exist for channel name: %s" + channelName);
			return ("ERROR: No Channel Exists for: " + channelName);
		}

		else {
			logger.info("Chain found for name: %s" + channelName);

			// org.non.config.Org to be updated with storing a list of peers
//
//			List<Peer> orgPeers = ch.getPeers().stream()
//					.filter(p -> p.getUrl().contains(config.getOrgDetailsByName(orgName).getDomain()))
//					.collect(Collectors.toList());
//			// To be updated: pass the orgPeers instead of channel.getPeers()
			
			
			/* Send the channel info and name of the partner to find to HyperledgerAPI to format a proposal */
			String result = HyperledgerAPI.query(config.getOrgDetailsByName(orgName).getUserByName(userName), client,
					chainCodeID, ch, compName);
			if (result.isEmpty())
				return "ERROR: No trading partner exists for: " + compName;

			return result;
		}
	}


}
