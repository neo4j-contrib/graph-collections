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

import org.neo4j.collections.graphdb.BinaryEdge;
import org.neo4j.collections.graphdb.BinaryEdgeType;
import org.neo4j.collections.graphdb.ConnectionMode;
import org.neo4j.collections.graphdb.ConnectorType;
import org.neo4j.collections.graphdb.DatabaseService;
import org.neo4j.collections.graphdb.Connector;
import org.neo4j.collections.graphdb.SurjectiveConnectionMode;
import org.neo4j.collections.graphdb.Vertex;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

public class BinaryEdgeTypeImpl extends EdgeTypeImpl implements BinaryEdgeType{

	public BinaryEdgeTypeImpl(Node node) {
		super(node);
	}

	protected static Class<?> getImplementationClass(){
		try{
			return Class.forName("org.neo4j.collections.graphdb.impl.BinaryEdgeTypeImpl");
		}catch(ClassNotFoundException cce){
			throw new RuntimeException(cce);
		}
	}

	public static BinaryEdgeTypeImpl getOrCreateInstance(DatabaseService db, RelationshipType relType){
		VertexTypeImpl vertexType = new VertexTypeImpl(getOrCreateByDescriptor(new TypeNodeDescriptor(db, relType.name(), getImplementationClass())));
		return new BinaryEdgeTypeImpl(vertexType.getNode());
	}

	public RelationshipType getRelationshipType() {
		return DynamicRelationshipType.withName(getName());
	}

	
	@Override
	public Set<Connector<?>> getConnectors() {
		Set<Connector<?>> roles = new HashSet<Connector<?>>();
		roles.add(getStartConnector());
		roles.add(getEndConnector());
		return roles;
	}

	@Override
	public <T extends ConnectionMode> Connector<T> getConnector(
			ConnectorType<T> edgeRoleType) {
		return new Connector<T>(edgeRoleType, this);
	}

	@Override
	public BinaryEdge createEdge(Vertex startVertex, Vertex endVertex) {
		return new BinaryEdgeImpl(startVertex.getNode().createRelationshipTo(endVertex.getNode(),
				this.getRelationshipType()));
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
			return new BinaryEdgeImpl(rel);
		}
	}

	@Override
	public boolean hasEdge(Vertex vertex, Direction dir) {
		return vertex.getNode().hasRelationship(getRelationshipType(), dir);
	}

	@Override
	public Connector<SurjectiveConnectionMode> getStartConnector() {
		return new Connector<SurjectiveConnectionMode>(BinaryConnectorTypeImpl.StartConnector.getOrCreateInstance(getDb()), this);
	}

	@Override
	public Connector<SurjectiveConnectionMode> getEndConnector() {
		return new Connector<SurjectiveConnectionMode>(BinaryConnectorTypeImpl.EndConnector.getOrCreateInstance(getDb()), this);
	}
	
}
