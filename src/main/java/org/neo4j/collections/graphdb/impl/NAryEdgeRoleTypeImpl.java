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

import org.neo4j.collections.graphdb.DatabaseService;
import org.neo4j.collections.graphdb.EdgeRole;
import org.neo4j.collections.graphdb.NAryEdgeRoleType;
import org.neo4j.collections.graphdb.NAryEdgeType;
import org.neo4j.graphdb.Node;

public class NAryEdgeRoleTypeImpl extends EdgeRoleTypeImpl implements NAryEdgeRoleType{


	public NAryEdgeRoleTypeImpl(Node node) {
		super(node);
	}

	public static NAryEdgeRoleType getOrCreateInstance(DatabaseService db, String name) {
		return new NAryEdgeRoleTypeImpl(EdgeRoleTypeImpl.getOrCreateInstanceNode(db, name));
	}
	
	@Override
	public EdgeRole<NAryEdgeType, NAryEdgeRoleType> getRole(NAryEdgeType edgeType) {
		return new EdgeRole<NAryEdgeType, NAryEdgeRoleType>(this, edgeType);
	}

	@Override
	public String getName() {
		return (String)getNode().getProperty(EDGEROLE_NAME);
	}

}
