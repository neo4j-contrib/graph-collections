package org.neo4j.collections.graphdb;

import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

/**
 * Created with IntelliJ IDEA.
 * User: peter
 * Date: 07/12/13
 * Time: 17:36
 * To change this template use File | Settings | File Templates.
 */
public class ReferenceNodes {
    private static ReferenceNodes instance;
    private final ExecutionEngine engine;
    private GraphDatabaseService db;
    private Node referenceNode;

    public ReferenceNodes(GraphDatabaseService db) {
        this.db = db;
        engine = new ExecutionEngine(db);
    }

    public static ReferenceNodes getOrCreateInstance(GraphDatabaseService db) {
        if (instance == null) {
            instance = new ReferenceNodes(db);
        }
        return instance;
    }

    public Node getReferenceNode() {
        String ref = "ref";
        ExecutionResult result = engine.execute("MERGE (" + ref + ":ReferenceNodes{name:'referenceNode'}) RETURN "+ref);
        return (Node)result.iterator().next().get(ref);
    }
}
