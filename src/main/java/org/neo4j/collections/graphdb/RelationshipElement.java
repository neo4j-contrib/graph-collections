package org.neo4j.collections.graphdb;

public class RelationshipElement<T extends Element>{

	private final RelationshipRole<T> role;
	private final T element;
	
	RelationshipElement(RelationshipRole<T> role, T element) {
		this.role = role;
		this.element = element;
	}

	public T getElement(){
		return element;
	}
	
	public RelationshipRole<T> getRole(){
		return role;
	}
	
}
