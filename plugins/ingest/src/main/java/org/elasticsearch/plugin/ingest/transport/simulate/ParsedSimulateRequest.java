begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.plugin.ingest.transport.simulate
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|plugin
operator|.
name|ingest
operator|.
name|transport
operator|.
name|simulate
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|Data
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
name|processor
operator|.
name|ConfigurationUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|plugin
operator|.
name|ingest
operator|.
name|PipelineStore
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
name|*
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|plugin
operator|.
name|ingest
operator|.
name|transport
operator|.
name|simulate
operator|.
name|SimulatePipelineRequest
operator|.
name|Fields
import|;
end_import

begin_class
DECL|class|ParsedSimulateRequest
specifier|public
class|class
name|ParsedSimulateRequest
block|{
DECL|field|documents
specifier|private
specifier|final
name|List
argument_list|<
name|Data
argument_list|>
name|documents
decl_stmt|;
DECL|field|pipeline
specifier|private
specifier|final
name|Pipeline
name|pipeline
decl_stmt|;
DECL|field|verbose
specifier|private
specifier|final
name|boolean
name|verbose
decl_stmt|;
DECL|method|ParsedSimulateRequest
name|ParsedSimulateRequest
parameter_list|(
name|Pipeline
name|pipeline
parameter_list|,
name|List
argument_list|<
name|Data
argument_list|>
name|documents
parameter_list|,
name|boolean
name|verbose
parameter_list|)
block|{
name|this
operator|.
name|pipeline
operator|=
name|pipeline
expr_stmt|;
name|this
operator|.
name|documents
operator|=
name|Collections
operator|.
name|unmodifiableList
argument_list|(
name|documents
argument_list|)
expr_stmt|;
name|this
operator|.
name|verbose
operator|=
name|verbose
expr_stmt|;
block|}
DECL|method|getPipeline
specifier|public
name|Pipeline
name|getPipeline
parameter_list|()
block|{
return|return
name|pipeline
return|;
block|}
DECL|method|getDocuments
specifier|public
name|List
argument_list|<
name|Data
argument_list|>
name|getDocuments
parameter_list|()
block|{
return|return
name|documents
return|;
block|}
DECL|method|isVerbose
specifier|public
name|boolean
name|isVerbose
parameter_list|()
block|{
return|return
name|verbose
return|;
block|}
DECL|class|Parser
specifier|public
specifier|static
class|class
name|Parser
block|{
DECL|field|PIPELINE_FACTORY
specifier|private
specifier|static
specifier|final
name|Pipeline
operator|.
name|Factory
name|PIPELINE_FACTORY
init|=
operator|new
name|Pipeline
operator|.
name|Factory
argument_list|()
decl_stmt|;
DECL|field|SIMULATED_PIPELINE_ID
specifier|public
specifier|static
specifier|final
name|String
name|SIMULATED_PIPELINE_ID
init|=
literal|"_simulate_pipeline"
decl_stmt|;
DECL|method|parseDocs
specifier|private
name|List
argument_list|<
name|Data
argument_list|>
name|parseDocs
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|config
parameter_list|)
block|{
name|List
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|>
name|docs
init|=
name|ConfigurationUtils
operator|.
name|readList
argument_list|(
name|config
argument_list|,
name|Fields
operator|.
name|DOCS
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Data
argument_list|>
name|dataList
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|dataMap
range|:
name|docs
control|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|document
init|=
name|ConfigurationUtils
operator|.
name|readMap
argument_list|(
name|dataMap
argument_list|,
name|Fields
operator|.
name|SOURCE
argument_list|)
decl_stmt|;
name|Data
name|data
init|=
operator|new
name|Data
argument_list|(
name|ConfigurationUtils
operator|.
name|readStringProperty
argument_list|(
name|dataMap
argument_list|,
name|Fields
operator|.
name|INDEX
argument_list|)
argument_list|,
name|ConfigurationUtils
operator|.
name|readStringProperty
argument_list|(
name|dataMap
argument_list|,
name|Fields
operator|.
name|TYPE
argument_list|)
argument_list|,
name|ConfigurationUtils
operator|.
name|readStringProperty
argument_list|(
name|dataMap
argument_list|,
name|Fields
operator|.
name|ID
argument_list|)
argument_list|,
name|document
argument_list|)
decl_stmt|;
name|dataList
operator|.
name|add
argument_list|(
name|data
argument_list|)
expr_stmt|;
block|}
return|return
name|dataList
return|;
block|}
DECL|method|parseWithPipelineId
specifier|public
name|ParsedSimulateRequest
name|parseWithPipelineId
parameter_list|(
name|String
name|pipelineId
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|config
parameter_list|,
name|boolean
name|verbose
parameter_list|,
name|PipelineStore
name|pipelineStore
parameter_list|)
block|{
if|if
condition|(
name|pipelineId
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"param [pipeline] is null"
argument_list|)
throw|;
block|}
name|Pipeline
name|pipeline
init|=
name|pipelineStore
operator|.
name|get
argument_list|(
name|pipelineId
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Data
argument_list|>
name|dataList
init|=
name|parseDocs
argument_list|(
name|config
argument_list|)
decl_stmt|;
return|return
operator|new
name|ParsedSimulateRequest
argument_list|(
name|pipeline
argument_list|,
name|dataList
argument_list|,
name|verbose
argument_list|)
return|;
block|}
DECL|method|parse
specifier|public
name|ParsedSimulateRequest
name|parse
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|config
parameter_list|,
name|boolean
name|verbose
parameter_list|,
name|PipelineStore
name|pipelineStore
parameter_list|)
throws|throws
name|IOException
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|pipelineConfig
init|=
name|ConfigurationUtils
operator|.
name|readMap
argument_list|(
name|config
argument_list|,
name|Fields
operator|.
name|PIPELINE
argument_list|)
decl_stmt|;
name|Pipeline
name|pipeline
init|=
name|PIPELINE_FACTORY
operator|.
name|create
argument_list|(
name|SIMULATED_PIPELINE_ID
argument_list|,
name|pipelineConfig
argument_list|,
name|pipelineStore
operator|.
name|getProcessorFactoryRegistry
argument_list|()
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Data
argument_list|>
name|dataList
init|=
name|parseDocs
argument_list|(
name|config
argument_list|)
decl_stmt|;
return|return
operator|new
name|ParsedSimulateRequest
argument_list|(
name|pipeline
argument_list|,
name|dataList
argument_list|,
name|verbose
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

