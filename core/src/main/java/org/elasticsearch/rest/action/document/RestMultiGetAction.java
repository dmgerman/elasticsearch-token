begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.rest.action.document
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|action
operator|.
name|document
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
name|get
operator|.
name|MultiGetRequest
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
name|MultiGetResponse
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
name|Strings
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
name|RestActions
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
name|RestToXContentListener
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
name|fetch
operator|.
name|subphase
operator|.
name|FetchSourceContext
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
name|POST
import|;
end_import

begin_class
DECL|class|RestMultiGetAction
specifier|public
class|class
name|RestMultiGetAction
extends|extends
name|BaseRestHandler
block|{
DECL|field|allowExplicitIndex
specifier|private
specifier|final
name|boolean
name|allowExplicitIndex
decl_stmt|;
annotation|@
name|Inject
DECL|method|RestMultiGetAction
specifier|public
name|RestMultiGetAction
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
literal|"/_mget"
argument_list|,
name|this
argument_list|)
expr_stmt|;
name|controller
operator|.
name|registerHandler
argument_list|(
name|POST
argument_list|,
literal|"/_mget"
argument_list|,
name|this
argument_list|)
expr_stmt|;
name|controller
operator|.
name|registerHandler
argument_list|(
name|GET
argument_list|,
literal|"/{index}/_mget"
argument_list|,
name|this
argument_list|)
expr_stmt|;
name|controller
operator|.
name|registerHandler
argument_list|(
name|POST
argument_list|,
literal|"/{index}/_mget"
argument_list|,
name|this
argument_list|)
expr_stmt|;
name|controller
operator|.
name|registerHandler
argument_list|(
name|GET
argument_list|,
literal|"/{index}/{type}/_mget"
argument_list|,
name|this
argument_list|)
expr_stmt|;
name|controller
operator|.
name|registerHandler
argument_list|(
name|POST
argument_list|,
literal|"/{index}/{type}/_mget"
argument_list|,
name|this
argument_list|)
expr_stmt|;
name|this
operator|.
name|allowExplicitIndex
operator|=
name|MULTI_ALLOW_EXPLICIT_INDEX
operator|.
name|get
argument_list|(
name|settings
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|handleRequest
specifier|public
name|void
name|handleRequest
parameter_list|(
specifier|final
name|RestRequest
name|request
parameter_list|,
specifier|final
name|RestChannel
name|channel
parameter_list|,
specifier|final
name|NodeClient
name|client
parameter_list|)
throws|throws
name|Exception
block|{
name|MultiGetRequest
name|multiGetRequest
init|=
operator|new
name|MultiGetRequest
argument_list|()
decl_stmt|;
name|multiGetRequest
operator|.
name|refresh
argument_list|(
name|request
operator|.
name|paramAsBoolean
argument_list|(
literal|"refresh"
argument_list|,
name|multiGetRequest
operator|.
name|refresh
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|multiGetRequest
operator|.
name|preference
argument_list|(
name|request
operator|.
name|param
argument_list|(
literal|"preference"
argument_list|)
argument_list|)
expr_stmt|;
name|multiGetRequest
operator|.
name|realtime
argument_list|(
name|request
operator|.
name|paramAsBoolean
argument_list|(
literal|"realtime"
argument_list|,
name|multiGetRequest
operator|.
name|realtime
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|request
operator|.
name|param
argument_list|(
literal|"fields"
argument_list|)
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"The parameter [fields] is no longer supported, "
operator|+
literal|"please use [stored_fields] to retrieve stored fields or _source filtering if the field is not stored"
argument_list|)
throw|;
block|}
name|String
index|[]
name|sFields
init|=
literal|null
decl_stmt|;
name|String
name|sField
init|=
name|request
operator|.
name|param
argument_list|(
literal|"stored_fields"
argument_list|)
decl_stmt|;
if|if
condition|(
name|sField
operator|!=
literal|null
condition|)
block|{
name|sFields
operator|=
name|Strings
operator|.
name|splitStringByCommaToArray
argument_list|(
name|sField
argument_list|)
expr_stmt|;
block|}
name|FetchSourceContext
name|defaultFetchSource
init|=
name|FetchSourceContext
operator|.
name|parseFromRestRequest
argument_list|(
name|request
argument_list|)
decl_stmt|;
name|multiGetRequest
operator|.
name|add
argument_list|(
name|request
operator|.
name|param
argument_list|(
literal|"index"
argument_list|)
argument_list|,
name|request
operator|.
name|param
argument_list|(
literal|"type"
argument_list|)
argument_list|,
name|sFields
argument_list|,
name|defaultFetchSource
argument_list|,
name|request
operator|.
name|param
argument_list|(
literal|"routing"
argument_list|)
argument_list|,
name|RestActions
operator|.
name|getRestContent
argument_list|(
name|request
argument_list|)
argument_list|,
name|allowExplicitIndex
argument_list|)
expr_stmt|;
name|client
operator|.
name|multiGet
argument_list|(
name|multiGetRequest
argument_list|,
operator|new
name|RestToXContentListener
argument_list|<
name|MultiGetResponse
argument_list|>
argument_list|(
name|channel
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

