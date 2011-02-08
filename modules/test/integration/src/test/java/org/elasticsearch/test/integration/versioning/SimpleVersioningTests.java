begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.test.integration.versioning
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|integration
operator|.
name|versioning
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticSearchException
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
name|engine
operator|.
name|VersionConflictEngineException
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
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|xcontent
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

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|SimpleVersioningTests
specifier|public
class|class
name|SimpleVersioningTests
extends|extends
name|AbstractNodesTests
block|{
DECL|field|client
specifier|private
name|Client
name|client
decl_stmt|;
DECL|field|client2
specifier|private
name|Client
name|client2
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
comment|// make sure we use bloom filters here!
name|Settings
name|settings
init|=
name|ImmutableSettings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"index.engine.robin.async_load_bloom"
argument_list|,
literal|false
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|startNode
argument_list|(
literal|"server1"
argument_list|,
name|settings
argument_list|)
expr_stmt|;
name|startNode
argument_list|(
literal|"server2"
argument_list|,
name|settings
argument_list|)
expr_stmt|;
name|client
operator|=
name|client
argument_list|(
literal|"server1"
argument_list|)
expr_stmt|;
name|client2
operator|=
name|client
argument_list|(
literal|"server2"
argument_list|)
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
DECL|method|testSimpleVersioning
annotation|@
name|Test
specifier|public
name|void
name|testSimpleVersioning
parameter_list|()
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
name|IndexMissingException
name|e
parameter_list|)
block|{
comment|// its ok
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
argument_list|(
literal|"test"
argument_list|)
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
name|IndexResponse
name|indexResponse
init|=
name|client
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"field1"
argument_list|,
literal|"value1_1"
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
name|indexResponse
operator|.
name|version
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1l
argument_list|)
argument_list|)
expr_stmt|;
name|indexResponse
operator|=
name|client
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"field1"
argument_list|,
literal|"value1_2"
argument_list|)
operator|.
name|setVersion
argument_list|(
literal|1
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
name|indexResponse
operator|.
name|version
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|2l
argument_list|)
argument_list|)
expr_stmt|;
try|try
block|{
name|client
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"field1"
argument_list|,
literal|"value1_1"
argument_list|)
operator|.
name|setVersion
argument_list|(
literal|1
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
name|ElasticSearchException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|e
operator|.
name|unwrapCause
argument_list|()
argument_list|,
name|instanceOf
argument_list|(
name|VersionConflictEngineException
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|client2
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"field1"
argument_list|,
literal|"value1_1"
argument_list|)
operator|.
name|setVersion
argument_list|(
literal|1
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
name|ElasticSearchException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|e
operator|.
name|unwrapCause
argument_list|()
argument_list|,
name|instanceOf
argument_list|(
name|VersionConflictEngineException
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|client
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setCreate
argument_list|(
literal|true
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"field1"
argument_list|,
literal|"value1_1"
argument_list|)
operator|.
name|setVersion
argument_list|(
literal|1
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
name|ElasticSearchException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|e
operator|.
name|unwrapCause
argument_list|()
argument_list|,
name|instanceOf
argument_list|(
name|VersionConflictEngineException
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|client2
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setCreate
argument_list|(
literal|true
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"field1"
argument_list|,
literal|"value1_1"
argument_list|)
operator|.
name|setVersion
argument_list|(
literal|1
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
name|ElasticSearchException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|e
operator|.
name|unwrapCause
argument_list|()
argument_list|,
name|instanceOf
argument_list|(
name|VersionConflictEngineException
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|client
operator|.
name|prepareDelete
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setVersion
argument_list|(
literal|1
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
name|ElasticSearchException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|e
operator|.
name|unwrapCause
argument_list|()
argument_list|,
name|instanceOf
argument_list|(
name|VersionConflictEngineException
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|client2
operator|.
name|prepareDelete
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setVersion
argument_list|(
literal|1
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
name|ElasticSearchException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|e
operator|.
name|unwrapCause
argument_list|()
argument_list|,
name|instanceOf
argument_list|(
name|VersionConflictEngineException
operator|.
name|class
argument_list|)
argument_list|)
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
name|assertThat
argument_list|(
name|client
operator|.
name|prepareGet
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
operator|.
name|version
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|2l
argument_list|)
argument_list|)
expr_stmt|;
block|}
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
name|SearchResponse
name|searchResponse
init|=
name|client
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
name|setVersion
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
name|searchResponse
operator|.
name|hits
argument_list|()
operator|.
name|getAt
argument_list|(
literal|0
argument_list|)
operator|.
name|version
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|2l
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testSimpleVersioningWithFlush
annotation|@
name|Test
specifier|public
name|void
name|testSimpleVersioningWithFlush
parameter_list|()
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
name|IndexMissingException
name|e
parameter_list|)
block|{
comment|// its ok
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
argument_list|(
literal|"test"
argument_list|)
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
name|IndexResponse
name|indexResponse
init|=
name|client
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"field1"
argument_list|,
literal|"value1_1"
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
name|indexResponse
operator|.
name|version
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1l
argument_list|)
argument_list|)
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
argument_list|()
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|indexResponse
operator|=
name|client
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"field1"
argument_list|,
literal|"value1_2"
argument_list|)
operator|.
name|setVersion
argument_list|(
literal|1
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
name|indexResponse
operator|.
name|version
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|2l
argument_list|)
argument_list|)
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
argument_list|()
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
try|try
block|{
name|client
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"field1"
argument_list|,
literal|"value1_1"
argument_list|)
operator|.
name|setVersion
argument_list|(
literal|1
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
name|ElasticSearchException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|e
operator|.
name|unwrapCause
argument_list|()
argument_list|,
name|instanceOf
argument_list|(
name|VersionConflictEngineException
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|client2
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"field1"
argument_list|,
literal|"value1_1"
argument_list|)
operator|.
name|setVersion
argument_list|(
literal|1
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
name|ElasticSearchException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|e
operator|.
name|unwrapCause
argument_list|()
argument_list|,
name|instanceOf
argument_list|(
name|VersionConflictEngineException
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|client
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setCreate
argument_list|(
literal|true
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"field1"
argument_list|,
literal|"value1_1"
argument_list|)
operator|.
name|setVersion
argument_list|(
literal|1
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
name|ElasticSearchException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|e
operator|.
name|unwrapCause
argument_list|()
argument_list|,
name|instanceOf
argument_list|(
name|VersionConflictEngineException
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|client2
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setCreate
argument_list|(
literal|true
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"field1"
argument_list|,
literal|"value1_1"
argument_list|)
operator|.
name|setVersion
argument_list|(
literal|1
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
name|ElasticSearchException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|e
operator|.
name|unwrapCause
argument_list|()
argument_list|,
name|instanceOf
argument_list|(
name|VersionConflictEngineException
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|client
operator|.
name|prepareDelete
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setVersion
argument_list|(
literal|1
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
name|ElasticSearchException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|e
operator|.
name|unwrapCause
argument_list|()
argument_list|,
name|instanceOf
argument_list|(
name|VersionConflictEngineException
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|client2
operator|.
name|prepareDelete
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setVersion
argument_list|(
literal|1
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
name|ElasticSearchException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|e
operator|.
name|unwrapCause
argument_list|()
argument_list|,
name|instanceOf
argument_list|(
name|VersionConflictEngineException
operator|.
name|class
argument_list|)
argument_list|)
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
name|assertThat
argument_list|(
name|client
operator|.
name|prepareGet
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
operator|.
name|version
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|2l
argument_list|)
argument_list|)
expr_stmt|;
block|}
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
name|SearchResponse
name|searchResponse
init|=
name|client
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
name|setVersion
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
name|searchResponse
operator|.
name|hits
argument_list|()
operator|.
name|getAt
argument_list|(
literal|0
argument_list|)
operator|.
name|version
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|2l
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

