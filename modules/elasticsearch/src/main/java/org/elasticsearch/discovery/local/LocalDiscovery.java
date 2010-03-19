begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.discovery.local
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|discovery
operator|.
name|local
package|;
end_package

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
name|ElasticSearchIllegalStateException
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
name|node
operator|.
name|Node
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
name|Nodes
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
name|Discovery
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
name|InitialStateDiscoveryListener
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
name|component
operator|.
name|Lifecycle
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
name|java
operator|.
name|util
operator|.
name|Queue
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
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ConcurrentHashMap
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
name|ConcurrentLinkedQueue
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
name|ConcurrentMap
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
name|atomic
operator|.
name|AtomicBoolean
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
name|AtomicLong
import|;
end_import

begin_import
import|import static
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Sets
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

begin_comment
comment|/**  * @author kimchy (Shay Banon)  */
end_comment

begin_class
DECL|class|LocalDiscovery
specifier|public
class|class
name|LocalDiscovery
extends|extends
name|AbstractComponent
implements|implements
name|Discovery
block|{
DECL|field|lifecycle
specifier|private
specifier|final
name|Lifecycle
name|lifecycle
init|=
operator|new
name|Lifecycle
argument_list|()
decl_stmt|;
DECL|field|transportService
specifier|private
specifier|final
name|TransportService
name|transportService
decl_stmt|;
DECL|field|clusterService
specifier|private
specifier|final
name|ClusterService
name|clusterService
decl_stmt|;
DECL|field|clusterName
specifier|private
specifier|final
name|ClusterName
name|clusterName
decl_stmt|;
DECL|field|localNode
specifier|private
name|Node
name|localNode
decl_stmt|;
DECL|field|master
specifier|private
specifier|volatile
name|boolean
name|master
init|=
literal|false
decl_stmt|;
DECL|field|firstMaster
specifier|private
specifier|volatile
name|boolean
name|firstMaster
init|=
literal|false
decl_stmt|;
DECL|field|initialStateSent
specifier|private
specifier|final
name|AtomicBoolean
name|initialStateSent
init|=
operator|new
name|AtomicBoolean
argument_list|()
decl_stmt|;
DECL|field|initialStateListeners
specifier|private
specifier|final
name|CopyOnWriteArrayList
argument_list|<
name|InitialStateDiscoveryListener
argument_list|>
name|initialStateListeners
init|=
operator|new
name|CopyOnWriteArrayList
argument_list|<
name|InitialStateDiscoveryListener
argument_list|>
argument_list|()
decl_stmt|;
comment|// use CHM here and not ConcurrentMaps#new since we want to be able to agentify this using TC later on...
DECL|field|clusterGroups
specifier|private
specifier|static
specifier|final
name|ConcurrentMap
argument_list|<
name|ClusterName
argument_list|,
name|ClusterGroup
argument_list|>
name|clusterGroups
init|=
operator|new
name|ConcurrentHashMap
argument_list|<
name|ClusterName
argument_list|,
name|ClusterGroup
argument_list|>
argument_list|()
decl_stmt|;
DECL|field|nodeIdGenerator
specifier|private
specifier|static
specifier|final
name|AtomicLong
name|nodeIdGenerator
init|=
operator|new
name|AtomicLong
argument_list|()
decl_stmt|;
DECL|method|LocalDiscovery
annotation|@
name|Inject
specifier|public
name|LocalDiscovery
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
name|ClusterService
name|clusterService
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
name|clusterService
operator|=
name|clusterService
expr_stmt|;
name|this
operator|.
name|transportService
operator|=
name|transportService
expr_stmt|;
block|}
DECL|method|lifecycleState
annotation|@
name|Override
specifier|public
name|Lifecycle
operator|.
name|State
name|lifecycleState
parameter_list|()
block|{
return|return
name|this
operator|.
name|lifecycle
operator|.
name|state
argument_list|()
return|;
block|}
DECL|method|start
annotation|@
name|Override
specifier|public
name|Discovery
name|start
parameter_list|()
throws|throws
name|ElasticSearchException
block|{
if|if
condition|(
operator|!
name|lifecycle
operator|.
name|moveToStarted
argument_list|()
condition|)
block|{
return|return
name|this
return|;
block|}
synchronized|synchronized
init|(
name|clusterGroups
init|)
block|{
name|ClusterGroup
name|clusterGroup
init|=
name|clusterGroups
operator|.
name|get
argument_list|(
name|clusterName
argument_list|)
decl_stmt|;
if|if
condition|(
name|clusterGroup
operator|==
literal|null
condition|)
block|{
name|clusterGroup
operator|=
operator|new
name|ClusterGroup
argument_list|()
expr_stmt|;
name|clusterGroups
operator|.
name|put
argument_list|(
name|clusterName
argument_list|,
name|clusterGroup
argument_list|)
expr_stmt|;
block|}
name|logger
operator|.
name|debug
argument_list|(
literal|"Connected to cluster [{}]"
argument_list|,
name|clusterName
argument_list|)
expr_stmt|;
name|this
operator|.
name|localNode
operator|=
operator|new
name|Node
argument_list|(
name|settings
operator|.
name|get
argument_list|(
literal|"name"
argument_list|)
argument_list|,
name|settings
operator|.
name|getAsBoolean
argument_list|(
literal|"node.data"
argument_list|,
literal|true
argument_list|)
argument_list|,
name|Long
operator|.
name|toString
argument_list|(
name|nodeIdGenerator
operator|.
name|incrementAndGet
argument_list|()
argument_list|)
argument_list|,
name|transportService
operator|.
name|boundAddress
argument_list|()
operator|.
name|publishAddress
argument_list|()
argument_list|)
expr_stmt|;
name|clusterGroup
operator|.
name|members
argument_list|()
operator|.
name|add
argument_list|(
name|this
argument_list|)
expr_stmt|;
if|if
condition|(
name|clusterGroup
operator|.
name|members
argument_list|()
operator|.
name|size
argument_list|()
operator|==
literal|1
condition|)
block|{
comment|// we are the first master (and the master)
name|master
operator|=
literal|true
expr_stmt|;
name|firstMaster
operator|=
literal|true
expr_stmt|;
name|clusterService
operator|.
name|submitStateUpdateTask
argument_list|(
literal|"local-disco-initialconnect(master)"
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
name|Nodes
operator|.
name|Builder
name|builder
init|=
operator|new
name|Nodes
operator|.
name|Builder
argument_list|()
operator|.
name|localNodeId
argument_list|(
name|localNode
operator|.
name|id
argument_list|()
argument_list|)
operator|.
name|masterNodeId
argument_list|(
name|localNode
operator|.
name|id
argument_list|()
argument_list|)
comment|// put our local node
operator|.
name|put
argument_list|(
name|localNode
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
name|nodes
argument_list|(
name|builder
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
name|sendInitialStateEventIfNeeded
argument_list|()
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// we are not the master, tell the master to send it
name|LocalDiscovery
name|master
init|=
name|clusterGroup
operator|.
name|members
argument_list|()
operator|.
name|peek
argument_list|()
decl_stmt|;
name|master
operator|.
name|clusterService
operator|.
name|submitStateUpdateTask
argument_list|(
literal|"local-disco-receive(from node["
operator|+
name|localNode
operator|+
literal|"])"
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
if|if
condition|(
name|currentState
operator|.
name|nodes
argument_list|()
operator|.
name|nodeExists
argument_list|(
name|localNode
operator|.
name|id
argument_list|()
argument_list|)
condition|)
block|{
comment|// no change, the node already exists in the cluster
name|logger
operator|.
name|warn
argument_list|(
literal|"Received an address [{}] for an existing node [{}]"
argument_list|,
name|localNode
operator|.
name|address
argument_list|()
argument_list|,
name|localNode
argument_list|)
expr_stmt|;
return|return
name|currentState
return|;
block|}
return|return
name|newClusterStateBuilder
argument_list|()
operator|.
name|state
argument_list|(
name|currentState
argument_list|)
operator|.
name|nodes
argument_list|(
name|currentState
operator|.
name|nodes
argument_list|()
operator|.
name|newNode
argument_list|(
name|localNode
argument_list|)
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
name|sendInitialStateEventIfNeeded
argument_list|()
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|this
return|;
block|}
DECL|method|stop
annotation|@
name|Override
specifier|public
name|Discovery
name|stop
parameter_list|()
throws|throws
name|ElasticSearchException
block|{
if|if
condition|(
operator|!
name|lifecycle
operator|.
name|moveToStopped
argument_list|()
condition|)
block|{
return|return
name|this
return|;
block|}
synchronized|synchronized
init|(
name|clusterGroups
init|)
block|{
name|ClusterGroup
name|clusterGroup
init|=
name|clusterGroups
operator|.
name|get
argument_list|(
name|clusterName
argument_list|)
decl_stmt|;
if|if
condition|(
name|clusterGroup
operator|==
literal|null
condition|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"Illegal state, should not have an empty cluster group when stopping, I should be there at teh very least..."
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
name|clusterGroup
operator|.
name|members
argument_list|()
operator|.
name|remove
argument_list|(
name|this
argument_list|)
expr_stmt|;
if|if
condition|(
name|clusterGroup
operator|.
name|members
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
comment|// no more members, remove and return
name|clusterGroups
operator|.
name|remove
argument_list|(
name|clusterName
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
specifier|final
name|LocalDiscovery
name|masterDiscovery
init|=
name|clusterGroup
operator|.
name|members
argument_list|()
operator|.
name|peek
argument_list|()
decl_stmt|;
comment|// if the removed node is the master, make the next one as the master
if|if
condition|(
name|master
condition|)
block|{
name|masterDiscovery
operator|.
name|master
operator|=
literal|true
expr_stmt|;
block|}
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|newMembers
init|=
name|newHashSet
argument_list|()
decl_stmt|;
for|for
control|(
name|LocalDiscovery
name|discovery
range|:
name|clusterGroup
operator|.
name|members
argument_list|()
control|)
block|{
name|newMembers
operator|.
name|add
argument_list|(
name|discovery
operator|.
name|localNode
operator|.
name|id
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|masterDiscovery
operator|.
name|clusterService
operator|.
name|submitStateUpdateTask
argument_list|(
literal|"local-disco-update"
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
name|Nodes
name|newNodes
init|=
name|currentState
operator|.
name|nodes
argument_list|()
operator|.
name|removeDeadMembers
argument_list|(
name|newMembers
argument_list|,
name|masterDiscovery
operator|.
name|localNode
operator|.
name|id
argument_list|()
argument_list|)
decl_stmt|;
name|Nodes
operator|.
name|Delta
name|delta
init|=
name|newNodes
operator|.
name|delta
argument_list|(
name|currentState
operator|.
name|nodes
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|delta
operator|.
name|added
argument_list|()
condition|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"No new nodes should be created when a new discovery view is accepted"
argument_list|)
expr_stmt|;
block|}
return|return
name|newClusterStateBuilder
argument_list|()
operator|.
name|state
argument_list|(
name|currentState
argument_list|)
operator|.
name|nodes
argument_list|(
name|newNodes
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
return|return
name|this
return|;
block|}
DECL|method|close
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|ElasticSearchException
block|{
if|if
condition|(
name|lifecycle
operator|.
name|started
argument_list|()
condition|)
block|{
name|stop
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|lifecycle
operator|.
name|moveToClosed
argument_list|()
condition|)
block|{
return|return;
block|}
block|}
DECL|method|addListener
annotation|@
name|Override
specifier|public
name|void
name|addListener
parameter_list|(
name|InitialStateDiscoveryListener
name|listener
parameter_list|)
block|{
name|this
operator|.
name|initialStateListeners
operator|.
name|add
argument_list|(
name|listener
argument_list|)
expr_stmt|;
block|}
DECL|method|removeListener
annotation|@
name|Override
specifier|public
name|void
name|removeListener
parameter_list|(
name|InitialStateDiscoveryListener
name|listener
parameter_list|)
block|{
name|this
operator|.
name|initialStateListeners
operator|.
name|remove
argument_list|(
name|listener
argument_list|)
expr_stmt|;
block|}
DECL|method|nodeDescription
annotation|@
name|Override
specifier|public
name|String
name|nodeDescription
parameter_list|()
block|{
return|return
name|clusterName
operator|.
name|value
argument_list|()
operator|+
literal|"/"
operator|+
name|localNode
operator|.
name|id
argument_list|()
return|;
block|}
DECL|method|firstMaster
annotation|@
name|Override
specifier|public
name|boolean
name|firstMaster
parameter_list|()
block|{
return|return
name|firstMaster
return|;
block|}
DECL|method|publish
annotation|@
name|Override
specifier|public
name|void
name|publish
parameter_list|(
name|ClusterState
name|clusterState
parameter_list|)
block|{
if|if
condition|(
operator|!
name|master
condition|)
block|{
throw|throw
operator|new
name|ElasticSearchIllegalStateException
argument_list|(
literal|"Shouldn't publish state when not master"
argument_list|)
throw|;
block|}
name|ClusterGroup
name|clusterGroup
init|=
name|clusterGroups
operator|.
name|get
argument_list|(
name|clusterName
argument_list|)
decl_stmt|;
if|if
condition|(
name|clusterGroup
operator|==
literal|null
condition|)
block|{
comment|// nothing to publish to
return|return;
block|}
try|try
block|{
comment|// we do the marshaling intentionally, to check it works well...
specifier|final
name|byte
index|[]
name|clusterStateBytes
init|=
name|Builder
operator|.
name|toBytes
argument_list|(
name|clusterState
argument_list|)
decl_stmt|;
for|for
control|(
name|LocalDiscovery
name|discovery
range|:
name|clusterGroup
operator|.
name|members
argument_list|()
control|)
block|{
if|if
condition|(
name|discovery
operator|.
name|master
condition|)
block|{
continue|continue;
block|}
specifier|final
name|ClusterState
name|nodeSpecificClusterState
init|=
name|ClusterState
operator|.
name|Builder
operator|.
name|fromBytes
argument_list|(
name|clusterStateBytes
argument_list|,
name|discovery
operator|.
name|settings
argument_list|,
name|discovery
operator|.
name|localNode
argument_list|)
decl_stmt|;
comment|// ignore cluster state messages that do not include "me", not in the game yet...
if|if
condition|(
name|nodeSpecificClusterState
operator|.
name|nodes
argument_list|()
operator|.
name|localNode
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|discovery
operator|.
name|clusterService
operator|.
name|submitStateUpdateTask
argument_list|(
literal|"local-disco-receive(from master)"
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
return|return
name|nodeSpecificClusterState
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
name|sendInitialStateEventIfNeeded
argument_list|()
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// failure to marshal or un-marshal
throw|throw
operator|new
name|ElasticSearchIllegalStateException
argument_list|(
literal|"Cluster state failed to serialize"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
DECL|method|sendInitialStateEventIfNeeded
specifier|private
name|void
name|sendInitialStateEventIfNeeded
parameter_list|()
block|{
if|if
condition|(
name|initialStateSent
operator|.
name|compareAndSet
argument_list|(
literal|false
argument_list|,
literal|true
argument_list|)
condition|)
block|{
for|for
control|(
name|InitialStateDiscoveryListener
name|listener
range|:
name|initialStateListeners
control|)
block|{
name|listener
operator|.
name|initialStateProcessed
argument_list|()
expr_stmt|;
block|}
block|}
block|}
DECL|class|ClusterGroup
specifier|private
class|class
name|ClusterGroup
block|{
DECL|field|members
specifier|private
name|ConcurrentLinkedQueue
argument_list|<
name|LocalDiscovery
argument_list|>
name|members
init|=
operator|new
name|ConcurrentLinkedQueue
argument_list|<
name|LocalDiscovery
argument_list|>
argument_list|()
decl_stmt|;
DECL|method|members
name|Queue
argument_list|<
name|LocalDiscovery
argument_list|>
name|members
parameter_list|()
block|{
return|return
name|members
return|;
block|}
block|}
block|}
end_class

end_unit

