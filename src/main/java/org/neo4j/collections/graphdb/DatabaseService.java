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

import java.util.Set;

import org.neo4j.collections.graphdb.PropertyType.ComparablePropertyType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;

public interface DatabaseService extends GraphDatabaseService{

	public Vertex createVertex();
	
	public NAryEdge createEdge(NAryEdgeType relType, Set<EdgeElement> relationshipElements);

	public Iterable<Vertex> getAllVertices();

	public Vertex getVertexById(long arg0);
	
	public Vertex getReferenceVertex();
	
	public Vertex getVertex(Node node);

	public BinaryEdge getBinaryEdgeById(long arg0);
	
	public BinaryEdgeType getBinaryEdgeType(RelationshipType relType);

	public BinaryEdgeRoleType getStartElementRoleType();
	
	public BinaryEdgeRoleType getEndElementRoleType();
	
	public PropertyRoleType getPropertyRoleType();
	
	public NAryEdgeRoleType getEdgeRoleType(String name);
	
	public NAryEdgeType getEdgeType(String name, Set<NAryEdgeRoleType> roles);
	
	public Iterable<EdgeType<?>> getEdgeTypes();

	public PropertyType<Boolean> getBooleanPropertyType(String name);
	
	public PropertyType<Boolean[]> getBooleanArrayPropertyType(String name);

	public PropertyType.ComparablePropertyType<Byte> getBytePropertyType(String name);
	
	public PropertyType<Byte[]> getByteArrayPropertyType(String name);
	
	public PropertyType.ComparablePropertyType<Double> getDoublePropertyType(String name);
	
	public PropertyType<Double[]> getDoubleArrayPropertyType(String name);

	public PropertyType.ComparablePropertyType<Float> getFloatPropertyType(String name);
	
	public PropertyType<Float[]> getFloatArrayPropertyType(String name);

	public PropertyType.ComparablePropertyType<Long> getLongPropertyType(String name);
	
	public PropertyType<Long[]> getLongArrayPropertyType(String name);

	public PropertyType.ComparablePropertyType<Short> getShortPropertyType(String name);
	
	public PropertyType<Short[]> getShortArrayPropertyType(String name);

	public PropertyType.ComparablePropertyType<String> getStringPropertyType(String name);
	
	public PropertyType<String[]> getStringArrayPropertyType(String name);

	public <T> SortableBinaryEdgeType<T> getSortableRelationshipType(String name, ComparablePropertyType<T> propertyType);
	
	public org.neo4j.graphdb.GraphDatabaseService getGraphDatabaseService();
	
}
