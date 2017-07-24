package org.non.api.code;

import static java.lang.String.format;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.ChannelConfiguration;
import org.hyperledger.fabric.sdk.EventHub;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.Orderer;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.hyperledger.fabric.sdk.exception.TransactionException;
import org.non.config.HLConfiguration;
import org.non.config.Organization;



public class HyperledgerTestAPI {
	private static Logger logger = LogManager.getLogger(HyperledgerAPI.class);
	
	/* Construct the channel with a list of orgs */
	public static Channel constructChannel(String name, HFClient client, List<Organization> orgs, Orderer orderer,
			String channelConfigFilePath, HLConfiguration config) throws IOException, InvalidArgumentException, TransactionException, ProposalException {
		// Can change to take a list of orderers

		logger.info("Constructing channel " + name);

		// Take the only orderer in our case to create the channel.
		Collection<Orderer> orderers = new LinkedList<>();
		orderers.add(orderer);

		ChannelConfiguration channelConfiguration = new ChannelConfiguration(new File(channelConfigFilePath));

		boolean firstOrgOnboard = true;
		Channel newChannel = null;

		for (Organization thisOrg : orgs) {
			client.setUserContext(thisOrg.getPeerAdmin());
			if (firstOrgOnboard) {
				newChannel = client.newChannel(name, orderer, channelConfiguration,
						client.getChannelConfigurationSignature(channelConfiguration, thisOrg.getPeerAdmin()));
				logger.info("Created channel %s " + name);
				firstOrgOnboard = false;
			}

			/* Add peers from orgs onto channel */
			List<Peer> peers = thisOrg.getPeers();
			for (Peer peer : peers) {
				newChannel.joinPeer(peer);
			//	logger.info("Peer " + peerName + " joined channel " + name);
				// thisOrg.addPeer(peer);
			}

			/* Add EventHubs from orgs onto channel */

			List<EventHub> eventHubs = thisOrg.getEventHub();
			for (EventHub eventHub : eventHubs) {
				newChannel.addEventHub(eventHub);
			}

			/* Parse Configblock, Load CAcerts, Connect EventHubs */
			newChannel.initialize();
		}

		// Add remaining orderers onto newChannel before return if any.

		logger.info("Finished initialization channel " + name);

		return newChannel;
	}
	
	/*Construct the channel with one org by passing it's peerAdmin, orderers, peers and eventHubs*/
	 public static Channel constructChannelwithOneOrg(String name, HFClient client, User peerAdmin, 
			 List<Orderer> orderers, List<Peer> peersFromOrg, List<EventHub> eventHubsFromOrg, 
			 String channelConfigFilePath) throws Exception {

	        logger.info("Constructing channel " + name);


	        //Only peer Admin org
	        client.setUserContext(peerAdmin);

	        //Just pick the first orderer in the list to create the channel.

	        Orderer anOrderer = orderers.iterator().next();
	        orderers.remove(anOrderer);

	        ChannelConfiguration channelConfiguration = new ChannelConfiguration(new File(channelConfigFilePath));

	        //Create channel that has only one signer that is this orgs peer admin. If channel creation policy needed more signature they would need to be added too.
	        Channel newChannel = client.newChannel(name, anOrderer, channelConfiguration, client.getChannelConfigurationSignature(channelConfiguration, peerAdmin));

	        logger.info("Created channel " + name);

	        for (Peer peer : peersFromOrg) {
	            newChannel.joinPeer(peer);
	            logger.info("Peer " + peer.getName() + " joined channel " + name);
	        }

	        for (Orderer orderer : orderers) { //add remaining orderers if any.
	            newChannel.addOrderer(orderer);
	            logger.info("Orderer " + orderer.getName() + " joined channel " + name);
	        }

	        for (EventHub eventHub : eventHubsFromOrg) {
	            newChannel.addEventHub(eventHub);
	            logger.info("EventHub " + eventHub.getName() +" joined channel " + name);
	        }

	        newChannel.initialize();
	        logger.info("Finished initialization channel " + name);

	        return newChannel;

	    }
	 
	 public static void addPeersOntoChannel(Channel channel, HFClient client, User peerAdmin, 
			 List<Orderer> orderers, List<Peer> peersFromOrg, List<EventHub> eventHubsFromOrg) throws Exception {
		 
		 	client.setUserContext(peerAdmin);
		 	logger.info("Adding peers onto channel" + channel.getName());

	        for (Peer peer : peersFromOrg) {
	            channel.joinPeer(peer);
	            logger.info("Peer " +  peer.getName() + " joined channel " + channel.getName());
	            
	        }

//	        for (Orderer orderer : orderers) {
//	            channel.addOrderer(orderer);
//	            out("Orderer %s joined channel %s", orderer.getName(), channel.getName());
//	        }

//	        for (EventHub eventHub : eventHubsFromOrg) {
//	            channel.addEventHub(eventHub);
//	            out("EventHub %s joined channel %s", eventHub.getName(), channel.getName());
//	        }

	        
	        logger.info("Finished adding peers onto channel " + channel.getName());

		 
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
