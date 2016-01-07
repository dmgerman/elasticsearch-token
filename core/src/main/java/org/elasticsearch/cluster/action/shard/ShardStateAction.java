begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.cluster.action.shard
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|action
operator|.
name|shard
package|;
end_package

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
name|RoutingService
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
name|FailedRerouteAllocation
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
name|Nullable
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
name|io
operator|.
name|stream
operator|.
name|StreamInput
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
name|io
operator|.
name|stream
operator|.
name|StreamOutput
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
name|EmptyTransportResponseHandler
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
name|TransportChannel
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
name|TransportException
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
name|TransportRequest
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
name|TransportRequestHandler
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
name|TransportRequestOptions
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
name|TransportResponse
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
name|ArrayList
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
name|Locale
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
name|routing
operator|.
name|ShardRouting
operator|.
name|readShardRoutingEntry
import|;
end_import

begin_class
DECL|class|ShardStateAction
specifier|public
class|class
name|ShardStateAction
extends|extends
name|AbstractComponent
block|{
DECL|field|SHARD_STARTED_ACTION_NAME
specifier|public
specifier|static
specifier|final
name|String
name|SHARD_STARTED_ACTION_NAME
init|=
literal|"internal:cluster/shard/started"
decl_stmt|;
DECL|field|SHARD_FAILED_ACTION_NAME
specifier|public
specifier|static
specifier|final
name|String
name|SHARD_FAILED_ACTION_NAME
init|=
literal|"internal:cluster/shard/failure"
decl_stmt|;
DECL|field|transportService
specifier|private
specifier|final
name|TransportService
name|transportService
decl_stmt|;
annotation|@
name|Inject
DECL|method|ShardStateAction
specifier|public
name|ShardStateAction
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ClusterService
name|clusterService
parameter_list|,
name|TransportService
name|transportService
parameter_list|,
name|AllocationService
name|allocationService
parameter_list|,
name|RoutingService
name|routingService
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|transportService
operator|=
name|transportService
expr_stmt|;
name|transportService
operator|.
name|registerRequestHandler
argument_list|(
name|SHARD_STARTED_ACTION_NAME
argument_list|,
name|ShardRoutingEntry
operator|::
operator|new
argument_list|,
name|ThreadPool
operator|.
name|Names
operator|.
name|SAME
argument_list|,
operator|new
name|ShardStartedTransportHandler
argument_list|(
name|clusterService
argument_list|,
operator|new
name|ShardStartedClusterStateTaskExecutor
argument_list|(
name|allocationService
argument_list|,
name|logger
argument_list|)
argument_list|,
name|logger
argument_list|)
argument_list|)
expr_stmt|;
name|transportService
operator|.
name|registerRequestHandler
argument_list|(
name|SHARD_FAILED_ACTION_NAME
argument_list|,
name|ShardRoutingEntry
operator|::
operator|new
argument_list|,
name|ThreadPool
operator|.
name|Names
operator|.
name|SAME
argument_list|,
operator|new
name|ShardFailedTransportHandler
argument_list|(
name|clusterService
argument_list|,
operator|new
name|ShardFailedClusterStateTaskExecutor
argument_list|(
name|allocationService
argument_list|,
name|routingService
argument_list|,
name|logger
argument_list|)
argument_list|,
name|logger
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|shardFailed
specifier|public
name|void
name|shardFailed
parameter_list|(
specifier|final
name|ClusterState
name|clusterState
parameter_list|,
specifier|final
name|ShardRouting
name|shardRouting
parameter_list|,
specifier|final
name|String
name|indexUUID
parameter_list|,
specifier|final
name|String
name|message
parameter_list|,
annotation|@
name|Nullable
specifier|final
name|Throwable
name|failure
parameter_list|,
name|Listener
name|listener
parameter_list|)
block|{
name|shardFailed
argument_list|(
name|clusterState
argument_list|,
name|shardRouting
argument_list|,
name|indexUUID
argument_list|,
name|message
argument_list|,
name|failure
argument_list|,
literal|null
argument_list|,
name|listener
argument_list|)
expr_stmt|;
block|}
DECL|method|resendShardFailed
specifier|public
name|void
name|resendShardFailed
parameter_list|(
specifier|final
name|ClusterState
name|clusterState
parameter_list|,
specifier|final
name|ShardRouting
name|shardRouting
parameter_list|,
specifier|final
name|String
name|indexUUID
parameter_list|,
specifier|final
name|String
name|message
parameter_list|,
annotation|@
name|Nullable
specifier|final
name|Throwable
name|failure
parameter_list|,
name|Listener
name|listener
parameter_list|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"{} re-sending failed shard [{}], index UUID [{}], reason [{}]"
argument_list|,
name|shardRouting
operator|.
name|shardId
argument_list|()
argument_list|,
name|failure
argument_list|,
name|shardRouting
argument_list|,
name|indexUUID
argument_list|,
name|message
argument_list|)
expr_stmt|;
name|shardFailed
argument_list|(
name|clusterState
argument_list|,
name|shardRouting
argument_list|,
name|indexUUID
argument_list|,
name|message
argument_list|,
name|failure
argument_list|,
name|listener
argument_list|)
expr_stmt|;
block|}
DECL|method|shardFailed
specifier|public
name|void
name|shardFailed
parameter_list|(
specifier|final
name|ClusterState
name|clusterState
parameter_list|,
specifier|final
name|ShardRouting
name|shardRouting
parameter_list|,
specifier|final
name|String
name|indexUUID
parameter_list|,
specifier|final
name|String
name|message
parameter_list|,
annotation|@
name|Nullable
specifier|final
name|Throwable
name|failure
parameter_list|,
name|TimeValue
name|timeout
parameter_list|,
name|Listener
name|listener
parameter_list|)
block|{
name|DiscoveryNode
name|masterNode
init|=
name|clusterState
operator|.
name|nodes
argument_list|()
operator|.
name|masterNode
argument_list|()
decl_stmt|;
if|if
condition|(
name|masterNode
operator|==
literal|null
condition|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"{} no master known to fail shard [{}]"
argument_list|,
name|shardRouting
operator|.
name|shardId
argument_list|()
argument_list|,
name|shardRouting
argument_list|)
expr_stmt|;
name|listener
operator|.
name|onShardFailedNoMaster
argument_list|()
expr_stmt|;
return|return;
block|}
name|ShardRoutingEntry
name|shardRoutingEntry
init|=
operator|new
name|ShardRoutingEntry
argument_list|(
name|shardRouting
argument_list|,
name|indexUUID
argument_list|,
name|message
argument_list|,
name|failure
argument_list|)
decl_stmt|;
name|TransportRequestOptions
name|options
init|=
name|TransportRequestOptions
operator|.
name|EMPTY
decl_stmt|;
if|if
condition|(
name|timeout
operator|!=
literal|null
condition|)
block|{
name|options
operator|=
name|TransportRequestOptions
operator|.
name|builder
argument_list|()
operator|.
name|withTimeout
argument_list|(
name|timeout
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
block|}
name|transportService
operator|.
name|sendRequest
argument_list|(
name|masterNode
argument_list|,
name|SHARD_FAILED_ACTION_NAME
argument_list|,
name|shardRoutingEntry
argument_list|,
name|options
argument_list|,
operator|new
name|EmptyTransportResponseHandler
argument_list|(
name|ThreadPool
operator|.
name|Names
operator|.
name|SAME
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|void
name|handleResponse
parameter_list|(
name|TransportResponse
operator|.
name|Empty
name|response
parameter_list|)
block|{
name|listener
operator|.
name|onSuccess
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
name|exp
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"{} unexpected failure while sending request to [{}] to fail shard [{}]"
argument_list|,
name|exp
argument_list|,
name|shardRoutingEntry
operator|.
name|shardRouting
operator|.
name|shardId
argument_list|()
argument_list|,
name|masterNode
argument_list|,
name|shardRoutingEntry
argument_list|)
expr_stmt|;
name|listener
operator|.
name|onShardFailedFailure
argument_list|(
name|masterNode
argument_list|,
name|exp
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
DECL|class|ShardFailedTransportHandler
specifier|private
specifier|static
class|class
name|ShardFailedTransportHandler
implements|implements
name|TransportRequestHandler
argument_list|<
name|ShardRoutingEntry
argument_list|>
block|{
DECL|field|clusterService
specifier|private
specifier|final
name|ClusterService
name|clusterService
decl_stmt|;
DECL|field|shardFailedClusterStateTaskExecutor
specifier|private
specifier|final
name|ShardFailedClusterStateTaskExecutor
name|shardFailedClusterStateTaskExecutor
decl_stmt|;
DECL|field|logger
specifier|private
specifier|final
name|ESLogger
name|logger
decl_stmt|;
DECL|method|ShardFailedTransportHandler
specifier|public
name|ShardFailedTransportHandler
parameter_list|(
name|ClusterService
name|clusterService
parameter_list|,
name|ShardFailedClusterStateTaskExecutor
name|shardFailedClusterStateTaskExecutor
parameter_list|,
name|ESLogger
name|logger
parameter_list|)
block|{
name|this
operator|.
name|clusterService
operator|=
name|clusterService
expr_stmt|;
name|this
operator|.
name|shardFailedClusterStateTaskExecutor
operator|=
name|shardFailedClusterStateTaskExecutor
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
DECL|method|messageReceived
specifier|public
name|void
name|messageReceived
parameter_list|(
name|ShardRoutingEntry
name|request
parameter_list|,
name|TransportChannel
name|channel
parameter_list|)
throws|throws
name|Exception
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"{} received shard failed for {}"
argument_list|,
name|request
operator|.
name|failure
argument_list|,
name|request
operator|.
name|shardRouting
operator|.
name|shardId
argument_list|()
argument_list|,
name|request
argument_list|)
expr_stmt|;
name|clusterService
operator|.
name|submitStateUpdateTask
argument_list|(
literal|"shard-failed ("
operator|+
name|request
operator|.
name|shardRouting
operator|+
literal|"), message ["
operator|+
name|request
operator|.
name|message
operator|+
literal|"]"
argument_list|,
name|request
argument_list|,
name|ClusterStateTaskConfig
operator|.
name|build
argument_list|(
name|Priority
operator|.
name|HIGH
argument_list|)
argument_list|,
name|shardFailedClusterStateTaskExecutor
argument_list|,
operator|new
name|ClusterStateTaskListener
argument_list|()
block|{
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
name|logger
operator|.
name|error
argument_list|(
literal|"{} unexpected failure while failing shard [{}]"
argument_list|,
name|t
argument_list|,
name|request
operator|.
name|shardRouting
operator|.
name|shardId
argument_list|()
argument_list|,
name|request
operator|.
name|shardRouting
argument_list|)
expr_stmt|;
try|try
block|{
name|channel
operator|.
name|sendResponse
argument_list|(
name|t
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|channelThrowable
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"{} failed to send failure [{}] while failing shard [{}]"
argument_list|,
name|channelThrowable
argument_list|,
name|request
operator|.
name|shardRouting
operator|.
name|shardId
argument_list|()
argument_list|,
name|t
argument_list|,
name|request
operator|.
name|shardRouting
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|onNoLongerMaster
parameter_list|(
name|String
name|source
parameter_list|)
block|{
name|logger
operator|.
name|error
argument_list|(
literal|"{} no longer master while failing shard [{}]"
argument_list|,
name|request
operator|.
name|shardRouting
operator|.
name|shardId
argument_list|()
argument_list|,
name|request
operator|.
name|shardRouting
argument_list|)
expr_stmt|;
try|try
block|{
name|channel
operator|.
name|sendResponse
argument_list|(
operator|new
name|NotMasterException
argument_list|(
name|source
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|channelThrowable
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"{} failed to send no longer master while failing shard [{}]"
argument_list|,
name|channelThrowable
argument_list|,
name|request
operator|.
name|shardRouting
operator|.
name|shardId
argument_list|()
argument_list|,
name|request
operator|.
name|shardRouting
argument_list|)
expr_stmt|;
block|}
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
block|{
try|try
block|{
name|channel
operator|.
name|sendResponse
argument_list|(
name|TransportResponse
operator|.
name|Empty
operator|.
name|INSTANCE
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|channelThrowable
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"{} failed to send response while failing shard [{}]"
argument_list|,
name|channelThrowable
argument_list|,
name|request
operator|.
name|shardRouting
operator|.
name|shardId
argument_list|()
argument_list|,
name|request
operator|.
name|shardRouting
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|ShardFailedClusterStateTaskExecutor
specifier|private
specifier|static
class|class
name|ShardFailedClusterStateTaskExecutor
implements|implements
name|ClusterStateTaskExecutor
argument_list|<
name|ShardRoutingEntry
argument_list|>
block|{
DECL|field|allocationService
specifier|private
specifier|final
name|AllocationService
name|allocationService
decl_stmt|;
DECL|field|routingService
specifier|private
specifier|final
name|RoutingService
name|routingService
decl_stmt|;
DECL|field|logger
specifier|private
specifier|final
name|ESLogger
name|logger
decl_stmt|;
DECL|method|ShardFailedClusterStateTaskExecutor
specifier|public
name|ShardFailedClusterStateTaskExecutor
parameter_list|(
name|AllocationService
name|allocationService
parameter_list|,
name|RoutingService
name|routingService
parameter_list|,
name|ESLogger
name|logger
parameter_list|)
block|{
name|this
operator|.
name|allocationService
operator|=
name|allocationService
expr_stmt|;
name|this
operator|.
name|routingService
operator|=
name|routingService
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
DECL|method|execute
specifier|public
name|BatchResult
argument_list|<
name|ShardRoutingEntry
argument_list|>
name|execute
parameter_list|(
name|ClusterState
name|currentState
parameter_list|,
name|List
argument_list|<
name|ShardRoutingEntry
argument_list|>
name|tasks
parameter_list|)
throws|throws
name|Exception
block|{
name|BatchResult
operator|.
name|Builder
argument_list|<
name|ShardRoutingEntry
argument_list|>
name|batchResultBuilder
init|=
name|BatchResult
operator|.
name|builder
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|FailedRerouteAllocation
operator|.
name|FailedShard
argument_list|>
name|failedShards
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|tasks
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|ShardRoutingEntry
name|task
range|:
name|tasks
control|)
block|{
name|failedShards
operator|.
name|add
argument_list|(
operator|new
name|FailedRerouteAllocation
operator|.
name|FailedShard
argument_list|(
name|task
operator|.
name|shardRouting
argument_list|,
name|task
operator|.
name|message
argument_list|,
name|task
operator|.
name|failure
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|ClusterState
name|maybeUpdatedState
init|=
name|currentState
decl_stmt|;
try|try
block|{
name|RoutingAllocation
operator|.
name|Result
name|result
init|=
name|allocationService
operator|.
name|applyFailedShards
argument_list|(
name|currentState
argument_list|,
name|failedShards
argument_list|)
decl_stmt|;
if|if
condition|(
name|result
operator|.
name|changed
argument_list|()
condition|)
block|{
name|maybeUpdatedState
operator|=
name|ClusterState
operator|.
name|builder
argument_list|(
name|currentState
argument_list|)
operator|.
name|routingResult
argument_list|(
name|result
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
block|}
name|batchResultBuilder
operator|.
name|successes
argument_list|(
name|tasks
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|batchResultBuilder
operator|.
name|failures
argument_list|(
name|tasks
argument_list|,
name|t
argument_list|)
expr_stmt|;
block|}
return|return
name|batchResultBuilder
operator|.
name|build
argument_list|(
name|maybeUpdatedState
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|clusterStatePublished
specifier|public
name|void
name|clusterStatePublished
parameter_list|(
name|ClusterState
name|newClusterState
parameter_list|)
block|{
name|int
name|numberOfUnassignedShards
init|=
name|newClusterState
operator|.
name|getRoutingNodes
argument_list|()
operator|.
name|unassigned
argument_list|()
operator|.
name|size
argument_list|()
decl_stmt|;
if|if
condition|(
name|numberOfUnassignedShards
operator|>
literal|0
condition|)
block|{
name|String
name|reason
init|=
name|String
operator|.
name|format
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|,
literal|"[%d] unassigned shards after failing shards"
argument_list|,
name|numberOfUnassignedShards
argument_list|)
decl_stmt|;
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
name|reason
operator|+
literal|", scheduling a reroute"
argument_list|)
expr_stmt|;
block|}
name|routingService
operator|.
name|reroute
argument_list|(
name|reason
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|shardStarted
specifier|public
name|void
name|shardStarted
parameter_list|(
specifier|final
name|ClusterState
name|clusterState
parameter_list|,
specifier|final
name|ShardRouting
name|shardRouting
parameter_list|,
name|String
name|indexUUID
parameter_list|,
specifier|final
name|String
name|reason
parameter_list|)
block|{
name|DiscoveryNode
name|masterNode
init|=
name|clusterState
operator|.
name|nodes
argument_list|()
operator|.
name|masterNode
argument_list|()
decl_stmt|;
if|if
condition|(
name|masterNode
operator|==
literal|null
condition|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"{} no master known to start shard [{}]"
argument_list|,
name|shardRouting
operator|.
name|shardId
argument_list|()
argument_list|,
name|shardRouting
argument_list|)
expr_stmt|;
return|return;
block|}
name|ShardRoutingEntry
name|shardRoutingEntry
init|=
operator|new
name|ShardRoutingEntry
argument_list|(
name|shardRouting
argument_list|,
name|indexUUID
argument_list|,
name|reason
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|logger
operator|.
name|debug
argument_list|(
literal|"sending start shard [{}]"
argument_list|,
name|shardRoutingEntry
argument_list|)
expr_stmt|;
name|transportService
operator|.
name|sendRequest
argument_list|(
name|masterNode
argument_list|,
name|SHARD_STARTED_ACTION_NAME
argument_list|,
operator|new
name|ShardRoutingEntry
argument_list|(
name|shardRouting
argument_list|,
name|indexUUID
argument_list|,
name|reason
argument_list|,
literal|null
argument_list|)
argument_list|,
operator|new
name|EmptyTransportResponseHandler
argument_list|(
name|ThreadPool
operator|.
name|Names
operator|.
name|SAME
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|void
name|handleException
parameter_list|(
name|TransportException
name|exp
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"{} failure sending start shard [{}] to [{}]"
argument_list|,
name|exp
argument_list|,
name|shardRouting
operator|.
name|shardId
argument_list|()
argument_list|,
name|masterNode
argument_list|,
name|shardRouting
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
DECL|class|ShardStartedTransportHandler
specifier|private
specifier|static
class|class
name|ShardStartedTransportHandler
implements|implements
name|TransportRequestHandler
argument_list|<
name|ShardRoutingEntry
argument_list|>
block|{
DECL|field|clusterService
specifier|private
specifier|final
name|ClusterService
name|clusterService
decl_stmt|;
DECL|field|shardStartedClusterStateTaskExecutor
specifier|private
specifier|final
name|ShardStartedClusterStateTaskExecutor
name|shardStartedClusterStateTaskExecutor
decl_stmt|;
DECL|field|logger
specifier|private
specifier|final
name|ESLogger
name|logger
decl_stmt|;
DECL|method|ShardStartedTransportHandler
specifier|public
name|ShardStartedTransportHandler
parameter_list|(
name|ClusterService
name|clusterService
parameter_list|,
name|ShardStartedClusterStateTaskExecutor
name|shardStartedClusterStateTaskExecutor
parameter_list|,
name|ESLogger
name|logger
parameter_list|)
block|{
name|this
operator|.
name|clusterService
operator|=
name|clusterService
expr_stmt|;
name|this
operator|.
name|shardStartedClusterStateTaskExecutor
operator|=
name|shardStartedClusterStateTaskExecutor
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
DECL|method|messageReceived
specifier|public
name|void
name|messageReceived
parameter_list|(
name|ShardRoutingEntry
name|request
parameter_list|,
name|TransportChannel
name|channel
parameter_list|)
throws|throws
name|Exception
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"{} received shard started for [{}]"
argument_list|,
name|request
operator|.
name|shardRouting
operator|.
name|shardId
argument_list|()
argument_list|,
name|request
argument_list|)
expr_stmt|;
name|clusterService
operator|.
name|submitStateUpdateTask
argument_list|(
literal|"shard-started ("
operator|+
name|request
operator|.
name|shardRouting
operator|+
literal|"), reason ["
operator|+
name|request
operator|.
name|message
operator|+
literal|"]"
argument_list|,
name|request
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
name|shardStartedClusterStateTaskExecutor
argument_list|,
name|shardStartedClusterStateTaskExecutor
argument_list|)
expr_stmt|;
name|channel
operator|.
name|sendResponse
argument_list|(
name|TransportResponse
operator|.
name|Empty
operator|.
name|INSTANCE
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|ShardStartedClusterStateTaskExecutor
specifier|private
specifier|static
class|class
name|ShardStartedClusterStateTaskExecutor
implements|implements
name|ClusterStateTaskExecutor
argument_list|<
name|ShardRoutingEntry
argument_list|>
implements|,
name|ClusterStateTaskListener
block|{
DECL|field|allocationService
specifier|private
specifier|final
name|AllocationService
name|allocationService
decl_stmt|;
DECL|field|logger
specifier|private
specifier|final
name|ESLogger
name|logger
decl_stmt|;
DECL|method|ShardStartedClusterStateTaskExecutor
specifier|public
name|ShardStartedClusterStateTaskExecutor
parameter_list|(
name|AllocationService
name|allocationService
parameter_list|,
name|ESLogger
name|logger
parameter_list|)
block|{
name|this
operator|.
name|allocationService
operator|=
name|allocationService
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
DECL|method|execute
specifier|public
name|BatchResult
argument_list|<
name|ShardRoutingEntry
argument_list|>
name|execute
parameter_list|(
name|ClusterState
name|currentState
parameter_list|,
name|List
argument_list|<
name|ShardRoutingEntry
argument_list|>
name|tasks
parameter_list|)
throws|throws
name|Exception
block|{
name|BatchResult
operator|.
name|Builder
argument_list|<
name|ShardRoutingEntry
argument_list|>
name|builder
init|=
name|BatchResult
operator|.
name|builder
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|ShardRouting
argument_list|>
name|shardRoutingsToBeApplied
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|tasks
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|ShardRoutingEntry
name|task
range|:
name|tasks
control|)
block|{
name|shardRoutingsToBeApplied
operator|.
name|add
argument_list|(
name|task
operator|.
name|shardRouting
argument_list|)
expr_stmt|;
block|}
name|ClusterState
name|maybeUpdatedState
init|=
name|currentState
decl_stmt|;
try|try
block|{
name|RoutingAllocation
operator|.
name|Result
name|result
init|=
name|allocationService
operator|.
name|applyStartedShards
argument_list|(
name|currentState
argument_list|,
name|shardRoutingsToBeApplied
argument_list|,
literal|true
argument_list|)
decl_stmt|;
if|if
condition|(
name|result
operator|.
name|changed
argument_list|()
condition|)
block|{
name|maybeUpdatedState
operator|=
name|ClusterState
operator|.
name|builder
argument_list|(
name|currentState
argument_list|)
operator|.
name|routingResult
argument_list|(
name|result
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
block|}
name|builder
operator|.
name|successes
argument_list|(
name|tasks
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|builder
operator|.
name|failures
argument_list|(
name|tasks
argument_list|,
name|t
argument_list|)
expr_stmt|;
block|}
return|return
name|builder
operator|.
name|build
argument_list|(
name|maybeUpdatedState
argument_list|)
return|;
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
name|Throwable
name|t
parameter_list|)
block|{
name|logger
operator|.
name|error
argument_list|(
literal|"unexpected failure during [{}]"
argument_list|,
name|t
argument_list|,
name|source
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|ShardRoutingEntry
specifier|public
specifier|static
class|class
name|ShardRoutingEntry
extends|extends
name|TransportRequest
block|{
DECL|field|shardRouting
name|ShardRouting
name|shardRouting
decl_stmt|;
DECL|field|indexUUID
name|String
name|indexUUID
init|=
name|IndexMetaData
operator|.
name|INDEX_UUID_NA_VALUE
decl_stmt|;
DECL|field|message
name|String
name|message
decl_stmt|;
DECL|field|failure
name|Throwable
name|failure
decl_stmt|;
DECL|method|ShardRoutingEntry
specifier|public
name|ShardRoutingEntry
parameter_list|()
block|{         }
DECL|method|ShardRoutingEntry
name|ShardRoutingEntry
parameter_list|(
name|ShardRouting
name|shardRouting
parameter_list|,
name|String
name|indexUUID
parameter_list|,
name|String
name|message
parameter_list|,
annotation|@
name|Nullable
name|Throwable
name|failure
parameter_list|)
block|{
name|this
operator|.
name|shardRouting
operator|=
name|shardRouting
expr_stmt|;
name|this
operator|.
name|indexUUID
operator|=
name|indexUUID
expr_stmt|;
name|this
operator|.
name|message
operator|=
name|message
expr_stmt|;
name|this
operator|.
name|failure
operator|=
name|failure
expr_stmt|;
block|}
DECL|method|getShardRouting
specifier|public
name|ShardRouting
name|getShardRouting
parameter_list|()
block|{
return|return
name|shardRouting
return|;
block|}
annotation|@
name|Override
DECL|method|readFrom
specifier|public
name|void
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|shardRouting
operator|=
name|readShardRoutingEntry
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|indexUUID
operator|=
name|in
operator|.
name|readString
argument_list|()
expr_stmt|;
name|message
operator|=
name|in
operator|.
name|readString
argument_list|()
expr_stmt|;
name|failure
operator|=
name|in
operator|.
name|readThrowable
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|writeTo
specifier|public
name|void
name|writeTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|shardRouting
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeString
argument_list|(
name|indexUUID
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeString
argument_list|(
name|message
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeThrowable
argument_list|(
name|failure
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|""
operator|+
name|shardRouting
operator|+
literal|", indexUUID ["
operator|+
name|indexUUID
operator|+
literal|"], message ["
operator|+
name|message
operator|+
literal|"], failure ["
operator|+
name|ExceptionsHelper
operator|.
name|detailedMessage
argument_list|(
name|failure
argument_list|)
operator|+
literal|"]"
return|;
block|}
block|}
DECL|interface|Listener
specifier|public
interface|interface
name|Listener
block|{
DECL|method|onSuccess
specifier|default
name|void
name|onSuccess
parameter_list|()
block|{         }
DECL|method|onShardFailedNoMaster
specifier|default
name|void
name|onShardFailedNoMaster
parameter_list|()
block|{         }
DECL|method|onShardFailedFailure
specifier|default
name|void
name|onShardFailedFailure
parameter_list|(
specifier|final
name|DiscoveryNode
name|master
parameter_list|,
specifier|final
name|TransportException
name|e
parameter_list|)
block|{         }
block|}
block|}
end_class

end_unit

