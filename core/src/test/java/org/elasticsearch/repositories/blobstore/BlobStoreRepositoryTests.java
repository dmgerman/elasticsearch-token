begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.repositories.blobstore
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|repositories
operator|.
name|blobstore
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
name|put
operator|.
name|PutRepositoryResponse
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
name|client
operator|.
name|Client
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
name|UUIDs
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
name|bytes
operator|.
name|BytesReference
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
name|BytesStreamOutput
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
name|OutputStreamStreamOutput
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
name|common
operator|.
name|xcontent
operator|.
name|XContentBuilder
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
name|xcontent
operator|.
name|XContentFactory
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
name|xcontent
operator|.
name|XContentType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|repositories
operator|.
name|RepositoriesService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|repositories
operator|.
name|RepositoryData
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
name|ESSingleNodeTestCase
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
name|List
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

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|repositories
operator|.
name|RepositoryDataTests
operator|.
name|generateRandomRepoData
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|repositories
operator|.
name|blobstore
operator|.
name|BlobStoreRepository
operator|.
name|blobId
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

begin_comment
comment|/**  * Tests for the {@link BlobStoreRepository} and its subclasses.  */
end_comment

begin_class
DECL|class|BlobStoreRepositoryTests
specifier|public
class|class
name|BlobStoreRepositoryTests
extends|extends
name|ESSingleNodeTestCase
block|{
DECL|method|testRetrieveSnapshots
specifier|public
name|void
name|testRetrieveSnapshots
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|Client
name|client
init|=
name|client
argument_list|()
decl_stmt|;
specifier|final
name|Path
name|location
init|=
name|ESIntegTestCase
operator|.
name|randomRepoPath
argument_list|(
name|node
argument_list|()
operator|.
name|settings
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|String
name|repositoryName
init|=
literal|"test-repo"
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"-->  creating repository"
argument_list|)
expr_stmt|;
name|PutRepositoryResponse
name|putRepositoryResponse
init|=
name|client
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|preparePutRepository
argument_list|(
name|repositoryName
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
name|node
argument_list|()
operator|.
name|settings
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
literal|"location"
argument_list|,
name|location
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|putRepositoryResponse
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
name|logger
operator|.
name|info
argument_list|(
literal|"--> creating an index and indexing documents"
argument_list|)
expr_stmt|;
specifier|final
name|String
name|indexName
init|=
literal|"test-idx"
decl_stmt|;
name|createIndex
argument_list|(
name|indexName
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
name|int
name|numDocs
init|=
name|randomIntBetween
argument_list|(
literal|10
argument_list|,
literal|20
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
name|numDocs
condition|;
name|i
operator|++
control|)
block|{
name|String
name|id
init|=
name|Integer
operator|.
name|toString
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
name|indexName
argument_list|,
literal|"type1"
argument_list|,
name|id
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"text"
argument_list|,
literal|"sometext"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareFlush
argument_list|(
name|indexName
argument_list|)
operator|.
name|setWaitIfOngoing
argument_list|(
literal|true
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> create first snapshot"
argument_list|)
expr_stmt|;
name|CreateSnapshotResponse
name|createSnapshotResponse
init|=
name|client
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareCreateSnapshot
argument_list|(
name|repositoryName
argument_list|,
literal|"test-snap-1"
argument_list|)
operator|.
name|setWaitForCompletion
argument_list|(
literal|true
argument_list|)
operator|.
name|setIndices
argument_list|(
name|indexName
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
specifier|final
name|SnapshotId
name|snapshotId1
init|=
name|createSnapshotResponse
operator|.
name|getSnapshotInfo
argument_list|()
operator|.
name|snapshotId
argument_list|()
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> create second snapshot"
argument_list|)
expr_stmt|;
name|createSnapshotResponse
operator|=
name|client
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareCreateSnapshot
argument_list|(
name|repositoryName
argument_list|,
literal|"test-snap-2"
argument_list|)
operator|.
name|setWaitForCompletion
argument_list|(
literal|true
argument_list|)
operator|.
name|setIndices
argument_list|(
name|indexName
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
specifier|final
name|SnapshotId
name|snapshotId2
init|=
name|createSnapshotResponse
operator|.
name|getSnapshotInfo
argument_list|()
operator|.
name|snapshotId
argument_list|()
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> make sure the node's repository can resolve the snapshots"
argument_list|)
expr_stmt|;
specifier|final
name|RepositoriesService
name|repositoriesService
init|=
name|getInstanceFromNode
argument_list|(
name|RepositoriesService
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|final
name|BlobStoreRepository
name|repository
init|=
operator|(
name|BlobStoreRepository
operator|)
name|repositoriesService
operator|.
name|repository
argument_list|(
name|repositoryName
argument_list|)
decl_stmt|;
specifier|final
name|List
argument_list|<
name|SnapshotId
argument_list|>
name|originalSnapshots
init|=
name|Arrays
operator|.
name|asList
argument_list|(
name|snapshotId1
argument_list|,
name|snapshotId2
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|SnapshotId
argument_list|>
name|snapshotIds
init|=
name|repository
operator|.
name|getSnapshots
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|sorted
argument_list|(
parameter_list|(
name|s1
parameter_list|,
name|s2
parameter_list|)
lambda|->
name|s1
operator|.
name|getName
argument_list|()
operator|.
name|compareTo
argument_list|(
name|s2
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
name|toList
argument_list|()
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|snapshotIds
argument_list|,
name|equalTo
argument_list|(
name|originalSnapshots
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testReadAndWriteSnapshotsThroughIndexFile
specifier|public
name|void
name|testReadAndWriteSnapshotsThroughIndexFile
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|BlobStoreRepository
name|repository
init|=
name|setupRepo
argument_list|()
decl_stmt|;
comment|// write to and read from a index file with no entries
name|assertThat
argument_list|(
name|repository
operator|.
name|getSnapshots
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
specifier|final
name|RepositoryData
name|emptyData
init|=
operator|new
name|RepositoryData
argument_list|(
name|Collections
operator|.
name|emptyList
argument_list|()
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|)
decl_stmt|;
name|repository
operator|.
name|writeIndexGen
argument_list|(
name|emptyData
argument_list|)
expr_stmt|;
specifier|final
name|RepositoryData
name|readData
init|=
name|repository
operator|.
name|getRepositoryData
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|readData
argument_list|,
name|emptyData
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|readData
operator|.
name|getIndices
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|readData
operator|.
name|getSnapshotIds
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
literal|0
argument_list|)
expr_stmt|;
comment|// write to and read from an index file with snapshots but no indices
specifier|final
name|int
name|numSnapshots
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|20
argument_list|)
decl_stmt|;
specifier|final
name|List
argument_list|<
name|SnapshotId
argument_list|>
name|snapshotIds
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|numSnapshots
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
name|numSnapshots
condition|;
name|i
operator|++
control|)
block|{
name|snapshotIds
operator|.
name|add
argument_list|(
operator|new
name|SnapshotId
argument_list|(
name|randomAsciiOfLength
argument_list|(
literal|8
argument_list|)
argument_list|,
name|UUIDs
operator|.
name|randomBase64UUID
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|RepositoryData
name|repositoryData
init|=
operator|new
name|RepositoryData
argument_list|(
name|snapshotIds
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|)
decl_stmt|;
name|repository
operator|.
name|writeIndexGen
argument_list|(
name|repositoryData
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|repository
operator|.
name|getRepositoryData
argument_list|()
argument_list|,
name|repositoryData
argument_list|)
expr_stmt|;
comment|// write to and read from a index file with random repository data
name|repositoryData
operator|=
name|generateRandomRepoData
argument_list|()
expr_stmt|;
name|repository
operator|.
name|writeIndexGen
argument_list|(
name|repositoryData
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|repository
operator|.
name|getRepositoryData
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|repositoryData
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testIndexGenerationalFiles
specifier|public
name|void
name|testIndexGenerationalFiles
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|BlobStoreRepository
name|repository
init|=
name|setupRepo
argument_list|()
decl_stmt|;
comment|// write to index generational file
name|RepositoryData
name|repositoryData
init|=
name|generateRandomRepoData
argument_list|()
decl_stmt|;
name|repository
operator|.
name|writeIndexGen
argument_list|(
name|repositoryData
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|repository
operator|.
name|getRepositoryData
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|repositoryData
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|repository
operator|.
name|latestIndexBlobId
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|repository
operator|.
name|readSnapshotIndexLatestBlob
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
comment|// adding more and writing to a new index generational file
name|repositoryData
operator|=
name|generateRandomRepoData
argument_list|()
expr_stmt|;
name|repository
operator|.
name|writeIndexGen
argument_list|(
name|repositoryData
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|repository
operator|.
name|getRepositoryData
argument_list|()
argument_list|,
name|repositoryData
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|repository
operator|.
name|latestIndexBlobId
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1L
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|repository
operator|.
name|readSnapshotIndexLatestBlob
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1L
argument_list|)
argument_list|)
expr_stmt|;
comment|// removing a snapshot and writing to a new index generational file
name|repositoryData
operator|=
name|repositoryData
operator|.
name|removeSnapshot
argument_list|(
name|repositoryData
operator|.
name|getSnapshotIds
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|repository
operator|.
name|writeIndexGen
argument_list|(
name|repositoryData
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|repository
operator|.
name|getRepositoryData
argument_list|()
argument_list|,
name|repositoryData
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|repository
operator|.
name|latestIndexBlobId
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|2L
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|repository
operator|.
name|readSnapshotIndexLatestBlob
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|2L
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testOldIndexFileFormat
specifier|public
name|void
name|testOldIndexFileFormat
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|BlobStoreRepository
name|repository
init|=
name|setupRepo
argument_list|()
decl_stmt|;
comment|// write old index file format
specifier|final
name|int
name|numOldSnapshots
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|30
argument_list|)
decl_stmt|;
specifier|final
name|List
argument_list|<
name|SnapshotId
argument_list|>
name|snapshotIds
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
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
name|numOldSnapshots
condition|;
name|i
operator|++
control|)
block|{
name|snapshotIds
operator|.
name|add
argument_list|(
operator|new
name|SnapshotId
argument_list|(
name|randomAsciiOfLength
argument_list|(
literal|8
argument_list|)
argument_list|,
name|SnapshotId
operator|.
name|UNASSIGNED_UUID
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|writeOldFormat
argument_list|(
name|repository
argument_list|,
name|snapshotIds
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|SnapshotId
operator|::
name|getName
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|Sets
operator|.
name|newHashSet
argument_list|(
name|repository
operator|.
name|getSnapshots
argument_list|()
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|Sets
operator|.
name|newHashSet
argument_list|(
name|snapshotIds
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// write to and read from a snapshot file with a random number of new entries added
specifier|final
name|RepositoryData
name|repositoryData
init|=
name|generateRandomRepoData
argument_list|(
name|snapshotIds
argument_list|)
decl_stmt|;
name|repository
operator|.
name|writeIndexGen
argument_list|(
name|repositoryData
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|repository
operator|.
name|getRepositoryData
argument_list|()
argument_list|,
name|repositoryData
argument_list|)
expr_stmt|;
block|}
DECL|method|testBlobId
specifier|public
name|void
name|testBlobId
parameter_list|()
block|{
name|SnapshotId
name|snapshotId
init|=
operator|new
name|SnapshotId
argument_list|(
literal|"abc123"
argument_list|,
name|SnapshotId
operator|.
name|UNASSIGNED_UUID
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|blobId
argument_list|(
name|snapshotId
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"abc123"
argument_list|)
argument_list|)
expr_stmt|;
comment|// just the snapshot name
name|snapshotId
operator|=
operator|new
name|SnapshotId
argument_list|(
literal|"abc-123"
argument_list|,
name|SnapshotId
operator|.
name|UNASSIGNED_UUID
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|blobId
argument_list|(
name|snapshotId
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"abc-123"
argument_list|)
argument_list|)
expr_stmt|;
comment|// just the snapshot name
name|String
name|uuid
init|=
name|UUIDs
operator|.
name|randomBase64UUID
argument_list|()
decl_stmt|;
name|snapshotId
operator|=
operator|new
name|SnapshotId
argument_list|(
literal|"abc123"
argument_list|,
name|uuid
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|blobId
argument_list|(
name|snapshotId
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|uuid
argument_list|)
argument_list|)
expr_stmt|;
comment|// uuid only
name|uuid
operator|=
name|UUIDs
operator|.
name|randomBase64UUID
argument_list|()
expr_stmt|;
name|snapshotId
operator|=
operator|new
name|SnapshotId
argument_list|(
literal|"abc-123"
argument_list|,
name|uuid
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|blobId
argument_list|(
name|snapshotId
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|uuid
argument_list|)
argument_list|)
expr_stmt|;
comment|// uuid only
block|}
DECL|method|setupRepo
specifier|private
name|BlobStoreRepository
name|setupRepo
parameter_list|()
block|{
specifier|final
name|Client
name|client
init|=
name|client
argument_list|()
decl_stmt|;
specifier|final
name|Path
name|location
init|=
name|ESIntegTestCase
operator|.
name|randomRepoPath
argument_list|(
name|node
argument_list|()
operator|.
name|settings
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|String
name|repositoryName
init|=
literal|"test-repo"
decl_stmt|;
name|PutRepositoryResponse
name|putRepositoryResponse
init|=
name|client
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|preparePutRepository
argument_list|(
name|repositoryName
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
name|node
argument_list|()
operator|.
name|settings
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
literal|"location"
argument_list|,
name|location
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|putRepositoryResponse
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
specifier|final
name|RepositoriesService
name|repositoriesService
init|=
name|getInstanceFromNode
argument_list|(
name|RepositoriesService
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|final
name|BlobStoreRepository
name|repository
init|=
operator|(
name|BlobStoreRepository
operator|)
name|repositoriesService
operator|.
name|repository
argument_list|(
name|repositoryName
argument_list|)
decl_stmt|;
return|return
name|repository
return|;
block|}
DECL|method|writeOldFormat
specifier|private
name|void
name|writeOldFormat
parameter_list|(
specifier|final
name|BlobStoreRepository
name|repository
parameter_list|,
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|snapshotNames
parameter_list|)
throws|throws
name|Exception
block|{
specifier|final
name|BytesReference
name|bRef
decl_stmt|;
try|try
init|(
name|BytesStreamOutput
name|bStream
init|=
operator|new
name|BytesStreamOutput
argument_list|()
init|)
block|{
try|try
init|(
name|StreamOutput
name|stream
init|=
operator|new
name|OutputStreamStreamOutput
argument_list|(
name|bStream
argument_list|)
init|)
block|{
name|XContentBuilder
name|builder
init|=
name|XContentFactory
operator|.
name|contentBuilder
argument_list|(
name|XContentType
operator|.
name|JSON
argument_list|,
name|stream
argument_list|)
decl_stmt|;
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|startArray
argument_list|(
literal|"snapshots"
argument_list|)
expr_stmt|;
for|for
control|(
specifier|final
name|String
name|snapshotName
range|:
name|snapshotNames
control|)
block|{
name|builder
operator|.
name|value
argument_list|(
name|snapshotName
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endArray
argument_list|()
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|bRef
operator|=
name|bStream
operator|.
name|bytes
argument_list|()
expr_stmt|;
block|}
name|repository
operator|.
name|blobContainer
argument_list|()
operator|.
name|writeBlob
argument_list|(
name|BlobStoreRepository
operator|.
name|SNAPSHOTS_FILE
argument_list|,
name|bRef
argument_list|)
expr_stmt|;
comment|// write to index file
block|}
block|}
end_class

end_unit

