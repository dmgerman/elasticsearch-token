begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.rest.action.bulk
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|action
operator|.
name|bulk
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
name|bulk
operator|.
name|BulkItemResponse
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
name|bulk
operator|.
name|BulkRequest
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
name|bulk
operator|.
name|BulkResponse
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
name|index
operator|.
name|IndexResponse
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
name|Requests
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
name|rest
operator|.
name|*
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
name|*
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
name|*
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
name|action
operator|.
name|support
operator|.
name|RestXContentBuilder
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  *<pre>  * { "index" : { "_index" : "test", "_type" : "type1", "_id" : "1" }  * { "type1" : { "field1" : "value1" } }  * { "delete" : { "_index" : "test", "_type" : "type1", "_id" : "2" } }  * { "create" : { "_index" : "test", "_type" : "type1", "_id" : "1" }  * { "type1" : { "field1" : "value1" } }  *</pre>  *  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|RestBulkAction
specifier|public
class|class
name|RestBulkAction
extends|extends
name|BaseRestHandler
block|{
DECL|method|RestBulkAction
annotation|@
name|Inject
specifier|public
name|RestBulkAction
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
literal|"/_bulk"
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
literal|"/_bulk"
argument_list|,
name|this
argument_list|)
expr_stmt|;
block|}
DECL|method|handleRequest
annotation|@
name|Override
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
name|BulkRequest
name|bulkRequest
init|=
name|Requests
operator|.
name|bulkRequest
argument_list|()
decl_stmt|;
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
name|bulkRequest
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
name|bulkRequest
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
name|bulkRequest
operator|.
name|refresh
argument_list|(
name|request
operator|.
name|paramAsBoolean
argument_list|(
literal|"refresh"
argument_list|,
name|bulkRequest
operator|.
name|refresh
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
try|try
block|{
name|bulkRequest
operator|.
name|add
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
argument_list|,
name|request
operator|.
name|contentUnsafe
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
try|try
block|{
name|XContentBuilder
name|builder
init|=
name|restContentBuilder
argument_list|(
name|request
argument_list|)
decl_stmt|;
name|channel
operator|.
name|sendResponse
argument_list|(
operator|new
name|XContentRestResponse
argument_list|(
name|request
argument_list|,
name|BAD_REQUEST
argument_list|,
name|builder
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"error"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
operator|.
name|endObject
argument_list|()
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
return|return;
block|}
name|client
operator|.
name|bulk
argument_list|(
name|bulkRequest
argument_list|,
operator|new
name|ActionListener
argument_list|<
name|BulkResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onResponse
parameter_list|(
name|BulkResponse
name|response
parameter_list|)
block|{
try|try
block|{
name|XContentBuilder
name|builder
init|=
name|restContentBuilder
argument_list|(
name|request
argument_list|)
decl_stmt|;
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|TOOK
argument_list|,
name|response
operator|.
name|tookInMillis
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|startArray
argument_list|(
name|Fields
operator|.
name|ITEMS
argument_list|)
expr_stmt|;
for|for
control|(
name|BulkItemResponse
name|itemResponse
range|:
name|response
control|)
block|{
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|startObject
argument_list|(
name|itemResponse
operator|.
name|opType
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|_INDEX
argument_list|,
name|itemResponse
operator|.
name|index
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|_TYPE
argument_list|,
name|itemResponse
operator|.
name|type
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|_ID
argument_list|,
name|itemResponse
operator|.
name|id
argument_list|()
argument_list|)
expr_stmt|;
name|long
name|version
init|=
name|itemResponse
operator|.
name|version
argument_list|()
decl_stmt|;
if|if
condition|(
name|version
operator|!=
operator|-
literal|1
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|_VERSION
argument_list|,
name|itemResponse
operator|.
name|version
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|itemResponse
operator|.
name|failed
argument_list|()
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|ERROR
argument_list|,
name|itemResponse
operator|.
name|failure
argument_list|()
operator|.
name|message
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|OK
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|itemResponse
operator|.
name|response
argument_list|()
operator|instanceof
name|IndexResponse
condition|)
block|{
name|IndexResponse
name|indexResponse
init|=
name|itemResponse
operator|.
name|response
argument_list|()
decl_stmt|;
if|if
condition|(
name|indexResponse
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
name|indexResponse
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
name|endArray
argument_list|()
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|channel
operator|.
name|sendResponse
argument_list|(
operator|new
name|XContentRestResponse
argument_list|(
name|request
argument_list|,
name|OK
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
DECL|field|ITEMS
specifier|static
specifier|final
name|XContentBuilderString
name|ITEMS
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"items"
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
DECL|field|ERROR
specifier|static
specifier|final
name|XContentBuilderString
name|ERROR
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"error"
argument_list|)
decl_stmt|;
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
DECL|field|TOOK
specifier|static
specifier|final
name|XContentBuilderString
name|TOOK
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"took"
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

