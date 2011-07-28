/**
 * Copyright (c) 2002-2011 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.collections.graphdb.impl;


import java.util.Set;

import org.neo4j.collections.graphdb.Element;
import org.neo4j.collections.graphdb.HyperRelationshipType;
import org.neo4j.collections.graphdb.GraphDatabaseService;
import org.neo4j.collections.graphdb.Node;
import org.neo4j.collections.graphdb.PropertyType;
import org.neo4j.collections.graphdb.Relationship;
import org.neo4j.collections.graphdb.RelationshipRole;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.RelationshipType;

public class RelationshipTypeImpl extends ElementImpl implements HyperRelationshipType{

	public final static String REL_TYPE = "org.neo4j.collections.graphdb.rel_type";
	public final static String REL_TYPE_ROLES = "org.neo4j.collections.graphdb.rel_type_roles";
	
	private final org.neo4j.graphdb.RelationshipType relType;
	private final GraphDatabaseService graphDb;
	private final Set<RelationshipRole<?>> roles;
	private Node node = null;

	enum RelTypes implements org.neo4j.graphdb.RelationshipType{
		RELTYPE_SUBREF
	}
	
	RelationshipTypeImpl(GraphDatabaseService graphDb, RelationshipType relType, Set<RelationshipRole<?>> roles){
		this.relType = relType;
		this.graphDb = graphDb;
		this.roles = roles;
	}
	
	public String name() {
		return relType.name();
	}

	public long getId(){
		return getNode().getId();
	}

	public org.neo4j.graphdb.RelationshipType getRelationshipType(){
		return relType;
	}
	
	public GraphDatabaseService getGraphDatabase() {
		return graphDb;
	}
	
	
	public static Node getOrCreateRoleSubRef(GraphDatabaseService graphDb){
		Relationship subRefRel = graphDb.getReferenceNode().getSingleRelationship(RelTypes.RELTYPE_SUBREF, Direction.OUTGOING);
		if(subRefRel == null){
			Node n = graphDb.createNode();
			graphDb.getReferenceNode().createRelationshipTo(n, RelTypes.RELTYPE_SUBREF);
			return n;
		}else{
			return subRefRel.getEndNode();
		}
		
	}

	private Node getOrCreateAssociatedNode(RelationshipType relType){
		Node subRef = getOrCreateRoleSubRef(getGraphDatabase()); 
		Relationship rel = subRef.getSingleRelationship(relType, Direction.OUTGOING);
		if(rel != null){
			return rel.getEndNode();
		}else{
			Node n = graphDb.createNode();
			subRef.createRelationshipTo(n, relType);
			n.setProperty(REL_TYPE, relType.name());
			return n;
		}
	}
	
	public org.neo4j.graphdb.Node getNode(){
		if(node == null){
			node = getOrCreateAssociatedNode(relType);
			if(!node.hasProperty(REL_TYPE_ROLES)){
				String[] names = new String[roles.size()];
				int i=0;
				for(RelationshipRole<?> role: roles){
					names[i] = role.getName();
					i++;
				}
				node.setProperty(REL_TYPE_ROLES, names);
			}
		}
		return node.getNode();
	}


	@Override
	public Iterable<PropertyType<?>> getPropertyTypes() {
		return PropertyType.getPropertyTypes(this, getGraphDatabase());
	}

	@Override
	public PropertyContainer getPropertyContainer() {
		return getNode();
	}

	@Override
	public RelationshipRole<? extends Element>[] getRoles() {
		RelationshipRole<? extends Element>[] rls = new RelationshipRole<?>[roles.size()];
		int i=0;
		for(RelationshipRole<? extends Element> role: roles){
			rls[i] = role;
			i++;
		}
		return rls;
	}
}
