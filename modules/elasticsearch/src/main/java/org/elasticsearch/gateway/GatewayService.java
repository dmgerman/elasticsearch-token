begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.gateway
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|gateway
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
name|cluster
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
name|cluster
operator|.
name|block
operator|.
name|ClusterBlock
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
name|block
operator|.
name|ClusterBlocks
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
name|metadata
operator|.
name|MetaData
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
name|MetaDataService
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
name|AbstractLifecycleComponent
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
name|TimeValue
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|discovery
operator|.
name|DiscoveryService
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
name|javax
operator|.
name|annotation
operator|.
name|Nullable
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
name|ExecutorService
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
name|TimeUnit
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
name|AtomicBoolean
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|Executors
operator|.
name|*
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|ClusterState
operator|.
name|*
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|metadata
operator|.
name|MetaData
operator|.
name|*
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
name|*
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
name|util
operator|.
name|concurrent
operator|.
name|DynamicExecutors
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|GatewayService
specifier|public
class|class
name|GatewayService
extends|extends
name|AbstractLifecycleComponent
argument_list|<
name|GatewayService
argument_list|>
implements|implements
name|ClusterStateListener
block|{
DECL|field|NOT_RECOVERED_FROM_GATEWAY_BLOCK
specifier|public
specifier|final
name|ClusterBlock
name|NOT_RECOVERED_FROM_GATEWAY_BLOCK
init|=
operator|new
name|ClusterBlock
argument_list|(
literal|1
argument_list|,
literal|"not recovered from gateway"
argument_list|)
decl_stmt|;
DECL|field|gateway
specifier|private
specifier|final
name|Gateway
name|gateway
decl_stmt|;
DECL|field|threadPool
specifier|private
specifier|final
name|ThreadPool
name|threadPool
decl_stmt|;
DECL|field|executor
specifier|private
specifier|volatile
name|ExecutorService
name|executor
decl_stmt|;
DECL|field|clusterService
specifier|private
specifier|final
name|ClusterService
name|clusterService
decl_stmt|;
DECL|field|discoveryService
specifier|private
specifier|final
name|DiscoveryService
name|discoveryService
decl_stmt|;
DECL|field|metaDataService
specifier|private
specifier|final
name|MetaDataService
name|metaDataService
decl_stmt|;
DECL|field|initialStateTimeout
specifier|private
specifier|final
name|TimeValue
name|initialStateTimeout
decl_stmt|;
DECL|field|recoverAfterTime
specifier|private
specifier|final
name|TimeValue
name|recoverAfterTime
decl_stmt|;
DECL|field|recoverAfterNodes
specifier|private
specifier|final
name|int
name|recoverAfterNodes
decl_stmt|;
DECL|field|readFromGateway
specifier|private
specifier|final
name|AtomicBoolean
name|readFromGateway
init|=
operator|new
name|AtomicBoolean
argument_list|()
decl_stmt|;
DECL|method|GatewayService
annotation|@
name|Inject
specifier|public
name|GatewayService
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|Gateway
name|gateway
parameter_list|,
name|ClusterService
name|clusterService
parameter_list|,
name|DiscoveryService
name|discoveryService
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|,
name|MetaDataService
name|metaDataService
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|gateway
operator|=
name|gateway
expr_stmt|;
name|this
operator|.
name|clusterService
operator|=
name|clusterService
expr_stmt|;
name|this
operator|.
name|discoveryService
operator|=
name|discoveryService
expr_stmt|;
name|this
operator|.
name|threadPool
operator|=
name|threadPool
expr_stmt|;
name|this
operator|.
name|metaDataService
operator|=
name|metaDataService
expr_stmt|;
name|this
operator|.
name|initialStateTimeout
operator|=
name|componentSettings
operator|.
name|getAsTime
argument_list|(
literal|"initial_state_timeout"
argument_list|,
name|TimeValue
operator|.
name|timeValueSeconds
argument_list|(
literal|30
argument_list|)
argument_list|)
expr_stmt|;
comment|// allow to control a delay of when indices will get created
name|this
operator|.
name|recoverAfterTime
operator|=
name|componentSettings
operator|.
name|getAsTime
argument_list|(
literal|"recover_after_time"
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|this
operator|.
name|recoverAfterNodes
operator|=
name|componentSettings
operator|.
name|getAsInt
argument_list|(
literal|"recover_after_nodes"
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
DECL|method|doStart
annotation|@
name|Override
specifier|protected
name|void
name|doStart
parameter_list|()
throws|throws
name|ElasticSearchException
block|{
name|gateway
operator|.
name|start
argument_list|()
expr_stmt|;
name|this
operator|.
name|executor
operator|=
name|newSingleThreadExecutor
argument_list|(
name|daemonThreadFactory
argument_list|(
name|settings
argument_list|,
literal|"gateway"
argument_list|)
argument_list|)
expr_stmt|;
comment|// if we received initial state, see if we can recover within the start phase, so we hold the
comment|// node from starting until we recovered properly
if|if
condition|(
name|discoveryService
operator|.
name|initialStateReceived
argument_list|()
condition|)
block|{
name|ClusterState
name|clusterState
init|=
name|clusterService
operator|.
name|state
argument_list|()
decl_stmt|;
if|if
condition|(
name|clusterState
operator|.
name|nodes
argument_list|()
operator|.
name|localNodeMaster
argument_list|()
operator|&&
operator|!
name|clusterState
operator|.
name|metaData
argument_list|()
operator|.
name|recoveredFromGateway
argument_list|()
condition|)
block|{
if|if
condition|(
name|recoverAfterNodes
operator|!=
operator|-
literal|1
operator|&&
name|clusterState
operator|.
name|nodes
argument_list|()
operator|.
name|size
argument_list|()
operator|<
name|recoverAfterNodes
condition|)
block|{
name|updateClusterStateBlockedOnNotRecovered
argument_list|()
expr_stmt|;
name|logger
operator|.
name|debug
argument_list|(
literal|"not recovering from gateway, nodes_size ["
operator|+
name|clusterState
operator|.
name|nodes
argument_list|()
operator|.
name|size
argument_list|()
operator|+
literal|"]< recover_after_nodes ["
operator|+
name|recoverAfterNodes
operator|+
literal|"]"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|readFromGateway
operator|.
name|compareAndSet
argument_list|(
literal|false
argument_list|,
literal|true
argument_list|)
condition|)
block|{
name|Boolean
name|waited
init|=
name|readFromGateway
argument_list|(
name|initialStateTimeout
argument_list|)
decl_stmt|;
if|if
condition|(
name|waited
operator|!=
literal|null
operator|&&
operator|!
name|waited
condition|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"waited for {} for indices to be created from the gateway, and not all have been created"
argument_list|,
name|initialStateTimeout
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
else|else
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"can't wait on start for (possibly) reading state from gateway, will do it asynchronously"
argument_list|)
expr_stmt|;
block|}
name|clusterService
operator|.
name|add
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
DECL|method|doStop
annotation|@
name|Override
specifier|protected
name|void
name|doStop
parameter_list|()
throws|throws
name|ElasticSearchException
block|{
name|clusterService
operator|.
name|remove
argument_list|(
name|this
argument_list|)
expr_stmt|;
name|executor
operator|.
name|shutdown
argument_list|()
expr_stmt|;
try|try
block|{
name|executor
operator|.
name|awaitTermination
argument_list|(
literal|10
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
comment|// ignore
block|}
name|gateway
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
DECL|method|doClose
annotation|@
name|Override
specifier|protected
name|void
name|doClose
parameter_list|()
throws|throws
name|ElasticSearchException
block|{
name|gateway
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
DECL|method|clusterChanged
annotation|@
name|Override
specifier|public
name|void
name|clusterChanged
parameter_list|(
specifier|final
name|ClusterChangedEvent
name|event
parameter_list|)
block|{
if|if
condition|(
operator|!
name|lifecycle
operator|.
name|started
argument_list|()
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|event
operator|.
name|localNodeMaster
argument_list|()
condition|)
block|{
if|if
condition|(
operator|!
name|event
operator|.
name|state
argument_list|()
operator|.
name|metaData
argument_list|()
operator|.
name|recoveredFromGateway
argument_list|()
condition|)
block|{
name|ClusterState
name|clusterState
init|=
name|event
operator|.
name|state
argument_list|()
decl_stmt|;
if|if
condition|(
name|recoverAfterNodes
operator|!=
operator|-
literal|1
operator|&&
name|clusterState
operator|.
name|nodes
argument_list|()
operator|.
name|size
argument_list|()
operator|<
name|recoverAfterNodes
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"not recovering from gateway, nodes_size ["
operator|+
name|clusterState
operator|.
name|nodes
argument_list|()
operator|.
name|size
argument_list|()
operator|+
literal|"]< recover_after_nodes ["
operator|+
name|recoverAfterNodes
operator|+
literal|"]"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|readFromGateway
operator|.
name|compareAndSet
argument_list|(
literal|false
argument_list|,
literal|true
argument_list|)
condition|)
block|{
name|executor
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
name|readFromGateway
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
name|writeToGateway
argument_list|(
name|event
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|writeToGateway
specifier|private
name|void
name|writeToGateway
parameter_list|(
specifier|final
name|ClusterChangedEvent
name|event
parameter_list|)
block|{
if|if
condition|(
operator|!
name|event
operator|.
name|metaDataChanged
argument_list|()
condition|)
block|{
return|return;
block|}
name|executor
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
name|logger
operator|.
name|debug
argument_list|(
literal|"writing to gateway"
argument_list|)
expr_stmt|;
try|try
block|{
name|gateway
operator|.
name|write
argument_list|(
name|event
operator|.
name|state
argument_list|()
operator|.
name|metaData
argument_list|()
argument_list|)
expr_stmt|;
comment|// TODO, we need to remember that we failed, maybe add a retry scheduler?
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|logger
operator|.
name|error
argument_list|(
literal|"failed to write to gateway"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
comment|/**      * Reads from the gateway. If the waitTimeout is set, will wait till all the indices      * have been created from the meta data read from the gateway. Return value only applicable      * when waiting, and indicates that everything was created within teh wait timeout.      */
DECL|method|readFromGateway
specifier|private
name|Boolean
name|readFromGateway
parameter_list|(
annotation|@
name|Nullable
name|TimeValue
name|waitTimeout
parameter_list|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"reading state from gateway..."
argument_list|)
expr_stmt|;
name|MetaData
name|metaData
decl_stmt|;
try|try
block|{
name|metaData
operator|=
name|gateway
operator|.
name|read
argument_list|()
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
name|error
argument_list|(
literal|"failed to read from gateway"
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|markMetaDataAsReadFromGateway
argument_list|(
literal|"failure"
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
if|if
condition|(
name|metaData
operator|==
literal|null
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"no state read from gateway"
argument_list|)
expr_stmt|;
name|markMetaDataAsReadFromGateway
argument_list|(
literal|"no state"
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
specifier|final
name|MetaData
name|fMetaData
init|=
name|metaData
decl_stmt|;
specifier|final
name|CountDownLatch
name|latch
init|=
operator|new
name|CountDownLatch
argument_list|(
name|fMetaData
operator|.
name|indices
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|recoverAfterTime
operator|!=
literal|null
condition|)
block|{
name|updateClusterStateBlockedOnNotRecovered
argument_list|()
expr_stmt|;
name|logger
operator|.
name|debug
argument_list|(
literal|"delaying initial state index creation for [{}]"
argument_list|,
name|recoverAfterTime
argument_list|)
expr_stmt|;
name|threadPool
operator|.
name|schedule
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
name|updateClusterStateFromGateway
argument_list|(
name|fMetaData
argument_list|,
name|latch
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|,
name|recoverAfterTime
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|updateClusterStateFromGateway
argument_list|(
name|fMetaData
argument_list|,
name|latch
argument_list|)
expr_stmt|;
block|}
comment|// if we delay indices creation, then waiting for them does not make sense
if|if
condition|(
name|recoverAfterTime
operator|!=
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
if|if
condition|(
name|waitTimeout
operator|!=
literal|null
condition|)
block|{
try|try
block|{
return|return
name|latch
operator|.
name|await
argument_list|(
name|waitTimeout
operator|.
name|millis
argument_list|()
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
comment|// ignore
block|}
block|}
return|return
literal|null
return|;
block|}
DECL|method|markMetaDataAsReadFromGateway
specifier|private
name|void
name|markMetaDataAsReadFromGateway
parameter_list|(
name|String
name|reason
parameter_list|)
block|{
name|clusterService
operator|.
name|submitStateUpdateTask
argument_list|(
literal|"gateway (marked as read, reason="
operator|+
name|reason
operator|+
literal|")"
argument_list|,
operator|new
name|ClusterStateUpdateTask
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|ClusterState
name|execute
parameter_list|(
name|ClusterState
name|currentState
parameter_list|)
block|{
name|MetaData
operator|.
name|Builder
name|metaDataBuilder
init|=
name|newMetaDataBuilder
argument_list|()
operator|.
name|metaData
argument_list|(
name|currentState
operator|.
name|metaData
argument_list|()
argument_list|)
comment|// mark the metadata as read from gateway
operator|.
name|markAsRecoveredFromGateway
argument_list|()
decl_stmt|;
comment|// remove the block, since we recovered from gateway
name|ClusterBlocks
operator|.
name|Builder
name|blocks
init|=
name|ClusterBlocks
operator|.
name|builder
argument_list|()
operator|.
name|blocks
argument_list|(
name|currentState
operator|.
name|blocks
argument_list|()
argument_list|)
operator|.
name|removeGlobalBlock
argument_list|(
name|NOT_RECOVERED_FROM_GATEWAY_BLOCK
argument_list|)
decl_stmt|;
return|return
name|newClusterStateBuilder
argument_list|()
operator|.
name|state
argument_list|(
name|currentState
argument_list|)
operator|.
name|metaData
argument_list|(
name|metaDataBuilder
argument_list|)
operator|.
name|blocks
argument_list|(
name|blocks
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
DECL|method|updateClusterStateFromGateway
specifier|private
name|void
name|updateClusterStateFromGateway
parameter_list|(
specifier|final
name|MetaData
name|fMetaData
parameter_list|,
specifier|final
name|CountDownLatch
name|latch
parameter_list|)
block|{
name|clusterService
operator|.
name|submitStateUpdateTask
argument_list|(
literal|"gateway (recovered meta-data)"
argument_list|,
operator|new
name|ProcessedClusterStateUpdateTask
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|ClusterState
name|execute
parameter_list|(
name|ClusterState
name|currentState
parameter_list|)
block|{
name|MetaData
operator|.
name|Builder
name|metaDataBuilder
init|=
name|newMetaDataBuilder
argument_list|()
operator|.
name|metaData
argument_list|(
name|currentState
operator|.
name|metaData
argument_list|()
argument_list|)
operator|.
name|maxNumberOfShardsPerNode
argument_list|(
name|fMetaData
operator|.
name|maxNumberOfShardsPerNode
argument_list|()
argument_list|)
decl_stmt|;
comment|// mark the metadata as read from gateway
name|metaDataBuilder
operator|.
name|markAsRecoveredFromGateway
argument_list|()
expr_stmt|;
comment|// remove the block, since we recovered from gateway
name|ClusterBlocks
operator|.
name|Builder
name|blocks
init|=
name|ClusterBlocks
operator|.
name|builder
argument_list|()
operator|.
name|blocks
argument_list|(
name|currentState
operator|.
name|blocks
argument_list|()
argument_list|)
operator|.
name|removeGlobalBlock
argument_list|(
name|NOT_RECOVERED_FROM_GATEWAY_BLOCK
argument_list|)
decl_stmt|;
return|return
name|newClusterStateBuilder
argument_list|()
operator|.
name|state
argument_list|(
name|currentState
argument_list|)
operator|.
name|metaData
argument_list|(
name|metaDataBuilder
argument_list|)
operator|.
name|blocks
argument_list|(
name|blocks
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|clusterStateProcessed
parameter_list|(
name|ClusterState
name|clusterState
parameter_list|)
block|{
comment|// go over the meta data and create indices, we don't really need to copy over
comment|// the meta data per index, since we create the index and it will be added automatically
for|for
control|(
specifier|final
name|IndexMetaData
name|indexMetaData
range|:
name|fMetaData
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
name|metaDataService
operator|.
name|createIndex
argument_list|(
literal|"gateway"
argument_list|,
name|indexMetaData
operator|.
name|index
argument_list|()
argument_list|,
name|indexMetaData
operator|.
name|settings
argument_list|()
argument_list|,
name|indexMetaData
operator|.
name|mappings
argument_list|()
argument_list|,
name|timeValueMillis
argument_list|(
name|initialStateTimeout
operator|.
name|millis
argument_list|()
operator|-
literal|1000
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
name|logger
operator|.
name|error
argument_list|(
literal|"failed to create index ["
operator|+
name|indexMetaData
operator|.
name|index
argument_list|()
operator|+
literal|"]"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
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
block|}
block|}
argument_list|)
expr_stmt|;
block|}
DECL|method|updateClusterStateBlockedOnNotRecovered
specifier|private
name|void
name|updateClusterStateBlockedOnNotRecovered
parameter_list|()
block|{
name|clusterService
operator|.
name|submitStateUpdateTask
argument_list|(
literal|"gateway (block: not recovered from gateway)"
argument_list|,
operator|new
name|ClusterStateUpdateTask
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|ClusterState
name|execute
parameter_list|(
name|ClusterState
name|currentState
parameter_list|)
block|{
name|ClusterBlocks
name|blocks
init|=
name|ClusterBlocks
operator|.
name|builder
argument_list|()
operator|.
name|blocks
argument_list|(
name|currentState
operator|.
name|blocks
argument_list|()
argument_list|)
operator|.
name|addGlobalBlock
argument_list|(
name|NOT_RECOVERED_FROM_GATEWAY_BLOCK
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
return|return
name|ClusterState
operator|.
name|builder
argument_list|()
operator|.
name|state
argument_list|(
name|currentState
argument_list|)
operator|.
name|blocks
argument_list|(
name|blocks
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

