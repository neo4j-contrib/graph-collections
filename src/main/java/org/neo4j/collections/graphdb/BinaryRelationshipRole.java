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
package org.neo4j.collections.graphdb;

import org.neo4j.collections.graphdb.impl.RelationshipRoleImpl;
import org.neo4j.graphdb.RelationshipType;

public class BinaryRelationshipRole<T extends Element> extends RelationshipRoleImpl<T>{

	private static final String startElementName = "StartElement";
	private static final String endElementName = "EndElement";
	
	public enum RelTypes implements RelationshipType{
		ROLETYPES_SUBREF
	}

	private BinaryRelationshipRole(GraphDatabaseService graphDb, String name){
		super(graphDb, name);
	}
	

	public static class StartElement extends BinaryRelationshipRole<Element>{
		public StartElement(GraphDatabaseService graphDb){
			super(graphDb, startElementName);
		}
	}

	public static class EndElement extends BinaryRelationshipRole<Element>{
		public EndElement(GraphDatabaseService graphDb){
			super(graphDb, endElementName);
		}
	}

}
