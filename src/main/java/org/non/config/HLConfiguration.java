package org.non.config;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.hyperledger.fabric_ca.sdk.HFCAClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

/*
 * This class provides initial configuration and helper functions. 
 */

public class HLConfiguration {

	private NetworkConfig config; // class mapped to config_properties file
	private static Logger logger = LogManager.getLogger(HLConfiguration.class);

	/*
	 * The function loads all org configuration from networkConfig_properties.yaml file
	 * (using Jackson library) and set Org properties at startup
	 * 
	 * @param path - networkConfig_properties.yaml file location
	 */
	public void load(String path) {
		Path configProperitesFilePath = Paths.get(path);
		File file = new File(configProperitesFilePath.toString());
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
		try {
			config = mapper.readValue(file, NetworkConfig.class);
			setOrgProperties();
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

	/*
	 * The function set properties of each Org at startup
	 */
	private void setOrgProperties() throws MalformedURLException {
		for (Organization org : this.config.getOrgs()) {
			setOrgCaClientAndProperties(org);
		}
	}

	/*
	 * The function set Ca client and properties for Org
	 * 
	 * @param org - Org object
	 */
	private void setOrgCaClientAndProperties(Organization org) throws MalformedURLException {
		String cert = "crypto-config/peerOrganizations/DNAME/ca/ca.DNAME-cert.pem".replaceAll("DNAME", org.getDomain());
		File cf = new File(cert);
		if (!cf.exists() || !cf.isFile()) {
			throw new RuntimeException("TEST is missing cert file " + cf.getAbsolutePath());
		}
		Properties properties = new Properties();
		properties.setProperty("pemFile", cf.getAbsolutePath());
		properties.setProperty("allowAllHostNames", "true");// testing
															// environment only
															// NOT FOR
															// PRODUCTION!
		org.getCA().setCAProperties(properties);
		org.getCA().setCAClient(HFCAClient.createNewInstance(org.getCA().getLocation(), properties));
	}

	/*
	 * The function get list of all channels with details specified in
	 * networkConfig_properties file
	 */
	public List<ChannelDetails> getAllChannelsWithDetails() {
		return config.getChannelDetails();
	}

	/*
	 * The function get details (tx file path and orgs on channel) of a single
	 * channel
	 * 
	 * @param channelName - name of channel
	 */
	public ChannelDetails getChannelDetails(String channelName) {
		return getAllChannelsWithDetails().stream().filter(p -> p.getName().equals(channelName))
				.collect(Collectors.<ChannelDetails>toList()).get(0);
	}

	/*
	 * The function get all orgs on a channel as list of Org object
	 */
	public List<Organization> getOrgsOnChannel(String channelName) {
		ChannelDetails channelDetails = getChannelDetails(channelName);
		List<Organization> orgsOnChannel = new ArrayList<Organization>();
		for (String org : channelDetails.getOrgsOnChannel()) {
			orgsOnChannel.add(getOrgDetailsByName(org));
		}
		return orgsOnChannel;
	}

	/*
	 * This function return all orgs with details
	 */
	public List<Organization> getAllOrgsWithDetails() {
		return config.getOrgs();
	}

	/*
	 * This function is used to get org details by org name
	 * 
	 * @param orgName : name of org
	 */
	public Organization getOrgDetailsByName(String orgName) {
		return getAllOrgsWithDetails().stream().filter(p -> p.getName().equals(orgName))
				.collect(Collectors.<Organization>toList()).get(0);
	}

	/*
	 * This function is used to get list of users of a org with their details
	 * 
	 * @param orgName : name of org
	 */
	public List<NetworkUser> getUsersForOrg(String orgName) {
		return getOrgDetailsByName(orgName).getUsers();
	}

	/*
	 * This function is used to get list of peer details of a org
	 * 
	 * @param orgName : name of org
	 */
	public List<PeerDetails> getPeersByOrg(String orgName) {
		return getOrgDetailsByName(orgName).getPeer();
	}

	public Properties getPeerProperties(String name) {
		return getEndPointProperties("peer", name);
	}

	public Properties getOrdererProperties(String name) {
		return getEndPointProperties("orderer", name);
	}

	public Properties getEventHubProperties(String name) {
		return getEndPointProperties("peer", name); // uses same as named peer
	}

	/*
	 * This function is used to get end point properties including certificates
	 * and protocol of peers or orderer
	 * 
	 * @param type : peer or orderer
	 * 
	 * @param name : peer or orderer name
	 */
	private Properties getEndPointProperties(final String type, final String name) {
		final String domainName = getDomainName(name);
		File cert = Paths.get("crypto-config/ordererOrganizations".replace("orderer", type), domainName, type + "s",
				name, "tls/server.crt").toFile();
		if (!cert.exists()) {
			throw new RuntimeException(String.format("Missing cert file for: %s. Could not find at location: %s", name,
					cert.getAbsolutePath()));
		}
		Properties ret = new Properties();
		ret.setProperty("pemFile", cert.getAbsolutePath());
		ret.setProperty("hostnameOverride", name);
		ret.setProperty("sslProvider", "openSSL");
		ret.setProperty("negotiationType", "TLS");
		return ret;
	}

	private String getDomainName(final String name) {
		int dot = name.indexOf(".");
		if (-1 == dot) {
			return null;
		} else {
			return name.substring(dot + 1);
		}
	}

}
