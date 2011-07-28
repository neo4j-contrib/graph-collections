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

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.RelationshipType;

public interface RelationshipContainer{

	public Relationship createRelationshipTo(RelationshipContainer n, RelationshipType rt);
	
	public <T> SortableRelationship<T> createRelationshipTo(RelationshipContainer n, SortableRelationshipType<T> rt);
	
	public Iterable<HyperRelationship> getRelationships();
	
	public Iterable<HyperRelationship> getRelationships(RelationshipType... relTypes);

	public Iterable<Relationship> getRelationships(Direction dir);

	public Iterable<Relationship> getRelationships(Direction dir,
			RelationshipType... relTypes);
	
	public Iterable<Relationship> getRelationships(RelationshipType relType,
			Direction dir);

	public Iterable<HyperRelationship> getRelationships(RelationshipRole<? extends Element> role, RelationshipType... relTypes);
	
	public Relationship getSingleRelationship(RelationshipType relType,
			Direction dir);

	public HyperRelationship getSingleRelationship(RelationshipRole<? extends Element> role, RelationshipType relType);
	
	public boolean hasRelationship();

	public boolean hasRelationship(RelationshipType... relTypes);
	
	public boolean hasRelationship(RelationshipRole<? extends Element> role, RelationshipType... relTypes);
	
	public boolean hasRelationship(Direction dir);

	public boolean hasRelationship(Direction dir, RelationshipType... relTypes);

	public boolean hasRelationship(RelationshipType relTypes, Direction dir);

	abstract org.neo4j.graphdb.Node getNode();
}
