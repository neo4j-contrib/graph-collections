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
package org.neo4j.collections.graphdb.impl;

import org.neo4j.collections.graphdb.DatabaseService;
import org.neo4j.collections.graphdb.VertexType;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;

public class VertexTypeImpl extends VertexImpl implements VertexType {

	public enum RelTypes implements RelationshipType {
		ORG_NEO4J_COLLECTIONS_GRAPHDB_TYPE_SUBREF, ORG_NEO4J_COLLECTIONS_GRAPHDB_TYPE
	}

	public static class TypeNodeDescriptor {

		public final DatabaseService db;

		public final String name;
		public final Class<?> claz;

		public TypeNodeDescriptor(DatabaseService db, String name, Class<?> claz) {
			this.db = db;
			this.name = name;
			this.claz = claz;
		}

		public void initialize(Node n) {
			if (name.startsWith("org.neo4j.collections.graphdb")
					|| name.startsWith("ORG_NEO4J_COLLECTIONS_GRAPHDB")) {
				throw new RuntimeException(
						"Type names should never start with org.neo4j.collections.graphdb or with ORG_NEO4J_COLLECTIONS_GRAPHDB");
			}
			n.setProperty(TYPE_NAME, name);
			n.setProperty(CLASS_NAME, claz.getName());
		}
	}

	public static String TYPE_NAME = "org.neo4j.collections.graphdb.TYPE_NAME";

	public static String CLASS_NAME = "org.neo4j.collections.graphdb.CLASS_NAME";

	public static VertexType getByName(DatabaseService db, String name) {
		return (VertexType) db.getVertex(getNodeByName(db, name));
	}

	private static Class<?> getImplementationClass() {
		try {
			return Class
					.forName("org.neo4j.collections.graphdb.impl.VertexTypeImpl");
		} catch (ClassNotFoundException cce) {
			throw new RuntimeException(cce);
		}
	}

	private static Node getNodeByName(DatabaseService db, String name) {
		Node typeSubRef = getOrCreateTypeSubRef(db);
		RelationshipType relType = DynamicRelationshipType.withName(name);
		if (typeSubRef.hasRelationship(relType, Direction.OUTGOING)) {
			return typeSubRef
					.getSingleRelationship(relType, Direction.OUTGOING)
					.getEndNode();
		} else {
			return null;
		}
	}

	public static Node getOrCreateByDescriptor(TypeNodeDescriptor tnd) {
		Node typeSubRef = getOrCreateTypeSubRef(tnd.db);
		RelationshipType relType = DynamicRelationshipType.withName(tnd.name);
		Node foundNode = getNodeByName(tnd.db, tnd.name);
		if (foundNode != null) {
			if (tnd.claz.getName().equals(foundNode.getProperty(CLASS_NAME))) {
				return foundNode;
			} else {
				throw new RuntimeException(
						"A type already exists with than name");
			}

		} else {
			Node newNode = tnd.db.createNode();
			tnd.initialize(newNode);
			typeSubRef.createRelationshipTo(newNode, relType);
			return newNode;
		}
	}

	public static VertexTypeImpl getOrCreateInstance(DatabaseService db,
			String name) {
		return new VertexTypeImpl(db, getOrCreateByDescriptor(
				new TypeNodeDescriptor(db, name, getImplementationClass()))
				.getId());
	}

	private static Node getOrCreateTypeSubRef(DatabaseService db) {
        Node refNode = db.getReferenceNode();
		RelationshipType relType = RelTypes.ORG_NEO4J_COLLECTIONS_GRAPHDB_TYPE_SUBREF;
		if (refNode.hasRelationship(relType, Direction.OUTGOING)) {
			return refNode.getSingleRelationship(relType, Direction.OUTGOING)
					.getEndNode();
		} else {
			Node n = db.createNode();
			refNode.createRelationshipTo(n, relType);
			return n;
		}
	}

	public VertexTypeImpl(DatabaseService db, Long id) {
		super(db, id);
	}

	@Override
	public String getName() {
		return (String) getNode().getProperty(TYPE_NAME);
	}
}
