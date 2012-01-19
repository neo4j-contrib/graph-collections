/**
 * Copyright (c) 2002-2012 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.collections.graphdb.impl;

import org.neo4j.collections.graphdb.ConnectionMode;
import org.neo4j.collections.graphdb.ConnectorType;
import org.neo4j.collections.graphdb.DatabaseService;
import org.neo4j.collections.graphdb.EdgeType;
import org.neo4j.collections.graphdb.VertexType;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

public class ConnectorTypeImpl<T extends ConnectionMode> extends VertexImpl implements ConnectorType<T>{

	public final static String CONNECTOR_TYPE_NAME = "org.neo4j.collections.graphdb.connector_type_name";
	public final static String CONNECTOR_MODE = "org.neo4j.collections.graphdb.connector_mode";

	public enum RelTypes implements RelationshipType{
		ORG_NEO4J_COLLECTIONS_GRAPHDB_CONNECTOR_TYPE, ORG_NEO4J_COLLECTIONS_GRAPHDB_CONNECTOR_DOMAIN  
	}

	public ConnectorTypeImpl(DatabaseService db, Long id) {
		super(db, id);
	}

	private static <U extends ConnectionMode> ConnectorType<U> create(DatabaseService db, String name, Node edgeTypeNode, U connectionMode, VertexType domain){
		Node n = db.createNode();
		edgeTypeNode.createRelationshipTo(n, RelTypes.ORG_NEO4J_COLLECTIONS_GRAPHDB_CONNECTOR_TYPE);
		n.setProperty(CONNECTOR_TYPE_NAME, name);
		n.setProperty(CONNECTOR_MODE, connectionMode.getName());
		if(domain == null){
			n.createRelationshipTo(db.getRootType().getNode(), RelTypes.ORG_NEO4J_COLLECTIONS_GRAPHDB_CONNECTOR_DOMAIN);
		}else{
			n.createRelationshipTo(domain.getNode(), RelTypes.ORG_NEO4J_COLLECTIONS_GRAPHDB_CONNECTOR_DOMAIN);
		}
		return new ConnectorTypeImpl<U>(db, n.getId());
	}

	public static <U extends ConnectionMode> ConnectorType<U> getOrCreateInstance(DatabaseService db, String name, Node edgeTypeNode, U connectionMode) {
		return getOrCreateInstance(db, name, edgeTypeNode, connectionMode, db.getRootType());
	}

	public static <U extends ConnectionMode> ConnectorType<U> getOrCreateInstance(DatabaseService db, String name, Node edgeTypeNode, U connectionMode, VertexType domain) {
		if(edgeTypeNode.hasRelationship(RelTypes.ORG_NEO4J_COLLECTIONS_GRAPHDB_CONNECTOR_TYPE, Direction.OUTGOING)){
			for(Relationship rel: edgeTypeNode.getRelationships(RelTypes.ORG_NEO4J_COLLECTIONS_GRAPHDB_CONNECTOR_TYPE, Direction.OUTGOING)){
				if(rel.getEndNode().getProperty(CONNECTOR_TYPE_NAME).equals(name))
					return new ConnectorTypeImpl<U>(db, rel.getEndNode().getId());				
			}
			return create(db, name, edgeTypeNode, connectionMode, domain);
		}else{
			return create(db, name, edgeTypeNode, connectionMode, domain);
		}
	}

	@Override
	public String getName() {
		return (String)getNode().getProperty(CONNECTOR_TYPE_NAME);
	}

	public static ConnectionMode getConnectionMode(String name){
		if(name.equals(ConnectionMode.UNRESTRICTED.getName())){
			return ConnectionMode.UNRESTRICTED;
		}
		if(name.equals(ConnectionMode.INJECTIVE.getName())){
			return ConnectionMode.INJECTIVE;
		}
		if(name.equals(ConnectionMode.SURJECTIVE.getName())){
			return ConnectionMode.SURJECTIVE;
		}
		if(name.equals(ConnectionMode.BIJECTIVE.getName())){
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

	@Override
	public EdgeType getEdgeType() {
		return (EdgeType)db.getVertex(getNode().getSingleRelationship(DynamicRelationshipType.withName(getName()), Direction.INCOMING).getStartNode());
	}

	@Override
	public VertexType getDomain() {
		return (VertexType)db.getVertex(getNode().getSingleRelationship(RelTypes.ORG_NEO4J_COLLECTIONS_GRAPHDB_CONNECTOR_DOMAIN, Direction.OUTGOING).getEndNode());
	}
}
