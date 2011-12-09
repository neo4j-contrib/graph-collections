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

import org.neo4j.graphdb.Relationship;

/**
 * A specialized version of an {@link Edge}, where the number of
 * {@link Connectors} is two, and where those Connectors have a
 * {@link SurjectiveConnectionMode}. BinaryEdges is a wrapper
 * {@link Relationship} in the standard Neo4j API.
 * 
 */
public interface BinaryEdge extends Edge {

	/**
	 * @return the Vertex connected to the EndConnector.
	 */
	public Vertex getEndVertex();

	/**
	 * @param vertex
	 * @return the StartVertex if the vertex argument is the EndVertex or the
	 *         EndVertex if the vertex argument is the StartVertex
	 */
	public Vertex getOtherVertex(Vertex vertex);

	/**
	 * @return the Vertex connected to the StartConnector.
	 */
	public Vertex getStartVertex();

	/**
	 * @return the Neo4j Relationship wrapped by this BinaryEdge
	 */
	public org.neo4j.graphdb.Relationship getRelationship();

}
