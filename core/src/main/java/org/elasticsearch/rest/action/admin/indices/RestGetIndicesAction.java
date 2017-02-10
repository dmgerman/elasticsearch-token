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
name|get
operator|.
name|GetIndexRequest
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
name|get
operator|.
name|GetIndexRequest
operator|.
name|Feature
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
name|get
operator|.
name|GetIndexResponse
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
name|IndicesOptions
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
name|cluster
operator|.
name|metadata
operator|.
name|MappingMetaData
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
name|collect
operator|.
name|ImmutableOpenMap
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
name|IndexScopedSettings
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
name|settings
operator|.
name|SettingsFilter
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
name|ToXContent
operator|.
name|Params
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
name|action
operator|.
name|RestBuilderListener
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
name|List
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

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|RestStatus
operator|.
name|OK
import|;
end_import

begin_class
DECL|class|RestGetIndicesAction
specifier|public
class|class
name|RestGetIndicesAction
extends|extends
name|BaseRestHandler
block|{
DECL|field|indexScopedSettings
specifier|private
specifier|final
name|IndexScopedSettings
name|indexScopedSettings
decl_stmt|;
DECL|field|settingsFilter
specifier|private
specifier|final
name|SettingsFilter
name|settingsFilter
decl_stmt|;
DECL|method|RestGetIndicesAction
specifier|public
name|RestGetIndicesAction
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|RestController
name|controller
parameter_list|,
name|IndexScopedSettings
name|indexScopedSettings
parameter_list|,
name|SettingsFilter
name|settingsFilter
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|indexScopedSettings
operator|=
name|indexScopedSettings
expr_stmt|;
name|controller
operator|.
name|registerHandler
argument_list|(
name|GET
argument_list|,
literal|"/{index}"
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
literal|"/{index}"
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
literal|"/{index}/{type}"
argument_list|,
name|this
argument_list|)
expr_stmt|;
name|this
operator|.
name|settingsFilter
operator|=
name|settingsFilter
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
index|[]
name|featureParams
init|=
name|request
operator|.
name|paramAsStringArray
argument_list|(
literal|"type"
argument_list|,
literal|null
argument_list|)
decl_stmt|;
comment|// Work out if the indices is a list of features
if|if
condition|(
name|featureParams
operator|==
literal|null
operator|&&
name|indices
operator|.
name|length
operator|>
literal|0
operator|&&
name|indices
index|[
literal|0
index|]
operator|!=
literal|null
operator|&&
name|indices
index|[
literal|0
index|]
operator|.
name|startsWith
argument_list|(
literal|"_"
argument_list|)
operator|&&
operator|!
literal|"_all"
operator|.
name|equals
argument_list|(
name|indices
index|[
literal|0
index|]
argument_list|)
condition|)
block|{
name|featureParams
operator|=
name|indices
expr_stmt|;
name|indices
operator|=
operator|new
name|String
index|[]
block|{
literal|"_all"
block|}
expr_stmt|;
block|}
specifier|final
name|GetIndexRequest
name|getIndexRequest
init|=
operator|new
name|GetIndexRequest
argument_list|()
decl_stmt|;
name|getIndexRequest
operator|.
name|indices
argument_list|(
name|indices
argument_list|)
expr_stmt|;
if|if
condition|(
name|featureParams
operator|!=
literal|null
condition|)
block|{
name|Feature
index|[]
name|features
init|=
name|Feature
operator|.
name|convertToFeatures
argument_list|(
name|featureParams
argument_list|)
decl_stmt|;
name|getIndexRequest
operator|.
name|features
argument_list|(
name|features
argument_list|)
expr_stmt|;
block|}
name|getIndexRequest
operator|.
name|indicesOptions
argument_list|(
name|IndicesOptions
operator|.
name|fromRequest
argument_list|(
name|request
argument_list|,
name|getIndexRequest
operator|.
name|indicesOptions
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|getIndexRequest
operator|.
name|local
argument_list|(
name|request
operator|.
name|paramAsBoolean
argument_list|(
literal|"local"
argument_list|,
name|getIndexRequest
operator|.
name|local
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|getIndexRequest
operator|.
name|humanReadable
argument_list|(
name|request
operator|.
name|paramAsBoolean
argument_list|(
literal|"human"
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
specifier|final
name|boolean
name|defaults
init|=
name|request
operator|.
name|paramAsBoolean
argument_list|(
literal|"include_defaults"
argument_list|,
literal|false
argument_list|)
decl_stmt|;
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
name|getIndex
argument_list|(
name|getIndexRequest
argument_list|,
operator|new
name|RestBuilderListener
argument_list|<
name|GetIndexResponse
argument_list|>
argument_list|(
name|channel
argument_list|)
block|{
block|@Override             public RestResponse buildResponse(GetIndexResponse response
operator|,
name|XContentBuilder
name|builder
block|)
throws|throws
name|Exception
block|{
name|Feature
index|[]
name|features
init|=
name|getIndexRequest
operator|.
name|features
argument_list|()
decl_stmt|;
name|String
index|[]
name|indices
init|=
name|response
operator|.
name|indices
argument_list|()
decl_stmt|;
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
for|for
control|(
name|String
name|index
range|:
name|indices
control|)
block|{
name|builder
operator|.
name|startObject
argument_list|(
name|index
argument_list|)
expr_stmt|;
for|for
control|(
name|Feature
name|feature
range|:
name|features
control|)
block|{
switch|switch
condition|(
name|feature
condition|)
block|{
case|case
name|ALIASES
case|:
name|writeAliases
argument_list|(
name|response
operator|.
name|aliases
argument_list|()
operator|.
name|get
argument_list|(
name|index
argument_list|)
argument_list|,
name|builder
argument_list|,
name|request
argument_list|)
expr_stmt|;
break|break;
case|case
name|MAPPINGS
case|:
name|writeMappings
argument_list|(
name|response
operator|.
name|mappings
argument_list|()
operator|.
name|get
argument_list|(
name|index
argument_list|)
argument_list|,
name|builder
argument_list|,
name|request
argument_list|)
expr_stmt|;
break|break;
case|case
name|SETTINGS
case|:
name|writeSettings
argument_list|(
name|response
operator|.
name|settings
argument_list|()
operator|.
name|get
argument_list|(
name|index
argument_list|)
argument_list|,
name|builder
argument_list|,
name|request
argument_list|,
name|defaults
argument_list|)
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"feature ["
operator|+
name|feature
operator|+
literal|"] is not valid"
argument_list|)
throw|;
block|}
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
return|return
operator|new
name|BytesRestResponse
argument_list|(
name|OK
argument_list|,
name|builder
argument_list|)
return|;
block|}
specifier|private
name|void
name|writeAliases
parameter_list|(
name|List
argument_list|<
name|AliasMetaData
argument_list|>
name|aliases
parameter_list|,
name|XContentBuilder
name|builder
parameter_list|,
name|Params
name|params
parameter_list|)
throws|throws
name|IOException
block|{
name|builder
operator|.
name|startObject
argument_list|(
name|Fields
operator|.
name|ALIASES
argument_list|)
expr_stmt|;
if|if
condition|(
name|aliases
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|AliasMetaData
name|alias
range|:
name|aliases
control|)
block|{
name|AliasMetaData
operator|.
name|Builder
operator|.
name|toXContent
argument_list|(
name|alias
argument_list|,
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
block|}
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|writeMappings
parameter_list|(
name|ImmutableOpenMap
argument_list|<
name|String
argument_list|,
name|MappingMetaData
argument_list|>
name|mappings
parameter_list|,
name|XContentBuilder
name|builder
parameter_list|,
name|Params
name|params
parameter_list|)
throws|throws
name|IOException
block|{
name|builder
operator|.
name|startObject
argument_list|(
name|Fields
operator|.
name|MAPPINGS
argument_list|)
expr_stmt|;
if|if
condition|(
name|mappings
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|ObjectObjectCursor
argument_list|<
name|String
argument_list|,
name|MappingMetaData
argument_list|>
name|typeEntry
range|:
name|mappings
control|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|typeEntry
operator|.
name|key
argument_list|)
expr_stmt|;
name|builder
operator|.
name|map
argument_list|(
name|typeEntry
operator|.
name|value
operator|.
name|sourceAsMap
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
specifier|private
name|void
name|writeSettings
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|XContentBuilder
name|builder
parameter_list|,
name|Params
name|params
parameter_list|,
name|boolean
name|defaults
parameter_list|)
throws|throws
name|IOException
block|{
name|builder
operator|.
name|startObject
argument_list|(
name|Fields
operator|.
name|SETTINGS
argument_list|)
expr_stmt|;
name|settings
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
if|if
condition|(
name|defaults
condition|)
block|{
name|builder
operator|.
name|startObject
argument_list|(
literal|"defaults"
argument_list|)
expr_stmt|;
name|settingsFilter
operator|.
name|filter
argument_list|(
name|indexScopedSettings
operator|.
name|diff
argument_list|(
name|settings
argument_list|,
name|RestGetIndicesAction
operator|.
name|this
operator|.
name|settings
argument_list|)
argument_list|)
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|request
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
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
DECL|method|responseParams
specifier|protected
name|Set
argument_list|<
name|String
argument_list|>
name|responseParams
parameter_list|()
block|{
return|return
name|Settings
operator|.
name|FORMAT_PARAMS
return|;
block|}
end_function

begin_class
DECL|class|Fields
specifier|static
class|class
name|Fields
block|{
DECL|field|ALIASES
specifier|static
specifier|final
name|String
name|ALIASES
init|=
literal|"aliases"
decl_stmt|;
DECL|field|MAPPINGS
specifier|static
specifier|final
name|String
name|MAPPINGS
init|=
literal|"mappings"
decl_stmt|;
DECL|field|SETTINGS
specifier|static
specifier|final
name|String
name|SETTINGS
init|=
literal|"settings"
decl_stmt|;
block|}
end_class

unit|}
end_unit

