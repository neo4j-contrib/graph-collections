/**
 * Copyright (c) 2002-2012 "Neo Technology,"
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

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;

import org.neo4j.collections.NodeCollection;
import org.neo4j.collections.graphdb.PropertyComparator;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;

/**
 * A sorted list of nodes (structured as a Btree in neo4j).
 */
public class SortedTree implements NodeCollection//Iterable<Relationship>
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
	}

    private final Node baseNode;
    private final Comparator<Node> nodeComparator;
    private final String treeName;
	private final boolean isUniqueIndex;
	private TreeNode treeRoot;

    /**
     * Instantiate a previously stored SortedTree from the base node.
     *
     * @param baseNode the base node of the sorted tree.
     */
    public SortedTree( Node baseNode )
    {
        this.baseNode = baseNode;

        try
        {
            Relationship rel = baseNode.getSingleRelationship( RelTypes.TREE_ROOT, Direction.OUTGOING );
            String comparatorClass = (String) rel.getProperty( COMPARATOR_CLASS );
            this.nodeComparator = (Comparator<Node>) Class.forName( comparatorClass ).newInstance();
            this.treeName = (String) rel.getProperty( TREE_NAME );
            this.isUniqueIndex = (Boolean) rel.getProperty( IS_UNIQUE_INDEX );
            this.treeRoot = new TreeNode( this, rel.getEndNode() );
        }
        catch ( Exception e )
        {
            throw new IllegalStateException( "Unable to re-instantiate SortedTree from graph data structure.", e );
        }
    }

    /**
     * Create a new sorted tree within the graph database.
     * 
	 * @param graphDb the {@link org.neo4j.graphdb.GraphDatabaseService} instance.
     * @param nodeComparator the {@link java.util.Comparator} to use to sort the nodes.
     * @param isUniqueIndex determines if every entry in the tree needs to have a unique comparator value
     * @param treeName value set on both the TREE_ROOT and the KEY_VALUE relations.
     */
	public SortedTree( GraphDatabaseService graphDb, Comparator<Node> nodeComparator, boolean isUniqueIndex,
                       String treeName )
	{

        if (graphDb == null )
        {
            throw new IllegalArgumentException( "Graph Database must be provided when creating new SortedTree" );
        }
        this.baseNode = graphDb.createNode();

        Transaction tx = graphDb.beginTx();
        try
        {
            Node rootNode = graphDb.createNode();
            Relationship treeRootRelationship = baseNode.createRelationshipTo( rootNode, RelTypes.TREE_ROOT );
            this.treeRoot = new TreeNode( this, rootNode );
            baseNode.setProperty( NodeCollection.GRAPH_COLLECTION_CLASS, SortedTree.class.getName() );

            treeRootRelationship.setProperty(  TREE_NAME, treeName );
            treeRootRelationship.setProperty( IS_UNIQUE_INDEX, isUniqueIndex );
            treeRootRelationship.setProperty( COMPARATOR_CLASS, nodeComparator.getClass().getName());

            this.treeName = treeName;
    		this.isUniqueIndex = isUniqueIndex;
            this.nodeComparator = nodeComparator;

            tx.success();
        }
        finally
        {
            tx.finish();
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

    @Override
    public Node getBaseNode()
    {
        return baseNode;
    }

    /**
	 * Adds a {@link Node} to this list.
	 * @param node the {@link Node} to add.
	 * @return {@code true} if this call modified the tree, i.e. if the node
	 * wasn't already added.
	 */
	public Relationship addNode( Node node )
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
	public boolean remove( Node node )
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
		return baseNode.getGraphDatabase();
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

    class RelationshipIterator implements Iterator<Relationship>{

    	private TreeNode currentNode;

        NodeEntry entry = null;
        Iterator<Relationship> ni = null;
        RelationshipIterator bi = null;
        RelationshipIterator ai = null;
        int step = 0;

    	RelationshipIterator(TreeNode currentNode){
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
				bi = new RelationshipIterator(beforeTree);
			}
			ni = entry.getRelationships().iterator();
    	}

    	private void initAfterEntry(NodeEntry entry){
            TreeNode afterTree = entry.getAfterSubTree();
            if ( afterTree != null )
            {
                ai = new RelationshipIterator( afterTree );
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
		public Relationship next() {
			if(bi != null && bi.hasNext()){
				return bi.next();
			}else if(ni.hasNext()){
				Relationship n = ni.next();
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

    @Override
    public Iterable<Relationship> getValueRelationships()
    {
        return new Iterable<Relationship>() {

            @Override
            public Iterator<Relationship> iterator()
            {
                return new RelationshipIterator( treeRoot );
            }
        };
    }
}