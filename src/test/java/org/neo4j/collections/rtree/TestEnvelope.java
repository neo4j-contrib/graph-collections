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
package org.neo4j.collections.rtree;

import org.junit.Test;
import static org.junit.Assert.*;

public class TestEnvelope extends SpatialTestCase {

	@Test
	public void testEnvelope() throws Exception {
		Envelope envelope = new Envelope();
		assertTrue("Empty Envelope '" + envelope + "' should be invalid", !envelope.isValid());
		envelope.expandToInclude(new double[]{1,1});
		assertTrue("Point Envelope '" + envelope + "' should be valid", envelope.isValid());
		assertEquals("2D Point Envelope '" + envelope + "' should have 2 dimensions", 2, envelope.getDimension());
		assertEquals("2D Point Envelope '" + envelope + "' should have 0 width", 0, envelope.getWidth(), 0);
		envelope.expandToInclude(new double[]{-1,5});
		assertTrue("BBox Envelope '" + envelope + "' should be valid", envelope.isValid());
		assertEquals("2D Box Envelope '" + envelope + "' should have 2 dimensions", 2, envelope.getDimension());
		assertEquals("2D Box Envelope '" + envelope + "' should have 2,4 width", 2, envelope.getWidth(0), 0);
		assertEquals("2D Box Envelope '" + envelope + "' should have 2,4 width", 4, envelope.getWidth(1), 0);
		assertEquals("2D Box Envelope '" + envelope + "' should have 2,4 width", 2, envelope.getWidth(), 0);
		assertEquals("2D Box Envelope '" + envelope + "' should have 2,4 width", 4, envelope.getHeight(), 0);
    }

	@Test
	public void testDisjoint() {		
		Envelope a = new Envelope(new double[] { 0 }, new double[] { 1 });
		Envelope b = new Envelope(new double[] { 2 }, new double[] { 3 });		
		Envelope c = new Envelope(new double[] { 1 }, new double[] { 3 });
		Envelope d = new Envelope(new double[] { -2 }, new double[] { -1 });				
		Envelope e = new Envelope(new double[] { -2 }, new double[] { 0 });						
		
		assertTrue(a.disjoint(b));
		assertFalse(a.disjoint(c));
		assertTrue(b.disjoint(a));
		assertTrue(a.disjoint(d));
		assertFalse(a.disjoint(e));
		
		assertTrue(a.intersects(c));
		assertTrue(c.intersects(a));
		assertTrue(a.intersects(e));
		assertTrue(e.intersects(a));
	}

}