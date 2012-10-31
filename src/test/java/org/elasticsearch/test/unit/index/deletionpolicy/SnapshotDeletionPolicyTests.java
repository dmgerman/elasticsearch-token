begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.test.unit.index.deletionpolicy
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|unit
operator|.
name|index
operator|.
name|deletionpolicy
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
name|document
operator|.
name|Document
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|document
operator|.
name|Field
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|document
operator|.
name|TextField
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|index
operator|.
name|IndexWriter
import|;
end_import

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
name|RAMDirectory
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
name|lucene
operator|.
name|Lucene
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
name|Index
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
name|deletionpolicy
operator|.
name|KeepOnlyLastDeletionPolicy
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
name|deletionpolicy
operator|.
name|SnapshotDeletionPolicy
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
name|deletionpolicy
operator|.
name|SnapshotIndexCommit
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
name|deletionpolicy
operator|.
name|SnapshotIndexCommits
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
name|testng
operator|.
name|annotations
operator|.
name|AfterClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|testng
operator|.
name|annotations
operator|.
name|BeforeClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|testng
operator|.
name|annotations
operator|.
name|Test
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|index
operator|.
name|IndexReader
operator|.
name|listCommits
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|settings
operator|.
name|ImmutableSettings
operator|.
name|Builder
operator|.
name|EMPTY_SETTINGS
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|MatcherAssert
operator|.
name|assertThat
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
comment|/**  * A set of tests for {@link org.elasticsearch.index.deletionpolicy.SnapshotDeletionPolicy}.  *  *  */
end_comment

begin_class
DECL|class|SnapshotDeletionPolicyTests
specifier|public
class|class
name|SnapshotDeletionPolicyTests
block|{
DECL|field|shardId
specifier|protected
specifier|final
name|ShardId
name|shardId
init|=
operator|new
name|ShardId
argument_list|(
operator|new
name|Index
argument_list|(
literal|"index"
argument_list|)
argument_list|,
literal|1
argument_list|)
decl_stmt|;
DECL|field|dir
specifier|private
name|RAMDirectory
name|dir
decl_stmt|;
DECL|field|deletionPolicy
specifier|private
name|SnapshotDeletionPolicy
name|deletionPolicy
decl_stmt|;
DECL|field|indexWriter
specifier|private
name|IndexWriter
name|indexWriter
decl_stmt|;
annotation|@
name|BeforeClass
DECL|method|setUp
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|dir
operator|=
operator|new
name|RAMDirectory
argument_list|()
expr_stmt|;
name|deletionPolicy
operator|=
operator|new
name|SnapshotDeletionPolicy
argument_list|(
operator|new
name|KeepOnlyLastDeletionPolicy
argument_list|(
name|shardId
argument_list|,
name|EMPTY_SETTINGS
argument_list|)
argument_list|)
expr_stmt|;
name|indexWriter
operator|=
operator|new
name|IndexWriter
argument_list|(
name|dir
argument_list|,
name|Lucene
operator|.
name|STANDARD_ANALYZER
argument_list|,
literal|true
argument_list|,
name|deletionPolicy
argument_list|,
name|IndexWriter
operator|.
name|MaxFieldLength
operator|.
name|UNLIMITED
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
DECL|method|tearDown
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|indexWriter
operator|.
name|close
argument_list|()
expr_stmt|;
name|dir
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
DECL|method|testDocument
specifier|private
name|Document
name|testDocument
parameter_list|()
block|{
name|Document
name|document
init|=
operator|new
name|Document
argument_list|()
decl_stmt|;
name|document
operator|.
name|add
argument_list|(
operator|new
name|TextField
argument_list|(
literal|"test"
argument_list|,
literal|"1"
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|YES
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|document
return|;
block|}
annotation|@
name|Test
DECL|method|testSimpleSnapshot
specifier|public
name|void
name|testSimpleSnapshot
parameter_list|()
throws|throws
name|Exception
block|{
comment|// add a document and commit, resulting in one commit point
name|indexWriter
operator|.
name|addDocument
argument_list|(
name|testDocument
argument_list|()
argument_list|)
expr_stmt|;
name|indexWriter
operator|.
name|commit
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|listCommits
argument_list|(
name|dir
argument_list|)
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
comment|// add another document and commit, resulting again in one commit point
name|indexWriter
operator|.
name|addDocument
argument_list|(
name|testDocument
argument_list|()
argument_list|)
expr_stmt|;
name|indexWriter
operator|.
name|commit
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|listCommits
argument_list|(
name|dir
argument_list|)
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
comment|// snapshot the last commit, and then add a document and commit, now we should have two commit points
name|SnapshotIndexCommit
name|snapshot
init|=
name|deletionPolicy
operator|.
name|snapshot
argument_list|()
decl_stmt|;
name|indexWriter
operator|.
name|addDocument
argument_list|(
name|testDocument
argument_list|()
argument_list|)
expr_stmt|;
name|indexWriter
operator|.
name|commit
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|listCommits
argument_list|(
name|dir
argument_list|)
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
comment|// release the commit, add a document and commit, now we should be back to one commit point
name|assertThat
argument_list|(
name|snapshot
operator|.
name|release
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|indexWriter
operator|.
name|addDocument
argument_list|(
name|testDocument
argument_list|()
argument_list|)
expr_stmt|;
name|indexWriter
operator|.
name|commit
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|listCommits
argument_list|(
name|dir
argument_list|)
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
block|}
annotation|@
name|Test
DECL|method|testMultiSnapshot
specifier|public
name|void
name|testMultiSnapshot
parameter_list|()
throws|throws
name|Exception
block|{
comment|// add a document and commit, resulting in one commit point
name|indexWriter
operator|.
name|addDocument
argument_list|(
name|testDocument
argument_list|()
argument_list|)
expr_stmt|;
name|indexWriter
operator|.
name|commit
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|listCommits
argument_list|(
name|dir
argument_list|)
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
comment|// take two snapshots
name|SnapshotIndexCommit
name|snapshot1
init|=
name|deletionPolicy
operator|.
name|snapshot
argument_list|()
decl_stmt|;
name|SnapshotIndexCommit
name|snapshot2
init|=
name|deletionPolicy
operator|.
name|snapshot
argument_list|()
decl_stmt|;
comment|// we should have two commits points
name|indexWriter
operator|.
name|addDocument
argument_list|(
name|testDocument
argument_list|()
argument_list|)
expr_stmt|;
name|indexWriter
operator|.
name|commit
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|listCommits
argument_list|(
name|dir
argument_list|)
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
comment|// release one snapshot, we should still have two commit points
name|assertThat
argument_list|(
name|snapshot1
operator|.
name|release
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|indexWriter
operator|.
name|addDocument
argument_list|(
name|testDocument
argument_list|()
argument_list|)
expr_stmt|;
name|indexWriter
operator|.
name|commit
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|listCommits
argument_list|(
name|dir
argument_list|)
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
comment|// release the second snapshot, we should be back to one commit
name|assertThat
argument_list|(
name|snapshot2
operator|.
name|release
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|indexWriter
operator|.
name|addDocument
argument_list|(
name|testDocument
argument_list|()
argument_list|)
expr_stmt|;
name|indexWriter
operator|.
name|commit
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|listCommits
argument_list|(
name|dir
argument_list|)
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
block|}
annotation|@
name|Test
DECL|method|testMultiReleaseException
specifier|public
name|void
name|testMultiReleaseException
parameter_list|()
throws|throws
name|Exception
block|{
comment|// add a document and commit, resulting in one commit point
name|indexWriter
operator|.
name|addDocument
argument_list|(
name|testDocument
argument_list|()
argument_list|)
expr_stmt|;
name|indexWriter
operator|.
name|commit
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|listCommits
argument_list|(
name|dir
argument_list|)
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
comment|// snapshot the last commit, and release it twice, the seconds should throw an exception
name|SnapshotIndexCommit
name|snapshot
init|=
name|deletionPolicy
operator|.
name|snapshot
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|snapshot
operator|.
name|release
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|snapshot
operator|.
name|release
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testSimpleSnapshots
specifier|public
name|void
name|testSimpleSnapshots
parameter_list|()
throws|throws
name|Exception
block|{
comment|// add a document and commit, resulting in one commit point
name|indexWriter
operator|.
name|addDocument
argument_list|(
name|testDocument
argument_list|()
argument_list|)
expr_stmt|;
name|indexWriter
operator|.
name|commit
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|listCommits
argument_list|(
name|dir
argument_list|)
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
comment|// add another document and commit, resulting again in one commint point
name|indexWriter
operator|.
name|addDocument
argument_list|(
name|testDocument
argument_list|()
argument_list|)
expr_stmt|;
name|indexWriter
operator|.
name|commit
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|listCommits
argument_list|(
name|dir
argument_list|)
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
comment|// snapshot the last commit, and then add a document and commit, now we should have two commit points
name|SnapshotIndexCommit
name|snapshot
init|=
name|deletionPolicy
operator|.
name|snapshot
argument_list|()
decl_stmt|;
name|indexWriter
operator|.
name|addDocument
argument_list|(
name|testDocument
argument_list|()
argument_list|)
expr_stmt|;
name|indexWriter
operator|.
name|commit
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|listCommits
argument_list|(
name|dir
argument_list|)
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
comment|// now, take a snapshot of all the commits
name|SnapshotIndexCommits
name|snapshots
init|=
name|deletionPolicy
operator|.
name|snapshots
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|snapshots
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
comment|// release the snapshot, add a document and commit
comment|// we should have 3 commits points since we are holding onto the first two with snapshots
comment|// and we are using the keep only last
name|assertThat
argument_list|(
name|snapshot
operator|.
name|release
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|indexWriter
operator|.
name|addDocument
argument_list|(
name|testDocument
argument_list|()
argument_list|)
expr_stmt|;
name|indexWriter
operator|.
name|commit
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|listCommits
argument_list|(
name|dir
argument_list|)
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
comment|// now release the snapshots, we should be back to a single commit point
name|assertThat
argument_list|(
name|snapshots
operator|.
name|release
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|indexWriter
operator|.
name|addDocument
argument_list|(
name|testDocument
argument_list|()
argument_list|)
expr_stmt|;
name|indexWriter
operator|.
name|commit
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|listCommits
argument_list|(
name|dir
argument_list|)
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
block|}
block|}
end_class

end_unit

