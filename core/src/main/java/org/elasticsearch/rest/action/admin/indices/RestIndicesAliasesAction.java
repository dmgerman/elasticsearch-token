begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.rest.action.admin.indices
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|action
operator|.
name|admin
operator|.
name|indices
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
name|alias
operator|.
name|IndicesAliasesRequest
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
name|alias
operator|.
name|IndicesAliasesRequest
operator|.
name|AliasActions
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
name|ParseField
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
name|ObjectParser
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
name|XContentParser
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
DECL|class|RestIndicesAliasesAction
specifier|public
class|class
name|RestIndicesAliasesAction
extends|extends
name|BaseRestHandler
block|{
DECL|field|PARSER
specifier|static
specifier|final
name|ObjectParser
argument_list|<
name|IndicesAliasesRequest
argument_list|,
name|Void
argument_list|>
name|PARSER
init|=
operator|new
name|ObjectParser
argument_list|<>
argument_list|(
literal|"aliases"
argument_list|)
decl_stmt|;
static|static
block|{
name|PARSER
operator|.
name|declareObjectArray
argument_list|(
parameter_list|(
name|request
parameter_list|,
name|actions
parameter_list|)
lambda|->
block|{
for|for
control|(
name|AliasActions
name|action
range|:
name|actions
control|)
block|{
name|request
operator|.
name|addAliasAction
argument_list|(
name|action
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|,
name|AliasActions
operator|.
name|PARSER
argument_list|,
operator|new
name|ParseField
argument_list|(
literal|"actions"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Inject
DECL|method|RestIndicesAliasesAction
specifier|public
name|RestIndicesAliasesAction
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
name|POST
argument_list|,
literal|"/_aliases"
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
name|IndicesAliasesRequest
name|indicesAliasesRequest
init|=
operator|new
name|IndicesAliasesRequest
argument_list|()
decl_stmt|;
name|indicesAliasesRequest
operator|.
name|masterNodeTimeout
argument_list|(
name|request
operator|.
name|paramAsTime
argument_list|(
literal|"master_timeout"
argument_list|,
name|indicesAliasesRequest
operator|.
name|masterNodeTimeout
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|indicesAliasesRequest
operator|.
name|timeout
argument_list|(
name|request
operator|.
name|paramAsTime
argument_list|(
literal|"timeout"
argument_list|,
name|indicesAliasesRequest
operator|.
name|timeout
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
try|try
init|(
name|XContentParser
name|parser
init|=
name|request
operator|.
name|contentParser
argument_list|()
init|)
block|{
name|PARSER
operator|.
name|parse
argument_list|(
name|parser
argument_list|,
name|indicesAliasesRequest
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|indicesAliasesRequest
operator|.
name|getAliasActions
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"No action specified"
argument_list|)
throw|;
block|}
return|return
name|channel
lambda|->
name|client
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|aliases
argument_list|(
name|indicesAliasesRequest
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

