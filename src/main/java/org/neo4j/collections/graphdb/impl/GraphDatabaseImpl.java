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
import java.util.Set;

import org.neo4j.collections.graphdb.Element;
import org.neo4j.collections.graphdb.HyperRelationship;
import org.neo4j.collections.graphdb.HyperRelationshipType;
import org.neo4j.collections.graphdb.GraphDatabaseService;
import org.neo4j.collections.graphdb.Node;
import org.neo4j.collections.graphdb.PropertyType;
import org.neo4j.collections.graphdb.Relationship;
import org.neo4j.collections.graphdb.RelationshipElement;
import org.neo4j.collections.graphdb.RelationshipRole;
import org.neo4j.collections.graphdb.BinaryRelationshipRole.*;
import org.neo4j.collections.graphdb.wrappers.IndexManager;
import org.neo4j.collections.graphdb.PropertyType.ComparablePropertyType;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.event.KernelEventHandler;
import org.neo4j.graphdb.event.TransactionEventHandler;

public class GraphDatabaseImpl implements GraphDatabaseService {

	private static String HYPERRELATIONSHIP_TYPE = "org.neo4j.collections.graphdb.hyperrelationship_type";
	
	private final org.neo4j.graphdb.GraphDatabaseService graphDb;

	GraphDatabaseImpl(org.neo4j.graphdb.GraphDatabaseService graphDb) {
		this.graphDb = graphDb;
	}

	public org.neo4j.graphdb.GraphDatabaseService getGraphDatabaseService() {
		return graphDb;
	}

	@Override
	public Transaction beginTx() {
		return getGraphDatabaseService().beginTx();
	}

	@Override
	public Node createNode() {
		return new NodeImpl(getGraphDatabaseService().createNode());
	}

	@Override
	public Iterable<Node> getAllNodes() {
		return new NodeIterable(getGraphDatabaseService().getAllNodes());
	}

	@Override
	public Node getNodeById(long arg0) {
		return new NodeImpl(getGraphDatabaseService().getNodeById(arg0));
	}

	
	@Override
	public Node getReferenceNode() {
		return new NodeImpl(getGraphDatabaseService().getReferenceNode());
	}

	@Override
	public Relationship getRelationshipById(long arg0) {
		return new RelationshipImpl(getGraphDatabaseService()
				.getRelationshipById(arg0));
	}

	@Override
	public Iterable<HyperRelationshipType> getRelationshipTypes() {
		return new RelationshipTypeIterable(graphDb.getRelationshipTypes(),
				this);
	}

	@Override
	public IndexManager index() {
		return new IndexManagerImpl(graphDb.index());
	}

	@Override
	public KernelEventHandler registerKernelEventHandler(KernelEventHandler arg0) {
		return graphDb.registerKernelEventHandler(arg0);
	}

	@Override
	public <T> TransactionEventHandler<T> registerTransactionEventHandler(
			TransactionEventHandler<T> arg0) {
		return graphDb.registerTransactionEventHandler(arg0);
	}

	@Override
	public void shutdown() {
		graphDb.shutdown();

	}

	@Override
	public KernelEventHandler unregisterKernelEventHandler(
			KernelEventHandler arg0) {
		return graphDb.unregisterKernelEventHandler(arg0);
	}

	@Override
	public <T> TransactionEventHandler<T> unregisterTransactionEventHandler(
			TransactionEventHandler<T> arg0) {
		return graphDb.unregisterTransactionEventHandler(arg0);
	}

	@Override
	public PropertyType<Boolean> getBooleanPropertyType(String name) {
		return new PropertyType.BooleanPropertyType(name, this);
	}

	@Override
	public PropertyType<Boolean[]> getBooleanArrayPropertyType(String name) {
		return new PropertyType.BooleanArrayPropertyType(name, this);
	}

	@Override
	public ComparablePropertyType<Byte> getBytePropertyType(String name) {
		return new PropertyType.BytePropertyType(name, this);
	}

	@Override
	public PropertyType<Byte[]> getByteArrayPropertyType(String name) {
		return new PropertyType.ByteArrayPropertyType(name, this);
	}

	@Override
	public ComparablePropertyType<Double> getDoublePropertyType(String name) {
		return new PropertyType.DoublePropertyType(name, this);
	}

	@Override
	public PropertyType<Double[]> getDoubleArrayPropertyType(String name) {
		return new PropertyType.DoubleArrayPropertyType(name, this);
	}

	@Override
	public ComparablePropertyType<Float> getFloatPropertyType(String name) {
		return new PropertyType.FloatPropertyType(name, this);
	}

	@Override
	public PropertyType<Float[]> getFloatArrayPropertyType(String name) {
		return new PropertyType.FloatArrayPropertyType(name, this);
	}

	@Override
	public ComparablePropertyType<Long> getLongPropertyType(String name) {
		return new PropertyType.LongPropertyType(name, this);
	}

	@Override
	public PropertyType<Long[]> getLongArrayPropertyType(String name) {
		return new PropertyType.LongArrayPropertyType(name, this);
	}

	@Override
	public ComparablePropertyType<Short> getShortPropertyType(String name) {
		return new PropertyType.ShortPropertyType(name, this);
	}

	@Override
	public PropertyType<Short[]> getShortArrayPropertyType(String name) {
		return new PropertyType.ShortArrayPropertyType(name, this);
	}

	@Override
	public ComparablePropertyType<String> getStringPropertyType(String name) {
		return new PropertyType.StringPropertyType(name, this);
	}

	@Override
	public PropertyType<String[]> getStringArrayPropertyType(String name) {
		return new PropertyType.StringArrayPropertyType(name, this);
	}

	@Override
	public HyperRelationshipType getRelationshipType(String name) {
		return getRelationshipType(DynamicRelationshipType.withName(name));
	}

	private Node getRelationshipTypeNode(RelationshipType relType){
		Node subRef = RelationshipTypeImpl.getOrCreateRoleSubRef(this);
		Relationship rel = subRef.getSingleRelationship(relType, Direction.OUTGOING);
		if(rel != null){
			return rel.getEndNode();
		}else{
			Node n = this.createNode();
			subRef.createRelationshipTo(n, relType);
			n.setProperty(RelationshipTypeImpl.REL_TYPE, relType.name());
			return n;
		}
	}
	
	@Override
	public HyperRelationshipType getRelationshipType(RelationshipType relType) {
		Node n = getRelationshipTypeNode(relType);
		if(n == null){
			return null;
		}else{
			if(n.hasProperty(RelationshipTypeImpl.REL_TYPE_ROLES)){
				String[] names = (String[])n.getProperty(RelationshipTypeImpl.REL_TYPE_ROLES);
				Set<RelationshipRole<?>> roles = new HashSet<RelationshipRole<?>>();
				for(String name: names){
					roles.add(getRelationshipRole(name));
				}
				return new RelationshipTypeImpl(this, relType, roles);
			}else{
				return null;
			}
		}
		
	}

	@Override
	public HyperRelationshipType getOrCreateRelationshipType(RelationshipType relType, Set<RelationshipRole<? extends Element>>roles) {
		return new RelationshipTypeImpl(this, relType, roles);
	}

	
	@Override
	public HyperRelationship createRelationship(HyperRelationshipType relType,
			Set<RelationshipElement<? extends Element>> relationshipElements) {
		RelationshipRole<? extends Element>[] roles = relType.getRoles();
		for(RelationshipRole<? extends Element> role: roles){
			boolean found = false;
			for(RelationshipElement<? extends Element> relement: relationshipElements){
				if(relement.getRole().getName().equals(role.getName())){
					found = true;
				}
			}
			if(found == false){
				throw new RuntimeException("To create relationship an element with role "+role.getName()+" should be provide");
			}
		}
		Node n = createNode();
		n.setProperty(HYPERRELATIONSHIP_TYPE, relType.name());
		for(RelationshipElement<? extends Element> relement: relationshipElements){
			for(Element elem: relement.getElements()){
				n.createRelationshipTo(elem, DynamicRelationshipType.withName(relType.name()+"/#/"+relement.getRole().getName()));
			}
		}
		return new HyperRelationshipImpl(n, relType);
	}

	public RelationshipType[] expandRelationshipTypes(
			RelationshipType... relTypes) {
		Set<RelationshipType> relTypesToReturn = new HashSet<RelationshipType>();
		for (RelationshipType relType : relTypes) {
			HyperRelationshipType hreltype = getRelationshipType(relType);
			RelationshipRole<?>[] roles = hreltype.getRoles();
			if (roles.length == 2) {
				relTypesToReturn.add(DynamicRelationshipType.withName(hreltype
						.name()));
			} else {
				for (RelationshipRole<?> role : roles) {
					relTypesToReturn
							.add(DynamicRelationshipType.withName(hreltype
									.name() + "/#/" + role.getName()));
				}
			}
		}
		return (RelationshipType[]) relTypesToReturn.toArray();
	}

	@Override
	public <T extends Element> RelationshipRole<T> getRelationshipRole(
			String name) {
		return new RelationshipRoleImpl<T>(this, name);
	}

	@Override
	public RelationshipRole<Element> getStartElementRole() {
		return new StartElement(this);
	}

	@Override
	public RelationshipRole<Element> getEndElementRole() {
		return new EndElement(this);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Element getElement(org.neo4j.graphdb.Node node) {
		if(node.hasProperty(RelationshipImpl.REL_ID)){
			return getRelationshipById((Long)node.getProperty(RelationshipImpl.REL_ID));
		}else if(node.hasProperty(HYPERRELATIONSHIP_TYPE)){
			return new HyperRelationshipImpl(new NodeImpl(node), getRelationshipType((String)node.getProperty(HYPERRELATIONSHIP_TYPE)));
		}else if(node.hasProperty(RelationshipTypeImpl.REL_TYPE)){
			return getRelationshipType(DynamicRelationshipType.withName((String)node.getProperty(RelationshipTypeImpl.REL_TYPE)));
		}else if(node.hasProperty(PropertyType.PROP_TYPE)){
			String propType = (String)node.getProperty(PropertyType.PROP_TYPE);
			return PropertyType.getPropertyTypeByName(propType, this);
		}else if(node.hasProperty(RelationshipRoleImpl.ROLE_NAME)){
			if(node.hasProperty(FunctionalRelationshipRoleImpl.IS_FUNCTIONAL_ROLE)){
				return new FunctionalRelationshipRoleImpl<Element>(this, (String)node.getProperty(RelationshipRoleImpl.ROLE_NAME));
			}else{
				return new RelationshipRoleImpl<Element>(this, (String)node.getProperty(RelationshipRoleImpl.ROLE_NAME));
			}
		}else if(node.hasProperty(PropertyImpl.PROPERTYCONTAINER_ID) && node.hasProperty(PropertyImpl.PROPERTYCONTAINER_TYPE) && node.hasProperty(PropertyImpl.PROPERTY_NAME)){
			if(node.getProperty(PropertyImpl.PROPERTYCONTAINER_TYPE).equals(PropertyImpl.PropertyContainerType.RELATIONSHIP.name())){
				Relationship rel = getRelationshipById((Long)node.getProperty(PropertyImpl.PROPERTYCONTAINER_ID));
				PropertyType<?> pt = PropertyType.getPropertyTypeByName((String)node.getProperty(PropertyImpl.PROPERTY_NAME), this);
				return new PropertyImpl(this, rel, pt);
			}else{
				Node n = getNodeById((Long)node.getProperty(PropertyImpl.PROPERTYCONTAINER_ID));
				PropertyType<?> pt = PropertyType.getPropertyTypeByName((String)node.getProperty(PropertyImpl.PROPERTY_NAME), this);
				return new PropertyImpl(this, n, pt);
			}
		}else{
			return new NodeImpl(node);
		}
	}
}
