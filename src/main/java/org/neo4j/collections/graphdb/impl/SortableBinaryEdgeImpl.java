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
package org.neo4j.collections.graphdb.impl;

import org.neo4j.collections.graphdb.BinaryEdgeType;
import org.neo4j.collections.graphdb.DatabaseService;
import org.neo4j.collections.graphdb.EdgeType;
import org.neo4j.collections.graphdb.SortableBinaryEdge;
import org.neo4j.collections.graphdb.Vertex;
import org.neo4j.collections.graphdb.VertexType;
import org.neo4j.collections.indexedrelationship.IndexedRelationship;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Node;

public class SortableBinaryEdgeImpl<T> extends BinaryEdgeImpl implements SortableBinaryEdge<T>{

	SortableBinaryEdgeImpl(DatabaseService db, Long id, IndexedRelationship relIdx) {
		super(db, id);
		this.relIdx = relIdx;
	}

	private IndexedRelationship relIdx;
	
	
	public IndexedRelationship getRelIdx(){
		return relIdx;
	}
	
	public Node getRootNode(){
		return relIdx.getIndexedNode();
	}
	
	
	@Override
	public Vertex getEndVertex() {
		return getDb().getVertex(getRelationship().getEndNode());
	}

	@Override
	public Vertex getOtherVertex(Vertex element) {
		return getDb().getVertex(getRelationship().getOtherNode(element.getNode()));
	}

	@Override
	public Vertex getStartVertex() {
		return getDb().getVertex(getRootNode());
	}

	@Override
	public void delete() {
		getRelIdx().removeRelationshipTo(getEndVertex().getNode());
	}

	@Override
	protected VertexType getSpecialVertexType(){
		return getType();
	}
	
	@Override
	public BinaryEdgeType getType() {
		return SortableBinaryEdgeTypeImpl.getOrCreateInstance(getDb(), getRelationship().getType());
	}

	@Override
	public boolean isType(EdgeType relType) {
		return getRelationship().isType(DynamicRelationshipType.withName(relType.getName()));
	}

	@Override
	public org.neo4j.graphdb.PropertyContainer getPropertyContainer() {
		return getNode();
	}
}
