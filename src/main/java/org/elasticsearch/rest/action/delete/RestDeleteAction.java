begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.rest.action.delete
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|action
operator|.
name|delete
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
name|delete
operator|.
name|DeleteRequest
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
name|delete
operator|.
name|DeleteResponse
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
name|VersionType
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
name|DELETE
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
name|NOT_FOUND
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
DECL|class|RestDeleteAction
specifier|public
class|class
name|RestDeleteAction
extends|extends
name|BaseRestHandler
block|{
annotation|@
name|Inject
DECL|method|RestDeleteAction
specifier|public
name|RestDeleteAction
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
name|DELETE
argument_list|,
literal|"/{index}/{type}/{id}"
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
name|DeleteRequest
name|deleteRequest
init|=
operator|new
name|DeleteRequest
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
name|deleteRequest
operator|.
name|setListenerThreaded
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|deleteRequest
operator|.
name|setOperationThreaded
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|deleteRequest
operator|.
name|setParent
argument_list|(
name|request
operator|.
name|param
argument_list|(
literal|"parent"
argument_list|)
argument_list|)
expr_stmt|;
name|deleteRequest
operator|.
name|setRouting
argument_list|(
name|request
operator|.
name|param
argument_list|(
literal|"routing"
argument_list|)
argument_list|)
expr_stmt|;
name|deleteRequest
operator|.
name|setTimeout
argument_list|(
name|request
operator|.
name|paramAsTime
argument_list|(
literal|"timeout"
argument_list|,
name|DeleteRequest
operator|.
name|DEFAULT_TIMEOUT
argument_list|)
argument_list|)
expr_stmt|;
name|deleteRequest
operator|.
name|setRefresh
argument_list|(
name|request
operator|.
name|paramAsBoolean
argument_list|(
literal|"refresh"
argument_list|,
name|deleteRequest
operator|.
name|isRefresh
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|deleteRequest
operator|.
name|setVersion
argument_list|(
name|RestActions
operator|.
name|parseVersion
argument_list|(
name|request
argument_list|)
argument_list|)
expr_stmt|;
name|deleteRequest
operator|.
name|setVersionType
argument_list|(
name|VersionType
operator|.
name|fromString
argument_list|(
name|request
operator|.
name|param
argument_list|(
literal|"version_type"
argument_list|)
argument_list|,
name|deleteRequest
operator|.
name|getVersionType
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
name|deleteRequest
operator|.
name|setReplicationType
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
name|deleteRequest
operator|.
name|setConsistencyLevel
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
name|client
operator|.
name|delete
argument_list|(
name|deleteRequest
argument_list|,
operator|new
name|ActionListener
argument_list|<
name|DeleteResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onResponse
parameter_list|(
name|DeleteResponse
name|result
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
name|FOUND
argument_list|,
operator|!
name|result
operator|.
name|isNotFound
argument_list|()
argument_list|)
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|_INDEX
argument_list|,
name|result
operator|.
name|getIndex
argument_list|()
argument_list|)
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|_TYPE
argument_list|,
name|result
operator|.
name|getType
argument_list|()
argument_list|)
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|_ID
argument_list|,
name|result
operator|.
name|getId
argument_list|()
argument_list|)
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|_VERSION
argument_list|,
name|result
operator|.
name|getVersion
argument_list|()
argument_list|)
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
name|result
operator|.
name|isNotFound
argument_list|()
condition|)
block|{
name|status
operator|=
name|NOT_FOUND
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
DECL|field|FOUND
specifier|static
specifier|final
name|XContentBuilderString
name|FOUND
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"found"
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
block|}
block|}
end_class

end_unit

