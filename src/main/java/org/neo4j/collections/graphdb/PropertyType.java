/**
 * Copyright (c) 2002-2011 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.collections.graphdb;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.collections.graphdb.impl.EdgeTypeImpl;
import org.neo4j.collections.graphdb.impl.PropertyImpl;
import org.neo4j.collections.graphdb.impl.VertexTypeImpl;

public abstract class PropertyType<T> extends EdgeTypeImpl{

	private PropertyType(Node node) {
		super(node);
	}

	public boolean hasProperty(Vertex vertex){
		return vertex.getPropertyContainer().hasProperty(getName());
	}
	
	@SuppressWarnings("unchecked")
	public T getPropertyValue(Vertex vertex){
		return (T)vertex.getPropertyContainer().getProperty(getName());
	}

	public Property<T> getProperty(Vertex vertex){
		return new PropertyImpl<T>(getDb(), vertex, this);
	}

	
	public Property<T> setProperty(Vertex vertex, T value){
		vertex.getPropertyContainer().setProperty(getName(), value);
		return getProperty(vertex);
	}
	
	@SuppressWarnings("unchecked")
	public T removeProperty(Vertex vertex) {
		return (T)vertex.getPropertyContainer().removeProperty(this.getName());
	}
	
	@Override
	public PropertyContainer getPropertyContainer() {
		return getNode();
	}

	public Connector<BijectiveConnectionMode> getPropertyConnector(){
		return new PropertyConnector(PropertyConnectorType.getOrCreateInstance(getDb()), this);		
	}
	
	@Override
	public Set<Connector<?>> getConnectors() {
		Set<Connector<?>> roles = new HashSet<Connector<?>>();
		roles.add(getPropertyConnector());
		return roles;
	}
	
	public PropertyConnectorType getRole(String name) {
		if(name.equals(PropertyConnectorType.getOrCreateInstance(getDb()).getName())){
			return PropertyConnectorType.getOrCreateInstance(getDb());
		}else{
			return null;
		}
	}

	@Override
	public <U extends ConnectionMode> Connector<U> getConnector(
			ConnectorType<U> edgeRoleType) {
		return new Connector<U>(edgeRoleType, this);
	}
	
	
	public static PropertyType<?> getPropertyTypeByName(DatabaseService db, String name){
		return (PropertyType<?>)VertexTypeImpl.getByName(db, name);
	}
	
	public static abstract class ComparablePropertyType<T> extends PropertyType<T> implements Comparator<org.neo4j.graphdb.Node>, PropertyComparator<T>{

		ComparablePropertyType(Node node) {
			super(node);
		}
		
		public abstract int compare(org.neo4j.graphdb.Node node1, org.neo4j.graphdb.Node node2);
		
		public abstract int compare(T value, org.neo4j.graphdb.Node node);
	}

	public static class BooleanArrayPropertyType extends PropertyType<Boolean[]>{

		public BooleanArrayPropertyType(Node node){
			super(node);
		}

		private static Class<?> getImplementationClass(){
			try{
				return Class.forName("org.neo4j.collections.graphdb.PropertyType$BooleanArrayPropertyType");
			}catch(ClassNotFoundException cce){
				throw new RuntimeException(cce);
			}
		}
		
		public static BooleanArrayPropertyType getOrCreateInstance(DatabaseService db, String name) {
			VertexTypeImpl vertexType = new VertexTypeImpl(getOrCreateByDescriptor(new TypeNodeDescriptor(db, name, getImplementationClass())));
			return new BooleanArrayPropertyType(vertexType.getNode());
		}

	}
	
	public static class BooleanPropertyType extends PropertyType<Boolean>{

		public BooleanPropertyType(Node node){
			super(node);
		}

		private static Class<?> getImplementationClass(){
			try{
				return Class.forName("org.neo4j.collections.graphdb.PropertyType$BooleanPropertyType");
			}catch(ClassNotFoundException cce){
				throw new RuntimeException(cce);
			}
		}
		
		public static BooleanPropertyType getOrCreateInstance(DatabaseService db, String name) {
			VertexTypeImpl vertexType = new VertexTypeImpl(getOrCreateByDescriptor(new TypeNodeDescriptor(db, name, getImplementationClass())));
			return new BooleanPropertyType(vertexType.getNode());
		}
	}
	
	public static class ByteArrayPropertyType extends PropertyType<Byte[]>{

		public ByteArrayPropertyType(Node node){
			super(node);
		}

		private static Class<?> getImplementationClass(){
			try{
				return Class.forName("org.neo4j.collections.graphdb.PropertyType$ByteArrayPropertyType");
			}catch(ClassNotFoundException cce){
				throw new RuntimeException(cce);
			}
		}

		public static ByteArrayPropertyType getOrCreateInstance(DatabaseService db, String name) {
			VertexTypeImpl vertexType = new VertexTypeImpl(getOrCreateByDescriptor(new TypeNodeDescriptor(db, name, getImplementationClass())));
			return new ByteArrayPropertyType(vertexType.getNode());
		}
		
	}

	public static class BytePropertyType extends ComparablePropertyType<Byte>{

		public BytePropertyType(Node node){
			super(node);
		}

		private static Class<?> getImplementationClass(){
			try{
				return Class.forName("org.neo4j.collections.graphdb.PropertyType$BytePropertyType");
			}catch(ClassNotFoundException cce){
				throw new RuntimeException(cce);
			}
		}
		
		public static BytePropertyType getOrCreateInstance(DatabaseService db, String name) {
			VertexTypeImpl vertexType = new VertexTypeImpl(getOrCreateByDescriptor(new TypeNodeDescriptor(db, name, getImplementationClass())));
			return new BytePropertyType(vertexType.getNode());
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

		public DoubleArrayPropertyType(Node node){
			super(node);
		}

		private static Class<?> getImplementationClass(){
			try{
				return Class.forName("org.neo4j.collections.graphdb.PropertyType$DoubleArrayPropertyType");
			}catch(ClassNotFoundException cce){
				throw new RuntimeException(cce);
			}
		}
		
		public static DoubleArrayPropertyType getOrCreateInstance(DatabaseService db, String name) {
			VertexTypeImpl vertexType = new VertexTypeImpl(getOrCreateByDescriptor(new TypeNodeDescriptor(db, name, getImplementationClass())));
			return new DoubleArrayPropertyType(vertexType.getNode());
		}
		
	}

	public static class DoublePropertyType extends ComparablePropertyType<Double>{

		public DoublePropertyType(Node node){
			super(node);
		}

		private static Class<?> getImplementationClass(){
			try{
				return Class.forName("org.neo4j.collections.graphdb.PropertyType$DoublePropertyType");
			}catch(ClassNotFoundException cce){
				throw new RuntimeException(cce);
			}
		}

		public static DoublePropertyType getOrCreateInstance(DatabaseService db, String name) {
			VertexTypeImpl vertexType = new VertexTypeImpl(getOrCreateByDescriptor(new TypeNodeDescriptor(db, name, getImplementationClass())));
			return new DoublePropertyType(vertexType.getNode());
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

		public FloatArrayPropertyType(Node node){
			super(node);
		}

		private static Class<?> getImplementationClass(){
			try{
				return Class.forName("org.neo4j.collections.graphdb.PropertyType$FloatArrayPropertyType");
			}catch(ClassNotFoundException cce){
				throw new RuntimeException(cce);
			}
		}
		
		public static FloatArrayPropertyType getOrCreateInstance(DatabaseService db, String name) {
			VertexTypeImpl vertexType = new VertexTypeImpl(getOrCreateByDescriptor(new TypeNodeDescriptor(db, name, getImplementationClass())));
			return new FloatArrayPropertyType(vertexType.getNode());
		}
		
	}

	public static class FloatPropertyType extends ComparablePropertyType<Float>{

		public FloatPropertyType(Node node){
			super(node);
		}

		private static Class<?> getImplementationClass(){
			try{
				return Class.forName("org.neo4j.collections.graphdb.PropertyType$FloatPropertyType");
			}catch(ClassNotFoundException cce){
				throw new RuntimeException(cce);
			}
		}

		public static FloatPropertyType getOrCreateInstance(DatabaseService db, String name) {
			VertexTypeImpl vertexType = new VertexTypeImpl(getOrCreateByDescriptor(new TypeNodeDescriptor(db, name, getImplementationClass())));
			return new FloatPropertyType(vertexType.getNode());
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

	public static class IntegerArrayPropertyType extends PropertyType<Integer[]>{

		public IntegerArrayPropertyType(Node node){
			super(node);
		}
		
		private static Class<?> getImplementationClass(){
			try{
				return Class.forName("org.neo4j.collections.graphdb.PropertyType$IntegerArrayPropertyType");
			}catch(ClassNotFoundException cce){
				throw new RuntimeException(cce);
			}
		}

		public static IntegerArrayPropertyType getOrCreateInstance(DatabaseService db, String name) {
			VertexTypeImpl vertexType = new VertexTypeImpl(getOrCreateByDescriptor(new TypeNodeDescriptor(db, name, getImplementationClass())));
			return new IntegerArrayPropertyType(vertexType.getNode());
		}
		
	}

	public static class IntegerPropertyType extends ComparablePropertyType<Integer>{


		public IntegerPropertyType(Node node){
			super(node);
		}

		private static Class<?> getImplementationClass(){
			try{
				return Class.forName("org.neo4j.collections.graphdb.PropertyType$IntegerPropertyType");
			}catch(ClassNotFoundException cce){
				throw new RuntimeException(cce);
			}
		}

		public static IntegerPropertyType getOrCreateInstance(DatabaseService db, String name) {
			VertexTypeImpl vertexType = new VertexTypeImpl(getOrCreateByDescriptor(new TypeNodeDescriptor(db, name, getImplementationClass())));
			return new IntegerPropertyType(vertexType.getNode());
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

		public LongArrayPropertyType(Node node){
			super(node);
		}
		
		private static Class<?> getImplementationClass(){
			try{
				return Class.forName("org.neo4j.collections.graphdb.PropertyType$LongArrayPropertyType");
			}catch(ClassNotFoundException cce){
				throw new RuntimeException(cce);
			}
		}

		public static LongArrayPropertyType getOrCreateInstance(DatabaseService db, String name) {
			VertexTypeImpl vertexType = new VertexTypeImpl(getOrCreateByDescriptor(new TypeNodeDescriptor(db, name, getImplementationClass())));
			return new LongArrayPropertyType(vertexType.getNode());
		}
	}

	public static class LongPropertyType extends ComparablePropertyType<Long>{

		public LongPropertyType(Node node){
			super(node);
		}
		
		public static LongPropertyType getOrCreateInstance(DatabaseService db, String name) {
			VertexTypeImpl vertexType = new VertexTypeImpl(getOrCreateByDescriptor(new TypeNodeDescriptor(db, name, getImplementationClass())));
			return new LongPropertyType(vertexType.getNode());
		}

		private static Class<?> getImplementationClass(){
			try{
				return Class.forName("org.neo4j.collections.graphdb.PropertyType$LongPropertyType");
			}catch(ClassNotFoundException cce){
				throw new RuntimeException(cce);
			}
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

		public ShortArrayPropertyType(Node node){
			super(node);
		}
		
		private static Class<?> getImplementationClass(){
			try{
				return Class.forName("org.neo4j.collections.graphdb.PropertyType$ShortArrayPropertyType");
			}catch(ClassNotFoundException cce){
				throw new RuntimeException(cce);
			}
		}

		public static ShortArrayPropertyType getOrCreateInstance(DatabaseService db, String name) {
			VertexTypeImpl vertexType = new VertexTypeImpl(getOrCreateByDescriptor(new TypeNodeDescriptor(db, name, getImplementationClass())));
			return new ShortArrayPropertyType(vertexType.getNode());
		}

	}

	public static class ShortPropertyType extends ComparablePropertyType<Short>{

		public ShortPropertyType(Node node){
			super(node);
		}
		
		public static ShortPropertyType getOrCreateInstance(DatabaseService db, String name) {
			VertexTypeImpl vertexType = new VertexTypeImpl(getOrCreateByDescriptor(new TypeNodeDescriptor(db, name, getImplementationClass())));
			return new ShortPropertyType(vertexType.getNode());
		}

		private static Class<?> getImplementationClass(){
			try{
				return Class.forName("org.neo4j.collections.graphdb.PropertyType$ShortPropertyType");
			}catch(ClassNotFoundException cce){
				throw new RuntimeException(cce);
			}
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

		public StringArrayPropertyType(Node node){
			super(node);
		}
		
		private static Class<?> getImplementationClass(){
			try{
				return Class.forName("org.neo4j.collections.graphdb.PropertyType$StringArrayPropertyType");
			}catch(ClassNotFoundException cce){
				throw new RuntimeException(cce);
			}
		}

		public static StringArrayPropertyType getOrCreateInstance(DatabaseService db, String name) {
			VertexTypeImpl vertexType = new VertexTypeImpl(getOrCreateByDescriptor(new TypeNodeDescriptor(db, name, getImplementationClass())));
			return new StringArrayPropertyType(vertexType.getNode());
		}
	}

	public static class StringPropertyType extends ComparablePropertyType<String>{

		public StringPropertyType(Node node){
			super(node);
		}

		public static StringPropertyType getOrCreateInstance(DatabaseService db, String name) {
			VertexTypeImpl vertexType = new VertexTypeImpl(getOrCreateByDescriptor(new TypeNodeDescriptor(db, name, getImplementationClass())));
			return new StringPropertyType(vertexType.getNode());
		}

		private static Class<?> getImplementationClass(){
			try{
				return Class.forName("org.neo4j.collections.graphdb.PropertyType$StringPropertyType");
			}catch(ClassNotFoundException cce){
				throw new RuntimeException(cce);
			}
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
	
	public static class VertexPropertyType extends PropertyType<Vertex>{

		public VertexPropertyType(Node node){
			super(node);
		}
		
		private static Class<?> getImplementationClass(){
			try{
				return Class.forName("org.neo4j.collections.graphdb.PropertyType$VertexPropertyType");
			}catch(ClassNotFoundException cce){
				throw new RuntimeException(cce);
			}
		}

		public static VertexPropertyType getOrCreateInstance(DatabaseService db, String name) {
			VertexTypeImpl vertexType = new VertexTypeImpl(getOrCreateByDescriptor(new TypeNodeDescriptor(db, name, getImplementationClass())));
			return new VertexPropertyType(vertexType.getNode());
		}
		
		public boolean hasProperty(Vertex vertex){
			return vertex.getPropertyContainer().hasProperty(getName());
		}
		
		public Vertex getPropertyValue(Vertex vertex){
			return getDb().getVertex(getDb().getNodeById((Long)vertex.getPropertyContainer().getProperty(getName())));
		}

		public Property<Vertex> getProperty(Vertex vertex){
			return new PropertyImpl<Vertex>(getDb(), vertex, this);
		}

		
		public Property<Vertex> setProperty(Vertex vertex, Vertex value){
			vertex.getPropertyContainer().setProperty(getName(), value.getNode().getId());
			return getProperty(vertex);
		}
		
		public Vertex removeProperty(Vertex vertex) {
			return getDb().getVertex(getDb().getNodeById((Long)vertex.getPropertyContainer().removeProperty(getName())));
		}

	}

	
}
