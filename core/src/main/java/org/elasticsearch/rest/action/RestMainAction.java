begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.rest.action
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|action
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
name|main
operator|.
name|MainAction
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
name|main
operator|.
name|MainRequest
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
name|main
operator|.
name|MainResponse
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
name|BytesArray
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
name|XContentBuilder
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
name|BytesRestResponse
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
name|RestResponse
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
name|RestStatus
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
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|RestRequest
operator|.
name|Method
operator|.
name|GET
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|RestRequest
operator|.
name|Method
operator|.
name|HEAD
import|;
end_import

begin_class
DECL|class|RestMainAction
specifier|public
class|class
name|RestMainAction
extends|extends
name|BaseRestHandler
block|{
annotation|@
name|Inject
DECL|method|RestMainAction
specifier|public
name|RestMainAction
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
name|GET
argument_list|,
literal|"/"
argument_list|,
name|this
argument_list|)
expr_stmt|;
name|controller
operator|.
name|registerHandler
argument_list|(
name|HEAD
argument_list|,
literal|"/"
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
specifier|final
name|RestRequest
name|request
parameter_list|,
specifier|final
name|NodeClient
name|client
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|channel
lambda|->
name|client
operator|.
name|execute
argument_list|(
name|MainAction
operator|.
name|INSTANCE
argument_list|,
operator|new
name|MainRequest
argument_list|()
argument_list|,
operator|new
name|RestBuilderListener
argument_list|<
name|MainResponse
argument_list|>
argument_list|(
name|channel
argument_list|)
block|{
block|@Override             public RestResponse buildResponse(MainResponse mainResponse
operator|,
name|XContentBuilder
name|builder
block|)
throws|throws
name|Exception
block|{
return|return
name|convertMainResponse
argument_list|(
name|mainResponse
argument_list|,
name|request
argument_list|,
name|builder
argument_list|)
return|;
block|}
block|}
end_class

begin_empty_stmt
unit|)
empty_stmt|;
end_empty_stmt

begin_function
unit|}      static
DECL|method|convertMainResponse
name|BytesRestResponse
name|convertMainResponse
parameter_list|(
name|MainResponse
name|response
parameter_list|,
name|RestRequest
name|request
parameter_list|,
name|XContentBuilder
name|builder
parameter_list|)
throws|throws
name|IOException
block|{
name|RestStatus
name|status
init|=
name|response
operator|.
name|isAvailable
argument_list|()
condition|?
name|RestStatus
operator|.
name|OK
else|:
name|RestStatus
operator|.
name|SERVICE_UNAVAILABLE
decl_stmt|;
comment|// Default to pretty printing, but allow ?pretty=false to disable
if|if
condition|(
name|request
operator|.
name|hasParam
argument_list|(
literal|"pretty"
argument_list|)
operator|==
literal|false
condition|)
block|{
name|builder
operator|.
name|prettyPrint
argument_list|()
operator|.
name|lfAtEnd
argument_list|()
expr_stmt|;
block|}
name|response
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|request
argument_list|)
expr_stmt|;
return|return
operator|new
name|BytesRestResponse
argument_list|(
name|status
argument_list|,
name|builder
argument_list|)
return|;
block|}
end_function

unit|}
end_unit

