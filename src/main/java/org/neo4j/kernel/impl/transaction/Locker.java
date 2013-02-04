/**
 * Copyright (c) 2002-2013 "Neo Technology,"
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
package org.neo4j.kernel.impl.transaction;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.PropertyContainer;

import java.lang.reflect.InvocationTargetException;

public abstract class Locker {
    public static Locker getInstance(GraphDatabaseService graphDatabaseService) {
        try {
            return newLockerInstance("Neo4j19Locker", graphDatabaseService);
        } catch (Exception e) {
            // ignore
        }
        return newLockerInstance("Neo4j18Locker", graphDatabaseService);
    }

    private static Locker newLockerInstance(String subClass, GraphDatabaseService graphDatabaseService) {
        try {
            return (Locker)Class.forName("org.neo4j.kernel.impl.transaction." + subClass).getConstructor(GraphDatabaseService.class).newInstance(graphDatabaseService);
        } catch (Exception e) {
            throw new RuntimeException("Error creating Locker "+subClass,e);
        }
    }

    public abstract void acquireLock(LockType lockType, PropertyContainer element);
}
