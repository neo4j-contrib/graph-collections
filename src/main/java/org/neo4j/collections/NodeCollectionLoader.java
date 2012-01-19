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

import java.lang.reflect.Constructor;

import org.neo4j.graphdb.Node;

/**
 * Load a node collection from the graph database.
 */
public class NodeCollectionLoader
{
    /**
     * Load a node collection from the graph database given the base node of the collection.
     * <p/>
     * To load a collection from
     * the database the node must have a { @link NodeCollection#NODE_COLLECTION_CLASS } property with the full class
     * name of the { @link NodeCollection } class represented by the datastructure in the graph.
     * <p/>
     * The { @link NodeCollection } referenced by this node must have a constructor that takes the base node in order
     * to be loaded.
     *
     * @param baseNode the base node of the { @link NodeCollection } 
     * @return the { @link NodeCollection }
     */
    @SuppressWarnings({"unchecked"})
    public static NodeCollection load( Node baseNode ) {
        String className = (String) baseNode.getProperty( NodeCollection.GRAPH_COLLECTION_CLASS, null );
        if ( className != null ) {
            try {
                Class<? extends NodeCollection> nodeCollectionClass =
                    (Class<? extends NodeCollection>) Class.forName( className );
                Constructor<? extends NodeCollection> constructor = nodeCollectionClass.getConstructor( Node.class );
                return constructor.newInstance( baseNode );
            }
            catch ( Exception e )
            {
                throw new RuntimeException( "Unable to load node collection", e );
            }
        }
        return null;
    }
}
