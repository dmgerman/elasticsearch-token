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
name|MMapDirectory
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
name|MockDirectoryWrapper
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
name|MockDirectoryWrapper
operator|.
name|Throttling
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
name|Constants
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
name|logging
operator|.
name|ESLogger
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
name|concurrent
operator|.
name|ConcurrentCollections
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
name|store
operator|.
name|DirectoryService
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
name|fs
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
name|ram
operator|.
name|RamDirectoryService
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
name|util
operator|.
name|Random
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

begin_class
DECL|class|MockDirectoryHelper
specifier|public
class|class
name|MockDirectoryHelper
block|{
DECL|field|RANDOM_IO_EXCEPTION_RATE
specifier|public
specifier|static
specifier|final
name|String
name|RANDOM_IO_EXCEPTION_RATE
init|=
literal|"index.store.mock.random.io_exception_rate"
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
DECL|field|RANDOM_THROTTLE
specifier|public
specifier|static
specifier|final
name|String
name|RANDOM_THROTTLE
init|=
literal|"index.store.mock.random.throttle"
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
DECL|field|wrappers
specifier|public
specifier|static
specifier|final
name|Set
argument_list|<
name|ElasticsearchMockDirectoryWrapper
argument_list|>
name|wrappers
init|=
name|ConcurrentCollections
operator|.
name|newConcurrentSet
argument_list|()
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
name|Throttling
name|throttle
decl_stmt|;
DECL|field|indexSettings
specifier|private
specifier|final
name|Settings
name|indexSettings
decl_stmt|;
DECL|field|shardId
specifier|private
specifier|final
name|ShardId
name|shardId
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
DECL|field|logger
specifier|private
specifier|final
name|ESLogger
name|logger
decl_stmt|;
DECL|field|crashIndex
specifier|private
specifier|final
name|boolean
name|crashIndex
decl_stmt|;
DECL|method|MockDirectoryHelper
specifier|public
name|MockDirectoryHelper
parameter_list|(
name|ShardId
name|shardId
parameter_list|,
name|Settings
name|indexSettings
parameter_list|,
name|ESLogger
name|logger
parameter_list|,
name|Random
name|random
parameter_list|,
name|long
name|seed
parameter_list|)
block|{
name|this
operator|.
name|random
operator|=
name|random
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
name|Throttling
operator|.
name|valueOf
argument_list|(
name|indexSettings
operator|.
name|get
argument_list|(
name|RANDOM_THROTTLE
argument_list|,
name|random
operator|.
name|nextDouble
argument_list|()
operator|<
literal|0.1
condition|?
literal|"SOMETIMES"
else|:
literal|"NEVER"
argument_list|)
argument_list|)
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
name|this
operator|.
name|shardId
operator|=
name|shardId
expr_stmt|;
name|this
operator|.
name|logger
operator|=
name|logger
expr_stmt|;
block|}
DECL|method|wrap
specifier|public
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
name|logger
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
name|wrappers
operator|.
name|add
argument_list|(
name|w
argument_list|)
expr_stmt|;
return|return
name|w
return|;
block|}
DECL|method|wrapAllInplace
specifier|public
name|Directory
index|[]
name|wrapAllInplace
parameter_list|(
name|Directory
index|[]
name|dirs
parameter_list|)
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
name|dirs
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|dirs
index|[
name|i
index|]
operator|=
name|wrap
argument_list|(
name|dirs
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
return|return
name|dirs
return|;
block|}
DECL|method|randomDirectorService
specifier|public
name|FsDirectoryService
name|randomDirectorService
parameter_list|(
name|IndexStore
name|indexStore
parameter_list|)
block|{
if|if
condition|(
operator|(
name|Constants
operator|.
name|WINDOWS
operator|||
name|Constants
operator|.
name|SUN_OS
operator|)
operator|&&
name|Constants
operator|.
name|JRE_IS_64BIT
operator|&&
name|MMapDirectory
operator|.
name|UNMAP_SUPPORTED
condition|)
block|{
return|return
operator|new
name|MmapFsDirectoryService
argument_list|(
name|shardId
argument_list|,
name|indexSettings
argument_list|,
name|indexStore
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|Constants
operator|.
name|WINDOWS
condition|)
block|{
return|return
operator|new
name|SimpleFsDirectoryService
argument_list|(
name|shardId
argument_list|,
name|indexSettings
argument_list|,
name|indexStore
argument_list|)
return|;
block|}
switch|switch
condition|(
name|random
operator|.
name|nextInt
argument_list|(
literal|4
argument_list|)
condition|)
block|{
case|case
literal|2
case|:
return|return
operator|new
name|DefaultFsDirectoryService
argument_list|(
name|shardId
argument_list|,
name|indexSettings
argument_list|,
name|indexStore
argument_list|)
return|;
case|case
literal|1
case|:
return|return
operator|new
name|MmapFsDirectoryService
argument_list|(
name|shardId
argument_list|,
name|indexSettings
argument_list|,
name|indexStore
argument_list|)
return|;
case|case
literal|0
case|:
return|return
operator|new
name|SimpleFsDirectoryService
argument_list|(
name|shardId
argument_list|,
name|indexSettings
argument_list|,
name|indexStore
argument_list|)
return|;
default|default:
return|return
operator|new
name|NioFsDirectoryService
argument_list|(
name|shardId
argument_list|,
name|indexSettings
argument_list|,
name|indexStore
argument_list|)
return|;
block|}
block|}
DECL|method|randomRamDirectoryService
specifier|public
name|DirectoryService
name|randomRamDirectoryService
parameter_list|()
block|{
return|return
operator|new
name|RamDirectoryService
argument_list|(
name|shardId
argument_list|,
name|indexSettings
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
DECL|field|logger
specifier|private
specifier|final
name|ESLogger
name|logger
decl_stmt|;
DECL|field|crash
specifier|private
specifier|final
name|boolean
name|crash
decl_stmt|;
DECL|field|closeException
specifier|private
specifier|volatile
name|RuntimeException
name|closeException
decl_stmt|;
DECL|field|lock
specifier|private
specifier|final
name|Object
name|lock
init|=
operator|new
name|Object
argument_list|()
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
name|ESLogger
name|logger
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
name|this
operator|.
name|logger
operator|=
name|logger
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|close
specifier|public
specifier|synchronized
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
try|try
block|{
name|super
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RuntimeException
name|ex
parameter_list|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"MockDirectoryWrapper#close() threw exception"
argument_list|,
name|ex
argument_list|)
expr_stmt|;
name|closeException
operator|=
name|ex
expr_stmt|;
throw|throw
name|ex
throw|;
block|}
finally|finally
block|{
synchronized|synchronized
init|(
name|lock
init|)
block|{
name|lock
operator|.
name|notifyAll
argument_list|()
expr_stmt|;
block|}
block|}
block|}
DECL|method|awaitClosed
specifier|public
name|void
name|awaitClosed
parameter_list|(
name|long
name|timeout
parameter_list|)
throws|throws
name|InterruptedException
block|{
synchronized|synchronized
init|(
name|lock
init|)
block|{
if|if
condition|(
name|isOpen
argument_list|()
condition|)
block|{
name|lock
operator|.
name|wait
argument_list|(
name|timeout
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|successfullyClosed
specifier|public
specifier|synchronized
name|boolean
name|successfullyClosed
parameter_list|()
block|{
return|return
name|closeException
operator|==
literal|null
operator|&&
operator|!
name|isOpen
argument_list|()
return|;
block|}
DECL|method|closeException
specifier|public
specifier|synchronized
name|RuntimeException
name|closeException
parameter_list|()
block|{
return|return
name|closeException
return|;
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
block|}
end_class

end_unit

