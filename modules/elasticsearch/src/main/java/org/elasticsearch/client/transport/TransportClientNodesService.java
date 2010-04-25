begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.client.transport
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|client
operator|.
name|transport
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|gcommon
operator|.
name|collect
operator|.
name|ImmutableList
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
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
name|ClusterChangedEvent
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
name|ClusterStateListener
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
name|node
operator|.
name|DiscoveryNodes
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
name|transport
operator|.
name|BaseTransportResponseHandler
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
name|ConnectTransportException
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
name|RemoteTransportException
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

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|TimeValue
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
name|component
operator|.
name|AbstractComponent
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
name|transport
operator|.
name|TransportAddress
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|CopyOnWriteArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|CountDownLatch
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ScheduledFuture
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicInteger
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|TimeValue
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|TransportClientNodesService
specifier|public
class|class
name|TransportClientNodesService
extends|extends
name|AbstractComponent
implements|implements
name|ClusterStateListener
block|{
DECL|field|nodesSamplerInterval
specifier|private
specifier|final
name|TimeValue
name|nodesSamplerInterval
decl_stmt|;
DECL|field|clusterName
specifier|private
specifier|final
name|ClusterName
name|clusterName
decl_stmt|;
DECL|field|transportService
specifier|private
specifier|final
name|TransportService
name|transportService
decl_stmt|;
DECL|field|threadPool
specifier|private
specifier|final
name|ThreadPool
name|threadPool
decl_stmt|;
comment|// nodes that are added to be discovered
DECL|field|listedNodes
specifier|private
specifier|volatile
name|ImmutableList
argument_list|<
name|DiscoveryNode
argument_list|>
name|listedNodes
init|=
name|ImmutableList
operator|.
name|of
argument_list|()
decl_stmt|;
DECL|field|transportMutex
specifier|private
specifier|final
name|Object
name|transportMutex
init|=
operator|new
name|Object
argument_list|()
decl_stmt|;
DECL|field|nodes
specifier|private
specifier|volatile
name|ImmutableList
argument_list|<
name|DiscoveryNode
argument_list|>
name|nodes
init|=
name|ImmutableList
operator|.
name|of
argument_list|()
decl_stmt|;
DECL|field|discoveredNodes
specifier|private
specifier|volatile
name|DiscoveryNodes
name|discoveredNodes
decl_stmt|;
DECL|field|tempNodeIdGenerator
specifier|private
specifier|final
name|AtomicInteger
name|tempNodeIdGenerator
init|=
operator|new
name|AtomicInteger
argument_list|()
decl_stmt|;
DECL|field|nodesSampler
specifier|private
specifier|final
name|ScheduledNodesSampler
name|nodesSampler
init|=
operator|new
name|ScheduledNodesSampler
argument_list|()
decl_stmt|;
DECL|field|nodesSamplerFuture
specifier|private
specifier|final
name|ScheduledFuture
name|nodesSamplerFuture
decl_stmt|;
DECL|field|randomNodeGenerator
specifier|private
specifier|final
name|AtomicInteger
name|randomNodeGenerator
init|=
operator|new
name|AtomicInteger
argument_list|()
decl_stmt|;
DECL|method|TransportClientNodesService
annotation|@
name|Inject
specifier|public
name|TransportClientNodesService
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ClusterName
name|clusterName
parameter_list|,
name|TransportService
name|transportService
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|)
block|{
name|super
argument_list|(
name|settings
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
name|transportService
operator|=
name|transportService
expr_stmt|;
name|this
operator|.
name|threadPool
operator|=
name|threadPool
expr_stmt|;
name|this
operator|.
name|nodesSamplerInterval
operator|=
name|componentSettings
operator|.
name|getAsTime
argument_list|(
literal|"nodes_sampler_interval"
argument_list|,
name|timeValueSeconds
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|logger
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"node_sampler_interval["
operator|+
name|nodesSamplerInterval
operator|+
literal|"]"
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|nodesSamplerFuture
operator|=
name|threadPool
operator|.
name|scheduleWithFixedDelay
argument_list|(
name|nodesSampler
argument_list|,
name|nodesSamplerInterval
argument_list|)
expr_stmt|;
comment|// we want the transport service to throw connect exceptions, so we can retry
name|transportService
operator|.
name|throwConnectException
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
DECL|method|transportAddresses
specifier|public
name|ImmutableList
argument_list|<
name|TransportAddress
argument_list|>
name|transportAddresses
parameter_list|()
block|{
name|ImmutableList
operator|.
name|Builder
argument_list|<
name|TransportAddress
argument_list|>
name|lstBuilder
init|=
name|ImmutableList
operator|.
name|builder
argument_list|()
decl_stmt|;
for|for
control|(
name|DiscoveryNode
name|listedNode
range|:
name|listedNodes
control|)
block|{
name|lstBuilder
operator|.
name|add
argument_list|(
name|listedNode
operator|.
name|address
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|lstBuilder
operator|.
name|build
argument_list|()
return|;
block|}
DECL|method|connectedNodes
specifier|public
name|ImmutableList
argument_list|<
name|DiscoveryNode
argument_list|>
name|connectedNodes
parameter_list|()
block|{
return|return
name|this
operator|.
name|nodes
return|;
block|}
DECL|method|addTransportAddress
specifier|public
name|TransportClientNodesService
name|addTransportAddress
parameter_list|(
name|TransportAddress
name|transportAddress
parameter_list|)
block|{
synchronized|synchronized
init|(
name|transportMutex
init|)
block|{
name|ImmutableList
operator|.
name|Builder
argument_list|<
name|DiscoveryNode
argument_list|>
name|builder
init|=
name|ImmutableList
operator|.
name|builder
argument_list|()
decl_stmt|;
name|listedNodes
operator|=
name|builder
operator|.
name|addAll
argument_list|(
name|listedNodes
argument_list|)
operator|.
name|add
argument_list|(
operator|new
name|DiscoveryNode
argument_list|(
literal|"#temp#-"
operator|+
name|tempNodeIdGenerator
operator|.
name|incrementAndGet
argument_list|()
argument_list|,
name|transportAddress
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
block|}
name|nodesSampler
operator|.
name|run
argument_list|()
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|removeTransportAddress
specifier|public
name|TransportClientNodesService
name|removeTransportAddress
parameter_list|(
name|TransportAddress
name|transportAddress
parameter_list|)
block|{
synchronized|synchronized
init|(
name|transportMutex
init|)
block|{
name|ImmutableList
operator|.
name|Builder
argument_list|<
name|DiscoveryNode
argument_list|>
name|builder
init|=
name|ImmutableList
operator|.
name|builder
argument_list|()
decl_stmt|;
for|for
control|(
name|DiscoveryNode
name|otherNode
range|:
name|listedNodes
control|)
block|{
if|if
condition|(
operator|!
name|otherNode
operator|.
name|address
argument_list|()
operator|.
name|equals
argument_list|(
name|transportAddress
argument_list|)
condition|)
block|{
name|builder
operator|.
name|add
argument_list|(
name|otherNode
argument_list|)
expr_stmt|;
block|}
block|}
name|listedNodes
operator|=
name|builder
operator|.
name|build
argument_list|()
expr_stmt|;
block|}
name|nodesSampler
operator|.
name|run
argument_list|()
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|execute
specifier|public
parameter_list|<
name|T
parameter_list|>
name|T
name|execute
parameter_list|(
name|NodeCallback
argument_list|<
name|T
argument_list|>
name|callback
parameter_list|)
throws|throws
name|ElasticSearchException
block|{
name|ImmutableList
argument_list|<
name|DiscoveryNode
argument_list|>
name|nodes
init|=
name|this
operator|.
name|nodes
decl_stmt|;
if|if
condition|(
name|nodes
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|NoNodeAvailableException
argument_list|()
throw|;
block|}
name|int
name|index
init|=
name|randomNodeGenerator
operator|.
name|incrementAndGet
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|nodes
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|DiscoveryNode
name|node
init|=
name|nodes
operator|.
name|get
argument_list|(
operator|(
name|index
operator|+
name|i
operator|)
operator|%
name|nodes
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
return|return
name|callback
operator|.
name|doWithNode
argument_list|(
name|node
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|ConnectTransportException
name|e
parameter_list|)
block|{
comment|// retry in this case
block|}
block|}
throw|throw
operator|new
name|NoNodeAvailableException
argument_list|()
throw|;
block|}
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
block|{
name|nodesSamplerFuture
operator|.
name|cancel
argument_list|(
literal|true
argument_list|)
expr_stmt|;
for|for
control|(
name|DiscoveryNode
name|listedNode
range|:
name|listedNodes
control|)
name|transportService
operator|.
name|disconnectFromNode
argument_list|(
name|listedNode
argument_list|)
expr_stmt|;
block|}
DECL|method|clusterChanged
annotation|@
name|Override
specifier|public
name|void
name|clusterChanged
parameter_list|(
name|ClusterChangedEvent
name|event
parameter_list|)
block|{
for|for
control|(
name|DiscoveryNode
name|node
range|:
name|event
operator|.
name|nodesDelta
argument_list|()
operator|.
name|addedNodes
argument_list|()
control|)
block|{
try|try
block|{
name|transportService
operator|.
name|connectToNode
argument_list|(
name|node
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"Failed to connect to discovered node ["
operator|+
name|node
operator|+
literal|"]"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
name|this
operator|.
name|discoveredNodes
operator|=
name|event
operator|.
name|state
argument_list|()
operator|.
name|nodes
argument_list|()
expr_stmt|;
name|HashSet
argument_list|<
name|DiscoveryNode
argument_list|>
name|newNodes
init|=
operator|new
name|HashSet
argument_list|<
name|DiscoveryNode
argument_list|>
argument_list|(
name|nodes
argument_list|)
decl_stmt|;
name|newNodes
operator|.
name|addAll
argument_list|(
name|discoveredNodes
operator|.
name|nodes
argument_list|()
operator|.
name|values
argument_list|()
argument_list|)
expr_stmt|;
name|nodes
operator|=
operator|new
name|ImmutableList
operator|.
name|Builder
argument_list|<
name|DiscoveryNode
argument_list|>
argument_list|()
operator|.
name|addAll
argument_list|(
name|newNodes
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
for|for
control|(
name|DiscoveryNode
name|node
range|:
name|event
operator|.
name|nodesDelta
argument_list|()
operator|.
name|removedNodes
argument_list|()
control|)
block|{
name|transportService
operator|.
name|disconnectFromNode
argument_list|(
name|node
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|ScheduledNodesSampler
specifier|private
class|class
name|ScheduledNodesSampler
implements|implements
name|Runnable
block|{
DECL|method|run
annotation|@
name|Override
specifier|public
specifier|synchronized
name|void
name|run
parameter_list|()
block|{
name|ImmutableList
argument_list|<
name|DiscoveryNode
argument_list|>
name|listedNodes
init|=
name|TransportClientNodesService
operator|.
name|this
operator|.
name|listedNodes
decl_stmt|;
specifier|final
name|CountDownLatch
name|latch
init|=
operator|new
name|CountDownLatch
argument_list|(
name|listedNodes
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|CopyOnWriteArrayList
argument_list|<
name|NodesInfoResponse
argument_list|>
name|nodesInfoResponses
init|=
operator|new
name|CopyOnWriteArrayList
argument_list|<
name|NodesInfoResponse
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
specifier|final
name|DiscoveryNode
name|listedNode
range|:
name|listedNodes
control|)
block|{
name|threadPool
operator|.
name|execute
argument_list|(
operator|new
name|Runnable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
name|transportService
operator|.
name|connectToNode
argument_list|(
name|listedNode
argument_list|)
expr_stmt|;
comment|// make sure we are connected to it
name|transportService
operator|.
name|sendRequest
argument_list|(
name|listedNode
argument_list|,
name|TransportActions
operator|.
name|Admin
operator|.
name|Cluster
operator|.
name|Node
operator|.
name|INFO
argument_list|,
name|Requests
operator|.
name|nodesInfo
argument_list|(
literal|"_local"
argument_list|)
argument_list|,
operator|new
name|BaseTransportResponseHandler
argument_list|<
name|NodesInfoResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|NodesInfoResponse
name|newInstance
parameter_list|()
block|{
return|return
operator|new
name|NodesInfoResponse
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|handleResponse
parameter_list|(
name|NodesInfoResponse
name|response
parameter_list|)
block|{
name|nodesInfoResponses
operator|.
name|add
argument_list|(
name|response
argument_list|)
expr_stmt|;
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|handleException
parameter_list|(
name|RemoteTransportException
name|exp
parameter_list|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"Failed to get node info from "
operator|+
name|listedNode
operator|+
literal|", removed from nodes list"
argument_list|,
name|exp
argument_list|)
expr_stmt|;
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"Failed to get node info from "
operator|+
name|listedNode
operator|+
literal|", removed from nodes list"
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|latch
operator|.
name|await
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
return|return;
block|}
name|HashSet
argument_list|<
name|DiscoveryNode
argument_list|>
name|newNodes
init|=
operator|new
name|HashSet
argument_list|<
name|DiscoveryNode
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|NodesInfoResponse
name|nodesInfoResponse
range|:
name|nodesInfoResponses
control|)
block|{
if|if
condition|(
name|nodesInfoResponse
operator|.
name|nodes
argument_list|()
operator|.
name|length
operator|>
literal|0
condition|)
block|{
name|DiscoveryNode
name|node
init|=
name|nodesInfoResponse
operator|.
name|nodes
argument_list|()
index|[
literal|0
index|]
operator|.
name|node
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|clusterName
operator|.
name|equals
argument_list|(
name|nodesInfoResponse
operator|.
name|clusterName
argument_list|()
argument_list|)
condition|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"Node {} not part of the cluster {}, ignoring..."
argument_list|,
name|node
argument_list|,
name|clusterName
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|newNodes
operator|.
name|add
argument_list|(
name|node
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
comment|// should not really happen....
name|logger
operator|.
name|debug
argument_list|(
literal|"No info returned from node..."
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|discoveredNodes
operator|!=
literal|null
condition|)
block|{
name|newNodes
operator|.
name|addAll
argument_list|(
name|discoveredNodes
operator|.
name|nodes
argument_list|()
operator|.
name|values
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// now, make sure we are connected to all the updated nodes
for|for
control|(
name|DiscoveryNode
name|node
range|:
name|newNodes
control|)
block|{
try|try
block|{
name|transportService
operator|.
name|connectToNode
argument_list|(
name|node
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"Failed to connect to discovered node ["
operator|+
name|node
operator|+
literal|"]"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
name|nodes
operator|=
operator|new
name|ImmutableList
operator|.
name|Builder
argument_list|<
name|DiscoveryNode
argument_list|>
argument_list|()
operator|.
name|addAll
argument_list|(
name|newNodes
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
block|}
block|}
DECL|interface|NodeCallback
specifier|public
specifier|static
interface|interface
name|NodeCallback
parameter_list|<
name|T
parameter_list|>
block|{
DECL|method|doWithNode
name|T
name|doWithNode
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|)
throws|throws
name|ElasticSearchException
function_decl|;
block|}
block|}
end_class

end_unit

