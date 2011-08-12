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
import org.neo4j.graphdb.RelationshipType;
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
abstract public class AbstractBTree
{
    /**
     * All {@link RelationshipType}s used internally in this b-tree
     * implementation.
     */
	public static enum RelTypes implements RelationshipType
	{
	    /**
	     * A relationship which goes from the supplied root node to a node
	     * which represents the current root. The root can change when the tree
	     * is balanced.
	     */
		TREE_ROOT,
		
		/**
		 * Relationships between a parent and its children is of this type.
		 */
		SUB_TREE,
		
		/**
		 * A relationship type where the relationship actually is the
		 * *key entry*. i.e. the relationship holds information about the entry.
		 */
		KEY_ENTRY 
	};
	
	private GraphDatabaseService graphDb;
	private TreeNode treeRoot;
	
	protected TreeNode getTreeRoot(){
		return treeRoot;
	}
	
	/**
	 * Creates a b-tree using {@code rootNode} as root. The root node must have
	 * an incoming relationship of {@link RelTypes TREE_ROOT} else a runtime
	 * exception will be thrown.
	 * 
	 * @param graphDb the embedded graph database instance
	 * @param rootNode root node with incoming {@code TREE_ROOT} relationship.
	 */
	public AbstractBTree( GraphDatabaseService graphDb, Node rootNode )
	{
		this.graphDb = graphDb;
		this.treeRoot = new TreeNode( this, rootNode );
	}
	
	void makeRoot( TreeNode newRoot )
	{
		Relationship rel = treeRoot.getUnderlyingNode().getSingleRelationship( 
			RelTypes.TREE_ROOT, Direction.INCOMING );
		Node startNode = rel.getStartNode();
		rel.delete();
		startNode.createRelationshipTo( newRoot.getUnderlyingNode(), 
			RelTypes.TREE_ROOT );
		treeRoot = newRoot;
	}
	
	/**
	 * Deletes this b-tree.
	 */
	public void delete()
	{
		Relationship rel = treeRoot.getUnderlyingNode().getSingleRelationship( 
			RelTypes.TREE_ROOT, Direction.INCOMING );
		treeRoot.delete();
		rel.delete();
	}
	
	/**
	 * Deletes this b-tree using a commit interval.
	 * 
	 * @param commitInterval number of entries to remove before a new 
	 * transaction is started
	 */
	public void delete( int commitInterval )
	{
		Relationship rel = treeRoot.getUnderlyingNode().getSingleRelationship( 
			RelTypes.TREE_ROOT, Direction.INCOMING );
		treeRoot.delete( commitInterval, 0);
		rel.delete();
	}
	
	/**
	 * Public for testing purpose. Validates this b-tree making sure it is 
	 * balanced and consistent.
	 */
	public void validateTree()
	{
		long currentValue = Long.MIN_VALUE;
		KeyEntry entry = null;
		KeyEntry keyEntry = treeRoot.getFirstEntry();
		boolean hasSubTree = false;
		int entryCount = 0;
		while ( keyEntry != null )
		{
			entry = keyEntry;
			entryCount++;
			if ( entry.getKey() <= currentValue )
			{
				throw new RuntimeException( "Key entry ordering inconsistency");
			}
			currentValue = entry.getKey();
			TreeNode subTree = entry.getBeforeSubTree();
			if ( subTree != null )
			{
				hasSubTree = true;
				validateAllLessThan( subTree, currentValue );
			}
			else if ( hasSubTree )
			{
				throw new RuntimeException( "Leaf/no leaf inconsistency");
			}
			keyEntry = keyEntry.getNextKey();
		}
		// root so we don't validate to few entries
		if ( entryCount >= getOrder() )
		{
			throw new RuntimeException( "To many entries" );
		}
		if ( hasSubTree )
		{
			TreeNode subTree = entry.getAfterSubTree();
			if ( subTree == null )
			{
				throw new RuntimeException( "Leaf/no leaf inconsistency" );
			}
			validateAllGreaterThan( subTree, currentValue );
		}
	}
	
	private void validateAllLessThan( TreeNode treeNode, long value )
	{
		long currentValue = Long.MIN_VALUE;
		KeyEntry entry = null;
		KeyEntry keyEntry = treeNode.getFirstEntry();
		boolean hasSubTree = false;
		int entryCount = 0;
		while ( keyEntry != null )
		{
			entryCount++;
			entry = keyEntry;
			if ( entry.getKey() >= value )
			{
				throw new RuntimeException( "Depth key inconsistency" );
			}
			if ( entry.getKey() <= currentValue )
			{
				throw new RuntimeException( "Key entry ordering inconsistency");
			}
			currentValue = entry.getKey();
			TreeNode subTree = entry.getBeforeSubTree();
			if ( subTree != null )
			{
				hasSubTree = true;
				validateAllLessThan( subTree, currentValue );
			}
			else if ( hasSubTree )
			{
				throw new RuntimeException( "Leaf/no leaf inconsistency");
			}
			keyEntry = keyEntry.getNextKey();
		}
		if ( entryCount < getOrder() / 2 - 1 )
		{
			throw new RuntimeException( "To few entries" );
		}
		if ( entryCount >= getOrder() )
		{
			throw new RuntimeException( "To many entries" );
		}
		if ( hasSubTree )
		{
			TreeNode subTree = entry.getAfterSubTree();
			if ( subTree == null )
			{
				throw new RuntimeException( "Leaf/no leaf inconsistency" );
			}
			validateAllGreaterThan( subTree, currentValue );
		}
	}

	private void validateAllGreaterThan( TreeNode treeNode, long value )
	{
		long currentValue = Long.MIN_VALUE;
		KeyEntry entry = null;
		KeyEntry keyEntry = treeNode.getFirstEntry();
		boolean hasSubTree = false;
		int entryCount = 0;
		while ( keyEntry != null )
		{
			entryCount++;
			entry = keyEntry;
			if ( entry.getKey() <= value )
			{
				throw new RuntimeException( "Depth key inconsistency" );
			}
			if ( entry.getKey() <= currentValue )
			{
				throw new RuntimeException( "Key entry ordering inconsistency");
			}
			currentValue = entry.getKey();
			TreeNode subTree = entry.getBeforeSubTree();
			if ( subTree != null )
			{
				hasSubTree = true;
				validateAllLessThan( subTree, currentValue );
			}
			else if ( hasSubTree )
			{
				throw new RuntimeException( "Leaf/no leaf inconsistency");
			}
			keyEntry = keyEntry.getNextKey();
		}
		if ( entryCount < getOrder() / 2 - 1 )
		{
			throw new RuntimeException( "To few entries" );
		}
		if ( entryCount >= getOrder() )
		{
			throw new RuntimeException( "To many entries" );
		}
		if ( hasSubTree )
		{
			TreeNode subTree = entry.getAfterSubTree();
			if ( subTree == null )
			{
				throw new RuntimeException( "Leaf/no leaf inconsistency" );
			}
			validateAllGreaterThan( subTree, currentValue );
		}
	}
	
	/**
	 * Returns the {@code KeyEntry}} for a key or null if it doesn't exist.
	 * 
	 * @param key the key
	 * @return the entry connected to the key
	 */
	public KeyEntry getAsKeyEntry( long key )
	{
		return treeRoot.getEntry( key );
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
		return treeRoot.removeEntry( key );
	}
	
	int getOrder()
	{
		return 9;
	}
	
	GraphDatabaseService getGraphDb()
	{
		return graphDb;
	}
	
	/**
	 * Returns all the entries in this b-tree. The iterable returned back is
	 * a wrapped {@link Traverser}.
	 * 
	 * @return an Iterable of all the entries in this b-tree
	 */
	public Iterable<KeyEntry> entries()
	{
		EntryReturnableEvaluator entryEvaluator = 
			new EntryReturnableEvaluator();
		
		Traverser trav = treeRoot.getUnderlyingNode().traverse( 
			Order.DEPTH_FIRST, StopEvaluator.END_OF_GRAPH, 
			entryEvaluator, RelTypes.KEY_ENTRY, Direction.OUTGOING, 
			RelTypes.SUB_TREE, Direction.OUTGOING );
		return new EntryTraverser( trav, this, entryEvaluator );
	}
	
	private static class EntryTraverser implements Iterable<KeyEntry>, 
		Iterator<KeyEntry>
	{
		private EntryReturnableEvaluator entryEvaluator;
		private AbstractBTree bTree;
		private Iterator<Node> itr;
		
		EntryTraverser( Traverser trav, AbstractBTree tree, 
			EntryReturnableEvaluator entry )
		{
			this.itr = trav.iterator();
			this.bTree = tree;
			this.entryEvaluator = entry;
		}
	
		public boolean hasNext()
	    {
			return itr.hasNext();
	    }
	
		public KeyEntry next()
	    {
			Node node = itr.next();
			TreeNode treeNode = new TreeNode( bTree, 
				entryEvaluator.getCurrentTreeNode() );
	        return new KeyEntry( treeNode, node.getSingleRelationship( 
	        	RelTypes.KEY_ENTRY, Direction.INCOMING ) );
	    }
	
		public void remove()
	    {
			throw new UnsupportedOperationException();
	    }
	
		public Iterator<KeyEntry> iterator()
	    {
			return this;
	    }
	}
	
	private static class EntryReturnableEvaluator implements ReturnableEvaluator
	{
		private Node currentTreeNode = null;
		
		public Node getCurrentTreeNode()
		{
			return currentTreeNode;
		}
		
		public boolean isReturnableNode( TraversalPosition pos )
        {
			if ( !pos.notStartNode() )
			{
				currentTreeNode = pos.currentNode();
				return false;
			}
			Relationship last = pos.lastRelationshipTraversed();
			if ( last.isType( RelTypes.KEY_ENTRY ) )
			{
				return true;
			}
			if ( last.isType( RelTypes.SUB_TREE ) )
			{
				currentTreeNode = pos.currentNode();
			}
			return false;
        }
	}
}
