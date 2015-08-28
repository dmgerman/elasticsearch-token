begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.shard
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|shard
package|;
end_package

begin_import
import|import
name|com
operator|.
name|carrotsearch
operator|.
name|randomizedtesting
operator|.
name|annotations
operator|.
name|Repeat
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
name|mockfile
operator|.
name|FilterFileSystem
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
name|mockfile
operator|.
name|FilterFileSystemProvider
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
name|mockfile
operator|.
name|FilterPath
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
name|common
operator|.
name|SuppressForbidden
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
name|PathUtils
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
name|env
operator|.
name|Environment
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|env
operator|.
name|NodeEnvironment
operator|.
name|NodePath
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|env
operator|.
name|NodeEnvironment
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
name|ESTestCase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|AfterClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|BeforeClass
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
name|File
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
name|lang
operator|.
name|reflect
operator|.
name|Field
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
name|FileStore
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
name|FileSystem
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
name|FileSystems
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
name|attribute
operator|.
name|FileAttributeView
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
name|attribute
operator|.
name|FileStoreAttributeView
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
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|settings
operator|.
name|Settings
operator|.
name|settingsBuilder
import|;
end_import

begin_comment
comment|/** Separate test class from ShardPathTests because we need static (BeforeClass) setup to install mock filesystems... */
end_comment

begin_class
annotation|@
name|SuppressForbidden
argument_list|(
name|reason
operator|=
literal|"ProviderMismatchException if I try to use PathUtils.getDefault instead"
argument_list|)
DECL|class|NewPathForShardTest
specifier|public
class|class
name|NewPathForShardTest
extends|extends
name|ESTestCase
block|{
comment|// Sneakiness to install mock file stores so we can pretend how much free space we have on each path.data:
DECL|field|aFileStore
specifier|private
specifier|static
name|MockFileStore
name|aFileStore
init|=
operator|new
name|MockFileStore
argument_list|(
literal|"mocka"
argument_list|)
decl_stmt|;
DECL|field|bFileStore
specifier|private
specifier|static
name|MockFileStore
name|bFileStore
init|=
operator|new
name|MockFileStore
argument_list|(
literal|"mockb"
argument_list|)
decl_stmt|;
DECL|field|origFileSystem
specifier|private
specifier|static
name|FileSystem
name|origFileSystem
decl_stmt|;
DECL|field|aPathPart
specifier|private
specifier|static
name|String
name|aPathPart
init|=
name|File
operator|.
name|separator
operator|+
literal|'a'
operator|+
name|File
operator|.
name|separator
decl_stmt|;
DECL|field|bPathPart
specifier|private
specifier|static
name|String
name|bPathPart
init|=
name|File
operator|.
name|separator
operator|+
literal|'b'
operator|+
name|File
operator|.
name|separator
decl_stmt|;
annotation|@
name|BeforeClass
DECL|method|installMockUsableSpaceFS
specifier|public
specifier|static
name|void
name|installMockUsableSpaceFS
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Necessary so when Environment.clinit runs, to gather all FileStores, it sees ours:
name|origFileSystem
operator|=
name|FileSystems
operator|.
name|getDefault
argument_list|()
expr_stmt|;
name|Field
name|field
init|=
name|PathUtils
operator|.
name|class
operator|.
name|getDeclaredField
argument_list|(
literal|"DEFAULT"
argument_list|)
decl_stmt|;
name|field
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|FileSystem
name|mock
init|=
operator|new
name|MockUsableSpaceFileSystemProvider
argument_list|()
operator|.
name|getFileSystem
argument_list|(
name|getBaseTempDirForTestClass
argument_list|()
operator|.
name|toUri
argument_list|()
argument_list|)
decl_stmt|;
name|field
operator|.
name|set
argument_list|(
literal|null
argument_list|,
name|mock
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|mock
argument_list|,
name|PathUtils
operator|.
name|getDefaultFileSystem
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
DECL|method|removeMockUsableSpaceFS
specifier|public
specifier|static
name|void
name|removeMockUsableSpaceFS
parameter_list|()
throws|throws
name|Exception
block|{
name|Field
name|field
init|=
name|PathUtils
operator|.
name|class
operator|.
name|getDeclaredField
argument_list|(
literal|"DEFAULT"
argument_list|)
decl_stmt|;
name|field
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|field
operator|.
name|set
argument_list|(
literal|null
argument_list|,
name|origFileSystem
argument_list|)
expr_stmt|;
name|origFileSystem
operator|=
literal|null
expr_stmt|;
name|aFileStore
operator|=
literal|null
expr_stmt|;
name|bFileStore
operator|=
literal|null
expr_stmt|;
block|}
comment|/** Mock file system that fakes usable space for each FileStore */
annotation|@
name|SuppressForbidden
argument_list|(
name|reason
operator|=
literal|"ProviderMismatchException if I try to use PathUtils.getDefault instead"
argument_list|)
DECL|class|MockUsableSpaceFileSystemProvider
specifier|static
class|class
name|MockUsableSpaceFileSystemProvider
extends|extends
name|FilterFileSystemProvider
block|{
DECL|method|MockUsableSpaceFileSystemProvider
specifier|public
name|MockUsableSpaceFileSystemProvider
parameter_list|()
block|{
name|super
argument_list|(
literal|"mockusablespace://"
argument_list|,
name|FileSystems
operator|.
name|getDefault
argument_list|()
argument_list|)
expr_stmt|;
specifier|final
name|List
argument_list|<
name|FileStore
argument_list|>
name|fileStores
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|fileStores
operator|.
name|add
argument_list|(
name|aFileStore
argument_list|)
expr_stmt|;
name|fileStores
operator|.
name|add
argument_list|(
name|bFileStore
argument_list|)
expr_stmt|;
name|fileSystem
operator|=
operator|new
name|FilterFileSystem
argument_list|(
name|this
argument_list|,
name|origFileSystem
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|Iterable
argument_list|<
name|FileStore
argument_list|>
name|getFileStores
parameter_list|()
block|{
return|return
name|fileStores
return|;
block|}
block|}
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getFileStore
specifier|public
name|FileStore
name|getFileStore
parameter_list|(
name|Path
name|path
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|path
operator|.
name|toString
argument_list|()
operator|.
name|contains
argument_list|(
name|aPathPart
argument_list|)
condition|)
block|{
return|return
name|aFileStore
return|;
block|}
else|else
block|{
return|return
name|bFileStore
return|;
block|}
block|}
block|}
DECL|class|MockFileStore
specifier|static
class|class
name|MockFileStore
extends|extends
name|FileStore
block|{
DECL|field|usableSpace
specifier|public
name|long
name|usableSpace
decl_stmt|;
DECL|field|desc
specifier|private
specifier|final
name|String
name|desc
decl_stmt|;
DECL|method|MockFileStore
specifier|public
name|MockFileStore
parameter_list|(
name|String
name|desc
parameter_list|)
block|{
name|this
operator|.
name|desc
operator|=
name|desc
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|type
specifier|public
name|String
name|type
parameter_list|()
block|{
return|return
literal|"mock"
return|;
block|}
annotation|@
name|Override
DECL|method|name
specifier|public
name|String
name|name
parameter_list|()
block|{
return|return
name|desc
return|;
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
name|desc
return|;
block|}
annotation|@
name|Override
DECL|method|isReadOnly
specifier|public
name|boolean
name|isReadOnly
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
DECL|method|getTotalSpace
specifier|public
name|long
name|getTotalSpace
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|usableSpace
operator|*
literal|3
return|;
block|}
annotation|@
name|Override
DECL|method|getUsableSpace
specifier|public
name|long
name|getUsableSpace
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|usableSpace
return|;
block|}
annotation|@
name|Override
DECL|method|getUnallocatedSpace
specifier|public
name|long
name|getUnallocatedSpace
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|usableSpace
operator|*
literal|2
return|;
block|}
annotation|@
name|Override
DECL|method|supportsFileAttributeView
specifier|public
name|boolean
name|supportsFileAttributeView
parameter_list|(
name|Class
argument_list|<
name|?
extends|extends
name|FileAttributeView
argument_list|>
name|type
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
DECL|method|supportsFileAttributeView
specifier|public
name|boolean
name|supportsFileAttributeView
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
DECL|method|getFileStoreAttributeView
specifier|public
parameter_list|<
name|V
extends|extends
name|FileStoreAttributeView
parameter_list|>
name|V
name|getFileStoreAttributeView
parameter_list|(
name|Class
argument_list|<
name|V
argument_list|>
name|type
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|getAttribute
specifier|public
name|Object
name|getAttribute
parameter_list|(
name|String
name|attribute
parameter_list|)
throws|throws
name|IOException
block|{
return|return
literal|null
return|;
block|}
block|}
DECL|method|testSelectNewPathForShard
specifier|public
name|void
name|testSelectNewPathForShard
parameter_list|()
throws|throws
name|Exception
block|{
name|Path
name|path
init|=
name|PathUtils
operator|.
name|get
argument_list|(
name|createTempDir
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
comment|// Use 2 data paths:
name|String
index|[]
name|paths
init|=
operator|new
name|String
index|[]
block|{
name|path
operator|.
name|resolve
argument_list|(
literal|"a"
argument_list|)
operator|.
name|toString
argument_list|()
block|,
name|path
operator|.
name|resolve
argument_list|(
literal|"b"
argument_list|)
operator|.
name|toString
argument_list|()
block|}
decl_stmt|;
name|Settings
name|settings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"path.home"
argument_list|,
name|path
argument_list|)
operator|.
name|putArray
argument_list|(
literal|"path.data"
argument_list|,
name|paths
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|NodeEnvironment
name|nodeEnv
init|=
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
decl_stmt|;
comment|// Make sure all our mocking above actually worked:
name|NodePath
index|[]
name|nodePaths
init|=
name|nodeEnv
operator|.
name|nodePaths
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|nodePaths
operator|.
name|length
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"mocka"
argument_list|,
name|nodePaths
index|[
literal|0
index|]
operator|.
name|fileStore
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"mockb"
argument_list|,
name|nodePaths
index|[
literal|1
index|]
operator|.
name|fileStore
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
comment|// Path a has lots of free space, but b has little, so new shard should go to a:
name|aFileStore
operator|.
name|usableSpace
operator|=
literal|100000
expr_stmt|;
name|bFileStore
operator|.
name|usableSpace
operator|=
literal|1000
expr_stmt|;
name|ShardId
name|shardId
init|=
operator|new
name|ShardId
argument_list|(
literal|"index"
argument_list|,
literal|0
argument_list|)
decl_stmt|;
name|ShardPath
name|result
init|=
name|ShardPath
operator|.
name|selectNewPathForShard
argument_list|(
name|nodeEnv
argument_list|,
name|shardId
argument_list|,
name|Settings
operator|.
name|EMPTY
argument_list|,
literal|100
argument_list|,
name|Collections
operator|.
expr|<
name|Path
argument_list|,
name|Integer
operator|>
name|emptyMap
argument_list|()
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|result
operator|.
name|getDataPath
argument_list|()
operator|.
name|toString
argument_list|()
operator|.
name|contains
argument_list|(
name|aPathPart
argument_list|)
argument_list|)
expr_stmt|;
comment|// Test the reverse: b has lots of free space, but a has little, so new shard should go to b:
name|aFileStore
operator|.
name|usableSpace
operator|=
literal|1000
expr_stmt|;
name|bFileStore
operator|.
name|usableSpace
operator|=
literal|100000
expr_stmt|;
name|shardId
operator|=
operator|new
name|ShardId
argument_list|(
literal|"index"
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|result
operator|=
name|ShardPath
operator|.
name|selectNewPathForShard
argument_list|(
name|nodeEnv
argument_list|,
name|shardId
argument_list|,
name|Settings
operator|.
name|EMPTY
argument_list|,
literal|100
argument_list|,
name|Collections
operator|.
expr|<
name|Path
argument_list|,
name|Integer
operator|>
name|emptyMap
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|result
operator|.
name|getDataPath
argument_list|()
operator|.
name|toString
argument_list|()
operator|.
name|contains
argument_list|(
name|bPathPart
argument_list|)
argument_list|)
expr_stmt|;
comment|// Now a and be have equal usable space; we allocate two shards to the node, and each should go to different paths:
name|aFileStore
operator|.
name|usableSpace
operator|=
literal|100000
expr_stmt|;
name|bFileStore
operator|.
name|usableSpace
operator|=
literal|100000
expr_stmt|;
name|Map
argument_list|<
name|Path
argument_list|,
name|Integer
argument_list|>
name|dataPathToShardCount
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|ShardPath
name|result1
init|=
name|ShardPath
operator|.
name|selectNewPathForShard
argument_list|(
name|nodeEnv
argument_list|,
name|shardId
argument_list|,
name|Settings
operator|.
name|EMPTY
argument_list|,
literal|100
argument_list|,
name|dataPathToShardCount
argument_list|)
decl_stmt|;
name|dataPathToShardCount
operator|.
name|put
argument_list|(
name|NodeEnvironment
operator|.
name|shardStatePathToDataPath
argument_list|(
name|result1
operator|.
name|getDataPath
argument_list|()
argument_list|)
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|ShardPath
name|result2
init|=
name|ShardPath
operator|.
name|selectNewPathForShard
argument_list|(
name|nodeEnv
argument_list|,
name|shardId
argument_list|,
name|Settings
operator|.
name|EMPTY
argument_list|,
literal|100
argument_list|,
name|dataPathToShardCount
argument_list|)
decl_stmt|;
comment|// #11122: this was the original failure: on a node with 2 disks that have nearly equal
comment|// free space, we would always allocate all N incoming shards to the one path that
comment|// had the most free space, never using the other drive unless new shards arrive
comment|// after the first shards started using storage:
name|assertNotEquals
argument_list|(
name|result1
operator|.
name|getDataPath
argument_list|()
argument_list|,
name|result2
operator|.
name|getDataPath
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

