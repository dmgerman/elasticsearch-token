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
name|ShardSearchFailure
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
name|Nullable
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
name|io
operator|.
name|stream
operator|.
name|StreamInput
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
name|io
operator|.
name|stream
operator|.
name|StreamOutput
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
name|io
operator|.
name|stream
operator|.
name|Writeable
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
name|xcontent
operator|.
name|ToXContent
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
name|index
operator|.
name|reindex
operator|.
name|remote
operator|.
name|RemoteScrollableHitSource
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
name|threadpool
operator|.
name|ThreadPool
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
name|List
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
name|AtomicReference
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
name|Objects
operator|.
name|requireNonNull
import|;
end_import

begin_comment
comment|/**  * A scrollable source of results.  */
end_comment

begin_class
DECL|class|ScrollableHitSource
specifier|public
specifier|abstract
class|class
name|ScrollableHitSource
implements|implements
name|Closeable
block|{
DECL|field|scrollId
specifier|private
specifier|final
name|AtomicReference
argument_list|<
name|String
argument_list|>
name|scrollId
init|=
operator|new
name|AtomicReference
argument_list|<>
argument_list|()
decl_stmt|;
DECL|field|logger
specifier|protected
specifier|final
name|Logger
name|logger
decl_stmt|;
DECL|field|backoffPolicy
specifier|protected
specifier|final
name|BackoffPolicy
name|backoffPolicy
decl_stmt|;
DECL|field|threadPool
specifier|protected
specifier|final
name|ThreadPool
name|threadPool
decl_stmt|;
DECL|field|countSearchRetry
specifier|protected
specifier|final
name|Runnable
name|countSearchRetry
decl_stmt|;
DECL|field|fail
specifier|protected
specifier|final
name|Consumer
argument_list|<
name|Exception
argument_list|>
name|fail
decl_stmt|;
DECL|method|ScrollableHitSource
specifier|public
name|ScrollableHitSource
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
parameter_list|)
block|{
name|this
operator|.
name|logger
operator|=
name|logger
expr_stmt|;
name|this
operator|.
name|backoffPolicy
operator|=
name|backoffPolicy
expr_stmt|;
name|this
operator|.
name|threadPool
operator|=
name|threadPool
expr_stmt|;
name|this
operator|.
name|countSearchRetry
operator|=
name|countSearchRetry
expr_stmt|;
name|this
operator|.
name|fail
operator|=
name|fail
expr_stmt|;
block|}
DECL|method|start
specifier|public
specifier|final
name|void
name|start
parameter_list|(
name|Consumer
argument_list|<
name|Response
argument_list|>
name|onResponse
parameter_list|)
block|{
name|doStart
argument_list|(
name|response
lambda|->
block|{
name|setScroll
argument_list|(
name|response
operator|.
name|getScrollId
argument_list|()
argument_list|)
expr_stmt|;
name|logger
operator|.
name|debug
argument_list|(
literal|"scroll returned [{}] documents with a scroll id of [{}]"
argument_list|,
name|response
operator|.
name|getHits
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|response
operator|.
name|getScrollId
argument_list|()
argument_list|)
expr_stmt|;
name|onResponse
operator|.
name|accept
argument_list|(
name|response
argument_list|)
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
block|}
DECL|method|doStart
specifier|protected
specifier|abstract
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
function_decl|;
DECL|method|startNextScroll
specifier|public
specifier|final
name|void
name|startNextScroll
parameter_list|(
name|TimeValue
name|extraKeepAlive
parameter_list|,
name|Consumer
argument_list|<
name|Response
argument_list|>
name|onResponse
parameter_list|)
block|{
name|doStartNextScroll
argument_list|(
name|scrollId
operator|.
name|get
argument_list|()
argument_list|,
name|extraKeepAlive
argument_list|,
name|response
lambda|->
block|{
name|setScroll
argument_list|(
name|response
operator|.
name|getScrollId
argument_list|()
argument_list|)
expr_stmt|;
name|onResponse
operator|.
name|accept
argument_list|(
name|response
argument_list|)
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
block|}
DECL|method|doStartNextScroll
specifier|protected
specifier|abstract
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
function_decl|;
annotation|@
name|Override
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
block|{
name|String
name|scrollId
init|=
name|this
operator|.
name|scrollId
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|Strings
operator|.
name|hasLength
argument_list|(
name|scrollId
argument_list|)
condition|)
block|{
name|clearScroll
argument_list|(
name|scrollId
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|clearScroll
specifier|protected
specifier|abstract
name|void
name|clearScroll
parameter_list|(
name|String
name|scrollId
parameter_list|)
function_decl|;
comment|/**      * Set the id of the last scroll. Used for debugging.      */
DECL|method|setScroll
specifier|final
name|void
name|setScroll
parameter_list|(
name|String
name|scrollId
parameter_list|)
block|{
name|this
operator|.
name|scrollId
operator|.
name|set
argument_list|(
name|scrollId
argument_list|)
expr_stmt|;
block|}
comment|/**      * Response from each scroll batch.      */
DECL|class|Response
specifier|public
specifier|static
class|class
name|Response
block|{
DECL|field|timedOut
specifier|private
specifier|final
name|boolean
name|timedOut
decl_stmt|;
DECL|field|failures
specifier|private
specifier|final
name|List
argument_list|<
name|SearchFailure
argument_list|>
name|failures
decl_stmt|;
DECL|field|totalHits
specifier|private
specifier|final
name|long
name|totalHits
decl_stmt|;
DECL|field|hits
specifier|private
specifier|final
name|List
argument_list|<
name|?
extends|extends
name|Hit
argument_list|>
name|hits
decl_stmt|;
DECL|field|scrollId
specifier|private
specifier|final
name|String
name|scrollId
decl_stmt|;
DECL|method|Response
specifier|public
name|Response
parameter_list|(
name|boolean
name|timedOut
parameter_list|,
name|List
argument_list|<
name|SearchFailure
argument_list|>
name|failures
parameter_list|,
name|long
name|totalHits
parameter_list|,
name|List
argument_list|<
name|?
extends|extends
name|Hit
argument_list|>
name|hits
parameter_list|,
name|String
name|scrollId
parameter_list|)
block|{
name|this
operator|.
name|timedOut
operator|=
name|timedOut
expr_stmt|;
name|this
operator|.
name|failures
operator|=
name|failures
expr_stmt|;
name|this
operator|.
name|totalHits
operator|=
name|totalHits
expr_stmt|;
name|this
operator|.
name|hits
operator|=
name|hits
expr_stmt|;
name|this
operator|.
name|scrollId
operator|=
name|scrollId
expr_stmt|;
block|}
comment|/**          * Did this batch time out?          */
DECL|method|isTimedOut
specifier|public
name|boolean
name|isTimedOut
parameter_list|()
block|{
return|return
name|timedOut
return|;
block|}
comment|/**          * Where there any search failures?          */
DECL|method|getFailures
specifier|public
specifier|final
name|List
argument_list|<
name|SearchFailure
argument_list|>
name|getFailures
parameter_list|()
block|{
return|return
name|failures
return|;
block|}
comment|/**          * What were the total number of documents matching the search?          */
DECL|method|getTotalHits
specifier|public
name|long
name|getTotalHits
parameter_list|()
block|{
return|return
name|totalHits
return|;
block|}
comment|/**          * The documents returned in this batch.          */
DECL|method|getHits
specifier|public
name|List
argument_list|<
name|?
extends|extends
name|Hit
argument_list|>
name|getHits
parameter_list|()
block|{
return|return
name|hits
return|;
block|}
comment|/**          * The scroll id used to fetch the next set of documents.          */
DECL|method|getScrollId
specifier|public
name|String
name|getScrollId
parameter_list|()
block|{
return|return
name|scrollId
return|;
block|}
block|}
comment|/**      * A document returned as part of the response. Think of it like {@link SearchHit} but with all the things reindex needs in convenient      * methods.      */
DECL|interface|Hit
specifier|public
interface|interface
name|Hit
block|{
comment|/**          * The index in which the hit is stored.          */
DECL|method|getIndex
name|String
name|getIndex
parameter_list|()
function_decl|;
comment|/**          * The type that the hit has.          */
DECL|method|getType
name|String
name|getType
parameter_list|()
function_decl|;
comment|/**          * The document id of the hit.          */
DECL|method|getId
name|String
name|getId
parameter_list|()
function_decl|;
comment|/**          * The version of the match or {@code -1} if the version wasn't requested. The {@code -1} keeps it inline with Elasticsearch's          * internal APIs.          */
DECL|method|getVersion
name|long
name|getVersion
parameter_list|()
function_decl|;
comment|/**          * The source of the hit. Returns null if the source didn't come back from the search, usually because it source wasn't stored at          * all.          */
DECL|method|getSource
annotation|@
name|Nullable
name|BytesReference
name|getSource
parameter_list|()
function_decl|;
comment|/**          * The document id of the parent of the hit if there is a parent or null if there isn't.          */
DECL|method|getParent
annotation|@
name|Nullable
name|String
name|getParent
parameter_list|()
function_decl|;
comment|/**          * The routing on the hit if there is any or null if there isn't.          */
DECL|method|getRouting
annotation|@
name|Nullable
name|String
name|getRouting
parameter_list|()
function_decl|;
block|}
comment|/**      * An implementation of {@linkplain Hit} that uses getters and setters. Primarily used for testing and {@link RemoteScrollableHitSource}      * .      */
DECL|class|BasicHit
specifier|public
specifier|static
class|class
name|BasicHit
implements|implements
name|Hit
block|{
DECL|field|index
specifier|private
specifier|final
name|String
name|index
decl_stmt|;
DECL|field|type
specifier|private
specifier|final
name|String
name|type
decl_stmt|;
DECL|field|id
specifier|private
specifier|final
name|String
name|id
decl_stmt|;
DECL|field|version
specifier|private
specifier|final
name|long
name|version
decl_stmt|;
DECL|field|source
specifier|private
name|BytesReference
name|source
decl_stmt|;
DECL|field|parent
specifier|private
name|String
name|parent
decl_stmt|;
DECL|field|routing
specifier|private
name|String
name|routing
decl_stmt|;
DECL|method|BasicHit
specifier|public
name|BasicHit
parameter_list|(
name|String
name|index
parameter_list|,
name|String
name|type
parameter_list|,
name|String
name|id
parameter_list|,
name|long
name|version
parameter_list|)
block|{
name|this
operator|.
name|index
operator|=
name|index
expr_stmt|;
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
name|this
operator|.
name|id
operator|=
name|id
expr_stmt|;
name|this
operator|.
name|version
operator|=
name|version
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
name|index
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
name|type
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
name|id
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
name|version
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
DECL|method|setSource
specifier|public
name|BasicHit
name|setSource
parameter_list|(
name|BytesReference
name|source
parameter_list|)
block|{
name|this
operator|.
name|source
operator|=
name|source
expr_stmt|;
return|return
name|this
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
name|parent
return|;
block|}
DECL|method|setParent
specifier|public
name|BasicHit
name|setParent
parameter_list|(
name|String
name|parent
parameter_list|)
block|{
name|this
operator|.
name|parent
operator|=
name|parent
expr_stmt|;
return|return
name|this
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
name|routing
return|;
block|}
DECL|method|setRouting
specifier|public
name|BasicHit
name|setRouting
parameter_list|(
name|String
name|routing
parameter_list|)
block|{
name|this
operator|.
name|routing
operator|=
name|routing
expr_stmt|;
return|return
name|this
return|;
block|}
block|}
comment|/**      * A failure during search. Like {@link ShardSearchFailure} but useful for reindex from remote as well.      */
DECL|class|SearchFailure
specifier|public
specifier|static
class|class
name|SearchFailure
implements|implements
name|Writeable
implements|,
name|ToXContent
block|{
DECL|field|reason
specifier|private
specifier|final
name|Throwable
name|reason
decl_stmt|;
annotation|@
name|Nullable
DECL|field|index
specifier|private
specifier|final
name|String
name|index
decl_stmt|;
annotation|@
name|Nullable
DECL|field|shardId
specifier|private
specifier|final
name|Integer
name|shardId
decl_stmt|;
annotation|@
name|Nullable
DECL|field|nodeId
specifier|private
specifier|final
name|String
name|nodeId
decl_stmt|;
DECL|method|SearchFailure
specifier|public
name|SearchFailure
parameter_list|(
name|Throwable
name|reason
parameter_list|,
annotation|@
name|Nullable
name|String
name|index
parameter_list|,
annotation|@
name|Nullable
name|Integer
name|shardId
parameter_list|,
annotation|@
name|Nullable
name|String
name|nodeId
parameter_list|)
block|{
name|this
operator|.
name|index
operator|=
name|index
expr_stmt|;
name|this
operator|.
name|shardId
operator|=
name|shardId
expr_stmt|;
name|this
operator|.
name|reason
operator|=
name|requireNonNull
argument_list|(
name|reason
argument_list|,
literal|"reason cannot be null"
argument_list|)
expr_stmt|;
name|this
operator|.
name|nodeId
operator|=
name|nodeId
expr_stmt|;
block|}
comment|/**          * Build a search failure that doesn't have shard information available.          */
DECL|method|SearchFailure
specifier|public
name|SearchFailure
parameter_list|(
name|Throwable
name|reason
parameter_list|)
block|{
name|this
argument_list|(
name|reason
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**          * Read from a stream.          */
DECL|method|SearchFailure
specifier|public
name|SearchFailure
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|reason
operator|=
name|in
operator|.
name|readException
argument_list|()
expr_stmt|;
name|index
operator|=
name|in
operator|.
name|readOptionalString
argument_list|()
expr_stmt|;
name|shardId
operator|=
name|in
operator|.
name|readOptionalVInt
argument_list|()
expr_stmt|;
name|nodeId
operator|=
name|in
operator|.
name|readOptionalString
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|writeTo
specifier|public
name|void
name|writeTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeException
argument_list|(
name|reason
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeOptionalString
argument_list|(
name|index
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeOptionalVInt
argument_list|(
name|shardId
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeOptionalString
argument_list|(
name|nodeId
argument_list|)
expr_stmt|;
block|}
DECL|method|getIndex
specifier|public
name|String
name|getIndex
parameter_list|()
block|{
return|return
name|index
return|;
block|}
DECL|method|getShardId
specifier|public
name|Integer
name|getShardId
parameter_list|()
block|{
return|return
name|shardId
return|;
block|}
DECL|method|getReason
specifier|public
name|Throwable
name|getReason
parameter_list|()
block|{
return|return
name|reason
return|;
block|}
annotation|@
name|Nullable
DECL|method|getNodeId
specifier|public
name|String
name|getNodeId
parameter_list|()
block|{
return|return
name|nodeId
return|;
block|}
annotation|@
name|Override
DECL|method|toXContent
specifier|public
name|XContentBuilder
name|toXContent
parameter_list|(
name|XContentBuilder
name|builder
parameter_list|,
name|Params
name|params
parameter_list|)
throws|throws
name|IOException
block|{
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
if|if
condition|(
name|index
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"index"
argument_list|,
name|index
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|shardId
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"shard"
argument_list|,
name|shardId
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|nodeId
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"node"
argument_list|,
name|nodeId
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|field
argument_list|(
literal|"reason"
argument_list|)
expr_stmt|;
block|{
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|ElasticsearchException
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|,
name|reason
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
return|return
name|builder
return|;
block|}
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|Strings
operator|.
name|toString
argument_list|(
name|this
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

