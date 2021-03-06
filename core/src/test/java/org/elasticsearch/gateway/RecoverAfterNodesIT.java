begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.gateway
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|gateway
package|;
end_package

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
name|ClusterBlock
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
name|ClusterBlockLevel
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
name|discovery
operator|.
name|zen
operator|.
name|ElectMasterService
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
name|util
operator|.
name|Set
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
name|hasItem
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
argument_list|,
name|autoMinMasterNodes
operator|=
literal|false
argument_list|)
DECL|class|RecoverAfterNodesIT
specifier|public
class|class
name|RecoverAfterNodesIT
extends|extends
name|ESIntegTestCase
block|{
DECL|field|BLOCK_WAIT_TIMEOUT
specifier|private
specifier|static
specifier|final
name|TimeValue
name|BLOCK_WAIT_TIMEOUT
init|=
name|TimeValue
operator|.
name|timeValueSeconds
argument_list|(
literal|10
argument_list|)
decl_stmt|;
DECL|method|waitForNoBlocksOnNode
specifier|public
name|Set
argument_list|<
name|ClusterBlock
argument_list|>
name|waitForNoBlocksOnNode
parameter_list|(
name|TimeValue
name|timeout
parameter_list|,
name|Client
name|nodeClient
parameter_list|)
throws|throws
name|InterruptedException
block|{
name|long
name|start
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|ClusterBlock
argument_list|>
name|blocks
decl_stmt|;
do|do
block|{
name|blocks
operator|=
name|nodeClient
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
operator|.
name|blocks
argument_list|()
operator|.
name|global
argument_list|(
name|ClusterBlockLevel
operator|.
name|METADATA_WRITE
argument_list|)
expr_stmt|;
block|}
do|while
condition|(
operator|!
name|blocks
operator|.
name|isEmpty
argument_list|()
operator|&&
operator|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|start
operator|)
operator|<
name|timeout
operator|.
name|millis
argument_list|()
condition|)
do|;
return|return
name|blocks
return|;
block|}
DECL|method|startNode
specifier|public
name|Client
name|startNode
parameter_list|(
name|Settings
operator|.
name|Builder
name|settings
parameter_list|,
name|int
name|minMasterNodes
parameter_list|)
block|{
name|String
name|name
init|=
name|internalCluster
argument_list|()
operator|.
name|startNode
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|ElectMasterService
operator|.
name|DISCOVERY_ZEN_MINIMUM_MASTER_NODES_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
name|minMasterNodes
argument_list|)
operator|.
name|put
argument_list|(
name|settings
operator|.
name|build
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
return|return
name|internalCluster
argument_list|()
operator|.
name|client
argument_list|(
name|name
argument_list|)
return|;
block|}
DECL|method|testRecoverAfterNodes
specifier|public
name|void
name|testRecoverAfterNodes
parameter_list|()
throws|throws
name|Exception
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"--> start node (1)"
argument_list|)
expr_stmt|;
name|Client
name|clientNode1
init|=
name|startNode
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"gateway.recover_after_nodes"
argument_list|,
literal|3
argument_list|)
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|clientNode1
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
operator|.
name|blocks
argument_list|()
operator|.
name|global
argument_list|(
name|ClusterBlockLevel
operator|.
name|METADATA_WRITE
argument_list|)
argument_list|,
name|hasItem
argument_list|(
name|GatewayService
operator|.
name|STATE_NOT_RECOVERED_BLOCK
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> start node (2)"
argument_list|)
expr_stmt|;
name|Client
name|clientNode2
init|=
name|startNode
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"gateway.recover_after_nodes"
argument_list|,
literal|3
argument_list|)
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
name|BLOCK_WAIT_TIMEOUT
operator|.
name|millis
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|clientNode1
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
operator|.
name|blocks
argument_list|()
operator|.
name|global
argument_list|(
name|ClusterBlockLevel
operator|.
name|METADATA_WRITE
argument_list|)
argument_list|,
name|hasItem
argument_list|(
name|GatewayService
operator|.
name|STATE_NOT_RECOVERED_BLOCK
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|clientNode2
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
operator|.
name|blocks
argument_list|()
operator|.
name|global
argument_list|(
name|ClusterBlockLevel
operator|.
name|METADATA_WRITE
argument_list|)
argument_list|,
name|hasItem
argument_list|(
name|GatewayService
operator|.
name|STATE_NOT_RECOVERED_BLOCK
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> start node (3)"
argument_list|)
expr_stmt|;
name|Client
name|clientNode3
init|=
name|startNode
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"gateway.recover_after_nodes"
argument_list|,
literal|3
argument_list|)
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|waitForNoBlocksOnNode
argument_list|(
name|BLOCK_WAIT_TIMEOUT
argument_list|,
name|clientNode1
argument_list|)
operator|.
name|isEmpty
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|waitForNoBlocksOnNode
argument_list|(
name|BLOCK_WAIT_TIMEOUT
argument_list|,
name|clientNode2
argument_list|)
operator|.
name|isEmpty
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|waitForNoBlocksOnNode
argument_list|(
name|BLOCK_WAIT_TIMEOUT
argument_list|,
name|clientNode3
argument_list|)
operator|.
name|isEmpty
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testRecoverAfterMasterNodes
specifier|public
name|void
name|testRecoverAfterMasterNodes
parameter_list|()
throws|throws
name|Exception
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"--> start master_node (1)"
argument_list|)
expr_stmt|;
name|Client
name|master1
init|=
name|startNode
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"gateway.recover_after_master_nodes"
argument_list|,
literal|2
argument_list|)
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
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|master1
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
operator|.
name|blocks
argument_list|()
operator|.
name|global
argument_list|(
name|ClusterBlockLevel
operator|.
name|METADATA_WRITE
argument_list|)
argument_list|,
name|hasItem
argument_list|(
name|GatewayService
operator|.
name|STATE_NOT_RECOVERED_BLOCK
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> start data_node (1)"
argument_list|)
expr_stmt|;
name|Client
name|data1
init|=
name|startNode
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"gateway.recover_after_master_nodes"
argument_list|,
literal|2
argument_list|)
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
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|master1
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
operator|.
name|blocks
argument_list|()
operator|.
name|global
argument_list|(
name|ClusterBlockLevel
operator|.
name|METADATA_WRITE
argument_list|)
argument_list|,
name|hasItem
argument_list|(
name|GatewayService
operator|.
name|STATE_NOT_RECOVERED_BLOCK
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|data1
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
operator|.
name|blocks
argument_list|()
operator|.
name|global
argument_list|(
name|ClusterBlockLevel
operator|.
name|METADATA_WRITE
argument_list|)
argument_list|,
name|hasItem
argument_list|(
name|GatewayService
operator|.
name|STATE_NOT_RECOVERED_BLOCK
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> start data_node (2)"
argument_list|)
expr_stmt|;
name|Client
name|data2
init|=
name|startNode
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"gateway.recover_after_master_nodes"
argument_list|,
literal|2
argument_list|)
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
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|master1
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
operator|.
name|blocks
argument_list|()
operator|.
name|global
argument_list|(
name|ClusterBlockLevel
operator|.
name|METADATA_WRITE
argument_list|)
argument_list|,
name|hasItem
argument_list|(
name|GatewayService
operator|.
name|STATE_NOT_RECOVERED_BLOCK
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|data1
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
operator|.
name|blocks
argument_list|()
operator|.
name|global
argument_list|(
name|ClusterBlockLevel
operator|.
name|METADATA_WRITE
argument_list|)
argument_list|,
name|hasItem
argument_list|(
name|GatewayService
operator|.
name|STATE_NOT_RECOVERED_BLOCK
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|data2
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
operator|.
name|blocks
argument_list|()
operator|.
name|global
argument_list|(
name|ClusterBlockLevel
operator|.
name|METADATA_WRITE
argument_list|)
argument_list|,
name|hasItem
argument_list|(
name|GatewayService
operator|.
name|STATE_NOT_RECOVERED_BLOCK
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> start master_node (2)"
argument_list|)
expr_stmt|;
name|Client
name|master2
init|=
name|startNode
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"gateway.recover_after_master_nodes"
argument_list|,
literal|2
argument_list|)
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
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|waitForNoBlocksOnNode
argument_list|(
name|BLOCK_WAIT_TIMEOUT
argument_list|,
name|master1
argument_list|)
operator|.
name|isEmpty
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|waitForNoBlocksOnNode
argument_list|(
name|BLOCK_WAIT_TIMEOUT
argument_list|,
name|master2
argument_list|)
operator|.
name|isEmpty
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|waitForNoBlocksOnNode
argument_list|(
name|BLOCK_WAIT_TIMEOUT
argument_list|,
name|data1
argument_list|)
operator|.
name|isEmpty
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|waitForNoBlocksOnNode
argument_list|(
name|BLOCK_WAIT_TIMEOUT
argument_list|,
name|data2
argument_list|)
operator|.
name|isEmpty
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testRecoverAfterDataNodes
specifier|public
name|void
name|testRecoverAfterDataNodes
parameter_list|()
throws|throws
name|Exception
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"--> start master_node (1)"
argument_list|)
expr_stmt|;
name|Client
name|master1
init|=
name|startNode
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"gateway.recover_after_data_nodes"
argument_list|,
literal|2
argument_list|)
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
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|master1
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
operator|.
name|blocks
argument_list|()
operator|.
name|global
argument_list|(
name|ClusterBlockLevel
operator|.
name|METADATA_WRITE
argument_list|)
argument_list|,
name|hasItem
argument_list|(
name|GatewayService
operator|.
name|STATE_NOT_RECOVERED_BLOCK
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> start data_node (1)"
argument_list|)
expr_stmt|;
name|Client
name|data1
init|=
name|startNode
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"gateway.recover_after_data_nodes"
argument_list|,
literal|2
argument_list|)
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
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|master1
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
operator|.
name|blocks
argument_list|()
operator|.
name|global
argument_list|(
name|ClusterBlockLevel
operator|.
name|METADATA_WRITE
argument_list|)
argument_list|,
name|hasItem
argument_list|(
name|GatewayService
operator|.
name|STATE_NOT_RECOVERED_BLOCK
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|data1
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
operator|.
name|blocks
argument_list|()
operator|.
name|global
argument_list|(
name|ClusterBlockLevel
operator|.
name|METADATA_WRITE
argument_list|)
argument_list|,
name|hasItem
argument_list|(
name|GatewayService
operator|.
name|STATE_NOT_RECOVERED_BLOCK
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> start master_node (2)"
argument_list|)
expr_stmt|;
name|Client
name|master2
init|=
name|startNode
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"gateway.recover_after_data_nodes"
argument_list|,
literal|2
argument_list|)
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
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|master2
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
operator|.
name|blocks
argument_list|()
operator|.
name|global
argument_list|(
name|ClusterBlockLevel
operator|.
name|METADATA_WRITE
argument_list|)
argument_list|,
name|hasItem
argument_list|(
name|GatewayService
operator|.
name|STATE_NOT_RECOVERED_BLOCK
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|data1
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
operator|.
name|blocks
argument_list|()
operator|.
name|global
argument_list|(
name|ClusterBlockLevel
operator|.
name|METADATA_WRITE
argument_list|)
argument_list|,
name|hasItem
argument_list|(
name|GatewayService
operator|.
name|STATE_NOT_RECOVERED_BLOCK
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|master2
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
operator|.
name|blocks
argument_list|()
operator|.
name|global
argument_list|(
name|ClusterBlockLevel
operator|.
name|METADATA_WRITE
argument_list|)
argument_list|,
name|hasItem
argument_list|(
name|GatewayService
operator|.
name|STATE_NOT_RECOVERED_BLOCK
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> start data_node (2)"
argument_list|)
expr_stmt|;
name|Client
name|data2
init|=
name|startNode
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"gateway.recover_after_data_nodes"
argument_list|,
literal|2
argument_list|)
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
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|waitForNoBlocksOnNode
argument_list|(
name|BLOCK_WAIT_TIMEOUT
argument_list|,
name|master1
argument_list|)
operator|.
name|isEmpty
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|waitForNoBlocksOnNode
argument_list|(
name|BLOCK_WAIT_TIMEOUT
argument_list|,
name|master2
argument_list|)
operator|.
name|isEmpty
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|waitForNoBlocksOnNode
argument_list|(
name|BLOCK_WAIT_TIMEOUT
argument_list|,
name|data1
argument_list|)
operator|.
name|isEmpty
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|waitForNoBlocksOnNode
argument_list|(
name|BLOCK_WAIT_TIMEOUT
argument_list|,
name|data2
argument_list|)
operator|.
name|isEmpty
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

