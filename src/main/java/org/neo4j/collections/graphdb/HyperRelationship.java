package org.neo4j.collections.graphdb;

import org.neo4j.graphdb.RelationshipType;

public interface HyperRelationship extends Element{

	public long getId();
	
	public void delete();

	public HyperRelationshipType getType();

	public boolean isType(RelationshipType relType);

	public RelationshipElement<? extends Element>[] getRelationshipElements();

	public <T extends Element> T getElement(RelationshipRole<T> role);
	
}
