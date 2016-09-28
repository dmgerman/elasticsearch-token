begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.preference
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|preference
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
name|search
operator|.
name|SearchRequestBuilder
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
name|client
operator|.
name|Client
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
name|health
operator|.
name|ClusterHealthStatus
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
name|Strings
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
name|rest
operator|.
name|RestStatus
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
name|hamcrest
operator|.
name|CoreMatchers
operator|.
name|containsString
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
name|hasToString
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
name|not
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
name|is
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
DECL|class|SearchPreferenceIT
specifier|public
class|class
name|SearchPreferenceIT
extends|extends
name|ESIntegTestCase
block|{
comment|// see #2896
DECL|method|testStopOneNodePreferenceWithRedState
specifier|public
name|void
name|testStopOneNodePreferenceWithRedState
parameter_list|()
throws|throws
name|InterruptedException
throws|,
name|IOException
block|{
name|assertAcked
argument_list|(
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
name|cluster
argument_list|()
operator|.
name|numDataNodes
argument_list|()
operator|+
literal|2
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.number_of_replicas"
argument_list|,
literal|0
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|10
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
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
literal|""
operator|+
name|i
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"field1"
argument_list|,
literal|"value1"
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
block|}
name|refresh
argument_list|()
expr_stmt|;
name|internalCluster
argument_list|()
operator|.
name|stopRandomDataNode
argument_list|()
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
name|prepareHealth
argument_list|()
operator|.
name|setWaitForStatus
argument_list|(
name|ClusterHealthStatus
operator|.
name|RED
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|String
index|[]
name|preferences
init|=
operator|new
name|String
index|[]
block|{
literal|"_primary"
block|,
literal|"_local"
block|,
literal|"_primary_first"
block|,
literal|"_prefer_nodes:somenode"
block|,
literal|"_prefer_nodes:server2"
block|,
literal|"_prefer_nodes:somenode,server2"
block|}
decl_stmt|;
for|for
control|(
name|String
name|pref
range|:
name|preferences
control|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"--> Testing out preference={}"
argument_list|,
name|pref
argument_list|)
expr_stmt|;
name|SearchResponse
name|searchResponse
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|()
operator|.
name|setSize
argument_list|(
literal|0
argument_list|)
operator|.
name|setPreference
argument_list|(
name|pref
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
name|RestStatus
operator|.
name|OK
argument_list|,
name|equalTo
argument_list|(
name|searchResponse
operator|.
name|status
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|pref
argument_list|,
name|searchResponse
operator|.
name|getFailedShards
argument_list|()
argument_list|,
name|greaterThanOrEqualTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|searchResponse
operator|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|()
operator|.
name|setPreference
argument_list|(
name|pref
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
name|RestStatus
operator|.
name|OK
argument_list|,
name|equalTo
argument_list|(
name|searchResponse
operator|.
name|status
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|pref
argument_list|,
name|searchResponse
operator|.
name|getFailedShards
argument_list|()
argument_list|,
name|greaterThanOrEqualTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|//_only_local is a stricter preference, we need to send the request to a data node
name|SearchResponse
name|searchResponse
init|=
name|dataNodeClient
argument_list|()
operator|.
name|prepareSearch
argument_list|()
operator|.
name|setSize
argument_list|(
literal|0
argument_list|)
operator|.
name|setPreference
argument_list|(
literal|"_only_local"
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
name|RestStatus
operator|.
name|OK
argument_list|,
name|equalTo
argument_list|(
name|searchResponse
operator|.
name|status
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"_only_local"
argument_list|,
name|searchResponse
operator|.
name|getFailedShards
argument_list|()
argument_list|,
name|greaterThanOrEqualTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|searchResponse
operator|=
name|dataNodeClient
argument_list|()
operator|.
name|prepareSearch
argument_list|()
operator|.
name|setPreference
argument_list|(
literal|"_only_local"
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
name|RestStatus
operator|.
name|OK
argument_list|,
name|equalTo
argument_list|(
name|searchResponse
operator|.
name|status
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"_only_local"
argument_list|,
name|searchResponse
operator|.
name|getFailedShards
argument_list|()
argument_list|,
name|greaterThanOrEqualTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testNoPreferenceRandom
specifier|public
name|void
name|testNoPreferenceRandom
parameter_list|()
throws|throws
name|Exception
block|{
name|assertAcked
argument_list|(
name|prepareCreate
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setSettings
argument_list|(
comment|//this test needs at least a replica to make sure two consecutive searches go to two different copies of the same data
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|indexSettings
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|SETTING_NUMBER_OF_REPLICAS
argument_list|,
name|between
argument_list|(
literal|1
argument_list|,
name|maximumNumberOfReplicas
argument_list|()
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"field1"
argument_list|,
literal|"value1"
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|refresh
argument_list|()
expr_stmt|;
specifier|final
name|Client
name|client
init|=
name|internalCluster
argument_list|()
operator|.
name|smartClient
argument_list|()
decl_stmt|;
name|SearchResponse
name|searchResponse
init|=
name|client
operator|.
name|prepareSearch
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|matchAllQuery
argument_list|()
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|String
name|firstNodeId
init|=
name|searchResponse
operator|.
name|getHits
argument_list|()
operator|.
name|getAt
argument_list|(
literal|0
argument_list|)
operator|.
name|shard
argument_list|()
operator|.
name|nodeId
argument_list|()
decl_stmt|;
name|searchResponse
operator|=
name|client
operator|.
name|prepareSearch
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|matchAllQuery
argument_list|()
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|String
name|secondNodeId
init|=
name|searchResponse
operator|.
name|getHits
argument_list|()
operator|.
name|getAt
argument_list|(
literal|0
argument_list|)
operator|.
name|shard
argument_list|()
operator|.
name|nodeId
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|firstNodeId
argument_list|,
name|not
argument_list|(
name|equalTo
argument_list|(
name|secondNodeId
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testSimplePreference
specifier|public
name|void
name|testSimplePreference
parameter_list|()
throws|throws
name|Exception
block|{
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
literal|"{\"number_of_replicas\": 1}"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"field1"
argument_list|,
literal|"value1"
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|refresh
argument_list|()
expr_stmt|;
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
name|setPreference
argument_list|(
literal|"_local"
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
name|searchResponse
operator|.
name|getHits
argument_list|()
operator|.
name|totalHits
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1L
argument_list|)
argument_list|)
expr_stmt|;
name|searchResponse
operator|=
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
name|setPreference
argument_list|(
literal|"_local"
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
name|searchResponse
operator|.
name|getHits
argument_list|()
operator|.
name|totalHits
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1L
argument_list|)
argument_list|)
expr_stmt|;
name|searchResponse
operator|=
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
name|setPreference
argument_list|(
literal|"_primary"
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
name|searchResponse
operator|.
name|getHits
argument_list|()
operator|.
name|totalHits
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1L
argument_list|)
argument_list|)
expr_stmt|;
name|searchResponse
operator|=
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
name|setPreference
argument_list|(
literal|"_primary"
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
name|searchResponse
operator|.
name|getHits
argument_list|()
operator|.
name|totalHits
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1L
argument_list|)
argument_list|)
expr_stmt|;
name|searchResponse
operator|=
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
name|setPreference
argument_list|(
literal|"_replica"
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
name|searchResponse
operator|.
name|getHits
argument_list|()
operator|.
name|totalHits
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1L
argument_list|)
argument_list|)
expr_stmt|;
name|searchResponse
operator|=
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
name|setPreference
argument_list|(
literal|"_replica"
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
name|searchResponse
operator|.
name|getHits
argument_list|()
operator|.
name|totalHits
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1L
argument_list|)
argument_list|)
expr_stmt|;
name|searchResponse
operator|=
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
name|setPreference
argument_list|(
literal|"_replica_first"
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
name|searchResponse
operator|.
name|getHits
argument_list|()
operator|.
name|totalHits
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1L
argument_list|)
argument_list|)
expr_stmt|;
name|searchResponse
operator|=
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
name|setPreference
argument_list|(
literal|"_replica_first"
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
name|searchResponse
operator|.
name|getHits
argument_list|()
operator|.
name|totalHits
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1L
argument_list|)
argument_list|)
expr_stmt|;
name|searchResponse
operator|=
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
name|setPreference
argument_list|(
literal|"1234"
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
name|searchResponse
operator|.
name|getHits
argument_list|()
operator|.
name|totalHits
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1L
argument_list|)
argument_list|)
expr_stmt|;
name|searchResponse
operator|=
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
name|setPreference
argument_list|(
literal|"1234"
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
name|searchResponse
operator|.
name|getHits
argument_list|()
operator|.
name|totalHits
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1L
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testReplicaPreference
specifier|public
name|void
name|testReplicaPreference
parameter_list|()
throws|throws
name|Exception
block|{
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
literal|"{\"number_of_replicas\": 0}"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"field1"
argument_list|,
literal|"value1"
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|refresh
argument_list|()
expr_stmt|;
try|try
block|{
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
name|setPreference
argument_list|(
literal|"_replica"
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|fail
argument_list|(
literal|"should have failed because there are no replicas"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// pass
block|}
name|SearchResponse
name|resp
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
name|setPreference
argument_list|(
literal|"_replica_first"
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
name|resp
operator|.
name|getHits
argument_list|()
operator|.
name|totalHits
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1L
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
name|prepareUpdateSettings
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setSettings
argument_list|(
literal|"{\"number_of_replicas\": 1}"
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
name|resp
operator|=
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
name|setPreference
argument_list|(
literal|"_replica"
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
name|resp
operator|.
name|getHits
argument_list|()
operator|.
name|totalHits
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1L
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testThatSpecifyingNonExistingNodesReturnsUsefulError
specifier|public
name|void
name|testThatSpecifyingNonExistingNodesReturnsUsefulError
parameter_list|()
throws|throws
name|Exception
block|{
name|createIndex
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
try|try
block|{
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
name|setPreference
argument_list|(
literal|"_only_nodes:DOES-NOT-EXIST"
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|fail
argument_list|(
literal|"Expected IllegalArgumentException"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|e
argument_list|,
name|hasToString
argument_list|(
name|containsString
argument_list|(
literal|"no data nodes with criteria [DOES-NOT-EXIST] found for shard: [test]["
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testNodesOnlyRandom
specifier|public
name|void
name|testNodesOnlyRandom
parameter_list|()
throws|throws
name|Exception
block|{
name|assertAcked
argument_list|(
name|prepareCreate
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setSettings
argument_list|(
comment|//this test needs at least a replica to make sure two consecutive searches go to two different copies of the same data
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|indexSettings
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|SETTING_NUMBER_OF_REPLICAS
argument_list|,
name|between
argument_list|(
literal|1
argument_list|,
name|maximumNumberOfReplicas
argument_list|()
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"field1"
argument_list|,
literal|"value1"
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|refresh
argument_list|()
expr_stmt|;
specifier|final
name|Client
name|client
init|=
name|internalCluster
argument_list|()
operator|.
name|smartClient
argument_list|()
decl_stmt|;
name|SearchRequestBuilder
name|request
init|=
name|client
operator|.
name|prepareSearch
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|matchAllQuery
argument_list|()
argument_list|)
operator|.
name|setPreference
argument_list|(
literal|"_only_nodes:*,nodes*"
argument_list|)
decl_stmt|;
comment|// multiple wildchar  to cover multi-param usecase
name|assertSearchOnRandomNodes
argument_list|(
name|request
argument_list|)
expr_stmt|;
name|request
operator|=
name|client
operator|.
name|prepareSearch
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|matchAllQuery
argument_list|()
argument_list|)
operator|.
name|setPreference
argument_list|(
literal|"_only_nodes:*"
argument_list|)
expr_stmt|;
name|assertSearchOnRandomNodes
argument_list|(
name|request
argument_list|)
expr_stmt|;
name|ArrayList
argument_list|<
name|String
argument_list|>
name|allNodeIds
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|ArrayList
argument_list|<
name|String
argument_list|>
name|allNodeNames
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|ArrayList
argument_list|<
name|String
argument_list|>
name|allNodeHosts
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
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
for|for
control|(
name|NodeStats
name|node
range|:
name|nodeStats
operator|.
name|getNodes
argument_list|()
control|)
block|{
name|allNodeIds
operator|.
name|add
argument_list|(
name|node
operator|.
name|getNode
argument_list|()
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
name|allNodeNames
operator|.
name|add
argument_list|(
name|node
operator|.
name|getNode
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|allNodeHosts
operator|.
name|add
argument_list|(
name|node
operator|.
name|getHostname
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|String
name|node_expr
init|=
literal|"_only_nodes:"
operator|+
name|Strings
operator|.
name|arrayToCommaDelimitedString
argument_list|(
name|allNodeIds
operator|.
name|toArray
argument_list|()
argument_list|)
decl_stmt|;
name|request
operator|=
name|client
operator|.
name|prepareSearch
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|matchAllQuery
argument_list|()
argument_list|)
operator|.
name|setPreference
argument_list|(
name|node_expr
argument_list|)
expr_stmt|;
name|assertSearchOnRandomNodes
argument_list|(
name|request
argument_list|)
expr_stmt|;
name|node_expr
operator|=
literal|"_only_nodes:"
operator|+
name|Strings
operator|.
name|arrayToCommaDelimitedString
argument_list|(
name|allNodeNames
operator|.
name|toArray
argument_list|()
argument_list|)
expr_stmt|;
name|request
operator|=
name|client
operator|.
name|prepareSearch
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|matchAllQuery
argument_list|()
argument_list|)
operator|.
name|setPreference
argument_list|(
name|node_expr
argument_list|)
expr_stmt|;
name|assertSearchOnRandomNodes
argument_list|(
name|request
argument_list|)
expr_stmt|;
name|node_expr
operator|=
literal|"_only_nodes:"
operator|+
name|Strings
operator|.
name|arrayToCommaDelimitedString
argument_list|(
name|allNodeHosts
operator|.
name|toArray
argument_list|()
argument_list|)
expr_stmt|;
name|request
operator|=
name|client
operator|.
name|prepareSearch
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|matchAllQuery
argument_list|()
argument_list|)
operator|.
name|setPreference
argument_list|(
name|node_expr
argument_list|)
expr_stmt|;
name|assertSearchOnRandomNodes
argument_list|(
name|request
argument_list|)
expr_stmt|;
name|node_expr
operator|=
literal|"_only_nodes:"
operator|+
name|Strings
operator|.
name|arrayToCommaDelimitedString
argument_list|(
name|allNodeHosts
operator|.
name|toArray
argument_list|()
argument_list|)
expr_stmt|;
name|request
operator|=
name|client
operator|.
name|prepareSearch
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|matchAllQuery
argument_list|()
argument_list|)
operator|.
name|setPreference
argument_list|(
name|node_expr
argument_list|)
expr_stmt|;
name|assertSearchOnRandomNodes
argument_list|(
name|request
argument_list|)
expr_stmt|;
comment|// Mix of valid and invalid nodes
name|node_expr
operator|=
literal|"_only_nodes:*,invalidnode"
expr_stmt|;
name|request
operator|=
name|client
operator|.
name|prepareSearch
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|matchAllQuery
argument_list|()
argument_list|)
operator|.
name|setPreference
argument_list|(
name|node_expr
argument_list|)
expr_stmt|;
name|assertSearchOnRandomNodes
argument_list|(
name|request
argument_list|)
expr_stmt|;
block|}
DECL|method|assertSearchOnRandomNodes
specifier|private
name|void
name|assertSearchOnRandomNodes
parameter_list|(
name|SearchRequestBuilder
name|request
parameter_list|)
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|hitNodes
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
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
literal|2
condition|;
name|i
operator|++
control|)
block|{
name|SearchResponse
name|searchResponse
init|=
name|request
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|searchResponse
operator|.
name|getHits
argument_list|()
operator|.
name|getHits
argument_list|()
operator|.
name|length
argument_list|,
name|greaterThan
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|hitNodes
operator|.
name|add
argument_list|(
name|searchResponse
operator|.
name|getHits
argument_list|()
operator|.
name|getAt
argument_list|(
literal|0
argument_list|)
operator|.
name|shard
argument_list|()
operator|.
name|nodeId
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|assertThat
argument_list|(
name|hitNodes
operator|.
name|size
argument_list|()
argument_list|,
name|greaterThan
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

