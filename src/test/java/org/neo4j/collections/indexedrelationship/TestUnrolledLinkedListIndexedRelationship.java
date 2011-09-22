/**
 * Copyright (c) 2002-2011 "Neo Technology,"
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
package org.neo4j.collections.indexedrelationship;

import org.junit.Test;
import org.neo4j.collections.Neo4jTestCase;
import org.neo4j.collections.list.UnrolledLinkedList;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

import java.util.ArrayList;
import java.util.Collections;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestUnrolledLinkedListIndexedRelationship extends Neo4jTestCase
{
    public static class IdComparator implements java.util.Comparator<Node>
    {
        public int compare( Node n1, Node n2 )
        {
            return ((Long) n1.getId()).compareTo( n2.getId() );
        }
    }

    public static enum RelTypes implements RelationshipType
    {
        DIRECT_RELATIONSHIP,
        INDEXED_RELATIONSHIP,
        INDEXED_RELATIONSHIP_TWO
    }

    @Test
    public void testIndexRelationshipBasic()
    {
        Node indexedNode = graphDb().createNode();
        UnrolledLinkedList ull = new UnrolledLinkedList( graphDb(), new IdComparator(), 4 );
        IndexedRelationship ir = new IndexedRelationship( indexedNode, RelTypes.INDEXED_RELATIONSHIP,
            Direction.OUTGOING, ull );

        Node n1 = graphDb().createNode();
        n1.setProperty( "name", "n1" );
        Node n2 = graphDb().createNode();
        n2.setProperty( "name", "n2" );
        Node n3 = graphDb().createNode();
        n3.setProperty( "name", "n3" );
        Node n4 = graphDb().createNode();
        n4.setProperty( "name", "n4" );

        indexedNode.createRelationshipTo( n1, RelTypes.DIRECT_RELATIONSHIP );
        indexedNode.createRelationshipTo( n3, RelTypes.DIRECT_RELATIONSHIP );
        ir.createRelationshipTo( n2 );
        ir.createRelationshipTo( n4 );

        IndexedRelationshipExpander re1 = new IndexedRelationshipExpander( graphDb(), Direction.OUTGOING,
            RelTypes.DIRECT_RELATIONSHIP );
        IndexedRelationshipExpander re2 = new IndexedRelationshipExpander( graphDb(), Direction.OUTGOING,
            RelTypes.INDEXED_RELATIONSHIP );

        int count = 0;
        for ( Relationship rel : re1.expand( indexedNode ) )
        {
            assertTrue( rel.getEndNode().equals( n1 ) || rel.getEndNode().equals( n3 ) );
            assertEquals( indexedNode, rel.getStartNode() );
            count++;
        }
        assertEquals( 2, count );

        count = 0;
        for ( Relationship rel : re2.expand( indexedNode ) )
        {
            if ( count == 0 )
            {
                assertEquals( n2, rel.getEndNode() );
            }
            if ( count == 1 )
            {
                assertEquals( n4, rel.getEndNode() );
            }
            assertEquals( indexedNode, rel.getStartNode() );
            count++;
        }
        assertEquals( 2, count );
    }

    @Test
    public void testIndexRelationshipIncoming()
    {
        Node indexedNode = graphDb().createNode();
        UnrolledLinkedList ull = new UnrolledLinkedList( graphDb(), new IdComparator(), 4 );
        IndexedRelationship ir = new IndexedRelationship( indexedNode, RelTypes.INDEXED_RELATIONSHIP,
            Direction.INCOMING, ull );

        Node n1 = graphDb().createNode();
        n1.setProperty( "name", "n1" );
        Node n2 = graphDb().createNode();
        n2.setProperty( "name", "n2" );
        Node n3 = graphDb().createNode();
        n3.setProperty( "name", "n3" );
        Node n4 = graphDb().createNode();
        n4.setProperty( "name", "n4" );

        n1.createRelationshipTo( indexedNode, RelTypes.DIRECT_RELATIONSHIP );
        n3.createRelationshipTo( indexedNode, RelTypes.DIRECT_RELATIONSHIP );
        ir.createRelationshipTo( n2 );
        ir.createRelationshipTo( n4 );

        IndexedRelationshipExpander re1 = new IndexedRelationshipExpander( graphDb(), Direction.INCOMING,
            RelTypes.DIRECT_RELATIONSHIP );
        IndexedRelationshipExpander re2 = new IndexedRelationshipExpander( graphDb(), Direction.INCOMING,
            RelTypes.INDEXED_RELATIONSHIP );

        int count = 0;
        for ( Relationship rel : re1.expand( indexedNode ) )
        {
            assertTrue( rel.getStartNode().equals( n1 ) || rel.getStartNode().equals( n3 ) );
            assertEquals( indexedNode, rel.getEndNode() );
            count++;
        }
        assertEquals( 2, count );

        count = 0;
        for ( Relationship rel : re2.expand( indexedNode ) )
        {
            if ( count == 0 )
            {
                assertEquals( n2, rel.getStartNode() );
            }
            if ( count == 1 )
            {
                assertEquals( n4, rel.getStartNode() );
            }
            assertEquals( indexedNode, rel.getEndNode() );
            count++;
        }
        assertEquals( 2, count );
    }

    @Test
    public void testTwoIndexRelationshipsOnSingleNode()
    {
        Node indexedNode = graphDb().createNode();
        UnrolledLinkedList ull1 = new UnrolledLinkedList( graphDb(), new IdComparator(), 4 );
        IndexedRelationship ir = new IndexedRelationship( indexedNode, RelTypes.INDEXED_RELATIONSHIP,
            Direction.OUTGOING, ull1 );

        UnrolledLinkedList ull2 = new UnrolledLinkedList( graphDb(), new IdComparator(), 4 );
        IndexedRelationship ir2 = new IndexedRelationship( indexedNode, RelTypes.INDEXED_RELATIONSHIP_TWO,
            Direction.OUTGOING, ull2 );

        Node n1 = graphDb().createNode();
        n1.setProperty( "name", "n1" );
        Node n2 = graphDb().createNode();
        n2.setProperty( "name", "n2" );
        Node n3 = graphDb().createNode();
        n3.setProperty( "name", "n3" );
        Node n4 = graphDb().createNode();
        n4.setProperty( "name", "n4" );

        ir.createRelationshipTo( n2 );
        ir.createRelationshipTo( n4 );
        ir2.createRelationshipTo( n1 );
        ir2.createRelationshipTo( n3 );

        IndexedRelationshipExpander re1 = new IndexedRelationshipExpander( graphDb(), Direction.OUTGOING,
            RelTypes.INDEXED_RELATIONSHIP_TWO );
        IndexedRelationshipExpander re2 = new IndexedRelationshipExpander( graphDb(), Direction.OUTGOING,
            RelTypes.INDEXED_RELATIONSHIP );

        int count = 0;
        for ( Relationship rel : re1.expand( indexedNode ) )
        {
            assertTrue( rel.getEndNode().equals( n1 ) || rel.getEndNode().equals( n3 ) );
            assertEquals( indexedNode, rel.getStartNode() );
            count++;
        }
        assertEquals( 2, count );

        count = 0;
        for ( Relationship rel : re2.expand( indexedNode ) )
        {
            if ( count == 0 )
            {
                assertEquals( n2, rel.getEndNode() );
            }
            if ( count == 1 )
            {
                assertEquals( n4, rel.getEndNode() );
            }
            assertEquals( indexedNode, rel.getStartNode() );
            count++;
        }
        assertEquals( 2, count );
    }

    @Test
    public void testTwoIndexRelationshipsToSingleDestination()
    {
        Node indexedNode = graphDb().createNode();
        UnrolledLinkedList ull1 = new UnrolledLinkedList( graphDb(), new IdComparator(), 4 );
        IndexedRelationship ir = new IndexedRelationship( indexedNode, RelTypes.INDEXED_RELATIONSHIP,
            Direction.OUTGOING, ull1 );

        Node indexedNode2 = graphDb().createNode();
        UnrolledLinkedList ull2 = new UnrolledLinkedList( graphDb(), new IdComparator(), 4 );
        IndexedRelationship ir2 = new IndexedRelationship( indexedNode2, RelTypes.INDEXED_RELATIONSHIP,
            Direction.OUTGOING, ull2 );

        Node destination = graphDb().createNode();
        destination.setProperty( "name", "n1" );

        ir.createRelationshipTo( destination );
        ir2.createRelationshipTo( destination );

        IndexedRelationshipExpander relationshipExpander = new IndexedRelationshipExpander( graphDb(),
            Direction.OUTGOING, RelTypes.INDEXED_RELATIONSHIP );

        int count = 0;
        for ( Relationship rel : relationshipExpander.expand( indexedNode ) )
        {
            assertEquals( destination, rel.getEndNode() );
            assertEquals( indexedNode, rel.getStartNode() );
            count++;
        }
        for ( Relationship rel : relationshipExpander.expand( indexedNode2 ) )
        {
            assertEquals( destination, rel.getEndNode() );
            assertEquals( indexedNode2, rel.getStartNode() );
            count++;
        }
        assertEquals( 2, count );
    }

    @Test
    public void testIncomingAndOutgoingIndexRelationships()
    {
        Node indexedNode = graphDb().createNode();
        UnrolledLinkedList ull1 = new UnrolledLinkedList( graphDb(), new IdComparator(), 4 );
        IndexedRelationship ir = new IndexedRelationship( indexedNode, RelTypes.INDEXED_RELATIONSHIP,
            Direction.OUTGOING, ull1 );

        Node indexedNode2 = graphDb().createNode();
        UnrolledLinkedList ull2 = new UnrolledLinkedList( graphDb(), new IdComparator(), 4 );
        IndexedRelationship ir2 = new IndexedRelationship( indexedNode2, RelTypes.INDEXED_RELATIONSHIP,
            Direction.INCOMING, ull2 );

        Node leafEnd = graphDb().createNode();
        leafEnd.setProperty( "name", "n1" );

        ir.createRelationshipTo( leafEnd );
        ir2.createRelationshipTo( leafEnd );

        IndexedRelationshipExpander relationshipExpander = new IndexedRelationshipExpander( graphDb(),
            Direction.OUTGOING, RelTypes.INDEXED_RELATIONSHIP );
        IndexedRelationshipExpander relationshipExpander2 = new IndexedRelationshipExpander( graphDb(),
            Direction.INCOMING, RelTypes.INDEXED_RELATIONSHIP );

        int count = 0;
        for ( Relationship rel : relationshipExpander.expand( indexedNode ) )
        {
            assertEquals( leafEnd, rel.getEndNode() );
            assertEquals( indexedNode, rel.getStartNode() );
            count++;
        }
        for ( Relationship rel : relationshipExpander2.expand( indexedNode2 ) )
        {
            assertEquals( leafEnd, rel.getStartNode() );
            assertEquals( indexedNode2, rel.getEndNode() );
            count++;
        }
        assertEquals( 2, count );
    }

    @Test
    public void testIndexedRelationshipExpanderAtDestination()
    {
        Node indexedNode = graphDb().createNode();
        Node nonIndexedNode = graphDb().createNode();
        UnrolledLinkedList ull = new UnrolledLinkedList( graphDb(), new IdComparator(), 4 );
        IndexedRelationship ir = new IndexedRelationship( indexedNode, RelTypes.INDEXED_RELATIONSHIP,
            Direction.OUTGOING, ull );

        Node n1 = graphDb().createNode();
        ir.createRelationshipTo( n1 );
        nonIndexedNode.createRelationshipTo( n1, RelTypes.INDEXED_RELATIONSHIP );

        IndexedRelationshipExpander relationshipExpander = new IndexedRelationshipExpander( graphDb(),
            Direction.INCOMING, RelTypes.INDEXED_RELATIONSHIP );

        int count = 0;
        for ( Relationship rel : relationshipExpander.expand( n1 ) )
        {
            assertTrue( rel.getStartNode().equals( indexedNode ) || rel.getStartNode().equals( nonIndexedNode ) );
            assertEquals( n1, rel.getEndNode() );
            count++;
        }
        assertEquals( 2, count );
    }

    @Test
    public void testIndexedRelationshipExpanderMultiplePage()
    {
        Node indexedNode = graphDb().createNode();
        Node nonIndexedNode = graphDb().createNode();
        UnrolledLinkedList ull = new UnrolledLinkedList( graphDb(), new IdComparator(), 4 );
        IndexedRelationship ir = new IndexedRelationship( indexedNode, RelTypes.INDEXED_RELATIONSHIP,
            Direction.OUTGOING, ull );

        ArrayList<Node> nodes = createNodes( 20 );
        for ( Node node : nodes )
        {
            ir.createRelationshipTo( node );
            nonIndexedNode.createRelationshipTo( node, RelTypes.INDEXED_RELATIONSHIP );
        }

        IndexedRelationshipExpander outgoingRelationshipExpander = new IndexedRelationshipExpander( graphDb(),
            Direction.OUTGOING, RelTypes.INDEXED_RELATIONSHIP );
        IndexedRelationshipExpander incomingRelationshipExpander = new IndexedRelationshipExpander( graphDb(),
            Direction.INCOMING, RelTypes.INDEXED_RELATIONSHIP );

        int count = 0;
        for ( Relationship rel : outgoingRelationshipExpander.expand( indexedNode ) )
        {
            assertTrue( rel.getStartNode().equals( indexedNode ) );
            assertEquals( nodes.get(count), rel.getEndNode() );
            count++;
        }
        assertEquals( nodes.size(), count );

        for ( Node node : nodes )
        {
            count = 0;
            for ( Relationship rel : incomingRelationshipExpander.expand( node ) )
            {
                assertTrue( rel.getStartNode().equals( indexedNode ) || rel.getStartNode().equals( nonIndexedNode ) );
                assertEquals( node, rel.getEndNode() );
                count++;
            }
            assertEquals( 2, count );
        }
    }

    private ArrayList<Node> createNodes( int count )
    {
        ArrayList<Node> nodes = new ArrayList<Node>();
        for ( int i = 0; i < count; i++ )
        {
            nodes.add( graphDb().createNode() );
        }
        Collections.sort( nodes, new IdComparator() );
        return nodes;
    }
}