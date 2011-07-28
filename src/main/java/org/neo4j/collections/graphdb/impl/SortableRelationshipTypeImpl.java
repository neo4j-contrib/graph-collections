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

import java.util.HashSet;
import java.util.Set;

import org.neo4j.collections.graphdb.Element;
import org.neo4j.collections.graphdb.GraphDatabaseService;
import org.neo4j.collections.graphdb.PropertyType;
import org.neo4j.collections.graphdb.PropertyType.ComparablePropertyType;
import org.neo4j.collections.graphdb.RelationshipRole;
import org.neo4j.collections.graphdb.SortableRelationshipType;
import org.neo4j.graphdb.RelationshipType;

public class SortableRelationshipTypeImpl<T> extends RelationshipTypeImpl implements SortableRelationshipType<T>{

	private final PropertyType.ComparablePropertyType<T> propertyType;
	
	private static Set<RelationshipRole<?>> roles(GraphDatabaseService graphDb){ 
		Set<RelationshipRole<? extends Element>> roles = new HashSet<RelationshipRole<? extends Element>>();
		roles.add(graphDb.getStartElementRole());
		roles.add(graphDb.getEndElementRole());
		return roles;
	}

	SortableRelationshipTypeImpl(GraphDatabaseService graphDb,
			RelationshipType relType, PropertyType.ComparablePropertyType<T> propertyType) {
		super(graphDb, relType, roles(graphDb));
		this.propertyType = propertyType;
	}

	@Override
	public ComparablePropertyType<T> getPropertyType() {
		return propertyType;
	}

	
	
}
