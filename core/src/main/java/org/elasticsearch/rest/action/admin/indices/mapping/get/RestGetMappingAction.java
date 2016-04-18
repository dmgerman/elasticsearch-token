begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.rest.action.admin.indices.mapping.get
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
operator|.
name|mapping
operator|.
name|get
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
name|mapping
operator|.
name|get
operator|.
name|GetMappingsRequest
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
name|mapping
operator|.
name|get
operator|.
name|GetMappingsResponse
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
name|Client
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
name|common
operator|.
name|xcontent
operator|.
name|XContentBuilderString
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|Index
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|IndexNotFoundException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|TypeMissingException
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
name|support
operator|.
name|RestBuilderListener
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
name|RestStatus
operator|.
name|OK
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|RestGetMappingAction
specifier|public
class|class
name|RestGetMappingAction
extends|extends
name|BaseRestHandler
block|{
annotation|@
name|Inject
DECL|method|RestGetMappingAction
specifier|public
name|RestGetMappingAction
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
name|client
argument_list|)
expr_stmt|;
name|controller
operator|.
name|registerHandler
argument_list|(
name|GET
argument_list|,
literal|"/{index}/{type}/_mapping"
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
literal|"/{index}/_mappings/{type}"
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
literal|"/{index}/_mapping/{type}"
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
literal|"/_mapping/{type}"
argument_list|,
name|this
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
name|Client
name|client
parameter_list|)
block|{
specifier|final
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
specifier|final
name|String
index|[]
name|types
init|=
name|request
operator|.
name|paramAsStringArrayOrEmptyIfAll
argument_list|(
literal|"type"
argument_list|)
decl_stmt|;
name|GetMappingsRequest
name|getMappingsRequest
init|=
operator|new
name|GetMappingsRequest
argument_list|()
decl_stmt|;
name|getMappingsRequest
operator|.
name|indices
argument_list|(
name|indices
argument_list|)
operator|.
name|types
argument_list|(
name|types
argument_list|)
expr_stmt|;
name|getMappingsRequest
operator|.
name|indicesOptions
argument_list|(
name|IndicesOptions
operator|.
name|fromRequest
argument_list|(
name|request
argument_list|,
name|getMappingsRequest
operator|.
name|indicesOptions
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|getMappingsRequest
operator|.
name|local
argument_list|(
name|request
operator|.
name|paramAsBoolean
argument_list|(
literal|"local"
argument_list|,
name|getMappingsRequest
operator|.
name|local
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|client
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|getMappings
argument_list|(
name|getMappingsRequest
argument_list|,
operator|new
name|RestBuilderListener
argument_list|<
name|GetMappingsResponse
argument_list|>
argument_list|(
name|channel
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|RestResponse
name|buildResponse
parameter_list|(
name|GetMappingsResponse
name|response
parameter_list|,
name|XContentBuilder
name|builder
parameter_list|)
throws|throws
name|Exception
block|{
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|ImmutableOpenMap
argument_list|<
name|String
argument_list|,
name|ImmutableOpenMap
argument_list|<
name|String
argument_list|,
name|MappingMetaData
argument_list|>
argument_list|>
name|mappingsByIndex
init|=
name|response
operator|.
name|getMappings
argument_list|()
decl_stmt|;
if|if
condition|(
name|mappingsByIndex
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
if|if
condition|(
name|indices
operator|.
name|length
operator|!=
literal|0
operator|&&
name|types
operator|.
name|length
operator|!=
literal|0
condition|)
block|{
return|return
operator|new
name|BytesRestResponse
argument_list|(
name|OK
argument_list|,
name|builder
operator|.
name|endObject
argument_list|()
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|indices
operator|.
name|length
operator|!=
literal|0
condition|)
block|{
return|return
operator|new
name|BytesRestResponse
argument_list|(
name|channel
argument_list|,
operator|new
name|IndexNotFoundException
argument_list|(
name|indices
index|[
literal|0
index|]
argument_list|)
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|types
operator|.
name|length
operator|!=
literal|0
condition|)
block|{
return|return
operator|new
name|BytesRestResponse
argument_list|(
name|channel
argument_list|,
operator|new
name|TypeMissingException
argument_list|(
literal|"_all"
argument_list|,
name|types
index|[
literal|0
index|]
argument_list|)
argument_list|)
return|;
block|}
else|else
block|{
return|return
operator|new
name|BytesRestResponse
argument_list|(
name|OK
argument_list|,
name|builder
operator|.
name|endObject
argument_list|()
argument_list|)
return|;
block|}
block|}
for|for
control|(
name|ObjectObjectCursor
argument_list|<
name|String
argument_list|,
name|ImmutableOpenMap
argument_list|<
name|String
argument_list|,
name|MappingMetaData
argument_list|>
argument_list|>
name|indexEntry
range|:
name|mappingsByIndex
control|)
block|{
if|if
condition|(
name|indexEntry
operator|.
name|value
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
continue|continue;
block|}
name|builder
operator|.
name|startObject
argument_list|(
name|indexEntry
operator|.
name|key
argument_list|)
expr_stmt|;
name|builder
operator|.
name|startObject
argument_list|(
name|Fields
operator|.
name|MAPPINGS
argument_list|)
expr_stmt|;
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
name|indexEntry
operator|.
name|value
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
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
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
block|}
argument_list|)
expr_stmt|;
block|}
DECL|class|Fields
specifier|static
class|class
name|Fields
block|{
DECL|field|MAPPINGS
specifier|static
specifier|final
name|XContentBuilderString
name|MAPPINGS
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"mappings"
argument_list|)
decl_stmt|;
block|}
block|}
end_class

end_unit

