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
import org.neo4j.collections.graphdb.ConnectionMode;
import org.neo4j.collections.graphdb.ConnectorType;
import org.neo4j.collections.graphdb.DatabaseService;
import org.neo4j.collections.graphdb.EdgeElement;
import org.neo4j.collections.graphdb.EdgeType;
import org.neo4j.collections.graphdb.LeftRestricedEdgeElement;
import org.neo4j.collections.graphdb.LeftRestrictedConnectionMode;
import org.neo4j.collections.graphdb.VertexType;
import org.neo4j.graphdb.Node;
import org.neo4j.collections.graphdb.Property;
import org.neo4j.collections.graphdb.PropertyType;
import org.neo4j.collections.graphdb.Vertex;

public class PropertyImpl<T> extends EdgeImpl implements Property<T>{

	public final static String PROPERTYCONTAINER_ID = "org.neo4j.collections.graphdb.propertycontainer_id";
	public final static String PROPERTYCONTAINER_TYPE = "org.neo4j.collections.graphdb.propertycontainer_type";
	public final static String PROPERTY_NAME = "org.neo4j.collections.graphdb.property_name";

	public enum PropertyContainerType{
		NODE, RELATIONSHIP
	}
	
	private final Vertex vertex;
	private final PropertyType<T> propertyType;
	private final DatabaseService graphDb;
	private Node node;

	public PropertyImpl(DatabaseService graphDb, Vertex vertex, PropertyType<T> propertyType){
		super(null);
		this.vertex = vertex;
		this.propertyType = propertyType;
		this.graphDb = graphDb;
	}
	
	public long getId(){
		return getNode().getId();
	}
	
	@Override
	public T getValue() {
		return vertex.getPropertyValue(propertyType);
	}

	@Override
	public PropertyType<T> getPropertyType() {
		return propertyType;
	}

	@Override
	public org.neo4j.graphdb.PropertyContainer getPropertyContainer() {
		return getNode();
	}

	@Override
	public DatabaseService getDb() {
		return graphDb;
	}

	@Override
	public Iterable<PropertyType<?>> getPropertyTypes() {
		return null;
	}

	@Override
	public Node getNode() {
		if(node != null){
			return node;
		}else{
			if(vertex.getPropertyContainer().hasProperty(propertyType.getName()+".node_id")){
				return getDb().getGraphDatabaseService().getNodeById((Long)vertex.getPropertyContainer().getProperty(propertyType.getName()+".node_id"));
			}else{
				Node n = graphDb.createNode();
				n.setProperty(PROPERTYCONTAINER_ID, vertex.getNode().getId());
				n.setProperty(PROPERTY_NAME, propertyType.getName());
				if(vertex instanceof BinaryEdge){
					n.setProperty(PROPERTYCONTAINER_TYPE, PropertyContainerType.RELATIONSHIP.name());
				}else{
					n.setProperty(PROPERTYCONTAINER_TYPE, PropertyContainerType.NODE.name());
				}
				vertex.getPropertyContainer().setProperty(propertyType.getName()+".node_id", n.getId());
				return n;
			}
		}
	}

	@Override
	public Vertex getVertex() {
		return new VertexImpl(getNode());
	}

	@Override
	public void delete() {
		node.removeProperty(getType().getName());
	}

	@Override
	protected VertexType getSpecialVertexType(){
		return getType();
	}
	
	@Override
	public PropertyType<T> getType() {
		return this.propertyType;
	}

	@Override
	public boolean isType(EdgeType relType) {
		return this.propertyType.getNode().getId() == relType.getNode().getId();
	}

	@Override
	public Iterable<EdgeElement> getEdgeElements() {
		ArrayList<EdgeElement> elems = new ArrayList<EdgeElement>();
		elems.add(new LeftRestricedEdgeElement(getType().getPropertyConnector().getConnectorType(), getVertex()));
		return elems;
	}

	@Override
	public <U extends LeftRestrictedConnectionMode>Vertex getVertex(ConnectorType<U> connectorType) {
		if(connectorType.getName().equals(getDb().getPropertyRoleType().getName())){
			return getVertex();
		}else{
			return null;
		}
	}

	@Override
	public Iterable<EdgeElement> getEdgeElements(ConnectorType<?>... connectorType) {
		return getEdgeElements();
	}

	@Override
	public <U extends ConnectionMode> Iterable<Vertex> getVertices(ConnectorType<U> connectorType) {
		ArrayList<Vertex> vertices = new ArrayList<Vertex>();
		vertices.add(getVertex());
		return vertices;
	}


}
