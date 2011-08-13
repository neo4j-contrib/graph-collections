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

import java.util.Iterator;

import org.neo4j.collections.graphdb.BinaryEdge;
import org.neo4j.graphdb.Relationship;

class RelationshipIterator implements Iterator<BinaryEdge>{

	private final Iterator<Relationship> rels;
	
	RelationshipIterator(Iterator<Relationship> rels){
		this.rels = rels;
	}
	
	@Override
	public boolean hasNext() {
		return rels.hasNext();
	}

	@Override
	public BinaryEdge next() {
		Relationship rel = rels.next();
		return new BinaryEdgeImpl(new GraphDatabaseImpl(rel.getGraphDatabase()),rel.getId());
	}

	@Override
	public void remove() {
		rels.remove();
	}
	
}
