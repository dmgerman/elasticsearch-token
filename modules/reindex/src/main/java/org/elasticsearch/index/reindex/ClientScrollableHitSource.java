begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.reindex
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|reindex
package|;
end_package

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
name|elasticsearch
operator|.
name|ExceptionsHelper
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
name|ActionListener
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
name|bulk
operator|.
name|BackoffPolicy
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
name|search
operator|.
name|ClearScrollRequest
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
name|search
operator|.
name|ClearScrollResponse
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
name|search
operator|.
name|SearchRequest
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
name|search
operator|.
name|SearchResponse
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
name|search
operator|.
name|SearchScrollRequest
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
name|search
operator|.
name|ShardSearchFailure
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|client
operator|.
name|Client
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|client
operator|.
name|ParentTaskAssigningClient
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
name|unit
operator|.
name|TimeValue
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
name|AbstractRunnable
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
name|EsRejectedExecutionException
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
name|ParentFieldMapper
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
name|RoutingFieldMapper
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
name|TTLFieldMapper
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
name|TimestampFieldMapper
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
name|SearchHit
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
name|SearchHitField
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
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
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
name|Consumer
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|util
operator|.
name|Collections
operator|.
name|emptyList
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|util
operator|.
name|Collections
operator|.
name|unmodifiableList
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|unit
operator|.
name|TimeValue
operator|.
name|timeValueNanos
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|util
operator|.
name|CollectionUtils
operator|.
name|isEmpty
import|;
end_import

begin_comment
comment|/**  * A scrollable source of hits from a {@linkplain Client} instance.  */
end_comment

begin_class
DECL|class|ClientScrollableHitSource
specifier|public
class|class
name|ClientScrollableHitSource
extends|extends
name|ScrollableHitSource
block|{
DECL|field|client
specifier|private
specifier|final
name|ParentTaskAssigningClient
name|client
decl_stmt|;
DECL|field|firstSearchRequest
specifier|private
specifier|final
name|SearchRequest
name|firstSearchRequest
decl_stmt|;
DECL|method|ClientScrollableHitSource
specifier|public
name|ClientScrollableHitSource
parameter_list|(
name|Logger
name|logger
parameter_list|,
name|BackoffPolicy
name|backoffPolicy
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|,
name|Runnable
name|countSearchRetry
parameter_list|,
name|Consumer
argument_list|<
name|Exception
argument_list|>
name|fail
parameter_list|,
name|ParentTaskAssigningClient
name|client
parameter_list|,
name|SearchRequest
name|firstSearchRequest
parameter_list|)
block|{
name|super
argument_list|(
name|logger
argument_list|,
name|backoffPolicy
argument_list|,
name|threadPool
argument_list|,
name|countSearchRetry
argument_list|,
name|fail
argument_list|)
expr_stmt|;
name|this
operator|.
name|client
operator|=
name|client
expr_stmt|;
name|this
operator|.
name|firstSearchRequest
operator|=
name|firstSearchRequest
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doStart
specifier|public
name|void
name|doStart
parameter_list|(
name|Consumer
argument_list|<
name|?
super|super
name|Response
argument_list|>
name|onResponse
parameter_list|)
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
literal|"executing initial scroll against {}{}"
argument_list|,
name|isEmpty
argument_list|(
name|firstSearchRequest
operator|.
name|indices
argument_list|()
argument_list|)
condition|?
literal|"all indices"
else|:
name|firstSearchRequest
operator|.
name|indices
argument_list|()
argument_list|,
name|isEmpty
argument_list|(
name|firstSearchRequest
operator|.
name|types
argument_list|()
argument_list|)
condition|?
literal|""
else|:
name|firstSearchRequest
operator|.
name|types
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|searchWithRetry
argument_list|(
name|listener
lambda|->
name|client
operator|.
name|search
argument_list|(
name|firstSearchRequest
argument_list|,
name|listener
argument_list|)
argument_list|,
name|r
lambda|->
name|consume
argument_list|(
name|r
argument_list|,
name|onResponse
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doStartNextScroll
specifier|protected
name|void
name|doStartNextScroll
parameter_list|(
name|String
name|scrollId
parameter_list|,
name|TimeValue
name|extraKeepAlive
parameter_list|,
name|Consumer
argument_list|<
name|?
super|super
name|Response
argument_list|>
name|onResponse
parameter_list|)
block|{
name|SearchScrollRequest
name|request
init|=
operator|new
name|SearchScrollRequest
argument_list|()
decl_stmt|;
comment|// Add the wait time into the scroll timeout so it won't timeout while we wait for throttling
name|request
operator|.
name|scrollId
argument_list|(
name|scrollId
argument_list|)
operator|.
name|scroll
argument_list|(
name|timeValueNanos
argument_list|(
name|firstSearchRequest
operator|.
name|scroll
argument_list|()
operator|.
name|keepAlive
argument_list|()
operator|.
name|nanos
argument_list|()
operator|+
name|extraKeepAlive
operator|.
name|nanos
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|searchWithRetry
argument_list|(
name|listener
lambda|->
name|client
operator|.
name|searchScroll
argument_list|(
name|request
argument_list|,
name|listener
argument_list|)
argument_list|,
name|r
lambda|->
name|consume
argument_list|(
name|r
argument_list|,
name|onResponse
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|clearScroll
specifier|public
name|void
name|clearScroll
parameter_list|(
name|String
name|scrollId
parameter_list|)
block|{
comment|/*          * Fire off the clear scroll but don't wait for it it return before          * we send the use their response.          */
name|ClearScrollRequest
name|clearScrollRequest
init|=
operator|new
name|ClearScrollRequest
argument_list|()
decl_stmt|;
name|clearScrollRequest
operator|.
name|addScrollId
argument_list|(
name|scrollId
argument_list|)
expr_stmt|;
comment|/*          * Unwrap the client so we don't set our task as the parent. If we *did* set our ID then the clear scroll would be cancelled as          * if this task is cancelled. But we want to clear the scroll regardless of whether or not the main request was cancelled.          */
name|client
operator|.
name|unwrap
argument_list|()
operator|.
name|clearScroll
argument_list|(
name|clearScrollRequest
argument_list|,
operator|new
name|ActionListener
argument_list|<
name|ClearScrollResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onResponse
parameter_list|(
name|ClearScrollResponse
name|response
parameter_list|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"Freed [{}] contexts"
argument_list|,
name|response
operator|.
name|getNumFreed
argument_list|()
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
name|logger
operator|.
name|warn
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
literal|"Failed to clear scroll [{}]"
argument_list|,
name|scrollId
argument_list|)
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
comment|/**      * Run a search action and call onResponse when a the response comes in, retrying if the action fails with an exception caused by      * rejected execution.      *      * @param action consumes a listener and starts the action. The listener it consumes is rigged to retry on failure.      * @param onResponse consumes the response from the action      */
DECL|method|searchWithRetry
specifier|private
name|void
name|searchWithRetry
parameter_list|(
name|Consumer
argument_list|<
name|ActionListener
argument_list|<
name|SearchResponse
argument_list|>
argument_list|>
name|action
parameter_list|,
name|Consumer
argument_list|<
name|SearchResponse
argument_list|>
name|onResponse
parameter_list|)
block|{
comment|/*          * RetryHelper is both an AbstractRunnable and an ActionListener<SearchResponse> - meaning that it both starts the search and          * handles reacts to the results. The complexity is all in onFailure which either adapts the failure to the "fail" listener or          * retries the search. Since both AbstractRunnable and ActionListener define the onFailure method it is called for either failure          * to run the action (either while running or before starting) and for failure on the response from the action.          */
class|class
name|RetryHelper
extends|extends
name|AbstractRunnable
implements|implements
name|ActionListener
argument_list|<
name|SearchResponse
argument_list|>
block|{
specifier|private
specifier|final
name|Iterator
argument_list|<
name|TimeValue
argument_list|>
name|retries
init|=
name|backoffPolicy
operator|.
name|iterator
argument_list|()
decl_stmt|;
specifier|private
specifier|volatile
name|int
name|retryCount
init|=
literal|0
decl_stmt|;
annotation|@
name|Override
specifier|protected
name|void
name|doRun
parameter_list|()
throws|throws
name|Exception
block|{
name|action
operator|.
name|accept
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|onResponse
parameter_list|(
name|SearchResponse
name|response
parameter_list|)
block|{
name|onResponse
operator|.
name|accept
argument_list|(
name|response
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
if|if
condition|(
name|ExceptionsHelper
operator|.
name|unwrap
argument_list|(
name|e
argument_list|,
name|EsRejectedExecutionException
operator|.
name|class
argument_list|)
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|retries
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|retryCount
operator|+=
literal|1
expr_stmt|;
name|TimeValue
name|delay
init|=
name|retries
operator|.
name|next
argument_list|()
decl_stmt|;
name|logger
operator|.
name|trace
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
literal|"retrying rejected search after [{}]"
argument_list|,
name|delay
argument_list|)
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|countSearchRetry
operator|.
name|run
argument_list|()
expr_stmt|;
name|threadPool
operator|.
name|schedule
argument_list|(
name|delay
argument_list|,
name|ThreadPool
operator|.
name|Names
operator|.
name|SAME
argument_list|,
name|this
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|logger
operator|.
name|warn
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
literal|"giving up on search because we retried [{}] times without success"
argument_list|,
name|retryCount
argument_list|)
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|fail
operator|.
name|accept
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"giving up on search because it failed with a non-retryable exception"
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|fail
operator|.
name|accept
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
operator|new
name|RetryHelper
argument_list|()
operator|.
name|run
argument_list|()
expr_stmt|;
block|}
DECL|method|consume
specifier|private
name|void
name|consume
parameter_list|(
name|SearchResponse
name|response
parameter_list|,
name|Consumer
argument_list|<
name|?
super|super
name|Response
argument_list|>
name|onResponse
parameter_list|)
block|{
name|onResponse
operator|.
name|accept
argument_list|(
name|wrap
argument_list|(
name|response
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|wrap
specifier|private
name|Response
name|wrap
parameter_list|(
name|SearchResponse
name|response
parameter_list|)
block|{
name|List
argument_list|<
name|SearchFailure
argument_list|>
name|failures
decl_stmt|;
if|if
condition|(
name|response
operator|.
name|getShardFailures
argument_list|()
operator|==
literal|null
condition|)
block|{
name|failures
operator|=
name|emptyList
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|failures
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|response
operator|.
name|getShardFailures
argument_list|()
operator|.
name|length
argument_list|)
expr_stmt|;
for|for
control|(
name|ShardSearchFailure
name|failure
range|:
name|response
operator|.
name|getShardFailures
argument_list|()
control|)
block|{
name|String
name|nodeId
init|=
name|failure
operator|.
name|shard
argument_list|()
operator|==
literal|null
condition|?
literal|null
else|:
name|failure
operator|.
name|shard
argument_list|()
operator|.
name|nodeId
argument_list|()
decl_stmt|;
name|failures
operator|.
name|add
argument_list|(
operator|new
name|SearchFailure
argument_list|(
name|failure
operator|.
name|getCause
argument_list|()
argument_list|,
name|failure
operator|.
name|index
argument_list|()
argument_list|,
name|failure
operator|.
name|shardId
argument_list|()
argument_list|,
name|nodeId
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|List
argument_list|<
name|Hit
argument_list|>
name|hits
decl_stmt|;
if|if
condition|(
name|response
operator|.
name|getHits
argument_list|()
operator|.
name|getHits
argument_list|()
operator|==
literal|null
operator|||
name|response
operator|.
name|getHits
argument_list|()
operator|.
name|getHits
argument_list|()
operator|.
name|length
operator|==
literal|0
condition|)
block|{
name|hits
operator|=
name|emptyList
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|hits
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|response
operator|.
name|getHits
argument_list|()
operator|.
name|getHits
argument_list|()
operator|.
name|length
argument_list|)
expr_stmt|;
for|for
control|(
name|SearchHit
name|hit
range|:
name|response
operator|.
name|getHits
argument_list|()
operator|.
name|getHits
argument_list|()
control|)
block|{
name|hits
operator|.
name|add
argument_list|(
operator|new
name|ClientHit
argument_list|(
name|hit
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|hits
operator|=
name|unmodifiableList
argument_list|(
name|hits
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|Response
argument_list|(
name|response
operator|.
name|isTimedOut
argument_list|()
argument_list|,
name|failures
argument_list|,
name|response
operator|.
name|getHits
argument_list|()
operator|.
name|getTotalHits
argument_list|()
argument_list|,
name|hits
argument_list|,
name|response
operator|.
name|getScrollId
argument_list|()
argument_list|)
return|;
block|}
DECL|class|ClientHit
specifier|private
specifier|static
class|class
name|ClientHit
implements|implements
name|Hit
block|{
DECL|field|delegate
specifier|private
specifier|final
name|SearchHit
name|delegate
decl_stmt|;
DECL|field|source
specifier|private
specifier|final
name|BytesReference
name|source
decl_stmt|;
DECL|method|ClientHit
name|ClientHit
parameter_list|(
name|SearchHit
name|delegate
parameter_list|)
block|{
name|this
operator|.
name|delegate
operator|=
name|delegate
expr_stmt|;
name|source
operator|=
name|delegate
operator|.
name|hasSource
argument_list|()
condition|?
name|delegate
operator|.
name|getSourceRef
argument_list|()
else|:
literal|null
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getIndex
specifier|public
name|String
name|getIndex
parameter_list|()
block|{
return|return
name|delegate
operator|.
name|getIndex
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|getType
specifier|public
name|String
name|getType
parameter_list|()
block|{
return|return
name|delegate
operator|.
name|getType
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|getId
specifier|public
name|String
name|getId
parameter_list|()
block|{
return|return
name|delegate
operator|.
name|getId
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|getSource
specifier|public
name|BytesReference
name|getSource
parameter_list|()
block|{
return|return
name|source
return|;
block|}
annotation|@
name|Override
DECL|method|getVersion
specifier|public
name|long
name|getVersion
parameter_list|()
block|{
return|return
name|delegate
operator|.
name|getVersion
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|getParent
specifier|public
name|String
name|getParent
parameter_list|()
block|{
return|return
name|fieldValue
argument_list|(
name|ParentFieldMapper
operator|.
name|NAME
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|getRouting
specifier|public
name|String
name|getRouting
parameter_list|()
block|{
return|return
name|fieldValue
argument_list|(
name|RoutingFieldMapper
operator|.
name|NAME
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|getTimestamp
specifier|public
name|Long
name|getTimestamp
parameter_list|()
block|{
return|return
name|fieldValue
argument_list|(
name|TimestampFieldMapper
operator|.
name|NAME
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|getTTL
specifier|public
name|Long
name|getTTL
parameter_list|()
block|{
return|return
name|fieldValue
argument_list|(
name|TTLFieldMapper
operator|.
name|NAME
argument_list|)
return|;
block|}
DECL|method|fieldValue
specifier|private
parameter_list|<
name|T
parameter_list|>
name|T
name|fieldValue
parameter_list|(
name|String
name|fieldName
parameter_list|)
block|{
name|SearchHitField
name|field
init|=
name|delegate
operator|.
name|field
argument_list|(
name|fieldName
argument_list|)
decl_stmt|;
return|return
name|field
operator|==
literal|null
condition|?
literal|null
else|:
name|field
operator|.
name|value
argument_list|()
return|;
block|}
block|}
block|}
end_class

end_unit

