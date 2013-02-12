begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.test.integration.indices.stats
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|integration
operator|.
name|indices
operator|.
name|stats
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
name|admin
operator|.
name|indices
operator|.
name|stats
operator|.
name|IndicesStats
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
name|get
operator|.
name|GetResponse
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
name|*
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|SimpleIndexStatsTests
specifier|public
class|class
name|SimpleIndexStatsTests
extends|extends
name|AbstractNodesTests
block|{
DECL|field|client
specifier|private
name|Client
name|client
decl_stmt|;
annotation|@
name|BeforeClass
DECL|method|createNodes
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
annotation|@
name|AfterClass
DECL|method|closeNodes
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
literal|"node2"
argument_list|)
return|;
block|}
annotation|@
name|Test
DECL|method|simpleStats
specifier|public
name|void
name|simpleStats
parameter_list|()
throws|throws
name|Exception
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
argument_list|()
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
comment|// rely on 1 replica for this tests
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
literal|"test1"
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
name|indices
argument_list|()
operator|.
name|prepareCreate
argument_list|(
literal|"test2"
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
decl_stmt|;
name|assertThat
argument_list|(
name|clusterHealthResponse
operator|.
name|timedOut
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|client
operator|.
name|prepareIndex
argument_list|(
literal|"test1"
argument_list|,
literal|"type1"
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
literal|1
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
name|client
operator|.
name|prepareIndex
argument_list|(
literal|"test1"
argument_list|,
literal|"type2"
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
literal|1
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
name|client
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
literal|1
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
name|IndicesStats
name|stats
init|=
name|client
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
name|stats
operator|.
name|primaries
argument_list|()
operator|.
name|docs
argument_list|()
operator|.
name|count
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|3l
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|stats
operator|.
name|total
argument_list|()
operator|.
name|docs
argument_list|()
operator|.
name|count
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|6l
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|stats
operator|.
name|primaries
argument_list|()
operator|.
name|indexing
argument_list|()
operator|.
name|total
argument_list|()
operator|.
name|indexCount
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|3l
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|stats
operator|.
name|total
argument_list|()
operator|.
name|indexing
argument_list|()
operator|.
name|total
argument_list|()
operator|.
name|indexCount
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|6l
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|stats
operator|.
name|total
argument_list|()
operator|.
name|store
argument_list|()
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
comment|// verify nulls
name|assertThat
argument_list|(
name|stats
operator|.
name|total
argument_list|()
operator|.
name|merge
argument_list|()
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|stats
operator|.
name|total
argument_list|()
operator|.
name|flush
argument_list|()
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|stats
operator|.
name|total
argument_list|()
operator|.
name|refresh
argument_list|()
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|stats
operator|.
name|index
argument_list|(
literal|"test1"
argument_list|)
operator|.
name|primaries
argument_list|()
operator|.
name|docs
argument_list|()
operator|.
name|count
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|2l
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|stats
operator|.
name|index
argument_list|(
literal|"test1"
argument_list|)
operator|.
name|total
argument_list|()
operator|.
name|docs
argument_list|()
operator|.
name|count
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|4l
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|stats
operator|.
name|index
argument_list|(
literal|"test1"
argument_list|)
operator|.
name|primaries
argument_list|()
operator|.
name|store
argument_list|()
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|stats
operator|.
name|index
argument_list|(
literal|"test1"
argument_list|)
operator|.
name|primaries
argument_list|()
operator|.
name|merge
argument_list|()
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|stats
operator|.
name|index
argument_list|(
literal|"test1"
argument_list|)
operator|.
name|primaries
argument_list|()
operator|.
name|flush
argument_list|()
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|stats
operator|.
name|index
argument_list|(
literal|"test1"
argument_list|)
operator|.
name|primaries
argument_list|()
operator|.
name|refresh
argument_list|()
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|stats
operator|.
name|index
argument_list|(
literal|"test2"
argument_list|)
operator|.
name|primaries
argument_list|()
operator|.
name|docs
argument_list|()
operator|.
name|count
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1l
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|stats
operator|.
name|index
argument_list|(
literal|"test2"
argument_list|)
operator|.
name|total
argument_list|()
operator|.
name|docs
argument_list|()
operator|.
name|count
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|2l
argument_list|)
argument_list|)
expr_stmt|;
comment|// make sure that number of requests in progress is 0
name|assertThat
argument_list|(
name|stats
operator|.
name|index
argument_list|(
literal|"test1"
argument_list|)
operator|.
name|total
argument_list|()
operator|.
name|indexing
argument_list|()
operator|.
name|total
argument_list|()
operator|.
name|indexCurrent
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
name|stats
operator|.
name|index
argument_list|(
literal|"test1"
argument_list|)
operator|.
name|total
argument_list|()
operator|.
name|indexing
argument_list|()
operator|.
name|total
argument_list|()
operator|.
name|deleteCurrent
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
name|stats
operator|.
name|index
argument_list|(
literal|"test1"
argument_list|)
operator|.
name|total
argument_list|()
operator|.
name|search
argument_list|()
operator|.
name|total
argument_list|()
operator|.
name|fetchCurrent
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
name|stats
operator|.
name|index
argument_list|(
literal|"test1"
argument_list|)
operator|.
name|total
argument_list|()
operator|.
name|search
argument_list|()
operator|.
name|total
argument_list|()
operator|.
name|queryCurrent
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0l
argument_list|)
argument_list|)
expr_stmt|;
comment|// check flags
name|stats
operator|=
name|client
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
name|setDocs
argument_list|(
literal|false
argument_list|)
operator|.
name|setStore
argument_list|(
literal|false
argument_list|)
operator|.
name|setIndexing
argument_list|(
literal|false
argument_list|)
operator|.
name|setFlush
argument_list|(
literal|true
argument_list|)
operator|.
name|setRefresh
argument_list|(
literal|true
argument_list|)
operator|.
name|setMerge
argument_list|(
literal|true
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
name|stats
operator|.
name|total
argument_list|()
operator|.
name|docs
argument_list|()
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|stats
operator|.
name|total
argument_list|()
operator|.
name|store
argument_list|()
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|stats
operator|.
name|total
argument_list|()
operator|.
name|indexing
argument_list|()
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|stats
operator|.
name|total
argument_list|()
operator|.
name|merge
argument_list|()
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|stats
operator|.
name|total
argument_list|()
operator|.
name|flush
argument_list|()
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|stats
operator|.
name|total
argument_list|()
operator|.
name|refresh
argument_list|()
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
comment|// check types
name|stats
operator|=
name|client
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
name|setTypes
argument_list|(
literal|"type1"
argument_list|,
literal|"type"
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
name|stats
operator|.
name|primaries
argument_list|()
operator|.
name|indexing
argument_list|()
operator|.
name|typeStats
argument_list|()
operator|.
name|get
argument_list|(
literal|"type1"
argument_list|)
operator|.
name|indexCount
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1l
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|stats
operator|.
name|primaries
argument_list|()
operator|.
name|indexing
argument_list|()
operator|.
name|typeStats
argument_list|()
operator|.
name|get
argument_list|(
literal|"type"
argument_list|)
operator|.
name|indexCount
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1l
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|stats
operator|.
name|primaries
argument_list|()
operator|.
name|indexing
argument_list|()
operator|.
name|typeStats
argument_list|()
operator|.
name|get
argument_list|(
literal|"type2"
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|stats
operator|.
name|primaries
argument_list|()
operator|.
name|indexing
argument_list|()
operator|.
name|typeStats
argument_list|()
operator|.
name|get
argument_list|(
literal|"type1"
argument_list|)
operator|.
name|indexCurrent
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
name|stats
operator|.
name|primaries
argument_list|()
operator|.
name|indexing
argument_list|()
operator|.
name|typeStats
argument_list|()
operator|.
name|get
argument_list|(
literal|"type1"
argument_list|)
operator|.
name|deleteCurrent
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
name|stats
operator|.
name|total
argument_list|()
operator|.
name|get
argument_list|()
operator|.
name|count
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0l
argument_list|)
argument_list|)
expr_stmt|;
comment|// check get
name|GetResponse
name|getResponse
init|=
name|client
operator|.
name|prepareGet
argument_list|(
literal|"test1"
argument_list|,
literal|"type1"
argument_list|,
literal|"1"
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
name|getResponse
operator|.
name|exists
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|stats
operator|=
name|client
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
name|stats
operator|.
name|total
argument_list|()
operator|.
name|get
argument_list|()
operator|.
name|count
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1l
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|stats
operator|.
name|total
argument_list|()
operator|.
name|get
argument_list|()
operator|.
name|existsCount
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1l
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|stats
operator|.
name|total
argument_list|()
operator|.
name|get
argument_list|()
operator|.
name|missingCount
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0l
argument_list|)
argument_list|)
expr_stmt|;
comment|// missing get
name|getResponse
operator|=
name|client
operator|.
name|prepareGet
argument_list|(
literal|"test1"
argument_list|,
literal|"type1"
argument_list|,
literal|"2"
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
name|getResponse
operator|.
name|exists
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|stats
operator|=
name|client
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
name|stats
operator|.
name|total
argument_list|()
operator|.
name|get
argument_list|()
operator|.
name|count
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|2l
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|stats
operator|.
name|total
argument_list|()
operator|.
name|get
argument_list|()
operator|.
name|existsCount
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1l
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|stats
operator|.
name|total
argument_list|()
operator|.
name|get
argument_list|()
operator|.
name|missingCount
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1l
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

