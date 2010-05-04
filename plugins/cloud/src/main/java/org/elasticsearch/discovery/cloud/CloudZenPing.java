begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.discovery.cloud
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|discovery
operator|.
name|cloud
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
name|compute
operator|.
name|CloudComputeService
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

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
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
name|util
operator|.
name|transport
operator|.
name|InetSocketTransportAddress
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|transport
operator|.
name|PortsRange
import|;
end_import

begin_import
import|import
name|org
operator|.
name|jclouds
operator|.
name|compute
operator|.
name|ComputeService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|jclouds
operator|.
name|compute
operator|.
name|domain
operator|.
name|ComputeMetadata
import|;
end_import

begin_import
import|import
name|org
operator|.
name|jclouds
operator|.
name|compute
operator|.
name|domain
operator|.
name|NodeMetadata
import|;
end_import

begin_import
import|import
name|org
operator|.
name|jclouds
operator|.
name|compute
operator|.
name|domain
operator|.
name|NodeState
import|;
end_import

begin_import
import|import
name|org
operator|.
name|jclouds
operator|.
name|compute
operator|.
name|options
operator|.
name|GetNodesOptions
import|;
end_import

begin_import
import|import
name|org
operator|.
name|jclouds
operator|.
name|domain
operator|.
name|Location
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|InetAddress
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|InetSocketAddress
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
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|gcommon
operator|.
name|collect
operator|.
name|Lists
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|CloudZenPing
specifier|public
class|class
name|CloudZenPing
extends|extends
name|UnicastZenPing
block|{
DECL|field|computeService
specifier|private
specifier|final
name|ComputeService
name|computeService
decl_stmt|;
DECL|field|ports
specifier|private
specifier|final
name|String
name|ports
decl_stmt|;
DECL|field|tag
specifier|private
specifier|final
name|String
name|tag
decl_stmt|;
DECL|field|location
specifier|private
specifier|final
name|String
name|location
decl_stmt|;
DECL|method|CloudZenPing
specifier|public
name|CloudZenPing
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|,
name|TransportService
name|transportService
parameter_list|,
name|ClusterName
name|clusterName
parameter_list|,
name|CloudComputeService
name|computeService
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|threadPool
argument_list|,
name|transportService
argument_list|,
name|clusterName
argument_list|)
expr_stmt|;
name|this
operator|.
name|computeService
operator|=
name|computeService
operator|.
name|context
argument_list|()
operator|.
name|getComputeService
argument_list|()
expr_stmt|;
name|this
operator|.
name|tag
operator|=
name|componentSettings
operator|.
name|get
argument_list|(
literal|"tag"
argument_list|)
expr_stmt|;
name|this
operator|.
name|location
operator|=
name|componentSettings
operator|.
name|get
argument_list|(
literal|"location"
argument_list|)
expr_stmt|;
name|this
operator|.
name|ports
operator|=
name|componentSettings
operator|.
name|get
argument_list|(
literal|"ports"
argument_list|,
literal|"9300-9302"
argument_list|)
expr_stmt|;
comment|// parse the ports just to see that they are valid
operator|new
name|PortsRange
argument_list|(
name|ports
argument_list|)
operator|.
name|ports
argument_list|()
expr_stmt|;
block|}
DECL|method|buildDynamicNodes
annotation|@
name|Override
specifier|protected
name|List
argument_list|<
name|DiscoveryNode
argument_list|>
name|buildDynamicNodes
parameter_list|()
block|{
name|List
argument_list|<
name|DiscoveryNode
argument_list|>
name|discoNodes
init|=
name|newArrayList
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|?
extends|extends
name|ComputeMetadata
argument_list|>
name|nodes
init|=
name|computeService
operator|.
name|getNodes
argument_list|(
name|GetNodesOptions
operator|.
name|Builder
operator|.
name|withDetails
argument_list|()
argument_list|)
decl_stmt|;
name|logger
operator|.
name|trace
argument_list|(
literal|"Processing Nodes {}"
argument_list|,
name|nodes
argument_list|)
expr_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|?
extends|extends
name|ComputeMetadata
argument_list|>
name|node
range|:
name|nodes
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|NodeMetadata
name|nodeMetadata
init|=
operator|(
name|NodeMetadata
operator|)
name|node
operator|.
name|getValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|tag
operator|!=
literal|null
operator|&&
operator|!
name|nodeMetadata
operator|.
name|getTag
argument_list|()
operator|.
name|equals
argument_list|(
name|tag
argument_list|)
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"Filtering node {} with unmatched tag {}"
argument_list|,
name|nodeMetadata
operator|.
name|getName
argument_list|()
argument_list|,
name|nodeMetadata
operator|.
name|getTag
argument_list|()
argument_list|)
expr_stmt|;
continue|continue;
block|}
name|boolean
name|filteredByLocation
init|=
literal|true
decl_stmt|;
if|if
condition|(
name|location
operator|!=
literal|null
condition|)
block|{
name|Location
name|nodeLocation
init|=
name|nodeMetadata
operator|.
name|getLocation
argument_list|()
decl_stmt|;
if|if
condition|(
name|location
operator|.
name|equals
argument_list|(
name|nodeLocation
operator|.
name|getId
argument_list|()
argument_list|)
condition|)
block|{
name|filteredByLocation
operator|=
literal|false
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|nodeLocation
operator|.
name|getParent
argument_list|()
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|location
operator|.
name|equals
argument_list|(
name|nodeLocation
operator|.
name|getParent
argument_list|()
operator|.
name|getId
argument_list|()
argument_list|)
condition|)
block|{
name|filteredByLocation
operator|=
literal|false
expr_stmt|;
block|}
block|}
block|}
block|}
else|else
block|{
name|filteredByLocation
operator|=
literal|false
expr_stmt|;
block|}
if|if
condition|(
name|filteredByLocation
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"Filtering node {} with unmatched location {}"
argument_list|,
name|nodeMetadata
operator|.
name|getName
argument_list|()
argument_list|,
name|nodeMetadata
operator|.
name|getLocation
argument_list|()
argument_list|)
expr_stmt|;
continue|continue;
block|}
if|if
condition|(
name|nodeMetadata
operator|.
name|getState
argument_list|()
operator|==
name|NodeState
operator|.
name|PENDING
operator|||
name|nodeMetadata
operator|.
name|getState
argument_list|()
operator|==
name|NodeState
operator|.
name|RUNNING
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"Adding {}/{}"
argument_list|,
name|nodeMetadata
operator|.
name|getName
argument_list|()
argument_list|,
name|nodeMetadata
operator|.
name|getPrivateAddresses
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|InetAddress
name|inetAddress
range|:
name|nodeMetadata
operator|.
name|getPrivateAddresses
argument_list|()
control|)
block|{
for|for
control|(
name|int
name|port
range|:
operator|new
name|PortsRange
argument_list|(
name|ports
argument_list|)
operator|.
name|ports
argument_list|()
control|)
block|{
name|discoNodes
operator|.
name|add
argument_list|(
operator|new
name|DiscoveryNode
argument_list|(
literal|"#cloud-"
operator|+
name|inetAddress
operator|.
name|getHostAddress
argument_list|()
operator|+
literal|"-"
operator|+
name|port
argument_list|,
operator|new
name|InetSocketTransportAddress
argument_list|(
operator|new
name|InetSocketAddress
argument_list|(
name|inetAddress
argument_list|,
name|port
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
return|return
name|discoNodes
return|;
block|}
block|}
end_class

end_unit

