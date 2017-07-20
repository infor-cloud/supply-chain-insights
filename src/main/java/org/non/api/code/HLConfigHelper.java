package org.non.api.code;

import static java.lang.String.format;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.EventHub;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.Orderer;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.hyperledger.fabric_ca.sdk.RegistrationRequest;
import org.non.config.HLConfiguration;
import org.non.config.NetworkUser;
import org.non.config.OrdererDetails;
import org.non.config.Organization;
import org.non.config.PeerDetails;

/*This class is for reading the information of orgs and channel to do the configuration*/
public class HLConfigHelper {
	protected static HFClient client;
	protected static HLConfiguration config;
	private final static String CONFIG_PROPERTIES_FILE_PATH = "scripts/config/networkConfig_properties.yml";
	protected static String TESTUSER_1_NAME = "User1";
	protected static String TEST_ADMIN_NAME = "admin";
	private static Logger logger = LogManager.getLogger(HLConfigHelper.class);


	static {
		buildRegistry();
	}
	
	private static void buildRegistry() {
		client = HFClient.createNewInstance();
		config = new HLConfiguration();

		try {
			config.load(CONFIG_PROPERTIES_FILE_PATH);
			client.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());

			/* Load all the configuration for each org. */
			for (Organization thisOrg : config.getAllOrgsWithDetails()) {

				HFCAClient ca = thisOrg.getCA().getCAClient();
				final String mspid = thisOrg.getMsp().getId();
				ca.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());

				NetworkUser admin = thisOrg.getUserByName(TEST_ADMIN_NAME);
				if (!admin.isEnrolled()) { // *Preregistered admin only needs to
											// be enrolled with Fabric caClient.
					admin.setEnrollment(ca.enroll(admin.getName(), "adminpw"));
					admin.setMPSID(mspid);
				}
				thisOrg.setAdmin(admin); // The admin of this org

				NetworkUser user = thisOrg.getUserByName(TESTUSER_1_NAME);
				if (!user.isRegistered()) { // users need to be registered AND
											// enrolled
					RegistrationRequest rr = new RegistrationRequest(user.getName(), "org1.department1");
					user.setEnrollmentSecret(ca.register(rr, admin));
				}
				if (!user.isEnrolled()) {
					user.setEnrollment(ca.enroll(user.getName(), user.getEnrollmentSecret()));
					user.setMPSID(mspid);
				}

				thisOrg.addUser(user); // The user of this org

				final String thisOrgDomainName = thisOrg.getDomain();

				// To be checked. Maybe we don't need this part.
				NetworkUser peerOrgAdmin = thisOrg.getUserByName("admin");
				File certificateFile = Paths.get("crypto-config/peerOrganizations/", thisOrgDomainName,
						format("/users/Admin@%s/msp/signcerts/Admin@%s-cert.pem", thisOrgDomainName, thisOrgDomainName))
						.toFile();
				String certificate = new String(IOUtils.toByteArray(new FileInputStream(certificateFile)), "UTF-8");

				PrivateKey privateKey = getPrivateKeyFromBytes(
						IOUtils.toByteArray(
								new FileInputStream(
										findFile_sk(Paths
												.get("crypto-config/peerOrganizations/", thisOrgDomainName,
														format("/users/Admin@%s/msp/keystore", thisOrgDomainName))
												.toFile()))));

				peerOrgAdmin.setEnrollment(new SampleStoreEnrollement(privateKey, certificate));

				/*
				 * A special user that can crate channels, join peers and
				 * install chain code
				 */
				thisOrg.setPeerAdmin(peerOrgAdmin);
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}
	

	public static List<Orderer> getOrderers(List<OrdererDetails> ordererDetails, HFClient client,
			HLConfiguration config) throws InvalidArgumentException {
		List<Orderer> orderers = new LinkedList<Orderer>();
		for (OrdererDetails od : ordererDetails) {
			Properties ordererProperties = config.getOrdererProperties(od.getName());

			// example of setting keepAlive to avoid timeouts on inactive http2
			// connections.
			// Under 5 minutes would require changes to server side to accept
			// faster ping rates.
			ordererProperties.put("grpc.NettyChannelBuilderOption.keepAliveTime",
					new Object[] { 5L, TimeUnit.MINUTES });
			ordererProperties.put("grpc.NettyChannelBuilderOption.keepAliveTimeout",
					new Object[] { 8L, TimeUnit.SECONDS });

			Orderer thisOrderer = client.newOrderer(od.getName(), od.getLocation(), ordererProperties);
			orderers.add(thisOrderer);
		}
		return orderers;
	}

	public static List<Peer> getPeers(List<PeerDetails> peerDetails, HFClient client, HLConfiguration config)
			throws InvalidArgumentException {
		List<Peer> peers = new LinkedList<Peer>();
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
			peers.add(peer);
		}
		return peers;
	}

	public static List<EventHub> getEventHubs(Map<String, String> eventHubDetails, Set<String> eventHubNames,
			HFClient client, HLConfiguration config) throws InvalidArgumentException {
		List<EventHub> eventHubs = new LinkedList<EventHub>();
		for (String eventHubName : eventHubNames) {
			EventHub eventHub = client.newEventHub(eventHubName, eventHubDetails.get(eventHubName),
					config.getEventHubProperties(eventHubName));
			eventHubs.add(eventHub);
		}
		return eventHubs;
	}

	private static PrivateKey getPrivateKeyFromBytes(byte[] data)
			throws IOException, NoSuchProviderException, NoSuchAlgorithmException, InvalidKeySpecException {

		try (PEMParser pemParser = new PEMParser(new StringReader(new String(data)))) {

			PrivateKeyInfo pemPair = (PrivateKeyInfo) pemParser.readObject();
			PrivateKey privateKey = new JcaPEMKeyConverter().setProvider(BouncyCastleProvider.PROVIDER_NAME)
					.getPrivateKey(pemPair);

			return privateKey;
		}
	}

	private static File findFile_sk(File directory) {

		File[] matches = directory.listFiles((dir, name) -> name.endsWith("_sk"));

		if (null == matches) {
			throw new RuntimeException(
					format("Matches returned null does %s directory exist?", directory.getAbsoluteFile().getName()));
		}

		if (matches.length != 1) {
			throw new RuntimeException(format("Expected in %s only 1 sk file but found %d",
					directory.getAbsoluteFile().getName(), matches.length));
		}

		return matches[0];
	}

	public static HFClient getHyperledgerFabricClient() {
		return client;
	}

	public static HLConfiguration getHyperledgerFabricConfig() {
		return config;
	}
	
	static class SampleStoreEnrollement implements Enrollment, Serializable {
		private static final long serialVersionUID = 1L;
		private PrivateKey privateKey;
		private final String certificate;

		SampleStoreEnrollement(PrivateKey privateKey, String certificate) {

			this.certificate = certificate;

			this.privateKey = privateKey;
		}

		@Override
		public PrivateKey getKey() {

			return privateKey;
		}

		@Override
		public String getCert() {
			return certificate;
		}

	}

}
