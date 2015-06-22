begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

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
name|apache
operator|.
name|lucene
operator|.
name|search
operator|.
name|TermQuery
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
name|util
operator|.
name|BytesRef
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
name|util
operator|.
name|CloseableThreadLocal
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchException
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
name|fielddata
operator|.
name|IndexFieldDataService
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
name|DocumentMapper
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
name|percolator
operator|.
name|stats
operator|.
name|ShardPercolateService
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
name|query
operator|.
name|QueryParsingException
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
name|IndexShard
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
name|io
operator|.
name|Closeable
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicBoolean
import|;
end_import

begin_comment
comment|/**  * Each shard will have a percolator registry even if there isn't a {@link PercolatorService#TYPE_NAME} document type in the index.  * For shards with indices that have no {@link PercolatorService#TYPE_NAME} document type, this will hold no percolate queries.  *<p/>  * Once a document type has been created, the real-time percolator will start to listen to write events and update the  * this registry with queries in real time.  */
end_comment

begin_class
DECL|class|PercolatorQueriesRegistry
specifier|public
class|class
name|PercolatorQueriesRegistry
extends|extends
name|AbstractIndexShardComponent
implements|implements
name|Closeable
block|{
DECL|field|MAP_UNMAPPED_FIELDS_AS_STRING
specifier|public
specifier|final
name|String
name|MAP_UNMAPPED_FIELDS_AS_STRING
init|=
literal|"index.percolator.map_unmapped_fields_as_string"
decl_stmt|;
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
DECL|field|indexFieldDataService
specifier|private
specifier|final
name|IndexFieldDataService
name|indexFieldDataService
decl_stmt|;
DECL|field|indexingService
specifier|private
specifier|final
name|ShardIndexingService
name|indexingService
decl_stmt|;
DECL|field|shardPercolateService
specifier|private
specifier|final
name|ShardPercolateService
name|shardPercolateService
decl_stmt|;
DECL|field|percolateQueries
specifier|private
specifier|final
name|ConcurrentMap
argument_list|<
name|BytesRef
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
specifier|final
name|AtomicBoolean
name|realTimePercolatorEnabled
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
DECL|field|mapUnmappedFieldsAsString
specifier|private
name|boolean
name|mapUnmappedFieldsAsString
decl_stmt|;
DECL|field|cache
specifier|private
name|CloseableThreadLocal
argument_list|<
name|QueryParseContext
argument_list|>
name|cache
init|=
operator|new
name|CloseableThreadLocal
argument_list|<
name|QueryParseContext
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|protected
name|QueryParseContext
name|initialValue
parameter_list|()
block|{
return|return
operator|new
name|QueryParseContext
argument_list|(
name|shardId
operator|.
name|index
argument_list|()
argument_list|,
name|queryParserService
argument_list|)
return|;
block|}
block|}
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
name|IndexFieldDataService
name|indexFieldDataService
parameter_list|,
name|ShardPercolateService
name|shardPercolateService
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
name|indexFieldDataService
operator|=
name|indexFieldDataService
expr_stmt|;
name|this
operator|.
name|shardPercolateService
operator|=
name|shardPercolateService
expr_stmt|;
name|this
operator|.
name|mapUnmappedFieldsAsString
operator|=
name|indexSettings
operator|.
name|getAsBoolean
argument_list|(
name|MAP_UNMAPPED_FIELDS_AS_STRING
argument_list|,
literal|false
argument_list|)
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
name|BytesRef
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
annotation|@
name|Override
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
name|realTimePercolatorEnabled
operator|.
name|compareAndSet
argument_list|(
literal|false
argument_list|,
literal|true
argument_list|)
condition|)
block|{
name|indexingService
operator|.
name|addListener
argument_list|(
name|realTimePercolatorOperationListener
argument_list|)
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
operator|.
name|compareAndSet
argument_list|(
literal|true
argument_list|,
literal|false
argument_list|)
condition|)
block|{
name|indexingService
operator|.
name|removeListener
argument_list|(
name|realTimePercolatorOperationListener
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|addPercolateQuery
specifier|public
name|void
name|addPercolateQuery
parameter_list|(
name|String
name|idAsString
parameter_list|,
name|BytesReference
name|source
parameter_list|)
block|{
name|Query
name|newquery
init|=
name|parsePercolatorDocument
argument_list|(
name|idAsString
argument_list|,
name|source
argument_list|)
decl_stmt|;
name|BytesRef
name|id
init|=
operator|new
name|BytesRef
argument_list|(
name|idAsString
argument_list|)
decl_stmt|;
name|Query
name|previousQuery
init|=
name|percolateQueries
operator|.
name|put
argument_list|(
name|id
argument_list|,
name|newquery
argument_list|)
decl_stmt|;
name|shardPercolateService
operator|.
name|addedQuery
argument_list|(
name|id
argument_list|,
name|previousQuery
argument_list|,
name|newquery
argument_list|)
expr_stmt|;
block|}
DECL|method|removePercolateQuery
specifier|public
name|void
name|removePercolateQuery
parameter_list|(
name|String
name|idAsString
parameter_list|)
block|{
name|BytesRef
name|id
init|=
operator|new
name|BytesRef
argument_list|(
name|idAsString
argument_list|)
decl_stmt|;
name|Query
name|query
init|=
name|percolateQueries
operator|.
name|remove
argument_list|(
name|id
argument_list|)
decl_stmt|;
if|if
condition|(
name|query
operator|!=
literal|null
condition|)
block|{
name|shardPercolateService
operator|.
name|removedQuery
argument_list|(
name|id
argument_list|,
name|query
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|parsePercolatorDocument
name|Query
name|parsePercolatorDocument
parameter_list|(
name|String
name|id
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
try|try
init|(
name|XContentParser
name|sourceParser
init|=
name|XContentHelper
operator|.
name|createParser
argument_list|(
name|source
argument_list|)
init|)
block|{
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
name|sourceParser
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
name|ElasticsearchException
argument_list|(
literal|"failed to parse query ["
operator|+
name|id
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
name|sourceParser
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
name|sourceParser
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
name|sourceParser
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
name|sourceParser
operator|.
name|contentType
argument_list|()
argument_list|)
decl_stmt|;
name|builder
operator|.
name|copyCurrentStructure
argument_list|(
name|sourceParser
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
name|sourceParser
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
name|sourceParser
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
name|sourceParser
operator|.
name|text
argument_list|()
expr_stmt|;
block|}
block|}
block|}
try|try
init|(
name|XContentParser
name|queryParser
init|=
name|XContentHelper
operator|.
name|createParser
argument_list|(
name|querySource
argument_list|)
init|)
block|{
return|return
name|parseQuery
argument_list|(
name|type
argument_list|,
name|queryParser
argument_list|)
return|;
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
argument_list|()
operator|.
name|index
argument_list|()
argument_list|,
literal|"failed to parse query ["
operator|+
name|id
operator|+
literal|"]"
argument_list|,
name|e
argument_list|)
throw|;
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
name|XContentParser
name|parser
parameter_list|)
block|{
name|String
index|[]
name|previousTypes
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|type
operator|!=
literal|null
condition|)
block|{
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
expr_stmt|;
block|}
name|QueryParseContext
name|context
init|=
name|cache
operator|.
name|get
argument_list|()
decl_stmt|;
try|try
block|{
name|context
operator|.
name|reset
argument_list|(
name|parser
argument_list|)
expr_stmt|;
comment|// This means that fields in the query need to exist in the mapping prior to registering this query
comment|// The reason that this is required, is that if a field doesn't exist then the query assumes defaults, which may be undesired.
comment|//
comment|// Even worse when fields mentioned in percolator queries do go added to map after the queries have been registered
comment|// then the percolator queries don't work as expected any more.
comment|//
comment|// Query parsing can't introduce new fields in mappings (which happens when registering a percolator query),
comment|// because field type can't be inferred from queries (like document do) so the best option here is to disallow
comment|// the usage of unmapped fields in percolator queries to avoid unexpected behaviour
comment|//
comment|// if index.percolator.map_unmapped_fields_as_string is set to true, query can contain unmapped fields which will be mapped
comment|// as an analyzed string.
name|context
operator|.
name|setAllowUnmappedFields
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|context
operator|.
name|setMapUnmappedFieldAsString
argument_list|(
name|mapUnmappedFieldsAsString
condition|?
literal|true
else|:
literal|false
argument_list|)
expr_stmt|;
return|return
name|queryParserService
operator|.
name|parseInnerQuery
argument_list|(
name|context
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|QueryParsingException
argument_list|(
name|context
argument_list|,
literal|"Failed to parse"
argument_list|,
name|e
argument_list|)
throw|;
block|}
finally|finally
block|{
if|if
condition|(
name|type
operator|!=
literal|null
condition|)
block|{
name|QueryParseContext
operator|.
name|setTypes
argument_list|(
name|previousTypes
argument_list|)
expr_stmt|;
block|}
name|context
operator|.
name|reset
argument_list|(
literal|null
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
DECL|method|beforeCreate
specifier|public
name|void
name|beforeCreate
parameter_list|(
name|DocumentMapper
name|mapper
parameter_list|)
block|{
if|if
condition|(
name|PercolatorService
operator|.
name|TYPE_NAME
operator|.
name|equals
argument_list|(
name|mapper
operator|.
name|type
argument_list|()
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
DECL|method|afterRemove
specifier|public
name|void
name|afterRemove
parameter_list|(
name|DocumentMapper
name|mapper
parameter_list|)
block|{
if|if
condition|(
name|PercolatorService
operator|.
name|TYPE_NAME
operator|.
name|equals
argument_list|(
name|mapper
operator|.
name|type
argument_list|()
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
DECL|method|afterIndexShardPostRecovery
specifier|public
name|void
name|afterIndexShardPostRecovery
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
name|trace
argument_list|(
literal|"loading percolator queries for [{}]..."
argument_list|,
name|shardId
argument_list|)
expr_stmt|;
name|int
name|loadedQueries
init|=
name|loadQueries
argument_list|(
name|indexShard
argument_list|)
decl_stmt|;
name|logger
operator|.
name|debug
argument_list|(
literal|"done loading [{}] percolator queries for [{}]"
argument_list|,
name|loadedQueries
argument_list|,
name|shardId
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
name|TYPE_NAME
argument_list|)
return|;
block|}
DECL|method|loadQueries
specifier|private
name|int
name|loadQueries
parameter_list|(
name|IndexShard
name|shard
parameter_list|)
block|{
name|shard
operator|.
name|refresh
argument_list|(
literal|"percolator_load_queries"
argument_list|)
expr_stmt|;
comment|// Maybe add a mode load? This isn't really a write. We need write b/c state=post_recovery
try|try
init|(
name|Engine
operator|.
name|Searcher
name|searcher
init|=
name|shard
operator|.
name|acquireSearcher
argument_list|(
literal|"percolator_load_queries"
argument_list|,
literal|true
argument_list|)
init|)
block|{
name|Query
name|query
init|=
operator|new
name|TermQuery
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
name|TYPE_NAME
argument_list|)
argument_list|)
decl_stmt|;
name|QueriesLoaderCollector
name|queryCollector
init|=
operator|new
name|QueriesLoaderCollector
argument_list|(
name|PercolatorQueriesRegistry
operator|.
name|this
argument_list|,
name|logger
argument_list|,
name|mapperService
argument_list|,
name|indexFieldDataService
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
name|queryCollector
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|BytesRef
argument_list|,
name|Query
argument_list|>
name|queries
init|=
name|queryCollector
operator|.
name|queries
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|BytesRef
argument_list|,
name|Query
argument_list|>
name|entry
range|:
name|queries
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|Query
name|previousQuery
init|=
name|percolateQueries
operator|.
name|put
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
decl_stmt|;
name|shardPercolateService
operator|.
name|addedQuery
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|previousQuery
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|queries
operator|.
name|size
argument_list|()
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
block|}
block|}
end_class

end_unit

