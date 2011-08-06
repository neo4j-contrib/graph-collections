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

import java.util.Set;

import org.neo4j.collections.graphdb.BinaryEdge;
import org.neo4j.collections.graphdb.BinaryEdgeRoleType;
import org.neo4j.collections.graphdb.BinaryEdgeType;
import org.neo4j.collections.graphdb.EdgeRole;
import org.neo4j.collections.graphdb.NAryEdge;
import org.neo4j.collections.graphdb.NAryEdgeRoleType;
import org.neo4j.collections.graphdb.NAryEdgeType;
import org.neo4j.collections.graphdb.PropertyRoleType;
import org.neo4j.collections.graphdb.Vertex;
import org.neo4j.collections.graphdb.EdgeType;
import org.neo4j.collections.graphdb.DatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.collections.graphdb.PropertyType;
import org.neo4j.collections.graphdb.PropertyType.ComparablePropertyType;
import org.neo4j.graphdb.Relationship;
import org.neo4j.collections.graphdb.EdgeElement;
import org.neo4j.collections.graphdb.SortableBinaryEdgeType;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.event.KernelEventHandler;
import org.neo4j.graphdb.event.TransactionEventHandler;

public class GraphDatabaseImpl implements DatabaseService {

	public static String EDGE_TYPE = "org.neo4j.collections.graphdb.edge_type";
	
	private final org.neo4j.graphdb.GraphDatabaseService graphDb;

	GraphDatabaseImpl(org.neo4j.graphdb.GraphDatabaseService graphDb) {
		this.graphDb = graphDb;
	}

	public org.neo4j.graphdb.GraphDatabaseService getGraphDatabaseService() {
		return graphDb;
	}

	@Override
	public Transaction beginTx() {
		return getGraphDatabaseService().beginTx();
	}

	@Override
	public Vertex createVertex() {
		return new VertexImpl(getGraphDatabaseService().createNode());
	}

	@Override
	public Iterable<Vertex> getAllVertices() {
		return new NodeIterable(getGraphDatabaseService().getAllNodes());
	}

/*	
	@Override
	public Vertex getVertexById(long id) {
		return getVertex(getGraphDatabaseService().getNodeById(id));
	}
*/
	
	@Override
	public Vertex getReferenceVertex() {
		return new VertexImpl(getGraphDatabaseService().getReferenceNode());
	}

	@Override
	public BinaryEdge getBinaryEdgeById(long arg0) {
		return new BinaryEdgeImpl(getGraphDatabaseService()
				.getRelationshipById(arg0));
	}

	@Override
	public Iterable<EdgeType<?>> getEdgeTypes() {
		return new RelationshipTypeIterable(graphDb.getRelationshipTypes(),
				this);
	}

	@Override
	public IndexManager index() {
		return graphDb.index();
	}

	@Override
	public KernelEventHandler registerKernelEventHandler(KernelEventHandler arg0) {
		return graphDb.registerKernelEventHandler(arg0);
	}

	@Override
	public <T> TransactionEventHandler<T> registerTransactionEventHandler(
			TransactionEventHandler<T> arg0) {
		return graphDb.registerTransactionEventHandler(arg0);
	}

	@Override
	public void shutdown() {
		graphDb.shutdown();

	}

	@Override
	public KernelEventHandler unregisterKernelEventHandler(
			KernelEventHandler arg0) {
		return graphDb.unregisterKernelEventHandler(arg0);
	}

	@Override
	public <T> TransactionEventHandler<T> unregisterTransactionEventHandler(
			TransactionEventHandler<T> arg0) {
		return graphDb.unregisterTransactionEventHandler(arg0);
	}

	@Override
	public PropertyType<Boolean> getBooleanPropertyType(String name) {
		return PropertyType.BooleanPropertyType.getOrCreateInstance(this, name);
	}

	@Override
	public PropertyType<Boolean[]> getBooleanArrayPropertyType(String name) {
		return PropertyType.BooleanArrayPropertyType.getOrCreateInstance(this, name);
	}

	@Override
	public ComparablePropertyType<Byte> getBytePropertyType(String name) {
		return PropertyType.BytePropertyType.getOrCreateInstance(this, name);
	}

	@Override
	public PropertyType<Byte[]> getByteArrayPropertyType(String name) {
		return PropertyType.ByteArrayPropertyType.getOrCreateInstance(this, name);
	}

	@Override
	public ComparablePropertyType<Double> getDoublePropertyType(String name) {
		return PropertyType.DoublePropertyType.getOrCreateInstance(this, name);
	}

	@Override
	public PropertyType<Double[]> getDoubleArrayPropertyType(String name) {
		return PropertyType.DoubleArrayPropertyType.getOrCreateInstance(this, name);
	}

	@Override
	public ComparablePropertyType<Float> getFloatPropertyType(String name) {
		return PropertyType.FloatPropertyType.getOrCreateInstance(this, name);
	}

	@Override
	public PropertyType<Float[]> getFloatArrayPropertyType(String name) {
		return PropertyType.FloatArrayPropertyType.getOrCreateInstance(this, name);
	}

	@Override
	public ComparablePropertyType<Long> getLongPropertyType(String name) {
		return PropertyType.LongPropertyType.getOrCreateInstance(this, name);
	}

	@Override
	public PropertyType<Long[]> getLongArrayPropertyType(String name) {
		return PropertyType.LongArrayPropertyType.getOrCreateInstance(this, name);
	}

	@Override
	public ComparablePropertyType<Short> getShortPropertyType(String name) {
		return PropertyType.ShortPropertyType.getOrCreateInstance(this, name);
	}

	@Override
	public PropertyType<Short[]> getShortArrayPropertyType(String name) {
		return PropertyType.ShortArrayPropertyType.getOrCreateInstance(this, name);
	}

	@Override
	public ComparablePropertyType<String> getStringPropertyType(String name) {
		return PropertyType.StringPropertyType.getOrCreateInstance(this, name);
	}

	@Override
	public PropertyType<String[]> getStringArrayPropertyType(String name) {
		return PropertyType.StringArrayPropertyType.getOrCreateInstance(this, name);
	}

	@Override
	public NAryEdge createEdge(NAryEdgeType edgeType,
			EdgeElement... edgeElements) {
		if(edgeElements.length != edgeType.getRoles().size()){
			throw new RuntimeException("Number of edge elements provided ("+edgeElements.length+") is different from the number of edge roles required ("+edgeType.getRoles().size()+")");
		}
		for(EdgeRole<?,?> role: edgeType.getRoles()){
			boolean found = false;
			for(EdgeElement relement: edgeElements){
				if(relement.getRole().getName().equals(role.getName())){
					found = true;
				}
			}
			if(found == false){
				throw new RuntimeException("To create relationship an element with role "+role.getName()+" should be provide");
			}
		}
		Node n = graphDb.createNode();
		n.setProperty(EDGE_TYPE, edgeType.getNode().getId());
		for(EdgeElement relement: edgeElements){
			for(Vertex elem: relement.getVertices()){
				n.createRelationshipTo(elem.getNode(), DynamicRelationshipType.withName(edgeType.getName()+VertexImpl.EDGEROLE_SEPARATOR+relement.getRole().getName()));
			}
		}
		return new NAryEdgeImpl(n);
	}


	@Override
	public Vertex getVertex(Node node) {
		if(node.hasProperty(VertexTypeImpl.CLASS_NAME)){
			try{
				@SuppressWarnings("unchecked")
				Class<Vertex> claz = (Class<Vertex>)Class.forName((String)node.getProperty(VertexTypeImpl.CLASS_NAME));
				@SuppressWarnings("unchecked")
				Class<Node> nclaz = (Class<Node>)Class.forName("org.neo4j.graphdb.Node");
				return claz.getConstructor(nclaz).newInstance(node);
			}catch (Exception e){
				throw new RuntimeException(e);
			}
		}else if(node.hasProperty(BinaryEdgeImpl.REL_ID)){
			return new BinaryEdgeImpl(graphDb.getRelationshipById((Long)node.getProperty(BinaryEdgeImpl.REL_ID)));
		}else{
			return new VertexImpl(node);
		}
	}

	@Override
	public <T> SortableBinaryEdgeType<T> getSortableRelationshipType(String name, ComparablePropertyType<T> propertyType) {
		return SortableBinaryEdgeTypeImpl.getOrCreateInstance(this, DynamicRelationshipType.withName(name), propertyType);
	}

	@Override
	public Node createNode() {
		return graphDb.createNode();
	}

	@Override
	public Node getNodeById(long id) {
		return graphDb.getNodeById(id);
	}

	@Override
	public Relationship getRelationshipById(long id) {
		return graphDb.getRelationshipById(id);
	}

	@Override
	public Node getReferenceNode() {
		return graphDb.getReferenceNode();
	}

	@Override
	public Iterable<Node> getAllNodes() {
		return graphDb.getAllNodes();
	}

	@Override
	public Iterable<RelationshipType> getRelationshipTypes() {
		return graphDb.getRelationshipTypes();
	}

	@Override
	public BinaryEdgeType getBinaryEdgeType(RelationshipType relType) {
		return BinaryEdgeTypeImpl.getOrCreateInstance(this, relType);
	}

	@Override
	public BinaryEdgeRoleType getStartElementRoleType() {
		return BinaryEdgeRoleType.StartElement.getOrCreateInstance(this);
	}

	@Override
	public BinaryEdgeRoleType getEndElementRoleType() {
		return BinaryEdgeRoleType.EndElement.getOrCreateInstance(this);
	}

	@Override
	public NAryEdgeRoleType getEdgeRoleType(String name) {
		return NAryEdgeRoleTypeImpl.getOrCreateInstance(this, name);
	}

	@Override
	public NAryEdgeType getEdgeType(String name, Set<NAryEdgeRoleType> roles) {
		return NAryEdgeTypeImpl.getOrCreateInstance(this, name, roles);
	}

	@Override
	public PropertyRoleType getPropertyRoleType() {
		return PropertyRoleType.getOrCreateInstance(this);
	}
}
