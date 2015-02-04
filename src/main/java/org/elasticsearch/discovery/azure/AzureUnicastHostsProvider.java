begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.discovery.azure
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|discovery
operator|.
name|azure
package|;
end_package

begin_import
import|import
name|com
operator|.
name|microsoft
operator|.
name|windowsazure
operator|.
name|management
operator|.
name|compute
operator|.
name|models
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
name|Version
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cloud
operator|.
name|azure
operator|.
name|AzureServiceDisableException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cloud
operator|.
name|azure
operator|.
name|AzureServiceRemoteException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cloud
operator|.
name|azure
operator|.
name|management
operator|.
name|AzureComputeService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cloud
operator|.
name|azure
operator|.
name|management
operator|.
name|AzureComputeService
operator|.
name|Fields
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
name|common
operator|.
name|collect
operator|.
name|Lists
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
name|AbstractComponent
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
name|common
operator|.
name|transport
operator|.
name|TransportAddress
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
name|ping
operator|.
name|unicast
operator|.
name|UnicastHostsProvider
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
name|java
operator|.
name|io
operator|.
name|IOException
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
name|util
operator|.
name|List
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|AzureUnicastHostsProvider
specifier|public
class|class
name|AzureUnicastHostsProvider
extends|extends
name|AbstractComponent
implements|implements
name|UnicastHostsProvider
block|{
DECL|enum|HostType
specifier|public
specifier|static
enum|enum
name|HostType
block|{
DECL|enum constant|PRIVATE_IP
name|PRIVATE_IP
argument_list|(
literal|"private_ip"
argument_list|)
block|,
DECL|enum constant|PUBLIC_IP
name|PUBLIC_IP
argument_list|(
literal|"public_ip"
argument_list|)
block|;
DECL|field|type
specifier|private
name|String
name|type
decl_stmt|;
DECL|method|HostType
specifier|private
name|HostType
parameter_list|(
name|String
name|type
parameter_list|)
block|{
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
block|}
DECL|method|fromString
specifier|public
specifier|static
name|HostType
name|fromString
parameter_list|(
name|String
name|type
parameter_list|)
block|{
for|for
control|(
name|HostType
name|hostType
range|:
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|hostType
operator|.
name|type
operator|.
name|equalsIgnoreCase
argument_list|(
name|type
argument_list|)
condition|)
block|{
return|return
name|hostType
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
block|}
DECL|enum|Deployment
specifier|public
specifier|static
enum|enum
name|Deployment
block|{
DECL|enum constant|PRODUCTION
name|PRODUCTION
argument_list|(
literal|"production"
argument_list|,
name|DeploymentSlot
operator|.
name|Production
argument_list|)
block|,
DECL|enum constant|STAGING
name|STAGING
argument_list|(
literal|"staging"
argument_list|,
name|DeploymentSlot
operator|.
name|Staging
argument_list|)
block|;
DECL|field|deployment
specifier|private
name|String
name|deployment
decl_stmt|;
DECL|field|slot
specifier|private
name|DeploymentSlot
name|slot
decl_stmt|;
DECL|method|Deployment
specifier|private
name|Deployment
parameter_list|(
name|String
name|deployment
parameter_list|,
name|DeploymentSlot
name|slot
parameter_list|)
block|{
name|this
operator|.
name|deployment
operator|=
name|deployment
expr_stmt|;
name|this
operator|.
name|slot
operator|=
name|slot
expr_stmt|;
block|}
DECL|method|fromString
specifier|public
specifier|static
name|Deployment
name|fromString
parameter_list|(
name|String
name|string
parameter_list|)
block|{
for|for
control|(
name|Deployment
name|deployment
range|:
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|deployment
operator|.
name|deployment
operator|.
name|equalsIgnoreCase
argument_list|(
name|string
argument_list|)
condition|)
block|{
return|return
name|deployment
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
block|}
DECL|field|azureComputeService
specifier|private
specifier|final
name|AzureComputeService
name|azureComputeService
decl_stmt|;
DECL|field|transportService
specifier|private
name|TransportService
name|transportService
decl_stmt|;
DECL|field|networkService
specifier|private
name|NetworkService
name|networkService
decl_stmt|;
DECL|field|version
specifier|private
specifier|final
name|Version
name|version
decl_stmt|;
DECL|field|refreshInterval
specifier|private
specifier|final
name|TimeValue
name|refreshInterval
decl_stmt|;
DECL|field|lastRefresh
specifier|private
name|long
name|lastRefresh
decl_stmt|;
DECL|field|cachedDiscoNodes
specifier|private
name|List
argument_list|<
name|DiscoveryNode
argument_list|>
name|cachedDiscoNodes
decl_stmt|;
DECL|field|hostType
specifier|private
specifier|final
name|HostType
name|hostType
decl_stmt|;
DECL|field|publicEndpointName
specifier|private
specifier|final
name|String
name|publicEndpointName
decl_stmt|;
DECL|field|deploymentName
specifier|private
specifier|final
name|String
name|deploymentName
decl_stmt|;
DECL|field|deploymentSlot
specifier|private
specifier|final
name|DeploymentSlot
name|deploymentSlot
decl_stmt|;
annotation|@
name|Inject
DECL|method|AzureUnicastHostsProvider
specifier|public
name|AzureUnicastHostsProvider
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|AzureComputeService
name|azureComputeService
parameter_list|,
name|TransportService
name|transportService
parameter_list|,
name|NetworkService
name|networkService
parameter_list|,
name|Version
name|version
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|azureComputeService
operator|=
name|azureComputeService
expr_stmt|;
name|this
operator|.
name|transportService
operator|=
name|transportService
expr_stmt|;
name|this
operator|.
name|networkService
operator|=
name|networkService
expr_stmt|;
name|this
operator|.
name|version
operator|=
name|version
expr_stmt|;
name|this
operator|.
name|refreshInterval
operator|=
name|componentSettings
operator|.
name|getAsTime
argument_list|(
name|Fields
operator|.
name|REFRESH
argument_list|,
name|settings
operator|.
name|getAsTime
argument_list|(
literal|"cloud.azure."
operator|+
name|Fields
operator|.
name|REFRESH
argument_list|,
name|TimeValue
operator|.
name|timeValueSeconds
argument_list|(
literal|0
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|strHostType
init|=
name|componentSettings
operator|.
name|get
argument_list|(
name|Fields
operator|.
name|HOST_TYPE
argument_list|,
name|settings
operator|.
name|get
argument_list|(
literal|"cloud.azure."
operator|+
name|Fields
operator|.
name|HOST_TYPE_DEPRECATED
argument_list|,
name|HostType
operator|.
name|PRIVATE_IP
operator|.
name|name
argument_list|()
argument_list|)
argument_list|)
operator|.
name|toUpperCase
argument_list|()
decl_stmt|;
name|HostType
name|tmpHostType
init|=
name|HostType
operator|.
name|fromString
argument_list|(
name|strHostType
argument_list|)
decl_stmt|;
if|if
condition|(
name|tmpHostType
operator|==
literal|null
condition|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"wrong value for [{}]: [{}]. falling back to [{}]..."
argument_list|,
name|Fields
operator|.
name|HOST_TYPE
argument_list|,
name|strHostType
argument_list|,
name|HostType
operator|.
name|PRIVATE_IP
operator|.
name|name
argument_list|()
operator|.
name|toLowerCase
argument_list|()
argument_list|)
expr_stmt|;
name|tmpHostType
operator|=
name|HostType
operator|.
name|PRIVATE_IP
expr_stmt|;
block|}
name|this
operator|.
name|hostType
operator|=
name|tmpHostType
expr_stmt|;
comment|// TODO Remove in 3.0.0
name|String
name|portName
init|=
name|settings
operator|.
name|get
argument_list|(
literal|"cloud.azure."
operator|+
name|Fields
operator|.
name|PORT_NAME_DEPRECATED
argument_list|)
decl_stmt|;
if|if
condition|(
name|portName
operator|!=
literal|null
condition|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"setting [{}] has been deprecated. please replace with [{}]."
argument_list|,
name|Fields
operator|.
name|PORT_NAME_DEPRECATED
argument_list|,
name|Fields
operator|.
name|ENDPOINT_NAME
argument_list|)
expr_stmt|;
name|this
operator|.
name|publicEndpointName
operator|=
name|portName
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|publicEndpointName
operator|=
name|componentSettings
operator|.
name|get
argument_list|(
name|Fields
operator|.
name|ENDPOINT_NAME
argument_list|,
literal|"elasticsearch"
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|deploymentName
operator|=
name|componentSettings
operator|.
name|get
argument_list|(
name|Fields
operator|.
name|DEPLOYMENT_NAME
argument_list|,
name|settings
operator|.
name|get
argument_list|(
literal|"cloud.azure.management."
operator|+
name|Fields
operator|.
name|SERVICE_NAME
argument_list|,
name|settings
operator|.
name|get
argument_list|(
literal|"cloud.azure."
operator|+
name|Fields
operator|.
name|SERVICE_NAME_DEPRECATED
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// Reading deployment_slot
name|String
name|strDeployment
init|=
name|componentSettings
operator|.
name|get
argument_list|(
name|Fields
operator|.
name|DEPLOYMENT_SLOT
argument_list|,
name|Deployment
operator|.
name|PRODUCTION
operator|.
name|deployment
argument_list|)
decl_stmt|;
name|Deployment
name|tmpDeployment
init|=
name|Deployment
operator|.
name|fromString
argument_list|(
name|strDeployment
argument_list|)
decl_stmt|;
if|if
condition|(
name|tmpDeployment
operator|==
literal|null
condition|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"wrong value for [{}]: [{}]. falling back to [{}]..."
argument_list|,
name|Fields
operator|.
name|DEPLOYMENT_SLOT
argument_list|,
name|strDeployment
argument_list|,
name|Deployment
operator|.
name|PRODUCTION
operator|.
name|deployment
argument_list|)
expr_stmt|;
name|tmpDeployment
operator|=
name|Deployment
operator|.
name|PRODUCTION
expr_stmt|;
block|}
name|this
operator|.
name|deploymentSlot
operator|=
name|tmpDeployment
operator|.
name|slot
expr_stmt|;
block|}
comment|/**      * We build the list of Nodes from Azure Management API      * Information can be cached using `cloud.azure.refresh_interval` property if needed.      * Setting `cloud.azure.refresh_interval` to `-1` will cause infinite caching.      * Setting `cloud.azure.refresh_interval` to `0` will disable caching (default).      */
annotation|@
name|Override
DECL|method|buildDynamicNodes
specifier|public
name|List
argument_list|<
name|DiscoveryNode
argument_list|>
name|buildDynamicNodes
parameter_list|()
block|{
if|if
condition|(
name|refreshInterval
operator|.
name|millis
argument_list|()
operator|!=
literal|0
condition|)
block|{
if|if
condition|(
name|cachedDiscoNodes
operator|!=
literal|null
operator|&&
operator|(
name|refreshInterval
operator|.
name|millis
argument_list|()
operator|<
literal|0
operator|||
operator|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|lastRefresh
operator|)
operator|<
name|refreshInterval
operator|.
name|millis
argument_list|()
operator|)
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"using cache to retrieve node list"
argument_list|)
expr_stmt|;
return|return
name|cachedDiscoNodes
return|;
block|}
name|lastRefresh
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
block|}
name|logger
operator|.
name|debug
argument_list|(
literal|"start building nodes list using Azure API"
argument_list|)
expr_stmt|;
name|cachedDiscoNodes
operator|=
name|Lists
operator|.
name|newArrayList
argument_list|()
expr_stmt|;
name|HostedServiceGetDetailedResponse
name|detailed
decl_stmt|;
try|try
block|{
name|detailed
operator|=
name|azureComputeService
operator|.
name|getServiceDetails
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|AzureServiceDisableException
name|e
parameter_list|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"Azure discovery service has been disabled. Returning empty list of nodes."
argument_list|)
expr_stmt|;
return|return
name|cachedDiscoNodes
return|;
block|}
catch|catch
parameter_list|(
name|AzureServiceRemoteException
name|e
parameter_list|)
block|{
comment|// We got a remote exception
name|logger
operator|.
name|warn
argument_list|(
literal|"can not get list of azure nodes: [{}]. Returning empty list of nodes."
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|logger
operator|.
name|trace
argument_list|(
literal|"AzureServiceRemoteException caught"
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return
name|cachedDiscoNodes
return|;
block|}
name|InetAddress
name|ipAddress
init|=
literal|null
decl_stmt|;
try|try
block|{
name|ipAddress
operator|=
name|networkService
operator|.
name|resolvePublishHostAddress
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|logger
operator|.
name|trace
argument_list|(
literal|"ip of current node: [{}]"
argument_list|,
name|ipAddress
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// We can't find the publish host address... Hmmm. Too bad :-(
name|logger
operator|.
name|trace
argument_list|(
literal|"exception while finding ip"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|HostedServiceGetDetailedResponse
operator|.
name|Deployment
name|deployment
range|:
name|detailed
operator|.
name|getDeployments
argument_list|()
control|)
block|{
comment|// We check the deployment slot
if|if
condition|(
name|deployment
operator|.
name|getDeploymentSlot
argument_list|()
operator|!=
name|deploymentSlot
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"current deployment slot [{}] for [{}] is different from [{}]. skipping..."
argument_list|,
name|deployment
operator|.
name|getDeploymentSlot
argument_list|()
argument_list|,
name|deployment
operator|.
name|getName
argument_list|()
argument_list|,
name|deploymentSlot
argument_list|)
expr_stmt|;
continue|continue;
block|}
comment|// If provided, we check the deployment name
if|if
condition|(
name|deploymentName
operator|!=
literal|null
operator|&&
operator|!
name|deploymentName
operator|.
name|equals
argument_list|(
name|deployment
operator|.
name|getName
argument_list|()
argument_list|)
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"current deployment name [{}] different from [{}]. skipping..."
argument_list|,
name|deployment
operator|.
name|getName
argument_list|()
argument_list|,
name|deploymentName
argument_list|)
expr_stmt|;
continue|continue;
block|}
comment|// We check current deployment status
if|if
condition|(
name|deployment
operator|.
name|getStatus
argument_list|()
operator|!=
name|DeploymentStatus
operator|.
name|Starting
operator|&&
name|deployment
operator|.
name|getStatus
argument_list|()
operator|!=
name|DeploymentStatus
operator|.
name|Deploying
operator|&&
name|deployment
operator|.
name|getStatus
argument_list|()
operator|!=
name|DeploymentStatus
operator|.
name|Running
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"[{}] status is [{}]. skipping..."
argument_list|,
name|deployment
operator|.
name|getName
argument_list|()
argument_list|,
name|deployment
operator|.
name|getStatus
argument_list|()
argument_list|)
expr_stmt|;
continue|continue;
block|}
comment|// In other case, it should be the right deployment so we can add it to the list of instances
for|for
control|(
name|RoleInstance
name|instance
range|:
name|deployment
operator|.
name|getRoleInstances
argument_list|()
control|)
block|{
name|String
name|networkAddress
init|=
literal|null
decl_stmt|;
comment|// Let's detect if we want to use public or private IP
switch|switch
condition|(
name|hostType
condition|)
block|{
case|case
name|PRIVATE_IP
case|:
name|InetAddress
name|privateIp
init|=
name|instance
operator|.
name|getIPAddress
argument_list|()
decl_stmt|;
if|if
condition|(
name|privateIp
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|privateIp
operator|.
name|equals
argument_list|(
name|ipAddress
argument_list|)
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"adding ourselves {}"
argument_list|,
name|ipAddress
argument_list|)
expr_stmt|;
block|}
name|networkAddress
operator|=
name|privateIp
operator|.
name|getHostAddress
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"no private ip provided. ignoring [{}]..."
argument_list|,
name|instance
operator|.
name|getInstanceName
argument_list|()
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
name|PUBLIC_IP
case|:
for|for
control|(
name|InstanceEndpoint
name|endpoint
range|:
name|instance
operator|.
name|getInstanceEndpoints
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|publicEndpointName
operator|.
name|equals
argument_list|(
name|endpoint
operator|.
name|getName
argument_list|()
argument_list|)
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"ignoring endpoint [{}] as different than [{}]"
argument_list|,
name|endpoint
operator|.
name|getName
argument_list|()
argument_list|,
name|publicEndpointName
argument_list|)
expr_stmt|;
continue|continue;
block|}
name|networkAddress
operator|=
name|endpoint
operator|.
name|getVirtualIPAddress
argument_list|()
operator|.
name|getHostAddress
argument_list|()
operator|+
literal|":"
operator|+
name|endpoint
operator|.
name|getPort
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|networkAddress
operator|==
literal|null
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"no public ip provided. ignoring [{}]..."
argument_list|,
name|instance
operator|.
name|getInstanceName
argument_list|()
argument_list|)
expr_stmt|;
block|}
break|break;
default|default:
comment|// This could never happen!
name|logger
operator|.
name|warn
argument_list|(
literal|"undefined host_type [{}]. Please check your settings."
argument_list|,
name|hostType
argument_list|)
expr_stmt|;
return|return
name|cachedDiscoNodes
return|;
block|}
if|if
condition|(
name|networkAddress
operator|==
literal|null
condition|)
block|{
comment|// We have a bad parameter here or not enough information from azure
name|logger
operator|.
name|warn
argument_list|(
literal|"no network address found. ignoring [{}]..."
argument_list|,
name|instance
operator|.
name|getInstanceName
argument_list|()
argument_list|)
expr_stmt|;
continue|continue;
block|}
try|try
block|{
name|TransportAddress
index|[]
name|addresses
init|=
name|transportService
operator|.
name|addressesFromString
argument_list|(
name|networkAddress
argument_list|)
decl_stmt|;
comment|// we only limit to 1 addresses, makes no sense to ping 100 ports
name|logger
operator|.
name|trace
argument_list|(
literal|"adding {}, transport_address {}"
argument_list|,
name|networkAddress
argument_list|,
name|addresses
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|cachedDiscoNodes
operator|.
name|add
argument_list|(
operator|new
name|DiscoveryNode
argument_list|(
literal|"#cloud-"
operator|+
name|instance
operator|.
name|getInstanceName
argument_list|()
argument_list|,
name|addresses
index|[
literal|0
index|]
argument_list|,
name|version
operator|.
name|minimumCompatibilityVersion
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"can not convert [{}] to transport address. skipping. [{}]"
argument_list|,
name|networkAddress
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|logger
operator|.
name|debug
argument_list|(
literal|"{} node(s) added"
argument_list|,
name|cachedDiscoNodes
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|cachedDiscoNodes
return|;
block|}
block|}
end_class

end_unit

