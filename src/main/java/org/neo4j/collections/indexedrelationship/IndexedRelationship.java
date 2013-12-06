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
package org.neo4j.collections.indexedrelationship;

import java.util.Iterator;

import org.neo4j.collections.NodeCollection;
import org.neo4j.collections.NodeCollectionLoader;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

/**
 * A specialized relationship type using a {@link NodeCollection} index.
 * Every relationship is stored in the index, making it possible to
 * have a sorted collection of relationships, or to enforce a unicity
 * constraint on the relationship.
 * <p/>
 * This class is also useful when confronted with situations where
 * normal relationships would lead to densely populated nodes.
 * <p/>
 * Given an end node, the start node of the relationship
 * can be found by traversing:
 * <ol>
 * <li>KEY_VALUE, INCOMING</li>
 * <li>KEY_ENTRY, INCOMING</li>
 * <li>SUB_TREE, INCOMING</li>
 * <li>TREE_ROOT, INCOMING</li>
 * </ol>
 * <p/>
 * The name of the RelationshipType is stored as a property with keyname "tree_name" on both
 * the TREE_ROOT and the KEY_VALUE relationships.
 * <p/>
 * Given a start node of the relationship, all end nodes
 * can be found by traversing:
 * <ol>
 * <li>TREE_ROOT, OUTGOING</li>
 * <li>SUB_TREE, OUTGOING</li>
 * <li>KEY_ENTRY, OUTGOING</li>
 * <li>KEY_VALUE, OUTGOING</li>
 * </ol>
 */
public class IndexedRelationship implements Iterable<Relationship>
{
    public static final String RELATIONSHIP_DIRECTION = "relationship_direction";
    public static final String RELATIONSHIP_TYPE = "relationship_type";

    public static enum RelationshipTypes implements RelationshipType {
        NODE_COLLECTION
    }

    private final Node indexedNode;
    private final RelationshipType relType;
    private final Direction direction;
    private NodeCollection nodeCollection;

    /**
     * Create an IndexedRelationship around all the relationships of a given type from a given node. The underlying
     * NodeCollection must already exist and be attached to the indexed node for the given relationship type and
     * direction.
     *
     * @param indexedNode the node that the indexing is being based on.
     * @param relType {@link org.neo4j.graphdb.RelationshipType} of the relationships maintained in the index.
     * @param direction the {@link org.neo4j.graphdb.Direction} of the relationship.
     */
    public IndexedRelationship( Node indexedNode, RelationshipType relType, Direction direction )
    {
        this.indexedNode = indexedNode;
        this.relType = relType;
        this.direction = direction;
        nodeCollection = getNodeCollection();
    }

    /**
     * Create an IndexedRelationship around all the relationships of a given type from a given node.
     *
     * @param indexedNode the node that the indexing is being based on.
     * @param relType {@link org.neo4j.graphdb.RelationshipType} of the relationships maintained in the index.
     * @param direction the {@link org.neo4j.graphdb.Direction} of the relationship.
     * @param nodeCollection the collection to use to index the nodes linked to by the relationships being indexed.
     */
    public IndexedRelationship( Node indexedNode, RelationshipType relType, Direction direction,
                                    NodeCollection nodeCollection )
    {
        this.indexedNode = indexedNode;
        this.relType = relType;
        this.direction = direction;
        create( nodeCollection );
    }

    /**
     * Does an IndexedRelationship exist at this node for the given relationship type and direction.
     *
     * @return <code>true</code> if an indexed relationship exists.
     */
    public boolean exists()
    {
        return nodeCollection != null;
    }

    /**
     * Create an IndexedRelationship against this node with the given relationship type and direction. The node
     * collection supplied to the IndexedRelationship must already have a base node. The node collection may or may not
     * have any other nodes related to it.
     *
     * @param nodeCollection the { @link NodeCollection } backing this indexed relationship.
     */
    public void create( NodeCollection nodeCollection )
    {
        Relationship indexRelationship = indexedNode.createRelationshipTo( nodeCollection.getBaseNode(), RelationshipTypes.NODE_COLLECTION );
        indexRelationship.setProperty( RELATIONSHIP_TYPE, relType.name() );
        indexRelationship.setProperty( RELATIONSHIP_DIRECTION, direction.name() );
        this.nodeCollection = nodeCollection;
    }

    /**
     * Creates a relationship from the indexed node to the supplied node
     *
     * @param node the end node of the relationship.
     *
     * @return the relationship that was created between the indexedNode and the end node..
     */
    public Relationship createRelationshipTo( Node node )
    {
        Relationship keyValueRelationship = nodeCollection.addNode( node );
        if ( !keyValueRelationship.hasProperty( RELATIONSHIP_DIRECTION ) )
        {
            keyValueRelationship.setProperty( RELATIONSHIP_DIRECTION, direction.name() );
        }
        if ( !keyValueRelationship.hasProperty( RELATIONSHIP_TYPE ) )
        {
            keyValueRelationship.setProperty( RELATIONSHIP_TYPE, relType.name() );
        }
        return getRelationship( keyValueRelationship );
    }

    /**
     * Gets the relationship between the indexed node and the given node.
     *
     * @param relationship the {@link org.neo4j.collections.NodeCollection.RelationshipTypes#VALUE} relationship at the end of the indexed relationship.
     *
     * @return the relationship between the indexed node and the given node.
     */
    public Relationship getRelationship( Relationship relationship )
    {
        return new DirectRelationship( indexedNode, relationship );
    }

    /**
     * Removes the relationship from the indexed node to the supplied node if it exists
     *
     * @param node the end node of the relationship.
     *
     * @return {@code true} if this call modified the index, i.e. if the node
     *         was actually stored in the index.
     */
    public boolean removeRelationshipTo( Node node )
    {
        return nodeCollection.remove( node );
    }

    /**
     * @return the {@link Node} whose outgoing relationships are being indexed.
     */
    public Node getIndexedNode()
    {
        return indexedNode;
    }

    /**
     * @return the {@link RelationshipType} of the indexed relationships.
     */
    public RelationshipType getRelationshipType()
    {
        return relType;
    }

    /**
     * @return the {@link Node}s whose incoming relationships are being indexed.
     */
    public Iterator<Relationship> iterator()
    {
        return new RelationshipIterator();
    }

    private NodeCollection getNodeCollection()
    {
        Relationship indexRelationship = null;
        for ( Relationship candidateRelationship : this.indexedNode.getRelationships( RelationshipTypes.NODE_COLLECTION, Direction.OUTGOING ) )
        {
            String relName = (String) candidateRelationship.getProperty( RELATIONSHIP_TYPE, null );
            if ( relType.name().equals( relName ) )
            {
                String dir = (String) candidateRelationship.getProperty( RELATIONSHIP_DIRECTION, null );
                if ( direction.name().equals( dir ) )
                {
                    if ( indexRelationship != null ) {
                        throw new IllegalStateException(
                            "Multiple IndexedRelationship's on the given node with relationship type: "  +
                                relType.name() + " and direction: " + direction.name());
                    }
                    
                    indexRelationship = candidateRelationship;
                }
            }
        }

        if ( indexRelationship != null )
        {
            return NodeCollectionLoader.load(indexRelationship.getEndNode());
        }

        return null;
    }

    private class RelationshipIterator implements Iterator<Relationship>
    {
        Iterator<Relationship> it = nodeCollection.getValueRelationships().iterator();
        Relationship currentRelationship = null;

        @Override
        public boolean hasNext()
        {
            return it.hasNext();
        }

        @Override
        public Relationship next()
        {
            currentRelationship = it.next();
            return getRelationship( currentRelationship );
        }

        @Override
        public void remove()
        {
            if ( currentRelationship != null )
            {
                removeRelationshipTo( currentRelationship.getEndNode() );
            }
        }
    }

    private class DirectRelationship implements Relationship
    {
        private final Node indexedNode;
        private final Relationship keyValueRelationship;

        DirectRelationship( Node indexedNode, Relationship keyValueRelationship )
        {
            this.indexedNode = indexedNode;
            this.keyValueRelationship = keyValueRelationship;
        }

        @Override
        public GraphDatabaseService getGraphDatabase()
        {
            return indexedNode.getGraphDatabase();
        }

        @Override
        public Object getProperty( String key )
        {
            return keyValueRelationship.getProperty( key );
        }

        @Override
        public Object getProperty( String key, Object defaultValue )
        {
            return keyValueRelationship.getProperty( key, defaultValue );
        }

        @Override
        public Iterable<String> getPropertyKeys()
        {
            return keyValueRelationship.getPropertyKeys();
        }

        @Override
        public boolean hasProperty( String key )
        {
            return keyValueRelationship.hasProperty( key );
        }

        @Override
        public Object removeProperty( String key )
        {
            return keyValueRelationship.removeProperty( key );
        }

        @Override
        public void setProperty( String key, Object value )
        {
            keyValueRelationship.setProperty( key, value );
        }

        @Override
        public void delete()
        {
            removeRelationshipTo( keyValueRelationship.getEndNode() );
        }

        @Override
        public Node getEndNode()
        {
            return direction.equals(Direction.OUTGOING) ? keyValueRelationship.getEndNode() : indexedNode;
        }

        @Override
        public long getId()
        {
            throw new UnsupportedOperationException( "Indexed relationships don't have an ID" );
        }

        @Override
        public Node[] getNodes()
        {
            Node[] nodes = new Node[2];
            nodes[0] = getStartNode();
            nodes[1] = getEndNode();
            return nodes;
        }

        @Override
        public Node getOtherNode( Node node )
        {
            if ( node.equals( indexedNode ) )
            {
                return keyValueRelationship.getEndNode();
            }
            else if ( node.equals( keyValueRelationship.getEndNode() ) )
            {
                return indexedNode;
            }
            else
            {
                throw new RuntimeException( "Node is neither the start nor the end node" );
            }
        }

        @Override
        public Node getStartNode()
        {
            return direction.equals(Direction.OUTGOING) ? indexedNode : keyValueRelationship.getEndNode();
        }

        @Override
        public RelationshipType getType()
        {
            return relType;
        }

        @Override
        public boolean isType( RelationshipType relType )
        {
            return relType.equals( IndexedRelationship.this.relType );
        }
    }
}
