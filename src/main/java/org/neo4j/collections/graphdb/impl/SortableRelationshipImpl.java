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
import org.neo4j.collections.graphdb.GraphDatabaseService;
import org.neo4j.collections.graphdb.HyperRelationshipType;
import org.neo4j.collections.graphdb.Node;
import org.neo4j.collections.graphdb.RelationshipElement;
import org.neo4j.collections.graphdb.RelationshipRole;
import org.neo4j.collections.graphdb.SortableRelationship;
import org.neo4j.collections.graphdb.Relationship;
import org.neo4j.collections.indexedrelationship.IndexedRelationship;
import org.neo4j.graphdb.RelationshipType;

public class SortableRelationshipImpl<T> extends ElementImpl implements SortableRelationship<T>{

	private final Node rootNode;
	private final Relationship endRel;
	private final IndexedRelationship relIdx;
	
	
	SortableRelationshipImpl(Relationship endRel, IndexedRelationship relIdx){
		this.endRel = endRel;
		this.rootNode = new NodeImpl(relIdx.getIndexedNode());
		this.relIdx = relIdx;
	}
	
	@Override
	public Node getEndNode() {
		return endRel.getEndNode();
	}

	@Override
	public Element getEndElement() {
		return endRel.getEndElement();
	}

	@Override
	public Node getOtherNode(Node node) {
		return endRel.getOtherNode(node);
	}

	@Override
	public Element getOtherElement(Element element) {
		return endRel.getOtherElement(element);
	}

	@Override
	public Node getStartNode() {
		return rootNode;
	}

	@Override
	public Element getStartElement() {
		return rootNode;
	}

	@Override
	public Node[] getNodes() {
		Node[] nodes = new Node[2];
		nodes[0] = rootNode;
		nodes[1] = endRel.getEndNode();
		return nodes;
	}

	@Override
	public long getId() {
		return getNode().getId();
	}

	@Override
	public void delete() {
		relIdx.removeRelationshipTo(getEndNode().getNode());
	}

	@Override
	public HyperRelationshipType getType() {
		return getGraphDatabase().getRelationshipType(relIdx.getRelationshipType());
	}

	@Override
	public boolean isType(RelationshipType relType) {
		return (relType.name().equals(getType().name()));
	}

	@Override
	public Iterable<RelationshipElement<? extends Element>> getRelationshipElements() {
		ArrayList<RelationshipElement<? extends Element>> relements = new ArrayList<RelationshipElement<? extends Element>>();
		relements.add(new FunctionalRelationshipElement<Element>(getGraphDatabase().getStartElementRole(), getGraphDatabase().getElement(getStartNode().getNode())));
		relements.add(new FunctionalRelationshipElement<Element>(getGraphDatabase().getEndElementRole(), getGraphDatabase().getElement(getEndNode().getNode())));
		return relements;
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
			relements.add(new FunctionalRelationshipElement<Element>(getGraphDatabase().getStartElementRole(), getGraphDatabase().getElement(getStartNode().getNode())));
		}
		if(includeEnd){
			relements.add(new FunctionalRelationshipElement<Element>(getGraphDatabase().getEndElementRole(), getGraphDatabase().getElement(getStartNode().getNode())));
		}
		return relements;
	}

	@SuppressWarnings({ "unchecked"})
	@Override
	public <U extends Element> Iterable<U> getElements(RelationshipRole<U> role) {
		ArrayList<Element> elements = new ArrayList<Element>();
		if(role.getName().equals(getGraphDatabase().getStartElementRole())){
			elements.add(getGraphDatabase().getElement(getStartNode().getNode()));
			return (ArrayList<U>)elements;
		}else if(role.getName().equals(getGraphDatabase().getEndElementRole())){
			elements.add(getGraphDatabase().getElement(getEndNode().getNode()));
			return (ArrayList<U>)elements;
		}else{
			throw new RuntimeException("Supplied role is not supported");
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <U extends Element> U getElement(FunctionalRelationshipRole<U> role) {
		if(role.getName().equals(getGraphDatabase().getStartElementRole().getName())){
			return (U) getGraphDatabase().getElement(getStartNode().getNode());
		}else if(role.getName().equals(getGraphDatabase().getEndElementRole().getName())){
			return (U) getGraphDatabase().getElement(getEndNode().getNode());
		}else{
			throw new RuntimeException("Supplied role is not supported");
		}
	}

	@Override
	public org.neo4j.graphdb.PropertyContainer getPropertyContainer() {
		return getNode();
	}

	@Override
	public GraphDatabaseService getGraphDatabase() {
		return rootNode.getGraphDatabase();
	}

	@Override
	public org.neo4j.graphdb.Node getNode() {
		return endRel.getNode();
	}

}
