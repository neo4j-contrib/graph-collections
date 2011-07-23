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
import org.neo4j.collections.graphdb.TraversalPosition;

public class TraversalPositionImpl implements TraversalPosition {

	private final org.neo4j.graphdb.TraversalPosition traversalPosition;
	
	public TraversalPositionImpl(
			org.neo4j.graphdb.TraversalPosition traversalPosition) {
		this.traversalPosition = traversalPosition;
	}

	@Override
	public Node currentNode() {
		return new NodeImpl(this.traversalPosition.currentNode());
	}

	@Override
	public Node previousNode() {
		return new NodeImpl(this.traversalPosition.previousNode());
	}

	@Override
	public Relationship lastRelationshipTraversed() {
		return new RelationshipImpl(this.traversalPosition.lastRelationshipTraversed());
	}

	@Override
	public int depth() {
		return this.traversalPosition.depth();
	}

	@Override
	public int returnedNodesCount() {
		return this.traversalPosition.returnedNodesCount();
	}

	@Override
	public boolean notStartNode() {
		return this.traversalPosition.notStartNode();
	}

	@Override
	public boolean isStartNode() {
		return this.traversalPosition.isStartNode();
	}

}
