begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_package
DECL|package|org.elasticsearch.index.percolator
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|percolator
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|index
operator|.
name|Term
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|search
operator|.
name|Query
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticSearchException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|Strings
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|bytes
operator|.
name|BytesReference
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|bytes
operator|.
name|HashedBytesArray
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|inject
operator|.
name|Inject
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|lucene
operator|.
name|search
operator|.
name|TermFilter
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|lucene
operator|.
name|search
operator|.
name|XConstantScoreQuery
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|settings
operator|.
name|Settings
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|text
operator|.
name|BytesText
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|text
operator|.
name|Text
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ConcurrentCollections
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|xcontent
operator|.
name|XContentBuilder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|xcontent
operator|.
name|XContentFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|xcontent
operator|.
name|XContentHelper
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|xcontent
operator|.
name|XContentParser
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|cache
operator|.
name|IndexCache
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|engine
operator|.
name|Engine
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|indexing
operator|.
name|IndexingOperationListener
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|indexing
operator|.
name|ShardIndexingService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
operator|.
name|DocumentTypeListener
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
operator|.
name|MapperService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
operator|.
name|internal
operator|.
name|TypeFieldMapper
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|IndexQueryParserService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|QueryParseContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|settings
operator|.
name|IndexSettings
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|shard
operator|.
name|AbstractIndexShardComponent
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|shard
operator|.
name|ShardId
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|shard
operator|.
name|service
operator|.
name|IndexShard
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|IndicesLifecycle
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|percolator
operator|.
name|PercolatorService
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ConcurrentMap
import|;
end_import

begin_comment
comment|/**  * Each shard will have a percolator registry even if there isn't a _percolator document type in the index.  * For shards with indices that have no _percolator document type, this will hold no percolate queries.  *<p/>  * Once a document type has been created, the real-time percolator will start to listen to write events and update the  * this registry with queries in real time.  */
end_comment

begin_class
DECL|class|PercolatorQueriesRegistry
specifier|public
class|class
name|PercolatorQueriesRegistry
extends|extends
name|AbstractIndexShardComponent
block|{
comment|// This is a shard level service, but these below are index level service:
DECL|field|queryParserService
specifier|private
specifier|final
name|IndexQueryParserService
name|queryParserService
decl_stmt|;
DECL|field|mapperService
specifier|private
specifier|final
name|MapperService
name|mapperService
decl_stmt|;
DECL|field|indicesLifecycle
specifier|private
specifier|final
name|IndicesLifecycle
name|indicesLifecycle
decl_stmt|;
DECL|field|indexCache
specifier|private
specifier|final
name|IndexCache
name|indexCache
decl_stmt|;
DECL|field|indexingService
specifier|private
specifier|final
name|ShardIndexingService
name|indexingService
decl_stmt|;
DECL|field|percolateQueries
specifier|private
specifier|final
name|ConcurrentMap
argument_list|<
name|Text
argument_list|,
name|Query
argument_list|>
name|percolateQueries
init|=
name|ConcurrentCollections
operator|.
name|newConcurrentMapWithAggressiveConcurrency
argument_list|()
decl_stmt|;
DECL|field|shardLifecycleListener
specifier|private
specifier|final
name|ShardLifecycleListener
name|shardLifecycleListener
init|=
operator|new
name|ShardLifecycleListener
argument_list|()
decl_stmt|;
DECL|field|realTimePercolatorOperationListener
specifier|private
specifier|final
name|RealTimePercolatorOperationListener
name|realTimePercolatorOperationListener
init|=
operator|new
name|RealTimePercolatorOperationListener
argument_list|()
decl_stmt|;
DECL|field|percolateTypeListener
specifier|private
specifier|final
name|PercolateTypeListener
name|percolateTypeListener
init|=
operator|new
name|PercolateTypeListener
argument_list|()
decl_stmt|;
DECL|field|realTimePercolatorEnabled
specifier|private
specifier|volatile
name|boolean
name|realTimePercolatorEnabled
init|=
literal|false
decl_stmt|;
annotation|@
name|Inject
DECL|method|PercolatorQueriesRegistry
specifier|public
name|PercolatorQueriesRegistry
parameter_list|(
name|ShardId
name|shardId
parameter_list|,
annotation|@
name|IndexSettings
name|Settings
name|indexSettings
parameter_list|,
name|IndexQueryParserService
name|queryParserService
parameter_list|,
name|ShardIndexingService
name|indexingService
parameter_list|,
name|IndicesLifecycle
name|indicesLifecycle
parameter_list|,
name|MapperService
name|mapperService
parameter_list|,
name|IndexCache
name|indexCache
parameter_list|)
block|{
name|super
argument_list|(
name|shardId
argument_list|,
name|indexSettings
argument_list|)
expr_stmt|;
name|this
operator|.
name|queryParserService
operator|=
name|queryParserService
expr_stmt|;
name|this
operator|.
name|mapperService
operator|=
name|mapperService
expr_stmt|;
name|this
operator|.
name|indicesLifecycle
operator|=
name|indicesLifecycle
expr_stmt|;
name|this
operator|.
name|indexingService
operator|=
name|indexingService
expr_stmt|;
name|this
operator|.
name|indexCache
operator|=
name|indexCache
expr_stmt|;
name|indicesLifecycle
operator|.
name|addListener
argument_list|(
name|shardLifecycleListener
argument_list|)
expr_stmt|;
name|mapperService
operator|.
name|addTypeListener
argument_list|(
name|percolateTypeListener
argument_list|)
expr_stmt|;
block|}
DECL|method|percolateQueries
specifier|public
name|ConcurrentMap
argument_list|<
name|Text
argument_list|,
name|Query
argument_list|>
name|percolateQueries
parameter_list|()
block|{
return|return
name|percolateQueries
return|;
block|}
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
block|{
name|mapperService
operator|.
name|removeTypeListener
argument_list|(
name|percolateTypeListener
argument_list|)
expr_stmt|;
name|indicesLifecycle
operator|.
name|removeListener
argument_list|(
name|shardLifecycleListener
argument_list|)
expr_stmt|;
name|indexingService
operator|.
name|removeListener
argument_list|(
name|realTimePercolatorOperationListener
argument_list|)
expr_stmt|;
name|clear
argument_list|()
expr_stmt|;
block|}
DECL|method|clear
specifier|public
name|void
name|clear
parameter_list|()
block|{
name|percolateQueries
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
DECL|method|enableRealTimePercolator
name|void
name|enableRealTimePercolator
parameter_list|()
block|{
if|if
condition|(
operator|!
name|realTimePercolatorEnabled
condition|)
block|{
name|indexingService
operator|.
name|addListener
argument_list|(
name|realTimePercolatorOperationListener
argument_list|)
expr_stmt|;
name|realTimePercolatorEnabled
operator|=
literal|true
expr_stmt|;
block|}
block|}
DECL|method|disableRealTimePercolator
name|void
name|disableRealTimePercolator
parameter_list|()
block|{
if|if
condition|(
name|realTimePercolatorEnabled
condition|)
block|{
name|indexingService
operator|.
name|removeListener
argument_list|(
name|realTimePercolatorOperationListener
argument_list|)
expr_stmt|;
name|realTimePercolatorEnabled
operator|=
literal|false
expr_stmt|;
block|}
block|}
DECL|method|addPercolateQuery
specifier|public
name|void
name|addPercolateQuery
parameter_list|(
name|String
name|uidAsString
parameter_list|,
name|BytesReference
name|source
parameter_list|)
block|{
name|Query
name|query
init|=
name|parsePercolatorDocument
argument_list|(
name|uidAsString
argument_list|,
name|source
argument_list|)
decl_stmt|;
name|BytesText
name|uid
init|=
operator|new
name|BytesText
argument_list|(
operator|new
name|HashedBytesArray
argument_list|(
name|Strings
operator|.
name|toUTF8Bytes
argument_list|(
name|uidAsString
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|percolateQueries
operator|.
name|put
argument_list|(
name|uid
argument_list|,
name|query
argument_list|)
expr_stmt|;
block|}
DECL|method|removePercolateQuery
specifier|public
name|void
name|removePercolateQuery
parameter_list|(
name|String
name|uidAsString
parameter_list|)
block|{
name|BytesText
name|uid
init|=
operator|new
name|BytesText
argument_list|(
operator|new
name|HashedBytesArray
argument_list|(
name|Strings
operator|.
name|toUTF8Bytes
argument_list|(
name|uidAsString
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|percolateQueries
operator|.
name|remove
argument_list|(
name|uid
argument_list|)
expr_stmt|;
block|}
DECL|method|parsePercolatorDocument
name|Query
name|parsePercolatorDocument
parameter_list|(
name|String
name|uid
parameter_list|,
name|BytesReference
name|source
parameter_list|)
block|{
name|String
name|type
init|=
literal|null
decl_stmt|;
name|BytesReference
name|querySource
init|=
literal|null
decl_stmt|;
name|XContentParser
name|parser
init|=
literal|null
decl_stmt|;
try|try
block|{
name|parser
operator|=
name|XContentHelper
operator|.
name|createParser
argument_list|(
name|source
argument_list|)
expr_stmt|;
name|String
name|currentFieldName
init|=
literal|null
decl_stmt|;
name|XContentParser
operator|.
name|Token
name|token
init|=
name|parser
operator|.
name|nextToken
argument_list|()
decl_stmt|;
comment|// move the START_OBJECT
if|if
condition|(
name|token
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|START_OBJECT
condition|)
block|{
throw|throw
operator|new
name|ElasticSearchException
argument_list|(
literal|"failed to parse query ["
operator|+
name|uid
operator|+
literal|"], not starting with OBJECT"
argument_list|)
throw|;
block|}
while|while
condition|(
operator|(
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
operator|)
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|END_OBJECT
condition|)
block|{
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|FIELD_NAME
condition|)
block|{
name|currentFieldName
operator|=
name|parser
operator|.
name|currentName
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|START_OBJECT
condition|)
block|{
if|if
condition|(
literal|"query"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
if|if
condition|(
name|type
operator|!=
literal|null
condition|)
block|{
return|return
name|parseQuery
argument_list|(
name|type
argument_list|,
literal|null
argument_list|,
name|parser
argument_list|)
return|;
block|}
else|else
block|{
name|XContentBuilder
name|builder
init|=
name|XContentFactory
operator|.
name|contentBuilder
argument_list|(
name|parser
operator|.
name|contentType
argument_list|()
argument_list|)
decl_stmt|;
name|builder
operator|.
name|copyCurrentStructure
argument_list|(
name|parser
argument_list|)
expr_stmt|;
name|querySource
operator|=
name|builder
operator|.
name|bytes
argument_list|()
expr_stmt|;
name|builder
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
else|else
block|{
name|parser
operator|.
name|skipChildren
argument_list|()
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|START_ARRAY
condition|)
block|{
name|parser
operator|.
name|skipChildren
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|token
operator|.
name|isValue
argument_list|()
condition|)
block|{
if|if
condition|(
literal|"type"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|type
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
block|}
block|}
block|}
return|return
name|parseQuery
argument_list|(
name|type
argument_list|,
name|querySource
argument_list|,
literal|null
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|PercolatorException
argument_list|(
name|shardId
argument_list|()
operator|.
name|index
argument_list|()
argument_list|,
literal|"failed to parse query ["
operator|+
name|uid
operator|+
literal|"]"
argument_list|,
name|e
argument_list|)
throw|;
block|}
finally|finally
block|{
if|if
condition|(
name|parser
operator|!=
literal|null
condition|)
block|{
name|parser
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
DECL|method|parseQuery
specifier|private
name|Query
name|parseQuery
parameter_list|(
name|String
name|type
parameter_list|,
name|BytesReference
name|querySource
parameter_list|,
name|XContentParser
name|parser
parameter_list|)
block|{
if|if
condition|(
name|type
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|parser
operator|!=
literal|null
condition|)
block|{
return|return
name|queryParserService
operator|.
name|parse
argument_list|(
name|parser
argument_list|)
operator|.
name|query
argument_list|()
return|;
block|}
else|else
block|{
return|return
name|queryParserService
operator|.
name|parse
argument_list|(
name|querySource
argument_list|)
operator|.
name|query
argument_list|()
return|;
block|}
block|}
name|String
index|[]
name|previousTypes
init|=
name|QueryParseContext
operator|.
name|setTypesWithPrevious
argument_list|(
operator|new
name|String
index|[]
block|{
name|type
block|}
argument_list|)
decl_stmt|;
try|try
block|{
if|if
condition|(
name|parser
operator|!=
literal|null
condition|)
block|{
return|return
name|queryParserService
operator|.
name|parse
argument_list|(
name|parser
argument_list|)
operator|.
name|query
argument_list|()
return|;
block|}
else|else
block|{
return|return
name|queryParserService
operator|.
name|parse
argument_list|(
name|querySource
argument_list|)
operator|.
name|query
argument_list|()
return|;
block|}
block|}
finally|finally
block|{
name|QueryParseContext
operator|.
name|setTypes
argument_list|(
name|previousTypes
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|PercolateTypeListener
specifier|private
class|class
name|PercolateTypeListener
implements|implements
name|DocumentTypeListener
block|{
annotation|@
name|Override
DECL|method|created
specifier|public
name|void
name|created
parameter_list|(
name|String
name|type
parameter_list|)
block|{
if|if
condition|(
name|PercolatorService
operator|.
name|Constants
operator|.
name|TYPE_NAME
operator|.
name|equals
argument_list|(
name|type
argument_list|)
condition|)
block|{
name|enableRealTimePercolator
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|removed
specifier|public
name|void
name|removed
parameter_list|(
name|String
name|type
parameter_list|)
block|{
if|if
condition|(
name|PercolatorService
operator|.
name|Constants
operator|.
name|TYPE_NAME
operator|.
name|equals
argument_list|(
name|type
argument_list|)
condition|)
block|{
name|disableRealTimePercolator
argument_list|()
expr_stmt|;
name|clear
argument_list|()
expr_stmt|;
block|}
block|}
block|}
DECL|class|ShardLifecycleListener
specifier|private
class|class
name|ShardLifecycleListener
extends|extends
name|IndicesLifecycle
operator|.
name|Listener
block|{
annotation|@
name|Override
DECL|method|afterIndexShardCreated
specifier|public
name|void
name|afterIndexShardCreated
parameter_list|(
name|IndexShard
name|indexShard
parameter_list|)
block|{
if|if
condition|(
name|hasPercolatorType
argument_list|(
name|indexShard
argument_list|)
condition|)
block|{
name|enableRealTimePercolator
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|afterIndexShardStarted
specifier|public
name|void
name|afterIndexShardStarted
parameter_list|(
name|IndexShard
name|indexShard
parameter_list|)
block|{
if|if
condition|(
name|hasPercolatorType
argument_list|(
name|indexShard
argument_list|)
condition|)
block|{
comment|// percolator index has started, fetch what we can from it and initialize the indices
comment|// we have
name|logger
operator|.
name|debug
argument_list|(
literal|"loading percolator queries for index [{}] and shard[{}]..."
argument_list|,
name|shardId
operator|.
name|index
argument_list|()
argument_list|,
name|shardId
operator|.
name|id
argument_list|()
argument_list|)
expr_stmt|;
name|loadQueries
argument_list|(
name|indexShard
argument_list|)
expr_stmt|;
name|logger
operator|.
name|trace
argument_list|(
literal|"done loading percolator queries for index [{}] and shard[{}]"
argument_list|,
name|shardId
operator|.
name|index
argument_list|()
argument_list|,
name|shardId
operator|.
name|id
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|hasPercolatorType
specifier|private
name|boolean
name|hasPercolatorType
parameter_list|(
name|IndexShard
name|indexShard
parameter_list|)
block|{
name|ShardId
name|otherShardId
init|=
name|indexShard
operator|.
name|shardId
argument_list|()
decl_stmt|;
return|return
name|shardId
operator|.
name|equals
argument_list|(
name|otherShardId
argument_list|)
operator|&&
name|mapperService
operator|.
name|hasMapping
argument_list|(
name|PercolatorService
operator|.
name|Constants
operator|.
name|TYPE_NAME
argument_list|)
return|;
block|}
DECL|method|loadQueries
specifier|private
name|void
name|loadQueries
parameter_list|(
name|IndexShard
name|shard
parameter_list|)
block|{
try|try
block|{
name|shard
operator|.
name|refresh
argument_list|(
operator|new
name|Engine
operator|.
name|Refresh
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|Engine
operator|.
name|Searcher
name|searcher
init|=
name|shard
operator|.
name|searcher
argument_list|()
decl_stmt|;
try|try
block|{
name|Query
name|query
init|=
operator|new
name|XConstantScoreQuery
argument_list|(
name|indexCache
operator|.
name|filter
argument_list|()
operator|.
name|cache
argument_list|(
operator|new
name|TermFilter
argument_list|(
operator|new
name|Term
argument_list|(
name|TypeFieldMapper
operator|.
name|NAME
argument_list|,
name|PercolatorService
operator|.
name|Constants
operator|.
name|TYPE_NAME
argument_list|)
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|QueriesLoaderCollector
name|queries
init|=
operator|new
name|QueriesLoaderCollector
argument_list|(
name|PercolatorQueriesRegistry
operator|.
name|this
argument_list|,
name|logger
argument_list|)
decl_stmt|;
name|searcher
operator|.
name|searcher
argument_list|()
operator|.
name|search
argument_list|(
name|query
argument_list|,
name|queries
argument_list|)
expr_stmt|;
name|percolateQueries
operator|.
name|putAll
argument_list|(
name|queries
operator|.
name|queries
argument_list|()
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|searcher
operator|.
name|release
argument_list|()
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|PercolatorException
argument_list|(
name|shardId
operator|.
name|index
argument_list|()
argument_list|,
literal|"failed to load queries from percolator index"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
DECL|class|RealTimePercolatorOperationListener
specifier|private
class|class
name|RealTimePercolatorOperationListener
extends|extends
name|IndexingOperationListener
block|{
annotation|@
name|Override
DECL|method|preCreate
specifier|public
name|Engine
operator|.
name|Create
name|preCreate
parameter_list|(
name|Engine
operator|.
name|Create
name|create
parameter_list|)
block|{
comment|// validate the query here, before we index
if|if
condition|(
name|PercolatorService
operator|.
name|Constants
operator|.
name|TYPE_NAME
operator|.
name|equals
argument_list|(
name|create
operator|.
name|type
argument_list|()
argument_list|)
condition|)
block|{
name|parsePercolatorDocument
argument_list|(
name|create
operator|.
name|id
argument_list|()
argument_list|,
name|create
operator|.
name|source
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|create
return|;
block|}
annotation|@
name|Override
DECL|method|postCreateUnderLock
specifier|public
name|void
name|postCreateUnderLock
parameter_list|(
name|Engine
operator|.
name|Create
name|create
parameter_list|)
block|{
comment|// add the query under a doc lock
if|if
condition|(
name|PercolatorService
operator|.
name|Constants
operator|.
name|TYPE_NAME
operator|.
name|equals
argument_list|(
name|create
operator|.
name|type
argument_list|()
argument_list|)
condition|)
block|{
name|addPercolateQuery
argument_list|(
name|create
operator|.
name|id
argument_list|()
argument_list|,
name|create
operator|.
name|source
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|preIndex
specifier|public
name|Engine
operator|.
name|Index
name|preIndex
parameter_list|(
name|Engine
operator|.
name|Index
name|index
parameter_list|)
block|{
comment|// validate the query here, before we index
if|if
condition|(
name|PercolatorService
operator|.
name|Constants
operator|.
name|TYPE_NAME
operator|.
name|equals
argument_list|(
name|index
operator|.
name|type
argument_list|()
argument_list|)
condition|)
block|{
name|parsePercolatorDocument
argument_list|(
name|index
operator|.
name|id
argument_list|()
argument_list|,
name|index
operator|.
name|source
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|index
return|;
block|}
annotation|@
name|Override
DECL|method|postIndexUnderLock
specifier|public
name|void
name|postIndexUnderLock
parameter_list|(
name|Engine
operator|.
name|Index
name|index
parameter_list|)
block|{
comment|// add the query under a doc lock
if|if
condition|(
name|PercolatorService
operator|.
name|Constants
operator|.
name|TYPE_NAME
operator|.
name|equals
argument_list|(
name|index
operator|.
name|type
argument_list|()
argument_list|)
condition|)
block|{
name|addPercolateQuery
argument_list|(
name|index
operator|.
name|id
argument_list|()
argument_list|,
name|index
operator|.
name|source
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|postDeleteUnderLock
specifier|public
name|void
name|postDeleteUnderLock
parameter_list|(
name|Engine
operator|.
name|Delete
name|delete
parameter_list|)
block|{
comment|// remove the query under a lock
if|if
condition|(
name|PercolatorService
operator|.
name|Constants
operator|.
name|TYPE_NAME
operator|.
name|equals
argument_list|(
name|delete
operator|.
name|type
argument_list|()
argument_list|)
condition|)
block|{
name|removePercolateQuery
argument_list|(
name|delete
operator|.
name|id
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Updating the live percolate queries for a delete by query is tricky with the current way delete by queries
comment|// are handled. It is only possible if we put a big lock around the post delete by query hook...
comment|// If we implement delete by query, that just runs a query and generates delete operations in a bulk, then
comment|// updating the live percolator is automatically supported for delete by query.
comment|//        @Override
comment|//        public void postDeleteByQuery(Engine.DeleteByQuery deleteByQuery) {
comment|//        }
block|}
block|}
end_class

end_unit

