begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.indices
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|indices
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
name|admin
operator|.
name|indices
operator|.
name|stats
operator|.
name|CommonStats
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
name|admin
operator|.
name|indices
operator|.
name|stats
operator|.
name|IndexShardStats
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
name|admin
operator|.
name|indices
operator|.
name|stats
operator|.
name|ShardStats
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
name|index
operator|.
name|Index
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
name|cache
operator|.
name|query
operator|.
name|QueryCacheStats
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
name|cache
operator|.
name|request
operator|.
name|RequestCacheStats
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
name|SegmentsStats
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
name|fielddata
operator|.
name|FieldDataStats
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
name|flush
operator|.
name|FlushStats
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
name|get
operator|.
name|GetStats
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
name|merge
operator|.
name|MergeStats
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
name|recovery
operator|.
name|RecoveryStats
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
name|refresh
operator|.
name|RefreshStats
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
name|search
operator|.
name|stats
operator|.
name|SearchStats
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
name|shard
operator|.
name|DocsStats
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
name|shard
operator|.
name|IndexingStats
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
name|store
operator|.
name|StoreStats
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
name|suggest
operator|.
name|completion
operator|.
name|CompletionStats
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
comment|/**  * Global information on indices stats running on a specific node.  */
end_comment

begin_class
DECL|class|NodeIndicesStats
specifier|public
class|class
name|NodeIndicesStats
implements|implements
name|Streamable
implements|,
name|ToXContent
block|{
DECL|field|stats
specifier|private
name|CommonStats
name|stats
decl_stmt|;
DECL|field|statsByShard
specifier|private
name|Map
argument_list|<
name|Index
argument_list|,
name|List
argument_list|<
name|IndexShardStats
argument_list|>
argument_list|>
name|statsByShard
decl_stmt|;
DECL|method|NodeIndicesStats
name|NodeIndicesStats
parameter_list|()
block|{     }
DECL|method|NodeIndicesStats
specifier|public
name|NodeIndicesStats
parameter_list|(
name|CommonStats
name|oldStats
parameter_list|,
name|Map
argument_list|<
name|Index
argument_list|,
name|List
argument_list|<
name|IndexShardStats
argument_list|>
argument_list|>
name|statsByShard
parameter_list|)
block|{
comment|//this.stats = stats;
name|this
operator|.
name|statsByShard
operator|=
name|statsByShard
expr_stmt|;
comment|// make a total common stats from old ones and current ones
name|this
operator|.
name|stats
operator|=
name|oldStats
expr_stmt|;
for|for
control|(
name|List
argument_list|<
name|IndexShardStats
argument_list|>
name|shardStatsList
range|:
name|statsByShard
operator|.
name|values
argument_list|()
control|)
block|{
for|for
control|(
name|IndexShardStats
name|indexShardStats
range|:
name|shardStatsList
control|)
block|{
for|for
control|(
name|ShardStats
name|shardStats
range|:
name|indexShardStats
operator|.
name|getShards
argument_list|()
control|)
block|{
name|stats
operator|.
name|add
argument_list|(
name|shardStats
operator|.
name|getStats
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
annotation|@
name|Nullable
DECL|method|getStore
specifier|public
name|StoreStats
name|getStore
parameter_list|()
block|{
return|return
name|stats
operator|.
name|getStore
argument_list|()
return|;
block|}
annotation|@
name|Nullable
DECL|method|getDocs
specifier|public
name|DocsStats
name|getDocs
parameter_list|()
block|{
return|return
name|stats
operator|.
name|getDocs
argument_list|()
return|;
block|}
annotation|@
name|Nullable
DECL|method|getIndexing
specifier|public
name|IndexingStats
name|getIndexing
parameter_list|()
block|{
return|return
name|stats
operator|.
name|getIndexing
argument_list|()
return|;
block|}
annotation|@
name|Nullable
DECL|method|getGet
specifier|public
name|GetStats
name|getGet
parameter_list|()
block|{
return|return
name|stats
operator|.
name|getGet
argument_list|()
return|;
block|}
annotation|@
name|Nullable
DECL|method|getSearch
specifier|public
name|SearchStats
name|getSearch
parameter_list|()
block|{
return|return
name|stats
operator|.
name|getSearch
argument_list|()
return|;
block|}
annotation|@
name|Nullable
DECL|method|getMerge
specifier|public
name|MergeStats
name|getMerge
parameter_list|()
block|{
return|return
name|stats
operator|.
name|getMerge
argument_list|()
return|;
block|}
annotation|@
name|Nullable
DECL|method|getRefresh
specifier|public
name|RefreshStats
name|getRefresh
parameter_list|()
block|{
return|return
name|stats
operator|.
name|getRefresh
argument_list|()
return|;
block|}
annotation|@
name|Nullable
DECL|method|getFlush
specifier|public
name|FlushStats
name|getFlush
parameter_list|()
block|{
return|return
name|stats
operator|.
name|getFlush
argument_list|()
return|;
block|}
annotation|@
name|Nullable
DECL|method|getFieldData
specifier|public
name|FieldDataStats
name|getFieldData
parameter_list|()
block|{
return|return
name|stats
operator|.
name|getFieldData
argument_list|()
return|;
block|}
annotation|@
name|Nullable
DECL|method|getQueryCache
specifier|public
name|QueryCacheStats
name|getQueryCache
parameter_list|()
block|{
return|return
name|stats
operator|.
name|getQueryCache
argument_list|()
return|;
block|}
annotation|@
name|Nullable
DECL|method|getRequestCache
specifier|public
name|RequestCacheStats
name|getRequestCache
parameter_list|()
block|{
return|return
name|stats
operator|.
name|getRequestCache
argument_list|()
return|;
block|}
annotation|@
name|Nullable
DECL|method|getCompletion
specifier|public
name|CompletionStats
name|getCompletion
parameter_list|()
block|{
return|return
name|stats
operator|.
name|getCompletion
argument_list|()
return|;
block|}
annotation|@
name|Nullable
DECL|method|getSegments
specifier|public
name|SegmentsStats
name|getSegments
parameter_list|()
block|{
return|return
name|stats
operator|.
name|getSegments
argument_list|()
return|;
block|}
annotation|@
name|Nullable
DECL|method|getRecoveryStats
specifier|public
name|RecoveryStats
name|getRecoveryStats
parameter_list|()
block|{
return|return
name|stats
operator|.
name|getRecoveryStats
argument_list|()
return|;
block|}
DECL|method|readIndicesStats
specifier|public
specifier|static
name|NodeIndicesStats
name|readIndicesStats
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|NodeIndicesStats
name|stats
init|=
operator|new
name|NodeIndicesStats
argument_list|()
decl_stmt|;
name|stats
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
return|return
name|stats
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
name|stats
operator|=
operator|new
name|CommonStats
argument_list|(
name|in
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
name|int
name|entries
init|=
name|in
operator|.
name|readVInt
argument_list|()
decl_stmt|;
name|statsByShard
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
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
name|entries
condition|;
name|i
operator|++
control|)
block|{
name|Index
name|index
init|=
operator|new
name|Index
argument_list|(
name|in
argument_list|)
decl_stmt|;
name|int
name|indexShardListSize
init|=
name|in
operator|.
name|readVInt
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|IndexShardStats
argument_list|>
name|indexShardStats
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|indexShardListSize
argument_list|)
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
name|indexShardListSize
condition|;
name|j
operator|++
control|)
block|{
name|indexShardStats
operator|.
name|add
argument_list|(
name|IndexShardStats
operator|.
name|readIndexShardStats
argument_list|(
name|in
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|statsByShard
operator|.
name|put
argument_list|(
name|index
argument_list|,
name|indexShardStats
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
name|stats
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|statsByShard
operator|!=
literal|null
argument_list|)
expr_stmt|;
if|if
condition|(
name|statsByShard
operator|!=
literal|null
condition|)
block|{
name|out
operator|.
name|writeVInt
argument_list|(
name|statsByShard
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
name|Index
argument_list|,
name|List
argument_list|<
name|IndexShardStats
argument_list|>
argument_list|>
name|entry
range|:
name|statsByShard
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|entry
operator|.
name|getKey
argument_list|()
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
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|IndexShardStats
name|indexShardStats
range|:
name|entry
operator|.
name|getValue
argument_list|()
control|)
block|{
name|indexShardStats
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
block|}
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
specifier|final
name|String
name|level
init|=
name|params
operator|.
name|param
argument_list|(
literal|"level"
argument_list|,
literal|"node"
argument_list|)
decl_stmt|;
specifier|final
name|boolean
name|isLevelValid
init|=
literal|"indices"
operator|.
name|equalsIgnoreCase
argument_list|(
name|level
argument_list|)
operator|||
literal|"node"
operator|.
name|equalsIgnoreCase
argument_list|(
name|level
argument_list|)
operator|||
literal|"shards"
operator|.
name|equalsIgnoreCase
argument_list|(
name|level
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|isLevelValid
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"level parameter must be one of [indices] or [node] or [shards] but was ["
operator|+
name|level
operator|+
literal|"]"
argument_list|)
throw|;
block|}
comment|// "node" level
name|builder
operator|.
name|startObject
argument_list|(
name|Fields
operator|.
name|INDICES
argument_list|)
expr_stmt|;
name|stats
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
if|if
condition|(
literal|"indices"
operator|.
name|equals
argument_list|(
name|level
argument_list|)
condition|)
block|{
name|Map
argument_list|<
name|Index
argument_list|,
name|CommonStats
argument_list|>
name|indexStats
init|=
name|createStatsByIndex
argument_list|()
decl_stmt|;
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
name|Map
operator|.
name|Entry
argument_list|<
name|Index
argument_list|,
name|CommonStats
argument_list|>
name|entry
range|:
name|indexStats
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|builder
operator|.
name|startObject
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
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
block|}
elseif|else
if|if
condition|(
literal|"shards"
operator|.
name|equals
argument_list|(
name|level
argument_list|)
condition|)
block|{
name|builder
operator|.
name|startObject
argument_list|(
literal|"shards"
argument_list|)
expr_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|Index
argument_list|,
name|List
argument_list|<
name|IndexShardStats
argument_list|>
argument_list|>
name|entry
range|:
name|statsByShard
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|builder
operator|.
name|startArray
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|IndexShardStats
name|indexShardStats
range|:
name|entry
operator|.
name|getValue
argument_list|()
control|)
block|{
name|builder
operator|.
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
name|String
operator|.
name|valueOf
argument_list|(
name|indexShardStats
operator|.
name|getShardId
argument_list|()
operator|.
name|getId
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|ShardStats
name|shardStats
range|:
name|indexShardStats
operator|.
name|getShards
argument_list|()
control|)
block|{
name|shardStats
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
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
DECL|method|createStatsByIndex
specifier|private
name|Map
argument_list|<
name|Index
argument_list|,
name|CommonStats
argument_list|>
name|createStatsByIndex
parameter_list|()
block|{
name|Map
argument_list|<
name|Index
argument_list|,
name|CommonStats
argument_list|>
name|statsMap
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|Index
argument_list|,
name|List
argument_list|<
name|IndexShardStats
argument_list|>
argument_list|>
name|entry
range|:
name|statsByShard
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|statsMap
operator|.
name|containsKey
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
condition|)
block|{
name|statsMap
operator|.
name|put
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
operator|new
name|CommonStats
argument_list|()
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|IndexShardStats
name|indexShardStats
range|:
name|entry
operator|.
name|getValue
argument_list|()
control|)
block|{
for|for
control|(
name|ShardStats
name|shardStats
range|:
name|indexShardStats
operator|.
name|getShards
argument_list|()
control|)
block|{
name|statsMap
operator|.
name|get
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
operator|.
name|add
argument_list|(
name|shardStats
operator|.
name|getStats
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|statsMap
return|;
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
name|String
name|INDICES
init|=
literal|"indices"
decl_stmt|;
block|}
block|}
end_class

end_unit

