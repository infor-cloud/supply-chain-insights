package org.non.api;

public class Identifier {

	private String schemaName;
	private String id;
	
	public Identifier(){
		// Jackson deserialization
	}
	
	public Identifier(String schemaName, String id) {
		this.schemaName = schemaName;
		this.id = id;
	}
	
	public String getSchemaName() {
		return schemaName;
	}
	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
}
