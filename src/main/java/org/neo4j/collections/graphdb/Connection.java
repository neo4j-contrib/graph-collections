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

public class Connection<T extends ConnectionMode> {

	private final Connector<T> connector;
	private final Vertex vertex;

	public Connection(Connector<T> connector, Vertex vertex) {
		super();
		this.connector = connector;
		this.vertex = vertex;
	}
	
	public Connector<T> getConnector() {
		return connector;
	}
	
	public Edge getEdge() {
		return connector.getEdge();
	}
	
	public Vertex getVertex() {
		return vertex;
	}
	
}
