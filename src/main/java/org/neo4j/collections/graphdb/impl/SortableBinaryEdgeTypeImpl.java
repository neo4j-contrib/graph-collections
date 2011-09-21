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
package org.neo4j.collections.graphdb.impl;

import org.neo4j.collections.graphdb.DatabaseService;
import org.neo4j.collections.graphdb.PropertyType;
import org.neo4j.collections.graphdb.SortableBinaryEdge;
import org.neo4j.collections.graphdb.Vertex;
import org.neo4j.collections.graphdb.PropertyType.ComparablePropertyType;
import org.neo4j.collections.graphdb.SortableBinaryEdgeType;
import org.neo4j.collections.indexedrelationship.IndexedRelationship;
import org.neo4j.collections.sortedtree.PropertySortedTree;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

public class SortableBinaryEdgeTypeImpl<T> extends BinaryEdgeTypeImpl implements SortableBinaryEdgeType<T>{

	public static String PROPERTY_TYPE = "org.neo4j.collections.graphdb.property_type";
	
	public SortableBinaryEdgeTypeImpl(DatabaseService db, Long id) {
		super(db, id);
	}

	protected static Class<?> getImplementationClass(){
		try{
			return Class.forName("org.neo4j.collections.graphdb.impl.SortableBinaryEdgeTypeImpl");
		}catch(ClassNotFoundException cce){
			throw new RuntimeException(cce);
		}
	}
	
	public static class SortableTypeNodeDescriptor<T> extends TypeNodeDescriptor{

		public final ComparablePropertyType<T> propertyType;
		
		SortableTypeNodeDescriptor(DatabaseService db, String name,
				Class<?> claz, ComparablePropertyType<T> propertyType) {
			super(db, name, claz);
			this.propertyType = propertyType;
		}
		
		@Override
		public void initialize(Node n){
			super.initialize(n);
			n.setProperty(PROPERTY_TYPE, propertyType.getName());
		}
	}
	
	public static <T> SortableBinaryEdgeType<T> getOrCreateInstance(DatabaseService db,
			RelationshipType relType, PropertyType.ComparablePropertyType<T> propertyType) {
		VertexTypeImpl vertexType = new VertexTypeImpl(db, getOrCreateByDescriptor(new SortableTypeNodeDescriptor<T>(db, relType.name(), getImplementationClass(), propertyType)).getId());
		return new SortableBinaryEdgeTypeImpl<T>(db, vertexType.getNode().getId());
	}

	@SuppressWarnings("unchecked")
	@Override
	public ComparablePropertyType<T> getPropertyType() {
		return (ComparablePropertyType<T>) getByName(getDb(), (String)getNode().getProperty(PROPERTY_TYPE));
	}
	
	@Override
	public SortableBinaryEdge<T> createEdge(Vertex startVertex, Vertex endVertex) {
        IndexedRelationship idxRel = new IndexedRelationship( startVertex.getNode(),
            DynamicRelationshipType.withName(this.getName()), Direction.OUTGOING );
        if ( !idxRel.exists() )
        {
            PropertySortedTree<T> propertySortedTree = new PropertySortedTree<T>( getDb().getGraphDatabaseService(),
                getPropertyType(), true, getName() );
            idxRel.create( propertySortedTree );
        }
		Relationship rel = idxRel.createRelationshipTo(endVertex.getNode());
		return new SortableBinaryEdgeImpl<T>(db, rel.getId(), idxRel);
	}

}
