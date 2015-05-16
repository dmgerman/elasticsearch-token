begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.support.broadcast
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|support
operator|.
name|broadcast
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
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|support
operator|.
name|DefaultShardOperationFailedException
operator|.
name|readShardOperationFailed
import|;
end_import

begin_comment
comment|/**  * Base class for all broadcast operation based responses.  */
end_comment

begin_class
DECL|class|BroadcastOperationResponse
specifier|public
specifier|abstract
class|class
name|BroadcastOperationResponse
extends|extends
name|ActionResponse
block|{
DECL|field|EMPTY
specifier|private
specifier|static
specifier|final
name|ShardOperationFailedException
index|[]
name|EMPTY
init|=
operator|new
name|ShardOperationFailedException
index|[
literal|0
index|]
decl_stmt|;
DECL|field|totalShards
specifier|private
name|int
name|totalShards
decl_stmt|;
DECL|field|successfulShards
specifier|private
name|int
name|successfulShards
decl_stmt|;
DECL|field|failedShards
specifier|private
name|int
name|failedShards
decl_stmt|;
DECL|field|shardFailures
specifier|private
name|ShardOperationFailedException
index|[]
name|shardFailures
init|=
name|EMPTY
decl_stmt|;
DECL|method|BroadcastOperationResponse
specifier|protected
name|BroadcastOperationResponse
parameter_list|()
block|{     }
DECL|method|BroadcastOperationResponse
specifier|protected
name|BroadcastOperationResponse
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
name|List
argument_list|<
name|?
extends|extends
name|ShardOperationFailedException
argument_list|>
name|shardFailures
parameter_list|)
block|{
name|this
operator|.
name|totalShards
operator|=
name|totalShards
expr_stmt|;
name|this
operator|.
name|successfulShards
operator|=
name|successfulShards
expr_stmt|;
name|this
operator|.
name|failedShards
operator|=
name|failedShards
expr_stmt|;
name|this
operator|.
name|shardFailures
operator|=
name|shardFailures
operator|==
literal|null
condition|?
name|EMPTY
else|:
name|shardFailures
operator|.
name|toArray
argument_list|(
operator|new
name|ShardOperationFailedException
index|[
name|shardFailures
operator|.
name|size
argument_list|()
index|]
argument_list|)
expr_stmt|;
block|}
comment|/**      * The total shards this request ran against.      */
DECL|method|getTotalShards
specifier|public
name|int
name|getTotalShards
parameter_list|()
block|{
return|return
name|totalShards
return|;
block|}
comment|/**      * The successful shards this request was executed on.      */
DECL|method|getSuccessfulShards
specifier|public
name|int
name|getSuccessfulShards
parameter_list|()
block|{
return|return
name|successfulShards
return|;
block|}
comment|/**      * The failed shards this request was executed on.      */
DECL|method|getFailedShards
specifier|public
name|int
name|getFailedShards
parameter_list|()
block|{
return|return
name|failedShards
return|;
block|}
comment|/**      * The list of shard failures exception.      */
DECL|method|getShardFailures
specifier|public
name|ShardOperationFailedException
index|[]
name|getShardFailures
parameter_list|()
block|{
return|return
name|shardFailures
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
name|totalShards
operator|=
name|in
operator|.
name|readVInt
argument_list|()
expr_stmt|;
name|successfulShards
operator|=
name|in
operator|.
name|readVInt
argument_list|()
expr_stmt|;
name|failedShards
operator|=
name|in
operator|.
name|readVInt
argument_list|()
expr_stmt|;
name|int
name|size
init|=
name|in
operator|.
name|readVInt
argument_list|()
decl_stmt|;
if|if
condition|(
name|size
operator|>
literal|0
condition|)
block|{
name|shardFailures
operator|=
operator|new
name|ShardOperationFailedException
index|[
name|size
index|]
expr_stmt|;
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
name|shardFailures
index|[
name|i
index|]
operator|=
name|readShardOperationFailed
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
block|}
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
name|totalShards
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|successfulShards
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|failedShards
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|shardFailures
operator|.
name|length
argument_list|)
expr_stmt|;
for|for
control|(
name|ShardOperationFailedException
name|exp
range|:
name|shardFailures
control|)
block|{
name|exp
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

