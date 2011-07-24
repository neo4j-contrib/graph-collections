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

import org.neo4j.collections.graphdb.Element;
import org.neo4j.collections.graphdb.HyperRelationship;
import org.neo4j.collections.graphdb.HyperRelationshipType;
import org.neo4j.collections.graphdb.GraphDatabaseService;
import org.neo4j.collections.graphdb.Node;
import org.neo4j.collections.graphdb.PropertyType;
import org.neo4j.collections.graphdb.Relationship;
import org.neo4j.collections.graphdb.RelationshipElement;
import org.neo4j.collections.graphdb.wrappers.IndexManager;
import org.neo4j.collections.graphdb.PropertyType.ComparablePropertyType;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.event.KernelEventHandler;
import org.neo4j.graphdb.event.TransactionEventHandler;


public class GraphDatabaseImpl implements GraphDatabaseService{
	
	private final org.neo4j.graphdb.GraphDatabaseService graphDb;
	
	GraphDatabaseImpl(org.neo4j.graphdb.GraphDatabaseService graphDb){
		this.graphDb = graphDb;
	}
	
	public org.neo4j.graphdb.GraphDatabaseService getGraphDatabaseService(){
		return graphDb;
	}
	@Override
	public Transaction beginTx() {
		return getGraphDatabaseService().beginTx();
	}

	@Override
	public Node createNode() {
		return new NodeImpl(getGraphDatabaseService().createNode());
	}

	@Override
	public Iterable<Node> getAllNodes() {
		return new NodeIterable(getGraphDatabaseService().getAllNodes());
	}

	@Override
	public Node getNodeById(long arg0) {
		return new NodeImpl(getGraphDatabaseService().getNodeById(arg0));
	}

	@Override
	public Node getReferenceNode() {
		return new NodeImpl(getGraphDatabaseService().getReferenceNode());
	}

	@Override
	public Relationship getRelationshipById(long arg0) {
		return new RelationshipImpl(getGraphDatabaseService().getRelationshipById(arg0));
	}

	@Override
	public Iterable<HyperRelationshipType> getRelationshipTypes() {
		return new RelationshipTypeIterable(graphDb.getRelationshipTypes(), this);
	}

	@Override
	public IndexManager index() {
		return new IndexManagerImpl(graphDb.index());
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
		return new PropertyType.BooleanPropertyType(name, this);
	}

	@Override
	public PropertyType<Boolean[]> getBooleanArrayPropertyType(String name) {
		return new PropertyType.BooleanArrayPropertyType(name, this);
	}

	@Override
	public ComparablePropertyType<Byte> getBytePropertyType(String name) {
		return new PropertyType.BytePropertyType(name, this);
	}

	@Override
	public PropertyType<Byte[]> getByteArrayPropertyType(String name) {
		return new PropertyType.ByteArrayPropertyType(name, this);
	}

	@Override
	public ComparablePropertyType<Double> getDoublePropertyType(String name) {
		return new PropertyType.DoublePropertyType(name, this);
	}

	@Override
	public PropertyType<Double[]> getDoubleArrayPropertyType(String name) {
		return new PropertyType.DoubleArrayPropertyType(name, this);
	}

	@Override
	public ComparablePropertyType<Float> getFloatPropertyType(String name) {
		return new PropertyType.FloatPropertyType(name, this);	}

	@Override
	public PropertyType<Float[]> getFloatArrayPropertyType(String name) {
		return new PropertyType.FloatArrayPropertyType(name, this);
	}

	@Override
	public ComparablePropertyType<Long> getLongPropertyType(String name) {
		return new PropertyType.LongPropertyType(name, this);
	}

	@Override
	public PropertyType<Long[]> getLongArrayPropertyType(String name) {
		return new PropertyType.LongArrayPropertyType(name, this);
	}

	@Override
	public ComparablePropertyType<Short> getShortPropertyType(String name) {
		return new PropertyType.ShortPropertyType(name, this);
	}

	@Override
	public PropertyType<Short[]> getShortArrayPropertyType(String name) {
		return new PropertyType.ShortArrayPropertyType(name, this);
	}

	@Override
	public ComparablePropertyType<String> getStringPropertyType(String name) {
		return new PropertyType.StringPropertyType(name, this);
	}

	@Override
	public PropertyType<String[]> getStringArrayPropertyType(String name) {
		return new PropertyType.StringArrayPropertyType(name, this);
	}

	@Override
	public HyperRelationshipType getRelationshipType(String name) {
		return new RelationshipTypeImpl(DynamicRelationshipType.withName(name), this);
	}

	@Override
	public RelationshipType getRelationshipType(RelationshipType relType) {
		return new RelationshipTypeImpl(relType, this);	
	}

	@Override
	public HyperRelationship createRelationship(
			Set<RelationshipElement<? extends Element>> relationshipElements) {
		// TODO Auto-generated method stub
		return null;
	}
}
