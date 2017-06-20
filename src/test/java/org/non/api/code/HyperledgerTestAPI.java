package org.non.api.code;

import static java.lang.String.format;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.ChannelConfiguration;
import org.hyperledger.fabric.sdk.EventHub;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.Orderer;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.hyperledger.fabric.sdk.exception.TransactionException;
import org.non.config.HLConfiguration;
import org.non.config.Org;
import org.non.config.PeerDetails;



public class HyperledgerTestAPI {
	private static Logger logger = LogManager.getLogger(HyperledgerAPI.class);
	
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
	
	public static void showPeersOnChannel(Channel channel) {
		logger.info("Peers of channel " + channel.getName());
		out("Peers of channel " + channel.getName());
		Collection<Peer> peers = channel.getPeers();
		for (Peer p : peers) {
			logger.info(p.getName());
			out(p.getName());
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
