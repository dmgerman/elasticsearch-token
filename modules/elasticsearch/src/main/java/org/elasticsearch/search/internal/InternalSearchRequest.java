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
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|search
operator|.
name|SearchType
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
name|Bytes
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
name|search
operator|.
name|Scroll
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
name|common
operator|.
name|unit
operator|.
name|TimeValue
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
name|Scroll
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * Source structure:  *<p/>  *<pre>  * {  *  from : 0, size : 20, (optional, can be set on the request)  *  sort : { "name.first" : {}, "name.last" : { reverse : true } }  *  fields : [ "name.first", "name.last" ]  *  query : { ... }  *  facets : {  *      "facet1" : {  *          query : { ... }  *      }  *  }  * }  *</pre>  *  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|InternalSearchRequest
specifier|public
class|class
name|InternalSearchRequest
implements|implements
name|Streamable
block|{
DECL|field|index
specifier|private
name|String
name|index
decl_stmt|;
DECL|field|shardId
specifier|private
name|int
name|shardId
decl_stmt|;
DECL|field|numberOfShards
specifier|private
name|int
name|numberOfShards
decl_stmt|;
DECL|field|searchType
specifier|private
name|SearchType
name|searchType
decl_stmt|;
DECL|field|scroll
specifier|private
name|Scroll
name|scroll
decl_stmt|;
DECL|field|timeout
specifier|private
name|TimeValue
name|timeout
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
specifier|private
name|String
index|[]
name|filteringAliases
decl_stmt|;
DECL|field|source
specifier|private
name|byte
index|[]
name|source
decl_stmt|;
DECL|field|sourceOffset
specifier|private
name|int
name|sourceOffset
decl_stmt|;
DECL|field|sourceLength
specifier|private
name|int
name|sourceLength
decl_stmt|;
DECL|field|extraSource
specifier|private
name|byte
index|[]
name|extraSource
decl_stmt|;
DECL|field|extraSourceOffset
specifier|private
name|int
name|extraSourceOffset
decl_stmt|;
DECL|field|extraSourceLength
specifier|private
name|int
name|extraSourceLength
decl_stmt|;
DECL|method|InternalSearchRequest
specifier|public
name|InternalSearchRequest
parameter_list|()
block|{     }
DECL|method|InternalSearchRequest
specifier|public
name|InternalSearchRequest
parameter_list|(
name|ShardRouting
name|shardRouting
parameter_list|,
name|int
name|numberOfShards
parameter_list|,
name|SearchType
name|searchType
parameter_list|)
block|{
name|this
argument_list|(
name|shardRouting
operator|.
name|index
argument_list|()
argument_list|,
name|shardRouting
operator|.
name|id
argument_list|()
argument_list|,
name|numberOfShards
argument_list|,
name|searchType
argument_list|)
expr_stmt|;
block|}
DECL|method|InternalSearchRequest
specifier|public
name|InternalSearchRequest
parameter_list|(
name|String
name|index
parameter_list|,
name|int
name|shardId
parameter_list|,
name|int
name|numberOfShards
parameter_list|,
name|SearchType
name|searchType
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
name|numberOfShards
operator|=
name|numberOfShards
expr_stmt|;
name|this
operator|.
name|searchType
operator|=
name|searchType
expr_stmt|;
block|}
DECL|method|index
specifier|public
name|String
name|index
parameter_list|()
block|{
return|return
name|index
return|;
block|}
DECL|method|shardId
specifier|public
name|int
name|shardId
parameter_list|()
block|{
return|return
name|shardId
return|;
block|}
DECL|method|searchType
specifier|public
name|SearchType
name|searchType
parameter_list|()
block|{
return|return
name|this
operator|.
name|searchType
return|;
block|}
DECL|method|numberOfShards
specifier|public
name|int
name|numberOfShards
parameter_list|()
block|{
return|return
name|numberOfShards
return|;
block|}
DECL|method|source
specifier|public
name|byte
index|[]
name|source
parameter_list|()
block|{
return|return
name|this
operator|.
name|source
return|;
block|}
DECL|method|sourceOffset
specifier|public
name|int
name|sourceOffset
parameter_list|()
block|{
return|return
name|sourceOffset
return|;
block|}
DECL|method|sourceLength
specifier|public
name|int
name|sourceLength
parameter_list|()
block|{
return|return
name|sourceLength
return|;
block|}
DECL|method|extraSource
specifier|public
name|byte
index|[]
name|extraSource
parameter_list|()
block|{
return|return
name|this
operator|.
name|extraSource
return|;
block|}
DECL|method|extraSourceOffset
specifier|public
name|int
name|extraSourceOffset
parameter_list|()
block|{
return|return
name|extraSourceOffset
return|;
block|}
DECL|method|extraSourceLength
specifier|public
name|int
name|extraSourceLength
parameter_list|()
block|{
return|return
name|extraSourceLength
return|;
block|}
DECL|method|source
specifier|public
name|InternalSearchRequest
name|source
parameter_list|(
name|byte
index|[]
name|source
parameter_list|)
block|{
return|return
name|source
argument_list|(
name|source
argument_list|,
literal|0
argument_list|,
name|source
operator|.
name|length
argument_list|)
return|;
block|}
DECL|method|source
specifier|public
name|InternalSearchRequest
name|source
parameter_list|(
name|byte
index|[]
name|source
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
block|{
name|this
operator|.
name|source
operator|=
name|source
expr_stmt|;
name|this
operator|.
name|sourceOffset
operator|=
name|offset
expr_stmt|;
name|this
operator|.
name|sourceLength
operator|=
name|length
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|extraSource
specifier|public
name|InternalSearchRequest
name|extraSource
parameter_list|(
name|byte
index|[]
name|extraSource
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
block|{
name|this
operator|.
name|extraSource
operator|=
name|extraSource
expr_stmt|;
name|this
operator|.
name|extraSourceOffset
operator|=
name|offset
expr_stmt|;
name|this
operator|.
name|extraSourceLength
operator|=
name|length
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|scroll
specifier|public
name|Scroll
name|scroll
parameter_list|()
block|{
return|return
name|scroll
return|;
block|}
DECL|method|scroll
specifier|public
name|InternalSearchRequest
name|scroll
parameter_list|(
name|Scroll
name|scroll
parameter_list|)
block|{
name|this
operator|.
name|scroll
operator|=
name|scroll
expr_stmt|;
return|return
name|this
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
DECL|method|timeout
specifier|public
name|InternalSearchRequest
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
name|this
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
DECL|method|filteringAliases
specifier|public
name|void
name|filteringAliases
parameter_list|(
name|String
index|[]
name|filteringAliases
parameter_list|)
block|{
name|this
operator|.
name|filteringAliases
operator|=
name|filteringAliases
expr_stmt|;
block|}
DECL|method|types
specifier|public
name|String
index|[]
name|types
parameter_list|()
block|{
return|return
name|types
return|;
block|}
DECL|method|types
specifier|public
name|void
name|types
parameter_list|(
name|String
index|[]
name|types
parameter_list|)
block|{
name|this
operator|.
name|types
operator|=
name|types
expr_stmt|;
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
name|in
operator|.
name|readUTF
argument_list|()
expr_stmt|;
name|shardId
operator|=
name|in
operator|.
name|readVInt
argument_list|()
expr_stmt|;
name|searchType
operator|=
name|SearchType
operator|.
name|fromId
argument_list|(
name|in
operator|.
name|readByte
argument_list|()
argument_list|)
expr_stmt|;
name|numberOfShards
operator|=
name|in
operator|.
name|readVInt
argument_list|()
expr_stmt|;
if|if
condition|(
name|in
operator|.
name|readBoolean
argument_list|()
condition|)
block|{
name|scroll
operator|=
name|readScroll
argument_list|(
name|in
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
name|timeout
operator|=
name|readTimeValue
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
name|sourceOffset
operator|=
literal|0
expr_stmt|;
name|sourceLength
operator|=
name|in
operator|.
name|readVInt
argument_list|()
expr_stmt|;
if|if
condition|(
name|sourceLength
operator|==
literal|0
condition|)
block|{
name|source
operator|=
name|Bytes
operator|.
name|EMPTY_ARRAY
expr_stmt|;
block|}
else|else
block|{
name|source
operator|=
operator|new
name|byte
index|[
name|sourceLength
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
name|extraSourceOffset
operator|=
literal|0
expr_stmt|;
name|extraSourceLength
operator|=
name|in
operator|.
name|readVInt
argument_list|()
expr_stmt|;
if|if
condition|(
name|extraSourceLength
operator|==
literal|0
condition|)
block|{
name|extraSource
operator|=
name|Bytes
operator|.
name|EMPTY_ARRAY
expr_stmt|;
block|}
else|else
block|{
name|extraSource
operator|=
operator|new
name|byte
index|[
name|extraSourceLength
index|]
expr_stmt|;
name|in
operator|.
name|readFully
argument_list|(
name|extraSource
argument_list|)
expr_stmt|;
block|}
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
name|indicesSize
init|=
name|in
operator|.
name|readVInt
argument_list|()
decl_stmt|;
if|if
condition|(
name|indicesSize
operator|>
literal|0
condition|)
block|{
name|filteringAliases
operator|=
operator|new
name|String
index|[
name|indicesSize
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
name|indicesSize
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
else|else
block|{
name|filteringAliases
operator|=
literal|null
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
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeUTF
argument_list|(
name|index
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|shardId
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeByte
argument_list|(
name|searchType
operator|.
name|id
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|numberOfShards
argument_list|)
expr_stmt|;
if|if
condition|(
name|scroll
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
name|scroll
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|timeout
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
name|timeout
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|source
operator|==
literal|null
condition|)
block|{
name|out
operator|.
name|writeVInt
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|writeVInt
argument_list|(
name|sourceLength
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBytes
argument_list|(
name|source
argument_list|,
name|sourceOffset
argument_list|,
name|sourceLength
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|extraSource
operator|==
literal|null
condition|)
block|{
name|out
operator|.
name|writeVInt
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|writeVInt
argument_list|(
name|extraSourceLength
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBytes
argument_list|(
name|extraSource
argument_list|,
name|extraSourceOffset
argument_list|,
name|extraSourceLength
argument_list|)
expr_stmt|;
block|}
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
name|index
range|:
name|filteringAliases
control|)
block|{
name|out
operator|.
name|writeUTF
argument_list|(
name|index
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

