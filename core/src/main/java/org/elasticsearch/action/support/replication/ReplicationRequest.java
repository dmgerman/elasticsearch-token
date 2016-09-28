begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.support.replication
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|support
operator|.
name|replication
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
name|ActionRequest
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
name|ActionRequestValidationException
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
name|IndicesRequest
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
name|admin
operator|.
name|indices
operator|.
name|refresh
operator|.
name|TransportShardRefreshAction
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
name|index
operator|.
name|IndexRequest
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
name|ActiveShardCount
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
name|IndicesOptions
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
name|tasks
operator|.
name|Task
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|tasks
operator|.
name|TaskId
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
name|concurrent
operator|.
name|TimeUnit
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|ValidateActions
operator|.
name|addValidationError
import|;
end_import

begin_comment
comment|/**  * Requests that are run on a particular replica, first on the primary and then on the replicas like {@link IndexRequest} or  * {@link TransportShardRefreshAction}.  */
end_comment

begin_class
DECL|class|ReplicationRequest
specifier|public
specifier|abstract
class|class
name|ReplicationRequest
parameter_list|<
name|Request
extends|extends
name|ReplicationRequest
parameter_list|<
name|Request
parameter_list|>
parameter_list|>
extends|extends
name|ActionRequest
argument_list|<
name|Request
argument_list|>
implements|implements
name|IndicesRequest
block|{
DECL|field|DEFAULT_TIMEOUT
specifier|public
specifier|static
specifier|final
name|TimeValue
name|DEFAULT_TIMEOUT
init|=
operator|new
name|TimeValue
argument_list|(
literal|1
argument_list|,
name|TimeUnit
operator|.
name|MINUTES
argument_list|)
decl_stmt|;
comment|/**      * Target shard the request should execute on. In case of index and delete requests,      * shard id gets resolved by the transport action before performing request operation      * and at request creation time for shard-level bulk, refresh and flush requests.      */
DECL|field|shardId
specifier|protected
name|ShardId
name|shardId
decl_stmt|;
DECL|field|seqNo
name|long
name|seqNo
decl_stmt|;
DECL|field|primaryTerm
name|long
name|primaryTerm
decl_stmt|;
DECL|field|timeout
specifier|protected
name|TimeValue
name|timeout
init|=
name|DEFAULT_TIMEOUT
decl_stmt|;
DECL|field|index
specifier|protected
name|String
name|index
decl_stmt|;
comment|/**      * The number of shard copies that must be active before proceeding with the replication action.      */
DECL|field|waitForActiveShards
specifier|protected
name|ActiveShardCount
name|waitForActiveShards
init|=
name|ActiveShardCount
operator|.
name|DEFAULT
decl_stmt|;
DECL|field|routedBasedOnClusterVersion
specifier|private
name|long
name|routedBasedOnClusterVersion
init|=
literal|0
decl_stmt|;
DECL|method|ReplicationRequest
specifier|public
name|ReplicationRequest
parameter_list|()
block|{      }
comment|/**      * Creates a new request with resolved shard id      */
DECL|method|ReplicationRequest
specifier|public
name|ReplicationRequest
parameter_list|(
name|ShardId
name|shardId
parameter_list|)
block|{
name|this
operator|.
name|index
operator|=
name|shardId
operator|.
name|getIndexName
argument_list|()
expr_stmt|;
name|this
operator|.
name|shardId
operator|=
name|shardId
expr_stmt|;
block|}
comment|/**      * A timeout to wait if the index operation can't be performed immediately. Defaults to<tt>1m</tt>.      */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|method|timeout
specifier|public
specifier|final
name|Request
name|timeout
parameter_list|(
name|TimeValue
name|timeout
parameter_list|)
block|{
name|this
operator|.
name|timeout
operator|=
name|timeout
expr_stmt|;
return|return
operator|(
name|Request
operator|)
name|this
return|;
block|}
comment|/**      * A timeout to wait if the index operation can't be performed immediately. Defaults to<tt>1m</tt>.      */
DECL|method|timeout
specifier|public
specifier|final
name|Request
name|timeout
parameter_list|(
name|String
name|timeout
parameter_list|)
block|{
return|return
name|timeout
argument_list|(
name|TimeValue
operator|.
name|parseTimeValue
argument_list|(
name|timeout
argument_list|,
literal|null
argument_list|,
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|".timeout"
argument_list|)
argument_list|)
return|;
block|}
DECL|method|timeout
specifier|public
name|TimeValue
name|timeout
parameter_list|()
block|{
return|return
name|timeout
return|;
block|}
DECL|method|index
specifier|public
name|String
name|index
parameter_list|()
block|{
return|return
name|this
operator|.
name|index
return|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|method|index
specifier|public
specifier|final
name|Request
name|index
parameter_list|(
name|String
name|index
parameter_list|)
block|{
name|this
operator|.
name|index
operator|=
name|index
expr_stmt|;
return|return
operator|(
name|Request
operator|)
name|this
return|;
block|}
annotation|@
name|Override
DECL|method|indices
specifier|public
name|String
index|[]
name|indices
parameter_list|()
block|{
return|return
operator|new
name|String
index|[]
block|{
name|index
block|}
return|;
block|}
annotation|@
name|Override
DECL|method|indicesOptions
specifier|public
name|IndicesOptions
name|indicesOptions
parameter_list|()
block|{
return|return
name|IndicesOptions
operator|.
name|strictSingleIndexNoExpandForbidClosed
argument_list|()
return|;
block|}
DECL|method|waitForActiveShards
specifier|public
name|ActiveShardCount
name|waitForActiveShards
parameter_list|()
block|{
return|return
name|this
operator|.
name|waitForActiveShards
return|;
block|}
comment|/**      * @return the shardId of the shard where this operation should be executed on.      * can be null if the shardID has not yet been resolved      */
annotation|@
name|Nullable
DECL|method|shardId
specifier|public
name|ShardId
name|shardId
parameter_list|()
block|{
return|return
name|shardId
return|;
block|}
comment|/**      * Sets the number of shard copies that must be active before proceeding with the replication      * operation. Defaults to {@link ActiveShardCount#DEFAULT}, which requires one shard copy      * (the primary) to be active. Set this value to {@link ActiveShardCount#ALL} to      * wait for all shards (primary and all replicas) to be active. Otherwise, use      * {@link ActiveShardCount#from(int)} to set this value to any non-negative integer, up to the      * total number of shard copies (number of replicas + 1).      */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|method|waitForActiveShards
specifier|public
specifier|final
name|Request
name|waitForActiveShards
parameter_list|(
name|ActiveShardCount
name|waitForActiveShards
parameter_list|)
block|{
name|this
operator|.
name|waitForActiveShards
operator|=
name|waitForActiveShards
expr_stmt|;
return|return
operator|(
name|Request
operator|)
name|this
return|;
block|}
comment|/**      * A shortcut for {@link #waitForActiveShards(ActiveShardCount)} where the numerical      * shard count is passed in, instead of having to first call {@link ActiveShardCount#from(int)}      * to get the ActiveShardCount.      */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|method|waitForActiveShards
specifier|public
specifier|final
name|Request
name|waitForActiveShards
parameter_list|(
specifier|final
name|int
name|waitForActiveShards
parameter_list|)
block|{
return|return
name|waitForActiveShards
argument_list|(
name|ActiveShardCount
operator|.
name|from
argument_list|(
name|waitForActiveShards
argument_list|)
argument_list|)
return|;
block|}
comment|/**      * Sets the minimum version of the cluster state that is required on the next node before we redirect to another primary.      * Used to prevent redirect loops, see also {@link TransportReplicationAction.ReroutePhase#doRun()}      */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|method|routedBasedOnClusterVersion
name|Request
name|routedBasedOnClusterVersion
parameter_list|(
name|long
name|routedBasedOnClusterVersion
parameter_list|)
block|{
name|this
operator|.
name|routedBasedOnClusterVersion
operator|=
name|routedBasedOnClusterVersion
expr_stmt|;
return|return
operator|(
name|Request
operator|)
name|this
return|;
block|}
DECL|method|routedBasedOnClusterVersion
name|long
name|routedBasedOnClusterVersion
parameter_list|()
block|{
return|return
name|routedBasedOnClusterVersion
return|;
block|}
comment|/**      * Returns the sequence number for this operation. The sequence number is assigned while the operation      * is performed on the primary shard.      */
DECL|method|seqNo
specifier|public
name|long
name|seqNo
parameter_list|()
block|{
return|return
name|seqNo
return|;
block|}
comment|/** sets the sequence number for this operation. should only be called on the primary shard */
DECL|method|seqNo
specifier|public
name|void
name|seqNo
parameter_list|(
name|long
name|seqNo
parameter_list|)
block|{
name|this
operator|.
name|seqNo
operator|=
name|seqNo
expr_stmt|;
block|}
comment|/** returns the primary term active at the time the operation was performed on the primary shard */
DECL|method|primaryTerm
specifier|public
name|long
name|primaryTerm
parameter_list|()
block|{
return|return
name|primaryTerm
return|;
block|}
comment|/** marks the primary term in which the operation was performed */
DECL|method|primaryTerm
specifier|public
name|void
name|primaryTerm
parameter_list|(
name|long
name|term
parameter_list|)
block|{
name|primaryTerm
operator|=
name|term
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|validate
specifier|public
name|ActionRequestValidationException
name|validate
parameter_list|()
block|{
name|ActionRequestValidationException
name|validationException
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|index
operator|==
literal|null
condition|)
block|{
name|validationException
operator|=
name|addValidationError
argument_list|(
literal|"index is missing"
argument_list|,
name|validationException
argument_list|)
expr_stmt|;
block|}
return|return
name|validationException
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
name|super
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
if|if
condition|(
name|in
operator|.
name|readBoolean
argument_list|()
condition|)
block|{
name|shardId
operator|=
name|ShardId
operator|.
name|readShardId
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|shardId
operator|=
literal|null
expr_stmt|;
block|}
name|waitForActiveShards
operator|=
name|ActiveShardCount
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|timeout
operator|=
operator|new
name|TimeValue
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|index
operator|=
name|in
operator|.
name|readString
argument_list|()
expr_stmt|;
name|routedBasedOnClusterVersion
operator|=
name|in
operator|.
name|readVLong
argument_list|()
expr_stmt|;
name|seqNo
operator|=
name|in
operator|.
name|readVLong
argument_list|()
expr_stmt|;
name|primaryTerm
operator|=
name|in
operator|.
name|readVLong
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
name|super
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
if|if
condition|(
name|shardId
operator|!=
literal|null
condition|)
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|shardId
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
name|waitForActiveShards
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|timeout
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeString
argument_list|(
name|index
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVLong
argument_list|(
name|routedBasedOnClusterVersion
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVLong
argument_list|(
name|seqNo
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVLong
argument_list|(
name|primaryTerm
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|createTask
specifier|public
name|Task
name|createTask
parameter_list|(
name|long
name|id
parameter_list|,
name|String
name|type
parameter_list|,
name|String
name|action
parameter_list|,
name|TaskId
name|parentTaskId
parameter_list|)
block|{
return|return
operator|new
name|ReplicationTask
argument_list|(
name|id
argument_list|,
name|type
argument_list|,
name|action
argument_list|,
name|getDescription
argument_list|()
argument_list|,
name|parentTaskId
argument_list|)
return|;
block|}
comment|/**      * Sets the target shard id for the request. The shard id is set when a      * index/delete request is resolved by the transport action      */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|method|setShardId
specifier|public
name|Request
name|setShardId
parameter_list|(
name|ShardId
name|shardId
parameter_list|)
block|{
name|this
operator|.
name|shardId
operator|=
name|shardId
expr_stmt|;
return|return
operator|(
name|Request
operator|)
name|this
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
if|if
condition|(
name|shardId
operator|!=
literal|null
condition|)
block|{
return|return
name|shardId
operator|.
name|toString
argument_list|()
return|;
block|}
else|else
block|{
return|return
name|index
return|;
block|}
block|}
annotation|@
name|Override
DECL|method|getDescription
specifier|public
name|String
name|getDescription
parameter_list|()
block|{
return|return
name|toString
argument_list|()
return|;
block|}
comment|/**      * This method is called before this replication request is retried      * the first time.      */
DECL|method|onRetry
specifier|public
name|void
name|onRetry
parameter_list|()
block|{
comment|// nothing by default
block|}
block|}
end_class

end_unit

