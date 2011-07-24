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

import java.util.Map;

import org.neo4j.collections.graphdb.Node;
import org.neo4j.collections.graphdb.PropertyContainer;
import org.neo4j.collections.graphdb.wrappers.AutoIndexer;
import org.neo4j.collections.graphdb.wrappers.Index;
import org.neo4j.collections.graphdb.wrappers.IndexManager;
import org.neo4j.collections.graphdb.wrappers.RelationshipAutoIndexer;
import org.neo4j.collections.graphdb.wrappers.RelationshipIndex;

public class IndexManagerImpl implements IndexManager{
	
	private final org.neo4j.graphdb.index.IndexManager indexManager;

	public IndexManagerImpl(org.neo4j.graphdb.index.IndexManager indexManager){
		this.indexManager = indexManager;
	}

	@Override
	public boolean existsForNodes(String indexName) {
		return indexManager.existsForNodes(indexName);
	}

	@Override
	public Index<Node> forNodes(String indexName) {
		return new IndexImpl<Node, org.neo4j.graphdb.Node>(indexManager.forNodes(indexName));
	}

	@Override
	public Index<Node> forNodes(String indexName,
			Map<String, String> customConfiguration) {
		return new IndexImpl<Node, org.neo4j.graphdb.Node>(indexManager.forNodes(indexName, customConfiguration));
	}

	@Override
	public String[] nodeIndexNames() {
		return this.indexManager.nodeIndexNames();
	}

	@Override
	public boolean existsForRelationships(String indexName) {
		return this.indexManager.existsForRelationships(indexName);
	}

	@Override
	public RelationshipIndex forRelationships(String indexName) {
		return new RelationshipIndexImpl(this.indexManager.forRelationships(indexName));
	}

	@Override
	public RelationshipIndex forRelationships(String indexName,
			Map<String, String> customConfiguration) {
		return new RelationshipIndexImpl(this.indexManager.forRelationships(indexName, customConfiguration));
	}

	@Override
	public String[] relationshipIndexNames() {
		return this.indexManager.relationshipIndexNames();
	}

	@Override
	public Map<String, String> getConfiguration(
			Index<? extends PropertyContainer> index) {
		return this.indexManager.getConfiguration(index.getIndex());
	}

	@Override
	public String setConfiguration(Index<? extends PropertyContainer> index,
			String key, String value) {
		return this.indexManager.setConfiguration(index.getIndex(), key, value);
	}

	@Override
	public String removeConfiguration(Index<? extends PropertyContainer> index,
			String key) {
		return this.indexManager.removeConfiguration(index.getIndex(), key);
	}

	@Override
	public AutoIndexer<Node> getNodeAutoIndexer() {
		return new AutoIndexerImpl<Node, org.neo4j.graphdb.Node>(this.indexManager.getNodeAutoIndexer());
	}

	@Override
	public RelationshipAutoIndexer getRelationshipAutoIndexer() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
