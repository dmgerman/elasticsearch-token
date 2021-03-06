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
name|Version
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
name|ShardRouting
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
name|ShardRoutingState
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
name|TestShardRouting
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
name|AllocateUnassignedDecision
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
name|AllocationDecision
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
name|MoveDecision
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
name|ShardAllocationDecision
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
name|io
operator|.
name|stream
operator|.
name|BytesStreamOutput
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
name|XContentFactory
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
name|test
operator|.
name|ESTestCase
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|util
operator|.
name|Collections
operator|.
name|emptyMap
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|util
operator|.
name|Collections
operator|.
name|emptySet
import|;
end_import

begin_comment
comment|/**  * Tests for the cluster allocation explanation  */
end_comment

begin_class
DECL|class|ClusterAllocationExplanationTests
specifier|public
specifier|final
class|class
name|ClusterAllocationExplanationTests
extends|extends
name|ESTestCase
block|{
DECL|method|testDecisionEquality
specifier|public
name|void
name|testDecisionEquality
parameter_list|()
block|{
name|Decision
operator|.
name|Multi
name|d
init|=
operator|new
name|Decision
operator|.
name|Multi
argument_list|()
decl_stmt|;
name|Decision
operator|.
name|Multi
name|d2
init|=
operator|new
name|Decision
operator|.
name|Multi
argument_list|()
decl_stmt|;
name|d
operator|.
name|add
argument_list|(
name|Decision
operator|.
name|single
argument_list|(
name|Decision
operator|.
name|Type
operator|.
name|NO
argument_list|,
literal|"no label"
argument_list|,
literal|"because I said no"
argument_list|)
argument_list|)
expr_stmt|;
name|d
operator|.
name|add
argument_list|(
name|Decision
operator|.
name|single
argument_list|(
name|Decision
operator|.
name|Type
operator|.
name|YES
argument_list|,
literal|"yes label"
argument_list|,
literal|"yes please"
argument_list|)
argument_list|)
expr_stmt|;
name|d
operator|.
name|add
argument_list|(
name|Decision
operator|.
name|single
argument_list|(
name|Decision
operator|.
name|Type
operator|.
name|THROTTLE
argument_list|,
literal|"throttle label"
argument_list|,
literal|"wait a sec"
argument_list|)
argument_list|)
expr_stmt|;
name|d2
operator|.
name|add
argument_list|(
name|Decision
operator|.
name|single
argument_list|(
name|Decision
operator|.
name|Type
operator|.
name|NO
argument_list|,
literal|"no label"
argument_list|,
literal|"because I said no"
argument_list|)
argument_list|)
expr_stmt|;
name|d2
operator|.
name|add
argument_list|(
name|Decision
operator|.
name|single
argument_list|(
name|Decision
operator|.
name|Type
operator|.
name|YES
argument_list|,
literal|"yes label"
argument_list|,
literal|"yes please"
argument_list|)
argument_list|)
expr_stmt|;
name|d2
operator|.
name|add
argument_list|(
name|Decision
operator|.
name|single
argument_list|(
name|Decision
operator|.
name|Type
operator|.
name|THROTTLE
argument_list|,
literal|"throttle label"
argument_list|,
literal|"wait a sec"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|d
argument_list|,
name|d2
argument_list|)
expr_stmt|;
block|}
DECL|method|testExplanationSerialization
specifier|public
name|void
name|testExplanationSerialization
parameter_list|()
throws|throws
name|Exception
block|{
name|ClusterAllocationExplanation
name|cae
init|=
name|randomClusterAllocationExplanation
argument_list|(
name|randomBoolean
argument_list|()
argument_list|)
decl_stmt|;
name|BytesStreamOutput
name|out
init|=
operator|new
name|BytesStreamOutput
argument_list|()
decl_stmt|;
name|cae
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|StreamInput
name|in
init|=
name|out
operator|.
name|bytes
argument_list|()
operator|.
name|streamInput
argument_list|()
decl_stmt|;
name|ClusterAllocationExplanation
name|cae2
init|=
operator|new
name|ClusterAllocationExplanation
argument_list|(
name|in
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|cae
operator|.
name|getShard
argument_list|()
argument_list|,
name|cae2
operator|.
name|getShard
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|cae
operator|.
name|isPrimary
argument_list|()
argument_list|,
name|cae2
operator|.
name|isPrimary
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|cae2
operator|.
name|isPrimary
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|cae
operator|.
name|getUnassignedInfo
argument_list|()
argument_list|,
name|cae2
operator|.
name|getUnassignedInfo
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|cae
operator|.
name|getCurrentNode
argument_list|()
argument_list|,
name|cae2
operator|.
name|getCurrentNode
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|cae
operator|.
name|getShardState
argument_list|()
argument_list|,
name|cae2
operator|.
name|getShardState
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|cae
operator|.
name|getClusterInfo
argument_list|()
operator|==
literal|null
condition|)
block|{
name|assertNull
argument_list|(
name|cae2
operator|.
name|getClusterInfo
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertNotNull
argument_list|(
name|cae2
operator|.
name|getClusterInfo
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|cae
operator|.
name|getClusterInfo
argument_list|()
operator|.
name|getNodeMostAvailableDiskUsages
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|cae2
operator|.
name|getClusterInfo
argument_list|()
operator|.
name|getNodeMostAvailableDiskUsages
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|cae
operator|.
name|getShardAllocationDecision
argument_list|()
operator|.
name|getAllocateDecision
argument_list|()
argument_list|,
name|cae2
operator|.
name|getShardAllocationDecision
argument_list|()
operator|.
name|getAllocateDecision
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|cae
operator|.
name|getShardAllocationDecision
argument_list|()
operator|.
name|getMoveDecision
argument_list|()
argument_list|,
name|cae2
operator|.
name|getShardAllocationDecision
argument_list|()
operator|.
name|getMoveDecision
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testExplanationToXContent
specifier|public
name|void
name|testExplanationToXContent
parameter_list|()
throws|throws
name|Exception
block|{
name|ClusterAllocationExplanation
name|cae
init|=
name|randomClusterAllocationExplanation
argument_list|(
literal|true
argument_list|)
decl_stmt|;
name|XContentBuilder
name|builder
init|=
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
decl_stmt|;
name|cae
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|ToXContent
operator|.
name|EMPTY_PARAMS
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"{\"index\":\"idx\",\"shard\":0,\"primary\":true,\"current_state\":\"started\",\"current_node\":"
operator|+
literal|"{\"id\":\"node-0\",\"name\":\"\",\"transport_address\":\""
operator|+
name|cae
operator|.
name|getCurrentNode
argument_list|()
operator|.
name|getAddress
argument_list|()
operator|+
literal|"\",\"weight_ranking\":3},\"can_remain_on_current_node\":\"yes\",\"can_rebalance_cluster\":\"yes\","
operator|+
literal|"\"can_rebalance_to_other_node\":\"no\",\"rebalance_explanation\":\"cannot rebalance as no target node exists "
operator|+
literal|"that can both allocate this shard and improve the cluster balance\"}"
argument_list|,
name|builder
operator|.
name|string
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|randomClusterAllocationExplanation
specifier|private
specifier|static
name|ClusterAllocationExplanation
name|randomClusterAllocationExplanation
parameter_list|(
name|boolean
name|assignedShard
parameter_list|)
block|{
name|ShardRouting
name|shardRouting
init|=
name|TestShardRouting
operator|.
name|newShardRouting
argument_list|(
operator|new
name|ShardId
argument_list|(
operator|new
name|Index
argument_list|(
literal|"idx"
argument_list|,
literal|"123"
argument_list|)
argument_list|,
literal|0
argument_list|)
argument_list|,
name|assignedShard
condition|?
literal|"node-0"
else|:
literal|null
argument_list|,
literal|true
argument_list|,
name|assignedShard
condition|?
name|ShardRoutingState
operator|.
name|STARTED
else|:
name|ShardRoutingState
operator|.
name|UNASSIGNED
argument_list|)
decl_stmt|;
name|DiscoveryNode
name|node
init|=
name|assignedShard
condition|?
operator|new
name|DiscoveryNode
argument_list|(
literal|"node-0"
argument_list|,
name|buildNewFakeTransportAddress
argument_list|()
argument_list|,
name|emptyMap
argument_list|()
argument_list|,
name|emptySet
argument_list|()
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
else|:
literal|null
decl_stmt|;
name|ShardAllocationDecision
name|shardAllocationDecision
decl_stmt|;
if|if
condition|(
name|assignedShard
condition|)
block|{
name|MoveDecision
name|moveDecision
init|=
name|MoveDecision
operator|.
name|cannotRebalance
argument_list|(
name|Decision
operator|.
name|YES
argument_list|,
name|AllocationDecision
operator|.
name|NO
argument_list|,
literal|3
argument_list|,
literal|null
argument_list|)
operator|.
name|withRemainDecision
argument_list|(
name|Decision
operator|.
name|YES
argument_list|)
decl_stmt|;
name|shardAllocationDecision
operator|=
operator|new
name|ShardAllocationDecision
argument_list|(
name|AllocateUnassignedDecision
operator|.
name|NOT_TAKEN
argument_list|,
name|moveDecision
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|AllocateUnassignedDecision
name|allocateDecision
init|=
name|AllocateUnassignedDecision
operator|.
name|no
argument_list|(
name|UnassignedInfo
operator|.
name|AllocationStatus
operator|.
name|DECIDERS_NO
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|shardAllocationDecision
operator|=
operator|new
name|ShardAllocationDecision
argument_list|(
name|allocateDecision
argument_list|,
name|MoveDecision
operator|.
name|NOT_TAKEN
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|ClusterAllocationExplanation
argument_list|(
name|shardRouting
argument_list|,
name|node
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|shardAllocationDecision
argument_list|)
return|;
block|}
block|}
end_class

end_unit

