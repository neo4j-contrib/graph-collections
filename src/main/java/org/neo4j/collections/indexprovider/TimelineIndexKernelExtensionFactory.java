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
package org.neo4j.collections.indexprovider;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.index.IndexProviders;
import org.neo4j.kernel.extension.KernelExtensionFactory;
import org.neo4j.kernel.lifecycle.Lifecycle;

import static org.neo4j.collections.indexprovider.TimelineIndexProvider.TimelineIndexImplementation;

public class TimelineIndexKernelExtensionFactory extends KernelExtensionFactory<TimelineIndexKernelExtensionFactory.Depencies> {

    public TimelineIndexKernelExtensionFactory() {
        super(TimelineIndexProvider.SERVICE_NAME);
    }

    @Override
    public Lifecycle newKernelExtension(Depencies depencies) throws Throwable {
        return new TimelineIndexKerneExtension(depencies.getGraphDatabaseService(), depencies.getIndexProviders());
    }

    public static interface Depencies {
        IndexProviders getIndexProviders();

        GraphDatabaseService getGraphDatabaseService();

    }

    public static class TimelineIndexKerneExtension implements Lifecycle {
        private final GraphDatabaseService graphDatabaseService;
        private final IndexProviders indexProviders;

        public TimelineIndexKerneExtension(GraphDatabaseService graphDatabaseService, IndexProviders indexProviders) {
            this.graphDatabaseService = graphDatabaseService;
            this.indexProviders = indexProviders;
        }

        @Override
        public void init() throws Throwable {
        }

        @Override
        public void start() throws Throwable {
            TimelineIndexImplementation indexImplementation = new TimelineIndexImplementation(graphDatabaseService);
            indexProviders.registerIndexProvider(TimelineIndexProvider.SERVICE_NAME, indexImplementation);
        }

        @Override
        public void stop() throws Throwable {
            indexProviders.unregisterIndexProvider(TimelineIndexProvider.SERVICE_NAME);
        }

        @Override
        public void shutdown() throws Throwable {
        }
    }
}
