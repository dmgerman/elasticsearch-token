begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.test.integration.search.stats
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|integration
operator|.
name|search
operator|.
name|stats
package|;
end_package

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
name|hamcrest
operator|.
name|MatcherAssert
operator|.
name|assertThat
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
name|action
operator|.
name|search
operator|.
name|SearchType
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
name|test
operator|.
name|integration
operator|.
name|AbstractSharedClusterTest
import|;
end_import

begin_import
import|import
name|org
operator|.
name|testng
operator|.
name|annotations
operator|.
name|Test
import|;
end_import

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|SearchStatsTests
specifier|public
class|class
name|SearchStatsTests
extends|extends
name|AbstractSharedClusterTest
block|{
annotation|@
name|Override
DECL|method|getSettings
specifier|public
name|Settings
name|getSettings
parameter_list|()
block|{
return|return
name|randomSettingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"index.number_of_replicas"
argument_list|,
literal|0
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
annotation|@
name|Test
DECL|method|testSimpleStats
specifier|public
name|void
name|testSimpleStats
parameter_list|()
throws|throws
name|Exception
block|{
name|createIndex
argument_list|(
literal|"test1"
argument_list|)
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
literal|500
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
name|i
operator|==
literal|10
condition|)
block|{
name|refresh
argument_list|()
expr_stmt|;
block|}
block|}
name|createIndex
argument_list|(
literal|"test2"
argument_list|)
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
literal|500
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
name|i
operator|==
literal|10
condition|)
block|{
name|refresh
argument_list|()
expr_stmt|;
block|}
block|}
name|cluster
argument_list|()
operator|.
name|ensureAtMostNumNodes
argument_list|(
name|numAssignedShards
argument_list|(
literal|"test1"
argument_list|,
literal|"test2"
argument_list|)
argument_list|)
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
literal|200
condition|;
name|i
operator|++
control|)
block|{
name|client
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
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
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
literal|0l
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
literal|0l
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
literal|0l
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
literal|0l
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
literal|0l
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
literal|0l
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
literal|0l
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
literal|0l
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
name|nodeStats
operator|.
name|getNodes
argument_list|()
index|[
literal|0
index|]
operator|.
name|getIndices
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
literal|0l
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|nodeStats
operator|.
name|getNodes
argument_list|()
index|[
literal|0
index|]
operator|.
name|getIndices
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
literal|0l
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
name|nodeStats
operator|.
name|getNodes
argument_list|()
index|[
literal|0
index|]
operator|.
name|getIndices
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
name|equalTo
argument_list|(
literal|0l
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|nodeStats
operator|.
name|getNodes
argument_list|()
index|[
literal|0
index|]
operator|.
name|getIndices
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
name|equalTo
argument_list|(
literal|0l
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
annotation|@
name|Test
DECL|method|testOpenContexts
specifier|public
name|void
name|testOpenContexts
parameter_list|()
block|{
name|createIndex
argument_list|(
literal|"test1"
argument_list|)
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
literal|50
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
literal|0l
argument_list|)
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
name|setSearchType
argument_list|(
name|SearchType
operator|.
name|SCAN
argument_list|)
operator|.
name|setQuery
argument_list|(
name|matchAllQuery
argument_list|()
argument_list|)
operator|.
name|setSize
argument_list|(
literal|5
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
literal|"test1"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// scroll, but with no timeout (so no context)
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
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
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
literal|0l
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

