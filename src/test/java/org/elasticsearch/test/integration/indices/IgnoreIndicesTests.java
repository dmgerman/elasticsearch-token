begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.test.integration.indices
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
name|search
operator|.
name|MultiSearchResponse
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
name|support
operator|.
name|IgnoreIndices
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
name|block
operator|.
name|ClusterBlockException
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
name|indices
operator|.
name|IndexMissingException
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

begin_import
import|import static
name|org
operator|.
name|testng
operator|.
name|Assert
operator|.
name|fail
import|;
end_import

begin_class
DECL|class|IgnoreIndicesTests
specifier|public
class|class
name|IgnoreIndicesTests
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
name|client
operator|=
name|client
argument_list|(
literal|"node1"
argument_list|)
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
annotation|@
name|Test
DECL|method|testMissing
specifier|public
name|void
name|testMissing
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
name|setWaitForYellowStatus
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
name|isTimedOut
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
try|try
block|{
name|client
operator|.
name|prepareSearch
argument_list|(
literal|"test1"
argument_list|,
literal|"test2"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
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
name|fail
argument_list|(
literal|"Exception should have been thrown."
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IndexMissingException
name|e
parameter_list|)
block|{         }
name|MultiSearchResponse
name|multiSearchResponse
init|=
name|client
operator|.
name|prepareMultiSearch
argument_list|()
operator|.
name|add
argument_list|(
name|client
operator|.
name|prepareSearch
argument_list|(
literal|"test1"
argument_list|,
literal|"test2"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
name|matchAllQuery
argument_list|()
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
name|multiSearchResponse
operator|.
name|getResponses
argument_list|()
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|multiSearchResponse
operator|.
name|getResponses
argument_list|()
index|[
literal|0
index|]
operator|.
name|getResponse
argument_list|()
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
name|client
operator|.
name|prepareCount
argument_list|(
literal|"test1"
argument_list|,
literal|"test2"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
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
name|fail
argument_list|(
literal|"Exception should have been thrown."
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IndexMissingException
name|e
parameter_list|)
block|{         }
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
name|prepareClearCache
argument_list|(
literal|"test1"
argument_list|,
literal|"test2"
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
literal|"Exception should have been thrown."
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IndexMissingException
name|e
parameter_list|)
block|{         }
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
name|prepareFlush
argument_list|(
literal|"test1"
argument_list|,
literal|"test2"
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
literal|"Exception should have been thrown."
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IndexMissingException
name|e
parameter_list|)
block|{         }
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
name|prepareGatewaySnapshot
argument_list|(
literal|"test1"
argument_list|,
literal|"test2"
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
literal|"Exception should have been thrown."
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IndexMissingException
name|e
parameter_list|)
block|{         }
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
name|prepareSegments
argument_list|(
literal|"test1"
argument_list|,
literal|"test2"
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
literal|"Exception should have been thrown."
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IndexMissingException
name|e
parameter_list|)
block|{         }
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
name|prepareStats
argument_list|(
literal|"test1"
argument_list|,
literal|"test2"
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
literal|"Exception should have been thrown."
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IndexMissingException
name|e
parameter_list|)
block|{         }
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
name|prepareStatus
argument_list|(
literal|"test1"
argument_list|,
literal|"test2"
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
literal|"Exception should have been thrown."
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IndexMissingException
name|e
parameter_list|)
block|{         }
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
name|prepareOptimize
argument_list|(
literal|"test1"
argument_list|,
literal|"test2"
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
literal|"Exception should have been thrown."
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IndexMissingException
name|e
parameter_list|)
block|{         }
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
name|prepareRefresh
argument_list|(
literal|"test1"
argument_list|,
literal|"test2"
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
literal|"Exception should have been thrown."
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IndexMissingException
name|e
parameter_list|)
block|{         }
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
name|prepareValidateQuery
argument_list|(
literal|"test1"
argument_list|,
literal|"test2"
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
literal|"Exception should have been thrown."
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IndexMissingException
name|e
parameter_list|)
block|{         }
name|client
operator|.
name|prepareSearch
argument_list|(
literal|"test1"
argument_list|,
literal|"test2"
argument_list|)
operator|.
name|setIgnoreIndices
argument_list|(
name|IgnoreIndices
operator|.
name|MISSING
argument_list|)
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
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
name|multiSearchResponse
operator|=
name|client
operator|.
name|prepareMultiSearch
argument_list|()
operator|.
name|setIgnoreIndices
argument_list|(
name|IgnoreIndices
operator|.
name|MISSING
argument_list|)
operator|.
name|add
argument_list|(
name|client
operator|.
name|prepareSearch
argument_list|(
literal|"test1"
argument_list|,
literal|"test2"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
name|matchAllQuery
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
name|assertThat
argument_list|(
name|multiSearchResponse
operator|.
name|getResponses
argument_list|()
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|multiSearchResponse
operator|.
name|getResponses
argument_list|()
index|[
literal|0
index|]
operator|.
name|getResponse
argument_list|()
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|client
operator|.
name|prepareCount
argument_list|(
literal|"test1"
argument_list|,
literal|"test2"
argument_list|)
operator|.
name|setIgnoreIndices
argument_list|(
name|IgnoreIndices
operator|.
name|MISSING
argument_list|)
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
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
name|client
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareClearCache
argument_list|(
literal|"test1"
argument_list|,
literal|"test2"
argument_list|)
operator|.
name|setIgnoreIndices
argument_list|(
name|IgnoreIndices
operator|.
name|MISSING
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
name|prepareFlush
argument_list|(
literal|"test1"
argument_list|,
literal|"test2"
argument_list|)
operator|.
name|setIgnoreIndices
argument_list|(
name|IgnoreIndices
operator|.
name|MISSING
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
name|prepareGatewaySnapshot
argument_list|(
literal|"test1"
argument_list|,
literal|"test2"
argument_list|)
operator|.
name|setIgnoreIndices
argument_list|(
name|IgnoreIndices
operator|.
name|MISSING
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
name|prepareSegments
argument_list|(
literal|"test1"
argument_list|,
literal|"test2"
argument_list|)
operator|.
name|setIgnoreIndices
argument_list|(
name|IgnoreIndices
operator|.
name|MISSING
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
name|prepareStats
argument_list|(
literal|"test1"
argument_list|,
literal|"test2"
argument_list|)
operator|.
name|setIgnoreIndices
argument_list|(
name|IgnoreIndices
operator|.
name|MISSING
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
name|prepareStatus
argument_list|(
literal|"test1"
argument_list|,
literal|"test2"
argument_list|)
operator|.
name|setIgnoreIndices
argument_list|(
name|IgnoreIndices
operator|.
name|MISSING
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
name|prepareOptimize
argument_list|(
literal|"test1"
argument_list|,
literal|"test2"
argument_list|)
operator|.
name|setIgnoreIndices
argument_list|(
name|IgnoreIndices
operator|.
name|MISSING
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
argument_list|(
literal|"test1"
argument_list|,
literal|"test2"
argument_list|)
operator|.
name|setIgnoreIndices
argument_list|(
name|IgnoreIndices
operator|.
name|MISSING
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
name|prepareValidateQuery
argument_list|(
literal|"test1"
argument_list|,
literal|"test2"
argument_list|)
operator|.
name|setIgnoreIndices
argument_list|(
name|IgnoreIndices
operator|.
name|MISSING
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
name|clusterHealthResponse
operator|=
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
name|setWaitForYellowStatus
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
name|client
operator|.
name|prepareSearch
argument_list|(
literal|"test1"
argument_list|,
literal|"test2"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
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
name|client
operator|.
name|prepareCount
argument_list|(
literal|"test1"
argument_list|,
literal|"test2"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
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
name|client
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareClearCache
argument_list|(
literal|"test1"
argument_list|,
literal|"test2"
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
name|prepareFlush
argument_list|(
literal|"test1"
argument_list|,
literal|"test2"
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
name|prepareGatewaySnapshot
argument_list|(
literal|"test1"
argument_list|,
literal|"test2"
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
name|prepareSegments
argument_list|(
literal|"test1"
argument_list|,
literal|"test2"
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
name|prepareStats
argument_list|(
literal|"test1"
argument_list|,
literal|"test2"
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
name|prepareStatus
argument_list|(
literal|"test1"
argument_list|,
literal|"test2"
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
name|prepareOptimize
argument_list|(
literal|"test1"
argument_list|,
literal|"test2"
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
argument_list|(
literal|"test1"
argument_list|,
literal|"test2"
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
name|prepareValidateQuery
argument_list|(
literal|"test1"
argument_list|,
literal|"test2"
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
comment|// For now don't handle closed indices
DECL|method|testClosed
specifier|public
name|void
name|testClosed
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
name|setWaitForYellowStatus
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
name|isTimedOut
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
name|prepareSearch
argument_list|(
literal|"test1"
argument_list|,
literal|"test2"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
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
name|client
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareClose
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
name|clusterHealthResponse
operator|=
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
name|setWaitForYellowStatus
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
try|try
block|{
name|client
operator|.
name|prepareSearch
argument_list|(
literal|"test1"
argument_list|,
literal|"test2"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
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
name|fail
argument_list|(
literal|"Exception should have been thrown"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ClusterBlockException
name|e
parameter_list|)
block|{         }
try|try
block|{
name|client
operator|.
name|prepareCount
argument_list|(
literal|"test1"
argument_list|,
literal|"test2"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
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
name|fail
argument_list|(
literal|"Exception should have been thrown"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ClusterBlockException
name|e
parameter_list|)
block|{         }
block|}
block|}
end_class

end_unit

