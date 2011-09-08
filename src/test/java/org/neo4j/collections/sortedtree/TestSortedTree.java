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
package org.neo4j.collections.sortedtree;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.collections.Neo4jTestCase;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import static org.junit.Assert.assertTrue;

public class TestSortedTree extends Neo4jTestCase
{
	private Node longTreeNode;
	private Node stringTreeNode;
	
	private SortedTree longTree;
	private SortedTree stringTree;

	class IdComparator implements java.util.Comparator<Node>{
		public int compare(Node n1, Node n2){
			long l1 = n1.getId();
			long l2 = n2.getId();
			if(l1 == l2) return 0;
			else if(l1 < l2) return -1;
			else return 1;
		}
	}
	
	class NameComparator implements java.util.Comparator<Node>{
		public int compare(Node n1, Node n2){
			String s1 = (String)n1.getProperty("name");
			String s2 = (String)n2.getProperty("name");
			return s1.compareTo(s2);
		}
	}
	
	@Before
	public void setUpSortedTree() throws Exception
	{
		longTreeNode = graphDb().createNode();
		Node longTreeInitialRootNode = graphDb().createNode();
		Node stringTreeNode = graphDb().createNode();
		Node stringTreeInitialRootNode = graphDb().createNode();
		longTreeNode.createRelationshipTo(longTreeInitialRootNode, SortedTree.RelTypes.TREE_ROOT);
		stringTreeNode.createRelationshipTo(stringTreeInitialRootNode, SortedTree.RelTypes.TREE_ROOT);
		longTree = new SortedTree(graphDb(), longTreeInitialRootNode, new IdComparator(), true, "Unique long test");
		stringTree = new SortedTree(graphDb(), stringTreeInitialRootNode, new NameComparator(), false, "Non unique string test");
	}
	
	@After
	public void tearDownSortedTree() throws Exception
	{
		longTree.delete();
		stringTree.delete();
	}
	
	@Test
	public void testTimelineBasic()
	{
		Node node1 = graphDb().createNode();
		node1.setProperty("name", "nodehsakjgh");
		Node node2 = graphDb().createNode();
		node2.setProperty("name", "nodeweiutyp");
		Node node3 = graphDb().createNode();
		node3.setProperty("name", "nodehsdpfgh");
		Node node4 = graphDb().createNode();
		node4.setProperty("name", "nodedkfkjgh");
		Node node5 = graphDb().createNode();
		node5.setProperty("name", "nodeaaaaa");
		Node node6 = graphDb().createNode();
		node6.setProperty("name", "nodepayghaj");
		Node node7 = graphDb().createNode();
		node7.setProperty("name", "nodeaupghkj");
		Node node8 = graphDb().createNode();
		node8.setProperty("name", "nodespfiugy");
		Node node9 = graphDb().createNode();
		node9.setProperty("name", "nodespfgiuy");
		Node node10 = graphDb().createNode();
		node10.setProperty("name", "nodesdfgiou");
		Node node11 = graphDb().createNode();
		node11.setProperty("name", "nodezzzzzzz");
		Node node12 = graphDb().createNode();
		node12.setProperty("name", "nodefgiuios");
		Node node13 = graphDb().createNode();
		node13.setProperty("name", "nodespdfiuo");
		Node node14 = graphDb().createNode();
		node14.setProperty("name", "nodesgdfiogu");
		Node node15 = graphDb().createNode();
		node15.setProperty("name", "nodedfkgsdff");
		Node node16 = graphDb().createNode();
		node16.setProperty("name", "nodesgupiohnd");
		Node node17 = graphDb().createNode();
		node17.setProperty("name", "nodedgfipoufd");
		Node node18 = graphDb().createNode();
		node18.setProperty("name", "nodegapiugffs");
		Node node19 = graphDb().createNode();
		node19.setProperty("name", "nodeapgyadgaf");
		Node node20 = graphDb().createNode();
		node20.setProperty("name", "nodepsuiyhfps");
		Node node21 = graphDb().createNode();
		node21.setProperty("name", "nodespdfuigyg");
		Node node22 = graphDb().createNode();
		node22.setProperty("name", "nodeypiusdfygi");
		Node node23 = graphDb().createNode();
		node23.setProperty("name", "nodejuroerhgio");
		Node node24 = graphDb().createNode();
		node24.setProperty("name", "nodeuihitbhhiu");
		Node node25 = graphDb().createNode();
		node25.setProperty("name", "nodeisdshdfijgh");
		Node node26 = graphDb().createNode();
		node26.setProperty("name", "nodesdufgypiuhsg");
		Node node27 = graphDb().createNode();
		node27.setProperty("name", "nodesdiuhyughds");
		Node node28 = graphDb().createNode();
		node28.setProperty("name", "nodehspdfpghsdf");
		Node node29 = graphDb().createNode();
		node29.setProperty("name", "nodesdfhgfdioh");
		Node node30 = graphDb().createNode();
		node30.setProperty("name", "nodesodfgiosdfg");
		Node node31 = graphDb().createNode();
		node31.setProperty("name", "nodejsdhfghiohet");
		Node node32 = graphDb().createNode();
		node32.setProperty("name", "nodesdgjisdfgsdf");
		Node node33 = graphDb().createNode();
		node33.setProperty("name", "nodesfgphnhiotre");
		Node node34 = graphDb().createNode();
		node34.setProperty("name", "nodehwpiothgergt");
		Node node35 = graphDb().createNode();
		node35.setProperty("name", "nodehipsdfhgier");
		Node node36 = graphDb().createNode();
		node36.setProperty("name", "nodehwiehrgweg");
		Node node37 = graphDb().createNode();
		node37.setProperty("name", "nodehwegbnsdkjfgn");
		Node node38 = graphDb().createNode();
		node38.setProperty("name", "nodesdhfgpsdhnfgo");
		Node node39 = graphDb().createNode();
		node39.setProperty("name", "nodegsdhfgiojsdfg");

		assertTrue( !longTree.iterator().hasNext() );
		
		longTree.addNode(node1);
		longTree.addNode(node2);
		longTree.addNode(node3);
		longTree.addNode(node4);
		longTree.addNode(node5);
		longTree.addNode(node6);
		longTree.addNode(node7);
		longTree.addNode(node8);
		longTree.addNode(node9);
		longTree.addNode(node10);
		longTree.addNode(node11);
		longTree.addNode(node12);
		longTree.addNode(node13);
		longTree.addNode(node14);
		longTree.addNode(node15);
		longTree.addNode(node16);
		longTree.addNode(node17);
		longTree.addNode(node18);
		longTree.addNode(node19);
		longTree.addNode(node20);
		longTree.addNode(node21);
		longTree.addNode(node22);
		longTree.addNode(node23);
		longTree.addNode(node24);
		longTree.addNode(node25);
		longTree.addNode(node26);
		longTree.addNode(node27);
		longTree.addNode(node28);
		longTree.addNode(node29);
		longTree.addNode(node30);
		longTree.addNode(node31);
		longTree.addNode(node32);
		longTree.addNode(node33);
		longTree.addNode(node34);
		longTree.addNode(node35);
		longTree.addNode(node36);
		longTree.addNode(node37);
		longTree.addNode(node38);
		longTree.addNode(node39);

		assertTrue( longTree.iterator().hasNext() );
		int count = 0;
		for( Relationship r: longTree){
			count++;
		}
		assertTrue( count == 39);
		
		assertTrue( !stringTree.iterator().hasNext() );

		stringTree.addNode(node1);
		stringTree.addNode(node2);
		stringTree.addNode(node3);
		stringTree.addNode(node4);
		stringTree.addNode(node5);
		stringTree.addNode(node6);
		stringTree.addNode(node7);
		stringTree.addNode(node8);
		stringTree.addNode(node9);
		stringTree.addNode(node10);
		stringTree.addNode(node11);
		stringTree.addNode(node12);
		stringTree.addNode(node13);
		stringTree.addNode(node14);
		stringTree.addNode(node15);
		stringTree.addNode(node16);
		stringTree.addNode(node17);
		stringTree.addNode(node18);
		stringTree.addNode(node19);
		stringTree.addNode(node20);
		stringTree.addNode(node21);
		stringTree.addNode(node22);
		stringTree.addNode(node23);
		stringTree.addNode(node24);
		stringTree.addNode(node25);
		stringTree.addNode(node26);
		stringTree.addNode(node27);
		stringTree.addNode(node28);
		stringTree.addNode(node29);
		stringTree.addNode(node30);
		stringTree.addNode(node31);
		stringTree.addNode(node32);
		stringTree.addNode(node33);
		stringTree.addNode(node34);
		stringTree.addNode(node35);
		stringTree.addNode(node36);
		stringTree.addNode(node37);
		stringTree.addNode(node38);
		stringTree.addNode(node39);
		
		assertTrue( stringTree.iterator().hasNext() );
		count = 0;
		for(Relationship r: stringTree){
			count++;
		}
		assertTrue( count == 39);		
		count = 0;
		for(Relationship r: stringTree){
            Node n = r.getEndNode();
			count++;
			if(count == 1){
				assertTrue(n.getProperty("name").equals("nodeaaaaa"));
			}
			if(count == 39){
				assertTrue(n.getProperty("name").equals("nodezzzzzzz"));
			}
			
		}
		assertTrue(stringTree.containsNode(node2));
		stringTree.removeNode(node2);
		assertTrue(!stringTree.containsNode(node2));
		stringTree.removeNode(node4);
		stringTree.removeNode(node6);
		stringTree.removeNode(node8);
		stringTree.removeNode(node10);
		stringTree.removeNode(node12);
		stringTree.removeNode(node14);
		stringTree.removeNode(node16);
		stringTree.removeNode(node18);
		stringTree.removeNode(node20);
		stringTree.removeNode(node22);
		stringTree.removeNode(node24);
		stringTree.removeNode(node26);
		stringTree.removeNode(node28);
		stringTree.removeNode(node30);
		stringTree.removeNode(node32);
		stringTree.removeNode(node34);
		stringTree.removeNode(node36);
		stringTree.removeNode(node38);
		count = 0;
		for(Relationship r: stringTree){
			count++;
		}
		assertTrue( count == 20);
		
		stringTree.removeNode(node1);
		stringTree.removeNode(node3);
		stringTree.removeNode(node5);
		stringTree.removeNode(node7);
		stringTree.removeNode(node9);
		stringTree.removeNode(node11);
		stringTree.removeNode(node13);
		stringTree.removeNode(node15);
		stringTree.removeNode(node17);
		stringTree.removeNode(node19);
		stringTree.removeNode(node21);
		stringTree.removeNode(node23);
		stringTree.removeNode(node25);
		stringTree.removeNode(node27);
		stringTree.removeNode(node29);
		stringTree.removeNode(node31);
		stringTree.removeNode(node33);
		stringTree.removeNode(node35);
		stringTree.removeNode(node37);
		stringTree.removeNode(node39);
		count = 0;
		for(Relationship r: stringTree){
			count++;
		}
		assertTrue( count == 0);		
		
	}
	

}