begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.rest.action.admin.cluster.state
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
name|state
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
name|ClusterState
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
name|RestStatus
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
import|import
name|java
operator|.
name|util
operator|.
name|EnumSet
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|RestClusterStateAction
specifier|public
class|class
name|RestClusterStateAction
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
DECL|method|RestClusterStateAction
specifier|public
name|RestClusterStateAction
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|RestController
name|controller
parameter_list|,
name|Client
name|client
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
literal|"/_cluster/state"
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
literal|"/_cluster/state/{metric}"
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
literal|"/_cluster/state/{metric}/{indices}"
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
parameter_list|,
specifier|final
name|Client
name|client
parameter_list|)
block|{
specifier|final
name|ClusterStateRequest
name|clusterStateRequest
init|=
name|Requests
operator|.
name|clusterStateRequest
argument_list|()
decl_stmt|;
name|clusterStateRequest
operator|.
name|indicesOptions
argument_list|(
name|IndicesOptions
operator|.
name|fromRequest
argument_list|(
name|request
argument_list|,
name|clusterStateRequest
operator|.
name|indicesOptions
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|clusterStateRequest
operator|.
name|local
argument_list|(
name|request
operator|.
name|paramAsBoolean
argument_list|(
literal|"local"
argument_list|,
name|clusterStateRequest
operator|.
name|local
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|clusterStateRequest
operator|.
name|masterNodeTimeout
argument_list|(
name|request
operator|.
name|paramAsTime
argument_list|(
literal|"master_timeout"
argument_list|,
name|clusterStateRequest
operator|.
name|masterNodeTimeout
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
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
literal|"indices"
argument_list|,
literal|"_all"
argument_list|)
argument_list|)
decl_stmt|;
name|boolean
name|isAllIndicesOnly
init|=
name|indices
operator|.
name|length
operator|==
literal|1
operator|&&
literal|"_all"
operator|.
name|equals
argument_list|(
name|indices
index|[
literal|0
index|]
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|isAllIndicesOnly
condition|)
block|{
name|clusterStateRequest
operator|.
name|indices
argument_list|(
name|indices
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|request
operator|.
name|hasParam
argument_list|(
literal|"metric"
argument_list|)
condition|)
block|{
name|EnumSet
argument_list|<
name|ClusterState
operator|.
name|Metric
argument_list|>
name|metrics
init|=
name|ClusterState
operator|.
name|Metric
operator|.
name|parseString
argument_list|(
name|request
operator|.
name|param
argument_list|(
literal|"metric"
argument_list|)
argument_list|,
literal|true
argument_list|)
decl_stmt|;
comment|// do not ask for what we do not need.
name|clusterStateRequest
operator|.
name|nodes
argument_list|(
name|metrics
operator|.
name|contains
argument_list|(
name|ClusterState
operator|.
name|Metric
operator|.
name|NODES
argument_list|)
operator|||
name|metrics
operator|.
name|contains
argument_list|(
name|ClusterState
operator|.
name|Metric
operator|.
name|MASTER_NODE
argument_list|)
argument_list|)
expr_stmt|;
comment|//there is no distinction in Java api between routing_table and routing_nodes, it's the same info set over the wire, one single flag to ask for it
name|clusterStateRequest
operator|.
name|routingTable
argument_list|(
name|metrics
operator|.
name|contains
argument_list|(
name|ClusterState
operator|.
name|Metric
operator|.
name|ROUTING_TABLE
argument_list|)
operator|||
name|metrics
operator|.
name|contains
argument_list|(
name|ClusterState
operator|.
name|Metric
operator|.
name|ROUTING_NODES
argument_list|)
argument_list|)
expr_stmt|;
name|clusterStateRequest
operator|.
name|metaData
argument_list|(
name|metrics
operator|.
name|contains
argument_list|(
name|ClusterState
operator|.
name|Metric
operator|.
name|METADATA
argument_list|)
argument_list|)
expr_stmt|;
name|clusterStateRequest
operator|.
name|blocks
argument_list|(
name|metrics
operator|.
name|contains
argument_list|(
name|ClusterState
operator|.
name|Metric
operator|.
name|BLOCKS
argument_list|)
argument_list|)
expr_stmt|;
name|clusterStateRequest
operator|.
name|customs
argument_list|(
name|metrics
operator|.
name|contains
argument_list|(
name|ClusterState
operator|.
name|Metric
operator|.
name|CUSTOMS
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|settingsFilter
operator|.
name|addFilterSettingParams
argument_list|(
name|request
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
name|RestBuilderListener
argument_list|<
name|ClusterStateResponse
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
name|ClusterStateResponse
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
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|CLUSTER_NAME
argument_list|,
name|response
operator|.
name|getClusterName
argument_list|()
operator|.
name|value
argument_list|()
argument_list|)
expr_stmt|;
name|response
operator|.
name|getState
argument_list|()
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
return|return
operator|new
name|BytesRestResponse
argument_list|(
name|RestStatus
operator|.
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
specifier|final
class|class
name|Fields
block|{
DECL|field|CLUSTER_NAME
specifier|static
specifier|final
name|String
name|CLUSTER_NAME
init|=
literal|"cluster_name"
decl_stmt|;
block|}
block|}
end_class

end_unit

