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
name|admin
operator|.
name|indices
operator|.
name|stats
operator|.
name|CommonStats
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
name|IndicesStatsRequest
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
name|IndicesStatsResponse
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

begin_class
DECL|class|RestShardsAction
specifier|public
class|class
name|RestShardsAction
extends|extends
name|BaseRestHandler
block|{
annotation|@
name|Inject
DECL|method|RestShardsAction
specifier|public
name|RestShardsAction
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
literal|"/_cat/shards"
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
literal|"/_cat/shards/{index}"
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
literal|"index"
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
specifier|final
name|String
index|[]
name|concreteIndices
init|=
name|clusterStateResponse
operator|.
name|getState
argument_list|()
operator|.
name|metaData
argument_list|()
operator|.
name|concreteIndicesIgnoreMissing
argument_list|(
name|indices
argument_list|)
decl_stmt|;
name|IndicesStatsRequest
name|indicesStatsRequest
init|=
operator|new
name|IndicesStatsRequest
argument_list|()
decl_stmt|;
name|indicesStatsRequest
operator|.
name|clear
argument_list|()
operator|.
name|docs
argument_list|(
literal|true
argument_list|)
operator|.
name|store
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|client
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|stats
argument_list|(
name|indicesStatsRequest
argument_list|,
operator|new
name|ActionListener
argument_list|<
name|IndicesStatsResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onResponse
parameter_list|(
name|IndicesStatsResponse
name|indicesStatsResponse
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
name|concreteIndices
argument_list|,
name|clusterStateResponse
argument_list|,
name|indicesStatsResponse
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
DECL|method|buildTable
specifier|private
name|Table
name|buildTable
parameter_list|(
name|String
index|[]
name|concreteIndices
parameter_list|,
name|ClusterStateResponse
name|state
parameter_list|,
name|IndicesStatsResponse
name|stats
parameter_list|)
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|indices
init|=
name|Sets
operator|.
name|newHashSet
argument_list|(
name|concreteIndices
argument_list|)
decl_stmt|;
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
operator|.
name|addCell
argument_list|(
literal|"index"
argument_list|,
literal|"default:true;"
argument_list|)
operator|.
name|addCell
argument_list|(
literal|"shard"
argument_list|,
literal|"default:true;"
argument_list|)
operator|.
name|addCell
argument_list|(
literal|"p/r"
argument_list|,
literal|"default:true;"
argument_list|)
operator|.
name|addCell
argument_list|(
literal|"state"
argument_list|,
literal|"default:true;"
argument_list|)
operator|.
name|addCell
argument_list|(
literal|"docs"
argument_list|,
literal|"text-align:right;"
argument_list|)
operator|.
name|addCell
argument_list|(
literal|"store"
argument_list|,
literal|"text-align:right;"
argument_list|)
operator|.
name|addCell
argument_list|(
literal|"ip"
argument_list|,
literal|"default:true;"
argument_list|)
operator|.
name|addCell
argument_list|(
literal|"node"
argument_list|,
literal|"default:true;"
argument_list|)
operator|.
name|endHeaders
argument_list|()
expr_stmt|;
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
if|if
condition|(
operator|!
name|indices
operator|.
name|contains
argument_list|(
name|shard
operator|.
name|index
argument_list|()
argument_list|)
condition|)
block|{
continue|continue;
block|}
name|CommonStats
name|shardStats
init|=
name|stats
operator|.
name|asMap
argument_list|()
operator|.
name|get
argument_list|(
name|shard
argument_list|)
decl_stmt|;
name|table
operator|.
name|startRow
argument_list|()
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
name|shard
operator|.
name|index
argument_list|()
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
name|shard
operator|.
name|id
argument_list|()
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
name|shard
operator|.
name|primary
argument_list|()
condition|?
literal|"p"
else|:
literal|"r"
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
name|shard
operator|.
name|state
argument_list|()
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
name|shardStats
operator|==
literal|null
condition|?
literal|null
else|:
name|shardStats
operator|.
name|getDocs
argument_list|()
operator|.
name|getCount
argument_list|()
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
name|shardStats
operator|==
literal|null
condition|?
literal|null
else|:
name|shardStats
operator|.
name|getStore
argument_list|()
operator|.
name|getSize
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|shard
operator|.
name|assignedToNode
argument_list|()
condition|)
block|{
name|String
name|ip
init|=
operator|(
operator|(
name|InetSocketTransportAddress
operator|)
name|state
operator|.
name|getState
argument_list|()
operator|.
name|nodes
argument_list|()
operator|.
name|get
argument_list|(
name|shard
operator|.
name|currentNodeId
argument_list|()
argument_list|)
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
decl_stmt|;
name|StringBuilder
name|name
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|name
operator|.
name|append
argument_list|(
name|state
operator|.
name|getState
argument_list|()
operator|.
name|nodes
argument_list|()
operator|.
name|get
argument_list|(
name|shard
operator|.
name|currentNodeId
argument_list|()
argument_list|)
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|shard
operator|.
name|relocating
argument_list|()
condition|)
block|{
name|String
name|reloIp
init|=
operator|(
operator|(
name|InetSocketTransportAddress
operator|)
name|state
operator|.
name|getState
argument_list|()
operator|.
name|nodes
argument_list|()
operator|.
name|get
argument_list|(
name|shard
operator|.
name|relocatingNodeId
argument_list|()
argument_list|)
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
decl_stmt|;
name|String
name|reloNme
init|=
name|state
operator|.
name|getState
argument_list|()
operator|.
name|nodes
argument_list|()
operator|.
name|get
argument_list|(
name|shard
operator|.
name|relocatingNodeId
argument_list|()
argument_list|)
operator|.
name|name
argument_list|()
decl_stmt|;
name|name
operator|.
name|append
argument_list|(
literal|" -> "
argument_list|)
expr_stmt|;
name|name
operator|.
name|append
argument_list|(
name|reloIp
argument_list|)
expr_stmt|;
name|name
operator|.
name|append
argument_list|(
literal|" "
argument_list|)
expr_stmt|;
name|name
operator|.
name|append
argument_list|(
name|reloNme
argument_list|)
expr_stmt|;
block|}
name|table
operator|.
name|addCell
argument_list|(
name|ip
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
name|name
argument_list|)
expr_stmt|;
block|}
else|else
block|{
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
block|}
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

