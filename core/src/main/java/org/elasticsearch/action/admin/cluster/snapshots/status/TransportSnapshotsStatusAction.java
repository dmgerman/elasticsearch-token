begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.cluster.snapshots.status
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
name|snapshots
operator|.
name|status
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
name|cursors
operator|.
name|ObjectCursor
import|;
end_import

begin_import
import|import
name|com
operator|.
name|carrotsearch
operator|.
name|hppc
operator|.
name|cursors
operator|.
name|ObjectObjectCursor
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
name|support
operator|.
name|ActionFilters
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
name|TransportMasterNodeAction
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
name|SnapshotsInProgress
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
name|ClusterBlockException
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
name|ClusterBlockLevel
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
name|IndexNameExpressionResolver
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
name|util
operator|.
name|set
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
name|index
operator|.
name|shard
operator|.
name|ShardId
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
name|snapshots
operator|.
name|IndexShardSnapshotStatus
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|snapshots
operator|.
name|Snapshot
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|snapshots
operator|.
name|SnapshotId
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|snapshots
operator|.
name|SnapshotInfo
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|snapshots
operator|.
name|SnapshotMissingException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|snapshots
operator|.
name|SnapshotsService
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
name|Arrays
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
name|HashSet
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
name|Set
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|function
operator|.
name|Function
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|Collectors
import|;
end_import

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|TransportSnapshotsStatusAction
specifier|public
class|class
name|TransportSnapshotsStatusAction
extends|extends
name|TransportMasterNodeAction
argument_list|<
name|SnapshotsStatusRequest
argument_list|,
name|SnapshotsStatusResponse
argument_list|>
block|{
DECL|field|snapshotsService
specifier|private
specifier|final
name|SnapshotsService
name|snapshotsService
decl_stmt|;
DECL|field|transportNodesSnapshotsStatus
specifier|private
specifier|final
name|TransportNodesSnapshotsStatus
name|transportNodesSnapshotsStatus
decl_stmt|;
annotation|@
name|Inject
DECL|method|TransportSnapshotsStatusAction
specifier|public
name|TransportSnapshotsStatusAction
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
name|SnapshotsService
name|snapshotsService
parameter_list|,
name|TransportNodesSnapshotsStatus
name|transportNodesSnapshotsStatus
parameter_list|,
name|ActionFilters
name|actionFilters
parameter_list|,
name|IndexNameExpressionResolver
name|indexNameExpressionResolver
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|SnapshotsStatusAction
operator|.
name|NAME
argument_list|,
name|transportService
argument_list|,
name|clusterService
argument_list|,
name|threadPool
argument_list|,
name|actionFilters
argument_list|,
name|indexNameExpressionResolver
argument_list|,
name|SnapshotsStatusRequest
operator|::
operator|new
argument_list|)
expr_stmt|;
name|this
operator|.
name|snapshotsService
operator|=
name|snapshotsService
expr_stmt|;
name|this
operator|.
name|transportNodesSnapshotsStatus
operator|=
name|transportNodesSnapshotsStatus
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|executor
specifier|protected
name|String
name|executor
parameter_list|()
block|{
return|return
name|ThreadPool
operator|.
name|Names
operator|.
name|GENERIC
return|;
block|}
annotation|@
name|Override
DECL|method|checkBlock
specifier|protected
name|ClusterBlockException
name|checkBlock
parameter_list|(
name|SnapshotsStatusRequest
name|request
parameter_list|,
name|ClusterState
name|state
parameter_list|)
block|{
return|return
name|state
operator|.
name|blocks
argument_list|()
operator|.
name|globalBlockedException
argument_list|(
name|ClusterBlockLevel
operator|.
name|METADATA_READ
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|newResponse
specifier|protected
name|SnapshotsStatusResponse
name|newResponse
parameter_list|()
block|{
return|return
operator|new
name|SnapshotsStatusResponse
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|masterOperation
specifier|protected
name|void
name|masterOperation
parameter_list|(
specifier|final
name|SnapshotsStatusRequest
name|request
parameter_list|,
specifier|final
name|ClusterState
name|state
parameter_list|,
specifier|final
name|ActionListener
argument_list|<
name|SnapshotsStatusResponse
argument_list|>
name|listener
parameter_list|)
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|SnapshotsInProgress
operator|.
name|Entry
argument_list|>
name|currentSnapshots
init|=
name|snapshotsService
operator|.
name|currentSnapshots
argument_list|(
name|request
operator|.
name|repository
argument_list|()
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|request
operator|.
name|snapshots
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|currentSnapshots
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|listener
operator|.
name|onResponse
argument_list|(
name|buildResponse
argument_list|(
name|request
argument_list|,
name|currentSnapshots
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
return|return;
block|}
name|Set
argument_list|<
name|String
argument_list|>
name|nodesIds
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|SnapshotsInProgress
operator|.
name|Entry
name|entry
range|:
name|currentSnapshots
control|)
block|{
for|for
control|(
name|ObjectCursor
argument_list|<
name|SnapshotsInProgress
operator|.
name|ShardSnapshotStatus
argument_list|>
name|status
range|:
name|entry
operator|.
name|shards
argument_list|()
operator|.
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|status
operator|.
name|value
operator|.
name|nodeId
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|nodesIds
operator|.
name|add
argument_list|(
name|status
operator|.
name|value
operator|.
name|nodeId
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
operator|!
name|nodesIds
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
comment|// There are still some snapshots running - check their progress
name|Snapshot
index|[]
name|snapshots
init|=
operator|new
name|Snapshot
index|[
name|currentSnapshots
operator|.
name|size
argument_list|()
index|]
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
name|currentSnapshots
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|snapshots
index|[
name|i
index|]
operator|=
name|currentSnapshots
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|snapshot
argument_list|()
expr_stmt|;
block|}
name|TransportNodesSnapshotsStatus
operator|.
name|Request
name|nodesRequest
init|=
operator|new
name|TransportNodesSnapshotsStatus
operator|.
name|Request
argument_list|(
name|nodesIds
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
name|nodesIds
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|)
operator|.
name|snapshots
argument_list|(
name|snapshots
argument_list|)
operator|.
name|timeout
argument_list|(
name|request
operator|.
name|masterNodeTimeout
argument_list|()
argument_list|)
decl_stmt|;
name|transportNodesSnapshotsStatus
operator|.
name|execute
argument_list|(
name|nodesRequest
argument_list|,
operator|new
name|ActionListener
argument_list|<
name|TransportNodesSnapshotsStatus
operator|.
name|NodesSnapshotStatus
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onResponse
parameter_list|(
name|TransportNodesSnapshotsStatus
operator|.
name|NodesSnapshotStatus
name|nodeSnapshotStatuses
parameter_list|)
block|{
try|try
block|{
name|List
argument_list|<
name|SnapshotsInProgress
operator|.
name|Entry
argument_list|>
name|currentSnapshots
init|=
name|snapshotsService
operator|.
name|currentSnapshots
argument_list|(
name|request
operator|.
name|repository
argument_list|()
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|request
operator|.
name|snapshots
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|listener
operator|.
name|onResponse
argument_list|(
name|buildResponse
argument_list|(
name|request
argument_list|,
name|currentSnapshots
argument_list|,
name|nodeSnapshotStatuses
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
name|listener
operator|.
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
name|Exception
name|e
parameter_list|)
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
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// We don't have any in-progress shards, just return current stats
name|listener
operator|.
name|onResponse
argument_list|(
name|buildResponse
argument_list|(
name|request
argument_list|,
name|currentSnapshots
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|buildResponse
specifier|private
name|SnapshotsStatusResponse
name|buildResponse
parameter_list|(
name|SnapshotsStatusRequest
name|request
parameter_list|,
name|List
argument_list|<
name|SnapshotsInProgress
operator|.
name|Entry
argument_list|>
name|currentSnapshotEntries
parameter_list|,
name|TransportNodesSnapshotsStatus
operator|.
name|NodesSnapshotStatus
name|nodeSnapshotStatuses
parameter_list|)
throws|throws
name|IOException
block|{
comment|// First process snapshot that are currently processed
name|List
argument_list|<
name|SnapshotStatus
argument_list|>
name|builder
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|currentSnapshotNames
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|currentSnapshotEntries
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|TransportNodesSnapshotsStatus
operator|.
name|NodeSnapshotStatus
argument_list|>
name|nodeSnapshotStatusMap
decl_stmt|;
if|if
condition|(
name|nodeSnapshotStatuses
operator|!=
literal|null
condition|)
block|{
name|nodeSnapshotStatusMap
operator|=
name|nodeSnapshotStatuses
operator|.
name|getNodesMap
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|nodeSnapshotStatusMap
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
expr_stmt|;
block|}
for|for
control|(
name|SnapshotsInProgress
operator|.
name|Entry
name|entry
range|:
name|currentSnapshotEntries
control|)
block|{
name|currentSnapshotNames
operator|.
name|add
argument_list|(
name|entry
operator|.
name|snapshot
argument_list|()
operator|.
name|getSnapshotId
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|SnapshotIndexShardStatus
argument_list|>
name|shardStatusBuilder
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|ObjectObjectCursor
argument_list|<
name|ShardId
argument_list|,
name|SnapshotsInProgress
operator|.
name|ShardSnapshotStatus
argument_list|>
name|shardEntry
range|:
name|entry
operator|.
name|shards
argument_list|()
control|)
block|{
name|SnapshotsInProgress
operator|.
name|ShardSnapshotStatus
name|status
init|=
name|shardEntry
operator|.
name|value
decl_stmt|;
if|if
condition|(
name|status
operator|.
name|nodeId
argument_list|()
operator|!=
literal|null
condition|)
block|{
comment|// We should have information about this shard from the shard:
name|TransportNodesSnapshotsStatus
operator|.
name|NodeSnapshotStatus
name|nodeStatus
init|=
name|nodeSnapshotStatusMap
operator|.
name|get
argument_list|(
name|status
operator|.
name|nodeId
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|nodeStatus
operator|!=
literal|null
condition|)
block|{
name|Map
argument_list|<
name|ShardId
argument_list|,
name|SnapshotIndexShardStatus
argument_list|>
name|shardStatues
init|=
name|nodeStatus
operator|.
name|status
argument_list|()
operator|.
name|get
argument_list|(
name|entry
operator|.
name|snapshot
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|shardStatues
operator|!=
literal|null
condition|)
block|{
name|SnapshotIndexShardStatus
name|shardStatus
init|=
name|shardStatues
operator|.
name|get
argument_list|(
name|shardEntry
operator|.
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|shardStatus
operator|!=
literal|null
condition|)
block|{
comment|// We have full information about this shard
name|shardStatusBuilder
operator|.
name|add
argument_list|(
name|shardStatus
argument_list|)
expr_stmt|;
continue|continue;
block|}
block|}
block|}
block|}
specifier|final
name|SnapshotIndexShardStage
name|stage
decl_stmt|;
switch|switch
condition|(
name|shardEntry
operator|.
name|value
operator|.
name|state
argument_list|()
condition|)
block|{
case|case
name|FAILED
case|:
case|case
name|ABORTED
case|:
case|case
name|MISSING
case|:
name|stage
operator|=
name|SnapshotIndexShardStage
operator|.
name|FAILURE
expr_stmt|;
break|break;
case|case
name|INIT
case|:
case|case
name|WAITING
case|:
case|case
name|STARTED
case|:
name|stage
operator|=
name|SnapshotIndexShardStage
operator|.
name|STARTED
expr_stmt|;
break|break;
case|case
name|SUCCESS
case|:
name|stage
operator|=
name|SnapshotIndexShardStage
operator|.
name|DONE
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Unknown snapshot state "
operator|+
name|shardEntry
operator|.
name|value
operator|.
name|state
argument_list|()
argument_list|)
throw|;
block|}
name|SnapshotIndexShardStatus
name|shardStatus
init|=
operator|new
name|SnapshotIndexShardStatus
argument_list|(
name|shardEntry
operator|.
name|key
argument_list|,
name|stage
argument_list|)
decl_stmt|;
name|shardStatusBuilder
operator|.
name|add
argument_list|(
name|shardStatus
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|add
argument_list|(
operator|new
name|SnapshotStatus
argument_list|(
name|entry
operator|.
name|snapshot
argument_list|()
argument_list|,
name|entry
operator|.
name|state
argument_list|()
argument_list|,
name|Collections
operator|.
name|unmodifiableList
argument_list|(
name|shardStatusBuilder
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Now add snapshots on disk that are not currently running
specifier|final
name|String
name|repositoryName
init|=
name|request
operator|.
name|repository
argument_list|()
decl_stmt|;
if|if
condition|(
name|Strings
operator|.
name|hasText
argument_list|(
name|repositoryName
argument_list|)
operator|&&
name|request
operator|.
name|snapshots
argument_list|()
operator|!=
literal|null
operator|&&
name|request
operator|.
name|snapshots
argument_list|()
operator|.
name|length
operator|>
literal|0
condition|)
block|{
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|requestedSnapshotNames
init|=
name|Sets
operator|.
name|newHashSet
argument_list|(
name|request
operator|.
name|snapshots
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|SnapshotId
argument_list|>
name|matchedSnapshotIds
init|=
name|snapshotsService
operator|.
name|snapshotIds
argument_list|(
name|repositoryName
argument_list|)
operator|.
name|stream
argument_list|()
operator|.
name|filter
argument_list|(
name|s
lambda|->
name|requestedSnapshotNames
operator|.
name|contains
argument_list|(
name|s
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toMap
argument_list|(
name|SnapshotId
operator|::
name|getName
argument_list|,
name|Function
operator|.
name|identity
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
for|for
control|(
specifier|final
name|String
name|snapshotName
range|:
name|request
operator|.
name|snapshots
argument_list|()
control|)
block|{
if|if
condition|(
name|currentSnapshotNames
operator|.
name|contains
argument_list|(
name|snapshotName
argument_list|)
condition|)
block|{
comment|// we've already found this snapshot in the current snapshot entries, so skip over
continue|continue;
block|}
name|SnapshotId
name|snapshotId
init|=
name|matchedSnapshotIds
operator|.
name|get
argument_list|(
name|snapshotName
argument_list|)
decl_stmt|;
if|if
condition|(
name|snapshotId
operator|==
literal|null
condition|)
block|{
comment|// neither in the current snapshot entries nor found in the repository
if|if
condition|(
name|request
operator|.
name|ignoreUnavailable
argument_list|()
condition|)
block|{
comment|// ignoring unavailable snapshots, so skip over
name|logger
operator|.
name|debug
argument_list|(
literal|"snapshot status request ignoring snapshot [{}], not found in repository [{}]"
argument_list|,
name|snapshotName
argument_list|,
name|repositoryName
argument_list|)
expr_stmt|;
continue|continue;
block|}
else|else
block|{
throw|throw
operator|new
name|SnapshotMissingException
argument_list|(
name|repositoryName
argument_list|,
name|snapshotName
argument_list|)
throw|;
block|}
block|}
name|SnapshotInfo
name|snapshotInfo
init|=
name|snapshotsService
operator|.
name|snapshot
argument_list|(
name|repositoryName
argument_list|,
name|snapshotId
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|SnapshotIndexShardStatus
argument_list|>
name|shardStatusBuilder
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
if|if
condition|(
name|snapshotInfo
operator|.
name|state
argument_list|()
operator|.
name|completed
argument_list|()
condition|)
block|{
name|Map
argument_list|<
name|ShardId
argument_list|,
name|IndexShardSnapshotStatus
argument_list|>
name|shardStatues
init|=
name|snapshotsService
operator|.
name|snapshotShards
argument_list|(
name|request
operator|.
name|repository
argument_list|()
argument_list|,
name|snapshotInfo
argument_list|)
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|ShardId
argument_list|,
name|IndexShardSnapshotStatus
argument_list|>
name|shardStatus
range|:
name|shardStatues
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|shardStatusBuilder
operator|.
name|add
argument_list|(
operator|new
name|SnapshotIndexShardStatus
argument_list|(
name|shardStatus
operator|.
name|getKey
argument_list|()
argument_list|,
name|shardStatus
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|final
name|SnapshotsInProgress
operator|.
name|State
name|state
decl_stmt|;
switch|switch
condition|(
name|snapshotInfo
operator|.
name|state
argument_list|()
condition|)
block|{
case|case
name|FAILED
case|:
name|state
operator|=
name|SnapshotsInProgress
operator|.
name|State
operator|.
name|FAILED
expr_stmt|;
break|break;
case|case
name|SUCCESS
case|:
case|case
name|PARTIAL
case|:
comment|// Translating both PARTIAL and SUCCESS to SUCCESS for now
comment|// TODO: add the differentiation on the metadata level in the next major release
name|state
operator|=
name|SnapshotsInProgress
operator|.
name|State
operator|.
name|SUCCESS
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Unknown snapshot state "
operator|+
name|snapshotInfo
operator|.
name|state
argument_list|()
argument_list|)
throw|;
block|}
name|builder
operator|.
name|add
argument_list|(
operator|new
name|SnapshotStatus
argument_list|(
operator|new
name|Snapshot
argument_list|(
name|repositoryName
argument_list|,
name|snapshotInfo
operator|.
name|snapshotId
argument_list|()
argument_list|)
argument_list|,
name|state
argument_list|,
name|Collections
operator|.
name|unmodifiableList
argument_list|(
name|shardStatusBuilder
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
operator|new
name|SnapshotsStatusResponse
argument_list|(
name|Collections
operator|.
name|unmodifiableList
argument_list|(
name|builder
argument_list|)
argument_list|)
return|;
block|}
block|}
end_class

end_unit

