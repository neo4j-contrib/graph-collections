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

public class ConnectorTypeDescription {

	private final String name;
	private final ConnectionMode connectionMode;
	private final VertexType domain;
	
	public ConnectorTypeDescription(String name, ConnectionMode connectionMode, VertexType domain) {
		this.name = name;
		this.connectionMode = connectionMode;
		this.domain = domain;
	}
	
	public ConnectorTypeDescription(String name, ConnectionMode connectionMode) {
		this.name = name;
		this.connectionMode = connectionMode;
		this.domain = null;
	}
	
	public String getName() {
		return name;
	}
	
	public ConnectionMode getConnectionMode() {
		return connectionMode;
	}

	public VertexType getDomain() {
		return domain;
	}
	
	
}
