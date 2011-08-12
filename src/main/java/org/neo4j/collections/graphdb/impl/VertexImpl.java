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
package org.neo4j.collections.graphdb.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.neo4j.collections.graphdb.BinaryEdge;
import org.neo4j.graphdb.Node;
import org.neo4j.collections.graphdb.BijectiveConnectionMode;
import org.neo4j.collections.graphdb.Connection;
import org.neo4j.collections.graphdb.DatabaseService;
import org.neo4j.collections.graphdb.Connector;
import org.neo4j.collections.graphdb.EdgeType;
import org.neo4j.collections.graphdb.Edge;
import org.neo4j.collections.graphdb.ConnectorType;
import org.neo4j.collections.graphdb.TraversalPath;
import org.neo4j.collections.graphdb.Traversal;
import org.neo4j.collections.graphdb.TraversalDescription;
import org.neo4j.collections.graphdb.Vertex;
import org.neo4j.collections.graphdb.Property;
import org.neo4j.collections.graphdb.VertexType;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.collections.graphdb.PropertyType;
import org.neo4j.collections.graphdb.SortableBinaryEdge;
import org.neo4j.collections.graphdb.SortableBinaryEdgeType;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

public class VertexImpl implements Vertex {

	static class EdgeIterator implements Iterator<Edge> {

		private final Iterator<Relationship> connectorRels;

		public EdgeIterator(
				EdgeType edgeType,
				ConnectorType<?> connectorType,
				Vertex vertex) {
			this.connectorRels = vertex
					.getNode()
					.getRelationships(
							DynamicRelationshipType.withName(edgeType.getName()
									+ EDGEROLE_SEPARATOR
									+ connectorType.getName()),
							Direction.INCOMING).iterator();
		}

		@Override
		public boolean hasNext() {
			return connectorRels.hasNext();
		}

		@Override
		public Edge next() {
			if (connectorRels.hasNext()) {
				return new EdgeImpl(connectorRels.next().getStartNode());
			} else {
				throw new NoSuchElementException();
			}
		}

		@Override
		public void remove() {
		}
	}

	static class ConnectorTypeIterable implements Iterable<Edge> {

		final Iterable<ConnectorType<?>> connectorTypes;
		final EdgeType edgeType;

		private final Vertex vertex;

		public ConnectorTypeIterable(
				EdgeType edgeType,
				Iterable<ConnectorType<?>> connectorTypes,
				Vertex vertex) {
			this.edgeType = edgeType;
			this.connectorTypes = connectorTypes;
			this.vertex = vertex;
		}

		@Override
		public Iterator<Edge> iterator() {
			return new ConnectorTypeIterator(edgeType, connectorTypes.iterator(), vertex);
		}

	}

	static class ConnectorTypeIterator implements Iterator<Edge> {

		private final Iterator<ConnectorType<?>> connectorTypes;

		private final Vertex vertex;
		private final EdgeType edgeType;
		private EdgeIterator currentEdgeIterator = null;
		private Boolean hasNext = null;

		public ConnectorTypeIterator(
				EdgeType edgeType,
				Iterator<ConnectorType<?>> connectorTypes,
				Vertex vertex) {
			this.edgeType = edgeType;
			this.connectorTypes = connectorTypes;
			this.vertex = vertex;
		}

		@Override
		public boolean hasNext() {
			if (hasNext == null) {
				if (currentEdgeIterator == null) {
					if (connectorTypes.hasNext()) {
						currentEdgeIterator = new EdgeIterator(edgeType,
								connectorTypes.next(), vertex);
						return hasNext();
					} else {
						hasNext = false;
						return false;
					}
				} else {
					if (currentEdgeIterator.hasNext()) {
						hasNext = true;
						return true;
					} else {
						currentEdgeIterator = null;
						return hasNext();
					}
				}
			} else {
				return hasNext;
			}
		}

		@Override
		public Edge next() {
			if (hasNext()) {
				hasNext = null;
				return currentEdgeIterator.next();
			} else {
				throw new NoSuchElementException();
			}
		}

		@Override
		public void remove() {
		}

	}

	static class EdgeTypeIterable implements Iterable<Edge> {

		EdgeType[] edgeTypes;

		private final Vertex vertex;

		EdgeTypeIterable(Vertex vertex, EdgeType... edgeTypes) {
			this.edgeTypes = edgeTypes;
			this.vertex = vertex;
		}

		@Override
		public Iterator<Edge> iterator() {
			ArrayList<EdgeType> ets = new ArrayList<EdgeType>();
			for (EdgeType edgeType : edgeTypes) {
				ets.add(edgeType);
			}
			return new EdgeTypeIterator(ets.iterator(), vertex);
		}

	}

	static class EdgeTypeIterator implements Iterator<Edge> {

		Iterator<EdgeType> edgeTypes;
		ConnectorTypeIterator currentConnectorTypeIterator = null;
		Boolean hasNext = null;
		private final Vertex vertex;

		EdgeTypeIterator(Iterator<EdgeType> edgeTypes, Vertex vertex) {
			this.edgeTypes = edgeTypes;
			this.vertex = vertex;
		}

		@Override
		public boolean hasNext() {
			if (hasNext == null) {
				if (currentConnectorTypeIterator == null) {
					if (edgeTypes.hasNext()) {
						EdgeType edgeType = edgeTypes.next();
						currentConnectorTypeIterator = new ConnectorTypeIterator(edgeType, edgeType.getConnectorTypes().iterator(), vertex);
						return hasNext();
					} else {
						hasNext = false;
						return false;
					}
				} else {
					if (currentConnectorTypeIterator.hasNext()) {
						hasNext = true;
						return true;
					} else {
						currentConnectorTypeIterator = null;
						return hasNext();
					}
				}
			} else {
				return hasNext;
			}
		}

		@Override
		public Edge next() {
			if (hasNext()) {
				hasNext = null;
				return currentConnectorTypeIterator.next();
			} else {
				throw new NoSuchElementException();
			}
		}

		@Override
		public void remove() {
		}

	}

	class TraversalIterator implements Iterator<Traversal> {

		@Override
		public boolean hasNext() {
			return false;
		}

		@Override
		public Traversal next() {
			throw new NoSuchElementException();
		}

		@Override
		public void remove() {
		}

	}

	class PropertyTypeIterable implements Iterable<PropertyType<?>> {

		final Node node;

		public PropertyTypeIterable(Node node) {
			this.node = node;
		}

		@Override
		public Iterator<PropertyType<?>> iterator() {
			return new PropertyTypeIterator(node);
		}

	}

	class PropertyTypeIterator implements Iterator<PropertyType<?>> {

		final Iterator<String> keys;

		PropertyTypeIterator(Node n) {
			keys = n.getPropertyKeys().iterator();
		}

		@Override
		public boolean hasNext() {
			return keys.hasNext();
		}

		@Override
		public PropertyType<?> next() {
			return PropertyType.getPropertyTypeByName(getDb(), keys.next());
		}

		@Override
		public void remove() {
		}

	}

	private class VertexTypeIterable implements Iterable<VertexType> {

		@Override
		public Iterator<VertexType> iterator() {
			return new VertexTypeIterator();
		}

	}

	private class VertexTypeIterator implements Iterator<VertexType> {

		private final Iterator<Node> nodes;
		boolean foundSpecial = false;
		VertexType special = getSpecialVertexType();

		public VertexTypeIterator() {
			ArrayList<Node> nodes = new ArrayList<Node>();
			Long[] nodeIds = (Long[]) getNode().getProperty(TYPE_IDS);
			for (Long id : nodeIds) {
				nodes.add(getDb().getNodeById(id));
			}
			this.nodes = nodes.iterator();
		}

		@Override
		public boolean hasNext() {
			if (nodes.hasNext()) {
				return true;
			} else if (foundSpecial) {
				return false;
			} else if (special == null) {
				return false;
			} else {
				return true;
			}
		}

		@Override
		public VertexType next() {
			if (hasNext()) {
				if (nodes.hasNext()) {
					VertexType vertexType = (VertexType) getDb().getVertex(
							nodes.next());
					if (special.getName().equals(vertexType.getName())) {
						foundSpecial = true;
					}
					return vertexType;
				} else {
					return special;
				}
			} else {
				throw new NoSuchElementException();
			}
		}

		@Override
		public void remove() {
		}

	}

	public static final String EDGEROLE_SEPARATOR = "/#/";

	public static final String TYPE_IDS = "org.neo4j.collections.graphdb.type_ids";

	private Node node;

	public VertexImpl(Node node) {
		this.node = node;
	}

	@Override
	public Vertex addEdge(Vertex vertex, RelationshipType type) {
		createEdgeTo(vertex, type);
		return this;
	}

	@Override
	public Vertex addEdge(Vertex vertex, SortableBinaryEdgeType<?> type) {
		createEdgeTo(vertex, type);
		return this;
	}

	@Override
	public Vertex addType(VertexType vertexType) {
		Long[] nodeIds = (Long[]) getNode().getProperty(TYPE_IDS);
		boolean exists = false;
		for (Long id : nodeIds) {
			if (id == vertexType.getNode().getId()) {
				exists = true;
			}
		}
		if (!exists) {
			Long[] ids = new Long[nodeIds.length + 1];
			for (int i = 0; i < nodeIds.length; i++) {
				ids[i] = nodeIds[i];
			}
			ids[nodeIds.length] = vertexType.getNode().getId();
		}
		return this;
	}

	@Override
	public BinaryEdge createEdgeTo(Vertex vertex, RelationshipType type) {
		return getDb().getBinaryEdgeType(type).createEdge(this, vertex);
	}

	@Override
	public <T> SortableBinaryEdge<T> createEdgeTo(Vertex vertex,
			SortableBinaryEdgeType<T> type) {
		return type.createEdge(this, vertex);
	}

	/**
	 * We don't dispatch here to Binary Edges because we can't iterate over
	 * relationship types.
	 */
	@Override
	public Iterable<BinaryEdge> getBinaryEdges() {
		return new RelationshipIterable(node.getRelationships());
	}

	/**
	 * We don't dispatch here to Binary Edges because we can't iterate over
	 * relationship types.
	 */
	@Override
	public Iterable<BinaryEdge> getBinaryEdges(Direction dir) {
		return new RelationshipIterable(getNode().getRelationships(dir));
	}

	/**
	 * We don't dispatch here to Binary Edges because then we would have to read
	 * all relationships for each type. As soon as relationships are partitioned
	 * per relationship type, per role/direction, the implementation of this
	 * method can dispatch to Binary Edge
	 */
	@Override
	public Iterable<BinaryEdge> getBinaryEdges(Direction dir,
			RelationshipType... relTypes) {
		return new RelationshipIterable(getNode().getRelationships(dir,
				relTypes));
	}

	/**
	 * We don't dispatch here to Binary Edges because then we would have to read
	 * all relationships for each type. As soon as relationships are partitioned
	 * per relationship type, per role/direction, the implementation of this
	 * method can dispatch to Binary Edge
	 */
	@Override
	public Iterable<BinaryEdge> getBinaryEdges(RelationshipType... relTypes) {
		return new RelationshipIterable(node.getRelationships(relTypes));
	}

	/**
	 * We can dispatch here to Binary Edge because we have a single
	 * RelationshipType
	 */
	@Override
	public Iterable<BinaryEdge> getBinaryEdges(RelationshipType relType,
			Direction dir) {
		return getDb().getBinaryEdgeType(relType).getEdges(this, dir);
	}

	@Override
	public DatabaseService getDb() {
		return new GraphDatabaseImpl(getPropertyContainer().getGraphDatabase());
	}

	/**
	 * We don't dispatch here to Binary Edges because then we would have to read
	 * all relationships for each type. As soon as relationships are partitioned
	 * per relationship type, per role/direction, the implementation of this
	 * method can dispatch to Binary Edge
	 */
	@Override
	public Iterable<Edge> getEdges(EdgeType... edgeTypes) {
		return new EdgeTypeIterable(this, edgeTypes);
	}

	@Override
	public Iterable<Edge> getEdges(EdgeType type,
			ConnectorType<?>... connectors) {
		return type.getEdges(this, connectors);
	}

	@Override
	public Node getNode() {
		return node;
	}

	@Override
	public <T> Property<T> getProperty(PropertyType<T> pt) {
		return pt.getProperty(this);
	}

	@Override
	public PropertyContainer getPropertyContainer() {
		return node;
	}

	@Override
	public Iterable<PropertyType<?>> getPropertyTypes() {
		return new PropertyTypeIterable(getNode());
	}

	@Override
	public <T> T getPropertyValue(PropertyType<T> pt) {
		return pt.getPropertyValue(this);
	}

	@Override
	public BinaryEdge getSingleBinaryEdge(RelationshipType type, Direction dir) {
		return getDb().getBinaryEdgeType(type).getSingleBinaryEdge(this, dir);
	}

	protected VertexType getSpecialVertexType() {
		return null;
	}

	@Override
	public Iterable<VertexType> getTypes() {
		return new VertexTypeIterable();
	}

	@Override
	public boolean hasBinaryEdge() {
		return getNode().hasRelationship();
	}

	@Override
	public boolean hasBinaryEdge(Direction dir) {
		return getNode().hasRelationship(dir);
	}

	@Override
	public boolean hasBinaryEdge(Direction dir, RelationshipType... relTypes) {
		return getNode().hasRelationship(dir, relTypes);
	}

	@Override
	public boolean hasBinaryEdge(RelationshipType... relType) {
		return getNode().hasRelationship(relType);
	}

	@Override
	public boolean hasBinaryEdge(RelationshipType relType, Direction dir) {
		return getDb().getBinaryEdgeType(relType).hasEdge(this, dir);
	}

	@Override
	public boolean hasEdge(EdgeType edgeType, ConnectorType<?>... connectors) {
		return edgeType.hasEdge(this, connectors);
	}

	@Override
	public <T> boolean hasProperty(PropertyType<T> pt) {
		return pt.hasProperty(this);
	}

	@Override
	public Iterator<Traversal> iterator() {
		return new TraversalIterator();
	}

	@Override
	public Vertex removeProperty(PropertyType<?> pt) {
		pt.removeProperty(this);
		return this;
	}

	@Override
	public Vertex removeType(VertexType vertexType) {
		Long[] nodeIds = (Long[]) getNode().getProperty(TYPE_IDS);
		boolean exists = false;
		for (Long id : nodeIds) {
			if (id == vertexType.getNode().getId()) {
				exists = true;
			}
		}
		if (!exists) {
			Long[] ids = new Long[nodeIds.length - 1];
			boolean skipped = false;
			for (int i = 0; i < nodeIds.length; i++) {
				if (skipped) {
					ids[i - 1] = nodeIds[i];
				} else {
					if (ids[i] == nodeIds[i]) {
						skipped = true;
					} else {
						ids[i] = nodeIds[i];
					}
				}
			}
			ids[nodeIds.length] = vertexType.getNode().getId();
		}
		return this;
	}

	@Override
	public <T> Vertex setProperty(PropertyType<T> pt, T value) {
		pt.setProperty(this, value);
		return this;
	}

	@Override
	public TraversalPath getPath() {
		return new TraversalPath() {

			@Override
			public Iterator<Connection<?>> iterator() {
				return new Iterator<Connection<?>>() {

					boolean hasNext = true;

					@Override
					public boolean hasNext() {
						return hasNext;
					}

					@Override
					public Connection<?> next() {
						if (hasNext) {
							hasNext = false;
							return getSelfConnection();
						} else {
							throw new NoSuchElementException();
						}
					}

					@Override
					public void remove() {
					}
				};
			}

			@Override
			public Connection<?> getFirstElement() {
				return getSelfConnection();
			}

			@Override
			public Connection<?> getLastElement() {
				return getSelfConnection();
			}

			@Override
			public int length() {
				return 0;
			}

		};

	}

	@SuppressWarnings("unchecked")
	@Override
	public Connection<BijectiveConnectionMode> getSelfConnection() {
		ConnectorType<BijectiveConnectionMode> cnt = NullaryConnectorTypeImpl.NullaryConnectorType.getOrCreateInstance(getDb());
		Edge edge = new NullaryEdgeImpl(getNode());
		return new Connection<BijectiveConnectionMode>((Connector<BijectiveConnectionMode>) Connector.getInstance(cnt, edge), this);
	}

	@Override
	public Traversal traverse(TraversalDescription descr) {
		return descr.traverse(this);
	}

	@Override
	public Iterable<TraversalPath> getContainedPaths() {
		ArrayList<TraversalPath> paths = new ArrayList<TraversalPath>();
		return paths;
	}

}
