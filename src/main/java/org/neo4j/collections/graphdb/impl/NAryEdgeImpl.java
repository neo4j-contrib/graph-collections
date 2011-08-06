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

import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import org.neo4j.collections.graphdb.DatabaseService;
import org.neo4j.collections.graphdb.EdgeElement;
import org.neo4j.collections.graphdb.EdgeRole;
import org.neo4j.collections.graphdb.FunctionalEdgeRoleType;
import org.neo4j.collections.graphdb.NAryEdge;
import org.neo4j.collections.graphdb.NAryEdgeRole;
import org.neo4j.collections.graphdb.NAryEdgeRoleType;
import org.neo4j.collections.graphdb.NAryEdgeType;
import org.neo4j.collections.graphdb.Vertex;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;

public class NAryEdgeImpl extends EdgeImpl<NAryEdgeType, NAryEdgeRoleType> implements NAryEdge{

	private NAryEdgeType relType;
	
	NAryEdgeImpl(Node node) {
		super(node);
	}

	@Override
	public NAryEdgeType getType() {
		if(relType == null){
			relType = (NAryEdgeType)getDb().getVertex(getDb().getNodeById((Long)getNode().getProperty(GraphDatabaseImpl.EDGE_TYPE)));
		}
		return relType;
	}

	@Override
	public boolean isType(NAryEdgeType relType) {
		return (relType.getNode().getId() == getType().getNode().getId());
	}
	@Override
	public PropertyContainer getPropertyContainer() {
		return getNode();
	}

	@Override
	public DatabaseService getDb() {
		return new GraphDatabaseImpl(getNode().getGraphDatabase());
	}

	@Override
	public void delete() {
		getNode().delete();
	}

	private class RelationshipElementIterator implements Iterator<EdgeElement>{

		private final Set<EdgeRole<NAryEdgeType, NAryEdgeRoleType>> roles;
		private int index = 0;
		
		
		RelationshipElementIterator(
				Set<EdgeRole<NAryEdgeType, NAryEdgeRoleType>> roles) {
			this.roles = roles;
		}

		@Override
		public boolean hasNext() {
			return index < roles.size();
		}

		@Override
		public EdgeElement next() {
			if(hasNext()){
				String roleName = getType().getName().substring(getType().getName().indexOf(EDGEROLE_SEPARATOR)+3);
				NAryEdgeRole role = getType().getRole(roleName);
				Iterable<Vertex> elems = new ElementIterable(role.getEdgeRoleType());
				return new EdgeElement(role.getEdgeRoleType(), elems);
			}else{
				throw new NoSuchElementException();
			}
		}

		@Override
		public void remove() {
		}
	}

	private class ElementIterator implements Iterator<Vertex>{

		private final Iterator<Relationship> rels;
		
		public ElementIterator(NAryEdgeRoleType role) {
			this.rels = getNode().getRelationships(DynamicRelationshipType.withName(getType().getName()+EDGEROLE_SEPARATOR+role.getName()), Direction.OUTGOING).iterator();
		}

		@Override
		public boolean hasNext() {
			return rels.hasNext();
		}

		@Override
		public Vertex next() {
			Vertex elem = getDb().getVertex(rels.next().getEndNode());
			return elem;
		}

		@Override
		public void remove() {
		}
	}

	private class ElementIterable implements Iterable<Vertex>{

		private final NAryEdgeRoleType role;
		
		
		public ElementIterable(NAryEdgeRoleType role) {
			this.role = role;
		}

		@Override
		public Iterator<Vertex> iterator() {
			return new ElementIterator(role);
		}
	}
	
	private class RelationshipElementIterable implements Iterable<EdgeElement>{

		private final Set<EdgeRole<NAryEdgeType, NAryEdgeRoleType>> roles;
		
		public RelationshipElementIterable(
				Set<EdgeRole<NAryEdgeType, NAryEdgeRoleType>> roles) {
			this.roles = roles;
		}

		public RelationshipElementIterable() {
			this.roles = getType().getRoles();
		}
		
		@Override
		public Iterator<EdgeElement> iterator() {
			return new RelationshipElementIterator(roles);
		}
		
	}

	
	
	@Override
	public Iterable<EdgeElement> getEdgeElements() {
		return new RelationshipElementIterable();
	}

	@Override
	public Iterable<EdgeElement> getEdgeElements(NAryEdgeRoleType... roles) {
		Set<EdgeRole<NAryEdgeType, NAryEdgeRoleType>> roleSet = new HashSet<EdgeRole<NAryEdgeType, NAryEdgeRoleType>>();
		for(NAryEdgeRoleType role: roles){
			EdgeRole<NAryEdgeType, NAryEdgeRoleType> er = new NAryEdgeRole(role, getType());
			roleSet.add(er);
		}
		return new RelationshipElementIterable(roleSet);
	}

	
	@Override
	public Iterable<Vertex> getVertices(NAryEdgeRoleType role) {
		return new ElementIterable(role);
	}

	@Override
	public Vertex getVertex(FunctionalEdgeRoleType role) {
		return getDb().getVertex(getNode().getSingleRelationship(DynamicRelationshipType.withName(getType().getName()+EDGEROLE_SEPARATOR+role.getName()), Direction.OUTGOING).getEndNode());
	}

}
