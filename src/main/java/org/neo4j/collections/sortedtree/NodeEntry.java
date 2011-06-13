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

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.collections.btree.BTree.RelTypes;

class NodeEntry
{
	static final String NODE_ID = "node_id";
	
	private Relationship entryRelationship;
	private TreeNode treeNode;
	
	NodeEntry( TreeNode treeNode, Relationship underlyingRelationship )
	{
		assert treeNode != null;
		assert underlyingRelationship != null;
		this.treeNode = treeNode;
		this.entryRelationship = underlyingRelationship;
	}
	
	Relationship getUnderlyingRelationship()
	{
		return entryRelationship;
	}
	
	TreeNode getTreeNode()
	{
		return treeNode;
	}
	
	private SortedTree getBTree()
	{
		return treeNode.getBTree();
	}
	
	TreeNode getBeforeSubTree()
	{
		Relationship subTreeRel = getStartNode().getSingleRelationship( 
			RelTypes.SUB_TREE, Direction.OUTGOING );
		if ( subTreeRel != null )
		{
			return new TreeNode( getBTree(), 
				subTreeRel.getEndNode() );
		}
		return null;
	}
	
	TreeNode getAfterSubTree()
	{
		Relationship subTreeRel = getEndNode().getSingleRelationship( 
			RelTypes.SUB_TREE, Direction.OUTGOING );
		if ( subTreeRel != null )
		{
			return new TreeNode( getBTree(), 
				subTreeRel.getEndNode() );
		}
		return null;
	}
	
	NodeEntry getNextKey()
	{
		Relationship nextKeyRel = getEndNode().getSingleRelationship( 
			RelTypes.KEY_ENTRY, Direction.OUTGOING );
		if ( nextKeyRel != null )
		{
			return new NodeEntry( getTreeNode(), nextKeyRel );
		}
		return null;
	}
	
	NodeEntry getPreviousKey()
	{
		Relationship prevKeyRel = getStartNode().getSingleRelationship( 
			RelTypes.KEY_ENTRY, Direction.INCOMING );
		if ( prevKeyRel != null )
		{
			return new NodeEntry( getTreeNode(), prevKeyRel );
		}
		return null;
	}
	
	public void remove()
	{
        treeNode.removeEntry( this.getTheNode() );
	}
    
	@Override
	public String toString()
	{
		return "Entry[" + getTheNode() + "]";
	}
	
	boolean isLeaf()
	{
		if ( getUnderlyingRelationship().getStartNode().getSingleRelationship( 
			RelTypes.SUB_TREE, Direction.OUTGOING ) != null )
		{
			assert getUnderlyingRelationship().getEndNode().
				getSingleRelationship( RelTypes.SUB_TREE, Direction.OUTGOING )
				!= null;
			return false;
		}
		assert getUnderlyingRelationship().getEndNode().getSingleRelationship( 
			RelTypes.SUB_TREE, Direction.OUTGOING ) == null;
		return true;
	}
	
    Node getTheNode()
    {
        return getBTree().getGraphDb().getNodeById( 
            (Long) getUnderlyingRelationship().getProperty( NODE_ID ) ); 
    }
    
    void setTheNode( Node node )
    {
        getUnderlyingRelationship().setProperty( NODE_ID, node.getId() );
    }
    
    
	Node getStartNode()
	{
		return entryRelationship.getStartNode();
	}
	
	Node getEndNode()
	{
		return entryRelationship.getEndNode();
	}

	void move( TreeNode node, Node startNode, Node endNode )
    {
		assert node != null;
		this.treeNode = node;
		Node theNode = getTheNode();
		entryRelationship.delete();
		entryRelationship = startNode.createRelationshipTo( endNode, 
			RelTypes.KEY_ENTRY );
        setTheNode( theNode );
    }
}