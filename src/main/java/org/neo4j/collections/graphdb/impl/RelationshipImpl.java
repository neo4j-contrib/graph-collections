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

import org.neo4j.collections.graphdb.Element;
import org.neo4j.collections.graphdb.FunctionalRelationshipRole;
import org.neo4j.collections.graphdb.HyperRelationshipType;
import org.neo4j.collections.graphdb.GraphDatabaseService;
import org.neo4j.collections.graphdb.Node;
import org.neo4j.collections.graphdb.Relationship;
import org.neo4j.collections.graphdb.RelationshipElement;
import org.neo4j.collections.graphdb.RelationshipRole;

import org.neo4j.graphdb.RelationshipType;

public class RelationshipImpl extends ElementImpl implements Relationship{

	public static String NODE_ID = "org.neo4j.collections.graphdb.node_id";
	public static String REL_ID = "org.neo4j.collections.graphdb.rel_id";
	
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
	public Node getEndNode() {
		return new NodeImpl(rel.getEndNode());
	}

	@Override
	public long getId() {
		return rel.getId();
	}

	@Override
	public Node[] getNodes() {
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
	public Node getOtherNode(Node node) {
		return new NodeImpl(rel.getOtherNode(node.getNode()));
	}

	@Override
	public Node getStartNode() {
		return new NodeImpl(rel.getStartNode());
	}

	@Override
	public HyperRelationshipType getType() {
		return new RelationshipTypeImpl(rel.getType(), getGraphDatabase());
	}

	@Override
	public boolean isType(RelationshipType relType) {
		return rel.isType(relType);
	}

	@Override
	public GraphDatabaseService getGraphDatabase() {
		return new GraphDatabaseImpl(rel.getGraphDatabase());
	}

	@Override
	public org.neo4j.graphdb.Node getNode() {
		if(node == null){
			Node n = getGraphDatabase().createNode();
			n.setProperty(REL_ID, rel.getId());
			rel.setProperty(NODE_ID, n.getId());
			return n.getNode();
		}else{
			return node.getNode();
		}
	}

	@Override
	public org.neo4j.graphdb.PropertyContainer getPropertyContainer() {
		return rel;
	}

	@Override
	public Iterable<RelationshipElement<? extends Element>> getRelationshipElements(){
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
	
	
	@Override
	public Element getEndElement() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Element getOtherElement(Element element) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Element getStartElement() {
		// TODO Auto-generated method stub
		return null;
	}
}
