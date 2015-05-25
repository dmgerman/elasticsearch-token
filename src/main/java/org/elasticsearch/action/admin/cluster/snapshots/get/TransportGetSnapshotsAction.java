begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.cluster.snapshots.get
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
name|get
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
name|SnapshotId
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

begin_comment
comment|/**  * Transport Action for get snapshots operation  */
end_comment

begin_class
DECL|class|TransportGetSnapshotsAction
specifier|public
class|class
name|TransportGetSnapshotsAction
extends|extends
name|TransportMasterNodeOperationAction
argument_list|<
name|GetSnapshotsRequest
argument_list|,
name|GetSnapshotsResponse
argument_list|>
block|{
DECL|field|snapshotsService
specifier|private
specifier|final
name|SnapshotsService
name|snapshotsService
decl_stmt|;
annotation|@
name|Inject
DECL|method|TransportGetSnapshotsAction
specifier|public
name|TransportGetSnapshotsAction
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
name|ActionFilters
name|actionFilters
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|GetSnapshotsAction
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
name|GetSnapshotsRequest
operator|.
name|class
argument_list|)
expr_stmt|;
name|this
operator|.
name|snapshotsService
operator|=
name|snapshotsService
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
DECL|method|newResponse
specifier|protected
name|GetSnapshotsResponse
name|newResponse
parameter_list|()
block|{
return|return
operator|new
name|GetSnapshotsResponse
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|checkBlock
specifier|protected
name|ClusterBlockException
name|checkBlock
parameter_list|(
name|GetSnapshotsRequest
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
name|indexBlockedException
argument_list|(
name|ClusterBlockLevel
operator|.
name|METADATA_READ
argument_list|,
literal|""
argument_list|)
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
name|GetSnapshotsRequest
name|request
parameter_list|,
name|ClusterState
name|state
parameter_list|,
specifier|final
name|ActionListener
argument_list|<
name|GetSnapshotsResponse
argument_list|>
name|listener
parameter_list|)
block|{
try|try
block|{
name|ImmutableList
operator|.
name|Builder
argument_list|<
name|SnapshotInfo
argument_list|>
name|snapshotInfoBuilder
init|=
name|ImmutableList
operator|.
name|builder
argument_list|()
decl_stmt|;
if|if
condition|(
name|isAllSnapshots
argument_list|(
name|request
operator|.
name|snapshots
argument_list|()
argument_list|)
condition|)
block|{
name|ImmutableList
argument_list|<
name|Snapshot
argument_list|>
name|snapshots
init|=
name|snapshotsService
operator|.
name|snapshots
argument_list|(
name|request
operator|.
name|repository
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|Snapshot
name|snapshot
range|:
name|snapshots
control|)
block|{
name|snapshotInfoBuilder
operator|.
name|add
argument_list|(
operator|new
name|SnapshotInfo
argument_list|(
name|snapshot
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|isCurrentSnapshots
argument_list|(
name|request
operator|.
name|snapshots
argument_list|()
argument_list|)
condition|)
block|{
name|ImmutableList
argument_list|<
name|Snapshot
argument_list|>
name|snapshots
init|=
name|snapshotsService
operator|.
name|currentSnapshots
argument_list|(
name|request
operator|.
name|repository
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|Snapshot
name|snapshot
range|:
name|snapshots
control|)
block|{
name|snapshotInfoBuilder
operator|.
name|add
argument_list|(
operator|new
name|SnapshotInfo
argument_list|(
name|snapshot
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|request
operator|.
name|snapshots
argument_list|()
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|SnapshotId
name|snapshotId
init|=
operator|new
name|SnapshotId
argument_list|(
name|request
operator|.
name|repository
argument_list|()
argument_list|,
name|request
operator|.
name|snapshots
argument_list|()
index|[
name|i
index|]
argument_list|)
decl_stmt|;
name|snapshotInfoBuilder
operator|.
name|add
argument_list|(
operator|new
name|SnapshotInfo
argument_list|(
name|snapshotsService
operator|.
name|snapshot
argument_list|(
name|snapshotId
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|listener
operator|.
name|onResponse
argument_list|(
operator|new
name|GetSnapshotsResponse
argument_list|(
name|snapshotInfoBuilder
operator|.
name|build
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
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
block|}
DECL|method|isAllSnapshots
specifier|private
name|boolean
name|isAllSnapshots
parameter_list|(
name|String
index|[]
name|snapshots
parameter_list|)
block|{
return|return
operator|(
name|snapshots
operator|.
name|length
operator|==
literal|0
operator|)
operator|||
operator|(
name|snapshots
operator|.
name|length
operator|==
literal|1
operator|&&
name|GetSnapshotsRequest
operator|.
name|ALL_SNAPSHOTS
operator|.
name|equalsIgnoreCase
argument_list|(
name|snapshots
index|[
literal|0
index|]
argument_list|)
operator|)
return|;
block|}
DECL|method|isCurrentSnapshots
specifier|private
name|boolean
name|isCurrentSnapshots
parameter_list|(
name|String
index|[]
name|snapshots
parameter_list|)
block|{
return|return
operator|(
name|snapshots
operator|.
name|length
operator|==
literal|1
operator|&&
name|GetSnapshotsRequest
operator|.
name|CURRENT_SNAPSHOT
operator|.
name|equalsIgnoreCase
argument_list|(
name|snapshots
index|[
literal|0
index|]
argument_list|)
operator|)
return|;
block|}
block|}
end_class

end_unit

