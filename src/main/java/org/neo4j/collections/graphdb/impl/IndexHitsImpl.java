/**
 * Copyright (c) 2002-2011 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.collections.graphdb.impl;

import java.util.Iterator;

import org.neo4j.collections.graphdb.wrappers.IndexHits;

public class IndexHitsImpl<T, U> implements IndexHits<T>{
	
	private final org.neo4j.graphdb.index.IndexHits<U> indexHits;

	IndexHitsImpl(org.neo4j.graphdb.index.IndexHits<U> indexHits){
		this.indexHits = indexHits;
	}
	
	@Override
	public boolean hasNext() {
		return indexHits.hasNext();
	}

	@SuppressWarnings("unchecked")
	@Override
	public T next() {
		U indexHit = indexHits.next();
		if(indexHit instanceof org.neo4j.graphdb.Node){
			return (T)new NodeImpl((org.neo4j.graphdb.Node)indexHit);
		}else{
			return (T)new RelationshipImpl((org.neo4j.graphdb.Relationship)indexHit);
		}
	}

	@Override
	public void remove() {
		indexHits.remove();
	}

	@Override
	public Iterator<T> iterator() {
		return this;
	}

	@Override
	public int size() {
		return indexHits.size();
	}

	@Override
	public void close() {
		indexHits.close();
	}

	@SuppressWarnings("unchecked")
	@Override
	public T getSingle() {
		U indexHit = indexHits.getSingle();
		if(indexHit instanceof org.neo4j.graphdb.Node){
			return (T)new NodeImpl((org.neo4j.graphdb.Node)indexHit);
		}else{
			return (T)new RelationshipImpl((org.neo4j.graphdb.Relationship)indexHit);
		}
	}

	@Override
	public float currentScore() {
		return indexHits.currentScore();
	}

}
