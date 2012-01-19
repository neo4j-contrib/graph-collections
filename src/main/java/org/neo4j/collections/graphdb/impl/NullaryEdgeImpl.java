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
package org.neo4j.collections.graphdb.impl;

import java.util.ArrayList;

import org.neo4j.collections.graphdb.Connection;
import org.neo4j.collections.graphdb.ConnectionMode;
import org.neo4j.collections.graphdb.Connector;
import org.neo4j.collections.graphdb.ConnectorType;
import org.neo4j.collections.graphdb.DatabaseService;
import org.neo4j.collections.graphdb.EdgeType;
import org.neo4j.collections.graphdb.LeftRestrictedConnectionMode;
import org.neo4j.collections.graphdb.NullaryEdge;
import org.neo4j.collections.graphdb.Vertex;

public class NullaryEdgeImpl extends VertexImpl implements NullaryEdge{

	
	public NullaryEdgeImpl(DatabaseService db, Long id) {
		super(db, id);
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
	public Iterable<Connector<?>> getConnectors() {
		ArrayList<Connector<?>> connectors = new ArrayList<Connector<?>>();
		connectors.add(Connector.getInstance(ConnectorTypeImpl.getOrCreateInstance(db, NullaryEdgeTypeImpl.NULLARYCONNECTORNAME, getType().getNode(), ConnectionMode.BIJECTIVE), this));
		return connectors;
	}

	@Override
	public Iterable<Connector<?>> getConnectors(
			ConnectorType<?>... connectorTypes) {
		ArrayList<Connector<?>> connectors = new ArrayList<Connector<?>>();
		for(ConnectorType<?> connectorType: connectorTypes){
			if(connectorType.getName().equals(NullaryEdgeTypeImpl.NULLARYCONNECTORNAME)){
				connectors.add(Connector.getInstance(ConnectorTypeImpl.getOrCreateInstance(db, NullaryEdgeTypeImpl.NULLARYCONNECTORNAME, getType().getNode(), ConnectionMode.BIJECTIVE), this));
			}
		}
		return connectors;
	}

	@Override
	public <T extends ConnectionMode> Iterable<Vertex> getVertices(
			ConnectorType<T> connectorType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <U extends LeftRestrictedConnectionMode> Vertex getVertex(
			ConnectorType<U> connectorType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends ConnectionMode> Connector<T> getConnector(
			ConnectorType<T> connectorType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends ConnectionMode> Iterable<Connection<T>> getConnections(
			ConnectorType<T> connectorType) {
		// TODO Auto-generated method stub
		return null;
	}

}
