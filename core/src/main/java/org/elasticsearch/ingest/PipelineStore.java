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
name|elasticsearch
operator|.
name|action
operator|.
name|ActionListener
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
name|delete
operator|.
name|DeleteRequest
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
name|delete
operator|.
name|DeleteResponse
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
name|get
operator|.
name|GetRequest
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
name|get
operator|.
name|GetResponse
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
name|index
operator|.
name|IndexRequest
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
name|index
operator|.
name|IndexResponse
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
name|search
operator|.
name|SearchRequest
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
name|support
operator|.
name|IndicesOptions
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
name|SearchScrollIterator
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
name|bytes
operator|.
name|BytesReference
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
name|regex
operator|.
name|Regex
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
name|common
operator|.
name|xcontent
operator|.
name|XContentHelper
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
name|index
operator|.
name|IndexNotFoundException
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
name|ingest
operator|.
name|DeletePipelineRequest
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
name|ingest
operator|.
name|PutPipelineRequest
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
name|ingest
operator|.
name|ReloadPipelinesAction
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
name|core
operator|.
name|Pipeline
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
name|core
operator|.
name|Processor
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
name|core
operator|.
name|TemplateService
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
name|SearchHit
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
name|builder
operator|.
name|SearchSourceBuilder
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
name|sort
operator|.
name|SortOrder
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
name|BiFunction
import|;
end_import

begin_class
DECL|class|PipelineStore
specifier|public
class|class
name|PipelineStore
extends|extends
name|AbstractComponent
implements|implements
name|Closeable
block|{
DECL|field|INDEX
specifier|public
specifier|final
specifier|static
name|String
name|INDEX
init|=
literal|".ingest"
decl_stmt|;
DECL|field|TYPE
specifier|public
specifier|final
specifier|static
name|String
name|TYPE
init|=
literal|"pipeline"
decl_stmt|;
DECL|field|client
specifier|private
name|Client
name|client
decl_stmt|;
DECL|field|scrollTimeout
specifier|private
specifier|final
name|TimeValue
name|scrollTimeout
decl_stmt|;
DECL|field|reloadPipelinesAction
specifier|private
specifier|final
name|ReloadPipelinesAction
name|reloadPipelinesAction
decl_stmt|;
DECL|field|factory
specifier|private
specifier|final
name|Pipeline
operator|.
name|Factory
name|factory
init|=
operator|new
name|Pipeline
operator|.
name|Factory
argument_list|()
decl_stmt|;
DECL|field|processorFactoryRegistry
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|Processor
operator|.
name|Factory
argument_list|>
name|processorFactoryRegistry
decl_stmt|;
DECL|field|started
specifier|private
specifier|volatile
name|boolean
name|started
init|=
literal|false
decl_stmt|;
DECL|field|pipelines
specifier|private
specifier|volatile
name|Map
argument_list|<
name|String
argument_list|,
name|PipelineDefinition
argument_list|>
name|pipelines
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
DECL|method|PipelineStore
specifier|public
name|PipelineStore
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ClusterService
name|clusterService
parameter_list|,
name|TransportService
name|transportService
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|scrollTimeout
operator|=
name|settings
operator|.
name|getAsTime
argument_list|(
literal|"ingest.pipeline.store.scroll.timeout"
argument_list|,
name|TimeValue
operator|.
name|timeValueSeconds
argument_list|(
literal|30
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|reloadPipelinesAction
operator|=
operator|new
name|ReloadPipelinesAction
argument_list|(
name|settings
argument_list|,
name|this
argument_list|,
name|clusterService
argument_list|,
name|transportService
argument_list|)
expr_stmt|;
block|}
DECL|method|setClient
specifier|public
name|void
name|setClient
parameter_list|(
name|Client
name|client
parameter_list|)
block|{
name|this
operator|.
name|client
operator|=
name|client
expr_stmt|;
block|}
DECL|method|buildProcessorFactoryRegistry
specifier|public
name|void
name|buildProcessorFactoryRegistry
parameter_list|(
name|ProcessorsRegistry
name|processorsRegistry
parameter_list|,
name|Environment
name|environment
parameter_list|,
name|ScriptService
name|scriptService
parameter_list|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Processor
operator|.
name|Factory
argument_list|>
name|processorFactories
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|TemplateService
name|templateService
init|=
operator|new
name|InternalTemplateService
argument_list|(
name|scriptService
argument_list|)
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|BiFunction
argument_list|<
name|Environment
argument_list|,
name|TemplateService
argument_list|,
name|Processor
operator|.
name|Factory
argument_list|<
name|?
argument_list|>
argument_list|>
argument_list|>
name|entry
range|:
name|processorsRegistry
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|Processor
operator|.
name|Factory
name|processorFactory
init|=
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|apply
argument_list|(
name|environment
argument_list|,
name|templateService
argument_list|)
decl_stmt|;
name|processorFactories
operator|.
name|put
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|processorFactory
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|processorFactoryRegistry
operator|=
name|Collections
operator|.
name|unmodifiableMap
argument_list|(
name|processorFactories
argument_list|)
expr_stmt|;
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
name|stop
argument_list|(
literal|"closing"
argument_list|)
expr_stmt|;
comment|// TODO: When org.elasticsearch.node.Node can close Closable instances we should try to remove this code,
comment|// since any wired closable should be able to close itself
name|List
argument_list|<
name|Closeable
argument_list|>
name|closeables
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|Processor
operator|.
name|Factory
name|factory
range|:
name|processorFactoryRegistry
operator|.
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|factory
operator|instanceof
name|Closeable
condition|)
block|{
name|closeables
operator|.
name|add
argument_list|(
operator|(
name|Closeable
operator|)
name|factory
argument_list|)
expr_stmt|;
block|}
block|}
name|IOUtils
operator|.
name|close
argument_list|(
name|closeables
argument_list|)
expr_stmt|;
block|}
comment|/**      * Deletes the pipeline specified by id in the request.      */
DECL|method|delete
specifier|public
name|void
name|delete
parameter_list|(
name|DeletePipelineRequest
name|request
parameter_list|,
name|ActionListener
argument_list|<
name|DeleteResponse
argument_list|>
name|listener
parameter_list|)
block|{
name|ensureReady
argument_list|()
expr_stmt|;
name|DeleteRequest
name|deleteRequest
init|=
operator|new
name|DeleteRequest
argument_list|(
name|request
argument_list|)
decl_stmt|;
name|deleteRequest
operator|.
name|index
argument_list|(
name|PipelineStore
operator|.
name|INDEX
argument_list|)
expr_stmt|;
name|deleteRequest
operator|.
name|type
argument_list|(
name|PipelineStore
operator|.
name|TYPE
argument_list|)
expr_stmt|;
name|deleteRequest
operator|.
name|id
argument_list|(
name|request
operator|.
name|id
argument_list|()
argument_list|)
expr_stmt|;
name|deleteRequest
operator|.
name|refresh
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|client
operator|.
name|delete
argument_list|(
name|deleteRequest
argument_list|,
name|handleWriteResponseAndReloadPipelines
argument_list|(
name|listener
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**      * Stores the specified pipeline definition in the request.      *      * @throws IllegalArgumentException If the pipeline holds incorrect configuration      */
DECL|method|put
specifier|public
name|void
name|put
parameter_list|(
name|PutPipelineRequest
name|request
parameter_list|,
name|ActionListener
argument_list|<
name|IndexResponse
argument_list|>
name|listener
parameter_list|)
throws|throws
name|IllegalArgumentException
block|{
name|ensureReady
argument_list|()
expr_stmt|;
try|try
block|{
comment|// validates the pipeline and processor configuration:
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|pipelineConfig
init|=
name|XContentHelper
operator|.
name|convertToMap
argument_list|(
name|request
operator|.
name|source
argument_list|()
argument_list|,
literal|false
argument_list|)
operator|.
name|v2
argument_list|()
decl_stmt|;
name|constructPipeline
argument_list|(
name|request
operator|.
name|id
argument_list|()
argument_list|,
name|pipelineConfig
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Invalid pipeline configuration"
argument_list|,
name|e
argument_list|)
throw|;
block|}
name|IndexRequest
name|indexRequest
init|=
operator|new
name|IndexRequest
argument_list|(
name|request
argument_list|)
decl_stmt|;
name|indexRequest
operator|.
name|index
argument_list|(
name|PipelineStore
operator|.
name|INDEX
argument_list|)
expr_stmt|;
name|indexRequest
operator|.
name|type
argument_list|(
name|PipelineStore
operator|.
name|TYPE
argument_list|)
expr_stmt|;
name|indexRequest
operator|.
name|id
argument_list|(
name|request
operator|.
name|id
argument_list|()
argument_list|)
expr_stmt|;
name|indexRequest
operator|.
name|source
argument_list|(
name|request
operator|.
name|source
argument_list|()
argument_list|)
expr_stmt|;
name|indexRequest
operator|.
name|refresh
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|client
operator|.
name|index
argument_list|(
name|indexRequest
argument_list|,
name|handleWriteResponseAndReloadPipelines
argument_list|(
name|listener
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**      * Returns the pipeline by the specified id      */
DECL|method|get
specifier|public
name|Pipeline
name|get
parameter_list|(
name|String
name|id
parameter_list|)
block|{
name|ensureReady
argument_list|()
expr_stmt|;
name|PipelineDefinition
name|ref
init|=
name|pipelines
operator|.
name|get
argument_list|(
name|id
argument_list|)
decl_stmt|;
if|if
condition|(
name|ref
operator|!=
literal|null
condition|)
block|{
return|return
name|ref
operator|.
name|getPipeline
argument_list|()
return|;
block|}
else|else
block|{
return|return
literal|null
return|;
block|}
block|}
DECL|method|getProcessorFactoryRegistry
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|Processor
operator|.
name|Factory
argument_list|>
name|getProcessorFactoryRegistry
parameter_list|()
block|{
return|return
name|processorFactoryRegistry
return|;
block|}
DECL|method|getReference
specifier|public
name|List
argument_list|<
name|PipelineDefinition
argument_list|>
name|getReference
parameter_list|(
name|String
modifier|...
name|ids
parameter_list|)
block|{
name|ensureReady
argument_list|()
expr_stmt|;
name|List
argument_list|<
name|PipelineDefinition
argument_list|>
name|result
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|ids
operator|.
name|length
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|id
range|:
name|ids
control|)
block|{
if|if
condition|(
name|Regex
operator|.
name|isSimpleMatchPattern
argument_list|(
name|id
argument_list|)
condition|)
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|PipelineDefinition
argument_list|>
name|entry
range|:
name|pipelines
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
name|Regex
operator|.
name|simpleMatch
argument_list|(
name|id
argument_list|,
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
condition|)
block|{
name|result
operator|.
name|add
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
name|PipelineDefinition
name|reference
init|=
name|pipelines
operator|.
name|get
argument_list|(
name|id
argument_list|)
decl_stmt|;
if|if
condition|(
name|reference
operator|!=
literal|null
condition|)
block|{
name|result
operator|.
name|add
argument_list|(
name|reference
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|result
return|;
block|}
DECL|method|updatePipelines
specifier|public
specifier|synchronized
name|void
name|updatePipelines
parameter_list|()
throws|throws
name|Exception
block|{
comment|// note: this process isn't fast or smart, but the idea is that there will not be many pipelines,
comment|// so for that reason the goal is to keep the update logic simple.
name|int
name|changed
init|=
literal|0
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|PipelineDefinition
argument_list|>
name|newPipelines
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|(
name|pipelines
argument_list|)
decl_stmt|;
for|for
control|(
name|SearchHit
name|hit
range|:
name|readAllPipelines
argument_list|()
control|)
block|{
name|String
name|pipelineId
init|=
name|hit
operator|.
name|getId
argument_list|()
decl_stmt|;
name|BytesReference
name|pipelineSource
init|=
name|hit
operator|.
name|getSourceRef
argument_list|()
decl_stmt|;
name|PipelineDefinition
name|current
init|=
name|newPipelines
operator|.
name|get
argument_list|(
name|pipelineId
argument_list|)
decl_stmt|;
if|if
condition|(
name|current
operator|!=
literal|null
condition|)
block|{
comment|// If we first read from a primary shard copy and then from a replica copy,
comment|// and a write did not yet make it into the replica shard
comment|// then the source is not equal but we don't update because the current pipeline is the latest:
if|if
condition|(
name|current
operator|.
name|getVersion
argument_list|()
operator|>
name|hit
operator|.
name|getVersion
argument_list|()
condition|)
block|{
continue|continue;
block|}
if|if
condition|(
name|current
operator|.
name|getSource
argument_list|()
operator|.
name|equals
argument_list|(
name|pipelineSource
argument_list|)
condition|)
block|{
continue|continue;
block|}
block|}
name|changed
operator|++
expr_stmt|;
name|Pipeline
name|pipeline
init|=
name|constructPipeline
argument_list|(
name|hit
operator|.
name|getId
argument_list|()
argument_list|,
name|hit
operator|.
name|sourceAsMap
argument_list|()
argument_list|)
decl_stmt|;
name|newPipelines
operator|.
name|put
argument_list|(
name|pipelineId
argument_list|,
operator|new
name|PipelineDefinition
argument_list|(
name|pipeline
argument_list|,
name|hit
operator|.
name|getVersion
argument_list|()
argument_list|,
name|pipelineSource
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|int
name|removed
init|=
literal|0
decl_stmt|;
for|for
control|(
name|String
name|existingPipelineId
range|:
name|pipelines
operator|.
name|keySet
argument_list|()
control|)
block|{
if|if
condition|(
name|pipelineExists
argument_list|(
name|existingPipelineId
argument_list|)
operator|==
literal|false
condition|)
block|{
name|newPipelines
operator|.
name|remove
argument_list|(
name|existingPipelineId
argument_list|)
expr_stmt|;
name|removed
operator|++
expr_stmt|;
block|}
block|}
if|if
condition|(
name|changed
operator|!=
literal|0
operator|||
name|removed
operator|!=
literal|0
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"adding or updating [{}] pipelines and [{}] pipelines removed"
argument_list|,
name|changed
argument_list|,
name|removed
argument_list|)
expr_stmt|;
name|pipelines
operator|=
name|newPipelines
expr_stmt|;
block|}
else|else
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"no pipelines changes detected"
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|constructPipeline
specifier|private
name|Pipeline
name|constructPipeline
parameter_list|(
name|String
name|id
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|config
parameter_list|)
throws|throws
name|Exception
block|{
return|return
name|factory
operator|.
name|create
argument_list|(
name|id
argument_list|,
name|config
argument_list|,
name|processorFactoryRegistry
argument_list|)
return|;
block|}
DECL|method|pipelineExists
name|boolean
name|pipelineExists
parameter_list|(
name|String
name|pipelineId
parameter_list|)
block|{
name|GetRequest
name|request
init|=
operator|new
name|GetRequest
argument_list|(
name|PipelineStore
operator|.
name|INDEX
argument_list|,
name|PipelineStore
operator|.
name|TYPE
argument_list|,
name|pipelineId
argument_list|)
decl_stmt|;
try|try
block|{
name|GetResponse
name|response
init|=
name|client
operator|.
name|get
argument_list|(
name|request
argument_list|)
operator|.
name|actionGet
argument_list|()
decl_stmt|;
return|return
name|response
operator|.
name|isExists
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|IndexNotFoundException
name|e
parameter_list|)
block|{
comment|// the ingest index doesn't exist, so the pipeline doesn't either:
return|return
literal|false
return|;
block|}
block|}
DECL|method|start
specifier|synchronized
name|void
name|start
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
name|started
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"Pipeline already started"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|updatePipelines
argument_list|()
expr_stmt|;
name|started
operator|=
literal|true
expr_stmt|;
name|logger
operator|.
name|debug
argument_list|(
literal|"Pipeline store started with [{}] pipelines"
argument_list|,
name|pipelines
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|stop
specifier|synchronized
name|void
name|stop
parameter_list|(
name|String
name|reason
parameter_list|)
block|{
if|if
condition|(
name|started
condition|)
block|{
name|started
operator|=
literal|false
expr_stmt|;
name|pipelines
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
expr_stmt|;
name|logger
operator|.
name|debug
argument_list|(
literal|"Pipeline store stopped, reason [{}]"
argument_list|,
name|reason
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"Pipeline alreadt stopped"
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|isStarted
specifier|public
name|boolean
name|isStarted
parameter_list|()
block|{
return|return
name|started
return|;
block|}
DECL|method|readAllPipelines
specifier|private
name|Iterable
argument_list|<
name|SearchHit
argument_list|>
name|readAllPipelines
parameter_list|()
block|{
comment|// TODO: the search should be replaced with an ingest API when it is available
name|SearchSourceBuilder
name|sourceBuilder
init|=
operator|new
name|SearchSourceBuilder
argument_list|()
decl_stmt|;
name|sourceBuilder
operator|.
name|version
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|sourceBuilder
operator|.
name|sort
argument_list|(
literal|"_doc"
argument_list|,
name|SortOrder
operator|.
name|ASC
argument_list|)
expr_stmt|;
name|SearchRequest
name|searchRequest
init|=
operator|new
name|SearchRequest
argument_list|(
name|PipelineStore
operator|.
name|INDEX
argument_list|)
decl_stmt|;
name|searchRequest
operator|.
name|source
argument_list|(
name|sourceBuilder
argument_list|)
expr_stmt|;
name|searchRequest
operator|.
name|indicesOptions
argument_list|(
name|IndicesOptions
operator|.
name|lenientExpandOpen
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|SearchScrollIterator
operator|.
name|createIterator
argument_list|(
name|client
argument_list|,
name|scrollTimeout
argument_list|,
name|searchRequest
argument_list|)
return|;
block|}
DECL|method|ensureReady
specifier|private
name|void
name|ensureReady
parameter_list|()
block|{
if|if
condition|(
name|started
operator|==
literal|false
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"pipeline store isn't ready yet"
argument_list|)
throw|;
block|}
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|method|handleWriteResponseAndReloadPipelines
specifier|private
parameter_list|<
name|T
parameter_list|>
name|ActionListener
argument_list|<
name|T
argument_list|>
name|handleWriteResponseAndReloadPipelines
parameter_list|(
name|ActionListener
argument_list|<
name|T
argument_list|>
name|listener
parameter_list|)
block|{
return|return
operator|new
name|ActionListener
argument_list|<
name|T
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onResponse
parameter_list|(
name|T
name|result
parameter_list|)
block|{
try|try
block|{
name|reloadPipelinesAction
operator|.
name|reloadPipelinesOnAllNodes
argument_list|(
name|reloadResult
lambda|->
name|listener
operator|.
name|onResponse
argument_list|(
name|result
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|listener
operator|.
name|onFailure
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|onFailure
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|listener
operator|.
name|onFailure
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
return|;
block|}
block|}
end_class

end_unit

