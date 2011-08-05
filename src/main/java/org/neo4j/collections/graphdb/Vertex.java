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
package org.neo4j.collections.graphdb;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.RelationshipType;

public interface Vertex{

	public BinaryEdge createEdgeTo(Vertex n, RelationshipType rt);
	
	public <T> SortableBinaryEdge<T> createRelationshipTo(Vertex n, SortableBinaryEdgeType<T> rt);
	
	public Iterable<BinaryEdge> getBinaryEdges();
	
	public Iterable<NAryEdge> getEdges(NAryEdgeType... edgeTypes);
	
	public Iterable<BinaryEdge> getBinaryEdges(RelationshipType... relTypes);

	public Iterable<BinaryEdge> getBinaryEdges(Direction dir);

	public Iterable<BinaryEdge> getBinaryEdges(Direction dir,
			RelationshipType... relTypes);
	
	public Iterable<BinaryEdge> getBinaryEdges(RelationshipType relType,
			Direction dir);

	public Iterable<NAryEdge> getEdges(NAryEdgeType edgeType, NAryEdgeRoleType... role);
	
	public BinaryEdge getSingleBinaryEdge(RelationshipType relType,
			Direction dir);

	public boolean hasBinaryEdge();

	public boolean hasBinaryEdge(RelationshipType... relTypes);
	
	public boolean hasEdge(NAryEdgeType edgeType, NAryEdgeRoleType... role);
	
	public boolean hasBinaryEdge(Direction dir);

	public boolean hasBinaryEdge(Direction dir, RelationshipType... relTypes);

	public boolean hasBinaryEdge(RelationshipType relTypes, Direction dir);

	abstract org.neo4j.graphdb.Node getNode();

	public long getId();
	
	public <T> Property<T> getProperty(PropertyType<T> pt);

	public <T> T getPropertyValue(PropertyType<T> pt);
	
	public <T> boolean hasProperty(PropertyType<T> pt);

	public <T> T removeProperty(PropertyType<T> pt);
	
	public <T> void setProperty(PropertyType<T> pt, T value);

	public Iterable<PropertyType<?>> getPropertyTypes();
	
	public PropertyContainer getPropertyContainer();
	
	public DatabaseService getDb();

}
