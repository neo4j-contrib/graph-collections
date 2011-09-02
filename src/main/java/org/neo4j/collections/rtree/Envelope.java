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
		this.xmin = 0;
		this.xmax = -1;
		this.ymin = 0;
		this.ymax = -1;
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
	
	public boolean contains(Envelope other) {
		if (!isValid() || !other.isValid()) {
            return false;
        }
		
		return other.xmin >= xmin && other.xmax <= xmax 
			&& other.ymin >= ymin && other.ymax <= ymax;
	}

	public boolean intersects(Envelope other) {
		if (!isValid() || !other.isValid()) {
            return false;
        }
		
		return !(other.xmin > xmax || other.xmax < xmin
                || other.ymin > ymax || other.ymax < ymin);
	}	
	
	public void expandToInclude(Envelope other) {
		if (!other.isValid()) {
			return;
		}
		
		if (!isValid()) {
			xmin = other.xmin;
			xmax = other.xmax;
			ymin = other.ymin;
			ymax = other.ymax;
		} else {
			if (other.xmin < xmin) xmin = other.xmin;
			if (other.xmax > xmax) xmax = other.xmax;
			if (other.ymin < ymin) ymin = other.ymin;
			if (other.ymax > ymax) ymax = other.ymax;
		}
	}

	public double getHeight() {
		return isValid() ? ymax - ymin : 0;
	}

	public double getWidth() {
		return isValid() ? xmax - xmin : 0;
	}

	public double getArea() {
		return getWidth() * getHeight();
	}
	
	public boolean isValid() {
		return xmin <= xmax && ymin <= ymax;
	}
	
	
	// Attributes
	
	private double xmin, xmax, ymin, ymax;
}