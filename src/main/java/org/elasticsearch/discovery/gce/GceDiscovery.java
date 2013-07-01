begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.discovery.gce
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|discovery
operator|.
name|gce
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cloud
operator|.
name|gce
operator|.
name|GceComputeService
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
name|ClusterName
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
name|node
operator|.
name|DiscoveryNodeService
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
name|collect
operator|.
name|ImmutableList
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
name|network
operator|.
name|NetworkService
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
name|zen
operator|.
name|ZenDiscovery
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
name|ping
operator|.
name|ZenPing
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
name|ping
operator|.
name|ZenPingService
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
name|ping
operator|.
name|unicast
operator|.
name|UnicastZenPing
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
name|settings
operator|.
name|NodeSettingsService
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
name|elasticsearch
operator|.
name|transport
operator|.
name|TransportService
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|GceDiscovery
specifier|public
class|class
name|GceDiscovery
extends|extends
name|ZenDiscovery
block|{
annotation|@
name|Inject
DECL|method|GceDiscovery
specifier|public
name|GceDiscovery
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ClusterName
name|clusterName
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|,
name|TransportService
name|transportService
parameter_list|,
name|ClusterService
name|clusterService
parameter_list|,
name|NodeSettingsService
name|nodeSettingsService
parameter_list|,
name|ZenPingService
name|pingService
parameter_list|,
name|DiscoveryNodeService
name|discoveryNodeService
parameter_list|,
name|GceComputeService
name|gceComputeService
parameter_list|,
name|NetworkService
name|networkService
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|clusterName
argument_list|,
name|threadPool
argument_list|,
name|transportService
argument_list|,
name|clusterService
argument_list|,
name|nodeSettingsService
argument_list|,
name|discoveryNodeService
argument_list|,
name|pingService
argument_list|)
expr_stmt|;
if|if
condition|(
name|settings
operator|.
name|getAsBoolean
argument_list|(
literal|"cloud.enabled"
argument_list|,
literal|true
argument_list|)
condition|)
block|{
name|ImmutableList
argument_list|<
name|?
extends|extends
name|ZenPing
argument_list|>
name|zenPings
init|=
name|pingService
operator|.
name|zenPings
argument_list|()
decl_stmt|;
name|UnicastZenPing
name|unicastZenPing
init|=
literal|null
decl_stmt|;
for|for
control|(
name|ZenPing
name|zenPing
range|:
name|zenPings
control|)
block|{
if|if
condition|(
name|zenPing
operator|instanceof
name|UnicastZenPing
condition|)
block|{
name|unicastZenPing
operator|=
operator|(
name|UnicastZenPing
operator|)
name|zenPing
expr_stmt|;
break|break;
block|}
block|}
if|if
condition|(
name|unicastZenPing
operator|!=
literal|null
condition|)
block|{
comment|// update the unicast zen ping to add cloud hosts provider
comment|// and, while we are at it, use only it and not the multicast for example
name|unicastZenPing
operator|.
name|addHostsProvider
argument_list|(
operator|new
name|GceUnicastHostsProvider
argument_list|(
name|settings
argument_list|,
name|gceComputeService
argument_list|,
name|transportService
argument_list|,
name|networkService
argument_list|)
argument_list|)
expr_stmt|;
name|pingService
operator|.
name|zenPings
argument_list|(
name|ImmutableList
operator|.
name|of
argument_list|(
name|unicastZenPing
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"failed to apply gce unicast discovery, no unicast ping found"
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

