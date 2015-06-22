begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.test.cluster
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|test
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
name|Version
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
name|*
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
name|node
operator|.
name|DiscoveryNode
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
name|node
operator|.
name|DiscoveryNodes
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
name|routing
operator|.
name|OperationRouting
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
name|service
operator|.
name|PendingClusterTask
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
name|Lifecycle
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
name|LifecycleListener
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
name|transport
operator|.
name|DummyTransportAddress
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
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_class
DECL|class|NoopClusterService
specifier|public
class|class
name|NoopClusterService
implements|implements
name|ClusterService
block|{
DECL|field|state
specifier|final
name|ClusterState
name|state
decl_stmt|;
DECL|method|NoopClusterService
specifier|public
name|NoopClusterService
parameter_list|()
block|{
name|this
argument_list|(
name|ClusterState
operator|.
name|builder
argument_list|(
operator|new
name|ClusterName
argument_list|(
literal|"noop"
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|NoopClusterService
specifier|public
name|NoopClusterService
parameter_list|(
name|ClusterState
name|state
parameter_list|)
block|{
if|if
condition|(
name|state
operator|.
name|getNodes
argument_list|()
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
name|state
operator|=
name|ClusterState
operator|.
name|builder
argument_list|(
name|state
argument_list|)
operator|.
name|nodes
argument_list|(
name|DiscoveryNodes
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
operator|new
name|DiscoveryNode
argument_list|(
literal|"noop_id"
argument_list|,
name|DummyTransportAddress
operator|.
name|INSTANCE
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
argument_list|)
operator|.
name|localNodeId
argument_list|(
literal|"noop_id"
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
block|}
assert|assert
name|state
operator|.
name|getNodes
argument_list|()
operator|.
name|localNode
argument_list|()
operator|!=
literal|null
assert|;
name|this
operator|.
name|state
operator|=
name|state
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|localNode
specifier|public
name|DiscoveryNode
name|localNode
parameter_list|()
block|{
return|return
name|state
operator|.
name|getNodes
argument_list|()
operator|.
name|localNode
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|state
specifier|public
name|ClusterState
name|state
parameter_list|()
block|{
return|return
name|state
return|;
block|}
annotation|@
name|Override
DECL|method|addInitialStateBlock
specifier|public
name|void
name|addInitialStateBlock
parameter_list|(
name|ClusterBlock
name|block
parameter_list|)
throws|throws
name|IllegalStateException
block|{      }
annotation|@
name|Override
DECL|method|removeInitialStateBlock
specifier|public
name|void
name|removeInitialStateBlock
parameter_list|(
name|ClusterBlock
name|block
parameter_list|)
throws|throws
name|IllegalStateException
block|{      }
annotation|@
name|Override
DECL|method|operationRouting
specifier|public
name|OperationRouting
name|operationRouting
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|addFirst
specifier|public
name|void
name|addFirst
parameter_list|(
name|ClusterStateListener
name|listener
parameter_list|)
block|{      }
annotation|@
name|Override
DECL|method|addLast
specifier|public
name|void
name|addLast
parameter_list|(
name|ClusterStateListener
name|listener
parameter_list|)
block|{      }
annotation|@
name|Override
DECL|method|add
specifier|public
name|void
name|add
parameter_list|(
name|ClusterStateListener
name|listener
parameter_list|)
block|{      }
annotation|@
name|Override
DECL|method|remove
specifier|public
name|void
name|remove
parameter_list|(
name|ClusterStateListener
name|listener
parameter_list|)
block|{      }
annotation|@
name|Override
DECL|method|add
specifier|public
name|void
name|add
parameter_list|(
name|LocalNodeMasterListener
name|listener
parameter_list|)
block|{      }
annotation|@
name|Override
DECL|method|remove
specifier|public
name|void
name|remove
parameter_list|(
name|LocalNodeMasterListener
name|listener
parameter_list|)
block|{      }
annotation|@
name|Override
DECL|method|add
specifier|public
name|void
name|add
parameter_list|(
name|TimeValue
name|timeout
parameter_list|,
name|TimeoutClusterStateListener
name|listener
parameter_list|)
block|{      }
annotation|@
name|Override
DECL|method|submitStateUpdateTask
specifier|public
name|void
name|submitStateUpdateTask
parameter_list|(
name|String
name|source
parameter_list|,
name|Priority
name|priority
parameter_list|,
name|ClusterStateUpdateTask
name|updateTask
parameter_list|)
block|{      }
annotation|@
name|Override
DECL|method|submitStateUpdateTask
specifier|public
name|void
name|submitStateUpdateTask
parameter_list|(
name|String
name|source
parameter_list|,
name|ClusterStateUpdateTask
name|updateTask
parameter_list|)
block|{      }
annotation|@
name|Override
DECL|method|pendingTasks
specifier|public
name|List
argument_list|<
name|PendingClusterTask
argument_list|>
name|pendingTasks
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|numberOfPendingTasks
specifier|public
name|int
name|numberOfPendingTasks
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
DECL|method|getMaxTaskWaitTime
specifier|public
name|TimeValue
name|getMaxTaskWaitTime
parameter_list|()
block|{
return|return
name|TimeValue
operator|.
name|timeValueMillis
argument_list|(
literal|0
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|lifecycleState
specifier|public
name|Lifecycle
operator|.
name|State
name|lifecycleState
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|addLifecycleListener
specifier|public
name|void
name|addLifecycleListener
parameter_list|(
name|LifecycleListener
name|listener
parameter_list|)
block|{      }
annotation|@
name|Override
DECL|method|removeLifecycleListener
specifier|public
name|void
name|removeLifecycleListener
parameter_list|(
name|LifecycleListener
name|listener
parameter_list|)
block|{      }
annotation|@
name|Override
DECL|method|start
specifier|public
name|ClusterService
name|start
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|stop
specifier|public
name|ClusterService
name|stop
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
block|{      }
block|}
end_class

end_unit

