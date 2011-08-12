/**
 * Copyright (c) 2002-2011 "Neo Technology,"
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

import java.util.HashSet;
import java.util.Set;

import org.neo4j.collections.graphdb.Connector;
import org.neo4j.collections.graphdb.ConnectionMode;
import org.neo4j.collections.graphdb.ConnectorType;
import org.neo4j.collections.graphdb.DatabaseService;
import org.neo4j.collections.graphdb.Edge;
import org.neo4j.collections.graphdb.EdgeType;
import org.neo4j.collections.graphdb.Vertex;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

public class EdgeTypeImpl extends VertexTypeImpl implements EdgeType {

	public EdgeTypeImpl(Node node){
		super(node);
	}
	
	public enum RelTypes implements RelationshipType{
		ORG_NEO4J_COLLECTIONS_GRAPHDB_EGDE_ROLE 
	}
	
	private static Class<?> getImplementationClass(){
		try{
			return Class.forName("org.neo4j.collections.graphdb.impl.EdgeTypeImpl");
		}catch(ClassNotFoundException cce){
			throw new RuntimeException(cce);
		}
	}

	public static class EdgeTypeNodeDescriptor extends TypeNodeDescriptor{

		private final ConnectorType<?>[] connectorTypes;
		
		public EdgeTypeNodeDescriptor(DatabaseService db, String name,
				Class<?> claz, ConnectorType<?>... connectorTypes) {
			super(db, name, claz);
			this.connectorTypes = connectorTypes;
		}
		
		@Override
		public void initialize(Node n){
			super.initialize(n);
			for(ConnectorType<?> connectorType: connectorTypes){
				n.createRelationshipTo(connectorType.getNode(), RelTypes.ORG_NEO4J_COLLECTIONS_GRAPHDB_EGDE_ROLE);
			}
		}
	}
	
	public static EdgeTypeImpl getOrCreateInstance(DatabaseService db, String name, ConnectorType<?>... connectorTypes){
		VertexTypeImpl vertexType = new VertexTypeImpl(getOrCreateByDescriptor(new EdgeTypeNodeDescriptor(db, name, getImplementationClass(), connectorTypes)));
		return new EdgeTypeImpl(vertexType.getNode());
	}

	@Override
	public Connector<?> getConnector(String name) {
		for(Relationship rel: getNode().getRelationships(RelTypes.ORG_NEO4J_COLLECTIONS_GRAPHDB_EGDE_ROLE, Direction.OUTGOING)){
			if(rel.getEndNode().hasProperty(ConnectorTypeImpl.CONNECTOR_TYPE_NAME)){
				if(rel.getEndNode().getProperty(ConnectorTypeImpl.CONNECTOR_TYPE_NAME).equals(name)){
					String connectionModeName = (String)rel.getEndNode().getProperty(ConnectorTypeImpl.CONNECTOR_MODE);
					return Connector.getInstance(ConnectorTypeImpl.getOrCreateInstance(getDb(), name, ConnectorTypeImpl.getConnectionMode(connectionModeName)), this);
				}
			}
		}
		return null;
	}
	
	@Override
	public <T extends ConnectionMode> Connector<T> getConnector(
			ConnectorType<T> connectorType) {
		for(Relationship rel: getNode().getRelationships(RelTypes.ORG_NEO4J_COLLECTIONS_GRAPHDB_EGDE_ROLE, Direction.OUTGOING)){
			if(rel.getEndNode().hasProperty(ConnectorTypeImpl.CONNECTOR_TYPE_NAME)){
				if(rel.getEndNode().getProperty(ConnectorTypeImpl.CONNECTOR_TYPE_NAME).equals(connectorType.getName())){
					return new Connector<T>(connectorType, this);
				}
			}
		}
		return null;
	}

	public Iterable<Edge> getEdges(Vertex vertex, ConnectorType<?>... connectorTypes) {
		Set<Connector<?>> connectorTypes1 = new HashSet<Connector<?>>();
		Set<Connector<?>> connectorTypes2 = getConnectors();
		for (ConnectorType<?> connectorType : connectorTypes) {
			for (Connector<?> connectorType2 : connectorTypes2) {
				if (connectorType.getName().equals(connectorType2.getConnectorType().getName())) {
					connectorTypes1.add(connectorType2);
				}
			}
		}

		return new ConnectorIterable(connectorTypes1, vertex);
	}

	@Override
	public Set<Connector<?>> getConnectors() {
		Set<Connector<?>> connectorTypes = new HashSet<Connector<?>>();
		for(Relationship rel: getNode().getRelationships(RelTypes.ORG_NEO4J_COLLECTIONS_GRAPHDB_EGDE_ROLE, Direction.OUTGOING)){
			String connectorName = (String)rel.getEndNode().getProperty(ConnectorTypeImpl.CONNECTOR_TYPE_NAME);
			ConnectionMode connectionMode = ConnectorTypeImpl.getConnectionMode((String) rel.getEndNode().getProperty(ConnectorTypeImpl.CONNECTOR_MODE));
			connectorTypes.add(Connector.getInstance(ConnectorTypeImpl.getOrCreateInstance(getDb(), connectorName, connectionMode), this));
		}
		return connectorTypes;
	}

	public boolean hasEdge(Vertex vertex, ConnectorType<?>... connectorTypes){
		return getEdges(vertex, connectorTypes).iterator().hasNext();
	}
	

}
