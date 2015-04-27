begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.cluster.metadata
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|metadata
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
name|ElasticsearchIllegalArgumentException
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
name|AbstractDiffable
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
name|metadata
operator|.
name|MetaData
operator|.
name|Custom
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
name|common
operator|.
name|xcontent
operator|.
name|XContentParser
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
name|ShardId
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
name|EnumSet
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Maps
operator|.
name|newHashMap
import|;
end_import

begin_comment
comment|/**  * Meta data about snapshots that are currently executing  */
end_comment

begin_class
DECL|class|SnapshotMetaData
specifier|public
class|class
name|SnapshotMetaData
extends|extends
name|AbstractDiffable
argument_list|<
name|Custom
argument_list|>
implements|implements
name|MetaData
operator|.
name|Custom
block|{
DECL|field|TYPE
specifier|public
specifier|static
specifier|final
name|String
name|TYPE
init|=
literal|"snapshots"
decl_stmt|;
DECL|field|PROTO
specifier|public
specifier|static
specifier|final
name|SnapshotMetaData
name|PROTO
init|=
operator|new
name|SnapshotMetaData
argument_list|()
decl_stmt|;
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
operator|||
name|getClass
argument_list|()
operator|!=
name|o
operator|.
name|getClass
argument_list|()
condition|)
return|return
literal|false
return|;
name|SnapshotMetaData
name|that
init|=
operator|(
name|SnapshotMetaData
operator|)
name|o
decl_stmt|;
if|if
condition|(
operator|!
name|entries
operator|.
name|equals
argument_list|(
name|that
operator|.
name|entries
argument_list|)
condition|)
return|return
literal|false
return|;
return|return
literal|true
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
name|entries
operator|.
name|hashCode
argument_list|()
return|;
block|}
DECL|class|Entry
specifier|public
specifier|static
class|class
name|Entry
block|{
DECL|field|state
specifier|private
specifier|final
name|State
name|state
decl_stmt|;
DECL|field|snapshotId
specifier|private
specifier|final
name|SnapshotId
name|snapshotId
decl_stmt|;
DECL|field|includeGlobalState
specifier|private
specifier|final
name|boolean
name|includeGlobalState
decl_stmt|;
DECL|field|shards
specifier|private
specifier|final
name|ImmutableMap
argument_list|<
name|ShardId
argument_list|,
name|ShardSnapshotStatus
argument_list|>
name|shards
decl_stmt|;
DECL|field|indices
specifier|private
specifier|final
name|ImmutableList
argument_list|<
name|String
argument_list|>
name|indices
decl_stmt|;
DECL|field|waitingIndices
specifier|private
specifier|final
name|ImmutableMap
argument_list|<
name|String
argument_list|,
name|ImmutableList
argument_list|<
name|ShardId
argument_list|>
argument_list|>
name|waitingIndices
decl_stmt|;
DECL|field|startTime
specifier|private
specifier|final
name|long
name|startTime
decl_stmt|;
DECL|method|Entry
specifier|public
name|Entry
parameter_list|(
name|SnapshotId
name|snapshotId
parameter_list|,
name|boolean
name|includeGlobalState
parameter_list|,
name|State
name|state
parameter_list|,
name|ImmutableList
argument_list|<
name|String
argument_list|>
name|indices
parameter_list|,
name|long
name|startTime
parameter_list|,
name|ImmutableMap
argument_list|<
name|ShardId
argument_list|,
name|ShardSnapshotStatus
argument_list|>
name|shards
parameter_list|)
block|{
name|this
operator|.
name|state
operator|=
name|state
expr_stmt|;
name|this
operator|.
name|snapshotId
operator|=
name|snapshotId
expr_stmt|;
name|this
operator|.
name|includeGlobalState
operator|=
name|includeGlobalState
expr_stmt|;
name|this
operator|.
name|indices
operator|=
name|indices
expr_stmt|;
name|this
operator|.
name|startTime
operator|=
name|startTime
expr_stmt|;
if|if
condition|(
name|shards
operator|==
literal|null
condition|)
block|{
name|this
operator|.
name|shards
operator|=
name|ImmutableMap
operator|.
name|of
argument_list|()
expr_stmt|;
name|this
operator|.
name|waitingIndices
operator|=
name|ImmutableMap
operator|.
name|of
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|shards
operator|=
name|shards
expr_stmt|;
name|this
operator|.
name|waitingIndices
operator|=
name|findWaitingIndices
argument_list|(
name|shards
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|Entry
specifier|public
name|Entry
parameter_list|(
name|Entry
name|entry
parameter_list|,
name|State
name|state
parameter_list|,
name|ImmutableMap
argument_list|<
name|ShardId
argument_list|,
name|ShardSnapshotStatus
argument_list|>
name|shards
parameter_list|)
block|{
name|this
argument_list|(
name|entry
operator|.
name|snapshotId
argument_list|,
name|entry
operator|.
name|includeGlobalState
argument_list|,
name|state
argument_list|,
name|entry
operator|.
name|indices
argument_list|,
name|entry
operator|.
name|startTime
argument_list|,
name|shards
argument_list|)
expr_stmt|;
block|}
DECL|method|Entry
specifier|public
name|Entry
parameter_list|(
name|Entry
name|entry
parameter_list|,
name|ImmutableMap
argument_list|<
name|ShardId
argument_list|,
name|ShardSnapshotStatus
argument_list|>
name|shards
parameter_list|)
block|{
name|this
argument_list|(
name|entry
argument_list|,
name|entry
operator|.
name|state
argument_list|,
name|shards
argument_list|)
expr_stmt|;
block|}
DECL|method|snapshotId
specifier|public
name|SnapshotId
name|snapshotId
parameter_list|()
block|{
return|return
name|this
operator|.
name|snapshotId
return|;
block|}
DECL|method|shards
specifier|public
name|ImmutableMap
argument_list|<
name|ShardId
argument_list|,
name|ShardSnapshotStatus
argument_list|>
name|shards
parameter_list|()
block|{
return|return
name|this
operator|.
name|shards
return|;
block|}
DECL|method|state
specifier|public
name|State
name|state
parameter_list|()
block|{
return|return
name|state
return|;
block|}
DECL|method|indices
specifier|public
name|ImmutableList
argument_list|<
name|String
argument_list|>
name|indices
parameter_list|()
block|{
return|return
name|indices
return|;
block|}
DECL|method|waitingIndices
specifier|public
name|ImmutableMap
argument_list|<
name|String
argument_list|,
name|ImmutableList
argument_list|<
name|ShardId
argument_list|>
argument_list|>
name|waitingIndices
parameter_list|()
block|{
return|return
name|waitingIndices
return|;
block|}
DECL|method|includeGlobalState
specifier|public
name|boolean
name|includeGlobalState
parameter_list|()
block|{
return|return
name|includeGlobalState
return|;
block|}
DECL|method|startTime
specifier|public
name|long
name|startTime
parameter_list|()
block|{
return|return
name|startTime
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
operator|||
name|getClass
argument_list|()
operator|!=
name|o
operator|.
name|getClass
argument_list|()
condition|)
return|return
literal|false
return|;
name|Entry
name|entry
init|=
operator|(
name|Entry
operator|)
name|o
decl_stmt|;
if|if
condition|(
name|includeGlobalState
operator|!=
name|entry
operator|.
name|includeGlobalState
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|startTime
operator|!=
name|entry
operator|.
name|startTime
condition|)
return|return
literal|false
return|;
if|if
condition|(
operator|!
name|indices
operator|.
name|equals
argument_list|(
name|entry
operator|.
name|indices
argument_list|)
condition|)
return|return
literal|false
return|;
if|if
condition|(
operator|!
name|shards
operator|.
name|equals
argument_list|(
name|entry
operator|.
name|shards
argument_list|)
condition|)
return|return
literal|false
return|;
if|if
condition|(
operator|!
name|snapshotId
operator|.
name|equals
argument_list|(
name|entry
operator|.
name|snapshotId
argument_list|)
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|state
operator|!=
name|entry
operator|.
name|state
condition|)
return|return
literal|false
return|;
if|if
condition|(
operator|!
name|waitingIndices
operator|.
name|equals
argument_list|(
name|entry
operator|.
name|waitingIndices
argument_list|)
condition|)
return|return
literal|false
return|;
return|return
literal|true
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
name|int
name|result
init|=
name|state
operator|.
name|hashCode
argument_list|()
decl_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
name|snapshotId
operator|.
name|hashCode
argument_list|()
expr_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
operator|(
name|includeGlobalState
condition|?
literal|1
else|:
literal|0
operator|)
expr_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
name|shards
operator|.
name|hashCode
argument_list|()
expr_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
name|indices
operator|.
name|hashCode
argument_list|()
expr_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
name|waitingIndices
operator|.
name|hashCode
argument_list|()
expr_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
call|(
name|int
call|)
argument_list|(
name|startTime
operator|^
operator|(
name|startTime
operator|>>>
literal|32
operator|)
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
DECL|method|findWaitingIndices
specifier|private
name|ImmutableMap
argument_list|<
name|String
argument_list|,
name|ImmutableList
argument_list|<
name|ShardId
argument_list|>
argument_list|>
name|findWaitingIndices
parameter_list|(
name|ImmutableMap
argument_list|<
name|ShardId
argument_list|,
name|ShardSnapshotStatus
argument_list|>
name|shards
parameter_list|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|ImmutableList
operator|.
name|Builder
argument_list|<
name|ShardId
argument_list|>
argument_list|>
name|waitingIndicesMap
init|=
name|newHashMap
argument_list|()
decl_stmt|;
for|for
control|(
name|ImmutableMap
operator|.
name|Entry
argument_list|<
name|ShardId
argument_list|,
name|ShardSnapshotStatus
argument_list|>
name|entry
range|:
name|shards
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|state
argument_list|()
operator|==
name|State
operator|.
name|WAITING
condition|)
block|{
name|ImmutableList
operator|.
name|Builder
argument_list|<
name|ShardId
argument_list|>
name|waitingShards
init|=
name|waitingIndicesMap
operator|.
name|get
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|getIndex
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|waitingShards
operator|==
literal|null
condition|)
block|{
name|waitingShards
operator|=
name|ImmutableList
operator|.
name|builder
argument_list|()
expr_stmt|;
name|waitingIndicesMap
operator|.
name|put
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|getIndex
argument_list|()
argument_list|,
name|waitingShards
argument_list|)
expr_stmt|;
block|}
name|waitingShards
operator|.
name|add
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
operator|!
name|waitingIndicesMap
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|ImmutableMap
operator|.
name|Builder
argument_list|<
name|String
argument_list|,
name|ImmutableList
argument_list|<
name|ShardId
argument_list|>
argument_list|>
name|waitingIndicesBuilder
init|=
name|ImmutableMap
operator|.
name|builder
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|ImmutableList
operator|.
name|Builder
argument_list|<
name|ShardId
argument_list|>
argument_list|>
name|entry
range|:
name|waitingIndicesMap
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|waitingIndicesBuilder
operator|.
name|put
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|waitingIndicesBuilder
operator|.
name|build
argument_list|()
return|;
block|}
else|else
block|{
return|return
name|ImmutableMap
operator|.
name|of
argument_list|()
return|;
block|}
block|}
block|}
DECL|class|ShardSnapshotStatus
specifier|public
specifier|static
class|class
name|ShardSnapshotStatus
block|{
DECL|field|state
specifier|private
name|State
name|state
decl_stmt|;
DECL|field|nodeId
specifier|private
name|String
name|nodeId
decl_stmt|;
DECL|field|reason
specifier|private
name|String
name|reason
decl_stmt|;
DECL|method|ShardSnapshotStatus
specifier|private
name|ShardSnapshotStatus
parameter_list|()
block|{         }
DECL|method|ShardSnapshotStatus
specifier|public
name|ShardSnapshotStatus
parameter_list|(
name|String
name|nodeId
parameter_list|)
block|{
name|this
argument_list|(
name|nodeId
argument_list|,
name|State
operator|.
name|INIT
argument_list|)
expr_stmt|;
block|}
DECL|method|ShardSnapshotStatus
specifier|public
name|ShardSnapshotStatus
parameter_list|(
name|String
name|nodeId
parameter_list|,
name|State
name|state
parameter_list|)
block|{
name|this
argument_list|(
name|nodeId
argument_list|,
name|state
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
DECL|method|ShardSnapshotStatus
specifier|public
name|ShardSnapshotStatus
parameter_list|(
name|String
name|nodeId
parameter_list|,
name|State
name|state
parameter_list|,
name|String
name|reason
parameter_list|)
block|{
name|this
operator|.
name|nodeId
operator|=
name|nodeId
expr_stmt|;
name|this
operator|.
name|state
operator|=
name|state
expr_stmt|;
name|this
operator|.
name|reason
operator|=
name|reason
expr_stmt|;
block|}
DECL|method|state
specifier|public
name|State
name|state
parameter_list|()
block|{
return|return
name|state
return|;
block|}
DECL|method|nodeId
specifier|public
name|String
name|nodeId
parameter_list|()
block|{
return|return
name|nodeId
return|;
block|}
DECL|method|reason
specifier|public
name|String
name|reason
parameter_list|()
block|{
return|return
name|reason
return|;
block|}
DECL|method|readShardSnapshotStatus
specifier|public
specifier|static
name|ShardSnapshotStatus
name|readShardSnapshotStatus
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|ShardSnapshotStatus
name|shardSnapshotStatus
init|=
operator|new
name|ShardSnapshotStatus
argument_list|()
decl_stmt|;
name|shardSnapshotStatus
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
return|return
name|shardSnapshotStatus
return|;
block|}
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
name|nodeId
operator|=
name|in
operator|.
name|readOptionalString
argument_list|()
expr_stmt|;
name|state
operator|=
name|State
operator|.
name|fromValue
argument_list|(
name|in
operator|.
name|readByte
argument_list|()
argument_list|)
expr_stmt|;
name|reason
operator|=
name|in
operator|.
name|readOptionalString
argument_list|()
expr_stmt|;
block|}
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
name|out
operator|.
name|writeOptionalString
argument_list|(
name|nodeId
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeByte
argument_list|(
name|state
operator|.
name|value
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeOptionalString
argument_list|(
name|reason
argument_list|)
expr_stmt|;
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
operator|||
name|getClass
argument_list|()
operator|!=
name|o
operator|.
name|getClass
argument_list|()
condition|)
return|return
literal|false
return|;
name|ShardSnapshotStatus
name|status
init|=
operator|(
name|ShardSnapshotStatus
operator|)
name|o
decl_stmt|;
if|if
condition|(
name|nodeId
operator|!=
literal|null
condition|?
operator|!
name|nodeId
operator|.
name|equals
argument_list|(
name|status
operator|.
name|nodeId
argument_list|)
else|:
name|status
operator|.
name|nodeId
operator|!=
literal|null
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|reason
operator|!=
literal|null
condition|?
operator|!
name|reason
operator|.
name|equals
argument_list|(
name|status
operator|.
name|reason
argument_list|)
else|:
name|status
operator|.
name|reason
operator|!=
literal|null
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|state
operator|!=
name|status
operator|.
name|state
condition|)
return|return
literal|false
return|;
return|return
literal|true
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
name|int
name|result
init|=
name|state
operator|!=
literal|null
condition|?
name|state
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
operator|(
name|nodeId
operator|!=
literal|null
condition|?
name|nodeId
operator|.
name|hashCode
argument_list|()
else|:
literal|0
operator|)
expr_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
operator|(
name|reason
operator|!=
literal|null
condition|?
name|reason
operator|.
name|hashCode
argument_list|()
else|:
literal|0
operator|)
expr_stmt|;
return|return
name|result
return|;
block|}
block|}
DECL|enum|State
specifier|public
specifier|static
enum|enum
name|State
block|{
DECL|enum constant|INIT
name|INIT
argument_list|(
operator|(
name|byte
operator|)
literal|0
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
block|,
DECL|enum constant|STARTED
name|STARTED
argument_list|(
operator|(
name|byte
operator|)
literal|1
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
block|,
DECL|enum constant|SUCCESS
name|SUCCESS
argument_list|(
operator|(
name|byte
operator|)
literal|2
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|)
block|,
DECL|enum constant|FAILED
name|FAILED
argument_list|(
operator|(
name|byte
operator|)
literal|3
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|)
block|,
DECL|enum constant|ABORTED
name|ABORTED
argument_list|(
operator|(
name|byte
operator|)
literal|4
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|)
block|,
DECL|enum constant|MISSING
name|MISSING
argument_list|(
operator|(
name|byte
operator|)
literal|5
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|)
block|,
DECL|enum constant|WAITING
name|WAITING
argument_list|(
operator|(
name|byte
operator|)
literal|6
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
block|;
DECL|field|value
specifier|private
name|byte
name|value
decl_stmt|;
DECL|field|completed
specifier|private
name|boolean
name|completed
decl_stmt|;
DECL|field|failed
specifier|private
name|boolean
name|failed
decl_stmt|;
DECL|method|State
name|State
parameter_list|(
name|byte
name|value
parameter_list|,
name|boolean
name|completed
parameter_list|,
name|boolean
name|failed
parameter_list|)
block|{
name|this
operator|.
name|value
operator|=
name|value
expr_stmt|;
name|this
operator|.
name|completed
operator|=
name|completed
expr_stmt|;
name|this
operator|.
name|failed
operator|=
name|failed
expr_stmt|;
block|}
DECL|method|value
specifier|public
name|byte
name|value
parameter_list|()
block|{
return|return
name|value
return|;
block|}
DECL|method|completed
specifier|public
name|boolean
name|completed
parameter_list|()
block|{
return|return
name|completed
return|;
block|}
DECL|method|failed
specifier|public
name|boolean
name|failed
parameter_list|()
block|{
return|return
name|failed
return|;
block|}
DECL|method|fromValue
specifier|public
specifier|static
name|State
name|fromValue
parameter_list|(
name|byte
name|value
parameter_list|)
block|{
switch|switch
condition|(
name|value
condition|)
block|{
case|case
literal|0
case|:
return|return
name|INIT
return|;
case|case
literal|1
case|:
return|return
name|STARTED
return|;
case|case
literal|2
case|:
return|return
name|SUCCESS
return|;
case|case
literal|3
case|:
return|return
name|FAILED
return|;
case|case
literal|4
case|:
return|return
name|ABORTED
return|;
case|case
literal|5
case|:
return|return
name|MISSING
return|;
case|case
literal|6
case|:
return|return
name|WAITING
return|;
default|default:
throw|throw
operator|new
name|ElasticsearchIllegalArgumentException
argument_list|(
literal|"No snapshot state for value ["
operator|+
name|value
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
block|}
DECL|field|entries
specifier|private
specifier|final
name|ImmutableList
argument_list|<
name|Entry
argument_list|>
name|entries
decl_stmt|;
DECL|method|SnapshotMetaData
specifier|public
name|SnapshotMetaData
parameter_list|(
name|ImmutableList
argument_list|<
name|Entry
argument_list|>
name|entries
parameter_list|)
block|{
name|this
operator|.
name|entries
operator|=
name|entries
expr_stmt|;
block|}
DECL|method|SnapshotMetaData
specifier|public
name|SnapshotMetaData
parameter_list|(
name|Entry
modifier|...
name|entries
parameter_list|)
block|{
name|this
operator|.
name|entries
operator|=
name|ImmutableList
operator|.
name|copyOf
argument_list|(
name|entries
argument_list|)
expr_stmt|;
block|}
DECL|method|entries
specifier|public
name|ImmutableList
argument_list|<
name|Entry
argument_list|>
name|entries
parameter_list|()
block|{
return|return
name|this
operator|.
name|entries
return|;
block|}
DECL|method|snapshot
specifier|public
name|Entry
name|snapshot
parameter_list|(
name|SnapshotId
name|snapshotId
parameter_list|)
block|{
for|for
control|(
name|Entry
name|entry
range|:
name|entries
control|)
block|{
if|if
condition|(
name|snapshotId
operator|.
name|equals
argument_list|(
name|entry
operator|.
name|snapshotId
argument_list|()
argument_list|)
condition|)
block|{
return|return
name|entry
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|type
specifier|public
name|String
name|type
parameter_list|()
block|{
return|return
name|TYPE
return|;
block|}
annotation|@
name|Override
DECL|method|readFrom
specifier|public
name|SnapshotMetaData
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|Entry
index|[]
name|entries
init|=
operator|new
name|Entry
index|[
name|in
operator|.
name|readVInt
argument_list|()
index|]
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
name|entries
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|SnapshotId
name|snapshotId
init|=
name|SnapshotId
operator|.
name|readSnapshotId
argument_list|(
name|in
argument_list|)
decl_stmt|;
name|boolean
name|includeGlobalState
init|=
name|in
operator|.
name|readBoolean
argument_list|()
decl_stmt|;
name|State
name|state
init|=
name|State
operator|.
name|fromValue
argument_list|(
name|in
operator|.
name|readByte
argument_list|()
argument_list|)
decl_stmt|;
name|int
name|indices
init|=
name|in
operator|.
name|readVInt
argument_list|()
decl_stmt|;
name|ImmutableList
operator|.
name|Builder
argument_list|<
name|String
argument_list|>
name|indexBuilder
init|=
name|ImmutableList
operator|.
name|builder
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
name|indices
condition|;
name|j
operator|++
control|)
block|{
name|indexBuilder
operator|.
name|add
argument_list|(
name|in
operator|.
name|readString
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|long
name|startTime
init|=
name|in
operator|.
name|readLong
argument_list|()
decl_stmt|;
name|ImmutableMap
operator|.
name|Builder
argument_list|<
name|ShardId
argument_list|,
name|ShardSnapshotStatus
argument_list|>
name|builder
init|=
name|ImmutableMap
operator|.
name|builder
argument_list|()
decl_stmt|;
name|int
name|shards
init|=
name|in
operator|.
name|readVInt
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
name|shards
condition|;
name|j
operator|++
control|)
block|{
name|ShardId
name|shardId
init|=
name|ShardId
operator|.
name|readShardId
argument_list|(
name|in
argument_list|)
decl_stmt|;
name|String
name|nodeId
init|=
name|in
operator|.
name|readOptionalString
argument_list|()
decl_stmt|;
name|State
name|shardState
init|=
name|State
operator|.
name|fromValue
argument_list|(
name|in
operator|.
name|readByte
argument_list|()
argument_list|)
decl_stmt|;
name|builder
operator|.
name|put
argument_list|(
name|shardId
argument_list|,
operator|new
name|ShardSnapshotStatus
argument_list|(
name|nodeId
argument_list|,
name|shardState
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|entries
index|[
name|i
index|]
operator|=
operator|new
name|Entry
argument_list|(
name|snapshotId
argument_list|,
name|includeGlobalState
argument_list|,
name|state
argument_list|,
name|indexBuilder
operator|.
name|build
argument_list|()
argument_list|,
name|startTime
argument_list|,
name|builder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|SnapshotMetaData
argument_list|(
name|entries
argument_list|)
return|;
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
name|out
operator|.
name|writeVInt
argument_list|(
name|entries
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Entry
name|entry
range|:
name|entries
control|)
block|{
name|entry
operator|.
name|snapshotId
argument_list|()
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
name|entry
operator|.
name|includeGlobalState
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeByte
argument_list|(
name|entry
operator|.
name|state
argument_list|()
operator|.
name|value
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|entry
operator|.
name|indices
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|index
range|:
name|entry
operator|.
name|indices
argument_list|()
control|)
block|{
name|out
operator|.
name|writeString
argument_list|(
name|index
argument_list|)
expr_stmt|;
block|}
name|out
operator|.
name|writeLong
argument_list|(
name|entry
operator|.
name|startTime
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|entry
operator|.
name|shards
argument_list|()
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
name|ShardId
argument_list|,
name|ShardSnapshotStatus
argument_list|>
name|shardEntry
range|:
name|entry
operator|.
name|shards
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|shardEntry
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
name|writeOptionalString
argument_list|(
name|shardEntry
operator|.
name|getValue
argument_list|()
operator|.
name|nodeId
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeByte
argument_list|(
name|shardEntry
operator|.
name|getValue
argument_list|()
operator|.
name|state
argument_list|()
operator|.
name|value
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
DECL|method|fromXContent
specifier|public
name|SnapshotMetaData
name|fromXContent
parameter_list|(
name|XContentParser
name|parser
parameter_list|)
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
DECL|method|context
specifier|public
name|EnumSet
argument_list|<
name|MetaData
operator|.
name|XContentContext
argument_list|>
name|context
parameter_list|()
block|{
return|return
name|MetaData
operator|.
name|API_ONLY
return|;
block|}
DECL|class|Fields
specifier|static
specifier|final
class|class
name|Fields
block|{
DECL|field|REPOSITORY
specifier|static
specifier|final
name|XContentBuilderString
name|REPOSITORY
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"repository"
argument_list|)
decl_stmt|;
DECL|field|SNAPSHOTS
specifier|static
specifier|final
name|XContentBuilderString
name|SNAPSHOTS
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"snapshots"
argument_list|)
decl_stmt|;
DECL|field|SNAPSHOT
specifier|static
specifier|final
name|XContentBuilderString
name|SNAPSHOT
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"snapshot"
argument_list|)
decl_stmt|;
DECL|field|INCLUDE_GLOBAL_STATE
specifier|static
specifier|final
name|XContentBuilderString
name|INCLUDE_GLOBAL_STATE
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"include_global_state"
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
DECL|field|START_TIME_MILLIS
specifier|static
specifier|final
name|XContentBuilderString
name|START_TIME_MILLIS
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"start_time_millis"
argument_list|)
decl_stmt|;
DECL|field|START_TIME
specifier|static
specifier|final
name|XContentBuilderString
name|START_TIME
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"start_time"
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
DECL|field|INDEX
specifier|static
specifier|final
name|XContentBuilderString
name|INDEX
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"index"
argument_list|)
decl_stmt|;
DECL|field|SHARD
specifier|static
specifier|final
name|XContentBuilderString
name|SHARD
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"shard"
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
name|ToXContent
operator|.
name|Params
name|params
parameter_list|)
throws|throws
name|IOException
block|{
name|builder
operator|.
name|startArray
argument_list|(
name|Fields
operator|.
name|SNAPSHOTS
argument_list|)
expr_stmt|;
for|for
control|(
name|Entry
name|entry
range|:
name|entries
control|)
block|{
name|toXContent
argument_list|(
name|entry
argument_list|,
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endArray
argument_list|()
expr_stmt|;
return|return
name|builder
return|;
block|}
DECL|method|toXContent
specifier|public
name|void
name|toXContent
parameter_list|(
name|Entry
name|entry
parameter_list|,
name|XContentBuilder
name|builder
parameter_list|,
name|ToXContent
operator|.
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
name|Fields
operator|.
name|REPOSITORY
argument_list|,
name|entry
operator|.
name|snapshotId
argument_list|()
operator|.
name|getRepository
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|SNAPSHOT
argument_list|,
name|entry
operator|.
name|snapshotId
argument_list|()
operator|.
name|getSnapshot
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|INCLUDE_GLOBAL_STATE
argument_list|,
name|entry
operator|.
name|includeGlobalState
argument_list|()
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
name|entry
operator|.
name|state
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|startArray
argument_list|(
name|Fields
operator|.
name|INDICES
argument_list|)
expr_stmt|;
block|{
for|for
control|(
name|String
name|index
range|:
name|entry
operator|.
name|indices
argument_list|()
control|)
block|{
name|builder
operator|.
name|value
argument_list|(
name|index
argument_list|)
expr_stmt|;
block|}
block|}
name|builder
operator|.
name|endArray
argument_list|()
expr_stmt|;
name|builder
operator|.
name|timeValueField
argument_list|(
name|Fields
operator|.
name|START_TIME_MILLIS
argument_list|,
name|Fields
operator|.
name|START_TIME
argument_list|,
name|entry
operator|.
name|startTime
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|startArray
argument_list|(
name|Fields
operator|.
name|SHARDS
argument_list|)
expr_stmt|;
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|ShardId
argument_list|,
name|ShardSnapshotStatus
argument_list|>
name|shardEntry
range|:
name|entry
operator|.
name|shards
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|ShardId
name|shardId
init|=
name|shardEntry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|ShardSnapshotStatus
name|status
init|=
name|shardEntry
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
block|{
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|INDEX
argument_list|,
name|shardId
operator|.
name|getIndex
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|SHARD
argument_list|,
name|shardId
operator|.
name|getId
argument_list|()
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
name|status
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
name|NODE
argument_list|,
name|status
operator|.
name|nodeId
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
block|}
name|builder
operator|.
name|endArray
argument_list|()
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

