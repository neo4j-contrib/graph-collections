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
package org.neo4j.collections.propertytype;

import org.neo4j.graphdb.Node;

public class FloatProperty extends ComparablePropertyType<Float>{

	public FloatProperty(String name) {
		super(name);
	}

	@Override
	public int compare(Float value, Node node) {
		if(node.hasProperty(getName()))
			throw new RuntimeException("Node does not have property "+getName());

		Float propertyValue = (Float)node.getProperty(getName());
		
		return value.compareTo(propertyValue);
	}

	public int compare(Node node1, Node node2) {
		if(node1.hasProperty(getName()))
			throw new RuntimeException("Node does not have property "+getName());

		Float propertyValue1 = (Float)node1.getProperty(getName());

		if(node2.hasProperty(getName()))
			throw new RuntimeException("Node does not have property "+getName());

		Float propertyValue2 = (Float)node2.getProperty(getName());
		
		return propertyValue1.compareTo(propertyValue2);
	}
	
	
}
