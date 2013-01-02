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

import org.neo4j.collections.graphdb.impl.ConnectionModeImpl.Bijective;
import org.neo4j.collections.graphdb.impl.ConnectionModeImpl.Injective;
import org.neo4j.collections.graphdb.impl.ConnectionModeImpl.Surjective;
import org.neo4j.collections.graphdb.impl.ConnectionModeImpl.Unrestricted;


/**
 * Defines the mode of a {@link Connection}.
 * <p>A connection is defined as going from a {@link Vertex} to an {@link Edge}.</p>
 * <p>The left hand side (the Vertex side) can either be restricted, 
 * ie. allowing only one Vertex to connect to an Edge with a {@link Connector}
 * having a {@link LeftRestrictedConnectionMode}, or it can be unrestricted,
 * ie. allowing an unlimited number of Vertices to connect to an Edge
 * with a Connector having a {@link LeftUnrestrictedConnectionMode}.</p>
 * <p>The right hand side (the Edge side) can also either be restricted,
 * ie. allowing a Vertex to only connect to one edge with a particular 
 * {@EdgeType} on a Connector having a {@link RightRestrictedConnectionMode}.
 * The right hand side can be unrestricted too, ie. allowing a Vertex
 * to connect to any number of EdgeTypes on a Connectior having a
 * {@link RightRestrictedConnectionMode}.</p>
 * The four restrictions lead to four basic ConnectionModes:
 * <ul>
 * <li>{@link UnrestrictedConnectionMode}: having both a RightUnrestrictedConnectionMode and a LeftUnrestrictedConnectionMode</li>
 * <li>{@link InjectiveConnectionMode}: having a RightUnrestrictedConnectionMode and a LeftRestrictedConnectionMode</li>
 * <li>{@link SurjectiveConnectionMode}: having a RightRestrictedConnectionMode and a LeftUnrestrictedConnectionMode</li>   
 * <li>{@link BijectiveConnectionMode}: having a RightRestrictedConnectionMode and a LeftRestrictedConnectionMode</li>
 * </ul> 
 * A {@link ConnectorType} can only be created with one of these four ConnectionModes.
 *
 */
public interface ConnectionMode {

	/**
	 * @return the name of the ConnectionMode
	 */
	public String getName();
	
	/**
	 * Access method for UnrestricedConnectionMode object 
	 */
	public static UnrestrictedConnectionMode UNRESTRICTED = new Unrestricted();

	/**
	 * Access method for InjectiveConnectionMode object 
	 */
	public static InjectiveConnectionMode INJECTIVE = new Injective();

	/**
	 * Access method for SurjectiveConnectionMode object 
	 */
	public static SurjectiveConnectionMode SURJECTIVE = new Surjective();
	
	/**
	 * Access method for BijectiveConnectionMode object 
	 */
	public static BijectiveConnectionMode BIJECTIVE = new Bijective();
	
}
