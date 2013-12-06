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

import org.junit.Before;
import org.junit.Test;
import org.neo4j.collections.timeline.Timeline;
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

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TimelineIndexProviderTest {

    private ImpermanentGraphDatabase db;

    @Before
    public void setup() throws Exception {
        db = new ImpermanentGraphDatabase();
        db.cleanContent(true);
    }

    @Test
    public void testLoadIndex() {
        db.beginTx();
        Map<String, String> config = TimelineNodeIndex.CONFIG;
        IndexManager indexMan = db.index();
        Index<Node> index = indexMan.forNodes("timeline1", config);
        assertNotNull(index);

    }

    @Test
    public void testLoadIndexWithRootNode() {
        db.beginTx();
        Map<String, String> config = new HashMap<String, String>(TimelineNodeIndex.CONFIG);
        final Node startNode = db.getReferenceNode();
        config.put(TimelineNodeIndex.START_NODE_ID, String.valueOf(startNode.getId()));
        IndexManager indexMan = db.index();
        Index<Node> index = indexMan.forNodes("timeline1", config);
        final Timeline timeline = ((TimelineNodeIndex) index).getTimeline();
        assertEquals(startNode, timeline.getUnderlyingNode());
        assertNotNull(index);

    }

    @Test
    public void testAddToIndex() throws Exception {
        Transaction tx = db.beginTx();
        Map<String, String> config = TimelineNodeIndex.CONFIG;
        IndexManager indexMan = db.index();
        Index<Node> index = indexMan.forNodes("timeline1", config);
        assertNotNull(index);
        Node n1 = db.createNode();
        n1.setProperty("time", 123);
        index.add(n1, "timestamp", 123L);
        Node n2 = db.createNode();
        n2.setProperty("time", 123);
        index.add(n2, "timestamp", 123L);
        Node n3 = db.createNode();
        n3.setProperty("time", 124);
        index.add(n3, "timestamp", 124L);
        tx.success();
        tx.finish();

        tx = db.beginTx();
        GraphvizWriter writer = new GraphvizWriter();
        writer.emit(System.out, Walker.fullGraph(db));
        IndexHits<Node> hits = index.get("timestamp", 123L);
        assertEquals(2, hits.size());
        hits = index.query("[122 TO 125]");
        assertEquals(3, hits.size());

        ExecutionEngine engine = new ExecutionEngine(db);
        ExecutionResult result = engine.execute("start n=node:timeline1('[100 TO 200]') return n");
        System.out.println(result.toString());
        tx.success();
        tx.finish();
    }

}
