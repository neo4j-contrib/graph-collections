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

import java.util.Set;

import org.neo4j.collections.graphdb.ReadableRelationshipIndex;
import org.neo4j.collections.graphdb.RelationshipAutoIndexer;

public class RelationshipAutoIndexerImpl implements RelationshipAutoIndexer{
	
	private final org.neo4j.graphdb.index.RelationshipAutoIndexer autoIndexer;

	public RelationshipAutoIndexerImpl(org.neo4j.graphdb.index.RelationshipAutoIndexer autoIndexer) {
		this.autoIndexer = autoIndexer;
	}

	@Override
	public Set<String> getAutoIndexedProperties() {
		return this.autoIndexer.getAutoIndexedProperties();
	}

	@Override
	public boolean isEnabled() {
		return this.autoIndexer.isEnabled();
	}

	@Override
	public void setEnabled(boolean enabled) {
		this.autoIndexer.setEnabled(enabled);
	}

	@Override
	public void startAutoIndexingProperty(String prop) {
		this.autoIndexer.startAutoIndexingProperty(prop);
		
	}

	@Override
	public void stopAutoIndexingProperty(String prop) {
		this.autoIndexer.stopAutoIndexingProperty(prop);
	}

	@Override
	public ReadableRelationshipIndex getAutoIndex() {
		return new ReadableRelationshipIndexImpl(this.autoIndexer.getAutoIndex());
	}

}
