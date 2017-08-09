package org.non.web.app.resources;

import static java.lang.String.format;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.hyperledger.fabric.sdk.BlockEvent.TransactionEvent;
import org.hyperledger.fabric.sdk.exception.TransactionEventException;
import org.hyperledger.fabric.sdk.BlockEvent;
import org.hyperledger.fabric.sdk.ChaincodeID;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.Orderer;
import org.hyperledger.fabric.sdk.Peer;
import org.non.api.code.HLConfigHelper;
import org.non.api.code.HyperledgerAPI;
import org.non.api.model.Connection;
import org.non.api.code.NetworkBlockListener;
import org.non.api.model.TradingPartner;
import org.non.config.ChannelDetails;
import org.non.config.HLConfiguration;
import org.non.config.NetworkUser;
import org.non.config.Organization;

public class HLConnection {
	private String ORG_NAME_GTN = "GTN";
	protected static String TESTUSER_1_NAME = "User1";
	protected static String TEST_ADMIN_NAME = "admin";
	private String CHAIN_CODE_NAME = "myCC_go";
	private String CHAIN_CODE_PATH = "main/go/chaincode/myCC";
	private String CHAIN_CODE_VERSION = "1";
	private ChaincodeID chaincodeID;

	private String CONNECTION_CHAIN_CODE_NAME = "connectionCC_go";
	private String CONNECTION_CHAIN_CODE_PATH = "main/go/chaincode/connectionChaincode";
	private String CONNECTION_CHAIN_CODE_VERSION = "1";
	private ChaincodeID transactionChaincodeID;

	// private Block block;
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

		try {
			/* Construct Channel */
			Organization thisOrg = config.getOrgDetailsByName(ORG_NAME_GTN);
			client.setUserContext(thisOrg.getPeerAdmin());
			List<Orderer> orderers = thisOrg.getOrderer();
			Channel ch1 = HyperledgerAPI.constructChannel("ch1", client, channelorgs, orderers.get(0),
					channelTxFilePath, config);
			// block=ch1.queryBlockByNumber(0).getBlock();

			/* ChainCode Configuration */
			chaincodeID = ChaincodeID.newBuilder().setName(CHAIN_CODE_NAME).setVersion(CHAIN_CODE_VERSION)
					.setPath(CHAIN_CODE_PATH).build();

			transactionChaincodeID = ChaincodeID.newBuilder().setName(CONNECTION_CHAIN_CODE_NAME)
					.setVersion(CONNECTION_CHAIN_CODE_VERSION).setPath(CONNECTION_CHAIN_CODE_PATH).build();

			/* Install Chaincode on peers */
			for (Organization org : channelorgs) {
				HyperledgerAPI.installChaincode(client, org.getPeerAdmin(), org.getPeers(), chaincodeID);
				HyperledgerAPI.installChaincode(client, org.getPeerAdmin(), org.getPeers(), transactionChaincodeID);
			}

			/*Register a BlockListener on channel*/
			NetworkBlockListener listener = new NetworkBlockListener();
			ch1.registerBlockListener(listener);
			
			/* Initiate Chaincode on peers on the channel */
			HyperledgerAPI.initiateChaincode(client, transactionChaincodeID, ch1, "scripts/chaincodeendorsementpolicy.yaml");
			CompletableFuture<TransactionEvent> initFuture = HyperledgerAPI.initiateChaincode(client, chaincodeID, ch1,
					"scripts/chaincodeendorsementpolicy.yaml");
			int TRANSACTIONWAITTIME = 140000;

			initFuture.thenApply(transactionEvent -> {
				assertTrue(transactionEvent.isValid());

				try {
					// function here!!
					setUpOrgs();

				} catch (Exception e) {
					if (e instanceof TransactionEventException) {
						BlockEvent.TransactionEvent te = ((TransactionEventException) e).getTransactionEvent();
						if (te != null) {
							fail(format("Transaction with txid %s failed. %s", te.getTransactionID(), e.getMessage()));
						}
						fail(format("Test failed with %s exception %s", e.getClass().getName(), e.getMessage()));
					} else {
						e.printStackTrace();
						fail("Test failed with error : " + e.getMessage());
					}
				}
				return null;

			}).get(TRANSACTIONWAITTIME, TimeUnit.SECONDS);

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

  // public Block getBlock(){
	// return block;
	// }
	public String createConnection(String orgName, String userName, String channelName, Connection connect)
			throws Exception {
		logger.info("Adding a new connection!");
		Channel ch = client.getChannel(channelName.toLowerCase());

		String connectString = connect.toJSONString();
		if (ch == null) {
			logger.info("No channel exists for channel name: %s" + channelName);
			return ("ERROR: No channel exists for channel name " + channelName);
		} else if (connectString == null) {
			System.out.println("Error parsing the Trading Partner to a string");
			return ("ERROR: Improper input of params");
		} else {
			String args[] = { "add", connect.getComp1(), connect.getComp2(), connectString };

			HyperledgerAPI.invoke(args, config.getOrgDetailsByName(orgName).getUserByName(userName), client,
					transactionChaincodeID, ch);
			return ("SUCCESS");
		}

	}
	
	public String createPartner(String orgName, String userName, String channelName, TradingPartner tradingPartner, String functionName)
			throws Exception {
		logger.info("Got Request to add a new Trading Partner");
		/* Get the channel specified from the UI */
		Channel ch = client.getChannel(channelName.toLowerCase());

		/*
		 * Convert the trading partner into something readable by the chaincode
		 */
		String tradeString = tradingPartner.toJSONString();

		/* Make sure the information provided exists for this channel */
		if (ch == null) {
			logger.info("No channel exists for channel name: %s" + channelName);
			return ("ERROR: No channel exists for channel name " + channelName);
		} else if (tradeString == null) {
			System.out.println("Error parsing the Trading Partner to a string");
			return ("ERROR: Improper input of params");
		}

		else {
			String args[] = { "add", tradingPartner.getName(), tradeString , functionName };

			/*
			 * Send transaction to HyperledgerAPI to properly format the
			 * proposal request
			 */
			try{
			HyperledgerAPI.invoke(args, config.getOrgDetailsByName(orgName).getUserByName(userName), client,
					chaincodeID, ch);
			}
			catch(Exception e){
				System.out.println(e.getMessage());
			}

			return ("SUCCESS");

		}
	}

	public String getTradingPartner(String orgName, String userName, String channelName, String compName)
			throws Exception {
		logger.info("Got Request to Query a Trading Partner");

		/* Get the channel specified from the UI */
		Channel ch = client.getChannel(channelName.toLowerCase());
		if (ch == null) {
			logger.info("No channel exist for channel name: %s" + channelName);
			return ("ERROR: No Channel Exists for: " + channelName);
		}

		else {
			logger.info("Chain found for name: %s" + channelName);
			
			String args[] = { "query", compName };

			// org.non.config.Org to be updated with storing a list of peers
			List<Peer> orgPeers = config.getOrgDetailsByName(orgName).getPeers();
			String result = HyperledgerAPI.query(args, config.getOrgDetailsByName(orgName).getUserByName(userName),
					client, chaincodeID, ch, orgPeers);
			logger.info("Result: " + result);

			if (result.isEmpty())
				return "ERROR: No trading partner exists for: " + compName;

			return result;
		}
	}

	public String queryVerified(String orgName, String userName, String channelName) throws Exception {
		String args[] = { "queryVerified", "Unverified" };
		Channel ch = client.getChannel(channelName);
		List<Peer> orgPeers = config.getOrgDetailsByName(orgName).getPeers();
		System.out.println("OrgName: " + orgName + " UserName: " + userName);
		String result = HyperledgerAPI.query(args, config.getOrgDetailsByName(orgName).getUserByName(userName), client, chaincodeID,
				ch, orgPeers);
		return result;
	}

	public String queryConnection(String orgName, String userName, String channelName, String compName)
			throws Exception {
		String args[] = { "query", compName };
		Channel ch = client.getChannel(channelName);
		NetworkUser user = config.getOrgDetailsByName(orgName).getUserByName(userName);
		List<Peer> orgPeers = config.getOrgDetailsByName(orgName).getPeers();
		String result = HyperledgerAPI.query(args, user, client,transactionChaincodeID, ch,orgPeers);
		
		System.out.println(result);
		if (result.isEmpty()){
			return "No connection found";
		}
		return result;
	}
	
	public String queryByRange(String orgName, String userName, String channelName)
			throws Exception {
		String args[] = { "queryByRange", "0" , "z"};
		Channel ch = client.getChannel(channelName);
		NetworkUser user = config.getOrgDetailsByName(orgName).getUserByName(userName);
		List<Peer> orgPeers = config.getOrgDetailsByName(orgName).getPeers();
		String result = HyperledgerAPI.query(args, user, client,chaincodeID, ch, orgPeers);
		
		System.out.println("RANGE QUERY RESULT: " + result);
		if (result.isEmpty()){
			return "No connection found";
		}
		return result;
	}
	
	public String queryHistory(String orgName, String userName, String channelName, String compName)
			throws Exception {
		logger.info("Got Request to Query the History of a Trading Partner");

		Channel ch = client.getChannel(channelName.toLowerCase());
		if (ch == null) {
			logger.info("No channel exist for channel name: %s" + channelName);
			return ("ERROR: No Channel Exists for: " + channelName);
		}

		else {
			logger.info("Chain found for name: %s" + channelName);
			List<Peer> orgPeers = config.getOrgDetailsByName(orgName).getPeers();			
			String result = HyperledgerAPI.query(new String[] { "getHistory", compName }, config.getOrgDetailsByName(orgName).getUserByName(userName), client,
					chaincodeID, ch, orgPeers);
			if (result.isEmpty())
				return "ERROR: No trading partner exists for: " + compName;

			return result;
		}
	}

	public void setUpOrgs() throws Exception {
		List<NetworkUser> gtnUsers = config.getUsersForOrg("GTN");
		List<NetworkUser> elemicaUsers = config.getUsersForOrg("Elemica");
		List<NetworkUser> dnbUsers = config.getUsersForOrg("Dun&BradStreet");
		Channel ch1 = client.getChannel("ch1");

		for (NetworkUser user : gtnUsers) {
			String args[] = { "addMember", "ntp" };
			HyperledgerAPI.invoke(args, user, client, chaincodeID, ch1);
			System.out.println(user.toString());
		}
		for (NetworkUser user : elemicaUsers) {
			String args[] = { "addMember", "ntp" };
			HyperledgerAPI.invoke(args, user, client, chaincodeID, ch1);
			System.out.println(user.toString());
		}
		for (NetworkUser user : dnbUsers) {
			String args[] = { "addMember", "oracle" };
			HyperledgerAPI.invoke(args, user, client, chaincodeID, ch1);
			System.out.println(user.toString());
		}
	}

}