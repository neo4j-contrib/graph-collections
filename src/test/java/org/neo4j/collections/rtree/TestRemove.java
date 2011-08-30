package org.neo4j.collections.rtree;

import org.junit.Test;
import org.neo4j.collections.Neo4jTestCase;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;


public class TestRemove extends Neo4jTestCase {

	@Test
	public void testAddMoreThanMaxNodeRefThenDeleteAll() throws Exception {
		int rtreeMaxNodeReferences = 100;
		
		RTreeIndex index = new RTreeIndex(graphDb(), graphDb().getReferenceNode(), 
				new EnvelopeEncoder() {
					@Override
					public Envelope decodeEnvelope(PropertyContainer c) {
						double[] bbox = (double[]) c.getProperty("bbox");
						return new Envelope(bbox[0], bbox[1], bbox[2], bbox[3]);
					}
		}, rtreeMaxNodeReferences, 51);
		
        long[] ids = new long[rtreeMaxNodeReferences + 1];
        for (int i = 0; i < ids.length; i++) {
        	Node node = graphDb().createNode();
        	node.setProperty("bbox", new double[] { i, i + 1, i, i + 1 });
        	ids[i] = node.getId();
        	index.add(node);
        }

        debugIndexTree(index, graphDb().getReferenceNode());        
        
        for (long id : ids) {
        	index.remove(id, true);
        }
        
        debugIndexTree(index, graphDb().getReferenceNode());
    }		
	
	private Node getIndexRoot(Node rootNode) {
		return rootNode.getSingleRelationship(RTreeRelationshipTypes.RTREE_ROOT, Direction.OUTGOING).getEndNode();
	}
	
	private void debugIndexTree(RTreeIndex index, Node rootNode) {
		printTree(getIndexRoot(rootNode), 0);
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