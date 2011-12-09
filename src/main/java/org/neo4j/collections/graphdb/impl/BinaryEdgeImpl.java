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

import java.util.ArrayList;

import org.neo4j.collections.graphdb.BinaryEdge;
import org.neo4j.collections.graphdb.BinaryEdgeType;
import org.neo4j.collections.graphdb.ConnectionMode;
import org.neo4j.collections.graphdb.Connector;
import org.neo4j.collections.graphdb.ConnectorType;
import org.neo4j.collections.graphdb.DatabaseService;
import org.neo4j.collections.graphdb.EdgeType;
import org.neo4j.collections.graphdb.LeftRestrictedConnectionMode;
import org.neo4j.collections.graphdb.Vertex;
import org.neo4j.collections.graphdb.VertexType;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Node;

public class BinaryEdgeImpl extends EdgeImpl implements BinaryEdge{

	private Node node;
	
	public static String NODE_ID = "org.neo4j.collections.graphdb.node_id";
	public static String REL_ID = "org.neo4j.collections.graphdb.rel_id";
	
	BinaryEdgeImpl(DatabaseService db, Long id){
		super(db, id);
	}

	@Override
	public void delete() {
		getRelationship().delete();
		if(node != null){
			node.delete();
		}
	}
	@Override
	public org.neo4j.graphdb.Relationship getRelationship() {
		return db.getRelationshipById(id);
	}

	
	@Override
	protected VertexType getSpecialVertexType(){
		return getType();
	}

	@Override
	public BinaryEdgeType getType() {
		return BinaryEdgeTypeImpl.getOrCreateInstance(getDb(), getRelationship().getType());
	}

	@Override
	public boolean isType(EdgeType relType) {
		return getRelationship().isType(DynamicRelationshipType.withName(relType.getName()));
	}

	@Override
	public DatabaseService getDb() {
		return new GraphDatabaseImpl(getRelationship().getGraphDatabase());
	}

	@Override
	public Node getNode() {
		if(node == null){
			if(getRelationship().hasProperty(NODE_ID)){
				node = getDb().getGraphDatabaseService().getNodeById((Long)getRelationship().getProperty(NODE_ID));
			}else{
				node = getDb().getGraphDatabaseService().createNode();
				node.setProperty(REL_ID, getRelationship().getId());
				getRelationship().setProperty(NODE_ID, node.getId());
			}
		}
		return node;
	}

	@Override
	public org.neo4j.graphdb.PropertyContainer getPropertyContainer() {
		return getRelationship();
	}

	@Override
	public Iterable<Connector<?>> getConnectors(){
		ArrayList<Connector<?>> connectors = new ArrayList<Connector<?>>();
		connectors.add(Connector.getInstance(getType().getStartConnectorType(), this));
		connectors.add(Connector.getInstance(getType().getEndConnectorType(), this));
		return connectors;
	}

	@Override
	public <T extends ConnectionMode> Iterable<Vertex> getVertices(ConnectorType<T> connectorType) {
		ArrayList<Vertex> elements = new ArrayList<Vertex>();
		if(connectorType.getName().equals(getType().getStartConnectorType())){
			elements.add(getDb().getVertex(getRelationship().getStartNode()));
			return elements;
		}else if(connectorType.getName().equals(getType().getEndConnectorType().getName())){
			elements.add(getDb().getVertex(getRelationship().getEndNode()));
			return elements;
		}else{
			return elements;
		}
	}

	@Override
	public <U extends LeftRestrictedConnectionMode>Vertex getVertex(ConnectorType<U> connectorType) {
		if(connectorType.getName().equals(getType().getStartConnectorType().getName())){
			return getDb().getVertex(getRelationship().getStartNode());
		}else if(connectorType.getName().equals(getType().getEndConnectorType().getName())){
			return getDb().getVertex(getRelationship().getEndNode());
		}else{
			return null;
		}
	}
	
	
	@Override
	public Vertex getEndVertex() {
		return getDb().getVertex(getRelationship().getEndNode());
	}

	@Override
	public Vertex getStartVertex() {
		return getDb().getVertex(getRelationship().getStartNode());
	}

	@Override
	public Iterable<Connector<?>> getConnectors(
			ConnectorType<?>... connectorTypes) {
		boolean includeStart = false;
		boolean includeEnd = false;
		for(ConnectorType<?> connectorType: connectorTypes){
			if(connectorType.getName().equals(getType().getStartConnectorType().getName())){
				includeStart = true;
			}else if(connectorType.getName().equals(getType().getEndConnectorType().getName())){
				includeEnd = true;
			}
		}
		ArrayList<Connector<?>> connectors = new ArrayList<Connector<?>>();
		if(includeStart){
			connectors.add(Connector.getInstance(getType().getStartConnectorType(), this));
		}
		if(includeEnd){
			connectors.add(Connector.getInstance(getType().getEndConnectorType(), this));
		}
		return connectors;
	}

	@Override
	public Vertex getOtherVertex(Vertex vertex) {
		return getDb().getVertex(getRelationship().getOtherNode(vertex.getNode()));
	}
	
}
