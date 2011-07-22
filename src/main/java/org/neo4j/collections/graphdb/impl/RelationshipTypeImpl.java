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

import org.neo4j.collections.graphdb.EnhancedRelationshipType;
import org.neo4j.collections.graphdb.GraphDatabaseService;
import org.neo4j.collections.graphdb.Node;
import org.neo4j.collections.graphdb.Property;
import org.neo4j.collections.graphdb.PropertyType;
import org.neo4j.collections.graphdb.Relationship;
import org.neo4j.collections.graphdb.RelationshipContainer;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.RelationshipType;

public class RelationshipTypeImpl implements EnhancedRelationshipType{

	private final org.neo4j.graphdb.RelationshipType relType;
	private final GraphDatabaseService graphDb;
	private Node node = null;

	enum RelTypes implements org.neo4j.graphdb.RelationshipType{
		RELTYPE_SUBREF
	}
	
	RelationshipTypeImpl(org.neo4j.graphdb.RelationshipType relType, GraphDatabaseService graphDb){
		this.relType = relType;
		this.graphDb = graphDb;
	}
	
	public String name() {
		return relType.name();
	}

	@Override
	public org.neo4j.graphdb.PropertyContainer getPropertyContainer() {
		return getNode();
	}

	public Node getNode(){
		if(node == null){
			Relationship subRefRel = getGraphDatabaseExt().getReferenceNodeExt().getSingleRelationshipExt(RelTypes.RELTYPE_SUBREF, Direction.OUTGOING);
			Node subRef = null;
			if(subRefRel == null){
				Node n = getGraphDatabaseExt().createNodeExt();
				getGraphDatabaseExt().getReferenceNode().createRelationshipTo(n, RelTypes.RELTYPE_SUBREF);
				subRef = n;
			}else{
				subRef = (Node)subRefRel.getEndRelationshipContainer();
			}
			if(subRef.hasProperty(relType.name())){
				node = getGraphDatabaseExt().getNodeByIdExt((Long)subRef.getProperty(relType.name()));
			}else{
				Node n = getGraphDatabaseExt().createNodeExt();
				subRef.setProperty(relType.name(),n.getId());
			}
		}
		return node;
	}
	
	public org.neo4j.graphdb.RelationshipType getRelationshipType(){
		return relType;
	}
	
	public GraphDatabaseService getGraphDatabaseExt() {
		return graphDb;
	}

	public GraphDatabaseService getGraphDatabase() {
		return graphDb;
	}
	
	@Override
	public Relationship createRelationshipToExt(
			RelationshipContainer n,
			RelationshipType rt) {
		return getNode().createRelationshipToExt(n, rt);
	}


	@Override
	public Iterable<Relationship> getRelationshipsExt() {
		return getNode().getRelationshipsExt();
	}

	@Override
	public Iterable<Relationship> getRelationshipsExt(
			RelationshipType... arg0) {
		return getNode().getRelationshipsExt(arg0);
	}

	@Override
	public Iterable<Relationship> getRelationshipsExt(
			Direction arg0) {
		return getNode().getRelationshipsExt(arg0);
	}

	@Override
	public Iterable<Relationship> getRelationshipsExt(
			Direction arg0, RelationshipType... arg1) {
		return getNode().getRelationshipsExt(arg0, arg1);
	}

	@Override
	public Iterable<Relationship> getRelationshipsExt(
			RelationshipType arg0, Direction arg1) {
		return getNode().getRelationshipsExt(arg0, arg1);
	}

	@Override
	public Relationship getSingleRelationshipExt(
			RelationshipType arg0, Direction arg1) {
		return getNode().getSingleRelationshipExt(arg0, arg1);
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
	public boolean hasRelationship(Direction arg0,
			RelationshipType... arg1) {
		return getNode().hasRelationship(arg0, arg1);
	}

	@Override
	public boolean hasRelationship(RelationshipType arg0,
			Direction arg1) {
		return getNode().hasRelationship(arg0, arg1);
	}

	@Override
	public <T> Property<T> getProperty(PropertyType<T> pt) {
		return new PropertyImpl<T>(getNode(), pt);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getPropertyValue(PropertyType<T> pt) {
		return (T)getNode().getProperty(pt.getName());
	}

	@Override
	public <T> boolean hasProperty(PropertyType<T> pt) {
		return getNode().hasProperty(pt.getName());
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T removeProperty(PropertyType<T> pt) {
		return (T)getNode().removeProperty(pt.getName());
	}

	@Override
	public <T> void setProperty(PropertyType<T> pt, T value) {
		getNode().setProperty(pt.getName(), value);
	}

	@Override
	public Iterable<PropertyType<?>> getPropertyTypes() {
		return PropertyType.getPropertyTypes(this, getGraphDatabaseExt());
	}
}
