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
import org.neo4j.kernel.GraphDatabaseAPI;
import org.neo4j.kernel.impl.core.TransactionState;

import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

class Neo4j19Locker extends Locker {

    private final TransactionManager txManager;

    public Neo4j19Locker(GraphDatabaseService graphDatabaseService) {
        if (!(graphDatabaseService instanceof GraphDatabaseAPI)) throw new RuntimeException("Error accessing transaction management, not a GraphDatabaseAPI " + graphDatabaseService);
        GraphDatabaseAPI graphDatabaseAPI = (GraphDatabaseAPI) graphDatabaseService;
        txManager = graphDatabaseAPI.getDependencyResolver().resolveDependency(TransactionManager.class);
    }

    @Override
    public void acquireLock(LockType lockType, PropertyContainer element) {
        TransactionImpl tx = getCurrentTransaction();
        if (tx==null) return; // no lock taken without external tx
        TransactionState state = tx.getState();
        switch (lockType) {
            case READ:
                state.acquireReadLock(element);
                break;
            case WRITE:
                state.acquireWriteLock(element);
                break;
            default: throw new IllegalStateException("Unknown lock type "+lockType);
        }
    }
    private TransactionImpl getCurrentTransaction() {
        try {
            return (TransactionImpl) txManager.getTransaction();
        } catch (SystemException e) {
            throw new RuntimeException("Error accessing current transaction", e);
        }
    }
}
