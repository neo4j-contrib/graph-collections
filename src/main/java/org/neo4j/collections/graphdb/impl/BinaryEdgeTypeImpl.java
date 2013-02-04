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
import java.util.Set;

import org.neo4j.collections.graphdb.BinaryEdge;
import org.neo4j.collections.graphdb.BinaryEdgeType;
import org.neo4j.collections.graphdb.ConnectionMode;
import org.neo4j.collections.graphdb.ConnectorType;
import org.neo4j.collections.graphdb.DatabaseService;
import org.neo4j.collections.graphdb.SurjectiveConnectionMode;
import org.neo4j.collections.graphdb.Vertex;
import org.neo4j.collections.graphdb.VertexType;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

public class BinaryEdgeTypeImpl extends EdgeTypeImpl implements BinaryEdgeType{

	static final public String STARTCONNECTORNAME = "StartConnector";
	static final public String ENDCONNECTORNAME = "EndConnector";
	
	public BinaryEdgeTypeImpl(DatabaseService db, Long id) {
		super(db, id);
	}

	protected static Class<?> getImplementationClass(){
		try{
			return Class.forName("org.neo4j.collections.graphdb.impl.BinaryEdgeTypeImpl");
		}catch(ClassNotFoundException cce){
			throw new RuntimeException(cce);
		}
	}

	public static class BinaryEdgeTypeNodeDescriptor extends TypeNodeDescriptor{

		private final VertexType domain;
		private final VertexType range;
		
		public BinaryEdgeTypeNodeDescriptor(DatabaseService db, String name,
				Class<?> claz, VertexType domain, VertexType range) {
			super(db, name, claz);
			this.domain = domain;
			this.range = range;
		}
		
		@Override
		public void initialize(Node n){
			super.initialize(n);
			ConnectorTypeImpl.getOrCreateInstance(db, STARTCONNECTORNAME, n, ConnectionMode.BIJECTIVE, domain);
			ConnectorTypeImpl.getOrCreateInstance(db, ENDCONNECTORNAME, n, ConnectionMode.BIJECTIVE, range);
		}
	}
	
	
	public static BinaryEdgeTypeImpl getOrCreateInstance(DatabaseService db, RelationshipType relType, VertexType domain, VertexType range){
		VertexTypeImpl vertexType = new VertexTypeImpl(db, getOrCreateByDescriptor(new BinaryEdgeTypeNodeDescriptor(db, relType.name(), getImplementationClass(), domain, range)).getId());
		return new BinaryEdgeTypeImpl(db, vertexType.getNode().getId());
	}

	
	public static BinaryEdgeTypeImpl getOrCreateInstance(DatabaseService db, RelationshipType relType){
		VertexTypeImpl vertexType = new VertexTypeImpl(db, getOrCreateByDescriptor(new TypeNodeDescriptor(db, relType.name(), getImplementationClass())).getId());
		return new BinaryEdgeTypeImpl(db, vertexType.getNode().getId());
	}

	public RelationshipType getRelationshipType() {
		return DynamicRelationshipType.withName(getName());
	}

	
	@Override
	public Set<ConnectorType<?>> getConnectorTypes() {
		Set<ConnectorType<?>> connectorTypes = new HashSet<ConnectorType<?>>();
		connectorTypes.add(getStartConnectorType());
		connectorTypes.add(getEndConnectorType());
		return connectorTypes;
	}

	@Override
	public BinaryEdge createEdge(Vertex startVertex, Vertex endVertex) {
		return new BinaryEdgeImpl(db, startVertex.getNode().createRelationshipTo(endVertex.getNode(),
				this.getRelationshipType()).getId());
	}

	@Override
	public Iterable<BinaryEdge> getEdges(Vertex vertex) {
		return new RelationshipIterable(vertex.getNode().getRelationships(this.getRelationshipType()));
	}

	@Override
	public Iterable<BinaryEdge> getEdges(Vertex vertex, Direction dir) {
		return new RelationshipIterable(vertex.getNode().getRelationships(this.getRelationshipType(), dir));
	}

	@Override
	public BinaryEdge getSingleBinaryEdge(Vertex vertex, Direction dir) {
		Relationship rel = vertex.getNode().getSingleRelationship(getRelationshipType(), dir);
		if (rel == null) {
			return null;
		} else {
			return new BinaryEdgeImpl(db, rel.getId());
		}
	}

	@Override
	public boolean hasEdge(Vertex vertex, Direction dir) {
		return vertex.getNode().hasRelationship(getRelationshipType(), dir);
	}

	@Override
	public ConnectorType<SurjectiveConnectionMode> getStartConnectorType() {
		return ConnectorTypeImpl.getOrCreateInstance(db, STARTCONNECTORNAME, this.getNode(), ConnectionMode.SURJECTIVE);
	}

	@Override
	public ConnectorType<SurjectiveConnectionMode> getEndConnectorType() {
		return ConnectorTypeImpl.getOrCreateInstance(db, ENDCONNECTORNAME, this.getNode(), ConnectionMode.SURJECTIVE);
	}

	@Override
	public boolean hasEdge(Vertex vertex) {
		return vertex.getNode().hasRelationship(getRelationshipType());
	}
	
}
