package org.non.api.code;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.hyperledger.fabric.sdk.BlockEvent.TransactionEvent;
import org.hyperledger.fabric.sdk.ChaincodeEndorsementPolicy;
import org.hyperledger.fabric.sdk.ChaincodeID;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.ChannelConfiguration;
import org.hyperledger.fabric.sdk.EventHub;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.InstallProposalRequest;
import org.hyperledger.fabric.sdk.InstantiateProposalRequest;
import org.hyperledger.fabric.sdk.Orderer;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.ProposalResponse;
import org.hyperledger.fabric.sdk.QueryByChaincodeRequest;
import org.hyperledger.fabric.sdk.TransactionProposalRequest;
import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric.sdk.exception.ChaincodeEndorsementPolicyParseException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.hyperledger.fabric.sdk.exception.TransactionException;
import org.non.config.HLConfiguration;
import org.non.config.Org;
import org.non.config.PeerDetails;


/*A class of higher level function calls using Hyperledger Java API.*/
public class HyperledgerAPI {
	private static Logger logger = LogManager.getLogger(HyperledgerAPI.class);

	private static int TRANSACTIONWAITTIME = 150000;
	private static int PROPOSALWAITTIME = 150000;
	private static int DEPLOYWAITTIME = 140000;

	/* Construct the channel with a list of orgs */
	public static Channel constructChannel(String name, HFClient client, List<Org> orgs, Orderer orderer,
			String channelConfigFilePath, HLConfiguration config) throws IOException, InvalidArgumentException, TransactionException, ProposalException {
		// Can change to take a list of orderers

		logger.info("Constructing channel " + name);

		// Take the only orderer in our case to create the channel.
		Collection<Orderer> orderers = new LinkedList<>();
		orderers.add(orderer);

		ChannelConfiguration channelConfiguration = new ChannelConfiguration(new File(channelConfigFilePath));

		boolean firstOrgOnboard = true;
		Channel newChannel = null;

		for (Org thisOrg : orgs) {
			client.setUserContext(thisOrg.getPeerAdmin());
			if (firstOrgOnboard) {
				newChannel = client.newChannel(name, orderer, channelConfiguration,
						client.getChannelConfigurationSignature(channelConfiguration, thisOrg.getPeerAdmin()));
				logger.info("Created channel %s " + name);
				firstOrgOnboard = false;
			}

			/* Add peers from orgs onto channel */
			List<PeerDetails> peerDetails = thisOrg.getPeer();
			for (PeerDetails thisPeerDetail : peerDetails) {
				String peerName = thisPeerDetail.getName();
				String peerLocation = thisPeerDetail.getLocation();

				Properties peerProperties = config.getPeerProperties(peerName);
				if (peerProperties == null) {
					peerProperties = new Properties();
				}
				// Example of setting specific options on grpc's
				// ManagedChannelBuilder
				peerProperties.put("grpc.ManagedChannelBuilderOption.maxInboundMessageSize", 9000000);

				Peer peer = client.newPeer(peerName, peerLocation, peerProperties);
				newChannel.joinPeer(peer);
				logger.info("Peer " + peerName + " joined channel " + name);
				// thisOrg.addPeer(peer);
			}

			/* Add EventHubs from orgs onto channel */

			Map<String, String> eventHubDetails = thisOrg.getEventHub();
			for (String eventHubName : thisOrg.getEventHubNames()) {
				EventHub eventHub = client.newEventHub(eventHubName, eventHubDetails.get(eventHubName),
						config.getEventHubProperties(eventHubName));
				newChannel.addEventHub(eventHub);
			}

			/* Parse Configblock, Load CAcerts, Connect EventHubs */
			newChannel.initialize();
		}

		// Add remaining orderers onto newChannel before return if any.

		logger.info("Finished initialization channel " + name);

		return newChannel;
	}
	

	/* Install the chaincode after constructing the channel and before running the channel */
	public static void installChaincode(HFClient client, User peerAdmin, List<Peer> peerList, ChaincodeID chaincodeID) 
			throws InvalidArgumentException, IOException, ProposalException {
		Collection<ProposalResponse> responses;
		Collection<ProposalResponse> successful = new LinkedList<>();
		Collection<ProposalResponse> failed = new LinkedList<>();
		////////////////////////////
		// Install Proposal Request
		//

		client.setUserContext(peerAdmin);

		logger.info("Creating install proposal");

		InstallProposalRequest installProposalRequest = client.newInstallProposalRequest();
		installProposalRequest.setChaincodeID(chaincodeID);
		installProposalRequest.setChaincodeSourceLocation(new File("scripts/gocc/sample1"));
		installProposalRequest.setChaincodeVersion("1");// CHAIN_CODE_VERSION

		logger.info("Sending install proposal");

		////////////////////////////
		// only a client from the same org as the peer can issue an install
		////////////////////////////
		int numInstallProposal = 0;

		Set<Peer> peersFromOrg = new HashSet<Peer>(peerList);
		
		numInstallProposal = numInstallProposal + peersFromOrg.size();
		responses = client.sendInstallProposal(installProposalRequest, peersFromOrg);

		for (ProposalResponse response : responses) {
			if (response.getStatus() == ProposalResponse.Status.SUCCESS) {
				logger.info("Successful install proposal response Txid: " + response.getTransactionID() + " from peer "
						+ response.getPeer().getName());
				successful.add(response);
			} else {
				failed.add(response);
			}
		}

		logger.info("Received " + numInstallProposal + " install proposal responses. Successful+verified: "
				+ successful.size() + ". Failed: " + failed.size());
		if (failed.size() > 0) {
			ProposalResponse first = failed.iterator().next();
			throw new RuntimeException(
					"Not enough endorsers for install :" + successful.size() + ".  " + first.getMessage());
		}
	}

	/* Initiate the chaincode of peers on channel */
	public static CompletableFuture<TransactionEvent> initiateChaincode(HFClient client, ChaincodeID chaincodeID,
			Channel channel, String chaincodeEndorsementPolicyFilePath)
			throws ChaincodeEndorsementPolicyParseException, IOException, InvalidArgumentException, ProposalException {
		//////////////////////
		/// Instantiate chain code.
		///
		logger.info("Running Channel " + channel.getName());
		channel.setTransactionWaitTime(TRANSACTIONWAITTIME);
		channel.setDeployWaitTime(DEPLOYWAITTIME);
		InstantiateProposalRequest instantiateProposalRequest = client.newInstantiationProposalRequest();
		ChaincodeEndorsementPolicy chaincodeEndorsementPolicy;
		Collection<ProposalResponse> successful = new LinkedList<>();
		Collection<ProposalResponse> failed = new LinkedList<>();
		Collection<ProposalResponse> responses = null;

		instantiateProposalRequest.setProposalWaitTime(PROPOSALWAITTIME);
		instantiateProposalRequest.setChaincodeID(chaincodeID);
		instantiateProposalRequest.setFcn("init");
		instantiateProposalRequest.setArgs(new String[] {});
		Map<String, byte[]> tm = new HashMap<>();
		tm.put("HyperLedgerFabric", "InstantiateProposalRequest:JavaSDK".getBytes(UTF_8));
		tm.put("method", "InstantiateProposalRequest".getBytes(UTF_8));
		instantiateProposalRequest.setTransientMap(tm);

		chaincodeEndorsementPolicy = new ChaincodeEndorsementPolicy();
		chaincodeEndorsementPolicy.fromYamlFile(new File(chaincodeEndorsementPolicyFilePath));
		instantiateProposalRequest.setChaincodeEndorsementPolicy(chaincodeEndorsementPolicy);

		logger.info("Sending instantiateProposalRequest to all peers.");
		successful.clear();
		failed.clear();

		responses = channel.sendInstantiationProposal(instantiateProposalRequest, channel.getPeers());
		for (ProposalResponse response : responses) {
			if (response.isVerified() && response.getStatus() == ProposalResponse.Status.SUCCESS) {
				successful.add(response);
				logger.info("Succesful instantiate proposal response Txid: " + response.getTransactionID()
						+ " from peer " + response.getPeer().getName());
			} else {
				failed.add(response);
			}
		}
		logger.info("Received " + responses.size() + " instantiate proposal responses. Successful+verified: "
				+ successful.size() + " . Failed: " + failed.size());
		if (failed.size() > 0) {
			ProposalResponse first = failed.iterator().next();
			throw new RuntimeException("Not enough endorsers for instantiate :" + successful.size()
					+ "endorser failed with " + first.getMessage() + ". Was verified:" + first.isVerified());
		}
		
		Collection<Orderer> orderers = channel.getOrderers();
		logger.info("Sending instantiateTransaction to orderer.");
		return channel.sendTransaction(successful, orderers);
	}

	/*
	 * Send invoke to chaincode with args. Functions such as "add", "delete"...
	 */
	public static CompletableFuture<TransactionEvent> invoke(String[] args, User user, HFClient client,
			ChaincodeID chaincodeID, Channel channel) throws InvalidArgumentException, ProposalException {
		Collection<ProposalResponse> successful = new LinkedList<>();
		Collection<ProposalResponse> failed = new LinkedList<>();

		successful.clear();
		failed.clear();
		client.setUserContext(user);

		///////////////
		/// Send transaction proposal to all peers
		logger.info("Running Invoke.");
		TransactionProposalRequest transactionProposalRequest = client.newTransactionProposalRequest();
		transactionProposalRequest.setChaincodeID(chaincodeID);
		transactionProposalRequest.setFcn("invoke");
		transactionProposalRequest.setArgs(args);

		Map<String, byte[]> tm2 = new HashMap<>();
		tm2.put("HyperLedgerFabric", "TransactionProposalRequest:JavaSDK".getBytes(UTF_8));
		tm2.put("method", "TransactionProposalRequest".getBytes(UTF_8));
		tm2.put("result", ":)".getBytes(UTF_8)); // This should be returned see
													// chaincode.

		transactionProposalRequest.setTransientMap(tm2);

		logger.info("sending transactionProposal to all peers with arguments " + args[0] + " " + args[1]);
		
		/*
		 * We have the assumption that all the peers on this channel have to endorse the transaction proposal
		 * for calling invoke methods which may modify the state of world. 
		 */
		Collection<ProposalResponse> transactionPropResp = channel.sendTransactionProposal(transactionProposalRequest,
				channel.getPeers());
		for (ProposalResponse response : transactionPropResp) {
			if (response.getStatus() == ProposalResponse.Status.SUCCESS) {
				logger.info("Successful transaction proposal response Txid: " + response.getTransactionID()
						+ " from peer " + response.getPeer().getName());
				successful.add(response);
			} else {
				failed.add(response);
			}
		}
		logger.info("Received " + transactionPropResp.size() + " transaction proposal responses. Successful+verified: "
				+ successful.size() + ". Failed: " + failed.size());
		if (failed.size() > 0) {
			ProposalResponse firstTransactionProposalResponse = failed.iterator().next();
			new RuntimeException("Not enough endorsers for invoke(add key,value):" + failed.size() + " endorser error: "
					+ firstTransactionProposalResponse.getMessage() + ". Was verified: "
					+ firstTransactionProposalResponse.isVerified());
		}
		logger.info("Successfully received transaction proposal responses.");

		logger.info("Sending chaincode transaction(" + args[0] + " " + args[1] + ") to orderer.");
		return channel.sendTransaction(successful);
	}

	/*
	 * Send query proposal to all peers. Must provide key of data
	 * instance(trading partner)
	 */
	public static String query(User user, HFClient client, ChaincodeID chaincodeID,Channel channel,
			String key) throws ProposalException, InvalidArgumentException {
		Set<String> payloadSet = new HashSet<>();

		////////////////////////////
		// Send Query Proposal to all peers
		//
		client.setUserContext(user);
		logger.info("Query chaincode for the value of " + key);
		QueryByChaincodeRequest queryByChaincodeRequest = client.newQueryProposalRequest();
		queryByChaincodeRequest.setArgs(new String[] { "query", key });
		queryByChaincodeRequest.setFcn("invoke");
		queryByChaincodeRequest.setChaincodeID(chaincodeID);

		Map<String, byte[]> tm2 = new HashMap<>();
		tm2.put("HyperLedgerFabric", "QueryByChaincodeRequest:JavaSDK".getBytes(UTF_8));
		tm2.put("method", "QueryByChaincodeRequest".getBytes(UTF_8));
		queryByChaincodeRequest.setTransientMap(tm2);


		/*
		 * We assume that we only need to ask the peers of this user's org
		 * for querying a piece of data. But we keep the codes sending the proposal to
		 * all the peers on channel for now before figuring out more details about it.
		 */
		Collection<ProposalResponse> queryProposals = channel.queryByChaincode(queryByChaincodeRequest,
				channel.getPeers());

		for (ProposalResponse proposalResponse : queryProposals) {
			if (!proposalResponse.isVerified() || proposalResponse.getStatus() != ProposalResponse.Status.SUCCESS) {
				throw new RuntimeException("Failed query proposal from peer " + proposalResponse.getPeer().getName()
						+ " status: " + proposalResponse.getStatus() + ". Messages: " + proposalResponse.getMessage()
						+ ". Was verified : " + proposalResponse.isVerified());
			} else {
				String payload = proposalResponse.getProposalResponse().getResponse().getPayload().toStringUtf8();
				payloadSet.add(payload);
				logger.info("[TEST] query got payload: " + payload);
				// proposalResponse.getProposalResponse().getResponse().getPayload().toByteArray();
			}
		}

		if (payloadSet.size() != 1) {
			throw new RuntimeException("Payloads contain inconsistent data");
		} else {
			return payloadSet.iterator().next();
		}
	}

	
}
