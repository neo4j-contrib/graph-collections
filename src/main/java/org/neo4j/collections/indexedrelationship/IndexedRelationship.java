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

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Node;
import org.neo4j.collections.sortedtree.SortedTree;

public class IndexedRelationship {
	
	private final SortedTree bTree;
	private final Node indexedNode;
	private final RelationshipType relType; 
	
	
	/**
  	 * @param relType {@link RelationshipType} of the relationships maintained in the index.
	 * @param nodeComparator the {@link Comparator} to use to sort the nodes.
	 * @param isUniqueIndex determines if every entry in the tree needs to have a unique comparator value
	 * @param node the start node of the relationship. 
	 * @param graphDb the {@link GraphDatabaseService} instance.
	 */
	public IndexedRelationship(RelationshipType relType, Comparator<Node> nodeComparator, boolean isUniqueIndex, Node node, GraphDatabaseService graphDb){
		indexedNode = node;
		this.relType = relType;
		Relationship rel = node.getSingleRelationship(SortedTree.RelTypes.TREE_ROOT, Direction.OUTGOING);
		Node treeNode = ( rel == null ) ? graphDb.createNode() : rel.getEndNode();
		bTree = new SortedTree(graphDb, treeNode, nodeComparator, isUniqueIndex, relType.name());
	}

	/**
	 * Creates a relationship from the indexed node to the supplied node
	 * @param node the end node of the relationship.
	 * @return {@code true} if this call modified the index, i.e. if the node
	 * wasn't already added.
	 */
	public boolean createRelationshipTo(Node node){
		return bTree.addNode(node);
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
	public Iterable<Node> getIndexEntries(){
		return bTree;
	}
	
}
