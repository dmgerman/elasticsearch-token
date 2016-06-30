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
name|elasticsearch
operator|.
name|SpecialPermission
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
name|GceModule
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
name|Module
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
name|ESLogger
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
name|common
operator|.
name|settings
operator|.
name|SettingsModule
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
name|Plugin
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|AccessController
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|PrivilegedAction
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
name|Collection
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

begin_class
DECL|class|GceDiscoveryPlugin
specifier|public
class|class
name|GceDiscoveryPlugin
extends|extends
name|Plugin
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
static|static
block|{
comment|/*          * GCE's http client changes access levels because its silly and we          * can't allow that on any old stack so we pull it here, up front,          * so we can cleanly check the permissions for it. Without this changing          * the permission can fail if any part of core is on the stack because          * our plugin permissions don't allow core to "reach through" plugins to          * change the permission. Because that'd be silly.          */
name|SecurityManager
name|sm
init|=
name|System
operator|.
name|getSecurityManager
argument_list|()
decl_stmt|;
if|if
condition|(
name|sm
operator|!=
literal|null
condition|)
block|{
name|sm
operator|.
name|checkPermission
argument_list|(
operator|new
name|SpecialPermission
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|AccessController
operator|.
name|doPrivileged
argument_list|(
operator|new
name|PrivilegedAction
argument_list|<
name|Void
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Void
name|run
parameter_list|()
block|{
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
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|nodeModules
specifier|public
name|Collection
argument_list|<
name|Module
argument_list|>
name|nodeModules
parameter_list|()
block|{
return|return
name|Collections
operator|.
name|singletonList
argument_list|(
operator|new
name|GceModule
argument_list|(
name|settings
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
annotation|@
name|SuppressWarnings
argument_list|(
literal|"rawtypes"
argument_list|)
comment|// Supertype uses raw type
DECL|method|nodeServices
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
name|nodeServices
parameter_list|()
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"Register gce compute and metadata services"
argument_list|)
expr_stmt|;
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
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|services
operator|.
name|add
argument_list|(
name|GceModule
operator|.
name|getComputeServiceImpl
argument_list|()
argument_list|)
expr_stmt|;
name|services
operator|.
name|add
argument_list|(
name|GceModule
operator|.
name|getMetadataServiceImpl
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|services
return|;
block|}
DECL|method|onModule
specifier|public
name|void
name|onModule
parameter_list|(
name|DiscoveryModule
name|discoveryModule
parameter_list|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"Register gce discovery type and gce unicast provider"
argument_list|)
expr_stmt|;
name|discoveryModule
operator|.
name|addDiscoveryType
argument_list|(
name|GCE
argument_list|,
name|ZenDiscovery
operator|.
name|class
argument_list|)
expr_stmt|;
name|discoveryModule
operator|.
name|addUnicastHostProvider
argument_list|(
name|GCE
argument_list|,
name|GceUnicastHostsProvider
operator|.
name|class
argument_list|)
expr_stmt|;
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
block|}
end_class

end_unit

