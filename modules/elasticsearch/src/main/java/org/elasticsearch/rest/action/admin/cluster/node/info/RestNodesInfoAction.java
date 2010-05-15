begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.rest.action.admin.cluster.node.info
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
name|cluster
operator|.
name|node
operator|.
name|info
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
name|node
operator|.
name|info
operator|.
name|NodeInfo
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
name|node
operator|.
name|info
operator|.
name|NodesInfoRequest
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
name|node
operator|.
name|info
operator|.
name|NodesInfoResponse
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
name|org
operator|.
name|elasticsearch
operator|.
name|util
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
name|util
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
name|util
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
name|util
operator|.
name|xcontent
operator|.
name|builder
operator|.
name|XContentBuilder
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

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|RestNodesInfoAction
specifier|public
class|class
name|RestNodesInfoAction
extends|extends
name|BaseRestHandler
block|{
DECL|field|settingsFilter
specifier|private
specifier|final
name|SettingsFilter
name|settingsFilter
decl_stmt|;
DECL|method|RestNodesInfoAction
annotation|@
name|Inject
specifier|public
name|RestNodesInfoAction
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
name|RestRequest
operator|.
name|Method
operator|.
name|GET
argument_list|,
literal|"/_cluster/nodes"
argument_list|,
name|this
argument_list|)
expr_stmt|;
name|controller
operator|.
name|registerHandler
argument_list|(
name|RestRequest
operator|.
name|Method
operator|.
name|GET
argument_list|,
literal|"/_cluster/nodes/{nodeId}"
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
name|String
index|[]
name|nodesIds
init|=
name|RestActions
operator|.
name|splitNodes
argument_list|(
name|request
operator|.
name|param
argument_list|(
literal|"nodeId"
argument_list|)
argument_list|)
decl_stmt|;
specifier|final
name|boolean
name|includeSettings
init|=
name|request
operator|.
name|paramAsBoolean
argument_list|(
literal|"settings"
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|NodesInfoRequest
name|nodesInfoRequest
init|=
operator|new
name|NodesInfoRequest
argument_list|(
name|nodesIds
argument_list|)
decl_stmt|;
name|nodesInfoRequest
operator|.
name|listenerThreaded
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
name|nodesInfo
argument_list|(
name|nodesInfoRequest
argument_list|,
operator|new
name|ActionListener
argument_list|<
name|NodesInfoResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onResponse
parameter_list|(
name|NodesInfoResponse
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
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"cluster_name"
argument_list|,
name|result
operator|.
name|clusterName
argument_list|()
operator|.
name|value
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|startObject
argument_list|(
literal|"nodes"
argument_list|)
expr_stmt|;
for|for
control|(
name|NodeInfo
name|nodeInfo
range|:
name|result
control|)
block|{
name|builder
operator|.
name|startObject
argument_list|(
name|nodeInfo
operator|.
name|node
argument_list|()
operator|.
name|id
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"name"
argument_list|,
name|nodeInfo
operator|.
name|node
argument_list|()
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"transport_address"
argument_list|,
name|nodeInfo
operator|.
name|node
argument_list|()
operator|.
name|address
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|startArray
argument_list|(
literal|"attributes"
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
name|attr
range|:
name|nodeInfo
operator|.
name|node
argument_list|()
operator|.
name|attributes
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
name|attr
operator|.
name|getKey
argument_list|()
argument_list|,
name|attr
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endArray
argument_list|()
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
name|nodeAttribute
range|:
name|nodeInfo
operator|.
name|attributes
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
name|nodeAttribute
operator|.
name|getKey
argument_list|()
argument_list|,
name|nodeAttribute
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|includeSettings
condition|)
block|{
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
name|nodeInfo
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
block|}
if|if
condition|(
name|nodeInfo
operator|.
name|os
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|nodeInfo
operator|.
name|os
argument_list|()
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|request
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|nodeInfo
operator|.
name|process
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|nodeInfo
operator|.
name|process
argument_list|()
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|request
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|nodeInfo
operator|.
name|jvm
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|nodeInfo
operator|.
name|jvm
argument_list|()
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|request
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|nodeInfo
operator|.
name|network
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|nodeInfo
operator|.
name|network
argument_list|()
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|request
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|nodeInfo
operator|.
name|threadPool
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|nodeInfo
operator|.
name|threadPool
argument_list|()
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|request
argument_list|)
expr_stmt|;
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
name|RestResponse
operator|.
name|Status
operator|.
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
block|}
end_class

end_unit

