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
package org.neo4j.collections.graphdb;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.neo4j.collections.Neo4jTestCase;
import org.neo4j.collections.graphdb.impl.BinaryEdgeImpl;
import org.neo4j.collections.graphdb.impl.PropertyImpl;
import org.neo4j.collections.graphdb.impl.VertexTypeImpl;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.RelationshipType;

public class TestEnhancedAPI extends Neo4jTestCase
{
	
	enum RelTypes implements RelationshipType{
		TEST_REL1, TEST_REL2
	}
	
	@Test
	public void testIndexRelationshipBasic()
	{
		
		DatabaseService db = graphDbExt(); 
		
		Vertex v1 = db.createVertex();
		Vertex v2 = db.createVertex();
		
		PropertyType<String> pt1 = PropertyType.StringPropertyType.getOrCreateInstance(db, "test prop");
		
		v1.setProperty(pt1, "test1");
		v2.setProperty(pt1, "test2");
		BinaryEdge rel1 = v1.createEdgeTo(v2, RelTypes.TEST_REL1);
		assertTrue(v1.hasBinaryEdge(RelTypes.TEST_REL1, Direction.OUTGOING));
		assertTrue(v2.hasBinaryEdge(RelTypes.TEST_REL1, Direction.INCOMING));
		assertTrue(v1.hasProperty(pt1));
		assertTrue(v2.hasProperty(pt1));
		assertTrue(v1.getPropertyValue(pt1).equals("test1"));
		assertTrue(v2.getPropertyValue(pt1).equals("test2"));
		
		PropertyType<String> pt = graphDbExt().getStringPropertyType("test prop");
		assertTrue(pt1.getName().equals("test prop"));
		assertTrue(pt1.getNode().hasProperty(VertexTypeImpl.TYPE_NAME));
		assertTrue(pt1.getNode().hasProperty(VertexTypeImpl.CLASS_NAME));
		assertTrue(pt.getNode().hasRelationship(DynamicRelationshipType.withName(pt.getName())));
		PropertyType<Boolean> isChecked = db.getBooleanPropertyType("is_checked");
		pt.setProperty(isChecked, true);
		assertTrue(pt.getPropertyValue(isChecked) == true);
		assertTrue(pt.getProperty(isChecked).getPropertyType().equals(isChecked));
		assertTrue(pt.getProperty(isChecked).getValue() == true);

		Vertex elem = graphDbExt().getVertex(isChecked.getNode());
		
		assertTrue( elem instanceof PropertyType<?>);
		
		rel1.setProperty(pt, "test3");
		rel1.createEdgeTo(v1, RelTypes.TEST_REL2);
		assertTrue(rel1.hasBinaryEdge(RelTypes.TEST_REL2, Direction.OUTGOING));
		assertTrue(v1.hasBinaryEdge(RelTypes.TEST_REL2, Direction.INCOMING));
		assertTrue(rel1.getSingleBinaryEdge(RelTypes.TEST_REL2, Direction.OUTGOING).getEndVertex().getNode().getId() == v1.getNode().getId());
		
		assertTrue(rel1.getPropertyContainer().hasProperty(BinaryEdgeImpl.NODE_ID));
		assertTrue(rel1.getPropertyContainer().getProperty(BinaryEdgeImpl.NODE_ID).equals(rel1.getNode().getId()));
		assertTrue(rel1.getNode().getProperty(BinaryEdgeImpl.REL_ID).equals(rel1.getRelationship().getId()));
		
		Property<String> prop = v1.getProperty(pt);
		prop.setProperty(isChecked, true);
		assertTrue(prop.getValue().equals("test1"));
		assertTrue(prop.getPropertyType().equals(pt));
		assertTrue(prop.hasProperty(isChecked));
		assertTrue(prop.getPropertyValue(isChecked) == true);
		assertTrue(v1.getNode().hasProperty(pt.getName()+".node_id"));
		assertTrue(v1.getNode().getProperty(pt.getName()+".node_id").equals(prop.getNode().getId()));
		assertTrue(prop.getNode().hasProperty(PropertyImpl.PROPERTY_NAME));
		assertTrue(prop.getNode().hasProperty(PropertyImpl.PROPERTYCONTAINER_ID));
		assertTrue(prop.getNode().hasProperty(PropertyImpl.PROPERTYCONTAINER_TYPE));
		assertTrue(prop.getNode().getProperty(PropertyImpl.PROPERTY_NAME).equals(prop.getPropertyType().getName()));
		assertTrue(prop.getNode().getProperty(PropertyImpl.PROPERTYCONTAINER_ID).equals(v1.getNode().getId()));
		assertTrue(prop.getNode().getProperty(PropertyImpl.PROPERTYCONTAINER_TYPE).equals(PropertyImpl.PropertyContainerType.NODE.name()));
		
		prop.createEdgeTo(v2, RelTypes.TEST_REL2);
		assertTrue(prop.hasBinaryEdge(RelTypes.TEST_REL2, Direction.OUTGOING));
		assertTrue(v2.hasBinaryEdge(RelTypes.TEST_REL2, Direction.INCOMING));
		assertTrue(v2.getSingleBinaryEdge(RelTypes.TEST_REL2, Direction.INCOMING).getStartVertex().getNode().getId() ==  prop.getNode().getId());
		
		BinaryEdgeType relType = graphDbExt().getBinaryEdgeType(RelTypes.TEST_REL1);
		assertTrue(relType.getName().equals(RelTypes.TEST_REL1.name()));
/*	
		assertTrue(relType.getRoles().length == 2);
		assertTrue(relType.getRoles()[0].getId() == graphDbExt().getStartElementRoleType().getId() || relType.getRoles()[0].getId() == graphDbExt().getEndElementRoleType().getId());
		assertTrue(relType.getRoles()[1].getId() == graphDbExt().getStartElementRoleType().getId() || relType.getRoles()[1].getId() == graphDbExt().getEndElementRoleType().getId());
*/		
/*		
		assertTrue(rel1.getVertex(graphDbExt().getStartElementRoleType()).getNode().getId() == v1.getNode().getId());
		assertTrue(rel1.getVertex(graphDbExt().getEndElementRoleType()).getNode().getId() == v2.getNode().getId());
*/		
		
		ConnectorTypeDescription giverDescr = new ConnectorTypeDescription("giver", ConnectionMode.UNRESTRICTED);
		ConnectorTypeDescription recipientDescr = new ConnectorTypeDescription("recipient", ConnectionMode.UNRESTRICTED);
		ConnectorTypeDescription giftDescr = new ConnectorTypeDescription("gift", ConnectionMode.UNRESTRICTED);

		EdgeType edgeType = db.createEdgeType("GIVES", giverDescr, recipientDescr, giftDescr);
		
		ConnectorType<?> giver = edgeType.getConnectorType("giver");
		ConnectorType<?> recipient = edgeType.getConnectorType("recipient");
		ConnectorType<?> gift = edgeType.getConnectorType("gift");
		
		Vertex flo = graphDbExt().createVertex();
		Vertex eddie = graphDbExt().createVertex();
		Vertex tom = graphDbExt().createVertex();
		Vertex dick = graphDbExt().createVertex();
		Vertex harry = graphDbExt().createVertex();
		Vertex book = graphDbExt().createVertex();
		Vertex spatula = graphDbExt().createVertex();

		ConnectorDescription givers = new ConnectorDescription(giver, flo, eddie);
		ConnectorDescription recipients = new ConnectorDescription(recipient, tom, dick, harry);
		ConnectorDescription gifts = new ConnectorDescription(gift, book, spatula);

		Edge edge = graphDbExt().createEdge(edgeType, givers, recipients, gifts);
		int count = 0;
		for(Vertex element: edge.getVertices(giver)){
			assertTrue(element.getNode().getId() == flo.getNode().getId() || element.getNode().getId() == eddie.getNode().getId());
			count++;
		}
		assertTrue(count == 2);
		count = 0;
		for(Vertex element: edge.getVertices(recipient)){
			assertTrue(element.getNode().getId() == tom.getNode().getId() || element.getNode().getId() == dick.getNode().getId()  || element.getNode().getId() == harry.getNode().getId());
			count++;
		}
		assertTrue(count == 3);
		count = 0;
		for(Vertex element: edge.getVertices(gift)){
			assertTrue(element.getNode().getId() == book.getNode().getId() || element.getNode().getId() == spatula.getNode().getId());
			count++;
		}
		assertTrue(count == 2);
		
	}
}