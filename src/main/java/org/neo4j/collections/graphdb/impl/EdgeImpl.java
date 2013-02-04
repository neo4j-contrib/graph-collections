/**
 * Copyright (c) 2002-2013 "Neo Technology,"
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
import java.util.Iterator;
import java.util.Set;

import org.neo4j.collections.graphdb.Connection;
import org.neo4j.collections.graphdb.ConnectionMode;
import org.neo4j.collections.graphdb.Connector;
import org.neo4j.collections.graphdb.ConnectorType;
import org.neo4j.collections.graphdb.DatabaseService;
import org.neo4j.collections.graphdb.Edge;
import org.neo4j.collections.graphdb.EdgeType;
import org.neo4j.collections.graphdb.LeftRestrictedConnectionMode;
import org.neo4j.collections.graphdb.Vertex;
import org.neo4j.collections.graphdb.VertexType;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;


public class EdgeImpl extends VertexImpl implements Edge{

	Edge outer = this;
	
	private class ElementIterable implements Iterable<Vertex>{

		private final ConnectorType<?> connectorType;
		
		
		public ElementIterable(ConnectorType<?> connectorType) {
			this.connectorType = connectorType;
		}

		@Override
		public Iterator<Vertex> iterator() {
			return new ElementIterator(connectorType);
		}
	}

	private class ElementIterator implements Iterator<Vertex>{

		private final Iterator<Relationship> rels;
		
		public ElementIterator(ConnectorType<?> connectorType) {
			this.rels = getNode().getRelationships(DynamicRelationshipType.withName(getType().getName()+EDGEROLE_SEPARATOR+connectorType.getName()), Direction.OUTGOING).iterator();
		}

		@Override
		public boolean hasNext() {
			return rels.hasNext();
		}

		@Override
		public Vertex next() {
			Vertex elem = getDb().getVertex(rels.next().getEndNode());
			return elem;
		}

		@Override
		public void remove() {
		}
	}

	
	private class ConnectorIterable implements Iterable<Connector<?>>{

		private final Set<Connector<?>> connectors;
		
		public ConnectorIterable() {
			this.connectors = new HashSet<Connector<?>>();
			for(ConnectorType<?> connectorType: getType().getConnectorTypes()){
				connectors.add(Connector.getInstance(connectorType, outer));
			}
		}

		public ConnectorIterable(
				Set<Connector<?>> connectors) {
			this.connectors = connectors;
		}
		
		@Override
		public Iterator<Connector<?>> iterator() {
			return connectors.iterator();
		}
		
	}
	
	private EdgeType relType;

	EdgeImpl(DatabaseService db, Long id){
		super(db, id);
	}

	@Override
	public void delete() {
		getNode().delete();
	}

	@Override
	public Iterable<Connector<?>> getConnectors() {
		return new ConnectorIterable();
	}

	@Override
	public Iterable<Connector<?>> getConnectors(ConnectorType<?>... connectorTypes) {
		Set<Connector<?>> connectorSet = new HashSet<Connector<?>>();
		for(ConnectorType<?> connectorType: connectorTypes){
			Connector<?> er = Connector.getInstance(connectorType, outer);
			connectorSet.add(er);
		}
		return new ConnectorIterable(connectorSet);
	}

	@Override
	public PropertyContainer getPropertyContainer() {
		return getNode();
	}

	
	@Override
	protected VertexType getSpecialVertexType(){
		return getType();
	}

	@Override
	public EdgeType getType() {
		if(relType == null){
			relType = (EdgeType)getDb().getVertex(getDb().getNodeById((Long)getNode().getProperty(GraphDatabaseImpl.EDGE_TYPE)));
		}
		return relType;
	}

	@Override
	public <U extends LeftRestrictedConnectionMode>Vertex getVertex(ConnectorType<U> connectorType) {
		Relationship rel = getNode().getSingleRelationship(DynamicRelationshipType.withName(getType().getName()+EDGEROLE_SEPARATOR+connectorType.getName()), Direction.OUTGOING); 
		return getDb().getVertex(rel.getEndNode());
	}

	@Override
	public <T extends ConnectionMode> Iterable<Vertex> getVertices(ConnectorType<T> connectorType) {
		return new ElementIterable(connectorType);
	}
	
	@Override
	public boolean isType(EdgeType relType) {
		return (relType.getNode().getId() == getType().getNode().getId());
	}

	@Override
	public <T extends ConnectionMode> Iterable<Connection<T>> getConnections(
			ConnectorType<T> connectorType) {
		Connector<T> connector = getConnector(connectorType); 
		return connector.getConnections();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends ConnectionMode> Connector<T> getConnector(ConnectorType<T> connectorType) {
		for(Connector<?> connector: getConnectors(connectorType)){
			return (Connector<T>) connector;
		}
		return null;
	}


}
