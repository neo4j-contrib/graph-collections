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

import org.neo4j.collections.graphdb.ConnectionMode;
import org.neo4j.collections.graphdb.DatabaseService;
import org.neo4j.collections.graphdb.ConnectorType;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;

public class ConnectorTypeImpl<T extends ConnectionMode> extends VertexImpl implements ConnectorType<T>{

	public final static String CONNECTOR_TYPE_NAME = "org.neo4j.collections.graphdb.connector_type_name";
	public final static String CONNECTOR_MODE = "org.neo4j.collections.graphdb.connector_mode";
	
	public ConnectorTypeImpl(Node node) {
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
	
	public static <U extends ConnectionMode> ConnectorType<U> getOrCreateInstance(DatabaseService db, String name, U connectionMode) {
		Node subRef = getRolesSubRef(db);
		if(getRolesSubRef(db).hasRelationship(DynamicRelationshipType.withName(name), Direction.OUTGOING)){
			return new ConnectorTypeImpl<U>(getRolesSubRef(db).getSingleRelationship(DynamicRelationshipType.withName(name), Direction.OUTGOING).getEndNode());
		}else{
			Node n = db.createNode();
			subRef.createRelationshipTo(n, DynamicRelationshipType.withName(name));
			n.setProperty(CONNECTOR_TYPE_NAME, name);
			n.setProperty(CONNECTOR_MODE, connectionMode.getName());
			return new ConnectorTypeImpl<U>(n);
		}
	}

	@Override
	public String getName() {
		return (String)getNode().getProperty(CONNECTOR_TYPE_NAME);
	}

	public static ConnectionMode getConnectionMode(String name){
		if(name.equals("unrestricted")){
			return ConnectionMode.UNRESTRICTED;
		}
		if(name.equals("injective")){
			return ConnectionMode.INJECTIVE;
		}
		if(name.equals("surjective")){
			return ConnectionMode.SURJECTIVE;
		}
		if(name.equals("bijective")){
			return ConnectionMode.BIJECTIVE;
		}else{
			throw new RuntimeException("Unknown connection mode "+name);
		}
	}
	
	@Override
	public ConnectionMode getConnectionMode() {
		String connectionModeName = (String)getNode().getProperty(CONNECTOR_MODE);
		return getConnectionMode(connectionModeName);
	}

	
}
