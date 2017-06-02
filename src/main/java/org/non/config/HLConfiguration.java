package org.non.config;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class HLConfiguration {
	
	
	private static OrgList CONFIG;
	private static List<Org> ALL_ORGS_WITH_DETAILS;
	
	
	public void load(String path){
		Path configProperitesFilePath=Paths.get(path);
        File file=new File(configProperitesFilePath.toString());
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try {
            CONFIG = mapper.readValue(file, OrgList.class);
            ALL_ORGS_WITH_DETAILS=CONFIG.getOrgList();
        }
        catch(Exception e){
        	System.out.println(e.getMessage());
        }
        
	}
	
	public List<Org> getAllOrgsWithDetails()
	{
		return ALL_ORGS_WITH_DETAILS;
	}
	
	public Org getOrgDetailsByName(String orgName){
		return getAllOrgsWithDetails().stream().filter(p->p.getName().equals(orgName)).collect(Collectors.<Org> toList()).get(0);
		
	}
	
	public List<Users> getUsersForOrg(String orgName ){
		return  getOrgDetailsByName(orgName).getUser();
	}

}
