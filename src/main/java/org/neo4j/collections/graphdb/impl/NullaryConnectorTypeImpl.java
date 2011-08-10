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

import org.neo4j.collections.graphdb.BijectiveConnectionMode;
import org.neo4j.collections.graphdb.ConnectionMode;
import org.neo4j.collections.graphdb.DatabaseService;
import org.neo4j.collections.graphdb.NullaryConnectorType;
import org.neo4j.graphdb.Node;

public abstract class NullaryConnectorTypeImpl extends ConnectorTypeImpl<BijectiveConnectionMode> implements NullaryConnectorType{

	private NullaryConnectorTypeImpl(Node node) {
		super(node);
	}

	@Override
	public BijectiveConnectionMode getConnectionMode() {
		return ConnectionMode.BIJECTIVE;
	}

	private static final String nullaryConnectorName = "NullaryConnector";

	public static class NullaryConnectorType extends NullaryConnectorTypeImpl{

		public NullaryConnectorType(Node node){
			super(node);
		}
		

		public static NullaryConnectorType getOrCreateInstance(DatabaseService db){
			return new NullaryConnectorType(ConnectorTypeImpl.getOrCreateInstance(db, nullaryConnectorName, ConnectionMode.BIJECTIVE).getNode());
		}
		
		@Override
		public String getName() {
			return nullaryConnectorName;
		}
	}

}