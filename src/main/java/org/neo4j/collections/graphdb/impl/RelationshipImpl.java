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

import java.util.ArrayList;

import org.neo4j.collections.graphdb.Element;
import org.neo4j.collections.graphdb.FunctionalRelationshipElement;
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
		if(rel == null){
			throw new RuntimeException("Cannot instantiate RelationshipImpl with null");
		}
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
		return getGraphDatabase().getRelationshipType(rel.getType());
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
		ArrayList<RelationshipElement<? extends Element>> relements = new ArrayList<RelationshipElement<? extends Element>>();
		relements.add(new FunctionalRelationshipElement<Element>(getGraphDatabase().getStartElementRole(), getGraphDatabase().getElement(rel.getStartNode())));
		relements.add(new FunctionalRelationshipElement<Element>(getGraphDatabase().getEndElementRole(), getGraphDatabase().getElement(rel.getStartNode())));
		return relements;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Element> Iterable<T> getElements(RelationshipRole<T> role) {
		ArrayList<Element> elements = new ArrayList<Element>();
		if(role.getName().equals(getGraphDatabase().getStartElementRole())){
			elements.add(getGraphDatabase().getElement(rel.getStartNode()));
			return (ArrayList<T>)elements;
		}else if(role.getName().equals(getGraphDatabase().getEndElementRole())){
			elements.add(getGraphDatabase().getElement(rel.getEndNode()));
			return (ArrayList<T>)elements;
		}else{
			throw new RuntimeException("Supplied role is not supported");
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Element> T getElement(FunctionalRelationshipRole<T> role) {
		if(role.getName().equals(getGraphDatabase().getStartElementRole())){
			return (T) getGraphDatabase().getElement(rel.getStartNode());
		}else if(role.getName().equals(getGraphDatabase().getEndElementRole())){
			return (T) getGraphDatabase().getElement(rel.getEndNode());
		}else{
			throw new RuntimeException("Supplied role is not supported");
		}
	}
	
	
	@Override
	public Element getEndElement() {
		return getGraphDatabase().getElement(rel.getEndNode());
	}

	@Override
	public Element getOtherElement(Element element) {
		return getGraphDatabase().getElement(rel.getOtherNode(element.getNode()));
	}

	@Override
	public Element getStartElement() {
		return getGraphDatabase().getElement(rel.getStartNode());
	}

	@Override
	public Iterable<RelationshipElement<? extends Element>> getRelationshipElements(
			RelationshipRole<?>... roles) {
		boolean includeStart = false;
		boolean includeEnd = false;
		for(RelationshipRole<?> role: roles){
			if(role.getName().equals(getGraphDatabase().getStartElementRole())){
				includeStart = true;
			}else if(role.getName().equals(getGraphDatabase().getEndElementRole())){
				includeEnd = true;
			}else{
				throw new RuntimeException("Supplied role is not part of this RelationshipType");
			}
		}
		ArrayList<RelationshipElement<? extends Element>> relements = new ArrayList<RelationshipElement<? extends Element>>();
		if(includeStart){
			relements.add(new FunctionalRelationshipElement<Element>(getGraphDatabase().getStartElementRole(), getGraphDatabase().getElement(rel.getStartNode())));
		}
		if(includeEnd){
			relements.add(new FunctionalRelationshipElement<Element>(getGraphDatabase().getEndElementRole(), getGraphDatabase().getElement(rel.getStartNode())));
		}
		return relements;
	}
}
