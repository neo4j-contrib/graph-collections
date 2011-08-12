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
package org.neo4j.collections.graphdb.impl;

import org.neo4j.collections.graphdb.BijectiveConnectionMode;
import org.neo4j.collections.graphdb.ConnectionMode;
import org.neo4j.collections.graphdb.InjectiveConnectionMode;
import org.neo4j.collections.graphdb.SurjectiveConnectionMode;
import org.neo4j.collections.graphdb.UnrestrictedConnectionMode;

public abstract class ConnectionModeImpl implements ConnectionMode{


	private ConnectionModeImpl(){
	}
	
	public static class Unrestricted extends ConnectionModeImpl implements UnrestrictedConnectionMode{
		public Unrestricted(){
			super();
		}
		public String getName(){
			return "Unrestricted";
		}
		
	}

	public static class Injective extends ConnectionModeImpl implements InjectiveConnectionMode{
		public Injective(){
			super();
		}
		
		public String getName(){
			return "Injective";
		}
	}
	
	public static class Surjective extends ConnectionModeImpl implements SurjectiveConnectionMode{
		public Surjective(){
			super();
		}
		
		public String getName(){
			return "Surjective";
		}

	}

	public static class Bijective extends ConnectionModeImpl implements BijectiveConnectionMode{
		public Bijective(){
			super();
		}
		
		public String getName(){
			return "Bijective";
		}

	}
	
}
