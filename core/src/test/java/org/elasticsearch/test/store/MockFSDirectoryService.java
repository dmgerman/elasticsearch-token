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
name|carrotsearch
operator|.
name|randomizedtesting
operator|.
name|SeedUtils
import|;
end_import

begin_import
import|import
name|com
operator|.
name|carrotsearch
operator|.
name|randomizedtesting
operator|.
name|generators
operator|.
name|RandomPicks
import|;
end_import

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
name|*
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
name|LuceneTestCase
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
name|TestRuleMarkFailure
import|;
end_import

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
name|*
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
name|FsDirectoryService
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
name|IndexStoreModule
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
name|ElasticsearchTestCase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Assert
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Closeable
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
name|Path
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|*
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
DECL|field|CHECK_INDEX_ON_CLOSE
specifier|public
specifier|static
specifier|final
name|String
name|CHECK_INDEX_ON_CLOSE
init|=
literal|"index.store.mock.check_index_on_close"
decl_stmt|;
DECL|field|RANDOM_IO_EXCEPTION_RATE_ON_OPEN
specifier|public
specifier|static
specifier|final
name|String
name|RANDOM_IO_EXCEPTION_RATE_ON_OPEN
init|=
literal|"index.store.mock.random.io_exception_rate_on_open"
decl_stmt|;
DECL|field|RANDOM_PREVENT_DOUBLE_WRITE
specifier|public
specifier|static
specifier|final
name|String
name|RANDOM_PREVENT_DOUBLE_WRITE
init|=
literal|"index.store.mock.random.prevent_double_write"
decl_stmt|;
DECL|field|RANDOM_NO_DELETE_OPEN_FILE
specifier|public
specifier|static
specifier|final
name|String
name|RANDOM_NO_DELETE_OPEN_FILE
init|=
literal|"index.store.mock.random.no_delete_open_file"
decl_stmt|;
DECL|field|CRASH_INDEX
specifier|public
specifier|static
specifier|final
name|String
name|CRASH_INDEX
init|=
literal|"index.store.mock.random.crash_index"
decl_stmt|;
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
DECL|field|delegateService
specifier|private
specifier|final
name|FsDirectoryService
name|delegateService
decl_stmt|;
DECL|field|checkIndexOnClose
specifier|private
specifier|final
name|boolean
name|checkIndexOnClose
decl_stmt|;
DECL|field|random
specifier|private
specifier|final
name|Random
name|random
decl_stmt|;
DECL|field|randomIOExceptionRate
specifier|private
specifier|final
name|double
name|randomIOExceptionRate
decl_stmt|;
DECL|field|randomIOExceptionRateOnOpen
specifier|private
specifier|final
name|double
name|randomIOExceptionRateOnOpen
decl_stmt|;
DECL|field|throttle
specifier|private
specifier|final
name|MockDirectoryWrapper
operator|.
name|Throttling
name|throttle
decl_stmt|;
DECL|field|indexSettings
specifier|private
specifier|final
name|Settings
name|indexSettings
decl_stmt|;
DECL|field|preventDoubleWrite
specifier|private
specifier|final
name|boolean
name|preventDoubleWrite
decl_stmt|;
DECL|field|noDeleteOpenFile
specifier|private
specifier|final
name|boolean
name|noDeleteOpenFile
decl_stmt|;
DECL|field|crashIndex
specifier|private
specifier|final
name|boolean
name|crashIndex
decl_stmt|;
annotation|@
name|Inject
DECL|method|MockFSDirectoryService
specifier|public
name|MockFSDirectoryService
parameter_list|(
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
parameter_list|,
specifier|final
name|ShardPath
name|path
parameter_list|)
block|{
name|super
argument_list|(
name|indexSettings
argument_list|,
name|indexStore
argument_list|,
name|path
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
name|this
operator|.
name|random
operator|=
operator|new
name|Random
argument_list|(
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
name|randomIOExceptionRate
operator|=
name|indexSettings
operator|.
name|getAsDouble
argument_list|(
name|RANDOM_IO_EXCEPTION_RATE
argument_list|,
literal|0.0d
argument_list|)
expr_stmt|;
name|randomIOExceptionRateOnOpen
operator|=
name|indexSettings
operator|.
name|getAsDouble
argument_list|(
name|RANDOM_IO_EXCEPTION_RATE_ON_OPEN
argument_list|,
literal|0.0d
argument_list|)
expr_stmt|;
name|preventDoubleWrite
operator|=
name|indexSettings
operator|.
name|getAsBoolean
argument_list|(
name|RANDOM_PREVENT_DOUBLE_WRITE
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|// true is default in MDW
name|noDeleteOpenFile
operator|=
name|indexSettings
operator|.
name|getAsBoolean
argument_list|(
name|RANDOM_NO_DELETE_OPEN_FILE
argument_list|,
name|random
operator|.
name|nextBoolean
argument_list|()
argument_list|)
expr_stmt|;
comment|// true is default in MDW
name|random
operator|.
name|nextInt
argument_list|(
name|shardId
operator|.
name|getId
argument_list|()
operator|+
literal|1
argument_list|)
expr_stmt|;
comment|// some randomness per shard
name|throttle
operator|=
name|MockDirectoryWrapper
operator|.
name|Throttling
operator|.
name|NEVER
expr_stmt|;
name|crashIndex
operator|=
name|indexSettings
operator|.
name|getAsBoolean
argument_list|(
name|CRASH_INDEX
argument_list|,
literal|true
argument_list|)
expr_stmt|;
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
literal|"Using MockDirWrapper with seed [{}] throttle: [{}] crashIndex: [{}]"
argument_list|,
name|SeedUtils
operator|.
name|formatSeed
argument_list|(
name|seed
argument_list|)
argument_list|,
name|throttle
argument_list|,
name|crashIndex
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|indexSettings
operator|=
name|indexSettings
expr_stmt|;
name|delegateService
operator|=
name|randomDirectorService
argument_list|(
name|indexStore
argument_list|,
name|path
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
DECL|method|newDirectory
specifier|public
name|Directory
name|newDirectory
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|wrap
argument_list|(
name|delegateService
operator|.
name|newDirectory
argument_list|()
argument_list|)
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
name|ElasticsearchTestCase
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
name|ElasticsearchTestCase
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
DECL|field|RANDOM_IO_EXCEPTION_RATE
specifier|public
specifier|static
specifier|final
name|String
name|RANDOM_IO_EXCEPTION_RATE
init|=
literal|"index.store.mock.random.io_exception_rate"
decl_stmt|;
DECL|method|wrap
specifier|private
name|Directory
name|wrap
parameter_list|(
name|Directory
name|dir
parameter_list|)
block|{
specifier|final
name|ElasticsearchMockDirectoryWrapper
name|w
init|=
operator|new
name|ElasticsearchMockDirectoryWrapper
argument_list|(
name|random
argument_list|,
name|dir
argument_list|,
name|this
operator|.
name|crashIndex
argument_list|)
decl_stmt|;
name|w
operator|.
name|setRandomIOExceptionRate
argument_list|(
name|randomIOExceptionRate
argument_list|)
expr_stmt|;
name|w
operator|.
name|setRandomIOExceptionRateOnOpen
argument_list|(
name|randomIOExceptionRateOnOpen
argument_list|)
expr_stmt|;
name|w
operator|.
name|setThrottling
argument_list|(
name|throttle
argument_list|)
expr_stmt|;
name|w
operator|.
name|setCheckIndexOnClose
argument_list|(
literal|false
argument_list|)
expr_stmt|;
comment|// we do this on the index level
name|w
operator|.
name|setPreventDoubleWrite
argument_list|(
name|preventDoubleWrite
argument_list|)
expr_stmt|;
comment|// TODO: make this test robust to virus scanner
name|w
operator|.
name|setEnableVirusScanner
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|w
operator|.
name|setNoDeleteOpenFile
argument_list|(
name|noDeleteOpenFile
argument_list|)
expr_stmt|;
name|w
operator|.
name|setUseSlowOpenClosers
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|LuceneTestCase
operator|.
name|closeAfterSuite
argument_list|(
operator|new
name|CloseableDirectory
argument_list|(
name|w
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|w
return|;
block|}
DECL|method|randomDirectorService
specifier|private
name|FsDirectoryService
name|randomDirectorService
parameter_list|(
name|IndexStore
name|indexStore
parameter_list|,
name|ShardPath
name|path
parameter_list|)
block|{
name|Settings
operator|.
name|Builder
name|builder
init|=
name|Settings
operator|.
name|settingsBuilder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|put
argument_list|(
name|indexSettings
argument_list|)
expr_stmt|;
name|builder
operator|.
name|put
argument_list|(
name|IndexStoreModule
operator|.
name|STORE_TYPE
argument_list|,
name|RandomPicks
operator|.
name|randomFrom
argument_list|(
name|random
argument_list|,
name|IndexStoreModule
operator|.
name|Type
operator|.
name|values
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
return|return
operator|new
name|FsDirectoryService
argument_list|(
name|builder
operator|.
name|build
argument_list|()
argument_list|,
name|indexStore
argument_list|,
name|path
argument_list|)
return|;
block|}
DECL|class|ElasticsearchMockDirectoryWrapper
specifier|public
specifier|static
specifier|final
class|class
name|ElasticsearchMockDirectoryWrapper
extends|extends
name|MockDirectoryWrapper
block|{
DECL|field|crash
specifier|private
specifier|final
name|boolean
name|crash
decl_stmt|;
DECL|field|superUnSyncedFiles
specifier|private
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|superUnSyncedFiles
decl_stmt|;
DECL|field|superRandomState
specifier|private
specifier|final
name|Random
name|superRandomState
decl_stmt|;
DECL|method|ElasticsearchMockDirectoryWrapper
specifier|public
name|ElasticsearchMockDirectoryWrapper
parameter_list|(
name|Random
name|random
parameter_list|,
name|Directory
name|delegate
parameter_list|,
name|boolean
name|crash
parameter_list|)
block|{
name|super
argument_list|(
name|random
argument_list|,
name|delegate
argument_list|)
expr_stmt|;
name|this
operator|.
name|crash
operator|=
name|crash
expr_stmt|;
comment|// TODO: remove all this and cutover to MockFS (DisableFsyncFS) instead
try|try
block|{
name|Field
name|field
init|=
name|MockDirectoryWrapper
operator|.
name|class
operator|.
name|getDeclaredField
argument_list|(
literal|"unSyncedFiles"
argument_list|)
decl_stmt|;
name|field
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|superUnSyncedFiles
operator|=
operator|(
name|Set
argument_list|<
name|String
argument_list|>
operator|)
name|field
operator|.
name|get
argument_list|(
name|this
argument_list|)
expr_stmt|;
name|field
operator|=
name|MockDirectoryWrapper
operator|.
name|class
operator|.
name|getDeclaredField
argument_list|(
literal|"randomState"
argument_list|)
expr_stmt|;
name|field
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|superRandomState
operator|=
operator|(
name|Random
operator|)
name|field
operator|.
name|get
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ReflectiveOperationException
name|roe
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|roe
argument_list|)
throw|;
block|}
block|}
comment|/**          * Returns true if {@link #in} must sync its files.          * Currently, only {@link org.apache.lucene.store.NRTCachingDirectory} requires sync'ing its files          * because otherwise they are cached in an internal {@link org.apache.lucene.store.RAMDirectory}. If          * other directories require that too, they should be added to this method.          */
DECL|method|mustSync
specifier|private
name|boolean
name|mustSync
parameter_list|()
block|{
name|Directory
name|delegate
init|=
name|in
decl_stmt|;
while|while
condition|(
name|delegate
operator|instanceof
name|FilterDirectory
condition|)
block|{
if|if
condition|(
name|delegate
operator|instanceof
name|NRTCachingDirectory
condition|)
block|{
return|return
literal|true
return|;
block|}
name|delegate
operator|=
operator|(
operator|(
name|FilterDirectory
operator|)
name|delegate
operator|)
operator|.
name|getDelegate
argument_list|()
expr_stmt|;
block|}
return|return
name|delegate
operator|instanceof
name|NRTCachingDirectory
return|;
block|}
annotation|@
name|Override
DECL|method|sync
specifier|public
specifier|synchronized
name|void
name|sync
parameter_list|(
name|Collection
argument_list|<
name|String
argument_list|>
name|names
parameter_list|)
throws|throws
name|IOException
block|{
comment|// don't wear out our hardware so much in tests.
if|if
condition|(
name|superRandomState
operator|.
name|nextInt
argument_list|(
literal|100
argument_list|)
operator|==
literal|0
operator|||
name|mustSync
argument_list|()
condition|)
block|{
name|super
operator|.
name|sync
argument_list|(
name|names
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|superUnSyncedFiles
operator|.
name|removeAll
argument_list|(
name|names
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|crash
specifier|public
specifier|synchronized
name|void
name|crash
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|crash
condition|)
block|{
name|super
operator|.
name|crash
argument_list|()
expr_stmt|;
block|}
block|}
block|}
DECL|class|CloseableDirectory
specifier|final
class|class
name|CloseableDirectory
implements|implements
name|Closeable
block|{
DECL|field|dir
specifier|private
specifier|final
name|BaseDirectoryWrapper
name|dir
decl_stmt|;
DECL|field|failureMarker
specifier|private
specifier|final
name|TestRuleMarkFailure
name|failureMarker
decl_stmt|;
DECL|method|CloseableDirectory
specifier|public
name|CloseableDirectory
parameter_list|(
name|BaseDirectoryWrapper
name|dir
parameter_list|)
block|{
name|this
operator|.
name|dir
operator|=
name|dir
expr_stmt|;
try|try
block|{
specifier|final
name|Field
name|suiteFailureMarker
init|=
name|LuceneTestCase
operator|.
name|class
operator|.
name|getDeclaredField
argument_list|(
literal|"suiteFailureMarker"
argument_list|)
decl_stmt|;
name|suiteFailureMarker
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|this
operator|.
name|failureMarker
operator|=
operator|(
name|TestRuleMarkFailure
operator|)
name|suiteFailureMarker
operator|.
name|get
argument_list|(
name|LuceneTestCase
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticsearchException
argument_list|(
literal|"foo"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
block|{
comment|// We only attempt to check open/closed state if there were no other test
comment|// failures.
try|try
block|{
if|if
condition|(
name|failureMarker
operator|.
name|wasSuccessful
argument_list|()
operator|&&
name|dir
operator|.
name|isOpen
argument_list|()
condition|)
block|{
name|Assert
operator|.
name|fail
argument_list|(
literal|"Directory not closed: "
operator|+
name|dir
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
comment|// TODO: perform real close of the delegate: LUCENE-4058
comment|// dir.close();
block|}
block|}
block|}
block|}
end_class

end_unit
