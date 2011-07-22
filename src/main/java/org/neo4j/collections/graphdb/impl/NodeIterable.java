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

import org.neo4j.collections.graphdb.Node;

class NodeIterable implements Iterable<Node>{

	private Iterable<org.neo4j.graphdb.Node> rels;
	
	NodeIterable(Iterable<org.neo4j.graphdb.Node> rels){
		this.rels = rels;
	}
	
	@Override
	public Iterator<Node> iterator() {
		return new NodeIterator(rels.iterator());
	}
	
}
