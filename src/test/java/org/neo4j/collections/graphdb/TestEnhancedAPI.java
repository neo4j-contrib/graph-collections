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

package org.neo4j.collections.graphdb;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.collections.graphdb.Node;
import org.neo4j.collections.graphdb.Relationship;
import org.neo4j.collections.graphdb.impl.RelationshipImpl;
import org.neo4j.collections.graphdb.impl.PropertyImpl;
import org.neo4j.collections.graphdb.impl.PropertyImpl.PropertyContainerType;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.collections.Neo4jTestCase;

public class TestEnhancedAPI extends Neo4jTestCase
{
	
	enum RelTypes implements RelationshipType{
		TEST_REL1, TEST_REL2
	}
	
	@Test
	public void testIndexRelationshipBasic()
	{
		Node n1 = graphDbExt().createNode();
		Node n2 = graphDbExt().createNode();
		n1.setProperty("test prop", "test1");
		n2.setProperty("test prop", "test2");
		Relationship rel1 = n1.createRelationshipTo(n2, RelTypes.TEST_REL1);
		assertTrue(n1.hasRelationship(RelTypes.TEST_REL1, Direction.OUTGOING));
		assertTrue(n2.hasRelationship(RelTypes.TEST_REL1, Direction.INCOMING));
		assertTrue(n1.hasProperty("test prop"));
		assertTrue(n2.hasProperty("test prop"));
		assertTrue(n1.getProperty("test prop").equals("test1"));
		assertTrue(n2.getProperty("test prop").equals("test2"));
		
		PropertyType<String> pt = graphDbExt().getStringPropertyType("test prop");
		assertTrue(n1.hasProperty(pt));
		assertTrue(n2.hasProperty(pt));
		assertTrue(n1.getPropertyValue(pt).equals("test1"));
		assertTrue(n2.getPropertyValue(pt).equals("test2"));
		assertTrue(pt.getName().equals("test prop"));
		assertTrue(pt.getNode().hasProperty(PropertyType.PROP_TYPE));
		assertTrue(pt.getNode().hasRelationship(DynamicRelationshipType.withName(pt.getName())));
		PropertyType<Boolean> isChecked = graphDbExt().getBooleanPropertyType("is_checked");
		pt.setProperty(isChecked, true);
		assertTrue(pt.getPropertyValue(isChecked) == true);
		assertTrue(pt.getProperty(isChecked).getPropertyType().equals(isChecked));
		assertTrue(pt.getProperty(isChecked).getValue() == true);

		Element elem = graphDbExt().getElement(isChecked.getNode());
		
		assertTrue( elem instanceof PropertyType<?>);
		
		rel1.setProperty(pt, "test3");
		rel1.createRelationshipTo(n1, RelTypes.TEST_REL2);
		assertTrue(rel1.hasRelationship(RelTypes.TEST_REL2, Direction.OUTGOING));
		assertTrue(n1.hasRelationship(RelTypes.TEST_REL2, Direction.INCOMING));
		assertTrue(rel1.getSingleRelationship(RelTypes.TEST_REL2, Direction.OUTGOING).getEndNode().getId() == n1.getId());
		assertTrue(rel1.hasProperty(RelationshipImpl.NODE_ID));
		assertTrue(rel1.getProperty(RelationshipImpl.NODE_ID).equals(rel1.getNode().getId()));
		assertTrue(rel1.getNode().getProperty(RelationshipImpl.REL_ID).equals(rel1.getId()));
		
		Property<String> prop = n1.getProperty(pt);
		prop.setProperty(isChecked, true);
		assertTrue(prop.getValue().equals("test1"));
		assertTrue(prop.getPropertyType().equals(pt));
		assertTrue(prop.hasProperty(isChecked));
		assertTrue(prop.getPropertyValue(isChecked) == true);
		assertTrue(n1.hasProperty(pt.getName()+".node_id"));
		assertTrue(n1.getProperty(pt.getName()+".node_id").equals(prop.getNode().getId()));
		assertTrue(prop.getNode().hasProperty(PropertyImpl.PROPERTY_NAME));
		assertTrue(prop.getNode().hasProperty(PropertyImpl.PROPERTYCONTAINER_ID));
		assertTrue(prop.getNode().hasProperty(PropertyImpl.PROPERTYCONTAINER_TYPE));
		assertTrue(prop.getNode().getProperty(PropertyImpl.PROPERTY_NAME).equals(prop.getPropertyType().getName()));
		assertTrue(prop.getNode().getProperty(PropertyImpl.PROPERTYCONTAINER_ID).equals(n1.getId()));
		assertTrue(prop.getNode().getProperty(PropertyImpl.PROPERTYCONTAINER_TYPE).equals(PropertyContainerType.NODE.name()));
		
		prop.createRelationshipTo(n2, RelTypes.TEST_REL2);
		assertTrue(prop.hasRelationship(RelTypes.TEST_REL2, Direction.OUTGOING));
		assertTrue(n2.hasRelationship(RelTypes.TEST_REL2, Direction.INCOMING));
		assertTrue(n2.getSingleRelationship(RelTypes.TEST_REL2, Direction.INCOMING).getStartElement().getId() ==  prop.getNode().getId());
		
		HyperRelationshipType relType = graphDbExt().getRelationshipType(RelTypes.TEST_REL1);
		assertTrue(relType.name().equals(RelTypes.TEST_REL1.name()));
		assertTrue(relType.getRoles().length == 2);
		assertTrue(relType.getRoles()[0].getId() == graphDbExt().getStartElementRole().getId() || relType.getRoles()[0].getId() == graphDbExt().getEndElementRole().getId());
		assertTrue(relType.getRoles()[1].getId() == graphDbExt().getStartElementRole().getId() || relType.getRoles()[1].getId() == graphDbExt().getEndElementRole().getId());
		assertTrue(rel1.getElement(graphDbExt().getStartElementRole()).getId() == n1.getId());
		assertTrue(rel1.getElement(graphDbExt().getEndElementRole()).getId() == n2.getId());
		
		RelationshipRole<Element> giver = graphDbExt().getRelationshipRole("giver");
		RelationshipRole<Element> recipient = graphDbExt().getRelationshipRole("recipient");
		RelationshipRole<Element> gift = graphDbExt().getRelationshipRole("gift");

		Set<RelationshipRole<? extends Element>> roles = new HashSet<RelationshipRole<? extends Element>>();
		roles.add(giver);
		roles.add(recipient);
		roles.add(gift);

		HyperRelationshipType hrelType = graphDbExt().getOrCreateRelationshipType(DynamicRelationshipType.withName("GIVES"), roles);
		
		Node flo = graphDbExt().createNode();
		Node eddie = graphDbExt().createNode();
		Node tom = graphDbExt().createNode();
		Node dick = graphDbExt().createNode();
		Node harry = graphDbExt().createNode();
		Node book = graphDbExt().createNode();
		Node spatula = graphDbExt().createNode();

		ArrayList<Element> gv = new ArrayList<Element>();
		gv.add(flo);
		gv.add(eddie);
		ArrayList<Element> rp = new ArrayList<Element>();
		rp.add(tom);
		rp.add(dick);
		rp.add(harry);
		ArrayList<Element> gf = new ArrayList<Element>();
		gf.add(book);
		gf.add(spatula);

		RelationshipElement<Element> givers = new RelationshipElement<Element>(giver, gv);
		RelationshipElement<Element> recipients = new RelationshipElement<Element>(recipient, rp);
		RelationshipElement<Element> gifts = new RelationshipElement<Element>(gift, gf);

		Set<RelationshipElement<? extends Element>> relationshipElements = new HashSet<RelationshipElement<? extends Element>>();
		relationshipElements.add(givers);
		relationshipElements.add(recipients);
		relationshipElements.add(gifts);
		
		HyperRelationship hrel = graphDbExt().createRelationship(hrelType, relationshipElements);
		int count = 0;
		for(Element element: hrel.getElements(giver)){
			assertTrue(element.getId() == flo.getId() || element.getId() == eddie.getId());
			count++;
		}
		assertTrue(count == 2);
		count = 0;
		for(Element element: hrel.getElements(recipient)){
			assertTrue(element.getId() == tom.getId() || element.getId() == dick.getId()  || element.getId() == harry.getId());
			count++;
		}
		assertTrue(count == 3);
		count = 0;
		for(Element element: hrel.getElements(gift)){
			assertTrue(element.getId() == book.getId() || element.getId() == spatula.getId());
			count++;
		}
		assertTrue(count == 2);
		
	}
}