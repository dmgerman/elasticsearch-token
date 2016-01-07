begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.rest.action.ingest
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|rest
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
name|action
operator|.
name|ingest
operator|.
name|PutPipelineAction
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
name|rest
operator|.
name|BaseRestHandler
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
name|RestChannel
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
name|RestRequest
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
name|action
operator|.
name|support
operator|.
name|RestStatusToXContentListener
import|;
end_import

begin_class
DECL|class|RestPutPipelineAction
specifier|public
class|class
name|RestPutPipelineAction
extends|extends
name|BaseRestHandler
block|{
annotation|@
name|Inject
DECL|method|RestPutPipelineAction
specifier|public
name|RestPutPipelineAction
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|RestController
name|controller
parameter_list|,
name|Client
name|client
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|controller
argument_list|,
name|client
argument_list|)
expr_stmt|;
name|controller
operator|.
name|registerHandler
argument_list|(
name|RestRequest
operator|.
name|Method
operator|.
name|PUT
argument_list|,
literal|"/_ingest/pipeline/{id}"
argument_list|,
name|this
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|handleRequest
specifier|protected
name|void
name|handleRequest
parameter_list|(
name|RestRequest
name|restRequest
parameter_list|,
name|RestChannel
name|channel
parameter_list|,
name|Client
name|client
parameter_list|)
throws|throws
name|Exception
block|{
name|PutPipelineRequest
name|request
init|=
operator|new
name|PutPipelineRequest
argument_list|()
decl_stmt|;
name|request
operator|.
name|id
argument_list|(
name|restRequest
operator|.
name|param
argument_list|(
literal|"id"
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|restRequest
operator|.
name|hasContent
argument_list|()
condition|)
block|{
name|request
operator|.
name|source
argument_list|(
name|restRequest
operator|.
name|content
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|client
operator|.
name|execute
argument_list|(
name|PutPipelineAction
operator|.
name|INSTANCE
argument_list|,
name|request
argument_list|,
operator|new
name|RestStatusToXContentListener
argument_list|<>
argument_list|(
name|channel
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

