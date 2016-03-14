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
name|inject
operator|.
name|AbstractModule
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
name|multibindings
operator|.
name|Multibinder
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
name|common
operator|.
name|util
operator|.
name|ExtensionPoint
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
name|local
operator|.
name|LocalDiscovery
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
name|elect
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
name|ping
operator|.
name|unicast
operator|.
name|UnicastZenPing
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
name|function
operator|.
name|Function
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
extends|extends
name|AbstractModule
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
name|settings
lambda|->
name|DiscoveryNode
operator|.
name|localNode
argument_list|(
name|settings
argument_list|)
condition|?
literal|"local"
else|:
literal|"zen"
condition|,
name|Function
operator|.
name|identity
argument_list|()
condition|,
name|Property
operator|.
name|NodeScope
argument_list|)
decl_stmt|;
DECL|field|ZEN_MASTER_SERVICE_TYPE_SETTING
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|String
argument_list|>
name|ZEN_MASTER_SERVICE_TYPE_SETTING
init|=
operator|new
name|Setting
argument_list|<>
argument_list|(
literal|"discovery.zen.masterservice.type"
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
DECL|field|settings
specifier|private
specifier|final
name|Settings
name|settings
decl_stmt|;
DECL|field|unicastHostProviders
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|UnicastHostsProvider
argument_list|>
argument_list|>
argument_list|>
name|unicastHostProviders
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
DECL|field|zenPings
specifier|private
specifier|final
name|ExtensionPoint
operator|.
name|ClassSet
argument_list|<
name|ZenPing
argument_list|>
name|zenPings
init|=
operator|new
name|ExtensionPoint
operator|.
name|ClassSet
argument_list|<>
argument_list|(
literal|"zen_ping"
argument_list|,
name|ZenPing
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|discoveryTypes
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Class
argument_list|<
name|?
extends|extends
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
DECL|field|masterServiceType
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Class
argument_list|<
name|?
extends|extends
name|ElectMasterService
argument_list|>
argument_list|>
name|masterServiceType
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
DECL|method|DiscoveryModule
specifier|public
name|DiscoveryModule
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
name|addDiscoveryType
argument_list|(
literal|"local"
argument_list|,
name|LocalDiscovery
operator|.
name|class
argument_list|)
expr_stmt|;
name|addDiscoveryType
argument_list|(
literal|"zen"
argument_list|,
name|ZenDiscovery
operator|.
name|class
argument_list|)
expr_stmt|;
name|addElectMasterService
argument_list|(
literal|"zen"
argument_list|,
name|ElectMasterService
operator|.
name|class
argument_list|)
expr_stmt|;
comment|// always add the unicast hosts, or things get angry!
name|addZenPing
argument_list|(
name|UnicastZenPing
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
comment|/**      * Adds a custom unicast hosts provider to build a dynamic list of unicast hosts list when doing unicast discovery.      *      * @param type discovery for which this provider is relevant      * @param unicastHostProvider the host provider      */
DECL|method|addUnicastHostProvider
specifier|public
name|void
name|addUnicastHostProvider
parameter_list|(
name|String
name|type
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|UnicastHostsProvider
argument_list|>
name|unicastHostProvider
parameter_list|)
block|{
name|List
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|UnicastHostsProvider
argument_list|>
argument_list|>
name|providerList
init|=
name|unicastHostProviders
operator|.
name|get
argument_list|(
name|type
argument_list|)
decl_stmt|;
if|if
condition|(
name|providerList
operator|==
literal|null
condition|)
block|{
name|providerList
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
name|unicastHostProviders
operator|.
name|put
argument_list|(
name|type
argument_list|,
name|providerList
argument_list|)
expr_stmt|;
block|}
name|providerList
operator|.
name|add
argument_list|(
name|unicastHostProvider
argument_list|)
expr_stmt|;
block|}
comment|/**      * Adds a custom Discovery type.      */
DECL|method|addDiscoveryType
specifier|public
name|void
name|addDiscoveryType
parameter_list|(
name|String
name|type
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|Discovery
argument_list|>
name|clazz
parameter_list|)
block|{
if|if
condition|(
name|discoveryTypes
operator|.
name|containsKey
argument_list|(
name|type
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"discovery type ["
operator|+
name|type
operator|+
literal|"] is already registered"
argument_list|)
throw|;
block|}
name|discoveryTypes
operator|.
name|put
argument_list|(
name|type
argument_list|,
name|clazz
argument_list|)
expr_stmt|;
block|}
comment|/**      * Adds a custom zen master service type.      */
DECL|method|addElectMasterService
specifier|public
name|void
name|addElectMasterService
parameter_list|(
name|String
name|type
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|ElectMasterService
argument_list|>
name|masterService
parameter_list|)
block|{
if|if
condition|(
name|masterServiceType
operator|.
name|containsKey
argument_list|(
name|type
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"master service type ["
operator|+
name|type
operator|+
literal|"] is already registered"
argument_list|)
throw|;
block|}
name|this
operator|.
name|masterServiceType
operator|.
name|put
argument_list|(
name|type
argument_list|,
name|masterService
argument_list|)
expr_stmt|;
block|}
DECL|method|addZenPing
specifier|public
name|void
name|addZenPing
parameter_list|(
name|Class
argument_list|<
name|?
extends|extends
name|ZenPing
argument_list|>
name|clazz
parameter_list|)
block|{
name|zenPings
operator|.
name|registerExtension
argument_list|(
name|clazz
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|configure
specifier|protected
name|void
name|configure
parameter_list|()
block|{
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
name|Class
argument_list|<
name|?
extends|extends
name|Discovery
argument_list|>
name|discoveryClass
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
name|discoveryClass
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Unknown Discovery type ["
operator|+
name|discoveryType
operator|+
literal|"]"
argument_list|)
throw|;
block|}
if|if
condition|(
name|discoveryType
operator|.
name|equals
argument_list|(
literal|"local"
argument_list|)
operator|==
literal|false
condition|)
block|{
name|String
name|masterServiceTypeKey
init|=
name|ZEN_MASTER_SERVICE_TYPE_SETTING
operator|.
name|get
argument_list|(
name|settings
argument_list|)
decl_stmt|;
specifier|final
name|Class
argument_list|<
name|?
extends|extends
name|ElectMasterService
argument_list|>
name|masterService
init|=
name|masterServiceType
operator|.
name|get
argument_list|(
name|masterServiceTypeKey
argument_list|)
decl_stmt|;
if|if
condition|(
name|masterService
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Unknown master service type ["
operator|+
name|masterServiceTypeKey
operator|+
literal|"]"
argument_list|)
throw|;
block|}
if|if
condition|(
name|masterService
operator|==
name|ElectMasterService
operator|.
name|class
condition|)
block|{
name|bind
argument_list|(
name|ElectMasterService
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|bind
argument_list|(
name|ElectMasterService
operator|.
name|class
argument_list|)
operator|.
name|to
argument_list|(
name|masterService
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
block|}
name|bind
argument_list|(
name|ZenPingService
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|Multibinder
argument_list|<
name|UnicastHostsProvider
argument_list|>
name|unicastHostsProviderMultibinder
init|=
name|Multibinder
operator|.
name|newSetBinder
argument_list|(
name|binder
argument_list|()
argument_list|,
name|UnicastHostsProvider
operator|.
name|class
argument_list|)
decl_stmt|;
for|for
control|(
name|Class
argument_list|<
name|?
extends|extends
name|UnicastHostsProvider
argument_list|>
name|unicastHostProvider
range|:
name|unicastHostProviders
operator|.
name|getOrDefault
argument_list|(
name|discoveryType
argument_list|,
name|Collections
operator|.
name|emptyList
argument_list|()
argument_list|)
control|)
block|{
name|unicastHostsProviderMultibinder
operator|.
name|addBinding
argument_list|()
operator|.
name|to
argument_list|(
name|unicastHostProvider
argument_list|)
expr_stmt|;
block|}
name|zenPings
operator|.
name|bind
argument_list|(
name|binder
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|bind
argument_list|(
name|Discovery
operator|.
name|class
argument_list|)
operator|.
name|to
argument_list|(
name|discoveryClass
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

