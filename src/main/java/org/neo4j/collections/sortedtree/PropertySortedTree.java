/**
 * Copyright (c) 2002-2012 "Neo Technology,"
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
package org.neo4j.collections.sortedtree;

import org.neo4j.collections.graphdb.PropertyType.ComparablePropertyType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

public class PropertySortedTree<T> extends SortedTree{

	final ComparablePropertyType<T> propertyType;

    /**
     * @param val the value to check if it's in the tree.
     * @return {@code true} if this list contains the node, otherwise
     * {@code false}.
     */
    public boolean containsValue(T val) {
    	return containsValue(val, propertyType);
    }
    
    public Iterable<Node> getWithValue(T val){
    	return getWithValue(val, propertyType);
    }
    
	public PropertySortedTree( GraphDatabaseService graphDb, ComparablePropertyType<T> propertyType,
                               boolean isUniqueIndex, String treeName ) {
		super(graphDb, propertyType, isUniqueIndex, treeName);
		this.propertyType = propertyType;
	}

}
