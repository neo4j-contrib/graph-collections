/**
 * Copyright (c) 2002-2012 "Neo Technology,"
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
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.RelationshipType;

/**
 * A vertex in the graph with properties and edges to other vertices.
 * 
 * Vertices are created by invoking {@link DatabaseService#createVertex} 
 */
public interface Vertex extends Traversal {

	/**
	 * Creates a {@link BinaryEdge} from this vertex to another vertex. The
     * edge is of type <code>type</code>. It starts at this vertex and
     * ends at <code>otherVertex</code>.
     * <p>
     * A binary edge is equally well traversed in both directions so there is no
     * need to create another relationship in the opposite direction (with regards
     * to traversal or performance).
	 * 
	 * @param vertex, the vertex to create a {@link BinaryEdge} to 
	 * @param type, the type of the relationship 
	 * @return the modified vertex
	 */
	public Vertex addEdge(Vertex vertex, RelationshipType type);

	/**
	 * Creates a {@link SortableBinaryEdge} from this vertex to another vertex. The
     * edge is of type <code>type</code>. It starts at this vertex and
     * ends at <code>otherVertex</code>.
     * <p>
	 * A sortable binary edge is stored in a Btree having its root at this vertex. The
	 * tree is sorted on a property of the <code>otherVertex</code>.
	 * 
	 * @param vertex, the vertex to create a {@link SortableBinaryEdge} to
	 * @param type, the type of the relationship
	 * @return the modified vertex
	 */
	public Vertex addEdge(Vertex vertex, SortableBinaryEdgeType<?> type);

	/**
	 * States that a a vertex belongs to a certain type. Vertex types
	 * are vertices with a unique name, that applications can use to make
	 * statements about vertices. 
	 * 
	 * @param type, the type a vertex belongs to
	 * @return the modified vertex
	 */
	public Vertex addType(VertexType type);

	/**
	 * Creates a {@link BinaryEdge} from this vertex to another vertex. The
     * edge is of type <code>type</code>. It starts at this vertex and
     * ends at <code>otherVertex</code>.
     * <p>
     * A binary edge is equally well traversed in both directions so there is no
     * need to create another relationship in the opposite direction (with regards
     * to traversal or performance).
     * 
	 * @param vertex, the vertex to create an edge to 
	 * @param type, the type of the relationship 
	 * @return the created edge
	 */
	public BinaryEdge createEdgeTo(Vertex vertex, RelationshipType type);

	/**
	 * Creates a {@link SortableBinaryEdge} from this vertex to another vertex. The
     * edge is of type <code>type</code>. It starts at this vertex and
     * ends at <code>otherVertex</code>.
     * <p>
	 * A sortable binary edge is stored in a Btree having its root at this vertex. The
	 * tree is sorted on a property of the <code>otherVertex</code>.
	 * 
	 * @param <T> the data type of the property used to sort the index tree by 
	 * @param vertex, the vertex to create an edge to
	 * @param type, the type of the relationship
	 * @return the created edge
	 */
	public <T> SortableBinaryEdge<T> createEdgeTo(Vertex vertex,
			SortableBinaryEdgeType<T> type);

	/**
	 * Returns all binary edges this vertex is connected to. If no 
	 * binary edges are found, an empty iterable is returned
	 * 
	 * @return the binary edges this vertex is connected to
	 */
	public Iterable<BinaryEdge> getBinaryEdges();

	/**
     * Returns all {@link Direction#OUTGOING OUTGOING} or
     * {@link Direction#INCOMING INCOMING} binary edges from this node. If
     * there are no edges with the given direction attached to this
     * node, an empty iterable will be returned. If {@link Direction#BOTH BOTH}
     * is passed in as a direction, relationships of both directions are
     * returned (effectively turning this into <code>getBinaryEdges()</code>).
 
    * @param dir the given direction, where <code>Direction.OUTGOING</code>
     *            means all binary edges that have this vertex as
     *            {@link BinaryEdge#getStartVertex() start vertex} and <code>
	 * Direction.INCOMING</code>
     *            means all binary edges that have this vertex as
     *            {@link BinaryEdge#getEndVertex() end vertex}
     * @return all binary edges with the given direction that are attached to
     *         this vertex
     *         	 
	*/
	public Iterable<BinaryEdge> getBinaryEdges(Direction dir);

	/**
     * Returns all binary edges of any of the types in <code>types</code>
     * that are attached to this vertex and have the given <code>direction</code>.
     * If no binary edges of the given types are attached to this vertex, an empty
     * iterable will be returned.
     *
     * @param direction, the direction of the relationships to return.
     * @param types, the given relationship type(s)
     * @return all binary edges of the given type(s) that are attached to this
     *         vertex
     */
	public Iterable<BinaryEdge> getBinaryEdges(Direction direction, RelationshipType... types);

	/**
     * Returns all binary edges of any of the types in <code>types</code>
     * that are attached to this vertex, regardless of direction. If no
     * vcertices of the given types are attached to this vertex, an empty
     * iterable will be returned.
     *
     * @param types, the given relationship type(s)
     * @return all relationships of the given type(s) that are attached to this
     *         node
     */
	public Iterable<BinaryEdge> getBinaryEdges(RelationshipType... types);
	
	/**
     * Returns all binary edges with the given type and direction that are
     * attached to this vertex. If there are no matching edges, an empty
     * iterable will be returned.
     *
     * @param type, the given type
     * @param dir, the given direction, where <code>Direction.OUTGOING</code>
     *            means all relationships that have this node as
     *            {@link BinaryEdge#getStartVertex() start vertex} and <code>
	 * Direction.INCOMING</code>
     *            means all edges that have this edge as
     *            {@link BinaryEdge#getEndVertex() end vertex}
     * @return all relationships attached to this node that match the given type
     *         and direction
     */
	public Iterable<BinaryEdge> getBinaryEdges(RelationshipType type, Direction dir);

	/**
	 * @return the database this vertex is created in
	 */
	public DatabaseService getDb();

	/**
	 * @param types, the types for which to return edges
	 * @return edges connected to this Vertex having one of the
	 * supplied types 
	 */
	public Iterable<Edge> getEdges(EdgeType... types);

	public Iterable<Edge> getEdges(EdgeType edgeType, ConnectorType<?>... connectorType);
	
	public Edge getEdge(EdgeType edgeType, ConnectorType<RightRestrictedConnectionMode> connectorType);

	Node getNode();

	public <T> Property<T> getProperty(PropertyType<T> type);

	public PropertyContainer getPropertyContainer();

	public Iterable<PropertyType<?>> getPropertyTypes();

	public <T> T getPropertyValue(PropertyType<T> type);

	public BinaryEdge getSingleBinaryEdge(RelationshipType type,
			Direction dir);

	/**
	 * @return Iterable<VertexType>
	 */
	public Iterable<VertexType> getTypes();

	public boolean hasBinaryEdge();

	public boolean hasBinaryEdge(Direction dir);

	public boolean hasBinaryEdge(Direction dir, RelationshipType... types);

	public boolean hasBinaryEdge(RelationshipType... types);

	public boolean hasBinaryEdge(RelationshipType types, Direction dir);

	public boolean hasEdge(EdgeType edgeType, ConnectorType<?>... role);

	/**
	 * @param <T>, the data type of the property
	 * @param type, the given type
	 * @return boolean, indicates if this Vertex has a propety of the given type 
	 */
	public <T> boolean hasProperty(PropertyType<T> type);

	/**
	 * @param type, the given type
	 * @return the modified Vertex after removing a property of the given type.
	 */
	public Vertex removeProperty(PropertyType<?> type);

	/**
	 * @param type, type of the Vertex to remove
	 * @return the modified Vertex.
	 */
	public Vertex removeType(VertexType type);

	public <T> Vertex setProperty(PropertyType<T> type, T value);

	public Connection<BijectiveConnectionMode> getSelfConnection();
	
	public Traversal traverse(TraversalDescription descr);
	
}
