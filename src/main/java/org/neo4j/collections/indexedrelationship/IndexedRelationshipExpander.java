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
package org.neo4j.collections.indexedrelationship;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.neo4j.collections.sortedtree.SortedTree;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipExpander;
import org.neo4j.graphdb.RelationshipType;

class IndexedRelationshipExpander implements RelationshipExpander {
    private final Direction direction;
    private final RelationshipType relType;
    private final GraphDatabaseService graphDb;
//    private static RelationshipIndex index = gr.index().forRelationships("myindex");

    public IndexedRelationshipExpander(GraphDatabaseService graphDb, Direction direction, RelationshipType relType) {
        this.direction = direction;
        this.relType = relType;
        this.graphDb = graphDb;
    }

    class RelationshipIterable implements Iterable<Relationship>{

    	final Node node;
    	
    	RelationshipIterable(Node node){
    		this.node = node;
    	}
    	
		@Override
		public Iterator<Relationship> iterator() {
			return new RelationshipIterator(node, relType, direction);
		}
    	
    }
    
    class RelationshipIterator implements Iterator<Relationship>{

    	final Iterator<Relationship> directRelationshipIterator;
    	final Iterator<Relationship> indexedRelationshipIterator;
    	
    	boolean inIndexedRelationshipIterator = false;
    	
    	RelationshipIterator(Node node, RelationshipType relType, Direction direction){
    		if(node.hasRelationship(relType, direction)){
    			directRelationshipIterator = node.getRelationships(relType, direction).iterator();
    		}else{
    			directRelationshipIterator = null;
    		}
    		Iterable<Relationship> indexRels = node.getRelationships(SortedTree.RelTypes.TREE_ROOT);
    		Relationship ir = null;
    		for(Relationship indexRel: indexRels){
    			String relName = (String)indexRel.getProperty(SortedTree.TREE_NAME);
    			if(relName.equals(relType.name())){
    				if(indexRel.hasProperty(IndexedRelationship.directionPropertyName)){
    					String dir = (String)indexRel.getProperty(IndexedRelationship.directionPropertyName);
    					if(dir.equals(direction.name())){
    						ir = indexRel;
    						break;
    					}
    				}
    			}
    		}
    		if(ir == null){
    			indexedRelationshipIterator = null;
    		}else{
    			boolean isUniqueIndex = (Boolean)ir.getProperty(SortedTree.IS_UNIQUE_INDEX);
    			try{
    				Class<?> comparatorClass = Class.forName((String)ir.getProperty(SortedTree.COMPARATOR_CLASS));
    				Comparator<Node> comparator = (Comparator<Node>)comparatorClass.newInstance();
    				indexedRelationshipIterator = new IndexedRelationship(relType, direction, comparator, isUniqueIndex, node, graphDb).iterator();
    			}catch(Exception e){
    				throw new RuntimeException("Comparator class cannot be instantiated");
    			}
    		}
    		
    	}
    	
		@Override
		public boolean hasNext() {
			if(directRelationshipIterator != null){
				if(directRelationshipIterator.hasNext()){
					return true;
				}else{
					if(indexedRelationshipIterator != null){
						return indexedRelationshipIterator.hasNext();
					}else{
						return false;
					}
				}
			}else{
				if(indexedRelationshipIterator != null){
					return indexedRelationshipIterator.hasNext();
				}else{
					return false;
				}
			}
		}

		@Override
		public Relationship next() {
			if(directRelationshipIterator != null){
				if(directRelationshipIterator.hasNext()){
					return directRelationshipIterator.next();
				}else{
					if(indexedRelationshipIterator != null){
						inIndexedRelationshipIterator = true;
						if(indexedRelationshipIterator.hasNext()){
							return indexedRelationshipIterator.next();
						}else{
							throw new NoSuchElementException();
						}
					}else{
						throw new NoSuchElementException();
					}
				}
			}else{
				if(indexedRelationshipIterator != null){
					inIndexedRelationshipIterator = true;
					if(indexedRelationshipIterator.hasNext()){
						return indexedRelationshipIterator.next();
					}else{
						throw new NoSuchElementException();
					}
				}else{
					throw new NoSuchElementException();
				}
			}
		}

		@Override
		public void remove() {
			if(directRelationshipIterator != null &&  !inIndexedRelationshipIterator){
				directRelationshipIterator.remove();
			}else{
				if(indexedRelationshipIterator != null){
					indexedRelationshipIterator.remove();
				}
			}
		}
    }
    
    @Override
    public Iterable<Relationship> expand(Node node) {
    	return new RelationshipIterable(node);
    }

    @Override
    public RelationshipExpander reversed() {
       return new IndexedRelationshipExpander(graphDb, direction.reverse(), relType);
    }
}