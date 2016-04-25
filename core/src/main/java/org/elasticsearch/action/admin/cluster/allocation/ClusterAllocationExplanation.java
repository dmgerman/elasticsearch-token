begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.cluster.allocation
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
name|allocation
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ExceptionsHelper
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
name|shards
operator|.
name|IndicesShardStoresResponse
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
name|node
operator|.
name|DiscoveryNode
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
name|UnassignedInfo
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
name|allocation
operator|.
name|decider
operator|.
name|Decision
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
name|Writeable
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
name|shard
operator|.
name|ShardId
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
name|ShardStateMetaData
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
name|Collections
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
name|HashSet
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

begin_comment
comment|/**  * A {@code ClusterAllocationExplanation} is an explanation of why a shard may or may not be allocated to nodes. It also includes weights  * for where the shard is likely to be assigned. It is an immutable class  */
end_comment

begin_class
DECL|class|ClusterAllocationExplanation
specifier|public
specifier|final
class|class
name|ClusterAllocationExplanation
implements|implements
name|ToXContent
implements|,
name|Writeable
block|{
DECL|field|shard
specifier|private
specifier|final
name|ShardId
name|shard
decl_stmt|;
DECL|field|primary
specifier|private
specifier|final
name|boolean
name|primary
decl_stmt|;
DECL|field|assignedNodeId
specifier|private
specifier|final
name|String
name|assignedNodeId
decl_stmt|;
DECL|field|unassignedInfo
specifier|private
specifier|final
name|UnassignedInfo
name|unassignedInfo
decl_stmt|;
DECL|field|remainingDelayNanos
specifier|private
specifier|final
name|long
name|remainingDelayNanos
decl_stmt|;
DECL|field|activeAllocationIds
specifier|private
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|activeAllocationIds
decl_stmt|;
DECL|field|nodeExplanations
specifier|private
specifier|final
name|Map
argument_list|<
name|DiscoveryNode
argument_list|,
name|NodeExplanation
argument_list|>
name|nodeExplanations
decl_stmt|;
DECL|method|ClusterAllocationExplanation
specifier|public
name|ClusterAllocationExplanation
parameter_list|(
name|ShardId
name|shard
parameter_list|,
name|boolean
name|primary
parameter_list|,
annotation|@
name|Nullable
name|String
name|assignedNodeId
parameter_list|,
name|UnassignedInfo
name|unassignedInfo
parameter_list|,
name|Map
argument_list|<
name|DiscoveryNode
argument_list|,
name|Decision
argument_list|>
name|nodeToDecision
parameter_list|,
name|Map
argument_list|<
name|DiscoveryNode
argument_list|,
name|Float
argument_list|>
name|nodeToWeight
parameter_list|,
name|long
name|remainingDelayNanos
parameter_list|,
name|List
argument_list|<
name|IndicesShardStoresResponse
operator|.
name|StoreStatus
argument_list|>
name|shardStores
parameter_list|,
name|Set
argument_list|<
name|String
argument_list|>
name|activeAllocationIds
parameter_list|)
block|{
name|this
operator|.
name|shard
operator|=
name|shard
expr_stmt|;
name|this
operator|.
name|primary
operator|=
name|primary
expr_stmt|;
name|this
operator|.
name|assignedNodeId
operator|=
name|assignedNodeId
expr_stmt|;
name|this
operator|.
name|unassignedInfo
operator|=
name|unassignedInfo
expr_stmt|;
name|this
operator|.
name|remainingDelayNanos
operator|=
name|remainingDelayNanos
expr_stmt|;
name|this
operator|.
name|activeAllocationIds
operator|=
name|activeAllocationIds
expr_stmt|;
specifier|final
name|Map
argument_list|<
name|DiscoveryNode
argument_list|,
name|Decision
argument_list|>
name|nodeDecisions
init|=
name|nodeToDecision
operator|==
literal|null
condition|?
name|Collections
operator|.
name|emptyMap
argument_list|()
else|:
name|nodeToDecision
decl_stmt|;
specifier|final
name|Map
argument_list|<
name|DiscoveryNode
argument_list|,
name|Float
argument_list|>
name|nodeWeights
init|=
name|nodeToWeight
operator|==
literal|null
condition|?
name|Collections
operator|.
name|emptyMap
argument_list|()
else|:
name|nodeToWeight
decl_stmt|;
assert|assert
name|nodeDecisions
operator|.
name|size
argument_list|()
operator|==
name|nodeWeights
operator|.
name|size
argument_list|()
operator|:
literal|"decision and weight list should be the same size"
assert|;
specifier|final
name|Map
argument_list|<
name|DiscoveryNode
argument_list|,
name|IndicesShardStoresResponse
operator|.
name|StoreStatus
argument_list|>
name|storeStatuses
init|=
name|calculateStoreStatuses
argument_list|(
name|shardStores
argument_list|)
decl_stmt|;
name|this
operator|.
name|nodeExplanations
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|(
name|nodeDecisions
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
name|DiscoveryNode
argument_list|,
name|Decision
argument_list|>
name|entry
range|:
name|nodeDecisions
operator|.
name|entrySet
argument_list|()
control|)
block|{
specifier|final
name|DiscoveryNode
name|node
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
specifier|final
name|Decision
name|decision
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
specifier|final
name|NodeExplanation
name|nodeExplanation
init|=
name|calculateNodeExplanation
argument_list|(
name|node
argument_list|,
name|decision
argument_list|,
name|nodeWeights
operator|.
name|get
argument_list|(
name|node
argument_list|)
argument_list|,
name|storeStatuses
operator|.
name|get
argument_list|(
name|node
argument_list|)
argument_list|,
name|assignedNodeId
argument_list|,
name|activeAllocationIds
argument_list|)
decl_stmt|;
name|nodeExplanations
operator|.
name|put
argument_list|(
name|node
argument_list|,
name|nodeExplanation
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|ClusterAllocationExplanation
specifier|public
name|ClusterAllocationExplanation
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|shard
operator|=
name|ShardId
operator|.
name|readShardId
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|this
operator|.
name|primary
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
name|this
operator|.
name|assignedNodeId
operator|=
name|in
operator|.
name|readOptionalString
argument_list|()
expr_stmt|;
name|this
operator|.
name|unassignedInfo
operator|=
name|in
operator|.
name|readOptionalWriteable
argument_list|(
name|UnassignedInfo
operator|::
operator|new
argument_list|)
expr_stmt|;
name|this
operator|.
name|remainingDelayNanos
operator|=
name|in
operator|.
name|readVLong
argument_list|()
expr_stmt|;
name|int
name|allocIdSize
init|=
name|in
operator|.
name|readVInt
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|activeIds
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|allocIdSize
argument_list|)
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
name|allocIdSize
condition|;
name|i
operator|++
control|)
block|{
name|activeIds
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
name|this
operator|.
name|activeAllocationIds
operator|=
name|activeIds
expr_stmt|;
name|int
name|mapSize
init|=
name|in
operator|.
name|readVInt
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|DiscoveryNode
argument_list|,
name|NodeExplanation
argument_list|>
name|nodeToExplanation
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|(
name|mapSize
argument_list|)
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
name|mapSize
condition|;
name|i
operator|++
control|)
block|{
name|NodeExplanation
name|nodeExplanation
init|=
operator|new
name|NodeExplanation
argument_list|(
name|in
argument_list|)
decl_stmt|;
name|nodeToExplanation
operator|.
name|put
argument_list|(
name|nodeExplanation
operator|.
name|getNode
argument_list|()
argument_list|,
name|nodeExplanation
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|nodeExplanations
operator|=
name|nodeToExplanation
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
name|this
operator|.
name|getShard
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
name|this
operator|.
name|isPrimary
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeOptionalString
argument_list|(
name|this
operator|.
name|getAssignedNodeId
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeOptionalWriteable
argument_list|(
name|this
operator|.
name|getUnassignedInfo
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVLong
argument_list|(
name|remainingDelayNanos
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|activeAllocationIds
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|id
range|:
name|activeAllocationIds
control|)
block|{
name|out
operator|.
name|writeString
argument_list|(
name|id
argument_list|)
expr_stmt|;
block|}
name|out
operator|.
name|writeVInt
argument_list|(
name|this
operator|.
name|nodeExplanations
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|NodeExplanation
name|explanation
range|:
name|this
operator|.
name|nodeExplanations
operator|.
name|values
argument_list|()
control|)
block|{
name|explanation
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|calculateNodeExplanation
specifier|private
name|NodeExplanation
name|calculateNodeExplanation
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|,
name|Decision
name|nodeDecision
parameter_list|,
name|Float
name|nodeWeight
parameter_list|,
name|IndicesShardStoresResponse
operator|.
name|StoreStatus
name|storeStatus
parameter_list|,
name|String
name|assignedNodeId
parameter_list|,
name|Set
argument_list|<
name|String
argument_list|>
name|activeAllocationIds
parameter_list|)
block|{
name|FinalDecision
name|finalDecision
decl_stmt|;
name|StoreCopy
name|storeCopy
decl_stmt|;
name|String
name|finalExplanation
decl_stmt|;
if|if
condition|(
name|node
operator|.
name|getId
argument_list|()
operator|.
name|equals
argument_list|(
name|assignedNodeId
argument_list|)
condition|)
block|{
name|finalDecision
operator|=
name|FinalDecision
operator|.
name|ALREADY_ASSIGNED
expr_stmt|;
name|finalExplanation
operator|=
literal|"the shard is already assigned to this node"
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|nodeDecision
operator|.
name|type
argument_list|()
operator|==
name|Decision
operator|.
name|Type
operator|.
name|NO
condition|)
block|{
name|finalDecision
operator|=
name|FinalDecision
operator|.
name|NO
expr_stmt|;
name|finalExplanation
operator|=
literal|"the shard cannot be assigned because one or more allocation decider returns a 'NO' decision"
expr_stmt|;
block|}
else|else
block|{
name|finalDecision
operator|=
name|FinalDecision
operator|.
name|YES
expr_stmt|;
name|finalExplanation
operator|=
literal|"the shard can be assigned"
expr_stmt|;
block|}
if|if
condition|(
name|storeStatus
operator|!=
literal|null
condition|)
block|{
specifier|final
name|Throwable
name|storeErr
init|=
name|storeStatus
operator|.
name|getStoreException
argument_list|()
decl_stmt|;
if|if
condition|(
name|storeErr
operator|!=
literal|null
condition|)
block|{
name|finalDecision
operator|=
name|FinalDecision
operator|.
name|NO
expr_stmt|;
if|if
condition|(
name|ExceptionsHelper
operator|.
name|unwrapCause
argument_list|(
name|storeErr
argument_list|)
operator|instanceof
name|IOException
condition|)
block|{
name|storeCopy
operator|=
name|StoreCopy
operator|.
name|IO_ERROR
expr_stmt|;
name|finalExplanation
operator|=
literal|"there was an IO error reading from data in the shard store"
expr_stmt|;
block|}
else|else
block|{
name|storeCopy
operator|=
name|StoreCopy
operator|.
name|CORRUPT
expr_stmt|;
name|finalExplanation
operator|=
literal|"the copy of data in the shard store is corrupt"
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|activeAllocationIds
operator|.
name|isEmpty
argument_list|()
operator|||
name|activeAllocationIds
operator|.
name|contains
argument_list|(
name|storeStatus
operator|.
name|getAllocationId
argument_list|()
argument_list|)
condition|)
block|{
comment|// If either we don't have allocation IDs, or they contain the store allocation id, show the allocation
comment|// status
name|storeCopy
operator|=
name|StoreCopy
operator|.
name|AVAILABLE
expr_stmt|;
name|finalExplanation
operator|=
literal|"the shard can be assigned and the node contains a valid copy of the shard data"
expr_stmt|;
block|}
else|else
block|{
comment|// Otherwise, this is a stale copy of the data (allocation ids don't match)
name|storeCopy
operator|=
name|StoreCopy
operator|.
name|STALE
expr_stmt|;
name|finalExplanation
operator|=
literal|"the copy of the shard is stale, allocation ids do not match"
expr_stmt|;
name|finalDecision
operator|=
name|FinalDecision
operator|.
name|NO
expr_stmt|;
block|}
block|}
else|else
block|{
comment|// No copies of the data, so deciders are what influence the decision and explanation
name|storeCopy
operator|=
name|StoreCopy
operator|.
name|NONE
expr_stmt|;
block|}
return|return
operator|new
name|NodeExplanation
argument_list|(
name|node
argument_list|,
name|nodeDecision
argument_list|,
name|nodeWeight
argument_list|,
name|storeStatus
argument_list|,
name|finalDecision
argument_list|,
name|finalExplanation
argument_list|,
name|storeCopy
argument_list|)
return|;
block|}
DECL|method|calculateStoreStatuses
specifier|private
specifier|static
name|Map
argument_list|<
name|DiscoveryNode
argument_list|,
name|IndicesShardStoresResponse
operator|.
name|StoreStatus
argument_list|>
name|calculateStoreStatuses
parameter_list|(
name|List
argument_list|<
name|IndicesShardStoresResponse
operator|.
name|StoreStatus
argument_list|>
name|shardStores
parameter_list|)
block|{
name|Map
argument_list|<
name|DiscoveryNode
argument_list|,
name|IndicesShardStoresResponse
operator|.
name|StoreStatus
argument_list|>
name|nodeToStatus
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|(
name|shardStores
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|IndicesShardStoresResponse
operator|.
name|StoreStatus
name|status
range|:
name|shardStores
control|)
block|{
name|nodeToStatus
operator|.
name|put
argument_list|(
name|status
operator|.
name|getNode
argument_list|()
argument_list|,
name|status
argument_list|)
expr_stmt|;
block|}
return|return
name|nodeToStatus
return|;
block|}
comment|/** Return the shard that the explanation is about */
DECL|method|getShard
specifier|public
name|ShardId
name|getShard
parameter_list|()
block|{
return|return
name|this
operator|.
name|shard
return|;
block|}
comment|/** Return true if the explained shard is primary, false otherwise */
DECL|method|isPrimary
specifier|public
name|boolean
name|isPrimary
parameter_list|()
block|{
return|return
name|this
operator|.
name|primary
return|;
block|}
comment|/** Return turn if the shard is assigned to a node */
DECL|method|isAssigned
specifier|public
name|boolean
name|isAssigned
parameter_list|()
block|{
return|return
name|this
operator|.
name|assignedNodeId
operator|!=
literal|null
return|;
block|}
comment|/** Return the assigned node id or null if not assigned */
annotation|@
name|Nullable
DECL|method|getAssignedNodeId
specifier|public
name|String
name|getAssignedNodeId
parameter_list|()
block|{
return|return
name|this
operator|.
name|assignedNodeId
return|;
block|}
comment|/** Return the unassigned info for the shard or null if the shard is assigned */
annotation|@
name|Nullable
DECL|method|getUnassignedInfo
specifier|public
name|UnassignedInfo
name|getUnassignedInfo
parameter_list|()
block|{
return|return
name|this
operator|.
name|unassignedInfo
return|;
block|}
comment|/** Return the remaining allocation delay for this shard in nanoseconds */
DECL|method|getRemainingDelayNanos
specifier|public
name|long
name|getRemainingDelayNanos
parameter_list|()
block|{
return|return
name|this
operator|.
name|remainingDelayNanos
return|;
block|}
comment|/** Return a set of the active allocation ids for this shard */
DECL|method|getActiveAllocationIds
specifier|public
name|Set
argument_list|<
name|String
argument_list|>
name|getActiveAllocationIds
parameter_list|()
block|{
return|return
name|this
operator|.
name|activeAllocationIds
return|;
block|}
comment|/** Return a map of node to the explanation for that node */
DECL|method|getNodeExplanations
specifier|public
name|Map
argument_list|<
name|DiscoveryNode
argument_list|,
name|NodeExplanation
argument_list|>
name|getNodeExplanations
parameter_list|()
block|{
return|return
name|this
operator|.
name|nodeExplanations
return|;
block|}
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
argument_list|()
expr_stmt|;
block|{
name|builder
operator|.
name|startObject
argument_list|(
literal|"shard"
argument_list|)
expr_stmt|;
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"index"
argument_list|,
name|shard
operator|.
name|getIndexName
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"index_uuid"
argument_list|,
name|shard
operator|.
name|getIndex
argument_list|()
operator|.
name|getUUID
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"id"
argument_list|,
name|shard
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"primary"
argument_list|,
name|primary
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
comment|// end shard
name|builder
operator|.
name|field
argument_list|(
literal|"assigned"
argument_list|,
name|this
operator|.
name|assignedNodeId
operator|!=
literal|null
argument_list|)
expr_stmt|;
comment|// If assigned, show the node id of the node it's assigned to
if|if
condition|(
name|assignedNodeId
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"assigned_node_id"
argument_list|,
name|this
operator|.
name|assignedNodeId
argument_list|)
expr_stmt|;
block|}
comment|// If we have unassigned info, show that
if|if
condition|(
name|unassignedInfo
operator|!=
literal|null
condition|)
block|{
name|unassignedInfo
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
name|long
name|delay
init|=
name|unassignedInfo
operator|.
name|getLastComputedLeftDelayNanos
argument_list|()
decl_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"allocation_delay"
argument_list|,
name|TimeValue
operator|.
name|timeValueNanos
argument_list|(
name|delay
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"allocation_delay_ms"
argument_list|,
name|TimeValue
operator|.
name|timeValueNanos
argument_list|(
name|delay
argument_list|)
operator|.
name|millis
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"remaining_delay"
argument_list|,
name|TimeValue
operator|.
name|timeValueNanos
argument_list|(
name|remainingDelayNanos
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"remaining_delay_ms"
argument_list|,
name|TimeValue
operator|.
name|timeValueNanos
argument_list|(
name|remainingDelayNanos
argument_list|)
operator|.
name|millis
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|startObject
argument_list|(
literal|"nodes"
argument_list|)
expr_stmt|;
for|for
control|(
name|NodeExplanation
name|explanation
range|:
name|nodeExplanations
operator|.
name|values
argument_list|()
control|)
block|{
name|explanation
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
comment|// end nodes
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
comment|// end wrapping object
return|return
name|builder
return|;
block|}
comment|/** The cluster allocation explanation for a single node */
DECL|class|NodeExplanation
specifier|public
class|class
name|NodeExplanation
implements|implements
name|Writeable
implements|,
name|ToXContent
block|{
DECL|field|node
specifier|private
specifier|final
name|DiscoveryNode
name|node
decl_stmt|;
DECL|field|nodeDecision
specifier|private
specifier|final
name|Decision
name|nodeDecision
decl_stmt|;
DECL|field|nodeWeight
specifier|private
specifier|final
name|Float
name|nodeWeight
decl_stmt|;
DECL|field|storeStatus
specifier|private
specifier|final
name|IndicesShardStoresResponse
operator|.
name|StoreStatus
name|storeStatus
decl_stmt|;
DECL|field|finalDecision
specifier|private
specifier|final
name|FinalDecision
name|finalDecision
decl_stmt|;
DECL|field|finalExplanation
specifier|private
specifier|final
name|String
name|finalExplanation
decl_stmt|;
DECL|field|storeCopy
specifier|private
specifier|final
name|StoreCopy
name|storeCopy
decl_stmt|;
DECL|method|NodeExplanation
specifier|public
name|NodeExplanation
parameter_list|(
specifier|final
name|DiscoveryNode
name|node
parameter_list|,
specifier|final
name|Decision
name|nodeDecision
parameter_list|,
specifier|final
name|Float
name|nodeWeight
parameter_list|,
specifier|final
annotation|@
name|Nullable
name|IndicesShardStoresResponse
operator|.
name|StoreStatus
name|storeStatus
parameter_list|,
specifier|final
name|FinalDecision
name|finalDecision
parameter_list|,
specifier|final
name|String
name|finalExplanation
parameter_list|,
specifier|final
name|StoreCopy
name|storeCopy
parameter_list|)
block|{
name|this
operator|.
name|node
operator|=
name|node
expr_stmt|;
name|this
operator|.
name|nodeDecision
operator|=
name|nodeDecision
expr_stmt|;
name|this
operator|.
name|nodeWeight
operator|=
name|nodeWeight
expr_stmt|;
name|this
operator|.
name|storeStatus
operator|=
name|storeStatus
expr_stmt|;
name|this
operator|.
name|finalDecision
operator|=
name|finalDecision
expr_stmt|;
name|this
operator|.
name|finalExplanation
operator|=
name|finalExplanation
expr_stmt|;
name|this
operator|.
name|storeCopy
operator|=
name|storeCopy
expr_stmt|;
block|}
DECL|method|NodeExplanation
specifier|public
name|NodeExplanation
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|node
operator|=
operator|new
name|DiscoveryNode
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|this
operator|.
name|nodeDecision
operator|=
name|Decision
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|this
operator|.
name|nodeWeight
operator|=
name|in
operator|.
name|readFloat
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
name|this
operator|.
name|storeStatus
operator|=
name|IndicesShardStoresResponse
operator|.
name|StoreStatus
operator|.
name|readStoreStatus
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|storeStatus
operator|=
literal|null
expr_stmt|;
block|}
name|this
operator|.
name|finalDecision
operator|=
name|FinalDecision
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|this
operator|.
name|finalExplanation
operator|=
name|in
operator|.
name|readString
argument_list|()
expr_stmt|;
name|this
operator|.
name|storeCopy
operator|=
name|StoreCopy
operator|.
name|readFrom
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
name|node
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|Decision
operator|.
name|writeTo
argument_list|(
name|nodeDecision
argument_list|,
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeFloat
argument_list|(
name|nodeWeight
argument_list|)
expr_stmt|;
if|if
condition|(
name|storeStatus
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
name|storeStatus
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
name|finalDecision
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeString
argument_list|(
name|finalExplanation
argument_list|)
expr_stmt|;
name|storeCopy
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
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
name|node
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"node_name"
argument_list|,
name|node
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|startObject
argument_list|(
literal|"node_attributes"
argument_list|)
expr_stmt|;
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|attrEntry
range|:
name|node
operator|.
name|getAttributes
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|attrEntry
operator|.
name|getKey
argument_list|()
argument_list|,
name|attrEntry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
comment|// end attributes
name|builder
operator|.
name|startObject
argument_list|(
literal|"store"
argument_list|)
expr_stmt|;
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"shard_copy"
argument_list|,
name|storeCopy
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|storeStatus
operator|!=
literal|null
condition|)
block|{
specifier|final
name|Throwable
name|storeErr
init|=
name|storeStatus
operator|.
name|getStoreException
argument_list|()
decl_stmt|;
if|if
condition|(
name|storeErr
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"store_exception"
argument_list|,
name|ExceptionsHelper
operator|.
name|detailedMessage
argument_list|(
name|storeErr
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
comment|// end store
name|builder
operator|.
name|field
argument_list|(
literal|"final_decision"
argument_list|,
name|finalDecision
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"final_explanation"
argument_list|,
name|finalExplanation
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"weight"
argument_list|,
name|nodeWeight
argument_list|)
expr_stmt|;
name|nodeDecision
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
comment|// end node<uuid>
return|return
name|builder
return|;
block|}
DECL|method|getNode
specifier|public
name|DiscoveryNode
name|getNode
parameter_list|()
block|{
return|return
name|this
operator|.
name|node
return|;
block|}
DECL|method|getDecision
specifier|public
name|Decision
name|getDecision
parameter_list|()
block|{
return|return
name|this
operator|.
name|nodeDecision
return|;
block|}
DECL|method|getWeight
specifier|public
name|Float
name|getWeight
parameter_list|()
block|{
return|return
name|this
operator|.
name|nodeWeight
return|;
block|}
annotation|@
name|Nullable
DECL|method|getStoreStatus
specifier|public
name|IndicesShardStoresResponse
operator|.
name|StoreStatus
name|getStoreStatus
parameter_list|()
block|{
return|return
name|this
operator|.
name|storeStatus
return|;
block|}
DECL|method|getFinalDecision
specifier|public
name|FinalDecision
name|getFinalDecision
parameter_list|()
block|{
return|return
name|this
operator|.
name|finalDecision
return|;
block|}
DECL|method|getFinalExplanation
specifier|public
name|String
name|getFinalExplanation
parameter_list|()
block|{
return|return
name|this
operator|.
name|finalExplanation
return|;
block|}
DECL|method|getStoreCopy
specifier|public
name|StoreCopy
name|getStoreCopy
parameter_list|()
block|{
return|return
name|this
operator|.
name|storeCopy
return|;
block|}
block|}
comment|/** An Enum representing the final decision for a shard allocation on a node */
DECL|enum|FinalDecision
specifier|public
enum|enum
name|FinalDecision
block|{
comment|// Yes, the shard can be assigned
DECL|enum constant|YES
name|YES
argument_list|(
operator|(
name|byte
operator|)
literal|0
argument_list|)
block|,
comment|// No, the shard cannot be assigned
DECL|enum constant|NO
name|NO
argument_list|(
operator|(
name|byte
operator|)
literal|1
argument_list|)
block|,
comment|// The shard is already assigned to this node
DECL|enum constant|ALREADY_ASSIGNED
name|ALREADY_ASSIGNED
argument_list|(
operator|(
name|byte
operator|)
literal|2
argument_list|)
block|;
DECL|field|id
specifier|private
specifier|final
name|byte
name|id
decl_stmt|;
DECL|method|FinalDecision
name|FinalDecision
parameter_list|(
name|byte
name|id
parameter_list|)
block|{
name|this
operator|.
name|id
operator|=
name|id
expr_stmt|;
block|}
DECL|method|fromId
specifier|private
specifier|static
name|FinalDecision
name|fromId
parameter_list|(
name|byte
name|id
parameter_list|)
block|{
switch|switch
condition|(
name|id
condition|)
block|{
case|case
literal|0
case|:
return|return
name|YES
return|;
case|case
literal|1
case|:
return|return
name|NO
return|;
case|case
literal|2
case|:
return|return
name|ALREADY_ASSIGNED
return|;
default|default:
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"unknown id for final decision: ["
operator|+
name|id
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
switch|switch
condition|(
name|id
condition|)
block|{
case|case
literal|0
case|:
return|return
literal|"YES"
return|;
case|case
literal|1
case|:
return|return
literal|"NO"
return|;
case|case
literal|2
case|:
return|return
literal|"ALREADY_ASSIGNED"
return|;
default|default:
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"unknown id for final decision: ["
operator|+
name|id
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
DECL|method|readFrom
specifier|static
name|FinalDecision
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|fromId
argument_list|(
name|in
operator|.
name|readByte
argument_list|()
argument_list|)
return|;
block|}
DECL|method|writeTo
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
name|writeByte
argument_list|(
name|id
argument_list|)
expr_stmt|;
block|}
block|}
comment|/** An Enum representing the state of the shard store's copy of the data on a node */
DECL|enum|StoreCopy
specifier|public
enum|enum
name|StoreCopy
block|{
comment|// No data for this shard is on the node
DECL|enum constant|NONE
name|NONE
argument_list|(
operator|(
name|byte
operator|)
literal|0
argument_list|)
block|,
comment|// A copy of the data is available on this node
DECL|enum constant|AVAILABLE
name|AVAILABLE
argument_list|(
operator|(
name|byte
operator|)
literal|1
argument_list|)
block|,
comment|// The copy of the data on the node is corrupt
DECL|enum constant|CORRUPT
name|CORRUPT
argument_list|(
operator|(
name|byte
operator|)
literal|2
argument_list|)
block|,
comment|// There was an error reading this node's copy of the data
DECL|enum constant|IO_ERROR
name|IO_ERROR
argument_list|(
operator|(
name|byte
operator|)
literal|3
argument_list|)
block|,
comment|// The copy of the data on the node is stale
DECL|enum constant|STALE
name|STALE
argument_list|(
operator|(
name|byte
operator|)
literal|4
argument_list|)
block|;
DECL|field|id
specifier|private
specifier|final
name|byte
name|id
decl_stmt|;
DECL|method|StoreCopy
name|StoreCopy
parameter_list|(
name|byte
name|id
parameter_list|)
block|{
name|this
operator|.
name|id
operator|=
name|id
expr_stmt|;
block|}
DECL|method|fromId
specifier|private
specifier|static
name|StoreCopy
name|fromId
parameter_list|(
name|byte
name|id
parameter_list|)
block|{
switch|switch
condition|(
name|id
condition|)
block|{
case|case
literal|0
case|:
return|return
name|NONE
return|;
case|case
literal|1
case|:
return|return
name|AVAILABLE
return|;
case|case
literal|2
case|:
return|return
name|CORRUPT
return|;
case|case
literal|3
case|:
return|return
name|IO_ERROR
return|;
case|case
literal|4
case|:
return|return
name|STALE
return|;
default|default:
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"unknown id for store copy: ["
operator|+
name|id
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
switch|switch
condition|(
name|id
condition|)
block|{
case|case
literal|0
case|:
return|return
literal|"NONE"
return|;
case|case
literal|1
case|:
return|return
literal|"AVAILABLE"
return|;
case|case
literal|2
case|:
return|return
literal|"CORRUPT"
return|;
case|case
literal|3
case|:
return|return
literal|"IO_ERROR"
return|;
case|case
literal|4
case|:
return|return
literal|"STALE"
return|;
default|default:
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"unknown id for store copy: ["
operator|+
name|id
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
DECL|method|readFrom
specifier|static
name|StoreCopy
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|fromId
argument_list|(
name|in
operator|.
name|readByte
argument_list|()
argument_list|)
return|;
block|}
DECL|method|writeTo
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
name|writeByte
argument_list|(
name|id
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

