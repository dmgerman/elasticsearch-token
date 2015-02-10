begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.indices.recovery
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
name|recovery
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
name|support
operator|.
name|broadcast
operator|.
name|BroadcastOperationResponse
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

begin_comment
comment|/**  * Information regarding the recovery state of indices and their associated shards.  */
end_comment

begin_class
DECL|class|RecoveryResponse
specifier|public
class|class
name|RecoveryResponse
extends|extends
name|BroadcastOperationResponse
implements|implements
name|ToXContent
block|{
DECL|field|detailed
specifier|private
name|boolean
name|detailed
init|=
literal|false
decl_stmt|;
DECL|field|shardResponses
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|ShardRecoveryResponse
argument_list|>
argument_list|>
name|shardResponses
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
DECL|method|RecoveryResponse
specifier|public
name|RecoveryResponse
parameter_list|()
block|{ }
comment|/**      * Constructs recovery information for a collection of indices and associated shards. Keeps track of how many total shards      * were seen, and out of those how many were successfully processed and how many failed.      *      * @param totalShards       Total count of shards seen      * @param successfulShards  Count of shards successfully processed      * @param failedShards      Count of shards which failed to process      * @param detailed          Display detailed metrics      * @param shardResponses    Map of indices to shard recovery information      * @param shardFailures     List of failures processing shards      */
DECL|method|RecoveryResponse
specifier|public
name|RecoveryResponse
parameter_list|(
name|int
name|totalShards
parameter_list|,
name|int
name|successfulShards
parameter_list|,
name|int
name|failedShards
parameter_list|,
name|boolean
name|detailed
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|ShardRecoveryResponse
argument_list|>
argument_list|>
name|shardResponses
parameter_list|,
name|List
argument_list|<
name|ShardOperationFailedException
argument_list|>
name|shardFailures
parameter_list|)
block|{
name|super
argument_list|(
name|totalShards
argument_list|,
name|successfulShards
argument_list|,
name|failedShards
argument_list|,
name|shardFailures
argument_list|)
expr_stmt|;
name|this
operator|.
name|shardResponses
operator|=
name|shardResponses
expr_stmt|;
name|this
operator|.
name|detailed
operator|=
name|detailed
expr_stmt|;
block|}
DECL|method|hasRecoveries
specifier|public
name|boolean
name|hasRecoveries
parameter_list|()
block|{
return|return
name|shardResponses
operator|.
name|size
argument_list|()
operator|>
literal|0
return|;
block|}
DECL|method|detailed
specifier|public
name|boolean
name|detailed
parameter_list|()
block|{
return|return
name|detailed
return|;
block|}
DECL|method|detailed
specifier|public
name|void
name|detailed
parameter_list|(
name|boolean
name|detailed
parameter_list|)
block|{
name|this
operator|.
name|detailed
operator|=
name|detailed
expr_stmt|;
block|}
DECL|method|shardResponses
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|ShardRecoveryResponse
argument_list|>
argument_list|>
name|shardResponses
parameter_list|()
block|{
return|return
name|shardResponses
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
if|if
condition|(
name|hasRecoveries
argument_list|()
condition|)
block|{
for|for
control|(
name|String
name|index
range|:
name|shardResponses
operator|.
name|keySet
argument_list|()
control|)
block|{
name|List
argument_list|<
name|ShardRecoveryResponse
argument_list|>
name|responses
init|=
name|shardResponses
operator|.
name|get
argument_list|(
name|index
argument_list|)
decl_stmt|;
if|if
condition|(
name|responses
operator|==
literal|null
operator|||
name|responses
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
continue|continue;
block|}
name|builder
operator|.
name|startObject
argument_list|(
name|index
argument_list|)
expr_stmt|;
name|builder
operator|.
name|startArray
argument_list|(
literal|"shards"
argument_list|)
expr_stmt|;
for|for
control|(
name|ShardRecoveryResponse
name|recoveryResponse
range|:
name|responses
control|)
block|{
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|recoveryResponse
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
block|}
name|builder
operator|.
name|endArray
argument_list|()
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
block|}
return|return
name|builder
return|;
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
name|out
operator|.
name|writeVInt
argument_list|(
name|shardResponses
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
name|ShardRecoveryResponse
argument_list|>
argument_list|>
name|entry
range|:
name|shardResponses
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
name|writeVInt
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
name|ShardRecoveryResponse
name|recoveryResponse
range|:
name|entry
operator|.
name|getValue
argument_list|()
control|)
block|{
name|recoveryResponse
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
block|}
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
name|int
name|size
init|=
name|in
operator|.
name|readVInt
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
name|size
condition|;
name|i
operator|++
control|)
block|{
name|String
name|s
init|=
name|in
operator|.
name|readString
argument_list|()
decl_stmt|;
name|int
name|listSize
init|=
name|in
operator|.
name|readVInt
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|ShardRecoveryResponse
argument_list|>
name|list
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|listSize
argument_list|)
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
name|listSize
condition|;
name|j
operator|++
control|)
block|{
name|list
operator|.
name|add
argument_list|(
name|ShardRecoveryResponse
operator|.
name|readShardRecoveryResponse
argument_list|(
name|in
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|shardResponses
operator|.
name|put
argument_list|(
name|s
argument_list|,
name|list
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

