begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.node.service
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|node
operator|.
name|service
package|;
end_package

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
name|org
operator|.
name|elasticsearch
operator|.
name|Build
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
name|admin
operator|.
name|cluster
operator|.
name|node
operator|.
name|info
operator|.
name|NodeInfo
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
name|admin
operator|.
name|cluster
operator|.
name|node
operator|.
name|stats
operator|.
name|NodeStats
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
name|admin
operator|.
name|indices
operator|.
name|stats
operator|.
name|CommonStatsFlags
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
name|ClusterService
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
name|SettingsFilter
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
name|breaker
operator|.
name|CircuitBreakerService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|IngestService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|ProcessorsRegistry
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
comment|/**  */
end_comment

begin_class
DECL|class|NodeService
specifier|public
class|class
name|NodeService
extends|extends
name|AbstractComponent
implements|implements
name|Closeable
block|{
DECL|field|threadPool
specifier|private
specifier|final
name|ThreadPool
name|threadPool
decl_stmt|;
DECL|field|monitorService
specifier|private
specifier|final
name|MonitorService
name|monitorService
decl_stmt|;
DECL|field|transportService
specifier|private
specifier|final
name|TransportService
name|transportService
decl_stmt|;
DECL|field|indicesService
specifier|private
specifier|final
name|IndicesService
name|indicesService
decl_stmt|;
DECL|field|pluginService
specifier|private
specifier|final
name|PluginsService
name|pluginService
decl_stmt|;
DECL|field|circuitBreakerService
specifier|private
specifier|final
name|CircuitBreakerService
name|circuitBreakerService
decl_stmt|;
DECL|field|ingestService
specifier|private
specifier|final
name|IngestService
name|ingestService
decl_stmt|;
DECL|field|settingsFilter
specifier|private
specifier|final
name|SettingsFilter
name|settingsFilter
decl_stmt|;
DECL|field|clusterService
specifier|private
name|ClusterService
name|clusterService
decl_stmt|;
DECL|field|scriptService
specifier|private
name|ScriptService
name|scriptService
decl_stmt|;
DECL|field|httpServer
specifier|private
specifier|final
name|HttpServer
name|httpServer
decl_stmt|;
DECL|field|discovery
specifier|private
specifier|final
name|Discovery
name|discovery
decl_stmt|;
annotation|@
name|Inject
DECL|method|NodeService
specifier|public
name|NodeService
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|,
name|MonitorService
name|monitorService
parameter_list|,
name|Discovery
name|discovery
parameter_list|,
name|TransportService
name|transportService
parameter_list|,
name|IndicesService
name|indicesService
parameter_list|,
name|PluginsService
name|pluginService
parameter_list|,
name|CircuitBreakerService
name|circuitBreakerService
parameter_list|,
name|HttpServer
name|httpServer
parameter_list|,
name|ProcessorsRegistry
operator|.
name|Builder
name|processorsRegistryBuilder
parameter_list|,
name|ClusterService
name|clusterService
parameter_list|,
name|SettingsFilter
name|settingsFilter
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|threadPool
operator|=
name|threadPool
expr_stmt|;
name|this
operator|.
name|monitorService
operator|=
name|monitorService
expr_stmt|;
name|this
operator|.
name|transportService
operator|=
name|transportService
expr_stmt|;
name|this
operator|.
name|indicesService
operator|=
name|indicesService
expr_stmt|;
name|this
operator|.
name|discovery
operator|=
name|discovery
expr_stmt|;
name|this
operator|.
name|pluginService
operator|=
name|pluginService
expr_stmt|;
name|this
operator|.
name|circuitBreakerService
operator|=
name|circuitBreakerService
expr_stmt|;
name|this
operator|.
name|httpServer
operator|=
name|httpServer
expr_stmt|;
name|this
operator|.
name|clusterService
operator|=
name|clusterService
expr_stmt|;
name|this
operator|.
name|ingestService
operator|=
operator|new
name|IngestService
argument_list|(
name|settings
argument_list|,
name|threadPool
argument_list|,
name|processorsRegistryBuilder
argument_list|)
expr_stmt|;
name|this
operator|.
name|settingsFilter
operator|=
name|settingsFilter
expr_stmt|;
name|clusterService
operator|.
name|add
argument_list|(
name|ingestService
operator|.
name|getPipelineStore
argument_list|()
argument_list|)
expr_stmt|;
name|clusterService
operator|.
name|add
argument_list|(
name|ingestService
operator|.
name|getPipelineExecutionService
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// can not use constructor injection or there will be a circular dependency
annotation|@
name|Inject
argument_list|(
name|optional
operator|=
literal|true
argument_list|)
DECL|method|setScriptService
specifier|public
name|void
name|setScriptService
parameter_list|(
name|ScriptService
name|scriptService
parameter_list|)
block|{
name|this
operator|.
name|scriptService
operator|=
name|scriptService
expr_stmt|;
name|this
operator|.
name|ingestService
operator|.
name|buildProcessorsFactoryRegistry
argument_list|(
name|scriptService
argument_list|,
name|clusterService
argument_list|)
expr_stmt|;
block|}
DECL|method|info
specifier|public
name|NodeInfo
name|info
parameter_list|()
block|{
return|return
operator|new
name|NodeInfo
argument_list|(
name|Version
operator|.
name|CURRENT
argument_list|,
name|Build
operator|.
name|CURRENT
argument_list|,
name|discovery
operator|.
name|localNode
argument_list|()
argument_list|,
name|settings
argument_list|,
name|monitorService
operator|.
name|osService
argument_list|()
operator|.
name|info
argument_list|()
argument_list|,
name|monitorService
operator|.
name|processService
argument_list|()
operator|.
name|info
argument_list|()
argument_list|,
name|monitorService
operator|.
name|jvmService
argument_list|()
operator|.
name|info
argument_list|()
argument_list|,
name|threadPool
operator|.
name|info
argument_list|()
argument_list|,
name|transportService
operator|.
name|info
argument_list|()
argument_list|,
name|httpServer
operator|.
name|info
argument_list|()
argument_list|,
name|pluginService
operator|==
literal|null
condition|?
literal|null
else|:
name|pluginService
operator|.
name|info
argument_list|()
argument_list|,
name|ingestService
operator|==
literal|null
condition|?
literal|null
else|:
name|ingestService
operator|.
name|info
argument_list|()
argument_list|,
name|indicesService
operator|.
name|getTotalIndexingBufferBytes
argument_list|()
argument_list|)
return|;
block|}
DECL|method|info
specifier|public
name|NodeInfo
name|info
parameter_list|(
name|boolean
name|settings
parameter_list|,
name|boolean
name|os
parameter_list|,
name|boolean
name|process
parameter_list|,
name|boolean
name|jvm
parameter_list|,
name|boolean
name|threadPool
parameter_list|,
name|boolean
name|transport
parameter_list|,
name|boolean
name|http
parameter_list|,
name|boolean
name|plugin
parameter_list|,
name|boolean
name|ingest
parameter_list|,
name|boolean
name|indices
parameter_list|)
block|{
return|return
operator|new
name|NodeInfo
argument_list|(
name|Version
operator|.
name|CURRENT
argument_list|,
name|Build
operator|.
name|CURRENT
argument_list|,
name|discovery
operator|.
name|localNode
argument_list|()
argument_list|,
name|settings
condition|?
name|settingsFilter
operator|.
name|filter
argument_list|(
name|this
operator|.
name|settings
argument_list|)
else|:
literal|null
argument_list|,
name|os
condition|?
name|monitorService
operator|.
name|osService
argument_list|()
operator|.
name|info
argument_list|()
else|:
literal|null
argument_list|,
name|process
condition|?
name|monitorService
operator|.
name|processService
argument_list|()
operator|.
name|info
argument_list|()
else|:
literal|null
argument_list|,
name|jvm
condition|?
name|monitorService
operator|.
name|jvmService
argument_list|()
operator|.
name|info
argument_list|()
else|:
literal|null
argument_list|,
name|threadPool
condition|?
name|this
operator|.
name|threadPool
operator|.
name|info
argument_list|()
else|:
literal|null
argument_list|,
name|transport
condition|?
name|transportService
operator|.
name|info
argument_list|()
else|:
literal|null
argument_list|,
name|http
condition|?
name|httpServer
operator|.
name|info
argument_list|()
else|:
literal|null
argument_list|,
name|plugin
condition|?
operator|(
name|pluginService
operator|==
literal|null
condition|?
literal|null
else|:
name|pluginService
operator|.
name|info
argument_list|()
operator|)
else|:
literal|null
argument_list|,
name|ingest
condition|?
operator|(
name|ingestService
operator|==
literal|null
condition|?
literal|null
else|:
name|ingestService
operator|.
name|info
argument_list|()
operator|)
else|:
literal|null
argument_list|,
name|indices
condition|?
name|indicesService
operator|.
name|getTotalIndexingBufferBytes
argument_list|()
else|:
literal|null
argument_list|)
return|;
block|}
DECL|method|stats
specifier|public
name|NodeStats
name|stats
parameter_list|()
throws|throws
name|IOException
block|{
comment|// for indices stats we want to include previous allocated shards stats as well (it will
comment|// only be applied to the sensible ones to use, like refresh/merge/flush/indexing stats)
return|return
operator|new
name|NodeStats
argument_list|(
name|discovery
operator|.
name|localNode
argument_list|()
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|,
name|indicesService
operator|.
name|stats
argument_list|(
literal|true
argument_list|)
argument_list|,
name|monitorService
operator|.
name|osService
argument_list|()
operator|.
name|stats
argument_list|()
argument_list|,
name|monitorService
operator|.
name|processService
argument_list|()
operator|.
name|stats
argument_list|()
argument_list|,
name|monitorService
operator|.
name|jvmService
argument_list|()
operator|.
name|stats
argument_list|()
argument_list|,
name|threadPool
operator|.
name|stats
argument_list|()
argument_list|,
name|monitorService
operator|.
name|fsService
argument_list|()
operator|.
name|stats
argument_list|()
argument_list|,
name|transportService
operator|.
name|stats
argument_list|()
argument_list|,
name|httpServer
operator|.
name|stats
argument_list|()
argument_list|,
name|circuitBreakerService
operator|.
name|stats
argument_list|()
argument_list|,
name|scriptService
operator|.
name|stats
argument_list|()
argument_list|,
name|discovery
operator|.
name|stats
argument_list|()
argument_list|,
name|ingestService
operator|.
name|getPipelineExecutionService
argument_list|()
operator|.
name|stats
argument_list|()
argument_list|)
return|;
block|}
DECL|method|stats
specifier|public
name|NodeStats
name|stats
parameter_list|(
name|CommonStatsFlags
name|indices
parameter_list|,
name|boolean
name|os
parameter_list|,
name|boolean
name|process
parameter_list|,
name|boolean
name|jvm
parameter_list|,
name|boolean
name|threadPool
parameter_list|,
name|boolean
name|fs
parameter_list|,
name|boolean
name|transport
parameter_list|,
name|boolean
name|http
parameter_list|,
name|boolean
name|circuitBreaker
parameter_list|,
name|boolean
name|script
parameter_list|,
name|boolean
name|discoveryStats
parameter_list|,
name|boolean
name|ingest
parameter_list|)
block|{
comment|// for indices stats we want to include previous allocated shards stats as well (it will
comment|// only be applied to the sensible ones to use, like refresh/merge/flush/indexing stats)
return|return
operator|new
name|NodeStats
argument_list|(
name|discovery
operator|.
name|localNode
argument_list|()
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|,
name|indices
operator|.
name|anySet
argument_list|()
condition|?
name|indicesService
operator|.
name|stats
argument_list|(
literal|true
argument_list|,
name|indices
argument_list|)
else|:
literal|null
argument_list|,
name|os
condition|?
name|monitorService
operator|.
name|osService
argument_list|()
operator|.
name|stats
argument_list|()
else|:
literal|null
argument_list|,
name|process
condition|?
name|monitorService
operator|.
name|processService
argument_list|()
operator|.
name|stats
argument_list|()
else|:
literal|null
argument_list|,
name|jvm
condition|?
name|monitorService
operator|.
name|jvmService
argument_list|()
operator|.
name|stats
argument_list|()
else|:
literal|null
argument_list|,
name|threadPool
condition|?
name|this
operator|.
name|threadPool
operator|.
name|stats
argument_list|()
else|:
literal|null
argument_list|,
name|fs
condition|?
name|monitorService
operator|.
name|fsService
argument_list|()
operator|.
name|stats
argument_list|()
else|:
literal|null
argument_list|,
name|transport
condition|?
name|transportService
operator|.
name|stats
argument_list|()
else|:
literal|null
argument_list|,
name|http
condition|?
name|httpServer
operator|.
name|stats
argument_list|()
else|:
literal|null
argument_list|,
name|circuitBreaker
condition|?
name|circuitBreakerService
operator|.
name|stats
argument_list|()
else|:
literal|null
argument_list|,
name|script
condition|?
name|scriptService
operator|.
name|stats
argument_list|()
else|:
literal|null
argument_list|,
name|discoveryStats
condition|?
name|discovery
operator|.
name|stats
argument_list|()
else|:
literal|null
argument_list|,
name|ingest
condition|?
name|ingestService
operator|.
name|getPipelineExecutionService
argument_list|()
operator|.
name|stats
argument_list|()
else|:
literal|null
argument_list|)
return|;
block|}
DECL|method|getIngestService
specifier|public
name|IngestService
name|getIngestService
parameter_list|()
block|{
return|return
name|ingestService
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
name|indicesService
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

