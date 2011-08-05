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

import org.neo4j.collections.graphdb.BinaryEdgeRole;
import org.neo4j.collections.graphdb.BinaryEdgeRoleType;
import org.neo4j.collections.graphdb.BinaryEdgeType;
import org.neo4j.collections.graphdb.DatabaseService;
import org.neo4j.collections.graphdb.EdgeRole;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;

public class BinaryEdgeTypeImpl extends EdgeTypeImpl<BinaryEdgeRoleType> implements BinaryEdgeType{

	public BinaryEdgeTypeImpl(Node node) {
		super(node);
	}

	protected static Class<?> getImplementationClass(){
		try{
			return Class.forName("org.neo4j.collections.graphdb.impl.BinaryEdgeTypeImpl");
		}catch(ClassNotFoundException cce){
			throw new RuntimeException(cce);
		}
	}

	public static BinaryEdgeTypeImpl getOrCreateInstance(DatabaseService db, RelationshipType relType){
		VertexTypeImpl vertexType = new VertexTypeImpl(getOrCreateByDescriptor(new TypeNodeDescriptor(db, relType.name(), getImplementationClass())));
		return new BinaryEdgeTypeImpl(vertexType.getNode());
	}

	public RelationshipType getRelationshipType() {
		return DynamicRelationshipType.withName(getName());
	}

	@SuppressWarnings("unchecked")
	@Override
	public Set<BinaryEdgeRole> getRoles() {
		Set<BinaryEdgeRole> roles = new HashSet<BinaryEdgeRole>();
		roles.add(new BinaryEdgeRole(BinaryEdgeRoleType.StartElement.getOrCreateInstance(getDb()), this));
		roles.add(new BinaryEdgeRole(BinaryEdgeRoleType.EndElement.getOrCreateInstance(getDb()), this));
		return roles;
	}

	@SuppressWarnings("unchecked")
	@Override
	public EdgeRole<BinaryEdgeType, BinaryEdgeRoleType> getRole(BinaryEdgeRoleType edgeRoleType) {
		return new EdgeRole<BinaryEdgeType, BinaryEdgeRoleType>(edgeRoleType, this);
	}
	
}
