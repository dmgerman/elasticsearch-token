begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.env
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|env
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
name|LockObtainFailedException
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
name|util
operator|.
name|IOUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchIllegalStateException
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
name|test
operator|.
name|ElasticsearchTestCase
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
name|nio
operator|.
name|file
operator|.
name|Paths
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
name|Set
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
name|atomic
operator|.
name|AtomicInteger
import|;
end_import

begin_class
DECL|class|NodeEnvironmentTests
specifier|public
class|class
name|NodeEnvironmentTests
extends|extends
name|ElasticsearchTestCase
block|{
annotation|@
name|Test
DECL|method|testNodeLockSingleEnvironment
specifier|public
name|void
name|testNodeLockSingleEnvironment
parameter_list|()
throws|throws
name|IOException
block|{
name|NodeEnvironment
name|env
init|=
name|newNodeEnvironment
argument_list|(
name|ImmutableSettings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"node.max_local_storage_nodes"
argument_list|,
literal|1
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|Settings
name|settings
init|=
name|env
operator|.
name|getSettings
argument_list|()
decl_stmt|;
name|String
index|[]
name|dataPaths
init|=
name|env
operator|.
name|getSettings
argument_list|()
operator|.
name|getAsArray
argument_list|(
literal|"path.data"
argument_list|)
decl_stmt|;
try|try
block|{
operator|new
name|NodeEnvironment
argument_list|(
name|settings
argument_list|,
operator|new
name|Environment
argument_list|(
name|settings
argument_list|)
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"env is already locked"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ElasticsearchIllegalStateException
name|ex
parameter_list|)
block|{          }
name|env
operator|.
name|close
argument_list|()
expr_stmt|;
comment|// now can recreate and lock it
name|env
operator|=
operator|new
name|NodeEnvironment
argument_list|(
name|settings
argument_list|,
operator|new
name|Environment
argument_list|(
name|settings
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|env
operator|.
name|nodeDataPaths
argument_list|()
operator|.
name|length
argument_list|,
name|dataPaths
operator|.
name|length
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
name|dataPaths
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|assertTrue
argument_list|(
name|env
operator|.
name|nodeDataPaths
argument_list|()
index|[
name|i
index|]
operator|.
name|startsWith
argument_list|(
name|Paths
operator|.
name|get
argument_list|(
name|dataPaths
index|[
name|i
index|]
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|env
operator|.
name|close
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
literal|"LockedShards: "
operator|+
name|env
operator|.
name|lockedShards
argument_list|()
argument_list|,
name|env
operator|.
name|lockedShards
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testNodeLockMultipleEnvironment
specifier|public
name|void
name|testNodeLockMultipleEnvironment
parameter_list|()
throws|throws
name|IOException
block|{
specifier|final
name|NodeEnvironment
name|first
init|=
name|newNodeEnvironment
argument_list|()
decl_stmt|;
name|String
index|[]
name|dataPaths
init|=
name|first
operator|.
name|getSettings
argument_list|()
operator|.
name|getAsArray
argument_list|(
literal|"path.data"
argument_list|)
decl_stmt|;
name|NodeEnvironment
name|second
init|=
operator|new
name|NodeEnvironment
argument_list|(
name|first
operator|.
name|getSettings
argument_list|()
argument_list|,
operator|new
name|Environment
argument_list|(
name|first
operator|.
name|getSettings
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|first
operator|.
name|nodeDataPaths
argument_list|()
operator|.
name|length
argument_list|,
name|dataPaths
operator|.
name|length
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|second
operator|.
name|nodeDataPaths
argument_list|()
operator|.
name|length
argument_list|,
name|dataPaths
operator|.
name|length
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
name|dataPaths
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|assertEquals
argument_list|(
name|first
operator|.
name|nodeDataPaths
argument_list|()
index|[
name|i
index|]
operator|.
name|getParent
argument_list|()
argument_list|,
name|second
operator|.
name|nodeDataPaths
argument_list|()
index|[
name|i
index|]
operator|.
name|getParent
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|IOUtils
operator|.
name|close
argument_list|(
name|first
argument_list|,
name|second
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testShardLock
specifier|public
name|void
name|testShardLock
parameter_list|()
throws|throws
name|IOException
block|{
specifier|final
name|NodeEnvironment
name|env
init|=
name|newNodeEnvironment
argument_list|()
decl_stmt|;
name|ShardLock
name|fooLock
init|=
name|env
operator|.
name|shardLock
argument_list|(
operator|new
name|ShardId
argument_list|(
literal|"foo"
argument_list|,
literal|1
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
operator|new
name|ShardId
argument_list|(
literal|"foo"
argument_list|,
literal|1
argument_list|)
argument_list|,
name|fooLock
operator|.
name|getShardId
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
name|env
operator|.
name|shardLock
argument_list|(
operator|new
name|ShardId
argument_list|(
literal|"foo"
argument_list|,
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"shard is locked"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|LockObtainFailedException
name|ex
parameter_list|)
block|{
comment|// expected
block|}
for|for
control|(
name|Path
name|path
range|:
name|env
operator|.
name|indexPaths
argument_list|(
operator|new
name|Index
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
control|)
block|{
name|Files
operator|.
name|createDirectories
argument_list|(
name|path
operator|.
name|resolve
argument_list|(
literal|"1"
argument_list|)
argument_list|)
expr_stmt|;
name|Files
operator|.
name|createDirectories
argument_list|(
name|path
operator|.
name|resolve
argument_list|(
literal|"2"
argument_list|)
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|env
operator|.
name|lockAllForIndex
argument_list|(
operator|new
name|Index
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"shard 1 is locked"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|LockObtainFailedException
name|ex
parameter_list|)
block|{
comment|// expected
block|}
name|fooLock
operator|.
name|close
argument_list|()
expr_stmt|;
comment|// can lock again?
name|env
operator|.
name|shardLock
argument_list|(
operator|new
name|ShardId
argument_list|(
literal|"foo"
argument_list|,
literal|1
argument_list|)
argument_list|)
operator|.
name|close
argument_list|()
expr_stmt|;
name|List
argument_list|<
name|ShardLock
argument_list|>
name|locks
init|=
name|env
operator|.
name|lockAllForIndex
argument_list|(
operator|new
name|Index
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
decl_stmt|;
try|try
block|{
name|env
operator|.
name|shardLock
argument_list|(
operator|new
name|ShardId
argument_list|(
literal|"foo"
argument_list|,
name|randomBoolean
argument_list|()
condition|?
literal|1
else|:
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"shard is locked"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|LockObtainFailedException
name|ex
parameter_list|)
block|{
comment|// expected
block|}
name|IOUtils
operator|.
name|close
argument_list|(
name|locks
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"LockedShards: "
operator|+
name|env
operator|.
name|lockedShards
argument_list|()
argument_list|,
name|env
operator|.
name|lockedShards
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|env
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testGetAllIndices
specifier|public
name|void
name|testGetAllIndices
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|NodeEnvironment
name|env
init|=
name|newNodeEnvironment
argument_list|()
decl_stmt|;
specifier|final
name|int
name|numIndices
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|10
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
name|numIndices
condition|;
name|i
operator|++
control|)
block|{
for|for
control|(
name|Path
name|path
range|:
name|env
operator|.
name|indexPaths
argument_list|(
operator|new
name|Index
argument_list|(
literal|"foo"
operator|+
name|i
argument_list|)
argument_list|)
control|)
block|{
name|Files
operator|.
name|createDirectories
argument_list|(
name|path
argument_list|)
expr_stmt|;
block|}
block|}
name|Set
argument_list|<
name|String
argument_list|>
name|indices
init|=
name|env
operator|.
name|findAllIndices
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|indices
operator|.
name|size
argument_list|()
argument_list|,
name|numIndices
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
name|numIndices
condition|;
name|i
operator|++
control|)
block|{
name|assertTrue
argument_list|(
name|indices
operator|.
name|contains
argument_list|(
literal|"foo"
operator|+
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|assertTrue
argument_list|(
literal|"LockedShards: "
operator|+
name|env
operator|.
name|lockedShards
argument_list|()
argument_list|,
name|env
operator|.
name|lockedShards
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|env
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testDeleteSafe
specifier|public
name|void
name|testDeleteSafe
parameter_list|()
throws|throws
name|IOException
block|{
specifier|final
name|NodeEnvironment
name|env
init|=
name|newNodeEnvironment
argument_list|()
decl_stmt|;
name|ShardLock
name|fooLock
init|=
name|env
operator|.
name|shardLock
argument_list|(
operator|new
name|ShardId
argument_list|(
literal|"foo"
argument_list|,
literal|1
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
operator|new
name|ShardId
argument_list|(
literal|"foo"
argument_list|,
literal|1
argument_list|)
argument_list|,
name|fooLock
operator|.
name|getShardId
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Path
name|path
range|:
name|env
operator|.
name|indexPaths
argument_list|(
operator|new
name|Index
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
control|)
block|{
name|Files
operator|.
name|createDirectories
argument_list|(
name|path
operator|.
name|resolve
argument_list|(
literal|"1"
argument_list|)
argument_list|)
expr_stmt|;
name|Files
operator|.
name|createDirectories
argument_list|(
name|path
operator|.
name|resolve
argument_list|(
literal|"2"
argument_list|)
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|env
operator|.
name|deleteShardDirectorySafe
argument_list|(
operator|new
name|ShardId
argument_list|(
literal|"foo"
argument_list|,
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"shard is locked"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|LockObtainFailedException
name|ex
parameter_list|)
block|{
comment|// expected
block|}
for|for
control|(
name|Path
name|path
range|:
name|env
operator|.
name|indexPaths
argument_list|(
operator|new
name|Index
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
control|)
block|{
name|assertTrue
argument_list|(
name|Files
operator|.
name|exists
argument_list|(
name|path
operator|.
name|resolve
argument_list|(
literal|"1"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Files
operator|.
name|exists
argument_list|(
name|path
operator|.
name|resolve
argument_list|(
literal|"2"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|env
operator|.
name|deleteShardDirectorySafe
argument_list|(
operator|new
name|ShardId
argument_list|(
literal|"foo"
argument_list|,
literal|2
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|Path
name|path
range|:
name|env
operator|.
name|indexPaths
argument_list|(
operator|new
name|Index
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
control|)
block|{
name|assertTrue
argument_list|(
name|Files
operator|.
name|exists
argument_list|(
name|path
operator|.
name|resolve
argument_list|(
literal|"1"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|Files
operator|.
name|exists
argument_list|(
name|path
operator|.
name|resolve
argument_list|(
literal|"2"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|env
operator|.
name|deleteIndexDirectorySafe
argument_list|(
operator|new
name|Index
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"shard is locked"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|LockObtainFailedException
name|ex
parameter_list|)
block|{
comment|// expected
block|}
name|fooLock
operator|.
name|close
argument_list|()
expr_stmt|;
for|for
control|(
name|Path
name|path
range|:
name|env
operator|.
name|indexPaths
argument_list|(
operator|new
name|Index
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
control|)
block|{
name|assertTrue
argument_list|(
name|Files
operator|.
name|exists
argument_list|(
name|path
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|env
operator|.
name|deleteIndexDirectorySafe
argument_list|(
operator|new
name|Index
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|Path
name|path
range|:
name|env
operator|.
name|indexPaths
argument_list|(
operator|new
name|Index
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
control|)
block|{
name|assertFalse
argument_list|(
name|Files
operator|.
name|exists
argument_list|(
name|path
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|assertTrue
argument_list|(
literal|"LockedShards: "
operator|+
name|env
operator|.
name|lockedShards
argument_list|()
argument_list|,
name|env
operator|.
name|lockedShards
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|env
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testGetAllShards
specifier|public
name|void
name|testGetAllShards
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|NodeEnvironment
name|env
init|=
name|newNodeEnvironment
argument_list|()
decl_stmt|;
specifier|final
name|int
name|numIndices
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
decl_stmt|;
specifier|final
name|Set
argument_list|<
name|ShardId
argument_list|>
name|createdShards
init|=
operator|new
name|HashSet
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
name|numIndices
condition|;
name|i
operator|++
control|)
block|{
for|for
control|(
name|Path
name|path
range|:
name|env
operator|.
name|indexPaths
argument_list|(
operator|new
name|Index
argument_list|(
literal|"foo"
operator|+
name|i
argument_list|)
argument_list|)
control|)
block|{
specifier|final
name|int
name|numShards
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|numShards
condition|;
name|j
operator|++
control|)
block|{
name|Files
operator|.
name|createDirectories
argument_list|(
name|path
operator|.
name|resolve
argument_list|(
name|Integer
operator|.
name|toString
argument_list|(
name|j
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|createdShards
operator|.
name|add
argument_list|(
operator|new
name|ShardId
argument_list|(
literal|"foo"
operator|+
name|i
argument_list|,
name|j
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|Set
argument_list|<
name|ShardId
argument_list|>
name|shards
init|=
name|env
operator|.
name|findAllShardIds
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|shards
operator|.
name|size
argument_list|()
argument_list|,
name|createdShards
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|shards
argument_list|,
name|createdShards
argument_list|)
expr_stmt|;
name|Index
name|index
init|=
operator|new
name|Index
argument_list|(
literal|"foo"
operator|+
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
name|numIndices
argument_list|)
argument_list|)
decl_stmt|;
name|shards
operator|=
name|env
operator|.
name|findAllShardIds
argument_list|(
name|index
argument_list|)
expr_stmt|;
for|for
control|(
name|ShardId
name|id
range|:
name|createdShards
control|)
block|{
if|if
condition|(
name|index
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
name|id
operator|.
name|getIndex
argument_list|()
argument_list|)
condition|)
block|{
name|assertNotNull
argument_list|(
literal|"missing shard "
operator|+
name|id
argument_list|,
name|shards
operator|.
name|remove
argument_list|(
name|id
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|assertEquals
argument_list|(
literal|"too many shards found"
argument_list|,
name|shards
operator|.
name|size
argument_list|()
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"LockedShards: "
operator|+
name|env
operator|.
name|lockedShards
argument_list|()
argument_list|,
name|env
operator|.
name|lockedShards
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|env
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testStressShardLock
specifier|public
name|void
name|testStressShardLock
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
class|class
name|Int
block|{
name|int
name|value
init|=
literal|0
decl_stmt|;
block|}
specifier|final
name|NodeEnvironment
name|env
init|=
name|newNodeEnvironment
argument_list|()
decl_stmt|;
specifier|final
name|int
name|shards
init|=
name|randomIntBetween
argument_list|(
literal|2
argument_list|,
literal|10
argument_list|)
decl_stmt|;
specifier|final
name|Int
index|[]
name|counts
init|=
operator|new
name|Int
index|[
name|shards
index|]
decl_stmt|;
specifier|final
name|AtomicInteger
index|[]
name|countsAtomic
init|=
operator|new
name|AtomicInteger
index|[
name|shards
index|]
decl_stmt|;
specifier|final
name|AtomicInteger
index|[]
name|flipFlop
init|=
operator|new
name|AtomicInteger
index|[
name|shards
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
name|counts
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|counts
index|[
name|i
index|]
operator|=
operator|new
name|Int
argument_list|()
expr_stmt|;
name|countsAtomic
index|[
name|i
index|]
operator|=
operator|new
name|AtomicInteger
argument_list|()
expr_stmt|;
name|flipFlop
index|[
name|i
index|]
operator|=
operator|new
name|AtomicInteger
argument_list|()
expr_stmt|;
block|}
name|Thread
index|[]
name|threads
init|=
operator|new
name|Thread
index|[
name|randomIntBetween
argument_list|(
literal|2
argument_list|,
literal|5
argument_list|)
index|]
decl_stmt|;
specifier|final
name|CountDownLatch
name|latch
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
specifier|final
name|int
name|iters
init|=
name|scaledRandomIntBetween
argument_list|(
literal|10000
argument_list|,
literal|100000
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
name|threads
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|threads
index|[
name|i
index|]
operator|=
operator|new
name|Thread
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
name|latch
operator|.
name|await
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|fail
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|iters
condition|;
name|i
operator|++
control|)
block|{
name|int
name|shard
init|=
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
name|counts
operator|.
name|length
operator|-
literal|1
argument_list|)
decl_stmt|;
try|try
block|{
try|try
init|(
name|ShardLock
name|_
init|=
name|env
operator|.
name|shardLock
argument_list|(
operator|new
name|ShardId
argument_list|(
literal|"foo"
argument_list|,
name|shard
argument_list|)
argument_list|,
name|scaledRandomIntBetween
argument_list|(
literal|0
argument_list|,
literal|10
argument_list|)
argument_list|)
init|)
block|{
name|counts
index|[
name|shard
index|]
operator|.
name|value
operator|++
expr_stmt|;
name|countsAtomic
index|[
name|shard
index|]
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
name|flipFlop
index|[
name|shard
index|]
operator|.
name|incrementAndGet
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|flipFlop
index|[
name|shard
index|]
operator|.
name|decrementAndGet
argument_list|()
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|LockObtainFailedException
name|ex
parameter_list|)
block|{
comment|// ok
block|}
catch|catch
parameter_list|(
name|IOException
name|ex
parameter_list|)
block|{
name|fail
argument_list|(
name|ex
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
expr_stmt|;
name|threads
index|[
name|i
index|]
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
comment|// fire the threads up
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|threads
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|threads
index|[
name|i
index|]
operator|.
name|join
argument_list|()
expr_stmt|;
block|}
name|assertTrue
argument_list|(
literal|"LockedShards: "
operator|+
name|env
operator|.
name|lockedShards
argument_list|()
argument_list|,
name|env
operator|.
name|lockedShards
argument_list|()
operator|.
name|isEmpty
argument_list|()
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
name|counts
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|assertTrue
argument_list|(
name|counts
index|[
name|i
index|]
operator|.
name|value
operator|>
literal|0
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|flipFlop
index|[
name|i
index|]
operator|.
name|get
argument_list|()
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|counts
index|[
name|i
index|]
operator|.
name|value
argument_list|,
name|countsAtomic
index|[
name|i
index|]
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|env
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

