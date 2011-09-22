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
package org.neo4j.collections.list;

import org.neo4j.collections.GraphCollection;
import org.neo4j.collections.NodeCollection;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Implementation of an UnrolledLinkedList for storage of nodes. This collection is primarily for use within an
 * {@link org.neo4j.collections.indexedrelationship.IndexedRelationship}. The benefits of the UnrolledLinkedList is
 * that it has very good performance in cases where items are added at the head of the list, and items are generally
 * read from the head in the order they are added.
 * <p/>
 * The structure is broken into "pages" of links to nodes where the size of the page can be controlled at initial
 * construction time. Page size is not fixed but instead can float between a lower bound, and an upper bound. The bounds
 * are at a fixed margin from the page size of M. When a page drops below the lower bound it will be joined onto the an
 * adjacent page, and when the page goes above the upper bound it will be split in half. <b>IMPORTANT NOTE:</b> the
 * margin must be greater than a third of the page size in order to stop a page being split, then both pages being lower
 * than the lower bound, the default margin is 1/2 page size.
 * <p/>
 * The exception to the bounds is the head page which can contain less than the lower bound. A new head page is created
 * when adding a new node to the current head would put it above the page size, and instead a new head will be created
 * that has a single node in it.
 * <p/>
 * There is a trade off between page size and number of page to page relationships need to be followed. As nodes are not
 * stored sorted within the page they must all be read in and compared using the comparator in order to iterate over
 * them in order. However any given node within P(X) will be in order compared to any given node within P(X+1).
 * <p/>
 * A perfect example would be an inbox where content is added sorted by date and always added in increasing date order.
 * Then the content is read out from most recent backwards in time and generally only the first page of content is
 * retrieved.
 * <p/>
 * This data structure is not as good as a sorted tree when content is added randomly against the given order, or where
 * the items have their position within the list change based on data changes.
 */
public class UnrolledLinkedList implements NodeCollection
{
    private static enum RelationshipTypes implements RelationshipType
    {
        NEXT_PAGE, HEAD
    }

    public static final String COMPARATOR_CLASS = "comparator_class";
    public static final String PAGE_SIZE = "page_size";
    public static final String MARGIN = "margin";
    public static final String ITEM_COUNT = "item_count";

    private final Node baseNode;
    private final Comparator<Node> nodeComparator;
    private final Comparator<Relationship> relationshipComparator;
    private final int pageSize;
    private final int margin;

    /**
     * Instantiate a previously stored UnrolledLinkedList from the base node.
     *
     * @param baseNode the base node of the sorted tree.
     */
    @SuppressWarnings({"unchecked"})
    public UnrolledLinkedList( Node baseNode )
    {
        this.baseNode = baseNode;

        try
        {
            String comparatorClass = (String) baseNode.getProperty( COMPARATOR_CLASS );
            nodeComparator = (Comparator<Node>) Class.forName( comparatorClass ).newInstance();
            relationshipComparator = new Comparator<Relationship>()
            {
                @Override
                public int compare( Relationship o1, Relationship o2 )
                {
                    return nodeComparator.compare( o1.getEndNode(), o2.getEndNode() );
                }
            };
            pageSize = (Integer) baseNode.getProperty( PAGE_SIZE );
            margin = (Integer) baseNode.getProperty( MARGIN );
        }
        catch ( Exception e )
        {
            throw new IllegalStateException( "Unable to re-instantiate UnrolledLinkedList from graph data structure.", e );
        }
    }

    /**
     * Create a new UnrolledLinkedList within the graph database.
     *
     * @param graphDb the {@link org.neo4j.graphdb.GraphDatabaseService} instance.
     * @param nodeComparator the {@link java.util.Comparator} to use to sort the nodes.
     * @param pageSize the page size.
     */
    public UnrolledLinkedList( GraphDatabaseService graphDb, Comparator<Node> nodeComparator, int pageSize )
    {
        this( graphDb, nodeComparator, pageSize, pageSize / 2 );
    }

    /**
     * Create a new UnrolledLinkedList within the graph database.
     *
     * @param graphDb the {@link org.neo4j.graphdb.GraphDatabaseService} instance.
     * @param nodeComparator the {@link java.util.Comparator} to use to sort the nodes.
     * @param pageSize the page size;
     * @param margin the margin to define the lower and upper bounds, note: must be greater than a third of page size.
     */
    public UnrolledLinkedList( GraphDatabaseService graphDb, final Comparator<Node> nodeComparator, int pageSize, int margin )
    {
        if ( 3 * margin < pageSize )
        {
            throw new IllegalArgumentException( "Margin must be greater than a third of the page size." );
        }

        baseNode = graphDb.createNode();
        baseNode.setProperty( GRAPH_COLLECTION_CLASS, UnrolledLinkedList.class.getName() );
        baseNode.setProperty( COMPARATOR_CLASS, nodeComparator.getClass().getName() );
        baseNode.setProperty( PAGE_SIZE, pageSize );
        baseNode.setProperty( MARGIN, margin );

        this.nodeComparator = nodeComparator;
        relationshipComparator = new Comparator<Relationship>()
        {
            @Override
            public int compare( Relationship o1, Relationship o2 )
            {
                return nodeComparator.compare( o1.getEndNode(), o2.getEndNode() );
            }
        };
        this.pageSize = pageSize;
        this.margin = margin;
    }

    @Override
    public Relationship addNode( Node node )
    {
        acquireLock();

        Node page = checkSplitNode( getPage( node ), node );
        page.setProperty( ITEM_COUNT, ((Integer) page.getProperty( ITEM_COUNT )) + 1 );
        return page.createRelationshipTo( node, GraphCollection.RelationshipTypes.VALUE );
    }

    @Override
    public Node getBaseNode()
    {
        return baseNode;
    }

    @Override
    public boolean remove( Node node )
    {
        Node page = getPage( node );
        do
        {
            for ( Relationship relationship : page.getRelationships(
                GraphCollection.RelationshipTypes.VALUE, Direction.OUTGOING ) )
            {
                Node candidate = relationship.getEndNode();
                if ( candidate.equals( node ) )
                {
                    relationship.delete();
                    page.setProperty( ITEM_COUNT, ((Integer) page.getProperty( ITEM_COUNT )) - 1 );
                    checkJoinNode( page );
                    return true;
                }
            }

            // If some values are compared as equal then this node could be in the next page
            Relationship nextPageRelationship = page.getSingleRelationship(
                RelationshipTypes.NEXT_PAGE, Direction.OUTGOING );
            if ( nextPageRelationship == null )
            {
                return false;
            }

            page = nextPageRelationship.getEndNode();
        }
        while ( shouldContainNode( page, node ) );


        return false;
    }

    @Override
    public Iterator<Node> iterator()
    {
        return new NodeIterator( getHead(), nodeComparator );
    }

    @Override
    public Iterable<Relationship> getValueRelationships()
    {
        return new Iterable<Relationship>()
        {
            @Override
            public Iterator<Relationship> iterator()
            {
                return new RelationshipIterator( getHead(), relationshipComparator );
            }
        };
    }

    private Node getHead()
    {
        Node head;
        Relationship relationship = baseNode.getSingleRelationship( RelationshipTypes.HEAD, Direction.OUTGOING );
        if ( relationship != null )
        {
            head = relationship.getEndNode();
        }
        else
        {
            head = baseNode.getGraphDatabase().createNode();
            head.setProperty( ITEM_COUNT, 0 );
            baseNode.createRelationshipTo( head, RelationshipTypes.HEAD );
        }
        return head;
    }

    private Node getPage( Node node )
    {
        Node pageNode = getHead();
        while ( true )
        {
            if ( shouldContainNode( pageNode, node ) )
            {
                return pageNode;
            }

            Relationship nextPageRelationship = pageNode.getSingleRelationship(
                RelationshipTypes.NEXT_PAGE, Direction.OUTGOING );
            if ( nextPageRelationship == null )
            {
                return pageNode;
            }

            pageNode = nextPageRelationship.getEndNode();
        }
    }

    private boolean shouldContainNode( Node pageNode, Node node )
    {
        for ( Relationship relationship : pageNode.getRelationships(
            GraphCollection.RelationshipTypes.VALUE, Direction.OUTGOING ) )
        {
            Node valueNode = relationship.getEndNode();
            if ( nodeComparator.compare( node, valueNode ) <= 0 )
            {
                return true;
            }
        }
        return false;
    }

    private Node checkSplitNode( Node candidatePage, Node node )
    {
        int count = (Integer) candidatePage.getProperty( ITEM_COUNT );
        if ( (count + 1) > (pageSize + margin) )
        {
            // If we are about to go above the upper bound then split the current page and return whichever page the
            // new node should fall within.

            ArrayList<Relationship> relationships = getSortedRelationships( candidatePage );
            Node newPage = baseNode.getGraphDatabase().createNode();
            int moveCount = count / 2;
            moveFirstRelationships( relationships, newPage, moveCount );
            newPage.setProperty( ITEM_COUNT, moveCount );
            candidatePage.setProperty( ITEM_COUNT, count - moveCount );

            Relationship previous = candidatePage.getSingleRelationship(
                RelationshipTypes.NEXT_PAGE, Direction.INCOMING );
            if ( previous != null )
            {
                Node previousNode = previous.getStartNode();
                previous.delete();
                previousNode.createRelationshipTo( newPage, RelationshipTypes.NEXT_PAGE );
            }
            else
            {
                Relationship head = candidatePage.getSingleRelationship(
                    RelationshipTypes.HEAD, Direction.INCOMING );
                if ( head != null )
                {
                    head.delete();
                    baseNode.createRelationshipTo( newPage, RelationshipTypes.HEAD );
                }
                else
                {
                    throw new IllegalStateException(
                        "Candidate node does not have incoming next page or head relationships" );
                }
            }
            newPage.createRelationshipTo( candidatePage, RelationshipTypes.NEXT_PAGE );
            if ( shouldContainNode( newPage, node ) )
            {
                return newPage;
            }
        }
        else if ( (count + 1) > pageSize )
        {
            Relationship head = candidatePage.getSingleRelationship(
                RelationshipTypes.HEAD, Direction.INCOMING );
            if ( head != null )
            {
                // If we are at the page size (or above) and we are adding to the head then if the new node is the first
                // then split off a new head and add it to this.

                boolean first = true;
                for ( Relationship relationship : candidatePage.getRelationships(
                    GraphCollection.RelationshipTypes.VALUE, Direction.OUTGOING ) )
                {
                    Node valueNode = relationship.getEndNode();
                    if ( nodeComparator.compare( node, valueNode ) > 0 )
                    {
                        first = false;
                        break;
                    }
                }

                if ( first )
                {
                    Node newHead = baseNode.getGraphDatabase().createNode();
                    newHead.setProperty( ITEM_COUNT, 0 );
                    head.delete();
                    baseNode.createRelationshipTo( newHead, RelationshipTypes.HEAD );
                    newHead.createRelationshipTo( candidatePage, RelationshipTypes.NEXT_PAGE );
                    return newHead;
                }
            }
        }
        return candidatePage;
    }

    private void checkJoinNode( Node candidatePage )
    {
        int count = (Integer) candidatePage.getProperty( ITEM_COUNT );
        Relationship head = candidatePage.getSingleRelationship( RelationshipTypes.HEAD, Direction.INCOMING );
        if ( head != null )
        {
            // Don't join head even if falling below lower bound, unless it is empty, then drop the page

            if ( count == 0 )
            {
                Relationship next = candidatePage.getSingleRelationship(
                    RelationshipTypes.NEXT_PAGE, Direction.OUTGOING );
                if ( next != null )
                {
                    head.delete();
                    candidatePage.delete();
                    baseNode.createRelationshipTo( next.getEndNode(), RelationshipTypes.HEAD );
                    next.delete();
                }
            }
        }
        else if ( count < (pageSize - margin) )
        {
            Relationship previousRelationship = candidatePage.getSingleRelationship(
                RelationshipTypes.NEXT_PAGE, Direction.INCOMING );
            Relationship nextRelationship = candidatePage.getSingleRelationship(
                RelationshipTypes.NEXT_PAGE, Direction.OUTGOING );

            Node previous = previousRelationship.getStartNode();
            Node next = nextRelationship != null ? nextRelationship.getEndNode() : null;

            int previousCount = (Integer) previous.getProperty( ITEM_COUNT );
            int nextCount = next != null ? (Integer) next.getProperty( ITEM_COUNT ) : -1;

            if ( (count + previousCount) <= (pageSize + margin) )
            {
                // Move this pages nodes into previous page

                moveValueRelationships( candidatePage, previous );
                previous.setProperty( ITEM_COUNT, previousCount + count );

                previousRelationship.delete();
                if ( next != null )
                {
                    nextRelationship.delete();
                    previous.createRelationshipTo( next, RelationshipTypes.NEXT_PAGE );
                }
                candidatePage.delete();
            }
            else if ( nextCount != -1 && (count + nextCount) <= (pageSize + margin) )
            {
                // Move this pages nodes into next page

                moveValueRelationships( candidatePage, next );
                next.setProperty( ITEM_COUNT, nextCount + count );

                previousRelationship.delete();
                nextRelationship.delete();
                previous.createRelationshipTo( next, RelationshipTypes.NEXT_PAGE );
                candidatePage.delete();
            }
            else if ( previousCount > nextCount )
            {
                // Joining the pages will force a split again, therefore bypass this by moving nodes out of previous
                // page into this page

                ArrayList<Relationship> relationships = getSortedRelationships( previous );
                Collections.reverse( relationships );
                int moveCount = ((previousCount + count) / 2) - count;
                moveFirstRelationships( relationships, candidatePage, moveCount );
                previous.setProperty( ITEM_COUNT, previousCount - moveCount );
                candidatePage.setProperty( ITEM_COUNT, count + moveCount );
            }
            else if ( nextCount != -1 ) // should never get here if nextCount == -1 since previousCount will be greater
            {
                // Joining the pages will force a split again, therefore bypass this by moving nodes out of next
                // page into this page 

                ArrayList<Relationship> relationships = getSortedRelationships( next );
                int moveCount = ((nextCount + count) / 2) - count;
                moveFirstRelationships( relationships, candidatePage, moveCount );
                next.setProperty( ITEM_COUNT, nextCount - moveCount );
                candidatePage.setProperty( ITEM_COUNT, count + moveCount );
            }
        }
    }

    private ArrayList<Relationship> getSortedRelationships( Node candidatePage )
    {
        ArrayList<Relationship> relationships = new ArrayList<Relationship>();
        for ( Relationship relationship : candidatePage.getRelationships(
            GraphCollection.RelationshipTypes.VALUE, Direction.OUTGOING ) )
        {
            relationships.add( relationship );
        }
        Collections.sort( relationships, relationshipComparator );
        return relationships;
    }

    private void moveValueRelationships( Node candidatePage, Node previous )
    {
        for ( Relationship valueRelationship : candidatePage.getRelationships(
            GraphCollection.RelationshipTypes.VALUE, Direction.OUTGOING ) )
        {
            moveValueRelationship( valueRelationship, previous );
        }
    }

    private void moveFirstRelationships( ArrayList<Relationship> relationships, Node targetPage, int moveCount )
    {
        for ( Relationship relationship : relationships )
        {
            moveValueRelationship( relationship, targetPage );
            moveCount--;
            if ( moveCount == 0 )
            {
                break;
            }
        }
    }

    private void moveValueRelationship( Relationship valueRelationship, Node targetPage )
    {
        Relationship newRelationship = targetPage.createRelationshipTo(
            valueRelationship.getEndNode(), GraphCollection.RelationshipTypes.VALUE );
        for ( String key : valueRelationship.getPropertyKeys() )
        {
            newRelationship.setProperty( key, valueRelationship.getProperty( key ) );
        }
        valueRelationship.delete();
    }

    private void acquireLock()
    {
        baseNode.removeProperty( "___dummy_property_to_acquire_lock___" );
    }

    private static abstract class ItemIterator<T> implements Iterator<T>
    {
        private Node currentPage;
        private Comparator<T> itemComparator;

        private ArrayList<T> currentItems;
        private int position;
        private boolean hasNext;

        public ItemIterator( Node head, Comparator<T> comparator )
        {
            currentPage = head;
            itemComparator = comparator;
            hasNext = true;
            populate();
            checkNext();
        }

        @Override
        public boolean hasNext()
        {
            return hasNext;
        }

        @Override
        public T next()
        {
            if ( !hasNext )
            {
                throw new NoSuchElementException();
            }

            T item = currentItems.get( position++ );
            checkNext();
            return item;
        }

        @Override
        public void remove()
        {
            throw new UnsupportedOperationException();
        }

        private void checkNext()
        {
            while ( position == currentItems.size() )
            {
                Relationship nextPageRelationship = currentPage.getSingleRelationship(
                    RelationshipTypes.NEXT_PAGE, Direction.OUTGOING );
                if ( nextPageRelationship != null )
                {
                    currentPage = nextPageRelationship.getEndNode();
                    populate();
                }
                else
                {
                    hasNext = false;
                    break;
                }
            }
        }

        private void populate()
        {
            currentItems = new ArrayList<T>();
            position = 0;
            for ( Relationship relationship : currentPage.getRelationships(
                GraphCollection.RelationshipTypes.VALUE, Direction.OUTGOING ) )
            {
                currentItems.add( getItem( relationship ) );
            }
            Collections.sort( currentItems, itemComparator );
        }

        protected abstract T getItem( Relationship relationship );
    }

    private static class NodeIterator extends ItemIterator<Node>
    {
        public NodeIterator( Node head, Comparator<Node> comparator )
        {
            super( head, comparator );
        }

        @Override
        protected Node getItem( Relationship relationship )
        {
            return relationship.getEndNode();
        }
    }

    private static class RelationshipIterator extends ItemIterator<Relationship>
    {
        public RelationshipIterator( Node head, Comparator<Relationship> comparator )
        {
            super( head, comparator );
        }

        @Override
        protected Relationship getItem( Relationship relationship )
        {
            return relationship;
        }
    }
}
