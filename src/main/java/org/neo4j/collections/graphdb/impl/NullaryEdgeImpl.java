package org.neo4j.collections.graphdb.impl;

import java.util.ArrayList;

import org.neo4j.collections.graphdb.ConnectionMode;
import org.neo4j.collections.graphdb.ConnectorType;
import org.neo4j.collections.graphdb.EdgeElement;
import org.neo4j.collections.graphdb.EdgeType;
import org.neo4j.collections.graphdb.InjectiveConnectionMode;
import org.neo4j.collections.graphdb.NullaryEdge;
import org.neo4j.collections.graphdb.Vertex;
import org.neo4j.collections.graphdb.impl.NullaryConnectorTypeImpl.NullaryConnectorType;
import org.neo4j.graphdb.Node;

public class NullaryEdgeImpl extends VertexImpl implements NullaryEdge{

	public NullaryEdgeImpl(Node node) {
		super(node);
	}

	@Override
	public void delete() {
		getNode().delete();
	}

	@Override
	public EdgeType getType() {
		return NullaryEdgeTypeImpl.getOrCreateInstance(getDb());
	}

	@Override
	public boolean isType(EdgeType relType) {
		return relType.getName().equals(getType().getName());
	}

	@Override
	public Iterable<EdgeElement> getEdgeElements() {
		ArrayList<EdgeElement> elems = new ArrayList<EdgeElement>();
		Vertex[] va = new Vertex[1];
		va[0] = getDb().getVertex(getNode()); 
		elems.add(new EdgeElement(NullaryConnectorType.getOrCreateInstance(getDb()), va));
		return null;
	}

	@Override
	public Iterable<EdgeElement> getEdgeElements(
			ConnectorType<?>... connectorType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends ConnectionMode> Iterable<Vertex> getVertices(
			ConnectorType<T> connectorType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <U extends InjectiveConnectionMode> Vertex getVertex(
			ConnectorType<U> connectorType) {
		// TODO Auto-generated method stub
		return null;
	}

}
