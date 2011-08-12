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
package org.neo4j.collections.graphdb.impl;

import java.util.ArrayList;

import org.neo4j.collections.graphdb.BinaryEdgeType;
import org.neo4j.collections.graphdb.EdgeType;
import org.neo4j.collections.graphdb.Vertex;
import org.neo4j.collections.graphdb.LeftRestricedEdgeElement;
import org.neo4j.collections.graphdb.VertexType;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Node;
import org.neo4j.collections.graphdb.EdgeElement;
import org.neo4j.collections.graphdb.SortableBinaryEdge;
import org.neo4j.graphdb.Relationship;
import org.neo4j.collections.indexedrelationship.IndexedRelationship;

public class SortableBinaryEdgeImpl<T> extends BinaryEdgeImpl implements SortableBinaryEdge<T>{

	
	SortableBinaryEdgeImpl(Relationship rel, IndexedRelationship relIdx) {
		super(rel);
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
		return getDb().getVertex(rel.getEndNode());
	}

	@Override
	public Vertex getOtherVertex(Vertex element) {
		return getDb().getVertex(rel.getOtherNode(element.getNode()));
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
		return SortableBinaryEdgeTypeImpl.getOrCreateInstance(getDb(), rel.getType());
	}

	@Override
	public boolean isType(EdgeType relType) {
		return rel.isType(DynamicRelationshipType.withName(relType.getName()));
	}

	@Override
	public Iterable<EdgeElement> getEdgeElements(){
		ArrayList<EdgeElement> relements = new ArrayList<EdgeElement>();
		relements.add(new LeftRestricedEdgeElement(getType().getStartConnectorType(), getDb().getVertex(rel.getStartNode())));
		relements.add(new LeftRestricedEdgeElement(getType().getEndConnectorType(), getDb().getVertex(rel.getEndNode())));
		return relements;
	}

	@Override
	public org.neo4j.graphdb.PropertyContainer getPropertyContainer() {
		return getNode();
	}
}
