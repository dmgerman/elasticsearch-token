begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.node.internal
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|node
operator|.
name|internal
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
name|Version
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
name|TransportActionModule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cache
operator|.
name|NodeCache
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cache
operator|.
name|NodeCacheModule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|client
operator|.
name|Client
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|client
operator|.
name|node
operator|.
name|NodeClientModule
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
name|ClusterModule
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
name|ClusterNameModule
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
name|CacheRecycler
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
name|StopWatch
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
name|Tuple
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
name|Injector
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
name|Injectors
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
name|ModulesBuilder
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
name|CachedStreams
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
name|network
operator|.
name|NetworkModule
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
name|ImmutableSettings
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
name|common
operator|.
name|thread
operator|.
name|ThreadLocals
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
name|DiscoveryService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|env
operator|.
name|Environment
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|env
operator|.
name|EnvironmentModule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|env
operator|.
name|NodeEnvironment
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|env
operator|.
name|NodeEnvironmentModule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|gateway
operator|.
name|GatewayModule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|gateway
operator|.
name|GatewayService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|http
operator|.
name|HttpServer
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|http
operator|.
name|HttpServerModule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|IndicesModule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|IndicesService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|cache
operator|.
name|filter
operator|.
name|IndicesNodeFilterCache
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|cluster
operator|.
name|IndicesClusterStateService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|memory
operator|.
name|IndexingMemoryBufferController
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|ttl
operator|.
name|IndicesTTLService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|jmx
operator|.
name|JmxModule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|jmx
operator|.
name|JmxService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|monitor
operator|.
name|MonitorModule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|monitor
operator|.
name|MonitorService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|monitor
operator|.
name|jvm
operator|.
name|JvmInfo
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
name|Node
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
name|PluginsModule
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
name|PluginsService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|RestController
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|RestModule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|river
operator|.
name|RiversManager
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|river
operator|.
name|RiversModule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|ScriptModule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|ScriptService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|SearchModule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|SearchService
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
name|threadpool
operator|.
name|ThreadPoolModule
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
name|TransportModule
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
name|concurrent
operator|.
name|TimeUnit
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|InternalNode
specifier|public
specifier|final
class|class
name|InternalNode
implements|implements
name|Node
block|{
DECL|field|lifecycle
specifier|private
specifier|final
name|Lifecycle
name|lifecycle
init|=
operator|new
name|Lifecycle
argument_list|()
decl_stmt|;
DECL|field|injector
specifier|private
specifier|final
name|Injector
name|injector
decl_stmt|;
DECL|field|settings
specifier|private
specifier|final
name|Settings
name|settings
decl_stmt|;
DECL|field|environment
specifier|private
specifier|final
name|Environment
name|environment
decl_stmt|;
DECL|field|pluginsService
specifier|private
specifier|final
name|PluginsService
name|pluginsService
decl_stmt|;
DECL|field|client
specifier|private
specifier|final
name|Client
name|client
decl_stmt|;
DECL|method|InternalNode
specifier|public
name|InternalNode
parameter_list|()
throws|throws
name|ElasticSearchException
block|{
name|this
argument_list|(
name|ImmutableSettings
operator|.
name|Builder
operator|.
name|EMPTY_SETTINGS
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
DECL|method|InternalNode
specifier|public
name|InternalNode
parameter_list|(
name|Settings
name|pSettings
parameter_list|,
name|boolean
name|loadConfigSettings
parameter_list|)
throws|throws
name|ElasticSearchException
block|{
name|Tuple
argument_list|<
name|Settings
argument_list|,
name|Environment
argument_list|>
name|tuple
init|=
name|InternalSettingsPerparer
operator|.
name|prepareSettings
argument_list|(
name|pSettings
argument_list|,
name|loadConfigSettings
argument_list|)
decl_stmt|;
name|ESLogger
name|logger
init|=
name|Loggers
operator|.
name|getLogger
argument_list|(
name|Node
operator|.
name|class
argument_list|,
name|tuple
operator|.
name|v1
argument_list|()
operator|.
name|get
argument_list|(
literal|"name"
argument_list|)
argument_list|)
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"{{}}[{}]: initializing ..."
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|,
name|JvmInfo
operator|.
name|jvmInfo
argument_list|()
operator|.
name|pid
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|pluginsService
operator|=
operator|new
name|PluginsService
argument_list|(
name|tuple
operator|.
name|v1
argument_list|()
argument_list|,
name|tuple
operator|.
name|v2
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|settings
operator|=
name|pluginsService
operator|.
name|updatedSettings
argument_list|()
expr_stmt|;
name|this
operator|.
name|environment
operator|=
name|tuple
operator|.
name|v2
argument_list|()
expr_stmt|;
name|NodeEnvironment
name|nodeEnvironment
init|=
operator|new
name|NodeEnvironment
argument_list|(
name|this
operator|.
name|settings
argument_list|,
name|this
operator|.
name|environment
argument_list|)
decl_stmt|;
name|ModulesBuilder
name|modules
init|=
operator|new
name|ModulesBuilder
argument_list|()
decl_stmt|;
name|modules
operator|.
name|add
argument_list|(
operator|new
name|PluginsModule
argument_list|(
name|settings
argument_list|,
name|pluginsService
argument_list|)
argument_list|)
expr_stmt|;
name|modules
operator|.
name|add
argument_list|(
operator|new
name|SettingsModule
argument_list|(
name|settings
argument_list|)
argument_list|)
expr_stmt|;
name|modules
operator|.
name|add
argument_list|(
operator|new
name|NodeModule
argument_list|(
name|this
argument_list|)
argument_list|)
expr_stmt|;
name|modules
operator|.
name|add
argument_list|(
operator|new
name|NetworkModule
argument_list|()
argument_list|)
expr_stmt|;
name|modules
operator|.
name|add
argument_list|(
operator|new
name|NodeCacheModule
argument_list|(
name|settings
argument_list|)
argument_list|)
expr_stmt|;
name|modules
operator|.
name|add
argument_list|(
operator|new
name|ScriptModule
argument_list|(
name|settings
argument_list|)
argument_list|)
expr_stmt|;
name|modules
operator|.
name|add
argument_list|(
operator|new
name|JmxModule
argument_list|(
name|settings
argument_list|)
argument_list|)
expr_stmt|;
name|modules
operator|.
name|add
argument_list|(
operator|new
name|EnvironmentModule
argument_list|(
name|environment
argument_list|)
argument_list|)
expr_stmt|;
name|modules
operator|.
name|add
argument_list|(
operator|new
name|NodeEnvironmentModule
argument_list|(
name|nodeEnvironment
argument_list|)
argument_list|)
expr_stmt|;
name|modules
operator|.
name|add
argument_list|(
operator|new
name|ClusterNameModule
argument_list|(
name|settings
argument_list|)
argument_list|)
expr_stmt|;
name|modules
operator|.
name|add
argument_list|(
operator|new
name|ThreadPoolModule
argument_list|(
name|settings
argument_list|)
argument_list|)
expr_stmt|;
name|modules
operator|.
name|add
argument_list|(
operator|new
name|DiscoveryModule
argument_list|(
name|settings
argument_list|)
argument_list|)
expr_stmt|;
name|modules
operator|.
name|add
argument_list|(
operator|new
name|ClusterModule
argument_list|(
name|settings
argument_list|)
argument_list|)
expr_stmt|;
name|modules
operator|.
name|add
argument_list|(
operator|new
name|RestModule
argument_list|(
name|settings
argument_list|)
argument_list|)
expr_stmt|;
name|modules
operator|.
name|add
argument_list|(
operator|new
name|TransportModule
argument_list|(
name|settings
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|settings
operator|.
name|getAsBoolean
argument_list|(
literal|"http.enabled"
argument_list|,
literal|true
argument_list|)
condition|)
block|{
name|modules
operator|.
name|add
argument_list|(
operator|new
name|HttpServerModule
argument_list|(
name|settings
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|modules
operator|.
name|add
argument_list|(
operator|new
name|RiversModule
argument_list|(
name|settings
argument_list|)
argument_list|)
expr_stmt|;
name|modules
operator|.
name|add
argument_list|(
operator|new
name|IndicesModule
argument_list|(
name|settings
argument_list|)
argument_list|)
expr_stmt|;
name|modules
operator|.
name|add
argument_list|(
operator|new
name|SearchModule
argument_list|()
argument_list|)
expr_stmt|;
name|modules
operator|.
name|add
argument_list|(
operator|new
name|TransportActionModule
argument_list|()
argument_list|)
expr_stmt|;
name|modules
operator|.
name|add
argument_list|(
operator|new
name|MonitorModule
argument_list|(
name|settings
argument_list|)
argument_list|)
expr_stmt|;
name|modules
operator|.
name|add
argument_list|(
operator|new
name|GatewayModule
argument_list|(
name|settings
argument_list|)
argument_list|)
expr_stmt|;
name|modules
operator|.
name|add
argument_list|(
operator|new
name|NodeClientModule
argument_list|()
argument_list|)
expr_stmt|;
name|injector
operator|=
name|modules
operator|.
name|createInjector
argument_list|()
expr_stmt|;
name|client
operator|=
name|injector
operator|.
name|getInstance
argument_list|(
name|Client
operator|.
name|class
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"{{}}[{}]: initialized"
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|,
name|JvmInfo
operator|.
name|jvmInfo
argument_list|()
operator|.
name|pid
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|settings
specifier|public
name|Settings
name|settings
parameter_list|()
block|{
return|return
name|this
operator|.
name|settings
return|;
block|}
annotation|@
name|Override
DECL|method|client
specifier|public
name|Client
name|client
parameter_list|()
block|{
return|return
name|client
return|;
block|}
DECL|method|start
specifier|public
name|Node
name|start
parameter_list|()
block|{
if|if
condition|(
operator|!
name|lifecycle
operator|.
name|moveToStarted
argument_list|()
condition|)
block|{
return|return
name|this
return|;
block|}
name|ESLogger
name|logger
init|=
name|Loggers
operator|.
name|getLogger
argument_list|(
name|Node
operator|.
name|class
argument_list|,
name|settings
operator|.
name|get
argument_list|(
literal|"name"
argument_list|)
argument_list|)
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"{{}}[{}]: starting ..."
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|,
name|JvmInfo
operator|.
name|jvmInfo
argument_list|()
operator|.
name|pid
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Class
argument_list|<
name|?
extends|extends
name|LifecycleComponent
argument_list|>
name|plugin
range|:
name|pluginsService
operator|.
name|services
argument_list|()
control|)
block|{
name|injector
operator|.
name|getInstance
argument_list|(
name|plugin
argument_list|)
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
name|injector
operator|.
name|getInstance
argument_list|(
name|IndicesService
operator|.
name|class
argument_list|)
operator|.
name|start
argument_list|()
expr_stmt|;
name|injector
operator|.
name|getInstance
argument_list|(
name|IndexingMemoryBufferController
operator|.
name|class
argument_list|)
operator|.
name|start
argument_list|()
expr_stmt|;
name|injector
operator|.
name|getInstance
argument_list|(
name|IndicesClusterStateService
operator|.
name|class
argument_list|)
operator|.
name|start
argument_list|()
expr_stmt|;
name|injector
operator|.
name|getInstance
argument_list|(
name|IndicesTTLService
operator|.
name|class
argument_list|)
operator|.
name|start
argument_list|()
expr_stmt|;
name|injector
operator|.
name|getInstance
argument_list|(
name|RiversManager
operator|.
name|class
argument_list|)
operator|.
name|start
argument_list|()
expr_stmt|;
name|injector
operator|.
name|getInstance
argument_list|(
name|ClusterService
operator|.
name|class
argument_list|)
operator|.
name|start
argument_list|()
expr_stmt|;
name|injector
operator|.
name|getInstance
argument_list|(
name|RoutingService
operator|.
name|class
argument_list|)
operator|.
name|start
argument_list|()
expr_stmt|;
name|injector
operator|.
name|getInstance
argument_list|(
name|SearchService
operator|.
name|class
argument_list|)
operator|.
name|start
argument_list|()
expr_stmt|;
name|injector
operator|.
name|getInstance
argument_list|(
name|MonitorService
operator|.
name|class
argument_list|)
operator|.
name|start
argument_list|()
expr_stmt|;
name|injector
operator|.
name|getInstance
argument_list|(
name|RestController
operator|.
name|class
argument_list|)
operator|.
name|start
argument_list|()
expr_stmt|;
name|injector
operator|.
name|getInstance
argument_list|(
name|TransportService
operator|.
name|class
argument_list|)
operator|.
name|start
argument_list|()
expr_stmt|;
name|DiscoveryService
name|discoService
init|=
name|injector
operator|.
name|getInstance
argument_list|(
name|DiscoveryService
operator|.
name|class
argument_list|)
operator|.
name|start
argument_list|()
decl_stmt|;
comment|// gateway should start after disco, so it can try and recovery from gateway on "start"
name|injector
operator|.
name|getInstance
argument_list|(
name|GatewayService
operator|.
name|class
argument_list|)
operator|.
name|start
argument_list|()
expr_stmt|;
if|if
condition|(
name|settings
operator|.
name|getAsBoolean
argument_list|(
literal|"http.enabled"
argument_list|,
literal|true
argument_list|)
condition|)
block|{
name|injector
operator|.
name|getInstance
argument_list|(
name|HttpServer
operator|.
name|class
argument_list|)
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
name|injector
operator|.
name|getInstance
argument_list|(
name|JmxService
operator|.
name|class
argument_list|)
operator|.
name|connectAndRegister
argument_list|(
name|discoService
operator|.
name|nodeDescription
argument_list|()
argument_list|,
name|injector
operator|.
name|getInstance
argument_list|(
name|NetworkService
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"{{}}[{}]: started"
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|,
name|JvmInfo
operator|.
name|jvmInfo
argument_list|()
operator|.
name|pid
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
DECL|method|stop
specifier|public
name|Node
name|stop
parameter_list|()
block|{
if|if
condition|(
operator|!
name|lifecycle
operator|.
name|moveToStopped
argument_list|()
condition|)
block|{
return|return
name|this
return|;
block|}
name|ESLogger
name|logger
init|=
name|Loggers
operator|.
name|getLogger
argument_list|(
name|Node
operator|.
name|class
argument_list|,
name|settings
operator|.
name|get
argument_list|(
literal|"name"
argument_list|)
argument_list|)
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"{{}}[{}]: stopping ..."
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|,
name|JvmInfo
operator|.
name|jvmInfo
argument_list|()
operator|.
name|pid
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|settings
operator|.
name|getAsBoolean
argument_list|(
literal|"http.enabled"
argument_list|,
literal|true
argument_list|)
condition|)
block|{
name|injector
operator|.
name|getInstance
argument_list|(
name|HttpServer
operator|.
name|class
argument_list|)
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
name|injector
operator|.
name|getInstance
argument_list|(
name|RiversManager
operator|.
name|class
argument_list|)
operator|.
name|stop
argument_list|()
expr_stmt|;
comment|// stop any changes happening as a result of cluster state changes
name|injector
operator|.
name|getInstance
argument_list|(
name|IndicesClusterStateService
operator|.
name|class
argument_list|)
operator|.
name|stop
argument_list|()
expr_stmt|;
comment|// we close indices first, so operations won't be allowed on it
name|injector
operator|.
name|getInstance
argument_list|(
name|IndexingMemoryBufferController
operator|.
name|class
argument_list|)
operator|.
name|stop
argument_list|()
expr_stmt|;
name|injector
operator|.
name|getInstance
argument_list|(
name|IndicesTTLService
operator|.
name|class
argument_list|)
operator|.
name|stop
argument_list|()
expr_stmt|;
name|injector
operator|.
name|getInstance
argument_list|(
name|IndicesService
operator|.
name|class
argument_list|)
operator|.
name|stop
argument_list|()
expr_stmt|;
comment|// sleep a bit to let operations finish with indices service
comment|//        try {
comment|//            Thread.sleep(500);
comment|//        } catch (InterruptedException e) {
comment|//            // ignore
comment|//        }
name|injector
operator|.
name|getInstance
argument_list|(
name|RoutingService
operator|.
name|class
argument_list|)
operator|.
name|stop
argument_list|()
expr_stmt|;
name|injector
operator|.
name|getInstance
argument_list|(
name|ClusterService
operator|.
name|class
argument_list|)
operator|.
name|stop
argument_list|()
expr_stmt|;
name|injector
operator|.
name|getInstance
argument_list|(
name|DiscoveryService
operator|.
name|class
argument_list|)
operator|.
name|stop
argument_list|()
expr_stmt|;
name|injector
operator|.
name|getInstance
argument_list|(
name|MonitorService
operator|.
name|class
argument_list|)
operator|.
name|stop
argument_list|()
expr_stmt|;
name|injector
operator|.
name|getInstance
argument_list|(
name|GatewayService
operator|.
name|class
argument_list|)
operator|.
name|stop
argument_list|()
expr_stmt|;
name|injector
operator|.
name|getInstance
argument_list|(
name|SearchService
operator|.
name|class
argument_list|)
operator|.
name|stop
argument_list|()
expr_stmt|;
name|injector
operator|.
name|getInstance
argument_list|(
name|RestController
operator|.
name|class
argument_list|)
operator|.
name|stop
argument_list|()
expr_stmt|;
name|injector
operator|.
name|getInstance
argument_list|(
name|TransportService
operator|.
name|class
argument_list|)
operator|.
name|stop
argument_list|()
expr_stmt|;
name|injector
operator|.
name|getInstance
argument_list|(
name|JmxService
operator|.
name|class
argument_list|)
operator|.
name|close
argument_list|()
expr_stmt|;
for|for
control|(
name|Class
argument_list|<
name|?
extends|extends
name|LifecycleComponent
argument_list|>
name|plugin
range|:
name|pluginsService
operator|.
name|services
argument_list|()
control|)
block|{
name|injector
operator|.
name|getInstance
argument_list|(
name|plugin
argument_list|)
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
name|logger
operator|.
name|info
argument_list|(
literal|"{{}}[{}]: stopped"
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|,
name|JvmInfo
operator|.
name|jvmInfo
argument_list|()
operator|.
name|pid
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
block|{
if|if
condition|(
name|lifecycle
operator|.
name|started
argument_list|()
condition|)
block|{
name|stop
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|lifecycle
operator|.
name|moveToClosed
argument_list|()
condition|)
block|{
return|return;
block|}
name|ESLogger
name|logger
init|=
name|Loggers
operator|.
name|getLogger
argument_list|(
name|Node
operator|.
name|class
argument_list|,
name|settings
operator|.
name|get
argument_list|(
literal|"name"
argument_list|)
argument_list|)
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"{{}}[{}]: closing ..."
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|,
name|JvmInfo
operator|.
name|jvmInfo
argument_list|()
operator|.
name|pid
argument_list|()
argument_list|)
expr_stmt|;
name|StopWatch
name|stopWatch
init|=
operator|new
name|StopWatch
argument_list|(
literal|"node_close"
argument_list|)
decl_stmt|;
name|stopWatch
operator|.
name|start
argument_list|(
literal|"http"
argument_list|)
expr_stmt|;
if|if
condition|(
name|settings
operator|.
name|getAsBoolean
argument_list|(
literal|"http.enabled"
argument_list|,
literal|true
argument_list|)
condition|)
block|{
name|injector
operator|.
name|getInstance
argument_list|(
name|HttpServer
operator|.
name|class
argument_list|)
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|stopWatch
operator|.
name|stop
argument_list|()
operator|.
name|start
argument_list|(
literal|"rivers"
argument_list|)
expr_stmt|;
name|injector
operator|.
name|getInstance
argument_list|(
name|RiversManager
operator|.
name|class
argument_list|)
operator|.
name|close
argument_list|()
expr_stmt|;
name|stopWatch
operator|.
name|stop
argument_list|()
operator|.
name|start
argument_list|(
literal|"client"
argument_list|)
expr_stmt|;
name|injector
operator|.
name|getInstance
argument_list|(
name|Client
operator|.
name|class
argument_list|)
operator|.
name|close
argument_list|()
expr_stmt|;
name|stopWatch
operator|.
name|stop
argument_list|()
operator|.
name|start
argument_list|(
literal|"indices_cluster"
argument_list|)
expr_stmt|;
name|injector
operator|.
name|getInstance
argument_list|(
name|IndicesClusterStateService
operator|.
name|class
argument_list|)
operator|.
name|close
argument_list|()
expr_stmt|;
name|stopWatch
operator|.
name|stop
argument_list|()
operator|.
name|start
argument_list|(
literal|"indices"
argument_list|)
expr_stmt|;
name|injector
operator|.
name|getInstance
argument_list|(
name|IndicesNodeFilterCache
operator|.
name|class
argument_list|)
operator|.
name|close
argument_list|()
expr_stmt|;
name|injector
operator|.
name|getInstance
argument_list|(
name|IndexingMemoryBufferController
operator|.
name|class
argument_list|)
operator|.
name|close
argument_list|()
expr_stmt|;
name|injector
operator|.
name|getInstance
argument_list|(
name|IndicesTTLService
operator|.
name|class
argument_list|)
operator|.
name|close
argument_list|()
expr_stmt|;
name|injector
operator|.
name|getInstance
argument_list|(
name|IndicesService
operator|.
name|class
argument_list|)
operator|.
name|close
argument_list|()
expr_stmt|;
name|stopWatch
operator|.
name|stop
argument_list|()
operator|.
name|start
argument_list|(
literal|"routing"
argument_list|)
expr_stmt|;
name|injector
operator|.
name|getInstance
argument_list|(
name|RoutingService
operator|.
name|class
argument_list|)
operator|.
name|close
argument_list|()
expr_stmt|;
name|stopWatch
operator|.
name|stop
argument_list|()
operator|.
name|start
argument_list|(
literal|"cluster"
argument_list|)
expr_stmt|;
name|injector
operator|.
name|getInstance
argument_list|(
name|ClusterService
operator|.
name|class
argument_list|)
operator|.
name|close
argument_list|()
expr_stmt|;
name|stopWatch
operator|.
name|stop
argument_list|()
operator|.
name|start
argument_list|(
literal|"discovery"
argument_list|)
expr_stmt|;
name|injector
operator|.
name|getInstance
argument_list|(
name|DiscoveryService
operator|.
name|class
argument_list|)
operator|.
name|close
argument_list|()
expr_stmt|;
name|stopWatch
operator|.
name|stop
argument_list|()
operator|.
name|start
argument_list|(
literal|"monitor"
argument_list|)
expr_stmt|;
name|injector
operator|.
name|getInstance
argument_list|(
name|MonitorService
operator|.
name|class
argument_list|)
operator|.
name|close
argument_list|()
expr_stmt|;
name|stopWatch
operator|.
name|stop
argument_list|()
operator|.
name|start
argument_list|(
literal|"gateway"
argument_list|)
expr_stmt|;
name|injector
operator|.
name|getInstance
argument_list|(
name|GatewayService
operator|.
name|class
argument_list|)
operator|.
name|close
argument_list|()
expr_stmt|;
name|stopWatch
operator|.
name|stop
argument_list|()
operator|.
name|start
argument_list|(
literal|"search"
argument_list|)
expr_stmt|;
name|injector
operator|.
name|getInstance
argument_list|(
name|SearchService
operator|.
name|class
argument_list|)
operator|.
name|close
argument_list|()
expr_stmt|;
name|stopWatch
operator|.
name|stop
argument_list|()
operator|.
name|start
argument_list|(
literal|"rest"
argument_list|)
expr_stmt|;
name|injector
operator|.
name|getInstance
argument_list|(
name|RestController
operator|.
name|class
argument_list|)
operator|.
name|close
argument_list|()
expr_stmt|;
name|stopWatch
operator|.
name|stop
argument_list|()
operator|.
name|start
argument_list|(
literal|"transport"
argument_list|)
expr_stmt|;
name|injector
operator|.
name|getInstance
argument_list|(
name|TransportService
operator|.
name|class
argument_list|)
operator|.
name|close
argument_list|()
expr_stmt|;
for|for
control|(
name|Class
argument_list|<
name|?
extends|extends
name|LifecycleComponent
argument_list|>
name|plugin
range|:
name|pluginsService
operator|.
name|services
argument_list|()
control|)
block|{
name|stopWatch
operator|.
name|stop
argument_list|()
operator|.
name|start
argument_list|(
literal|"plugin("
operator|+
name|plugin
operator|.
name|getName
argument_list|()
operator|+
literal|")"
argument_list|)
expr_stmt|;
name|injector
operator|.
name|getInstance
argument_list|(
name|plugin
argument_list|)
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|stopWatch
operator|.
name|stop
argument_list|()
operator|.
name|start
argument_list|(
literal|"node_cache"
argument_list|)
expr_stmt|;
name|injector
operator|.
name|getInstance
argument_list|(
name|NodeCache
operator|.
name|class
argument_list|)
operator|.
name|close
argument_list|()
expr_stmt|;
name|stopWatch
operator|.
name|stop
argument_list|()
operator|.
name|start
argument_list|(
literal|"script"
argument_list|)
expr_stmt|;
name|injector
operator|.
name|getInstance
argument_list|(
name|ScriptService
operator|.
name|class
argument_list|)
operator|.
name|close
argument_list|()
expr_stmt|;
name|stopWatch
operator|.
name|stop
argument_list|()
operator|.
name|start
argument_list|(
literal|"thread_pool"
argument_list|)
expr_stmt|;
name|injector
operator|.
name|getInstance
argument_list|(
name|ThreadPool
operator|.
name|class
argument_list|)
operator|.
name|shutdown
argument_list|()
expr_stmt|;
try|try
block|{
name|injector
operator|.
name|getInstance
argument_list|(
name|ThreadPool
operator|.
name|class
argument_list|)
operator|.
name|awaitTermination
argument_list|(
literal|10
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
comment|// ignore
block|}
name|stopWatch
operator|.
name|stop
argument_list|()
operator|.
name|start
argument_list|(
literal|"thread_pool_force_shutdown"
argument_list|)
expr_stmt|;
try|try
block|{
name|injector
operator|.
name|getInstance
argument_list|(
name|ThreadPool
operator|.
name|class
argument_list|)
operator|.
name|shutdownNow
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// ignore
block|}
name|stopWatch
operator|.
name|stop
argument_list|()
expr_stmt|;
name|CacheRecycler
operator|.
name|clear
argument_list|()
expr_stmt|;
name|CachedStreams
operator|.
name|clear
argument_list|()
expr_stmt|;
name|ThreadLocals
operator|.
name|clearReferencesThreadLocals
argument_list|()
expr_stmt|;
if|if
condition|(
name|logger
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"Close times for each service:\n{}"
argument_list|,
name|stopWatch
operator|.
name|prettyPrint
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|injector
operator|.
name|getInstance
argument_list|(
name|NodeEnvironment
operator|.
name|class
argument_list|)
operator|.
name|close
argument_list|()
expr_stmt|;
name|Injectors
operator|.
name|close
argument_list|(
name|injector
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"{{}}[{}]: closed"
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|,
name|JvmInfo
operator|.
name|jvmInfo
argument_list|()
operator|.
name|pid
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|isClosed
specifier|public
name|boolean
name|isClosed
parameter_list|()
block|{
return|return
name|lifecycle
operator|.
name|closed
argument_list|()
return|;
block|}
DECL|method|injector
specifier|public
name|Injector
name|injector
parameter_list|()
block|{
return|return
name|this
operator|.
name|injector
return|;
block|}
DECL|method|main
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|Exception
block|{
specifier|final
name|InternalNode
name|node
init|=
operator|new
name|InternalNode
argument_list|()
decl_stmt|;
name|node
operator|.
name|start
argument_list|()
expr_stmt|;
name|Runtime
operator|.
name|getRuntime
argument_list|()
operator|.
name|addShutdownHook
argument_list|(
operator|new
name|Thread
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|node
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

