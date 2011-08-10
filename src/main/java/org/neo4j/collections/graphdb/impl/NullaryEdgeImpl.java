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

import java.util.ArrayList;

import org.neo4j.collections.graphdb.ConnectionMode;
import org.neo4j.collections.graphdb.ConnectorType;
import org.neo4j.collections.graphdb.EdgeElement;
import org.neo4j.collections.graphdb.EdgeType;
import org.neo4j.collections.graphdb.InjectiveConnectionMode;
import org.neo4j.collections.graphdb.NullaryEdge;
import org.neo4j.collections.graphdb.Vertex;
import org.neo4j.collections.graphdb.impl.NullaryConnectorTypeImpl.NullaryConnectorType;
import org.neo4j.graphdb.Node;

public class NullaryEdgeImpl extends VertexImpl implements NullaryEdge{

	public NullaryEdgeImpl(Node node) {
		super(node);
	}

	@Override
	public void delete() {
		getNode().delete();
	}

	@Override
	public EdgeType getType() {
		return NullaryEdgeTypeImpl.getOrCreateInstance(getDb());
	}

	@Override
	public boolean isType(EdgeType relType) {
		return relType.getName().equals(getType().getName());
	}

	@Override
	public Iterable<EdgeElement> getEdgeElements() {
		ArrayList<EdgeElement> elems = new ArrayList<EdgeElement>();
		Vertex[] va = new Vertex[1];
		va[0] = getDb().getVertex(getNode()); 
		elems.add(new EdgeElement(NullaryConnectorType.getOrCreateInstance(getDb()), va));
		return null;
	}

	@Override
	public Iterable<EdgeElement> getEdgeElements(
			ConnectorType<?>... connectorType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends ConnectionMode> Iterable<Vertex> getVertices(
			ConnectorType<T> connectorType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <U extends InjectiveConnectionMode> Vertex getVertex(
			ConnectorType<U> connectorType) {
		// TODO Auto-generated method stub
		return null;
	}

}
