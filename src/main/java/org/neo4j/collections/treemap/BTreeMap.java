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
package org.neo4j.collections.treemap;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.neo4j.collections.btree.BTree;
import org.neo4j.collections.btree.KeyEntry;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;

/**
 * A map implementation using {@link org.neo4j.index.impl.btree.BTree BTree}
 * <p>
 * Note: this implementation is not thread safe (yet).
 * @param <K> The key type
 * @param <V> The value type
 * 
 * This class isn't ready for general usage yet and use of it is discouraged.
 * 
 */
public class BTreeMap<K,V> implements Map<K,V>
{
	static enum RelTypes implements RelationshipType
	{
		MAP_ENTRY,
	}
	
	private static final Object GOTO_NODE = Long.MIN_VALUE;
	
	private static final String MAP_NAME = "map_name";
	private static final String MAP_KEY = "map_key";
	private static final String MAP_VALUE = "map_value";
	// private static final String GOTO_NODE = "goto_node";
	
	private final Node underlyingNode;
	private BTree bTree;
	private String name;
	private GraphDatabaseService graphDb;
	
	
	/**
	 * Creates/loads a persistent map based on a b-tree. 
	 * The {@code underlyingNode} can either be a new (just created) node 
	 * or a node that already represents a previously created map.
	 *
	 * @param name The unique name of the map or null if map already
	 * created (using specified underlying node). The name must match the name
	 * of the underlyingNode, otherwise an {@link IllegalArgumentException}
	 * will be thrown.
	 * @param underlyingNode The underlying node representing the map
	 * @param graphDb The {@link GraphDatabaseService} instante.
	 * @throws IllegalArgumentException if the underlying node is a map with
	 * a different name set.
	 */
	public BTreeMap( String name, Node underlyingNode,
	    GraphDatabaseService graphDb )
	{
		if ( underlyingNode == null || graphDb == null )
		{
			throw new IllegalArgumentException( 
				"Null parameter underlyingNode=" + underlyingNode +
				" graphDb=" + graphDb );
		}
		this.underlyingNode = underlyingNode;
		this.graphDb = graphDb;
		Transaction tx = graphDb.beginTx();
		try
		{
			if ( underlyingNode.hasProperty( MAP_NAME ) )
			{
				String storedName = (String) underlyingNode.getProperty( 
					MAP_NAME );
				if ( name != null && !storedName.equals( name ) )
				{
					throw new IllegalArgumentException( "Name of map " + 
						"for node=" + underlyingNode.getId() + "," + 
						storedName + " is not same as passed in name=" + 
						name );
				}
				if ( name == null )
				{
					this.name = (String) underlyingNode.getProperty( 
						MAP_NAME );
				}
				else
				{
					this.name = name;
				}
			}
			else
			{
				underlyingNode.setProperty( MAP_NAME, name );
				this.name = name;
			}
			Relationship bTreeRel = underlyingNode.getSingleRelationship( 
				BTree.RelTypes.TREE_ROOT, 
				Direction.OUTGOING );
			if ( bTreeRel != null )
			{
				bTree = new BTree( graphDb, bTreeRel.getEndNode() );
			}
			else
			{
				Node bTreeNode = graphDb.createNode();
				underlyingNode.createRelationshipTo( bTreeNode, 
					BTree.RelTypes.TREE_ROOT );
				bTree = new BTree( graphDb, bTreeNode );
			}
			tx.success();
		}
		finally
		{
			tx.finish();
		}
	}
	
	/**
	 * Returns the name of this map, given at construction time.
	 * 
	 * @return the name of this map.
	 */
	public String getName()
	{
		return this.name;
	}
	
	Node getUnderlyingNode()
	{
		return underlyingNode;
	}
	
	/**
	 * If key or value is {@code null} {@link IllegalArgumentException} is 
	 * thrown. Key and value must be valid neo4j properties.
	 */
	public V put( K key, V value )
	{
		if ( key == null || value == null )
		{
			throw new IllegalArgumentException( "Null node" );
		}
		Transaction tx = graphDb.beginTx();
		try
		{
			int hashCode = key.hashCode();
			KeyEntry entry = bTree.addIfAbsent( hashCode, value );
			if ( entry != null )
			{
				entry.setKeyValue( key );
			}
			else
			{
				entry = bTree.getAsKeyEntry( hashCode );
				Object goOtherNode = entry.getKeyValue();
				Node bucketNode = null;
				if ( !goOtherNode.equals( GOTO_NODE ) )
				{
					Object prevValue = entry.getValue();
					Object prevKey = entry.getKeyValue();
					if ( prevKey.equals( key ) )
					{
						Object oldValue = entry.getValue();
						entry.setValue( value );
						tx.success();
						return (V) oldValue;
					}
					entry.setKeyValue( GOTO_NODE );
					bucketNode = graphDb.createNode();
					entry.setValue( bucketNode.getId() );
					Node prevEntry = graphDb.createNode();
					bucketNode.createRelationshipTo( prevEntry, 
						RelTypes.MAP_ENTRY );
					prevEntry.setProperty( MAP_KEY, prevKey );
					prevEntry.setProperty( MAP_VALUE, prevValue );
					Node newEntry = graphDb.createNode();
					bucketNode.createRelationshipTo( newEntry, 
						RelTypes.MAP_ENTRY );
					newEntry.setProperty( MAP_KEY, key );
					newEntry.setProperty( MAP_VALUE, value );
				}
				else
				{
					bucketNode = graphDb.getNodeById( (Long) entry.getValue() );
					for ( Relationship rel : bucketNode.getRelationships( 
						RelTypes.MAP_ENTRY, Direction.OUTGOING ) )
					{
						Node entryNode = rel.getEndNode();
						if ( entryNode.getProperty( MAP_KEY ).equals( key ) )
						{
							entryNode.setProperty( MAP_VALUE, value );
							tx.success();
							return null;
						}
					}
					Node newEntry = graphDb.createNode();
					bucketNode.createRelationshipTo( newEntry, 
						RelTypes.MAP_ENTRY );
					newEntry.setProperty( MAP_KEY, key );
					newEntry.setProperty( MAP_VALUE, value );
				}
			}
			tx.success();
			return null;
		}
		finally
		{
			tx.finish();
		}
	}
	
	public V remove( Object key )
	{
		Transaction tx = graphDb.beginTx();
		try
		{
			int hashCode = key.hashCode();
			KeyEntry entry = bTree.getAsKeyEntry( hashCode );
			if ( entry != null )
			{
				Object goOtherNode = entry.getKeyValue();
				if ( !goOtherNode.equals( GOTO_NODE ) )
				{
					if ( goOtherNode.equals( key ) )
					{
						Object value = entry.getValue();
						entry.remove();
						tx.success();
						return (V) value;
					}
				}
				else
				{
					Node bucketNode = graphDb.getNodeById( 
						(Long) entry.getValue() );
					for ( Relationship rel : bucketNode.getRelationships( 
						RelTypes.MAP_ENTRY, Direction.OUTGOING ) )
					{
						Node entryNode = rel.getEndNode();
						if ( entryNode.getProperty( MAP_KEY ).equals( key ) )
						{
							Object value = entryNode.getProperty( MAP_VALUE );
							rel.delete();
							entryNode.delete();
							tx.success();
							return (V) value;
						}
					}
				}
			}
			tx.success();
			return null;
		}
		finally
		{
			tx.finish();
		}
	}
	
	void validate()
	{
		bTree.validateTree();
	}
	
	public V get( Object key )
	{
		Transaction tx = graphDb.beginTx();
		try
		{
			int hashCode = key.hashCode();
			KeyEntry entry = bTree.getAsKeyEntry( hashCode );
			if ( entry != null )
			{
				Object goOtherNode = entry.getKeyValue();
				if ( !goOtherNode.equals( GOTO_NODE ) )
				{
					if ( goOtherNode.equals( key ) )
					{
						tx.success();
						return (V) entry.getValue();
					}
				}
				else
				{
					Node bucketNode = graphDb.getNodeById( 
						(Long) entry.getValue() );
					for ( Relationship rel : bucketNode.getRelationships( 
						RelTypes.MAP_ENTRY, Direction.OUTGOING ) )
					{
						Node entryNode = rel.getEndNode();
						if ( entryNode.getProperty( MAP_KEY ).equals( key ) )
						{
							tx.success();
							return (V) entryNode.getProperty( MAP_VALUE );
						}
					}
				}
			}
			tx.success();
			return null;
		}
		finally
		{
			tx.finish();
		}
	}
	
	public void clear()
	{
		deleteBuckets();
		bTree.delete();
		Node bTreeNode = graphDb.createNode();
		underlyingNode.createRelationshipTo( bTreeNode, 
			BTree.RelTypes.TREE_ROOT );
		bTree = new BTree( graphDb, bTreeNode );
	}
	
	/**
	 * Deletes this map and all its entries, even the underlyingNode.
	 */
	public void delete()
	{
		deleteBuckets();
		bTree.delete();
		underlyingNode.delete();
	}

    private void deleteBuckets()
    {
        for ( KeyEntry entry : bTree.entries() )
		{
			Object goOtherNode = entry.getKeyValue();
			if ( goOtherNode.equals( GOTO_NODE ) )
			{
				Node bucketNode = graphDb.getNodeById( 
					(Long) entry.getValue() );
				for ( Relationship rel : bucketNode.getRelationships( 
					RelTypes.MAP_ENTRY, Direction.OUTGOING ) )
				{
					Node entryNode = rel.getEndNode();
					rel.delete();
					entryNode.delete();
				}
				bucketNode.delete();
			}
		}
    }

	/**
     * Deletes this map and all its entries, even the underlyingNode.
     * 
	 * @param commitInterval commits the transaction 
	 */
	public void delete( int commitInterval )
	{
		deleteBuckets();
		bTree.delete( commitInterval );
		underlyingNode.delete();
	}
	
	public Collection<V> values()
    {
	    throw new UnsupportedOperationException();
    }

	public Set<K> keySet()
    {
	    throw new UnsupportedOperationException();
    }

	public boolean containsKey( Object key )
    {
		throw new UnsupportedOperationException();
    }

	public boolean containsValue( Object value )
    {
		throw new UnsupportedOperationException();
    }

	public Set<java.util.Map.Entry<K, V>> entrySet()
    {
		throw new UnsupportedOperationException();
    }

	public boolean isEmpty()
    {
	    // TODO Auto-generated method stub
		throw new UnsupportedOperationException();
    }

	public void putAll( Map<? extends K, ? extends V> t )
    {
		throw new UnsupportedOperationException();
    }

	public int size()
    {
		throw new UnsupportedOperationException();
    }
}
