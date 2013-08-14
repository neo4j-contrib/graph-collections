/**
 * Copyright (c) 2002-2013 "Neo Technology,"
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

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.neo4j.collections.NodeCollection;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipExpander;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.traversal.Evaluation;
import org.neo4j.graphdb.traversal.Evaluator;
import org.neo4j.kernel.Traversal;

public class IndexedRelationshipExpander implements RelationshipExpander
{
    private final Direction direction;
    private final RelationshipType relType;
    private final GraphDatabaseService graphDb;

    public IndexedRelationshipExpander( GraphDatabaseService graphDb, Direction direction, RelationshipType relType )
    {
        this.direction = direction;
        this.relType = relType;
        this.graphDb = graphDb;
    }

    class RelationshipIterable implements Iterable<Relationship>
    {
        final Node node;

        RelationshipIterable( Node node )
        {
            this.node = node;
        }

        @Override
        public Iterator<Relationship> iterator()
        {
            return new RelationshipIterator( node, relType, direction );
        }
    }

    class RelationshipIterator implements Iterator<Relationship>
    {
        final Iterator<Relationship> directRelationshipIterator;
        final Iterator<Relationship> indexedRelationshipIterator;
        final Iterator<Path> indexedRelationshipDestinationIterator;

        boolean inIndexedRelationshipIterator = false;

        RelationshipIterator( Node node, final RelationshipType relType, final Direction direction )
        {
            if ( node.hasRelationship( relType, direction ) )
            {
                directRelationshipIterator = node.getRelationships( relType, direction ).iterator();
            }
            else
            {
                directRelationshipIterator = null;
            }

            IndexedRelationship indexedRelationship = new IndexedRelationship( node, relType, direction );
            if ( !indexedRelationship.exists() )
            {
                indexedRelationshipIterator = null;
            }
            else
            {
                indexedRelationshipIterator = indexedRelationship.iterator();
            }

            indexedRelationshipDestinationIterator = Traversal.description().depthFirst()
                .evaluator( new Evaluator()
                {
                    @Override
                    public Evaluation evaluate( Path path )
                    {
                        if ( path.length() == 0 )
                        {
                            return Evaluation.EXCLUDE_AND_CONTINUE;
                        }

                        Relationship relationship = path.lastRelationship();
                        if ( path.length() == 1 )
                        {
                            if ( relationship.getType().name().equals( NodeCollection.RelationshipTypes.VALUE.name() ) ) {
                                String relationshipType = (String) relationship.getProperty(
                                    IndexedRelationship.RELATIONSHIP_TYPE, null );
                                String relationshipDirection = (String) relationship.getProperty(
                                    IndexedRelationship.RELATIONSHIP_DIRECTION, null );

                                if ( relType.name().equals( relationshipType ) &&
                                    direction.reverse().name().equals( relationshipDirection ) )
                                {
                                    return Evaluation.EXCLUDE_AND_CONTINUE;
                                }
                            }

                            return Evaluation.EXCLUDE_AND_PRUNE;
                        }

                        if ( relationship.getType().name().equals( IndexedRelationship.RelationshipTypes.NODE_COLLECTION.name() ) )
                        {
                            return Evaluation.INCLUDE_AND_PRUNE;
                        }
                        
                        return Evaluation.EXCLUDE_AND_CONTINUE;
                    }
                } ).traverse( node ).iterator();
        }

        @Override
        public boolean hasNext()
        {
            if ( directRelationshipIterator != null && directRelationshipIterator.hasNext() )
            {
                return true;
            }
            if ( indexedRelationshipIterator != null && indexedRelationshipIterator.hasNext() )
            {
                return true;
            }
            if ( indexedRelationshipDestinationIterator != null && indexedRelationshipDestinationIterator.hasNext() )
            {
                return true;
            }
            return false;
        }

        @Override
        public Relationship next()
        {
            if ( directRelationshipIterator != null && directRelationshipIterator.hasNext() )
            {
                return directRelationshipIterator.next();
            }
            if ( indexedRelationshipIterator != null && indexedRelationshipIterator.hasNext() )
            {
                return indexedRelationshipIterator.next();
            }
            if ( indexedRelationshipDestinationIterator != null && indexedRelationshipDestinationIterator.hasNext() )
            {
                Path path = indexedRelationshipDestinationIterator.next();
                String direction = (String) path.lastRelationship().getProperty(
                    IndexedRelationship.RELATIONSHIP_DIRECTION );
                try
                {
                    IndexedRelationship indexedRelationship = new IndexedRelationship( path.endNode(), relType,
                        Direction.valueOf( direction ) );
                    return indexedRelationship.getRelationship( path.relationships().iterator().next() );
                }
                catch ( Exception e )
                {
                    throw new RuntimeException( "Comparator class cannot be instantiated" );
                }
            }
            throw new NoSuchElementException();
        }

        @Override
        public void remove()
        {
            if ( directRelationshipIterator != null && !inIndexedRelationshipIterator )
            {
                directRelationshipIterator.remove();
            }
            else
            {
                if ( indexedRelationshipIterator != null )
                {
                    indexedRelationshipIterator.remove();
                }
            }
        }
    }

    @Override
    public Iterable<Relationship> expand( Node node )
    {
        return new RelationshipIterable( node );
    }

    @Override
    public RelationshipExpander reversed()
    {
        return new IndexedRelationshipExpander( graphDb, direction.reverse(), relType );
    }
}