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
package org.neo4j.collections.graphdb.traversal.impl;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.neo4j.collections.graphdb.Traversal;
import org.neo4j.collections.graphdb.TraversalDescription;
import org.neo4j.collections.graphdb.TraversalPath;

public class TraversalImpl implements Traversal{

	private final Traversal traversal;
	private final TraversalDescriptionImpl description;
	
	public TraversalImpl(Traversal traversal, TraversalDescriptionImpl description) {
		this.traversal = traversal;
		this.description = description;
	}

	private class PathIterator implements Iterator<TraversalPath>{

		DescriptionIterator iter = new DescriptionIterator();
		
		@Override
		public boolean hasNext() {
			return iter.hasNext();
		}

		@Override
		public TraversalPath next() {
			return concatenatePaths(getPath(), iter.next().getPath());
		}

		@Override
		public void remove() {
		}
		
	}
	
	private class DescriptionIterator implements Iterator<Traversal>{

		final Iterator<TraversalDescription> iter = description.descriptions.iterator();
		
		Boolean hasNext = null;
		
		@Override
		public boolean hasNext() {
			if(hasNext == null){
				hasNext = iter.hasNext();
				
			}
			return hasNext;
		}

		@Override
		public Traversal next() {
			if(hasNext == null){
				throw new NoSuchElementException();
			}else{
				if(hasNext()){
					return iter.next().traverse(traversal);
				}else{
					throw new NoSuchElementException();
				}
			}
		}

		@Override
		public void remove() {
		}
		
	}
	
	@Override
	public Iterator<Traversal> iterator() {
		return new DescriptionIterator();
	}

	@Override
	public TraversalPath getPath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterable<TraversalPath> getContainedPaths() {

		return new Iterable<TraversalPath>(){

			@Override
			public Iterator<TraversalPath> iterator() {
				return new PathIterator();
			}
			
		};
	}

	private TraversalPath concatenatePaths(TraversalPath path1, TraversalPath path2){
		//TODO
		return null;
	}
	
}
