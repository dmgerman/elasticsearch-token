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
name|client
operator|.
name|node
operator|.
name|NodeClient
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
name|XContentType
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
name|AcknowledgedRestListener
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

begin_class
DECL|class|RestPutPipelineAction
specifier|public
class|class
name|RestPutPipelineAction
extends|extends
name|BaseRestHandler
block|{
DECL|method|RestPutPipelineAction
specifier|public
name|RestPutPipelineAction
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|RestController
name|controller
parameter_list|)
block|{
name|super
argument_list|(
name|settings
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
DECL|method|prepareRequest
specifier|public
name|RestChannelConsumer
name|prepareRequest
parameter_list|(
name|RestRequest
name|restRequest
parameter_list|,
name|NodeClient
name|client
parameter_list|)
throws|throws
name|IOException
block|{
name|Tuple
argument_list|<
name|XContentType
argument_list|,
name|BytesReference
argument_list|>
name|sourceTuple
init|=
name|restRequest
operator|.
name|contentOrSourceParam
argument_list|()
decl_stmt|;
name|PutPipelineRequest
name|request
init|=
operator|new
name|PutPipelineRequest
argument_list|(
name|restRequest
operator|.
name|param
argument_list|(
literal|"id"
argument_list|)
argument_list|,
name|sourceTuple
operator|.
name|v2
argument_list|()
argument_list|,
name|sourceTuple
operator|.
name|v1
argument_list|()
argument_list|)
decl_stmt|;
name|request
operator|.
name|masterNodeTimeout
argument_list|(
name|restRequest
operator|.
name|paramAsTime
argument_list|(
literal|"master_timeout"
argument_list|,
name|request
operator|.
name|masterNodeTimeout
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|request
operator|.
name|timeout
argument_list|(
name|restRequest
operator|.
name|paramAsTime
argument_list|(
literal|"timeout"
argument_list|,
name|request
operator|.
name|timeout
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|channel
lambda|->
name|client
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|putPipeline
argument_list|(
name|request
argument_list|,
operator|new
name|AcknowledgedRestListener
argument_list|<>
argument_list|(
name|channel
argument_list|)
argument_list|)
return|;
block|}
block|}
end_class

end_unit

