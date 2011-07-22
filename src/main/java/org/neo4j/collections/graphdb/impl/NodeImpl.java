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

import org.neo4j.collections.graphdb.GraphDatabaseService;
import org.neo4j.collections.graphdb.Node;
import org.neo4j.collections.graphdb.Relationship;
import org.neo4j.collections.graphdb.RelationshipContainer;

import org.neo4j.graphdb.Direction;

import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.ReturnableEvaluator;
import org.neo4j.graphdb.StopEvaluator;
import org.neo4j.graphdb.Traverser;
import org.neo4j.graphdb.Traverser.Order;

public class NodeImpl extends NodeLikeImpl implements Node{
	
	final org.neo4j.graphdb.Node node;
	
	NodeImpl(org.neo4j.graphdb.Node node){
		this.node = node;
	}

	@Override
	public Relationship createRelationshipToExt(RelationshipContainer rc,
			RelationshipType rt) {
		return new RelationshipImpl(node.createRelationshipTo(rc.getNode(), rt));
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
		return node.traverse(order, stopEvaluator, returnableEvaluator, relTypesAndDirections);
	}

	@Override
	public Traverser traverse(Order order, StopEvaluator stopEvaluator,
			ReturnableEvaluator returnableEvaluator, RelationshipType relType, Direction dir) {
		return node.traverse(order, stopEvaluator,	returnableEvaluator, relType, dir);
	}

	@Override
	public Traverser traverse(Order order, StopEvaluator stopEvaluator,
			ReturnableEvaluator returnableEvaluator, RelationshipType relType1, Direction dir1,
			RelationshipType relType2, Direction dir2) {
		return node.traverse(order, stopEvaluator, returnableEvaluator, relType1, dir1, relType2, dir2);
	}

	@Override
	public GraphDatabaseService getGraphDatabaseExt() {
		return new GraphDatabaseImpl(node.getGraphDatabase());
	}

	@Override
	public Object getProperty(String key) {
		return node.getProperty(key);
	}

	@Override
	public Object getProperty(String key, Object defaultValue) {
		return node.getProperty(key, defaultValue);
	}

	@Override
	public Iterable<String> getPropertyKeys() {
		return node.getPropertyKeys();
	}

	@Deprecated
	public Iterable<Object> getPropertyValues() {
		return node.getPropertyValues();
	}

	@Override
	public boolean hasProperty(String key) {
		return node.hasProperty(key);
	}

	@Override
	public Object removeProperty(String key) {
		return node.removeProperty(key);
	}

	@Override
	public void setProperty(String key, Object value) {
		node.setProperty(key, value);
	}

	@Override
	public org.neo4j.graphdb.Node getNode() {
		return node;
	}
	
	@Override
	public org.neo4j.graphdb.PropertyContainer getPropertyContainer() {
		return node;
	}

	@Override
	public org.neo4j.graphdb.GraphDatabaseService getGraphDatabase() {
		return node.getGraphDatabase();
	}

	@Override
	public org.neo4j.graphdb.Relationship createRelationshipTo(
			org.neo4j.graphdb.Node node, RelationshipType relType) {
		return node.createRelationshipTo(node, relType);
	}

	@Override
	public Iterable<org.neo4j.graphdb.Relationship> getRelationships() {
		return node.getRelationships();
	}

	@Override
	public Iterable<org.neo4j.graphdb.Relationship> getRelationships(
			RelationshipType... relTypes) {
		return node.getRelationships(relTypes);
	}

	@Override
	public Iterable<org.neo4j.graphdb.Relationship> getRelationships(
			Direction dir) {
		return node.getRelationships(dir);
	}

	@Override
	public Iterable<org.neo4j.graphdb.Relationship> getRelationships(
			Direction dir, RelationshipType... relTypes) {
		return node.getRelationships(dir, relTypes);
	}

	@Override
	public Iterable<org.neo4j.graphdb.Relationship> getRelationships(
			RelationshipType relType, Direction dir) {
		return node.getRelationships(relType, dir);
	}

	@Override
	public org.neo4j.graphdb.Relationship getSingleRelationship(
			RelationshipType relType, Direction dir) {
		return node.getSingleRelationship(relType, dir);
	}
}
