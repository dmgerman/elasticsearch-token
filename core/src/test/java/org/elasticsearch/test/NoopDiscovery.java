begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.test
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|test
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
name|ClusterChangedEvent
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
name|routing
operator|.
name|RoutingService
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
name|discovery
operator|.
name|DiscoverySettings
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
name|DiscoveryStats
import|;
end_import

begin_class
DECL|class|NoopDiscovery
specifier|public
class|class
name|NoopDiscovery
implements|implements
name|Discovery
block|{
annotation|@
name|Override
DECL|method|localNode
specifier|public
name|DiscoveryNode
name|localNode
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|nodeDescription
specifier|public
name|String
name|nodeDescription
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|setRoutingService
specifier|public
name|void
name|setRoutingService
parameter_list|(
name|RoutingService
name|routingService
parameter_list|)
block|{      }
annotation|@
name|Override
DECL|method|publish
specifier|public
name|void
name|publish
parameter_list|(
name|ClusterChangedEvent
name|clusterChangedEvent
parameter_list|,
name|AckListener
name|ackListener
parameter_list|)
block|{      }
annotation|@
name|Override
DECL|method|stats
specifier|public
name|DiscoveryStats
name|stats
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|getDiscoverySettings
specifier|public
name|DiscoverySettings
name|getDiscoverySettings
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|startInitialJoin
specifier|public
name|void
name|startInitialJoin
parameter_list|()
block|{      }
annotation|@
name|Override
DECL|method|getMinimumMasterNodes
specifier|public
name|int
name|getMinimumMasterNodes
parameter_list|()
block|{
return|return
operator|-
literal|1
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
name|void
name|start
parameter_list|()
block|{}
annotation|@
name|Override
DECL|method|stop
specifier|public
name|void
name|stop
parameter_list|()
block|{}
annotation|@
name|Override
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
block|{}
block|}
end_class

end_unit

