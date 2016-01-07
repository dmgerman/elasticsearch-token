begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.ingest
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
package|;
end_package

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
name|template
operator|.
name|put
operator|.
name|PutIndexTemplateAction
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
name|template
operator|.
name|put
operator|.
name|PutIndexTemplateRequest
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
name|ClusterState
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
name|ClusterStateListener
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
name|metadata
operator|.
name|MetaData
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
name|IndexRoutingTable
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
name|AbstractLifecycleComponent
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
name|io
operator|.
name|Streams
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
name|BytesStreamOutput
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
name|concurrent
operator|.
name|EsRejectedExecutionException
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
name|io
operator|.
name|InputStream
import|;
end_import

begin_comment
comment|/**  * Instantiates and wires all the services that the ingest plugin will be needing.  * Also the bootstrapper is in charge of starting and stopping the ingest plugin based on the cluster state.  */
end_comment

begin_class
DECL|class|IngestBootstrapper
specifier|public
class|class
name|IngestBootstrapper
extends|extends
name|AbstractLifecycleComponent
implements|implements
name|ClusterStateListener
block|{
DECL|field|INGEST_INDEX_TEMPLATE_NAME
specifier|static
specifier|final
name|String
name|INGEST_INDEX_TEMPLATE_NAME
init|=
literal|"ingest-template"
decl_stmt|;
DECL|field|threadPool
specifier|private
specifier|final
name|ThreadPool
name|threadPool
decl_stmt|;
DECL|field|environment
specifier|private
specifier|final
name|Environment
name|environment
decl_stmt|;
DECL|field|pipelineStore
specifier|private
specifier|final
name|PipelineStore
name|pipelineStore
decl_stmt|;
DECL|field|pipelineExecutionService
specifier|private
specifier|final
name|PipelineExecutionService
name|pipelineExecutionService
decl_stmt|;
DECL|field|processorsRegistry
specifier|private
specifier|final
name|ProcessorsRegistry
name|processorsRegistry
decl_stmt|;
annotation|@
name|Inject
DECL|method|IngestBootstrapper
specifier|public
name|IngestBootstrapper
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|,
name|Environment
name|environment
parameter_list|,
name|ClusterService
name|clusterService
parameter_list|,
name|TransportService
name|transportService
parameter_list|,
name|ProcessorsRegistry
name|processorsRegistry
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
name|environment
operator|=
name|environment
expr_stmt|;
name|this
operator|.
name|processorsRegistry
operator|=
name|processorsRegistry
expr_stmt|;
name|this
operator|.
name|pipelineStore
operator|=
operator|new
name|PipelineStore
argument_list|(
name|settings
argument_list|,
name|clusterService
argument_list|,
name|transportService
argument_list|)
expr_stmt|;
name|this
operator|.
name|pipelineExecutionService
operator|=
operator|new
name|PipelineExecutionService
argument_list|(
name|pipelineStore
argument_list|,
name|threadPool
argument_list|)
expr_stmt|;
name|boolean
name|isNoTribeNode
init|=
name|settings
operator|.
name|getByPrefix
argument_list|(
literal|"tribe."
argument_list|)
operator|.
name|getAsMap
argument_list|()
operator|.
name|isEmpty
argument_list|()
decl_stmt|;
if|if
condition|(
name|isNoTribeNode
condition|)
block|{
name|clusterService
operator|.
name|add
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
block|}
comment|// for testing:
DECL|method|IngestBootstrapper
name|IngestBootstrapper
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|,
name|ClusterService
name|clusterService
parameter_list|,
name|PipelineStore
name|pipelineStore
parameter_list|,
name|PipelineExecutionService
name|pipelineExecutionService
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
name|environment
operator|=
literal|null
expr_stmt|;
name|clusterService
operator|.
name|add
argument_list|(
name|this
argument_list|)
expr_stmt|;
name|this
operator|.
name|pipelineStore
operator|=
name|pipelineStore
expr_stmt|;
name|this
operator|.
name|pipelineExecutionService
operator|=
name|pipelineExecutionService
expr_stmt|;
name|this
operator|.
name|processorsRegistry
operator|=
literal|null
expr_stmt|;
block|}
DECL|method|getPipelineStore
specifier|public
name|PipelineStore
name|getPipelineStore
parameter_list|()
block|{
return|return
name|pipelineStore
return|;
block|}
DECL|method|getPipelineExecutionService
specifier|public
name|PipelineExecutionService
name|getPipelineExecutionService
parameter_list|()
block|{
return|return
name|pipelineExecutionService
return|;
block|}
annotation|@
name|Inject
DECL|method|setClient
specifier|public
name|void
name|setClient
parameter_list|(
name|Client
name|client
parameter_list|)
block|{
name|pipelineStore
operator|.
name|setClient
argument_list|(
name|client
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Inject
DECL|method|setScriptService
specifier|public
name|void
name|setScriptService
parameter_list|(
name|ScriptService
name|scriptService
parameter_list|)
block|{
name|pipelineStore
operator|.
name|buildProcessorFactoryRegistry
argument_list|(
name|processorsRegistry
argument_list|,
name|environment
argument_list|,
name|scriptService
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|clusterChanged
specifier|public
name|void
name|clusterChanged
parameter_list|(
name|ClusterChangedEvent
name|event
parameter_list|)
block|{
name|ClusterState
name|state
init|=
name|event
operator|.
name|state
argument_list|()
decl_stmt|;
if|if
condition|(
name|state
operator|.
name|blocks
argument_list|()
operator|.
name|hasGlobalBlock
argument_list|(
name|GatewayService
operator|.
name|STATE_NOT_RECOVERED_BLOCK
argument_list|)
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|pipelineStore
operator|.
name|isStarted
argument_list|()
condition|)
block|{
if|if
condition|(
name|validClusterState
argument_list|(
name|state
argument_list|)
operator|==
literal|false
condition|)
block|{
name|stopPipelineStore
argument_list|(
literal|"cluster state invalid ["
operator|+
name|state
operator|+
literal|"]"
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
if|if
condition|(
name|validClusterState
argument_list|(
name|state
argument_list|)
condition|)
block|{
name|startPipelineStore
argument_list|(
name|state
operator|.
name|metaData
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|validClusterState
name|boolean
name|validClusterState
parameter_list|(
name|ClusterState
name|state
parameter_list|)
block|{
if|if
condition|(
name|state
operator|.
name|blocks
argument_list|()
operator|.
name|hasGlobalBlock
argument_list|(
name|DiscoverySettings
operator|.
name|NO_MASTER_BLOCK_WRITES
argument_list|)
operator|||
name|state
operator|.
name|blocks
argument_list|()
operator|.
name|hasGlobalBlock
argument_list|(
name|DiscoverySettings
operator|.
name|NO_MASTER_BLOCK_ALL
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|state
operator|.
name|getMetaData
argument_list|()
operator|.
name|hasConcreteIndex
argument_list|(
name|PipelineStore
operator|.
name|INDEX
argument_list|)
condition|)
block|{
name|IndexRoutingTable
name|routingTable
init|=
name|state
operator|.
name|getRoutingTable
argument_list|()
operator|.
name|index
argument_list|(
name|PipelineStore
operator|.
name|INDEX
argument_list|)
decl_stmt|;
return|return
name|routingTable
operator|.
name|allPrimaryShardsActive
argument_list|()
return|;
block|}
else|else
block|{
comment|// it will be ready when auto create index kicks in before the first pipeline doc gets added
return|return
literal|true
return|;
block|}
block|}
annotation|@
name|Override
DECL|method|doStart
specifier|protected
name|void
name|doStart
parameter_list|()
block|{     }
annotation|@
name|Override
DECL|method|doStop
specifier|protected
name|void
name|doStop
parameter_list|()
block|{     }
annotation|@
name|Override
DECL|method|doClose
specifier|protected
name|void
name|doClose
parameter_list|()
block|{
try|try
block|{
name|pipelineStore
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
DECL|method|startPipelineStore
name|void
name|startPipelineStore
parameter_list|(
name|MetaData
name|metaData
parameter_list|)
block|{
try|try
block|{
name|threadPool
operator|.
name|executor
argument_list|(
name|ThreadPool
operator|.
name|Names
operator|.
name|GENERIC
argument_list|)
operator|.
name|execute
argument_list|(
parameter_list|()
lambda|->
block|{
try|try
block|{
name|pipelineStore
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e1
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"pipeline store failed to start, retrying..."
argument_list|,
name|e1
argument_list|)
expr_stmt|;
name|startPipelineStore
argument_list|(
name|metaData
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|EsRejectedExecutionException
name|e
parameter_list|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"async pipeline store start failed"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|stopPipelineStore
name|void
name|stopPipelineStore
parameter_list|(
name|String
name|reason
parameter_list|)
block|{
try|try
block|{
name|threadPool
operator|.
name|executor
argument_list|(
name|ThreadPool
operator|.
name|Names
operator|.
name|GENERIC
argument_list|)
operator|.
name|execute
argument_list|(
parameter_list|()
lambda|->
block|{
try|try
block|{
name|pipelineStore
operator|.
name|stop
argument_list|(
name|reason
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
name|error
argument_list|(
literal|"pipeline store stop failure"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|EsRejectedExecutionException
name|e
parameter_list|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"async pipeline store stop failed"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

