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
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import org.neo4j.collections.graphdb.BinaryEdge;
import org.neo4j.graphdb.Node;
import org.neo4j.collections.graphdb.DatabaseService;
import org.neo4j.collections.graphdb.EdgeRole;
import org.neo4j.collections.graphdb.EdgeType;
import org.neo4j.collections.graphdb.NAryEdge;
import org.neo4j.collections.graphdb.NAryEdgeRoleType;
import org.neo4j.collections.graphdb.NAryEdgeType;
import org.neo4j.collections.graphdb.Vertex;
import org.neo4j.collections.graphdb.Property;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.collections.graphdb.PropertyType;
import org.neo4j.collections.graphdb.SortableBinaryEdge;
import org.neo4j.collections.graphdb.SortableBinaryEdgeType;
import org.neo4j.collections.indexedrelationship.IndexedRelationship;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

public class VertexImpl implements Vertex{

	public static final String hyperRelSeparator = "/#/";

	private Node node;  
	
	public VertexImpl(Node node){
		this.node = node;
	}
	
	@Override
	public BinaryEdge createEdgeTo(
			Vertex n,
			RelationshipType rt) {
		return new BinaryEdgeImpl(getNode().createRelationshipTo(n.getNode(), rt));
	}

	@Override
	public <T> SortableBinaryEdge<T> createRelationshipTo(
			Vertex n,
			SortableBinaryEdgeType<T> rt) {
		IndexedRelationship idxRel = new IndexedRelationship(DynamicRelationshipType.withName(rt.getName()), Direction.OUTGOING,  rt.getPropertyType(), true, this.getNode(), getDb().getGraphDatabaseService());
		Relationship rel = idxRel.createRelationshipTo(n.getNode());
		return new SortableBinaryEdgeImpl<T>(rel, idxRel);
	}
	
	@Override
	public Iterable<BinaryEdge> getBinaryEdges() {
		return new RelationshipIterable(node.getRelationships());
	}

	@Override
	public Iterable<BinaryEdge> getBinaryEdges(
			RelationshipType... relTypes) {
		return new RelationshipIterable(node.getRelationships(relTypes));
	}

	@Override
	public Iterable<BinaryEdge> getBinaryEdges(
			Direction dir) {
		return new RelationshipIterable(getNode().getRelationships(dir));
	}

	@Override
	public Iterable<BinaryEdge> getBinaryEdges(
			Direction dir, RelationshipType... relTypes) {
		return new RelationshipIterable(getNode().getRelationships(dir, relTypes));
	}

	@Override
	public Iterable<BinaryEdge> getBinaryEdges(
			RelationshipType relType, Direction dir) {
		return new RelationshipIterable(getNode().getRelationships(relType, dir));
	}

	@Override
	public BinaryEdge getSingleBinaryEdge(
			RelationshipType relType, Direction dir) {
		org.neo4j.graphdb.Relationship rel  = getNode().getSingleRelationship(relType, dir);
		if(rel == null){
			return null;
		}else{
			return new BinaryEdgeImpl(rel);
		}
	}

	@Override
	public boolean hasBinaryEdge() {
		return getNode().hasRelationship();
	}

	@Override
	public boolean hasBinaryEdge(RelationshipType... relType) {
		return getNode().hasRelationship(relType);
	}

	@Override
	public boolean hasBinaryEdge(Direction dir) {
		return getNode().hasRelationship(dir);
	}

	@Override
	public boolean hasBinaryEdge(Direction dir,
			RelationshipType... relTypes) {
		return getNode().hasRelationship(dir, relTypes);
	}

	@Override
	public boolean hasBinaryEdge(RelationshipType relType,
			Direction dir) {
		return getNode().hasRelationship(relType, dir);
	}

	@Override
	public <T> Property<T> getProperty(PropertyType<T> pt) {
		return new PropertyImpl<T>(getDb(), this, pt);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getPropertyValue(PropertyType<T> pt) {
		return (T)getPropertyContainer().getProperty(pt.getName());
	}

	@Override
	public <T> boolean hasProperty(PropertyType<T> pt) {
		return getPropertyContainer().hasProperty(pt.getName());
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T removeProperty(PropertyType<T> pt) {
		return (T)getPropertyContainer().removeProperty(pt.getName());
	}

	@Override
	public <T> void setProperty(PropertyType<T> pt, T value) {
		getPropertyContainer().setProperty(pt.getName(), value);
	}

	
	class PropertyTypeIterator implements Iterator<PropertyType<?>>{

		final Iterator<String> keys;
		
		PropertyTypeIterator(Node n){
			keys = n.getPropertyKeys().iterator();
		}
		
		@Override
		public boolean hasNext() {
			return keys.hasNext();
		}

		@Override
		public PropertyType<?> next() {
			return PropertyType.getPropertyTypeByName(getDb(), keys.next());
		}

		@Override
		public void remove() {
		}
		
	}
	
	class PropertyTypeIterable implements Iterable<PropertyType<?>>{

		final Node node;
		
		public PropertyTypeIterable(Node node) {
			this.node = node;
		}

		@Override
		public Iterator<PropertyType<?>> iterator() {
			return new PropertyTypeIterator(node);
		}
		
	}
	
	@Override
	public Iterable<PropertyType<?>> getPropertyTypes() {
		return new PropertyTypeIterable(getNode());
	}

	private class NAryEdgeIterator implements Iterator<NAryEdge>{

		private final Iterator<Relationship> edgeRoleRels;
		
		public NAryEdgeIterator(EdgeRole<EdgeType<NAryEdgeRoleType>, NAryEdgeRoleType> edgeRole) {
			this.edgeRoleRels = getNode().getRelationships(DynamicRelationshipType.withName(edgeRole.getEdgeType().getName()+"/#/"+edgeRole.getEdgeRoleType().getName()), Direction.INCOMING).iterator();
		}
		
		@Override
		public boolean hasNext() {
			return edgeRoleRels.hasNext();
		}

		@Override
		public NAryEdge next() {
			if(edgeRoleRels.hasNext()){
				return new NAryEdgeImpl(edgeRoleRels.next().getStartNode());
			}else{
				throw new NoSuchElementException();
			}
		}

		@Override
		public void remove() {
		}
	}

	
	private class NAryEdgeRoleIterator implements Iterator<NAryEdge>{

		private final Iterator<EdgeRole<EdgeType<NAryEdgeRoleType>, NAryEdgeRoleType>> edgeRoles;
		
		private NAryEdgeIterator currentEdgeIterator = null;
		private Boolean hasNext = null;
		
		public NAryEdgeRoleIterator(Iterable<EdgeRole<EdgeType<NAryEdgeRoleType>, NAryEdgeRoleType>> roles) {
			this.edgeRoles = roles.iterator();
		}
		
		@Override
		public boolean hasNext() {
			if(hasNext == null){
				if(currentEdgeIterator == null){
					if(edgeRoles.hasNext()){
						currentEdgeIterator = new NAryEdgeIterator(edgeRoles.next());
						return hasNext();
					}else{
						hasNext = false;
						return false;
					}
				}else{
					if(currentEdgeIterator.hasNext()){
						hasNext = true;
						return true;
					}else{
						currentEdgeIterator = null;
						return hasNext();
					}
				}
			}else{
				return hasNext;
			}
		}

		@Override
		public NAryEdge next() {
			if(hasNext()){
				hasNext = null;
				return currentEdgeIterator.next();
			}else{
				throw new NoSuchElementException();
			}
		}

		@Override
		public void remove() {
		}
		
	}

	private class NAryEdgeRoleIterable implements Iterable<NAryEdge>{

		Iterable<EdgeRole<EdgeType<NAryEdgeRoleType>, NAryEdgeRoleType>> roles;
		
		public NAryEdgeRoleIterable(
				Iterable<EdgeRole<EdgeType<NAryEdgeRoleType>, NAryEdgeRoleType>> roles) {
			this.roles = roles;
		}

		@Override
		public Iterator<NAryEdge> iterator() {
			return new NAryEdgeRoleIterator(roles);
		}
		
	}
	
	private class NAryEdgeTypeIterator implements Iterator<NAryEdge>{

		Iterator<NAryEdgeType> edgeTypes;
		NAryEdgeRoleIterator currentEdgeRoleIterator = null;
		Boolean hasNext = null;
		
		NAryEdgeTypeIterator(Iterator<NAryEdgeType> edgeTypes){
			this.edgeTypes = edgeTypes;
		}

		@Override
		public boolean hasNext() {
			if(hasNext == null){
				if(currentEdgeRoleIterator == null){
					if(edgeTypes.hasNext()){
						currentEdgeRoleIterator = new NAryEdgeRoleIterator(edgeTypes.next().getRoles());
						return hasNext();
					}else{
						hasNext = false;
						return false;
					}
				}else{
					if(currentEdgeRoleIterator.hasNext()){
						hasNext = true;
						return true;
					}else{
						currentEdgeRoleIterator = null;
						return hasNext();
					}
				}
			}else{
				return hasNext;
			}
		}

		@Override
		public NAryEdge next() {
			if(hasNext()){
				hasNext = null;
				return currentEdgeRoleIterator.next();
			}else{
				throw new NoSuchElementException();
			}
		}

		@Override
		public void remove() {
		}
		
	}
	
	private class NAryEdgeTypeIterable implements Iterable<NAryEdge>{

		NAryEdgeType[] edgeTypes;
		
		NAryEdgeTypeIterable(NAryEdgeType... edgeTypes){
			this.edgeTypes = edgeTypes;
		}
		
		@Override
		public Iterator<NAryEdge> iterator() {
			ArrayList<NAryEdgeType> ets = new ArrayList<NAryEdgeType>();
			for(NAryEdgeType edgeType: edgeTypes){
				ets.add(edgeType);
			}
			return new NAryEdgeTypeIterator(ets.iterator());
		}
		
	}
	
	@Override
	public Iterable<NAryEdge> getEdges(NAryEdgeType... edgeTypes) {
		return new NAryEdgeTypeIterable(edgeTypes);
	}

	@Override
	public Node getNode() {
		return node;
	}

	@Override
	public long getId() {
		return node.getId();
	}

	@Override
	public PropertyContainer getPropertyContainer() {
		return node;
	}

	@Override
	public DatabaseService getDb() {
		return new GraphDatabaseImpl(getPropertyContainer().getGraphDatabase());
	}

	@Override
	public Iterable<NAryEdge> getEdges(NAryEdgeType edgeType, NAryEdgeRoleType... roles) {
		Set<EdgeRole<EdgeType<NAryEdgeRoleType>, NAryEdgeRoleType>> roles1 = new HashSet<EdgeRole<EdgeType<NAryEdgeRoleType>, NAryEdgeRoleType>>();
		Set<EdgeRole<EdgeType<NAryEdgeRoleType>, NAryEdgeRoleType>> roles2 = edgeType.getRoles();
		for(NAryEdgeRoleType role: roles){
			for(EdgeRole<EdgeType<NAryEdgeRoleType>, NAryEdgeRoleType> role2: roles2){
				if(role.getName().equals(role2.getEdgeRoleType().getName())){
					roles1.add(role2);
				}
			}
		}
		
		return new NAryEdgeRoleIterable(roles1);
	}

	@Override
	public boolean hasEdge(NAryEdgeType edgeType, NAryEdgeRoleType... roles) {
		return getEdges(edgeType, roles).iterator().hasNext();
	}
	
}
