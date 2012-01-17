/**
 * Copyright (c) 2002-2012 "Neo Technology,"
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
/**
 ' * Copyright (c) 2002-2011 "Neo Technology,"
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
package org.neo4j.collections.indexprovider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.cypher.commands.Query;
import org.neo4j.cypher.javacompat.CypherParser;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.test.ImpermanentGraphDatabase;
import org.neo4j.visualization.graphviz.GraphvizWriter;
import org.neo4j.walk.Walker;

public class TimelineIndexProviderTest
{

    private ImpermanentGraphDatabase db;

    @Before
    public void setup() throws Exception
    {
        db = new ImpermanentGraphDatabase();
        db.cleanContent( false );
    }

    @Test
    public void testLoadIndex()
    {
        Map<String, String> config = TimelineIndexProvider.CONFIG;
        IndexManager indexMan = db.index();
        Index<Node> index = indexMan.forNodes( "timeline1", config );
        assertNotNull( index );

    }

    @Test
    public void testAddToIndex() throws Exception
    {
        Map<String, String> config = TimelineIndexProvider.CONFIG;
        IndexManager indexMan = db.index();
        Index<Node> index = indexMan.forNodes( "timeline1", config );
        assertNotNull( index );
        Transaction tx = db.beginTx();
        Node n1 = db.createNode();
        n1.setProperty( "time", 123 );
        index.add( n1, "timestamp", 123L );
        Node n2 = db.createNode();
        n2.setProperty( "time", 123 );
        index.add( n2, "timestamp", 123L );
        Node n3 = db.createNode();
        n3.setProperty( "time", 124 );
        index.add( n3, "timestamp", 124L );
        tx.success();
        tx.finish();
        GraphvizWriter writer = new GraphvizWriter();
        writer.emit( System.out, Walker.fullGraph( db ));
        IndexHits<Node> hits = index.get( "timestamp", 123L );
        assertEquals(2, hits.size());
        hits = index.query( "[122 TO 125]" );
        assertEquals(3, hits.size());
        
        CypherParser parser = new CypherParser();
        ExecutionEngine engine = new ExecutionEngine( db );
        Query query = parser.parse( "start n=node:timeline1('[100 TO 200]') return n" );
        ExecutionResult result = engine.execute( query );
        System.out.println( result.toString() );

    }

}
