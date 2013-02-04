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
