begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.percolate
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|percolate
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
name|ImmutableList
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
name|util
operator|.
name|BytesRef
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
name|BroadcastShardOperationResponse
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
name|percolator
operator|.
name|PercolateContext
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
name|aggregations
operator|.
name|InternalAggregations
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
name|facet
operator|.
name|InternalFacets
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
name|highlight
operator|.
name|HighlightField
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
name|query
operator|.
name|QuerySearchResult
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
comment|/**  */
end_comment

begin_class
DECL|class|PercolateShardResponse
specifier|public
class|class
name|PercolateShardResponse
extends|extends
name|BroadcastShardOperationResponse
block|{
DECL|field|EMPTY_MATCHES
specifier|private
specifier|static
specifier|final
name|BytesRef
index|[]
name|EMPTY_MATCHES
init|=
operator|new
name|BytesRef
index|[
literal|0
index|]
decl_stmt|;
DECL|field|EMPTY_SCORES
specifier|private
specifier|static
specifier|final
name|float
index|[]
name|EMPTY_SCORES
init|=
operator|new
name|float
index|[
literal|0
index|]
decl_stmt|;
DECL|field|EMPTY_HL
specifier|private
specifier|static
specifier|final
name|List
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|HighlightField
argument_list|>
argument_list|>
name|EMPTY_HL
init|=
name|ImmutableList
operator|.
name|of
argument_list|()
decl_stmt|;
DECL|field|count
specifier|private
name|long
name|count
decl_stmt|;
DECL|field|scores
specifier|private
name|float
index|[]
name|scores
decl_stmt|;
DECL|field|matches
specifier|private
name|BytesRef
index|[]
name|matches
decl_stmt|;
DECL|field|hls
specifier|private
name|List
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|HighlightField
argument_list|>
argument_list|>
name|hls
decl_stmt|;
DECL|field|percolatorTypeId
specifier|private
name|byte
name|percolatorTypeId
decl_stmt|;
DECL|field|requestedSize
specifier|private
name|int
name|requestedSize
decl_stmt|;
DECL|field|facets
specifier|private
name|InternalFacets
name|facets
decl_stmt|;
DECL|field|aggregations
specifier|private
name|InternalAggregations
name|aggregations
decl_stmt|;
DECL|method|PercolateShardResponse
name|PercolateShardResponse
parameter_list|()
block|{
name|hls
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
block|}
DECL|method|PercolateShardResponse
specifier|public
name|PercolateShardResponse
parameter_list|(
name|BytesRef
index|[]
name|matches
parameter_list|,
name|List
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|HighlightField
argument_list|>
argument_list|>
name|hls
parameter_list|,
name|long
name|count
parameter_list|,
name|float
index|[]
name|scores
parameter_list|,
name|PercolateContext
name|context
parameter_list|,
name|String
name|index
parameter_list|,
name|int
name|shardId
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
name|matches
operator|=
name|matches
expr_stmt|;
name|this
operator|.
name|hls
operator|=
name|hls
expr_stmt|;
name|this
operator|.
name|count
operator|=
name|count
expr_stmt|;
name|this
operator|.
name|scores
operator|=
name|scores
expr_stmt|;
name|this
operator|.
name|percolatorTypeId
operator|=
name|context
operator|.
name|percolatorTypeId
expr_stmt|;
name|this
operator|.
name|requestedSize
operator|=
name|context
operator|.
name|size
argument_list|()
expr_stmt|;
name|QuerySearchResult
name|result
init|=
name|context
operator|.
name|queryResult
argument_list|()
decl_stmt|;
if|if
condition|(
name|result
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|result
operator|.
name|facets
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|facets
operator|=
operator|new
name|InternalFacets
argument_list|(
name|result
operator|.
name|facets
argument_list|()
operator|.
name|facets
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|result
operator|.
name|aggregations
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|aggregations
operator|=
operator|(
name|InternalAggregations
operator|)
name|result
operator|.
name|aggregations
argument_list|()
expr_stmt|;
block|}
block|}
block|}
DECL|method|PercolateShardResponse
specifier|public
name|PercolateShardResponse
parameter_list|(
name|BytesRef
index|[]
name|matches
parameter_list|,
name|long
name|count
parameter_list|,
name|float
index|[]
name|scores
parameter_list|,
name|PercolateContext
name|context
parameter_list|,
name|String
name|index
parameter_list|,
name|int
name|shardId
parameter_list|)
block|{
name|this
argument_list|(
name|matches
argument_list|,
name|EMPTY_HL
argument_list|,
name|count
argument_list|,
name|scores
argument_list|,
name|context
argument_list|,
name|index
argument_list|,
name|shardId
argument_list|)
expr_stmt|;
block|}
DECL|method|PercolateShardResponse
specifier|public
name|PercolateShardResponse
parameter_list|(
name|BytesRef
index|[]
name|matches
parameter_list|,
name|List
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|HighlightField
argument_list|>
argument_list|>
name|hls
parameter_list|,
name|long
name|count
parameter_list|,
name|PercolateContext
name|context
parameter_list|,
name|String
name|index
parameter_list|,
name|int
name|shardId
parameter_list|)
block|{
name|this
argument_list|(
name|matches
argument_list|,
name|hls
argument_list|,
name|count
argument_list|,
name|EMPTY_SCORES
argument_list|,
name|context
argument_list|,
name|index
argument_list|,
name|shardId
argument_list|)
expr_stmt|;
block|}
DECL|method|PercolateShardResponse
specifier|public
name|PercolateShardResponse
parameter_list|(
name|long
name|count
parameter_list|,
name|PercolateContext
name|context
parameter_list|,
name|String
name|index
parameter_list|,
name|int
name|shardId
parameter_list|)
block|{
name|this
argument_list|(
name|EMPTY_MATCHES
argument_list|,
name|EMPTY_HL
argument_list|,
name|count
argument_list|,
name|EMPTY_SCORES
argument_list|,
name|context
argument_list|,
name|index
argument_list|,
name|shardId
argument_list|)
expr_stmt|;
block|}
DECL|method|PercolateShardResponse
specifier|public
name|PercolateShardResponse
parameter_list|(
name|PercolateContext
name|context
parameter_list|,
name|String
name|index
parameter_list|,
name|int
name|shardId
parameter_list|)
block|{
name|this
argument_list|(
name|EMPTY_MATCHES
argument_list|,
name|EMPTY_HL
argument_list|,
literal|0
argument_list|,
name|EMPTY_SCORES
argument_list|,
name|context
argument_list|,
name|index
argument_list|,
name|shardId
argument_list|)
expr_stmt|;
block|}
DECL|method|matches
specifier|public
name|BytesRef
index|[]
name|matches
parameter_list|()
block|{
return|return
name|matches
return|;
block|}
DECL|method|scores
specifier|public
name|float
index|[]
name|scores
parameter_list|()
block|{
return|return
name|scores
return|;
block|}
DECL|method|count
specifier|public
name|long
name|count
parameter_list|()
block|{
return|return
name|count
return|;
block|}
DECL|method|requestedSize
specifier|public
name|int
name|requestedSize
parameter_list|()
block|{
return|return
name|requestedSize
return|;
block|}
DECL|method|hls
specifier|public
name|List
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|HighlightField
argument_list|>
argument_list|>
name|hls
parameter_list|()
block|{
return|return
name|hls
return|;
block|}
DECL|method|facets
specifier|public
name|InternalFacets
name|facets
parameter_list|()
block|{
return|return
name|facets
return|;
block|}
DECL|method|aggregations
specifier|public
name|InternalAggregations
name|aggregations
parameter_list|()
block|{
return|return
name|aggregations
return|;
block|}
DECL|method|percolatorTypeId
specifier|public
name|byte
name|percolatorTypeId
parameter_list|()
block|{
return|return
name|percolatorTypeId
return|;
block|}
DECL|method|isEmpty
specifier|public
name|boolean
name|isEmpty
parameter_list|()
block|{
return|return
name|percolatorTypeId
operator|==
literal|0x00
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
name|percolatorTypeId
operator|=
name|in
operator|.
name|readByte
argument_list|()
expr_stmt|;
name|requestedSize
operator|=
name|in
operator|.
name|readVInt
argument_list|()
expr_stmt|;
name|count
operator|=
name|in
operator|.
name|readVLong
argument_list|()
expr_stmt|;
name|matches
operator|=
operator|new
name|BytesRef
index|[
name|in
operator|.
name|readVInt
argument_list|()
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
name|matches
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|matches
index|[
name|i
index|]
operator|=
name|in
operator|.
name|readBytesRef
argument_list|()
expr_stmt|;
block|}
name|scores
operator|=
operator|new
name|float
index|[
name|in
operator|.
name|readVInt
argument_list|()
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
name|scores
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|scores
index|[
name|i
index|]
operator|=
name|in
operator|.
name|readFloat
argument_list|()
expr_stmt|;
block|}
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
name|int
name|mSize
init|=
name|in
operator|.
name|readVInt
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|HighlightField
argument_list|>
name|fields
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
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
name|mSize
condition|;
name|j
operator|++
control|)
block|{
name|fields
operator|.
name|put
argument_list|(
name|in
operator|.
name|readString
argument_list|()
argument_list|,
name|HighlightField
operator|.
name|readHighlightField
argument_list|(
name|in
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|hls
operator|.
name|add
argument_list|(
name|fields
argument_list|)
expr_stmt|;
block|}
name|facets
operator|=
name|InternalFacets
operator|.
name|readOptionalFacets
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|aggregations
operator|=
name|InternalAggregations
operator|.
name|readOptionalAggregations
argument_list|(
name|in
argument_list|)
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
name|out
operator|.
name|writeByte
argument_list|(
name|percolatorTypeId
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVLong
argument_list|(
name|requestedSize
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVLong
argument_list|(
name|count
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|matches
operator|.
name|length
argument_list|)
expr_stmt|;
for|for
control|(
name|BytesRef
name|match
range|:
name|matches
control|)
block|{
name|out
operator|.
name|writeBytesRef
argument_list|(
name|match
argument_list|)
expr_stmt|;
block|}
name|out
operator|.
name|writeVLong
argument_list|(
name|scores
operator|.
name|length
argument_list|)
expr_stmt|;
for|for
control|(
name|float
name|score
range|:
name|scores
control|)
block|{
name|out
operator|.
name|writeFloat
argument_list|(
name|score
argument_list|)
expr_stmt|;
block|}
name|out
operator|.
name|writeVInt
argument_list|(
name|hls
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Map
argument_list|<
name|String
argument_list|,
name|HighlightField
argument_list|>
name|hl
range|:
name|hls
control|)
block|{
name|out
operator|.
name|writeVInt
argument_list|(
name|hl
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
name|HighlightField
argument_list|>
name|entry
range|:
name|hl
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
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
block|}
name|out
operator|.
name|writeOptionalStreamable
argument_list|(
name|facets
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeOptionalStreamable
argument_list|(
name|aggregations
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

