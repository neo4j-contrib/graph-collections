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

import java.util.HashSet;
import java.util.Set;

import org.neo4j.collections.graphdb.DatabaseService;
import org.neo4j.collections.graphdb.EdgeRole;
import org.neo4j.collections.graphdb.EdgeType;
import org.neo4j.collections.graphdb.NAryEdgeRole;
import org.neo4j.collections.graphdb.NAryEdgeRoleType;
import org.neo4j.collections.graphdb.NAryEdgeType;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

public class NAryEdgeTypeImpl extends EdgeTypeImpl<NAryEdgeRoleType> implements NAryEdgeType{

	public enum RelTypes implements RelationshipType{
		ORG_NEO4J_COLLECTIONS_GRAPHDB_EGDE_ROLE 
	}
	
	public NAryEdgeTypeImpl(Node node) {
		super(node);
	}

	protected static Class<?> getImplementationClass(){
		try{
			return Class.forName("org.neo4j.collections.graphdb.impl.NAryEdgeTypeImpl");
		}catch(ClassNotFoundException cce){
			throw new RuntimeException(cce);
		}
	}

	public static NAryEdgeTypeImpl getOrCreateInstance(DatabaseService db, String name, Set<NAryEdgeRoleType> edgeRoleTypes){
		VertexTypeImpl vertexType = new VertexTypeImpl(getOrCreateByDescriptor(new TypeNodeDescriptor(db, name, getImplementationClass())));
		return new NAryEdgeTypeImpl(vertexType.getNode());
	}

	public static class NAryTypeNodeDescriptor<T> extends TypeNodeDescriptor{

		public final Set<NAryEdgeRoleType> edgeRoleTypes;
		
		NAryTypeNodeDescriptor(DatabaseService db, String name,
				Class<?> claz, Set<NAryEdgeRoleType> edgeRoleTypes) {
			super(db, name, claz);
			this.edgeRoleTypes = edgeRoleTypes;
		}
		
		@Override
		public void initialize(Node n){
			super.initialize(n);
			for(NAryEdgeRoleType edgeRoleType: edgeRoleTypes){
				n.createRelationshipTo(edgeRoleType.getNode(), RelTypes.ORG_NEO4J_COLLECTIONS_GRAPHDB_EGDE_ROLE);
			}
		}
	}

	
	@SuppressWarnings("unchecked")
	@Override
	public Set<NAryEdgeRole> getRoles() {
		Set<NAryEdgeRole> roles = new HashSet<NAryEdgeRole>();
		for(Relationship rel: getNode().getRelationships(RelTypes.ORG_NEO4J_COLLECTIONS_GRAPHDB_EGDE_ROLE, Direction.OUTGOING)){
			roles.add(new NAryEdgeRole(new NAryEdgeRoleTypeImpl(rel.getEndNode()), this));
		}
		return roles;
	}


	@Override
	public NAryEdgeRole getRole(String name) {
		for(Relationship rel: getNode().getRelationships(RelTypes.ORG_NEO4J_COLLECTIONS_GRAPHDB_EGDE_ROLE, Direction.OUTGOING)){
			if(rel.getEndNode().hasProperty(NAryEdgeRoleTypeImpl.EDGEROLE_NAME)){
				if(rel.getEndNode().getProperty(NAryEdgeRoleTypeImpl.EDGEROLE_NAME).equals(name)){
					NAryEdgeRoleType edgeRoleType = NAryEdgeRoleTypeImpl.getOrCreateInstance(getDb(), name);
					return new NAryEdgeRole(edgeRoleType, this);
				}
			}
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <U extends EdgeType<NAryEdgeRoleType>> EdgeRole<U, NAryEdgeRoleType> getRole(
			NAryEdgeRoleType edgeRoleType) {
		for(Relationship rel: getNode().getRelationships(RelTypes.ORG_NEO4J_COLLECTIONS_GRAPHDB_EGDE_ROLE, Direction.OUTGOING)){
			if(rel.getEndNode().hasProperty(NAryEdgeRoleTypeImpl.EDGEROLE_NAME)){
				if(rel.getEndNode().getProperty(NAryEdgeRoleTypeImpl.EDGEROLE_NAME).equals(edgeRoleType.getName())){
					return (EdgeRole<U, NAryEdgeRoleType>) new NAryEdgeRole(edgeRoleType, this);
				}
			}
		}
		return null;
	}

}
