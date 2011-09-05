/**
 * Copyright (c) 2002-2011 "Neo Technology,"
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
package org.neo4j.collections.rtree;

import static org.junit.Assert.assertTrue;

import org.neo4j.collections.Neo4jTestCase;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;


public abstract class SpatialTestCase extends Neo4jTestCase {

	protected void assertEnvelopeEquals(Envelope a, Envelope b) {
		assertTrue(a.getMinX() == b.getMinX() &&
				a.getMinY() == b.getMinY() &&
				a.getMaxX() == b.getMaxX() &&
				a.getMaxY() == b.getMaxY());
	}	
	
	protected RTreeIndex createIndex() {
		return new RTreeIndex(graphDb(), graphDb().getReferenceNode(), 
				new EnvelopeDecoderFromDoubleArray("bbox"));
	}

	protected Node createGeomNode(double xmin, double ymin) {
		return createGeomNode(xmin, ymin, xmin, ymin);
	}
	
	protected Node createGeomNode(double xmin, double ymin, double xmax, double ymax) {
    	Node node = graphDb().createNode();
    	node.setProperty("bbox", new double[] { xmin, ymin, xmax, ymax });		
    	return node;
	}
	
	protected void debugIndexTree(RTreeIndex index, Node rootNode) {
		printTree(getIndexRoot(rootNode), 0);
	}

	private Node getIndexRoot(Node rootNode) {
		return rootNode.getSingleRelationship(RTreeRelationshipTypes.RTREE_ROOT, Direction.OUTGOING).getEndNode();
	}	
	
	private static String arrayString(double[] test) {
		StringBuffer sb = new StringBuffer();
		for (double d : test) {
			addToArrayString(sb, d);
		}
		sb.append("]");
		return sb.toString();
	}	
	
	private static void addToArrayString(StringBuffer sb, Object obj) {
		if (sb.length() == 0) {
			sb.append("[");
		} else {
			sb.append(",");
		}
		sb.append(obj);
	}
	
	private void printTree(Node root, int depth) {
		StringBuffer tab = new StringBuffer();
		for (int i = 0; i < depth; i++) {
			tab.append("  ");
		}
		
		if (root.hasProperty("bbox")) {
			System.out.println(tab.toString() + "INDEX: " + root + " BBOX[" + arrayString((double[]) root.getProperty("bbox")) + "]");
		} else {
			System.out.println(tab.toString() + "INDEX: " + root);
		}
		
		StringBuffer data = new StringBuffer();
		for (Relationship rel : root.getRelationships(RTreeRelationshipTypes.RTREE_REFERENCE, Direction.OUTGOING)) {
			if (data.length() > 0) {
				data.append(", ");
			} else {
				data.append("DATA: ");
			}
			data.append(rel.getEndNode().toString());
		}
		
		if (data.length() > 0) {
			System.out.println("  " + tab + data);
		}
		
		for (Relationship rel : root.getRelationships(RTreeRelationshipTypes.RTREE_CHILD, Direction.OUTGOING)) {
			printTree(rel.getEndNode(), depth + 1);
		}
	}
	
}