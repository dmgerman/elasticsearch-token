begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.indices.stats
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
name|stats
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
name|engine
operator|.
name|CommitStats
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
name|seqno
operator|.
name|SeqNoStats
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
name|ShardPath
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

begin_class
DECL|class|ShardStats
specifier|public
class|class
name|ShardStats
implements|implements
name|Streamable
implements|,
name|ToXContent
block|{
DECL|field|shardRouting
specifier|private
name|ShardRouting
name|shardRouting
decl_stmt|;
DECL|field|commonStats
specifier|private
name|CommonStats
name|commonStats
decl_stmt|;
annotation|@
name|Nullable
DECL|field|commitStats
specifier|private
name|CommitStats
name|commitStats
decl_stmt|;
annotation|@
name|Nullable
DECL|field|seqNoStats
specifier|private
name|SeqNoStats
name|seqNoStats
decl_stmt|;
DECL|field|dataPath
specifier|private
name|String
name|dataPath
decl_stmt|;
DECL|field|statePath
specifier|private
name|String
name|statePath
decl_stmt|;
DECL|field|isCustomDataPath
specifier|private
name|boolean
name|isCustomDataPath
decl_stmt|;
DECL|method|ShardStats
name|ShardStats
parameter_list|()
block|{     }
DECL|method|ShardStats
specifier|public
name|ShardStats
parameter_list|(
name|ShardRouting
name|routing
parameter_list|,
name|ShardPath
name|shardPath
parameter_list|,
name|CommonStats
name|commonStats
parameter_list|,
name|CommitStats
name|commitStats
parameter_list|,
name|SeqNoStats
name|seqNoStats
parameter_list|)
block|{
name|this
operator|.
name|shardRouting
operator|=
name|routing
expr_stmt|;
name|this
operator|.
name|dataPath
operator|=
name|shardPath
operator|.
name|getRootDataPath
argument_list|()
operator|.
name|toString
argument_list|()
expr_stmt|;
name|this
operator|.
name|statePath
operator|=
name|shardPath
operator|.
name|getRootStatePath
argument_list|()
operator|.
name|toString
argument_list|()
expr_stmt|;
name|this
operator|.
name|isCustomDataPath
operator|=
name|shardPath
operator|.
name|isCustomDataPath
argument_list|()
expr_stmt|;
name|this
operator|.
name|commitStats
operator|=
name|commitStats
expr_stmt|;
name|this
operator|.
name|commonStats
operator|=
name|commonStats
expr_stmt|;
name|this
operator|.
name|seqNoStats
operator|=
name|seqNoStats
expr_stmt|;
block|}
comment|/**      * The shard routing information (cluster wide shard state).      */
DECL|method|getShardRouting
specifier|public
name|ShardRouting
name|getShardRouting
parameter_list|()
block|{
return|return
name|this
operator|.
name|shardRouting
return|;
block|}
DECL|method|getStats
specifier|public
name|CommonStats
name|getStats
parameter_list|()
block|{
return|return
name|this
operator|.
name|commonStats
return|;
block|}
DECL|method|getCommitStats
specifier|public
name|CommitStats
name|getCommitStats
parameter_list|()
block|{
return|return
name|this
operator|.
name|commitStats
return|;
block|}
annotation|@
name|Nullable
DECL|method|getSeqNoStats
specifier|public
name|SeqNoStats
name|getSeqNoStats
parameter_list|()
block|{
return|return
name|this
operator|.
name|seqNoStats
return|;
block|}
DECL|method|getDataPath
specifier|public
name|String
name|getDataPath
parameter_list|()
block|{
return|return
name|dataPath
return|;
block|}
DECL|method|getStatePath
specifier|public
name|String
name|getStatePath
parameter_list|()
block|{
return|return
name|statePath
return|;
block|}
DECL|method|isCustomDataPath
specifier|public
name|boolean
name|isCustomDataPath
parameter_list|()
block|{
return|return
name|isCustomDataPath
return|;
block|}
DECL|method|readShardStats
specifier|public
specifier|static
name|ShardStats
name|readShardStats
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|ShardStats
name|stats
init|=
operator|new
name|ShardStats
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
name|shardRouting
operator|=
operator|new
name|ShardRouting
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|commonStats
operator|=
operator|new
name|CommonStats
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|commitStats
operator|=
name|CommitStats
operator|.
name|readOptionalCommitStatsFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|statePath
operator|=
name|in
operator|.
name|readString
argument_list|()
expr_stmt|;
name|dataPath
operator|=
name|in
operator|.
name|readString
argument_list|()
expr_stmt|;
name|isCustomDataPath
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
name|seqNoStats
operator|=
name|in
operator|.
name|readOptionalWriteable
argument_list|(
name|SeqNoStats
operator|::
operator|new
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
name|shardRouting
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|commonStats
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeOptionalStreamable
argument_list|(
name|commitStats
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeString
argument_list|(
name|statePath
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeString
argument_list|(
name|dataPath
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|isCustomDataPath
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeOptionalWriteable
argument_list|(
name|seqNoStats
argument_list|)
expr_stmt|;
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
name|ROUTING
argument_list|)
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|STATE
argument_list|,
name|shardRouting
operator|.
name|state
argument_list|()
argument_list|)
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|PRIMARY
argument_list|,
name|shardRouting
operator|.
name|primary
argument_list|()
argument_list|)
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|NODE
argument_list|,
name|shardRouting
operator|.
name|currentNodeId
argument_list|()
argument_list|)
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|RELOCATING_NODE
argument_list|,
name|shardRouting
operator|.
name|relocatingNodeId
argument_list|()
argument_list|)
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|commonStats
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
name|commitStats
operator|!=
literal|null
condition|)
block|{
name|commitStats
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|seqNoStats
operator|!=
literal|null
condition|)
block|{
name|seqNoStats
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
name|startObject
argument_list|(
name|Fields
operator|.
name|SHARD_PATH
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|STATE_PATH
argument_list|,
name|statePath
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|DATA_PATH
argument_list|,
name|dataPath
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|IS_CUSTOM_DATA_PATH
argument_list|,
name|isCustomDataPath
argument_list|)
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
DECL|class|Fields
specifier|static
specifier|final
class|class
name|Fields
block|{
DECL|field|ROUTING
specifier|static
specifier|final
name|String
name|ROUTING
init|=
literal|"routing"
decl_stmt|;
DECL|field|STATE
specifier|static
specifier|final
name|String
name|STATE
init|=
literal|"state"
decl_stmt|;
DECL|field|STATE_PATH
specifier|static
specifier|final
name|String
name|STATE_PATH
init|=
literal|"state_path"
decl_stmt|;
DECL|field|DATA_PATH
specifier|static
specifier|final
name|String
name|DATA_PATH
init|=
literal|"data_path"
decl_stmt|;
DECL|field|IS_CUSTOM_DATA_PATH
specifier|static
specifier|final
name|String
name|IS_CUSTOM_DATA_PATH
init|=
literal|"is_custom_data_path"
decl_stmt|;
DECL|field|SHARD_PATH
specifier|static
specifier|final
name|String
name|SHARD_PATH
init|=
literal|"shard_path"
decl_stmt|;
DECL|field|PRIMARY
specifier|static
specifier|final
name|String
name|PRIMARY
init|=
literal|"primary"
decl_stmt|;
DECL|field|NODE
specifier|static
specifier|final
name|String
name|NODE
init|=
literal|"node"
decl_stmt|;
DECL|field|RELOCATING_NODE
specifier|static
specifier|final
name|String
name|RELOCATING_NODE
init|=
literal|"relocating_node"
decl_stmt|;
block|}
block|}
end_class

end_unit

