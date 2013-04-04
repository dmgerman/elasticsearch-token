begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|com
operator|.
name|google
operator|.
name|common
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
name|common
operator|.
name|collect
operator|.
name|Lists
import|;
end_import

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
name|ElasticSearchException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ExceptionsHelper
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
name|NodesInfoAction
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
name|state
operator|.
name|ClusterStateAction
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
name|TransportAddress
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
name|TimeValue
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
name|util
operator|.
name|concurrent
operator|.
name|ConcurrentCollections
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
name|*
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|*
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
name|common
operator|.
name|unit
operator|.
name|TimeValue
operator|.
name|timeValueSeconds
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|TransportClientNodesService
specifier|public
class|class
name|TransportClientNodesService
extends|extends
name|AbstractComponent
block|{
DECL|field|nodesSamplerInterval
specifier|private
specifier|final
name|TimeValue
name|nodesSamplerInterval
decl_stmt|;
DECL|field|pingTimeout
specifier|private
specifier|final
name|long
name|pingTimeout
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
name|NodeSampler
name|nodesSampler
decl_stmt|;
DECL|field|nodesSamplerFuture
specifier|private
specifier|volatile
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
DECL|field|ignoreClusterName
specifier|private
specifier|final
name|boolean
name|ignoreClusterName
decl_stmt|;
DECL|field|closed
specifier|private
specifier|volatile
name|boolean
name|closed
decl_stmt|;
annotation|@
name|Inject
DECL|method|TransportClientNodesService
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
literal|5
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|pingTimeout
operator|=
name|componentSettings
operator|.
name|getAsTime
argument_list|(
literal|"ping_timeout"
argument_list|,
name|timeValueSeconds
argument_list|(
literal|5
argument_list|)
argument_list|)
operator|.
name|millis
argument_list|()
expr_stmt|;
name|this
operator|.
name|ignoreClusterName
operator|=
name|componentSettings
operator|.
name|getAsBoolean
argument_list|(
literal|"ignore_cluster_name"
argument_list|,
literal|false
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
if|if
condition|(
name|componentSettings
operator|.
name|getAsBoolean
argument_list|(
literal|"sniff"
argument_list|,
literal|false
argument_list|)
condition|)
block|{
name|this
operator|.
name|nodesSampler
operator|=
operator|new
name|SniffNodesSampler
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|nodesSampler
operator|=
operator|new
name|SimpleNodeSampler
argument_list|()
expr_stmt|;
block|}
name|this
operator|.
name|nodesSamplerFuture
operator|=
name|threadPool
operator|.
name|schedule
argument_list|(
name|nodesSamplerInterval
argument_list|,
name|ThreadPool
operator|.
name|Names
operator|.
name|GENERIC
argument_list|,
operator|new
name|ScheduledNodeSampler
argument_list|()
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
DECL|method|listedNodes
specifier|public
name|ImmutableList
argument_list|<
name|DiscoveryNode
argument_list|>
name|listedNodes
parameter_list|()
block|{
return|return
name|this
operator|.
name|listedNodes
return|;
block|}
DECL|method|addTransportAddresses
specifier|public
name|TransportClientNodesService
name|addTransportAddresses
parameter_list|(
name|TransportAddress
modifier|...
name|transportAddresses
parameter_list|)
block|{
synchronized|synchronized
init|(
name|transportMutex
init|)
block|{
name|List
argument_list|<
name|TransportAddress
argument_list|>
name|filtered
init|=
name|Lists
operator|.
name|newArrayListWithExpectedSize
argument_list|(
name|transportAddresses
operator|.
name|length
argument_list|)
decl_stmt|;
for|for
control|(
name|TransportAddress
name|transportAddress
range|:
name|transportAddresses
control|)
block|{
name|boolean
name|found
init|=
literal|false
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
name|found
operator|=
literal|true
expr_stmt|;
name|logger
operator|.
name|debug
argument_list|(
literal|"address [{}] already exists with [{}], ignoring..."
argument_list|,
name|transportAddress
argument_list|,
name|otherNode
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
if|if
condition|(
operator|!
name|found
condition|)
block|{
name|filtered
operator|.
name|add
argument_list|(
name|transportAddress
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|filtered
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
name|this
return|;
block|}
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
name|builder
operator|.
name|addAll
argument_list|(
name|listedNodes
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|TransportAddress
name|transportAddress
range|:
name|filtered
control|)
block|{
name|DiscoveryNode
name|node
init|=
operator|new
name|DiscoveryNode
argument_list|(
literal|"#transport#-"
operator|+
name|tempNodeIdGenerator
operator|.
name|incrementAndGet
argument_list|()
argument_list|,
name|transportAddress
argument_list|)
decl_stmt|;
name|logger
operator|.
name|debug
argument_list|(
literal|"adding address [{}]"
argument_list|,
name|node
argument_list|)
expr_stmt|;
name|builder
operator|.
name|add
argument_list|(
name|node
argument_list|)
expr_stmt|;
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
name|sample
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
else|else
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"removing address [{}]"
argument_list|,
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
name|sample
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
if|if
condition|(
name|index
operator|<
literal|0
condition|)
block|{
name|index
operator|=
literal|0
expr_stmt|;
name|randomNodeGenerator
operator|.
name|set
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
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
name|ElasticSearchException
name|e
parameter_list|)
block|{
if|if
condition|(
operator|!
operator|(
name|e
operator|.
name|unwrapCause
argument_list|()
operator|instanceof
name|ConnectTransportException
operator|)
condition|)
block|{
throw|throw
name|e
throw|;
block|}
block|}
block|}
throw|throw
operator|new
name|NoNodeAvailableException
argument_list|()
throw|;
block|}
DECL|method|execute
specifier|public
parameter_list|<
name|Response
parameter_list|>
name|void
name|execute
parameter_list|(
name|NodeListenerCallback
argument_list|<
name|Response
argument_list|>
name|callback
parameter_list|,
name|ActionListener
argument_list|<
name|Response
argument_list|>
name|listener
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
if|if
condition|(
name|index
operator|<
literal|0
condition|)
block|{
name|index
operator|=
literal|0
expr_stmt|;
name|randomNodeGenerator
operator|.
name|set
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
name|RetryListener
argument_list|<
name|Response
argument_list|>
name|retryListener
init|=
operator|new
name|RetryListener
argument_list|<
name|Response
argument_list|>
argument_list|(
name|callback
argument_list|,
name|listener
argument_list|,
name|nodes
argument_list|,
name|index
argument_list|)
decl_stmt|;
try|try
block|{
name|callback
operator|.
name|doWithNode
argument_list|(
name|nodes
operator|.
name|get
argument_list|(
operator|(
name|index
operator|)
operator|%
name|nodes
operator|.
name|size
argument_list|()
argument_list|)
argument_list|,
name|retryListener
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ElasticSearchException
name|e
parameter_list|)
block|{
if|if
condition|(
name|e
operator|.
name|unwrapCause
argument_list|()
operator|instanceof
name|ConnectTransportException
condition|)
block|{
name|retryListener
operator|.
name|onFailure
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
name|e
throw|;
block|}
block|}
block|}
DECL|class|RetryListener
specifier|public
specifier|static
class|class
name|RetryListener
parameter_list|<
name|Response
parameter_list|>
implements|implements
name|ActionListener
argument_list|<
name|Response
argument_list|>
block|{
DECL|field|callback
specifier|private
specifier|final
name|NodeListenerCallback
argument_list|<
name|Response
argument_list|>
name|callback
decl_stmt|;
DECL|field|listener
specifier|private
specifier|final
name|ActionListener
argument_list|<
name|Response
argument_list|>
name|listener
decl_stmt|;
DECL|field|nodes
specifier|private
specifier|final
name|ImmutableList
argument_list|<
name|DiscoveryNode
argument_list|>
name|nodes
decl_stmt|;
DECL|field|index
specifier|private
specifier|final
name|int
name|index
decl_stmt|;
DECL|field|i
specifier|private
specifier|volatile
name|int
name|i
decl_stmt|;
DECL|method|RetryListener
specifier|public
name|RetryListener
parameter_list|(
name|NodeListenerCallback
argument_list|<
name|Response
argument_list|>
name|callback
parameter_list|,
name|ActionListener
argument_list|<
name|Response
argument_list|>
name|listener
parameter_list|,
name|ImmutableList
argument_list|<
name|DiscoveryNode
argument_list|>
name|nodes
parameter_list|,
name|int
name|index
parameter_list|)
block|{
name|this
operator|.
name|callback
operator|=
name|callback
expr_stmt|;
name|this
operator|.
name|listener
operator|=
name|listener
expr_stmt|;
name|this
operator|.
name|nodes
operator|=
name|nodes
expr_stmt|;
name|this
operator|.
name|index
operator|=
name|index
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|onResponse
specifier|public
name|void
name|onResponse
parameter_list|(
name|Response
name|response
parameter_list|)
block|{
name|listener
operator|.
name|onResponse
argument_list|(
name|response
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|onFailure
specifier|public
name|void
name|onFailure
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
if|if
condition|(
name|ExceptionsHelper
operator|.
name|unwrapCause
argument_list|(
name|e
argument_list|)
operator|instanceof
name|ConnectTransportException
condition|)
block|{
name|int
name|i
init|=
operator|++
name|this
operator|.
name|i
decl_stmt|;
if|if
condition|(
name|i
operator|==
name|nodes
operator|.
name|size
argument_list|()
condition|)
block|{
name|listener
operator|.
name|onFailure
argument_list|(
operator|new
name|NoNodeAvailableException
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
try|try
block|{
name|callback
operator|.
name|doWithNode
argument_list|(
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
argument_list|,
name|this
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e1
parameter_list|)
block|{
comment|// retry the next one...
name|onFailure
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
name|listener
operator|.
name|onFailure
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
block|{
name|closed
operator|=
literal|true
expr_stmt|;
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
name|node
range|:
name|nodes
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
for|for
control|(
name|DiscoveryNode
name|listedNode
range|:
name|listedNodes
control|)
block|{
name|transportService
operator|.
name|disconnectFromNode
argument_list|(
name|listedNode
argument_list|)
expr_stmt|;
block|}
name|nodes
operator|=
name|ImmutableList
operator|.
name|of
argument_list|()
expr_stmt|;
block|}
DECL|interface|NodeSampler
interface|interface
name|NodeSampler
block|{
DECL|method|sample
name|void
name|sample
parameter_list|()
function_decl|;
block|}
DECL|class|ScheduledNodeSampler
class|class
name|ScheduledNodeSampler
implements|implements
name|Runnable
block|{
annotation|@
name|Override
DECL|method|run
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
name|nodesSampler
operator|.
name|sample
argument_list|()
expr_stmt|;
if|if
condition|(
operator|!
name|closed
condition|)
block|{
name|nodesSamplerFuture
operator|=
name|threadPool
operator|.
name|schedule
argument_list|(
name|nodesSamplerInterval
argument_list|,
name|ThreadPool
operator|.
name|Names
operator|.
name|GENERIC
argument_list|,
name|this
argument_list|)
expr_stmt|;
block|}
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
literal|"failed to sample"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|class|SimpleNodeSampler
class|class
name|SimpleNodeSampler
implements|implements
name|NodeSampler
block|{
annotation|@
name|Override
DECL|method|sample
specifier|public
specifier|synchronized
name|void
name|sample
parameter_list|()
block|{
if|if
condition|(
name|closed
condition|)
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
name|DiscoveryNode
name|node
range|:
name|listedNodes
control|)
block|{
if|if
condition|(
operator|!
name|transportService
operator|.
name|nodeConnected
argument_list|(
name|node
argument_list|)
condition|)
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
literal|"failed to connect to node [{}], removed from nodes list"
argument_list|,
name|e
argument_list|,
name|node
argument_list|)
expr_stmt|;
continue|continue;
block|}
block|}
try|try
block|{
name|NodesInfoResponse
name|nodeInfo
init|=
name|transportService
operator|.
name|submitRequest
argument_list|(
name|node
argument_list|,
name|NodesInfoAction
operator|.
name|NAME
argument_list|,
name|Requests
operator|.
name|nodesInfoRequest
argument_list|(
literal|"_local"
argument_list|)
argument_list|,
name|TransportRequestOptions
operator|.
name|options
argument_list|()
operator|.
name|withHighType
argument_list|()
operator|.
name|withTimeout
argument_list|(
name|pingTimeout
argument_list|)
argument_list|,
operator|new
name|FutureTransportResponseHandler
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
block|}
argument_list|)
operator|.
name|txGet
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|ignoreClusterName
operator|&&
operator|!
name|clusterName
operator|.
name|equals
argument_list|(
name|nodeInfo
operator|.
name|getClusterName
argument_list|()
argument_list|)
condition|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"node {} not part of the cluster {}, ignoring..."
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
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"failed to get node info for {}, disconnecting..."
argument_list|,
name|e
argument_list|,
name|node
argument_list|)
expr_stmt|;
name|transportService
operator|.
name|disconnectFromNode
argument_list|(
name|node
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
DECL|class|SniffNodesSampler
class|class
name|SniffNodesSampler
implements|implements
name|NodeSampler
block|{
annotation|@
name|Override
DECL|method|sample
specifier|public
specifier|synchronized
name|void
name|sample
parameter_list|()
block|{
if|if
condition|(
name|closed
condition|)
block|{
return|return;
block|}
comment|// the nodes we are going to ping include the core listed nodes that were added
comment|// and the last round of discovered nodes
name|Set
argument_list|<
name|DiscoveryNode
argument_list|>
name|nodesToPing
init|=
name|Sets
operator|.
name|newHashSet
argument_list|()
decl_stmt|;
for|for
control|(
name|DiscoveryNode
name|node
range|:
name|listedNodes
control|)
block|{
name|nodesToPing
operator|.
name|add
argument_list|(
name|node
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|DiscoveryNode
name|node
range|:
name|nodes
control|)
block|{
name|nodesToPing
operator|.
name|add
argument_list|(
name|node
argument_list|)
expr_stmt|;
block|}
specifier|final
name|CountDownLatch
name|latch
init|=
operator|new
name|CountDownLatch
argument_list|(
name|nodesToPing
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|Queue
argument_list|<
name|ClusterStateResponse
argument_list|>
name|clusterStateResponses
init|=
name|ConcurrentCollections
operator|.
name|newQueue
argument_list|()
decl_stmt|;
for|for
control|(
specifier|final
name|DiscoveryNode
name|listedNode
range|:
name|nodesToPing
control|)
block|{
name|threadPool
operator|.
name|executor
argument_list|(
name|ThreadPool
operator|.
name|Names
operator|.
name|MANAGEMENT
argument_list|)
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
if|if
condition|(
operator|!
name|transportService
operator|.
name|nodeConnected
argument_list|(
name|listedNode
argument_list|)
condition|)
block|{
try|try
block|{
comment|// if its one of hte actual nodes we will talk to, not to listed nodes, fully connect
if|if
condition|(
name|nodes
operator|.
name|contains
argument_list|(
name|listedNode
argument_list|)
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"connecting to cluster node [{}]"
argument_list|,
name|listedNode
argument_list|)
expr_stmt|;
name|transportService
operator|.
name|connectToNode
argument_list|(
name|listedNode
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// its a listed node, light connect to it...
name|logger
operator|.
name|trace
argument_list|(
literal|"connecting to listed node (light) [{}]"
argument_list|,
name|listedNode
argument_list|)
expr_stmt|;
name|transportService
operator|.
name|connectToNodeLight
argument_list|(
name|listedNode
argument_list|)
expr_stmt|;
block|}
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
literal|"failed to connect to node [{}], ignoring..."
argument_list|,
name|e
argument_list|,
name|listedNode
argument_list|)
expr_stmt|;
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
return|return;
block|}
block|}
name|transportService
operator|.
name|sendRequest
argument_list|(
name|listedNode
argument_list|,
name|ClusterStateAction
operator|.
name|NAME
argument_list|,
name|Requests
operator|.
name|clusterStateRequest
argument_list|()
operator|.
name|filterAll
argument_list|()
operator|.
name|filterNodes
argument_list|(
literal|false
argument_list|)
operator|.
name|local
argument_list|(
literal|true
argument_list|)
argument_list|,
name|TransportRequestOptions
operator|.
name|options
argument_list|()
operator|.
name|withHighType
argument_list|()
operator|.
name|withTimeout
argument_list|(
name|pingTimeout
argument_list|)
argument_list|,
operator|new
name|BaseTransportResponseHandler
argument_list|<
name|ClusterStateResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|ClusterStateResponse
name|newInstance
parameter_list|()
block|{
return|return
operator|new
name|ClusterStateResponse
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|executor
parameter_list|()
block|{
return|return
name|ThreadPool
operator|.
name|Names
operator|.
name|SAME
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|handleResponse
parameter_list|(
name|ClusterStateResponse
name|response
parameter_list|)
block|{
name|clusterStateResponses
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
name|TransportException
name|e
parameter_list|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"failed to get local cluster state for {}, disconnecting..."
argument_list|,
name|e
argument_list|,
name|listedNode
argument_list|)
expr_stmt|;
name|transportService
operator|.
name|disconnectFromNode
argument_list|(
name|listedNode
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
name|info
argument_list|(
literal|"failed to get local cluster state info for {}, disconnecting..."
argument_list|,
name|e
argument_list|,
name|listedNode
argument_list|)
expr_stmt|;
name|transportService
operator|.
name|disconnectFromNode
argument_list|(
name|listedNode
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
name|ClusterStateResponse
name|clusterStateResponse
range|:
name|clusterStateResponses
control|)
block|{
if|if
condition|(
operator|!
name|ignoreClusterName
operator|&&
operator|!
name|clusterName
operator|.
name|equals
argument_list|(
name|clusterStateResponse
operator|.
name|getClusterName
argument_list|()
argument_list|)
condition|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"node {} not part of the cluster {}, ignoring..."
argument_list|,
name|clusterStateResponse
operator|.
name|getState
argument_list|()
operator|.
name|nodes
argument_list|()
operator|.
name|localNode
argument_list|()
argument_list|,
name|clusterName
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|DiscoveryNode
name|node
range|:
name|clusterStateResponse
operator|.
name|getState
argument_list|()
operator|.
name|nodes
argument_list|()
operator|.
name|dataNodes
argument_list|()
operator|.
name|values
argument_list|()
control|)
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
comment|// now, make sure we are connected to all the updated nodes
for|for
control|(
name|Iterator
argument_list|<
name|DiscoveryNode
argument_list|>
name|it
init|=
name|newNodes
operator|.
name|iterator
argument_list|()
init|;
name|it
operator|.
name|hasNext
argument_list|()
condition|;
control|)
block|{
name|DiscoveryNode
name|node
init|=
name|it
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|transportService
operator|.
name|nodeConnected
argument_list|(
name|node
argument_list|)
condition|)
block|{
try|try
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"connecting to node [{}]"
argument_list|,
name|node
argument_list|)
expr_stmt|;
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
name|it
operator|.
name|remove
argument_list|()
expr_stmt|;
name|logger
operator|.
name|debug
argument_list|(
literal|"failed to connect to discovered node ["
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
DECL|interface|NodeListenerCallback
specifier|public
specifier|static
interface|interface
name|NodeListenerCallback
parameter_list|<
name|Response
parameter_list|>
block|{
DECL|method|doWithNode
name|void
name|doWithNode
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|,
name|ActionListener
argument_list|<
name|Response
argument_list|>
name|listener
parameter_list|)
throws|throws
name|ElasticSearchException
function_decl|;
block|}
block|}
end_class

end_unit

