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

import org.neo4j.collections.graphdb.Index;
import org.neo4j.collections.graphdb.IndexHits;
import org.neo4j.collections.graphdb.PropertyContainer;

public class IndexImpl<T extends PropertyContainer, U extends org.neo4j.graphdb.PropertyContainer> implements Index<T>{

	final private org.neo4j.graphdb.index.Index<U> index;
	
	IndexImpl(org.neo4j.graphdb.index.Index<U> index){
		this.index = index;
	}

	@Override
	public String getName() {
		return index.getName();
	}

	@Override
	public IndexHits<T> get(String key, Object value) {
		return new IndexHitsImpl<T, U>(this.index.get(key, value));
	}

	@Override
	public IndexHits<T> query(String key, Object queryOrQueryObject) {
		return new IndexHitsImpl<T, U>(this.index.query(key, queryOrQueryObject));
	}

	@Override
	public IndexHits<T> query(Object queryOrQueryObject) {
		return new IndexHitsImpl<T, U>(this.index.query(queryOrQueryObject));
	}

	@SuppressWarnings("unchecked")
	@Override
	public void add(T entity, String key, Object value) {
		this.index.add((U)entity.getPropertyContainer(), key, value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void remove(T entity, String key, Object value) {
		this.index.remove((U)entity.getPropertyContainer(), key, value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void remove(T entity, String key) {
		this.index.remove((U)entity.getPropertyContainer(), key);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void remove(T entity) {
		this.index.remove((U)entity.getPropertyContainer());
	}

	@Override
	public void delete() {
		this.index.delete();
	}

	@Override
	public org.neo4j.graphdb.index.Index<?> getIndex() {
		return this.index;
	}
	
}
