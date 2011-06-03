begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.count
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|count
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
name|support
operator|.
name|broadcast
operator|.
name|BroadcastShardOperationRequest
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

begin_comment
comment|/**  * Internal count request executed directly against a specific index shard.  *  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|ShardCountRequest
class|class
name|ShardCountRequest
extends|extends
name|BroadcastShardOperationRequest
block|{
DECL|field|minScore
specifier|private
name|float
name|minScore
decl_stmt|;
DECL|field|querySource
specifier|private
name|byte
index|[]
name|querySource
decl_stmt|;
DECL|field|querySourceOffset
specifier|private
name|int
name|querySourceOffset
decl_stmt|;
DECL|field|querySourceLength
specifier|private
name|int
name|querySourceLength
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
DECL|field|filteringAliases
annotation|@
name|Nullable
specifier|private
name|String
index|[]
name|filteringAliases
decl_stmt|;
DECL|method|ShardCountRequest
name|ShardCountRequest
parameter_list|()
block|{      }
DECL|method|ShardCountRequest
specifier|public
name|ShardCountRequest
parameter_list|(
name|String
name|index
parameter_list|,
name|int
name|shardId
parameter_list|,
annotation|@
name|Nullable
name|String
index|[]
name|filteringAliases
parameter_list|,
name|CountRequest
name|request
parameter_list|)
block|{
name|super
argument_list|(
name|index
argument_list|,
name|shardId
argument_list|)
expr_stmt|;
name|this
operator|.
name|minScore
operator|=
name|request
operator|.
name|minScore
argument_list|()
expr_stmt|;
name|this
operator|.
name|querySource
operator|=
name|request
operator|.
name|querySource
argument_list|()
expr_stmt|;
name|this
operator|.
name|querySourceOffset
operator|=
name|request
operator|.
name|querySourceOffset
argument_list|()
expr_stmt|;
name|this
operator|.
name|querySourceLength
operator|=
name|request
operator|.
name|querySourceLength
argument_list|()
expr_stmt|;
name|this
operator|.
name|types
operator|=
name|request
operator|.
name|types
argument_list|()
expr_stmt|;
name|this
operator|.
name|filteringAliases
operator|=
name|filteringAliases
expr_stmt|;
block|}
DECL|method|minScore
specifier|public
name|float
name|minScore
parameter_list|()
block|{
return|return
name|minScore
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
DECL|method|querySourceOffset
specifier|public
name|int
name|querySourceOffset
parameter_list|()
block|{
return|return
name|querySourceOffset
return|;
block|}
DECL|method|querySourceLength
specifier|public
name|int
name|querySourceLength
parameter_list|()
block|{
return|return
name|querySourceLength
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
DECL|method|filteringAliases
specifier|public
name|String
index|[]
name|filteringAliases
parameter_list|()
block|{
return|return
name|filteringAliases
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
name|minScore
operator|=
name|in
operator|.
name|readFloat
argument_list|()
expr_stmt|;
name|querySourceLength
operator|=
name|in
operator|.
name|readVInt
argument_list|()
expr_stmt|;
name|querySourceOffset
operator|=
literal|0
expr_stmt|;
name|querySource
operator|=
operator|new
name|byte
index|[
name|querySourceLength
index|]
expr_stmt|;
name|in
operator|.
name|readFully
argument_list|(
name|querySource
argument_list|)
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
name|int
name|aliasesSize
init|=
name|in
operator|.
name|readVInt
argument_list|()
decl_stmt|;
if|if
condition|(
name|aliasesSize
operator|>
literal|0
condition|)
block|{
name|filteringAliases
operator|=
operator|new
name|String
index|[
name|aliasesSize
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
name|aliasesSize
condition|;
name|i
operator|++
control|)
block|{
name|filteringAliases
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
name|writeFloat
argument_list|(
name|minScore
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|querySourceLength
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBytes
argument_list|(
name|querySource
argument_list|,
name|querySourceOffset
argument_list|,
name|querySourceLength
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
if|if
condition|(
name|filteringAliases
operator|!=
literal|null
condition|)
block|{
name|out
operator|.
name|writeVInt
argument_list|(
name|filteringAliases
operator|.
name|length
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|alias
range|:
name|filteringAliases
control|)
block|{
name|out
operator|.
name|writeUTF
argument_list|(
name|alias
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|out
operator|.
name|writeVInt
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

