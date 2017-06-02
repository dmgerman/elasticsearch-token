begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.rest.action.cat
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|action
operator|.
name|cat
package|;
end_package

begin_import
import|import
name|com
operator|.
name|carrotsearch
operator|.
name|hppc
operator|.
name|cursors
operator|.
name|ObjectObjectCursor
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
name|get
operator|.
name|GetAliasesRequest
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
name|get
operator|.
name|GetAliasesResponse
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
name|cluster
operator|.
name|metadata
operator|.
name|AliasMetaData
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
name|Table
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
name|action
operator|.
name|RestResponseListener
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

begin_class
DECL|class|RestAliasAction
specifier|public
class|class
name|RestAliasAction
extends|extends
name|AbstractCatAction
block|{
DECL|method|RestAliasAction
specifier|public
name|RestAliasAction
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
literal|"/_cat/aliases"
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
literal|"/_cat/aliases/{alias}"
argument_list|,
name|this
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getName
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
literal|"cat_alias_action"
return|;
block|}
annotation|@
name|Override
DECL|method|doCatRequest
specifier|protected
name|RestChannelConsumer
name|doCatRequest
parameter_list|(
specifier|final
name|RestRequest
name|request
parameter_list|,
specifier|final
name|NodeClient
name|client
parameter_list|)
block|{
specifier|final
name|GetAliasesRequest
name|getAliasesRequest
init|=
name|request
operator|.
name|hasParam
argument_list|(
literal|"alias"
argument_list|)
condition|?
operator|new
name|GetAliasesRequest
argument_list|(
name|Strings
operator|.
name|commaDelimitedListToStringArray
argument_list|(
name|request
operator|.
name|param
argument_list|(
literal|"alias"
argument_list|)
argument_list|)
argument_list|)
else|:
operator|new
name|GetAliasesRequest
argument_list|()
decl_stmt|;
name|getAliasesRequest
operator|.
name|local
argument_list|(
name|request
operator|.
name|paramAsBoolean
argument_list|(
literal|"local"
argument_list|,
name|getAliasesRequest
operator|.
name|local
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
name|indices
argument_list|()
operator|.
name|getAliases
argument_list|(
name|getAliasesRequest
argument_list|,
operator|new
name|RestResponseListener
argument_list|<
name|GetAliasesResponse
argument_list|>
argument_list|(
name|channel
argument_list|)
block|{
block|@Override             public RestResponse buildResponse(GetAliasesResponse response
block|)
throws|throws
name|Exception
block|{
name|Table
name|tab
init|=
name|buildTable
argument_list|(
name|request
argument_list|,
name|response
argument_list|)
decl_stmt|;
return|return
name|RestTable
operator|.
name|buildResponse
argument_list|(
name|tab
argument_list|,
name|channel
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
unit|}      @
name|Override
DECL|method|documentation
specifier|protected
name|void
name|documentation
parameter_list|(
name|StringBuilder
name|sb
parameter_list|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"/_cat/aliases\n"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"/_cat/aliases/{alias}\n"
argument_list|)
expr_stmt|;
block|}
end_function

begin_function
annotation|@
name|Override
DECL|method|getTableWithHeader
specifier|protected
name|Table
name|getTableWithHeader
parameter_list|(
name|RestRequest
name|request
parameter_list|)
block|{
specifier|final
name|Table
name|table
init|=
operator|new
name|Table
argument_list|()
decl_stmt|;
name|table
operator|.
name|startHeaders
argument_list|()
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
literal|"alias"
argument_list|,
literal|"alias:a;desc:alias name"
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
literal|"index"
argument_list|,
literal|"alias:i,idx;desc:index alias points to"
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
literal|"filter"
argument_list|,
literal|"alias:f,fi;desc:filter"
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
literal|"routing.index"
argument_list|,
literal|"alias:ri,routingIndex;desc:index routing"
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
literal|"routing.search"
argument_list|,
literal|"alias:rs,routingSearch;desc:search routing"
argument_list|)
expr_stmt|;
name|table
operator|.
name|endHeaders
argument_list|()
expr_stmt|;
return|return
name|table
return|;
block|}
end_function

begin_function
DECL|method|buildTable
specifier|private
name|Table
name|buildTable
parameter_list|(
name|RestRequest
name|request
parameter_list|,
name|GetAliasesResponse
name|response
parameter_list|)
block|{
name|Table
name|table
init|=
name|getTableWithHeader
argument_list|(
name|request
argument_list|)
decl_stmt|;
for|for
control|(
name|ObjectObjectCursor
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|AliasMetaData
argument_list|>
argument_list|>
name|cursor
range|:
name|response
operator|.
name|getAliases
argument_list|()
control|)
block|{
name|String
name|indexName
init|=
name|cursor
operator|.
name|key
decl_stmt|;
for|for
control|(
name|AliasMetaData
name|aliasMetaData
range|:
name|cursor
operator|.
name|value
control|)
block|{
name|table
operator|.
name|startRow
argument_list|()
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
name|aliasMetaData
operator|.
name|alias
argument_list|()
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
name|indexName
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
name|aliasMetaData
operator|.
name|filteringRequired
argument_list|()
condition|?
literal|"*"
else|:
literal|"-"
argument_list|)
expr_stmt|;
name|String
name|indexRouting
init|=
name|Strings
operator|.
name|hasLength
argument_list|(
name|aliasMetaData
operator|.
name|indexRouting
argument_list|()
argument_list|)
condition|?
name|aliasMetaData
operator|.
name|indexRouting
argument_list|()
else|:
literal|"-"
decl_stmt|;
name|table
operator|.
name|addCell
argument_list|(
name|indexRouting
argument_list|)
expr_stmt|;
name|String
name|searchRouting
init|=
name|Strings
operator|.
name|hasLength
argument_list|(
name|aliasMetaData
operator|.
name|searchRouting
argument_list|()
argument_list|)
condition|?
name|aliasMetaData
operator|.
name|searchRouting
argument_list|()
else|:
literal|"-"
decl_stmt|;
name|table
operator|.
name|addCell
argument_list|(
name|searchRouting
argument_list|)
expr_stmt|;
name|table
operator|.
name|endRow
argument_list|()
expr_stmt|;
block|}
block|}
return|return
name|table
return|;
block|}
end_function

unit|}
end_unit

