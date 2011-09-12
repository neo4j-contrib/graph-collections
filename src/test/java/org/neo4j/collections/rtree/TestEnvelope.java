package org.neo4j.collections.rtree;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestEnvelope {

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
	
	@Test
	public void testCovers() {
		Envelope a = new Envelope(new double[] { 0 }, new double[] { 0 });
		// assertTrue(1 == a.getArea());		
		
		
	}
	
}