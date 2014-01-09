begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Sets
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

begin_comment
comment|/**  *  */
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
DECL|field|ALLOWED_METRICS
specifier|private
specifier|final
specifier|static
name|Set
argument_list|<
name|String
argument_list|>
name|ALLOWED_METRICS
init|=
name|Sets
operator|.
name|newHashSet
argument_list|(
literal|"http"
argument_list|,
literal|"jvm"
argument_list|,
literal|"network"
argument_list|,
literal|"os"
argument_list|,
literal|"plugin"
argument_list|,
literal|"process"
argument_list|,
literal|"settings"
argument_list|,
literal|"thread_pool"
argument_list|,
literal|"transport"
argument_list|)
decl_stmt|;
annotation|@
name|Inject
DECL|method|RestNodesInfoAction
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
name|GET
argument_list|,
literal|"/_nodes"
argument_list|,
name|this
argument_list|)
expr_stmt|;
comment|// this endpoint is used for metrics, not for nodeIds, like /_nodes/fs
name|controller
operator|.
name|registerHandler
argument_list|(
name|GET
argument_list|,
literal|"/_nodes/{nodeId}"
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
literal|"/_nodes/{nodeId}/{metrics}"
argument_list|,
name|this
argument_list|)
expr_stmt|;
comment|// added this endpoint to be aligned with stats
name|controller
operator|.
name|registerHandler
argument_list|(
name|GET
argument_list|,
literal|"/_nodes/{nodeId}/info/{metrics}"
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
name|String
index|[]
name|nodeIds
decl_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|metrics
decl_stmt|;
comment|// special case like /_nodes/os (in this case os are metrics and not the nodeId)
comment|// still, /_nodes/_local (or any other node id) should work and be treated as usual
comment|// this means one must differentiate between allowed metrics and arbitrary node ids in the same place
if|if
condition|(
name|request
operator|.
name|hasParam
argument_list|(
literal|"nodeId"
argument_list|)
operator|&&
operator|!
name|request
operator|.
name|hasParam
argument_list|(
literal|"metrics"
argument_list|)
condition|)
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|metricsOrNodeIds
init|=
name|Strings
operator|.
name|splitStringByCommaToSet
argument_list|(
name|request
operator|.
name|param
argument_list|(
literal|"nodeId"
argument_list|,
literal|"_all"
argument_list|)
argument_list|)
decl_stmt|;
name|boolean
name|isMetricsOnly
init|=
name|ALLOWED_METRICS
operator|.
name|containsAll
argument_list|(
name|metricsOrNodeIds
argument_list|)
decl_stmt|;
if|if
condition|(
name|isMetricsOnly
condition|)
block|{
name|nodeIds
operator|=
operator|new
name|String
index|[]
block|{
literal|"_all"
block|}
expr_stmt|;
name|metrics
operator|=
name|metricsOrNodeIds
expr_stmt|;
block|}
else|else
block|{
name|nodeIds
operator|=
name|metricsOrNodeIds
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[]
block|{}
argument_list|)
expr_stmt|;
name|metrics
operator|=
name|Sets
operator|.
name|newHashSet
argument_list|(
literal|"_all"
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|nodeIds
operator|=
name|Strings
operator|.
name|splitStringByCommaToArray
argument_list|(
name|request
operator|.
name|param
argument_list|(
literal|"nodeId"
argument_list|,
literal|"_all"
argument_list|)
argument_list|)
expr_stmt|;
name|metrics
operator|=
name|Strings
operator|.
name|splitStringByCommaToSet
argument_list|(
name|request
operator|.
name|param
argument_list|(
literal|"metrics"
argument_list|,
literal|"_all"
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|final
name|NodesInfoRequest
name|nodesInfoRequest
init|=
operator|new
name|NodesInfoRequest
argument_list|(
name|nodeIds
argument_list|)
decl_stmt|;
name|nodesInfoRequest
operator|.
name|listenerThreaded
argument_list|(
literal|false
argument_list|)
expr_stmt|;
comment|// shortcut, dont do checks if only all is specified
if|if
condition|(
name|metrics
operator|.
name|size
argument_list|()
operator|==
literal|1
operator|&&
name|metrics
operator|.
name|contains
argument_list|(
literal|"_all"
argument_list|)
condition|)
block|{
name|nodesInfoRequest
operator|.
name|all
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|nodesInfoRequest
operator|.
name|clear
argument_list|()
expr_stmt|;
name|nodesInfoRequest
operator|.
name|settings
argument_list|(
name|metrics
operator|.
name|contains
argument_list|(
literal|"settings"
argument_list|)
argument_list|)
expr_stmt|;
name|nodesInfoRequest
operator|.
name|os
argument_list|(
name|metrics
operator|.
name|contains
argument_list|(
literal|"os"
argument_list|)
argument_list|)
expr_stmt|;
name|nodesInfoRequest
operator|.
name|process
argument_list|(
name|metrics
operator|.
name|contains
argument_list|(
literal|"process"
argument_list|)
argument_list|)
expr_stmt|;
name|nodesInfoRequest
operator|.
name|jvm
argument_list|(
name|metrics
operator|.
name|contains
argument_list|(
literal|"jvm"
argument_list|)
argument_list|)
expr_stmt|;
name|nodesInfoRequest
operator|.
name|threadPool
argument_list|(
name|metrics
operator|.
name|contains
argument_list|(
literal|"thread_pool"
argument_list|)
argument_list|)
expr_stmt|;
name|nodesInfoRequest
operator|.
name|network
argument_list|(
name|metrics
operator|.
name|contains
argument_list|(
literal|"network"
argument_list|)
argument_list|)
expr_stmt|;
name|nodesInfoRequest
operator|.
name|transport
argument_list|(
name|metrics
operator|.
name|contains
argument_list|(
literal|"transport"
argument_list|)
argument_list|)
expr_stmt|;
name|nodesInfoRequest
operator|.
name|http
argument_list|(
name|metrics
operator|.
name|contains
argument_list|(
literal|"http"
argument_list|)
argument_list|)
expr_stmt|;
name|nodesInfoRequest
operator|.
name|plugin
argument_list|(
name|metrics
operator|.
name|contains
argument_list|(
literal|"plugin"
argument_list|)
argument_list|)
expr_stmt|;
block|}
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
name|response
parameter_list|)
block|{
try|try
block|{
name|response
operator|.
name|settingsFilter
argument_list|(
name|settingsFilter
argument_list|)
expr_stmt|;
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
name|response
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
name|channel
operator|.
name|sendResponse
argument_list|(
operator|new
name|XContentRestResponse
argument_list|(
name|request
argument_list|,
name|RestStatus
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
name|Throwable
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

