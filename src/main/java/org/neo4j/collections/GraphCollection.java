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
package org.neo4j.collections;

import java.util.Iterator;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.RelationshipType;

/**
 * A GraphCollection provides an interface to store a collection of nodes or relationships using some form of in graph
 * data structure.
 */
public interface GraphCollection<T extends PropertyContainer> extends Iterable<T>
{
    public static enum RelationshipTypes implements RelationshipType
    {
        VALUE
    }

    String GRAPH_COLLECTION_CLASS = "graph_collection_class";

    /**
     * Get the base node for the graph collection. This is generally a node that doesn't directly participate in the
     * data structure but instead points to a significant node within the data structure, i.e. a starting point.
     * <p/>
     * In the case of a tree the base node will point to the root of the tree, but will not itself be the root of the
     * tree. The same is the case for a list with the base node pointing to the head of the list. The reason for this
     * node not being the root or head is that the root of a tree and the head of a list can change over time, which is
     * internal information to the given data structure and users of the graph collection should not need to know when
     * this occurs.
     *
     * @return the node used to base the node collection off
     */
    Node getBaseNode();

    /**
     * Remove the given item form the collection.
     *
     * @param item the item to remove from the collection.
     *
     * @return <code>true</code> if the node was in the collection.
     */
    boolean remove( T item );

    /**
     * Returns an iterator over the items within the collection.
     *
     * @return a iterator.
     */
    Iterator<T> iterator();
}
