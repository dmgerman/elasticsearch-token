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
name|RecoverySource
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
name|ShardRoutingHelper
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
name|IndexService
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
name|NodeServicesProvider
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
name|IndexEventListener
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
name|IndexShard
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
name|indices
operator|.
name|recovery
operator|.
name|RecoveryState
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
name|ESSingleNodeTestCase
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicInteger
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

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|metadata
operator|.
name|IndexMetaData
operator|.
name|SETTING_NUMBER_OF_REPLICAS
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|metadata
operator|.
name|IndexMetaData
operator|.
name|SETTING_NUMBER_OF_SHARDS
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|hamcrest
operator|.
name|ElasticsearchAssertions
operator|.
name|assertAcked
import|;
end_import

begin_class
DECL|class|IndicesLifecycleListenerSingleNodeTests
specifier|public
class|class
name|IndicesLifecycleListenerSingleNodeTests
extends|extends
name|ESSingleNodeTestCase
block|{
DECL|method|testCloseDeleteCallback
specifier|public
name|void
name|testCloseDeleteCallback
parameter_list|()
throws|throws
name|Throwable
block|{
name|IndicesService
name|indicesService
init|=
name|getInstanceFromNode
argument_list|(
name|IndicesService
operator|.
name|class
argument_list|)
decl_stmt|;
name|assertAcked
argument_list|(
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareCreate
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setSettings
argument_list|(
name|SETTING_NUMBER_OF_SHARDS
argument_list|,
literal|1
argument_list|,
name|SETTING_NUMBER_OF_REPLICAS
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
name|Index
name|idx
init|=
name|resolveIndex
argument_list|(
literal|"test"
argument_list|)
decl_stmt|;
name|IndexMetaData
name|metaData
init|=
name|indicesService
operator|.
name|indexService
argument_list|(
name|idx
argument_list|)
operator|.
name|getMetaData
argument_list|()
decl_stmt|;
name|ShardRouting
name|shardRouting
init|=
name|indicesService
operator|.
name|indexService
argument_list|(
name|idx
argument_list|)
operator|.
name|getShard
argument_list|(
literal|0
argument_list|)
operator|.
name|routingEntry
argument_list|()
decl_stmt|;
specifier|final
name|AtomicInteger
name|counter
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|IndexEventListener
name|countingListener
init|=
operator|new
name|IndexEventListener
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|afterIndexClosed
parameter_list|(
name|Index
name|index
parameter_list|,
name|Settings
name|indexSettings
parameter_list|)
block|{
name|assertEquals
argument_list|(
name|counter
operator|.
name|get
argument_list|()
argument_list|,
literal|5
argument_list|)
expr_stmt|;
name|counter
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|beforeIndexClosed
parameter_list|(
name|IndexService
name|indexService
parameter_list|)
block|{
name|assertEquals
argument_list|(
name|counter
operator|.
name|get
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|counter
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|afterIndexDeleted
parameter_list|(
name|Index
name|index
parameter_list|,
name|Settings
name|indexSettings
parameter_list|)
block|{
name|assertEquals
argument_list|(
name|counter
operator|.
name|get
argument_list|()
argument_list|,
literal|6
argument_list|)
expr_stmt|;
name|counter
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|beforeIndexDeleted
parameter_list|(
name|IndexService
name|indexService
parameter_list|)
block|{
name|assertEquals
argument_list|(
name|counter
operator|.
name|get
argument_list|()
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|counter
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|beforeIndexShardDeleted
parameter_list|(
name|ShardId
name|shardId
parameter_list|,
name|Settings
name|indexSettings
parameter_list|)
block|{
name|assertEquals
argument_list|(
name|counter
operator|.
name|get
argument_list|()
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|counter
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|afterIndexShardDeleted
parameter_list|(
name|ShardId
name|shardId
parameter_list|,
name|Settings
name|indexSettings
parameter_list|)
block|{
name|assertEquals
argument_list|(
name|counter
operator|.
name|get
argument_list|()
argument_list|,
literal|4
argument_list|)
expr_stmt|;
name|counter
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
block|}
decl_stmt|;
name|indicesService
operator|.
name|deleteIndex
argument_list|(
name|idx
argument_list|,
literal|"simon says"
argument_list|)
expr_stmt|;
try|try
block|{
name|NodeServicesProvider
name|nodeServicesProvider
init|=
name|getInstanceFromNode
argument_list|(
name|NodeServicesProvider
operator|.
name|class
argument_list|)
decl_stmt|;
name|IndexService
name|index
init|=
name|indicesService
operator|.
name|createIndex
argument_list|(
name|nodeServicesProvider
argument_list|,
name|metaData
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|countingListener
argument_list|)
argument_list|,
name|s
lambda|->
block|{}
argument_list|)
decl_stmt|;
name|idx
operator|=
name|index
operator|.
name|index
argument_list|()
expr_stmt|;
name|ShardRouting
name|newRouting
init|=
name|shardRouting
decl_stmt|;
name|String
name|nodeId
init|=
name|newRouting
operator|.
name|currentNodeId
argument_list|()
decl_stmt|;
name|UnassignedInfo
name|unassignedInfo
init|=
operator|new
name|UnassignedInfo
argument_list|(
name|UnassignedInfo
operator|.
name|Reason
operator|.
name|INDEX_CREATED
argument_list|,
literal|"boom"
argument_list|)
decl_stmt|;
name|newRouting
operator|=
name|newRouting
operator|.
name|moveToUnassigned
argument_list|(
name|unassignedInfo
argument_list|)
operator|.
name|updateUnassigned
argument_list|(
name|unassignedInfo
argument_list|,
name|RecoverySource
operator|.
name|StoreRecoverySource
operator|.
name|EMPTY_STORE_INSTANCE
argument_list|)
expr_stmt|;
name|newRouting
operator|=
name|ShardRoutingHelper
operator|.
name|initialize
argument_list|(
name|newRouting
argument_list|,
name|nodeId
argument_list|)
expr_stmt|;
name|IndexShard
name|shard
init|=
name|index
operator|.
name|createShard
argument_list|(
name|newRouting
argument_list|)
decl_stmt|;
name|shard
operator|.
name|updateRoutingEntry
argument_list|(
name|newRouting
argument_list|)
expr_stmt|;
specifier|final
name|DiscoveryNode
name|localNode
init|=
operator|new
name|DiscoveryNode
argument_list|(
literal|"foo"
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
name|shard
operator|.
name|markAsRecovering
argument_list|(
literal|"store"
argument_list|,
operator|new
name|RecoveryState
argument_list|(
name|newRouting
argument_list|,
name|localNode
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|shard
operator|.
name|recoverFromStore
argument_list|()
expr_stmt|;
name|newRouting
operator|=
name|ShardRoutingHelper
operator|.
name|moveToStarted
argument_list|(
name|newRouting
argument_list|)
expr_stmt|;
name|shard
operator|.
name|updateRoutingEntry
argument_list|(
name|newRouting
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|indicesService
operator|.
name|deleteIndex
argument_list|(
name|idx
argument_list|,
literal|"simon says"
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|7
argument_list|,
name|counter
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

