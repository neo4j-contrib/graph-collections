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
package org.neo4j.collections.list;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import org.neo4j.collections.GraphCollection;
import org.neo4j.collections.Neo4jTestCase;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

/**
 * @author Bryce Ewing
 * @version 0.1
 */
public class UnrolledLinkedListTestCase extends Neo4jTestCase
{
    protected void checkPageCount( Node baseNode, int min, int max )
    {
        int count = 1;
        Node node = baseNode.getSingleRelationship(
            DynamicRelationshipType.withName( "HEAD" ), Direction.OUTGOING ).getEndNode();
        while ( node.hasRelationship( DynamicRelationshipType.withName( "NEXT_PAGE" ), Direction.OUTGOING ) )
        {
            count++;
            node = node.getSingleRelationship(
                DynamicRelationshipType.withName( "NEXT_PAGE" ), Direction.OUTGOING ).getEndNode();
        }

        assertTrue( "Page count should be greater than or equal to " + min + " was " + count, count >= min );
        assertTrue( "Page count should be less than or equal to " + max + " was " + count, count <= max );
    }

    protected void checkItemCounts( Node baseNode )
    {
        Node page = baseNode.getSingleRelationship(
            DynamicRelationshipType.withName( "HEAD" ), Direction.OUTGOING ).getEndNode();
        do
        {
            Integer count = 0;
            for ( Relationship relationship : page.getRelationships(
                GraphCollection.RelationshipTypes.VALUE, Direction.OUTGOING ) )
            {
                count++;
            }
            assertTrue( page.hasProperty( UnrolledLinkedList.ITEM_COUNT ) );
            assertEquals( count, page.getProperty( UnrolledLinkedList.ITEM_COUNT ) );

            Relationship next = page.getSingleRelationship(
                DynamicRelationshipType.withName( "NEXT_PAGE" ), Direction.OUTGOING );
            if ( next != null )
            {
                page = next.getEndNode();
            }
            else
            {
                page = null;
            }
        }
        while ( page != null );
    }

    protected void removeNodes( ArrayList<Node> nodes, UnrolledLinkedList list, int removalCount )
    {
        int count = 0;
        for ( Iterator<Node> nodeIterator = nodes.iterator(); nodeIterator.hasNext(); )
        {
            Node node = nodeIterator.next();
            assertTrue( list.remove( node ) );
            nodeIterator.remove();

            count++;
            if ( count == removalCount )
            {
                return;
            }
        }
    }

    protected ArrayList<Node> createNodes( int count )
    {
        ArrayList<Node> nodes = new ArrayList<Node>();
        for ( int i = 0; i < count; i++ )
        {
            nodes.add( graphDb().createNode() );
        }
        Collections.sort( nodes, new IdComparator() );
        Collections.reverse( nodes );
        return nodes;
    }

    public static class IdComparator implements java.util.Comparator<Node>
    {
        public int compare( Node n1, Node n2 )
        {
            return ((Long) n1.getId()).compareTo( n2.getId() );
        }
    }

    public static class EqualComparator implements java.util.Comparator<Node>
    {
        public int compare( Node n1, Node n2 )
        {
            return 0;
        }
    }
}
