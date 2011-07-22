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

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.RelationshipType;

public abstract class PropertyType<T> implements RelationshipContainer{

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
	
	private Node node = null;
	
	private final String nm; 
	protected final GraphDatabaseService graphDb;
	
	public String getName(){
		return nm;
	}

	PropertyType(String name, GraphDatabaseService graphDb){
		this.nm = name;
		this.graphDb = graphDb;
	}

	@Override
	public <U> Property<U> getProperty(PropertyType<U> pt) {
		return getNode().getProperty(pt);
	}

	@Override
	public <U> U getPropertyValue(PropertyType<U> pt) {
		return getNode().getPropertyValue(pt);
	}

	@Override
	public <U> boolean hasProperty(PropertyType<U> pt) {
		return getNode().hasProperty(pt);
	}

	@Override
	public <U> U removeProperty(PropertyType<U> pt) {
		return getNode().removeProperty(pt);
	}

	@Override
	public <U> void setProperty(PropertyType<U> pt, U value) {
		getNode().setProperty(pt, value);
	}

	
	
	public static Iterable<PropertyType<?>> getPropertyTypes(PropertyContainer pc, GraphDatabaseService graphDb){
		ArrayList<PropertyType<?>> propertyTypes = new ArrayList<PropertyType<?>>();
		for(String key: pc.getPropertyContainer().getPropertyKeys()){
			for(org.neo4j.graphdb.RelationshipType relType: RelTypes.values()){
				if(!relType.equals(RelTypes.PROPTYPE_SUBREF)){
					Node typeSubRef = getTypeSubRef(graphDb, relType);
					if(typeSubRef.hasProperty(key)){
						if(relType.equals(RelTypes.BOOLEAN_ARRAY_PROPTYPE_SUBREF))
							propertyTypes.add(getBooleanArrayPropertyType(key, graphDb));
						else if(relType.equals(RelTypes.BOOLEAN_PROPTYPE_SUBREF))
							propertyTypes.add(getBooleanPropertyType(key, graphDb));
						else if(relType.equals(RelTypes.BYTE_ARRAY_PROPTYPE_SUBREF))
							propertyTypes.add(getByteArrayPropertyType(key, graphDb));
						else if(relType.equals(RelTypes.BYTE_PROPTYPE_SUBREF))
							propertyTypes.add(getBytePropertyType(key, graphDb));
						else if(relType.equals(RelTypes.DOUBLE_PROPTYPE_SUBREF))
							propertyTypes.add(getDoublePropertyType(key, graphDb));
						else if(relType.equals(RelTypes.DOUBLE_ARRAY_PROPTYPE_SUBREF))
							propertyTypes.add(getDoubleArrayPropertyType(key, graphDb));
						else if(relType.equals(RelTypes.FLOAT_PROPTYPE_SUBREF))
							propertyTypes.add(getFloatPropertyType(key, graphDb));
						else if(relType.equals(RelTypes.FLOAT_ARRAY_PROPTYPE_SUBREF))
							propertyTypes.add(getFloatArrayPropertyType(key, graphDb));
						else if(relType.equals(RelTypes.INTEGER_PROPTYPE_SUBREF))
							propertyTypes.add(getIntegerPropertyType(key, graphDb));
						else if(relType.equals(RelTypes.INTEGER_ARRAY_PROPTYPE_SUBREF))
							propertyTypes.add(getIntegerArrayPropertyType(key, graphDb));
						else if(relType.equals(RelTypes.LONG_PROPTYPE_SUBREF))
							propertyTypes.add(getFloatPropertyType(key, graphDb));
						else if(relType.equals(RelTypes.LONG_ARRAY_PROPTYPE_SUBREF))
							propertyTypes.add(getFloatArrayPropertyType(key, graphDb));
						else if(relType.equals(RelTypes.SHORT_PROPTYPE_SUBREF))
							propertyTypes.add(getFloatPropertyType(key, graphDb));
						else if(relType.equals(RelTypes.SHORT_ARRAY_PROPTYPE_SUBREF))
							propertyTypes.add(getFloatArrayPropertyType(key, graphDb));
						else if(relType.equals(RelTypes.STRING_PROPTYPE_SUBREF))
							propertyTypes.add(getFloatPropertyType(key, graphDb));
						else if(relType.equals(RelTypes.STRING_ARRAY_PROPTYPE_SUBREF))
							propertyTypes.add(getFloatArrayPropertyType(key, graphDb));
					}
				}
			}
		}
		return propertyTypes;
	}
	
	@Override
	public Iterable<PropertyType<?>> getPropertyTypes() {
		return getPropertyTypes(this, getGraphDatabaseExt());
	}

	@Override
	public Relationship createRelationshipToExt(RelationshipContainer n,
			RelationshipType rt) {
		return getNode().createRelationshipToExt(n, rt);
	}

	@Override
	public Iterable<Relationship> getRelationshipsExt() {
		return getNode().getRelationshipsExt();
	}

	@Override
	public Iterable<Relationship> getRelationshipsExt(RelationshipType... relTypes) {
		return getNode().getRelationshipsExt(relTypes);
	}

	@Override
	public Iterable<Relationship> getRelationshipsExt(Direction dir) {
		return getNode().getRelationshipsExt(dir);
	}

	@Override
	public Iterable<Relationship> getRelationshipsExt(Direction dir,
			RelationshipType... relTypes) {
		return getNode().getRelationshipsExt(dir, relTypes);
	}

	@Override
	public Iterable<Relationship> getRelationshipsExt(RelationshipType relType,
			Direction dir) {
		return getNode().getRelationshipsExt(relType, dir);
	}

	@Override
	public Relationship getSingleRelationshipExt(RelationshipType relType,
			Direction dir) {
		return getNode().getSingleRelationshipExt(relType, dir);
	}

	@Override
	public boolean hasRelationship() {
		return getNode().hasRelationship();
	}

	@Override
	public boolean hasRelationship(RelationshipType... relTypes) {
		return getNode().hasRelationship(relTypes);
	}

	@Override
	public boolean hasRelationship(Direction dir) {
		return getNode().hasRelationship(dir);
	}

	@Override
	public boolean hasRelationship(Direction dir, RelationshipType... relTypes) {
		return getNode().hasRelationship(dir, relTypes);
	}

	@Override
	public boolean hasRelationship(RelationshipType relType, Direction dir) {
		return getNode().hasRelationship(relType, dir);
	}

	protected abstract RelationshipType propertyNameSubRef();

	@Override
	public org.neo4j.graphdb.PropertyContainer getPropertyContainer() {
		return getNode();
	}

	private static Node getTypeSubRef(GraphDatabaseService graphDb, RelationshipType propertyNameSubRef){
		Relationship subRefRel = graphDb.getReferenceNodeExt().getSingleRelationshipExt(RelTypes.PROPTYPE_SUBREF, Direction.OUTGOING);
		Node subRef = null;
		if(subRefRel == null){
			Node n = graphDb.createNodeExt();
			graphDb.getReferenceNode().createRelationshipTo(n, RelTypes.PROPTYPE_SUBREF);
			subRef = n;
		}else{
			subRef = (Node)subRefRel.getEndRelationshipContainer();
		}
		Relationship typeSubRefRel = graphDb.getReferenceNodeExt().getSingleRelationshipExt(propertyNameSubRef, Direction.OUTGOING);
		Node typeSubRef = null;
		if(typeSubRefRel == null){
			Node n = graphDb.createNodeExt();
			subRef.createRelationshipTo(n, propertyNameSubRef);
			typeSubRef = n;
		}else{
			typeSubRef = (Node)subRefRel.getEndRelationshipContainer();
		}
		return typeSubRef;
	}
	
	
	@Override
	public Node getNode(){
		if(node == null){
			for(org.neo4j.graphdb.RelationshipType relType: RelTypes.values()){
				Node typeSubRef = getTypeSubRef(getGraphDatabaseExt(), relType);
				if(typeSubRef.hasProperty(getName())){
					if(relType.equals(propertyNameSubRef())){
						node = getGraphDatabaseExt().getNodeByIdExt((Long)typeSubRef.getProperty(getName()));
						if(node == null){
							typeSubRef.removeProperty(getName());
						}else {
							return node;
						}
					}else{
						throw new RuntimeException("PropertyType already exists with different data type");
					}
				}
			}
			node = getGraphDatabaseExt().createNodeExt(); 
			Node typeSubRef = getTypeSubRef(getGraphDatabaseExt(), propertyNameSubRef());
			typeSubRef.setProperty(getName(), node.getId());
		}
		return node;
	}

	@Override
	public GraphDatabaseService getGraphDatabaseExt() {
		return graphDb;
	}
	
	
	public static PropertyType<Boolean[]> getBooleanArrayPropertyType(String name, GraphDatabaseService graphDb) {
		return new BooleanArrayProperty(name, graphDb);
	}
	
	private static class BooleanArrayProperty extends PropertyType<Boolean[]>{

		protected RelationshipType propertyNameSubRef(){
			return RelTypes.BOOLEAN_ARRAY_PROPTYPE_SUBREF;	
		}

		BooleanArrayProperty(String name, GraphDatabaseService graphDb) {
			super(name, graphDb);
		}
	}
	
	public static PropertyType<Boolean> getBooleanPropertyType(String name, GraphDatabaseService graphDb) {
		return new BooleanProperty(name, graphDb);
	}

	private static class BooleanProperty extends PropertyType<Boolean>{

		protected RelationshipType propertyNameSubRef(){
			return RelTypes.BOOLEAN_PROPTYPE_SUBREF;	
		}
		
		BooleanProperty(String name, GraphDatabaseService graphDb) {
			super(name, graphDb);
		}
	}
	
	public static PropertyType<Byte[]> getByteArrayPropertyType(String name, GraphDatabaseService graphDb) {
		return new ByteArrayProperty(name, graphDb);
	}
	
	private static class ByteArrayProperty extends PropertyType<Byte[]>{

		protected RelationshipType propertyNameSubRef(){
			return RelTypes.BYTE_ARRAY_PROPTYPE_SUBREF;	
		}

		ByteArrayProperty(String name, GraphDatabaseService graphDb) {
			super(name, graphDb);
		}
		
	}

	public static ComparablePropertyType<Byte> getBytePropertyType(String name, GraphDatabaseService graphDb) {
		return new ByteProperty(name, graphDb);
	}

	private static class ByteProperty extends ComparablePropertyType<Byte>{

		protected RelationshipType propertyNameSubRef(){
			return RelTypes.BYTE_PROPTYPE_SUBREF;	
		}

		ByteProperty(String name, GraphDatabaseService graphDb) {
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

	public static PropertyType<Double[]> getDoubleArrayPropertyType(String name, GraphDatabaseService graphDb) {
		return new DoubleArrayProperty(name, graphDb);
	}

	private static class DoubleArrayProperty extends PropertyType<Double[]>{

		protected RelationshipType propertyNameSubRef(){
			return RelTypes.DOUBLE_ARRAY_PROPTYPE_SUBREF;	
		}

		DoubleArrayProperty(String name, GraphDatabaseService graphDb) {
			super(name, graphDb);
		}
		
	}

	public static ComparablePropertyType<Double> getDoublePropertyType(String name, GraphDatabaseService graphDb) {
		return new DoubleProperty(name, graphDb);
	}

	
	private static class DoubleProperty extends ComparablePropertyType<Double>{

		DoubleProperty(String name, GraphDatabaseService graphDb) {
			super(name, graphDb);
		}

		protected RelationshipType propertyNameSubRef(){
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

	public static PropertyType<Float[]> getFloatArrayPropertyType(String name, GraphDatabaseService graphDb) {
		return new FloatArrayProperty(name, graphDb);
	}

	
	private static class FloatArrayProperty extends PropertyType<Float[]>{

		protected RelationshipType propertyNameSubRef(){
			return RelTypes.FLOAT_ARRAY_PROPTYPE_SUBREF;	
		}

		FloatArrayProperty(String name, GraphDatabaseService graphDb) {
			super(name, graphDb);
		}
		
	}

	public static ComparablePropertyType<Float> getFloatPropertyType(String name, GraphDatabaseService graphDb) {
		return new FloatProperty(name, graphDb);
	}

	private static class FloatProperty extends ComparablePropertyType<Float>{

		protected RelationshipType propertyNameSubRef(){
			return RelTypes.FLOAT_PROPTYPE_SUBREF;	
		}
		
		public FloatProperty(String name, GraphDatabaseService graphDb) {
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
		return new IntegerArrayProperty(name, graphDb);
	}

	private static class IntegerArrayProperty extends PropertyType<Integer[]>{

		protected RelationshipType propertyNameSubRef(){
			return RelTypes.INTEGER_ARRAY_PROPTYPE_SUBREF;	
		}
		
		IntegerArrayProperty(String name, GraphDatabaseService graphDb) {
			super(name, graphDb);
		}
		
	}

	public static ComparablePropertyType<Integer> getIntegerPropertyType(String name, GraphDatabaseService graphDb) {
		return new IntegerProperty(name, graphDb);
	}

	private static class IntegerProperty extends ComparablePropertyType<Integer>{

		protected RelationshipType propertyNameSubRef(){
			return RelTypes.INTEGER_PROPTYPE_SUBREF;	
		}
		
		public IntegerProperty(String name, GraphDatabaseService graphDb) {
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

	public static PropertyType<Long[]> getLongArrayPropertyType(String name, GraphDatabaseService graphDb) {
		return new LongArrayProperty(name, graphDb);
	}

	private static class LongArrayProperty extends PropertyType<Long[]>{

		protected RelationshipType propertyNameSubRef(){
			return RelTypes.LONG_ARRAY_PROPTYPE_SUBREF;	
		}

		LongArrayProperty(String name, GraphDatabaseService graphDb) {
			super(name, graphDb);
		}
	}

	public static ComparablePropertyType<Long> getLongPropertyType(String name, GraphDatabaseService graphDb) {
		return new LongProperty(name, graphDb);
	}

	private static class LongProperty extends ComparablePropertyType<Long>{

		public LongProperty(String name, GraphDatabaseService graphDb) {
			super(name, graphDb);
		}

		protected RelationshipType propertyNameSubRef(){
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

	public static PropertyType<Short[]> getShortArrayPropertyType(String name, GraphDatabaseService graphDb) {
		return new ShortArrayProperty(name, graphDb);
	}
	
	private static class ShortArrayProperty extends PropertyType<Short[]>{

		ShortArrayProperty(String name, GraphDatabaseService graphDb) {
			super(name, graphDb);
		}

		protected RelationshipType propertyNameSubRef(){
			return RelTypes.SHORT_ARRAY_PROPTYPE_SUBREF;	
		}

	}

	public static ComparablePropertyType<Short> getShortPropertyType(String name, GraphDatabaseService graphDb) {
		return new ShortProperty(name, graphDb);
	}
	
	private static class ShortProperty extends ComparablePropertyType<Short>{

		ShortProperty(String name, GraphDatabaseService graphDb) {
			super(name, graphDb);
		}

		protected RelationshipType propertyNameSubRef(){
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

	public static PropertyType<String[]> getStringArrayPropertyType(String name, GraphDatabaseService graphDb) {
		return new StringArrayProperty(name, graphDb);
	}

	private static class StringArrayProperty extends PropertyType<String[]>{

		StringArrayProperty(String name, GraphDatabaseService graphDb) {
			super(name, graphDb);
		}
		
		protected RelationshipType propertyNameSubRef(){
			return RelTypes.STRING_ARRAY_PROPTYPE_SUBREF;	
		}
		
	}

	public static ComparablePropertyType<String> getStringPropertyType(String name, GraphDatabaseService graphDb) {
		return new StringProperty(name, graphDb);
	}

	private static class StringProperty extends ComparablePropertyType<String>{

		public StringProperty(String name, GraphDatabaseService graphDb) {
			super(name, graphDb);
		}

		protected RelationshipType propertyNameSubRef(){
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
