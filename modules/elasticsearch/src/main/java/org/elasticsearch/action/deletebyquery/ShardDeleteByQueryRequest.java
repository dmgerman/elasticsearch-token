begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.deletebyquery
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|deletebyquery
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
name|support
operator|.
name|replication
operator|.
name|ShardReplicationOperationRequest
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
name|Nullable
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
name|Strings
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
name|util
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
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|Actions
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * Delete by query request to execute on a specific shard.  *  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|ShardDeleteByQueryRequest
specifier|public
class|class
name|ShardDeleteByQueryRequest
extends|extends
name|ShardReplicationOperationRequest
block|{
DECL|field|shardId
specifier|private
name|int
name|shardId
decl_stmt|;
DECL|field|querySource
specifier|private
name|byte
index|[]
name|querySource
decl_stmt|;
DECL|field|queryParserName
specifier|private
name|String
name|queryParserName
decl_stmt|;
DECL|field|types
specifier|private
name|String
index|[]
name|types
init|=
name|Strings
operator|.
name|EMPTY_ARRAY
decl_stmt|;
DECL|method|ShardDeleteByQueryRequest
specifier|public
name|ShardDeleteByQueryRequest
parameter_list|(
name|String
name|index
parameter_list|,
name|byte
index|[]
name|querySource
parameter_list|,
annotation|@
name|Nullable
name|String
name|queryParserName
parameter_list|,
name|String
index|[]
name|types
parameter_list|,
name|int
name|shardId
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
name|querySource
operator|=
name|querySource
expr_stmt|;
name|this
operator|.
name|queryParserName
operator|=
name|queryParserName
expr_stmt|;
name|this
operator|.
name|types
operator|=
name|types
expr_stmt|;
name|this
operator|.
name|shardId
operator|=
name|shardId
expr_stmt|;
block|}
DECL|method|ShardDeleteByQueryRequest
name|ShardDeleteByQueryRequest
parameter_list|(
name|IndexDeleteByQueryRequest
name|request
parameter_list|,
name|int
name|shardId
parameter_list|)
block|{
name|this
argument_list|(
name|request
operator|.
name|index
argument_list|()
argument_list|,
name|request
operator|.
name|querySource
argument_list|()
argument_list|,
name|request
operator|.
name|queryParserName
argument_list|()
argument_list|,
name|request
operator|.
name|types
argument_list|()
argument_list|,
name|shardId
argument_list|)
expr_stmt|;
name|timeout
operator|=
name|request
operator|.
name|timeout
argument_list|()
expr_stmt|;
block|}
DECL|method|ShardDeleteByQueryRequest
name|ShardDeleteByQueryRequest
parameter_list|()
block|{     }
DECL|method|validate
annotation|@
name|Override
specifier|public
name|ActionRequestValidationException
name|validate
parameter_list|()
block|{
name|ActionRequestValidationException
name|validationException
init|=
name|super
operator|.
name|validate
argument_list|()
decl_stmt|;
if|if
condition|(
name|querySource
operator|==
literal|null
condition|)
block|{
name|addValidationError
argument_list|(
literal|"querySource is missing"
argument_list|,
name|validationException
argument_list|)
expr_stmt|;
block|}
return|return
name|validationException
return|;
block|}
DECL|method|shardId
specifier|public
name|int
name|shardId
parameter_list|()
block|{
return|return
name|this
operator|.
name|shardId
return|;
block|}
DECL|method|querySource
specifier|public
name|byte
index|[]
name|querySource
parameter_list|()
block|{
return|return
name|querySource
return|;
block|}
DECL|method|queryParserName
specifier|public
name|String
name|queryParserName
parameter_list|()
block|{
return|return
name|queryParserName
return|;
block|}
DECL|method|types
specifier|public
name|String
index|[]
name|types
parameter_list|()
block|{
return|return
name|this
operator|.
name|types
return|;
block|}
DECL|method|readFrom
annotation|@
name|Override
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
name|querySource
operator|=
operator|new
name|byte
index|[
name|in
operator|.
name|readVInt
argument_list|()
index|]
expr_stmt|;
name|in
operator|.
name|readFully
argument_list|(
name|querySource
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
name|queryParserName
operator|=
name|in
operator|.
name|readUTF
argument_list|()
expr_stmt|;
block|}
name|shardId
operator|=
name|in
operator|.
name|readVInt
argument_list|()
expr_stmt|;
name|int
name|typesSize
init|=
name|in
operator|.
name|readVInt
argument_list|()
decl_stmt|;
if|if
condition|(
name|typesSize
operator|>
literal|0
condition|)
block|{
name|types
operator|=
operator|new
name|String
index|[
name|typesSize
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
name|typesSize
condition|;
name|i
operator|++
control|)
block|{
name|types
index|[
name|i
index|]
operator|=
name|in
operator|.
name|readUTF
argument_list|()
expr_stmt|;
block|}
block|}
block|}
DECL|method|writeTo
annotation|@
name|Override
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
name|querySource
operator|.
name|length
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBytes
argument_list|(
name|querySource
argument_list|)
expr_stmt|;
if|if
condition|(
name|queryParserName
operator|==
literal|null
condition|)
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeUTF
argument_list|(
name|queryParserName
argument_list|)
expr_stmt|;
block|}
name|out
operator|.
name|writeVInt
argument_list|(
name|shardId
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|types
operator|.
name|length
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|type
range|:
name|types
control|)
block|{
name|out
operator|.
name|writeUTF
argument_list|(
name|type
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

