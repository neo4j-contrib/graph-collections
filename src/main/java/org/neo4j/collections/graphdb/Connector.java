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

public class Connector<T extends ConnectionMode> {
	
	private final ConnectorType<T> connectorType;
	private final EdgeType edgeType;
	
	@SuppressWarnings("unchecked")
	public static Connector<?> getInstance(ConnectorType<?> connectorType, EdgeType edgeType){
		if(connectorType.getConnectionMode().getName().equals(ConnectionMode.UNRESTRICTED.getName())){
			return new Connector<UnrestrictedConnectionMode>((ConnectorType<UnrestrictedConnectionMode>) connectorType, edgeType);
		}else if(connectorType.getConnectionMode().getName().equals(ConnectionMode.INJECTIVE.getName())){
			return new Connector<InjectiveConnectionMode>((ConnectorType<InjectiveConnectionMode>) connectorType, edgeType);
		}else if(connectorType.getConnectionMode().getName().equals(ConnectionMode.SURJECTIVE.getName())){
			return new Connector<SurjectiveConnectionMode>((ConnectorType<SurjectiveConnectionMode>) connectorType, edgeType);
		}else if(connectorType.getConnectionMode().getName().equals(ConnectionMode.BIJECTIVE.getName())){
			return new Connector<BijectiveConnectionMode>((ConnectorType<BijectiveConnectionMode>) connectorType, edgeType);
		}else{
			throw new RuntimeException("Unsupported ConnectionMode "+connectorType.getConnectionMode().getName()+"found");
		}
	}
	
	public Connector(ConnectorType<T> connectorType, EdgeType edgeType) {
		this.connectorType = connectorType;
		this.edgeType = edgeType;
	}

	public EdgeType getEdgeType(){
		return edgeType;
	}
	
	public ConnectorType<T> getConnectorType(){
		return connectorType;
	}

	public String getName(){
		return connectorType.getName();
	}
	
}
