begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.rest
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|rest
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
name|*
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
name|FilterClient
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
name|support
operator|.
name|AbstractClient
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
name|settings
operator|.
name|Settings
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

begin_comment
comment|/**  * Base handler for REST requests.  *<p/>  * This handler makes sure that the headers& context of the handled {@link RestRequest requests} are copied over to  * the transport requests executed by the associated client. While the context is fully copied over, not all the headers  * are copied, but a selected few. It is possible to control what headers are copied over by registering them using  * {@link org.elasticsearch.rest.RestController#registerRelevantHeaders(String...)}  */
end_comment

begin_class
DECL|class|BaseRestHandler
specifier|public
specifier|abstract
class|class
name|BaseRestHandler
extends|extends
name|AbstractComponent
implements|implements
name|RestHandler
block|{
DECL|field|controller
specifier|private
specifier|final
name|RestController
name|controller
decl_stmt|;
DECL|field|client
specifier|private
specifier|final
name|Client
name|client
decl_stmt|;
DECL|method|BaseRestHandler
specifier|protected
name|BaseRestHandler
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
argument_list|)
expr_stmt|;
name|this
operator|.
name|controller
operator|=
name|controller
expr_stmt|;
name|this
operator|.
name|client
operator|=
name|client
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|handleRequest
specifier|public
specifier|final
name|void
name|handleRequest
parameter_list|(
name|RestRequest
name|request
parameter_list|,
name|RestChannel
name|channel
parameter_list|)
throws|throws
name|Exception
block|{
name|handleRequest
argument_list|(
name|request
argument_list|,
name|channel
argument_list|,
operator|new
name|HeadersAndContextCopyClient
argument_list|(
name|client
argument_list|,
name|request
argument_list|,
name|controller
operator|.
name|relevantHeaders
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|handleRequest
specifier|protected
specifier|abstract
name|void
name|handleRequest
parameter_list|(
name|RestRequest
name|request
parameter_list|,
name|RestChannel
name|channel
parameter_list|,
name|Client
name|client
parameter_list|)
throws|throws
name|Exception
function_decl|;
DECL|class|HeadersAndContextCopyClient
specifier|static
specifier|final
class|class
name|HeadersAndContextCopyClient
extends|extends
name|FilterClient
block|{
DECL|field|restRequest
specifier|private
specifier|final
name|RestRequest
name|restRequest
decl_stmt|;
DECL|field|headers
specifier|private
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|headers
decl_stmt|;
DECL|method|HeadersAndContextCopyClient
name|HeadersAndContextCopyClient
parameter_list|(
name|Client
name|in
parameter_list|,
name|RestRequest
name|restRequest
parameter_list|,
name|Set
argument_list|<
name|String
argument_list|>
name|headers
parameter_list|)
block|{
name|super
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|this
operator|.
name|restRequest
operator|=
name|restRequest
expr_stmt|;
name|this
operator|.
name|headers
operator|=
name|headers
expr_stmt|;
block|}
DECL|method|copyHeadersAndContext
specifier|private
specifier|static
name|void
name|copyHeadersAndContext
parameter_list|(
name|ActionRequest
name|actionRequest
parameter_list|,
name|RestRequest
name|restRequest
parameter_list|,
name|Set
argument_list|<
name|String
argument_list|>
name|headers
parameter_list|)
block|{
for|for
control|(
name|String
name|usefulHeader
range|:
name|headers
control|)
block|{
name|String
name|headerValue
init|=
name|restRequest
operator|.
name|header
argument_list|(
name|usefulHeader
argument_list|)
decl_stmt|;
if|if
condition|(
name|headerValue
operator|!=
literal|null
condition|)
block|{
name|actionRequest
operator|.
name|putHeader
argument_list|(
name|usefulHeader
argument_list|,
name|headerValue
argument_list|)
expr_stmt|;
block|}
block|}
name|actionRequest
operator|.
name|copyContextFrom
argument_list|(
name|restRequest
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doExecute
specifier|protected
parameter_list|<
name|Request
extends|extends
name|ActionRequest
parameter_list|,
name|Response
extends|extends
name|ActionResponse
parameter_list|,
name|RequestBuilder
extends|extends
name|ActionRequestBuilder
argument_list|<
name|Request
argument_list|,
name|Response
argument_list|,
name|RequestBuilder
argument_list|>
parameter_list|>
name|void
name|doExecute
parameter_list|(
name|Action
argument_list|<
name|Request
argument_list|,
name|Response
argument_list|,
name|RequestBuilder
argument_list|>
name|action
parameter_list|,
name|Request
name|request
parameter_list|,
name|ActionListener
argument_list|<
name|Response
argument_list|>
name|listener
parameter_list|)
block|{
name|copyHeadersAndContext
argument_list|(
name|request
argument_list|,
name|restRequest
argument_list|,
name|headers
argument_list|)
expr_stmt|;
name|super
operator|.
name|doExecute
argument_list|(
name|action
argument_list|,
name|request
argument_list|,
name|listener
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

