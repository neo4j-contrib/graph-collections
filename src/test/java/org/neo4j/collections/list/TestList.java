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
package org.neo4j.collections.list;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.collections.Neo4jTestCase;
import org.neo4j.graphdb.Node;

public class TestList extends Neo4jTestCase{
	
	private List list;
	
	@Before
	public void setUpList() throws Exception
	{
		Node node = graphDb().createNode();
		list = new List( node, graphDb() ); 
	}
	
	@After
	public void tearDownList() throws Exception
	{
		list.delete();
	}
	
	@Test
	public void testTimelineBasic()
	{
		Node node1 = graphDb().createNode();
		node1.setProperty("name", "node1");
		Node node2 = graphDb().createNode();
		node2.setProperty("name", "node2");
		Node node3 = graphDb().createNode();
		node3.setProperty("name", "node3");
		Node node4 = graphDb().createNode();
		node4.setProperty("name", "node4");

		ArrayList<Node> al1 = new ArrayList<Node>();
		al1.add(node1);
		al1.add(node2);
		al1.add(node3);
		al1.add(node4);

		ArrayList<Node> al2 = new ArrayList<Node>();
		al2.add(node1);
		al2.add(node4);

		ArrayList<Node> al3 = new ArrayList<Node>();
		al3.add(node2);
		al3.add(node3);
		
		ArrayList<Node> al4 = new ArrayList<Node>();
		al4.add(node1);
		al4.add(node2);
		
		ArrayList<Node> al5 = new ArrayList<Node>();
		al5.add(node3);
		al5.add(node4);
		
		assertTrue( list.size() == 0 );
		assertTrue( list.isEmpty() );
		assertTrue( !list.iterator().hasNext() );
		
		list.add(node1);
		
		assertTrue( list.size() == 1 );
		assertTrue( !list.isEmpty() );
		assertTrue( list.iterator().hasNext() );
		assertTrue( ((String)list.get(0).getProperty("name")).equals("node1") );
		assertTrue( list.contains(node1) );
		assertTrue( !list.contains(node2) );
		
		list.add(node2);		
		assertTrue( list.size() == 2 );
		assertTrue( ((String)list.get(1).getProperty("name")).equals("node2") );
		assertTrue( list.containsAll(al4) );
		assertTrue( !list.containsAll(al3) );
		
		list.remove(1);
		assertTrue( list.size() == 1 );
		assertTrue( ((String)list.get(0).getProperty("name")).equals("node1") );
		
		list.add(node3);
		assertTrue( list.size() == 2 );
		assertTrue( ((String)list.get(1).getProperty("name")).equals("node3") );
		
		list.add(1, node2);
		assertTrue( list.size() == 3 );
		assertTrue( ((String)list.get(1).getProperty("name")).equals("node2") );
		
		list.remove(1);
		assertTrue( list.size() == 2 );
		assertTrue( ((String)list.get(1).getProperty("name")).equals("node3") );

		list.remove(1);
		assertTrue( list.size() == 1 );

		list.remove(0);
		assertTrue( list.size() == 0 );
		assertTrue( list.isEmpty() );
		assertTrue( !list.iterator().hasNext() );

		
		list.addAll(al1);
		assertTrue( list.size() == 4 );
		assertTrue( ((String)list.get(0).getProperty("name")).equals("node1") );
		assertTrue( ((String)list.get(1).getProperty("name")).equals("node2") );
		assertTrue( ((String)list.get(2).getProperty("name")).equals("node3") );
		assertTrue( ((String)list.get(3).getProperty("name")).equals("node4") );

		//remove an element in the middle
		for (Iterator<Node> i = list.iterator(); i.hasNext();) {
			Node n = i.next();
			if(n.getId() == node2.getId())
				i.remove();
		}
		assertTrue( list.size() == 3 );
		assertTrue( ((String)list.get(0).getProperty("name")).equals("node1") );
		assertTrue( ((String)list.get(1).getProperty("name")).equals("node3") );
		assertTrue( ((String)list.get(2).getProperty("name")).equals("node4") );

		
		//remove last element
		for (Iterator<Node> i = list.iterator(); i.hasNext();) {
			Node n = i.next();
			if(n.getId() == node4.getId())
				i.remove();
		}
		assertTrue( list.size() == 2 );
		assertTrue( ((String)list.get(0).getProperty("name")).equals("node1") );
		assertTrue( ((String)list.get(1).getProperty("name")).equals("node3") );

		//remove first element
		for (Iterator<Node> i = list.iterator(); i.hasNext();) {
			Node n = i.next();
			if(n.getId() == node1.getId())
				i.remove();
		}
		assertTrue( list.size() == 1 );
		assertTrue( ((String)list.get(0).getProperty("name")).equals("node3") );
		
		list.remove(node3);
		assertTrue( list.size() == 0 );
		assertTrue( list.isEmpty() );
		

		list.addAll(al1);
		assertTrue( list.indexOf(node1) == 0);
		assertTrue( list.indexOf(node2) == 1);
		assertTrue( list.indexOf(node3) == 2);
		assertTrue( list.indexOf(node4) == 3);
		
		list.removeAll(al2);
		assertTrue( list.size() == 2 );
		assertTrue( ((String)list.get(0).getProperty("name")).equals("node2") );
		assertTrue( ((String)list.get(1).getProperty("name")).equals("node3") );
		

		list.clear();
		list.addAll(al1);
		
		list.removeAll(al3);
		assertTrue( list.size() == 2 );
		assertTrue( ((String)list.get(0).getProperty("name")).equals("node1") );
		assertTrue( ((String)list.get(1).getProperty("name")).equals("node4") );

		list.clear();
		list.addAll(al1);
		
		list.removeAll(al4);
		assertTrue( list.size() == 2 );
		assertTrue( ((String)list.get(0).getProperty("name")).equals("node3") );
		assertTrue( ((String)list.get(1).getProperty("name")).equals("node4") );
		
		list.clear();
		list.addAll(al1);
		
		list.removeAll(al5);
		assertTrue( list.size() == 2 );
		assertTrue( ((String)list.get(0).getProperty("name")).equals("node1") );
		assertTrue( ((String)list.get(1).getProperty("name")).equals("node2") );

		list.clear();
		list.addAll(al1);
		
		list.retainAll(al2);
		assertTrue( list.size() == 2 );
		assertTrue( ((String)list.get(0).getProperty("name")).equals("node1") );
		assertTrue( ((String)list.get(1).getProperty("name")).equals("node4") );
		

		list.clear();
		list.addAll(al1);
		
		list.retainAll(al3);
		assertTrue( list.size() == 2 );
		assertTrue( ((String)list.get(0).getProperty("name")).equals("node2") );
		assertTrue( ((String)list.get(1).getProperty("name")).equals("node3") );

		list.clear();
		list.addAll(al1);
		
		list.retainAll(al4);
		assertTrue( list.size() == 2 );
		assertTrue( ((String)list.get(0).getProperty("name")).equals("node1") );
		assertTrue( ((String)list.get(1).getProperty("name")).equals("node2") );
		
		list.clear();
		list.addAll(al1);
		
		list.retainAll(al5);
		assertTrue( list.size() == 2 );
		assertTrue( ((String)list.get(0).getProperty("name")).equals("node3") );
		assertTrue( ((String)list.get(1).getProperty("name")).equals("node4") );

		list.clear();
		list.addAll(al1);
		list.addAll(al4);

		assertTrue( list.size() == 6 );
		assertTrue( list.indexOf(node1) == 0 );
		assertTrue( list.indexOf(node2) == 1 );
		assertTrue( list.lastIndexOf(node1) == 4 );
		assertTrue( list.lastIndexOf(node2) == 5 );
		
		list.clear();
		
		list.addAll(al1);
		list.addAll(2, al4);
		assertTrue( list.size() == 6 );
		assertTrue( ((String)list.get(0).getProperty("name")).equals("node1") );
		assertTrue( ((String)list.get(1).getProperty("name")).equals("node2") );
		assertTrue( ((String)list.get(2).getProperty("name")).equals("node1") );
		assertTrue( ((String)list.get(3).getProperty("name")).equals("node2") );
		assertTrue( ((String)list.get(4).getProperty("name")).equals("node3") );
		assertTrue( ((String)list.get(5).getProperty("name")).equals("node4") );
		
	}
}
