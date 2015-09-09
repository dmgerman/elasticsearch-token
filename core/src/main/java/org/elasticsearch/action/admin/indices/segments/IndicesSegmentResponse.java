begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.indices.segments
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
name|segments
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
name|Sets
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
name|Accountable
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
name|action
operator|.
name|support
operator|.
name|broadcast
operator|.
name|BroadcastResponse
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
name|unit
operator|.
name|ByteSizeValue
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
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|xcontent
operator|.
name|XContentBuilderString
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
name|engine
operator|.
name|Segment
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
name|Collection
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_class
DECL|class|IndicesSegmentResponse
specifier|public
class|class
name|IndicesSegmentResponse
extends|extends
name|BroadcastResponse
implements|implements
name|ToXContent
block|{
DECL|field|shards
specifier|private
name|ShardSegments
index|[]
name|shards
decl_stmt|;
DECL|field|indicesSegments
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|IndexSegments
argument_list|>
name|indicesSegments
decl_stmt|;
DECL|method|IndicesSegmentResponse
name|IndicesSegmentResponse
parameter_list|()
block|{      }
DECL|method|IndicesSegmentResponse
name|IndicesSegmentResponse
parameter_list|(
name|ShardSegments
index|[]
name|shards
parameter_list|,
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
name|shards
operator|=
name|shards
expr_stmt|;
block|}
DECL|method|getIndices
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|IndexSegments
argument_list|>
name|getIndices
parameter_list|()
block|{
if|if
condition|(
name|indicesSegments
operator|!=
literal|null
condition|)
block|{
return|return
name|indicesSegments
return|;
block|}
name|Map
argument_list|<
name|String
argument_list|,
name|IndexSegments
argument_list|>
name|indicesSegments
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|indices
init|=
name|Sets
operator|.
name|newHashSet
argument_list|()
decl_stmt|;
for|for
control|(
name|ShardSegments
name|shard
range|:
name|shards
control|)
block|{
name|indices
operator|.
name|add
argument_list|(
name|shard
operator|.
name|getShardRouting
argument_list|()
operator|.
name|getIndex
argument_list|()
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|String
name|index
range|:
name|indices
control|)
block|{
name|List
argument_list|<
name|ShardSegments
argument_list|>
name|shards
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|ShardSegments
name|shard
range|:
name|this
operator|.
name|shards
control|)
block|{
if|if
condition|(
name|shard
operator|.
name|getShardRouting
argument_list|()
operator|.
name|index
argument_list|()
operator|.
name|equals
argument_list|(
name|index
argument_list|)
condition|)
block|{
name|shards
operator|.
name|add
argument_list|(
name|shard
argument_list|)
expr_stmt|;
block|}
block|}
name|indicesSegments
operator|.
name|put
argument_list|(
name|index
argument_list|,
operator|new
name|IndexSegments
argument_list|(
name|index
argument_list|,
name|shards
operator|.
name|toArray
argument_list|(
operator|new
name|ShardSegments
index|[
name|shards
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|indicesSegments
operator|=
name|indicesSegments
expr_stmt|;
return|return
name|indicesSegments
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
name|shards
operator|=
operator|new
name|ShardSegments
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
name|shards
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|shards
index|[
name|i
index|]
operator|=
name|ShardSegments
operator|.
name|readShardSegments
argument_list|(
name|in
argument_list|)
expr_stmt|;
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
name|shards
operator|.
name|length
argument_list|)
expr_stmt|;
for|for
control|(
name|ShardSegments
name|shard
range|:
name|shards
control|)
block|{
name|shard
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
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
name|builder
operator|.
name|startObject
argument_list|(
name|Fields
operator|.
name|INDICES
argument_list|)
expr_stmt|;
for|for
control|(
name|IndexSegments
name|indexSegments
range|:
name|getIndices
argument_list|()
operator|.
name|values
argument_list|()
control|)
block|{
name|builder
operator|.
name|startObject
argument_list|(
name|indexSegments
operator|.
name|getIndex
argument_list|()
argument_list|,
name|XContentBuilder
operator|.
name|FieldCaseConversion
operator|.
name|NONE
argument_list|)
expr_stmt|;
name|builder
operator|.
name|startObject
argument_list|(
name|Fields
operator|.
name|SHARDS
argument_list|)
expr_stmt|;
for|for
control|(
name|IndexShardSegments
name|indexSegment
range|:
name|indexSegments
control|)
block|{
name|builder
operator|.
name|startArray
argument_list|(
name|Integer
operator|.
name|toString
argument_list|(
name|indexSegment
operator|.
name|getShardId
argument_list|()
operator|.
name|id
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|ShardSegments
name|shardSegments
range|:
name|indexSegment
control|)
block|{
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|startObject
argument_list|(
name|Fields
operator|.
name|ROUTING
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|STATE
argument_list|,
name|shardSegments
operator|.
name|getShardRouting
argument_list|()
operator|.
name|state
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|PRIMARY
argument_list|,
name|shardSegments
operator|.
name|getShardRouting
argument_list|()
operator|.
name|primary
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|NODE
argument_list|,
name|shardSegments
operator|.
name|getShardRouting
argument_list|()
operator|.
name|currentNodeId
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|shardSegments
operator|.
name|getShardRouting
argument_list|()
operator|.
name|relocatingNodeId
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|RELOCATING_NODE
argument_list|,
name|shardSegments
operator|.
name|getShardRouting
argument_list|()
operator|.
name|relocatingNodeId
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|NUM_COMMITTED_SEGMENTS
argument_list|,
name|shardSegments
operator|.
name|getNumberOfCommitted
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|NUM_SEARCH_SEGMENTS
argument_list|,
name|shardSegments
operator|.
name|getNumberOfSearch
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|startObject
argument_list|(
name|Fields
operator|.
name|SEGMENTS
argument_list|)
expr_stmt|;
for|for
control|(
name|Segment
name|segment
range|:
name|shardSegments
control|)
block|{
name|builder
operator|.
name|startObject
argument_list|(
name|segment
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|GENERATION
argument_list|,
name|segment
operator|.
name|getGeneration
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|NUM_DOCS
argument_list|,
name|segment
operator|.
name|getNumDocs
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|DELETED_DOCS
argument_list|,
name|segment
operator|.
name|getDeletedDocs
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|byteSizeField
argument_list|(
name|Fields
operator|.
name|SIZE_IN_BYTES
argument_list|,
name|Fields
operator|.
name|SIZE
argument_list|,
name|segment
operator|.
name|getSizeInBytes
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|byteSizeField
argument_list|(
name|Fields
operator|.
name|MEMORY_IN_BYTES
argument_list|,
name|Fields
operator|.
name|MEMORY
argument_list|,
name|segment
operator|.
name|getMemoryInBytes
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|COMMITTED
argument_list|,
name|segment
operator|.
name|isCommitted
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|SEARCH
argument_list|,
name|segment
operator|.
name|isSearch
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|segment
operator|.
name|getVersion
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|VERSION
argument_list|,
name|segment
operator|.
name|getVersion
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|segment
operator|.
name|isCompound
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|COMPOUND
argument_list|,
name|segment
operator|.
name|isCompound
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|segment
operator|.
name|getMergeId
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|MERGE_ID
argument_list|,
name|segment
operator|.
name|getMergeId
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|segment
operator|.
name|ramTree
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|startArray
argument_list|(
name|Fields
operator|.
name|RAM_TREE
argument_list|)
expr_stmt|;
for|for
control|(
name|Accountable
name|child
range|:
name|segment
operator|.
name|ramTree
operator|.
name|getChildResources
argument_list|()
control|)
block|{
name|toXContent
argument_list|(
name|builder
argument_list|,
name|child
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
name|builder
operator|.
name|endObject
argument_list|()
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
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
return|return
name|builder
return|;
block|}
DECL|method|toXContent
specifier|static
name|void
name|toXContent
parameter_list|(
name|XContentBuilder
name|builder
parameter_list|,
name|Accountable
name|tree
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
name|Fields
operator|.
name|DESCRIPTION
argument_list|,
name|tree
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|byteSizeField
argument_list|(
name|Fields
operator|.
name|SIZE_IN_BYTES
argument_list|,
name|Fields
operator|.
name|SIZE
argument_list|,
operator|new
name|ByteSizeValue
argument_list|(
name|tree
operator|.
name|ramBytesUsed
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|Collection
argument_list|<
name|Accountable
argument_list|>
name|children
init|=
name|tree
operator|.
name|getChildResources
argument_list|()
decl_stmt|;
if|if
condition|(
name|children
operator|.
name|isEmpty
argument_list|()
operator|==
literal|false
condition|)
block|{
name|builder
operator|.
name|startArray
argument_list|(
name|Fields
operator|.
name|CHILDREN
argument_list|)
expr_stmt|;
for|for
control|(
name|Accountable
name|child
range|:
name|children
control|)
block|{
name|toXContent
argument_list|(
name|builder
argument_list|,
name|child
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
DECL|class|Fields
specifier|static
specifier|final
class|class
name|Fields
block|{
DECL|field|INDICES
specifier|static
specifier|final
name|XContentBuilderString
name|INDICES
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"indices"
argument_list|)
decl_stmt|;
DECL|field|SHARDS
specifier|static
specifier|final
name|XContentBuilderString
name|SHARDS
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"shards"
argument_list|)
decl_stmt|;
DECL|field|ROUTING
specifier|static
specifier|final
name|XContentBuilderString
name|ROUTING
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"routing"
argument_list|)
decl_stmt|;
DECL|field|STATE
specifier|static
specifier|final
name|XContentBuilderString
name|STATE
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"state"
argument_list|)
decl_stmt|;
DECL|field|PRIMARY
specifier|static
specifier|final
name|XContentBuilderString
name|PRIMARY
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"primary"
argument_list|)
decl_stmt|;
DECL|field|NODE
specifier|static
specifier|final
name|XContentBuilderString
name|NODE
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"node"
argument_list|)
decl_stmt|;
DECL|field|RELOCATING_NODE
specifier|static
specifier|final
name|XContentBuilderString
name|RELOCATING_NODE
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"relocating_node"
argument_list|)
decl_stmt|;
DECL|field|SEGMENTS
specifier|static
specifier|final
name|XContentBuilderString
name|SEGMENTS
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"segments"
argument_list|)
decl_stmt|;
DECL|field|GENERATION
specifier|static
specifier|final
name|XContentBuilderString
name|GENERATION
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"generation"
argument_list|)
decl_stmt|;
DECL|field|NUM_COMMITTED_SEGMENTS
specifier|static
specifier|final
name|XContentBuilderString
name|NUM_COMMITTED_SEGMENTS
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"num_committed_segments"
argument_list|)
decl_stmt|;
DECL|field|NUM_SEARCH_SEGMENTS
specifier|static
specifier|final
name|XContentBuilderString
name|NUM_SEARCH_SEGMENTS
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"num_search_segments"
argument_list|)
decl_stmt|;
DECL|field|NUM_DOCS
specifier|static
specifier|final
name|XContentBuilderString
name|NUM_DOCS
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"num_docs"
argument_list|)
decl_stmt|;
DECL|field|DELETED_DOCS
specifier|static
specifier|final
name|XContentBuilderString
name|DELETED_DOCS
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"deleted_docs"
argument_list|)
decl_stmt|;
DECL|field|SIZE
specifier|static
specifier|final
name|XContentBuilderString
name|SIZE
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"size"
argument_list|)
decl_stmt|;
DECL|field|SIZE_IN_BYTES
specifier|static
specifier|final
name|XContentBuilderString
name|SIZE_IN_BYTES
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"size_in_bytes"
argument_list|)
decl_stmt|;
DECL|field|COMMITTED
specifier|static
specifier|final
name|XContentBuilderString
name|COMMITTED
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"committed"
argument_list|)
decl_stmt|;
DECL|field|SEARCH
specifier|static
specifier|final
name|XContentBuilderString
name|SEARCH
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"search"
argument_list|)
decl_stmt|;
DECL|field|VERSION
specifier|static
specifier|final
name|XContentBuilderString
name|VERSION
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"version"
argument_list|)
decl_stmt|;
DECL|field|COMPOUND
specifier|static
specifier|final
name|XContentBuilderString
name|COMPOUND
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"compound"
argument_list|)
decl_stmt|;
DECL|field|MERGE_ID
specifier|static
specifier|final
name|XContentBuilderString
name|MERGE_ID
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"merge_id"
argument_list|)
decl_stmt|;
DECL|field|MEMORY
specifier|static
specifier|final
name|XContentBuilderString
name|MEMORY
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"memory"
argument_list|)
decl_stmt|;
DECL|field|MEMORY_IN_BYTES
specifier|static
specifier|final
name|XContentBuilderString
name|MEMORY_IN_BYTES
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"memory_in_bytes"
argument_list|)
decl_stmt|;
DECL|field|RAM_TREE
specifier|static
specifier|final
name|XContentBuilderString
name|RAM_TREE
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"ram_tree"
argument_list|)
decl_stmt|;
DECL|field|DESCRIPTION
specifier|static
specifier|final
name|XContentBuilderString
name|DESCRIPTION
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"description"
argument_list|)
decl_stmt|;
DECL|field|CHILDREN
specifier|static
specifier|final
name|XContentBuilderString
name|CHILDREN
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"children"
argument_list|)
decl_stmt|;
block|}
block|}
end_class

end_unit

