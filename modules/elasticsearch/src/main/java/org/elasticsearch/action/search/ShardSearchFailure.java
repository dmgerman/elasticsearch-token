begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|ShardOperationFailedException
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
name|SearchException
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
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|SearchShardTarget
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * Represents a failure to search on a specific shard.  *  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|ShardSearchFailure
specifier|public
class|class
name|ShardSearchFailure
implements|implements
name|ShardOperationFailedException
block|{
DECL|field|EMPTY_ARRAY
specifier|public
specifier|static
specifier|final
name|ShardSearchFailure
index|[]
name|EMPTY_ARRAY
init|=
operator|new
name|ShardSearchFailure
index|[
literal|0
index|]
decl_stmt|;
DECL|field|shardTarget
specifier|private
name|SearchShardTarget
name|shardTarget
decl_stmt|;
DECL|field|reason
specifier|private
name|String
name|reason
decl_stmt|;
DECL|method|ShardSearchFailure
specifier|private
name|ShardSearchFailure
parameter_list|()
block|{      }
DECL|method|ShardSearchFailure
specifier|public
name|ShardSearchFailure
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|Throwable
name|actual
init|=
name|ExceptionsHelper
operator|.
name|unwrapCause
argument_list|(
name|t
argument_list|)
decl_stmt|;
if|if
condition|(
name|actual
operator|!=
literal|null
operator|&&
name|actual
operator|instanceof
name|SearchException
condition|)
block|{
name|this
operator|.
name|shardTarget
operator|=
operator|(
operator|(
name|SearchException
operator|)
name|actual
operator|)
operator|.
name|shard
argument_list|()
expr_stmt|;
block|}
name|this
operator|.
name|reason
operator|=
name|ExceptionsHelper
operator|.
name|detailedMessage
argument_list|(
name|t
argument_list|)
expr_stmt|;
block|}
DECL|method|ShardSearchFailure
specifier|public
name|ShardSearchFailure
parameter_list|(
name|String
name|reason
parameter_list|,
name|SearchShardTarget
name|shardTarget
parameter_list|)
block|{
name|this
operator|.
name|shardTarget
operator|=
name|shardTarget
expr_stmt|;
name|this
operator|.
name|reason
operator|=
name|reason
expr_stmt|;
block|}
comment|/**      * The search shard target the failure occured on.      */
DECL|method|shard
annotation|@
name|Nullable
specifier|public
name|SearchShardTarget
name|shard
parameter_list|()
block|{
return|return
name|this
operator|.
name|shardTarget
return|;
block|}
comment|/**      * The index the search failed on.      */
DECL|method|index
annotation|@
name|Override
specifier|public
name|String
name|index
parameter_list|()
block|{
if|if
condition|(
name|shardTarget
operator|!=
literal|null
condition|)
block|{
return|return
name|shardTarget
operator|.
name|index
argument_list|()
return|;
block|}
return|return
literal|null
return|;
block|}
comment|/**      * The shard id the search failed on.      */
DECL|method|shardId
annotation|@
name|Override
specifier|public
name|int
name|shardId
parameter_list|()
block|{
if|if
condition|(
name|shardTarget
operator|!=
literal|null
condition|)
block|{
return|return
name|shardTarget
operator|.
name|shardId
argument_list|()
return|;
block|}
return|return
operator|-
literal|1
return|;
block|}
comment|/**      * The reason of the failure.      */
DECL|method|reason
specifier|public
name|String
name|reason
parameter_list|()
block|{
return|return
name|this
operator|.
name|reason
return|;
block|}
DECL|method|toString
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"Search Failure Shard "
operator|+
name|shardTarget
operator|+
literal|", reason ["
operator|+
name|reason
operator|+
literal|"]"
return|;
block|}
DECL|method|readShardSearchFailure
specifier|public
specifier|static
name|ShardSearchFailure
name|readShardSearchFailure
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|ShardSearchFailure
name|shardSearchFailure
init|=
operator|new
name|ShardSearchFailure
argument_list|()
decl_stmt|;
name|shardSearchFailure
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
return|return
name|shardSearchFailure
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
if|if
condition|(
name|in
operator|.
name|readBoolean
argument_list|()
condition|)
block|{
name|shardTarget
operator|=
name|readSearchShardTarget
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
name|reason
operator|=
name|in
operator|.
name|readUTF
argument_list|()
expr_stmt|;
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
if|if
condition|(
name|shardTarget
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
name|shardTarget
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
name|out
operator|.
name|writeUTF
argument_list|(
name|reason
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

