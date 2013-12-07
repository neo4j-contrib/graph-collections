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

import org.junit.Test;
import org.neo4j.collections.graphdb.ReferenceNodes;
import org.neo4j.collections.rtree.filter.SearchCoveredByEnvelope;
import org.neo4j.collections.rtree.filter.SearchEqualEnvelopes;
import org.neo4j.collections.rtree.filter.SearchFilter;
import org.neo4j.collections.rtree.filter.SearchResults;
import org.neo4j.graphdb.Node;


public class TestSearchFilter extends SpatialTestCase {
	
	@Test
	public void searchIndexWithFilter() {
		RTreeIndex index = new RTreeIndex(graphDb(), ReferenceNodes.getOrCreateInstance(graphDb()).getReferenceNode(),  
				new EnvelopeDecoderFromDoubleArray("bbox"));

		// equal bbox test
		index.add(createGeomNode(0, 0, 2, 3));
		
		index.add(createGeomNode(10, 0));
		index.add(createGeomNode(12, 0));
		index.add(createGeomNode(14, 2));
		index.add(createGeomNode(25, 32));
		
		assertFalse(index.isEmpty());
		assertEquals(5, index.count());
		
		SearchFilter filter = new SearchEqualEnvelopes(index.getEnvelopeDecoder(), new Envelope(0, 2, 0, 3));
		SearchResults results = index.searchIndex(filter);

		int count = 0;
		for (Node node : results) {
			System.out.println("found node: " + node.getId());
			count++;
		}
		
		assertEquals(1, count);
		
		filter = new SearchCoveredByEnvelope(index.getEnvelopeDecoder(), new Envelope(9, 15, -1, 3));
		results = index.searchIndex(filter);
		
		count = 0;
		for (Node node : results) {
			System.out.println("found node: " + node.getId());
			count++;
		}
		
		assertEquals(3, count);
	}

}