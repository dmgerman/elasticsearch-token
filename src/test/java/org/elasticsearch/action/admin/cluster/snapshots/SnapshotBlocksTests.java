begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.cluster.snapshots
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
name|admin
operator|.
name|cluster
operator|.
name|repositories
operator|.
name|verify
operator|.
name|VerifyRepositoryResponse
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
name|snapshots
operator|.
name|create
operator|.
name|CreateSnapshotResponse
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
name|snapshots
operator|.
name|delete
operator|.
name|DeleteSnapshotResponse
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
name|snapshots
operator|.
name|get
operator|.
name|GetSnapshotsResponse
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
name|snapshots
operator|.
name|restore
operator|.
name|RestoreSnapshotResponse
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
name|snapshots
operator|.
name|status
operator|.
name|SnapshotsStatusResponse
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
name|common
operator|.
name|settings
operator|.
name|ImmutableSettings
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|RestStatus
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|ElasticsearchIntegrationTest
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|ElasticsearchIntegrationTest
operator|.
name|ClusterScope
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|hamcrest
operator|.
name|ElasticsearchAssertions
operator|.
name|assertAcked
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|hamcrest
operator|.
name|ElasticsearchAssertions
operator|.
name|assertBlocked
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|equalTo
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|hasSize
import|;
end_import

begin_comment
comment|/**  * This class tests that snapshot operations (Create, Delete, Restore) are blocked when the cluster is read-only.  *  * The @ClusterScope TEST is needed because this class updates the cluster setting "cluster.blocks.read_only".  */
end_comment

begin_class
annotation|@
name|ClusterScope
argument_list|(
name|scope
operator|=
name|ElasticsearchIntegrationTest
operator|.
name|Scope
operator|.
name|TEST
argument_list|)
DECL|class|SnapshotBlocksTests
specifier|public
class|class
name|SnapshotBlocksTests
extends|extends
name|ElasticsearchIntegrationTest
block|{
DECL|field|INDEX_NAME
specifier|protected
specifier|static
specifier|final
name|String
name|INDEX_NAME
init|=
literal|"test-blocks"
decl_stmt|;
DECL|field|REPOSITORY_NAME
specifier|protected
specifier|static
specifier|final
name|String
name|REPOSITORY_NAME
init|=
literal|"repo-"
operator|+
name|INDEX_NAME
decl_stmt|;
DECL|field|SNAPSHOT_NAME
specifier|protected
specifier|static
specifier|final
name|String
name|SNAPSHOT_NAME
init|=
literal|"snapshot-0"
decl_stmt|;
annotation|@
name|Before
DECL|method|setUpRepository
specifier|protected
name|void
name|setUpRepository
parameter_list|()
throws|throws
name|Exception
block|{
name|createIndex
argument_list|(
name|INDEX_NAME
argument_list|)
expr_stmt|;
name|int
name|docs
init|=
name|between
argument_list|(
literal|10
argument_list|,
literal|100
argument_list|)
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
name|docs
condition|;
name|i
operator|++
control|)
block|{
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
name|INDEX_NAME
argument_list|,
literal|"type"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"test"
argument_list|,
literal|"init"
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
block|}
name|logger
operator|.
name|info
argument_list|(
literal|"--> register a repository"
argument_list|)
expr_stmt|;
name|assertAcked
argument_list|(
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|preparePutRepository
argument_list|(
name|REPOSITORY_NAME
argument_list|)
operator|.
name|setType
argument_list|(
literal|"fs"
argument_list|)
operator|.
name|setSettings
argument_list|(
name|ImmutableSettings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"location"
argument_list|,
name|randomRepoPath
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> verify the repository"
argument_list|)
expr_stmt|;
name|VerifyRepositoryResponse
name|verifyResponse
init|=
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareVerifyRepository
argument_list|(
name|REPOSITORY_NAME
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|verifyResponse
operator|.
name|getNodes
argument_list|()
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
name|cluster
argument_list|()
operator|.
name|numDataAndMasterNodes
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> create a snapshot"
argument_list|)
expr_stmt|;
name|CreateSnapshotResponse
name|snapshotResponse
init|=
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareCreateSnapshot
argument_list|(
name|REPOSITORY_NAME
argument_list|,
name|SNAPSHOT_NAME
argument_list|)
operator|.
name|setIncludeGlobalState
argument_list|(
literal|true
argument_list|)
operator|.
name|setWaitForCompletion
argument_list|(
literal|true
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|snapshotResponse
operator|.
name|status
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|RestStatus
operator|.
name|OK
argument_list|)
argument_list|)
expr_stmt|;
name|ensureSearchable
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testCreateSnapshotWithBlocks
specifier|public
name|void
name|testCreateSnapshotWithBlocks
parameter_list|()
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"-->  creating a snapshot is blocked when the cluster is read only"
argument_list|)
expr_stmt|;
try|try
block|{
name|setClusterReadOnly
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|assertBlocked
argument_list|(
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareCreateSnapshot
argument_list|(
name|REPOSITORY_NAME
argument_list|,
literal|"snapshot-1"
argument_list|)
argument_list|,
name|MetaData
operator|.
name|CLUSTER_READ_ONLY_BLOCK
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|setClusterReadOnly
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
name|logger
operator|.
name|info
argument_list|(
literal|"-->  creating a snapshot is allowed when the cluster is not read only"
argument_list|)
expr_stmt|;
name|CreateSnapshotResponse
name|response
init|=
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareCreateSnapshot
argument_list|(
name|REPOSITORY_NAME
argument_list|,
literal|"snapshot-1"
argument_list|)
operator|.
name|setWaitForCompletion
argument_list|(
literal|true
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|status
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|RestStatus
operator|.
name|OK
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testDeleteSnapshotWithBlocks
specifier|public
name|void
name|testDeleteSnapshotWithBlocks
parameter_list|()
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"-->  deleting a snapshot is blocked when the cluster is read only"
argument_list|)
expr_stmt|;
try|try
block|{
name|setClusterReadOnly
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|assertBlocked
argument_list|(
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareDeleteSnapshot
argument_list|(
name|REPOSITORY_NAME
argument_list|,
name|SNAPSHOT_NAME
argument_list|)
argument_list|,
name|MetaData
operator|.
name|CLUSTER_READ_ONLY_BLOCK
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|setClusterReadOnly
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
name|logger
operator|.
name|info
argument_list|(
literal|"-->  deleting a snapshot is allowed when the cluster is not read only"
argument_list|)
expr_stmt|;
name|DeleteSnapshotResponse
name|response
init|=
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareDeleteSnapshot
argument_list|(
name|REPOSITORY_NAME
argument_list|,
name|SNAPSHOT_NAME
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|isAcknowledged
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testRestoreSnapshotWithBlocks
specifier|public
name|void
name|testRestoreSnapshotWithBlocks
parameter_list|()
block|{
name|assertAcked
argument_list|(
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareDelete
argument_list|(
name|INDEX_NAME
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareExists
argument_list|(
name|INDEX_NAME
argument_list|)
operator|.
name|get
argument_list|()
operator|.
name|isExists
argument_list|()
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"-->  restoring a snapshot is blocked when the cluster is read only"
argument_list|)
expr_stmt|;
try|try
block|{
name|setClusterReadOnly
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|assertBlocked
argument_list|(
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareRestoreSnapshot
argument_list|(
name|REPOSITORY_NAME
argument_list|,
name|SNAPSHOT_NAME
argument_list|)
argument_list|,
name|MetaData
operator|.
name|CLUSTER_READ_ONLY_BLOCK
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|setClusterReadOnly
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
name|logger
operator|.
name|info
argument_list|(
literal|"-->  creating a snapshot is allowed when the cluster is not read only"
argument_list|)
expr_stmt|;
name|RestoreSnapshotResponse
name|response
init|=
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareRestoreSnapshot
argument_list|(
name|REPOSITORY_NAME
argument_list|,
name|SNAPSHOT_NAME
argument_list|)
operator|.
name|setWaitForCompletion
argument_list|(
literal|true
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|status
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|RestStatus
operator|.
name|OK
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareExists
argument_list|(
name|INDEX_NAME
argument_list|)
operator|.
name|get
argument_list|()
operator|.
name|isExists
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testGetSnapshotWithBlocks
specifier|public
name|void
name|testGetSnapshotWithBlocks
parameter_list|()
block|{
comment|// This test checks that the Get Snapshot operation is never blocked, even if the cluster is read only.
try|try
block|{
name|setClusterReadOnly
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|GetSnapshotsResponse
name|response
init|=
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareGetSnapshots
argument_list|(
name|REPOSITORY_NAME
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getSnapshots
argument_list|()
argument_list|,
name|hasSize
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getSnapshots
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|name
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|SNAPSHOT_NAME
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|setClusterReadOnly
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
DECL|method|testSnapshotStatusWithBlocks
specifier|public
name|void
name|testSnapshotStatusWithBlocks
parameter_list|()
block|{
comment|// This test checks that the Snapshot Status operation is never blocked, even if the cluster is read only.
try|try
block|{
name|setClusterReadOnly
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|SnapshotsStatusResponse
name|response
init|=
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareSnapshotStatus
argument_list|(
name|REPOSITORY_NAME
argument_list|)
operator|.
name|setSnapshots
argument_list|(
name|SNAPSHOT_NAME
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getSnapshots
argument_list|()
argument_list|,
name|hasSize
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getSnapshots
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getState
argument_list|()
operator|.
name|completed
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|setClusterReadOnly
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

