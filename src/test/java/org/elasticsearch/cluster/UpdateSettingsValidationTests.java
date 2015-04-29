begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.cluster
package|package
name|org
operator|.
name|elasticsearch
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
name|test
operator|.
name|ElasticsearchIntegrationTest
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
name|ElasticsearchIntegrationTest
operator|.
name|ClusterScope
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
name|List
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
name|elasticsearch
operator|.
name|test
operator|.
name|ElasticsearchIntegrationTest
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
name|equalTo
import|;
end_import

begin_comment
comment|/**  */
end_comment

begin_class
annotation|@
name|ClusterScope
argument_list|(
name|scope
operator|=
name|Scope
operator|.
name|TEST
argument_list|,
name|numDataNodes
operator|=
literal|0
argument_list|)
DECL|class|UpdateSettingsValidationTests
specifier|public
class|class
name|UpdateSettingsValidationTests
extends|extends
name|ElasticsearchIntegrationTest
block|{
annotation|@
name|Test
DECL|method|testUpdateSettingsValidation
specifier|public
name|void
name|testUpdateSettingsValidation
parameter_list|()
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|String
argument_list|>
name|nodes
init|=
name|internalCluster
argument_list|()
operator|.
name|startNodesAsync
argument_list|(
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"node.data"
argument_list|,
literal|false
argument_list|)
operator|.
name|build
argument_list|()
argument_list|,
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"node.master"
argument_list|,
literal|false
argument_list|)
operator|.
name|build
argument_list|()
argument_list|,
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"node.master"
argument_list|,
literal|false
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|String
name|master
init|=
name|nodes
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|String
name|node_1
init|=
name|nodes
operator|.
name|get
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|String
name|node_2
init|=
name|nodes
operator|.
name|get
argument_list|(
literal|2
argument_list|)
decl_stmt|;
name|createIndex
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
name|NumShards
name|test
init|=
name|getNumShards
argument_list|(
literal|"test"
argument_list|)
decl_stmt|;
name|ClusterHealthResponse
name|healthResponse
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
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setWaitForEvents
argument_list|(
name|Priority
operator|.
name|LANGUID
argument_list|)
operator|.
name|setWaitForNodes
argument_list|(
literal|"3"
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
decl_stmt|;
name|assertThat
argument_list|(
name|healthResponse
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
name|assertThat
argument_list|(
name|healthResponse
operator|.
name|getIndices
argument_list|()
operator|.
name|get
argument_list|(
literal|"test"
argument_list|)
operator|.
name|getActiveShards
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|test
operator|.
name|totalNumShards
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
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"index.number_of_replicas"
argument_list|,
literal|0
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|healthResponse
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
name|setWaitForEvents
argument_list|(
name|Priority
operator|.
name|LANGUID
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
name|assertThat
argument_list|(
name|healthResponse
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
name|assertThat
argument_list|(
name|healthResponse
operator|.
name|getIndices
argument_list|()
operator|.
name|get
argument_list|(
literal|"test"
argument_list|)
operator|.
name|getActiveShards
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|test
operator|.
name|numPrimaries
argument_list|)
argument_list|)
expr_stmt|;
try|try
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
name|prepareUpdateSettings
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setSettings
argument_list|(
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"index.refresh_interval"
argument_list|,
literal|""
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|ex
parameter_list|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"Error message: [{}]"
argument_list|,
name|ex
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

