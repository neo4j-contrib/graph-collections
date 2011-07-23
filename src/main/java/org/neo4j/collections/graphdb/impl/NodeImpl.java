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
import java.util.Iterator;

import org.neo4j.collections.graphdb.GraphDatabaseService;
import org.neo4j.collections.graphdb.Node;
import org.neo4j.collections.graphdb.PropertyContainer;
import org.neo4j.collections.graphdb.Relationship;
import org.neo4j.collections.graphdb.Traverser;

import org.neo4j.graphdb.Direction;

import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.ReturnableEvaluator;
import org.neo4j.graphdb.StopEvaluator;
import org.neo4j.graphdb.Traverser.Order;

public class NodeImpl extends NodeLikeImpl implements Node{
	
	final org.neo4j.graphdb.Node node;
	
	public NodeImpl(org.neo4j.graphdb.Node node){
		this.node = node;
	}

	@Override
	public void delete() {
		node.delete();
	}

	@Override
	public long getId() {
		return node.getId();
	}

	@Override
	public Traverser traverse(Order order, StopEvaluator stopEvaluator,
			ReturnableEvaluator returnableEvaluator, Object... relTypesAndDirections) {
		return new TraverserImpl(node.traverse(order, stopEvaluator, returnableEvaluator, relTypesAndDirections));
	}

	@Override
	public Traverser traverse(Order order, StopEvaluator stopEvaluator,
			ReturnableEvaluator returnableEvaluator, RelationshipType relType, Direction dir) {
		return new TraverserImpl(node.traverse(order, stopEvaluator,	returnableEvaluator, relType, dir));
	}

	@Override
	public Traverser traverse(Order order, StopEvaluator stopEvaluator,
			ReturnableEvaluator returnableEvaluator, RelationshipType relType1, Direction dir1,
			RelationshipType relType2, Direction dir2) {
		return new TraverserImpl(node.traverse(order, stopEvaluator, returnableEvaluator, relType1, dir1, relType2, dir2));
	}

/*	
	@Override
	public GraphDatabaseService getGraphDatabase() {
		return new GraphDatabaseImpl(node.getGraphDatabase());
	}
*/	


	@Override
	public org.neo4j.graphdb.Node getNode() {
		return node;
	}
	
	@Override
	public org.neo4j.graphdb.PropertyContainer getPropertyContainer() {
		return node;
	}

	@Override
	public GraphDatabaseService getGraphDatabase() {
		return new GraphDatabaseImpl(node.getGraphDatabase());
	}

	@Override
	public Node startNode() {
		return this;
	}

	@Override
	public Node endNode() {
		return this;
	}

	@Override
	public Relationship lastRelationship() {
		return null;
	}

	@Override
	public Iterable<Relationship> relationships() {
		return new ArrayList<Relationship>();
	}

	@Override
	public Iterable<Node> nodes() {
		ArrayList<Node> nodes = new ArrayList<Node>();
		nodes.add(this);
		return nodes;
	}

	@Override
	public int length() {
		return 0;
	}

	@Override
	public Iterator<PropertyContainer> iterator() {
		ArrayList<PropertyContainer> nodes = new ArrayList<PropertyContainer>();
		nodes.add(this);
		return nodes.iterator();
	}

}
