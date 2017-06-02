package org.non.api;

import java.util.List;

import org.non.config.HLConfiguration;
import org.non.config.Org;

public class Main {
	public static void main(String[] args) {
		HLConfiguration config=new HLConfiguration();
		config.load("scripts/config/config_properties.yml");
		List<Org> list=config.getAllOrgsWithDetails();
		System.out.println(list.get(0).getUser().get(0).getName()+" "+list.get(1).getName()+" "+list.get(2).getName());
	}
}
