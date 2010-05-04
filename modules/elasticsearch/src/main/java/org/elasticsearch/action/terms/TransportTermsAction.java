begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.terms
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|terms
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
name|index
operator|.
name|TermDocs
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
name|index
operator|.
name|TermEnum
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
name|SortField
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
name|StringHelper
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
name|action
operator|.
name|ShardOperationFailedException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|TransportActions
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|support
operator|.
name|DefaultShardOperationFailedException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|support
operator|.
name|broadcast
operator|.
name|BroadcastShardOperationFailedException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|support
operator|.
name|broadcast
operator|.
name|TransportBroadcastOperationAction
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|ClusterService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|ClusterState
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|routing
operator|.
name|GroupShardsIterator
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|routing
operator|.
name|ShardRouting
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
name|mapper
operator|.
name|FieldMapper
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
name|service
operator|.
name|IndexService
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
name|IndicesService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|threadpool
operator|.
name|ThreadPool
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|transport
operator|.
name|TransportService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|BoundedTreeSet
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|collect
operator|.
name|Maps
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|gnu
operator|.
name|trove
operator|.
name|TObjectIntHashMap
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|gnu
operator|.
name|trove
operator|.
name|TObjectIntIterator
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|guice
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
name|util
operator|.
name|settings
operator|.
name|Settings
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|annotation
operator|.
name|Nullable
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
name|*
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
name|AtomicReferenceArray
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|collect
operator|.
name|Lists
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|TransportTermsAction
specifier|public
class|class
name|TransportTermsAction
extends|extends
name|TransportBroadcastOperationAction
argument_list|<
name|TermsRequest
argument_list|,
name|TermsResponse
argument_list|,
name|ShardTermsRequest
argument_list|,
name|ShardTermsResponse
argument_list|>
block|{
DECL|method|TransportTermsAction
annotation|@
name|Inject
specifier|public
name|TransportTermsAction
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|,
name|ClusterService
name|clusterService
parameter_list|,
name|TransportService
name|transportService
parameter_list|,
name|IndicesService
name|indicesService
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|threadPool
argument_list|,
name|clusterService
argument_list|,
name|transportService
argument_list|,
name|indicesService
argument_list|)
expr_stmt|;
block|}
DECL|method|newResponse
annotation|@
name|Override
specifier|protected
name|TermsResponse
name|newResponse
parameter_list|(
specifier|final
name|TermsRequest
name|request
parameter_list|,
name|AtomicReferenceArray
name|shardsResponses
parameter_list|,
name|ClusterState
name|clusterState
parameter_list|)
block|{
name|int
name|successfulShards
init|=
literal|0
decl_stmt|;
name|int
name|failedShards
init|=
literal|0
decl_stmt|;
name|long
name|numDocs
init|=
literal|0
decl_stmt|;
name|long
name|maxDoc
init|=
literal|0
decl_stmt|;
name|long
name|numDeletedDocs
init|=
literal|0
decl_stmt|;
name|List
argument_list|<
name|ShardOperationFailedException
argument_list|>
name|shardFailures
init|=
literal|null
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|TObjectIntHashMap
argument_list|<
name|Object
argument_list|>
argument_list|>
name|aggregator
init|=
name|Maps
operator|.
name|newHashMap
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|shardsResponses
operator|.
name|length
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|Object
name|shardResponse
init|=
name|shardsResponses
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|shardResponse
operator|==
literal|null
condition|)
block|{
name|failedShards
operator|++
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|shardResponse
operator|instanceof
name|BroadcastShardOperationFailedException
condition|)
block|{
name|failedShards
operator|++
expr_stmt|;
if|if
condition|(
name|shardFailures
operator|==
literal|null
condition|)
block|{
name|shardFailures
operator|=
name|newArrayList
argument_list|()
expr_stmt|;
block|}
name|shardFailures
operator|.
name|add
argument_list|(
operator|new
name|DefaultShardOperationFailedException
argument_list|(
operator|(
name|BroadcastShardOperationFailedException
operator|)
name|shardResponse
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|ShardTermsResponse
name|shardTermsResponse
init|=
operator|(
name|ShardTermsResponse
operator|)
name|shardResponse
decl_stmt|;
name|IndexService
name|indexService
init|=
name|indicesService
operator|.
name|indexServiceSafe
argument_list|(
name|shardTermsResponse
operator|.
name|index
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|TObjectIntHashMap
argument_list|<
name|Object
argument_list|>
argument_list|>
name|entry
range|:
name|shardTermsResponse
operator|.
name|fieldsTermsFreqs
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|fieldName
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|FieldMapper
name|fieldMapper
init|=
name|indexService
operator|.
name|mapperService
argument_list|()
operator|.
name|smartNameFieldMapper
argument_list|(
name|fieldName
argument_list|)
decl_stmt|;
name|TObjectIntHashMap
argument_list|<
name|Object
argument_list|>
name|termsFreqs
init|=
name|aggregator
operator|.
name|get
argument_list|(
name|fieldName
argument_list|)
decl_stmt|;
if|if
condition|(
name|termsFreqs
operator|==
literal|null
condition|)
block|{
name|termsFreqs
operator|=
operator|new
name|TObjectIntHashMap
argument_list|<
name|Object
argument_list|>
argument_list|()
expr_stmt|;
name|aggregator
operator|.
name|put
argument_list|(
name|fieldName
argument_list|,
name|termsFreqs
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|TObjectIntIterator
argument_list|<
name|Object
argument_list|>
name|it
init|=
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|iterator
argument_list|()
init|;
name|it
operator|.
name|hasNext
argument_list|()
condition|;
control|)
block|{
name|it
operator|.
name|advance
argument_list|()
expr_stmt|;
name|Object
name|termValue
init|=
name|it
operator|.
name|key
argument_list|()
decl_stmt|;
name|int
name|freq
init|=
name|it
operator|.
name|value
argument_list|()
decl_stmt|;
if|if
condition|(
name|fieldMapper
operator|!=
literal|null
condition|)
block|{
name|termValue
operator|=
name|fieldMapper
operator|.
name|valueForSearch
argument_list|(
name|termValue
argument_list|)
expr_stmt|;
block|}
name|termsFreqs
operator|.
name|adjustOrPutValue
argument_list|(
name|termValue
argument_list|,
name|freq
argument_list|,
name|freq
argument_list|)
expr_stmt|;
block|}
block|}
name|numDocs
operator|+=
name|shardTermsResponse
operator|.
name|numDocs
argument_list|()
expr_stmt|;
name|maxDoc
operator|+=
name|shardTermsResponse
operator|.
name|maxDoc
argument_list|()
expr_stmt|;
name|numDeletedDocs
operator|+=
name|shardTermsResponse
operator|.
name|numDeletedDocs
argument_list|()
expr_stmt|;
name|successfulShards
operator|++
expr_stmt|;
block|}
block|}
name|Map
argument_list|<
name|String
argument_list|,
name|NavigableSet
argument_list|<
name|TermFreq
argument_list|>
argument_list|>
name|fieldTermsFreqs
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|NavigableSet
argument_list|<
name|TermFreq
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
if|if
condition|(
name|aggregator
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|TObjectIntHashMap
argument_list|<
name|Object
argument_list|>
argument_list|>
name|entry
range|:
name|aggregator
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|fieldName
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|NavigableSet
argument_list|<
name|TermFreq
argument_list|>
name|sortedFreqs
init|=
name|fieldTermsFreqs
operator|.
name|get
argument_list|(
name|fieldName
argument_list|)
decl_stmt|;
if|if
condition|(
name|sortedFreqs
operator|==
literal|null
condition|)
block|{
name|Comparator
argument_list|<
name|TermFreq
argument_list|>
name|comparator
init|=
name|request
operator|.
name|sortType
argument_list|()
operator|==
name|TermsRequest
operator|.
name|SortType
operator|.
name|FREQ
condition|?
name|TermFreq
operator|.
name|freqComparator
argument_list|()
else|:
name|TermFreq
operator|.
name|termComparator
argument_list|()
decl_stmt|;
name|sortedFreqs
operator|=
operator|new
name|BoundedTreeSet
argument_list|<
name|TermFreq
argument_list|>
argument_list|(
name|comparator
argument_list|,
name|request
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|fieldTermsFreqs
operator|.
name|put
argument_list|(
name|fieldName
argument_list|,
name|sortedFreqs
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|TObjectIntIterator
argument_list|<
name|Object
argument_list|>
name|it
init|=
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|iterator
argument_list|()
init|;
name|it
operator|.
name|hasNext
argument_list|()
condition|;
control|)
block|{
name|it
operator|.
name|advance
argument_list|()
expr_stmt|;
if|if
condition|(
name|it
operator|.
name|value
argument_list|()
operator|>=
name|request
operator|.
name|minFreq
argument_list|()
operator|&&
name|it
operator|.
name|value
argument_list|()
operator|<=
name|request
operator|.
name|maxFreq
argument_list|()
condition|)
block|{
name|sortedFreqs
operator|.
name|add
argument_list|(
operator|new
name|TermFreq
argument_list|(
name|it
operator|.
name|key
argument_list|()
argument_list|,
name|it
operator|.
name|value
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
name|FieldTermsFreq
index|[]
name|resultFreqs
init|=
operator|new
name|FieldTermsFreq
index|[
name|fieldTermsFreqs
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
name|int
name|index
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|NavigableSet
argument_list|<
name|TermFreq
argument_list|>
argument_list|>
name|entry
range|:
name|fieldTermsFreqs
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|TermFreq
index|[]
name|freqs
init|=
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|toArray
argument_list|(
operator|new
name|TermFreq
index|[
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|size
argument_list|()
index|]
argument_list|)
decl_stmt|;
name|resultFreqs
index|[
name|index
operator|++
index|]
operator|=
operator|new
name|FieldTermsFreq
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|freqs
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|TermsResponse
argument_list|(
name|shardsResponses
operator|.
name|length
argument_list|()
argument_list|,
name|successfulShards
argument_list|,
name|failedShards
argument_list|,
name|shardFailures
argument_list|,
name|resultFreqs
argument_list|,
name|numDocs
argument_list|,
name|maxDoc
argument_list|,
name|numDeletedDocs
argument_list|)
return|;
block|}
DECL|method|shardOperation
annotation|@
name|Override
specifier|protected
name|ShardTermsResponse
name|shardOperation
parameter_list|(
name|ShardTermsRequest
name|request
parameter_list|)
throws|throws
name|ElasticSearchException
block|{
name|IndexService
name|indexService
init|=
name|indicesService
operator|.
name|indexServiceSafe
argument_list|(
name|request
operator|.
name|index
argument_list|()
argument_list|)
decl_stmt|;
name|IndexShard
name|shard
init|=
name|indexService
operator|.
name|shard
argument_list|(
name|request
operator|.
name|shardId
argument_list|()
argument_list|)
decl_stmt|;
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
name|ShardTermsResponse
name|response
init|=
operator|new
name|ShardTermsResponse
argument_list|(
name|request
operator|.
name|index
argument_list|()
argument_list|,
name|request
operator|.
name|shardId
argument_list|()
argument_list|,
name|searcher
operator|.
name|reader
argument_list|()
operator|.
name|numDocs
argument_list|()
argument_list|,
name|searcher
operator|.
name|reader
argument_list|()
operator|.
name|maxDoc
argument_list|()
argument_list|,
name|searcher
operator|.
name|reader
argument_list|()
operator|.
name|numDeletedDocs
argument_list|()
argument_list|)
decl_stmt|;
name|TermDocs
name|termDocs
init|=
literal|null
decl_stmt|;
try|try
block|{
name|Pattern
name|regexpPattern
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|request
operator|.
name|regexp
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|regexpPattern
operator|=
name|Pattern
operator|.
name|compile
argument_list|(
name|request
operator|.
name|regexp
argument_list|()
argument_list|,
name|Pattern
operator|.
name|DOTALL
operator||
name|Pattern
operator|.
name|CASE_INSENSITIVE
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|String
name|fieldName
range|:
name|request
operator|.
name|fields
argument_list|()
control|)
block|{
name|FieldMapper
name|fieldMapper
init|=
name|indexService
operator|.
name|mapperService
argument_list|()
operator|.
name|smartNameFieldMapper
argument_list|(
name|fieldName
argument_list|)
decl_stmt|;
name|String
name|indexFieldName
init|=
name|fieldName
decl_stmt|;
if|if
condition|(
name|fieldMapper
operator|!=
literal|null
condition|)
block|{
name|indexFieldName
operator|=
name|fieldMapper
operator|.
name|names
argument_list|()
operator|.
name|indexName
argument_list|()
expr_stmt|;
block|}
name|indexFieldName
operator|=
name|StringHelper
operator|.
name|intern
argument_list|(
name|indexFieldName
argument_list|)
expr_stmt|;
comment|// if we are sorting by term, and the field mapper sorting type is STRING, then do plain term extraction (which is faster)
try|try
block|{
name|ExecuteTermResult
name|executeTermResult
decl_stmt|;
if|if
condition|(
name|request
operator|.
name|sortType
argument_list|()
operator|==
name|TermsRequest
operator|.
name|SortType
operator|.
name|TERM
operator|&&
name|fieldMapper
operator|!=
literal|null
operator|&&
operator|(
name|fieldMapper
operator|.
name|sortType
argument_list|()
operator|==
name|SortField
operator|.
name|STRING
operator|||
name|fieldMapper
operator|.
name|sortType
argument_list|()
operator|==
name|SortField
operator|.
name|STRING_VAL
operator|)
condition|)
block|{
name|executeTermResult
operator|=
name|executeTermSortedStringTerm
argument_list|(
name|request
argument_list|,
name|indexFieldName
argument_list|,
name|searcher
argument_list|,
name|regexpPattern
argument_list|,
name|fieldMapper
argument_list|,
name|termDocs
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|executeTermResult
operator|=
name|executeTerms
argument_list|(
name|request
argument_list|,
name|indexFieldName
argument_list|,
name|searcher
argument_list|,
name|regexpPattern
argument_list|,
name|fieldMapper
argument_list|,
name|termDocs
argument_list|)
expr_stmt|;
block|}
name|termDocs
operator|=
name|executeTermResult
operator|.
name|termDocs
expr_stmt|;
name|response
operator|.
name|put
argument_list|(
name|fieldName
argument_list|,
name|executeTermResult
operator|.
name|termsFreqs
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// currently, just log
name|logger
operator|.
name|warn
argument_list|(
literal|"Failed to fetch terms for field ["
operator|+
name|fieldName
operator|+
literal|"]"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|response
return|;
block|}
finally|finally
block|{
if|if
condition|(
name|termDocs
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|termDocs
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// ignore
block|}
block|}
name|searcher
operator|.
name|release
argument_list|()
expr_stmt|;
block|}
block|}
DECL|class|ExecuteTermResult
specifier|static
class|class
name|ExecuteTermResult
block|{
DECL|field|termsFreqs
specifier|public
name|TObjectIntHashMap
argument_list|<
name|Object
argument_list|>
name|termsFreqs
decl_stmt|;
DECL|field|termDocs
specifier|public
name|TermDocs
name|termDocs
decl_stmt|;
DECL|method|ExecuteTermResult
name|ExecuteTermResult
parameter_list|(
name|TObjectIntHashMap
argument_list|<
name|Object
argument_list|>
name|termsFreqs
parameter_list|,
name|TermDocs
name|termDocs
parameter_list|)
block|{
name|this
operator|.
name|termsFreqs
operator|=
name|termsFreqs
expr_stmt|;
name|this
operator|.
name|termDocs
operator|=
name|termDocs
expr_stmt|;
block|}
block|}
DECL|method|executeTerms
specifier|private
name|ExecuteTermResult
name|executeTerms
parameter_list|(
name|ShardTermsRequest
name|request
parameter_list|,
name|String
name|indexFieldName
parameter_list|,
name|Engine
operator|.
name|Searcher
name|searcher
parameter_list|,
annotation|@
name|Nullable
name|Pattern
name|regexpPattern
parameter_list|,
annotation|@
name|Nullable
name|FieldMapper
name|fieldMapper
parameter_list|,
annotation|@
name|Nullable
name|TermDocs
name|termDocs
parameter_list|)
throws|throws
name|IOException
block|{
name|TObjectIntHashMap
argument_list|<
name|Object
argument_list|>
name|termsFreqs
init|=
operator|new
name|TObjectIntHashMap
argument_list|<
name|Object
argument_list|>
argument_list|()
decl_stmt|;
name|String
name|sFrom
init|=
name|request
operator|.
name|from
argument_list|()
decl_stmt|;
if|if
condition|(
name|sFrom
operator|==
literal|null
condition|)
block|{
comment|// really, only make sense for strings
name|sFrom
operator|=
name|request
operator|.
name|prefix
argument_list|()
expr_stmt|;
block|}
name|Object
name|from
init|=
name|sFrom
decl_stmt|;
if|if
condition|(
name|from
operator|!=
literal|null
operator|&&
name|fieldMapper
operator|!=
literal|null
condition|)
block|{
name|from
operator|=
name|fieldMapper
operator|.
name|valueFromString
argument_list|(
name|sFrom
argument_list|)
expr_stmt|;
block|}
name|String
name|sTo
init|=
name|request
operator|.
name|to
argument_list|()
decl_stmt|;
name|Object
name|to
init|=
name|sTo
decl_stmt|;
if|if
condition|(
name|to
operator|!=
literal|null
operator|&&
name|fieldMapper
operator|!=
literal|null
condition|)
block|{
name|to
operator|=
name|fieldMapper
operator|.
name|valueFromString
argument_list|(
name|sTo
argument_list|)
expr_stmt|;
block|}
name|TermEnum
name|termEnum
init|=
literal|null
decl_stmt|;
name|Comparator
argument_list|<
name|TermFreq
argument_list|>
name|comparator
init|=
name|request
operator|.
name|sortType
argument_list|()
operator|==
name|TermsRequest
operator|.
name|SortType
operator|.
name|TERM
condition|?
name|TermFreq
operator|.
name|termComparator
argument_list|()
else|:
name|TermFreq
operator|.
name|freqComparator
argument_list|()
decl_stmt|;
name|BoundedTreeSet
argument_list|<
name|TermFreq
argument_list|>
name|sortedFreq
init|=
operator|new
name|BoundedTreeSet
argument_list|<
name|TermFreq
argument_list|>
argument_list|(
name|comparator
argument_list|,
name|request
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
name|termEnum
operator|=
name|searcher
operator|.
name|reader
argument_list|()
operator|.
name|terms
argument_list|(
operator|new
name|Term
argument_list|(
name|indexFieldName
argument_list|,
literal|""
argument_list|)
argument_list|)
expr_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
name|Term
name|term
init|=
name|termEnum
operator|.
name|term
argument_list|()
decl_stmt|;
comment|// have we reached the end?
if|if
condition|(
name|term
operator|==
literal|null
operator|||
name|indexFieldName
operator|!=
name|term
operator|.
name|field
argument_list|()
condition|)
block|{
comment|// StirngHelper.intern
break|break;
block|}
name|Object
name|termValue
init|=
name|term
operator|.
name|text
argument_list|()
decl_stmt|;
if|if
condition|(
name|fieldMapper
operator|!=
literal|null
condition|)
block|{
name|termValue
operator|=
name|fieldMapper
operator|.
name|valueFromTerm
argument_list|(
name|term
operator|.
name|text
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|fieldMapper
operator|.
name|shouldBreakTermEnumeration
argument_list|(
name|termValue
argument_list|)
condition|)
block|{
break|break;
block|}
if|if
condition|(
name|termValue
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
block|}
comment|// check on the from term
if|if
condition|(
name|from
operator|!=
literal|null
condition|)
block|{
name|int
name|fromCompareResult
init|=
operator|(
operator|(
name|Comparable
operator|)
name|termValue
operator|)
operator|.
name|compareTo
argument_list|(
name|from
argument_list|)
decl_stmt|;
if|if
condition|(
name|fromCompareResult
operator|<
literal|0
operator|||
operator|(
name|fromCompareResult
operator|==
literal|0
operator|&&
operator|!
name|request
operator|.
name|fromInclusive
argument_list|()
operator|)
condition|)
block|{
name|termEnum
operator|.
name|next
argument_list|()
expr_stmt|;
continue|continue;
block|}
block|}
comment|// does it match on the prefix?
if|if
condition|(
name|request
operator|.
name|prefix
argument_list|()
operator|!=
literal|null
operator|&&
operator|!
name|term
operator|.
name|text
argument_list|()
operator|.
name|startsWith
argument_list|(
name|request
operator|.
name|prefix
argument_list|()
argument_list|)
condition|)
block|{
break|break;
block|}
comment|// does it match on regexp?
if|if
condition|(
name|regexpPattern
operator|!=
literal|null
operator|&&
operator|!
name|regexpPattern
operator|.
name|matcher
argument_list|(
name|term
operator|.
name|text
argument_list|()
argument_list|)
operator|.
name|matches
argument_list|()
condition|)
block|{
name|termEnum
operator|.
name|next
argument_list|()
expr_stmt|;
continue|continue;
block|}
comment|// check on the to term
if|if
condition|(
name|to
operator|!=
literal|null
condition|)
block|{
name|int
name|toCompareResult
init|=
operator|(
operator|(
name|Comparable
operator|)
name|termValue
operator|)
operator|.
name|compareTo
argument_list|(
name|to
argument_list|)
decl_stmt|;
if|if
condition|(
name|toCompareResult
operator|>
literal|0
operator|||
operator|(
name|toCompareResult
operator|==
literal|0
operator|&&
operator|!
name|request
operator|.
name|toInclusive
argument_list|()
operator|)
condition|)
block|{
break|break;
block|}
block|}
name|int
name|docFreq
init|=
name|termEnum
operator|.
name|docFreq
argument_list|()
decl_stmt|;
if|if
condition|(
name|request
operator|.
name|exact
argument_list|()
condition|)
block|{
if|if
condition|(
name|termDocs
operator|==
literal|null
condition|)
block|{
name|termDocs
operator|=
name|searcher
operator|.
name|reader
argument_list|()
operator|.
name|termDocs
argument_list|()
expr_stmt|;
block|}
name|termDocs
operator|.
name|seek
argument_list|(
name|termEnum
argument_list|)
expr_stmt|;
name|docFreq
operator|=
literal|0
expr_stmt|;
while|while
condition|(
name|termDocs
operator|.
name|next
argument_list|()
condition|)
block|{
if|if
condition|(
operator|!
name|searcher
operator|.
name|reader
argument_list|()
operator|.
name|isDeleted
argument_list|(
name|termDocs
operator|.
name|doc
argument_list|()
argument_list|)
condition|)
block|{
name|docFreq
operator|++
expr_stmt|;
block|}
block|}
block|}
name|sortedFreq
operator|.
name|add
argument_list|(
operator|new
name|TermFreq
argument_list|(
name|termValue
argument_list|,
name|docFreq
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|termEnum
operator|.
name|next
argument_list|()
condition|)
block|{
break|break;
block|}
block|}
block|}
finally|finally
block|{
if|if
condition|(
name|termEnum
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|termEnum
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// ignore
block|}
block|}
block|}
for|for
control|(
name|TermFreq
name|termFreq
range|:
name|sortedFreq
control|)
block|{
name|termsFreqs
operator|.
name|put
argument_list|(
name|termFreq
operator|.
name|term
argument_list|()
argument_list|,
name|termFreq
operator|.
name|docFreq
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|ExecuteTermResult
argument_list|(
name|termsFreqs
argument_list|,
name|termDocs
argument_list|)
return|;
block|}
DECL|method|executeTermSortedStringTerm
specifier|private
name|ExecuteTermResult
name|executeTermSortedStringTerm
parameter_list|(
name|ShardTermsRequest
name|request
parameter_list|,
name|String
name|indexFieldName
parameter_list|,
name|Engine
operator|.
name|Searcher
name|searcher
parameter_list|,
annotation|@
name|Nullable
name|Pattern
name|regexpPattern
parameter_list|,
annotation|@
name|Nullable
name|FieldMapper
name|fieldMapper
parameter_list|,
annotation|@
name|Nullable
name|TermDocs
name|termDocs
parameter_list|)
throws|throws
name|IOException
block|{
name|TObjectIntHashMap
argument_list|<
name|Object
argument_list|>
name|termsFreqs
init|=
operator|new
name|TObjectIntHashMap
argument_list|<
name|Object
argument_list|>
argument_list|()
decl_stmt|;
name|String
name|from
init|=
name|request
operator|.
name|from
argument_list|()
decl_stmt|;
if|if
condition|(
name|from
operator|==
literal|null
condition|)
block|{
name|from
operator|=
name|request
operator|.
name|prefix
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|from
operator|==
literal|null
condition|)
block|{
name|from
operator|=
literal|""
expr_stmt|;
block|}
name|Term
name|fromTerm
init|=
operator|new
name|Term
argument_list|(
name|indexFieldName
argument_list|,
name|from
argument_list|)
decl_stmt|;
name|String
name|to
init|=
name|request
operator|.
name|to
argument_list|()
decl_stmt|;
if|if
condition|(
name|to
operator|!=
literal|null
operator|&&
name|fieldMapper
operator|!=
literal|null
condition|)
block|{
name|to
operator|=
name|fieldMapper
operator|.
name|indexedValue
argument_list|(
name|to
argument_list|)
expr_stmt|;
block|}
name|Term
name|toTerm
init|=
name|to
operator|==
literal|null
condition|?
literal|null
else|:
operator|new
name|Term
argument_list|(
name|indexFieldName
argument_list|,
name|to
argument_list|)
decl_stmt|;
name|TermEnum
name|termEnum
init|=
literal|null
decl_stmt|;
try|try
block|{
name|termEnum
operator|=
name|searcher
operator|.
name|reader
argument_list|()
operator|.
name|terms
argument_list|(
name|fromTerm
argument_list|)
expr_stmt|;
comment|// skip the first if we are not inclusive on from
if|if
condition|(
operator|!
name|request
operator|.
name|fromInclusive
argument_list|()
operator|&&
name|request
operator|.
name|from
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|Term
name|term
init|=
name|termEnum
operator|.
name|term
argument_list|()
decl_stmt|;
if|if
condition|(
name|term
operator|!=
literal|null
operator|&&
name|indexFieldName
operator|==
name|term
operator|.
name|field
argument_list|()
operator|&&
name|term
operator|.
name|text
argument_list|()
operator|.
name|equals
argument_list|(
name|request
operator|.
name|from
argument_list|()
argument_list|)
condition|)
block|{
name|termEnum
operator|.
name|next
argument_list|()
expr_stmt|;
block|}
block|}
if|if
condition|(
name|request
operator|.
name|sortType
argument_list|()
operator|==
name|TermsRequest
operator|.
name|SortType
operator|.
name|TERM
condition|)
block|{
name|int
name|counter
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|counter
operator|<
name|request
operator|.
name|size
argument_list|()
condition|)
block|{
name|Term
name|term
init|=
name|termEnum
operator|.
name|term
argument_list|()
decl_stmt|;
comment|// have we reached the end?
if|if
condition|(
name|term
operator|==
literal|null
operator|||
name|indexFieldName
operator|!=
name|term
operator|.
name|field
argument_list|()
condition|)
block|{
comment|// StirngHelper.intern
break|break;
block|}
comment|// convert to actual term text
if|if
condition|(
name|fieldMapper
operator|!=
literal|null
condition|)
block|{
comment|// valueAsString returns null indicating that this is not interesting
name|Object
name|termObj
init|=
name|fieldMapper
operator|.
name|valueFromTerm
argument_list|(
name|term
operator|.
name|text
argument_list|()
argument_list|)
decl_stmt|;
comment|// if we need to break on this term enumeration, bail
if|if
condition|(
name|fieldMapper
operator|.
name|shouldBreakTermEnumeration
argument_list|(
name|termObj
argument_list|)
condition|)
block|{
break|break;
block|}
if|if
condition|(
name|termObj
operator|==
literal|null
condition|)
block|{
name|termEnum
operator|.
name|next
argument_list|()
expr_stmt|;
continue|continue;
block|}
block|}
comment|// does it match on the prefix?
if|if
condition|(
name|request
operator|.
name|prefix
argument_list|()
operator|!=
literal|null
operator|&&
operator|!
name|term
operator|.
name|text
argument_list|()
operator|.
name|startsWith
argument_list|(
name|request
operator|.
name|prefix
argument_list|()
argument_list|)
condition|)
block|{
break|break;
block|}
comment|// does it match on regexp?
if|if
condition|(
name|regexpPattern
operator|!=
literal|null
operator|&&
operator|!
name|regexpPattern
operator|.
name|matcher
argument_list|(
name|term
operator|.
name|text
argument_list|()
argument_list|)
operator|.
name|matches
argument_list|()
condition|)
block|{
name|termEnum
operator|.
name|next
argument_list|()
expr_stmt|;
continue|continue;
block|}
comment|// check on the to term
if|if
condition|(
name|toTerm
operator|!=
literal|null
condition|)
block|{
name|int
name|toCompareResult
init|=
name|term
operator|.
name|compareTo
argument_list|(
name|toTerm
argument_list|)
decl_stmt|;
if|if
condition|(
name|toCompareResult
operator|>
literal|0
operator|||
operator|(
name|toCompareResult
operator|==
literal|0
operator|&&
operator|!
name|request
operator|.
name|toInclusive
argument_list|()
operator|)
condition|)
block|{
break|break;
block|}
block|}
name|int
name|docFreq
init|=
name|termEnum
operator|.
name|docFreq
argument_list|()
decl_stmt|;
if|if
condition|(
name|request
operator|.
name|exact
argument_list|()
condition|)
block|{
if|if
condition|(
name|termDocs
operator|==
literal|null
condition|)
block|{
name|termDocs
operator|=
name|searcher
operator|.
name|reader
argument_list|()
operator|.
name|termDocs
argument_list|()
expr_stmt|;
block|}
name|termDocs
operator|.
name|seek
argument_list|(
name|termEnum
argument_list|)
expr_stmt|;
name|docFreq
operator|=
literal|0
expr_stmt|;
while|while
condition|(
name|termDocs
operator|.
name|next
argument_list|()
condition|)
block|{
if|if
condition|(
operator|!
name|searcher
operator|.
name|reader
argument_list|()
operator|.
name|isDeleted
argument_list|(
name|termDocs
operator|.
name|doc
argument_list|()
argument_list|)
condition|)
block|{
name|docFreq
operator|++
expr_stmt|;
block|}
block|}
block|}
name|termsFreqs
operator|.
name|put
argument_list|(
name|term
operator|.
name|text
argument_list|()
argument_list|,
name|docFreq
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|termEnum
operator|.
name|next
argument_list|()
condition|)
block|{
break|break;
block|}
name|counter
operator|++
expr_stmt|;
block|}
block|}
block|}
finally|finally
block|{
if|if
condition|(
name|termEnum
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|termEnum
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// ignore
block|}
block|}
block|}
return|return
operator|new
name|ExecuteTermResult
argument_list|(
name|termsFreqs
argument_list|,
name|termDocs
argument_list|)
return|;
block|}
DECL|method|transportAction
annotation|@
name|Override
specifier|protected
name|String
name|transportAction
parameter_list|()
block|{
return|return
name|TransportActions
operator|.
name|TERMS
return|;
block|}
DECL|method|transportShardAction
annotation|@
name|Override
specifier|protected
name|String
name|transportShardAction
parameter_list|()
block|{
return|return
literal|"indices/terms/shard"
return|;
block|}
DECL|method|newRequest
annotation|@
name|Override
specifier|protected
name|TermsRequest
name|newRequest
parameter_list|()
block|{
return|return
operator|new
name|TermsRequest
argument_list|()
return|;
block|}
DECL|method|newShardRequest
annotation|@
name|Override
specifier|protected
name|ShardTermsRequest
name|newShardRequest
parameter_list|()
block|{
return|return
operator|new
name|ShardTermsRequest
argument_list|()
return|;
block|}
DECL|method|newShardRequest
annotation|@
name|Override
specifier|protected
name|ShardTermsRequest
name|newShardRequest
parameter_list|(
name|ShardRouting
name|shard
parameter_list|,
name|TermsRequest
name|request
parameter_list|)
block|{
return|return
operator|new
name|ShardTermsRequest
argument_list|(
name|shard
operator|.
name|index
argument_list|()
argument_list|,
name|shard
operator|.
name|id
argument_list|()
argument_list|,
name|request
argument_list|)
return|;
block|}
DECL|method|newShardResponse
annotation|@
name|Override
specifier|protected
name|ShardTermsResponse
name|newShardResponse
parameter_list|()
block|{
return|return
operator|new
name|ShardTermsResponse
argument_list|()
return|;
block|}
DECL|method|shards
annotation|@
name|Override
specifier|protected
name|GroupShardsIterator
name|shards
parameter_list|(
name|TermsRequest
name|request
parameter_list|,
name|ClusterState
name|clusterState
parameter_list|)
block|{
return|return
name|indicesService
operator|.
name|searchShards
argument_list|(
name|clusterState
argument_list|,
name|request
operator|.
name|indices
argument_list|()
argument_list|,
name|request
operator|.
name|queryHint
argument_list|()
argument_list|)
return|;
block|}
block|}
end_class

end_unit

