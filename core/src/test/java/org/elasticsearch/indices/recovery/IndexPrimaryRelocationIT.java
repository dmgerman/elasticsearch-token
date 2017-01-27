begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.indices.recovery
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|recovery
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
name|DocWriteResponse
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
name|cluster
operator|.
name|health
operator|.
name|ClusterHealthResponse
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
name|delete
operator|.
name|DeleteResponse
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
name|index
operator|.
name|IndexResponse
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
name|ClusterState
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
name|command
operator|.
name|MoveAllocationCommand
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
name|Priority
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
name|index
operator|.
name|query
operator|.
name|QueryBuilders
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
name|ESIntegTestCase
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
name|hamcrest
operator|.
name|ElasticsearchAssertions
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
name|junit
operator|.
name|annotations
operator|.
name|TestLogging
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
name|AtomicBoolean
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
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|equalTo
import|;
end_import

begin_class
annotation|@
name|TestLogging
argument_list|(
literal|"_root:DEBUG"
argument_list|)
annotation|@
name|ESIntegTestCase
operator|.
name|ClusterScope
argument_list|(
name|scope
operator|=
name|ESIntegTestCase
operator|.
name|Scope
operator|.
name|TEST
argument_list|)
DECL|class|IndexPrimaryRelocationIT
specifier|public
class|class
name|IndexPrimaryRelocationIT
extends|extends
name|ESIntegTestCase
block|{
DECL|field|RELOCATION_COUNT
specifier|private
specifier|static
specifier|final
name|int
name|RELOCATION_COUNT
init|=
literal|25
decl_stmt|;
annotation|@
name|TestLogging
argument_list|(
literal|"_root:DEBUG,org.elasticsearch.action.bulk:TRACE,org.elasticsearch.index.shard:TRACE,org.elasticsearch.cluster.service:TRACE"
argument_list|)
DECL|method|testPrimaryRelocationWhileIndexing
specifier|public
name|void
name|testPrimaryRelocationWhileIndexing
parameter_list|()
throws|throws
name|Exception
block|{
name|internalCluster
argument_list|()
operator|.
name|ensureAtLeastNumDataNodes
argument_list|(
name|randomIntBetween
argument_list|(
literal|2
argument_list|,
literal|3
argument_list|)
argument_list|)
expr_stmt|;
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
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"index.number_of_shards"
argument_list|,
literal|1
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.number_of_replicas"
argument_list|,
literal|0
argument_list|)
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"type"
argument_list|,
literal|"field"
argument_list|,
literal|"type=text"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|ensureGreen
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
name|AtomicInteger
name|numAutoGenDocs
init|=
operator|new
name|AtomicInteger
argument_list|()
decl_stmt|;
specifier|final
name|AtomicBoolean
name|finished
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
name|Thread
name|indexingThread
init|=
operator|new
name|Thread
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
while|while
condition|(
name|finished
operator|.
name|get
argument_list|()
operator|==
literal|false
condition|)
block|{
name|IndexResponse
name|indexResponse
init|=
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
literal|"id"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"field"
argument_list|,
literal|"value"
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|DocWriteResponse
operator|.
name|Result
operator|.
name|CREATED
argument_list|,
name|indexResponse
operator|.
name|getResult
argument_list|()
argument_list|)
expr_stmt|;
name|DeleteResponse
name|deleteResponse
init|=
name|client
argument_list|()
operator|.
name|prepareDelete
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
literal|"id"
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|DocWriteResponse
operator|.
name|Result
operator|.
name|DELETED
argument_list|,
name|deleteResponse
operator|.
name|getResult
argument_list|()
argument_list|)
expr_stmt|;
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"auto"
argument_list|,
literal|true
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|numAutoGenDocs
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
block|}
block|}
decl_stmt|;
name|indexingThread
operator|.
name|start
argument_list|()
expr_stmt|;
name|ClusterState
name|initialState
init|=
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareState
argument_list|()
operator|.
name|get
argument_list|()
operator|.
name|getState
argument_list|()
decl_stmt|;
name|DiscoveryNode
index|[]
name|dataNodes
init|=
name|initialState
operator|.
name|getNodes
argument_list|()
operator|.
name|getDataNodes
argument_list|()
operator|.
name|values
argument_list|()
operator|.
name|toArray
argument_list|(
name|DiscoveryNode
operator|.
name|class
argument_list|)
decl_stmt|;
name|DiscoveryNode
name|relocationSource
init|=
name|initialState
operator|.
name|getNodes
argument_list|()
operator|.
name|getDataNodes
argument_list|()
operator|.
name|get
argument_list|(
name|initialState
operator|.
name|getRoutingTable
argument_list|()
operator|.
name|shardRoutingTable
argument_list|(
literal|"test"
argument_list|,
literal|0
argument_list|)
operator|.
name|primaryShard
argument_list|()
operator|.
name|currentNodeId
argument_list|()
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
name|RELOCATION_COUNT
condition|;
name|i
operator|++
control|)
block|{
name|DiscoveryNode
name|relocationTarget
init|=
name|randomFrom
argument_list|(
name|dataNodes
argument_list|)
decl_stmt|;
while|while
condition|(
name|relocationTarget
operator|.
name|equals
argument_list|(
name|relocationSource
argument_list|)
condition|)
block|{
name|relocationTarget
operator|=
name|randomFrom
argument_list|(
name|dataNodes
argument_list|)
expr_stmt|;
block|}
name|logger
operator|.
name|info
argument_list|(
literal|"--> [iteration {}] relocating from {} to {} "
argument_list|,
name|i
argument_list|,
name|relocationSource
operator|.
name|getName
argument_list|()
argument_list|,
name|relocationTarget
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareReroute
argument_list|()
operator|.
name|add
argument_list|(
operator|new
name|MoveAllocationCommand
argument_list|(
literal|"test"
argument_list|,
literal|0
argument_list|,
name|relocationSource
operator|.
name|getId
argument_list|()
argument_list|,
name|relocationTarget
operator|.
name|getId
argument_list|()
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|ClusterHealthResponse
name|clusterHealthResponse
init|=
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareHealth
argument_list|()
operator|.
name|setWaitForEvents
argument_list|(
name|Priority
operator|.
name|LANGUID
argument_list|)
operator|.
name|setWaitForNoRelocatingShards
argument_list|(
literal|true
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|clusterHealthResponse
operator|.
name|isTimedOut
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> [iteration {}] relocation complete"
argument_list|,
name|i
argument_list|)
expr_stmt|;
name|relocationSource
operator|=
name|relocationTarget
expr_stmt|;
if|if
condition|(
name|indexingThread
operator|.
name|isAlive
argument_list|()
operator|==
literal|false
condition|)
block|{
comment|// indexing process aborted early, no need for more relocations as test has already failed
break|break;
block|}
block|}
name|finished
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|indexingThread
operator|.
name|join
argument_list|()
expr_stmt|;
name|refresh
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
name|ElasticsearchAssertions
operator|.
name|assertHitCount
argument_list|(
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"test"
argument_list|)
operator|.
name|get
argument_list|()
argument_list|,
name|numAutoGenDocs
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|ElasticsearchAssertions
operator|.
name|assertHitCount
argument_list|(
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"test"
argument_list|)
comment|// extra paranoia ;)
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
name|termQuery
argument_list|(
literal|"auto"
argument_list|,
literal|true
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
argument_list|,
name|numAutoGenDocs
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

