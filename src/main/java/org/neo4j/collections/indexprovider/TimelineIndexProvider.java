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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.graphdb.DependencyResolver;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexImplementation;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.graphdb.index.IndexProvider;
import org.neo4j.graphdb.index.RelationshipIndex;
import org.neo4j.helpers.collection.MapUtil;
import org.neo4j.index.impl.lucene.LuceneDataSource;
import org.neo4j.kernel.configuration.Config;

public class TimelineIndexProvider extends IndexProvider
{

    public static final String SERVICE_NAME = "graph-collections-timeline";

    public TimelineIndexProvider()
    {
        super( SERVICE_NAME );
    }

    @Override
    public IndexImplementation load(DependencyResolver dependencyResolver) throws Exception
    {
        final GraphDatabaseService gds = dependencyResolver.resolveDependency(GraphDatabaseService.class);
        return new TimelineIndexImplementation(gds);
    }

    static class TimelineIndexImplementation implements IndexImplementation
    {

        private GraphDatabaseService db;
        private Map<String, TimelineNodeIndex> indexes = new HashMap<String, TimelineNodeIndex>();

        public TimelineIndexImplementation( GraphDatabaseService db )
        {

            this.db = db;
        }

        @Override
        public String getDataSourceName()
        {
            return LuceneDataSource.DEFAULT_NAME;
        }

        @Override
        public Index<Node> nodeIndex( String indexName,
                Map<String, String> config )
        {
            TimelineNodeIndex index = indexes.get( indexName );
            if ( index == null )
            {
                index = new TimelineNodeIndex( indexName, db,
                        config );
                indexes.put( indexName, index );
            }
            return index;
        }

        @Override
        public RelationshipIndex relationshipIndex( String indexName,
                Map<String, String> config )
        {
            throw new UnsupportedOperationException(
                    "timeline relationship indexing is not supported at the moment. Please use the node index." );
        }

        @Override
        public Map<String, String> fillInDefaults( Map<String, String> config )
        {
            return config;
        }

        @Override
        public boolean configMatches( Map<String, String> config1,
                Map<String, String> config2 )
        {
            return config1.equals(config2);
        }
    }

    public static final Map<String, String> CONFIG = Collections.unmodifiableMap( MapUtil.stringMap(
            IndexManager.PROVIDER, SERVICE_NAME ) );
}
