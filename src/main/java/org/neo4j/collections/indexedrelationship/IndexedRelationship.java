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

import org.neo4j.collections.graphdb.PropertyType.ComparablePropertyType;
import org.neo4j.collections.sortedtree.PropertySortedTree;
import org.neo4j.collections.sortedtree.SortedTree;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

import java.util.Comparator;
import java.util.Iterator;

/**
 * A specialized relationship type using a {@link SortedTree} index.
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
    public static final String DIRECTION_PROPERTY_NAME = "relationship_direction";
    public static final String PROPERTY_TYPE = "org.neo4j.collections.indexedrelationship.property_name";

    private final GraphDatabaseService graphDb;
    private final SortedTree bTree;
    private final Node indexedNode;
    private final RelationshipType relType;
    private final Direction direction;

    private Node createTreeRoot( Node node )
    {
        Node treeRoot = graphDb.createNode();
        Relationship rel = indexedNode.createRelationshipTo( treeRoot, SortedTree.RelTypes.TREE_ROOT );
        rel.setProperty( DIRECTION_PROPERTY_NAME, direction.name() );
        return treeRoot;
    }

    private class DirectRelationship implements Relationship
    {
        final Node indexedNode;
        final Relationship keyValueRelationship;

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
        @Deprecated
        public Iterable<Object> getPropertyValues()
        {
            return keyValueRelationship.getPropertyValues();
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
            if ( key.equals( SortedTree.TREE_NAME ) || key.equals( SortedTree.COMPARATOR_CLASS ) ||
                key.equals( SortedTree.IS_UNIQUE_INDEX ) )
            {
                throw new RuntimeException( "Property value " + key +
                    " is not a valid property name. This property is maintained by the SortedTree implementation" );
            }
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

    private class RelationshipIterator implements Iterator<Relationship>
    {
        Iterator<Relationship> it = bTree.iterator();
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

    /**
     * Create an IndexedRelationship around all the relationships of a given type from a given node.
     *
     * @param relType {@link RelationshipType} of the relationships maintained in the index.
     * @param direction the {@link Direction} of the relationship.
     * @param propertyType the {@link ComparablePropertyType} to use to sort the nodes.
     * @param isUniqueIndex determines if every entry in the tree needs to have a unique comparator value
     * @param node the start node of the relationship.
     * @param graphDb the {@link GraphDatabaseService} instance.
     */
    public <T> IndexedRelationship( RelationshipType relType, Direction direction,
                                    ComparablePropertyType<T> propertyType, boolean isUniqueIndex, Node node,
                                    GraphDatabaseService graphDb )
    {
        indexedNode = node;
        this.relType = relType;
        this.graphDb = graphDb;
        this.direction = direction;
        Relationship rel = getIndexedRootRelationship();
        if ( rel == null )
        {
            createTreeRoot( node );
            rel = getIndexedRootRelationship();
        }
        rel.setProperty( PROPERTY_TYPE, propertyType.getName() );
        bTree = new PropertySortedTree<T>( graphDb, rel.getEndNode(), propertyType, isUniqueIndex, relType.name() );
    }


    /**
     * Create an IndexedRelationship around all the relationships of a given type from a given node.
     * <p/>
     * <b>Note:</b> The comparator that is used for sorting the relationships cannot be an anonymous inner class.
     *
     * @param relType {@link RelationshipType} of the relationships maintained in the index.
     * @param direction the {@link Direction} of the relationship.
     * @param nodeComparator the {@link Comparator} to use to sort the nodes.
     * @param isUniqueIndex determines if every entry in the tree needs to have a unique comparator value
     * @param node the start node of the relationship.
     * @param graphDb the {@link GraphDatabaseService} instance.
     */
    public IndexedRelationship( RelationshipType relType, Direction direction, Comparator<Node> nodeComparator,
                                boolean isUniqueIndex, Node node, GraphDatabaseService graphDb )
    {
        indexedNode = node;
        this.relType = relType;
        this.graphDb = graphDb;
        this.direction = direction;
        Relationship rel = getIndexedRootRelationship();
        Node treeNode = (rel == null) ? createTreeRoot( node ) : rel.getEndNode();
        bTree = new SortedTree( graphDb, treeNode, nodeComparator, isUniqueIndex, relType.name() );
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
        Relationship keyValueRelationship = bTree.addNode( node );
        if ( !keyValueRelationship.hasProperty( DIRECTION_PROPERTY_NAME ) )
        {
            keyValueRelationship.setProperty( DIRECTION_PROPERTY_NAME, direction.name() );
        }
        return getRelationship( keyValueRelationship );
    }

    /**
     * Gets the relationship between the indexed node and the given node.
     *
     * @param relationship the SortedTree.RelTypes.KEY_VALUE relationship at the end of the indexed relationship.
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
        return bTree.removeNode( node );
    }

    /**
     * @return the {@link Node} whose outgoing relationships are being indexed.
     */
    public Node getIndexedNode()
    {
        return indexedNode;
    }

    /**
     * @return the {@link Relationship} pointing to the root of the index tree.
     */
    public Relationship getIndexedRootRelationship()
    {
        Iterable<Relationship> indexRelationships = this.indexedNode.getRelationships( SortedTree.RelTypes.TREE_ROOT );
        for ( Relationship indexRelationship : indexRelationships )
        {
            String relName = (String) indexRelationship.getProperty( SortedTree.TREE_NAME );
            if ( relName.equals( relType.name() ) )
            {
                if ( indexRelationship.hasProperty( IndexedRelationship.DIRECTION_PROPERTY_NAME ) )
                {
                    String dir = (String) indexRelationship.getProperty( IndexedRelationship.DIRECTION_PROPERTY_NAME );
                    if ( dir.equals( direction.name() ) )
                    {
                        return indexRelationship;
                    }
                }
            }
        }
        return null;
    }


    /**
     * @return the {@link RelationshipType} of the indexed relationships.
     */
    public RelationshipType getRelationshipType()
    {
        return relType;
    }

    /**
     * @return {@code true} of the index guarantees unicity
     */
    public boolean isUniqueIndex()
    {
        return bTree.isUniqueIndex();
    }

    /**
     * @return the {@link Node}s whose incoming relationships are being indexed.
     */
    public Iterator<Relationship> iterator()
    {
        return new RelationshipIterator();
    }
}
