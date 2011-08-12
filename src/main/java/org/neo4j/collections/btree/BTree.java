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

package org.neo4j.collections.btree;

import java.util.Iterator;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ReturnableEvaluator;
import org.neo4j.graphdb.StopEvaluator;
import org.neo4j.graphdb.TraversalPosition;
import org.neo4j.graphdb.Traverser;
import org.neo4j.graphdb.Traverser.Order;

/**
 * A b-tree implementation on top of neo4j (using nodes/relationships 
 * and properties).
 * <p>
 * This implementation is not thread safe (yet).
 * 
 * This class isn't ready for general usage yet and use of it is discouraged.
 * 
 * The implementation isn't thread safe.
 */
public class BTree extends AbstractBTree
{
	
	/**
	 * Creates a b-tree using {@code rootNode} as root. The root node must have
	 * an incoming relationship of {@link RelTypes TREE_ROOT} else a runtime
	 * exception will be thrown.
	 * 
	 * @param graphDb the embedded graph database instance
	 * @param rootNode root node with incoming {@code TREE_ROOT} relationship.
	 */
	public BTree( GraphDatabaseService graphDb, Node rootNode )
	{
		super(graphDb, rootNode);
//		this.graphDb = graphDb;
//		this.treeRoot = new TreeNode( this, rootNode );
	}
	
	
	/**
	 * Adds a entry to this b-tree. If key already exist a runtime exception
	 * is thrown. The {@code value} has to be a valid neo4j property.
	 * 
	 * @param key the key of the entry
	 * @param value value of the entry
	 * @return the added entry
	 */
	public KeyEntry addEntry( long key, Object value )
	{
		return getTreeRoot().addEntry( key, value );
	}
	
	/**
	 * Adds the entry to this b-tree. If key already exist nothing is modified 
	 * and {@code null} is returned. The {@code value} has to be a valid
	 * neo4j property.
	 * 
	 * @param key the key of the entry
	 * @param value value of the entry
	 * @return the added entry or {@code null} if key already existed
	 */
	public KeyEntry addIfAbsent( long key, Object value )
	{
		return getTreeRoot().addEntry( key, value, true );
	}
	
	/**
	 * Returns the value of an entry or {@code null} if no such entry exist.
	 * 
	 * @param key for the entry
	 * @return value of the entry
	 */
	public Object getEntry( long key )
	{
		KeyEntry entry = getTreeRoot().getEntry( key );
		if ( entry != null )
		{
			return entry.getValue();
		}
		return null;
	}
	
	/**
	 * Returns the closest entry value where {@code Entry.key &lt= key<} or
	 * {@code null} if no such entry exist. 
	 * 
	 * @param key the key
	 * @return the value of the closest lower entry
	 */
	public Object getClosestLowerEntry( long key )
	{
		KeyEntry entry = getTreeRoot().getClosestLowerEntry( null, key );
		if ( entry != null )
		{
			return entry.getValue();
		}
		return null;
	}
	
	/**
	 * Returns the closest entry value where {@code Entry.key &gt= key} or
	 * {@code null} if no such entry exist.
	 * 
	 * @param key the key
	 * @return the value of the closest lower entry
	 */
	public Object getClosestHigherEntry( long key )
	{
		KeyEntry entry = getTreeRoot().getClosestHigherEntry( null, key );
		if ( entry != null )
		{
			return entry.getValue();
		}
		return null;
	}
	
	/**
	 * Removes a entry and returns the value of the entry. If entry doesn't 
	 * exist {@code null} is returned.
	 * 
	 * @param key the key
	 * @return value of removed entry
	 */
	public Object removeEntry( long key )
	{
		return getTreeRoot().removeEntry( key );
	}
	
	int getOrder()
	{
		return 9;
	}
	
	
	/**
	 * Returns the values of all entries in this b-tree. The iterable which is
	 * returned back is wrapped {@link Traverser}.
	 * 
	 * @return the values of all entries values in this b-tree.
	 */
	public Iterable<Object> values()
	{
		Traverser trav = getTreeRoot().getUnderlyingNode().traverse( 
			Order.DEPTH_FIRST, StopEvaluator.END_OF_GRAPH, 
			new ReturnableEvaluator()
			{
				public boolean isReturnableNode( TraversalPosition pos )
				{
					Relationship last = pos.lastRelationshipTraversed();
					if ( last != null && last.getType().equals( 
						RelTypes.KEY_ENTRY ) )
					{
						return true;
					}
					return false;
				}
			}, RelTypes.KEY_ENTRY, Direction.OUTGOING, 
			RelTypes.SUB_TREE, Direction.OUTGOING );
		return new ValueTraverser( trav );
	}
	
	private static class ValueTraverser implements Iterable<Object>, 
		Iterator<Object>
	{
		private Iterator<Node> itr;
		
		ValueTraverser( Traverser trav )
		{
			this.itr = trav.iterator();
		}

		public boolean hasNext()
        {
			return itr.hasNext();
        }

		public Object next()
        {
			Node node = itr.next();
	        return node.getSingleRelationship( RelTypes.KEY_ENTRY, 
	        	Direction.INCOMING ).getProperty( KeyEntry.VALUE );
        }

		public void remove()
        {
			throw new UnsupportedOperationException();
        }

		public Iterator<Object> iterator()
        {
			return this;
        }
	}
}
