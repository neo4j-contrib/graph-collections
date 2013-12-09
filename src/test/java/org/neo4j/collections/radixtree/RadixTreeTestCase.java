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

import org.neo4j.collections.Neo4jTestCase;
import org.neo4j.collections.graphdb.ReferenceNodes;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;


public abstract class RadixTreeTestCase extends Neo4jTestCase {
	
	protected Node createSampleNode(String key) {
		Node node = graphDb().createNode();
		node.setProperty("label", key);
		return node;
	}
	
	protected RadixTree createIndex() {
		return new RadixTreeImpl(graphDb(), ReferenceNodes.getReferenceNode(graphDb()));
	}
	
	public static void debugIndexTree(RadixTree index, Node rootNode) {
		printTree(getIndexRoot(rootNode), 0);
	}

	private static Node getIndexRoot(Node rootNode) {
		return rootNode.getSingleRelationship(RadixTreeRelationshipTypes.RADIXTREE_ROOT, Direction.OUTGOING).getEndNode();
	}	
	
	private static void printTree(Node root, int depth) {
		StringBuffer tab = new StringBuffer();
		for (int i = 0; i < depth; i++) {
			tab.append("  ");
		}
		
		System.out.println(tab.toString() + "INDEX: " + root + " LABEL[" + root.getProperty(RadixTreeImpl.RADIXTREE_LABEL) + "]");
		
		StringBuffer data = new StringBuffer();
		for (Relationship rel : root.getRelationships(RadixTreeRelationshipTypes.RADIXTREE_LEAF, Direction.OUTGOING)) {
			if (data.length() > 0) {
				data.append(", ");
			} else {
				data.append("DATA: ");
			}
			Node valueNode = rel.getEndNode();
			data.append(valueNode.toString() + " LABEL[" + valueNode.getProperty("label") + "]");
		}
		
		if (data.length() > 0) {
			System.out.println("  " + tab + data);
		}
		
		for (Relationship rel : root.getRelationships(RadixTreeRelationshipTypes.RADIXTREE_TREE, Direction.OUTGOING)) {
			printTree(rel.getEndNode(), depth + 1);
		}
	}
	
}