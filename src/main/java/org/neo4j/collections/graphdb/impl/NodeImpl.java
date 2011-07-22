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
import org.neo4j.collections.graphdb.Property;
import org.neo4j.collections.graphdb.PropertyType;
import org.neo4j.collections.graphdb.Relationship;
import org.neo4j.collections.graphdb.RelationshipContainer;

import org.neo4j.graphdb.Direction;

import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.ReturnableEvaluator;
import org.neo4j.graphdb.StopEvaluator;
import org.neo4j.graphdb.Traverser;
import org.neo4j.graphdb.Traverser.Order;

public class NodeImpl implements Node{
	
	final org.neo4j.graphdb.Node node;
	
	NodeImpl(org.neo4j.graphdb.Node node){
		this.node = node;
	}

	@Override
	public Relationship createRelationshipToExt(RelationshipContainer rc,
			RelationshipType rt) {
		return new RelationshipImpl(getNode().createRelationshipTo(rc.getNode(), rt));
	}

	@Override
	public void delete() {
		getNode().delete();
	}

	@Override
	public long getId() {
		return getNode().getId();
	}

	@Override
	public Iterable<Relationship> getRelationshipsExt() {
		return new RelationshipIterable(getNode().getRelationships());
	}

	@Override
	public Iterable<Relationship> getRelationshipsExt(RelationshipType... arg0) {
		return new RelationshipIterable(getNode().getRelationships( arg0)) ;
	}

	@Override
	public Iterable<Relationship> getRelationshipsExt(Direction arg0) {
		return new RelationshipIterable(getNode().getRelationships(arg0));
	}

	@Override
	public Iterable<Relationship> getRelationshipsExt(Direction arg0,
			RelationshipType... arg1) {
		return new RelationshipIterable(getNode().getRelationships(arg0, arg1));
	}

	@Override
	public Iterable<Relationship> getRelationshipsExt(RelationshipType arg0,
			Direction arg1) {
		return new RelationshipIterable(getNode().getRelationships(arg0, arg1));
	}

	@Override
	public Relationship getSingleRelationshipExt(RelationshipType arg0,
			Direction arg1) {
		return new RelationshipImpl(getNode().getSingleRelationship(arg0, arg1));
	}

	@Override
	public boolean hasRelationship() {
		return getNode().hasRelationship();
	}

	@Override
	public boolean hasRelationship(RelationshipType... arg0) {
		return getNode().hasRelationship(arg0);
	}

	@Override
	public boolean hasRelationship(Direction arg0) {
		return getNode().hasRelationship(arg0);
	}

	@Override
	public boolean hasRelationship(Direction arg0, RelationshipType... arg1) {
		return getNode().hasRelationship(arg0, arg1);
	}

	@Override
	public boolean hasRelationship(RelationshipType arg0, Direction arg1) {
		return getNode().hasRelationship(arg0, arg1);
	}

	@Override
	public Traverser traverse(Order arg0, StopEvaluator arg1,
			ReturnableEvaluator arg2, Object... arg3) {
		return getNode().traverse(arg0, arg1, arg2, arg3);
	}

	@Override
	public Traverser traverse(Order arg0, StopEvaluator arg1,
			ReturnableEvaluator arg2, RelationshipType arg3, Direction arg4) {
		return getNode().traverse(arg0, arg1,	arg2, arg3, arg4);
	}

	@Override
	public Traverser traverse(Order arg0, StopEvaluator arg1,
			ReturnableEvaluator arg2, RelationshipType arg3, Direction arg4,
			RelationshipType arg5, Direction arg6) {
		return getNode().traverse(arg0, arg1, arg2, arg3, arg4, arg5, arg6);
	}

	@Override
	public GraphDatabaseService getGraphDatabaseExt() {
		return new GraphDatabaseImpl(getNode().getGraphDatabase());
	}

	@Override
	public Object getProperty(String arg0) {
		return getNode().getProperty(arg0);
	}

	@Override
	public Object getProperty(String arg0, Object arg1) {
		return getNode().getProperty(arg0, arg1);
	}

	@Override
	public Iterable<String> getPropertyKeys() {
		return getNode().getPropertyKeys();
	}

	@Deprecated
	public Iterable<Object> getPropertyValues() {
		return getNode().getPropertyValues();
	}

	@Override
	public boolean hasProperty(String arg0) {
		return getNode().hasProperty(arg0);
	}

	@Override
	public Object removeProperty(String arg0) {
		return getNode().removeProperty(arg0);
	}

	@Override
	public void setProperty(String arg0, Object arg1) {
		getNode().setProperty(arg0, arg1);
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
	public <T> Property<T> getProperty(PropertyType<T> pt) {
		return new PropertyImpl<T>(new NodeImpl(node), pt);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getPropertyValue(PropertyType<T> pt) {
		return (T)node.getProperty(pt.getName());
	}

	@Override
	public <T> boolean hasProperty(PropertyType<T> pt) {
		return node.hasProperty(pt.getName());
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T removeProperty(PropertyType<T> pt) {
		return (T)node.removeProperty(pt.getName());
	}

	@Override
	public <T> void setProperty(PropertyType<T> pt, T value) {
		node.setProperty(pt.getName(), value);
	}

	@Override
	public org.neo4j.graphdb.GraphDatabaseService getGraphDatabase() {
		return node.getGraphDatabase();
	}

	@Override
	public org.neo4j.graphdb.Relationship createRelationshipTo(
			org.neo4j.graphdb.Node arg0, RelationshipType arg1) {
		return node.createRelationshipTo(arg0, arg1);
	}

	@Override
	public Iterable<org.neo4j.graphdb.Relationship> getRelationships() {
		return node.getRelationships();
	}

	@Override
	public Iterable<org.neo4j.graphdb.Relationship> getRelationships(
			RelationshipType... arg0) {
		return node.getRelationships();
	}

	@Override
	public Iterable<org.neo4j.graphdb.Relationship> getRelationships(
			Direction arg0) {
		return node.getRelationships(arg0);
	}

	@Override
	public Iterable<org.neo4j.graphdb.Relationship> getRelationships(
			Direction arg0, RelationshipType... arg1) {
		return node.getRelationships(arg0, arg1);
	}

	@Override
	public Iterable<org.neo4j.graphdb.Relationship> getRelationships(
			RelationshipType arg0, Direction arg1) {
		return node.getRelationships(arg0, arg1);
	}

	@Override
	public org.neo4j.graphdb.Relationship getSingleRelationship(
			RelationshipType arg0, Direction arg1) {
		return node.getSingleRelationship(arg0, arg1);
	}

	@Override
	public Iterable<PropertyType<?>> getPropertyTypes() {
		return PropertyType.getPropertyTypes(this, getGraphDatabaseExt());
	}
	
}
