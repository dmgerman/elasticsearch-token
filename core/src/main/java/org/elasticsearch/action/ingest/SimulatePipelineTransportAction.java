begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.ingest
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
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
name|support
operator|.
name|ActionFilters
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
name|HandledTransportAction
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
name|IndexNameExpressionResolver
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
name|ingest
operator|.
name|PipelineStore
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
name|NodeService
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
name|Map
import|;
end_import

begin_class
DECL|class|SimulatePipelineTransportAction
specifier|public
class|class
name|SimulatePipelineTransportAction
extends|extends
name|HandledTransportAction
argument_list|<
name|SimulatePipelineRequest
argument_list|,
name|SimulatePipelineResponse
argument_list|>
block|{
DECL|field|pipelineStore
specifier|private
specifier|final
name|PipelineStore
name|pipelineStore
decl_stmt|;
DECL|field|executionService
specifier|private
specifier|final
name|SimulateExecutionService
name|executionService
decl_stmt|;
annotation|@
name|Inject
DECL|method|SimulatePipelineTransportAction
specifier|public
name|SimulatePipelineTransportAction
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
name|ActionFilters
name|actionFilters
parameter_list|,
name|IndexNameExpressionResolver
name|indexNameExpressionResolver
parameter_list|,
name|NodeService
name|nodeService
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|SimulatePipelineAction
operator|.
name|NAME
argument_list|,
name|threadPool
argument_list|,
name|transportService
argument_list|,
name|actionFilters
argument_list|,
name|indexNameExpressionResolver
argument_list|,
name|SimulatePipelineRequest
operator|::
operator|new
argument_list|)
expr_stmt|;
name|this
operator|.
name|pipelineStore
operator|=
name|nodeService
operator|.
name|getIngestService
argument_list|()
operator|.
name|getPipelineStore
argument_list|()
expr_stmt|;
name|this
operator|.
name|executionService
operator|=
operator|new
name|SimulateExecutionService
argument_list|(
name|threadPool
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doExecute
specifier|protected
name|void
name|doExecute
parameter_list|(
name|SimulatePipelineRequest
name|request
parameter_list|,
name|ActionListener
argument_list|<
name|SimulatePipelineResponse
argument_list|>
name|listener
parameter_list|)
block|{
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|source
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
specifier|final
name|SimulatePipelineRequest
operator|.
name|Parsed
name|simulateRequest
decl_stmt|;
try|try
block|{
if|if
condition|(
name|request
operator|.
name|getId
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|simulateRequest
operator|=
name|SimulatePipelineRequest
operator|.
name|parseWithPipelineId
argument_list|(
name|request
operator|.
name|getId
argument_list|()
argument_list|,
name|source
argument_list|,
name|request
operator|.
name|isVerbose
argument_list|()
argument_list|,
name|pipelineStore
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|simulateRequest
operator|=
name|SimulatePipelineRequest
operator|.
name|parse
argument_list|(
name|source
argument_list|,
name|request
operator|.
name|isVerbose
argument_list|()
argument_list|,
name|pipelineStore
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
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
return|return;
block|}
name|executionService
operator|.
name|execute
argument_list|(
name|simulateRequest
argument_list|,
name|listener
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

