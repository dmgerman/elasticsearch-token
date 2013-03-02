begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|cluster
operator|.
name|ClusterService
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
name|LocalNodeMasterListener
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
name|common
operator|.
name|component
operator|.
name|AbstractLifecycleComponent
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
name|component
operator|.
name|LifecycleComponent
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
name|inject
operator|.
name|Inject
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
name|inject
operator|.
name|Singleton
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
name|node
operator|.
name|internal
operator|.
name|InternalNode
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|plugins
operator|.
name|AbstractPlugin
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|threadpool
operator|.
name|ThreadPool
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
name|AfterMethod
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
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
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
DECL|class|LocalNodeMasterListenerTests
specifier|public
class|class
name|LocalNodeMasterListenerTests
extends|extends
name|AbstractZenNodesTests
block|{
annotation|@
name|AfterMethod
DECL|method|closeNodes
specifier|public
name|void
name|closeNodes
parameter_list|()
block|{
name|closeAllNodes
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testListenerCallbacks
specifier|public
name|void
name|testListenerCallbacks
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
literal|"discovery.zen.minimum_master_nodes"
argument_list|,
literal|1
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
literal|"plugin.types"
argument_list|,
name|TestPlugin
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|InternalNode
name|node1
init|=
operator|(
name|InternalNode
operator|)
name|startNode
argument_list|(
literal|"node1"
argument_list|,
name|settings
argument_list|)
decl_stmt|;
name|ClusterService
name|clusterService1
init|=
name|node1
operator|.
name|injector
argument_list|()
operator|.
name|getInstance
argument_list|(
name|ClusterService
operator|.
name|class
argument_list|)
decl_stmt|;
name|MasterAwareService
name|testService1
init|=
name|node1
operator|.
name|injector
argument_list|()
operator|.
name|getInstance
argument_list|(
name|MasterAwareService
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// the first node should be a master as the minimum required is 1
name|assertThat
argument_list|(
name|clusterService1
operator|.
name|state
argument_list|()
operator|.
name|nodes
argument_list|()
operator|.
name|masterNode
argument_list|()
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|clusterService1
operator|.
name|state
argument_list|()
operator|.
name|nodes
argument_list|()
operator|.
name|localNodeMaster
argument_list|()
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|testService1
operator|.
name|master
argument_list|()
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|InternalNode
name|node2
init|=
operator|(
name|InternalNode
operator|)
name|startNode
argument_list|(
literal|"node2"
argument_list|,
name|settings
argument_list|)
decl_stmt|;
name|ClusterService
name|clusterService2
init|=
name|node2
operator|.
name|injector
argument_list|()
operator|.
name|getInstance
argument_list|(
name|ClusterService
operator|.
name|class
argument_list|)
decl_stmt|;
name|MasterAwareService
name|testService2
init|=
name|node2
operator|.
name|injector
argument_list|()
operator|.
name|getInstance
argument_list|(
name|MasterAwareService
operator|.
name|class
argument_list|)
decl_stmt|;
name|ClusterHealthResponse
name|clusterHealth
init|=
name|node2
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
name|prepareHealth
argument_list|()
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
literal|"2"
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
name|clusterHealth
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
comment|// the second node should not be the master as node1 is already the master.
name|assertThat
argument_list|(
name|clusterService2
operator|.
name|state
argument_list|()
operator|.
name|nodes
argument_list|()
operator|.
name|localNodeMaster
argument_list|()
argument_list|,
name|is
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|testService2
operator|.
name|master
argument_list|()
argument_list|,
name|is
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|node1
operator|.
name|close
argument_list|()
expr_stmt|;
name|clusterHealth
operator|=
name|node2
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
name|prepareHealth
argument_list|()
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
literal|"1"
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
name|clusterHealth
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
comment|// now that node1 is closed, node2 should be elected as master
name|assertThat
argument_list|(
name|clusterService2
operator|.
name|state
argument_list|()
operator|.
name|nodes
argument_list|()
operator|.
name|localNodeMaster
argument_list|()
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|testService2
operator|.
name|master
argument_list|()
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|Settings
name|newSettings
init|=
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"discovery.zen.minimum_master_nodes"
argument_list|,
literal|2
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|node2
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
name|prepareUpdateSettings
argument_list|()
operator|.
name|setTransientSettings
argument_list|(
name|newSettings
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|200
argument_list|)
expr_stmt|;
comment|// there should not be any master as the minimum number of required eligible masters is not met
name|assertThat
argument_list|(
name|clusterService2
operator|.
name|state
argument_list|()
operator|.
name|nodes
argument_list|()
operator|.
name|masterNode
argument_list|()
argument_list|,
name|is
argument_list|(
name|nullValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|testService2
operator|.
name|master
argument_list|()
argument_list|,
name|is
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|node1
operator|=
operator|(
name|InternalNode
operator|)
name|startNode
argument_list|(
literal|"node1"
argument_list|,
name|settings
argument_list|)
expr_stmt|;
name|clusterService1
operator|=
name|node1
operator|.
name|injector
argument_list|()
operator|.
name|getInstance
argument_list|(
name|ClusterService
operator|.
name|class
argument_list|)
expr_stmt|;
name|testService1
operator|=
name|node1
operator|.
name|injector
argument_list|()
operator|.
name|getInstance
argument_list|(
name|MasterAwareService
operator|.
name|class
argument_list|)
expr_stmt|;
name|clusterHealth
operator|=
name|node2
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
name|prepareHealth
argument_list|()
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
name|clusterHealth
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
comment|// now that we started node1 again, a new master should be elected
name|assertThat
argument_list|(
name|clusterService1
operator|.
name|state
argument_list|()
operator|.
name|nodes
argument_list|()
operator|.
name|masterNode
argument_list|()
argument_list|,
name|is
argument_list|(
name|notNullValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
literal|"node1"
operator|.
name|equals
argument_list|(
name|clusterService1
operator|.
name|state
argument_list|()
operator|.
name|nodes
argument_list|()
operator|.
name|masterNode
argument_list|()
operator|.
name|name
argument_list|()
argument_list|)
condition|)
block|{
name|assertThat
argument_list|(
name|testService1
operator|.
name|master
argument_list|()
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|testService2
operator|.
name|master
argument_list|()
argument_list|,
name|is
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertThat
argument_list|(
name|testService1
operator|.
name|master
argument_list|()
argument_list|,
name|is
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|testService2
operator|.
name|master
argument_list|()
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|TestPlugin
specifier|public
specifier|static
class|class
name|TestPlugin
extends|extends
name|AbstractPlugin
block|{
annotation|@
name|Override
DECL|method|name
specifier|public
name|String
name|name
parameter_list|()
block|{
return|return
literal|"test plugin"
return|;
block|}
annotation|@
name|Override
DECL|method|description
specifier|public
name|String
name|description
parameter_list|()
block|{
return|return
literal|"test plugin"
return|;
block|}
annotation|@
name|Override
DECL|method|services
specifier|public
name|Collection
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|LifecycleComponent
argument_list|>
argument_list|>
name|services
parameter_list|()
block|{
name|List
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|LifecycleComponent
argument_list|>
argument_list|>
name|services
init|=
operator|new
name|ArrayList
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|LifecycleComponent
argument_list|>
argument_list|>
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|services
operator|.
name|add
argument_list|(
name|MasterAwareService
operator|.
name|class
argument_list|)
expr_stmt|;
return|return
name|services
return|;
block|}
block|}
annotation|@
name|Singleton
DECL|class|MasterAwareService
specifier|public
specifier|static
class|class
name|MasterAwareService
extends|extends
name|AbstractLifecycleComponent
argument_list|<
name|MasterAwareService
argument_list|>
implements|implements
name|LocalNodeMasterListener
block|{
DECL|field|clusterService
specifier|private
specifier|final
name|ClusterService
name|clusterService
decl_stmt|;
DECL|field|master
specifier|private
specifier|volatile
name|boolean
name|master
decl_stmt|;
annotation|@
name|Inject
DECL|method|MasterAwareService
specifier|public
name|MasterAwareService
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ClusterService
name|clusterService
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|clusterService
operator|.
name|add
argument_list|(
name|this
argument_list|)
expr_stmt|;
name|this
operator|.
name|clusterService
operator|=
name|clusterService
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"initialized test service"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|onMaster
specifier|public
name|void
name|onMaster
parameter_list|()
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"on master ["
operator|+
name|clusterService
operator|.
name|state
argument_list|()
operator|.
name|nodes
argument_list|()
operator|.
name|localNode
argument_list|()
operator|+
literal|"]"
argument_list|)
expr_stmt|;
name|master
operator|=
literal|true
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|offMaster
specifier|public
name|void
name|offMaster
parameter_list|()
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"off master ["
operator|+
name|clusterService
operator|.
name|state
argument_list|()
operator|.
name|nodes
argument_list|()
operator|.
name|localNode
argument_list|()
operator|+
literal|"]"
argument_list|)
expr_stmt|;
name|master
operator|=
literal|false
expr_stmt|;
block|}
DECL|method|master
specifier|public
name|boolean
name|master
parameter_list|()
block|{
return|return
name|master
return|;
block|}
annotation|@
name|Override
DECL|method|doStart
specifier|protected
name|void
name|doStart
parameter_list|()
throws|throws
name|ElasticSearchException
block|{         }
annotation|@
name|Override
DECL|method|doStop
specifier|protected
name|void
name|doStop
parameter_list|()
throws|throws
name|ElasticSearchException
block|{         }
annotation|@
name|Override
DECL|method|doClose
specifier|protected
name|void
name|doClose
parameter_list|()
throws|throws
name|ElasticSearchException
block|{         }
annotation|@
name|Override
DECL|method|executorName
specifier|public
name|String
name|executorName
parameter_list|()
block|{
return|return
name|ThreadPool
operator|.
name|Names
operator|.
name|SAME
return|;
block|}
block|}
block|}
end_class

end_unit

