begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.test.integration.cluster
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|integration
operator|.
name|cluster
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
name|percolate
operator|.
name|PercolateSourceBuilder
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
name|common
operator|.
name|xcontent
operator|.
name|XContentFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|discovery
operator|.
name|Discovery
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|node
operator|.
name|Node
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
name|integration
operator|.
name|AbstractNodesTests
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|After
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
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
name|HashMap
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|settings
operator|.
name|ImmutableSettings
operator|.
name|settingsBuilder
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

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|NoMasterNodeTests
specifier|public
class|class
name|NoMasterNodeTests
extends|extends
name|AbstractNodesTests
block|{
annotation|@
name|After
DECL|method|cleanAndCloseNodes
specifier|public
name|void
name|cleanAndCloseNodes
parameter_list|()
throws|throws
name|Exception
block|{
name|closeAllNodes
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testNoMasterActions
specifier|public
name|void
name|testNoMasterActions
parameter_list|()
throws|throws
name|Exception
block|{
name|Settings
name|settings
init|=
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"discovery.type"
argument_list|,
literal|"zen"
argument_list|)
operator|.
name|put
argument_list|(
literal|"action.auto_create_index"
argument_list|,
literal|false
argument_list|)
operator|.
name|put
argument_list|(
literal|"discovery.zen.minimum_master_nodes"
argument_list|,
literal|2
argument_list|)
operator|.
name|put
argument_list|(
literal|"discovery.zen.ping_timeout"
argument_list|,
literal|"200ms"
argument_list|)
operator|.
name|put
argument_list|(
literal|"discovery.initial_state_timeout"
argument_list|,
literal|"500ms"
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.number_of_shards"
argument_list|,
literal|1
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|TimeValue
name|timeout
init|=
name|TimeValue
operator|.
name|timeValueMillis
argument_list|(
literal|200
argument_list|)
decl_stmt|;
name|Node
name|node
init|=
name|startNode
argument_list|(
literal|"node1"
argument_list|,
name|settings
argument_list|)
decl_stmt|;
comment|// start a second node, create an index, and then shut it down so we have no master block
name|Node
name|node2
init|=
name|startNode
argument_list|(
literal|"node2"
argument_list|,
name|settings
argument_list|)
decl_stmt|;
name|node
operator|.
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
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|node2
operator|.
name|close
argument_list|()
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|200
argument_list|)
expr_stmt|;
name|ClusterState
name|state
init|=
name|node
operator|.
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
name|setLocal
argument_list|(
literal|true
argument_list|)
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
name|assertThat
argument_list|(
name|state
operator|.
name|blocks
argument_list|()
operator|.
name|hasGlobalBlock
argument_list|(
name|Discovery
operator|.
name|NO_MASTER_BLOCK
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
try|try
block|{
name|node
operator|.
name|client
argument_list|()
operator|.
name|prepareGet
argument_list|(
literal|"test"
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
expr_stmt|;
assert|assert
literal|false
assert|;
block|}
catch|catch
parameter_list|(
name|ClusterBlockException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|e
operator|.
name|status
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|RestStatus
operator|.
name|SERVICE_UNAVAILABLE
argument_list|)
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|node
operator|.
name|client
argument_list|()
operator|.
name|prepareMultiGet
argument_list|()
operator|.
name|add
argument_list|(
literal|"test"
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
expr_stmt|;
assert|assert
literal|false
assert|;
block|}
catch|catch
parameter_list|(
name|ClusterBlockException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|e
operator|.
name|status
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|RestStatus
operator|.
name|SERVICE_UNAVAILABLE
argument_list|)
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|PercolateSourceBuilder
name|percolateSource
init|=
operator|new
name|PercolateSourceBuilder
argument_list|()
decl_stmt|;
name|percolateSource
operator|.
name|percolateDocument
argument_list|()
operator|.
name|setDoc
argument_list|(
operator|new
name|HashMap
argument_list|()
argument_list|)
expr_stmt|;
name|node
operator|.
name|client
argument_list|()
operator|.
name|preparePercolate
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|)
operator|.
name|setSource
argument_list|(
name|percolateSource
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
assert|assert
literal|false
assert|;
block|}
catch|catch
parameter_list|(
name|ClusterBlockException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|e
operator|.
name|status
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|RestStatus
operator|.
name|SERVICE_UNAVAILABLE
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|long
name|now
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
try|try
block|{
name|node
operator|.
name|client
argument_list|()
operator|.
name|prepareUpdate
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setScript
argument_list|(
literal|"test script"
argument_list|)
operator|.
name|setTimeout
argument_list|(
name|timeout
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
assert|assert
literal|false
assert|;
block|}
catch|catch
parameter_list|(
name|ClusterBlockException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|now
argument_list|,
name|greaterThan
argument_list|(
name|timeout
operator|.
name|millis
argument_list|()
operator|-
literal|50
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|status
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|RestStatus
operator|.
name|SERVICE_UNAVAILABLE
argument_list|)
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|node
operator|.
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareAnalyze
argument_list|(
literal|"test"
argument_list|,
literal|"this is a test"
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
assert|assert
literal|false
assert|;
block|}
catch|catch
parameter_list|(
name|ClusterBlockException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|e
operator|.
name|status
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|RestStatus
operator|.
name|SERVICE_UNAVAILABLE
argument_list|)
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|node
operator|.
name|client
argument_list|()
operator|.
name|prepareCount
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
assert|assert
literal|false
assert|;
block|}
catch|catch
parameter_list|(
name|ClusterBlockException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|e
operator|.
name|status
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|RestStatus
operator|.
name|SERVICE_UNAVAILABLE
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|now
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
try|try
block|{
name|node
operator|.
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|(
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|endObject
argument_list|()
argument_list|)
operator|.
name|setTimeout
argument_list|(
name|timeout
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
assert|assert
literal|false
assert|;
block|}
catch|catch
parameter_list|(
name|ClusterBlockException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|now
argument_list|,
name|greaterThan
argument_list|(
name|timeout
operator|.
name|millis
argument_list|()
operator|-
literal|50
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|status
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|RestStatus
operator|.
name|SERVICE_UNAVAILABLE
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

