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

import org.neo4j.collections.graphdb.DatabaseService;
import org.neo4j.collections.graphdb.EdgeRoleType;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;

public abstract class EdgeRoleTypeImpl extends VertexImpl implements EdgeRoleType{

	public final static String EDGEROLE_NAME = "org.neo4j.collections.graphdb.edge_role_name";
	
	public EdgeRoleTypeImpl(Node node) {
		super(node);
	}
	public enum RelTypes implements RelationshipType{
		ORG_NEO4J_COLLECTIONS_GRAPHDB_EGDE_ROLE_SUBREF 
	}

	private static Node getRolesSubRef(DatabaseService db){
		if(db.getReferenceNode().hasRelationship(RelTypes.ORG_NEO4J_COLLECTIONS_GRAPHDB_EGDE_ROLE_SUBREF, Direction.OUTGOING)){
			return db.getReferenceNode().getSingleRelationship(RelTypes.ORG_NEO4J_COLLECTIONS_GRAPHDB_EGDE_ROLE_SUBREF, Direction.OUTGOING).getEndNode();
		}else{
			Node n = db.createNode();
			db.getReferenceNode().createRelationshipTo(n, RelTypes.ORG_NEO4J_COLLECTIONS_GRAPHDB_EGDE_ROLE_SUBREF);
			return n;
		}
	}
	
	public static Node getOrCreateInstanceNode(DatabaseService db, String name) {
		Node subRef = getRolesSubRef(db);
		if(getRolesSubRef(db).hasRelationship(DynamicRelationshipType.withName(name), Direction.OUTGOING)){
			return getRolesSubRef(db).getSingleRelationship(DynamicRelationshipType.withName(name), Direction.OUTGOING).getEndNode();
		}else{
			Node n = db.createNode();
			subRef.createRelationshipTo(n, DynamicRelationshipType.withName(name));
			n.setProperty(EDGEROLE_NAME, name);
			return n;
		}
	}

	
}
