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
package org.neo4j.collections.list;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.Ignore;
import org.junit.Test;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

public class TestUnrolledLinkedListConcurrency extends UnrolledLinkedListTestCase
{
    private enum States
    {
        WRITE, READ
    }

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Test
    public void testAddInSamePageHavingReadPastWithReadTransaction() throws Exception
    {
        final ArrayList<Node> nodes = createNodes( 4 );
        final UnrolledLinkedList list = new UnrolledLinkedList( graphDb(), new IdComparator(), 4 );
        final StateSynchronizer sync = new StateSynchronizer( States.class );

        FutureTask<Boolean> reader = new ThrowingFutureTask<Boolean>( new Callable<Boolean>()
        {
            @Override
            public Boolean call() throws Exception
            {
                Transaction tx = graphDb().beginTx();
                try
                {
                    ArrayList<Node> innerNodes = new ArrayList<Node>( nodes.subList( 0, 3 ) );
                    Collections.reverse( innerNodes );
                    assertTrue( sync.wait( States.READ ) );
                    UnrolledLinkedList innerList = new UnrolledLinkedList( list.getBaseNode() );

                    int count = 0;
                    for ( Node node : innerList )
                    {
                        assertEquals( innerNodes.get( count ), node );
                        if ( ++count == 2 )
                        {
                            // wont receive read signal as writer will be waiting to gain a write lock on base node
                            // will timeout then finish its reading process, whereby the writer will gain write lock
                            // and complete its write process
                            assertFalse( sync.signalAndWait( States.WRITE, States.READ, 1, TimeUnit.SECONDS ) );
                        }
                    }

                    tx.success();
                    return true;
                }
                finally
                {
                    tx.finish();
                }
            }
        } );
        executorService.execute( reader );

        int count = 0;
        for ( Node node : nodes )
        {
            list.addNode( node );
            if ( ++count == 3 )
            {
                // commits the nodes added through by the writer so the reader can then see them
                // also releases the write lock held on base node allowing reader thread access
                restartTx();
                assertTrue( sync.signalAndWait( States.READ, States.WRITE ) );
            }
        }
        restartTx();
        sync.signal( States.READ );

        assertTrue( reader.get( 1000, TimeUnit.MILLISECONDS ) );
    }

    @Test
    @Ignore("Not relevant anymore as there are no non-transactional reads")
    public void testAddInSamePageHavingReadPastWithoutReadTransaction() throws Exception
    {
        final ArrayList<Node> nodes = createNodes( 4 );
        final UnrolledLinkedList list = new UnrolledLinkedList( graphDb(), new IdComparator(), 4 );
        final StateSynchronizer sync = new StateSynchronizer( States.class );

        FutureTask<Boolean> reader = new ThrowingFutureTask<Boolean>( new Callable<Boolean>()
        {
            @Override
            public Boolean call() throws Exception
            {
                ArrayList<Node> innerNodes = new ArrayList<Node>( nodes.subList( 0, 3 ) );
                Collections.reverse( innerNodes );
                assertTrue( sync.wait( States.READ ) );
                UnrolledLinkedList innerList = new UnrolledLinkedList( list.getBaseNode() );

                int count = 0;
                for ( Node node : innerList )
                {
                    assertEquals( innerNodes.get( count ), node );
                    if ( ++count == 2 )
                    {
                        // will receive read signal as there will be no read lock against the base node therefore
                        // the writer add node will not block
                        assertTrue( sync.signalAndWait( States.WRITE, States.READ, 1, TimeUnit.SECONDS ) );
                    }
                }

                return true;
            }
        } );
        executorService.execute( reader );

        int count = 0;
        for ( Node node : nodes )
        {
            list.addNode( node );
            if ( ++count == 3 )
            {
                // commits the nodes added through by the writer so the reader can then see them
                // also releases the write lock held on base node allowing reader thread access
                restartTx();
                assertTrue( sync.signalAndWait( States.READ, States.WRITE ) );
            }
        }
        restartTx();
        sync.signal( States.READ );

        assertTrue( reader.get( 1000, TimeUnit.MILLISECONDS ) );
    }

    private static class StateSynchronizer
    {
        private static final long MAX_WAIT_SECONDS = 10;
        private final Lock lock;
        private final Map<Enum, Condition> conditions;
        private Enum state;

        public StateSynchronizer( Class<? extends Enum> states )
        {
            lock = new ReentrantLock();
            conditions = new HashMap<Enum, Condition>();

            for ( Enum state : states.getEnumConstants() )
            {
                conditions.put( state, lock.newCondition() );
            }
        }

        public boolean wait( Enum waitState, long timeout, TimeUnit unit ) throws InterruptedException
        {
            if ( !conditions.containsKey( waitState ) )
            {
                throw new IllegalArgumentException( "Not a valid state: " + waitState );
            }

            lock.lock();
            try
            {
                return waitState.equals( state ) ||
                    conditions.get( waitState ).await( timeout, unit );
            }
            finally
            {
                lock.unlock();
            }
        }

        public boolean wait( Enum waitState ) throws InterruptedException
        {
            if ( !conditions.containsKey( waitState ) )
            {
                throw new IllegalArgumentException( "Not a valid state: " + waitState );
            }

            lock.lock();

            try
            {
                return waitState.equals( state ) ||
                    conditions.get( waitState ).await( MAX_WAIT_SECONDS, TimeUnit.SECONDS );
            }
            finally
            {
                lock.unlock();
            }
        }

        public boolean signalAndWait( Enum state, Enum waitState, long timeout, TimeUnit unit )
            throws InterruptedException
        {
            if ( !conditions.containsKey( state ) )
            {
                throw new IllegalArgumentException( "Not a valid state: " + state );
            }
            if ( !conditions.containsKey( waitState ) )
            {
                throw new IllegalArgumentException( "Not a valid state: " + waitState );
            }

            lock.lock();
            try
            {
                this.state = state;
                conditions.get( state ).signal();
                return conditions.get( waitState ).await( timeout, unit );
            }
            finally
            {
                lock.unlock();
            }
        }

        public boolean signalAndWait( Enum state, Enum waitState ) throws InterruptedException
        {
            if ( !conditions.containsKey( state ) )
            {
                throw new IllegalArgumentException( "Not a valid state: " + state );
            }
            if ( !conditions.containsKey( waitState ) )
            {
                throw new IllegalArgumentException( "Not a valid state: " + waitState );
            }

            lock.lock();
            try
            {
                this.state = state;
                conditions.get( state ).signal();
                return conditions.get( waitState ).await( MAX_WAIT_SECONDS, TimeUnit.SECONDS );
            }
            finally
            {
                lock.unlock();
            }
        }

        public void signal( Enum state )
        {
            if ( !conditions.containsKey( state ) )
            {
                throw new IllegalArgumentException( "Not a valid state: " + state );
            }

            lock.lock();
            try
            {
                this.state = state;
                conditions.get( state ).signal();
            }
            finally
            {
                lock.unlock();
            }
        }
    }

    private static class ThrowingFutureTask<V> extends FutureTask<V>
    {
        public ThrowingFutureTask( Callable<V> callable )
        {
            super( callable );
        }

        @Override
        protected void done()
        {
            super.done();
            try
            {
                if ( !isCancelled() )
                {
                    get();
                }
            }
            catch ( ExecutionException e )
            {
                Throwable cause = e.getCause();
                if ( cause instanceof RuntimeException )
                {
                    throw (RuntimeException) cause;
                }
                if ( cause instanceof Error )
                {
                    throw (Error) cause;
                }
            }
            catch ( InterruptedException e )
            {
                throw new AssertionError( e );
            }
        }
    }
}