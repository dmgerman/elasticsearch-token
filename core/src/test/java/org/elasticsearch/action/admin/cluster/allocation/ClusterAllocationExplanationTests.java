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
name|apache
operator|.
name|lucene
operator|.
name|index
operator|.
name|CorruptIndexException
import|;
end_import

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
name|metadata
operator|.
name|IndexMetaData
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
name|settings
operator|.
name|Settings
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
name|LocalTransportAddress
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
name|util
operator|.
name|set
operator|.
name|Sets
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
DECL|field|i
specifier|private
name|Index
name|i
init|=
operator|new
name|Index
argument_list|(
literal|"foo"
argument_list|,
literal|"uuid"
argument_list|)
decl_stmt|;
DECL|field|primaryShard
specifier|private
name|ShardRouting
name|primaryShard
init|=
name|ShardRouting
operator|.
name|newUnassigned
argument_list|(
operator|new
name|ShardId
argument_list|(
name|i
argument_list|,
literal|0
argument_list|)
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|,
operator|new
name|UnassignedInfo
argument_list|(
name|UnassignedInfo
operator|.
name|Reason
operator|.
name|INDEX_CREATED
argument_list|,
literal|"foo"
argument_list|)
argument_list|)
decl_stmt|;
DECL|field|indexMetaData
specifier|private
name|IndexMetaData
name|indexMetaData
init|=
name|IndexMetaData
operator|.
name|builder
argument_list|(
literal|"foo"
argument_list|)
operator|.
name|settings
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_VERSION_CREATED
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_INDEX_UUID
argument_list|,
literal|"uuid"
argument_list|)
argument_list|)
operator|.
name|putInSyncAllocationIds
argument_list|(
literal|0
argument_list|,
name|Sets
operator|.
name|newHashSet
argument_list|(
literal|"aid1"
argument_list|,
literal|"aid2"
argument_list|)
argument_list|)
operator|.
name|numberOfShards
argument_list|(
literal|1
argument_list|)
operator|.
name|numberOfReplicas
argument_list|(
literal|1
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
DECL|field|node
specifier|private
name|DiscoveryNode
name|node
init|=
operator|new
name|DiscoveryNode
argument_list|(
literal|"node-0"
argument_list|,
name|LocalTransportAddress
operator|.
name|buildUnique
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
decl_stmt|;
DECL|field|yesDecision
specifier|private
specifier|static
name|Decision
operator|.
name|Multi
name|yesDecision
init|=
operator|new
name|Decision
operator|.
name|Multi
argument_list|()
decl_stmt|;
DECL|field|noDecision
specifier|private
specifier|static
name|Decision
operator|.
name|Multi
name|noDecision
init|=
operator|new
name|Decision
operator|.
name|Multi
argument_list|()
decl_stmt|;
static|static
block|{
name|yesDecision
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
name|noDecision
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
literal|"no thanks"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|assertExplanations
specifier|private
name|void
name|assertExplanations
parameter_list|(
name|NodeExplanation
name|ne
parameter_list|,
name|String
name|finalExplanation
parameter_list|,
name|ClusterAllocationExplanation
operator|.
name|FinalDecision
name|finalDecision
parameter_list|,
name|ClusterAllocationExplanation
operator|.
name|StoreCopy
name|storeCopy
parameter_list|)
block|{
name|assertEquals
argument_list|(
name|finalExplanation
argument_list|,
name|ne
operator|.
name|getFinalExplanation
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|finalDecision
argument_list|,
name|ne
operator|.
name|getFinalDecision
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|storeCopy
argument_list|,
name|ne
operator|.
name|getStoreCopy
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testDecisionAndExplanation
specifier|public
name|void
name|testDecisionAndExplanation
parameter_list|()
block|{
name|Exception
name|e
init|=
operator|new
name|IOException
argument_list|(
literal|"stuff's broke, yo"
argument_list|)
decl_stmt|;
name|Exception
name|corruptE
init|=
operator|new
name|CorruptIndexException
argument_list|(
literal|"stuff's corrupt, yo"
argument_list|,
literal|""
argument_list|)
decl_stmt|;
name|Float
name|nodeWeight
init|=
name|randomFloat
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|activeAllocationIds
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
name|activeAllocationIds
operator|.
name|add
argument_list|(
literal|"eggplant"
argument_list|)
expr_stmt|;
name|ShardRouting
name|primaryStartedShard
init|=
name|ShardRouting
operator|.
name|newUnassigned
argument_list|(
operator|new
name|ShardId
argument_list|(
name|i
argument_list|,
literal|0
argument_list|)
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|,
operator|new
name|UnassignedInfo
argument_list|(
name|UnassignedInfo
operator|.
name|Reason
operator|.
name|INDEX_REOPENED
argument_list|,
literal|"foo"
argument_list|)
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|primaryStartedShard
operator|.
name|allocatedPostIndexCreate
argument_list|(
name|indexMetaData
argument_list|)
argument_list|)
expr_stmt|;
name|ShardRouting
name|replicaStartedShard
init|=
name|ShardRouting
operator|.
name|newUnassigned
argument_list|(
operator|new
name|ShardId
argument_list|(
name|i
argument_list|,
literal|0
argument_list|)
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|,
operator|new
name|UnassignedInfo
argument_list|(
name|UnassignedInfo
operator|.
name|Reason
operator|.
name|INDEX_REOPENED
argument_list|,
literal|"foo"
argument_list|)
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|replicaStartedShard
operator|.
name|allocatedPostIndexCreate
argument_list|(
name|indexMetaData
argument_list|)
argument_list|)
expr_stmt|;
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
name|node
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
name|e
argument_list|)
decl_stmt|;
name|NodeExplanation
name|ne
init|=
name|TransportClusterAllocationExplainAction
operator|.
name|calculateNodeExplanation
argument_list|(
name|primaryShard
argument_list|,
name|indexMetaData
argument_list|,
name|node
argument_list|,
name|yesDecision
argument_list|,
name|nodeWeight
argument_list|,
name|storeStatus
argument_list|,
literal|""
argument_list|,
name|activeAllocationIds
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|assertExplanations
argument_list|(
name|ne
argument_list|,
literal|"the copy of the shard cannot be read"
argument_list|,
name|ClusterAllocationExplanation
operator|.
name|FinalDecision
operator|.
name|NO
argument_list|,
name|ClusterAllocationExplanation
operator|.
name|StoreCopy
operator|.
name|IO_ERROR
argument_list|)
expr_stmt|;
name|ne
operator|=
name|TransportClusterAllocationExplainAction
operator|.
name|calculateNodeExplanation
argument_list|(
name|primaryShard
argument_list|,
name|indexMetaData
argument_list|,
name|node
argument_list|,
name|yesDecision
argument_list|,
name|nodeWeight
argument_list|,
literal|null
argument_list|,
literal|""
argument_list|,
name|activeAllocationIds
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|assertExplanations
argument_list|(
name|ne
argument_list|,
literal|"the shard can be assigned"
argument_list|,
name|ClusterAllocationExplanation
operator|.
name|FinalDecision
operator|.
name|YES
argument_list|,
name|ClusterAllocationExplanation
operator|.
name|StoreCopy
operator|.
name|NONE
argument_list|)
expr_stmt|;
name|ne
operator|=
name|TransportClusterAllocationExplainAction
operator|.
name|calculateNodeExplanation
argument_list|(
name|primaryStartedShard
argument_list|,
name|indexMetaData
argument_list|,
name|node
argument_list|,
name|yesDecision
argument_list|,
name|nodeWeight
argument_list|,
literal|null
argument_list|,
literal|""
argument_list|,
name|activeAllocationIds
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|assertExplanations
argument_list|(
name|ne
argument_list|,
literal|"there is no copy of the shard available"
argument_list|,
name|ClusterAllocationExplanation
operator|.
name|FinalDecision
operator|.
name|NO
argument_list|,
name|ClusterAllocationExplanation
operator|.
name|StoreCopy
operator|.
name|NONE
argument_list|)
expr_stmt|;
name|ne
operator|=
name|TransportClusterAllocationExplainAction
operator|.
name|calculateNodeExplanation
argument_list|(
name|primaryShard
argument_list|,
name|indexMetaData
argument_list|,
name|node
argument_list|,
name|noDecision
argument_list|,
name|nodeWeight
argument_list|,
literal|null
argument_list|,
literal|""
argument_list|,
name|activeAllocationIds
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|assertExplanations
argument_list|(
name|ne
argument_list|,
literal|"the shard cannot be assigned because one or more allocation decider returns a 'NO' decision"
argument_list|,
name|ClusterAllocationExplanation
operator|.
name|FinalDecision
operator|.
name|NO
argument_list|,
name|ClusterAllocationExplanation
operator|.
name|StoreCopy
operator|.
name|NONE
argument_list|)
expr_stmt|;
name|storeStatus
operator|=
operator|new
name|IndicesShardStoresResponse
operator|.
name|StoreStatus
argument_list|(
name|node
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
expr_stmt|;
name|ne
operator|=
name|TransportClusterAllocationExplainAction
operator|.
name|calculateNodeExplanation
argument_list|(
name|primaryShard
argument_list|,
name|indexMetaData
argument_list|,
name|node
argument_list|,
name|noDecision
argument_list|,
name|nodeWeight
argument_list|,
name|storeStatus
argument_list|,
literal|""
argument_list|,
name|activeAllocationIds
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|assertExplanations
argument_list|(
name|ne
argument_list|,
literal|"the shard cannot be assigned because one or more allocation decider returns a 'NO' decision"
argument_list|,
name|ClusterAllocationExplanation
operator|.
name|FinalDecision
operator|.
name|NO
argument_list|,
name|ClusterAllocationExplanation
operator|.
name|StoreCopy
operator|.
name|AVAILABLE
argument_list|)
expr_stmt|;
name|storeStatus
operator|=
operator|new
name|IndicesShardStoresResponse
operator|.
name|StoreStatus
argument_list|(
name|node
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
name|corruptE
argument_list|)
expr_stmt|;
name|ne
operator|=
name|TransportClusterAllocationExplainAction
operator|.
name|calculateNodeExplanation
argument_list|(
name|primaryShard
argument_list|,
name|indexMetaData
argument_list|,
name|node
argument_list|,
name|yesDecision
argument_list|,
name|nodeWeight
argument_list|,
name|storeStatus
argument_list|,
literal|""
argument_list|,
name|activeAllocationIds
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|assertExplanations
argument_list|(
name|ne
argument_list|,
literal|"the copy of the shard is corrupt"
argument_list|,
name|ClusterAllocationExplanation
operator|.
name|FinalDecision
operator|.
name|NO
argument_list|,
name|ClusterAllocationExplanation
operator|.
name|StoreCopy
operator|.
name|CORRUPT
argument_list|)
expr_stmt|;
name|storeStatus
operator|=
operator|new
name|IndicesShardStoresResponse
operator|.
name|StoreStatus
argument_list|(
name|node
argument_list|,
literal|42
argument_list|,
literal|"banana"
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
expr_stmt|;
name|ne
operator|=
name|TransportClusterAllocationExplainAction
operator|.
name|calculateNodeExplanation
argument_list|(
name|primaryShard
argument_list|,
name|indexMetaData
argument_list|,
name|node
argument_list|,
name|yesDecision
argument_list|,
name|nodeWeight
argument_list|,
name|storeStatus
argument_list|,
literal|""
argument_list|,
name|activeAllocationIds
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|assertExplanations
argument_list|(
name|ne
argument_list|,
literal|"the shard can be assigned"
argument_list|,
name|ClusterAllocationExplanation
operator|.
name|FinalDecision
operator|.
name|YES
argument_list|,
name|ClusterAllocationExplanation
operator|.
name|StoreCopy
operator|.
name|STALE
argument_list|)
expr_stmt|;
name|storeStatus
operator|=
operator|new
name|IndicesShardStoresResponse
operator|.
name|StoreStatus
argument_list|(
name|node
argument_list|,
literal|42
argument_list|,
literal|"banana"
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
expr_stmt|;
name|ne
operator|=
name|TransportClusterAllocationExplainAction
operator|.
name|calculateNodeExplanation
argument_list|(
name|primaryStartedShard
argument_list|,
name|indexMetaData
argument_list|,
name|node
argument_list|,
name|yesDecision
argument_list|,
name|nodeWeight
argument_list|,
name|storeStatus
argument_list|,
literal|""
argument_list|,
name|activeAllocationIds
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|assertExplanations
argument_list|(
name|ne
argument_list|,
literal|"the copy of the shard is stale, allocation ids do not match"
argument_list|,
name|ClusterAllocationExplanation
operator|.
name|FinalDecision
operator|.
name|NO
argument_list|,
name|ClusterAllocationExplanation
operator|.
name|StoreCopy
operator|.
name|STALE
argument_list|)
expr_stmt|;
name|storeStatus
operator|=
operator|new
name|IndicesShardStoresResponse
operator|.
name|StoreStatus
argument_list|(
name|node
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
expr_stmt|;
name|ne
operator|=
name|TransportClusterAllocationExplainAction
operator|.
name|calculateNodeExplanation
argument_list|(
name|primaryShard
argument_list|,
name|indexMetaData
argument_list|,
name|node
argument_list|,
name|yesDecision
argument_list|,
name|nodeWeight
argument_list|,
name|storeStatus
argument_list|,
literal|"node-0"
argument_list|,
name|activeAllocationIds
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|assertExplanations
argument_list|(
name|ne
argument_list|,
literal|"the shard is already assigned to this node"
argument_list|,
name|ClusterAllocationExplanation
operator|.
name|FinalDecision
operator|.
name|ALREADY_ASSIGNED
argument_list|,
name|ClusterAllocationExplanation
operator|.
name|StoreCopy
operator|.
name|AVAILABLE
argument_list|)
expr_stmt|;
name|storeStatus
operator|=
operator|new
name|IndicesShardStoresResponse
operator|.
name|StoreStatus
argument_list|(
name|node
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
expr_stmt|;
name|ne
operator|=
name|TransportClusterAllocationExplainAction
operator|.
name|calculateNodeExplanation
argument_list|(
name|primaryShard
argument_list|,
name|indexMetaData
argument_list|,
name|node
argument_list|,
name|yesDecision
argument_list|,
name|nodeWeight
argument_list|,
name|storeStatus
argument_list|,
literal|""
argument_list|,
name|activeAllocationIds
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|assertExplanations
argument_list|(
name|ne
argument_list|,
literal|"the shard can be assigned and the node contains a valid copy of the shard data"
argument_list|,
name|ClusterAllocationExplanation
operator|.
name|FinalDecision
operator|.
name|YES
argument_list|,
name|ClusterAllocationExplanation
operator|.
name|StoreCopy
operator|.
name|AVAILABLE
argument_list|)
expr_stmt|;
name|storeStatus
operator|=
operator|new
name|IndicesShardStoresResponse
operator|.
name|StoreStatus
argument_list|(
name|node
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
expr_stmt|;
name|ne
operator|=
name|TransportClusterAllocationExplainAction
operator|.
name|calculateNodeExplanation
argument_list|(
name|primaryStartedShard
argument_list|,
name|indexMetaData
argument_list|,
name|node
argument_list|,
name|yesDecision
argument_list|,
name|nodeWeight
argument_list|,
name|storeStatus
argument_list|,
literal|""
argument_list|,
name|activeAllocationIds
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|assertExplanations
argument_list|(
name|ne
argument_list|,
literal|"the shard's state is still being fetched so it cannot be allocated"
argument_list|,
name|ClusterAllocationExplanation
operator|.
name|FinalDecision
operator|.
name|NO
argument_list|,
name|ClusterAllocationExplanation
operator|.
name|StoreCopy
operator|.
name|AVAILABLE
argument_list|)
expr_stmt|;
name|storeStatus
operator|=
operator|new
name|IndicesShardStoresResponse
operator|.
name|StoreStatus
argument_list|(
name|node
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
name|REPLICA
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|ne
operator|=
name|TransportClusterAllocationExplainAction
operator|.
name|calculateNodeExplanation
argument_list|(
name|replicaStartedShard
argument_list|,
name|indexMetaData
argument_list|,
name|node
argument_list|,
name|noDecision
argument_list|,
name|nodeWeight
argument_list|,
name|storeStatus
argument_list|,
literal|""
argument_list|,
name|activeAllocationIds
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|assertExplanations
argument_list|(
name|ne
argument_list|,
literal|"the shard cannot be assigned because allocation deciders return a NO "
operator|+
literal|"decision and the shard's state is still being fetched"
argument_list|,
name|ClusterAllocationExplanation
operator|.
name|FinalDecision
operator|.
name|NO
argument_list|,
name|ClusterAllocationExplanation
operator|.
name|StoreCopy
operator|.
name|AVAILABLE
argument_list|)
expr_stmt|;
block|}
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
name|long
name|allocationDelay
init|=
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|500
argument_list|)
decl_stmt|;
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
name|Map
argument_list|<
name|DiscoveryNode
argument_list|,
name|NodeExplanation
argument_list|>
name|nodeExplanations
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|Float
name|nodeWeight
init|=
name|randomFloat
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|activeAllocationIds
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
name|activeAllocationIds
operator|.
name|add
argument_list|(
literal|"eggplant"
argument_list|)
expr_stmt|;
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
name|node
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
name|NodeExplanation
name|ne
init|=
name|TransportClusterAllocationExplainAction
operator|.
name|calculateNodeExplanation
argument_list|(
name|primaryShard
argument_list|,
name|indexMetaData
argument_list|,
name|node
argument_list|,
name|yesDecision
argument_list|,
name|nodeWeight
argument_list|,
name|storeStatus
argument_list|,
literal|""
argument_list|,
name|activeAllocationIds
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|nodeExplanations
operator|.
name|put
argument_list|(
name|ne
operator|.
name|getNode
argument_list|()
argument_list|,
name|ne
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
name|allocationDelay
argument_list|,
name|remainingDelay
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|,
name|nodeExplanations
argument_list|,
literal|null
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
name|assertEquals
argument_list|(
name|allocationDelay
argument_list|,
name|cae2
operator|.
name|getAllocationDelayMillis
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|remainingDelay
argument_list|,
name|cae2
operator|.
name|getRemainingDelayMillis
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
name|NodeExplanation
argument_list|>
name|entry
range|:
name|cae2
operator|.
name|getNodeExplanations
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|NodeExplanation
name|explanation
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
name|explanation
operator|.
name|getStoreStatus
argument_list|()
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|explanation
operator|.
name|getDecision
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|nodeWeight
argument_list|,
name|explanation
operator|.
name|getWeight
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testExplanationToXContent
specifier|public
name|void
name|testExplanationToXContent
parameter_list|()
throws|throws
name|Exception
block|{
name|ShardId
name|shardId
init|=
operator|new
name|ShardId
argument_list|(
literal|"foo"
argument_list|,
literal|"uuid"
argument_list|,
literal|0
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
name|Float
name|nodeWeight
init|=
literal|1.5f
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
literal|"bar"
argument_list|)
expr_stmt|;
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
name|node
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
name|NodeExplanation
name|ne
init|=
name|TransportClusterAllocationExplainAction
operator|.
name|calculateNodeExplanation
argument_list|(
name|primaryShard
argument_list|,
name|indexMetaData
argument_list|,
name|node
argument_list|,
name|d
argument_list|,
name|nodeWeight
argument_list|,
name|storeStatus
argument_list|,
literal|"node-0"
argument_list|,
name|allocationIds
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|DiscoveryNode
argument_list|,
name|NodeExplanation
argument_list|>
name|nodeExplanations
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|nodeExplanations
operator|.
name|put
argument_list|(
name|ne
operator|.
name|getNode
argument_list|()
argument_list|,
name|ne
argument_list|)
expr_stmt|;
name|ClusterAllocationExplanation
name|cae
init|=
operator|new
name|ClusterAllocationExplanation
argument_list|(
name|shardId
argument_list|,
literal|true
argument_list|,
literal|"assignedNode"
argument_list|,
literal|42
argument_list|,
literal|42
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|,
name|nodeExplanations
argument_list|,
literal|null
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
literal|"{\"shard\":{\"index\":\"foo\",\"index_uuid\":\"uuid\",\"id\":0,\"primary\":true},\"assigned\":true,"
operator|+
literal|"\"assigned_node_id\":\"assignedNode\",\"shard_state_fetch_pending\":false,\"nodes\":{\"node-0\":"
operator|+
literal|"{\"node_name\":\"\",\"node_attributes\":{},\"store\":{\"shard_copy\":\"IO_ERROR\",\"store_except"
operator|+
literal|"ion\":\"ElasticsearchException[stuff's broke, yo]\"},\"final_decision\":\"ALREADY_ASSIGNED\",\"f"
operator|+
literal|"inal_explanation\":\"the shard is already assigned to this node\",\"weight\":1.5,\"decisions\":["
operator|+
literal|"{\"decider\":\"no label\",\"decision\":\"NO\",\"explanation\":\"because I said no\"},{\"decider"
operator|+
literal|"\":\"yes label\",\"decision\":\"YES\",\"explanation\":\"yes please\"},{\"decider\":\"throttle la"
operator|+
literal|"bel\",\"decision\":\"THROTTLE\",\"explanation\":\"wait a sec\"}]}}}"
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

