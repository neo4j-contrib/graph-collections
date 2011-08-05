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

import org.neo4j.collections.graphdb.impl.EdgeRoleTypeImpl;
import org.neo4j.graphdb.Node;

public abstract class BinaryEdgeRoleType extends EdgeRoleTypeImpl implements FunctionalEdgeRoleType{

	private BinaryEdgeRoleType(Node node) {
		super(node);
	}

	private static final String startElementName = "StartElement";
	private static final String endElementName = "EndElement";

	public static class StartElement extends BinaryEdgeRoleType{

		public StartElement(Node node){
			super(node);
		}
		

		public static StartElement getOrCreateInstance(DatabaseService db){
			return new StartElement(EdgeRoleTypeImpl.getOrCreateInstanceNode(db, startElementName));
		}
		
		@Override
		public String getName() {
			return startElementName;
		}


	}

	public static class EndElement extends BinaryEdgeRoleType{
		
		public EndElement(Node node){
			super(node);
		}
		
		public static EndElement getOrCreateInstance(DatabaseService db){
			return new EndElement(EdgeRoleTypeImpl.getOrCreateInstanceNode(db, endElementName));		
		}
		
		@Override
		public String getName() {
			return endElementName;
		}

	}
}
