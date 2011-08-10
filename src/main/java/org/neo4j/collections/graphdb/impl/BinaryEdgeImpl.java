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

import java.util.ArrayList;

import org.neo4j.collections.graphdb.BinaryEdge;
import org.neo4j.collections.graphdb.BinaryEdgeType;
import org.neo4j.collections.graphdb.ConnectionMode;
import org.neo4j.collections.graphdb.DatabaseService;
import org.neo4j.collections.graphdb.EdgeElement;
import org.neo4j.collections.graphdb.ConnectorType;
import org.neo4j.collections.graphdb.EdgeType;
import org.neo4j.collections.graphdb.InjectiveConnectionMode;
import org.neo4j.collections.graphdb.LeftRestrictedConnectionMode;
import org.neo4j.collections.graphdb.LeftRestricedEdgeElement;
import org.neo4j.collections.graphdb.Vertex;
import org.neo4j.collections.graphdb.VertexType;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

public class BinaryEdgeImpl extends EdgeImpl implements BinaryEdge{

	private Node node;
	
	protected final Relationship rel;
	public static String NODE_ID = "org.neo4j.collections.graphdb.node_id";
	public static String REL_ID = "org.neo4j.collections.graphdb.rel_id";
	
	BinaryEdgeImpl(Relationship rel){
		super(null);
		this.rel = rel;
	}

	@Override
	public void delete() {
		rel.delete();
		if(node != null){
			node.delete();
		}
	}
	@Override
	public org.neo4j.graphdb.Relationship getRelationship() {
		return rel;
	}

	@Override
	public Vertex[] getVertices() {
		org.neo4j.graphdb.Node[] nodes = rel.getNodes();
		Vertex[] enodes = new Vertex[nodes.length];
		int count = 0;
		for(org.neo4j.graphdb.Node n: nodes){
			enodes[count] = new VertexImpl(n);
			count++;
		}
		return enodes;
	}

	
	@Override
	protected VertexType getSpecialVertexType(){
		return getType();
	}

	@Override
	public BinaryEdgeType getType() {
		return BinaryEdgeTypeImpl.getOrCreateInstance(getDb(), rel.getType());
	}

	@Override
	public boolean isType(EdgeType relType) {
		return rel.isType(DynamicRelationshipType.withName(relType.getName()));
	}

	@Override
	public DatabaseService getDb() {
		return new GraphDatabaseImpl(rel.getGraphDatabase());
	}

	@Override
	public Node getNode() {
		if(node == null){
			if(rel.hasProperty(NODE_ID)){
				node = getDb().getGraphDatabaseService().getNodeById((Long)rel.getProperty(NODE_ID));
			}else{
				node = getDb().getGraphDatabaseService().createNode();
				node.setProperty(REL_ID, rel.getId());
				rel.setProperty(NODE_ID, node.getId());
			}
		}
		return node;
	}

	@Override
	public org.neo4j.graphdb.PropertyContainer getPropertyContainer() {
		return rel;
	}

	@Override
	public Iterable<EdgeElement> getEdgeElements(){
		ArrayList<EdgeElement> relements = new ArrayList<EdgeElement>();
		relements.add(new LeftRestricedEdgeElement(getType().getStartConnector().getConnectorType(), getStartVertex()));
		relements.add(new LeftRestricedEdgeElement(getType().getEndConnector().getConnectorType(), getEndVertex()));
		return relements;
	}

	@Override
	public <T extends ConnectionMode> Iterable<Vertex> getVertices(ConnectorType<T> connectorType) {
		ArrayList<Vertex> elements = new ArrayList<Vertex>();
		if(connectorType.getName().equals(getType().getStartConnector().getName())){
			elements.add(getDb().getVertex(rel.getStartNode()));
			return elements;
		}else if(connectorType.getName().equals(getType().getEndConnector().getName())){
			elements.add(getDb().getVertex(rel.getEndNode()));
			return elements;
		}else{
			throw new RuntimeException("Supplied role is not supported");
		}
	}

	@Override
	public <U extends LeftRestrictedConnectionMode>Vertex getVertex(ConnectorType<U> connectorType) {
		if(connectorType.getName().equals(getType().getStartConnector().getName())){
			return getDb().getVertex(rel.getStartNode());
		}else if(connectorType.getName().equals(getType().getEndConnector().getName())){
			return getDb().getVertex(rel.getEndNode());
		}else{
			throw new RuntimeException("Supplied role is not supported");
		}
	}
	
	
	@Override
	public Vertex getEndVertex() {
		return getDb().getVertex(rel.getEndNode());
	}

	@Override
	public Vertex getStartVertex() {
		return getDb().getVertex(rel.getStartNode());
	}

	@Override
	public Iterable<EdgeElement> getEdgeElements(
			ConnectorType<?>... connectorTypes) {
		boolean includeStart = false;
		boolean includeEnd = false;
		for(ConnectorType<?> connectorType: connectorTypes){
			if(connectorType.getName().equals(getType().getStartConnector().getName())){
				includeStart = true;
			}else if(connectorType.getName().equals(getType().getEndConnector().getName())){
				includeEnd = true;
			}else{
				throw new RuntimeException("Supplied role is not part of this RelationshipType");
			}
		}
		ArrayList<EdgeElement> relements = new ArrayList<EdgeElement>();
		if(includeStart){
			relements.add(new LeftRestricedEdgeElement(getType().getStartConnector().getConnectorType(), getStartVertex()));
		}
		if(includeEnd){
			relements.add(new LeftRestricedEdgeElement(getType().getEndConnector().getConnectorType(), getEndVertex()));
		}
		return relements;
	}

	@Override
	public Vertex getOtherVertex(Vertex vertex) {
		return getDb().getVertex(rel.getOtherNode(vertex.getNode()));
	}
	
}
