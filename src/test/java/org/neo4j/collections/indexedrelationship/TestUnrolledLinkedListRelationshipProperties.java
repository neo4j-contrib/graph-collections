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
package org.neo4j.collections.indexedrelationship;

import org.junit.Test;
import org.neo4j.collections.Neo4jTestCase;
import org.neo4j.collections.list.UnrolledLinkedList;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

import static junit.framework.Assert.assertEquals;

public class TestUnrolledLinkedListRelationshipProperties extends Neo4jTestCase
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
        INDEXED_RELATIONSHIP
    }

    @Test
    public void testIndexRelationshipAttributes()
    {
        Node indexedNode = graphDb().createNode();
        UnrolledLinkedList ull = new UnrolledLinkedList( graphDb(), new IdComparator(), 4 );
        IndexedRelationship ir = new IndexedRelationship( indexedNode, RelTypes.INDEXED_RELATIONSHIP,
            Direction.OUTGOING, ull );

        Node node1 = graphDb().createNode();
        node1.setProperty( "name", "node 1" );
        Node node2 = graphDb().createNode();
        node2.setProperty( "name", "node 2" );

        Relationship relationship1 = ir.createRelationshipTo( node1 );
        relationship1.setProperty( "rel property", "relationship 1" );
        Relationship relationship2 = ir.createRelationshipTo( node2 );
        relationship2.setProperty( "rel property", "relationship 2" );

        IndexedRelationshipExpander expander = new IndexedRelationshipExpander( graphDb(), Direction.OUTGOING,
            RelTypes.INDEXED_RELATIONSHIP );

        int count = 0;
        for ( Relationship rel : expander.expand( indexedNode ) )
        {
            if ( rel.getEndNode().equals( node1 ) )
            {
                assertEquals( "relationship 1", rel.getProperty( "rel property" ) );
            }

            if ( rel.getEndNode().equals( node2 ) )
            {
                assertEquals( "relationship 2", rel.getProperty( "rel property" ) );
            }

            count++;
        }

        assertEquals( 2, count );
    }

    @Test
    public void testIndexRelationshipAttributesFromDestination()
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
        destination.setProperty( "name", "node 1" );

        Relationship relationship1 = ir.createRelationshipTo( destination );
        relationship1.setProperty( "rel property", "relationship 1" );
        Relationship relationship2 = ir2.createRelationshipTo( destination );
        relationship2.setProperty( "rel property", "relationship 2" );

        IndexedRelationshipExpander expander = new IndexedRelationshipExpander( graphDb(), Direction.INCOMING,
            RelTypes.INDEXED_RELATIONSHIP );

        int count = 0;
        for ( Relationship rel : expander.expand( destination ) )
        {
            if ( rel.getStartNode().equals( indexedNode ) )
            {
                assertEquals( rel.getProperty( "rel property" ), "relationship 1" );
            }

            if ( rel.getStartNode().equals( indexedNode2 ) )
            {
                assertEquals( rel.getProperty( "rel property" ), "relationship 2" );
            }

            count++;
        }

        assertEquals( 2, count );
    }
}