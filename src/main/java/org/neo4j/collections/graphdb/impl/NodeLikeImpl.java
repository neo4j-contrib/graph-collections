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

import org.neo4j.collections.graphdb.NodeLike;
import org.neo4j.collections.graphdb.Property;
import org.neo4j.collections.graphdb.PropertyType;
import org.neo4j.collections.graphdb.Relationship;
import org.neo4j.collections.graphdb.RelationshipContainer;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.RelationshipType;

public abstract class NodeLikeImpl implements NodeLike{
	
	@Override
	public Relationship createRelationshipToExt(
			RelationshipContainer n,
			RelationshipType rt) {
		return new RelationshipImpl(getNode().createRelationshipTo(n.getNode(), rt));
	}


	@Override
	public Iterable<Relationship> getRelationshipsExt() {
		return new RelationshipIterable(getNode().getRelationships());
	}

	@Override
	public Iterable<Relationship> getRelationshipsExt(
			RelationshipType... relTypes) {
		return new RelationshipIterable(getNode().getRelationships(relTypes));
	}

	@Override
	public Iterable<Relationship> getRelationshipsExt(
			Direction dir) {
		return new RelationshipIterable(getNode().getRelationships(dir));
	}

	@Override
	public Iterable<Relationship> getRelationshipsExt(
			Direction dir, RelationshipType... relTypes) {
		return new RelationshipIterable(getNode().getRelationships(dir, relTypes));
	}

	@Override
	public Iterable<Relationship> getRelationshipsExt(
			RelationshipType relType, Direction dir) {
		return new RelationshipIterable(getNode().getRelationships(relType, dir));
	}

	@Override
	public Relationship getSingleRelationshipExt(
			RelationshipType relType, Direction dir) {
		return new RelationshipImpl(getNode().getSingleRelationship(relType, dir));
	}

	@Override
	public boolean hasRelationship() {
		return getNode().hasRelationship();
	}

	@Override
	public boolean hasRelationship(RelationshipType... relType) {
		return getNode().hasRelationship(relType);
	}

	@Override
	public boolean hasRelationship(Direction dir) {
		return getNode().hasRelationship(dir);
	}

	@Override
	public boolean hasRelationship(Direction dir,
			RelationshipType... relTypes) {
		return getNode().hasRelationship(dir, relTypes);
	}

	@Override
	public boolean hasRelationship(RelationshipType relType,
			Direction dir) {
		return getNode().hasRelationship(relType, dir);
	}

	@Override
	public <T> Property<T> getProperty(PropertyType<T> pt) {
		return new PropertyImpl<T>(getGraphDatabaseExt(), this, pt);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getPropertyValue(PropertyType<T> pt) {
		return (T)getPropertyContainer().getProperty(pt.getName());
	}

	@Override
	public <T> boolean hasProperty(PropertyType<T> pt) {
		return getPropertyContainer().hasProperty(pt.getName());
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T removeProperty(PropertyType<T> pt) {
		return (T)getPropertyContainer().removeProperty(pt.getName());
	}

	@Override
	public <T> void setProperty(PropertyType<T> pt, T value) {
		getPropertyContainer().setProperty(pt.getName(), value);
	}

	@Override
	public Iterable<PropertyType<?>> getPropertyTypes() {
		return PropertyType.getPropertyTypes(this, getGraphDatabaseExt());
	}

}
