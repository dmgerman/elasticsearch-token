begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.discovery
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|discovery
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
name|routing
operator|.
name|allocation
operator|.
name|AllocationService
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
name|ClusterApplier
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
name|MasterService
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
name|io
operator|.
name|stream
operator|.
name|NamedWriteableRegistry
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
name|logging
operator|.
name|Loggers
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
name|ClusterSettings
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
name|Setting
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
name|Setting
operator|.
name|Property
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
name|single
operator|.
name|SingleNodeDiscovery
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
name|UnicastHostsProvider
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
name|plugins
operator|.
name|DiscoveryPlugin
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
name|java
operator|.
name|util
operator|.
name|Collections
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
import|import
name|java
operator|.
name|util
operator|.
name|Objects
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Optional
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|function
operator|.
name|Function
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|function
operator|.
name|Supplier
import|;
end_import

begin_comment
comment|/**  * A module for loading classes for node discovery.  */
end_comment

begin_class
DECL|class|DiscoveryModule
specifier|public
class|class
name|DiscoveryModule
block|{
DECL|field|DISCOVERY_TYPE_SETTING
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|String
argument_list|>
name|DISCOVERY_TYPE_SETTING
init|=
operator|new
name|Setting
argument_list|<>
argument_list|(
literal|"discovery.type"
argument_list|,
literal|"zen"
argument_list|,
name|Function
operator|.
name|identity
argument_list|()
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|)
decl_stmt|;
DECL|field|DISCOVERY_HOSTS_PROVIDER_SETTING
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|Optional
argument_list|<
name|String
argument_list|>
argument_list|>
name|DISCOVERY_HOSTS_PROVIDER_SETTING
init|=
operator|new
name|Setting
argument_list|<>
argument_list|(
literal|"discovery.zen.hosts_provider"
argument_list|,
operator|(
name|String
operator|)
literal|null
argument_list|,
name|Optional
operator|::
name|ofNullable
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|)
decl_stmt|;
DECL|field|discovery
specifier|private
specifier|final
name|Discovery
name|discovery
decl_stmt|;
DECL|method|DiscoveryModule
specifier|public
name|DiscoveryModule
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
name|NamedWriteableRegistry
name|namedWriteableRegistry
parameter_list|,
name|NetworkService
name|networkService
parameter_list|,
name|MasterService
name|masterService
parameter_list|,
name|ClusterApplier
name|clusterApplier
parameter_list|,
name|ClusterSettings
name|clusterSettings
parameter_list|,
name|List
argument_list|<
name|DiscoveryPlugin
argument_list|>
name|plugins
parameter_list|,
name|AllocationService
name|allocationService
parameter_list|)
block|{
specifier|final
name|UnicastHostsProvider
name|hostsProvider
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Supplier
argument_list|<
name|UnicastHostsProvider
argument_list|>
argument_list|>
name|hostProviders
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|DiscoveryPlugin
name|plugin
range|:
name|plugins
control|)
block|{
name|plugin
operator|.
name|getZenHostsProviders
argument_list|(
name|transportService
argument_list|,
name|networkService
argument_list|)
operator|.
name|entrySet
argument_list|()
operator|.
name|forEach
argument_list|(
name|entry
lambda|->
block|{
if|if
condition|(
name|hostProviders
operator|.
name|put
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Cannot register zen hosts provider ["
operator|+
name|entry
operator|.
name|getKey
argument_list|()
operator|+
literal|"] twice"
argument_list|)
throw|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
name|Optional
argument_list|<
name|String
argument_list|>
name|hostsProviderName
init|=
name|DISCOVERY_HOSTS_PROVIDER_SETTING
operator|.
name|get
argument_list|(
name|settings
argument_list|)
decl_stmt|;
if|if
condition|(
name|hostsProviderName
operator|.
name|isPresent
argument_list|()
condition|)
block|{
name|Supplier
argument_list|<
name|UnicastHostsProvider
argument_list|>
name|hostsProviderSupplier
init|=
name|hostProviders
operator|.
name|get
argument_list|(
name|hostsProviderName
operator|.
name|get
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|hostsProviderSupplier
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Unknown zen hosts provider ["
operator|+
name|hostsProviderName
operator|.
name|get
argument_list|()
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|hostsProvider
operator|=
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|hostsProviderSupplier
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|hostsProvider
operator|=
name|Collections
operator|::
name|emptyList
expr_stmt|;
block|}
name|Map
argument_list|<
name|String
argument_list|,
name|Supplier
argument_list|<
name|Discovery
argument_list|>
argument_list|>
name|discoveryTypes
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|discoveryTypes
operator|.
name|put
argument_list|(
literal|"zen"
argument_list|,
parameter_list|()
lambda|->
operator|new
name|ZenDiscovery
argument_list|(
name|settings
argument_list|,
name|threadPool
argument_list|,
name|transportService
argument_list|,
name|namedWriteableRegistry
argument_list|,
name|masterService
argument_list|,
name|clusterApplier
argument_list|,
name|clusterSettings
argument_list|,
name|hostsProvider
argument_list|,
name|allocationService
argument_list|)
argument_list|)
expr_stmt|;
name|discoveryTypes
operator|.
name|put
argument_list|(
literal|"tribe"
argument_list|,
parameter_list|()
lambda|->
operator|new
name|TribeDiscovery
argument_list|(
name|settings
argument_list|,
name|transportService
argument_list|,
name|clusterApplier
argument_list|)
argument_list|)
expr_stmt|;
name|discoveryTypes
operator|.
name|put
argument_list|(
literal|"single-node"
argument_list|,
parameter_list|()
lambda|->
operator|new
name|SingleNodeDiscovery
argument_list|(
name|settings
argument_list|,
name|transportService
argument_list|,
name|clusterApplier
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|DiscoveryPlugin
name|plugin
range|:
name|plugins
control|)
block|{
name|plugin
operator|.
name|getDiscoveryTypes
argument_list|(
name|threadPool
argument_list|,
name|transportService
argument_list|,
name|namedWriteableRegistry
argument_list|,
name|masterService
argument_list|,
name|clusterApplier
argument_list|,
name|clusterSettings
argument_list|,
name|hostsProvider
argument_list|,
name|allocationService
argument_list|)
operator|.
name|entrySet
argument_list|()
operator|.
name|forEach
argument_list|(
name|entry
lambda|->
block|{
if|if
condition|(
name|discoveryTypes
operator|.
name|put
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Cannot register discovery type ["
operator|+
name|entry
operator|.
name|getKey
argument_list|()
operator|+
literal|"] twice"
argument_list|)
throw|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
name|String
name|discoveryType
init|=
name|DISCOVERY_TYPE_SETTING
operator|.
name|get
argument_list|(
name|settings
argument_list|)
decl_stmt|;
name|Supplier
argument_list|<
name|Discovery
argument_list|>
name|discoverySupplier
init|=
name|discoveryTypes
operator|.
name|get
argument_list|(
name|discoveryType
argument_list|)
decl_stmt|;
if|if
condition|(
name|discoverySupplier
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Unknown discovery type ["
operator|+
name|discoveryType
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|Loggers
operator|.
name|getLogger
argument_list|(
name|getClass
argument_list|()
argument_list|,
name|settings
argument_list|)
operator|.
name|info
argument_list|(
literal|"using discovery type [{}]"
argument_list|,
name|discoveryType
argument_list|)
expr_stmt|;
name|discovery
operator|=
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|discoverySupplier
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|getDiscovery
specifier|public
name|Discovery
name|getDiscovery
parameter_list|()
block|{
return|return
name|discovery
return|;
block|}
block|}
end_class

end_unit

