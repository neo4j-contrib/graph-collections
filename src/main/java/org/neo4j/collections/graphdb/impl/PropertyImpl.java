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

import org.neo4j.collections.graphdb.Property;
import org.neo4j.collections.graphdb.PropertyContainer;
import org.neo4j.collections.graphdb.PropertyType;

public class PropertyImpl<T> implements Property<T>{

	private final PropertyContainer pc;
	private final PropertyType<T> propertyType;

	PropertyImpl(PropertyContainer pc, PropertyType<T> propertyType){
		this.pc = pc;
		this.propertyType = propertyType;
	}
	
	@Override
	public T getValue() {
		return pc.getPropertyValue(propertyType);
	}

	@Override
	public PropertyType<T> getPropertyType() {
		return propertyType;
	}

	@Override
	public PropertyContainer getPropertyContainer() {
		return pc;
	}

}
