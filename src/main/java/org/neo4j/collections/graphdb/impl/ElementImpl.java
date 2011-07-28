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

import org.neo4j.collections.graphdb.Node;
import org.neo4j.collections.graphdb.Element;
import org.neo4j.collections.graphdb.HyperRelationship;
import org.neo4j.collections.graphdb.HyperRelationshipType;
import org.neo4j.collections.graphdb.Path;
import org.neo4j.collections.graphdb.Property;
import org.neo4j.collections.graphdb.PropertyContainer;
import org.neo4j.collections.graphdb.PropertyType;
import org.neo4j.collections.graphdb.PropertyType.ComparablePropertyType;
import org.neo4j.collections.graphdb.Relationship;
import org.neo4j.collections.graphdb.RelationshipContainer;
import org.neo4j.collections.graphdb.RelationshipRole;
import org.neo4j.collections.graphdb.SortableRelationship;
import org.neo4j.collections.graphdb.SortableRelationshipType;
import org.neo4j.collections.indexedrelationship.IndexedRelationship;
import org.neo4j.collections.sortedtree.SortedTree;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.RelationshipType;

public abstract class ElementImpl implements Element{

	public static final String hyperRelSeparator = "/#/";
	
	@Override
	public Relationship createRelationshipTo(
			RelationshipContainer n,
			RelationshipType rt) {
		return new RelationshipImpl(getNode().createRelationshipTo(n.getNode(), rt));
	}

	@Override
	public <T> SortableRelationship<T> createRelationshipTo(
			RelationshipContainer n,
			SortableRelationshipType<T> rt) {
		IndexedRelationship idxRel = new IndexedRelationship(DynamicRelationshipType.withName(rt.name()), Direction.OUTGOING,  rt.getPropertyType(), true, this.getNode(), getGraphDatabase().getGraphDatabaseService());
		idxRel.createRelationshipTo(n.getNode());
		return null;
	}
	

	private class SortableRelationshipIterator implements Iterator<SortableRelationship<?>>{

		private final Node node;
		private final Iterator<Relationship> trees;
		private IndexedRelationship idx = null;
		private Iterator<org.neo4j.graphdb.Relationship> rels = null;
		private SortableRelationship<?> currentRelationship = null;
		
		private Boolean hasNext = null;
		
		SortableRelationshipIterator(Node node){
			trees = node.getRelationships(SortedTree.RelTypes.TREE_ROOT, Direction.OUTGOING).iterator();
			this.node = node;
		}
		
		@SuppressWarnings("rawtypes")
		@Override
		public boolean hasNext() {
			if(hasNext != null){
				return hasNext;
			}else{
				if(idx == null){
					if(trees.hasNext()){
						Relationship rel = trees.next();
						String propertyTypeName = (String)rel.getProperty(IndexedRelationship.PROPERTY_TYPE); 
						ComparablePropertyType<?> propertyType = (ComparablePropertyType<?>) PropertyType.getPropertyTypeByName(propertyTypeName, getGraphDatabase());
						RelationshipType relType = DynamicRelationshipType.withName((String)rel.getProperty(SortedTree.TREE_NAME));
						idx = new IndexedRelationship(relType,Direction.OUTGOING, propertyType, true, node.getNode(), getGraphDatabase().getGraphDatabaseService());
						return hasNext();
					}else{
						hasNext = false;
						return false;
					}
				}else{
					if(rels.hasNext()){
						currentRelationship = new SortableRelationshipImpl(new RelationshipImpl(rels.next()), idx);
						hasNext = true;
						return true;
					}else{
						idx = null;
						return hasNext();
					}
				}
			}
		}

		@Override
		public SortableRelationship<?> next() {
			if(hasNext()){
				hasNext = null;
				return currentRelationship;
			}else{
				throw new NoSuchElementException();
			}
		}

		@Override
		public void remove() {
			currentRelationship.delete();
		}
	}
	
	private class HyperRelationshipIterator implements Iterator<HyperRelationship>{

		private Set<Node> visitedHyperRelationships = new HashSet<Node>();
		
		HyperRelationship currentRelationship = null;
		Boolean hasNextAfterMove = null;
		
		private final Iterator<org.neo4j.graphdb.Relationship> relIterator;
		private final Iterator<SortableRelationship<?>> sortableRelIterator;

		HyperRelationshipIterator(){
			this.relIterator = getNode().getRelationships().iterator();
			this.sortableRelIterator = new SortableRelationshipIterator(new NodeImpl(getNode()));
		}
		
		HyperRelationshipIterator(RelationshipType... relTypes){
			this.relIterator = getNode().getRelationships(getGraphDatabase().expandRelationshipTypes(relTypes)).iterator();
			this.sortableRelIterator = new SortableRelationshipIterator(new NodeImpl(getNode()));			
		}
		
		@Override
		public boolean hasNext() {
			if(hasNextAfterMove == null){
				if(relIterator.hasNext()){
					Relationship rel = new RelationshipImpl(relIterator.next());
					RelationshipType relType = rel.getType();
					if(rel.getType().getRelationshipType().equals(SortedTree.RelTypes.TREE_ROOT)){
						return hasNext();
					}
					int separatorPosition = relType.name().indexOf(hyperRelSeparator); 
					if( separatorPosition > -1){
						if(rel.getEndNode().getId() != getNode().getId()){
							return hasNext();
						}
						Node hyperRel = rel.getStartNode();
						if(visitedHyperRelationships.contains(hyperRel)){
							return hasNext();
						}else{
							HyperRelationshipType hrelType =  getGraphDatabase().getRelationshipType(DynamicRelationshipType.withName(relType.name().substring(separatorPosition)));
							this.currentRelationship = new HyperRelationshipImpl(hyperRel, hrelType);
							hasNextAfterMove = true;
							return true;
						}
					}else{
						this.currentRelationship = rel;
						hasNextAfterMove = true;
						return true;
					}
				}else{
					if(sortableRelIterator.hasNext()){
						currentRelationship = sortableRelIterator.next();
						hasNextAfterMove = true;
						return true;
					}else{
						hasNextAfterMove = false;
						return false;
					}
				}
			}else{
				return hasNextAfterMove;
			}
		}

		@Override
		public HyperRelationship next() {
			if(hasNext()){
				hasNextAfterMove = null;
				return currentRelationship;
			}else{
				return null;
			}
		}

		@Override
		public void remove() {
			if(currentRelationship != null){
				currentRelationship.delete();
			}
		}
	}
	
	private class HyperRelationshipIterable implements Iterable<HyperRelationship>{

		private final RelationshipType[] relTypes;
		
		HyperRelationshipIterable(){
			this.relTypes = null;
		}
		
		HyperRelationshipIterable(HyperRelationshipType... relTypes){
			super();
			this.relTypes = relTypes;
		}

		HyperRelationshipIterable(RelationshipRole<?> role, RelationshipType... relTypes){
			super();
			this.relTypes = new RelationshipType[relTypes.length];
			for(int i=0;i<relTypes.length;i++){
				this.relTypes[i] = DynamicRelationshipType.withName(relTypes[i].name()+hyperRelSeparator+role.getName());
			}
		}
		
		
		@Override
		public Iterator<HyperRelationship> iterator() {
			if(relTypes == null){
				return new HyperRelationshipIterator();
			}else{
				return new HyperRelationshipIterator(relTypes);
			}
		}	
	}
	
	@Override
	public Iterable<HyperRelationship> getRelationships() {
		return new HyperRelationshipIterable();
	}

	@Override
	public Iterable<HyperRelationship> getRelationships(
			RelationshipType... relTypes) {
		HyperRelationshipType[] hyperRelTypes = new HyperRelationshipType[relTypes.length];
		for(int i=0;i < relTypes.length;i++){
			hyperRelTypes[i] = getGraphDatabase().getRelationshipType(relTypes[i]);
		}
		return new HyperRelationshipIterable(hyperRelTypes);
	}

	@Override
	public Iterable<Relationship> getRelationships(
			Direction dir) {
		return new RelationshipIterable(getNode().getRelationships(dir));
	}

	@Override
	public Iterable<Relationship> getRelationships(
			Direction dir, RelationshipType... relTypes) {
		return new RelationshipIterable(getNode().getRelationships(dir, relTypes));
	}

	@Override
	public Iterable<Relationship> getRelationships(
			RelationshipType relType, Direction dir) {
		return new RelationshipIterable(getNode().getRelationships(relType, dir));
	}

	@Override
	public Relationship getSingleRelationship(
			RelationshipType relType, Direction dir) {
		org.neo4j.graphdb.Relationship rel  = getNode().getSingleRelationship(relType, dir);
		if(rel == null){
			return null;
		}else{
			return new RelationshipImpl(rel);
		}
	}

	@Override
	public boolean hasRelationship() {
		return getNode().hasRelationship();
	}

	@Override
	public boolean hasRelationship(RelationshipType... relType) {
		return getNode().hasRelationship(relType);
	}

	@Override
	public boolean hasRelationship(Direction dir) {
		return getNode().hasRelationship(dir);
	}

	@Override
	public boolean hasRelationship(Direction dir,
			RelationshipType... relTypes) {
		return getNode().hasRelationship(dir, relTypes);
	}

	@Override
	public boolean hasRelationship(RelationshipType relType,
			Direction dir) {
		return getNode().hasRelationship(relType, dir);
	}

	@Override
	public <T> Property<T> getProperty(PropertyType<T> pt) {
		return new PropertyImpl<T>(getGraphDatabase(), this, pt);
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

	@Override
	public Iterable<PropertyType<?>> getPropertyTypes() {
		return PropertyType.getPropertyTypes(this, getGraphDatabase());
	}
	@Override
	public Object getProperty(String key) {
		return getPropertyContainer().getProperty(key);
	}

	@Override
	public Object getProperty(String key, Object defaultValue) {
		return getPropertyContainer().getProperty(key, defaultValue);
	}

	@Override
	public Iterable<String> getPropertyKeys() {
		return getPropertyContainer().getPropertyKeys();
	}

	@Deprecated
	public Iterable<Object> getPropertyValues() {
		return getPropertyContainer().getPropertyValues();
	}

	@Override
	public boolean hasProperty(String key) {
		return getPropertyContainer().hasProperty(key);
	}

	@Override
	public Object removeProperty(String key) {
		return getPropertyContainer().removeProperty(key);
	}

	@Override
	public void setProperty(String key, Object value) {
		getPropertyContainer().setProperty(key, value);
	}

	@Override
	public Iterable<HyperRelationship> getRelationships(RelationshipRole<? extends Element> role,
			RelationshipType... relTypes) {
		return new HyperRelationshipIterable(role, relTypes);
	}


	@Override
	public HyperRelationship getSingleRelationship(RelationshipRole<? extends Element> role,
			RelationshipType relType) {
		Iterator<Relationship> rels = new RelationshipIterable(getNode().getRelationships(DynamicRelationshipType.withName(relType.name()+hyperRelSeparator+role.getName()), Direction.INCOMING)).iterator();
		if(rels.hasNext()){
			Relationship rel = rels.next();
			if(rels.hasNext()){
				throw new RuntimeException("More than one relationship found");
			}else{
				return new HyperRelationshipImpl(rel.getStartNode(), getGraphDatabase().getRelationshipType(relType));
			}
		}else{
			return null;
		}
	}


	@Override
	public boolean hasRelationship(RelationshipRole<? extends Element> role,
			RelationshipType... relTypes) {
		RelationshipType[] hRelTypes = new RelationshipType[relTypes.length];
		for(int i=0;i<relTypes.length;i++){
			hRelTypes[i] = DynamicRelationshipType.withName(relTypes[i].name()+hyperRelSeparator+role.getName());
		}
		return getNode().hasRelationship(Direction.INCOMING, relTypes);
	}

	class ElementPath implements Path{

		@Override
		public Node startNode() {
			return new NodeImpl(getNode());
		}

		@Override
		public Element startElement() {
			return getGraphDatabase().getElement(getNode());
		}

		@Override
		public Node endNode() {
			return new NodeImpl(getNode());
		}

		@Override
		public Element endElement() {
			return getGraphDatabase().getElement(getNode());
		}

		@Override
		public Relationship lastRelationship() {
			return null;
		}

		
		@Override
		public Iterable<Relationship> relationships() {
			return new ArrayList<Relationship>();
		}

		@Override
		public Iterable<Node> nodes() {
			ArrayList<Node> nodes = new ArrayList<Node>();
			nodes.add(new NodeImpl(getNode()));
			return nodes;
		}

		@Override
		public Iterable<Element> elements() {
			return null;
		}

		@Override
		public int length() {
			return 0;
		}

		@Override
		public Iterator<PropertyContainer> iterator() {
			ArrayList<PropertyContainer> nodes = new ArrayList<PropertyContainer>();
			nodes.add(new NodeImpl(getNode()));
			return nodes.iterator();
		}
		
	}
	
	class PathIterator implements Iterator<Path>{

		boolean first = true;
		
		@Override
		public boolean hasNext() {
			return first;
		}

		@Override
		public Path next() {
			if(hasNext()){
				first = false;
				return new ElementPath();
			}else{
				throw new NoSuchElementException();
			}
		}

		@Override
		public void remove() {
		}
		
	}
	
	@Override
	public Iterator<Path> iterator() {
		return new PathIterator();
	}
}
