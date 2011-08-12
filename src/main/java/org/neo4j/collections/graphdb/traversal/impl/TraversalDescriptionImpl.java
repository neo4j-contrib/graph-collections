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
package org.neo4j.collections.graphdb.traversal.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.neo4j.collections.graphdb.BinaryEdgeType;
import org.neo4j.collections.graphdb.Connector;
import org.neo4j.collections.graphdb.Traversal;
import org.neo4j.collections.graphdb.TraversalDescription;
import org.neo4j.collections.graphdb.traversal.BranchSelector;
import org.neo4j.collections.graphdb.traversal.Evaluator;
import org.neo4j.graphdb.Direction;

public class TraversalDescriptionImpl implements TraversalDescription{


	final Evaluator evaluator;
	final BranchSelector selector;
	final Connector<?>[] connectors;
	final List<TraversalDescription> descriptions;
	
	
	
	public TraversalDescriptionImpl(Evaluator evaluator,
			BranchSelector selector, Connector<?>[] connectors,
			List<TraversalDescription> descriptions) {
		super();
		this.evaluator = evaluator;
		this.selector = selector;
		this.connectors = connectors;
		this.descriptions = descriptions;
	}

	@Override
	public Iterator<TraversalDescription> iterator() {
		return descriptions.iterator();
	}

	@Override
	public TraversalDescription set(Evaluator evaluator) {
		return new TraversalDescriptionImpl(evaluator, selector, connectors, descriptions);
	}

	@Override
	public TraversalDescription set(BranchSelector selector) {
		return new TraversalDescriptionImpl(evaluator, selector, connectors, descriptions);	}

	@Override
	public TraversalDescription add(BinaryEdgeType edgeType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TraversalDescription add(BinaryEdgeType edgeType, Direction dir) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TraversalDescription add(Connector<?>[] connectors) {
		return new TraversalDescriptionImpl(evaluator, selector, connectors, descriptions);	}

	@Override
	public TraversalDescription add(TraversalDescription description) {
		List<TraversalDescription> descriptions = new ArrayList<TraversalDescription>();
		descriptions.addAll(this.descriptions);
		descriptions.add(description);
		return new TraversalDescriptionImpl(evaluator, selector, connectors, descriptions);	
	}

	@Override
	public TraversalDescription insert(TraversalDescription description,
			int position) {
		List<TraversalDescription> descriptions = new ArrayList<TraversalDescription>();
		descriptions.addAll(this.descriptions);
		descriptions.add(position, description);
		return new TraversalDescriptionImpl(evaluator, selector, connectors, descriptions);	
	}

	@Override
	public Traversal traverse(Traversal traversal) {
		return new TraversalImpl(traversal, this);
	}

}
