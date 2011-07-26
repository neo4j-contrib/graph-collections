
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
import java.util.NoSuchElementException;

import org.neo4j.collections.graphdb.Node;
import org.neo4j.collections.graphdb.Element;
import org.neo4j.collections.graphdb.FunctionalRelationshipRole;
import org.neo4j.collections.graphdb.GraphDatabaseService;
import org.neo4j.collections.graphdb.HyperRelationship;
import org.neo4j.collections.graphdb.HyperRelationshipType;
import org.neo4j.collections.graphdb.RelationshipElement;
import org.neo4j.collections.graphdb.RelationshipRole;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicRelationshipType;
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

	private class RelationshipElementIterator implements Iterator<RelationshipElement<? extends Element>>{

		private final RelationshipRole<? extends Element>[] roles;
		private int index = 0;
		
		
		RelationshipElementIterator(
				RelationshipRole<? extends Element>[] roles) {
			this.roles = roles;
		}

		@Override
		public boolean hasNext() {
			return index < roles.length;
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public RelationshipElement<? extends Element> next() {
			if(hasNext()){
				String roleName = relType.name().substring(relType.name().indexOf("/#/")+3);
				RelationshipRole<? extends Element> role = getGraphDatabase().getRelationshipRole(roleName);
				Iterable<? extends Element> elems = new ElementIterable(role);
				return new RelationshipElement(role, elems);
			}else{
				throw new NoSuchElementException();
			}
		}

		@Override
		public void remove() {
		}
	}

	private class ElementIterator<T extends Element> implements Iterator<T>{

		private final Iterator<org.neo4j.graphdb.Relationship> rels;
		
		public ElementIterator(RelationshipRole<T> role) {
			this.rels = getNode().getRelationships(DynamicRelationshipType.withName(relType.name()+"/#/"+role.getName()), Direction.OUTGOING).iterator();
		}

		@Override
		public boolean hasNext() {
			return rels.hasNext();
		}

		@SuppressWarnings("unchecked")
		@Override
		public T next() {
			return (T)getGraphDatabase().getElement(rels.next().getEndNode());
		}

		@Override
		public void remove() {
		}
	}

	private class ElementIterable<T extends Element> implements Iterable<T>{

		private final RelationshipRole<T> role;
		
		
		public ElementIterable(RelationshipRole<T> role) {
			this.role = role;
		}

		@Override
		public Iterator<T> iterator() {
			return new ElementIterator<T>(role);
		}
	}
	
	private class RelationshipElementIterable implements Iterable<RelationshipElement<? extends Element>>{

		private final RelationshipRole<? extends Element>[] roles;
		
		public RelationshipElementIterable(
				RelationshipRole<? extends Element>[] roles) {
			this.roles = roles;
		}

		public RelationshipElementIterable() {
			this.roles = relType.getRoles();
		}
		
		@Override
		public Iterator<RelationshipElement<? extends Element>> iterator() {
			return new RelationshipElementIterator(roles);
		}
		
	}
	
	@Override
	public Iterable<RelationshipElement<? extends Element>> getRelationshipElements() {
		return new RelationshipElementIterable();
	}

	@Override
	public Iterable<RelationshipElement<? extends Element>> getRelationshipElements(RelationshipRole<?>... roles) {
		return new RelationshipElementIterable(roles);
	}

	
	@Override
	public <T extends Element> Iterable<T> getElements(RelationshipRole<T> role) {
		return new ElementIterable<T>(role);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Element> T getElement(FunctionalRelationshipRole<T> role) {
		return (T)getGraphDatabase().getElement(getNode().getSingleRelationship(DynamicRelationshipType.withName(relType.name()+"/#/"+role.getName()), Direction.OUTGOING).getEndNode());
	}

}
