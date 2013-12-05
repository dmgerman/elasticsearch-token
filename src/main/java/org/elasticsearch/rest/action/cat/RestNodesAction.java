begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.rest.action.cat
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|action
operator|.
name|cat
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
name|NodeStats
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
name|cluster
operator|.
name|node
operator|.
name|DiscoveryNode
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
name|Table
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
name|transport
operator|.
name|InetSocketTransportAddress
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
name|unit
operator|.
name|ByteSizeValue
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|monitor
operator|.
name|fs
operator|.
name|FsStats
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
name|RestTable
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
name|Iterator
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Locale
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

begin_class
DECL|class|RestNodesAction
specifier|public
class|class
name|RestNodesAction
extends|extends
name|AbstractCatAction
block|{
annotation|@
name|Inject
DECL|method|RestNodesAction
specifier|public
name|RestNodesAction
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
literal|"/_cat/nodes"
argument_list|,
name|this
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|documentation
name|void
name|documentation
parameter_list|(
name|StringBuilder
name|sb
parameter_list|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"/_cat/nodes\n"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doRequest
specifier|public
name|void
name|doRequest
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
name|ClusterStateRequest
name|clusterStateRequest
init|=
operator|new
name|ClusterStateRequest
argument_list|()
decl_stmt|;
name|clusterStateRequest
operator|.
name|filterMetaData
argument_list|(
literal|true
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
specifier|final
name|ClusterStateResponse
name|clusterStateResponse
parameter_list|)
block|{
name|NodesInfoRequest
name|nodesInfoRequest
init|=
operator|new
name|NodesInfoRequest
argument_list|()
decl_stmt|;
name|nodesInfoRequest
operator|.
name|clear
argument_list|()
operator|.
name|jvm
argument_list|(
literal|true
argument_list|)
operator|.
name|os
argument_list|(
literal|true
argument_list|)
operator|.
name|process
argument_list|(
literal|true
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
specifier|final
name|NodesInfoResponse
name|nodesInfoResponse
parameter_list|)
block|{
name|NodesStatsRequest
name|nodesStatsRequest
init|=
operator|new
name|NodesStatsRequest
argument_list|()
decl_stmt|;
name|nodesStatsRequest
operator|.
name|clear
argument_list|()
operator|.
name|jvm
argument_list|(
literal|true
argument_list|)
operator|.
name|os
argument_list|(
literal|true
argument_list|)
operator|.
name|fs
argument_list|(
literal|true
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
name|nodesStatsResponse
parameter_list|)
block|{
try|try
block|{
name|channel
operator|.
name|sendResponse
argument_list|(
name|RestTable
operator|.
name|buildResponse
argument_list|(
name|buildTable
argument_list|(
name|request
argument_list|,
name|clusterStateResponse
argument_list|,
name|nodesInfoResponse
argument_list|,
name|nodesStatsResponse
argument_list|)
argument_list|,
name|request
argument_list|,
name|channel
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
annotation|@
name|Override
DECL|method|getTableWithHeader
name|Table
name|getTableWithHeader
parameter_list|(
specifier|final
name|RestRequest
name|request
parameter_list|)
block|{
name|Table
name|table
init|=
operator|new
name|Table
argument_list|()
decl_stmt|;
name|table
operator|.
name|startHeaders
argument_list|()
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
literal|"nodeId"
argument_list|,
literal|"desc:unique node id"
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
literal|"pid"
argument_list|,
literal|"desc:process id"
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
literal|"ip"
argument_list|,
literal|"desc:ip address"
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
literal|"port"
argument_list|,
literal|"desc:bound transport port"
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
literal|"es"
argument_list|,
literal|"desc:es version"
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
literal|"jdk"
argument_list|,
literal|"desc:jdk version"
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
literal|"diskAvail"
argument_list|,
literal|"text-align:right;desc:available disk space"
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
literal|"heapPercent"
argument_list|,
literal|"text-align:right;desc:used heap ratio"
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
literal|"heapMax"
argument_list|,
literal|"text-align:right;desc:max configured heap"
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
literal|"ramPercent"
argument_list|,
literal|"text-align:right;desc:used machine memory ratio"
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
literal|"ramMax"
argument_list|,
literal|"text-align:right;desc:total machine memory"
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
literal|"load"
argument_list|,
literal|"text-align:right;desc:most recent load avg"
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
literal|"uptime"
argument_list|,
literal|"text-align:right;desc:node uptime"
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
literal|"data/client"
argument_list|,
literal|"desc:d:data node, c:client node"
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
literal|"master"
argument_list|,
literal|"desc:m:master-eligible, *:current master"
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
literal|"name"
argument_list|,
literal|"desc:node name"
argument_list|)
expr_stmt|;
name|table
operator|.
name|endHeaders
argument_list|()
expr_stmt|;
return|return
name|table
return|;
block|}
DECL|method|buildTable
specifier|private
name|Table
name|buildTable
parameter_list|(
name|RestRequest
name|req
parameter_list|,
name|ClusterStateResponse
name|state
parameter_list|,
name|NodesInfoResponse
name|nodesInfo
parameter_list|,
name|NodesStatsResponse
name|nodesStats
parameter_list|)
block|{
name|boolean
name|fullId
init|=
name|req
operator|.
name|paramAsBoolean
argument_list|(
literal|"full_id"
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|String
name|masterId
init|=
name|state
operator|.
name|getState
argument_list|()
operator|.
name|nodes
argument_list|()
operator|.
name|masterNodeId
argument_list|()
decl_stmt|;
name|Table
name|table
init|=
name|getTableWithHeader
argument_list|(
name|req
argument_list|)
decl_stmt|;
for|for
control|(
name|DiscoveryNode
name|node
range|:
name|state
operator|.
name|getState
argument_list|()
operator|.
name|nodes
argument_list|()
control|)
block|{
name|NodeInfo
name|info
init|=
name|nodesInfo
operator|.
name|getNodesMap
argument_list|()
operator|.
name|get
argument_list|(
name|node
operator|.
name|id
argument_list|()
argument_list|)
decl_stmt|;
name|NodeStats
name|stats
init|=
name|nodesStats
operator|.
name|getNodesMap
argument_list|()
operator|.
name|get
argument_list|(
name|node
operator|.
name|id
argument_list|()
argument_list|)
decl_stmt|;
name|long
name|availableDisk
init|=
operator|-
literal|1
decl_stmt|;
name|long
name|heapUsed
init|=
operator|-
literal|1
decl_stmt|;
name|long
name|heapMax
init|=
operator|-
literal|1
decl_stmt|;
name|float
name|heapRatio
init|=
operator|-
literal|1.0f
decl_stmt|;
if|if
condition|(
literal|null
operator|!=
name|stats
operator|&&
literal|null
operator|!=
name|info
condition|)
block|{
name|heapUsed
operator|=
name|stats
operator|.
name|getJvm
argument_list|()
operator|.
name|mem
argument_list|()
operator|.
name|heapUsed
argument_list|()
operator|.
name|bytes
argument_list|()
expr_stmt|;
name|heapMax
operator|=
name|info
operator|.
name|getJvm
argument_list|()
operator|.
name|mem
argument_list|()
operator|.
name|heapMax
argument_list|()
operator|.
name|bytes
argument_list|()
expr_stmt|;
if|if
condition|(
name|heapMax
operator|>
literal|0
condition|)
block|{
name|heapRatio
operator|=
name|heapUsed
operator|/
operator|(
name|heapMax
operator|*
literal|1.0f
operator|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
operator|(
name|stats
operator|.
name|getFs
argument_list|()
operator|==
literal|null
operator|)
condition|)
block|{
name|availableDisk
operator|=
literal|0
expr_stmt|;
name|Iterator
argument_list|<
name|FsStats
operator|.
name|Info
argument_list|>
name|it
init|=
name|stats
operator|.
name|getFs
argument_list|()
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|it
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|availableDisk
operator|+=
name|it
operator|.
name|next
argument_list|()
operator|.
name|getAvailable
argument_list|()
operator|.
name|bytes
argument_list|()
expr_stmt|;
block|}
block|}
block|}
name|table
operator|.
name|startRow
argument_list|()
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
name|fullId
condition|?
name|node
operator|.
name|id
argument_list|()
else|:
name|node
operator|.
name|id
argument_list|()
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
literal|4
argument_list|)
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
name|info
operator|==
literal|null
condition|?
literal|null
else|:
name|info
operator|.
name|getProcess
argument_list|()
operator|.
name|id
argument_list|()
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
operator|(
operator|(
name|InetSocketTransportAddress
operator|)
name|node
operator|.
name|address
argument_list|()
operator|)
operator|.
name|address
argument_list|()
operator|.
name|getAddress
argument_list|()
operator|.
name|getHostAddress
argument_list|()
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
operator|(
operator|(
name|InetSocketTransportAddress
operator|)
name|node
operator|.
name|address
argument_list|()
operator|)
operator|.
name|address
argument_list|()
operator|.
name|getPort
argument_list|()
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
name|info
operator|==
literal|null
condition|?
literal|null
else|:
name|info
operator|.
name|getVersion
argument_list|()
operator|.
name|number
argument_list|()
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
name|info
operator|==
literal|null
condition|?
literal|null
else|:
name|info
operator|.
name|getJvm
argument_list|()
operator|.
name|version
argument_list|()
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
name|availableDisk
operator|<
literal|0
condition|?
literal|null
else|:
name|ByteSizeValue
operator|.
name|parseBytesSizeValue
argument_list|(
operator|new
name|Long
argument_list|(
name|availableDisk
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
name|heapRatio
operator|<
literal|0
condition|?
literal|null
else|:
name|String
operator|.
name|format
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|,
literal|"%.1f"
argument_list|,
name|heapRatio
operator|*
literal|100.0
argument_list|)
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
name|heapMax
operator|<
literal|0
condition|?
literal|null
else|:
operator|new
name|ByteSizeValue
argument_list|(
name|heapMax
argument_list|)
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
name|stats
operator|==
literal|null
condition|?
literal|null
else|:
name|stats
operator|.
name|getOs
argument_list|()
operator|.
name|mem
argument_list|()
operator|==
literal|null
condition|?
literal|null
else|:
name|stats
operator|.
name|getOs
argument_list|()
operator|.
name|mem
argument_list|()
operator|.
name|usedPercent
argument_list|()
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
name|info
operator|==
literal|null
condition|?
literal|null
else|:
name|info
operator|.
name|getOs
argument_list|()
operator|.
name|mem
argument_list|()
operator|==
literal|null
condition|?
literal|null
else|:
name|info
operator|.
name|getOs
argument_list|()
operator|.
name|mem
argument_list|()
operator|.
name|total
argument_list|()
argument_list|)
expr_stmt|;
comment|// sigar fails to load in IntelliJ
name|table
operator|.
name|addCell
argument_list|(
name|stats
operator|==
literal|null
condition|?
literal|null
else|:
name|stats
operator|.
name|getOs
argument_list|()
operator|==
literal|null
condition|?
literal|null
else|:
name|stats
operator|.
name|getOs
argument_list|()
operator|.
name|getLoadAverage
argument_list|()
operator|.
name|length
operator|<
literal|1
condition|?
literal|null
else|:
name|stats
operator|.
name|getOs
argument_list|()
operator|.
name|getLoadAverage
argument_list|()
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
name|stats
operator|==
literal|null
condition|?
literal|null
else|:
name|stats
operator|.
name|getJvm
argument_list|()
operator|.
name|uptime
argument_list|()
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
name|node
operator|.
name|clientNode
argument_list|()
condition|?
literal|"c"
else|:
name|node
operator|.
name|dataNode
argument_list|()
condition|?
literal|"d"
else|:
literal|"-"
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
name|masterId
operator|.
name|equals
argument_list|(
name|node
operator|.
name|id
argument_list|()
argument_list|)
condition|?
literal|"*"
else|:
name|node
operator|.
name|masterNode
argument_list|()
condition|?
literal|"m"
else|:
literal|"-"
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
name|node
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
name|table
operator|.
name|endRow
argument_list|()
expr_stmt|;
block|}
return|return
name|table
return|;
block|}
block|}
end_class

end_unit

