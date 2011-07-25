package org.neo4j.collections.graphdb;

import org.neo4j.collections.graphdb.impl.RelationshipRoleImpl;
import org.neo4j.graphdb.RelationshipType;

public class BinaryRelationshipRole<T extends Element> extends RelationshipRoleImpl<T>{

	private static final String startNodeName = "StartNode";
	private static final String endNodeName = "EndNode";
	
	public enum RelTypes implements RelationshipType{
		ROLETYPES_SUBREF
	}

	private BinaryRelationshipRole(GraphDatabaseService graphDb, String name){
		super(graphDb, name);
	}
	

	public static class StartElement extends BinaryRelationshipRole<Element>{
		public StartElement(GraphDatabaseService graphDb){
			super(graphDb, startNodeName);
		}
	}

	public static class EndElement extends BinaryRelationshipRole<Element>{
		public EndElement(GraphDatabaseService graphDb){
			super(graphDb, endNodeName);
		}
	}

}
