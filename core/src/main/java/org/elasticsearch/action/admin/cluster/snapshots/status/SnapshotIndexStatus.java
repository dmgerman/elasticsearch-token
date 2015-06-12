begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.cluster.snapshots.status
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|admin
operator|.
name|cluster
operator|.
name|snapshots
operator|.
name|status
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
name|Collection
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

begin_comment
comment|/**  * Represents snapshot status of all shards in the index  */
end_comment

begin_class
DECL|class|SnapshotIndexStatus
specifier|public
class|class
name|SnapshotIndexStatus
implements|implements
name|Iterable
argument_list|<
name|SnapshotIndexShardStatus
argument_list|>
implements|,
name|ToXContent
block|{
DECL|field|index
specifier|private
specifier|final
name|String
name|index
decl_stmt|;
DECL|field|indexShards
specifier|private
specifier|final
name|Map
argument_list|<
name|Integer
argument_list|,
name|SnapshotIndexShardStatus
argument_list|>
name|indexShards
decl_stmt|;
DECL|field|shardsStats
specifier|private
specifier|final
name|SnapshotShardsStats
name|shardsStats
decl_stmt|;
DECL|field|stats
specifier|private
specifier|final
name|SnapshotStats
name|stats
decl_stmt|;
DECL|method|SnapshotIndexStatus
name|SnapshotIndexStatus
parameter_list|(
name|String
name|index
parameter_list|,
name|Collection
argument_list|<
name|SnapshotIndexShardStatus
argument_list|>
name|shards
parameter_list|)
block|{
name|this
operator|.
name|index
operator|=
name|index
expr_stmt|;
name|ImmutableMap
operator|.
name|Builder
argument_list|<
name|Integer
argument_list|,
name|SnapshotIndexShardStatus
argument_list|>
name|builder
init|=
name|ImmutableMap
operator|.
name|builder
argument_list|()
decl_stmt|;
name|stats
operator|=
operator|new
name|SnapshotStats
argument_list|()
expr_stmt|;
for|for
control|(
name|SnapshotIndexShardStatus
name|shard
range|:
name|shards
control|)
block|{
name|builder
operator|.
name|put
argument_list|(
name|shard
operator|.
name|getShardId
argument_list|()
argument_list|,
name|shard
argument_list|)
expr_stmt|;
name|stats
operator|.
name|add
argument_list|(
name|shard
operator|.
name|getStats
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|shardsStats
operator|=
operator|new
name|SnapshotShardsStats
argument_list|(
name|shards
argument_list|)
expr_stmt|;
name|indexShards
operator|=
name|builder
operator|.
name|build
argument_list|()
expr_stmt|;
block|}
comment|/**      * Returns the index name      */
DECL|method|getIndex
specifier|public
name|String
name|getIndex
parameter_list|()
block|{
return|return
name|this
operator|.
name|index
return|;
block|}
comment|/**      * A shard id to index snapshot shard status map      */
DECL|method|getShards
specifier|public
name|Map
argument_list|<
name|Integer
argument_list|,
name|SnapshotIndexShardStatus
argument_list|>
name|getShards
parameter_list|()
block|{
return|return
name|this
operator|.
name|indexShards
return|;
block|}
comment|/**      * Shards stats      */
DECL|method|getShardsStats
specifier|public
name|SnapshotShardsStats
name|getShardsStats
parameter_list|()
block|{
return|return
name|shardsStats
return|;
block|}
comment|/**      * Returns snapshot stats      */
DECL|method|getStats
specifier|public
name|SnapshotStats
name|getStats
parameter_list|()
block|{
return|return
name|stats
return|;
block|}
annotation|@
name|Override
DECL|method|iterator
specifier|public
name|Iterator
argument_list|<
name|SnapshotIndexShardStatus
argument_list|>
name|iterator
parameter_list|()
block|{
return|return
name|indexShards
operator|.
name|values
argument_list|()
operator|.
name|iterator
argument_list|()
return|;
block|}
DECL|class|Fields
specifier|static
specifier|final
class|class
name|Fields
block|{
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
name|shardsStats
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
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
name|SnapshotIndexShardStatus
name|shard
range|:
name|indexShards
operator|.
name|values
argument_list|()
control|)
block|{
name|shard
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
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
return|return
name|builder
return|;
block|}
block|}
end_class

end_unit
