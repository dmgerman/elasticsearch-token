begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.rest.action.admin.indices.settings
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
name|settings
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
name|admin
operator|.
name|cluster
operator|.
name|state
operator|.
name|ClusterStateRequest
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
name|cluster
operator|.
name|state
operator|.
name|ClusterStateResponse
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
name|cluster
operator|.
name|metadata
operator|.
name|IndexMetaData
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
name|MetaData
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
name|XContentBuilder
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
name|indices
operator|.
name|IndexMissingException
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
name|RestActions
operator|.
name|splitIndices
import|;
end_import

begin_class
DECL|class|RestGetSettingsAction
specifier|public
class|class
name|RestGetSettingsAction
extends|extends
name|BaseRestHandler
block|{
DECL|field|settingsFilter
specifier|private
specifier|final
name|SettingsFilter
name|settingsFilter
decl_stmt|;
annotation|@
name|Inject
DECL|method|RestGetSettingsAction
specifier|public
name|RestGetSettingsAction
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|Client
name|client
parameter_list|,
name|RestController
name|controller
parameter_list|,
name|SettingsFilter
name|settingsFilter
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
literal|"/_settings"
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
literal|"/{index}/_settings"
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
specifier|final
name|String
index|[]
name|indices
init|=
name|splitIndices
argument_list|(
name|request
operator|.
name|param
argument_list|(
literal|"index"
argument_list|)
argument_list|)
decl_stmt|;
name|ClusterStateRequest
name|clusterStateRequest
init|=
name|Requests
operator|.
name|clusterStateRequest
argument_list|()
operator|.
name|setFilterRoutingTable
argument_list|(
literal|true
argument_list|)
operator|.
name|setFilterNodes
argument_list|(
literal|true
argument_list|)
operator|.
name|setFilteredIndices
argument_list|(
name|indices
argument_list|)
decl_stmt|;
name|clusterStateRequest
operator|.
name|setListenerThreaded
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|client
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|state
argument_list|(
name|clusterStateRequest
argument_list|,
operator|new
name|ActionListener
argument_list|<
name|ClusterStateResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onResponse
parameter_list|(
name|ClusterStateResponse
name|response
parameter_list|)
block|{
try|try
block|{
name|MetaData
name|metaData
init|=
name|response
operator|.
name|getState
argument_list|()
operator|.
name|metaData
argument_list|()
decl_stmt|;
if|if
condition|(
name|metaData
operator|.
name|indices
argument_list|()
operator|.
name|isEmpty
argument_list|()
operator|&&
name|indices
operator|.
name|length
operator|>
literal|0
condition|)
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
operator|new
name|IndexMissingException
argument_list|(
operator|new
name|Index
argument_list|(
name|indices
index|[
literal|0
index|]
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
return|return;
block|}
name|boolean
name|foundAny
init|=
literal|false
decl_stmt|;
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
expr_stmt|;
for|for
control|(
name|IndexMetaData
name|indexMetaData
range|:
name|metaData
control|)
block|{
name|builder
operator|.
name|startObject
argument_list|(
name|indexMetaData
operator|.
name|index
argument_list|()
argument_list|,
name|XContentBuilder
operator|.
name|FieldCaseConversion
operator|.
name|NONE
argument_list|)
expr_stmt|;
name|foundAny
operator|=
literal|true
expr_stmt|;
name|builder
operator|.
name|startObject
argument_list|(
literal|"settings"
argument_list|)
expr_stmt|;
name|Settings
name|settings
init|=
name|settingsFilter
operator|.
name|filterSettings
argument_list|(
name|indexMetaData
operator|.
name|settings
argument_list|()
argument_list|)
decl_stmt|;
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
name|settings
operator|.
name|getAsMap
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|entry
operator|.
name|getValue
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
name|channel
operator|.
name|sendResponse
argument_list|(
operator|new
name|XContentRestResponse
argument_list|(
name|request
argument_list|,
name|foundAny
condition|?
name|OK
else|:
name|NOT_FOUND
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
block|}
end_class

end_unit

