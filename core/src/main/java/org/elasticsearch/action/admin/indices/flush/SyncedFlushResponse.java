begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.indices.flush
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|admin
operator|.
name|indices
operator|.
name|flush
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|ActionResponse
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
name|Streamable
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
name|iterable
operator|.
name|Iterables
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
name|indices
operator|.
name|flush
operator|.
name|ShardsSyncedFlushResult
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
name|flush
operator|.
name|SyncedFlushService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|RestStatus
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
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
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
name|Map
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
name|unmodifiableMap
import|;
end_import

begin_comment
comment|/**  * The result of performing a sync flush operation on all shards of multiple indices  */
end_comment

begin_class
DECL|class|SyncedFlushResponse
specifier|public
class|class
name|SyncedFlushResponse
extends|extends
name|ActionResponse
implements|implements
name|ToXContent
block|{
DECL|field|shardsResultPerIndex
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|ShardsSyncedFlushResult
argument_list|>
argument_list|>
name|shardsResultPerIndex
decl_stmt|;
DECL|field|shardCounts
name|ShardCounts
name|shardCounts
decl_stmt|;
DECL|method|SyncedFlushResponse
name|SyncedFlushResponse
parameter_list|()
block|{      }
DECL|method|SyncedFlushResponse
specifier|public
name|SyncedFlushResponse
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|ShardsSyncedFlushResult
argument_list|>
argument_list|>
name|shardsResultPerIndex
parameter_list|)
block|{
comment|// shardsResultPerIndex is never modified after it is passed to this
comment|// constructor so this is safe even though shardsResultPerIndex is a
comment|// ConcurrentHashMap
name|this
operator|.
name|shardsResultPerIndex
operator|=
name|unmodifiableMap
argument_list|(
name|shardsResultPerIndex
argument_list|)
expr_stmt|;
name|this
operator|.
name|shardCounts
operator|=
name|calculateShardCounts
argument_list|(
name|Iterables
operator|.
name|flatten
argument_list|(
name|shardsResultPerIndex
operator|.
name|values
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**      * total number shards, including replicas, both assigned and unassigned      */
DECL|method|totalShards
specifier|public
name|int
name|totalShards
parameter_list|()
block|{
return|return
name|shardCounts
operator|.
name|total
return|;
block|}
comment|/**      * total number of shards for which the operation failed      */
DECL|method|failedShards
specifier|public
name|int
name|failedShards
parameter_list|()
block|{
return|return
name|shardCounts
operator|.
name|failed
return|;
block|}
comment|/**      * total number of shards which were successfully sync-flushed      */
DECL|method|successfulShards
specifier|public
name|int
name|successfulShards
parameter_list|()
block|{
return|return
name|shardCounts
operator|.
name|successful
return|;
block|}
DECL|method|restStatus
specifier|public
name|RestStatus
name|restStatus
parameter_list|()
block|{
return|return
name|failedShards
argument_list|()
operator|==
literal|0
condition|?
name|RestStatus
operator|.
name|OK
else|:
name|RestStatus
operator|.
name|CONFLICT
return|;
block|}
DECL|method|getShardsResultPerIndex
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|ShardsSyncedFlushResult
argument_list|>
argument_list|>
name|getShardsResultPerIndex
parameter_list|()
block|{
return|return
name|shardsResultPerIndex
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
argument_list|(
name|Fields
operator|.
name|_SHARDS
argument_list|)
expr_stmt|;
name|shardCounts
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|ShardsSyncedFlushResult
argument_list|>
argument_list|>
name|indexEntry
range|:
name|shardsResultPerIndex
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|List
argument_list|<
name|ShardsSyncedFlushResult
argument_list|>
name|indexResult
init|=
name|indexEntry
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|builder
operator|.
name|startObject
argument_list|(
name|indexEntry
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
name|ShardCounts
name|indexShardCounts
init|=
name|calculateShardCounts
argument_list|(
name|indexResult
argument_list|)
decl_stmt|;
name|indexShardCounts
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
if|if
condition|(
name|indexShardCounts
operator|.
name|failed
operator|>
literal|0
condition|)
block|{
name|builder
operator|.
name|startArray
argument_list|(
name|Fields
operator|.
name|FAILURES
argument_list|)
expr_stmt|;
for|for
control|(
name|ShardsSyncedFlushResult
name|shardResults
range|:
name|indexResult
control|)
block|{
if|if
condition|(
name|shardResults
operator|.
name|failed
argument_list|()
condition|)
block|{
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|SHARD
argument_list|,
name|shardResults
operator|.
name|shardId
argument_list|()
operator|.
name|id
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|REASON
argument_list|,
name|shardResults
operator|.
name|failureReason
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
continue|continue;
block|}
name|Map
argument_list|<
name|ShardRouting
argument_list|,
name|SyncedFlushService
operator|.
name|ShardSyncedFlushResponse
argument_list|>
name|failedShards
init|=
name|shardResults
operator|.
name|failedShards
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|ShardRouting
argument_list|,
name|SyncedFlushService
operator|.
name|ShardSyncedFlushResponse
argument_list|>
name|shardEntry
range|:
name|failedShards
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|SHARD
argument_list|,
name|shardResults
operator|.
name|shardId
argument_list|()
operator|.
name|id
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|REASON
argument_list|,
name|shardEntry
operator|.
name|getValue
argument_list|()
operator|.
name|failureReason
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|ROUTING
argument_list|,
name|shardEntry
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
block|}
name|builder
operator|.
name|endArray
argument_list|()
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
return|return
name|builder
return|;
block|}
DECL|method|calculateShardCounts
specifier|static
name|ShardCounts
name|calculateShardCounts
parameter_list|(
name|Iterable
argument_list|<
name|ShardsSyncedFlushResult
argument_list|>
name|results
parameter_list|)
block|{
name|int
name|total
init|=
literal|0
decl_stmt|,
name|successful
init|=
literal|0
decl_stmt|,
name|failed
init|=
literal|0
decl_stmt|;
for|for
control|(
name|ShardsSyncedFlushResult
name|result
range|:
name|results
control|)
block|{
name|total
operator|+=
name|result
operator|.
name|totalShards
argument_list|()
expr_stmt|;
name|successful
operator|+=
name|result
operator|.
name|successfulShards
argument_list|()
expr_stmt|;
if|if
condition|(
name|result
operator|.
name|failed
argument_list|()
condition|)
block|{
comment|// treat all shard copies as failed
name|failed
operator|+=
name|result
operator|.
name|totalShards
argument_list|()
expr_stmt|;
block|}
else|else
block|{
comment|// some shards may have failed during the sync phase
name|failed
operator|+=
name|result
operator|.
name|failedShards
argument_list|()
operator|.
name|size
argument_list|()
expr_stmt|;
block|}
block|}
return|return
operator|new
name|ShardCounts
argument_list|(
name|total
argument_list|,
name|successful
argument_list|,
name|failed
argument_list|)
return|;
block|}
DECL|class|ShardCounts
specifier|static
specifier|final
class|class
name|ShardCounts
implements|implements
name|ToXContent
implements|,
name|Streamable
block|{
DECL|field|total
specifier|public
name|int
name|total
decl_stmt|;
DECL|field|successful
specifier|public
name|int
name|successful
decl_stmt|;
DECL|field|failed
specifier|public
name|int
name|failed
decl_stmt|;
DECL|method|ShardCounts
name|ShardCounts
parameter_list|(
name|int
name|total
parameter_list|,
name|int
name|successful
parameter_list|,
name|int
name|failed
parameter_list|)
block|{
name|this
operator|.
name|total
operator|=
name|total
expr_stmt|;
name|this
operator|.
name|successful
operator|=
name|successful
expr_stmt|;
name|this
operator|.
name|failed
operator|=
name|failed
expr_stmt|;
block|}
DECL|method|ShardCounts
name|ShardCounts
parameter_list|()
block|{          }
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
name|field
argument_list|(
name|Fields
operator|.
name|TOTAL
argument_list|,
name|total
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|SUCCESSFUL
argument_list|,
name|successful
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|FAILED
argument_list|,
name|failed
argument_list|)
expr_stmt|;
return|return
name|builder
return|;
block|}
annotation|@
name|Override
DECL|method|readFrom
specifier|public
name|void
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|total
operator|=
name|in
operator|.
name|readInt
argument_list|()
expr_stmt|;
name|successful
operator|=
name|in
operator|.
name|readInt
argument_list|()
expr_stmt|;
name|failed
operator|=
name|in
operator|.
name|readInt
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
name|writeInt
argument_list|(
name|total
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|successful
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|failed
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|Fields
specifier|static
specifier|final
class|class
name|Fields
block|{
DECL|field|_SHARDS
specifier|static
specifier|final
name|String
name|_SHARDS
init|=
literal|"_shards"
decl_stmt|;
DECL|field|TOTAL
specifier|static
specifier|final
name|String
name|TOTAL
init|=
literal|"total"
decl_stmt|;
DECL|field|SUCCESSFUL
specifier|static
specifier|final
name|String
name|SUCCESSFUL
init|=
literal|"successful"
decl_stmt|;
DECL|field|FAILED
specifier|static
specifier|final
name|String
name|FAILED
init|=
literal|"failed"
decl_stmt|;
DECL|field|FAILURES
specifier|static
specifier|final
name|String
name|FAILURES
init|=
literal|"failures"
decl_stmt|;
DECL|field|SHARD
specifier|static
specifier|final
name|String
name|SHARD
init|=
literal|"shard"
decl_stmt|;
DECL|field|ROUTING
specifier|static
specifier|final
name|String
name|ROUTING
init|=
literal|"routing"
decl_stmt|;
DECL|field|REASON
specifier|static
specifier|final
name|String
name|REASON
init|=
literal|"reason"
decl_stmt|;
block|}
annotation|@
name|Override
DECL|method|readFrom
specifier|public
name|void
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|shardCounts
operator|=
operator|new
name|ShardCounts
argument_list|()
expr_stmt|;
name|shardCounts
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|ShardsSyncedFlushResult
argument_list|>
argument_list|>
name|tmpShardsResultPerIndex
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|int
name|numShardsResults
init|=
name|in
operator|.
name|readInt
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
name|numShardsResults
condition|;
name|i
operator|++
control|)
block|{
name|String
name|index
init|=
name|in
operator|.
name|readString
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|ShardsSyncedFlushResult
argument_list|>
name|shardsSyncedFlushResults
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|int
name|numShards
init|=
name|in
operator|.
name|readInt
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|numShards
condition|;
name|j
operator|++
control|)
block|{
name|shardsSyncedFlushResults
operator|.
name|add
argument_list|(
name|ShardsSyncedFlushResult
operator|.
name|readShardsSyncedFlushResult
argument_list|(
name|in
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|tmpShardsResultPerIndex
operator|.
name|put
argument_list|(
name|index
argument_list|,
name|shardsSyncedFlushResults
argument_list|)
expr_stmt|;
block|}
name|shardsResultPerIndex
operator|=
name|Collections
operator|.
name|unmodifiableMap
argument_list|(
name|tmpShardsResultPerIndex
argument_list|)
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
name|super
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|shardCounts
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|shardsResultPerIndex
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|ShardsSyncedFlushResult
argument_list|>
argument_list|>
name|entry
range|:
name|shardsResultPerIndex
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|out
operator|.
name|writeString
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|ShardsSyncedFlushResult
name|shardsSyncedFlushResult
range|:
name|entry
operator|.
name|getValue
argument_list|()
control|)
block|{
name|shardsSyncedFlushResult
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

