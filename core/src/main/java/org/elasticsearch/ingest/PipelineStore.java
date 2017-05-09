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
name|ElasticsearchParseException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ExceptionsHelper
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ResourceNotFoundException
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
name|WritePipelineResponse
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
name|AckedClusterStateUpdateTask
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
name|ClusterStateApplier
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
name|xcontent
operator|.
name|XContentHelper
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
name|HashSet
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
name|Set
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
name|ClusterStateApplier
block|{
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
DECL|field|processorFactories
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Processor
operator|.
name|Factory
argument_list|>
name|processorFactories
decl_stmt|;
DECL|field|newIngestDateFormat
specifier|private
specifier|volatile
name|boolean
name|newIngestDateFormat
decl_stmt|;
comment|// Ideally this should be in IngestMetadata class, but we don't have the processor factories around there.
comment|// We know of all the processor factories when a node with all its plugin have been initialized. Also some
comment|// processor factories rely on other node services. Custom metadata is statically registered when classes
comment|// are loaded, so in the cluster state we just save the pipeline config and here we keep the actual pipelines around.
DECL|field|pipelines
specifier|volatile
name|Map
argument_list|<
name|String
argument_list|,
name|Pipeline
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
name|ClusterSettings
name|clusterSettings
parameter_list|,
name|Settings
name|settings
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Processor
operator|.
name|Factory
argument_list|>
name|processorFactories
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|processorFactories
operator|=
name|processorFactories
expr_stmt|;
name|this
operator|.
name|newIngestDateFormat
operator|=
name|IngestService
operator|.
name|NEW_INGEST_DATE_FORMAT
operator|.
name|get
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|clusterSettings
operator|.
name|addSettingsUpdateConsumer
argument_list|(
name|IngestService
operator|.
name|NEW_INGEST_DATE_FORMAT
argument_list|,
name|this
operator|::
name|setNewIngestDateFormat
argument_list|)
expr_stmt|;
block|}
DECL|method|setNewIngestDateFormat
specifier|private
name|void
name|setNewIngestDateFormat
parameter_list|(
name|Boolean
name|newIngestDateFormat
parameter_list|)
block|{
name|this
operator|.
name|newIngestDateFormat
operator|=
name|newIngestDateFormat
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|applyClusterState
specifier|public
name|void
name|applyClusterState
parameter_list|(
name|ClusterChangedEvent
name|event
parameter_list|)
block|{
name|innerUpdatePipelines
argument_list|(
name|event
operator|.
name|previousState
argument_list|()
argument_list|,
name|event
operator|.
name|state
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|innerUpdatePipelines
name|void
name|innerUpdatePipelines
parameter_list|(
name|ClusterState
name|previousState
parameter_list|,
name|ClusterState
name|state
parameter_list|)
block|{
name|IngestMetadata
name|ingestMetadata
init|=
name|state
operator|.
name|getMetaData
argument_list|()
operator|.
name|custom
argument_list|(
name|IngestMetadata
operator|.
name|TYPE
argument_list|)
decl_stmt|;
name|IngestMetadata
name|previousIngestMetadata
init|=
name|previousState
operator|.
name|getMetaData
argument_list|()
operator|.
name|custom
argument_list|(
name|IngestMetadata
operator|.
name|TYPE
argument_list|)
decl_stmt|;
if|if
condition|(
name|Objects
operator|.
name|equals
argument_list|(
name|ingestMetadata
argument_list|,
name|previousIngestMetadata
argument_list|)
condition|)
block|{
return|return;
block|}
name|Map
argument_list|<
name|String
argument_list|,
name|Pipeline
argument_list|>
name|pipelines
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|PipelineConfiguration
name|pipeline
range|:
name|ingestMetadata
operator|.
name|getPipelines
argument_list|()
operator|.
name|values
argument_list|()
control|)
block|{
try|try
block|{
name|pipelines
operator|.
name|put
argument_list|(
name|pipeline
operator|.
name|getId
argument_list|()
argument_list|,
name|factory
operator|.
name|create
argument_list|(
name|pipeline
operator|.
name|getId
argument_list|()
argument_list|,
name|pipeline
operator|.
name|getConfigAsMap
argument_list|()
argument_list|,
name|processorFactories
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ElasticsearchParseException
name|e
parameter_list|)
block|{
throw|throw
name|e
throw|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticsearchParseException
argument_list|(
literal|"Error updating pipeline with id ["
operator|+
name|pipeline
operator|.
name|getId
argument_list|()
operator|+
literal|"]"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
name|this
operator|.
name|pipelines
operator|=
name|Collections
operator|.
name|unmodifiableMap
argument_list|(
name|pipelines
argument_list|)
expr_stmt|;
block|}
comment|/**      * Deletes the pipeline specified by id in the request.      */
DECL|method|delete
specifier|public
name|void
name|delete
parameter_list|(
name|ClusterService
name|clusterService
parameter_list|,
name|DeletePipelineRequest
name|request
parameter_list|,
name|ActionListener
argument_list|<
name|WritePipelineResponse
argument_list|>
name|listener
parameter_list|)
block|{
name|clusterService
operator|.
name|submitStateUpdateTask
argument_list|(
literal|"delete-pipeline-"
operator|+
name|request
operator|.
name|getId
argument_list|()
argument_list|,
operator|new
name|AckedClusterStateUpdateTask
argument_list|<
name|WritePipelineResponse
argument_list|>
argument_list|(
name|request
argument_list|,
name|listener
argument_list|)
block|{
annotation|@
name|Override
specifier|protected
name|WritePipelineResponse
name|newResponse
parameter_list|(
name|boolean
name|acknowledged
parameter_list|)
block|{
return|return
operator|new
name|WritePipelineResponse
argument_list|(
name|acknowledged
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|ClusterState
name|execute
parameter_list|(
name|ClusterState
name|currentState
parameter_list|)
throws|throws
name|Exception
block|{
return|return
name|innerDelete
argument_list|(
name|request
argument_list|,
name|currentState
argument_list|)
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
DECL|method|innerDelete
name|ClusterState
name|innerDelete
parameter_list|(
name|DeletePipelineRequest
name|request
parameter_list|,
name|ClusterState
name|currentState
parameter_list|)
block|{
name|IngestMetadata
name|currentIngestMetadata
init|=
name|currentState
operator|.
name|metaData
argument_list|()
operator|.
name|custom
argument_list|(
name|IngestMetadata
operator|.
name|TYPE
argument_list|)
decl_stmt|;
if|if
condition|(
name|currentIngestMetadata
operator|==
literal|null
condition|)
block|{
return|return
name|currentState
return|;
block|}
name|Map
argument_list|<
name|String
argument_list|,
name|PipelineConfiguration
argument_list|>
name|pipelines
init|=
name|currentIngestMetadata
operator|.
name|getPipelines
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|toRemove
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|pipelineKey
range|:
name|pipelines
operator|.
name|keySet
argument_list|()
control|)
block|{
if|if
condition|(
name|Regex
operator|.
name|simpleMatch
argument_list|(
name|request
operator|.
name|getId
argument_list|()
argument_list|,
name|pipelineKey
argument_list|)
condition|)
block|{
name|toRemove
operator|.
name|add
argument_list|(
name|pipelineKey
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|toRemove
operator|.
name|isEmpty
argument_list|()
operator|&&
name|Regex
operator|.
name|isMatchAllPattern
argument_list|(
name|request
operator|.
name|getId
argument_list|()
argument_list|)
operator|==
literal|false
condition|)
block|{
throw|throw
operator|new
name|ResourceNotFoundException
argument_list|(
literal|"pipeline [{}] is missing"
argument_list|,
name|request
operator|.
name|getId
argument_list|()
argument_list|)
throw|;
block|}
elseif|else
if|if
condition|(
name|toRemove
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
name|currentState
return|;
block|}
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|PipelineConfiguration
argument_list|>
name|pipelinesCopy
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
name|String
name|key
range|:
name|toRemove
control|)
block|{
name|pipelinesCopy
operator|.
name|remove
argument_list|(
name|key
argument_list|)
expr_stmt|;
block|}
name|ClusterState
operator|.
name|Builder
name|newState
init|=
name|ClusterState
operator|.
name|builder
argument_list|(
name|currentState
argument_list|)
decl_stmt|;
name|newState
operator|.
name|metaData
argument_list|(
name|MetaData
operator|.
name|builder
argument_list|(
name|currentState
operator|.
name|getMetaData
argument_list|()
argument_list|)
operator|.
name|putCustom
argument_list|(
name|IngestMetadata
operator|.
name|TYPE
argument_list|,
operator|new
name|IngestMetadata
argument_list|(
name|pipelinesCopy
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|newState
operator|.
name|build
argument_list|()
return|;
block|}
comment|/**      * Stores the specified pipeline definition in the request.      */
DECL|method|put
specifier|public
name|void
name|put
parameter_list|(
name|ClusterService
name|clusterService
parameter_list|,
name|Map
argument_list|<
name|DiscoveryNode
argument_list|,
name|IngestInfo
argument_list|>
name|ingestInfos
parameter_list|,
name|PutPipelineRequest
name|request
parameter_list|,
name|ActionListener
argument_list|<
name|WritePipelineResponse
argument_list|>
name|listener
parameter_list|)
throws|throws
name|Exception
block|{
comment|// validates the pipeline and processor configuration before submitting a cluster update task:
name|validatePipeline
argument_list|(
name|ingestInfos
argument_list|,
name|request
argument_list|)
expr_stmt|;
name|clusterService
operator|.
name|submitStateUpdateTask
argument_list|(
literal|"put-pipeline-"
operator|+
name|request
operator|.
name|getId
argument_list|()
argument_list|,
operator|new
name|AckedClusterStateUpdateTask
argument_list|<
name|WritePipelineResponse
argument_list|>
argument_list|(
name|request
argument_list|,
name|listener
argument_list|)
block|{
annotation|@
name|Override
specifier|protected
name|WritePipelineResponse
name|newResponse
parameter_list|(
name|boolean
name|acknowledged
parameter_list|)
block|{
return|return
operator|new
name|WritePipelineResponse
argument_list|(
name|acknowledged
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|ClusterState
name|execute
parameter_list|(
name|ClusterState
name|currentState
parameter_list|)
throws|throws
name|Exception
block|{
return|return
name|innerPut
argument_list|(
name|request
argument_list|,
name|currentState
argument_list|)
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
DECL|method|validatePipeline
name|void
name|validatePipeline
parameter_list|(
name|Map
argument_list|<
name|DiscoveryNode
argument_list|,
name|IngestInfo
argument_list|>
name|ingestInfos
parameter_list|,
name|PutPipelineRequest
name|request
parameter_list|)
throws|throws
name|Exception
block|{
if|if
condition|(
name|ingestInfos
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Ingest info is empty"
argument_list|)
throw|;
block|}
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
name|getSource
argument_list|()
argument_list|,
literal|false
argument_list|,
name|request
operator|.
name|getXContentType
argument_list|()
argument_list|)
operator|.
name|v2
argument_list|()
decl_stmt|;
name|Pipeline
name|pipeline
init|=
name|factory
operator|.
name|create
argument_list|(
name|request
operator|.
name|getId
argument_list|()
argument_list|,
name|pipelineConfig
argument_list|,
name|processorFactories
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Exception
argument_list|>
name|exceptions
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|Processor
name|processor
range|:
name|pipeline
operator|.
name|flattenAllProcessors
argument_list|()
control|)
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|DiscoveryNode
argument_list|,
name|IngestInfo
argument_list|>
name|entry
range|:
name|ingestInfos
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|containsProcessor
argument_list|(
name|processor
operator|.
name|getType
argument_list|()
argument_list|)
operator|==
literal|false
condition|)
block|{
name|String
name|message
init|=
literal|"Processor type ["
operator|+
name|processor
operator|.
name|getType
argument_list|()
operator|+
literal|"] is not installed on node ["
operator|+
name|entry
operator|.
name|getKey
argument_list|()
operator|+
literal|"]"
decl_stmt|;
name|exceptions
operator|.
name|add
argument_list|(
name|ConfigurationUtils
operator|.
name|newConfigurationException
argument_list|(
name|processor
operator|.
name|getType
argument_list|()
argument_list|,
name|processor
operator|.
name|getTag
argument_list|()
argument_list|,
literal|null
argument_list|,
name|message
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|ExceptionsHelper
operator|.
name|rethrowAndSuppress
argument_list|(
name|exceptions
argument_list|)
expr_stmt|;
block|}
DECL|method|innerPut
name|ClusterState
name|innerPut
parameter_list|(
name|PutPipelineRequest
name|request
parameter_list|,
name|ClusterState
name|currentState
parameter_list|)
block|{
name|IngestMetadata
name|currentIngestMetadata
init|=
name|currentState
operator|.
name|metaData
argument_list|()
operator|.
name|custom
argument_list|(
name|IngestMetadata
operator|.
name|TYPE
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|PipelineConfiguration
argument_list|>
name|pipelines
decl_stmt|;
if|if
condition|(
name|currentIngestMetadata
operator|!=
literal|null
condition|)
block|{
name|pipelines
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|(
name|currentIngestMetadata
operator|.
name|getPipelines
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|pipelines
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
expr_stmt|;
block|}
name|pipelines
operator|.
name|put
argument_list|(
name|request
operator|.
name|getId
argument_list|()
argument_list|,
operator|new
name|PipelineConfiguration
argument_list|(
name|request
operator|.
name|getId
argument_list|()
argument_list|,
name|request
operator|.
name|getSource
argument_list|()
argument_list|,
name|request
operator|.
name|getXContentType
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|ClusterState
operator|.
name|Builder
name|newState
init|=
name|ClusterState
operator|.
name|builder
argument_list|(
name|currentState
argument_list|)
decl_stmt|;
name|newState
operator|.
name|metaData
argument_list|(
name|MetaData
operator|.
name|builder
argument_list|(
name|currentState
operator|.
name|getMetaData
argument_list|()
argument_list|)
operator|.
name|putCustom
argument_list|(
name|IngestMetadata
operator|.
name|TYPE
argument_list|,
operator|new
name|IngestMetadata
argument_list|(
name|pipelines
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|newState
operator|.
name|build
argument_list|()
return|;
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
return|return
name|pipelines
operator|.
name|get
argument_list|(
name|id
argument_list|)
return|;
block|}
DECL|method|getProcessorFactories
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|Processor
operator|.
name|Factory
argument_list|>
name|getProcessorFactories
parameter_list|()
block|{
return|return
name|processorFactories
return|;
block|}
DECL|method|isNewIngestDateFormat
specifier|public
name|boolean
name|isNewIngestDateFormat
parameter_list|()
block|{
return|return
name|newIngestDateFormat
return|;
block|}
comment|/**      * @return pipeline configuration specified by id. If multiple ids or wildcards are specified multiple pipelines      * may be returned      */
comment|// Returning PipelineConfiguration instead of Pipeline, because Pipeline and Processor interface don't
comment|// know how to serialize themselves.
DECL|method|getPipelines
specifier|public
name|List
argument_list|<
name|PipelineConfiguration
argument_list|>
name|getPipelines
parameter_list|(
name|ClusterState
name|clusterState
parameter_list|,
name|String
modifier|...
name|ids
parameter_list|)
block|{
name|IngestMetadata
name|ingestMetadata
init|=
name|clusterState
operator|.
name|getMetaData
argument_list|()
operator|.
name|custom
argument_list|(
name|IngestMetadata
operator|.
name|TYPE
argument_list|)
decl_stmt|;
return|return
name|innerGetPipelines
argument_list|(
name|ingestMetadata
argument_list|,
name|ids
argument_list|)
return|;
block|}
DECL|method|innerGetPipelines
name|List
argument_list|<
name|PipelineConfiguration
argument_list|>
name|innerGetPipelines
parameter_list|(
name|IngestMetadata
name|ingestMetadata
parameter_list|,
name|String
modifier|...
name|ids
parameter_list|)
block|{
if|if
condition|(
name|ingestMetadata
operator|==
literal|null
condition|)
block|{
return|return
name|Collections
operator|.
name|emptyList
argument_list|()
return|;
block|}
comment|// if we didn't ask for _any_ ID, then we get them all (this is the same as if they ask for '*')
if|if
condition|(
name|ids
operator|.
name|length
operator|==
literal|0
condition|)
block|{
return|return
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|ingestMetadata
operator|.
name|getPipelines
argument_list|()
operator|.
name|values
argument_list|()
argument_list|)
return|;
block|}
name|List
argument_list|<
name|PipelineConfiguration
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
name|PipelineConfiguration
argument_list|>
name|entry
range|:
name|ingestMetadata
operator|.
name|getPipelines
argument_list|()
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
name|PipelineConfiguration
name|pipeline
init|=
name|ingestMetadata
operator|.
name|getPipelines
argument_list|()
operator|.
name|get
argument_list|(
name|id
argument_list|)
decl_stmt|;
if|if
condition|(
name|pipeline
operator|!=
literal|null
condition|)
block|{
name|result
operator|.
name|add
argument_list|(
name|pipeline
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|result
return|;
block|}
block|}
end_class

end_unit

