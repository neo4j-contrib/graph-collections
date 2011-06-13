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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.collections.btree.BTree;

/**
 * A sorted list of nodes (structured as a tree in neo4j).
 * 
 * This class isn't ready for general usage yet and use of it is discouraged.
 * 
 * @deprecated Builds in {@link BTree}, which also is deprecated.
 */
public class SortedTree
{
	static enum RelTypes implements RelationshipType
	{
		TREE_ROOT,
		SUB_TREE,
		// a relationship type where relationship actually is the *key entry*
		KEY_ENTRY 
	};
	
	private final GraphDatabaseService graphDb;
    private final Comparator<Node> nodeComparator;
	private TreeNode treeRoot;
	
	/**
	 * @param graphDb the {@link GraphDatabaseService} instance.
	 * @param rootNode the root of this tree.
	 * @param nodeComparator the {@link Comparator} to use to sort the nodes.
	 * It's important to use the same {@link Comparator} for a given root node
	 * to get the expected results.
	 */
	public SortedTree( GraphDatabaseService graphDb, Node rootNode, 
        Comparator<Node> nodeComparator )
	{
		this.graphDb = graphDb;
        this.nodeComparator = nodeComparator;
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
	 * Deletes this sorted tree.
	 */
	public void delete()
	{
		Relationship rel = treeRoot.getUnderlyingNode().getSingleRelationship( 
			RelTypes.TREE_ROOT, Direction.INCOMING );
		treeRoot.delete();
		rel.delete();
	}
	
	/**
	 * Deletes this sorted tree using a commit interval.
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
	 * Adds a {@link Node} to this list.
	 * @param node the {@link Node} to add.
	 * @return {@code true} if this call modified the tree, i.e. if the node
	 * wasn't already added.
	 */
	public boolean addNode( Node node )
	{
		return treeRoot.addEntry( node, true );
	}
    
    /**
     * @param node the {@link Node} to check if it's in the list.
     * @return {@code true} if this list contains the node, otherwise
     * {@code false}.
     */
    public boolean containsNode( Node node )
    {
        return treeRoot.containsEntry( node );
    }
	
	/**
	 * Removes the node from this list.
	 * @param node the {@link Node} to remove from this list.
	 * @return whether or not this call modified the list, i.e. if the node
	 * existed in this list.
	 */
	public boolean removeNode( Node node )
	{
		return treeRoot.removeEntry( node );
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
     * @return the {@link Comparator} used for this list.
     */
    public Comparator<Node> getComparator()
    {
        return nodeComparator;
    }
	
    /**
     * @return all the nodes in this list.
     */
    public Iterable<Node> getSortedNodes()
    {
        List<Node> nodeList = new ArrayList<Node>();
        traverseTreeNode( treeRoot, nodeList );
        return nodeList;
    }
    
    private void traverseTreeNode( TreeNode currentNode, List<Node> nodeList )
    {
        NodeEntry entry = currentNode.getFirstEntry();
        while ( entry != null )
        {
            TreeNode beforeTree = entry.getBeforeSubTree();
            if ( beforeTree != null )
            {
                traverseTreeNode( beforeTree, nodeList );
            }
            nodeList.add( entry.getTheNode() );
            NodeEntry nextEntry = entry.getNextKey();
            if ( nextEntry == null )
            {
                TreeNode afterTree = entry.getAfterSubTree();
                if ( afterTree != null )
                {
                    traverseTreeNode( afterTree, nodeList );
                }
            }
            entry = nextEntry;
        }
    }
}