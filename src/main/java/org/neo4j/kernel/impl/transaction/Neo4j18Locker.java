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
import org.neo4j.helpers.Pair;
import org.neo4j.kernel.AbstractGraphDatabase;

import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import java.lang.reflect.Method;

class Neo4j18Locker extends Locker {

    private final Object lockManager;
    private final Object lockReleaser;
    private final TransactionManager txManager;

    public Neo4j18Locker(GraphDatabaseService graphDatabaseService) {
        if (!(graphDatabaseService instanceof AbstractGraphDatabase)) throw new RuntimeException("Error accessing transaction management, not a AbstractGraphDatabase " + graphDatabaseService);
        final AbstractGraphDatabase graphDatabase = (AbstractGraphDatabase) graphDatabaseService;
        lockManager = graphDatabase.getLockManager();
        txManager = graphDatabase.getTxManager();
        lockReleaser = invoke(graphDatabase,"getLockReleaser");
    }

    private Object invoke(Object target, String methodName,Pair<Class,?>...params) {
        try {
            Class[] types=new Class[params.length];
            Object[] values=new Object[params.length];
            for (int i = 0; i < params.length; i++) {
                types[i] = params[i].first();
                values[i] = params[i].other();
            }
            Method method = target.getClass().getMethod(methodName,types);
            return method.invoke(target,values);
        } catch(Exception e) {
            System.out.println(target);
            System.out.println(target.getClass());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void acquireLock(LockType lockType, PropertyContainer element) {
        TransactionImpl tx = getCurrentTransaction();
        switch (lockType) {
            case READ:
                invoke(lockManager, "getReadLock", Pair.<Class,Object>of(Object.class,element), Pair.<Class,Object>of(Transaction.class,tx));
                invoke(lockReleaser, "addLockToTransaction", Pair.<Class,Object>of(Object.class,element),Pair.<Class,Object>of(LockType.class,LockType.READ), Pair.<Class,Object>of(Transaction.class,tx));
                break;
            case WRITE:
                invoke(lockManager, "getWriteLock", Pair.<Class,Object>of(Object.class,element), Pair.<Class,Object>of(Transaction.class,tx));
                invoke(lockReleaser, "addLockToTransaction", Pair.<Class,Object>of(Object.class,element),Pair.<Class,Object>of(LockType.class,LockType.WRITE), Pair.<Class,Object>of(Transaction.class,tx));
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
