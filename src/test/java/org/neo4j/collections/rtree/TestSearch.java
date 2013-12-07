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
package org.neo4j.collections.rtree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.neo4j.collections.graphdb.ReferenceNodes;
import org.neo4j.collections.rtree.filter.SearchCoveredByEnvelope;
import org.neo4j.collections.rtree.filter.SearchEqualEnvelopes;
import org.neo4j.collections.rtree.filter.SearchFilter;
import org.neo4j.collections.rtree.filter.SearchResults;
import org.neo4j.graphdb.Node;


public class TestSearch extends SpatialTestCase {
	
	@Test
	public void myFirstTest() {
		RTreeIndex index = new RTreeIndex(graphDb(), ReferenceNodes.getOrCreateInstance(graphDb()).getReferenceNode(), 
				new EnvelopeDecoderFromDoubleArray("bbox"));

		assertTrue(index.isEmpty());
		assertEquals(0, index.count());		
		
//		// invalid bbox test
//		index.add(createGeomNode(0, 0, -2, -3));
		
		// equal bbox test
		index.add(createGeomNode(0, 0, 2, 3));
		
		index.add(createGeomNode(10, 0));
		index.add(createGeomNode(12, 0));
		index.add(createGeomNode(14, 2));
		index.add(createGeomNode(25, 32));
		
		Node geomNode = createGeomNode(11, 1);
		index.add(geomNode);

		assertFalse(index.isEmpty());
		assertEquals(6, index.count());		
		
		assertTrue(index.isNodeIndexed(geomNode.getId()));
		index.remove(geomNode.getId(), false);
		assertFalse(index.isNodeIndexed(geomNode.getId()));		
		
		assertEquals(5, index.count());
		
		Envelope bbox = index.getBoundingBox();
		Envelope expectedBbox = new Envelope(0, 25, 0, 32);
		assertEnvelopeEquals(bbox, expectedBbox);
		
		SearchFilter search = new SearchEqualEnvelopes(index.getEnvelopeDecoder(), new Envelope(0, 2, 0, 3));
		SearchResults results = index.searchIndex(search);
		assertEquals(1, results.count());

		search = new SearchCoveredByEnvelope(index.getEnvelopeDecoder(), new Envelope(9, 15, -1, 3));
		results = index.searchIndex(search);
		assertEquals(3, results.count());
		
		index.clear(new NullListener());
		assertEquals(0, index.count());
		
		debugIndexTree(index, ReferenceNodes.getOrCreateInstance(graphDb()).getReferenceNode());
	}

}