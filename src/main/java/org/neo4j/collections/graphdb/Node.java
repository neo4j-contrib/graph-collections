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
package org.neo4j.collections.graphdb;

import org.neo4j.collections.graphdb.wrappers.Path;
import org.neo4j.collections.graphdb.wrappers.Traverser;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.ReturnableEvaluator;
import org.neo4j.graphdb.StopEvaluator;
import org.neo4j.graphdb.Traverser.Order;

public interface Node extends Element, Path{

	public org.neo4j.graphdb.Node getNode();
	
	public long getId();
	
	public void delete();
	
	public Traverser traverse(Order order, StopEvaluator stopEvaluator,
			ReturnableEvaluator returnableEvaluator, Object... relTypesAndDirections);
	
	public Traverser traverse(Order order, StopEvaluator stopEvaluator,
			ReturnableEvaluator returnableEvaluator, RelationshipType relType, Direction dir);

	public Traverser traverse(Order order, StopEvaluator stopEvaluator,
			ReturnableEvaluator returnableEvaluator, RelationshipType relType1, Direction dir1,
			RelationshipType relType2, Direction dir2);	
	
}
