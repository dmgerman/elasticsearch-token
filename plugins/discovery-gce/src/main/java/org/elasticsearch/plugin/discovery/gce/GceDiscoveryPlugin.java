begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.plugin.discovery.gce
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|plugin
operator|.
name|discovery
operator|.
name|gce
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|api
operator|.
name|client
operator|.
name|http
operator|.
name|HttpHeaders
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|api
operator|.
name|client
operator|.
name|util
operator|.
name|ClassInfo
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|logging
operator|.
name|log4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|IOUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|SetOnce
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
name|gce
operator|.
name|GceInstancesService
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
name|gce
operator|.
name|GceInstancesServiceImpl
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
name|gce
operator|.
name|GceMetadataService
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
name|gce
operator|.
name|network
operator|.
name|GceNameResolver
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
name|gce
operator|.
name|util
operator|.
name|Access
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
name|DeprecationLogger
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
name|DiscoveryModule
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
name|discovery
operator|.
name|gce
operator|.
name|GceUnicastHostsProvider
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
name|plugins
operator|.
name|Plugin
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
name|io
operator|.
name|Closeable
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
name|util
operator|.
name|Arrays
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
name|function
operator|.
name|Supplier
import|;
end_import

begin_class
DECL|class|GceDiscoveryPlugin
specifier|public
class|class
name|GceDiscoveryPlugin
extends|extends
name|Plugin
implements|implements
name|DiscoveryPlugin
implements|,
name|Closeable
block|{
DECL|field|GCE
specifier|public
specifier|static
specifier|final
name|String
name|GCE
init|=
literal|"gce"
decl_stmt|;
DECL|field|settings
specifier|private
specifier|final
name|Settings
name|settings
decl_stmt|;
DECL|field|logger
specifier|private
specifier|static
specifier|final
name|Logger
name|logger
init|=
name|Loggers
operator|.
name|getLogger
argument_list|(
name|GceDiscoveryPlugin
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|deprecationLogger
specifier|private
specifier|static
specifier|final
name|DeprecationLogger
name|deprecationLogger
init|=
operator|new
name|DeprecationLogger
argument_list|(
name|logger
argument_list|)
decl_stmt|;
comment|// stashed when created in order to properly close
DECL|field|gceInstancesService
specifier|private
specifier|final
name|SetOnce
argument_list|<
name|GceInstancesServiceImpl
argument_list|>
name|gceInstancesService
init|=
operator|new
name|SetOnce
argument_list|<>
argument_list|()
decl_stmt|;
static|static
block|{
comment|/*          * GCE's http client changes access levels because its silly and we          * can't allow that on any old stack so we pull it here, up front,          * so we can cleanly check the permissions for it. Without this changing          * the permission can fail if any part of core is on the stack because          * our plugin permissions don't allow core to "reach through" plugins to          * change the permission. Because that'd be silly.          */
name|Access
operator|.
name|doPrivilegedVoid
argument_list|(
parameter_list|()
lambda|->
name|ClassInfo
operator|.
name|of
argument_list|(
name|HttpHeaders
operator|.
name|class
argument_list|,
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|GceDiscoveryPlugin
specifier|public
name|GceDiscoveryPlugin
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|this
operator|.
name|settings
operator|=
name|settings
expr_stmt|;
name|logger
operator|.
name|trace
argument_list|(
literal|"starting gce discovery plugin..."
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getDiscoveryTypes
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|Supplier
argument_list|<
name|Discovery
argument_list|>
argument_list|>
name|getDiscoveryTypes
parameter_list|(
name|ThreadPool
name|threadPool
parameter_list|,
name|TransportService
name|transportService
parameter_list|,
name|NamedWriteableRegistry
name|namedWriteableRegistry
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
name|UnicastHostsProvider
name|hostsProvider
parameter_list|,
name|AllocationService
name|allocationService
parameter_list|)
block|{
comment|// this is for backcompat with pre 5.1, where users would set discovery.type to use ec2 hosts provider
return|return
name|Collections
operator|.
name|singletonMap
argument_list|(
name|GCE
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
return|;
block|}
annotation|@
name|Override
DECL|method|getZenHostsProviders
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|Supplier
argument_list|<
name|UnicastHostsProvider
argument_list|>
argument_list|>
name|getZenHostsProviders
parameter_list|(
name|TransportService
name|transportService
parameter_list|,
name|NetworkService
name|networkService
parameter_list|)
block|{
return|return
name|Collections
operator|.
name|singletonMap
argument_list|(
name|GCE
argument_list|,
parameter_list|()
lambda|->
block|{
name|gceInstancesService
operator|.
name|set
argument_list|(
operator|new
name|GceInstancesServiceImpl
argument_list|(
name|settings
argument_list|)
argument_list|)
expr_stmt|;
return|return
operator|new
name|GceUnicastHostsProvider
argument_list|(
name|settings
argument_list|,
name|gceInstancesService
operator|.
name|get
argument_list|()
argument_list|,
name|transportService
argument_list|,
name|networkService
argument_list|)
return|;
block|}
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|getCustomNameResolver
specifier|public
name|NetworkService
operator|.
name|CustomNameResolver
name|getCustomNameResolver
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"Register _gce_, _gce:xxx network names"
argument_list|)
expr_stmt|;
return|return
operator|new
name|GceNameResolver
argument_list|(
name|settings
argument_list|,
operator|new
name|GceMetadataService
argument_list|(
name|settings
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|getSettings
specifier|public
name|List
argument_list|<
name|Setting
argument_list|<
name|?
argument_list|>
argument_list|>
name|getSettings
parameter_list|()
block|{
return|return
name|Arrays
operator|.
name|asList
argument_list|(
comment|// Register GCE settings
name|GceInstancesService
operator|.
name|PROJECT_SETTING
argument_list|,
name|GceInstancesService
operator|.
name|ZONE_SETTING
argument_list|,
name|GceUnicastHostsProvider
operator|.
name|TAGS_SETTING
argument_list|,
name|GceInstancesService
operator|.
name|REFRESH_SETTING
argument_list|,
name|GceInstancesService
operator|.
name|RETRY_SETTING
argument_list|,
name|GceInstancesService
operator|.
name|MAX_WAIT_SETTING
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|additionalSettings
specifier|public
name|Settings
name|additionalSettings
parameter_list|()
block|{
comment|// For 5.0, the hosts provider was "zen", but this was before the discovery.zen.hosts_provider
comment|// setting existed. This check looks for the legacy setting, and sets hosts provider if set
name|String
name|discoveryType
init|=
name|DiscoveryModule
operator|.
name|DISCOVERY_TYPE_SETTING
operator|.
name|get
argument_list|(
name|settings
argument_list|)
decl_stmt|;
if|if
condition|(
name|discoveryType
operator|.
name|equals
argument_list|(
name|GCE
argument_list|)
condition|)
block|{
name|deprecationLogger
operator|.
name|deprecated
argument_list|(
literal|"Using "
operator|+
name|DiscoveryModule
operator|.
name|DISCOVERY_TYPE_SETTING
operator|.
name|getKey
argument_list|()
operator|+
literal|" setting to set hosts provider is deprecated. "
operator|+
literal|"Set \""
operator|+
name|DiscoveryModule
operator|.
name|DISCOVERY_HOSTS_PROVIDER_SETTING
operator|.
name|getKey
argument_list|()
operator|+
literal|": "
operator|+
name|GCE
operator|+
literal|"\" instead"
argument_list|)
expr_stmt|;
if|if
condition|(
name|DiscoveryModule
operator|.
name|DISCOVERY_HOSTS_PROVIDER_SETTING
operator|.
name|exists
argument_list|(
name|settings
argument_list|)
operator|==
literal|false
condition|)
block|{
return|return
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|DiscoveryModule
operator|.
name|DISCOVERY_HOSTS_PROVIDER_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
name|GCE
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
block|}
return|return
name|Settings
operator|.
name|EMPTY
return|;
block|}
annotation|@
name|Override
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
name|IOUtils
operator|.
name|close
argument_list|(
name|gceInstancesService
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

