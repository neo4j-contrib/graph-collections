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

import org.neo4j.collections.rtree.filter.SearchFilter;
import org.neo4j.collections.rtree.filter.SearchResults;
import org.neo4j.collections.rtree.search.Search;
import org.neo4j.graphdb.Node;


public class SpatialIndexPerformanceProxy implements SpatialIndexReader {

    // Constructor

    public SpatialIndexPerformanceProxy(SpatialIndexReader spatialIndex) {
        this.spatialIndex = spatialIndex;
    }

    
    // Public methods

	public EnvelopeDecoder getEnvelopeDecoder() {
		return spatialIndex.getEnvelopeDecoder();
	}
    
    public Envelope getBoundingBox() {
        long start = System.currentTimeMillis();
        Envelope result = spatialIndex.getBoundingBox();
        long stop = System.currentTimeMillis();
        System.out.println("# exec time(getBoundingBox): " + (stop - start) + "ms");
        return result;
    }

    public boolean isEmpty() {
        long start = System.currentTimeMillis();    	
    	boolean result = spatialIndex.isEmpty();
    	long stop = System.currentTimeMillis();
        System.out.println("# exec time(isEmpty): " + (stop - start) + "ms");
        return result;    	
    }
    
    public int count() {
        long start = System.currentTimeMillis();
        int count = spatialIndex.count();
        long stop = System.currentTimeMillis();
        System.out.println("# exec time(count): " + (stop - start) + "ms");
        return count;
    }
    
	public boolean isNodeIndexed(Long nodeId) {
        long start = System.currentTimeMillis();
		boolean result = spatialIndex.isNodeIndexed(nodeId);
        long stop = System.currentTimeMillis();
        System.out.println("# exec time(isNodeIndexed(" + nodeId + ")): " + (stop - start) + "ms");		
		return result;
	}    
    
    public void executeSearch(Search search) {
        long start = System.currentTimeMillis();
        spatialIndex.executeSearch(search);
        long stop = System.currentTimeMillis();
        System.out.println("# exec time(executeSearch(" + search + ")): " + (stop - start) + "ms");
    }

    public Iterable<Node> getAllIndexedNodes() {
        long start = System.currentTimeMillis();    	
	    Iterable<Node> result = spatialIndex.getAllIndexedNodes();
        long stop = System.currentTimeMillis();
        System.out.println("# exec time(getAllIndexedNodes()): " + (stop - start) + "ms");	    
	    return result;
    }

    
    // Attributes

    private SpatialIndexReader spatialIndex;


	@Override
	public SearchResults searchIndex(SearchFilter filter) {
        long start = System.currentTimeMillis();    	
	    SearchResults result = spatialIndex.searchIndex(filter);
        long stop = System.currentTimeMillis();
        System.out.println("# exec time(getAllIndexedNodes()): " + (stop - start) + "ms");	    
	    return result;
	}
}