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
package org.neo4j.collections.radixtree;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;


/**
 * Implementation of a Radix Tree (http://en.wikipedia.org/wiki/Radix_tree)
 */
public class RadixTreeImpl implements RadixTree {
    
    // Constructor
    
    public RadixTreeImpl(GraphDatabaseService database, Node refNode) {
		this.database = database;
		this.refNode = refNode;
		this.rootNode = database.createNode();
		
		rootNode.setProperty(RADIXTREE_LABEL, "");
		refNode.createRelationshipTo(rootNode, RadixTreeRelationshipTypes.RADIXTREE_ROOT);
    }
    
    
    // Public methods
    
	@Override
	public void insert(String key, Node node) {
		if (key.length() == 0) {
			throw new IllegalArgumentException("Key must have a length greater than zero");
		}
		
		insert(key, node, rootNode, "", false);
	}
    
	@Override
	public List<Node> get(String key) {
		List<Relationship> relationships = new ArrayList<Relationship>();
		getRelationships(key, rootNode, relationships);
		
		List<Node> results = new ArrayList<Node>(relationships.size());		
		for (Relationship rel : relationships) {
			results.add(rel.getEndNode());
		}
		
		return results;
	}
	
	@Override
	public boolean insertUnique(String key, Node node) {
		if (key.length() == 0) {
			throw new IllegalArgumentException("Key must have a length greater than zero");
		}
		
		return insert(key, node, rootNode, "", true);		
	}

	@Override
	public Node getUnique(String key) {
		List<Node> result = get(key);
		if (result != null && result.size() > 0) {
			return result.get(0);
		} else {
			return null;
		}
	}	
	
	@Override
	public int remove(String key, boolean deleteIndexedNodes) {
		// TODO remove empty index nodes

		List<Relationship> results = new ArrayList<Relationship>();
		getRelationships(key, rootNode, results);

		for (Relationship rel : results) {
			Node indexedNode = rel.getEndNode();
			rel.delete();
			if (deleteIndexedNodes) {
				indexedNode.delete();
			}
		}
		
		return results.size();
	}

	
	// Private methods
	
	private boolean isRoot(Node treeNode) {
		return treeNode.hasRelationship(RadixTreeRelationshipTypes.RADIXTREE_ROOT, Direction.INCOMING);
	}
	
	private String getLabel(Node treeNode) {
		return (String) treeNode.getProperty(RADIXTREE_LABEL);
	}
	
	private void setLabel(Node treeNode, String label) {
		treeNode.setProperty(RADIXTREE_LABEL, label);
	}
	
	private boolean insertInRootWithNoMatchingCharacters(String key, Node value, Node tree, boolean unique) {
		// root node
		if (!isRoot(tree)) {
			throw new IllegalStateException();
		}
		
		String completeLabel = getLabel(tree);		
		
		if (!tree.hasRelationship(Direction.OUTGOING, RadixTreeRelationshipTypes.RADIXTREE_TREE) && 
			!tree.hasRelationship(Direction.OUTGOING, RadixTreeRelationshipTypes.RADIXTREE_LEAF)) {
			// init
			setLabel(tree, key);
			tree.createRelationshipTo(value, RadixTreeRelationshipTypes.RADIXTREE_LEAF);
		} else if (completeLabel.equals("")) {
			Node child = searchNodeForNewKey(key, tree, completeLabel);
			if (child != null) {
				return insert(key, value, child, completeLabel, unique);
			} else {
				Node newTreeNode = createNode(key);
				newTreeNode.createRelationshipTo(value, RadixTreeRelationshipTypes.RADIXTREE_LEAF);
				tree.createRelationshipTo(newTreeNode, RadixTreeRelationshipTypes.RADIXTREE_TREE);
			}
		} else {
			Node newRootNode = createNode("");
			
			Node newTreeNode = createNode(key);
			newTreeNode.createRelationshipTo(value, RadixTreeRelationshipTypes.RADIXTREE_LEAF);
			
			refNode.createRelationshipTo(newRootNode, RadixTreeRelationshipTypes.RADIXTREE_ROOT);
			rootNode = newRootNode;
			tree.getSingleRelationship(RadixTreeRelationshipTypes.RADIXTREE_ROOT, Direction.INCOMING).delete();
			
			newRootNode.createRelationshipTo(tree, RadixTreeRelationshipTypes.RADIXTREE_TREE);
			newRootNode.createRelationshipTo(newTreeNode, RadixTreeRelationshipTypes.RADIXTREE_TREE);		
		}
		
		return true;
	}
	
	private void getLeafsRelationships(Node tree, List<Relationship> results) {
		for (Relationship r : tree.getRelationships(Direction.OUTGOING, RadixTreeRelationshipTypes.RADIXTREE_LEAF)) {
			results.add(r);
		}		
	}
	
	private void getRelationships(String key, Node tree, List<Relationship> results) {
		String label = getLabel(tree);
		
		if (label.length() > key.length()) {
			return;
		} else if (label.equals(key)) {
			getLeafsRelationships(tree, results);
		} else {
			int matchingCharacters = countMatchingCharacters(key, label);
			if (matchingCharacters > 0 || isRoot(tree)) {
				key = key.substring(matchingCharacters);
				for (Relationship r : tree.getRelationships(Direction.OUTGOING, RadixTreeRelationshipTypes.RADIXTREE_TREE)) {
					getRelationships(key, r.getEndNode(), results);
					if (results.size() > 0) {
						break;
					}
				}
			}
		}
	}	
		
	private boolean insert(String key, Node value, Node tree, String labelPrefix, boolean unique) {
		String label = getLabel(tree);
		String completeLabel = labelPrefix + label;
		int matchingCharacters = countMatchingCharacters(key, completeLabel);
		
		if (completeLabel.equals(key)) {
			// found tree node with exactly the same key
			if (unique && treeNodeContainsLeaf(tree)) {
				// key already present
				return false;
			}
			
			tree.createRelationshipTo(value, RadixTreeRelationshipTypes.RADIXTREE_LEAF);
		} else if (matchingCharacters == 0) {
			return insertInRootWithNoMatchingCharacters(key, value, tree, unique);
		} else if (matchingCharacters == completeLabel.length() && completeLabel.length() < key.length()) {
			// complete label: ram
			// key: rame
			Node child = searchNodeForNewKey(key, tree, completeLabel);
			if (child != null) {
				return insert(key, value, child, completeLabel, unique);
			}
			
			Node newChild = createNode(key.substring(matchingCharacters));
			newChild.createRelationshipTo(value, RadixTreeRelationshipTypes.RADIXTREE_LEAF);
			tree.createRelationshipTo(newChild, RadixTreeRelationshipTypes.RADIXTREE_TREE);
		} else if (matchingCharacters == labelPrefix.length()) {
			// complete label: r-oman
			// key: rame
			Node newParentNode = createNode(key.substring(0, matchingCharacters));
			getParent(tree).createRelationshipTo(newParentNode, RadixTreeRelationshipTypes.RADIXTREE_TREE);

			Node newTreeNode = createNode(key.substring(matchingCharacters));
			newTreeNode.createRelationshipTo(value, RadixTreeRelationshipTypes.RADIXTREE_LEAF);

			setLabel(tree, completeLabel.substring(matchingCharacters));
			tree.getSingleRelationship(RadixTreeRelationshipTypes.RADIXTREE_TREE, Direction.INCOMING).delete();

			newParentNode.createRelationshipTo(tree, RadixTreeRelationshipTypes.RADIXTREE_TREE);
			newParentNode.createRelationshipTo(newTreeNode, RadixTreeRelationshipTypes.RADIXTREE_TREE);				
		} else if (matchingCharacters == key.length()) {
			// complete label: r-amesses -> r-ame-sses
			// key: rame

			// ame
			Node newParentNode = createNode(key.substring(labelPrefix.length()));
			newParentNode.createRelationshipTo(value, RadixTreeRelationshipTypes.RADIXTREE_LEAF);		
			getParent(tree).createRelationshipTo(newParentNode, RadixTreeRelationshipTypes.RADIXTREE_TREE);

			// sses
			setLabel(tree, completeLabel.substring(matchingCharacters));
			tree.getSingleRelationship(RadixTreeRelationshipTypes.RADIXTREE_TREE, Direction.INCOMING).delete();
			newParentNode.createRelationshipTo(tree, RadixTreeRelationshipTypes.RADIXTREE_TREE);
		} else {
			// complete label: va-roman -> va-r-oman
			// key: varame

			boolean newRootNeeded = isRoot(tree);
			
			// r
			Node newParentNode = createNode(completeLabel.substring(0, matchingCharacters).substring(labelPrefix.length()));
			if (newRootNeeded) {
				refNode.createRelationshipTo(newParentNode, RadixTreeRelationshipTypes.RADIXTREE_ROOT);
				rootNode = newParentNode;
			} else {
				getParent(tree).createRelationshipTo(newParentNode, RadixTreeRelationshipTypes.RADIXTREE_TREE);				
			}

			// oman
			setLabel(tree, completeLabel.substring(matchingCharacters));
			if (newRootNeeded) {
				tree.getSingleRelationship(RadixTreeRelationshipTypes.RADIXTREE_ROOT, Direction.INCOMING).delete();								
			} else {
				tree.getSingleRelationship(RadixTreeRelationshipTypes.RADIXTREE_TREE, Direction.INCOMING).delete();								
			}

			// ame
			Node newTreeNode = createNode(key.substring(matchingCharacters));
			newTreeNode.createRelationshipTo(value, RadixTreeRelationshipTypes.RADIXTREE_LEAF);

			newParentNode.createRelationshipTo(tree, RadixTreeRelationshipTypes.RADIXTREE_TREE);
			newParentNode.createRelationshipTo(newTreeNode, RadixTreeRelationshipTypes.RADIXTREE_TREE);
		}
		
		return true;
	}
	
	private Node createNode(String label) {
		Node treeNode = database.createNode();
		setLabel(treeNode, label);
		return treeNode;
	}
			
	private Node searchNodeForNewKey(String key, Node tree, String labelPrefix) {
		Iterable<Relationship> rels = tree.getRelationships(RadixTreeRelationshipTypes.RADIXTREE_TREE, Direction.OUTGOING);
		for (Relationship rel : rels) {
			Node child = rel.getEndNode();
			String completeChildLabel = labelPrefix + getLabel(child);
			if (countMatchingCharacters(key, completeChildLabel) > labelPrefix.length()) {
				return child;
			}
		}
		
		return null;
	}
			
	private Node getParent(Node treeNode) {
		return treeNode.getSingleRelationship(RadixTreeRelationshipTypes.RADIXTREE_TREE, Direction.INCOMING).getStartNode();
	}
	
	private boolean treeNodeContainsLeaf(Node treeNode) {
		return treeNode.hasRelationship(RadixTreeRelationshipTypes.RADIXTREE_LEAF, Direction.OUTGOING);
	}
	
	private int countMatchingCharacters(String key, String treeLabel) {
		int count = 0;
        while (count < key.length() && 
        	   count < treeLabel.length()) {
            if (key.charAt(count) != treeLabel.charAt(count)) {
                break;
            }
            
            count++;
        }
		return count;
	}

	
	// Attributes
	
	private final GraphDatabaseService database;
	private final Node refNode;
	private Node rootNode;
	
	public static final String RADIXTREE_LABEL = "radixtree_label";
}