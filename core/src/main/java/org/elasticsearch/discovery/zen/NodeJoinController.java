begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.discovery.zen
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|discovery
operator|.
name|zen
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|store
operator|.
name|AlreadyClosedException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|Version
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
name|ClusterStateTaskConfig
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
name|ClusterStateTaskExecutor
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
name|ClusterStateTaskListener
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
name|NotMasterException
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
name|cluster
operator|.
name|routing
operator|.
name|allocation
operator|.
name|AllocationService
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
name|allocation
operator|.
name|RoutingAllocation
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
name|service
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
name|common
operator|.
name|Priority
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
name|logging
operator|.
name|ESLogger
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
name|LocalTransportAddress
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
name|DiscoverySettings
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
name|zen
operator|.
name|elect
operator|.
name|ElectMasterService
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
name|zen
operator|.
name|membership
operator|.
name|MembershipAction
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
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

begin_comment
comment|/**  * This class processes incoming join request (passed zia {@link ZenDiscovery}). Incoming nodes  * are directly added to the cluster state or are accumulated during master election.  */
end_comment

begin_class
DECL|class|NodeJoinController
specifier|public
class|class
name|NodeJoinController
extends|extends
name|AbstractComponent
block|{
DECL|field|clusterService
specifier|private
specifier|final
name|ClusterService
name|clusterService
decl_stmt|;
DECL|field|allocationService
specifier|private
specifier|final
name|AllocationService
name|allocationService
decl_stmt|;
DECL|field|electMaster
specifier|private
specifier|final
name|ElectMasterService
name|electMaster
decl_stmt|;
DECL|field|discoverySettings
specifier|private
specifier|final
name|DiscoverySettings
name|discoverySettings
decl_stmt|;
DECL|field|joinTaskExecutor
specifier|private
specifier|final
name|JoinTaskExecutor
name|joinTaskExecutor
init|=
operator|new
name|JoinTaskExecutor
argument_list|()
decl_stmt|;
comment|// this is set while trying to become a master
comment|// mutation should be done under lock
DECL|field|electionContext
specifier|private
name|ElectionContext
name|electionContext
init|=
literal|null
decl_stmt|;
DECL|method|NodeJoinController
specifier|public
name|NodeJoinController
parameter_list|(
name|ClusterService
name|clusterService
parameter_list|,
name|AllocationService
name|allocationService
parameter_list|,
name|ElectMasterService
name|electMaster
parameter_list|,
name|DiscoverySettings
name|discoverySettings
parameter_list|,
name|Settings
name|settings
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|clusterService
operator|=
name|clusterService
expr_stmt|;
name|this
operator|.
name|allocationService
operator|=
name|allocationService
expr_stmt|;
name|this
operator|.
name|electMaster
operator|=
name|electMaster
expr_stmt|;
name|this
operator|.
name|discoverySettings
operator|=
name|discoverySettings
expr_stmt|;
block|}
comment|/**      * waits for enough incoming joins from master eligible nodes to complete the master election      *<p>      * You must start accumulating joins before calling this method. See {@link #startElectionContext()}      *<p>      * The method will return once the local node has been elected as master or some failure/timeout has happened.      * The exact outcome is communicated via the callback parameter, which is guaranteed to be called.      *      * @param requiredMasterJoins the number of joins from master eligible needed to complete the election      * @param timeValue           how long to wait before failing. a timeout is communicated via the callback's onFailure method.      * @param callback            the result of the election (success or failure) will be communicated by calling methods on this      *                            object      **/
DECL|method|waitToBeElectedAsMaster
specifier|public
name|void
name|waitToBeElectedAsMaster
parameter_list|(
name|int
name|requiredMasterJoins
parameter_list|,
name|TimeValue
name|timeValue
parameter_list|,
specifier|final
name|ElectionCallback
name|callback
parameter_list|)
block|{
specifier|final
name|CountDownLatch
name|done
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
specifier|final
name|ElectionCallback
name|wrapperCallback
init|=
operator|new
name|ElectionCallback
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onElectedAsMaster
parameter_list|(
name|ClusterState
name|state
parameter_list|)
block|{
name|done
operator|.
name|countDown
argument_list|()
expr_stmt|;
name|callback
operator|.
name|onElectedAsMaster
argument_list|(
name|state
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
name|t
parameter_list|)
block|{
name|done
operator|.
name|countDown
argument_list|()
expr_stmt|;
name|callback
operator|.
name|onFailure
argument_list|(
name|t
argument_list|)
expr_stmt|;
block|}
block|}
decl_stmt|;
name|ElectionContext
name|myElectionContext
init|=
literal|null
decl_stmt|;
try|try
block|{
comment|// check what we have so far..
comment|// capture the context we add the callback to make sure we fail our own
synchronized|synchronized
init|(
name|this
init|)
block|{
assert|assert
name|electionContext
operator|!=
literal|null
operator|:
literal|"waitToBeElectedAsMaster is called we are not accumulating joins"
assert|;
name|myElectionContext
operator|=
name|electionContext
expr_stmt|;
name|electionContext
operator|.
name|onAttemptToBeElected
argument_list|(
name|requiredMasterJoins
argument_list|,
name|wrapperCallback
argument_list|)
expr_stmt|;
name|checkPendingJoinsAndElectIfNeeded
argument_list|()
expr_stmt|;
block|}
try|try
block|{
if|if
condition|(
name|done
operator|.
name|await
argument_list|(
name|timeValue
operator|.
name|millis
argument_list|()
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
condition|)
block|{
comment|// callback handles everything
return|return;
block|}
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{              }
if|if
condition|(
name|logger
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
specifier|final
name|int
name|pendingNodes
init|=
name|myElectionContext
operator|.
name|getPendingMasterJoinsCount
argument_list|()
decl_stmt|;
name|logger
operator|.
name|trace
argument_list|(
literal|"timed out waiting to be elected. waited [{}]. pending master node joins [{}]"
argument_list|,
name|timeValue
argument_list|,
name|pendingNodes
argument_list|)
expr_stmt|;
block|}
name|failContextIfNeeded
argument_list|(
name|myElectionContext
argument_list|,
literal|"timed out waiting to be elected"
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
literal|"unexpected failure while waiting for incoming joins"
argument_list|,
name|e
argument_list|)
expr_stmt|;
if|if
condition|(
name|myElectionContext
operator|!=
literal|null
condition|)
block|{
name|failContextIfNeeded
argument_list|(
name|myElectionContext
argument_list|,
literal|"unexpected failure while waiting for pending joins ["
operator|+
name|e
operator|.
name|getMessage
argument_list|()
operator|+
literal|"]"
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**      * utility method to fail the given election context under the cluster state thread      */
DECL|method|failContextIfNeeded
specifier|private
specifier|synchronized
name|void
name|failContextIfNeeded
parameter_list|(
specifier|final
name|ElectionContext
name|context
parameter_list|,
specifier|final
name|String
name|reason
parameter_list|)
block|{
if|if
condition|(
name|electionContext
operator|==
name|context
condition|)
block|{
name|stopElectionContext
argument_list|(
name|reason
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      * Accumulates any future incoming join request. Pending join requests will be processed in the final steps of becoming a      * master or when {@link #stopElectionContext(String)} is called.      */
DECL|method|startElectionContext
specifier|public
specifier|synchronized
name|void
name|startElectionContext
parameter_list|()
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"starting an election context, will accumulate joins"
argument_list|)
expr_stmt|;
assert|assert
name|electionContext
operator|==
literal|null
operator|:
literal|"double startElectionContext() calls"
assert|;
name|electionContext
operator|=
operator|new
name|ElectionContext
argument_list|()
expr_stmt|;
block|}
comment|/**      * Stopped accumulating joins. All pending joins will be processed. Future joins will be processed immediately      */
DECL|method|stopElectionContext
specifier|public
name|void
name|stopElectionContext
parameter_list|(
name|String
name|reason
parameter_list|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"stopping election ([{}])"
argument_list|,
name|reason
argument_list|)
expr_stmt|;
synchronized|synchronized
init|(
name|this
init|)
block|{
assert|assert
name|electionContext
operator|!=
literal|null
operator|:
literal|"stopElectionContext() called but not accumulating"
assert|;
name|electionContext
operator|.
name|closeAndProcessPending
argument_list|(
name|reason
argument_list|)
expr_stmt|;
name|electionContext
operator|=
literal|null
expr_stmt|;
block|}
block|}
comment|/**      * processes or queues an incoming join request.      *<p>      * Note: doesn't do any validation. This should have been done before.      */
DECL|method|handleJoinRequest
specifier|public
specifier|synchronized
name|void
name|handleJoinRequest
parameter_list|(
specifier|final
name|DiscoveryNode
name|node
parameter_list|,
specifier|final
name|MembershipAction
operator|.
name|JoinCallback
name|callback
parameter_list|)
block|{
if|if
condition|(
name|electionContext
operator|!=
literal|null
condition|)
block|{
name|electionContext
operator|.
name|addIncomingJoin
argument_list|(
name|node
argument_list|,
name|callback
argument_list|)
expr_stmt|;
name|checkPendingJoinsAndElectIfNeeded
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|clusterService
operator|.
name|submitStateUpdateTask
argument_list|(
literal|"zen-disco-join(node "
operator|+
name|node
operator|+
literal|"])"
argument_list|,
name|node
argument_list|,
name|ClusterStateTaskConfig
operator|.
name|build
argument_list|(
name|Priority
operator|.
name|URGENT
argument_list|)
argument_list|,
name|joinTaskExecutor
argument_list|,
operator|new
name|JoinTaskListener
argument_list|(
name|callback
argument_list|,
name|logger
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      * checks if there is an on going request to become master and if it has enough pending joins. If so, the node will      * become master via a ClusterState update task.      */
DECL|method|checkPendingJoinsAndElectIfNeeded
specifier|private
specifier|synchronized
name|void
name|checkPendingJoinsAndElectIfNeeded
parameter_list|()
block|{
assert|assert
name|electionContext
operator|!=
literal|null
operator|:
literal|"election check requested but no active context"
assert|;
specifier|final
name|int
name|pendingMasterJoins
init|=
name|electionContext
operator|.
name|getPendingMasterJoinsCount
argument_list|()
decl_stmt|;
if|if
condition|(
name|electionContext
operator|.
name|isEnoughPendingJoins
argument_list|(
name|pendingMasterJoins
argument_list|)
operator|==
literal|false
condition|)
block|{
if|if
condition|(
name|logger
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"not enough joins for election. Got [{}], required [{}]"
argument_list|,
name|pendingMasterJoins
argument_list|,
name|electionContext
operator|.
name|requiredMasterJoins
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
if|if
condition|(
name|logger
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"have enough joins for election. Got [{}], required [{}]"
argument_list|,
name|pendingMasterJoins
argument_list|,
name|electionContext
operator|.
name|requiredMasterJoins
argument_list|)
expr_stmt|;
block|}
name|electionContext
operator|.
name|closeAndBecomeMaster
argument_list|()
expr_stmt|;
name|electionContext
operator|=
literal|null
expr_stmt|;
comment|// clear this out so future joins won't be accumulated
block|}
block|}
DECL|interface|ElectionCallback
specifier|public
interface|interface
name|ElectionCallback
block|{
comment|/**          * called when the local node is successfully elected as master          * Guaranteed to be called on the cluster state update thread          **/
DECL|method|onElectedAsMaster
name|void
name|onElectedAsMaster
parameter_list|(
name|ClusterState
name|state
parameter_list|)
function_decl|;
comment|/**          * called when the local node failed to be elected as master          * Guaranteed to be called on the cluster state update thread          **/
DECL|method|onFailure
name|void
name|onFailure
parameter_list|(
name|Throwable
name|t
parameter_list|)
function_decl|;
block|}
DECL|class|ElectionContext
class|class
name|ElectionContext
block|{
DECL|field|callback
specifier|private
name|ElectionCallback
name|callback
init|=
literal|null
decl_stmt|;
DECL|field|requiredMasterJoins
specifier|private
name|int
name|requiredMasterJoins
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|joinRequestAccumulator
specifier|private
specifier|final
name|Map
argument_list|<
name|DiscoveryNode
argument_list|,
name|List
argument_list|<
name|MembershipAction
operator|.
name|JoinCallback
argument_list|>
argument_list|>
name|joinRequestAccumulator
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
DECL|field|closed
specifier|final
name|AtomicBoolean
name|closed
init|=
operator|new
name|AtomicBoolean
argument_list|()
decl_stmt|;
DECL|method|onAttemptToBeElected
specifier|public
specifier|synchronized
name|void
name|onAttemptToBeElected
parameter_list|(
name|int
name|requiredMasterJoins
parameter_list|,
name|ElectionCallback
name|callback
parameter_list|)
block|{
name|ensureOpen
argument_list|()
expr_stmt|;
assert|assert
name|this
operator|.
name|requiredMasterJoins
operator|<
literal|0
assert|;
assert|assert
name|this
operator|.
name|callback
operator|==
literal|null
assert|;
name|this
operator|.
name|requiredMasterJoins
operator|=
name|requiredMasterJoins
expr_stmt|;
name|this
operator|.
name|callback
operator|=
name|callback
expr_stmt|;
block|}
DECL|method|addIncomingJoin
specifier|public
specifier|synchronized
name|void
name|addIncomingJoin
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|,
name|MembershipAction
operator|.
name|JoinCallback
name|callback
parameter_list|)
block|{
name|ensureOpen
argument_list|()
expr_stmt|;
name|joinRequestAccumulator
operator|.
name|computeIfAbsent
argument_list|(
name|node
argument_list|,
name|n
lambda|->
operator|new
name|ArrayList
argument_list|<>
argument_list|()
argument_list|)
operator|.
name|add
argument_list|(
name|callback
argument_list|)
expr_stmt|;
block|}
DECL|method|isEnoughPendingJoins
specifier|public
specifier|synchronized
name|boolean
name|isEnoughPendingJoins
parameter_list|(
name|int
name|pendingMasterJoins
parameter_list|)
block|{
specifier|final
name|boolean
name|hasEnough
decl_stmt|;
if|if
condition|(
name|requiredMasterJoins
operator|<
literal|0
condition|)
block|{
comment|// requiredMasterNodes is unknown yet, return false and keep on waiting
name|hasEnough
operator|=
literal|false
expr_stmt|;
block|}
else|else
block|{
assert|assert
name|callback
operator|!=
literal|null
operator|:
literal|"requiredMasterJoins is set but not the callback"
assert|;
name|hasEnough
operator|=
name|pendingMasterJoins
operator|>=
name|requiredMasterJoins
expr_stmt|;
block|}
return|return
name|hasEnough
return|;
block|}
DECL|method|getPendingAsTasks
specifier|private
name|Map
argument_list|<
name|DiscoveryNode
argument_list|,
name|ClusterStateTaskListener
argument_list|>
name|getPendingAsTasks
parameter_list|()
block|{
name|Map
argument_list|<
name|DiscoveryNode
argument_list|,
name|ClusterStateTaskListener
argument_list|>
name|tasks
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|joinRequestAccumulator
operator|.
name|entrySet
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|forEach
argument_list|(
name|e
lambda|->
name|tasks
operator|.
name|put
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|,
operator|new
name|JoinTaskListener
argument_list|(
name|e
operator|.
name|getValue
argument_list|()
argument_list|,
name|logger
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|tasks
return|;
block|}
DECL|method|getPendingMasterJoinsCount
specifier|public
specifier|synchronized
name|int
name|getPendingMasterJoinsCount
parameter_list|()
block|{
name|int
name|pendingMasterJoins
init|=
literal|0
decl_stmt|;
for|for
control|(
name|DiscoveryNode
name|node
range|:
name|joinRequestAccumulator
operator|.
name|keySet
argument_list|()
control|)
block|{
if|if
condition|(
name|node
operator|.
name|isMasterNode
argument_list|()
condition|)
block|{
name|pendingMasterJoins
operator|++
expr_stmt|;
block|}
block|}
return|return
name|pendingMasterJoins
return|;
block|}
DECL|method|closeAndBecomeMaster
specifier|public
specifier|synchronized
name|void
name|closeAndBecomeMaster
parameter_list|()
block|{
assert|assert
name|callback
operator|!=
literal|null
operator|:
literal|"becoming a master but the callback is not yet set"
assert|;
assert|assert
name|isEnoughPendingJoins
argument_list|(
name|getPendingMasterJoinsCount
argument_list|()
argument_list|)
operator|:
literal|"becoming a master but pending joins of "
operator|+
name|getPendingMasterJoinsCount
argument_list|()
operator|+
literal|" are not enough. needs ["
operator|+
name|requiredMasterJoins
operator|+
literal|"];"
assert|;
name|innerClose
argument_list|()
expr_stmt|;
name|Map
argument_list|<
name|DiscoveryNode
argument_list|,
name|ClusterStateTaskListener
argument_list|>
name|tasks
init|=
name|getPendingAsTasks
argument_list|()
decl_stmt|;
specifier|final
name|String
name|source
init|=
literal|"zen-disco-join(elected_as_master, ["
operator|+
name|tasks
operator|.
name|size
argument_list|()
operator|+
literal|"] nodes joined)"
decl_stmt|;
name|tasks
operator|.
name|put
argument_list|(
name|BECOME_MASTER_TASK
argument_list|,
name|joinProcessedListener
argument_list|)
expr_stmt|;
name|clusterService
operator|.
name|submitStateUpdateTasks
argument_list|(
name|source
argument_list|,
name|tasks
argument_list|,
name|ClusterStateTaskConfig
operator|.
name|build
argument_list|(
name|Priority
operator|.
name|URGENT
argument_list|)
argument_list|,
name|joinTaskExecutor
argument_list|)
expr_stmt|;
block|}
DECL|method|closeAndProcessPending
specifier|public
specifier|synchronized
name|void
name|closeAndProcessPending
parameter_list|(
name|String
name|reason
parameter_list|)
block|{
name|innerClose
argument_list|()
expr_stmt|;
name|Map
argument_list|<
name|DiscoveryNode
argument_list|,
name|ClusterStateTaskListener
argument_list|>
name|tasks
init|=
name|getPendingAsTasks
argument_list|()
decl_stmt|;
specifier|final
name|String
name|source
init|=
literal|"zen-disco-join(election stopped ["
operator|+
name|reason
operator|+
literal|"] nodes joined"
decl_stmt|;
name|tasks
operator|.
name|put
argument_list|(
name|FINISH_ELECTION_NOT_MASTER_TASK
argument_list|,
name|joinProcessedListener
argument_list|)
expr_stmt|;
name|clusterService
operator|.
name|submitStateUpdateTasks
argument_list|(
name|source
argument_list|,
name|tasks
argument_list|,
name|ClusterStateTaskConfig
operator|.
name|build
argument_list|(
name|Priority
operator|.
name|URGENT
argument_list|)
argument_list|,
name|joinTaskExecutor
argument_list|)
expr_stmt|;
block|}
DECL|method|innerClose
specifier|private
name|void
name|innerClose
parameter_list|()
block|{
if|if
condition|(
name|closed
operator|.
name|getAndSet
argument_list|(
literal|true
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|AlreadyClosedException
argument_list|(
literal|"election context is already closed"
argument_list|)
throw|;
block|}
block|}
DECL|method|ensureOpen
specifier|private
name|void
name|ensureOpen
parameter_list|()
block|{
if|if
condition|(
name|closed
operator|.
name|get
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|AlreadyClosedException
argument_list|(
literal|"election context is already closed"
argument_list|)
throw|;
block|}
block|}
DECL|method|getCallback
specifier|private
specifier|synchronized
name|ElectionCallback
name|getCallback
parameter_list|()
block|{
return|return
name|callback
return|;
block|}
DECL|method|onElectedAsMaster
specifier|private
name|void
name|onElectedAsMaster
parameter_list|(
name|ClusterState
name|state
parameter_list|)
block|{
name|ClusterService
operator|.
name|assertClusterStateThread
argument_list|()
expr_stmt|;
assert|assert
name|state
operator|.
name|nodes
argument_list|()
operator|.
name|isLocalNodeElectedMaster
argument_list|()
operator|:
literal|"onElectedAsMaster called but local node is not master"
assert|;
name|ElectionCallback
name|callback
init|=
name|getCallback
argument_list|()
decl_stmt|;
comment|// get under lock
if|if
condition|(
name|callback
operator|!=
literal|null
condition|)
block|{
name|callback
operator|.
name|onElectedAsMaster
argument_list|(
name|state
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|onFailure
specifier|private
name|void
name|onFailure
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|ClusterService
operator|.
name|assertClusterStateThread
argument_list|()
expr_stmt|;
name|ElectionCallback
name|callback
init|=
name|getCallback
argument_list|()
decl_stmt|;
comment|// get under lock
if|if
condition|(
name|callback
operator|!=
literal|null
condition|)
block|{
name|callback
operator|.
name|onFailure
argument_list|(
name|t
argument_list|)
expr_stmt|;
block|}
block|}
DECL|field|joinProcessedListener
specifier|private
specifier|final
name|ClusterStateTaskListener
name|joinProcessedListener
init|=
operator|new
name|ClusterStateTaskListener
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|clusterStateProcessed
parameter_list|(
name|String
name|source
parameter_list|,
name|ClusterState
name|oldState
parameter_list|,
name|ClusterState
name|newState
parameter_list|)
block|{
assert|assert
name|newState
operator|.
name|nodes
argument_list|()
operator|.
name|isLocalNodeElectedMaster
argument_list|()
operator|:
literal|"should have become a master but isn't "
operator|+
name|newState
operator|.
name|prettyPrint
argument_list|()
assert|;
name|onElectedAsMaster
argument_list|(
name|newState
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|onFailure
parameter_list|(
name|String
name|source
parameter_list|,
name|Exception
name|e
parameter_list|)
block|{
name|ElectionContext
operator|.
name|this
operator|.
name|onFailure
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
decl_stmt|;
block|}
DECL|class|JoinTaskListener
specifier|static
class|class
name|JoinTaskListener
implements|implements
name|ClusterStateTaskListener
block|{
DECL|field|callbacks
specifier|final
name|List
argument_list|<
name|MembershipAction
operator|.
name|JoinCallback
argument_list|>
name|callbacks
decl_stmt|;
DECL|field|logger
specifier|private
specifier|final
name|ESLogger
name|logger
decl_stmt|;
DECL|method|JoinTaskListener
name|JoinTaskListener
parameter_list|(
name|MembershipAction
operator|.
name|JoinCallback
name|callback
parameter_list|,
name|ESLogger
name|logger
parameter_list|)
block|{
name|this
argument_list|(
name|Collections
operator|.
name|singletonList
argument_list|(
name|callback
argument_list|)
argument_list|,
name|logger
argument_list|)
expr_stmt|;
block|}
DECL|method|JoinTaskListener
name|JoinTaskListener
parameter_list|(
name|List
argument_list|<
name|MembershipAction
operator|.
name|JoinCallback
argument_list|>
name|callbacks
parameter_list|,
name|ESLogger
name|logger
parameter_list|)
block|{
name|this
operator|.
name|callbacks
operator|=
name|callbacks
expr_stmt|;
name|this
operator|.
name|logger
operator|=
name|logger
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|onFailure
specifier|public
name|void
name|onFailure
parameter_list|(
name|String
name|source
parameter_list|,
name|Exception
name|e
parameter_list|)
block|{
for|for
control|(
name|MembershipAction
operator|.
name|JoinCallback
name|callback
range|:
name|callbacks
control|)
block|{
try|try
block|{
name|callback
operator|.
name|onFailure
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|inner
parameter_list|)
block|{
name|logger
operator|.
name|error
argument_list|(
literal|"error handling task failure [{}]"
argument_list|,
name|inner
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
DECL|method|clusterStateProcessed
specifier|public
name|void
name|clusterStateProcessed
parameter_list|(
name|String
name|source
parameter_list|,
name|ClusterState
name|oldState
parameter_list|,
name|ClusterState
name|newState
parameter_list|)
block|{
for|for
control|(
name|MembershipAction
operator|.
name|JoinCallback
name|callback
range|:
name|callbacks
control|)
block|{
try|try
block|{
name|callback
operator|.
name|onSuccess
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
literal|"unexpected error during [{}]"
argument_list|,
name|e
argument_list|,
name|source
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
comment|// a task indicated that the current node should become master, if no current master is known
DECL|field|BECOME_MASTER_TASK
specifier|private
specifier|static
specifier|final
name|DiscoveryNode
name|BECOME_MASTER_TASK
init|=
operator|new
name|DiscoveryNode
argument_list|(
literal|"_BECOME_MASTER_TASK_"
argument_list|,
name|LocalTransportAddress
operator|.
name|buildUnique
argument_list|()
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|,
name|Collections
operator|.
name|emptySet
argument_list|()
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
decl_stmt|;
comment|// a task that is used to process pending joins without explicitly becoming a master and listening to the results
comment|// this task is used when election is stop without the local node becoming a master per se (though it might
DECL|field|FINISH_ELECTION_NOT_MASTER_TASK
specifier|private
specifier|static
specifier|final
name|DiscoveryNode
name|FINISH_ELECTION_NOT_MASTER_TASK
init|=
operator|new
name|DiscoveryNode
argument_list|(
literal|"_NOT_MASTER_TASK_"
argument_list|,
name|LocalTransportAddress
operator|.
name|buildUnique
argument_list|()
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|,
name|Collections
operator|.
name|emptySet
argument_list|()
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
decl_stmt|;
DECL|class|JoinTaskExecutor
class|class
name|JoinTaskExecutor
implements|implements
name|ClusterStateTaskExecutor
argument_list|<
name|DiscoveryNode
argument_list|>
block|{
annotation|@
name|Override
DECL|method|execute
specifier|public
name|BatchResult
argument_list|<
name|DiscoveryNode
argument_list|>
name|execute
parameter_list|(
name|ClusterState
name|currentState
parameter_list|,
name|List
argument_list|<
name|DiscoveryNode
argument_list|>
name|joiningNodes
parameter_list|)
throws|throws
name|Exception
block|{
specifier|final
name|DiscoveryNodes
name|currentNodes
init|=
name|currentState
operator|.
name|nodes
argument_list|()
decl_stmt|;
specifier|final
name|BatchResult
operator|.
name|Builder
argument_list|<
name|DiscoveryNode
argument_list|>
name|results
init|=
name|BatchResult
operator|.
name|builder
argument_list|()
decl_stmt|;
name|boolean
name|nodesChanged
init|=
literal|false
decl_stmt|;
name|ClusterState
operator|.
name|Builder
name|newState
init|=
name|ClusterState
operator|.
name|builder
argument_list|(
name|currentState
argument_list|)
decl_stmt|;
name|DiscoveryNodes
operator|.
name|Builder
name|nodesBuilder
init|=
name|DiscoveryNodes
operator|.
name|builder
argument_list|(
name|currentNodes
argument_list|)
decl_stmt|;
if|if
condition|(
name|currentNodes
operator|.
name|getMasterNode
argument_list|()
operator|==
literal|null
operator|&&
name|joiningNodes
operator|.
name|contains
argument_list|(
name|BECOME_MASTER_TASK
argument_list|)
condition|)
block|{
comment|// use these joins to try and become the master.
comment|// Note that we don't have to do any validation of the amount of joining nodes - the commit
comment|// during the cluster state publishing guarantees that we have enough
name|nodesBuilder
operator|.
name|masterNodeId
argument_list|(
name|currentNodes
operator|.
name|getLocalNodeId
argument_list|()
argument_list|)
expr_stmt|;
name|ClusterBlocks
name|clusterBlocks
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
name|discoverySettings
operator|.
name|getNoMasterBlock
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|newState
operator|.
name|blocks
argument_list|(
name|clusterBlocks
argument_list|)
expr_stmt|;
name|nodesChanged
operator|=
literal|true
expr_stmt|;
block|}
if|if
condition|(
name|nodesBuilder
operator|.
name|isLocalNodeElectedMaster
argument_list|()
operator|==
literal|false
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"processing node joins, but we are not the master. current master: {}"
argument_list|,
name|currentNodes
operator|.
name|getMasterNode
argument_list|()
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|NotMasterException
argument_list|(
literal|"Node ["
operator|+
name|currentNodes
operator|.
name|getLocalNode
argument_list|()
operator|+
literal|"] not master for join request"
argument_list|)
throw|;
block|}
for|for
control|(
specifier|final
name|DiscoveryNode
name|node
range|:
name|joiningNodes
control|)
block|{
if|if
condition|(
name|node
operator|.
name|equals
argument_list|(
name|BECOME_MASTER_TASK
argument_list|)
operator|||
name|node
operator|.
name|equals
argument_list|(
name|FINISH_ELECTION_NOT_MASTER_TASK
argument_list|)
condition|)
block|{
comment|// noop
block|}
elseif|else
if|if
condition|(
name|currentNodes
operator|.
name|nodeExists
argument_list|(
name|node
argument_list|)
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"received a join request for an existing node [{}]"
argument_list|,
name|node
argument_list|)
expr_stmt|;
block|}
else|else
block|{
try|try
block|{
name|nodesBuilder
operator|.
name|put
argument_list|(
name|node
argument_list|)
expr_stmt|;
name|nodesChanged
operator|=
literal|true
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
name|results
operator|.
name|failure
argument_list|(
name|node
argument_list|,
name|e
argument_list|)
expr_stmt|;
continue|continue;
block|}
block|}
name|results
operator|.
name|success
argument_list|(
name|node
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|nodesChanged
condition|)
block|{
name|newState
operator|.
name|nodes
argument_list|(
name|nodesBuilder
argument_list|)
expr_stmt|;
specifier|final
name|ClusterState
name|tmpState
init|=
name|newState
operator|.
name|build
argument_list|()
decl_stmt|;
name|RoutingAllocation
operator|.
name|Result
name|result
init|=
name|allocationService
operator|.
name|reroute
argument_list|(
name|tmpState
argument_list|,
literal|"node_join"
argument_list|)
decl_stmt|;
name|newState
operator|=
name|ClusterState
operator|.
name|builder
argument_list|(
name|tmpState
argument_list|)
expr_stmt|;
if|if
condition|(
name|result
operator|.
name|changed
argument_list|()
condition|)
block|{
name|newState
operator|.
name|routingResult
argument_list|(
name|result
argument_list|)
expr_stmt|;
block|}
block|}
comment|// we must return a new cluster state instance to force publishing. This is important
comment|// for the joining node to finalize its join and set us as a master
return|return
name|results
operator|.
name|build
argument_list|(
name|newState
operator|.
name|build
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|runOnlyOnMaster
specifier|public
name|boolean
name|runOnlyOnMaster
parameter_list|()
block|{
comment|// we validate that we are allowed to change the cluster state during cluster state processing
return|return
literal|false
return|;
block|}
annotation|@
name|Override
DECL|method|clusterStatePublished
specifier|public
name|void
name|clusterStatePublished
parameter_list|(
name|ClusterChangedEvent
name|event
parameter_list|)
block|{
name|NodeJoinController
operator|.
name|this
operator|.
name|electMaster
operator|.
name|logMinimumMasterNodesWarningIfNecessary
argument_list|(
name|event
operator|.
name|previousState
argument_list|()
argument_list|,
name|event
operator|.
name|state
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

