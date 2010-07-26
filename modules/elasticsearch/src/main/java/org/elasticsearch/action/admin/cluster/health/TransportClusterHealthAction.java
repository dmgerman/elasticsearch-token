begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.cluster.health
package|package
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
name|health
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticSearchException
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
name|TransportActions
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
name|master
operator|.
name|TransportMasterNodeOperationAction
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
name|ClusterName
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
name|ClusterService
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
name|routing
operator|.
name|IndexRoutingTable
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
name|IndexShardRoutingTable
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
name|RoutingTableValidation
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
name|threadpool
operator|.
name|ThreadPool
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|timer
operator|.
name|TimerService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|transport
operator|.
name|TransportService
import|;
end_import

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|TransportClusterHealthAction
specifier|public
class|class
name|TransportClusterHealthAction
extends|extends
name|TransportMasterNodeOperationAction
argument_list|<
name|ClusterHealthRequest
argument_list|,
name|ClusterHealthResponse
argument_list|>
block|{
DECL|field|clusterName
specifier|private
specifier|final
name|ClusterName
name|clusterName
decl_stmt|;
DECL|field|timerService
specifier|private
specifier|final
name|TimerService
name|timerService
decl_stmt|;
DECL|method|TransportClusterHealthAction
annotation|@
name|Inject
specifier|public
name|TransportClusterHealthAction
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|TransportService
name|transportService
parameter_list|,
name|ClusterService
name|clusterService
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|,
name|TimerService
name|timerService
parameter_list|,
name|ClusterName
name|clusterName
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|transportService
argument_list|,
name|clusterService
argument_list|,
name|threadPool
argument_list|)
expr_stmt|;
name|this
operator|.
name|clusterName
operator|=
name|clusterName
expr_stmt|;
name|this
operator|.
name|timerService
operator|=
name|timerService
expr_stmt|;
block|}
DECL|method|transportAction
annotation|@
name|Override
specifier|protected
name|String
name|transportAction
parameter_list|()
block|{
return|return
name|TransportActions
operator|.
name|Admin
operator|.
name|Cluster
operator|.
name|HEALTH
return|;
block|}
DECL|method|newRequest
annotation|@
name|Override
specifier|protected
name|ClusterHealthRequest
name|newRequest
parameter_list|()
block|{
return|return
operator|new
name|ClusterHealthRequest
argument_list|()
return|;
block|}
DECL|method|newResponse
annotation|@
name|Override
specifier|protected
name|ClusterHealthResponse
name|newResponse
parameter_list|()
block|{
return|return
operator|new
name|ClusterHealthResponse
argument_list|()
return|;
block|}
DECL|method|masterOperation
annotation|@
name|Override
specifier|protected
name|ClusterHealthResponse
name|masterOperation
parameter_list|(
name|ClusterHealthRequest
name|request
parameter_list|,
name|ClusterState
name|state
parameter_list|)
throws|throws
name|ElasticSearchException
block|{
name|int
name|waitFor
init|=
literal|4
decl_stmt|;
if|if
condition|(
name|request
operator|.
name|waitForStatus
argument_list|()
operator|==
literal|null
condition|)
block|{
name|waitFor
operator|--
expr_stmt|;
block|}
if|if
condition|(
name|request
operator|.
name|waitForRelocatingShards
argument_list|()
operator|==
operator|-
literal|1
condition|)
block|{
name|waitFor
operator|--
expr_stmt|;
block|}
if|if
condition|(
name|request
operator|.
name|waitForActiveShards
argument_list|()
operator|==
operator|-
literal|1
condition|)
block|{
name|waitFor
operator|--
expr_stmt|;
block|}
if|if
condition|(
name|request
operator|.
name|waitForNodes
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|waitFor
operator|--
expr_stmt|;
block|}
if|if
condition|(
name|waitFor
operator|==
literal|0
condition|)
block|{
comment|// no need to wait for anything
return|return
name|clusterHealth
argument_list|(
name|request
argument_list|)
return|;
block|}
name|long
name|endTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|+
name|request
operator|.
name|timeout
argument_list|()
operator|.
name|millis
argument_list|()
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
name|int
name|waitForCounter
init|=
literal|0
decl_stmt|;
name|ClusterHealthResponse
name|response
init|=
name|clusterHealth
argument_list|(
name|request
argument_list|)
decl_stmt|;
if|if
condition|(
name|request
operator|.
name|waitForStatus
argument_list|()
operator|!=
literal|null
operator|&&
name|response
operator|.
name|status
argument_list|()
operator|.
name|value
argument_list|()
operator|<=
name|request
operator|.
name|waitForStatus
argument_list|()
operator|.
name|value
argument_list|()
condition|)
block|{
name|waitForCounter
operator|++
expr_stmt|;
block|}
if|if
condition|(
name|request
operator|.
name|waitForRelocatingShards
argument_list|()
operator|!=
operator|-
literal|1
operator|&&
name|response
operator|.
name|relocatingShards
argument_list|()
operator|<=
name|request
operator|.
name|waitForRelocatingShards
argument_list|()
condition|)
block|{
name|waitForCounter
operator|++
expr_stmt|;
block|}
if|if
condition|(
name|request
operator|.
name|waitForActiveShards
argument_list|()
operator|!=
operator|-
literal|1
operator|&&
name|response
operator|.
name|activeShards
argument_list|()
operator|>=
name|request
operator|.
name|waitForActiveShards
argument_list|()
condition|)
block|{
name|waitForCounter
operator|++
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|request
operator|.
name|waitForNodes
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
if|if
condition|(
name|request
operator|.
name|waitForNodes
argument_list|()
operator|.
name|startsWith
argument_list|(
literal|">="
argument_list|)
condition|)
block|{
name|int
name|expected
init|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|request
operator|.
name|waitForNodes
argument_list|()
operator|.
name|substring
argument_list|(
literal|2
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|response
operator|.
name|numberOfNodes
argument_list|()
operator|>=
name|expected
condition|)
block|{
name|waitForCounter
operator|++
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|request
operator|.
name|waitForNodes
argument_list|()
operator|.
name|startsWith
argument_list|(
literal|"ge("
argument_list|)
condition|)
block|{
name|int
name|expected
init|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|request
operator|.
name|waitForNodes
argument_list|()
operator|.
name|substring
argument_list|(
literal|3
argument_list|,
name|request
operator|.
name|waitForNodes
argument_list|()
operator|.
name|length
argument_list|()
operator|-
literal|1
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|response
operator|.
name|numberOfNodes
argument_list|()
operator|>=
name|expected
condition|)
block|{
name|waitForCounter
operator|++
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|request
operator|.
name|waitForNodes
argument_list|()
operator|.
name|startsWith
argument_list|(
literal|"<="
argument_list|)
condition|)
block|{
name|int
name|expected
init|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|request
operator|.
name|waitForNodes
argument_list|()
operator|.
name|substring
argument_list|(
literal|2
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|response
operator|.
name|numberOfNodes
argument_list|()
operator|<=
name|expected
condition|)
block|{
name|waitForCounter
operator|++
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|request
operator|.
name|waitForNodes
argument_list|()
operator|.
name|startsWith
argument_list|(
literal|"le("
argument_list|)
condition|)
block|{
name|int
name|expected
init|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|request
operator|.
name|waitForNodes
argument_list|()
operator|.
name|substring
argument_list|(
literal|3
argument_list|,
name|request
operator|.
name|waitForNodes
argument_list|()
operator|.
name|length
argument_list|()
operator|-
literal|1
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|response
operator|.
name|numberOfNodes
argument_list|()
operator|<=
name|expected
condition|)
block|{
name|waitForCounter
operator|++
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|request
operator|.
name|waitForNodes
argument_list|()
operator|.
name|startsWith
argument_list|(
literal|">"
argument_list|)
condition|)
block|{
name|int
name|expected
init|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|request
operator|.
name|waitForNodes
argument_list|()
operator|.
name|substring
argument_list|(
literal|1
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|response
operator|.
name|numberOfNodes
argument_list|()
operator|>
name|expected
condition|)
block|{
name|waitForCounter
operator|++
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|request
operator|.
name|waitForNodes
argument_list|()
operator|.
name|startsWith
argument_list|(
literal|"gt("
argument_list|)
condition|)
block|{
name|int
name|expected
init|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|request
operator|.
name|waitForNodes
argument_list|()
operator|.
name|substring
argument_list|(
literal|3
argument_list|,
name|request
operator|.
name|waitForNodes
argument_list|()
operator|.
name|length
argument_list|()
operator|-
literal|1
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|response
operator|.
name|numberOfNodes
argument_list|()
operator|>
name|expected
condition|)
block|{
name|waitForCounter
operator|++
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|request
operator|.
name|waitForNodes
argument_list|()
operator|.
name|startsWith
argument_list|(
literal|"<"
argument_list|)
condition|)
block|{
name|int
name|expected
init|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|request
operator|.
name|waitForNodes
argument_list|()
operator|.
name|substring
argument_list|(
literal|1
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|response
operator|.
name|numberOfNodes
argument_list|()
operator|<
name|expected
condition|)
block|{
name|waitForCounter
operator|++
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|request
operator|.
name|waitForNodes
argument_list|()
operator|.
name|startsWith
argument_list|(
literal|"lt("
argument_list|)
condition|)
block|{
name|int
name|expected
init|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|request
operator|.
name|waitForNodes
argument_list|()
operator|.
name|substring
argument_list|(
literal|3
argument_list|,
name|request
operator|.
name|waitForNodes
argument_list|()
operator|.
name|length
argument_list|()
operator|-
literal|1
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|response
operator|.
name|numberOfNodes
argument_list|()
operator|<
name|expected
condition|)
block|{
name|waitForCounter
operator|++
expr_stmt|;
block|}
block|}
else|else
block|{
name|int
name|expected
init|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|request
operator|.
name|waitForNodes
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|response
operator|.
name|numberOfNodes
argument_list|()
operator|==
name|expected
condition|)
block|{
name|waitForCounter
operator|++
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|waitForCounter
operator|==
name|waitFor
condition|)
block|{
return|return
name|response
return|;
block|}
if|if
condition|(
name|timerService
operator|.
name|estimatedTimeInMillis
argument_list|()
operator|>
name|endTime
condition|)
block|{
name|response
operator|.
name|timedOut
operator|=
literal|true
expr_stmt|;
return|return
name|response
return|;
block|}
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|200
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|response
operator|.
name|timedOut
operator|=
literal|true
expr_stmt|;
comment|// we got interrupted, bail
return|return
name|response
return|;
block|}
block|}
block|}
DECL|method|clusterHealth
specifier|private
name|ClusterHealthResponse
name|clusterHealth
parameter_list|(
name|ClusterHealthRequest
name|request
parameter_list|)
block|{
name|ClusterState
name|clusterState
init|=
name|clusterService
operator|.
name|state
argument_list|()
decl_stmt|;
name|RoutingTableValidation
name|validation
init|=
name|clusterState
operator|.
name|routingTable
argument_list|()
operator|.
name|validate
argument_list|(
name|clusterState
operator|.
name|metaData
argument_list|()
argument_list|)
decl_stmt|;
name|ClusterHealthResponse
name|response
init|=
operator|new
name|ClusterHealthResponse
argument_list|(
name|clusterName
operator|.
name|value
argument_list|()
argument_list|,
name|validation
operator|.
name|failures
argument_list|()
argument_list|)
decl_stmt|;
name|response
operator|.
name|numberOfNodes
operator|=
name|clusterState
operator|.
name|nodes
argument_list|()
operator|.
name|size
argument_list|()
expr_stmt|;
name|request
operator|.
name|indices
argument_list|(
name|clusterState
operator|.
name|metaData
argument_list|()
operator|.
name|concreteIndices
argument_list|(
name|request
operator|.
name|indices
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|index
range|:
name|request
operator|.
name|indices
argument_list|()
control|)
block|{
name|IndexRoutingTable
name|indexRoutingTable
init|=
name|clusterState
operator|.
name|routingTable
argument_list|()
operator|.
name|index
argument_list|(
name|index
argument_list|)
decl_stmt|;
name|IndexMetaData
name|indexMetaData
init|=
name|clusterState
operator|.
name|metaData
argument_list|()
operator|.
name|index
argument_list|(
name|index
argument_list|)
decl_stmt|;
if|if
condition|(
name|indexRoutingTable
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
name|ClusterIndexHealth
name|indexHealth
init|=
operator|new
name|ClusterIndexHealth
argument_list|(
name|index
argument_list|,
name|indexMetaData
operator|.
name|numberOfShards
argument_list|()
argument_list|,
name|indexMetaData
operator|.
name|numberOfReplicas
argument_list|()
argument_list|,
name|validation
operator|.
name|indexFailures
argument_list|(
name|indexMetaData
operator|.
name|index
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
for|for
control|(
name|IndexShardRoutingTable
name|shardRoutingTable
range|:
name|indexRoutingTable
control|)
block|{
name|ClusterShardHealth
name|shardHealth
init|=
operator|new
name|ClusterShardHealth
argument_list|(
name|shardRoutingTable
operator|.
name|shardId
argument_list|()
operator|.
name|id
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|ShardRouting
name|shardRouting
range|:
name|shardRoutingTable
control|)
block|{
if|if
condition|(
name|shardRouting
operator|.
name|active
argument_list|()
condition|)
block|{
name|shardHealth
operator|.
name|activeShards
operator|++
expr_stmt|;
if|if
condition|(
name|shardRouting
operator|.
name|relocating
argument_list|()
condition|)
block|{
comment|// the shard is relocating, the one he is relocating to will be in initializing state, so we don't count it
name|shardHealth
operator|.
name|relocatingShards
operator|++
expr_stmt|;
block|}
if|if
condition|(
name|shardRouting
operator|.
name|primary
argument_list|()
condition|)
block|{
name|shardHealth
operator|.
name|primaryActive
operator|=
literal|true
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|shardHealth
operator|.
name|primaryActive
condition|)
block|{
if|if
condition|(
name|shardHealth
operator|.
name|activeShards
operator|==
name|shardRoutingTable
operator|.
name|size
argument_list|()
condition|)
block|{
name|shardHealth
operator|.
name|status
operator|=
name|ClusterHealthStatus
operator|.
name|GREEN
expr_stmt|;
block|}
else|else
block|{
name|shardHealth
operator|.
name|status
operator|=
name|ClusterHealthStatus
operator|.
name|YELLOW
expr_stmt|;
block|}
block|}
else|else
block|{
name|shardHealth
operator|.
name|status
operator|=
name|ClusterHealthStatus
operator|.
name|RED
expr_stmt|;
block|}
name|indexHealth
operator|.
name|shards
operator|.
name|put
argument_list|(
name|shardHealth
operator|.
name|id
argument_list|()
argument_list|,
name|shardHealth
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|ClusterShardHealth
name|shardHealth
range|:
name|indexHealth
control|)
block|{
if|if
condition|(
name|shardHealth
operator|.
name|primaryActive
argument_list|()
condition|)
block|{
name|indexHealth
operator|.
name|activePrimaryShards
operator|++
expr_stmt|;
block|}
name|indexHealth
operator|.
name|activeShards
operator|+=
name|shardHealth
operator|.
name|activeShards
expr_stmt|;
name|indexHealth
operator|.
name|relocatingShards
operator|+=
name|shardHealth
operator|.
name|relocatingShards
expr_stmt|;
block|}
comment|// update the index status
name|indexHealth
operator|.
name|status
operator|=
name|ClusterHealthStatus
operator|.
name|GREEN
expr_stmt|;
if|if
condition|(
operator|!
name|indexHealth
operator|.
name|validationFailures
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|indexHealth
operator|.
name|status
operator|=
name|ClusterHealthStatus
operator|.
name|RED
expr_stmt|;
block|}
else|else
block|{
for|for
control|(
name|ClusterShardHealth
name|shardHealth
range|:
name|indexHealth
control|)
block|{
if|if
condition|(
name|shardHealth
operator|.
name|status
argument_list|()
operator|==
name|ClusterHealthStatus
operator|.
name|RED
condition|)
block|{
name|indexHealth
operator|.
name|status
operator|=
name|ClusterHealthStatus
operator|.
name|RED
expr_stmt|;
break|break;
block|}
if|if
condition|(
name|shardHealth
operator|.
name|status
argument_list|()
operator|==
name|ClusterHealthStatus
operator|.
name|YELLOW
condition|)
block|{
name|indexHealth
operator|.
name|status
operator|=
name|ClusterHealthStatus
operator|.
name|YELLOW
expr_stmt|;
block|}
block|}
block|}
name|response
operator|.
name|indices
operator|.
name|put
argument_list|(
name|indexHealth
operator|.
name|index
argument_list|()
argument_list|,
name|indexHealth
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|ClusterIndexHealth
name|indexHealth
range|:
name|response
control|)
block|{
name|response
operator|.
name|activePrimaryShards
operator|+=
name|indexHealth
operator|.
name|activePrimaryShards
expr_stmt|;
name|response
operator|.
name|activeShards
operator|+=
name|indexHealth
operator|.
name|activeShards
expr_stmt|;
name|response
operator|.
name|relocatingShards
operator|+=
name|indexHealth
operator|.
name|relocatingShards
expr_stmt|;
block|}
name|response
operator|.
name|status
operator|=
name|ClusterHealthStatus
operator|.
name|GREEN
expr_stmt|;
if|if
condition|(
operator|!
name|response
operator|.
name|validationFailures
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|response
operator|.
name|status
operator|=
name|ClusterHealthStatus
operator|.
name|RED
expr_stmt|;
block|}
else|else
block|{
for|for
control|(
name|ClusterIndexHealth
name|indexHealth
range|:
name|response
control|)
block|{
if|if
condition|(
name|indexHealth
operator|.
name|status
argument_list|()
operator|==
name|ClusterHealthStatus
operator|.
name|RED
condition|)
block|{
name|response
operator|.
name|status
operator|=
name|ClusterHealthStatus
operator|.
name|RED
expr_stmt|;
break|break;
block|}
if|if
condition|(
name|indexHealth
operator|.
name|status
argument_list|()
operator|==
name|ClusterHealthStatus
operator|.
name|YELLOW
condition|)
block|{
name|response
operator|.
name|status
operator|=
name|ClusterHealthStatus
operator|.
name|YELLOW
expr_stmt|;
block|}
block|}
block|}
return|return
name|response
return|;
block|}
block|}
end_class

end_unit

