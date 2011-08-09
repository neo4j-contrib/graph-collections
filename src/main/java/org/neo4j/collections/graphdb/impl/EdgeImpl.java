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
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;
import org.neo4j.collections.graphdb.ConnectionMode;
import org.neo4j.collections.graphdb.Connector;
import org.neo4j.collections.graphdb.ConnectorType;
import org.neo4j.collections.graphdb.DatabaseService;
import org.neo4j.collections.graphdb.Edge;
import org.neo4j.collections.graphdb.EdgeElement;
import org.neo4j.collections.graphdb.EdgeType;
import org.neo4j.collections.graphdb.InjectiveConnectionMode;
import org.neo4j.collections.graphdb.Vertex;
import org.neo4j.collections.graphdb.VertexType;


public class EdgeImpl extends VertexImpl implements Edge{

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

	
	private class RelationshipElementIterable implements Iterable<EdgeElement>{

		private final Set<Connector<?>> connectorTypes;
		
		public RelationshipElementIterable() {
			this.connectorTypes = getType().getConnectors();
		}

		public RelationshipElementIterable(
				Set<Connector<?>> connectorTypes) {
			this.connectorTypes = connectorTypes;
		}
		
		@Override
		public Iterator<EdgeElement> iterator() {
			return new RelationshipElementIterator(connectorTypes);
		}
		
	}
	
	private class RelationshipElementIterator implements Iterator<EdgeElement>{

		private final Set<Connector<?>> connectorTypes;
		private int index = 0;
		
		
		RelationshipElementIterator(
				Set<Connector<?>> connectorTypes) {
			this.connectorTypes = connectorTypes;
		}

		@Override
		public boolean hasNext() {
			return index < connectorTypes.size();
		}

		@Override
		public EdgeElement next() {
			if(hasNext()){
				String connectorTypeName = getType().getName().substring(getType().getName().indexOf(EDGEROLE_SEPARATOR)+3);
				Connector<?> connector = getType().getConnector(connectorTypeName);
				Iterable<Vertex> elems = new ElementIterable(connector.getConnectorType());
				return new EdgeElement(connector.getConnectorType(), elems);
			}else{
				throw new NoSuchElementException();
			}
		}

		@Override
		public void remove() {
		}
	}

	private EdgeType relType;

	EdgeImpl(Node node){
		super(node);
	}

	@Override
	public void delete() {
		getNode().delete();
	}

	@Override
	public DatabaseService getDb() {
		return new GraphDatabaseImpl(getNode().getGraphDatabase());
	}

	@Override
	public Iterable<EdgeElement> getEdgeElements() {
		return new RelationshipElementIterable();
	}

	@Override
	public Iterable<EdgeElement> getEdgeElements(ConnectorType<?>... connectorTypes) {
		Set<Connector<?>> connectorTypeSet = new HashSet<Connector<?>>();
		for(ConnectorType<?> connectorType: connectorTypes){
			Connector<?> er = new Connector<ConnectionMode>((ConnectorType<ConnectionMode>) connectorType, getType());
			connectorTypeSet.add(er);
		}
		return new RelationshipElementIterable(connectorTypeSet);
	}

	@Override
	public Iterable<Edge> getEdges(EdgeType edgeType, ConnectorType<?>... connectorType) {
		// TODO Auto-generated method stub
		return null;
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
	public <U extends InjectiveConnectionMode>Vertex getVertex(ConnectorType<U> connectorType) {
		return getDb().getVertex(getNode().getSingleRelationship(DynamicRelationshipType.withName(getType().getName()+EDGEROLE_SEPARATOR+connectorType.getName()), Direction.OUTGOING).getEndNode());
	}

	@Override
	public <T extends ConnectionMode> Iterable<Vertex> getVertices(ConnectorType<T> connectorType) {
		return new ElementIterable(connectorType);
	}

	@Override
	public boolean hasEdge(EdgeType edgeType, ConnectorType<?>... connectorType) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isType(EdgeType relType) {
		return (relType.getNode().getId() == getType().getNode().getId());
	}


}
