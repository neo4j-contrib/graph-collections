/**
 * Copyright (c) 2002-2011 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.collections.sortedtree;

import javax.transaction.Transaction;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.collections.btree.BTree.RelTypes;
import org.neo4j.kernel.AbstractGraphDatabase;

class TreeNode
{
	private SortedTree bTree;
	private Node treeNode;
	
	TreeNode( SortedTree bTree, Node underlyingNode )
	{
		this.bTree = bTree;
		this.treeNode = underlyingNode;
	}
	
	Node getUnderlyingNode()
	{
		return treeNode;
	}
	
	TreeNode getParent()
	{
		Relationship toParentNode = treeNode.getSingleRelationship( 
			RelTypes.SUB_TREE, Direction.INCOMING );
		if ( toParentNode != null )
		{
			Node parentNode = toParentNode.getStartNode();
			Relationship prevEntry = parentNode.getSingleRelationship( 
				RelTypes.KEY_ENTRY, Direction.INCOMING );
			while ( prevEntry != null )
			{
				parentNode = prevEntry.getStartNode();
				prevEntry = parentNode.getSingleRelationship( 
					RelTypes.KEY_ENTRY, Direction.INCOMING );
			}
			return new TreeNode( bTree, parentNode );
		}
		return null;
	}
	
	// returns the old parent node
	private Node disconnectFromParent()
	{
		Relationship toParentNode = treeNode.getSingleRelationship( 
			RelTypes.SUB_TREE, Direction.INCOMING );
		Node parentNode = toParentNode.getStartNode();
		toParentNode.delete();
		return parentNode;
	}
	
	private void connectToParent( Node parent )
	{
		assert treeNode.getSingleRelationship( 
			RelTypes.SUB_TREE, Direction.INCOMING ) == null;
		parent.createRelationshipTo( treeNode, RelTypes.SUB_TREE );
	}
	
	void delete()
	{
		if ( !isRoot() )
		{
			disconnectFromParent();
		}
		NodeEntry entry = getFirstEntry();
		if ( entry == null )
		{
			getUnderlyingNode().delete();
			return;
		}
		TreeNode subTree = entry.getBeforeSubTree();
		if ( subTree != null )
		{
			subTree.delete();
		}
		Node lastNode = null;
		while ( entry != null )
		{
			subTree = entry.getAfterSubTree();
			if ( subTree != null )
			{
				subTree.delete();
			}
			NodeEntry nextEntry = entry.getNextKey();
			lastNode = entry.getEndNode();
			entry.getStartNode().delete();
			entry.getUnderlyingRelationship().delete();
			entry = nextEntry;
		}
		if ( lastNode != null )
		{
			lastNode.delete();
		}
	}
	
	int delete( int commitInterval, int count )
	{
		if ( !isRoot() )
		{
			disconnectFromParent();
			count++;
		}
		NodeEntry entry = getFirstEntry();
		if ( entry == null )
		{
			getUnderlyingNode().delete();
			return count++;
		}
		TreeNode subTree = entry.getBeforeSubTree();
		if ( subTree != null )
		{
			subTree.delete( commitInterval, count );
		}
		Node lastNode = null;
		while ( entry != null )
		{
			subTree = entry.getAfterSubTree();
			if ( subTree != null )
			{
				subTree.delete( commitInterval, count );
			}
			NodeEntry nextEntry = entry.getNextKey();
			lastNode = entry.getEndNode();
			entry.getStartNode().delete();
			entry.getUnderlyingRelationship().delete();
			count++;
			entry = nextEntry;
		}
		if ( lastNode != null )
		{
			lastNode.delete();
			count++;
		}
		if ( count >= commitInterval )
		{
            AbstractGraphDatabase graphDb = ( AbstractGraphDatabase ) bTree.getGraphDb();
            try
            {
                Transaction tx = graphDb.getConfig().getTxModule()
                    .getTxManager().getTransaction();
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
			count = 0;
		}
		return count;
	}
	
	NodeEntry getFirstEntry()
	{
		Relationship keyEntryRel = treeNode.getSingleRelationship( 
			RelTypes.KEY_ENTRY, Direction.OUTGOING );
		assert treeNode.getSingleRelationship( RelTypes.KEY_ENTRY, 
			Direction.INCOMING ) == null;
		if ( keyEntryRel != null )
		{
			return new NodeEntry( this, keyEntryRel );
		}
		return null;
	}
	
	NodeEntry getLastEntry()
	{
		Relationship keyEntryRel = treeNode.getSingleRelationship( 
			RelTypes.KEY_ENTRY, Direction.OUTGOING );
		NodeEntry last = null;
		while ( keyEntryRel != null )
		{
			last = new NodeEntry( this, keyEntryRel );
			keyEntryRel = keyEntryRel.getEndNode().getSingleRelationship( 
				RelTypes.KEY_ENTRY, Direction.OUTGOING );
		}
		return last;
	}
	
	private int getEntryCount()
	{
		int entryCount = 0;
		NodeEntry entry = getFirstEntry();
		while ( entry != null )
		{
			entryCount++;
			entry = entry.getNextKey();
		}
		return entryCount;
	}
	
	boolean addEntry( Node theNode, boolean ignoreIfExist )
	{
		int entryCount = 0;
		NodeEntry keyEntry = getFirstEntry();
		while ( keyEntry != null )
		{
			Node currentNode = keyEntry.getTheNode();
			if ( currentNode.equals( theNode ) )
			{
				if ( ignoreIfExist )
				{
					return false;
				}
				throw new RuntimeException( "Node already exist:" + theNode );
			}
			entryCount++;
			if ( bTree.getComparator().compare( theNode, currentNode ) < 0 )
			{
				// check if we have subtree
				TreeNode subTree = keyEntry.getBeforeSubTree();
				if ( subTree != null )
				{
					return subTree.addEntry( theNode, ignoreIfExist );
				}
				// no sub tree so we insert here
				// get current amount of entries
				NodeEntry entry = keyEntry.getNextKey();
				while ( entry != null )
				{
					entryCount++;
					entry = entry.getNextKey();
				}
				// create new blank node for key entry relationship
				Node blankNode = bTree.getGraphDb().createNode();
				createEntry( keyEntry.getStartNode(), blankNode, theNode );
				// move previous keyEntry to start at blank node
				keyEntry.move( this, blankNode, keyEntry.getEndNode() );
				entryCount++;
				assert entryCount <= bTree.getOrder();
				if ( bTree.getOrder() == entryCount )
				{
					moveMiddleUp();
				}
				return true;
			}
			// else if last entry, check for sub tree or add last
			if ( keyEntry.getNextKey() == null )
			{
				// check if we have subtree
				TreeNode subTree = keyEntry.getAfterSubTree();
				if ( subTree != null )
				{
					return subTree.addEntry( theNode, ignoreIfExist );
				}
				// ok just append the element
				Node blankNode = bTree.getGraphDb().createNode();				
				createEntry( keyEntry.getEndNode(), blankNode, theNode );
				entryCount++;
				assert entryCount <= bTree.getOrder();
				if ( bTree.getOrder() == entryCount )
				{
					moveMiddleUp();
				}
				return true;
			}
			keyEntry = keyEntry.getNextKey();
		}
		// we should never reach here unless root node is empty
		// sanity checks
		assert isRoot();
		assert !treeNode.getRelationships( 
			RelTypes.SUB_TREE ).iterator().hasNext();
		// ok add first entry in root
		Node blankNode = bTree.getGraphDb().createNode();
		createEntry( treeNode, blankNode, theNode );
        return true;
	}
	
    boolean containsEntry( Node theNode )
    {
        int entryCount = 0;
        NodeEntry keyEntry = getFirstEntry();
        while ( keyEntry != null )
        {
            Node currentNode = keyEntry.getTheNode();
            if ( currentNode.equals( theNode ) )
            {
                return true;
            }
            entryCount++;
            if ( bTree.getComparator().compare( theNode, currentNode ) < 0 )
            {
                // check if we have subtree
                TreeNode subTree = keyEntry.getBeforeSubTree();
                if ( subTree != null )
                {
                    return subTree.containsEntry( theNode );
                }
                return false;
            }
            // else if last entry, check for sub tree or add last
            if ( keyEntry.getNextKey() == null )
            {
                // check if we have subtree
                TreeNode subTree = keyEntry.getAfterSubTree();
                if ( subTree != null )
                {
                    return subTree.containsEntry( theNode );
                }
                // ok just append the element
                return false;
            }
            keyEntry = keyEntry.getNextKey();
        }
        return false;
    }
    
	private NodeEntry createEntry( Node startNode, Node endNode, Node theNode )
	{
		NodeEntry newEntry = new NodeEntry( this, startNode.createRelationshipTo( 
			endNode, RelTypes.KEY_ENTRY ) );
		newEntry.setTheNode( theNode );
		return newEntry;
	}
	
	boolean isRoot()
	{
		return treeNode.getSingleRelationship( RelTypes.TREE_ROOT, 
			Direction.INCOMING ) != null;
	}
	
	private NodeEntry insertEntry( Node theNode )
	{
		NodeEntry keyEntry = getFirstEntry();
		while ( keyEntry != null )
		{
			Node currentNode = keyEntry.getTheNode();
			assert !currentNode.equals( theNode );
			if ( bTree.getComparator().compare( theNode, currentNode ) < 0 )
			{
				// create new blank node for key entry relationship
				Node blankNode = bTree.getGraphDb().createNode();
				NodeEntry newEntry = createEntry( keyEntry.getStartNode(), 
					blankNode, theNode );
				// move previous keyEntry to start at blank node
				keyEntry.move( this, blankNode, keyEntry.getEndNode() );
				return newEntry;
			}
			// else if last entry
			if ( keyEntry.getNextKey() == null )
			{
				// just append the element
				Node blankNode = bTree.getGraphDb().createNode();
				return createEntry( keyEntry.getEndNode(), blankNode, theNode );
			}
			keyEntry = keyEntry.getNextKey();
		}
		// ok insert first entry (in new root)
		Node blankNode = bTree.getGraphDb().createNode();
		return createEntry( treeNode, blankNode, theNode );
	}
	
	private void moveMiddleUp()
	{
		TreeNode parent = getParent();
		if ( parent == null )
		{
			assert isRoot();
			// make new root
			parent = new TreeNode( bTree, bTree.getGraphDb().createNode() );
			bTree.makeRoot( parent );
		}
		else
		{
			// temporary disconnect this from parent
			disconnectFromParent();
		}
		// split the entries and move middle to parent
		NodeEntry middleEntry = getFirstEntry();
		for ( int i = 0; i < bTree.getOrder() / 2; i++ )
		{
			middleEntry = middleEntry.getNextKey();
		}
		TreeNode newTreeToTheRight = new TreeNode( bTree, 
			middleEntry.getEndNode() );
		// copy middle entry values to parent then remove it from this tree
		NodeEntry movedMiddleEntry = parent.insertEntry( 
            middleEntry.getTheNode() );
		middleEntry.getUnderlyingRelationship().delete();
		// connect left (this) and new right tree with new parent
		movedMiddleEntry.getStartNode().createRelationshipTo( 
			this.getUnderlyingNode(), RelTypes.SUB_TREE );
		movedMiddleEntry.getEndNode().createRelationshipTo( 
			newTreeToTheRight.getUnderlyingNode(), RelTypes.SUB_TREE );
		int parentEntryCount = parent.getEntryCount();
		if ( parentEntryCount == bTree.getOrder() )
		{
			parent.moveMiddleUp();
		}
        assert parent.getEntryCount() <= bTree.getOrder();
	}
	
	public boolean removeEntry( Node theNode )
	{
		NodeEntry entry = null;
		NodeEntry keyEntry = getFirstEntry();
		if ( keyEntry == null )
		{
			return false;
		}
		int entryCount = 0;
		// see if key is in this node else go down in tree
		while ( keyEntry != null )
		{
			entryCount++;
			Node currentNode = keyEntry.getTheNode();
			if ( currentNode.equals( theNode ) )
			{
				entry = keyEntry;
				// ok got the key, get total number of entries
				keyEntry = keyEntry.getNextKey();
				while ( keyEntry != null )
				{
					entryCount++;
					keyEntry = keyEntry.getNextKey();
				}
				break; // need to break since we don't have if else bellow
			}
			if ( bTree.getComparator().compare( theNode, currentNode ) < 0  )
			{
				// check if we have subtree
				TreeNode subTree = keyEntry.getBeforeSubTree();
				if ( subTree != null )
				{
					// go down in tree
					return subTree.removeEntry( theNode );
				}
				return false;
			}
			// else if last entry, check for sub tree or add last
			if ( keyEntry.getNextKey() == null )
			{
				// check if we have subtree
				TreeNode subTree = keyEntry.getAfterSubTree();
				if ( subTree != null )
				{
					// go down in tree
					return subTree.removeEntry( theNode );
				}
				return false;
			}
			keyEntry = keyEntry.getNextKey();
		}
		assert entry != null;
		// remove the found key
		if ( entry.isLeaf() )
		{
			// we can just remove it
			NodeEntry nextEntry = entry.getNextKey();
			if ( nextEntry != null )
			{
				nextEntry.move( this, entry.getStartNode(), 
					nextEntry.getEndNode() );
			}
//			Node value = entry.getTheNode();
			entry.getEndNode().delete();
			entry.getUnderlyingRelationship().delete();
			entryCount--;
			if ( entryCount < ( bTree.getOrder() / 2 ) && !isRoot() )
			{
				tryBorrowFromSibling();
			}
		}
		else
		{
			// while not leaf find first successor and move it to replace the 
			// current entry
			NodeEntry successor = entry.getAfterSubTree().getFirstEntry();
			while ( !successor.isLeaf() )
			{
				successor = successor.getBeforeSubTree().getFirstEntry();
			}
			TreeNode leafTree = successor.getTreeNode();
			NodeEntry next = successor.getNextKey();
			next.move( leafTree, successor.getStartNode(), next.getEndNode() );
			successor.move( this, entry.getStartNode(), entry.getEndNode() );
//			Node value = entry.getTheNode();
			entry.getUnderlyingRelationship().delete();
			// verify subTree entryCount
			entryCount = leafTree.getEntryCount();
			if ( entryCount < ( bTree.getOrder() / 2 ) && !leafTree.isRoot() )
			{
				leafTree.tryBorrowFromSibling();
			}
		}
        return true;
	}
	
	private void tryBorrowFromSibling()
	{
		TreeNode leftSibling = getLeftSibbling();
		TreeNode rightSibling = getRightSibbling();
		if ( leftSibling != null && ( leftSibling.getEntryCount() > 
			( bTree.getOrder() / 2 ) ) )
		{
			borrowFromLeftSibling( leftSibling );
		}
		else if ( rightSibling != null && ( rightSibling.getEntryCount() > 
			( bTree.getOrder() / 2 ) ) )
		{
			borrowFromRightSibling( rightSibling );
		}
		else if ( leftSibling != null )
		{
			mergeWithLeftSibling( leftSibling );
		}
		else if ( rightSibling != null )
		{
			mergeWithRightSibling( rightSibling );
		}
		else
		{
			throw new RuntimeException();
		}
	}
	
	private void borrowFromLeftSibling( TreeNode leftSibling )
	{
		// get last entry from sibling and set it as new parent, move parent 
		// down to fill upp for deleted entry
		// get after subtree from last entry in sibling and add it as
		// before subtree in in moved down parent
		TreeNode parentNode = getParent();
		NodeEntry entryToMoveDown = new NodeEntry( parentNode, 
			this.treeNode.getSingleRelationship( RelTypes.SUB_TREE, 
				Direction.INCOMING ).getStartNode().getSingleRelationship( 
					RelTypes.KEY_ENTRY, Direction.INCOMING ) );
		NodeEntry entryToMoveUp = leftSibling.getLastEntry();
		TreeNode subTree = entryToMoveUp.getAfterSubTree();
		if ( subTree != null )
		{
			subTree.disconnectFromParent();
		}
		entryToMoveUp.getEndNode().delete();
		entryToMoveUp.move( parentNode, entryToMoveDown.getStartNode(), 
			entryToMoveDown.getEndNode() );
		Node newStartNode = bTree.getGraphDb().createNode();
		entryToMoveDown.move( this, newStartNode, treeNode );
		Node parentToReAttachTo = disconnectFromParent();
		treeNode = newStartNode;
		connectToParent( parentToReAttachTo );
		if ( subTree != null )
		{
			subTree.connectToParent( newStartNode );
		}
	}
	
	private void borrowFromRightSibling( TreeNode rightSibling )
	{
		// get first entry from sibling and set it as new parent, move 
		// parent down to fill upp for deleted entry
		// get before subtree from first entry in sibling and add it as
		// after subtree in in moved down parent
		TreeNode parentNode = getParent();
		NodeEntry entryToMoveDown = new NodeEntry( parentNode, 
			this.treeNode.getSingleRelationship( RelTypes.SUB_TREE, 
				Direction.INCOMING ).getStartNode().getSingleRelationship( 
					RelTypes.KEY_ENTRY, Direction.OUTGOING ) );
		NodeEntry entryToMoveUp = rightSibling.getFirstEntry();
		TreeNode subTree = entryToMoveUp.getBeforeSubTree();
		if ( subTree != null )
		{
			subTree.disconnectFromParent();
		}
		Node rightParentToReAttachTo = rightSibling.disconnectFromParent();
		rightSibling.treeNode = entryToMoveUp.getEndNode();
		rightSibling.connectToParent( rightParentToReAttachTo );
		entryToMoveUp.getStartNode().delete();
		entryToMoveUp.move( parentNode, entryToMoveDown.getStartNode(), 
			entryToMoveDown.getEndNode() );
		Node newLastNode = bTree.getGraphDb().createNode();
		entryToMoveDown.move( this, this.getLastEntry().getEndNode(), 
			newLastNode );
		if ( subTree != null )
		{
			subTree.connectToParent( newLastNode );
		}
	}
	
	private void mergeWithLeftSibling( TreeNode leftSibling )
	{
		// disconnect this from parent,
		// move entry after entry to move down in parent if exist
		// use parent to move down values to connect left subtree with this
		// check entry count in parent
		TreeNode parentNode = getParent();
		NodeEntry entryToMoveDown = new NodeEntry( parentNode, 
			this.treeNode.getSingleRelationship( RelTypes.SUB_TREE, 
				Direction.INCOMING ).getStartNode().getSingleRelationship( 
					RelTypes.KEY_ENTRY, Direction.INCOMING ) );
		NodeEntry nextParentEntry = entryToMoveDown.getNextKey();
		if ( nextParentEntry != null )
		{
			nextParentEntry.move( parentNode, entryToMoveDown.getStartNode(), 
				nextParentEntry.getEndNode() );
		}
		NodeEntry entry = this.getFirstEntry();
		TreeNode subTree = entry.getBeforeSubTree();
		if ( subTree != null )
		{
			subTree.disconnectFromParent();
		}
		this.disconnectFromParent();
		entryToMoveDown.getEndNode().delete();
		Node blankNode = bTree.getGraphDb().createNode();
		entryToMoveDown.move( leftSibling, 
			leftSibling.getLastEntry().getEndNode(), blankNode );
		entry.getStartNode().delete();
		entry.move( leftSibling, blankNode, entry.getEndNode() );
		this.treeNode = leftSibling.treeNode;
		if ( subTree != null )
		{
			subTree.connectToParent( blankNode );
		}
		// validate parent
		int entryCount = parentNode.getEntryCount();
		if ( entryCount < bTree.getOrder() / 2 && !parentNode.isRoot() )
		{
			assert entryCount > 0;
			parentNode.tryBorrowFromSibling();
		}
		else if ( entryCount == 0 )
		{
			assert parentNode.isRoot();
			this.disconnectFromParent();
			bTree.makeRoot( this );
		}
	}
	
	private void mergeWithRightSibling( TreeNode rightSibling )
	{
		// disconnect right sibling from parent,
		// move entry after entry to move down in parent if exist
		// use parent to move down values to connect this with right subtree
		// check entry count in parent
		TreeNode parentNode = getParent();
		NodeEntry entryToMoveDown = new NodeEntry( parentNode, 
			this.treeNode.getSingleRelationship( RelTypes.SUB_TREE, 
				Direction.INCOMING ).getStartNode().getSingleRelationship( 
					RelTypes.KEY_ENTRY, Direction.OUTGOING ) );
		NodeEntry nextInParent = entryToMoveDown.getNextKey();
		if ( nextInParent != null )
		{
			nextInParent.move( parentNode, entryToMoveDown.getStartNode(), 
				nextInParent.getEndNode() );
		}
		NodeEntry entry = rightSibling.getFirstEntry();
		TreeNode subTree = entry.getBeforeSubTree();
		if ( subTree != null )
		{
			subTree.disconnectFromParent();
		}
		rightSibling.disconnectFromParent();
		entryToMoveDown.getEndNode().delete();
		Node blankNode = bTree.getGraphDb().createNode();
		entryToMoveDown.move( this, this.getLastEntry().getEndNode(), 
			blankNode );
		entry.getStartNode().delete();
		entry.move( this, blankNode, entry.getEndNode() );
		rightSibling.treeNode = this.treeNode;
		if ( subTree != null )
		{
			subTree.connectToParent( blankNode );
		}
		// validate parent
		int entryCount = parentNode.getEntryCount();
		if ( entryCount < bTree.getOrder() / 2 && !parentNode.isRoot() )
		{
			assert entryCount > 0;
			parentNode.tryBorrowFromSibling();
		}
		else if ( entryCount == 0 )
		{
			assert parentNode.isRoot();
			this.disconnectFromParent();
			bTree.makeRoot( this );
		}
	}
	
	TreeNode getLeftSibbling()
	{
		Relationship parent = treeNode.getSingleRelationship( RelTypes.SUB_TREE, 
			Direction.INCOMING );
		if ( parent == null )
		{
			return null;
		}
		Relationship prevEntry = parent.getStartNode().getSingleRelationship( 
			RelTypes.KEY_ENTRY, Direction.INCOMING );
		if ( prevEntry == null )
		{
			return null;
		}
		return new TreeNode( getBTree(), 
			prevEntry.getStartNode().getSingleRelationship( 
				RelTypes.SUB_TREE, Direction.OUTGOING ).getEndNode() );
	}
	
	TreeNode getRightSibbling()
	{
		Relationship parent = treeNode.getSingleRelationship( RelTypes.SUB_TREE, 
			Direction.INCOMING );
		if ( parent == null )
		{
			return null;
		}
		Relationship nextEntry = parent.getStartNode().getSingleRelationship( 
			RelTypes.KEY_ENTRY, Direction.OUTGOING );
		if ( nextEntry == null )
		{
			return null;
		}
		return new TreeNode( getBTree(), 
			nextEntry.getEndNode().getSingleRelationship( 
				RelTypes.SUB_TREE, Direction.OUTGOING ).getEndNode() );
	}
	
	SortedTree getBTree()
	{
		return bTree;
	}
}
