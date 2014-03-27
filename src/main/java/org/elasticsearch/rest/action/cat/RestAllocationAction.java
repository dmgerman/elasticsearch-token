begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|com
operator|.
name|carrotsearch
operator|.
name|hppc
operator|.
name|ObjectIntOpenHashMap
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
name|cluster
operator|.
name|routing
operator|.
name|ShardRouting
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
name|XContentThrowableRestResponse
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
DECL|class|RestAllocationAction
specifier|public
class|class
name|RestAllocationAction
extends|extends
name|AbstractCatAction
block|{
annotation|@
name|Inject
DECL|method|RestAllocationAction
specifier|public
name|RestAllocationAction
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
literal|"/_cat/allocation"
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
literal|"/_cat/allocation/{nodes}"
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
literal|"/_cat/allocation\n"
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
name|String
index|[]
name|nodes
init|=
name|Strings
operator|.
name|splitStringByCommaToArray
argument_list|(
name|request
operator|.
name|param
argument_list|(
literal|"nodes"
argument_list|)
argument_list|)
decl_stmt|;
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
name|clear
argument_list|()
operator|.
name|routingTable
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
name|state
parameter_list|)
block|{
name|NodesStatsRequest
name|statsRequest
init|=
operator|new
name|NodesStatsRequest
argument_list|(
name|nodes
argument_list|)
decl_stmt|;
name|statsRequest
operator|.
name|clear
argument_list|()
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
name|statsRequest
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
name|stats
parameter_list|)
block|{
try|try
block|{
name|Table
name|tab
init|=
name|buildTable
argument_list|(
name|request
argument_list|,
name|state
argument_list|,
name|stats
argument_list|)
decl_stmt|;
name|channel
operator|.
name|sendResponse
argument_list|(
name|RestTable
operator|.
name|buildResponse
argument_list|(
name|tab
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
DECL|method|getTableWithHeader
name|Table
name|getTableWithHeader
parameter_list|(
specifier|final
name|RestRequest
name|request
parameter_list|)
block|{
specifier|final
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
literal|"shards"
argument_list|,
literal|"alias:s;text-align:right;desc:number of shards on node"
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
literal|"disk.used"
argument_list|,
literal|"alias:du,diskUsed;text-align:right;desc:disk used (total, not just ES)"
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
literal|"disk.avail"
argument_list|,
literal|"alias:da,diskAvail;text-align:right;desc:disk available"
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
literal|"disk.total"
argument_list|,
literal|"alias:dt,diskTotal;text-align:right;desc:total capacity of all volumes"
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
literal|"disk.percent"
argument_list|,
literal|"alias:dp,diskPercent;text-align:right;desc:percent disk used"
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
literal|"host"
argument_list|,
literal|"alias:h;desc:host of node"
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
literal|"ip"
argument_list|,
literal|"desc:ip of node"
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
literal|"node"
argument_list|,
literal|"alias:n;desc:name of node"
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
name|request
parameter_list|,
specifier|final
name|ClusterStateResponse
name|state
parameter_list|,
specifier|final
name|NodesStatsResponse
name|stats
parameter_list|)
block|{
specifier|final
name|ObjectIntOpenHashMap
argument_list|<
name|String
argument_list|>
name|allocs
init|=
operator|new
name|ObjectIntOpenHashMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|ShardRouting
name|shard
range|:
name|state
operator|.
name|getState
argument_list|()
operator|.
name|routingTable
argument_list|()
operator|.
name|allShards
argument_list|()
control|)
block|{
name|String
name|nodeId
init|=
literal|"UNASSIGNED"
decl_stmt|;
if|if
condition|(
name|shard
operator|.
name|assignedToNode
argument_list|()
condition|)
block|{
name|nodeId
operator|=
name|shard
operator|.
name|currentNodeId
argument_list|()
expr_stmt|;
block|}
name|allocs
operator|.
name|addTo
argument_list|(
name|nodeId
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
name|Table
name|table
init|=
name|getTableWithHeader
argument_list|(
name|request
argument_list|)
decl_stmt|;
for|for
control|(
name|NodeStats
name|nodeStats
range|:
name|stats
operator|.
name|getNodes
argument_list|()
control|)
block|{
name|DiscoveryNode
name|node
init|=
name|nodeStats
operator|.
name|getNode
argument_list|()
decl_stmt|;
name|int
name|shardCount
init|=
literal|0
decl_stmt|;
if|if
condition|(
name|allocs
operator|.
name|containsKey
argument_list|(
name|node
operator|.
name|id
argument_list|()
argument_list|)
condition|)
block|{
name|shardCount
operator|=
name|allocs
operator|.
name|lget
argument_list|()
expr_stmt|;
block|}
name|long
name|used
init|=
name|nodeStats
operator|.
name|getFs
argument_list|()
operator|.
name|getTotal
argument_list|()
operator|.
name|getTotal
argument_list|()
operator|.
name|bytes
argument_list|()
operator|-
name|nodeStats
operator|.
name|getFs
argument_list|()
operator|.
name|getTotal
argument_list|()
operator|.
name|getAvailable
argument_list|()
operator|.
name|bytes
argument_list|()
decl_stmt|;
name|long
name|avail
init|=
name|nodeStats
operator|.
name|getFs
argument_list|()
operator|.
name|getTotal
argument_list|()
operator|.
name|getAvailable
argument_list|()
operator|.
name|bytes
argument_list|()
decl_stmt|;
name|short
name|diskPercent
init|=
operator|-
literal|1
decl_stmt|;
if|if
condition|(
name|used
operator|>=
literal|0
operator|&&
name|avail
operator|>=
literal|0
condition|)
block|{
name|diskPercent
operator|=
call|(
name|short
call|)
argument_list|(
name|used
operator|*
literal|100
operator|/
operator|(
name|used
operator|+
name|avail
operator|)
argument_list|)
expr_stmt|;
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
name|shardCount
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
name|used
operator|<
literal|0
condition|?
literal|null
else|:
operator|new
name|ByteSizeValue
argument_list|(
name|used
argument_list|)
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
name|avail
operator|<
literal|0
condition|?
literal|null
else|:
operator|new
name|ByteSizeValue
argument_list|(
name|avail
argument_list|)
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
name|nodeStats
operator|.
name|getFs
argument_list|()
operator|.
name|getTotal
argument_list|()
operator|.
name|getTotal
argument_list|()
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
name|diskPercent
operator|<
literal|0
condition|?
literal|null
else|:
name|diskPercent
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
name|node
operator|.
name|getHostName
argument_list|()
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
name|node
operator|.
name|getHostAddress
argument_list|()
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
if|if
condition|(
name|allocs
operator|.
name|containsKey
argument_list|(
literal|"UNASSIGNED"
argument_list|)
condition|)
block|{
name|table
operator|.
name|startRow
argument_list|()
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
name|allocs
operator|.
name|lget
argument_list|()
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
literal|"UNASSIGNED"
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

