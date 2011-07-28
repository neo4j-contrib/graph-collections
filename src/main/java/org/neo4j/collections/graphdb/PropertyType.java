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
package org.neo4j.collections.graphdb;

import java.util.ArrayList;
import java.util.Comparator;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.collections.graphdb.impl.ElementImpl;

public abstract class PropertyType<T> extends ElementImpl{

	public final static String PROP_TYPE = "org.neo4j.collections.graphdb.prop_type";
	
	public static enum RelTypes implements org.neo4j.graphdb.RelationshipType{
		PROPTYPE_SUBREF,
		BOOLEAN_PROPTYPE_SUBREF,
		BOOLEAN_ARRAY_PROPTYPE_SUBREF,
		BYTE_PROPTYPE_SUBREF,
		BYTE_ARRAY_PROPTYPE_SUBREF,		
		DOUBLE_PROPTYPE_SUBREF,		
		DOUBLE_ARRAY_PROPTYPE_SUBREF,
		FLOAT_PROPTYPE_SUBREF,
		FLOAT_ARRAY_PROPTYPE_SUBREF,
		LONG_PROPTYPE_SUBREF,
		LONG_ARRAY_PROPTYPE_SUBREF,
		INTEGER_PROPTYPE_SUBREF,
		INTEGER_ARRAY_PROPTYPE_SUBREF,
		SHORT_PROPTYPE_SUBREF,
		SHORT_ARRAY_PROPTYPE_SUBREF,
		STRING_PROPTYPE_SUBREF,
		STRING_ARRAY_PROPTYPE_SUBREF		
	}
	
	public long getId(){
		return getNode().getId();
	}
	
	private Node node;
	
	private final String nm; 
	protected final GraphDatabaseService graphDb;
	
	public String getName(){
		return nm;
	}

	private PropertyType(String name, GraphDatabaseService graphDb){
		this.nm = name;
		this.graphDb = graphDb;
	}

	public static PropertyType<?> getPropertyTypeByName(String key, GraphDatabaseService graphDb){
		for(org.neo4j.graphdb.RelationshipType relType: RelTypes.values()){
			if(!relType.equals(RelTypes.PROPTYPE_SUBREF)){
				Node typeSubRef = getTypeSubRef(graphDb, relType);
				if(typeSubRef.hasRelationship(DynamicRelationshipType.withName(key), Direction.OUTGOING)){
					if(relType.equals(RelTypes.BOOLEAN_ARRAY_PROPTYPE_SUBREF)){
						return new BooleanArrayPropertyType(key, graphDb);
					}else if(relType.equals(RelTypes.BOOLEAN_PROPTYPE_SUBREF)){
						return new BooleanPropertyType(key, graphDb);
					}else if(relType.equals(RelTypes.BYTE_ARRAY_PROPTYPE_SUBREF)){
						return new ByteArrayPropertyType(key, graphDb);
					}else if(relType.equals(RelTypes.BYTE_PROPTYPE_SUBREF)){
						return new BytePropertyType(key, graphDb);
					}else if(relType.equals(RelTypes.DOUBLE_PROPTYPE_SUBREF)){
						return new DoublePropertyType(key, graphDb);
					}else if(relType.equals(RelTypes.DOUBLE_ARRAY_PROPTYPE_SUBREF)){
						return new DoubleArrayPropertyType(key, graphDb);
					}else if(relType.equals(RelTypes.FLOAT_PROPTYPE_SUBREF)){
						return new FloatPropertyType(key, graphDb);
					}else if(relType.equals(RelTypes.FLOAT_ARRAY_PROPTYPE_SUBREF)){
						return new FloatArrayPropertyType(key, graphDb);
					}else if(relType.equals(RelTypes.INTEGER_PROPTYPE_SUBREF)){
						return new IntegerPropertyType(key, graphDb);
					}else if(relType.equals(RelTypes.INTEGER_ARRAY_PROPTYPE_SUBREF)){
						return new IntegerArrayPropertyType(key, graphDb);
					}else if(relType.equals(RelTypes.LONG_PROPTYPE_SUBREF)){
						return new LongPropertyType(key, graphDb);
					}else if(relType.equals(RelTypes.LONG_ARRAY_PROPTYPE_SUBREF)){
						return new LongArrayPropertyType(key, graphDb);
					}else if(relType.equals(RelTypes.SHORT_PROPTYPE_SUBREF)){
						return new ShortPropertyType(key, graphDb);
					}else if(relType.equals(RelTypes.SHORT_ARRAY_PROPTYPE_SUBREF)){
						return new ShortArrayPropertyType(key, graphDb);
					}else if(relType.equals(RelTypes.STRING_PROPTYPE_SUBREF)){
						return new StringPropertyType(key, graphDb);
					}else if(relType.equals(RelTypes.STRING_ARRAY_PROPTYPE_SUBREF)){
						return new StringArrayPropertyType(key, graphDb);
					}
				}
			}
		}
		return null;
	}
	
	public static Iterable<PropertyType<?>> getPropertyTypes(PropertyContainer pc, GraphDatabaseService graphDb){
		ArrayList<PropertyType<?>> propertyTypes = new ArrayList<PropertyType<?>>();
		for(String key: pc.getPropertyContainer().getPropertyKeys()){
			propertyTypes.add(getPropertyTypeByName(key, graphDb));
		}
		return propertyTypes;
	}
	
	protected abstract RelationshipType propertyNameSubRef();

	@Override
	public org.neo4j.graphdb.PropertyContainer getPropertyContainer() {
		return getNode();
	}

	private static Node getTypeSubRef(GraphDatabaseService graphDb, RelationshipType propertyNameSubRef){
		Relationship subRefRel = graphDb.getReferenceNode().getSingleRelationship(RelTypes.PROPTYPE_SUBREF, Direction.OUTGOING);
		Node subRef = null;
		if(subRefRel == null){
			Node n = graphDb.createNode();
			graphDb.getReferenceNode().createRelationshipTo(n, RelTypes.PROPTYPE_SUBREF);
			subRef = n;
		}else{
			subRef = (Node)subRefRel.getEndNode();
		}
		Relationship typeSubRefRel = subRef.getSingleRelationship(propertyNameSubRef, Direction.OUTGOING);
		Node typeSubRef = null;
		if(typeSubRefRel == null){
			Node n = graphDb.createNode();
			subRef.createRelationshipTo(n, propertyNameSubRef);
			typeSubRef = n;
		}else{
			typeSubRef = (Node)typeSubRefRel.getEndNode();
		}
		return typeSubRef;
	}
	
	
	@Override
	public org.neo4j.graphdb.Node getNode(){
		if(node == null){
			for(org.neo4j.graphdb.RelationshipType relType: RelTypes.values()){
				Node typeSubRef = getTypeSubRef(getGraphDatabase(), relType);
				if(typeSubRef.hasRelationship(DynamicRelationshipType.withName(getName()), Direction.OUTGOING)){
					if(relType.equals(propertyNameSubRef())){
						Node node = typeSubRef.getSingleRelationship(DynamicRelationshipType.withName(getName()), Direction.OUTGOING).getEndNode();
						return node.getNode();
					}else{
						throw new RuntimeException("PropertyType already exists with different data type");
					}
				}
			}
			Node typeSubRef = getTypeSubRef(getGraphDatabase(), propertyNameSubRef());			
			node = getGraphDatabase().createNode();
			node.setProperty(PROP_TYPE, getName());
			typeSubRef.createRelationshipTo(node, DynamicRelationshipType.withName(getName()));
			return node.getNode();
		}else{
			return node.getNode();
		}
	}

	@Override
	public GraphDatabaseService getGraphDatabase() {
		return graphDb;
	}
	
	public static abstract class ComparablePropertyType<T> extends PropertyType<T> implements Comparator<org.neo4j.graphdb.Node>, PropertyComparator<T>{

		ComparablePropertyType(String name, GraphDatabaseService graphDb) {
			super(name, graphDb);
		}
		
		public abstract int compare(org.neo4j.graphdb.Node node1, org.neo4j.graphdb.Node node2);
		
		public abstract int compare(T value, org.neo4j.graphdb.Node node);
	}

	public static class BooleanArrayPropertyType extends PropertyType<Boolean[]>{

		protected final RelationshipType propertyNameSubRef(){
			return RelTypes.BOOLEAN_ARRAY_PROPTYPE_SUBREF;	
		}

		public BooleanArrayPropertyType(String name, GraphDatabaseService graphDb) {
			super(name, graphDb);
		}
	}
	
	public static class BooleanPropertyType extends PropertyType<Boolean>{

		protected final RelationshipType propertyNameSubRef(){
			return RelTypes.BOOLEAN_PROPTYPE_SUBREF;	
		}
		
		public BooleanPropertyType(String name, GraphDatabaseService graphDb) {
			super(name, graphDb);
		}
	}
	
	public static class ByteArrayPropertyType extends PropertyType<Byte[]>{

		protected final RelationshipType propertyNameSubRef(){
			return RelTypes.BYTE_ARRAY_PROPTYPE_SUBREF;	
		}

		public ByteArrayPropertyType(String name, GraphDatabaseService graphDb) {
			super(name, graphDb);
		}
		
	}

	public static class BytePropertyType extends ComparablePropertyType<Byte>{

		protected final RelationshipType propertyNameSubRef(){
			return RelTypes.BYTE_PROPTYPE_SUBREF;	
		}

		public BytePropertyType(String name, GraphDatabaseService graphDb) {
			super(name, graphDb);
		}

		@Override
		public int compare(Byte value, org.neo4j.graphdb.Node node) {
			if(node.hasProperty(getName()))
				throw new RuntimeException("Node does not have property "+getName());

			Byte propertyValue = (Byte)node.getProperty(getName());
			
			return value.compareTo(propertyValue);
		}

		public int compare(org.neo4j.graphdb.Node node1, org.neo4j.graphdb.Node node2) {
			if(node1.hasProperty(getName()))
				throw new RuntimeException("Node does not have property "+getName());

			Byte propertyValue1 = (Byte)node1.getProperty(getName());

			if(node2.hasProperty(getName()))
				throw new RuntimeException("Node does not have property "+getName());

			Byte propertyValue2 = (Byte)node2.getProperty(getName());
			
			return propertyValue1.compareTo(propertyValue2);
		}
	}

	public static class DoubleArrayPropertyType extends PropertyType<Double[]>{

		protected final RelationshipType propertyNameSubRef(){
			return RelTypes.DOUBLE_ARRAY_PROPTYPE_SUBREF;	
		}

		public DoubleArrayPropertyType(String name, GraphDatabaseService graphDb) {
			super(name, graphDb);
		}
		
	}

	public static class DoublePropertyType extends ComparablePropertyType<Double>{

		public DoublePropertyType(String name, GraphDatabaseService graphDb) {
			super(name, graphDb);
		}

		protected final RelationshipType propertyNameSubRef(){
			return RelTypes.DOUBLE_PROPTYPE_SUBREF;	
		}

		@Override
		public int compare(Double value, org.neo4j.graphdb.Node node) {
			if(node.hasProperty(getName()))
				throw new RuntimeException("Node does not have property "+getName());

			Double propertyValue = (Double)node.getProperty(getName());
			
			return value.compareTo(propertyValue);
		}

		public int compare(org.neo4j.graphdb.Node node1, org.neo4j.graphdb.Node node2) {
			if(node1.hasProperty(getName()))
				throw new RuntimeException("Node does not have property "+getName());

			Double propertyValue1 = (Double)node1.getProperty(getName());

			if(node2.hasProperty(getName()))
				throw new RuntimeException("Node does not have property "+getName());

			Double propertyValue2 = (Double)node2.getProperty(getName());
			
			return propertyValue1.compareTo(propertyValue2);
		}
	}

	public static class FloatArrayPropertyType extends PropertyType<Float[]>{

		protected final RelationshipType propertyNameSubRef(){
			return RelTypes.FLOAT_ARRAY_PROPTYPE_SUBREF;	
		}

		public FloatArrayPropertyType(String name, GraphDatabaseService graphDb) {
			super(name, graphDb);
		}
		
	}

	public static class FloatPropertyType extends ComparablePropertyType<Float>{

		protected final RelationshipType propertyNameSubRef(){
			return RelTypes.FLOAT_PROPTYPE_SUBREF;	
		}
		
		public FloatPropertyType(String name, GraphDatabaseService graphDb) {
			super(name, graphDb);
		}

		@Override
		public int compare(Float value, org.neo4j.graphdb.Node node) {
			if(node.hasProperty(getName()))
				throw new RuntimeException("Node does not have property "+getName());

			Float propertyValue = (Float)node.getProperty(getName());
			
			return value.compareTo(propertyValue);
		}

		public int compare(org.neo4j.graphdb.Node node1, org.neo4j.graphdb.Node node2) {
			if(node1.hasProperty(getName()))
				throw new RuntimeException("Node does not have property "+getName());

			Float propertyValue1 = (Float)node1.getProperty(getName());

			if(node2.hasProperty(getName()))
				throw new RuntimeException("Node does not have property "+getName());

			Float propertyValue2 = (Float)node2.getProperty(getName());
			
			return propertyValue1.compareTo(propertyValue2);
		}
	}

	public static PropertyType<Integer[]> getIntegerArrayPropertyType(String name, GraphDatabaseService graphDb) {
		return new IntegerArrayPropertyType(name, graphDb);
	}

	private static class IntegerArrayPropertyType extends PropertyType<Integer[]>{

		protected final RelationshipType propertyNameSubRef(){
			return RelTypes.INTEGER_ARRAY_PROPTYPE_SUBREF;	
		}
		
		public IntegerArrayPropertyType(String name, GraphDatabaseService graphDb) {
			super(name, graphDb);
		}
		
	}

	public static class IntegerPropertyType extends ComparablePropertyType<Integer>{

		protected final RelationshipType propertyNameSubRef(){
			return RelTypes.INTEGER_PROPTYPE_SUBREF;	
		}
		
		public IntegerPropertyType(String name, GraphDatabaseService graphDb) {
			super(name, graphDb);
		}

		@Override
		public int compare(Integer value, org.neo4j.graphdb.Node node) {
			if(node.hasProperty(getName()))
				throw new RuntimeException("Node does not have property "+getName());

			Integer propertyValue = (Integer)node.getProperty(getName());
			
			return value.compareTo(propertyValue);
		}

		public int compare(org.neo4j.graphdb.Node node1, org.neo4j.graphdb.Node node2) {
			if(node1.hasProperty(getName()))
				throw new RuntimeException("Node does not have property "+getName());

			Integer propertyValue1 = (Integer)node1.getProperty(getName());

			if(node2.hasProperty(getName()))
				throw new RuntimeException("Node does not have property "+getName());

			Integer propertyValue2 = (Integer)node2.getProperty(getName());
			
			return propertyValue1.compareTo(propertyValue2);
		}
	}

	public static class LongArrayPropertyType extends PropertyType<Long[]>{

		protected final RelationshipType propertyNameSubRef(){
			return RelTypes.LONG_ARRAY_PROPTYPE_SUBREF;	
		}

		public LongArrayPropertyType(String name, GraphDatabaseService graphDb) {
			super(name, graphDb);
		}
	}

	public static class LongPropertyType extends ComparablePropertyType<Long>{

		public LongPropertyType(String name, GraphDatabaseService graphDb) {
			super(name, graphDb);
		}

		protected final RelationshipType propertyNameSubRef(){
			return RelTypes.LONG_PROPTYPE_SUBREF;	
		}

		@Override
		public int compare(Long value, org.neo4j.graphdb.Node node) {
			if(node.hasProperty(getName()))
				throw new RuntimeException("Node does not have property "+getName());

			Long propertyValue = (Long)node.getProperty(getName());
			
			return value.compareTo(propertyValue);
		}

		public int compare(org.neo4j.graphdb.Node node1, org.neo4j.graphdb.Node node2) {
			if(node1.hasProperty(getName()))
				throw new RuntimeException("Node does not have property "+getName());

			Long propertyValue1 = (Long)node1.getProperty(getName());

			if(node2.hasProperty(getName()))
				throw new RuntimeException("Node does not have property "+getName());

			Long propertyValue2 = (Long)node2.getProperty(getName());
			
			return propertyValue1.compareTo(propertyValue2);
		}
	}

	public static class ShortArrayPropertyType extends PropertyType<Short[]>{

		public ShortArrayPropertyType(String name, GraphDatabaseService graphDb) {
			super(name, graphDb);
		}

		protected final RelationshipType propertyNameSubRef(){
			return RelTypes.SHORT_ARRAY_PROPTYPE_SUBREF;	
		}

	}

	public static class ShortPropertyType extends ComparablePropertyType<Short>{

		public ShortPropertyType(String name, GraphDatabaseService graphDb) {
			super(name, graphDb);
		}

		protected final RelationshipType propertyNameSubRef(){
			return RelTypes.SHORT_PROPTYPE_SUBREF;	
		}

		@Override
		public int compare(Short value, org.neo4j.graphdb.Node node) {
			if(node.hasProperty(getName()))
				throw new RuntimeException("Node does not have property "+getName());

			Short propertyValue = (Short)node.getProperty(getName());
			
			return value.compareTo(propertyValue);
		}

		public int compare(org.neo4j.graphdb.Node node1, org.neo4j.graphdb.Node node2) {
			if(node1.hasProperty(getName()))
				throw new RuntimeException("Node does not have property "+getName());

			Short propertyValue1 = (Short)node1.getProperty(getName());

			if(node2.hasProperty(getName()))
				throw new RuntimeException("Node does not have property "+getName());

			Short propertyValue2 = (Short)node2.getProperty(getName());
			
			return propertyValue1.compareTo(propertyValue2);
		}
	}

	public static class StringArrayPropertyType extends PropertyType<String[]>{

		public StringArrayPropertyType(String name, GraphDatabaseService graphDb) {
			super(name, graphDb);
		}
		
		protected final RelationshipType propertyNameSubRef(){
			return RelTypes.STRING_ARRAY_PROPTYPE_SUBREF;	
		}
		
	}

	public static class StringPropertyType extends ComparablePropertyType<String>{

		public StringPropertyType(String name, GraphDatabaseService graphDb) {
			super(name, graphDb);
		}

		protected final RelationshipType propertyNameSubRef(){
			return RelTypes.STRING_PROPTYPE_SUBREF;	
		}
		
		@Override
		public int compare(String value, org.neo4j.graphdb.Node node) {
			if(node.hasProperty(getName()))
				throw new RuntimeException("Node does not have property "+getName());

			String propertyValue = (String)node.getProperty(getName());
			
			return value.compareTo(propertyValue);
		}
		
		@Override
		public int compare(org.neo4j.graphdb.Node node1, org.neo4j.graphdb.Node node2) {
			if(node1.hasProperty(getName()))
				throw new RuntimeException("Node does not have property "+getName());

			String propertyValue1 = (String)node1.getProperty(getName());

			if(node2.hasProperty(getName()))
				throw new RuntimeException("Node does not have property "+getName());

			String propertyValue2 = (String)node2.getProperty(getName());
			
			return propertyValue1.compareTo(propertyValue2);
		}
	}
}
