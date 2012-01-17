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
package org.neo4j.collections.rtree;

import org.junit.Test;
import org.neo4j.graphdb.Node;


public class TestRemove extends SpatialTestCase {

	@Test
	public void testAddMoreThanMaxNodeRefThenDeleteAll() throws Exception {
		int rtreeMaxNodeReferences = 100;
		
		RTreeIndex index = new RTreeIndex(graphDb(), graphDb().getReferenceNode(), 
				new EnvelopeDecoderFromDoubleArray("bbox"), rtreeMaxNodeReferences);
		
        long[] ids = new long[rtreeMaxNodeReferences + 1];
        for (int i = 0; i < ids.length; i++) {
        	Node node = createGeomNode(i, i, i + 1, i + 1);
        	ids[i] = node.getId();
        	index.add(node);
        	assertEnvelopeEquals(new Envelope(0, i + 1, 0, i + 1), index.getBoundingBox());
        }

        debugIndexTree(index, graphDb().getReferenceNode());        
        
        for (int i = 0; i < ids.length; i++) {
        	System.out.println("removing " + new Envelope(i, i + 1, i, i + 1));
        	debugIndexTree(index, graphDb().getReferenceNode());
        	assertEnvelopeEquals(new Envelope(i, 101, i, 101), index.getBoundingBox());
        	index.remove(ids[i], true);
        }
        
        assertEnvelopeEquals(new Envelope(0, 0, 0, 0), index.getBoundingBox());
        
        debugIndexTree(index, graphDb().getReferenceNode());
    }
	
}