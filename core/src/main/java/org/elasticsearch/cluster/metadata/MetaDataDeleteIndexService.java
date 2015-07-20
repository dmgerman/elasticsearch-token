begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.cluster.metadata
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|metadata
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
name|support
operator|.
name|master
operator|.
name|MasterNodeRequest
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
name|TimeoutClusterStateUpdateTask
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
name|action
operator|.
name|index
operator|.
name|NodeIndexDeletedAction
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
name|routing
operator|.
name|RoutingTable
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
name|common
operator|.
name|util
operator|.
name|concurrent
operator|.
name|FutureUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|IndexNotFoundException
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
name|Semaphore
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

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|MetaDataDeleteIndexService
specifier|public
class|class
name|MetaDataDeleteIndexService
extends|extends
name|AbstractComponent
block|{
DECL|field|threadPool
specifier|private
specifier|final
name|ThreadPool
name|threadPool
decl_stmt|;
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
DECL|field|nodeIndexDeletedAction
specifier|private
specifier|final
name|NodeIndexDeletedAction
name|nodeIndexDeletedAction
decl_stmt|;
DECL|field|metaDataService
specifier|private
specifier|final
name|MetaDataService
name|metaDataService
decl_stmt|;
annotation|@
name|Inject
DECL|method|MetaDataDeleteIndexService
specifier|public
name|MetaDataDeleteIndexService
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|,
name|ClusterService
name|clusterService
parameter_list|,
name|AllocationService
name|allocationService
parameter_list|,
name|NodeIndexDeletedAction
name|nodeIndexDeletedAction
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
name|threadPool
operator|=
name|threadPool
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
name|nodeIndexDeletedAction
operator|=
name|nodeIndexDeletedAction
expr_stmt|;
name|this
operator|.
name|metaDataService
operator|=
name|metaDataService
expr_stmt|;
block|}
DECL|method|deleteIndex
specifier|public
name|void
name|deleteIndex
parameter_list|(
specifier|final
name|Request
name|request
parameter_list|,
specifier|final
name|Listener
name|userListener
parameter_list|)
block|{
comment|// we lock here, and not within the cluster service callback since we don't want to
comment|// block the whole cluster state handling
specifier|final
name|Semaphore
name|mdLock
init|=
name|metaDataService
operator|.
name|indexMetaDataLock
argument_list|(
name|request
operator|.
name|index
argument_list|)
decl_stmt|;
comment|// quick check to see if we can acquire a lock, otherwise spawn to a thread pool
if|if
condition|(
name|mdLock
operator|.
name|tryAcquire
argument_list|()
condition|)
block|{
name|deleteIndex
argument_list|(
name|request
argument_list|,
name|userListener
argument_list|,
name|mdLock
argument_list|)
expr_stmt|;
return|return;
block|}
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
name|mdLock
operator|.
name|tryAcquire
argument_list|(
name|request
operator|.
name|masterTimeout
operator|.
name|nanos
argument_list|()
argument_list|,
name|TimeUnit
operator|.
name|NANOSECONDS
argument_list|)
condition|)
block|{
name|userListener
operator|.
name|onFailure
argument_list|(
operator|new
name|ProcessClusterEventTimeoutException
argument_list|(
name|request
operator|.
name|masterTimeout
argument_list|,
literal|"acquire index lock"
argument_list|)
argument_list|)
expr_stmt|;
return|return;
block|}
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|userListener
operator|.
name|onFailure
argument_list|(
name|e
argument_list|)
expr_stmt|;
return|return;
block|}
name|deleteIndex
argument_list|(
name|request
argument_list|,
name|userListener
argument_list|,
name|mdLock
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
DECL|method|deleteIndex
specifier|private
name|void
name|deleteIndex
parameter_list|(
specifier|final
name|Request
name|request
parameter_list|,
specifier|final
name|Listener
name|userListener
parameter_list|,
name|Semaphore
name|mdLock
parameter_list|)
block|{
specifier|final
name|DeleteIndexListener
name|listener
init|=
operator|new
name|DeleteIndexListener
argument_list|(
name|mdLock
argument_list|,
name|userListener
argument_list|)
decl_stmt|;
name|clusterService
operator|.
name|submitStateUpdateTask
argument_list|(
literal|"delete-index ["
operator|+
name|request
operator|.
name|index
operator|+
literal|"]"
argument_list|,
name|Priority
operator|.
name|URGENT
argument_list|,
operator|new
name|TimeoutClusterStateUpdateTask
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|TimeValue
name|timeout
parameter_list|()
block|{
return|return
name|request
operator|.
name|masterTimeout
return|;
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
name|Throwable
name|t
parameter_list|)
block|{
name|listener
operator|.
name|onFailure
argument_list|(
name|t
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|ClusterState
name|execute
parameter_list|(
specifier|final
name|ClusterState
name|currentState
parameter_list|)
block|{
if|if
condition|(
operator|!
name|currentState
operator|.
name|metaData
argument_list|()
operator|.
name|hasConcreteIndex
argument_list|(
name|request
operator|.
name|index
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IndexNotFoundException
argument_list|(
name|request
operator|.
name|index
argument_list|)
throw|;
block|}
name|logger
operator|.
name|info
argument_list|(
literal|"[{}] deleting index"
argument_list|,
name|request
operator|.
name|index
argument_list|)
expr_stmt|;
name|RoutingTable
operator|.
name|Builder
name|routingTableBuilder
init|=
name|RoutingTable
operator|.
name|builder
argument_list|(
name|currentState
operator|.
name|routingTable
argument_list|()
argument_list|)
decl_stmt|;
name|routingTableBuilder
operator|.
name|remove
argument_list|(
name|request
operator|.
name|index
argument_list|)
expr_stmt|;
name|MetaData
name|newMetaData
init|=
name|MetaData
operator|.
name|builder
argument_list|(
name|currentState
operator|.
name|metaData
argument_list|()
argument_list|)
operator|.
name|remove
argument_list|(
name|request
operator|.
name|index
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|RoutingAllocation
operator|.
name|Result
name|routingResult
init|=
name|allocationService
operator|.
name|reroute
argument_list|(
name|ClusterState
operator|.
name|builder
argument_list|(
name|currentState
argument_list|)
operator|.
name|routingTable
argument_list|(
name|routingTableBuilder
argument_list|)
operator|.
name|metaData
argument_list|(
name|newMetaData
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
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
name|removeIndexBlocks
argument_list|(
name|request
operator|.
name|index
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
comment|// wait for events from all nodes that it has been removed from their respective metadata...
name|int
name|count
init|=
name|currentState
operator|.
name|nodes
argument_list|()
operator|.
name|size
argument_list|()
decl_stmt|;
comment|// add the notifications that the store was deleted from *data* nodes
name|count
operator|+=
name|currentState
operator|.
name|nodes
argument_list|()
operator|.
name|dataNodes
argument_list|()
operator|.
name|size
argument_list|()
expr_stmt|;
specifier|final
name|AtomicInteger
name|counter
init|=
operator|new
name|AtomicInteger
argument_list|(
name|count
argument_list|)
decl_stmt|;
comment|// this listener will be notified once we get back a notification based on the cluster state change below.
specifier|final
name|NodeIndexDeletedAction
operator|.
name|Listener
name|nodeIndexDeleteListener
init|=
operator|new
name|NodeIndexDeletedAction
operator|.
name|Listener
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onNodeIndexDeleted
parameter_list|(
name|String
name|index
parameter_list|,
name|String
name|nodeId
parameter_list|)
block|{
if|if
condition|(
name|index
operator|.
name|equals
argument_list|(
name|request
operator|.
name|index
argument_list|)
condition|)
block|{
if|if
condition|(
name|counter
operator|.
name|decrementAndGet
argument_list|()
operator|==
literal|0
condition|)
block|{
name|listener
operator|.
name|onResponse
argument_list|(
operator|new
name|Response
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|nodeIndexDeletedAction
operator|.
name|remove
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|onNodeIndexStoreDeleted
parameter_list|(
name|String
name|index
parameter_list|,
name|String
name|nodeId
parameter_list|)
block|{
if|if
condition|(
name|index
operator|.
name|equals
argument_list|(
name|request
operator|.
name|index
argument_list|)
condition|)
block|{
if|if
condition|(
name|counter
operator|.
name|decrementAndGet
argument_list|()
operator|==
literal|0
condition|)
block|{
name|listener
operator|.
name|onResponse
argument_list|(
operator|new
name|Response
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|nodeIndexDeletedAction
operator|.
name|remove
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
decl_stmt|;
name|nodeIndexDeletedAction
operator|.
name|add
argument_list|(
name|nodeIndexDeleteListener
argument_list|)
expr_stmt|;
name|listener
operator|.
name|future
operator|=
name|threadPool
operator|.
name|schedule
argument_list|(
name|request
operator|.
name|timeout
argument_list|,
name|ThreadPool
operator|.
name|Names
operator|.
name|SAME
argument_list|,
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
name|listener
operator|.
name|onResponse
argument_list|(
operator|new
name|Response
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|nodeIndexDeletedAction
operator|.
name|remove
argument_list|(
name|nodeIndexDeleteListener
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
return|return
name|ClusterState
operator|.
name|builder
argument_list|(
name|currentState
argument_list|)
operator|.
name|routingResult
argument_list|(
name|routingResult
argument_list|)
operator|.
name|metaData
argument_list|(
name|newMetaData
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
name|String
name|source
parameter_list|,
name|ClusterState
name|oldState
parameter_list|,
name|ClusterState
name|newState
parameter_list|)
block|{             }
block|}
argument_list|)
expr_stmt|;
block|}
DECL|class|DeleteIndexListener
class|class
name|DeleteIndexListener
implements|implements
name|Listener
block|{
DECL|field|notified
specifier|private
specifier|final
name|AtomicBoolean
name|notified
init|=
operator|new
name|AtomicBoolean
argument_list|()
decl_stmt|;
DECL|field|mdLock
specifier|private
specifier|final
name|Semaphore
name|mdLock
decl_stmt|;
DECL|field|listener
specifier|private
specifier|final
name|Listener
name|listener
decl_stmt|;
DECL|field|future
specifier|volatile
name|ScheduledFuture
argument_list|<
name|?
argument_list|>
name|future
decl_stmt|;
DECL|method|DeleteIndexListener
specifier|private
name|DeleteIndexListener
parameter_list|(
name|Semaphore
name|mdLock
parameter_list|,
name|Listener
name|listener
parameter_list|)
block|{
name|this
operator|.
name|mdLock
operator|=
name|mdLock
expr_stmt|;
name|this
operator|.
name|listener
operator|=
name|listener
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|onResponse
specifier|public
name|void
name|onResponse
parameter_list|(
specifier|final
name|Response
name|response
parameter_list|)
block|{
if|if
condition|(
name|notified
operator|.
name|compareAndSet
argument_list|(
literal|false
argument_list|,
literal|true
argument_list|)
condition|)
block|{
name|mdLock
operator|.
name|release
argument_list|()
expr_stmt|;
name|FutureUtils
operator|.
name|cancel
argument_list|(
name|future
argument_list|)
expr_stmt|;
name|listener
operator|.
name|onResponse
argument_list|(
name|response
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|onFailure
specifier|public
name|void
name|onFailure
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
if|if
condition|(
name|notified
operator|.
name|compareAndSet
argument_list|(
literal|false
argument_list|,
literal|true
argument_list|)
condition|)
block|{
name|mdLock
operator|.
name|release
argument_list|()
expr_stmt|;
name|FutureUtils
operator|.
name|cancel
argument_list|(
name|future
argument_list|)
expr_stmt|;
name|listener
operator|.
name|onFailure
argument_list|(
name|t
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|interface|Listener
specifier|public
specifier|static
interface|interface
name|Listener
block|{
DECL|method|onResponse
name|void
name|onResponse
parameter_list|(
name|Response
name|response
parameter_list|)
function_decl|;
DECL|method|onFailure
name|void
name|onFailure
parameter_list|(
name|Throwable
name|t
parameter_list|)
function_decl|;
block|}
DECL|class|Request
specifier|public
specifier|static
class|class
name|Request
block|{
DECL|field|index
specifier|final
name|String
name|index
decl_stmt|;
DECL|field|timeout
name|TimeValue
name|timeout
init|=
name|TimeValue
operator|.
name|timeValueSeconds
argument_list|(
literal|10
argument_list|)
decl_stmt|;
DECL|field|masterTimeout
name|TimeValue
name|masterTimeout
init|=
name|MasterNodeRequest
operator|.
name|DEFAULT_MASTER_NODE_TIMEOUT
decl_stmt|;
DECL|method|Request
specifier|public
name|Request
parameter_list|(
name|String
name|index
parameter_list|)
block|{
name|this
operator|.
name|index
operator|=
name|index
expr_stmt|;
block|}
DECL|method|timeout
specifier|public
name|Request
name|timeout
parameter_list|(
name|TimeValue
name|timeout
parameter_list|)
block|{
name|this
operator|.
name|timeout
operator|=
name|timeout
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|masterTimeout
specifier|public
name|Request
name|masterTimeout
parameter_list|(
name|TimeValue
name|masterTimeout
parameter_list|)
block|{
name|this
operator|.
name|masterTimeout
operator|=
name|masterTimeout
expr_stmt|;
return|return
name|this
return|;
block|}
block|}
DECL|class|Response
specifier|public
specifier|static
class|class
name|Response
block|{
DECL|field|acknowledged
specifier|private
specifier|final
name|boolean
name|acknowledged
decl_stmt|;
DECL|method|Response
specifier|public
name|Response
parameter_list|(
name|boolean
name|acknowledged
parameter_list|)
block|{
name|this
operator|.
name|acknowledged
operator|=
name|acknowledged
expr_stmt|;
block|}
DECL|method|acknowledged
specifier|public
name|boolean
name|acknowledged
parameter_list|()
block|{
return|return
name|acknowledged
return|;
block|}
block|}
block|}
end_class

end_unit

