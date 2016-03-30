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
name|cluster
operator|.
name|metadata
operator|.
name|MappingMetaData
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
name|discovery
operator|.
name|MasterNotDiscoveredException
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
name|ESIntegTestCase
operator|.
name|ClusterScope
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
operator|.
name|Scope
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
annotation|@
name|ESIntegTestCase
operator|.
name|SuppressLocalMode
DECL|class|SpecificMasterNodesIT
specifier|public
class|class
name|SpecificMasterNodesIT
extends|extends
name|ESIntegTestCase
block|{
DECL|method|settingsBuilder
specifier|protected
specifier|final
name|Settings
operator|.
name|Builder
name|settingsBuilder
parameter_list|()
block|{
return|return
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"discovery.type"
argument_list|,
literal|"zen"
argument_list|)
return|;
block|}
DECL|method|testSimpleOnlyMasterNodeElection
specifier|public
name|void
name|testSimpleOnlyMasterNodeElection
parameter_list|()
throws|throws
name|IOException
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"--> start data node / non master node"
argument_list|)
expr_stmt|;
name|internalCluster
argument_list|()
operator|.
name|startNode
argument_list|(
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
name|Node
operator|.
name|NODE_DATA_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
literal|true
argument_list|)
operator|.
name|put
argument_list|(
name|Node
operator|.
name|NODE_MASTER_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
literal|false
argument_list|)
operator|.
name|put
argument_list|(
literal|"discovery.initial_state_timeout"
argument_list|,
literal|"1s"
argument_list|)
argument_list|)
expr_stmt|;
try|try
block|{
name|assertThat
argument_list|(
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
name|setMasterNodeTimeout
argument_list|(
literal|"100ms"
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
operator|.
name|nodes
argument_list|()
operator|.
name|getMasterNodeId
argument_list|()
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"should not be able to find master"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|MasterNotDiscoveredException
name|e
parameter_list|)
block|{
comment|// all is well, no master elected
block|}
name|logger
operator|.
name|info
argument_list|(
literal|"--> start master node"
argument_list|)
expr_stmt|;
specifier|final
name|String
name|masterNodeName
init|=
name|internalCluster
argument_list|()
operator|.
name|startNode
argument_list|(
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
name|Node
operator|.
name|NODE_DATA_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
literal|false
argument_list|)
operator|.
name|put
argument_list|(
name|Node
operator|.
name|NODE_MASTER_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
literal|true
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|internalCluster
argument_list|()
operator|.
name|nonMasterClient
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
operator|.
name|nodes
argument_list|()
operator|.
name|masterNode
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|masterNodeName
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|internalCluster
argument_list|()
operator|.
name|masterClient
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
operator|.
name|nodes
argument_list|()
operator|.
name|masterNode
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|masterNodeName
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> stop master node"
argument_list|)
expr_stmt|;
name|internalCluster
argument_list|()
operator|.
name|stopCurrentMasterNode
argument_list|()
expr_stmt|;
try|try
block|{
name|assertThat
argument_list|(
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
name|setMasterNodeTimeout
argument_list|(
literal|"100ms"
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
operator|.
name|nodes
argument_list|()
operator|.
name|getMasterNodeId
argument_list|()
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"should not be able to find master"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|MasterNotDiscoveredException
name|e
parameter_list|)
block|{
comment|// all is well, no master elected
block|}
name|logger
operator|.
name|info
argument_list|(
literal|"--> start master node"
argument_list|)
expr_stmt|;
specifier|final
name|String
name|nextMasterEligibleNodeName
init|=
name|internalCluster
argument_list|()
operator|.
name|startNode
argument_list|(
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
name|Node
operator|.
name|NODE_DATA_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
literal|false
argument_list|)
operator|.
name|put
argument_list|(
name|Node
operator|.
name|NODE_MASTER_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
literal|true
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|internalCluster
argument_list|()
operator|.
name|nonMasterClient
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
operator|.
name|nodes
argument_list|()
operator|.
name|masterNode
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|nextMasterEligibleNodeName
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|internalCluster
argument_list|()
operator|.
name|masterClient
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
operator|.
name|nodes
argument_list|()
operator|.
name|masterNode
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|nextMasterEligibleNodeName
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testElectOnlyBetweenMasterNodes
specifier|public
name|void
name|testElectOnlyBetweenMasterNodes
parameter_list|()
throws|throws
name|IOException
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"--> start data node / non master node"
argument_list|)
expr_stmt|;
name|internalCluster
argument_list|()
operator|.
name|startNode
argument_list|(
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
name|Node
operator|.
name|NODE_DATA_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
literal|true
argument_list|)
operator|.
name|put
argument_list|(
name|Node
operator|.
name|NODE_MASTER_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
literal|false
argument_list|)
operator|.
name|put
argument_list|(
literal|"discovery.initial_state_timeout"
argument_list|,
literal|"1s"
argument_list|)
argument_list|)
expr_stmt|;
try|try
block|{
name|assertThat
argument_list|(
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
name|setMasterNodeTimeout
argument_list|(
literal|"100ms"
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
operator|.
name|nodes
argument_list|()
operator|.
name|getMasterNodeId
argument_list|()
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"should not be able to find master"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|MasterNotDiscoveredException
name|e
parameter_list|)
block|{
comment|// all is well, no master elected
block|}
name|logger
operator|.
name|info
argument_list|(
literal|"--> start master node (1)"
argument_list|)
expr_stmt|;
specifier|final
name|String
name|masterNodeName
init|=
name|internalCluster
argument_list|()
operator|.
name|startNode
argument_list|(
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
name|Node
operator|.
name|NODE_DATA_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
literal|false
argument_list|)
operator|.
name|put
argument_list|(
name|Node
operator|.
name|NODE_MASTER_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
literal|true
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|internalCluster
argument_list|()
operator|.
name|nonMasterClient
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
operator|.
name|nodes
argument_list|()
operator|.
name|masterNode
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|masterNodeName
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|internalCluster
argument_list|()
operator|.
name|masterClient
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
operator|.
name|nodes
argument_list|()
operator|.
name|masterNode
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|masterNodeName
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> start master node (2)"
argument_list|)
expr_stmt|;
specifier|final
name|String
name|nextMasterEligableNodeName
init|=
name|internalCluster
argument_list|()
operator|.
name|startNode
argument_list|(
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
name|Node
operator|.
name|NODE_DATA_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
literal|false
argument_list|)
operator|.
name|put
argument_list|(
name|Node
operator|.
name|NODE_MASTER_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
literal|true
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|internalCluster
argument_list|()
operator|.
name|nonMasterClient
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
operator|.
name|nodes
argument_list|()
operator|.
name|masterNode
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|masterNodeName
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|internalCluster
argument_list|()
operator|.
name|nonMasterClient
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
operator|.
name|nodes
argument_list|()
operator|.
name|masterNode
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|masterNodeName
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|internalCluster
argument_list|()
operator|.
name|masterClient
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
operator|.
name|nodes
argument_list|()
operator|.
name|masterNode
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|masterNodeName
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> closing master node (1)"
argument_list|)
expr_stmt|;
name|internalCluster
argument_list|()
operator|.
name|stopCurrentMasterNode
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|internalCluster
argument_list|()
operator|.
name|nonMasterClient
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
operator|.
name|nodes
argument_list|()
operator|.
name|masterNode
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|nextMasterEligableNodeName
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|internalCluster
argument_list|()
operator|.
name|masterClient
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
operator|.
name|nodes
argument_list|()
operator|.
name|masterNode
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|nextMasterEligableNodeName
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**      * Tests that putting custom default mapping and then putting a type mapping will have the default mapping merged      * to the type mapping.      */
DECL|method|testCustomDefaultMapping
specifier|public
name|void
name|testCustomDefaultMapping
parameter_list|()
throws|throws
name|Exception
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"--> start master node / non data"
argument_list|)
expr_stmt|;
name|internalCluster
argument_list|()
operator|.
name|startNode
argument_list|(
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
name|Node
operator|.
name|NODE_DATA_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
literal|false
argument_list|)
operator|.
name|put
argument_list|(
name|Node
operator|.
name|NODE_MASTER_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> start data node / non master node"
argument_list|)
expr_stmt|;
name|internalCluster
argument_list|()
operator|.
name|startNode
argument_list|(
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
name|Node
operator|.
name|NODE_DATA_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
literal|true
argument_list|)
operator|.
name|put
argument_list|(
name|Node
operator|.
name|NODE_MASTER_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|createIndex
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
name|assertAcked
argument_list|(
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|preparePutMapping
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setType
argument_list|(
literal|"_default_"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"_timestamp"
argument_list|,
literal|"enabled=true"
argument_list|)
argument_list|)
expr_stmt|;
name|MappingMetaData
name|defaultMapping
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
name|get
argument_list|()
operator|.
name|getState
argument_list|()
operator|.
name|getMetaData
argument_list|()
operator|.
name|getIndices
argument_list|()
operator|.
name|get
argument_list|(
literal|"test"
argument_list|)
operator|.
name|getMappings
argument_list|()
operator|.
name|get
argument_list|(
literal|"_default_"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|defaultMapping
operator|.
name|getSourceAsMap
argument_list|()
operator|.
name|get
argument_list|(
literal|"_timestamp"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertAcked
argument_list|(
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|preparePutMapping
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setType
argument_list|(
literal|"_default_"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"_timestamp"
argument_list|,
literal|"enabled=true"
argument_list|)
argument_list|)
expr_stmt|;
name|assertAcked
argument_list|(
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|preparePutMapping
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setType
argument_list|(
literal|"type1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"foo"
argument_list|,
literal|"enabled=true"
argument_list|)
argument_list|)
expr_stmt|;
name|MappingMetaData
name|type1Mapping
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
name|get
argument_list|()
operator|.
name|getState
argument_list|()
operator|.
name|getMetaData
argument_list|()
operator|.
name|getIndices
argument_list|()
operator|.
name|get
argument_list|(
literal|"test"
argument_list|)
operator|.
name|getMappings
argument_list|()
operator|.
name|get
argument_list|(
literal|"type1"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|type1Mapping
operator|.
name|getSourceAsMap
argument_list|()
operator|.
name|get
argument_list|(
literal|"_timestamp"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testAliasFilterValidation
specifier|public
name|void
name|testAliasFilterValidation
parameter_list|()
throws|throws
name|Exception
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"--> start master node / non data"
argument_list|)
expr_stmt|;
name|internalCluster
argument_list|()
operator|.
name|startNode
argument_list|(
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
name|Node
operator|.
name|NODE_DATA_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
literal|false
argument_list|)
operator|.
name|put
argument_list|(
name|Node
operator|.
name|NODE_MASTER_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> start data node / non master node"
argument_list|)
expr_stmt|;
name|internalCluster
argument_list|()
operator|.
name|startNode
argument_list|(
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
name|Node
operator|.
name|NODE_DATA_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
literal|true
argument_list|)
operator|.
name|put
argument_list|(
name|Node
operator|.
name|NODE_MASTER_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertAcked
argument_list|(
name|prepareCreate
argument_list|(
literal|"test"
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"type1"
argument_list|,
literal|"{\"type1\" : {\"properties\" : {\"table_a\" : { \"type\" : \"nested\", \"properties\" : {\"field_a\" : { \"type\" : \"keyword\" },\"field_b\" :{ \"type\" : \"keyword\" }}}}}}"
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
name|prepareAliases
argument_list|()
operator|.
name|addAlias
argument_list|(
literal|"test"
argument_list|,
literal|"a_test"
argument_list|,
name|QueryBuilders
operator|.
name|nestedQuery
argument_list|(
literal|"table_a"
argument_list|,
name|QueryBuilders
operator|.
name|termQuery
argument_list|(
literal|"table_a.field_b"
argument_list|,
literal|"y"
argument_list|)
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

