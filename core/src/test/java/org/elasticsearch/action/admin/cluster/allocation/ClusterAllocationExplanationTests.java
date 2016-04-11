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
name|ElasticsearchException
import|;
end_import

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
name|Version
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
name|transport
operator|.
name|DummyTransportAddress
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
name|ShardId
name|shard
init|=
operator|new
name|ShardId
argument_list|(
literal|"test"
argument_list|,
literal|"uuid"
argument_list|,
literal|0
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|DiscoveryNode
argument_list|,
name|Decision
argument_list|>
name|nodeToDecisions
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|DiscoveryNode
argument_list|,
name|Float
argument_list|>
name|nodeToWeight
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
name|randomIntBetween
argument_list|(
literal|2
argument_list|,
literal|5
argument_list|)
init|;
name|i
operator|>
literal|0
condition|;
name|i
operator|--
control|)
block|{
name|DiscoveryNode
name|dn
init|=
operator|new
name|DiscoveryNode
argument_list|(
literal|"node-"
operator|+
name|i
argument_list|,
name|DummyTransportAddress
operator|.
name|INSTANCE
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
decl_stmt|;
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
name|nodeToDecisions
operator|.
name|put
argument_list|(
name|dn
argument_list|,
name|d
argument_list|)
expr_stmt|;
name|nodeToWeight
operator|.
name|put
argument_list|(
name|dn
argument_list|,
name|randomFloat
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|long
name|remainingDelay
init|=
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|500
argument_list|)
decl_stmt|;
name|DiscoveryNode
name|nodeWithStore
init|=
operator|new
name|DiscoveryNode
argument_list|(
literal|"node-1"
argument_list|,
name|DummyTransportAddress
operator|.
name|INSTANCE
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
decl_stmt|;
name|IndicesShardStoresResponse
operator|.
name|StoreStatus
name|storeStatus
init|=
operator|new
name|IndicesShardStoresResponse
operator|.
name|StoreStatus
argument_list|(
name|nodeWithStore
argument_list|,
literal|42
argument_list|,
literal|"eggplant"
argument_list|,
name|IndicesShardStoresResponse
operator|.
name|StoreStatus
operator|.
name|AllocationStatus
operator|.
name|PRIMARY
argument_list|,
operator|new
name|ElasticsearchException
argument_list|(
literal|"stuff's broke, yo"
argument_list|)
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|IndicesShardStoresResponse
operator|.
name|StoreStatus
argument_list|>
name|storeStatusList
init|=
name|Collections
operator|.
name|singletonList
argument_list|(
name|storeStatus
argument_list|)
decl_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|allocationIds
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
name|allocationIds
operator|.
name|add
argument_list|(
literal|"eggplant"
argument_list|)
expr_stmt|;
name|allocationIds
operator|.
name|add
argument_list|(
literal|"potato"
argument_list|)
expr_stmt|;
name|ClusterAllocationExplanation
name|cae
init|=
operator|new
name|ClusterAllocationExplanation
argument_list|(
name|shard
argument_list|,
literal|true
argument_list|,
literal|"assignedNode"
argument_list|,
literal|null
argument_list|,
name|nodeToDecisions
argument_list|,
name|nodeToWeight
argument_list|,
name|remainingDelay
argument_list|,
name|storeStatusList
argument_list|,
name|allocationIds
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
name|StreamInput
operator|.
name|wrap
argument_list|(
name|out
operator|.
name|bytes
argument_list|()
argument_list|)
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
name|shard
argument_list|,
name|cae2
operator|.
name|getShard
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
name|assertTrue
argument_list|(
name|cae2
operator|.
name|isAssigned
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"assignedNode"
argument_list|,
name|cae2
operator|.
name|getAssignedNodeId
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|cae2
operator|.
name|getUnassignedInfo
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
name|cae2
operator|.
name|getNodeDecisions
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|assertEquals
argument_list|(
name|nodeToDecisions
operator|.
name|get
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|DiscoveryNode
argument_list|,
name|IndicesShardStoresResponse
operator|.
name|StoreStatus
argument_list|>
name|entry
range|:
name|cae2
operator|.
name|getNodeStoreStatus
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|assertEquals
argument_list|(
name|nodeWithStore
argument_list|,
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
name|IndicesShardStoresResponse
operator|.
name|StoreStatus
name|status
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|storeStatus
operator|.
name|getLegacyVersion
argument_list|()
argument_list|,
name|status
operator|.
name|getLegacyVersion
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|storeStatus
operator|.
name|getAllocationId
argument_list|()
argument_list|,
name|status
operator|.
name|getAllocationId
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|storeStatus
operator|.
name|getAllocationStatus
argument_list|()
argument_list|,
name|status
operator|.
name|getAllocationStatus
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|ExceptionsHelper
operator|.
name|detailedMessage
argument_list|(
name|storeStatus
operator|.
name|getStoreException
argument_list|()
argument_list|)
argument_list|,
name|ExceptionsHelper
operator|.
name|detailedMessage
argument_list|(
name|status
operator|.
name|getStoreException
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|nodeToWeight
argument_list|,
name|cae2
operator|.
name|getNodeWeights
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|remainingDelay
argument_list|,
name|cae2
operator|.
name|getRemainingDelayNanos
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|allocationIds
argument_list|,
name|cae2
operator|.
name|getActiveAllocationIds
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testStaleShardExplanation
specifier|public
name|void
name|testStaleShardExplanation
parameter_list|()
throws|throws
name|Exception
block|{
name|ShardId
name|shard
init|=
operator|new
name|ShardId
argument_list|(
literal|"test"
argument_list|,
literal|"uuid"
argument_list|,
literal|0
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|DiscoveryNode
argument_list|,
name|Decision
argument_list|>
name|nodeToDecisions
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|DiscoveryNode
argument_list|,
name|Float
argument_list|>
name|nodeToWeight
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|DiscoveryNode
name|dn
init|=
operator|new
name|DiscoveryNode
argument_list|(
literal|"node1"
argument_list|,
name|DummyTransportAddress
operator|.
name|INSTANCE
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
decl_stmt|;
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
name|nodeToDecisions
operator|.
name|put
argument_list|(
name|dn
argument_list|,
name|d
argument_list|)
expr_stmt|;
name|nodeToWeight
operator|.
name|put
argument_list|(
name|dn
argument_list|,
literal|1.5f
argument_list|)
expr_stmt|;
name|long
name|remainingDelay
init|=
literal|42
decl_stmt|;
name|DiscoveryNode
name|nodeWithStore
init|=
operator|new
name|DiscoveryNode
argument_list|(
literal|"node1"
argument_list|,
name|DummyTransportAddress
operator|.
name|INSTANCE
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
decl_stmt|;
name|IndicesShardStoresResponse
operator|.
name|StoreStatus
name|storeStatus
init|=
operator|new
name|IndicesShardStoresResponse
operator|.
name|StoreStatus
argument_list|(
name|nodeWithStore
argument_list|,
literal|42
argument_list|,
literal|"eggplant"
argument_list|,
name|IndicesShardStoresResponse
operator|.
name|StoreStatus
operator|.
name|AllocationStatus
operator|.
name|PRIMARY
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|IndicesShardStoresResponse
operator|.
name|StoreStatus
argument_list|>
name|storeStatusList
init|=
name|Collections
operator|.
name|singletonList
argument_list|(
name|storeStatus
argument_list|)
decl_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|allocationIds
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
name|allocationIds
operator|.
name|add
argument_list|(
literal|"potato"
argument_list|)
expr_stmt|;
name|ClusterAllocationExplanation
name|cae
init|=
operator|new
name|ClusterAllocationExplanation
argument_list|(
name|shard
argument_list|,
literal|true
argument_list|,
literal|"assignedNode"
argument_list|,
literal|null
argument_list|,
name|nodeToDecisions
argument_list|,
name|nodeToWeight
argument_list|,
name|remainingDelay
argument_list|,
name|storeStatusList
argument_list|,
name|allocationIds
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
literal|"{\"shard\":{\"index\":\"test\",\"index_uuid\":\"uuid\",\"id\":0,\"primary\":true},"
operator|+
literal|"\"assigned\":true,\"assigned_node_id\":\"assignedNode\","
operator|+
literal|"\"nodes\":{\"node1\":{\"node_name\":\"\",\"node_attributes\":{},\"store\":{\"shard_copy\":\"STALE_COPY\"},"
operator|+
literal|"\"final_decision\":\"STORE_STALE\",\"weight\":1.5,\"decisions\":[{\"decider\":\"no label\",\"decision\":\"NO\","
operator|+
literal|"\"explanation\":\"because I said no\"},{\"decider\":\"yes label\",\"decision\":\"YES\","
operator|+
literal|"\"explanation\":\"yes please\"},{\"decider\":\"throttle label\",\"decision\":\"THROTTLE\","
operator|+
literal|"\"explanation\":\"wait a sec\"}]}}}"
argument_list|,
name|builder
operator|.
name|string
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

