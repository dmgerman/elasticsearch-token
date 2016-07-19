begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.basic
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|basic
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
name|indices
operator|.
name|refresh
operator|.
name|RefreshResponse
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
name|junit
operator|.
name|annotations
operator|.
name|TestLogging
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
name|hamcrest
operator|.
name|Matchers
operator|.
name|greaterThanOrEqualTo
import|;
end_import

begin_comment
comment|/**  * This test basically verifies that search with a single shard active (cause we indexed to it) and other  * shards possibly not active at all (cause they haven't allocated) will still work.  */
end_comment

begin_class
annotation|@
name|TestLogging
argument_list|(
literal|"_root:DEBUG"
argument_list|)
DECL|class|SearchWhileCreatingIndexIT
specifier|public
class|class
name|SearchWhileCreatingIndexIT
extends|extends
name|ESIntegTestCase
block|{
DECL|method|testIndexCausesIndexCreation
specifier|public
name|void
name|testIndexCausesIndexCreation
parameter_list|()
throws|throws
name|Exception
block|{
name|searchWhileCreatingIndex
argument_list|(
literal|false
argument_list|,
literal|1
argument_list|)
expr_stmt|;
comment|// 1 replica in our default...
block|}
DECL|method|testNoReplicas
specifier|public
name|void
name|testNoReplicas
parameter_list|()
throws|throws
name|Exception
block|{
name|searchWhileCreatingIndex
argument_list|(
literal|true
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
DECL|method|testOneReplica
specifier|public
name|void
name|testOneReplica
parameter_list|()
throws|throws
name|Exception
block|{
name|searchWhileCreatingIndex
argument_list|(
literal|true
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
DECL|method|testTwoReplicas
specifier|public
name|void
name|testTwoReplicas
parameter_list|()
throws|throws
name|Exception
block|{
name|searchWhileCreatingIndex
argument_list|(
literal|true
argument_list|,
literal|2
argument_list|)
expr_stmt|;
block|}
DECL|method|searchWhileCreatingIndex
specifier|private
name|void
name|searchWhileCreatingIndex
parameter_list|(
name|boolean
name|createIndex
parameter_list|,
name|int
name|numberOfReplicas
parameter_list|)
throws|throws
name|Exception
block|{
comment|// TODO: randomize the wait for active shards value on index creation and ensure the appropriate
comment|// number of data nodes are started for the randomized active shard count value
name|String
name|id
init|=
name|randomAsciiOfLength
argument_list|(
literal|5
argument_list|)
decl_stmt|;
comment|// we will go the primary or the replica, but in a
comment|// randomized re-creatable manner
name|int
name|counter
init|=
literal|0
decl_stmt|;
name|String
name|preference
init|=
name|randomAsciiOfLength
argument_list|(
literal|5
argument_list|)
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"running iteration for id {}, preference {}"
argument_list|,
name|id
argument_list|,
name|preference
argument_list|)
expr_stmt|;
if|if
condition|(
name|createIndex
condition|)
block|{
name|createIndex
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
block|}
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
name|id
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"field"
argument_list|,
literal|"test"
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|RefreshResponse
name|refreshResponse
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
name|prepareRefresh
argument_list|(
literal|"test"
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
name|refreshResponse
operator|.
name|getSuccessfulShards
argument_list|()
argument_list|,
name|greaterThanOrEqualTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
comment|// at least one shard should be successful when refreshing
name|logger
operator|.
name|info
argument_list|(
literal|"using preference {}"
argument_list|,
name|preference
argument_list|)
expr_stmt|;
comment|// we want to make sure that while recovery happens, and a replica gets recovered, its properly refreshed
name|ClusterHealthStatus
name|status
init|=
name|ClusterHealthStatus
operator|.
name|RED
decl_stmt|;
while|while
condition|(
name|status
operator|!=
name|ClusterHealthStatus
operator|.
name|GREEN
condition|)
block|{
comment|// first, verify that search on the primary search works
name|SearchResponse
name|searchResponse
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setPreference
argument_list|(
literal|"_primary"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
name|termQuery
argument_list|(
literal|"field"
argument_list|,
literal|"test"
argument_list|)
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
literal|1
argument_list|)
expr_stmt|;
name|Client
name|client
init|=
name|client
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
name|setPreference
argument_list|(
name|preference
operator|+
name|Integer
operator|.
name|toString
argument_list|(
name|counter
operator|++
argument_list|)
argument_list|)
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
name|termQuery
argument_list|(
literal|"field"
argument_list|,
literal|"test"
argument_list|)
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
name|searchResponse
operator|.
name|getHits
argument_list|()
operator|.
name|getTotalHits
argument_list|()
operator|!=
literal|1
condition|)
block|{
name|refresh
argument_list|()
expr_stmt|;
name|SearchResponse
name|searchResponseAfterRefresh
init|=
name|client
operator|.
name|prepareSearch
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setPreference
argument_list|(
name|preference
argument_list|)
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
name|termQuery
argument_list|(
literal|"field"
argument_list|,
literal|"test"
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"hits count mismatch on any shard search failed, post explicit refresh hits are {}"
argument_list|,
name|searchResponseAfterRefresh
operator|.
name|getHits
argument_list|()
operator|.
name|getTotalHits
argument_list|()
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
name|SearchResponse
name|searchResponseAfterGreen
init|=
name|client
operator|.
name|prepareSearch
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setPreference
argument_list|(
name|preference
argument_list|)
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
name|termQuery
argument_list|(
literal|"field"
argument_list|,
literal|"test"
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"hits count mismatch on any shard search failed, post explicit wait for green hits are {}"
argument_list|,
name|searchResponseAfterGreen
operator|.
name|getHits
argument_list|()
operator|.
name|getTotalHits
argument_list|()
argument_list|)
expr_stmt|;
name|assertHitCount
argument_list|(
name|searchResponse
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
name|assertHitCount
argument_list|(
name|searchResponse
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|status
operator|=
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
argument_list|(
literal|"test"
argument_list|)
operator|.
name|get
argument_list|()
operator|.
name|getStatus
argument_list|()
expr_stmt|;
name|internalCluster
argument_list|()
operator|.
name|ensureAtLeastNumDataNodes
argument_list|(
name|numberOfReplicas
operator|+
literal|1
argument_list|)
expr_stmt|;
block|}
name|cluster
argument_list|()
operator|.
name|wipeIndices
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

