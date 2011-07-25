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
import org.neo4j.collections.graphdb.Element;
import org.neo4j.collections.graphdb.FunctionalRelationshipRole;
import org.neo4j.collections.graphdb.GraphDatabaseService;
import org.neo4j.collections.graphdb.HyperRelationship;
import org.neo4j.collections.graphdb.HyperRelationshipType;
import org.neo4j.collections.graphdb.RelationshipElement;
import org.neo4j.collections.graphdb.RelationshipRole;

import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.RelationshipType;

public class HyperRelationshipImpl extends ElementImpl implements HyperRelationship{

	private final Node node;
	private final HyperRelationshipType relType;
	
	HyperRelationshipImpl(Node node, HyperRelationshipType relType){
		this.node = node;
		this.relType = relType;
	}
	
	@Override
	public PropertyContainer getPropertyContainer() {
		return getNode();
	}

	@Override
	public GraphDatabaseService getGraphDatabase() {
		return node.getGraphDatabase();
	}

	@Override
	public org.neo4j.graphdb.Node getNode() {
		return node.getNode();
	}

	@Override
	public long getId() {
		return getNode().getId();
	}

	@Override
	public void delete() {
		getNode().delete();
	}

	@Override
	public HyperRelationshipType getType() {
		return relType;
	}

	@Override
	public boolean isType(RelationshipType relType) {
		return (this.relType.name().equals(relType.name()));
	}

	@Override
	public Iterable<RelationshipElement<? extends Element>> getRelationshipElements() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends Element> Iterable<T> getElements(RelationshipRole<T> role) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends Element> T getElement(FunctionalRelationshipRole<T> role) {
		// TODO Auto-generated method stub
		return null;
	}

}
