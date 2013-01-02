/**
 * Copyright (c) 2002-2013 "Neo Technology,"
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


public interface Edge extends Vertex{

	public void delete();

	public EdgeType getType();

	public boolean isType(EdgeType relType);

	public Iterable<Connector<?>> getConnectors();

	public Iterable<Connector<?>> getConnectors(ConnectorType<?>... connectorType);
	
	public <T extends ConnectionMode> Connector<T> getConnector(ConnectorType<T> connectorType);
	
	public <T extends ConnectionMode> Iterable<Vertex> getVertices(ConnectorType<T> connectorType);
	
	public <T extends ConnectionMode> Iterable<Connection<T>> getConnections(ConnectorType<T> connectorType);
	
	public <T extends LeftRestrictedConnectionMode>Vertex getVertex(ConnectorType<T> connectorType);

}
