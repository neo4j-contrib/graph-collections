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
package org.neo4j.collections.list;

import org.junit.Test;
import org.neo4j.collections.NodeCollectionLoader;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import java.util.ArrayList;
import java.util.Collections;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;

/**
 * The normal order of adding to an UnrolledLinkedList would be in the end of the list forwards order, e.g. adding
 * oldest by date first when the list is ordered by date (most recent first). This is why the creation of the nodes
 * reverses the order of the nodes after sorting by the comparator, and why the adding in reverse test reverses it again
 * therefore actually adding the items in order.
 */
public class TestUnrolledLinkedList extends UnrolledLinkedListTestCase
{
    @Test
    public void testCreationAndRecreating()
    {
        ArrayList<Node> nodes = createNodes( 2 );
        UnrolledLinkedList list = new UnrolledLinkedList( graphDb(), new IdComparator(), 4 );
        for ( Node node : nodes )
        {
            list.addNode( node );
        }

        Collections.reverse( nodes );
        UnrolledLinkedList loaded = new UnrolledLinkedList( list.getBaseNode() );
        int count = 0;
        for ( Node node : loaded )
        {
            assertEquals( nodes.get( count++ ), node );
        }
        assertEquals( nodes.size(), count );

        checkPageCount( list.getBaseNode(), 1, 1 );
        checkItemCounts( list.getBaseNode() );
    }

    @Test
    public void testCreationAndLoading()
    {
        ArrayList<Node> nodes = createNodes( 2 );
        UnrolledLinkedList list = new UnrolledLinkedList( graphDb(), new IdComparator(), 4 );
        for ( Node node : nodes )
        {
            list.addNode( node );
        }

        Collections.reverse( nodes );
        UnrolledLinkedList loaded = (UnrolledLinkedList) NodeCollectionLoader.load( list.getBaseNode() );

        assertNotNull( loaded );

        int count = 0;
        for ( Node node : loaded )
        {
            assertEquals( nodes.get( count++ ), node );
        }
        assertEquals( nodes.size(), count );

        checkPageCount( list.getBaseNode(), 1, 1 );
        checkItemCounts( list.getBaseNode() );
    }

    @Test
    public void testNormalOrder()
    {
        ArrayList<Node> nodes = createNodes( 20 );
        UnrolledLinkedList list = new UnrolledLinkedList( graphDb(), new IdComparator(), 4 );
        for ( Node node : nodes )
        {
            list.addNode( node );
        }

        Collections.reverse( nodes );
        UnrolledLinkedList loaded = new UnrolledLinkedList( list.getBaseNode() );
        int count = 0;
        for ( Node node : loaded )
        {
            assertEquals( nodes.get( count++ ), node );
        }
        assertEquals( nodes.size(), count );

        count = 0;
        for ( Relationship relationship : loaded.getValueRelationships() )
        {
            assertEquals( nodes.get( count++ ), relationship.getEndNode() );
        }
        assertEquals( nodes.size(), count );

        checkPageCount( list.getBaseNode(), 5, 5 );
        checkItemCounts( list.getBaseNode() );
    }

    @Test
    public void testRandomOrder()
    {
        ArrayList<Node> nodes = createNodes( 20 );
        Collections.shuffle( nodes );
        UnrolledLinkedList list = new UnrolledLinkedList( graphDb(), new IdComparator(), 4 );
        for ( Node node : nodes )
        {
            list.addNode( node );
        }

        Collections.sort( nodes, new IdComparator() );
        UnrolledLinkedList loaded = new UnrolledLinkedList( list.getBaseNode() );
        int count = 0;
        for ( Node node : loaded )
        {
            assertEquals( nodes.get( count++ ), node );
        }
        assertEquals( nodes.size(), count );

        count = 0;
        for ( Relationship relationship : loaded.getValueRelationships() )
        {
            assertEquals( nodes.get( count++ ), relationship.getEndNode() );
        }
        assertEquals( nodes.size(), count );

        checkPageCount( list.getBaseNode(), 4, 6 );
        checkItemCounts( list.getBaseNode() );
    }

    @Test
    public void testReversedOrder()
    {
        ArrayList<Node> nodes = createNodes( 20 );
        Collections.reverse( nodes );
        UnrolledLinkedList list = new UnrolledLinkedList( graphDb(), new IdComparator(), 4 );
        for ( Node node : nodes )
        {
            list.addNode( node );
        }

        Collections.sort( nodes, new IdComparator() );
        UnrolledLinkedList loaded = new UnrolledLinkedList( list.getBaseNode() );
        int count = 0;
        for ( Node node : loaded )
        {
            assertEquals( nodes.get( count++ ), node );
        }
        assertEquals( nodes.size(), count );

        count = 0;
        for ( Relationship relationship : loaded.getValueRelationships() )
        {
            assertEquals( nodes.get( count++ ), relationship.getEndNode() );
        }
        assertEquals( nodes.size(), count );

        checkPageCount( list.getBaseNode(), 6, 6 );
        checkItemCounts( list.getBaseNode() );
    }

    @Test
    public void testAllEqual()
    {
        ArrayList<Node> nodes = createNodes( 20 );
        UnrolledLinkedList list = new UnrolledLinkedList( graphDb(), new EqualComparator(), 4 );
        for ( Node node : nodes )
        {
            list.addNode( node );
        }

        UnrolledLinkedList loaded = new UnrolledLinkedList( list.getBaseNode() );
        int count = 0;
        for ( Node node : loaded )
        {
            count++;
        }
        assertEquals( nodes.size(), count );

        count = 0;
        for ( Relationship relationship : loaded.getValueRelationships() )
        {
            count++;
        }
        assertEquals( nodes.size(), count );

        checkPageCount( list.getBaseNode(), 5, 5 );
        checkItemCounts( list.getBaseNode() );
    }

    @Test
    public void testRemovalNormalOrder()
    {
        ArrayList<Node> nodes = createNodes( 20 );
        UnrolledLinkedList list = new UnrolledLinkedList( graphDb(), new IdComparator(), 4 );
        for ( Node node : nodes )
        {
            list.addNode( node );
        }
        removeNodes( nodes, list, 10 );

        Collections.reverse( nodes );
        UnrolledLinkedList loaded = new UnrolledLinkedList( list.getBaseNode() );
        int count = 0;
        for ( Node node : loaded )
        {
            assertEquals( nodes.get( count++ ), node );
        }
        assertEquals( nodes.size(), count );

        count = 0;
        for ( Relationship relationship : loaded.getValueRelationships() )
        {
            assertEquals( nodes.get( count++ ), relationship.getEndNode() );
        }
        assertEquals( nodes.size(), count );

        checkPageCount( list.getBaseNode(), 3, 3 );
        checkItemCounts( list.getBaseNode() );
    }

    @Test
    public void testRemovalRandomOrder()
    {
        ArrayList<Node> nodes = createNodes( 20 );
        Collections.shuffle( nodes );
        UnrolledLinkedList list = new UnrolledLinkedList( graphDb(), new IdComparator(), 4 );
        for ( Node node : nodes )
        {
            list.addNode( node );
        }
        removeNodes( nodes, list, 10 );

        Collections.sort( nodes, new IdComparator() );
        UnrolledLinkedList loaded = new UnrolledLinkedList( list.getBaseNode() );
        int count = 0;
        for ( Node node : loaded )
        {
            assertEquals( nodes.get( count++ ), node );
        }
        assertEquals( nodes.size(), count );

        count = 0;
        for ( Relationship relationship : loaded.getValueRelationships() )
        {
            assertEquals( nodes.get( count++ ), relationship.getEndNode() );
        }
        assertEquals( nodes.size(), count );

        checkPageCount( list.getBaseNode(), 2, 5 );
        checkItemCounts( list.getBaseNode() );
    }

    @Test
    public void testRemovalReversedOrder()
    {
        ArrayList<Node> nodes = createNodes( 20 );
        Collections.reverse( nodes );
        UnrolledLinkedList list = new UnrolledLinkedList( graphDb(), new IdComparator(), 4 );
        for ( Node node : nodes )
        {
            list.addNode( node );
        }
        removeNodes( nodes, list, 10 );

        Collections.sort( nodes, new IdComparator() );
        UnrolledLinkedList loaded = new UnrolledLinkedList( list.getBaseNode() );
        int count = 0;
        for ( Node node : loaded )
        {
            assertEquals( nodes.get( count++ ), node );
        }
        assertEquals( nodes.size(), count );

        count = 0;
        for ( Relationship relationship : loaded.getValueRelationships() )
        {
            assertEquals( nodes.get( count++ ), relationship.getEndNode() );
        }
        assertEquals( nodes.size(), count );

        checkPageCount( list.getBaseNode(), 3, 3 );
        checkItemCounts( list.getBaseNode() );
    }

    @Test
    public void testRemoveAllEqual()
    {
        ArrayList<Node> nodes = createNodes( 20 );
        UnrolledLinkedList list = new UnrolledLinkedList( graphDb(), new EqualComparator(), 4 );
        for ( Node node : nodes )
        {
            list.addNode( node );
        }
        removeNodes( nodes, list, 10 );

        UnrolledLinkedList loaded = new UnrolledLinkedList( list.getBaseNode() );
        int count = 0;
        for ( Node node : loaded )
        {
            count++;
        }
        assertEquals( nodes.size(), count );

        count = 0;
        for ( Relationship relationship : loaded.getValueRelationships() )
        {
            count++;
        }
        assertEquals( nodes.size(), count );

        checkPageCount( list.getBaseNode(), 3, 3 );
        checkItemCounts( list.getBaseNode() );
    }

    @Test
    public void testRemoveNonExisting()
    {
        ArrayList<Node> nodes = createNodes( 20 );
        UnrolledLinkedList list = new UnrolledLinkedList( graphDb(), new EqualComparator(), 4 );
        for ( Node node : nodes )
        {
            list.addNode( node );
        }

        assertFalse( list.remove( graphDb().createNode() ) );

        UnrolledLinkedList loaded = new UnrolledLinkedList( list.getBaseNode() );
        int count = 0;
        for ( Node node : loaded )
        {
            count++;
        }
        assertEquals( nodes.size(), count );

        count = 0;
        for ( Relationship relationship : loaded.getValueRelationships() )
        {
            count++;
        }
        assertEquals( nodes.size(), count );

        checkItemCounts( list.getBaseNode() );
    }

    @Test
    public void testValueRelationshipKeepsAttributes()
    {
        ArrayList<Node> nodes = createNodes( 20 );
        UnrolledLinkedList list = new UnrolledLinkedList( graphDb(), new IdComparator(), 4 );
        
        // Nodes are added last first therefore reverse the order of the numbers when adding
        int count = 19;
        for ( Node node : nodes )
        {
            Relationship relationship = list.addNode( node );
            relationship.setProperty( "count", count-- );
        }

        Collections.reverse( nodes );
        UnrolledLinkedList loaded = new UnrolledLinkedList( list.getBaseNode() );
        count = 0;
        for ( Relationship relationship : loaded.getValueRelationships() )
        {
            assertEquals( count, relationship.getProperty( "count" ) );
            assertEquals( nodes.get( count++ ), relationship.getEndNode() );
        }
        assertEquals( nodes.size(), count );
    }
}