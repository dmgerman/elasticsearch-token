begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.messy.tests
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|messy
operator|.
name|tests
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
name|cluster
operator|.
name|node
operator|.
name|stats
operator|.
name|NodeStats
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
name|node
operator|.
name|stats
operator|.
name|NodesStatsResponse
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
name|IndicesStatsResponse
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
name|search
operator|.
name|SearchResponse
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
name|routing
operator|.
name|GroupShardsIterator
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
name|ShardIterator
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
name|index
operator|.
name|search
operator|.
name|stats
operator|.
name|SearchStats
operator|.
name|Stats
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|plugins
operator|.
name|Plugin
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|Script
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|groovy
operator|.
name|GroovyPlugin
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
name|highlight
operator|.
name|HighlightBuilder
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
name|Collections
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
name|Set
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
name|index
operator|.
name|query
operator|.
name|QueryBuilders
operator|.
name|matchAllQuery
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
name|assertAllSuccessful
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
name|assertHitCount
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
name|assertSearchResponse
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

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|greaterThan
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
name|greaterThanOrEqualTo
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
name|lessThanOrEqualTo
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
name|notNullValue
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
name|nullValue
import|;
end_import

begin_comment
comment|/**  */
end_comment

begin_class
annotation|@
name|ESIntegTestCase
operator|.
name|ClusterScope
argument_list|(
name|minNumDataNodes
operator|=
literal|2
argument_list|)
DECL|class|SearchStatsTests
specifier|public
class|class
name|SearchStatsTests
extends|extends
name|ESIntegTestCase
block|{
annotation|@
name|Override
DECL|method|nodePlugins
specifier|protected
name|Collection
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|Plugin
argument_list|>
argument_list|>
name|nodePlugins
parameter_list|()
block|{
return|return
name|Collections
operator|.
name|singleton
argument_list|(
name|GroovyPlugin
operator|.
name|class
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|numberOfReplicas
specifier|protected
name|int
name|numberOfReplicas
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
DECL|method|testSimpleStats
specifier|public
name|void
name|testSimpleStats
parameter_list|()
throws|throws
name|Exception
block|{
comment|// clear all stats first
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareStats
argument_list|()
operator|.
name|clear
argument_list|()
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
specifier|final
name|int
name|numNodes
init|=
name|cluster
argument_list|()
operator|.
name|numDataNodes
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|numNodes
argument_list|,
name|greaterThanOrEqualTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
specifier|final
name|int
name|shardsIdx1
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
decl_stmt|;
comment|// we make sure each node gets at least a single shard...
specifier|final
name|int
name|shardsIdx2
init|=
name|Math
operator|.
name|max
argument_list|(
name|numNodes
operator|-
name|shardsIdx1
argument_list|,
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|numNodes
argument_list|,
name|lessThanOrEqualTo
argument_list|(
name|shardsIdx1
operator|+
name|shardsIdx2
argument_list|)
argument_list|)
expr_stmt|;
name|assertAcked
argument_list|(
name|prepareCreate
argument_list|(
literal|"test1"
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
name|SETTING_NUMBER_OF_SHARDS
argument_list|,
name|shardsIdx1
argument_list|)
operator|.
name|put
argument_list|(
name|SETTING_NUMBER_OF_REPLICAS
argument_list|,
literal|0
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|int
name|docsTest1
init|=
name|scaledRandomIntBetween
argument_list|(
literal|3
operator|*
name|shardsIdx1
argument_list|,
literal|5
operator|*
name|shardsIdx1
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
name|docsTest1
condition|;
name|i
operator|++
control|)
block|{
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test1"
argument_list|,
literal|"type"
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|i
argument_list|)
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"field"
argument_list|,
literal|"value"
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
if|if
condition|(
name|rarely
argument_list|()
condition|)
block|{
name|refresh
argument_list|()
expr_stmt|;
block|}
block|}
name|assertAcked
argument_list|(
name|prepareCreate
argument_list|(
literal|"test2"
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
name|SETTING_NUMBER_OF_SHARDS
argument_list|,
name|shardsIdx2
argument_list|)
operator|.
name|put
argument_list|(
name|SETTING_NUMBER_OF_REPLICAS
argument_list|,
literal|0
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|int
name|docsTest2
init|=
name|scaledRandomIntBetween
argument_list|(
literal|3
operator|*
name|shardsIdx2
argument_list|,
literal|5
operator|*
name|shardsIdx2
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
name|docsTest2
condition|;
name|i
operator|++
control|)
block|{
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test2"
argument_list|,
literal|"type"
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|i
argument_list|)
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"field"
argument_list|,
literal|"value"
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
if|if
condition|(
name|rarely
argument_list|()
condition|)
block|{
name|refresh
argument_list|()
expr_stmt|;
block|}
block|}
name|assertThat
argument_list|(
name|shardsIdx1
operator|+
name|shardsIdx2
argument_list|,
name|equalTo
argument_list|(
name|numAssignedShards
argument_list|(
literal|"test1"
argument_list|,
literal|"test2"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|numAssignedShards
argument_list|(
literal|"test1"
argument_list|,
literal|"test2"
argument_list|)
argument_list|,
name|greaterThanOrEqualTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
comment|// THERE WILL BE AT LEAST 2 NODES HERE SO WE CAN WAIT FOR GREEN
name|ensureGreen
argument_list|()
expr_stmt|;
name|refresh
argument_list|()
expr_stmt|;
name|int
name|iters
init|=
name|scaledRandomIntBetween
argument_list|(
literal|100
argument_list|,
literal|150
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
name|iters
condition|;
name|i
operator|++
control|)
block|{
name|SearchResponse
name|searchResponse
init|=
name|internalCluster
argument_list|()
operator|.
name|clientNodeClient
argument_list|()
operator|.
name|prepareSearch
argument_list|()
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
name|termQuery
argument_list|(
literal|"field"
argument_list|,
literal|"value"
argument_list|)
argument_list|)
operator|.
name|setStats
argument_list|(
literal|"group1"
argument_list|,
literal|"group2"
argument_list|)
operator|.
name|highlighter
argument_list|(
operator|new
name|HighlightBuilder
argument_list|()
operator|.
name|field
argument_list|(
literal|"field"
argument_list|)
argument_list|)
operator|.
name|addScriptField
argument_list|(
literal|"scrip1"
argument_list|,
operator|new
name|Script
argument_list|(
literal|"_source.field"
argument_list|)
argument_list|)
operator|.
name|setSize
argument_list|(
literal|100
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|assertHitCount
argument_list|(
name|searchResponse
argument_list|,
name|docsTest1
operator|+
name|docsTest2
argument_list|)
expr_stmt|;
name|assertAllSuccessful
argument_list|(
name|searchResponse
argument_list|)
expr_stmt|;
block|}
name|IndicesStatsResponse
name|indicesStats
init|=
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareStats
argument_list|()
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|logger
operator|.
name|debug
argument_list|(
literal|"###### indices search stats: "
operator|+
name|indicesStats
operator|.
name|getTotal
argument_list|()
operator|.
name|getSearch
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|indicesStats
operator|.
name|getTotal
argument_list|()
operator|.
name|getSearch
argument_list|()
operator|.
name|getTotal
argument_list|()
operator|.
name|getQueryCount
argument_list|()
argument_list|,
name|greaterThan
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|indicesStats
operator|.
name|getTotal
argument_list|()
operator|.
name|getSearch
argument_list|()
operator|.
name|getTotal
argument_list|()
operator|.
name|getQueryTimeInMillis
argument_list|()
argument_list|,
name|greaterThan
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|indicesStats
operator|.
name|getTotal
argument_list|()
operator|.
name|getSearch
argument_list|()
operator|.
name|getTotal
argument_list|()
operator|.
name|getFetchCount
argument_list|()
argument_list|,
name|greaterThan
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|indicesStats
operator|.
name|getTotal
argument_list|()
operator|.
name|getSearch
argument_list|()
operator|.
name|getTotal
argument_list|()
operator|.
name|getFetchTimeInMillis
argument_list|()
argument_list|,
name|greaterThan
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|indicesStats
operator|.
name|getTotal
argument_list|()
operator|.
name|getSearch
argument_list|()
operator|.
name|getGroupStats
argument_list|()
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|indicesStats
operator|=
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareStats
argument_list|()
operator|.
name|setGroups
argument_list|(
literal|"group1"
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|indicesStats
operator|.
name|getTotal
argument_list|()
operator|.
name|getSearch
argument_list|()
operator|.
name|getGroupStats
argument_list|()
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|indicesStats
operator|.
name|getTotal
argument_list|()
operator|.
name|getSearch
argument_list|()
operator|.
name|getGroupStats
argument_list|()
operator|.
name|get
argument_list|(
literal|"group1"
argument_list|)
operator|.
name|getQueryCount
argument_list|()
argument_list|,
name|greaterThan
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|indicesStats
operator|.
name|getTotal
argument_list|()
operator|.
name|getSearch
argument_list|()
operator|.
name|getGroupStats
argument_list|()
operator|.
name|get
argument_list|(
literal|"group1"
argument_list|)
operator|.
name|getQueryTimeInMillis
argument_list|()
argument_list|,
name|greaterThan
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|indicesStats
operator|.
name|getTotal
argument_list|()
operator|.
name|getSearch
argument_list|()
operator|.
name|getGroupStats
argument_list|()
operator|.
name|get
argument_list|(
literal|"group1"
argument_list|)
operator|.
name|getFetchCount
argument_list|()
argument_list|,
name|greaterThan
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|indicesStats
operator|.
name|getTotal
argument_list|()
operator|.
name|getSearch
argument_list|()
operator|.
name|getGroupStats
argument_list|()
operator|.
name|get
argument_list|(
literal|"group1"
argument_list|)
operator|.
name|getFetchTimeInMillis
argument_list|()
argument_list|,
name|greaterThan
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
name|NodesStatsResponse
name|nodeStats
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
name|prepareNodesStats
argument_list|()
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|NodeStats
index|[]
name|nodes
init|=
name|nodeStats
operator|.
name|getNodes
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|nodeIdsWithIndex
init|=
name|nodeIdsWithIndex
argument_list|(
literal|"test1"
argument_list|,
literal|"test2"
argument_list|)
decl_stmt|;
name|int
name|num
init|=
literal|0
decl_stmt|;
for|for
control|(
name|NodeStats
name|stat
range|:
name|nodes
control|)
block|{
name|Stats
name|total
init|=
name|stat
operator|.
name|getIndices
argument_list|()
operator|.
name|getSearch
argument_list|()
operator|.
name|getTotal
argument_list|()
decl_stmt|;
if|if
condition|(
name|nodeIdsWithIndex
operator|.
name|contains
argument_list|(
name|stat
operator|.
name|getNode
argument_list|()
operator|.
name|getId
argument_list|()
argument_list|)
condition|)
block|{
name|assertThat
argument_list|(
name|total
operator|.
name|getQueryCount
argument_list|()
argument_list|,
name|greaterThan
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|total
operator|.
name|getQueryTimeInMillis
argument_list|()
argument_list|,
name|greaterThan
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
name|num
operator|++
expr_stmt|;
block|}
else|else
block|{
name|assertThat
argument_list|(
name|total
operator|.
name|getQueryCount
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|total
operator|.
name|getQueryTimeInMillis
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|assertThat
argument_list|(
name|num
argument_list|,
name|greaterThan
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|nodeIdsWithIndex
specifier|private
name|Set
argument_list|<
name|String
argument_list|>
name|nodeIdsWithIndex
parameter_list|(
name|String
modifier|...
name|indices
parameter_list|)
block|{
name|ClusterState
name|state
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
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
operator|.
name|getState
argument_list|()
decl_stmt|;
name|GroupShardsIterator
name|allAssignedShardsGrouped
init|=
name|state
operator|.
name|routingTable
argument_list|()
operator|.
name|allAssignedShardsGrouped
argument_list|(
name|indices
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|nodes
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|ShardIterator
name|shardIterator
range|:
name|allAssignedShardsGrouped
control|)
block|{
for|for
control|(
name|ShardRouting
name|routing
range|:
name|shardIterator
operator|.
name|asUnordered
argument_list|()
control|)
block|{
if|if
condition|(
name|routing
operator|.
name|active
argument_list|()
condition|)
block|{
name|nodes
operator|.
name|add
argument_list|(
name|routing
operator|.
name|currentNodeId
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|nodes
return|;
block|}
DECL|method|testOpenContexts
specifier|public
name|void
name|testOpenContexts
parameter_list|()
block|{
name|String
name|index
init|=
literal|"test1"
decl_stmt|;
name|createIndex
argument_list|(
name|index
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|(
name|index
argument_list|)
expr_stmt|;
comment|// create shards * docs number of docs and attempt to distribute them equally
comment|// this distribution will not be perfect; each shard will have an integer multiple of docs (possibly zero)
comment|// we do this so we have a lot of pages to scroll through
specifier|final
name|int
name|docs
init|=
name|scaledRandomIntBetween
argument_list|(
literal|20
argument_list|,
literal|50
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|s
init|=
literal|0
init|;
name|s
operator|<
name|numAssignedShards
argument_list|(
name|index
argument_list|)
condition|;
name|s
operator|++
control|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|docs
condition|;
name|i
operator|++
control|)
block|{
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
name|index
argument_list|,
literal|"type"
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|s
operator|*
name|docs
operator|+
name|i
argument_list|)
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"field"
argument_list|,
literal|"value"
argument_list|)
operator|.
name|setRouting
argument_list|(
name|Integer
operator|.
name|toString
argument_list|(
name|s
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
block|}
block|}
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareRefresh
argument_list|(
name|index
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|IndicesStatsResponse
name|indicesStats
init|=
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareStats
argument_list|(
name|index
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
name|indicesStats
operator|.
name|getTotal
argument_list|()
operator|.
name|getSearch
argument_list|()
operator|.
name|getOpenContexts
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
name|int
name|size
init|=
name|scaledRandomIntBetween
argument_list|(
literal|1
argument_list|,
name|docs
argument_list|)
decl_stmt|;
name|SearchResponse
name|searchResponse
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|()
operator|.
name|setQuery
argument_list|(
name|matchAllQuery
argument_list|()
argument_list|)
operator|.
name|setSize
argument_list|(
name|size
argument_list|)
operator|.
name|setScroll
argument_list|(
name|TimeValue
operator|.
name|timeValueMinutes
argument_list|(
literal|2
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|assertSearchResponse
argument_list|(
name|searchResponse
argument_list|)
expr_stmt|;
comment|// refresh the stats now that scroll contexts are opened
name|indicesStats
operator|=
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareStats
argument_list|(
name|index
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|indicesStats
operator|.
name|getTotal
argument_list|()
operator|.
name|getSearch
argument_list|()
operator|.
name|getOpenContexts
argument_list|()
argument_list|,
name|equalTo
argument_list|(
operator|(
name|long
operator|)
name|numAssignedShards
argument_list|(
name|index
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|indicesStats
operator|.
name|getTotal
argument_list|()
operator|.
name|getSearch
argument_list|()
operator|.
name|getTotal
argument_list|()
operator|.
name|getScrollCurrent
argument_list|()
argument_list|,
name|equalTo
argument_list|(
operator|(
name|long
operator|)
name|numAssignedShards
argument_list|(
name|index
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|int
name|hits
init|=
literal|0
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
if|if
condition|(
name|searchResponse
operator|.
name|getHits
argument_list|()
operator|.
name|getHits
argument_list|()
operator|.
name|length
operator|==
literal|0
condition|)
block|{
break|break;
block|}
name|hits
operator|+=
name|searchResponse
operator|.
name|getHits
argument_list|()
operator|.
name|getHits
argument_list|()
operator|.
name|length
expr_stmt|;
name|searchResponse
operator|=
name|client
argument_list|()
operator|.
name|prepareSearchScroll
argument_list|(
name|searchResponse
operator|.
name|getScrollId
argument_list|()
argument_list|)
operator|.
name|setScroll
argument_list|(
name|TimeValue
operator|.
name|timeValueMinutes
argument_list|(
literal|2
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
block|}
name|long
name|expected
init|=
literal|0
decl_stmt|;
comment|// the number of queries executed is equal to at least the sum of number of pages in shard over all shards
name|IndicesStatsResponse
name|r
init|=
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareStats
argument_list|(
name|index
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|s
init|=
literal|0
init|;
name|s
operator|<
name|numAssignedShards
argument_list|(
name|index
argument_list|)
condition|;
name|s
operator|++
control|)
block|{
name|expected
operator|+=
operator|(
name|long
operator|)
name|Math
operator|.
name|ceil
argument_list|(
name|r
operator|.
name|getShards
argument_list|()
index|[
name|s
index|]
operator|.
name|getStats
argument_list|()
operator|.
name|getDocs
argument_list|()
operator|.
name|getCount
argument_list|()
operator|/
name|size
argument_list|)
expr_stmt|;
block|}
name|indicesStats
operator|=
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareStats
argument_list|()
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|Stats
name|stats
init|=
name|indicesStats
operator|.
name|getTotal
argument_list|()
operator|.
name|getSearch
argument_list|()
operator|.
name|getTotal
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|hits
argument_list|,
name|docs
operator|*
name|numAssignedShards
argument_list|(
name|index
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|stats
operator|.
name|getQueryCount
argument_list|()
argument_list|,
name|greaterThanOrEqualTo
argument_list|(
name|expected
argument_list|)
argument_list|)
expr_stmt|;
name|clearScroll
argument_list|(
name|searchResponse
operator|.
name|getScrollId
argument_list|()
argument_list|)
expr_stmt|;
name|indicesStats
operator|=
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareStats
argument_list|()
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|stats
operator|=
name|indicesStats
operator|.
name|getTotal
argument_list|()
operator|.
name|getSearch
argument_list|()
operator|.
name|getTotal
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|indicesStats
operator|.
name|getTotal
argument_list|()
operator|.
name|getSearch
argument_list|()
operator|.
name|getOpenContexts
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|stats
operator|.
name|getScrollCount
argument_list|()
argument_list|,
name|equalTo
argument_list|(
operator|(
name|long
operator|)
name|numAssignedShards
argument_list|(
name|index
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|stats
operator|.
name|getScrollTimeInMillis
argument_list|()
argument_list|,
name|greaterThan
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|numAssignedShards
specifier|protected
name|int
name|numAssignedShards
parameter_list|(
name|String
modifier|...
name|indices
parameter_list|)
block|{
name|ClusterState
name|state
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
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
operator|.
name|getState
argument_list|()
decl_stmt|;
name|GroupShardsIterator
name|allAssignedShardsGrouped
init|=
name|state
operator|.
name|routingTable
argument_list|()
operator|.
name|allAssignedShardsGrouped
argument_list|(
name|indices
argument_list|,
literal|true
argument_list|)
decl_stmt|;
return|return
name|allAssignedShardsGrouped
operator|.
name|size
argument_list|()
return|;
block|}
block|}
end_class

end_unit

