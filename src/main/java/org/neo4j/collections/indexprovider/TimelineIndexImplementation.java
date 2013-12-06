package org.neo4j.collections.indexprovider;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexImplementation;
import org.neo4j.graphdb.index.RelationshipIndex;
import org.neo4j.index.impl.lucene.LuceneDataSource;

import java.util.HashMap;
import java.util.Map;

/**
* @author mh
* @since 25.11.13
*/
class TimelineIndexImplementation implements IndexImplementation
{

    private GraphDatabaseService db;
    private Map<String, TimelineNodeIndex> indexes = new HashMap<String, TimelineNodeIndex>();

    public TimelineIndexImplementation(GraphDatabaseService db)
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
