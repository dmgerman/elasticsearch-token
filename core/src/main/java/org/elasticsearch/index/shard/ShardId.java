begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|cluster
operator|.
name|metadata
operator|.
name|IndexMetaData
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

begin_comment
comment|/**  * Allows for shard level components to be injected with the shard id.  */
end_comment

begin_class
DECL|class|ShardId
specifier|public
class|class
name|ShardId
implements|implements
name|Streamable
implements|,
name|Comparable
argument_list|<
name|ShardId
argument_list|>
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
block|{     }
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
DECL|method|ShardId
specifier|public
name|ShardId
parameter_list|(
name|String
name|index
parameter_list|,
name|String
name|indexUUID
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
argument_list|,
name|indexUUID
argument_list|)
argument_list|,
name|shardId
argument_list|)
expr_stmt|;
block|}
DECL|method|getIndex
specifier|public
name|Index
name|getIndex
parameter_list|()
block|{
return|return
name|index
return|;
block|}
DECL|method|getIndexName
specifier|public
name|String
name|getIndexName
parameter_list|()
block|{
return|return
name|index
operator|.
name|getName
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
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"["
operator|+
name|index
operator|.
name|getName
argument_list|()
operator|+
literal|"]["
operator|+
name|shardId
operator|+
literal|"]"
return|;
block|}
comment|/**      * Parse the string representation of this shardId back to an object.      * We lose index uuid information here, but since we use toString in      * rest responses, this is the best we can do to reconstruct the object      * on the client side.      */
DECL|method|fromString
specifier|public
specifier|static
name|ShardId
name|fromString
parameter_list|(
name|String
name|shardIdString
parameter_list|)
block|{
name|int
name|splitPosition
init|=
name|shardIdString
operator|.
name|indexOf
argument_list|(
literal|"]["
argument_list|)
decl_stmt|;
if|if
condition|(
name|splitPosition
operator|<=
literal|0
operator|||
name|shardIdString
operator|.
name|charAt
argument_list|(
literal|0
argument_list|)
operator|!=
literal|'['
operator|||
name|shardIdString
operator|.
name|charAt
argument_list|(
name|shardIdString
operator|.
name|length
argument_list|()
operator|-
literal|1
argument_list|)
operator|!=
literal|']'
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Unexpected shardId string format, expected [indexName][shardId] but got "
operator|+
name|shardIdString
argument_list|)
throw|;
block|}
name|String
name|indexName
init|=
name|shardIdString
operator|.
name|substring
argument_list|(
literal|1
argument_list|,
name|splitPosition
argument_list|)
decl_stmt|;
name|int
name|shardId
init|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|shardIdString
operator|.
name|substring
argument_list|(
name|splitPosition
operator|+
literal|2
argument_list|,
name|shardIdString
operator|.
name|length
argument_list|()
operator|-
literal|1
argument_list|)
argument_list|)
decl_stmt|;
return|return
operator|new
name|ShardId
argument_list|(
operator|new
name|Index
argument_list|(
name|indexName
argument_list|,
name|IndexMetaData
operator|.
name|INDEX_UUID_NA_VALUE
argument_list|)
argument_list|,
name|shardId
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|equals
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
name|equals
argument_list|(
name|shardId1
operator|.
name|index
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|hashCode
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
name|index
operator|=
operator|new
name|Index
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
name|hashCode
operator|=
name|computeHashCode
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
annotation|@
name|Override
DECL|method|compareTo
specifier|public
name|int
name|compareTo
parameter_list|(
name|ShardId
name|o
parameter_list|)
block|{
if|if
condition|(
name|o
operator|.
name|getId
argument_list|()
operator|==
name|shardId
condition|)
block|{
name|int
name|compare
init|=
name|index
operator|.
name|getName
argument_list|()
operator|.
name|compareTo
argument_list|(
name|o
operator|.
name|getIndex
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|compare
operator|!=
literal|0
condition|)
block|{
return|return
name|compare
return|;
block|}
return|return
name|index
operator|.
name|getUUID
argument_list|()
operator|.
name|compareTo
argument_list|(
name|o
operator|.
name|getIndex
argument_list|()
operator|.
name|getUUID
argument_list|()
argument_list|)
return|;
block|}
return|return
name|Integer
operator|.
name|compare
argument_list|(
name|shardId
argument_list|,
name|o
operator|.
name|getId
argument_list|()
argument_list|)
return|;
block|}
block|}
end_class

end_unit

