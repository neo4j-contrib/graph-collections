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

import java.util.Collection;
import java.util.Iterator;

import org.neo4j.collections.graphdb.Node;
import org.neo4j.collections.graphdb.wrappers.TraversalPosition;
import org.neo4j.collections.graphdb.wrappers.Traverser;

/**
 * @author Niels
 *
 */
public class TraverserImpl implements Traverser {

	private final org.neo4j.graphdb.Traverser traverser;
	
	public TraverserImpl(org.neo4j.graphdb.Traverser traverser) {
		this.traverser = traverser;
	}

	/* (non-Javadoc)
	 * @see org.neo4j.collections.graphdb.Traverser#currentPosition()
	 */
	@Override
	public TraversalPosition currentPosition() {
		return new TraversalPositionImpl(this.traverser.currentPosition());
	}

	/* (non-Javadoc)
	 * @see org.neo4j.collections.graphdb.Traverser#getAllNodes()
	 */
	@Override
	public Collection<Node> getAllNodes() {
		return new NodeCollection(this.traverser.getAllNodes());
	}

	/* (non-Javadoc)
	 * @see org.neo4j.collections.graphdb.Traverser#iterator()
	 */
	@Override
	public Iterator<Node> iterator() {
		return new NodeIterator(this.traverser.iterator());
	}

}
