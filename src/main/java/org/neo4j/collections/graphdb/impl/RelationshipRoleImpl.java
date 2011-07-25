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

import org.neo4j.collections.graphdb.Element;
import org.neo4j.collections.graphdb.Node;
import org.neo4j.collections.graphdb.GraphDatabaseService;
import org.neo4j.collections.graphdb.Relationship;
import org.neo4j.collections.graphdb.RelationshipRole;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.RelationshipType;

public class RelationshipRoleImpl<T extends Element> extends ElementImpl implements RelationshipRole<T>{
	
	public final static String ROLE_NAME = "org.neo4j.collections.graphdb.role_name";
	
	public enum RelTypes implements RelationshipType{
		ROLETYPES_SUBREF
	}

	private final String name;
	private final GraphDatabaseService graphDb;
	private org.neo4j.graphdb.Node node;

	public RelationshipRoleImpl(GraphDatabaseService graphDb, String name){
		this.name = name;
		this.graphDb = graphDb;
	}

	public long getId(){
		return getNode().getId();
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
				associatedNode.setProperty(ROLE_NAME, getName());
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
