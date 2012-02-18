begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.rest.action.update
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|action
operator|.
name|update
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
name|ActionListener
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
name|WriteConsistencyLevel
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
name|replication
operator|.
name|ReplicationType
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
name|update
operator|.
name|UpdateRequest
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
name|update
operator|.
name|UpdateResponse
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
name|*
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
name|RestXContentBuilder
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
name|RestStatus
operator|.
name|CREATED
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
comment|/**  */
end_comment

begin_class
DECL|class|RestUpdateAction
specifier|public
class|class
name|RestUpdateAction
extends|extends
name|BaseRestHandler
block|{
annotation|@
name|Inject
DECL|method|RestUpdateAction
specifier|public
name|RestUpdateAction
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|Client
name|client
parameter_list|,
name|RestController
name|controller
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
name|POST
argument_list|,
literal|"/{index}/{type}/{id}/_update"
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
parameter_list|)
block|{
name|UpdateRequest
name|updateRequest
init|=
operator|new
name|UpdateRequest
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
name|request
operator|.
name|param
argument_list|(
literal|"id"
argument_list|)
argument_list|)
decl_stmt|;
name|updateRequest
operator|.
name|routing
argument_list|(
name|request
operator|.
name|param
argument_list|(
literal|"routing"
argument_list|)
argument_list|)
expr_stmt|;
name|updateRequest
operator|.
name|parent
argument_list|(
name|request
operator|.
name|param
argument_list|(
literal|"parent"
argument_list|)
argument_list|)
expr_stmt|;
comment|// order is important, set it after routing, so it will set the routing
name|updateRequest
operator|.
name|timeout
argument_list|(
name|request
operator|.
name|paramAsTime
argument_list|(
literal|"timeout"
argument_list|,
name|updateRequest
operator|.
name|timeout
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|updateRequest
operator|.
name|refresh
argument_list|(
name|request
operator|.
name|paramAsBoolean
argument_list|(
literal|"refresh"
argument_list|,
name|updateRequest
operator|.
name|refresh
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|replicationType
init|=
name|request
operator|.
name|param
argument_list|(
literal|"replication"
argument_list|)
decl_stmt|;
if|if
condition|(
name|replicationType
operator|!=
literal|null
condition|)
block|{
name|updateRequest
operator|.
name|replicationType
argument_list|(
name|ReplicationType
operator|.
name|fromString
argument_list|(
name|replicationType
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|String
name|consistencyLevel
init|=
name|request
operator|.
name|param
argument_list|(
literal|"consistency"
argument_list|)
decl_stmt|;
if|if
condition|(
name|consistencyLevel
operator|!=
literal|null
condition|)
block|{
name|updateRequest
operator|.
name|consistencyLevel
argument_list|(
name|WriteConsistencyLevel
operator|.
name|fromString
argument_list|(
name|consistencyLevel
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|updateRequest
operator|.
name|percolate
argument_list|(
name|request
operator|.
name|param
argument_list|(
literal|"percolate"
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
comment|// we just send a response, no need to fork
name|updateRequest
operator|.
name|listenerThreaded
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|updateRequest
operator|.
name|script
argument_list|(
name|request
operator|.
name|param
argument_list|(
literal|"script"
argument_list|)
argument_list|)
expr_stmt|;
name|updateRequest
operator|.
name|scriptLang
argument_list|(
name|request
operator|.
name|param
argument_list|(
literal|"lang"
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|entry
range|:
name|request
operator|.
name|params
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|startsWith
argument_list|(
literal|"sp_"
argument_list|)
condition|)
block|{
name|updateRequest
operator|.
name|addScriptParam
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|substring
argument_list|(
literal|3
argument_list|)
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|updateRequest
operator|.
name|retryOnConflict
argument_list|(
name|request
operator|.
name|paramAsInt
argument_list|(
literal|"retry_on_conflict"
argument_list|,
name|updateRequest
operator|.
name|retryOnConflict
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// see if we have it in the body
if|if
condition|(
name|request
operator|.
name|hasContent
argument_list|()
condition|)
block|{
name|XContentType
name|xContentType
init|=
name|XContentFactory
operator|.
name|xContentType
argument_list|(
name|request
operator|.
name|contentByteArray
argument_list|()
argument_list|,
name|request
operator|.
name|contentByteArrayOffset
argument_list|()
argument_list|,
name|request
operator|.
name|contentLength
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|xContentType
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|content
init|=
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|xContentType
argument_list|)
operator|.
name|createParser
argument_list|(
name|request
operator|.
name|contentByteArray
argument_list|()
argument_list|,
name|request
operator|.
name|contentByteArrayOffset
argument_list|()
argument_list|,
name|request
operator|.
name|contentLength
argument_list|()
argument_list|)
operator|.
name|mapAndClose
argument_list|()
decl_stmt|;
if|if
condition|(
name|content
operator|.
name|containsKey
argument_list|(
literal|"script"
argument_list|)
condition|)
block|{
name|updateRequest
operator|.
name|script
argument_list|(
name|content
operator|.
name|get
argument_list|(
literal|"script"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|content
operator|.
name|containsKey
argument_list|(
literal|"lang"
argument_list|)
condition|)
block|{
name|updateRequest
operator|.
name|scriptLang
argument_list|(
name|content
operator|.
name|get
argument_list|(
literal|"lang"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|content
operator|.
name|containsKey
argument_list|(
literal|"params"
argument_list|)
condition|)
block|{
name|updateRequest
operator|.
name|scriptParams
argument_list|(
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|content
operator|.
name|get
argument_list|(
literal|"params"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
try|try
block|{
name|channel
operator|.
name|sendResponse
argument_list|(
operator|new
name|XContentThrowableRestResponse
argument_list|(
name|request
argument_list|,
name|e
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e1
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"Failed to send response"
argument_list|,
name|e1
argument_list|)
expr_stmt|;
block|}
return|return;
block|}
block|}
block|}
name|client
operator|.
name|update
argument_list|(
name|updateRequest
argument_list|,
operator|new
name|ActionListener
argument_list|<
name|UpdateResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onResponse
parameter_list|(
name|UpdateResponse
name|response
parameter_list|)
block|{
try|try
block|{
name|XContentBuilder
name|builder
init|=
name|RestXContentBuilder
operator|.
name|restContentBuilder
argument_list|(
name|request
argument_list|)
decl_stmt|;
name|builder
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|OK
argument_list|,
literal|true
argument_list|)
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|_INDEX
argument_list|,
name|response
operator|.
name|index
argument_list|()
argument_list|)
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|_TYPE
argument_list|,
name|response
operator|.
name|type
argument_list|()
argument_list|)
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|_ID
argument_list|,
name|response
operator|.
name|id
argument_list|()
argument_list|)
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|_VERSION
argument_list|,
name|response
operator|.
name|version
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|response
operator|.
name|matches
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|startArray
argument_list|(
name|Fields
operator|.
name|MATCHES
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|match
range|:
name|response
operator|.
name|matches
argument_list|()
control|)
block|{
name|builder
operator|.
name|value
argument_list|(
name|match
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endArray
argument_list|()
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|RestStatus
name|status
init|=
name|OK
decl_stmt|;
if|if
condition|(
name|response
operator|.
name|version
argument_list|()
operator|==
literal|1
condition|)
block|{
name|status
operator|=
name|CREATED
expr_stmt|;
block|}
name|channel
operator|.
name|sendResponse
argument_list|(
operator|new
name|XContentRestResponse
argument_list|(
name|request
argument_list|,
name|status
argument_list|,
name|builder
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|onFailure
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|onFailure
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
try|try
block|{
name|channel
operator|.
name|sendResponse
argument_list|(
operator|new
name|XContentThrowableRestResponse
argument_list|(
name|request
argument_list|,
name|e
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e1
parameter_list|)
block|{
name|logger
operator|.
name|error
argument_list|(
literal|"Failed to send failure response"
argument_list|,
name|e1
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
DECL|class|Fields
specifier|static
specifier|final
class|class
name|Fields
block|{
DECL|field|OK
specifier|static
specifier|final
name|XContentBuilderString
name|OK
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"ok"
argument_list|)
decl_stmt|;
DECL|field|_INDEX
specifier|static
specifier|final
name|XContentBuilderString
name|_INDEX
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"_index"
argument_list|)
decl_stmt|;
DECL|field|_TYPE
specifier|static
specifier|final
name|XContentBuilderString
name|_TYPE
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"_type"
argument_list|)
decl_stmt|;
DECL|field|_ID
specifier|static
specifier|final
name|XContentBuilderString
name|_ID
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"_id"
argument_list|)
decl_stmt|;
DECL|field|_VERSION
specifier|static
specifier|final
name|XContentBuilderString
name|_VERSION
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"_version"
argument_list|)
decl_stmt|;
DECL|field|MATCHES
specifier|static
specifier|final
name|XContentBuilderString
name|MATCHES
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"matches"
argument_list|)
decl_stmt|;
block|}
block|}
end_class

end_unit

