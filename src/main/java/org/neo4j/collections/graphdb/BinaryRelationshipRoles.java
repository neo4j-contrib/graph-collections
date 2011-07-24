package org.neo4j.collections.graphdb;

import org.neo4j.collections.graphdb.impl.ElementImpl;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;

public class BinaryRelationshipRoles<T extends Element> extends ElementImpl implements RelationshipRole<T>{

	@Override
	public PropertyContainer getPropertyContainer() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GraphDatabaseService getGraphDatabase() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Node getNode() {
		// TODO Auto-generated method stub
		return null;
	}

}
