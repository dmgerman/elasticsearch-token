begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.rest.action.admin.cluster.node.stats
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
name|stats
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
name|stats
operator|.
name|NodesStatsRequest
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
name|stats
operator|.
name|NodesStatsResponse
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
name|stats
operator|.
name|CommonStatsFlags
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
name|stats
operator|.
name|CommonStatsFlags
operator|.
name|Flag
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
DECL|class|RestNodesStatsAction
specifier|public
class|class
name|RestNodesStatsAction
extends|extends
name|BaseRestHandler
block|{
annotation|@
name|Inject
DECL|method|RestNodesStatsAction
specifier|public
name|RestNodesStatsAction
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
name|GET
argument_list|,
literal|"/_nodes/stats"
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
literal|"/_nodes/{nodeId}/stats"
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
literal|"/_nodes/stats/{metric}"
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
literal|"/_nodes/{nodeId}/stats/{metric}"
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
literal|"/_nodes/stats/{metric}/{indexMetric}"
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
literal|"/_nodes/stats/{metric}/{indexMetric}/{fields}"
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
literal|"/_nodes/{nodeId}/stats/{metric}/{indexMetric}"
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
literal|"/_nodes/{nodeId}/stats/{metric}/{indexMetric}/{fields}"
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
name|String
index|[]
name|nodesIds
init|=
name|Strings
operator|.
name|splitStringByCommaToArray
argument_list|(
name|request
operator|.
name|param
argument_list|(
literal|"nodeId"
argument_list|)
argument_list|)
decl_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|metrics
init|=
name|Strings
operator|.
name|splitStringByCommaToSet
argument_list|(
name|request
operator|.
name|param
argument_list|(
literal|"metric"
argument_list|,
literal|"_all"
argument_list|)
argument_list|)
decl_stmt|;
name|NodesStatsRequest
name|nodesStatsRequest
init|=
operator|new
name|NodesStatsRequest
argument_list|(
name|nodesIds
argument_list|)
decl_stmt|;
name|nodesStatsRequest
operator|.
name|listenerThreaded
argument_list|(
literal|false
argument_list|)
expr_stmt|;
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
name|nodesStatsRequest
operator|.
name|all
argument_list|()
expr_stmt|;
name|nodesStatsRequest
operator|.
name|indices
argument_list|(
name|CommonStatsFlags
operator|.
name|ALL
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|nodesStatsRequest
operator|.
name|clear
argument_list|()
expr_stmt|;
name|nodesStatsRequest
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
name|nodesStatsRequest
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
name|nodesStatsRequest
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
name|nodesStatsRequest
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
name|nodesStatsRequest
operator|.
name|fs
argument_list|(
name|metrics
operator|.
name|contains
argument_list|(
literal|"fs"
argument_list|)
argument_list|)
expr_stmt|;
name|nodesStatsRequest
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
name|nodesStatsRequest
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
name|nodesStatsRequest
operator|.
name|indices
argument_list|(
name|metrics
operator|.
name|contains
argument_list|(
literal|"indices"
argument_list|)
argument_list|)
expr_stmt|;
name|nodesStatsRequest
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
name|nodesStatsRequest
operator|.
name|breaker
argument_list|(
name|metrics
operator|.
name|contains
argument_list|(
literal|"breaker"
argument_list|)
argument_list|)
expr_stmt|;
comment|// check for index specific metrics
if|if
condition|(
name|metrics
operator|.
name|contains
argument_list|(
literal|"indices"
argument_list|)
condition|)
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|indexMetrics
init|=
name|Strings
operator|.
name|splitStringByCommaToSet
argument_list|(
name|request
operator|.
name|param
argument_list|(
literal|"indexMetric"
argument_list|,
literal|"_all"
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|indexMetrics
operator|.
name|size
argument_list|()
operator|==
literal|1
operator|&&
name|indexMetrics
operator|.
name|contains
argument_list|(
literal|"_all"
argument_list|)
condition|)
block|{
name|nodesStatsRequest
operator|.
name|indices
argument_list|(
name|CommonStatsFlags
operator|.
name|ALL
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|CommonStatsFlags
name|flags
init|=
operator|new
name|CommonStatsFlags
argument_list|()
decl_stmt|;
for|for
control|(
name|Flag
name|flag
range|:
name|CommonStatsFlags
operator|.
name|Flag
operator|.
name|values
argument_list|()
control|)
block|{
name|flags
operator|.
name|set
argument_list|(
name|flag
argument_list|,
name|indexMetrics
operator|.
name|contains
argument_list|(
name|flag
operator|.
name|getRestName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|nodesStatsRequest
operator|.
name|indices
argument_list|(
name|flags
argument_list|)
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|nodesStatsRequest
operator|.
name|indices
argument_list|()
operator|.
name|isSet
argument_list|(
name|Flag
operator|.
name|FieldData
argument_list|)
operator|&&
operator|(
name|request
operator|.
name|hasParam
argument_list|(
literal|"fields"
argument_list|)
operator|||
name|request
operator|.
name|hasParam
argument_list|(
literal|"fielddata_fields"
argument_list|)
operator|)
condition|)
block|{
name|nodesStatsRequest
operator|.
name|indices
argument_list|()
operator|.
name|fieldDataFields
argument_list|(
name|request
operator|.
name|paramAsStringArray
argument_list|(
literal|"fielddata_fields"
argument_list|,
name|request
operator|.
name|paramAsStringArray
argument_list|(
literal|"fields"
argument_list|,
literal|null
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|nodesStatsRequest
operator|.
name|indices
argument_list|()
operator|.
name|isSet
argument_list|(
name|Flag
operator|.
name|Completion
argument_list|)
operator|&&
operator|(
name|request
operator|.
name|hasParam
argument_list|(
literal|"fields"
argument_list|)
operator|||
name|request
operator|.
name|hasParam
argument_list|(
literal|"completion_fields"
argument_list|)
operator|)
condition|)
block|{
name|nodesStatsRequest
operator|.
name|indices
argument_list|()
operator|.
name|completionDataFields
argument_list|(
name|request
operator|.
name|paramAsStringArray
argument_list|(
literal|"completion_fields"
argument_list|,
name|request
operator|.
name|paramAsStringArray
argument_list|(
literal|"fields"
argument_list|,
literal|null
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|nodesStatsRequest
operator|.
name|indices
argument_list|()
operator|.
name|isSet
argument_list|(
name|Flag
operator|.
name|Search
argument_list|)
operator|&&
operator|(
name|request
operator|.
name|hasParam
argument_list|(
literal|"groups"
argument_list|)
operator|)
condition|)
block|{
name|nodesStatsRequest
operator|.
name|indices
argument_list|()
operator|.
name|groups
argument_list|(
name|request
operator|.
name|paramAsStringArray
argument_list|(
literal|"groups"
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|nodesStatsRequest
operator|.
name|indices
argument_list|()
operator|.
name|isSet
argument_list|(
name|Flag
operator|.
name|Indexing
argument_list|)
operator|&&
operator|(
name|request
operator|.
name|hasParam
argument_list|(
literal|"types"
argument_list|)
operator|)
condition|)
block|{
name|nodesStatsRequest
operator|.
name|indices
argument_list|()
operator|.
name|types
argument_list|(
name|request
operator|.
name|paramAsStringArray
argument_list|(
literal|"types"
argument_list|,
literal|null
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
name|nodesStats
argument_list|(
name|nodesStatsRequest
argument_list|,
operator|new
name|ActionListener
argument_list|<
name|NodesStatsResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onResponse
parameter_list|(
name|NodesStatsResponse
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

