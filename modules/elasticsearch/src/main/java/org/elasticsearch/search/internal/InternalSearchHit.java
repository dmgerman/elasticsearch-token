begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.internal
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|internal
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableMap
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|search
operator|.
name|Explanation
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
name|search
operator|.
name|SearchHitField
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
name|Unicode
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
name|json
operator|.
name|JsonBuilder
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutput
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
name|Iterator
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

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|internal
operator|.
name|InternalSearchHitField
operator|.
name|*
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|lucene
operator|.
name|Lucene
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * @author kimchy (Shay Banon)  */
end_comment

begin_class
DECL|class|InternalSearchHit
specifier|public
class|class
name|InternalSearchHit
implements|implements
name|SearchHit
block|{
DECL|field|id
specifier|private
name|String
name|id
decl_stmt|;
DECL|field|type
specifier|private
name|String
name|type
decl_stmt|;
DECL|field|source
specifier|private
name|byte
index|[]
name|source
decl_stmt|;
DECL|field|fields
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|SearchHitField
argument_list|>
name|fields
decl_stmt|;
DECL|field|explanation
specifier|private
name|Explanation
name|explanation
decl_stmt|;
DECL|field|shard
annotation|@
name|Nullable
specifier|private
name|SearchShardTarget
name|shard
decl_stmt|;
DECL|method|InternalSearchHit
specifier|private
name|InternalSearchHit
parameter_list|()
block|{      }
DECL|method|InternalSearchHit
specifier|public
name|InternalSearchHit
parameter_list|(
name|String
name|id
parameter_list|,
name|String
name|type
parameter_list|,
name|byte
index|[]
name|source
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|SearchHitField
argument_list|>
name|fields
parameter_list|)
block|{
name|this
operator|.
name|id
operator|=
name|id
expr_stmt|;
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
name|this
operator|.
name|source
operator|=
name|source
expr_stmt|;
name|this
operator|.
name|fields
operator|=
name|fields
expr_stmt|;
block|}
DECL|method|index
annotation|@
name|Override
specifier|public
name|String
name|index
parameter_list|()
block|{
return|return
name|shard
operator|.
name|index
argument_list|()
return|;
block|}
DECL|method|id
annotation|@
name|Override
specifier|public
name|String
name|id
parameter_list|()
block|{
return|return
name|id
return|;
block|}
DECL|method|type
annotation|@
name|Override
specifier|public
name|String
name|type
parameter_list|()
block|{
return|return
name|type
return|;
block|}
DECL|method|source
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|source
parameter_list|()
block|{
return|return
name|source
return|;
block|}
DECL|method|sourceAsString
annotation|@
name|Override
specifier|public
name|String
name|sourceAsString
parameter_list|()
block|{
if|if
condition|(
name|source
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|Unicode
operator|.
name|fromBytes
argument_list|(
name|source
argument_list|)
return|;
block|}
DECL|method|iterator
annotation|@
name|Override
specifier|public
name|Iterator
argument_list|<
name|SearchHitField
argument_list|>
name|iterator
parameter_list|()
block|{
return|return
name|fields
operator|.
name|values
argument_list|()
operator|.
name|iterator
argument_list|()
return|;
block|}
DECL|method|fields
annotation|@
name|Override
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|SearchHitField
argument_list|>
name|fields
parameter_list|()
block|{
return|return
name|fields
return|;
block|}
DECL|method|fields
specifier|public
name|void
name|fields
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|SearchHitField
argument_list|>
name|fields
parameter_list|)
block|{
name|this
operator|.
name|fields
operator|=
name|fields
expr_stmt|;
block|}
DECL|method|explanation
annotation|@
name|Override
specifier|public
name|Explanation
name|explanation
parameter_list|()
block|{
return|return
name|explanation
return|;
block|}
DECL|method|explanation
specifier|public
name|void
name|explanation
parameter_list|(
name|Explanation
name|explanation
parameter_list|)
block|{
name|this
operator|.
name|explanation
operator|=
name|explanation
expr_stmt|;
block|}
DECL|method|shard
specifier|public
name|SearchShardTarget
name|shard
parameter_list|()
block|{
return|return
name|shard
return|;
block|}
DECL|method|shard
specifier|public
name|void
name|shard
parameter_list|(
name|SearchShardTarget
name|target
parameter_list|)
block|{
name|this
operator|.
name|shard
operator|=
name|target
expr_stmt|;
block|}
DECL|method|target
annotation|@
name|Override
specifier|public
name|SearchShardTarget
name|target
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
DECL|method|toJson
annotation|@
name|Override
specifier|public
name|void
name|toJson
parameter_list|(
name|JsonBuilder
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
name|builder
operator|.
name|field
argument_list|(
literal|"_index"
argument_list|,
name|shard
operator|.
name|index
argument_list|()
argument_list|)
expr_stmt|;
comment|//        builder.field("_shard", shard.shardId());
comment|//        builder.field("_node", shard.nodeId());
name|builder
operator|.
name|field
argument_list|(
literal|"_type"
argument_list|,
name|type
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"_id"
argument_list|,
name|id
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|source
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|raw
argument_list|(
literal|", \"_source\" : "
argument_list|)
expr_stmt|;
name|builder
operator|.
name|raw
argument_list|(
name|source
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|fields
argument_list|()
operator|!=
literal|null
operator|&&
operator|!
name|fields
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|builder
operator|.
name|startObject
argument_list|(
literal|"fields"
argument_list|)
expr_stmt|;
for|for
control|(
name|SearchHitField
name|field
range|:
name|fields
argument_list|()
operator|.
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|field
operator|.
name|values
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
continue|continue;
block|}
if|if
condition|(
name|field
operator|.
name|values
argument_list|()
operator|.
name|size
argument_list|()
operator|==
literal|1
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|field
operator|.
name|name
argument_list|()
argument_list|,
name|field
operator|.
name|values
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|builder
operator|.
name|field
argument_list|(
name|field
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|startArray
argument_list|()
expr_stmt|;
for|for
control|(
name|Object
name|value
range|:
name|field
operator|.
name|values
argument_list|()
control|)
block|{
name|builder
operator|.
name|value
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endArray
argument_list|()
expr_stmt|;
block|}
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|explanation
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"_explanation"
argument_list|)
expr_stmt|;
name|buildExplanation
argument_list|(
name|builder
argument_list|,
name|explanation
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
DECL|method|buildExplanation
specifier|private
name|void
name|buildExplanation
parameter_list|(
name|JsonBuilder
name|builder
parameter_list|,
name|Explanation
name|explanation
parameter_list|)
throws|throws
name|IOException
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
literal|"value"
argument_list|,
name|explanation
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"description"
argument_list|,
name|explanation
operator|.
name|getDescription
argument_list|()
argument_list|)
expr_stmt|;
name|Explanation
index|[]
name|innerExps
init|=
name|explanation
operator|.
name|getDetails
argument_list|()
decl_stmt|;
if|if
condition|(
name|innerExps
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|startArray
argument_list|(
literal|"details"
argument_list|)
expr_stmt|;
for|for
control|(
name|Explanation
name|exp
range|:
name|innerExps
control|)
block|{
name|buildExplanation
argument_list|(
name|builder
argument_list|,
name|exp
argument_list|)
expr_stmt|;
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
DECL|method|readSearchHit
specifier|public
specifier|static
name|InternalSearchHit
name|readSearchHit
parameter_list|(
name|DataInput
name|in
parameter_list|)
throws|throws
name|IOException
throws|,
name|ClassNotFoundException
block|{
name|InternalSearchHit
name|hit
init|=
operator|new
name|InternalSearchHit
argument_list|()
decl_stmt|;
name|hit
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
return|return
name|hit
return|;
block|}
DECL|method|readFrom
annotation|@
name|Override
specifier|public
name|void
name|readFrom
parameter_list|(
name|DataInput
name|in
parameter_list|)
throws|throws
name|IOException
throws|,
name|ClassNotFoundException
block|{
name|id
operator|=
name|in
operator|.
name|readUTF
argument_list|()
expr_stmt|;
name|type
operator|=
name|in
operator|.
name|readUTF
argument_list|()
expr_stmt|;
name|int
name|size
init|=
name|in
operator|.
name|readInt
argument_list|()
decl_stmt|;
if|if
condition|(
name|size
operator|>
literal|0
condition|)
block|{
name|source
operator|=
operator|new
name|byte
index|[
name|size
index|]
expr_stmt|;
name|in
operator|.
name|readFully
argument_list|(
name|source
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|in
operator|.
name|readBoolean
argument_list|()
condition|)
block|{
name|explanation
operator|=
name|readExplanation
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
name|size
operator|=
name|in
operator|.
name|readInt
argument_list|()
expr_stmt|;
if|if
condition|(
name|size
operator|==
literal|0
condition|)
block|{
name|fields
operator|=
name|ImmutableMap
operator|.
name|of
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|size
operator|==
literal|1
condition|)
block|{
name|SearchHitField
name|hitField
init|=
name|readSearchHitField
argument_list|(
name|in
argument_list|)
decl_stmt|;
name|fields
operator|=
name|ImmutableMap
operator|.
name|of
argument_list|(
name|hitField
operator|.
name|name
argument_list|()
argument_list|,
name|hitField
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|size
operator|==
literal|2
condition|)
block|{
name|SearchHitField
name|hitField1
init|=
name|readSearchHitField
argument_list|(
name|in
argument_list|)
decl_stmt|;
name|SearchHitField
name|hitField2
init|=
name|readSearchHitField
argument_list|(
name|in
argument_list|)
decl_stmt|;
name|fields
operator|=
name|ImmutableMap
operator|.
name|of
argument_list|(
name|hitField1
operator|.
name|name
argument_list|()
argument_list|,
name|hitField1
argument_list|,
name|hitField2
operator|.
name|name
argument_list|()
argument_list|,
name|hitField2
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|size
operator|==
literal|3
condition|)
block|{
name|SearchHitField
name|hitField1
init|=
name|readSearchHitField
argument_list|(
name|in
argument_list|)
decl_stmt|;
name|SearchHitField
name|hitField2
init|=
name|readSearchHitField
argument_list|(
name|in
argument_list|)
decl_stmt|;
name|SearchHitField
name|hitField3
init|=
name|readSearchHitField
argument_list|(
name|in
argument_list|)
decl_stmt|;
name|fields
operator|=
name|ImmutableMap
operator|.
name|of
argument_list|(
name|hitField1
operator|.
name|name
argument_list|()
argument_list|,
name|hitField1
argument_list|,
name|hitField2
operator|.
name|name
argument_list|()
argument_list|,
name|hitField2
argument_list|,
name|hitField3
operator|.
name|name
argument_list|()
argument_list|,
name|hitField3
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|size
operator|==
literal|4
condition|)
block|{
name|SearchHitField
name|hitField1
init|=
name|readSearchHitField
argument_list|(
name|in
argument_list|)
decl_stmt|;
name|SearchHitField
name|hitField2
init|=
name|readSearchHitField
argument_list|(
name|in
argument_list|)
decl_stmt|;
name|SearchHitField
name|hitField3
init|=
name|readSearchHitField
argument_list|(
name|in
argument_list|)
decl_stmt|;
name|SearchHitField
name|hitField4
init|=
name|readSearchHitField
argument_list|(
name|in
argument_list|)
decl_stmt|;
name|fields
operator|=
name|ImmutableMap
operator|.
name|of
argument_list|(
name|hitField1
operator|.
name|name
argument_list|()
argument_list|,
name|hitField1
argument_list|,
name|hitField2
operator|.
name|name
argument_list|()
argument_list|,
name|hitField2
argument_list|,
name|hitField3
operator|.
name|name
argument_list|()
argument_list|,
name|hitField3
argument_list|,
name|hitField4
operator|.
name|name
argument_list|()
argument_list|,
name|hitField4
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|size
operator|==
literal|5
condition|)
block|{
name|SearchHitField
name|hitField1
init|=
name|readSearchHitField
argument_list|(
name|in
argument_list|)
decl_stmt|;
name|SearchHitField
name|hitField2
init|=
name|readSearchHitField
argument_list|(
name|in
argument_list|)
decl_stmt|;
name|SearchHitField
name|hitField3
init|=
name|readSearchHitField
argument_list|(
name|in
argument_list|)
decl_stmt|;
name|SearchHitField
name|hitField4
init|=
name|readSearchHitField
argument_list|(
name|in
argument_list|)
decl_stmt|;
name|SearchHitField
name|hitField5
init|=
name|readSearchHitField
argument_list|(
name|in
argument_list|)
decl_stmt|;
name|fields
operator|=
name|ImmutableMap
operator|.
name|of
argument_list|(
name|hitField1
operator|.
name|name
argument_list|()
argument_list|,
name|hitField1
argument_list|,
name|hitField2
operator|.
name|name
argument_list|()
argument_list|,
name|hitField2
argument_list|,
name|hitField3
operator|.
name|name
argument_list|()
argument_list|,
name|hitField3
argument_list|,
name|hitField4
operator|.
name|name
argument_list|()
argument_list|,
name|hitField4
argument_list|,
name|hitField5
operator|.
name|name
argument_list|()
argument_list|,
name|hitField5
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|ImmutableMap
operator|.
name|Builder
argument_list|<
name|String
argument_list|,
name|SearchHitField
argument_list|>
name|builder
init|=
name|ImmutableMap
operator|.
name|builder
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
name|SearchHitField
name|hitField
init|=
name|readSearchHitField
argument_list|(
name|in
argument_list|)
decl_stmt|;
name|builder
operator|.
name|put
argument_list|(
name|hitField
operator|.
name|name
argument_list|()
argument_list|,
name|hitField
argument_list|)
expr_stmt|;
block|}
name|fields
operator|=
name|builder
operator|.
name|build
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|in
operator|.
name|readBoolean
argument_list|()
condition|)
block|{
name|shard
operator|=
name|readSearchShardTarget
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|writeTo
annotation|@
name|Override
specifier|public
name|void
name|writeTo
parameter_list|(
name|DataOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeUTF
argument_list|(
name|id
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeUTF
argument_list|(
name|type
argument_list|)
expr_stmt|;
if|if
condition|(
name|source
operator|==
literal|null
condition|)
block|{
name|out
operator|.
name|writeInt
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|writeInt
argument_list|(
name|source
operator|.
name|length
argument_list|)
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
name|source
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|explanation
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
name|writeExplanation
argument_list|(
name|out
argument_list|,
name|explanation
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|fields
operator|==
literal|null
condition|)
block|{
name|out
operator|.
name|writeInt
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|writeInt
argument_list|(
name|fields
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|SearchHitField
name|hitField
range|:
name|fields
argument_list|()
operator|.
name|values
argument_list|()
control|)
block|{
name|hitField
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|shard
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
name|shard
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

