begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.cluster.snapshots.create
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
name|create
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchException
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
comment|/**  * Transport action for create snapshot operation  */
end_comment

begin_class
DECL|class|TransportCreateSnapshotAction
specifier|public
class|class
name|TransportCreateSnapshotAction
extends|extends
name|TransportMasterNodeOperationAction
argument_list|<
name|CreateSnapshotRequest
argument_list|,
name|CreateSnapshotResponse
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
DECL|method|TransportCreateSnapshotAction
specifier|public
name|TransportCreateSnapshotAction
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
name|CreateSnapshotAction
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
name|SNAPSHOT
return|;
block|}
annotation|@
name|Override
DECL|method|newRequest
specifier|protected
name|CreateSnapshotRequest
name|newRequest
parameter_list|()
block|{
return|return
operator|new
name|CreateSnapshotRequest
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|newResponse
specifier|protected
name|CreateSnapshotResponse
name|newResponse
parameter_list|()
block|{
return|return
operator|new
name|CreateSnapshotResponse
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
name|CreateSnapshotRequest
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
name|METADATA
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
name|CreateSnapshotRequest
name|request
parameter_list|,
name|ClusterState
name|state
parameter_list|,
specifier|final
name|ActionListener
argument_list|<
name|CreateSnapshotResponse
argument_list|>
name|listener
parameter_list|)
throws|throws
name|ElasticsearchException
block|{
name|SnapshotsService
operator|.
name|SnapshotRequest
name|snapshotRequest
init|=
operator|new
name|SnapshotsService
operator|.
name|SnapshotRequest
argument_list|(
literal|"create_snapshot["
operator|+
name|request
operator|.
name|snapshot
argument_list|()
operator|+
literal|"]"
argument_list|,
name|request
operator|.
name|snapshot
argument_list|()
argument_list|,
name|request
operator|.
name|repository
argument_list|()
argument_list|)
operator|.
name|indices
argument_list|(
name|request
operator|.
name|indices
argument_list|()
argument_list|)
operator|.
name|indicesOptions
argument_list|(
name|request
operator|.
name|indicesOptions
argument_list|()
argument_list|)
operator|.
name|partial
argument_list|(
name|request
operator|.
name|partial
argument_list|()
argument_list|)
operator|.
name|settings
argument_list|(
name|request
operator|.
name|settings
argument_list|()
argument_list|)
operator|.
name|includeGlobalState
argument_list|(
name|request
operator|.
name|includeGlobalState
argument_list|()
argument_list|)
operator|.
name|masterNodeTimeout
argument_list|(
name|request
operator|.
name|masterNodeTimeout
argument_list|()
argument_list|)
decl_stmt|;
name|snapshotsService
operator|.
name|createSnapshot
argument_list|(
name|snapshotRequest
argument_list|,
operator|new
name|SnapshotsService
operator|.
name|CreateSnapshotListener
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onResponse
parameter_list|()
block|{
if|if
condition|(
name|request
operator|.
name|waitForCompletion
argument_list|()
condition|)
block|{
name|snapshotsService
operator|.
name|addListener
argument_list|(
operator|new
name|SnapshotsService
operator|.
name|SnapshotCompletionListener
argument_list|()
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
name|snapshot
argument_list|()
argument_list|)
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|onSnapshotCompletion
parameter_list|(
name|SnapshotId
name|snapshotId
parameter_list|,
name|SnapshotInfo
name|snapshot
parameter_list|)
block|{
if|if
condition|(
name|this
operator|.
name|snapshotId
operator|.
name|equals
argument_list|(
name|snapshotId
argument_list|)
condition|)
block|{
name|listener
operator|.
name|onResponse
argument_list|(
operator|new
name|CreateSnapshotResponse
argument_list|(
name|snapshot
argument_list|)
argument_list|)
expr_stmt|;
name|snapshotsService
operator|.
name|removeListener
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|onSnapshotFailure
parameter_list|(
name|SnapshotId
name|snapshotId
parameter_list|,
name|Throwable
name|t
parameter_list|)
block|{
if|if
condition|(
name|this
operator|.
name|snapshotId
operator|.
name|equals
argument_list|(
name|snapshotId
argument_list|)
condition|)
block|{
name|listener
operator|.
name|onFailure
argument_list|(
name|t
argument_list|)
expr_stmt|;
name|snapshotsService
operator|.
name|removeListener
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|listener
operator|.
name|onResponse
argument_list|(
operator|new
name|CreateSnapshotResponse
argument_list|()
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
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

