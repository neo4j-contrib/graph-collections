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
package org.neo4j.collections.rtree;


public class Envelope {

	// Constructor
	
	public Envelope(Envelope e) {
		this(e.xmin, e.xmax, e.ymin, e.ymax);
	}

	public Envelope(double xmin, double xmax, double ymin, double ymax) {
		this.xmin = xmin;
		this.xmax = xmax;
		this.ymin = ymin;
		this.ymax = ymax;
	}

	public Envelope() {
	}

	
	// Public methods

	public double getMinX() {
		return xmin;
	}

	public double getMaxX() {
		return xmax;
	}	
	
	public double getMinY() {
		return ymin;
	}
		
	public double getMaxY() {
		return ymax;
	}	
	
	public boolean contains(Envelope e) {
		return e.xmin >= xmin && e.xmax <= xmax 
			&& e.ymin >= ymin && e.ymax <= ymax;
	}

	public void expandToInclude(Envelope e) {
		if (e.xmin < xmin) xmin = e.xmin;
		if (e.xmax > xmax) xmax = e.xmax;
		if (e.ymin < ymin) ymin = e.ymin;
		if (e.ymax > ymax) ymax = e.ymax;
	}

	public double getHeight() {
		return ymax - ymin;
	}

	public double getWidth() {
		return xmax - xmin;
	}

	public double getArea() {
		return getWidth() * getHeight();
	}

	
	// Attributes
	
	private double xmin, xmax, ymin, ymax = 0;
}