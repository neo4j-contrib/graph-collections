/**
 * Copyright (c) 2002-2013 "Neo Technology,"
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

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

/**
 * A NodeCollection provides an interface to store a collection of nodes using some form of in graph data structure.
 */
public interface NodeCollection extends GraphCollection<Node>
{
    /**
     * Add a node to the to a collection.
     *
     * @param node the node to add the the collection.
     *
     * @return the value relationship, this is the relationship from an internal node in the collection to the added node.
     */
    Relationship addNode( Node node );

    /**
     * Returns an iterable over the value relationships. Value relationships are the relationships from the internal
     * data structure to the nodes added to the collection.
     *
     * @return a value relationship iterable.
     */
    Iterable<Relationship> getValueRelationships();
}
