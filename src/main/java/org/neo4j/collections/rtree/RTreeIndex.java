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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.ReturnableEvaluator;
import org.neo4j.graphdb.StopEvaluator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.Traverser.Order;


/**
 * 
 */
public class RTreeIndex implements SpatialIndexWriter {

	// Constructor
	
	public RTreeIndex(GraphDatabaseService database, Node rootNode, EnvelopeDecoder envelopeEncoder) {
		this(database, rootNode, envelopeEncoder, 100, 51);
	}
	
	public RTreeIndex(GraphDatabaseService database, Node rootNode, EnvelopeDecoder envelopeDecoder, int maxNodeReferences, int minNodeReferences) {
		this.database = database;
		this.rootNode = rootNode;
		this.envelopeDecoder = envelopeDecoder;
		this.maxNodeReferences = maxNodeReferences;
		this.minNodeReferences = minNodeReferences;

		initIndexRoot();
		initIndexMetadata();		
	}
	
	
	// Public methods
	
	public EnvelopeDecoder getEnvelopeDecoder() {
		return this.envelopeDecoder;
	}
	
	public void add(Node geomNode) {
		// initialize the search with root
		Node parent = getIndexRoot();
		
		// choose a path down to a leaf
		while (!nodeIsLeaf(parent)) {
			parent = chooseSubTree(parent, geomNode);
		}
		
		if (countChildren(parent, RTreeRelationshipTypes.RTREE_REFERENCE) == maxNodeReferences) {
			insertInLeaf(parent, geomNode);
			splitAndAdjustPathBoundingBox(parent);
		} else {
			if (insertInLeaf(parent, geomNode)) {
				// bbox enlargement needed
				adjustPathBoundingBox(parent);							
			}
		}
		
		countSaved = false;
		totalGeometryCount++;
	}
	
	public void remove(long geomNodeId, boolean deleteGeomNode) {
		Node geomNode = database.getNodeById(geomNodeId);
		
		// be sure geomNode is inside this RTree
		Node indexNode = findLeafContainingGeometryNode(geomNode, true);
		
		// remove the entry 
		geomNode.getSingleRelationship(RTreeRelationshipTypes.RTREE_REFERENCE, Direction.INCOMING).delete();
		if (deleteGeomNode) deleteNode(geomNode);
		
		// reorganize the tree if needed
		if (getIndexNodeParent(indexNode) != null && countChildren(indexNode, RTreeRelationshipTypes.RTREE_REFERENCE) < minNodeReferences) {
			// indexNode is not the root and contain less than the minimum number of entries
			// tree needs reorganization
			
			// find the parent that must be deleted (its children < minNodeReferences) nearest to the root
			Node lastParentNodeToDelete = findIndexNodeToDeleteNearestToRoot(indexNode);
			
			// find all geomNodes in the subtree
			SearchAll search = new SearchAll();
			visit(search, lastParentNodeToDelete);
			List<Node> orphanedGeometryNodes = search.getResults();

			// remove all geomNode in the subtree
			for (Node orphan : orphanedGeometryNodes) {
				orphan.getSingleRelationship(RTreeRelationshipTypes.RTREE_REFERENCE, Direction.INCOMING).delete();
			}
			
			Node deletedNodeParent = getIndexNodeParent(lastParentNodeToDelete);
			deleteRecursivelyEmptySubtree(lastParentNodeToDelete);

			// adjust tree
			adjustParentBoundingBox(deletedNodeParent, RTreeRelationshipTypes.RTREE_CHILD);
			adjustPathBoundingBox(deletedNodeParent);
			
			// add orphaned geomNodes
			for (Node orphan : orphanedGeometryNodes) {
				add(orphan);
			}			
		} else {
			// indexNode is root or contains more than the minimum number of geomNode references
			adjustParentBoundingBox(indexNode, RTreeRelationshipTypes.RTREE_REFERENCE);
			adjustPathBoundingBox(indexNode);
		}
		
		countSaved = false;
		totalGeometryCount--;		
	}
	
	public void removeAll(final boolean deleteGeomNodes, final Listener monitor) {
		Node indexRoot = getIndexRoot();
		
		monitor.begin(count());
		try {
			// delete all geometry nodes
			visitInTx(new SpatialIndexVisitor() {
				public boolean needsToVisit(Envelope indexNodeEnvelope) {
					return true;
				}
	
				public void onIndexReference(Node geomNode) {
					geomNode.getSingleRelationship(RTreeRelationshipTypes.RTREE_REFERENCE, Direction.INCOMING).delete();
					if (deleteGeomNodes) deleteNode(geomNode);
					
					monitor.worked(1);
				}
			}, indexRoot.getId());	
		} finally {
			monitor.done();
		}

		Transaction tx = database.beginTx();
		try {
			// delete index root relationship
			indexRoot.getSingleRelationship(RTreeRelationshipTypes.RTREE_ROOT, Direction.INCOMING).delete();
			
			// delete tree
			deleteRecursivelyEmptySubtree(indexRoot);
			
			// delete tree metadata
			Relationship metadataNodeRelationship = getRootNode().getSingleRelationship(RTreeRelationshipTypes.RTREE_METADATA, Direction.OUTGOING);
			Node metadataNode = metadataNodeRelationship.getEndNode();
			metadataNodeRelationship.delete();
			metadataNode.delete();
		
			tx.success();
		} finally {
			tx.finish();
		}		
		
		countSaved = false;
		totalGeometryCount = 0;				
	}
	
    public void clear(final Listener monitor) {
        removeAll(false, new NullListener());
        Transaction tx = database.beginTx();
        try {
            initIndexRoot();
            initIndexMetadata();            
            tx.success();
        } finally {
            tx.finish();
        }
    }
	
	public Envelope getBoundingBox() {
		return getIndexNodeEnvelope(getIndexRoot());
	}
	
	public int count() {
		saveCount();
		return totalGeometryCount;
	}

	public boolean isEmpty() {
		Node indexRoot = getIndexRoot();
		return !indexRoot.hasProperty(PROP_BBOX);
	}
	
	public boolean isNodeIndexed(Long geomNodeId) {
		Node geomNode = database.getNodeById(geomNodeId);			
		// be sure geomNode is inside this RTree
		return findLeafContainingGeometryNode(geomNode, false) != null;
	}

	public void executeSearch(Search search) {
		if (isEmpty()) return;
		
		saveCount();
		
		visit(search, getIndexRoot());
	}
	
	public void warmUp() {
		visit(new WarmUpVisitor(), getIndexRoot());
	}
	
	public Iterable<Node> getAllIndexInternalNodes() {
		return getIndexRoot().traverse(Order.BREADTH_FIRST, StopEvaluator.END_OF_GRAPH, ReturnableEvaluator.ALL_BUT_START_NODE,
		        RTreeRelationshipTypes.RTREE_CHILD, Direction.OUTGOING);
	}

	public Iterable<Node> getAllIndexedNodes() {
		return new IndexNodeToGeometryNodeIterable(getAllIndexInternalNodes());
	}
	
	
	// Private methods

	/**
	 * The leaf nodes belong to the domain model, and as such need to use the
	 * layers domain-specific GeometryEncoder for decoding the envelope.
	 */
	private Envelope getLeafNodeEnvelope(Node geomNode) {
		return envelopeDecoder.decodeEnvelope(geomNode);
	}
	
	/**
	 * The index nodes do NOT belong to the domain model, and as such need to
	 * use the indexes internal knowledge of the index tree and node structure
	 * for decoding the envelope.
	 */
	private Envelope getIndexNodeEnvelope(Node indexNode) {
		if (indexNode == null) indexNode = getIndexRoot();
		if (!indexNode.hasProperty(PROP_BBOX)) {
			System.err.println("node[" + indexNode + "] has no bounding box property '" + PROP_BBOX + "'");
			return null;
		}
		return bboxToEnvelope((double[]) indexNode.getProperty(PROP_BBOX));
	}
	
	private void visit(SpatialIndexVisitor visitor, Node indexNode) {
		if (!visitor.needsToVisit(getIndexNodeEnvelope(indexNode))) return;
		
		if (indexNode.hasRelationship(RTreeRelationshipTypes.RTREE_CHILD, Direction.OUTGOING)) {
			// Node is not a leaf
			for (Relationship rel : indexNode.getRelationships(RTreeRelationshipTypes.RTREE_CHILD, Direction.OUTGOING)) {
				Node child = rel.getEndNode();
				// collect children results
				visit(visitor, child);
			}
		} else if (indexNode.hasRelationship(RTreeRelationshipTypes.RTREE_REFERENCE, Direction.OUTGOING)) {
			// Node is a leaf
			for (Relationship rel : indexNode.getRelationships(RTreeRelationshipTypes.RTREE_REFERENCE, Direction.OUTGOING)) {
				visitor.onIndexReference(rel.getEndNode());
			}
		}
	}
	
	private void visitInTx(SpatialIndexVisitor visitor, Long indexNodeId) {
        Node indexNode = database.getNodeById(indexNodeId);
        if(!visitor.needsToVisit(getIndexNodeEnvelope(indexNode))) return;
		
		if (indexNode.hasRelationship(RTreeRelationshipTypes.RTREE_CHILD, Direction.OUTGOING)) {
			// Node is not a leaf
			
			// collect children
			List<Long> children = new ArrayList<Long>();
            for (Relationship rel : indexNode.getRelationships(RTreeRelationshipTypes.RTREE_CHILD, Direction.OUTGOING)) {
                children.add(rel.getEndNode().getId());
            }

			// visit children
			for (Long child : children) {
				visitInTx(visitor, child);	
			}
		} else if (indexNode.hasRelationship(RTreeRelationshipTypes.RTREE_REFERENCE, Direction.OUTGOING)) {
			// Node is a leaf
			Transaction tx = database.beginTx();
			try {
				for (Relationship rel : indexNode.getRelationships(RTreeRelationshipTypes.RTREE_REFERENCE, Direction.OUTGOING)) {
					visitor.onIndexReference(rel.getEndNode());
				}
			
				tx.success();
			} finally {
				tx.finish();
			}
		}
	}
	
	private void initIndexMetadata() {
		Node layerNode = getRootNode();
		if (layerNode.hasRelationship(RTreeRelationshipTypes.RTREE_METADATA, Direction.OUTGOING)) {
			// metadata already present
			metadataNode = layerNode.getSingleRelationship(RTreeRelationshipTypes.RTREE_METADATA, Direction.OUTGOING).getEndNode();
			
			maxNodeReferences = (Integer) metadataNode.getProperty("maxNodeReferences");
			minNodeReferences = (Integer) metadataNode.getProperty("minNodeReferences");
		} else {
			// metadata initialization
			metadataNode = database.createNode();
			layerNode.createRelationshipTo(metadataNode, RTreeRelationshipTypes.RTREE_METADATA);
			
			metadataNode.setProperty("maxNodeReferences", maxNodeReferences);
			metadataNode.setProperty("minNodeReferences", minNodeReferences);
		}
		
		saveCount();
	}

	private void initIndexRoot() {
		Node layerNode = getRootNode();
		if (!layerNode.hasRelationship(RTreeRelationshipTypes.RTREE_ROOT, Direction.OUTGOING)) {
			// index initialization
			Node root = database.createNode();
			layerNode.createRelationshipTo(root, RTreeRelationshipTypes.RTREE_ROOT);
		}
	}
	
	private Node getIndexRoot() {
		return getRootNode().getSingleRelationship(RTreeRelationshipTypes.RTREE_ROOT, Direction.OUTGOING).getEndNode();
	}
	
	private Node getMetadataNode() {
		if (metadataNode == null) {
			metadataNode = getRootNode().getSingleRelationship(RTreeRelationshipTypes.RTREE_METADATA, Direction.OUTGOING).getEndNode();
		}
		
		return metadataNode;
	}
	
	/**
	 * Save the geometry count to the database if it has not been saved yet.
	 * However, if the count is zero, first do an exhaustive search of the tree
	 * and count everything before saving it.
	 */
	private void saveCount() {
		if (totalGeometryCount == 0) {
			RecordCounter counter = new RecordCounter();
			visit(counter, getIndexRoot());
			totalGeometryCount = counter.getResult();
			countSaved = false;
		}
	 	
		if (!countSaved) {
			Transaction tx = database.beginTx();
			try {
				getMetadataNode().setProperty("totalGeometryCount", totalGeometryCount);
				countSaved = true;
				tx.success();
			} finally {
				tx.finish();
			}
		}
	}	
	
	private boolean nodeIsLeaf(Node node) {
		return !node.hasRelationship(RTreeRelationshipTypes.RTREE_CHILD, Direction.OUTGOING);
	}
	
	private Node chooseSubTree(Node parentIndexNode, Node geomRootNode) {
		// children that can contain the new geometry
		List<Node> indexNodes = new ArrayList<Node>();
		
		// pick the child that contains the new geometry bounding box		
		Iterable<Relationship> relationships = parentIndexNode.getRelationships(RTreeRelationshipTypes.RTREE_CHILD, Direction.OUTGOING);		
		for (Relationship relation : relationships) {
			Node indexNode = relation.getEndNode();
			if (getIndexNodeEnvelope(indexNode).contains(getLeafNodeEnvelope(geomRootNode))) {
				indexNodes.add(indexNode);
			}
		}

		if (indexNodes.size() > 1) {
			return chooseIndexNodeWithSmallestArea(indexNodes);
		} else if (indexNodes.size() == 1) {
			return indexNodes.get(0);
		}
		
		// pick the child that needs the minimum enlargement to include the new geometry
		double minimumEnlargement = Double.POSITIVE_INFINITY;
		relationships = parentIndexNode.getRelationships(RTreeRelationshipTypes.RTREE_CHILD, Direction.OUTGOING);
		for (Relationship relation : relationships) {
			Node indexNode = relation.getEndNode();
			double enlargementNeeded = getAreaEnlargement(indexNode, geomRootNode);

			if (enlargementNeeded < minimumEnlargement) {
				indexNodes.clear();
				indexNodes.add(indexNode);
				minimumEnlargement = enlargementNeeded;
			} else if (enlargementNeeded == minimumEnlargement) {
				indexNodes.add(indexNode);				
			}
		}
		
		if (indexNodes.size() > 1) {
			return chooseIndexNodeWithSmallestArea(indexNodes);
		} else if (indexNodes.size() == 1) {
			return indexNodes.get(0);
		} else {
			// this shouldn't happen
			throw new RuntimeException("No IndexNode found for new geometry");
		}
	}

    private double getAreaEnlargement(Node indexNode, Node geomRootNode) {
    	Envelope before = getIndexNodeEnvelope(indexNode);
    	
    	Envelope after = getLeafNodeEnvelope(geomRootNode);
    	after.expandToInclude(before);
    	
    	return getArea(after) - getArea(before);
    }
	
	private Node chooseIndexNodeWithSmallestArea(List<Node> indexNodes) {
		Node result = null;
		double smallestArea = -1;

		for (Node indexNode : indexNodes) {
			double area = getArea(indexNode);
			if (result == null || area < smallestArea) {
				result = indexNode;
				smallestArea = area;
			}
		}
		
		return result;
	}

	private int countChildren(Node indexNode, RelationshipType relationshipType) {
		int counter = 0;
		Iterator<Relationship> iterator = indexNode.getRelationships(relationshipType, Direction.OUTGOING).iterator();
		while (iterator.hasNext()) {
			iterator.next();
			counter++;
		}
		return counter;
	}
	
	/**
	 * @return is enlargement needed?
	 */
	private boolean insertInLeaf(Node indexNode, Node geomRootNode) {
		return addChild(indexNode, RTreeRelationshipTypes.RTREE_REFERENCE, geomRootNode);
	}

	private void splitAndAdjustPathBoundingBox(Node indexNode) {
		// create a new node and distribute the entries
		Node newIndexNode = quadraticSplit(indexNode);
		Node parent = getIndexNodeParent(indexNode);
		if (parent == null) {
			// if indexNode is the root
			createNewRoot(indexNode, newIndexNode);
		} else {
			adjustParentBoundingBox(parent, (double[]) indexNode.getProperty(PROP_BBOX));
			
			addChild(parent, RTreeRelationshipTypes.RTREE_CHILD, newIndexNode);

			if (countChildren(parent, RTreeRelationshipTypes.RTREE_CHILD) > maxNodeReferences) {
				splitAndAdjustPathBoundingBox(parent);
			} else {
				adjustPathBoundingBox(parent);
			}
		}
	}

	private Node quadraticSplit(Node indexNode) {
		if (nodeIsLeaf(indexNode)) return quadraticSplit(indexNode, RTreeRelationshipTypes.RTREE_REFERENCE);
		else return quadraticSplit(indexNode, RTreeRelationshipTypes.RTREE_CHILD);
	}

	private Node quadraticSplit(Node indexNode, RelationshipType relationshipType) {
 		List<Node> entries = new ArrayList<Node>();
		
		Iterable<Relationship> relationships = indexNode.getRelationships(relationshipType, Direction.OUTGOING);
		for (Relationship relationship : relationships) {
			entries.add(relationship.getEndNode());
			relationship.delete();
		}

		// pick two seed entries such that the dead space is maximal
		Node seed1 = null;
		Node seed2 = null;
		double worst = Double.NEGATIVE_INFINITY;
		for (Node e : entries) {
			Envelope eEnvelope = getLeafNodeEnvelope(e);
			for (Node e1 : entries) {
				Envelope e1Envelope = getLeafNodeEnvelope(e1);
				double deadSpace = getArea(createEnvelope(eEnvelope, e1Envelope)) - getArea(eEnvelope) - getArea(e1Envelope);
				if (deadSpace > worst) {
					worst = deadSpace;
					seed1 = e;
					seed2 = e1;
				}
			}
		}
		
		List<Node> group1 = new ArrayList<Node>();
		group1.add(seed1);
		Envelope group1envelope = getLeafNodeEnvelope(seed1);
		
		List<Node> group2 = new ArrayList<Node>();
		group2.add(seed2);
		Envelope group2envelope = getLeafNodeEnvelope(seed2);
		
		entries.remove(seed1);
		entries.remove(seed2);
		while (entries.size() > 0) {
			// compute the cost of inserting each entry
			List<Node> bestGroup = null;
			Envelope bestGroupEnvelope = null;
			Node bestEntry = null;
			double expansionMin = Double.POSITIVE_INFINITY;
			for (Node e : entries) {
				Envelope nodeEnvelope = getLeafNodeEnvelope(e);
				double expansion1 = getArea(createEnvelope(nodeEnvelope, group1envelope)) - getArea(group1envelope);
				double expansion2 = getArea(createEnvelope(nodeEnvelope, group2envelope)) - getArea(group2envelope);
						
				if (expansion1 < expansion2 && expansion1 < expansionMin) {
					bestGroup = group1;
					bestGroupEnvelope = group1envelope;
					bestEntry = e;
					expansionMin = expansion1;
				} else if (expansion2 < expansion1 && expansion2 < expansionMin) {
					bestGroup = group2;
					bestGroupEnvelope = group2envelope;					
					bestEntry = e;
					expansionMin = expansion2;					
				} else if (expansion1 == expansion2 && expansion1 < expansionMin) {
					// in case of equality choose the group with the smallest area
					if (getArea(group1envelope) < getArea(group2envelope)) {
						bestGroup = group1;
						bestGroupEnvelope = group1envelope; 
					} else {
						bestGroup = group2;
						bestGroupEnvelope = group2envelope; 
					}
					bestEntry = e;
					expansionMin = expansion1;					
				}
			}
			
			// insert the best candidate entry in the best group
			bestGroup.add(bestEntry);
			bestGroupEnvelope.expandToInclude(getLeafNodeEnvelope(bestEntry));

			entries.remove(bestEntry);
			
			// each group must contain at least minNodeReferences entries.
			// if the group size added to the number of remaining entries is equal to minNodeReferences
			// just add them to the group
			
			if (group1.size() + entries.size() == minNodeReferences) {
				group1.addAll(entries);
				entries.clear();
			}
			
			if (group2.size() + entries.size() == minNodeReferences) {
				group2.addAll(entries);
				entries.clear();
			}
		}
		
		// reset bounding box and add new children
		indexNode.removeProperty(PROP_BBOX);
		for (Node node : group1) {
			addChild(indexNode, relationshipType, node);
		}

		// create new node from split
		Node newIndexNode = database.createNode();
		for (Node node: group2) {
			addChild(newIndexNode, relationshipType, node);
		}
		
		return newIndexNode;
	}

	private void createNewRoot(Node oldRoot, Node newIndexNode) {
		Node newRoot = database.createNode();
		addChild(newRoot, RTreeRelationshipTypes.RTREE_CHILD, oldRoot);
		addChild(newRoot, RTreeRelationshipTypes.RTREE_CHILD, newIndexNode);
		
		Node layerNode = getRootNode();
		layerNode.getSingleRelationship(RTreeRelationshipTypes.RTREE_ROOT, Direction.OUTGOING).delete();
		layerNode.createRelationshipTo(newRoot, RTreeRelationshipTypes.RTREE_ROOT);
	}

    private double[] envelopeToBBox(Envelope bounds) {
        return new double[]{ bounds.getMinX(), bounds.getMinY(), bounds.getMaxX(), bounds.getMaxY() };
    }

    private Envelope bboxToEnvelope(double[] bbox) {
    	// Envelope parameters: xmin, xmax, ymin, ymax
        return new Envelope(bbox[0], bbox[2], bbox[1], bbox[3]);
    }

	private boolean addChild(Node parent, RelationshipType type, Node newChild) {
	    double[] childBBox = null;
	    if(type == RTreeRelationshipTypes.RTREE_REFERENCE) {
	        childBBox = envelopeToBBox(envelopeDecoder.decodeEnvelope(newChild));
	    } else {
	        childBBox = (double[]) newChild.getProperty(PROP_BBOX);
	    }
		parent.createRelationshipTo(newChild, type);
		return adjustParentBoundingBox(parent, childBBox);
	}
	
	private void adjustPathBoundingBox(Node indexNode) {
		Node parent = getIndexNodeParent(indexNode);
		if (parent != null) {
			if (adjustParentBoundingBox(parent, (double[]) indexNode.getProperty(PROP_BBOX))) {
				// entry has been modified: adjust the path for the parent
				adjustPathBoundingBox(parent);
			}
		}
	}

	/**
	 * Fix an IndexNode bounding box after a child has been removed
	 * @param indexNode
	 */
	private void adjustParentBoundingBox(Node indexNode, RelationshipType relationshipType) {
		Envelope bbox = null;
		
		Iterator<Relationship> iterator = indexNode.getRelationships(relationshipType, Direction.OUTGOING).iterator();
		while (iterator.hasNext()) {
			Node childNode = iterator.next().getEndNode();
			
			if (bbox == null) {
				bbox = new Envelope(getLeafNodeEnvelope(childNode));
			} else {
				bbox.expandToInclude(getLeafNodeEnvelope(childNode));
			}
		}

		if (bbox == null) {
			bbox = new Envelope();
		}
		
		indexNode.setProperty(PROP_BBOX, new double[] { bbox.getMinX(), bbox.getMinY(), bbox.getMaxX(), bbox.getMaxY() });
	}
		
	/**
	 * Adjust IndexNode bounding box according to the new child inserted
	 * @param parent IndexNode
	 * @param child geomNode inserted
	 * @return is bbox changed?
	 */
	private boolean adjustParentBoundingBox(Node parent, double[] childBBox) {
		if (!parent.hasProperty(PROP_BBOX)) {
			parent.setProperty(PROP_BBOX, new double[] { childBBox[0], childBBox[1], childBBox[2], childBBox[3] });
			return true;
		}
		
		double[] parentBBox = (double[]) parent.getProperty(PROP_BBOX);
		
		boolean valueChanged = setMin(parentBBox, childBBox, 0);
		valueChanged = setMin(parentBBox, childBBox, 1) || valueChanged;
		valueChanged = setMax(parentBBox, childBBox, 2) || valueChanged;
		valueChanged = setMax(parentBBox, childBBox, 3) || valueChanged;
		
		if (valueChanged) {
			parent.setProperty(PROP_BBOX, parentBBox);
		}
		
		return valueChanged;
	}

	private boolean setMin(double[] parent, double[] child, int index) {
		if (parent[index] > child[index]) {
			parent[index] = child[index];
			return true;
		} else {
			return false;
		}
	}
	
	private boolean setMax(double[] parent, double[] child, int index) {
		if (parent[index] < child[index]) {
			parent[index] = child[index];
			return true;
		} else {
			return false;
		}
	}
	
	private Node getIndexNodeParent(Node indexNode) {
		Relationship relationship = indexNode.getSingleRelationship(RTreeRelationshipTypes.RTREE_CHILD, Direction.INCOMING);
		if (relationship == null) return null;
		else return relationship.getStartNode();
	}	
	
	private double getArea(Node node) {
		return getArea(getLeafNodeEnvelope(node));
	}

	private double getArea(Envelope e) {
		return e.getWidth() * e.getHeight();
	}

	private Node findIndexNodeToDeleteNearestToRoot(Node indexNode) {
		Node indexNodeParent = getIndexNodeParent(indexNode);
		
		if (getIndexNodeParent(indexNodeParent) != null && countChildren(indexNodeParent, RTreeRelationshipTypes.RTREE_CHILD) == minNodeReferences) {
			// indexNodeParent is not the root and will contain less than the minimum number of entries
			return findIndexNodeToDeleteNearestToRoot(indexNodeParent);
		} else {
			return indexNode;
		}
	}
	
	private void deleteRecursivelyEmptySubtree(Node indexNode) {
		for (Relationship relationship : indexNode.getRelationships(RTreeRelationshipTypes.RTREE_CHILD, Direction.OUTGOING)) {
			deleteRecursivelyEmptySubtree(relationship.getEndNode());
		}
		
		Relationship relationshipWithFather = indexNode.getSingleRelationship(RTreeRelationshipTypes.RTREE_CHILD, Direction.INCOMING);
		// the following check is needed because rootNode doesn't have this relationship
		if (relationshipWithFather != null) {
			relationshipWithFather.delete();
		}
		
		indexNode.delete();
	}
	
	private Node findLeafContainingGeometryNode(Node geomNode, boolean throwExceptionIfNotFound) {
		if (!geomNode.hasRelationship(RTreeRelationshipTypes.RTREE_REFERENCE, Direction.INCOMING)) {
			if (throwExceptionIfNotFound) {
				throw new RuntimeException("GeometryNode not indexed with an RTree: " + geomNode.getId());
			} else {
				return null;
			}
		}

		Node indexNodeLeaf = geomNode.getSingleRelationship(RTreeRelationshipTypes.RTREE_REFERENCE, Direction.INCOMING).getStartNode();		
		
		Node root = null;
		Node child = indexNodeLeaf;
		while (root == null) {
			Node parent = getIndexNodeParent(child);
			if (parent == null) root = child;
			else child = parent;
		}
		
		if (root.getId() != getIndexRoot().getId()) {
			if (throwExceptionIfNotFound) {
				throw new RuntimeException("GeometryNode not indexed in this RTree: " + geomNode.getId());
			} else {
				return null;
			}
		} else {
			return indexNodeLeaf;
		}
	}
	
	private void deleteNode(Node node) {
		for (Relationship r : node.getRelationships()) {
			r.delete();
		}
		node.delete();
	}	
	
	private Node getRootNode() {
		return rootNode;
	}
	
    /**
     * Create a bounding box encompassing the two bounding boxes passed in.
     */	
	private static Envelope createEnvelope(Envelope e, Envelope e1) {
		Envelope result = new Envelope(e);
		result.expandToInclude(e1);
		return result;
	}

	
	// Attributes
	
	private GraphDatabaseService database;
	
	private Node rootNode;
	private EnvelopeDecoder envelopeDecoder;	
	private int maxNodeReferences;
	private int minNodeReferences;
	
	private Node metadataNode;
	private int totalGeometryCount = 0;
	private boolean countSaved = false;	
	
	private static final String PROP_BBOX = "bbox";
	
	
	// Private classes

	private class RecordCounter implements SpatialIndexVisitor {
		
		public boolean needsToVisit(Envelope indexNodeEnvelope) { return true; }	
		
		public void onIndexReference(Node geomNode) { count++; }
		
		public int getResult() { return count; }
		
		private int count = 0;
	}

	private class WarmUpVisitor implements SpatialIndexVisitor {
		
		public boolean needsToVisit(Envelope indexNodeEnvelope) { return true; }	
		
		public void onIndexReference(Node geomNode) { }	
	}

	/**
	 * In order to wrap one iterable or iterator in another that converts the
	 * objects from one type to another without loading all into memory, we need
	 * to use this ugly java-magic. Man, I miss Ruby right now!
	 * 
	 * @author Craig
	 */
	private class IndexNodeToGeometryNodeIterable implements Iterable<Node> {
		
		private Iterator<Node> allIndexNodeIterator;

		private class GeometryNodeIterator implements Iterator<Node> {
			
			Iterator<Node> geometryNodeIterator = null;

			public boolean hasNext() {
				checkGeometryNodeIterator();
				return geometryNodeIterator != null && geometryNodeIterator.hasNext();
			}

			public Node next() {
				checkGeometryNodeIterator();
				return geometryNodeIterator == null ? null : geometryNodeIterator.next();
			}

			private void checkGeometryNodeIterator() {
				while ((geometryNodeIterator == null || !geometryNodeIterator.hasNext()) && allIndexNodeIterator.hasNext()) {
					geometryNodeIterator = allIndexNodeIterator.next().traverse(Order.DEPTH_FIRST, StopEvaluator.DEPTH_ONE,
					        ReturnableEvaluator.ALL_BUT_START_NODE, RTreeRelationshipTypes.RTREE_REFERENCE, Direction.OUTGOING)
					        .iterator();
				}
			}

			public void remove() {
			}
		}

		public IndexNodeToGeometryNodeIterable(Iterable<Node> allIndexNodes) {
			this.allIndexNodeIterator = allIndexNodes.iterator();
		}

		public Iterator<Node> iterator() {
			return new GeometryNodeIterator();
		}
	}
}