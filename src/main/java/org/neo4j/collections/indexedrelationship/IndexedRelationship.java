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

/**
 * A specialized relationship type using a {@link SortedTree} index.
 * Every relationship is stored in the index, making it possible to
 * have a sorted collection of relationships, or to enforce a unicity 
 * constraint on the relationship.
 * 
 * This class is also useful when confronted with situations where
 * normal relationships would lead to densely populated nodes. 
 * 
 * Given an end node, the start node of the relationship 
 * can be found by traversing:
 * KEY_VALUE, INCOMING
 * KEY_ENTRY, INCOMING 
 * SUB_TREE, INCOMING
 * TREE_ROOT, INCOMING
 * 
 * The name of the RelationshipType is stored as a property with keyname "tree_name" on both
 * the TREE_ROOT and the KEY_VALUE relationships.
 * 
 * Given a start node of the relationship, all end nodes
 * can be found by traversing:
 * TREE_ROOT, OUTGOING
 * SUB_TREE, OUTGOING
 * KEY_ENTRY, OUTGOING
 * KEY_VALUE, OUTGOING  
 * 
 */
package org.neo4j.collections.indexedrelationship;

import java.util.Comparator;
import java.util.Iterator;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Node;
import org.neo4j.collections.sortedtree.SortedTree;
import org.neo4j.collections.sortedtree.PropertySortedTree;

import org.neo4j.collections.graphdb.PropertyType.ComparablePropertyType;

public class IndexedRelationship implements Iterable<Relationship>{

	public static final String directionPropertyName = "relationship_direction"; 
	
	private final GraphDatabaseService graphDb; 
	private final SortedTree bTree;
	private final Node indexedNode;
	private final RelationshipType relType;
	private final Direction direction;
	
	private Node createTreeRoot(Node node){
		Node treeRoot = graphDb.createNode();
		Relationship rel = indexedNode.createRelationshipTo(treeRoot, SortedTree.RelTypes.TREE_ROOT);
		rel.setProperty(directionPropertyName, direction.name());		
		return treeRoot;
		
	}
	
	private class DirectRelationship implements Relationship{

		final Node startNode;
		final Node endNode;
		final RelationshipType relType;
		final Direction direction;
		final Relationship endRelationship;
		
		DirectRelationship(Node startNode, Node endNode, RelationshipType relType, Direction direction){
			this.startNode = startNode;
			this.endNode = endNode;
			this.relType = relType;
			this.direction = direction;
			this.endRelationship = endNode.getSingleRelationship(SortedTree.RelTypes.KEY_VALUE, Direction.INCOMING); 
		}
		
		@Override
		public GraphDatabaseService getGraphDatabase() {
			return graphDb;
		}

		@Override
		public Object getProperty(String key) {
			return endRelationship.getProperty(key);
		}

		@Override
		public Object getProperty(String key, Object defaultValue) {
			return endRelationship.getProperty(key, defaultValue);
		}

		@Override
		public Iterable<String> getPropertyKeys() {
			return endRelationship.getPropertyKeys();
		}

		@Override
		@Deprecated
		public Iterable<Object> getPropertyValues() {
			return endRelationship.getPropertyValues();
		}

		@Override
		public boolean hasProperty(String key) {
			return endRelationship.hasProperty(key);
		}

		@Override
		public Object removeProperty(String key) {
			return endRelationship.removeProperty(key);
		}

		@Override
		public void setProperty(String key, Object value) {
			if(key.equals(SortedTree.TREE_NAME) || key.equals(SortedTree.COMPARATOR_CLASS) || key.equals(SortedTree.IS_UNIQUE_INDEX)){
				throw new RuntimeException("Property value "+key+" is not a valid property name. This property is maintained by the SortedTree implementation");
			}
			endRelationship.setProperty(key, value);
		}

		@Override
		public void delete() {
			removeRelationshipTo(endNode);
			
		}

		@Override
		public Node getEndNode() {
			return endNode;
		}

		@Override
		public long getId() {
			throw new UnsupportedOperationException("Indexed relationships don't have an ID");
		}

		@Override
		public Node[] getNodes() {
			Node[] nodes = new Node[2];
			nodes[0] = startNode;
			nodes[1] = endNode;
			return nodes;
		}

		@Override
		public Node getOtherNode(Node node) {
			if(node.equals(startNode)){
				return endNode;
			}else if(node.equals(endNode)){
				return startNode;
			}else{
				throw new RuntimeException("Node is neither the start nor the end node");
			}
		}

		@Override
		public Node getStartNode() {
			return startNode;
		}

		@Override
		public RelationshipType getType() {
			return relType;
		}

		@Override
		public boolean isType(RelationshipType relType) {
			if(relType.equals(this.relType)){
				return true;
			}else{
				return false;
			}
		}
		
	}
	
	private class RelationshipIterator implements Iterator<Relationship>{

		Iterator<Node> it = bTree.iterator();
		Node currentNode = null;
		
		@Override
		public boolean hasNext() {
			return it.hasNext();
		}

		@Override
		public Relationship next() {
			currentNode = it.next();
			return new DirectRelationship(indexedNode, currentNode, relType, Direction.OUTGOING);
		}

		@Override
		public void remove() {
			if(currentNode != null){
				removeRelationshipTo(currentNode);
			}
		}
	}

	/**
  	 * @param relType {@link RelationshipType} of the relationships maintained in the index.
	 * @param nodeComparator the {@link Comparator} to use to sort the nodes.
	 * @param isUniqueIndex determines if every entry in the tree needs to have a unique comparator value
	 * @param node the start node of the relationship. 
	 * @param graphDb the {@link GraphDatabaseService} instance.
	 */
	public <T> IndexedRelationship(RelationshipType relType, Direction direction, ComparablePropertyType<T> propertyType, boolean isUniqueIndex, Node node, GraphDatabaseService graphDb){
		indexedNode = node;
		this.relType = relType;
		this.graphDb = graphDb;
		this.direction = direction;
		Relationship rel = node.getSingleRelationship(SortedTree.RelTypes.TREE_ROOT, Direction.OUTGOING);
		Node treeNode = ( rel == null ) ? createTreeRoot(node) : rel.getEndNode();
		bTree = new PropertySortedTree<T>(graphDb, treeNode, propertyType, isUniqueIndex, relType.name());
	}
	
	
	/**
  	 * @param relType {@link RelationshipType} of the relationships maintained in the index.
	 * @param nodeComparator the {@link Comparator} to use to sort the nodes.
	 * @param isUniqueIndex determines if every entry in the tree needs to have a unique comparator value
	 * @param node the start node of the relationship. 
	 * @param graphDb the {@link GraphDatabaseService} instance.
	 */
	public IndexedRelationship(RelationshipType relType, Direction direction, Comparator<Node> nodeComparator, boolean isUniqueIndex, Node node, GraphDatabaseService graphDb){
		indexedNode = node;
		this.relType = relType;
		this.graphDb = graphDb;
		this.direction = direction;
		Relationship rel = node.getSingleRelationship(SortedTree.RelTypes.TREE_ROOT, Direction.OUTGOING);
		Node treeNode = ( rel == null ) ? createTreeRoot(node) : rel.getEndNode();
		bTree = new SortedTree(graphDb, treeNode, nodeComparator, isUniqueIndex, relType.name());
	}

	/**
	 * Creates a relationship from the indexed node to the supplied node
	 * @param node the end node of the relationship.
	 * @return {@code true} if this call modified the index, i.e. if the node
	 * wasn't already added.
	 */
	public Relationship createRelationshipTo(Node node){
		bTree.addNode(node);
		for(Relationship rel: node.getRelationships(SortedTree.RelTypes.KEY_VALUE, Direction.INCOMING)){
			if(rel.getProperty(SortedTree.TREE_NAME).equals(relType.name())){
				if(!rel.hasProperty(directionPropertyName)){
					rel.setProperty(directionPropertyName, direction.name());
				}
			}
		}
		return new DirectRelationship(indexedNode, node, relType, Direction.OUTGOING);
	}

	/**
 	 * Removes the relationship from the indexed node to the supplied node if it exists
	 * @param node the end node of the relationship.
	 * @return {@code true} if this call modified the index, i.e. if the node
	 * was actually stored in the index.
	 */
	public boolean removeRelationshipTo(Node node){
		return bTree.removeNode(node);
	}

	/**
	 * @return the {@link Node} whose outgoing relationships are being indexed.  
	 */
	public Node getIndexedNode(){
		return indexedNode;
	}

	/**
	 * @return the {@link RelationshipType} of the indexed relationships.  
	 */
	public RelationshipType getRelationshipType(){
		return relType;
	}

	/**
	 * @return {@code true} of the index guarantees unicity  
	 */
	public boolean isUniqueIndex(){
		return bTree.isUniqueIndex();
	}
	
	/**
	 * @return the {@link Node}s whose incoming relationships are being indexed.  
	 */
	public Iterator<Relationship> iterator(){
		return new RelationshipIterator();
	}
	
}
