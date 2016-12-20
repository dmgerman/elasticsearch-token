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
name|common
operator|.
name|xcontent
operator|.
name|XContentFactory
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
import|import
name|java
operator|.
name|util
operator|.
name|Map
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
name|PUT
import|;
end_import

begin_class
DECL|class|RestIndexPutAliasAction
specifier|public
class|class
name|RestIndexPutAliasAction
extends|extends
name|BaseRestHandler
block|{
annotation|@
name|Inject
DECL|method|RestIndexPutAliasAction
specifier|public
name|RestIndexPutAliasAction
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
name|PUT
argument_list|,
literal|"/{index}/_alias/{name}"
argument_list|,
name|this
argument_list|)
expr_stmt|;
name|controller
operator|.
name|registerHandler
argument_list|(
name|PUT
argument_list|,
literal|"/_alias/{name}"
argument_list|,
name|this
argument_list|)
expr_stmt|;
name|controller
operator|.
name|registerHandler
argument_list|(
name|PUT
argument_list|,
literal|"/{index}/_aliases/{name}"
argument_list|,
name|this
argument_list|)
expr_stmt|;
name|controller
operator|.
name|registerHandler
argument_list|(
name|PUT
argument_list|,
literal|"/_aliases/{name}"
argument_list|,
name|this
argument_list|)
expr_stmt|;
name|controller
operator|.
name|registerHandler
argument_list|(
name|PUT
argument_list|,
literal|"/{index}/_alias"
argument_list|,
name|this
argument_list|)
expr_stmt|;
name|controller
operator|.
name|registerHandler
argument_list|(
name|PUT
argument_list|,
literal|"/_alias"
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
literal|"/{index}/_alias/{name}"
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
literal|"/_alias/{name}"
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
literal|"/{index}/_aliases/{name}"
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
literal|"/_aliases/{name}"
argument_list|,
name|this
argument_list|)
expr_stmt|;
name|controller
operator|.
name|registerHandler
argument_list|(
name|PUT
argument_list|,
literal|"/{index}/_aliases"
argument_list|,
name|this
argument_list|)
expr_stmt|;
comment|//we cannot add POST for "/_aliases" because this is the _aliases api already defined in RestIndicesAliasesAction
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
name|String
index|[]
name|indices
init|=
name|Strings
operator|.
name|splitStringByCommaToArray
argument_list|(
name|request
operator|.
name|param
argument_list|(
literal|"index"
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|alias
init|=
name|request
operator|.
name|param
argument_list|(
literal|"name"
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|filter
init|=
literal|null
decl_stmt|;
name|String
name|routing
init|=
literal|null
decl_stmt|;
name|String
name|indexRouting
init|=
literal|null
decl_stmt|;
name|String
name|searchRouting
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|request
operator|.
name|hasContent
argument_list|()
condition|)
block|{
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
name|XContentParser
operator|.
name|Token
name|token
init|=
name|parser
operator|.
name|nextToken
argument_list|()
decl_stmt|;
if|if
condition|(
name|token
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"No index alias is specified"
argument_list|)
throw|;
block|}
name|String
name|currentFieldName
init|=
literal|null
decl_stmt|;
while|while
condition|(
operator|(
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
operator|)
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|FIELD_NAME
condition|)
block|{
name|currentFieldName
operator|=
name|parser
operator|.
name|currentName
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|token
operator|.
name|isValue
argument_list|()
condition|)
block|{
if|if
condition|(
literal|"index"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|indices
operator|=
name|Strings
operator|.
name|splitStringByCommaToArray
argument_list|(
name|parser
operator|.
name|text
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"alias"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|alias
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"routing"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|routing
operator|=
name|parser
operator|.
name|textOrNull
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"indexRouting"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
operator|||
literal|"index-routing"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
operator|||
literal|"index_routing"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|indexRouting
operator|=
name|parser
operator|.
name|textOrNull
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"searchRouting"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
operator|||
literal|"search-routing"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
operator|||
literal|"search_routing"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|searchRouting
operator|=
name|parser
operator|.
name|textOrNull
argument_list|()
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|START_OBJECT
condition|)
block|{
if|if
condition|(
literal|"filter"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|filter
operator|=
name|parser
operator|.
name|mapOrdered
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
name|IndicesAliasesRequest
name|indicesAliasesRequest
init|=
operator|new
name|IndicesAliasesRequest
argument_list|()
decl_stmt|;
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
name|IndicesAliasesRequest
operator|.
name|AliasActions
name|aliasAction
init|=
name|AliasActions
operator|.
name|add
argument_list|()
operator|.
name|indices
argument_list|(
name|indices
argument_list|)
operator|.
name|alias
argument_list|(
name|alias
argument_list|)
decl_stmt|;
if|if
condition|(
name|routing
operator|!=
literal|null
condition|)
block|{
name|aliasAction
operator|.
name|routing
argument_list|(
name|routing
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|searchRouting
operator|!=
literal|null
condition|)
block|{
name|aliasAction
operator|.
name|searchRouting
argument_list|(
name|searchRouting
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|indexRouting
operator|!=
literal|null
condition|)
block|{
name|aliasAction
operator|.
name|indexRouting
argument_list|(
name|indexRouting
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|filter
operator|!=
literal|null
condition|)
block|{
name|aliasAction
operator|.
name|filter
argument_list|(
name|filter
argument_list|)
expr_stmt|;
block|}
name|indicesAliasesRequest
operator|.
name|addAliasAction
argument_list|(
name|aliasAction
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

