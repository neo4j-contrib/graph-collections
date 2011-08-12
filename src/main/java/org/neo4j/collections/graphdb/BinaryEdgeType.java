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
package org.neo4j.collections.graphdb;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.RelationshipType;

/**
 * A wrapper around {@link RelationshipType} in the standard Neo4j API.
 * <p>
 * BinaryEdgeType is a specialized version of a generalized {@link Edge}.
 */
public interface BinaryEdgeType extends EdgeType {

	/**
	 * @return the {@link RelationshipType} wrapped by this BinaryEdgeType
	 */
	public RelationshipType getRelationshipType();

	/**
	 * @param startVertex
	 * @param endVertex
	 * @return a {@link BinaryEdge} created from startVertex to EndVertex with
	 *         this BinaryEdgeType
	 */
	public BinaryEdge createEdge(Vertex startVertex, Vertex endVertex);

	/**
	 * @param vertex
	 * @return the {@link BinaryEdge}s connected to vertex with this
	 *         BinaryEdgeType
	 */
	public Iterable<BinaryEdge> getEdges(Vertex vertex);

	/**
	 * @param vertex
	 * @param dir
	 * @return the {@link BinaryEdge}s connected to vertex with this
	 *         BinaryEdgeType and the given {@link Direction}
	 */
	public Iterable<BinaryEdge> getEdges(Vertex vertex, Direction dir);

	/**
	 * @param vertex
	 * @return true if the vertex is connected to a {@link BinaryEdge} with this
	 *         BinaryEdgeType
	 */
	public boolean hasEdge(Vertex vertex);

	/**
	 * @param vertex
	 * @param dir
	 * @return true if the vertex is connected to a {@link BinaryEdge} with this
	 *         BinaryEdgeType and the given direction.
	 */
	public boolean hasEdge(Vertex vertex, Direction dir);

	/**
	 * @param vertex
	 * @param dir
	 * @return the single BinaryEdge connected to vertex with this
	 *         BinaryEdgeType and the given direction. Null is returned if no
	 *         BinaryEdge is connected. If more than one BinaryEdge is found, an
	 *         exception will be raised.
	 */
	public BinaryEdge getSingleBinaryEdge(Vertex vertex, Direction dir);

	/**
	 * @return the StartConnector associated with this BinaryEdge
	 */
	public ConnectorType<SurjectiveConnectionMode> getStartConnectorType();

	/**
	 * @return the EndConnector associated with this BinaryEdge
	 */
	public ConnectorType<SurjectiveConnectionMode> getEndConnectorType();

}
