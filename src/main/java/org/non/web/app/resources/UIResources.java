package org.non.web.app.resources;

import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.hyperledger.fabric.sdk.ChaincodeID;
import org.non.api.model.Connection;
import org.non.api.model.TradingPartner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;

@Path("/fabric")
public class UIResources {
	private String CHAIN_CODE_NAME = "myCC_go";
	private String CHAIN_CODE_VERSION = "1.0";
	private String CHAIN_CODE_PATH = "scripts";
	ChaincodeID chainCodeID = ChaincodeID.newBuilder().setName(CHAIN_CODE_NAME).setVersion(CHAIN_CODE_VERSION)
			.setPath(CHAIN_CODE_PATH).build();

	HLConnection hlconnection = HLConnection.getInstance();

	public UIResources() {
	}

	@Path("/search")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String getTradingPartner(
			@QueryParam("orgName") String orgName, 
			@QueryParam("userName") String userName,
			@QueryParam("channelName") String channelName,
			@QueryParam("compName") String compName) throws Exception {
		
		System.out.println("\n Org Name: " + orgName + "\n User Name: " + userName + "\n Channel Name: " + channelName
				+ "\n compName: " + compName);
		
		String result = hlconnection.getTradingPartner(orgName, userName, channelName, compName);
		//hlconnection.queryVerified(orgName, userName, channelName);
		System.out.println("RESULT:" + result);
		return result;
	}

  @Path("/getUnverified")
	@GET
	@Produces (MediaType.APPLICATION_JSON)
	public String getUnverifiedTradingPartner (
			@QueryParam("orgName")     String orgName,
			@QueryParam("userName")    String userName,
			@QueryParam("channelName") String channelName) throws Exception{
		
		
		String result =  hlconnection.queryVerified(orgName, userName, channelName);
		System.out.println("RESULT:" + result);
		
		return result;
		//return "{\"name\":\"123\",\"websiteURI\":\"123213\",\"streetName\":\"132\",\"cityName\":\"123\",\"postalZone\":\"213\",\"contactName\":\"31\",\"contactEmail\":\"1322\",\"contactTelephone\":\"3\",\"ids\":\"[]\",\"idList\":[]}";
	}
	
	@Path("/create")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	// @Produces(MediaType.TEXT_PLAIN)
	public String createPartner(
			@QueryParam("userName") String userName, 
			@QueryParam("orgName") String orgName,
			@QueryParam("channelName") String channelName,
			@QueryParam("func") String func,
			@BeanParam TradingPartner tradingPartner) throws Exception{
		tradingPartner.init();

		ObjectMapper mapper = new ObjectMapper();
		String tradeString = mapper.writeValueAsString(tradingPartner);
		System.out.println(tradeString);
		System.out.println("\n User Name: " + userName +
						   "\n OrgName: " + orgName +
						   "\n channelName: " + channelName);
		
		String result = hlconnection.createPartner(orgName, userName, channelName, tradingPartner,func);
		System.out.println(result);
		JsonObject obj = new JsonObject();
		if (result.equalsIgnoreCase("SUCCESS")) {
			obj.addProperty("success", true);
			return obj.toString();
		} else
			return result;
	}

	/* NOT YET IMPLEMENTED/DECIDED ON WHAT TO DO */
	@Path("/modify")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public void modify(){
	
	}

	@Path("/createConnect")
	@GET
	@Produces (MediaType.APPLICATION_JSON)
	public Connection createConnection(
			@QueryParam("orgName") String orgName, 
			@QueryParam("userName") String userName,
			@QueryParam("channelName") String channelName, 
			@QueryParam("comp1") String comp1,
			@QueryParam("comp2") String comp2,
			@QueryParam("status") String status,
			@QueryParam("process") String process) throws Exception {
		Connection connect = new Connection(comp1, comp2, status, process);
		System.out.println(connect.toString());
		hlconnection.createConnection(orgName, userName, channelName, connect);
		return connect;
	}
	
	@Path("/queryConnection")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String queryConnection (
			@QueryParam("orgName") String orgName,
			@QueryParam("userName") String userName,
			@QueryParam("channelName") String channelName,
			@QueryParam("compName") String compName) throws Exception {
				System.out.println("Querying for Connection");
				String result = hlconnection.queryConnection(orgName, userName, channelName, compName);
				System.out.println(result);
				if (result.isEmpty() || result.length() < 3){
					System.out.println("Result is empty!");
					result = "No connection found!";
				}
				return result;
	}
	
}
