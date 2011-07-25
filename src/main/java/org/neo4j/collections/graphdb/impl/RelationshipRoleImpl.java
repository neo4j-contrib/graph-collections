package org.neo4j.collections.graphdb.impl;

import org.neo4j.collections.graphdb.Element;
import org.neo4j.collections.graphdb.Node;
import org.neo4j.collections.graphdb.GraphDatabaseService;
import org.neo4j.collections.graphdb.Relationship;
import org.neo4j.collections.graphdb.RelationshipRole;
import org.neo4j.collections.graphdb.BinaryRelationshipRole.RelTypes;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicRelationshipType;

public class RelationshipRoleImpl<T extends Element> extends ElementImpl implements RelationshipRole<T>{
	
	private final String name;
	private final GraphDatabaseService graphDb;
	private org.neo4j.graphdb.Node node;

	public RelationshipRoleImpl(GraphDatabaseService graphDb, String name){
		this.name = name;
		this.graphDb = graphDb;
	}

	
	@Override
	public org.neo4j.graphdb.PropertyContainer getPropertyContainer() {
		return getNode();
	}

	@Override
	public GraphDatabaseService getGraphDatabase() {
		return graphDb;
	}

	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public org.neo4j.graphdb.Node getNode() {
		if(node == null){
			Relationship refRel = graphDb.getReferenceNode().getSingleRelationship(RelTypes.ROLETYPES_SUBREF, Direction.OUTGOING);
			Node refNode = null;
			if(refRel == null){
				refNode = graphDb.createNode();
				graphDb.getReferenceNode().createRelationshipTo(refNode, RelTypes.ROLETYPES_SUBREF);
			}else{
				refNode = refRel.getEndNode();
			}
			Relationship roleRel = refNode.getSingleRelationship(DynamicRelationshipType.withName(getName()), Direction.OUTGOING);
			if(roleRel == null){
				Node associatedNode = graphDb.createNode();
				roleRel = refNode.createRelationshipTo(associatedNode, DynamicRelationshipType.withName(getName()));
				return associatedNode.getNode();
			}else{
				return roleRel.getEndNode().getNode();
			}
		}else{
			return node;
		}
	}

}
