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

import org.neo4j.collections.graphdb.Node;
import org.neo4j.collections.graphdb.Relationship;
import org.neo4j.collections.graphdb.wrappers.IndexHits;
import org.neo4j.collections.graphdb.wrappers.ReadableRelationshipIndex;
	
public class ReadableRelationshipIndexImpl implements ReadableRelationshipIndex {

	public ReadableRelationshipIndexImpl(
			org.neo4j.graphdb.index.ReadableRelationshipIndex index) {
		this.index = index;
	}

	private final org.neo4j.graphdb.index.ReadableRelationshipIndex index;
	
	@Override
	public String getName() {
		return index.getName();
	}

	@Override
	public IndexHits<Relationship> get(String key, Object value) {
		return new IndexHitsImpl<Relationship, org.neo4j.graphdb.Relationship>(index.get(key, value));
	}

	@Override
	public IndexHits<Relationship> query(String key, Object queryOrQueryObject) {
		return new IndexHitsImpl<Relationship, org.neo4j.graphdb.Relationship>(index.query(key, queryOrQueryObject));
	}

	@Override
	public IndexHits<Relationship> query(Object queryOrQueryObject) {
		return new IndexHitsImpl<Relationship, org.neo4j.graphdb.Relationship>(index.query(queryOrQueryObject));
	}

	@Override
	public IndexHits<Relationship> get(String key, Object valueOrNull,
			Node startNodeOrNull, Node endNodeOrNull) {
		org.neo4j.graphdb.Node startNode = (startNodeOrNull == null) ? null : startNodeOrNull.getNode();
		org.neo4j.graphdb.Node endNode = (startNodeOrNull == null) ? null : startNodeOrNull.getNode();
		return new IndexHitsImpl<Relationship, org.neo4j.graphdb.Relationship>(index.get(key, valueOrNull, startNode, endNode));
	}

	@Override
	public IndexHits<Relationship> query(String key,
			Object queryOrQueryObjectOrNull, Node startNodeOrNull,
			Node endNodeOrNull) {
		org.neo4j.graphdb.Node startNode = (startNodeOrNull == null) ? null : startNodeOrNull.getNode();
		org.neo4j.graphdb.Node endNode = (startNodeOrNull == null) ? null : startNodeOrNull.getNode();
		return new IndexHitsImpl<Relationship, org.neo4j.graphdb.Relationship>(index.query(key, queryOrQueryObjectOrNull, startNode, endNode));

	}

	@Override
	public IndexHits<Relationship> query(Object queryOrQueryObjectOrNull,
			Node startNodeOrNull, Node endNodeOrNull) {
		org.neo4j.graphdb.Node startNode = (startNodeOrNull == null) ? null : startNodeOrNull.getNode();
		org.neo4j.graphdb.Node endNode = (startNodeOrNull == null) ? null : startNodeOrNull.getNode();
		return new IndexHitsImpl<Relationship, org.neo4j.graphdb.Relationship>(index.query(queryOrQueryObjectOrNull, startNode, endNode));

	}

}
