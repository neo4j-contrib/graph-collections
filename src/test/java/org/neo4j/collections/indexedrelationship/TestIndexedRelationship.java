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

package org.neo4j.collections.indexedrelationship;

import static org.junit.Assert.assertTrue;

import java.util.Comparator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.collections.Neo4jTestCase;

public class TestIndexedRelationship extends Neo4jTestCase
{
	public static class IdComparator implements java.util.Comparator<Node>{
		public int compare(Node n1, Node n2){
			long l1 = Long.reverse(n1.getId());
			long l2 = Long.reverse(n2.getId());
			if(l1 == l2) return 0;
			else if(l1 < l2) return -1;
			else return 1;
		}
	}
	
	static enum RelTypes implements RelationshipType
	{
		DIRECT_RELATIONSHIP,
		INDEXED_RELATIONSHIP,
	};

	@Test
	public void testIndexRelationshipBasic()
	{
		Node indexedNode = graphDb().createNode();
		IndexedRelationship ir = new IndexedRelationship(RelTypes.INDEXED_RELATIONSHIP, Direction.OUTGOING, new IdComparator(), true, indexedNode, graphDb());
		
		Node n1 = graphDb().createNode();
		n1.setProperty("name", "n1");
		Node n2 = graphDb().createNode();
		n2.setProperty("name", "n2");
		Node n3 = graphDb().createNode();
		n3.setProperty("name", "n3");
		Node n4 = graphDb().createNode();
		n4.setProperty("name", "n4");
		
		indexedNode.createRelationshipTo(n1, RelTypes.DIRECT_RELATIONSHIP);
		indexedNode.createRelationshipTo(n3, RelTypes.DIRECT_RELATIONSHIP);
		ir.createRelationshipTo(n2);
		ir.createRelationshipTo(n4);
		
		IndexedRelationshipExpander re1 = new IndexedRelationshipExpander(graphDb(), Direction.OUTGOING, RelTypes.DIRECT_RELATIONSHIP);
		IndexedRelationshipExpander re2 = new IndexedRelationshipExpander(graphDb(), Direction.OUTGOING, RelTypes.INDEXED_RELATIONSHIP);
		
		int count = 0;
		for(Relationship rel: re1.expand(indexedNode)){
			if(count == 0){
				assertTrue( rel.getEndNode().equals(n1) || rel.getEndNode().equals(n3));
			}
			if(count == 1){
				assertTrue( rel.getEndNode().equals(n1) || rel.getEndNode().equals(n3));
			}
			count++;
		}
		assertTrue(count == 2);
		count = 0;
		for(Relationship rel: re2.expand(indexedNode)){
			if(count == 0){
				assertTrue( rel.getEndNode().equals(n2) );
			}
			if(count == 1){
				assertTrue( rel.getEndNode().equals(n4) );
			}
			count++;
		}
		assertTrue(count == 2);
	}
}