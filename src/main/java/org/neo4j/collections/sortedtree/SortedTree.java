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

import java.lang.UnsupportedOperationException;

import java.util.Comparator;
import java.util.Iterator;
import java.util.HashMap;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.collections.propertytype.PropertyComparator;

/**
 * A sorted list of nodes (structured as a Btree in neo4j).
 */
public class SortedTree implements Iterable<Node>
{
	
	public static final String TREE_NAME = "tree_name";
	public static final String IS_UNIQUE_INDEX = "is_unique_index";
	public static final String COMPARATOR_CLASS = "comparator_class";
	
	public static enum RelTypes implements RelationshipType
	{
		TREE_ROOT,
		SUB_TREE,
		// a relationship type where relationship actually is the *key entry*
		KEY_ENTRY,
		KEY_VALUE
	};
	
	private final GraphDatabaseService graphDb;
    private final Comparator<Node> nodeComparator;
    private final String treeName;
	private final boolean isUniqueIndex;
	private TreeNode treeRoot;
 
	
	/**
	 * @param graphDb the {@link GraphDatabaseService} instance.
	 * @param rootNode the root of this tree.
	 * @param nodeComparator the {@link Comparator} to use to sort the nodes. 
	 * @param isUniqueIndex determines if every entry in the tree needs to have a unique comparator value  
	 * @param treeName value set on both the TREE_ROOT and the KEY_VALUE relations.  
	 */
	public SortedTree( GraphDatabaseService graphDb, Node rootNode, 
        Comparator<Node> nodeComparator, boolean isUniqueIndex, String treeName )
	{
		
        if ( rootNode == null || graphDb == null )
        {
            throw new IllegalArgumentException(
                    "Null parameter rootNode=" + rootNode
                            + " graphDb=" + graphDb );
        }
		this.graphDb = graphDb;
		this.treeRoot = new TreeNode( this, rootNode );
		
        Transaction tx = graphDb.beginTx();
        try
        {
            assertPropertyIsSame( TREE_NAME, treeName );
            this.treeName = treeName;
            assertPropertyIsSame( IS_UNIQUE_INDEX, isUniqueIndex );
    		this.isUniqueIndex = isUniqueIndex;
            assertPropertyIsSame( COMPARATOR_CLASS, nodeComparator.getClass().getName());
            this.nodeComparator = nodeComparator;
            tx.success();
        }
        finally
        {
            tx.finish();
        }
	}

    private void assertPropertyIsSame( String key, Object value )
    {
        Object storedValue = treeRoot.getUnderlyingNode().getSingleRelationship(RelTypes.TREE_ROOT, Direction.INCOMING).getProperty( key, null );
        if ( storedValue != null )
        {
            if ( !storedValue.equals( value ) )
            {
                throw new IllegalArgumentException( "SortedTree("
                                                    + treeRoot.getUnderlyingNode().getSingleRelationship(RelTypes.TREE_ROOT, Direction.INCOMING)
                                                    + ") property '" + key
                                                    + "' is " + storedValue
                                                    + ", passed in " + value );
            }
        }
        else
        {
        	treeRoot.getUnderlyingNode().getSingleRelationship(RelTypes.TREE_ROOT, Direction.INCOMING).setProperty( key, value );
        }
    }

    private void acquireLock(){
    	Relationship rel = treeRoot.getUnderlyingNode().getSingleRelationship(RelTypes.TREE_ROOT, Direction.INCOMING);
    	rel.getStartNode().removeProperty("___dummy_property_to_acquire_lock_____");
    }
    
	void makeRoot( TreeNode newRoot )
	{
		Relationship rel = treeRoot.getUnderlyingNode().getSingleRelationship( 
			RelTypes.TREE_ROOT, Direction.INCOMING );
		Node startNode = rel.getStartNode();
		HashMap<String, Object> props = new HashMap<String, Object>();
		for(String key: rel.getPropertyKeys()){
			props.put(key, rel.getProperty(key));
		}
		rel.delete();
		Relationship newRel = startNode.createRelationshipTo( newRoot.getUnderlyingNode(), 
			RelTypes.TREE_ROOT );
		for(String key: props.keySet()){
			newRel.setProperty(key, props.get(key));
		}
		treeRoot = newRoot;
	}
	
	/**
	 * Deletes this sorted tree.
	 */
	public void delete()
	{
		acquireLock();
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
		acquireLock();
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
		acquireLock();
		return treeRoot.addEntry( node, true );
	}
    
    /**
     * @param node the {@link Node} to check if it's in the tree.
     * @return {@code true} if this list contains the node, otherwise
     * {@code false}.
     */
    public boolean containsNode( Node node )
    {
        return treeRoot.containsEntry( node );
    }


	protected <T> boolean containsValue(T val, PropertyComparator<T> comp) {
		return treeRoot.containsValue( val, comp );
	}

    protected <T> Iterable<Node> getWithValue(T val, PropertyComparator<T> comp){
    	return treeRoot.getWithValue(val, comp);
    }

	
	/**
	 * Removes the node from this list.
	 * @param node the {@link Node} to remove from this list.
	 * @return whether or not this call modified the list, i.e. if the node
	 * existed in this list.
	 */
	public boolean removeNode( Node node )
	{
		acquireLock();
		return treeRoot.removeEntry( node );
	}
	
	int getOrder()
	{
		return 25;
	}
	
	GraphDatabaseService getGraphDb()
	{
		return graphDb;
	}

	String getTreeName(){
		return treeName;
	}

	TreeNode getTreeRoot(){
		return treeRoot;
	}

	
    /**
     * @return the {@link Comparator} used for this list.
     */
    public Comparator<Node> getComparator()
    {
        return nodeComparator;
    }

    /**
     * @return the {@link Comparator} used for this list.
     */
    public boolean isUniqueIndex()
    {
        return isUniqueIndex;
    }
    
    class NodeIterator implements Iterator<Node>{

    	private TreeNode currentNode;

        NodeEntry entry = null; 
        Iterator<Node> ni = null;
        NodeIterator bi = null;
        NodeIterator ai = null;
        int step = 0;
    	
    	NodeIterator(TreeNode currentNode){
    		initTreeNode(currentNode);
    	}

    	private void initTreeNode(TreeNode currentNode){
    		this.currentNode = currentNode;
    		NodeEntry entry = (this.currentNode == null) ?  null : currentNode.getFirstEntry();
    		if(entry != null){
    			initEntry(entry);
    		}
    	}
    	
    	private void initEntry(NodeEntry nodeEntry){
    		this.entry = nodeEntry;
			TreeNode beforeTree = entry.getBeforeSubTree();
			if ( beforeTree != null )
			{
				bi = new NodeIterator(beforeTree);
			}
			ni = entry.getNodes().iterator();
    	}
    	
    	private void initAfterEntry(NodeEntry entry){
            TreeNode afterTree = entry.getAfterSubTree();
            if ( afterTree != null )
            {
                ai = new NodeIterator( afterTree );
            }
    	}
    	
		@Override
		public boolean hasNext() {
			if(entry != null){
				if(bi != null && bi.hasNext()){
					return true;
				}else if(ni.hasNext()){
					return true;
				}else{
					return false;
				}
			}else{
				if(ai != null && ai.hasNext()){
					return true;
				}else return false;
			}
		}

		@Override
		public Node next() {
			if(bi != null && bi.hasNext()){
				return bi.next();
			}else if(ni.hasNext()){
				Node n = ni.next();
				if(!ni.hasNext()){
					initAfterEntry(entry);
					entry = entry.getNextKey();
					if(entry != null){
						initEntry(entry);
					}
				}
				return n;
			}else if(ai.hasNext()){
				return ai.next();
			}
			return null;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
    }
    
    public Iterator<Node> iterator(){
    	return new NodeIterator(treeRoot);
    }
    
}