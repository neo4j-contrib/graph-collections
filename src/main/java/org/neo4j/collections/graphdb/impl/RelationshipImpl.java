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

import org.neo4j.collections.graphdb.EnhancedRelationshipType;
import org.neo4j.collections.graphdb.GraphDatabaseService;
import org.neo4j.collections.graphdb.Node;
import org.neo4j.collections.graphdb.Relationship;
import org.neo4j.collections.graphdb.RelationshipContainer;

import org.neo4j.graphdb.RelationshipType;

public class RelationshipImpl extends NodeLikeImpl implements Relationship{

	public static String NODE_ID = "org.neo4j.collections.graphdb.node_id";
	
	final org.neo4j.graphdb.Relationship rel;
	Node node = null;
	
	RelationshipImpl(org.neo4j.graphdb.Relationship rel){
		this.rel = rel;
	}
	
	@Override
	public void delete() {
		rel.delete();
		if(node != null){
			node.delete();
		}
	}
	@Override
	public org.neo4j.graphdb.Relationship getRelationship() {
		return rel;
	}
	
	@Override
	public RelationshipContainer getEndRelationshipContainer() {
		return new NodeImpl(rel.getEndNode());
	}

	@Override
	public long getId() {
		return rel.getId();
	}

	@Override
	public RelationshipContainer[] getRelationshipContainers() {
		org.neo4j.graphdb.Node[] nodes = rel.getNodes();
		Node[] enodes = new Node[nodes.length];
		int count = 0;
		for(org.neo4j.graphdb.Node n: nodes){
			enodes[count] = new NodeImpl(n);
			count++;
		}
		return enodes;
	}

	@Override
	public RelationshipContainer getOtherRelationshipContainer(RelationshipContainer relCont) {
		return new NodeImpl(rel.getOtherNode(relCont.getNode()));
	}

	@Override
	public RelationshipContainer getStartRelationshipContainer() {
		return new NodeImpl(rel.getStartNode());
	}

	@Override
	public EnhancedRelationshipType getType() {
		return new RelationshipTypeImpl(rel.getType(), getGraphDatabaseExt());
	}

	@Override
	public boolean isType(RelationshipType relType) {
		return rel.isType(relType);
	}

	@Override
	public GraphDatabaseService getGraphDatabaseExt() {
		return new GraphDatabaseImpl(rel.getGraphDatabase());
	}

	@Override
	public Object getProperty(String key) {
		return rel.getProperty(key);
	}

	@Override
	public Object getProperty(String key, Object value) {
		return rel.getProperty(key, value);
	}

	@Override
	public Iterable<String> getPropertyKeys() {
		return rel.getPropertyKeys();
	}

	@Deprecated
	public Iterable<Object> getPropertyValues() {
		return rel.getPropertyValues();
	}

	@Override
	public boolean hasProperty(String key) {
		return rel.hasProperty(key);
	}

	@Override
	public Object removeProperty(String key) {
		if(key.equals(NODE_ID)){
			throw new RuntimeException("Cannot remove node ID property of a relationship");
		}
		return rel.removeProperty(key);
	}

	@Override
	public void setProperty(String key, Object value) {
		if(key.equals(NODE_ID)){
			throw new RuntimeException("Cannot set node ID property of a relationship");
		}
		rel.setProperty(key, value);
	}

	@Override
	public org.neo4j.graphdb.Node getNode() {
		if(node == null){
			Node n = getGraphDatabaseExt().createNodeExt();
			rel.setProperty(NODE_ID, n.getId());
			return n.getNode();
		}else{
			return node;
		}
	}

	@Override
	public org.neo4j.graphdb.PropertyContainer getPropertyContainer() {
		return rel;
	}
	
	@Override
	public org.neo4j.graphdb.Node getOtherNode(org.neo4j.graphdb.Node node) {
		return rel.getOtherNode(node);
	}

	@Override
	public org.neo4j.graphdb.Node getEndNode() {
		return rel.getEndNode();
	}

	@Override
	public org.neo4j.graphdb.Node[] getNodes() {
		return rel.getNodes();
	}

	@Override
	public org.neo4j.graphdb.Node getStartNode() {
		return rel.getStartNode();
	}

	@Override
	public org.neo4j.graphdb.GraphDatabaseService getGraphDatabase() {
		return rel.getGraphDatabase();
	}
}
