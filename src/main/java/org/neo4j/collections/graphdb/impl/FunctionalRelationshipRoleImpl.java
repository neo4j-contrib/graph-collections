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

import org.neo4j.collections.graphdb.Element;
import org.neo4j.collections.graphdb.FunctionalRelationshipRole;
import org.neo4j.collections.graphdb.GraphDatabaseService;

public class FunctionalRelationshipRoleImpl<T extends Element> extends RelationshipRoleImpl<T> implements FunctionalRelationshipRole<T>{

	public final static String IS_FUNCTIONAL_ROLE = "org.neo4j.collections.graphdb.is_functional_role";
	
	public FunctionalRelationshipRoleImpl(GraphDatabaseService graphDb, String name) {
		super(graphDb, name);
	}

	@Override
	public org.neo4j.graphdb.Node getNode() {
		org.neo4j.graphdb.Node node = super.getNode();
		if(!node.hasProperty(IS_FUNCTIONAL_ROLE)){
			node.setProperty(IS_FUNCTIONAL_ROLE, true);
		}
		return node;
	}
	
}
