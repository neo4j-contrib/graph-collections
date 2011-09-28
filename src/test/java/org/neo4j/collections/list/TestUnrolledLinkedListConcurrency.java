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
package org.neo4j.collections.list;

import org.junit.Test;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class TestUnrolledLinkedListConcurrency extends UnrolledLinkedListTestCase
{
    private enum Signals
    {
        WRITE, READ
    }

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Test
    public void testAddInSamePageHavingReadPastWithReadTransaction() throws Exception
    {
        final ArrayList<Node> nodes = createNodes( 4 );
        final UnrolledLinkedList list = new UnrolledLinkedList( graphDb(), new IdComparator(), 4 );
        final SignalSynchronizer sync = new SignalSynchronizer( Signals.class );

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
                    assertTrue( sync.wait( Signals.READ ) );
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
                            assertFalse( sync.signalAndWait( Signals.WRITE, Signals.READ, 1, TimeUnit.SECONDS ) );
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
                assertTrue( sync.signalAndWait( Signals.READ, Signals.WRITE ) );
            }
        }
        restartTx();
        sync.signal( Signals.READ );

        assertTrue( reader.get( 1000, TimeUnit.MILLISECONDS ) );
    }

    @Test
    public void testAddInSamePageHavingReadPastWithoutReadTransaction() throws Exception
    {
        final ArrayList<Node> nodes = createNodes( 4 );
        final UnrolledLinkedList list = new UnrolledLinkedList( graphDb(), new IdComparator(), 4 );
        final SignalSynchronizer sync = new SignalSynchronizer( Signals.class );

        FutureTask<Boolean> reader = new ThrowingFutureTask<Boolean>( new Callable<Boolean>()
        {
            @Override
            public Boolean call() throws Exception
            {
                ArrayList<Node> innerNodes = new ArrayList<Node>( nodes.subList( 0, 3 ) );
                Collections.reverse( innerNodes );
                assertTrue( sync.wait( Signals.READ ) );
                UnrolledLinkedList innerList = new UnrolledLinkedList( list.getBaseNode() );

                int count = 0;
                for ( Node node : innerList )
                {
                    assertEquals( innerNodes.get( count ), node );
                    if ( ++count == 2 )
                    {
                        // will receive read signal as there will be no read lock against the base node therefore
                        // the writer add node will not block
                        assertTrue( sync.signalAndWait( Signals.WRITE, Signals.READ, 1, TimeUnit.SECONDS ) );
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
                assertTrue( sync.signalAndWait( Signals.READ, Signals.WRITE ) );
            }
        }
        restartTx();
        sync.signal( Signals.READ );

        assertTrue( reader.get( 1000, TimeUnit.MILLISECONDS ) );
    }

    private static class SignalSynchronizer
    {
        private static final long MAX_WAIT_SECONDS = 10;
        private final Lock lock;
        private final Map<Enum, Condition> conditions;

        public SignalSynchronizer( Class<? extends Enum> signals )
        {
            lock = new ReentrantLock();
            conditions = new HashMap<Enum, Condition>();

            for ( Enum signal : signals.getEnumConstants() )
            {
                conditions.put( signal, lock.newCondition() );
            }
        }

        public boolean wait( Enum waitSignal, long timeout, TimeUnit unit ) throws InterruptedException
        {
            if ( !conditions.containsKey( waitSignal ) )
            {
                throw new IllegalArgumentException( "Not a valid signal: " + waitSignal );
            }

            lock.lock();
            try
            {
                return conditions.get( waitSignal ).await( timeout, unit );
            }
            finally
            {
                lock.unlock();
            }
        }

        public boolean wait( Enum waitSignal ) throws InterruptedException
        {
            if ( !conditions.containsKey( waitSignal ) )
            {
                throw new IllegalArgumentException( "Not a valid signal: " + waitSignal );
            }

            lock.lock();
            try
            {
                return conditions.get( waitSignal ).await( MAX_WAIT_SECONDS, TimeUnit.SECONDS );
            }
            finally
            {
                lock.unlock();
            }
        }

        public boolean signalAndWait( Enum signal, Enum waitSignal, long timeout, TimeUnit unit )
            throws InterruptedException
        {
            if ( !conditions.containsKey( signal ) )
            {
                throw new IllegalArgumentException( "Not a valid signal: " + signal );
            }
            if ( !conditions.containsKey( waitSignal ) )
            {
                throw new IllegalArgumentException( "Not a valid signal: " + waitSignal );
            }

            lock.lock();
            try
            {
                conditions.get( signal ).signal();
                return conditions.get( waitSignal ).await( timeout, unit );
            }
            finally
            {
                lock.unlock();
            }
        }

        public boolean signalAndWait( Enum signal, Enum waitSignal ) throws InterruptedException
        {
            if ( !conditions.containsKey( signal ) )
            {
                throw new IllegalArgumentException( "Not a valid signal: " + signal );
            }
            if ( !conditions.containsKey( waitSignal ) )
            {
                throw new IllegalArgumentException( "Not a valid signal: " + waitSignal );
            }

            lock.lock();
            try
            {
                conditions.get( signal ).signal();
                return conditions.get( waitSignal ).await( MAX_WAIT_SECONDS, TimeUnit.SECONDS );
            }
            finally
            {
                lock.unlock();
            }
        }

        public void signal( Enum signal )
        {
            if ( !conditions.containsKey( signal ) )
            {
                throw new IllegalArgumentException( "Not a valid signal: " + signal );
            }

            lock.lock();
            try
            {
                conditions.get( signal ).signal();
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