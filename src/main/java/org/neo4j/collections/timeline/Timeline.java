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
package org.neo4j.collections.timeline;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.neo4j.collections.btree.BTree;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.ReturnableEvaluator;
import org.neo4j.graphdb.StopEvaluator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.TraversalPosition;
import org.neo4j.graphdb.Traverser;
import org.neo4j.graphdb.Traverser.Order;
import org.neo4j.kernel.AbstractGraphDatabase;

import javax.transaction.TransactionManager;


/**
 * An implementation of {@link TimelineIndex} on top of Neo4j, using
 * {@link BTree} for indexing. Note: this implementation is not thread-safe
 * (yet).
 * 
 * Nodes added to a timeline will get a {@link Relationship} created to it so if
 * you delete such a node later on you'll have to remove it from the timeline
 * first (or in the same transaction at least).
 */
public class Timeline implements TimelineIndex
{
    static enum RelTypes implements RelationshipType
    {
        TIMELINE_INSTANCE,
        TIMELINE_NEXT_ENTRY,
    }

    private static final String TIMESTAMP = "timestamp";
    private static final String TIMELINE_NAME = "timeline_name";
    private static final String TIMELINE_IS_INDEXED = "timeline_indexed";
    private static final String INDEX_COUNT = "index_count";
    private static int INDEX_TRIGGER_COUNT = 1000;

    private final Node underlyingNode;
    private final boolean indexed;
    private BTree indexBTree;
    private final String name;
    private final GraphDatabaseService graphDb;

    // lazy init cache holders for first and last
    private Node firstNode;
    private Node lastNode;

    /**
     * Creates/loads a timeline. The <CODE>underlyingNode</CODE> can either be a
     * new (just created) node or a node that already represents a previously
     * timeline.
     * 
     * @param name The unique name of the timeline or <CODE>null</CODE> if
     *            timeline already exist
     * @param underlyingNode The underlying node representing the timeline
     * @param indexed Set to <CODE>true</CODE> if this timeline is indexed
     * @param graphDb the {@link GraphDatabaseService}
     */
    public Timeline( String name, Node underlyingNode, boolean indexed,
            GraphDatabaseService graphDb )
    {
        if ( underlyingNode == null || graphDb == null )
        {
            throw new IllegalArgumentException(
                    "Null parameter underlyingNode=" + underlyingNode
                            + " graphDb=" + graphDb );
        }
        this.underlyingNode = underlyingNode;
        this.graphDb = graphDb;
        Transaction tx = graphDb.beginTx();
        try
        {
            assertPropertyIsSame( TIMELINE_NAME, name );
            this.name = name;
            assertPropertyIsSame( TIMELINE_IS_INDEXED, indexed );
            this.indexed = indexed;
            if ( indexed )
            {
                Relationship bTreeRel = underlyingNode.getSingleRelationship(
                        BTree.RelTypes.TREE_ROOT, Direction.OUTGOING );
                if ( bTreeRel == null )
                {
                    Node bTreeNode = graphDb.createNode();
                    bTreeRel = underlyingNode.createRelationshipTo( bTreeNode,
                            BTree.RelTypes.TREE_ROOT );
                }
                indexBTree = new BTree( graphDb, bTreeRel.getEndNode() );
            }
            tx.success();
        }
        finally
        {
            tx.finish();
        }
    }
    
    public Timeline( String name, Node underlyingNode, boolean indexed, int indexTriggerCount,
            GraphDatabaseService graphDb ) 
    {
        this(name, underlyingNode, indexed, graphDb);
        INDEX_TRIGGER_COUNT = indexTriggerCount;
    }

    private void assertPropertyIsSame( String key, Object value )
    {
        Object storedValue = underlyingNode.getProperty( key, null );
        if ( storedValue != null )
        {
            if ( !storedValue.equals( value ) )
            {
                throw new IllegalArgumentException( "Timeline("
                                                    + underlyingNode
                                                    + ") property '" + key
                                                    + "' is " + storedValue
                                                    + ", passed in " + value );
            }
        }
        else
        {
            underlyingNode.setProperty( key, value );
        }
    }

    /**
     * Creates/loads an indexed timeline. The <CODE>underlyingNode</CODE> can
     * either be a new (just created) node or a node that already represents a
     * previously timeline.
     * 
     * @param name The unique name of the timeline or <CODE>null</CODE> if
     *            timeline already exist
     * @param underlyingNode The underlying node representing the timeline
     * @param graphDb The {@link GraphDatabaseService}.
     */
    public Timeline( String name, Node underlyingNode,
            GraphDatabaseService graphDb )
    {
        this( name, underlyingNode, true, graphDb );
    }

    /**
     * Returns the underlying node representing this timeline.
     * 
     * @return The underlying node representing this timeline
     */
    public Node getUnderlyingNode()
    {
        return underlyingNode;
    }

    public Node getLastNode()
    {
        if ( lastNode != null )
        {
            return lastNode;
        }
            Relationship rel = underlyingNode.getSingleRelationship(
                    RelTypes.TIMELINE_NEXT_ENTRY, Direction.INCOMING );
            if ( rel == null )
            {
                return null;
            }
            lastNode = rel.getStartNode().getRelationships(
                    RelTypes.TIMELINE_INSTANCE, Direction.OUTGOING ).iterator().next().getEndNode();
            return lastNode;
    }

    public Node getFirstNode()
    {
        if ( firstNode != null )
        {
            return firstNode;
        }
            Relationship rel = underlyingNode.getSingleRelationship(
                    RelTypes.TIMELINE_NEXT_ENTRY, Direction.OUTGOING );
            if ( rel == null )
            {
                return null;
            }
            firstNode = rel.getEndNode().getRelationships(
                    RelTypes.TIMELINE_INSTANCE, Direction.OUTGOING ).iterator().next().getEndNode();
            return firstNode;
    }

    public void addNode( Node nodeToAdd, long timestamp )
    {
        if ( nodeToAdd == null )
        {
            throw new IllegalArgumentException( "Null node" );
        }
        Transaction tx = graphDb.beginTx();
        try
        {
            for ( Relationship rel : nodeToAdd.getRelationships( RelTypes.TIMELINE_INSTANCE ) )
            {
                if ( rel.getProperty( TIMELINE_NAME, "" ).equals( name ) )
                {
                    throw new IllegalArgumentException(
                            "Node[" + nodeToAdd.getId()
                                    + "] already connected to Timeline[" + name
                                    + "]" );
                }
            }
            Relationship rel = underlyingNode.getSingleRelationship(
                    RelTypes.TIMELINE_NEXT_ENTRY, Direction.INCOMING );
            if ( rel == null )
            {
                // timeline was empty
                Node node = createNewTimeNode( timestamp, nodeToAdd );
                underlyingNode.createRelationshipTo( node,
                        RelTypes.TIMELINE_NEXT_ENTRY );
                node.createRelationshipTo( underlyingNode,
                        RelTypes.TIMELINE_NEXT_ENTRY );
                firstNode = nodeToAdd;
                lastNode = nodeToAdd;
                updateNodeAdded( timestamp );
            }
            else
            {
                Node previousLast = rel.getStartNode();
                long previousTime = (Long) previousLast.getProperty( TIMESTAMP );
                if ( timestamp > previousTime )
                {
                    // add it last in chain
                    Node node = createNewTimeNode( timestamp, nodeToAdd );
                    rel.delete();
                    previousLast.createRelationshipTo( node,
                            RelTypes.TIMELINE_NEXT_ENTRY );
                    node.createRelationshipTo( underlyingNode,
                            RelTypes.TIMELINE_NEXT_ENTRY );
                    lastNode = nodeToAdd;
                    updateNodeAdded( timestamp );
                }
                else if ( timestamp == previousTime )
                {
                	Relationship instanceRel = previousLast.createRelationshipTo( nodeToAdd,
                            RelTypes.TIMELINE_INSTANCE );
                	instanceRel.setProperty( TIMELINE_NAME, name );
                }
                else
                {
                    // find where to insert
                    Iterator<Node> itr = getAllTimeNodesAfter( timestamp ).iterator();
                    assert itr.hasNext();
                    Node next = itr.next();
                    rel = next.getSingleRelationship(
                            RelTypes.TIMELINE_NEXT_ENTRY, Direction.INCOMING );
                    assert rel != null;
                    Node previous = rel.getStartNode();
                    long previousTimestamp = Long.MIN_VALUE;
                    if ( !previous.equals( underlyingNode ) )
                    {
                        previousTimestamp = (Long) previous.getProperty( TIMESTAMP );
                    }
                    if ( previousTimestamp == timestamp )
                    {
                        // just connect previous with node to add
                    	Relationship instanceRel = previous.createRelationshipTo( nodeToAdd,
                                RelTypes.TIMELINE_INSTANCE );
                    	instanceRel.setProperty( TIMELINE_NAME, name );
                        return;
                    }
                    long nextTimestamp = (Long) next.getProperty( TIMESTAMP );
                    if ( nextTimestamp == timestamp )
                    {
                        // just connect next with node to add
                    	Relationship instanceRel = next.createRelationshipTo( nodeToAdd,
                                RelTypes.TIMELINE_INSTANCE );
                    	instanceRel.setProperty( TIMELINE_NAME, name );
                        return;
                    }

                    assert previousTimestamp < timestamp;
                    assert nextTimestamp > timestamp;

                    Node node = createNewTimeNode( timestamp, nodeToAdd );
                    rel.delete();
                    previous.createRelationshipTo( node,
                            RelTypes.TIMELINE_NEXT_ENTRY );
                    node.createRelationshipTo( next,
                            RelTypes.TIMELINE_NEXT_ENTRY );
                    if ( previous.equals( underlyingNode ) )
                    {
                        firstNode = nodeToAdd;
                    }
                    updateNodeAdded( timestamp );
                }
            }
            tx.success();
        }
        finally
        {
            tx.finish();
        }
    }

    private Node createNewTimeNode( long timestamp, Node nodeToAdd )
    {
        Node node = graphDb.createNode();
        node.setProperty( TIMESTAMP, timestamp );
        Relationship instanceRel = node.createRelationshipTo( nodeToAdd,
                RelTypes.TIMELINE_INSTANCE );
        instanceRel.setProperty( TIMELINE_NAME, name );
        return node;
    }

    public long getTimestampForNode( Node node )
    {
        Transaction tx = graphDb.beginTx();
        try
        {
            Traverser traverser = node.traverse( Traverser.Order.DEPTH_FIRST,
                    StopEvaluator.END_OF_GRAPH, new ReturnableEvaluator()
                    {
                        public boolean isReturnableNode(
                                TraversalPosition position )
                        {
                            Node currentNode = position.currentNode();
                            return currentNode != null
                                   && !currentNode.hasRelationship(
                                           RelTypes.TIMELINE_INSTANCE,
                                           Direction.INCOMING );
                        }
                    }, RelTypes.TIMELINE_INSTANCE, Direction.INCOMING );

            Iterator<Node> hits = traverser.iterator();
            Long result = null;
            if ( hits.hasNext() )
            {
                Node hit = hits.next();
                result = (Long) hit.getProperty( TIMESTAMP );
            }
            else
            {
                throw new RuntimeException(
                        "No timpestamp found for '" + node
                                + "' maybe it's not in the timeline?" );
            }
            tx.success();
            return result;
        }
        finally
        {
            tx.finish();
        }
    }

    private synchronized void updateNodeAdded( final long timestamp )
    {
        if ( !indexed )
        {
            return;
        }
        Long nodeId = (Long) indexBTree.getClosestHigherEntry( timestamp );
        if ( nodeId == null )
        {
            // no indexing yet, check if time to add index
            int indexCount = (Integer) underlyingNode.getProperty( INDEX_COUNT,
                    0 );
            indexCount++;
            if ( indexCount >= INDEX_TRIGGER_COUNT )
            {
                indexCount = createIndex( underlyingNode, indexCount );
            }
            underlyingNode.setProperty( INDEX_COUNT, indexCount );
        }
        else
        {
            Node indexedNode = graphDb.getNodeById( nodeId );
            int indexCount = (Integer) indexedNode.getProperty( INDEX_COUNT );
            indexCount++;
            if ( indexCount >= INDEX_TRIGGER_COUNT )
            {
                indexCount = createIndex( indexedNode, indexCount );
            }
            indexedNode.setProperty( INDEX_COUNT, indexCount );
        }
    }

    // returns new count to set on next higher index and
    // creates the new indexing relationship
    private int createIndex( Node startIndexNode, int currentCount )
    {
        assert indexed;
        int newCount = 0;
        // use 0.33f beacuse most timelines are not random timestamp
        // insertion, instead they just grow at the end, so 0.33 (instead of
        // 0.5) results in less balancing of tree (and tree depth at start)
        int timesToTraverse = (int) ( INDEX_TRIGGER_COUNT * 0.33f );
        assert timesToTraverse > 0;
        Node newIndexedNode = startIndexNode;
        for ( int i = 0; i < timesToTraverse; i++ )
        {
            newIndexedNode = newIndexedNode.getSingleRelationship(
                    RelTypes.TIMELINE_NEXT_ENTRY, Direction.INCOMING ).getStartNode();
            
            newCount++;
            assert !newIndexedNode.hasProperty( INDEX_COUNT );
        }
        long timestamp = (Long) newIndexedNode.getProperty( TIMESTAMP );
        indexBTree.addEntry( timestamp, newIndexedNode.getId() );
        newIndexedNode.setProperty( INDEX_COUNT, currentCount - timesToTraverse );
        return newCount;
    }
    
    public void removeNode(Node nodeToRemove, boolean transactional)
    {
        if ( nodeToRemove == null )
        {
            throw new IllegalArgumentException( "Null parameter." );
        }
        if ( nodeToRemove.equals( underlyingNode ) )
        {
            throw new IllegalArgumentException( "Cannot remove underlying node" );
        }
        Transaction tx = graphDb.beginTx();
        try
        {
            Relationship instanceRel = null;
            for ( Relationship rel : nodeToRemove.getRelationships( RelTypes.TIMELINE_INSTANCE ) )
            {
                if ( rel.getProperty( TIMELINE_NAME, "" ).equals( name ) )
                {
                    assert instanceRel == null;
                    instanceRel = rel;
                }
            }
            if ( instanceRel == null )
            {
                throw new IllegalArgumentException(
                        "Node[" + nodeToRemove.getId()
                                + "] not added to Timeline[" + name + "]" );
            }
            Node node = instanceRel.getStartNode();
            instanceRel.delete();
            if ( firstNode != null && firstNode.equals( nodeToRemove ) )
            {
                firstNode = null;
            }
            if ( lastNode != null && lastNode.equals( nodeToRemove ) )
            {
                lastNode = null;
            }
            if ( node.getRelationships( RelTypes.TIMELINE_INSTANCE ).iterator().hasNext() )
            {
                // still have instances connected to this time
                return;
            }
            Relationship incoming = node.getSingleRelationship(
                    RelTypes.TIMELINE_NEXT_ENTRY, Direction.INCOMING );
            if ( incoming == null )
            {
                throw new RuntimeException( "No incoming relationship of "
                                            + RelTypes.TIMELINE_NEXT_ENTRY
                                            + " found" );
            }
            Relationship outgoing = node.getSingleRelationship(
                    RelTypes.TIMELINE_NEXT_ENTRY, Direction.OUTGOING );
            if ( outgoing == null )
            {
                throw new RuntimeException( "No outgoing relationship of "
                                            + RelTypes.TIMELINE_NEXT_ENTRY
                                            + " found" );
            }
            Node previous = incoming.getStartNode();
            Node next = outgoing.getEndNode();
            incoming.delete();
            outgoing.delete();
            // TODO: this needs proper synchronization
            if ( node.hasProperty( INDEX_COUNT ) )
            {
                long nodeId = (Long) indexBTree.removeEntry( (Long) node.getProperty( TIMESTAMP ) );
                assert nodeId == node.getId();
                int count = (Integer) node.getProperty( INDEX_COUNT );
                count--;
                if ( !previous.equals( underlyingNode )
                     && !previous.hasProperty( INDEX_COUNT ) )
                {
                    previous.setProperty( INDEX_COUNT, count );
                    indexBTree.addEntry(
                            (Long) previous.getProperty( TIMESTAMP ),
                            previous.getId() );
                }
            }
            else
            {
                long timestamp = (Long) node.getProperty( TIMESTAMP );
                if ( indexed )
                {
                    Long nodeId = (Long) indexBTree.getClosestHigherEntry( timestamp );
                    if ( nodeId != null )
                    {
                        Node indexedNode = graphDb.getNodeById( nodeId );
                        int count = (Integer) indexedNode.getProperty( INDEX_COUNT );
                        count--;
                        indexedNode.setProperty( INDEX_COUNT, count );
                    }
                    else
                    {
                        if ( underlyingNode.hasProperty( INDEX_COUNT ) )
                        {
                            int count = (Integer) underlyingNode.getProperty( INDEX_COUNT );
                            count--;
                            underlyingNode.setProperty( INDEX_COUNT, count );
                        }
                    }
                }
            }
            node.delete();
            if ( !previous.equals( next ) )
            {
                previous.createRelationshipTo( next,
                        RelTypes.TIMELINE_NEXT_ENTRY );
            }
            tx.success();
        }
        finally
        {
            if(transactional)
            {
                tx.finish();
            }
        }
    }

    public void removeNode( Node nodeToRemove )
    {
        removeNode( nodeToRemove, true );
    }

    public Iterable<Node> getAllNodes( Long afterTimestampOrNull,
            Long beforeTimestampOrNull )
    {
        Iterable<Node> result = null;
        if ( afterTimestampOrNull == null && beforeTimestampOrNull == null )
        {
            result = getAllNodes();
        }
        else if ( afterTimestampOrNull == null )
        {
            result = getAllNodesBefore( beforeTimestampOrNull );
        }
        else if ( beforeTimestampOrNull == null )
        {
            result = getAllNodesAfter( afterTimestampOrNull );
        }
        else
        {
            result = getAllNodesBetween( afterTimestampOrNull,
                    beforeTimestampOrNull );
        }
        return result;
    }

    public Iterable<Node> getAllNodes()
    {
        return underlyingNode.traverse(
                Order.BREADTH_FIRST,
                StopEvaluator.END_OF_GRAPH,
                new ReturnableEvaluator()
                {
                    public boolean isReturnableNode( TraversalPosition position )
                    {
                        Relationship last = position.lastRelationshipTraversed();
                        if ( last != null
                             && last.isType(
                                     RelTypes.TIMELINE_INSTANCE ) )
                        {
                            return true;
                        }
                        return false;
                    }
                }, RelTypes.TIMELINE_INSTANCE, Direction.OUTGOING,
                RelTypes.TIMELINE_NEXT_ENTRY, Direction.OUTGOING );
    }

    Iterable<Node> getAllTimeNodes()
    {
        return underlyingNode.traverse( Order.DEPTH_FIRST,
                StopEvaluator.END_OF_GRAPH, new ReturnableEvaluator()
                {
                    public boolean isReturnableNode( TraversalPosition position )
                    {
                        return position.depth() > 0;
                    }
                }, RelTypes.TIMELINE_NEXT_ENTRY, Direction.OUTGOING );
    }

    // from closest lower indexed start relationship
    private Node getIndexedStartNode( long timestamp )
    {
        if ( indexed )
        {
            Node startNode = underlyingNode;
            Long nodeId = (Long) indexBTree.getClosestLowerEntry( timestamp );
            if ( nodeId != null )
            {
                startNode = graphDb.getNodeById( nodeId );
            }
            return startNode;
        }
        return underlyingNode;
    }

    public Iterable<Node> getNodes( long timestamp )
    {
        Node currentNode = getIndexedStartNode( timestamp );
        List<Node> nodeList = new ArrayList<Node>();
        if ( currentNode.equals( underlyingNode ) )
        {
            if ( !currentNode.hasRelationship( RelTypes.TIMELINE_NEXT_ENTRY,
                    Direction.OUTGOING ) )
            {
                // empty timeline
                return nodeList;
            }
            // no index or best start node is underlying node
            currentNode = currentNode.getSingleRelationship(
                    RelTypes.TIMELINE_NEXT_ENTRY, Direction.OUTGOING ).getEndNode();
        }
        do
        {
            long currentTime = (Long) currentNode.getProperty( TIMESTAMP );
            if ( currentTime == timestamp )
            {
                for ( Relationship instanceRel : currentNode.getRelationships(
                        RelTypes.TIMELINE_INSTANCE, Direction.OUTGOING ) )
                {
                    nodeList.add( instanceRel.getEndNode() );
                }
                break;
            }
            if ( currentTime > timestamp )
            {
                break;
            }
            Relationship rel = currentNode.getSingleRelationship(
                    RelTypes.TIMELINE_NEXT_ENTRY, Direction.OUTGOING );
            currentNode = rel.getEndNode();
        }
        while ( !currentNode.equals( underlyingNode ) );
        return nodeList;
    }

    public Iterable<Node> getAllNodesAfter( final long timestamp )
    {
        Node startNode = getIndexedStartNode( timestamp );
        return startNode.traverse( Order.DEPTH_FIRST, new StopEvaluator()
        {
            public boolean isStopNode( TraversalPosition position )
            {
                if ( position.lastRelationshipTraversed() != null
                     && position.currentNode().equals( underlyingNode ) )
                {
                    return true;
                }
                return false;
            }
        }, new ReturnableEvaluator()
        {
            private boolean timeOk = false;

            public boolean isReturnableNode( TraversalPosition position )
            {
                if ( position.currentNode().equals( underlyingNode ) )
                {
                    return false;
                }
                Relationship last = position.lastRelationshipTraversed();
                if ( !timeOk && last != null
                     && last.isType( RelTypes.TIMELINE_NEXT_ENTRY ) )
                {
                    Node node = position.currentNode();
                    long currentTime = (Long) node.getProperty( TIMESTAMP );
                    timeOk = currentTime > timestamp;
                    return false;
                }
                if ( timeOk
                     && last.isType( RelTypes.TIMELINE_INSTANCE ) )
                {
                    return true;
                }
                return false;
            }
        }, RelTypes.TIMELINE_NEXT_ENTRY, Direction.OUTGOING,
                RelTypes.TIMELINE_INSTANCE, Direction.OUTGOING );
    }

    Iterable<Node> getAllTimeNodesAfter( final long timestamp )
    {
        Node startNode = getIndexedStartNode( timestamp );
        return startNode.traverse( Order.DEPTH_FIRST, new StopEvaluator()
        {
            public boolean isStopNode( TraversalPosition position )
            {
                if ( position.lastRelationshipTraversed() != null
                     && position.currentNode().equals( underlyingNode ) )
                {
                    return true;
                }
                return false;
            }
        }, new ReturnableEvaluator()
        {
            private boolean timeOk = false;

            public boolean isReturnableNode( TraversalPosition position )
            {
                if ( position.currentNode().equals( underlyingNode ) )
                {
                    return false;
                }
                Relationship last = position.lastRelationshipTraversed();
                if ( !timeOk && last != null
                     && last.isType( RelTypes.TIMELINE_NEXT_ENTRY ) )
                {
                    Node node = position.currentNode();
                    long currentTime = (Long) node.getProperty( TIMESTAMP );
                    timeOk = currentTime > timestamp;
                }
                return timeOk;
            }
        }, RelTypes.TIMELINE_NEXT_ENTRY, Direction.OUTGOING );
    }

    public Iterable<Node> getAllNodesBefore( final long timestamp )
    {
        return underlyingNode.traverse( Order.DEPTH_FIRST, new StopEvaluator()
        {
            public boolean isStopNode( TraversalPosition position )
            {
                Relationship last = position.lastRelationshipTraversed();
                if ( last != null
                     && last.isType( RelTypes.TIMELINE_NEXT_ENTRY ) )
                {
                    Node node = position.currentNode();
                    long currentTime = (Long) node.getProperty( TIMESTAMP );
                    return currentTime >= timestamp;
                }
                return false;
            }
        }, new ReturnableEvaluator()
        {
            public boolean isReturnableNode( TraversalPosition position )
            {
                Relationship last = position.lastRelationshipTraversed();
                if ( last != null
                     && last.isType( RelTypes.TIMELINE_INSTANCE) )
                {
                    return true;
                }
                return false;
            }
        }, RelTypes.TIMELINE_NEXT_ENTRY, Direction.OUTGOING,
                RelTypes.TIMELINE_INSTANCE, Direction.OUTGOING );
    }

    public Iterable<Node> getAllNodesBetween( final long startTime,
            final long endTime )
    {
        if ( startTime >= endTime )
        {
            throw new IllegalArgumentException(
                    "Start time greater or equal to end time" );
        }
        Node startNode = getIndexedStartNode( startTime );
        return startNode.traverse( Order.DEPTH_FIRST, new StopEvaluator()
        {
            public boolean isStopNode( TraversalPosition position )
            {
                Relationship last = position.lastRelationshipTraversed();
                if ( last != null
                     && position.currentNode().equals( underlyingNode ) )
                {
                    return true;
                }
                if ( last != null
                     && last.isType( RelTypes.TIMELINE_NEXT_ENTRY ) )
                {
                    Node node = position.currentNode();
                    long currentTime = (Long) node.getProperty( TIMESTAMP );
                    return currentTime >= endTime;
                }
                return false;
            }
        }, new ReturnableEvaluator()
        {
            private boolean timeOk = false;

            public boolean isReturnableNode( TraversalPosition position )
            {
                if ( position.currentNode().equals( underlyingNode ) )
                {
                    return false;
                }
                Relationship last = position.lastRelationshipTraversed();
                if ( !timeOk && last != null
                     && last.isType( RelTypes.TIMELINE_NEXT_ENTRY ) )
                {
                    Node node = position.currentNode();
                    long currentTime = (Long) node.getProperty( TIMESTAMP );
                    timeOk = currentTime > startTime;
                    return false;
                }
                if ( timeOk
                     && last.isType( RelTypes.TIMELINE_INSTANCE ) )
                {
                    return true;
                }
                return false;
            }
        }, RelTypes.TIMELINE_NEXT_ENTRY, Direction.OUTGOING,
                RelTypes.TIMELINE_INSTANCE, Direction.OUTGOING );
    }

    public void delete()
    {
        if ( indexed )
        {
            indexBTree.delete();
        }
        Relationship rel = underlyingNode.getSingleRelationship(
                RelTypes.TIMELINE_NEXT_ENTRY, Direction.OUTGOING );
        while ( rel != null )
        {
            Node node = rel.getEndNode();
            if ( !node.equals( underlyingNode ) )
            {
                for ( Relationship instance : node.getRelationships( RelTypes.TIMELINE_INSTANCE ) )
                {
                    instance.delete();
                }
                rel.delete();
                rel = node.getSingleRelationship( RelTypes.TIMELINE_NEXT_ENTRY,
                        Direction.OUTGOING );
                node.delete();
            }
            else
            {
                rel.delete();
                rel = null;
            }
        }
    }
    
    public void delete(int commitInterval)
    {
        int count = 0;
        while(this.getLastNode()!=null) {
            
            this.removeNode( this.getLastNode() );
            count++;
            if ( count > commitInterval )
            {
                System.out.print(".");
                restartTx();
                count = 0;
            }
        }
        if ( indexed )
        {
            indexBTree.delete(commitInterval);
        }
    }
    
    private void restartTx() {
            try
            {
                TransactionManager transactionManager = ((AbstractGraphDatabase) graphDb).getDependencyResolver().resolveDependency(TransactionManager.class);
                javax.transaction.Transaction tx = transactionManager.getTransaction();
                if ( tx != null )
                {
                    tx.commit();
                }
            }
            catch ( Exception e )
            {
                throw new RuntimeException( e );
            }
            graphDb.beginTx();
        }
}
