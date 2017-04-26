begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.search
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|search
package|;
end_package

begin_import
import|import
name|com
operator|.
name|carrotsearch
operator|.
name|hppc
operator|.
name|IntArrayList
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|logging
operator|.
name|log4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|logging
operator|.
name|log4j
operator|.
name|message
operator|.
name|ParameterizedMessage
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|logging
operator|.
name|log4j
operator|.
name|util
operator|.
name|Supplier
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
name|ScoreDoc
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
name|ActionRunnable
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
name|OriginalIndices
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
name|AtomicArray
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|SearchPhaseResult
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|SearchShardTarget
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|fetch
operator|.
name|FetchSearchResult
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|fetch
operator|.
name|ShardFetchSearchRequest
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|internal
operator|.
name|InternalSearchResponse
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|query
operator|.
name|QuerySearchResult
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
name|Transport
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
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|function
operator|.
name|Function
import|;
end_import

begin_comment
comment|/**  * This search phase merges the query results from the previous phase together and calculates the topN hits for this search.  * Then it reaches out to all relevant shards to fetch the topN hits.  */
end_comment

begin_class
DECL|class|FetchSearchPhase
specifier|final
class|class
name|FetchSearchPhase
extends|extends
name|SearchPhase
block|{
DECL|field|fetchResults
specifier|private
specifier|final
name|AtomicArray
argument_list|<
name|FetchSearchResult
argument_list|>
name|fetchResults
decl_stmt|;
DECL|field|searchPhaseController
specifier|private
specifier|final
name|SearchPhaseController
name|searchPhaseController
decl_stmt|;
DECL|field|queryResults
specifier|private
specifier|final
name|AtomicArray
argument_list|<
name|SearchPhaseResult
argument_list|>
name|queryResults
decl_stmt|;
DECL|field|nextPhaseFactory
specifier|private
specifier|final
name|Function
argument_list|<
name|SearchResponse
argument_list|,
name|SearchPhase
argument_list|>
name|nextPhaseFactory
decl_stmt|;
DECL|field|context
specifier|private
specifier|final
name|SearchPhaseContext
name|context
decl_stmt|;
DECL|field|logger
specifier|private
specifier|final
name|Logger
name|logger
decl_stmt|;
DECL|field|resultConsumer
specifier|private
specifier|final
name|InitialSearchPhase
operator|.
name|SearchPhaseResults
argument_list|<
name|SearchPhaseResult
argument_list|>
name|resultConsumer
decl_stmt|;
DECL|method|FetchSearchPhase
name|FetchSearchPhase
parameter_list|(
name|InitialSearchPhase
operator|.
name|SearchPhaseResults
argument_list|<
name|SearchPhaseResult
argument_list|>
name|resultConsumer
parameter_list|,
name|SearchPhaseController
name|searchPhaseController
parameter_list|,
name|SearchPhaseContext
name|context
parameter_list|)
block|{
name|this
argument_list|(
name|resultConsumer
argument_list|,
name|searchPhaseController
argument_list|,
name|context
argument_list|,
parameter_list|(
name|response
parameter_list|)
lambda|->
operator|new
name|ExpandSearchPhase
argument_list|(
name|context
argument_list|,
name|response
argument_list|,
comment|// collapse only happens if the request has inner hits
parameter_list|(
name|finalResponse
parameter_list|)
lambda|->
name|sendResponsePhase
argument_list|(
name|finalResponse
argument_list|,
name|context
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|FetchSearchPhase
name|FetchSearchPhase
parameter_list|(
name|InitialSearchPhase
operator|.
name|SearchPhaseResults
argument_list|<
name|SearchPhaseResult
argument_list|>
name|resultConsumer
parameter_list|,
name|SearchPhaseController
name|searchPhaseController
parameter_list|,
name|SearchPhaseContext
name|context
parameter_list|,
name|Function
argument_list|<
name|SearchResponse
argument_list|,
name|SearchPhase
argument_list|>
name|nextPhaseFactory
parameter_list|)
block|{
name|super
argument_list|(
literal|"fetch"
argument_list|)
expr_stmt|;
if|if
condition|(
name|context
operator|.
name|getNumShards
argument_list|()
operator|!=
name|resultConsumer
operator|.
name|getNumShards
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"number of shards must match the length of the query results but doesn't:"
operator|+
name|context
operator|.
name|getNumShards
argument_list|()
operator|+
literal|"!="
operator|+
name|resultConsumer
operator|.
name|getNumShards
argument_list|()
argument_list|)
throw|;
block|}
name|this
operator|.
name|fetchResults
operator|=
operator|new
name|AtomicArray
argument_list|<>
argument_list|(
name|resultConsumer
operator|.
name|getNumShards
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|searchPhaseController
operator|=
name|searchPhaseController
expr_stmt|;
name|this
operator|.
name|queryResults
operator|=
name|resultConsumer
operator|.
name|results
expr_stmt|;
name|this
operator|.
name|nextPhaseFactory
operator|=
name|nextPhaseFactory
expr_stmt|;
name|this
operator|.
name|context
operator|=
name|context
expr_stmt|;
name|this
operator|.
name|logger
operator|=
name|context
operator|.
name|getLogger
argument_list|()
expr_stmt|;
name|this
operator|.
name|resultConsumer
operator|=
name|resultConsumer
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|run
specifier|public
name|void
name|run
parameter_list|()
throws|throws
name|IOException
block|{
name|context
operator|.
name|execute
argument_list|(
operator|new
name|ActionRunnable
argument_list|<
name|SearchResponse
argument_list|>
argument_list|(
name|context
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|void
name|doRun
parameter_list|()
throws|throws
name|IOException
block|{
comment|// we do the heavy lifting in this inner run method where we reduce aggs etc. that's why we fork this phase
comment|// off immediately instead of forking when we send back the response to the user since there we only need
comment|// to merge together the fetched results which is a linear operation.
name|innerRun
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|onFailure
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|context
operator|.
name|onPhaseFailure
argument_list|(
name|FetchSearchPhase
operator|.
name|this
argument_list|,
literal|""
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
DECL|method|innerRun
specifier|private
name|void
name|innerRun
parameter_list|()
throws|throws
name|IOException
block|{
specifier|final
name|int
name|numShards
init|=
name|context
operator|.
name|getNumShards
argument_list|()
decl_stmt|;
specifier|final
name|boolean
name|isScrollSearch
init|=
name|context
operator|.
name|getRequest
argument_list|()
operator|.
name|scroll
argument_list|()
operator|!=
literal|null
decl_stmt|;
name|List
argument_list|<
name|SearchPhaseResult
argument_list|>
name|phaseResults
init|=
name|queryResults
operator|.
name|asList
argument_list|()
decl_stmt|;
name|String
name|scrollId
init|=
name|isScrollSearch
condition|?
name|TransportSearchHelper
operator|.
name|buildScrollId
argument_list|(
name|queryResults
argument_list|)
else|:
literal|null
decl_stmt|;
specifier|final
name|SearchPhaseController
operator|.
name|ReducedQueryPhase
name|reducedQueryPhase
init|=
name|resultConsumer
operator|.
name|reduce
argument_list|()
decl_stmt|;
specifier|final
name|boolean
name|queryAndFetchOptimization
init|=
name|queryResults
operator|.
name|length
argument_list|()
operator|==
literal|1
decl_stmt|;
specifier|final
name|Runnable
name|finishPhase
init|=
parameter_list|()
lambda|->
name|moveToNextPhase
argument_list|(
name|searchPhaseController
argument_list|,
name|scrollId
argument_list|,
name|reducedQueryPhase
argument_list|,
name|queryAndFetchOptimization
condition|?
name|queryResults
else|:
name|fetchResults
argument_list|)
decl_stmt|;
if|if
condition|(
name|queryAndFetchOptimization
condition|)
block|{
assert|assert
name|phaseResults
operator|.
name|isEmpty
argument_list|()
operator|||
name|phaseResults
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|fetchResult
argument_list|()
operator|!=
literal|null
assert|;
comment|// query AND fetch optimization
name|finishPhase
operator|.
name|run
argument_list|()
expr_stmt|;
block|}
else|else
block|{
specifier|final
name|IntArrayList
index|[]
name|docIdsToLoad
init|=
name|searchPhaseController
operator|.
name|fillDocIdsToLoad
argument_list|(
name|numShards
argument_list|,
name|reducedQueryPhase
operator|.
name|scoreDocs
argument_list|)
decl_stmt|;
if|if
condition|(
name|reducedQueryPhase
operator|.
name|scoreDocs
operator|.
name|length
operator|==
literal|0
condition|)
block|{
comment|// no docs to fetch -- sidestep everything and return
name|phaseResults
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|SearchPhaseResult
operator|::
name|queryResult
argument_list|)
operator|.
name|forEach
argument_list|(
name|this
operator|::
name|releaseIrrelevantSearchContext
argument_list|)
expr_stmt|;
comment|// we have to release contexts here to free up resources
name|finishPhase
operator|.
name|run
argument_list|()
expr_stmt|;
block|}
else|else
block|{
specifier|final
name|ScoreDoc
index|[]
name|lastEmittedDocPerShard
init|=
name|isScrollSearch
condition|?
name|searchPhaseController
operator|.
name|getLastEmittedDocPerShard
argument_list|(
name|reducedQueryPhase
argument_list|,
name|numShards
argument_list|)
else|:
literal|null
decl_stmt|;
specifier|final
name|CountedCollector
argument_list|<
name|FetchSearchResult
argument_list|>
name|counter
init|=
operator|new
name|CountedCollector
argument_list|<>
argument_list|(
name|r
lambda|->
name|fetchResults
operator|.
name|set
argument_list|(
name|r
operator|.
name|getShardIndex
argument_list|()
argument_list|,
name|r
argument_list|)
argument_list|,
name|docIdsToLoad
operator|.
name|length
argument_list|,
comment|// we count down every shard in the result no matter if we got any results or not
name|finishPhase
argument_list|,
name|context
argument_list|)
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
name|docIdsToLoad
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|IntArrayList
name|entry
init|=
name|docIdsToLoad
index|[
name|i
index|]
decl_stmt|;
name|SearchPhaseResult
name|queryResult
init|=
name|queryResults
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|entry
operator|==
literal|null
condition|)
block|{
comment|// no results for this shard ID
if|if
condition|(
name|queryResult
operator|!=
literal|null
condition|)
block|{
comment|// if we got some hits from this shard we have to release the context there
comment|// we do this as we go since it will free up resources and passing on the request on the
comment|// transport layer is cheap.
name|releaseIrrelevantSearchContext
argument_list|(
name|queryResult
operator|.
name|queryResult
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// in any case we count down this result since we don't talk to this shard anymore
name|counter
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|SearchShardTarget
name|searchShardTarget
init|=
name|queryResult
operator|.
name|getSearchShardTarget
argument_list|()
decl_stmt|;
name|Transport
operator|.
name|Connection
name|connection
init|=
name|context
operator|.
name|getConnection
argument_list|(
name|searchShardTarget
operator|.
name|getNodeId
argument_list|()
argument_list|)
decl_stmt|;
name|ShardFetchSearchRequest
name|fetchSearchRequest
init|=
name|createFetchRequest
argument_list|(
name|queryResult
operator|.
name|queryResult
argument_list|()
operator|.
name|getRequestId
argument_list|()
argument_list|,
name|i
argument_list|,
name|entry
argument_list|,
name|lastEmittedDocPerShard
argument_list|,
name|searchShardTarget
operator|.
name|getOriginalIndices
argument_list|()
argument_list|)
decl_stmt|;
name|executeFetch
argument_list|(
name|i
argument_list|,
name|searchShardTarget
argument_list|,
name|counter
argument_list|,
name|fetchSearchRequest
argument_list|,
name|queryResult
operator|.
name|queryResult
argument_list|()
argument_list|,
name|connection
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
DECL|method|createFetchRequest
specifier|protected
name|ShardFetchSearchRequest
name|createFetchRequest
parameter_list|(
name|long
name|queryId
parameter_list|,
name|int
name|index
parameter_list|,
name|IntArrayList
name|entry
parameter_list|,
name|ScoreDoc
index|[]
name|lastEmittedDocPerShard
parameter_list|,
name|OriginalIndices
name|originalIndices
parameter_list|)
block|{
specifier|final
name|ScoreDoc
name|lastEmittedDoc
init|=
operator|(
name|lastEmittedDocPerShard
operator|!=
literal|null
operator|)
condition|?
name|lastEmittedDocPerShard
index|[
name|index
index|]
else|:
literal|null
decl_stmt|;
return|return
operator|new
name|ShardFetchSearchRequest
argument_list|(
name|originalIndices
argument_list|,
name|queryId
argument_list|,
name|entry
argument_list|,
name|lastEmittedDoc
argument_list|)
return|;
block|}
DECL|method|executeFetch
specifier|private
name|void
name|executeFetch
parameter_list|(
specifier|final
name|int
name|shardIndex
parameter_list|,
specifier|final
name|SearchShardTarget
name|shardTarget
parameter_list|,
specifier|final
name|CountedCollector
argument_list|<
name|FetchSearchResult
argument_list|>
name|counter
parameter_list|,
specifier|final
name|ShardFetchSearchRequest
name|fetchSearchRequest
parameter_list|,
specifier|final
name|QuerySearchResult
name|querySearchResult
parameter_list|,
specifier|final
name|Transport
operator|.
name|Connection
name|connection
parameter_list|)
block|{
name|context
operator|.
name|getSearchTransport
argument_list|()
operator|.
name|sendExecuteFetch
argument_list|(
name|connection
argument_list|,
name|fetchSearchRequest
argument_list|,
name|context
operator|.
name|getTask
argument_list|()
argument_list|,
operator|new
name|SearchActionListener
argument_list|<
name|FetchSearchResult
argument_list|>
argument_list|(
name|shardTarget
argument_list|,
name|shardIndex
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|void
name|innerOnResponse
parameter_list|(
name|FetchSearchResult
name|result
parameter_list|)
block|{
name|counter
operator|.
name|onResult
argument_list|(
name|result
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|onFailure
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
try|try
block|{
if|if
condition|(
name|logger
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
call|(
name|Supplier
argument_list|<
name|?
argument_list|>
call|)
argument_list|()
operator|->
operator|new
name|ParameterizedMessage
argument_list|(
literal|"[{}] Failed to execute fetch phase"
argument_list|,
name|fetchSearchRequest
operator|.
name|id
argument_list|()
argument_list|)
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
name|counter
operator|.
name|onFailure
argument_list|(
name|shardIndex
argument_list|,
name|shardTarget
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
comment|// the search context might not be cleared on the node where the fetch was executed for example
comment|// because the action was rejected by the thread pool. in this case we need to send a dedicated
comment|// request to clear the search context.
name|releaseIrrelevantSearchContext
argument_list|(
name|querySearchResult
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
comment|/**      * Releases shard targets that are not used in the docsIdsToLoad.      */
DECL|method|releaseIrrelevantSearchContext
specifier|private
name|void
name|releaseIrrelevantSearchContext
parameter_list|(
name|QuerySearchResult
name|queryResult
parameter_list|)
block|{
comment|// we only release search context that we did not fetch from if we are not scrolling
comment|// and if it has at lease one hit that didn't make it to the global topDocs
if|if
condition|(
name|context
operator|.
name|getRequest
argument_list|()
operator|.
name|scroll
argument_list|()
operator|==
literal|null
operator|&&
name|queryResult
operator|.
name|hasSearchContext
argument_list|()
condition|)
block|{
try|try
block|{
name|SearchShardTarget
name|searchShardTarget
init|=
name|queryResult
operator|.
name|getSearchShardTarget
argument_list|()
decl_stmt|;
name|Transport
operator|.
name|Connection
name|connection
init|=
name|context
operator|.
name|getConnection
argument_list|(
name|searchShardTarget
operator|.
name|getNodeId
argument_list|()
argument_list|)
decl_stmt|;
name|context
operator|.
name|sendReleaseSearchContext
argument_list|(
name|queryResult
operator|.
name|getRequestId
argument_list|()
argument_list|,
name|connection
argument_list|,
name|searchShardTarget
operator|.
name|getOriginalIndices
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|context
operator|.
name|getLogger
argument_list|()
operator|.
name|trace
argument_list|(
literal|"failed to release context"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|moveToNextPhase
specifier|private
name|void
name|moveToNextPhase
parameter_list|(
name|SearchPhaseController
name|searchPhaseController
parameter_list|,
name|String
name|scrollId
parameter_list|,
name|SearchPhaseController
operator|.
name|ReducedQueryPhase
name|reducedQueryPhase
parameter_list|,
name|AtomicArray
argument_list|<
name|?
extends|extends
name|SearchPhaseResult
argument_list|>
name|fetchResultsArr
parameter_list|)
block|{
specifier|final
name|InternalSearchResponse
name|internalResponse
init|=
name|searchPhaseController
operator|.
name|merge
argument_list|(
name|context
operator|.
name|getRequest
argument_list|()
operator|.
name|scroll
argument_list|()
operator|!=
literal|null
argument_list|,
name|reducedQueryPhase
argument_list|,
name|fetchResultsArr
operator|.
name|asList
argument_list|()
argument_list|,
name|fetchResultsArr
operator|::
name|get
argument_list|)
decl_stmt|;
name|context
operator|.
name|executeNextPhase
argument_list|(
name|this
argument_list|,
name|nextPhaseFactory
operator|.
name|apply
argument_list|(
name|context
operator|.
name|buildSearchResponse
argument_list|(
name|internalResponse
argument_list|,
name|scrollId
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|sendResponsePhase
specifier|private
specifier|static
name|SearchPhase
name|sendResponsePhase
parameter_list|(
name|SearchResponse
name|response
parameter_list|,
name|SearchPhaseContext
name|context
parameter_list|)
block|{
return|return
operator|new
name|SearchPhase
argument_list|(
literal|"response"
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
throws|throws
name|IOException
block|{
name|context
operator|.
name|onResponse
argument_list|(
name|response
argument_list|)
expr_stmt|;
block|}
block|}
return|;
block|}
block|}
end_class

end_unit

