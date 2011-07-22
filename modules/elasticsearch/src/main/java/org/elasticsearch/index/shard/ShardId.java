begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.shard
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|shard
package|;
end_package

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
name|concurrent
operator|.
name|Immutable
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
name|Index
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
name|io
operator|.
name|Serializable
import|;
end_import

begin_comment
comment|/**  * Allows for shard level components to be injected with the shard id.  *  * @author kimchy (Shay Banon)  */
end_comment

begin_class
annotation|@
name|Immutable
DECL|class|ShardId
specifier|public
class|class
name|ShardId
implements|implements
name|Serializable
implements|,
name|Streamable
block|{
DECL|field|index
specifier|private
name|Index
name|index
decl_stmt|;
DECL|field|shardId
specifier|private
name|int
name|shardId
decl_stmt|;
DECL|field|hashCode
specifier|private
name|int
name|hashCode
decl_stmt|;
DECL|method|ShardId
specifier|private
name|ShardId
parameter_list|()
block|{      }
DECL|method|ShardId
specifier|public
name|ShardId
parameter_list|(
name|String
name|index
parameter_list|,
name|int
name|shardId
parameter_list|)
block|{
name|this
argument_list|(
operator|new
name|Index
argument_list|(
name|index
argument_list|)
argument_list|,
name|shardId
argument_list|)
expr_stmt|;
block|}
DECL|method|ShardId
specifier|public
name|ShardId
parameter_list|(
name|Index
name|index
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
name|shardId
operator|=
name|shardId
expr_stmt|;
name|this
operator|.
name|hashCode
operator|=
name|computeHashCode
argument_list|()
expr_stmt|;
block|}
DECL|method|index
specifier|public
name|Index
name|index
parameter_list|()
block|{
return|return
name|this
operator|.
name|index
return|;
block|}
DECL|method|getIndex
specifier|public
name|String
name|getIndex
parameter_list|()
block|{
return|return
name|index
argument_list|()
operator|.
name|name
argument_list|()
return|;
block|}
DECL|method|id
specifier|public
name|int
name|id
parameter_list|()
block|{
return|return
name|this
operator|.
name|shardId
return|;
block|}
DECL|method|getId
specifier|public
name|int
name|getId
parameter_list|()
block|{
return|return
name|id
argument_list|()
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
literal|"Index Shard ["
operator|+
name|index
operator|.
name|name
argument_list|()
operator|+
literal|"]["
operator|+
name|shardId
operator|+
literal|"]"
return|;
block|}
DECL|method|equals
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|o
condition|)
return|return
literal|true
return|;
if|if
condition|(
name|o
operator|==
literal|null
condition|)
return|return
literal|false
return|;
name|ShardId
name|shardId1
init|=
operator|(
name|ShardId
operator|)
name|o
decl_stmt|;
return|return
name|shardId
operator|==
name|shardId1
operator|.
name|shardId
operator|&&
name|index
operator|.
name|name
argument_list|()
operator|.
name|equals
argument_list|(
name|shardId1
operator|.
name|index
operator|.
name|name
argument_list|()
argument_list|)
return|;
block|}
DECL|method|hashCode
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|hashCode
return|;
block|}
DECL|method|computeHashCode
specifier|private
name|int
name|computeHashCode
parameter_list|()
block|{
name|int
name|result
init|=
name|index
operator|!=
literal|null
condition|?
name|index
operator|.
name|hashCode
argument_list|()
else|:
literal|0
decl_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
name|shardId
expr_stmt|;
return|return
name|result
return|;
block|}
DECL|method|readShardId
specifier|public
specifier|static
name|ShardId
name|readShardId
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|ShardId
name|shardId
init|=
operator|new
name|ShardId
argument_list|()
decl_stmt|;
name|shardId
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
return|return
name|shardId
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
name|index
operator|=
name|Index
operator|.
name|readIndexName
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|shardId
operator|=
name|in
operator|.
name|readVInt
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
name|index
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
name|shardId
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

