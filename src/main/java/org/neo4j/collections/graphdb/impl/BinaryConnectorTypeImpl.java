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

import org.neo4j.collections.graphdb.BinaryConnectorType;
import org.neo4j.collections.graphdb.ConnectionMode;
import org.neo4j.collections.graphdb.DatabaseService;
import org.neo4j.collections.graphdb.InjectiveConnectionMode;
import org.neo4j.graphdb.Node;

public abstract class BinaryConnectorTypeImpl extends ConnectorTypeImpl<InjectiveConnectionMode> implements BinaryConnectorType{

	private BinaryConnectorTypeImpl(Node node) {
		super(node);
	}

	@Override
	public InjectiveConnectionMode getConnectionMode() {
		return ConnectionMode.INJECTIVE;
	}

	private static final String startConnectorName = "StartConnector";
	private static final String endConnectorName = "EndConnector";

	
	
	public static class StartConnector extends BinaryConnectorTypeImpl{

		public StartConnector(Node node){
			super(node);
		}
		

		public static StartConnector getOrCreateInstance(DatabaseService db){
			return new StartConnector(ConnectorTypeImpl.getOrCreateInstance(db, startConnectorName, ConnectionMode.INJECTIVE).getNode());
		}
		
		@Override
		public String getName() {
			return startConnectorName;
		}
	}

	public static class EndConnector extends BinaryConnectorTypeImpl{
		
		public EndConnector(Node node){
			super(node);
		}
		
		public static EndConnector getOrCreateInstance(DatabaseService db){
			return new EndConnector(ConnectorTypeImpl.getOrCreateInstance(db, endConnectorName, ConnectionMode.INJECTIVE).getNode());		
		}
		
		@Override
		public String getName() {
			return endConnectorName;
		}

	}
}
