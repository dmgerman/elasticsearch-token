begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.test.integration.search.scan
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
name|scan
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
name|common
operator|.
name|collect
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
name|settings
operator|.
name|ImmutableSettings
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
name|search
operator|.
name|SearchHit
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
name|AbstractNodesTests
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
name|AfterClass
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
name|BeforeClass
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
name|index
operator|.
name|query
operator|.
name|QueryBuilders
operator|.
name|*
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
name|*
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
name|*
import|;
end_import

begin_class
DECL|class|SearchScanScrollingTests
specifier|public
class|class
name|SearchScanScrollingTests
extends|extends
name|AbstractNodesTests
block|{
DECL|field|client
specifier|private
name|Client
name|client
decl_stmt|;
DECL|method|createNodes
annotation|@
name|BeforeClass
specifier|public
name|void
name|createNodes
parameter_list|()
throws|throws
name|Exception
block|{
name|startNode
argument_list|(
literal|"node1"
argument_list|)
expr_stmt|;
name|startNode
argument_list|(
literal|"node2"
argument_list|)
expr_stmt|;
name|client
operator|=
name|getClient
argument_list|()
expr_stmt|;
block|}
DECL|method|closeNodes
annotation|@
name|AfterClass
specifier|public
name|void
name|closeNodes
parameter_list|()
block|{
name|client
operator|.
name|close
argument_list|()
expr_stmt|;
name|closeAllNodes
argument_list|()
expr_stmt|;
block|}
DECL|method|getClient
specifier|protected
name|Client
name|getClient
parameter_list|()
block|{
return|return
name|client
argument_list|(
literal|"node1"
argument_list|)
return|;
block|}
DECL|method|shard1docs100size3
annotation|@
name|Test
specifier|public
name|void
name|shard1docs100size3
parameter_list|()
throws|throws
name|Exception
block|{
name|testScroll
argument_list|(
literal|1
argument_list|,
literal|100
argument_list|,
literal|3
argument_list|)
expr_stmt|;
block|}
DECL|method|shard1docs100size7
annotation|@
name|Test
specifier|public
name|void
name|shard1docs100size7
parameter_list|()
throws|throws
name|Exception
block|{
name|testScroll
argument_list|(
literal|1
argument_list|,
literal|100
argument_list|,
literal|7
argument_list|)
expr_stmt|;
block|}
DECL|method|shard1docs100size13
annotation|@
name|Test
specifier|public
name|void
name|shard1docs100size13
parameter_list|()
throws|throws
name|Exception
block|{
name|testScroll
argument_list|(
literal|1
argument_list|,
literal|100
argument_list|,
literal|13
argument_list|)
expr_stmt|;
block|}
DECL|method|shard1docs100size24
annotation|@
name|Test
specifier|public
name|void
name|shard1docs100size24
parameter_list|()
throws|throws
name|Exception
block|{
name|testScroll
argument_list|(
literal|1
argument_list|,
literal|100
argument_list|,
literal|24
argument_list|)
expr_stmt|;
block|}
DECL|method|shard1docs100size45
annotation|@
name|Test
specifier|public
name|void
name|shard1docs100size45
parameter_list|()
throws|throws
name|Exception
block|{
name|testScroll
argument_list|(
literal|1
argument_list|,
literal|100
argument_list|,
literal|45
argument_list|)
expr_stmt|;
block|}
DECL|method|shard1docs100size63
annotation|@
name|Test
specifier|public
name|void
name|shard1docs100size63
parameter_list|()
throws|throws
name|Exception
block|{
name|testScroll
argument_list|(
literal|1
argument_list|,
literal|100
argument_list|,
literal|63
argument_list|)
expr_stmt|;
block|}
DECL|method|shard1docs100size89
annotation|@
name|Test
specifier|public
name|void
name|shard1docs100size89
parameter_list|()
throws|throws
name|Exception
block|{
name|testScroll
argument_list|(
literal|1
argument_list|,
literal|100
argument_list|,
literal|89
argument_list|)
expr_stmt|;
block|}
DECL|method|shard1docs100size99
annotation|@
name|Test
specifier|public
name|void
name|shard1docs100size99
parameter_list|()
throws|throws
name|Exception
block|{
name|testScroll
argument_list|(
literal|1
argument_list|,
literal|100
argument_list|,
literal|99
argument_list|)
expr_stmt|;
block|}
DECL|method|shard1docs100size100
annotation|@
name|Test
specifier|public
name|void
name|shard1docs100size100
parameter_list|()
throws|throws
name|Exception
block|{
name|testScroll
argument_list|(
literal|1
argument_list|,
literal|100
argument_list|,
literal|100
argument_list|)
expr_stmt|;
block|}
DECL|method|shard1docs100size101
annotation|@
name|Test
specifier|public
name|void
name|shard1docs100size101
parameter_list|()
throws|throws
name|Exception
block|{
name|testScroll
argument_list|(
literal|1
argument_list|,
literal|100
argument_list|,
literal|101
argument_list|)
expr_stmt|;
block|}
DECL|method|shard1docs100size120
annotation|@
name|Test
specifier|public
name|void
name|shard1docs100size120
parameter_list|()
throws|throws
name|Exception
block|{
name|testScroll
argument_list|(
literal|1
argument_list|,
literal|100
argument_list|,
literal|120
argument_list|)
expr_stmt|;
block|}
DECL|method|shard3docs100size3
annotation|@
name|Test
specifier|public
name|void
name|shard3docs100size3
parameter_list|()
throws|throws
name|Exception
block|{
name|testScroll
argument_list|(
literal|3
argument_list|,
literal|100
argument_list|,
literal|3
argument_list|)
expr_stmt|;
block|}
DECL|method|shard3docs100size7
annotation|@
name|Test
specifier|public
name|void
name|shard3docs100size7
parameter_list|()
throws|throws
name|Exception
block|{
name|testScroll
argument_list|(
literal|3
argument_list|,
literal|100
argument_list|,
literal|7
argument_list|)
expr_stmt|;
block|}
DECL|method|shard3docs100size13
annotation|@
name|Test
specifier|public
name|void
name|shard3docs100size13
parameter_list|()
throws|throws
name|Exception
block|{
name|testScroll
argument_list|(
literal|3
argument_list|,
literal|100
argument_list|,
literal|13
argument_list|)
expr_stmt|;
block|}
DECL|method|shard3docs100size24
annotation|@
name|Test
specifier|public
name|void
name|shard3docs100size24
parameter_list|()
throws|throws
name|Exception
block|{
name|testScroll
argument_list|(
literal|3
argument_list|,
literal|100
argument_list|,
literal|24
argument_list|)
expr_stmt|;
block|}
DECL|method|shard3docs100size45
annotation|@
name|Test
specifier|public
name|void
name|shard3docs100size45
parameter_list|()
throws|throws
name|Exception
block|{
name|testScroll
argument_list|(
literal|3
argument_list|,
literal|100
argument_list|,
literal|45
argument_list|)
expr_stmt|;
block|}
DECL|method|shard3docs100size63
annotation|@
name|Test
specifier|public
name|void
name|shard3docs100size63
parameter_list|()
throws|throws
name|Exception
block|{
name|testScroll
argument_list|(
literal|3
argument_list|,
literal|100
argument_list|,
literal|63
argument_list|)
expr_stmt|;
block|}
DECL|method|shard3docs100size89
annotation|@
name|Test
specifier|public
name|void
name|shard3docs100size89
parameter_list|()
throws|throws
name|Exception
block|{
name|testScroll
argument_list|(
literal|3
argument_list|,
literal|100
argument_list|,
literal|89
argument_list|)
expr_stmt|;
block|}
DECL|method|shard3docs100size120
annotation|@
name|Test
specifier|public
name|void
name|shard3docs100size120
parameter_list|()
throws|throws
name|Exception
block|{
name|testScroll
argument_list|(
literal|3
argument_list|,
literal|100
argument_list|,
literal|120
argument_list|)
expr_stmt|;
block|}
DECL|method|shard3docs100size3Unbalanced
annotation|@
name|Test
specifier|public
name|void
name|shard3docs100size3Unbalanced
parameter_list|()
throws|throws
name|Exception
block|{
name|testScroll
argument_list|(
literal|3
argument_list|,
literal|100
argument_list|,
literal|3
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
DECL|method|shard3docs100size7Unbalanced
annotation|@
name|Test
specifier|public
name|void
name|shard3docs100size7Unbalanced
parameter_list|()
throws|throws
name|Exception
block|{
name|testScroll
argument_list|(
literal|3
argument_list|,
literal|100
argument_list|,
literal|7
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
DECL|method|shard3docs100size13Unbalanced
annotation|@
name|Test
specifier|public
name|void
name|shard3docs100size13Unbalanced
parameter_list|()
throws|throws
name|Exception
block|{
name|testScroll
argument_list|(
literal|3
argument_list|,
literal|100
argument_list|,
literal|13
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
DECL|method|shard3docs100size24Unbalanced
annotation|@
name|Test
specifier|public
name|void
name|shard3docs100size24Unbalanced
parameter_list|()
throws|throws
name|Exception
block|{
name|testScroll
argument_list|(
literal|3
argument_list|,
literal|100
argument_list|,
literal|24
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
DECL|method|shard3docs100size45Unbalanced
annotation|@
name|Test
specifier|public
name|void
name|shard3docs100size45Unbalanced
parameter_list|()
throws|throws
name|Exception
block|{
name|testScroll
argument_list|(
literal|3
argument_list|,
literal|100
argument_list|,
literal|45
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
DECL|method|shard3docs100size63Unbalanced
annotation|@
name|Test
specifier|public
name|void
name|shard3docs100size63Unbalanced
parameter_list|()
throws|throws
name|Exception
block|{
name|testScroll
argument_list|(
literal|3
argument_list|,
literal|100
argument_list|,
literal|63
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
DECL|method|shard3docs100size89Unbalanced
annotation|@
name|Test
specifier|public
name|void
name|shard3docs100size89Unbalanced
parameter_list|()
throws|throws
name|Exception
block|{
name|testScroll
argument_list|(
literal|3
argument_list|,
literal|100
argument_list|,
literal|89
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
DECL|method|shard3docs100size120Unbalanced
annotation|@
name|Test
specifier|public
name|void
name|shard3docs100size120Unbalanced
parameter_list|()
throws|throws
name|Exception
block|{
name|testScroll
argument_list|(
literal|3
argument_list|,
literal|100
argument_list|,
literal|120
argument_list|)
expr_stmt|;
block|}
DECL|method|testScroll
specifier|private
name|void
name|testScroll
parameter_list|(
name|int
name|numberOfShards
parameter_list|,
name|long
name|numberOfDocs
parameter_list|,
name|int
name|size
parameter_list|)
throws|throws
name|Exception
block|{
name|testScroll
argument_list|(
name|numberOfShards
argument_list|,
name|numberOfDocs
argument_list|,
name|size
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
DECL|method|testScroll
specifier|private
name|void
name|testScroll
parameter_list|(
name|int
name|numberOfShards
parameter_list|,
name|long
name|numberOfDocs
parameter_list|,
name|int
name|size
parameter_list|,
name|boolean
name|unbalanced
parameter_list|)
throws|throws
name|Exception
block|{
try|try
block|{
name|client
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareDelete
argument_list|(
literal|"test"
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// ignore
block|}
name|client
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
name|ImmutableSettings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"index.number_of_shards"
argument_list|,
name|numberOfShards
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|client
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
name|setWaitForGreenStatus
argument_list|()
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|ids
init|=
name|Sets
operator|.
name|newHashSet
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|expectedIds
init|=
name|Sets
operator|.
name|newHashSet
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
name|numberOfDocs
condition|;
name|i
operator|++
control|)
block|{
name|String
name|id
init|=
name|Integer
operator|.
name|toString
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|expectedIds
operator|.
name|add
argument_list|(
name|id
argument_list|)
expr_stmt|;
name|String
name|routing
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|unbalanced
condition|)
block|{
if|if
condition|(
name|i
operator|<
operator|(
name|numberOfDocs
operator|*
literal|0.6
operator|)
condition|)
block|{
name|routing
operator|=
literal|"0"
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|i
operator|<
operator|(
name|numberOfDocs
operator|*
literal|0.9
operator|)
condition|)
block|{
name|routing
operator|=
literal|"1"
expr_stmt|;
block|}
else|else
block|{
name|routing
operator|=
literal|"2"
expr_stmt|;
block|}
block|}
name|client
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
name|setRouting
argument_list|(
name|routing
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"field"
argument_list|,
name|i
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
block|}
name|client
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareRefresh
argument_list|()
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|SearchResponse
name|searchResponse
init|=
name|client
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
name|assertThat
argument_list|(
name|searchResponse
operator|.
name|hits
argument_list|()
operator|.
name|totalHits
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|numberOfDocs
argument_list|)
argument_list|)
expr_stmt|;
comment|// start scrolling, until we get not results
while|while
condition|(
literal|true
condition|)
block|{
name|searchResponse
operator|=
name|client
operator|.
name|prepareSearchScroll
argument_list|(
name|searchResponse
operator|.
name|scrollId
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
name|assertThat
argument_list|(
name|searchResponse
operator|.
name|hits
argument_list|()
operator|.
name|totalHits
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|numberOfDocs
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|searchResponse
operator|.
name|failedShards
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|SearchHit
name|hit
range|:
name|searchResponse
operator|.
name|hits
argument_list|()
control|)
block|{
name|assertThat
argument_list|(
name|hit
operator|.
name|id
argument_list|()
operator|+
literal|"should not exists in the result set"
argument_list|,
name|ids
operator|.
name|contains
argument_list|(
name|hit
operator|.
name|id
argument_list|()
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|ids
operator|.
name|add
argument_list|(
name|hit
operator|.
name|id
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|searchResponse
operator|.
name|hits
argument_list|()
operator|.
name|hits
argument_list|()
operator|.
name|length
operator|==
literal|0
condition|)
block|{
break|break;
block|}
block|}
name|assertThat
argument_list|(
name|expectedIds
argument_list|,
name|equalTo
argument_list|(
name|ids
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

