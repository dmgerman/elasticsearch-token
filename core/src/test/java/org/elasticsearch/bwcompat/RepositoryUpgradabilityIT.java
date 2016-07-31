begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.bwcompat
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|bwcompat
package|;
end_package

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
name|FileTestUtils
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
name|snapshots
operator|.
name|AbstractSnapshotIntegTestCase
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
name|test
operator|.
name|ESIntegTestCase
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
name|junit
operator|.
name|annotations
operator|.
name|TestLogging
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|file
operator|.
name|DirectoryStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|file
operator|.
name|Files
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|file
operator|.
name|Path
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
name|List
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
name|greaterThan
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
name|greaterThanOrEqualTo
import|;
end_import

begin_comment
comment|/**  * Tests that a repository can handle both snapshots of previous version formats and new version formats,  * as blob names and repository blob formats have changed between the snapshot versions.  */
end_comment

begin_class
annotation|@
name|ESIntegTestCase
operator|.
name|ClusterScope
argument_list|(
name|scope
operator|=
name|ESIntegTestCase
operator|.
name|Scope
operator|.
name|TEST
argument_list|)
comment|// this test sometimes fails in recovery when the recovery is reset, increasing the logging level to help debug
annotation|@
name|TestLogging
argument_list|(
literal|"indices.recovery:DEBUG"
argument_list|)
DECL|class|RepositoryUpgradabilityIT
specifier|public
class|class
name|RepositoryUpgradabilityIT
extends|extends
name|AbstractSnapshotIntegTestCase
block|{
comment|/**      * This tests that a repository can inter-operate with snapshots that both have and don't have a UUID,      * namely when a repository was created in an older version with snapshots created in the old format      * (only snapshot name, no UUID) and then the repository is loaded into newer versions where subsequent      * snapshots have a name and a UUID.      */
DECL|method|testRepositoryWorksWithCrossVersions
specifier|public
name|void
name|testRepositoryWorksWithCrossVersions
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|repoVersions
init|=
name|listRepoVersions
argument_list|()
decl_stmt|;
comment|// run the test for each supported version
for|for
control|(
specifier|final
name|String
name|version
range|:
name|repoVersions
control|)
block|{
specifier|final
name|String
name|repoName
init|=
literal|"test-repo-"
operator|+
name|version
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"-->  creating repository [{}] for version [{}]"
argument_list|,
name|repoName
argument_list|,
name|version
argument_list|)
expr_stmt|;
name|createRepository
argument_list|(
name|version
argument_list|,
name|repoName
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> get the snapshots"
argument_list|)
expr_stmt|;
specifier|final
name|String
name|originalIndex
init|=
literal|"index-"
operator|+
name|version
decl_stmt|;
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|indices
init|=
name|Sets
operator|.
name|newHashSet
argument_list|(
name|originalIndex
argument_list|)
decl_stmt|;
specifier|final
name|Set
argument_list|<
name|SnapshotInfo
argument_list|>
name|snapshotInfos
init|=
name|Sets
operator|.
name|newHashSet
argument_list|(
name|getSnapshots
argument_list|(
name|repoName
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|snapshotInfos
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|SnapshotInfo
name|originalSnapshot
init|=
name|snapshotInfos
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|originalSnapshot
operator|.
name|snapshotId
argument_list|()
argument_list|,
name|equalTo
argument_list|(
operator|new
name|SnapshotId
argument_list|(
literal|"test_1"
argument_list|,
literal|"test_1"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|Sets
operator|.
name|newHashSet
argument_list|(
name|originalSnapshot
operator|.
name|indices
argument_list|()
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|indices
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> restore the original snapshot"
argument_list|)
expr_stmt|;
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|restoredIndices
init|=
name|Sets
operator|.
name|newHashSet
argument_list|(
name|restoreSnapshot
argument_list|(
name|repoName
argument_list|,
name|originalSnapshot
operator|.
name|snapshotId
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|restoredIndices
argument_list|,
name|equalTo
argument_list|(
name|indices
argument_list|)
argument_list|)
expr_stmt|;
comment|// make sure it has documents
for|for
control|(
specifier|final
name|String
name|searchIdx
range|:
name|restoredIndices
control|)
block|{
name|assertThat
argument_list|(
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
name|searchIdx
argument_list|)
operator|.
name|setSize
argument_list|(
literal|0
argument_list|)
operator|.
name|get
argument_list|()
operator|.
name|getHits
argument_list|()
operator|.
name|totalHits
argument_list|()
argument_list|,
name|greaterThan
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|deleteIndices
argument_list|(
name|restoredIndices
argument_list|)
expr_stmt|;
comment|// delete so we can restore again later
specifier|final
name|String
name|snapshotName2
init|=
literal|"test_2"
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> take a new snapshot of the old index"
argument_list|)
expr_stmt|;
specifier|final
name|int
name|addedDocSize
init|=
literal|10
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
name|addedDocSize
condition|;
name|i
operator|++
control|)
block|{
name|index
argument_list|(
name|originalIndex
argument_list|,
literal|"doc"
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|i
argument_list|)
argument_list|,
literal|"foo"
argument_list|,
literal|"new-bar-"
operator|+
name|i
argument_list|)
expr_stmt|;
block|}
name|refresh
argument_list|()
expr_stmt|;
name|snapshotInfos
operator|.
name|add
argument_list|(
name|createSnapshot
argument_list|(
name|repoName
argument_list|,
name|snapshotName2
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> get the snapshots with the newly created snapshot [{}]"
argument_list|,
name|snapshotName2
argument_list|)
expr_stmt|;
name|Set
argument_list|<
name|SnapshotInfo
argument_list|>
name|snapshotInfosFromRepo
init|=
name|Sets
operator|.
name|newHashSet
argument_list|(
name|getSnapshots
argument_list|(
name|repoName
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|snapshotInfosFromRepo
argument_list|,
name|equalTo
argument_list|(
name|snapshotInfos
argument_list|)
argument_list|)
expr_stmt|;
name|snapshotInfosFromRepo
operator|.
name|forEach
argument_list|(
name|snapshotInfo
lambda|->
block|{
name|assertThat
argument_list|(
name|Sets
operator|.
name|newHashSet
argument_list|(
name|snapshotInfo
operator|.
name|indices
argument_list|()
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|indices
argument_list|)
argument_list|)
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
specifier|final
name|String
name|snapshotName3
init|=
literal|"test_3"
decl_stmt|;
specifier|final
name|String
name|indexName2
init|=
literal|"index2"
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> take a new snapshot with a new index"
argument_list|)
expr_stmt|;
name|createIndex
argument_list|(
name|indexName2
argument_list|)
expr_stmt|;
name|indices
operator|.
name|add
argument_list|(
name|indexName2
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|addedDocSize
condition|;
name|i
operator|++
control|)
block|{
name|index
argument_list|(
name|indexName2
argument_list|,
literal|"doc"
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|i
argument_list|)
argument_list|,
literal|"foo"
argument_list|,
literal|"new-bar-"
operator|+
name|i
argument_list|)
expr_stmt|;
block|}
name|refresh
argument_list|()
expr_stmt|;
name|snapshotInfos
operator|.
name|add
argument_list|(
name|createSnapshot
argument_list|(
name|repoName
argument_list|,
name|snapshotName3
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> get the snapshots with the newly created snapshot [{}]"
argument_list|,
name|snapshotName3
argument_list|)
expr_stmt|;
name|snapshotInfosFromRepo
operator|=
name|Sets
operator|.
name|newHashSet
argument_list|(
name|getSnapshots
argument_list|(
name|repoName
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|snapshotInfosFromRepo
argument_list|,
name|equalTo
argument_list|(
name|snapshotInfos
argument_list|)
argument_list|)
expr_stmt|;
name|snapshotInfosFromRepo
operator|.
name|forEach
argument_list|(
name|snapshotInfo
lambda|->
block|{
if|if
condition|(
name|snapshotInfo
operator|.
name|snapshotId
argument_list|()
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
name|snapshotName3
argument_list|)
condition|)
block|{
comment|// only the last snapshot has all the indices
name|assertThat
argument_list|(
name|Sets
operator|.
name|newHashSet
argument_list|(
name|snapshotInfo
operator|.
name|indices
argument_list|()
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|indices
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertThat
argument_list|(
name|Sets
operator|.
name|newHashSet
argument_list|(
name|snapshotInfo
operator|.
name|indices
argument_list|()
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|Sets
operator|.
name|newHashSet
argument_list|(
name|originalIndex
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
name|deleteIndices
argument_list|(
name|indices
argument_list|)
expr_stmt|;
comment|// clean up indices
name|logger
operator|.
name|info
argument_list|(
literal|"--> restore the old snapshot again"
argument_list|)
expr_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|oldRestoredIndices
init|=
name|Sets
operator|.
name|newHashSet
argument_list|(
name|restoreSnapshot
argument_list|(
name|repoName
argument_list|,
name|originalSnapshot
operator|.
name|snapshotId
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|oldRestoredIndices
argument_list|,
name|equalTo
argument_list|(
name|Sets
operator|.
name|newHashSet
argument_list|(
name|originalIndex
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
specifier|final
name|String
name|searchIdx
range|:
name|oldRestoredIndices
control|)
block|{
name|assertThat
argument_list|(
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
name|searchIdx
argument_list|)
operator|.
name|setSize
argument_list|(
literal|0
argument_list|)
operator|.
name|get
argument_list|()
operator|.
name|getHits
argument_list|()
operator|.
name|totalHits
argument_list|()
argument_list|,
name|greaterThanOrEqualTo
argument_list|(
operator|(
name|long
operator|)
name|addedDocSize
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|deleteIndices
argument_list|(
name|oldRestoredIndices
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> restore the new snapshot"
argument_list|)
expr_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|newSnapshotIndices
init|=
name|Sets
operator|.
name|newHashSet
argument_list|(
name|restoreSnapshot
argument_list|(
name|repoName
argument_list|,
name|snapshotName3
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|newSnapshotIndices
argument_list|,
name|equalTo
argument_list|(
name|Sets
operator|.
name|newHashSet
argument_list|(
name|originalIndex
argument_list|,
name|indexName2
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
specifier|final
name|String
name|searchIdx
range|:
name|newSnapshotIndices
control|)
block|{
name|assertThat
argument_list|(
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
name|searchIdx
argument_list|)
operator|.
name|setSize
argument_list|(
literal|0
argument_list|)
operator|.
name|get
argument_list|()
operator|.
name|getHits
argument_list|()
operator|.
name|totalHits
argument_list|()
argument_list|,
name|greaterThanOrEqualTo
argument_list|(
operator|(
name|long
operator|)
name|addedDocSize
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|deleteIndices
argument_list|(
name|newSnapshotIndices
argument_list|)
expr_stmt|;
comment|// clean up indices before starting again
block|}
block|}
DECL|method|listRepoVersions
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|listRepoVersions
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|String
name|prefix
init|=
literal|"repo"
decl_stmt|;
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|repoVersions
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
specifier|final
name|Path
name|repoFiles
init|=
name|getBwcIndicesPath
argument_list|()
decl_stmt|;
try|try
init|(
specifier|final
name|DirectoryStream
argument_list|<
name|Path
argument_list|>
name|dirStream
init|=
name|Files
operator|.
name|newDirectoryStream
argument_list|(
name|repoFiles
argument_list|,
name|prefix
operator|+
literal|"-*.zip"
argument_list|)
init|)
block|{
for|for
control|(
specifier|final
name|Path
name|entry
range|:
name|dirStream
control|)
block|{
specifier|final
name|String
name|fileName
init|=
name|entry
operator|.
name|getFileName
argument_list|()
operator|.
name|toString
argument_list|()
decl_stmt|;
name|String
name|version
init|=
name|fileName
operator|.
name|substring
argument_list|(
name|prefix
operator|.
name|length
argument_list|()
operator|+
literal|1
argument_list|)
decl_stmt|;
name|version
operator|=
name|version
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|version
operator|.
name|length
argument_list|()
operator|-
literal|".zip"
operator|.
name|length
argument_list|()
argument_list|)
expr_stmt|;
name|repoVersions
operator|.
name|add
argument_list|(
name|version
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|Collections
operator|.
name|unmodifiableList
argument_list|(
name|repoVersions
argument_list|)
return|;
block|}
DECL|method|createRepository
specifier|private
name|void
name|createRepository
parameter_list|(
specifier|final
name|String
name|version
parameter_list|,
specifier|final
name|String
name|repoName
parameter_list|)
throws|throws
name|Exception
block|{
specifier|final
name|String
name|prefix
init|=
literal|"repo"
decl_stmt|;
specifier|final
name|Path
name|repoFile
init|=
name|getBwcIndicesPath
argument_list|()
operator|.
name|resolve
argument_list|(
name|prefix
operator|+
literal|"-"
operator|+
name|version
operator|+
literal|".zip"
argument_list|)
decl_stmt|;
specifier|final
name|Path
name|repoPath
init|=
name|randomRepoPath
argument_list|()
decl_stmt|;
name|FileTestUtils
operator|.
name|unzip
argument_list|(
name|repoFile
argument_list|,
name|repoPath
argument_list|,
literal|"repo/"
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
name|repoName
argument_list|)
operator|.
name|setType
argument_list|(
literal|"fs"
argument_list|)
operator|.
name|setSettings
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"location"
argument_list|,
name|repoPath
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|getSnapshots
specifier|private
name|List
argument_list|<
name|SnapshotInfo
argument_list|>
name|getSnapshots
parameter_list|(
specifier|final
name|String
name|repoName
parameter_list|)
throws|throws
name|Exception
block|{
return|return
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
name|repoName
argument_list|)
operator|.
name|addSnapshots
argument_list|(
literal|"_all"
argument_list|)
operator|.
name|get
argument_list|()
operator|.
name|getSnapshots
argument_list|()
return|;
block|}
DECL|method|createSnapshot
specifier|private
name|SnapshotInfo
name|createSnapshot
parameter_list|(
specifier|final
name|String
name|repoName
parameter_list|,
specifier|final
name|String
name|snapshotName
parameter_list|)
throws|throws
name|Exception
block|{
return|return
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
name|repoName
argument_list|,
name|snapshotName
argument_list|)
operator|.
name|setWaitForCompletion
argument_list|(
literal|true
argument_list|)
operator|.
name|get
argument_list|()
operator|.
name|getSnapshotInfo
argument_list|()
return|;
block|}
DECL|method|restoreSnapshot
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|restoreSnapshot
parameter_list|(
specifier|final
name|String
name|repoName
parameter_list|,
specifier|final
name|String
name|snapshotName
parameter_list|)
throws|throws
name|Exception
block|{
return|return
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
name|repoName
argument_list|,
name|snapshotName
argument_list|)
operator|.
name|setWaitForCompletion
argument_list|(
literal|true
argument_list|)
operator|.
name|get
argument_list|()
operator|.
name|getRestoreInfo
argument_list|()
operator|.
name|indices
argument_list|()
return|;
block|}
DECL|method|deleteIndices
specifier|private
name|void
name|deleteIndices
parameter_list|(
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|indices
parameter_list|)
throws|throws
name|Exception
block|{
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
name|indices
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
name|indices
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

