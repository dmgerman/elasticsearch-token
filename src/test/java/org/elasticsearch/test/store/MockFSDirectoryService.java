begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.test.store
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|store
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
name|base
operator|.
name|Charsets
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
name|CheckIndex
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
name|Directory
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
name|LockFactory
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
name|StoreRateLimiting
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
name|lease
operator|.
name|Releasables
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
name|settings
operator|.
name|IndexSettings
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
name|IndexShardException
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
name|IndexShardState
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
name|shard
operator|.
name|IndexShard
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
name|store
operator|.
name|IndexStore
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
name|store
operator|.
name|Store
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
name|store
operator|.
name|distributor
operator|.
name|Distributor
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
name|store
operator|.
name|fs
operator|.
name|FsDirectoryService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|IndicesLifecycle
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|IndicesService
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
name|elasticsearch
operator|.
name|test
operator|.
name|ElasticsearchIntegrationTest
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
name|io
operator|.
name|PrintStream
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
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|EnumSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Random
import|;
end_import

begin_class
DECL|class|MockFSDirectoryService
specifier|public
class|class
name|MockFSDirectoryService
extends|extends
name|FsDirectoryService
block|{
DECL|field|validCheckIndexStates
specifier|private
specifier|static
specifier|final
name|EnumSet
argument_list|<
name|IndexShardState
argument_list|>
name|validCheckIndexStates
init|=
name|EnumSet
operator|.
name|of
argument_list|(
name|IndexShardState
operator|.
name|STARTED
argument_list|,
name|IndexShardState
operator|.
name|RELOCATED
argument_list|,
name|IndexShardState
operator|.
name|POST_RECOVERY
argument_list|)
decl_stmt|;
DECL|field|helper
specifier|private
specifier|final
name|MockDirectoryHelper
name|helper
decl_stmt|;
DECL|field|delegateService
specifier|private
name|FsDirectoryService
name|delegateService
decl_stmt|;
DECL|field|CHECK_INDEX_ON_CLOSE
specifier|public
specifier|static
specifier|final
name|String
name|CHECK_INDEX_ON_CLOSE
init|=
literal|"index.store.mock.check_index_on_close"
decl_stmt|;
DECL|field|checkIndexOnClose
specifier|private
specifier|final
name|boolean
name|checkIndexOnClose
decl_stmt|;
annotation|@
name|Inject
DECL|method|MockFSDirectoryService
specifier|public
name|MockFSDirectoryService
parameter_list|(
specifier|final
name|ShardId
name|shardId
parameter_list|,
annotation|@
name|IndexSettings
name|Settings
name|indexSettings
parameter_list|,
name|IndexStore
name|indexStore
parameter_list|,
specifier|final
name|IndicesService
name|service
parameter_list|)
block|{
name|super
argument_list|(
name|shardId
argument_list|,
name|indexSettings
argument_list|,
name|indexStore
argument_list|)
expr_stmt|;
specifier|final
name|long
name|seed
init|=
name|indexSettings
operator|.
name|getAsLong
argument_list|(
name|ElasticsearchIntegrationTest
operator|.
name|SETTING_INDEX_SEED
argument_list|,
literal|0l
argument_list|)
decl_stmt|;
name|Random
name|random
init|=
operator|new
name|Random
argument_list|(
name|seed
argument_list|)
decl_stmt|;
name|helper
operator|=
operator|new
name|MockDirectoryHelper
argument_list|(
name|shardId
argument_list|,
name|indexSettings
argument_list|,
name|logger
argument_list|,
name|random
argument_list|,
name|seed
argument_list|)
expr_stmt|;
name|checkIndexOnClose
operator|=
name|indexSettings
operator|.
name|getAsBoolean
argument_list|(
name|CHECK_INDEX_ON_CLOSE
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|delegateService
operator|=
name|helper
operator|.
name|randomDirectorService
argument_list|(
name|indexStore
argument_list|)
expr_stmt|;
if|if
condition|(
name|checkIndexOnClose
condition|)
block|{
specifier|final
name|IndicesLifecycle
operator|.
name|Listener
name|listener
init|=
operator|new
name|IndicesLifecycle
operator|.
name|Listener
argument_list|()
block|{
name|boolean
name|canRun
init|=
literal|false
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|beforeIndexShardClosed
parameter_list|(
name|ShardId
name|sid
parameter_list|,
annotation|@
name|Nullable
name|IndexShard
name|indexShard
parameter_list|,
annotation|@
name|IndexSettings
name|Settings
name|indexSettings
parameter_list|)
block|{
if|if
condition|(
name|indexShard
operator|!=
literal|null
operator|&&
name|shardId
operator|.
name|equals
argument_list|(
name|sid
argument_list|)
condition|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"{} shard state before potentially flushing is {}"
argument_list|,
name|indexShard
operator|.
name|shardId
argument_list|()
argument_list|,
name|indexShard
operator|.
name|state
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|validCheckIndexStates
operator|.
name|contains
argument_list|(
name|indexShard
operator|.
name|state
argument_list|()
argument_list|)
operator|&&
name|IndexMetaData
operator|.
name|isOnSharedFilesystem
argument_list|(
name|indexSettings
argument_list|)
operator|==
literal|false
condition|)
block|{
comment|// When the the internal engine closes we do a rollback, which removes uncommitted segments
comment|// By doing a commit flush we perform a Lucene commit, but don't clear the translog,
comment|// so that even in tests where don't flush we can check the integrity of the Lucene index
if|if
condition|(
name|indexShard
operator|.
name|engine
argument_list|()
operator|.
name|hasUncommittedChanges
argument_list|()
condition|)
block|{
comment|// only if we have any changes
name|logger
operator|.
name|info
argument_list|(
literal|"{} flushing in order to run checkindex"
argument_list|,
name|indexShard
operator|.
name|shardId
argument_list|()
argument_list|)
expr_stmt|;
name|Releasables
operator|.
name|close
argument_list|(
name|indexShard
operator|.
name|engine
argument_list|()
operator|.
name|snapshotIndex
argument_list|()
argument_list|)
expr_stmt|;
comment|// Keep translog for tests that rely on replaying it
block|}
name|logger
operator|.
name|info
argument_list|(
literal|"{} flush finished in beforeIndexShardClosed"
argument_list|,
name|indexShard
operator|.
name|shardId
argument_list|()
argument_list|)
expr_stmt|;
name|canRun
operator|=
literal|true
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|afterIndexShardClosed
parameter_list|(
name|ShardId
name|sid
parameter_list|,
annotation|@
name|Nullable
name|IndexShard
name|indexShard
parameter_list|,
annotation|@
name|IndexSettings
name|Settings
name|indexSettings
parameter_list|)
block|{
if|if
condition|(
name|shardId
operator|.
name|equals
argument_list|(
name|sid
argument_list|)
operator|&&
name|indexShard
operator|!=
literal|null
operator|&&
name|canRun
condition|)
block|{
assert|assert
name|indexShard
operator|.
name|state
argument_list|()
operator|==
name|IndexShardState
operator|.
name|CLOSED
operator|:
literal|"Current state must be closed"
assert|;
name|checkIndex
argument_list|(
name|indexShard
operator|.
name|store
argument_list|()
argument_list|,
name|sid
argument_list|)
expr_stmt|;
block|}
name|service
operator|.
name|indicesLifecycle
argument_list|()
operator|.
name|removeListener
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
block|}
decl_stmt|;
name|service
operator|.
name|indicesLifecycle
argument_list|()
operator|.
name|addListener
argument_list|(
name|listener
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|build
specifier|public
name|Directory
index|[]
name|build
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|delegateService
operator|.
name|build
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|newFSDirectory
specifier|protected
specifier|synchronized
name|Directory
name|newFSDirectory
parameter_list|(
name|Path
name|location
parameter_list|,
name|LockFactory
name|lockFactory
parameter_list|)
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
DECL|method|checkIndex
specifier|public
name|void
name|checkIndex
parameter_list|(
name|Store
name|store
parameter_list|,
name|ShardId
name|shardId
parameter_list|)
throws|throws
name|IndexShardException
block|{
if|if
condition|(
name|store
operator|.
name|tryIncRef
argument_list|()
condition|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"start check index"
argument_list|)
expr_stmt|;
try|try
block|{
name|Directory
name|dir
init|=
name|store
operator|.
name|directory
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|Lucene
operator|.
name|indexExists
argument_list|(
name|dir
argument_list|)
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|IndexWriter
operator|.
name|isLocked
argument_list|(
name|dir
argument_list|)
condition|)
block|{
name|ESTestCase
operator|.
name|checkIndexFailed
operator|=
literal|true
expr_stmt|;
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"IndexWriter is still open on shard "
operator|+
name|shardId
argument_list|)
throw|;
block|}
try|try
init|(
name|CheckIndex
name|checkIndex
init|=
operator|new
name|CheckIndex
argument_list|(
name|dir
argument_list|)
init|)
block|{
name|BytesStreamOutput
name|os
init|=
operator|new
name|BytesStreamOutput
argument_list|()
decl_stmt|;
name|PrintStream
name|out
init|=
operator|new
name|PrintStream
argument_list|(
name|os
argument_list|,
literal|false
argument_list|,
name|Charsets
operator|.
name|UTF_8
operator|.
name|name
argument_list|()
argument_list|)
decl_stmt|;
name|checkIndex
operator|.
name|setInfoStream
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|flush
argument_list|()
expr_stmt|;
name|CheckIndex
operator|.
name|Status
name|status
init|=
name|checkIndex
operator|.
name|checkIndex
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|status
operator|.
name|clean
condition|)
block|{
name|ESTestCase
operator|.
name|checkIndexFailed
operator|=
literal|true
expr_stmt|;
name|logger
operator|.
name|warn
argument_list|(
literal|"check index [failure] index files={}\n{}"
argument_list|,
name|Arrays
operator|.
name|toString
argument_list|(
name|dir
operator|.
name|listAll
argument_list|()
argument_list|)
argument_list|,
operator|new
name|String
argument_list|(
name|os
operator|.
name|bytes
argument_list|()
operator|.
name|toBytes
argument_list|()
argument_list|,
name|Charsets
operator|.
name|UTF_8
argument_list|)
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IndexShardException
argument_list|(
name|shardId
argument_list|,
literal|"index check failure"
argument_list|)
throw|;
block|}
else|else
block|{
if|if
condition|(
name|logger
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"check index [success]\n{}"
argument_list|,
operator|new
name|String
argument_list|(
name|os
operator|.
name|bytes
argument_list|()
operator|.
name|toBytes
argument_list|()
argument_list|,
name|Charsets
operator|.
name|UTF_8
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"failed to check index"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"end check index"
argument_list|)
expr_stmt|;
name|store
operator|.
name|decRef
argument_list|()
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
DECL|method|onPause
specifier|public
name|void
name|onPause
parameter_list|(
name|long
name|nanos
parameter_list|)
block|{
name|delegateService
operator|.
name|onPause
argument_list|(
name|nanos
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|rateLimiting
specifier|public
name|StoreRateLimiting
name|rateLimiting
parameter_list|()
block|{
return|return
name|delegateService
operator|.
name|rateLimiting
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|throttleTimeInNanos
specifier|public
name|long
name|throttleTimeInNanos
parameter_list|()
block|{
return|return
name|delegateService
operator|.
name|throttleTimeInNanos
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|newFromDistributor
specifier|public
name|Directory
name|newFromDistributor
parameter_list|(
name|Distributor
name|distributor
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|helper
operator|.
name|wrap
argument_list|(
name|super
operator|.
name|newFromDistributor
argument_list|(
name|distributor
argument_list|)
argument_list|)
return|;
block|}
block|}
end_class

end_unit

