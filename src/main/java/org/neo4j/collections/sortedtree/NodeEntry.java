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
package org.neo4j.collections.sortedtree;

import java.util.ArrayList;
import java.util.Iterator;

import org.neo4j.collections.NodeCollection;
import org.neo4j.collections.sortedtree.SortedTree.RelTypes;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

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

/*	
	public void remove()
	{
        treeNode.removeEntry( this.getTheNode() );
	}
*/

	@Override
	public String toString()
	{
		return "Entry[" + getNodes() + "]";
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

	Node getANode()
	{
			Iterable<Relationship> rels = getEndNode().getRelationships(NodeCollection.RelationshipTypes.VALUE, Direction.OUTGOING);
			for(Relationship rel: rels){
				return rel.getEndNode();
			}
			throw new RuntimeException("Key entry is empty");
	}

	class NodeIterator implements Iterator<Node>{

		Iterator<Relationship> rels;

		NodeIterator(Iterator<Relationship> rels){
			this.rels = rels;
		}

		@Override
		public boolean hasNext() {
			return rels.hasNext();
		}

		@Override
		public Node next() {
			return rels.next().getEndNode();
		}

		@Override
		public void remove() {
			rels.remove();
		}
	}

	class NodeIterable implements Iterable<Node>{

		@Override
		public Iterator<Node> iterator() {
			Iterable<Relationship> rels = getEndNode().getRelationships(NodeCollection.RelationshipTypes.VALUE, Direction.OUTGOING);
			return new NodeIterator(rels.iterator());
		}

	}

    Iterable<Node> getNodes()
    {
    	return new NodeIterable();


//        return getBTree().getGraphDb().getNodeById( 
//            (Long) getUnderlyingRelationship().getProperty( NODE_ID ) ); 
    }

	class RelationshipIterable implements Iterable<Relationship>{

		@Override
		public Iterator<Relationship> iterator() {
            return getEndNode().getRelationships(NodeCollection.RelationshipTypes.VALUE, Direction.OUTGOING).iterator();
		}

	}

    Iterable<Relationship> getRelationships()
    {
    	return new RelationshipIterable();
    }

    Relationship addNode( Node node )
    {
    	Relationship rel = getEndNode().createRelationshipTo(node, NodeCollection.RelationshipTypes.VALUE);
    	String treeName = treeNode.getBTree().getTreeName();
    	if(treeName != null){
    		rel.setProperty(SortedTree.TREE_NAME, treeName);
    	}
        return rel;
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
		ArrayList<TempRelationship> trls = new ArrayList<TempRelationship>();
		for(Relationship rel: getEndNode().getRelationships(NodeCollection.RelationshipTypes.VALUE, Direction.OUTGOING)){
			trls.add(new TempRelationship(rel));
			rel.delete();
		}
		entryRelationship.delete();
		entryRelationship = startNode.createRelationshipTo( endNode,
			RelTypes.KEY_ENTRY );
		for(TempRelationship trl: trls){
			Relationship rel = getEndNode().createRelationshipTo(trl.getEndNode(), NodeCollection.RelationshipTypes.VALUE);
			for(String key: trl.getProperties().keySet()){
				rel.setProperty(key, trl.getProperties().get(key));
			}
		}
    }
}