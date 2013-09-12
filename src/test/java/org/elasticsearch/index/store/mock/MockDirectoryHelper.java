begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.store.mock
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|store
operator|.
name|mock
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
name|AbstractRandomizedTest
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
name|cache
operator|.
name|memory
operator|.
name|ByteBufferCache
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
name|fs
operator|.
name|MmapFsDirectoryService
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
name|NioFsDirectoryService
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
name|SimpleFsDirectoryService
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
name|memory
operator|.
name|ByteBufferDirectoryService
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
DECL|field|RANDOM_SEED
specifier|public
specifier|static
specifier|final
name|String
name|RANDOM_SEED
init|=
literal|"index.store.mock.random.seed"
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
DECL|field|CHECK_INDEX_ON_CLOSE
specifier|public
specifier|static
specifier|final
name|String
name|CHECK_INDEX_ON_CLOSE
init|=
literal|"index.store.mock.check_index_on_close"
decl_stmt|;
DECL|field|wrappers
specifier|public
specifier|static
specifier|final
name|Set
argument_list|<
name|MockDirectoryWrapper
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
DECL|field|checkIndexOnClose
specifier|private
specifier|final
name|boolean
name|checkIndexOnClose
decl_stmt|;
DECL|field|indexSettings
specifier|private
name|Settings
name|indexSettings
decl_stmt|;
DECL|field|shardId
specifier|private
name|ShardId
name|shardId
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
parameter_list|)
block|{
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
specifier|final
name|Long
name|currentSeed
init|=
name|AbstractRandomizedTest
operator|.
name|getCurrentSeed
argument_list|()
decl_stmt|;
assert|assert
name|currentSeed
operator|!=
literal|null
assert|;
specifier|final
name|long
name|seed
init|=
name|indexSettings
operator|.
name|getAsLong
argument_list|(
name|RANDOM_SEED
argument_list|,
name|currentSeed
argument_list|)
decl_stmt|;
name|random
operator|=
operator|new
name|Random
argument_list|(
name|seed
argument_list|)
expr_stmt|;
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
name|checkIndexOnClose
operator|=
name|indexSettings
operator|.
name|getAsBoolean
argument_list|(
name|CHECK_INDEX_ON_CLOSE
argument_list|,
name|random
operator|.
name|nextDouble
argument_list|()
operator|<
literal|0.1
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
literal|"Using MockDirWrapper with seed [{}] throttle: [{}] checkIndexOnClose: [{}]"
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
name|checkIndexOnClose
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
name|MockDirectoryWrapper
name|w
init|=
operator|new
name|MockDirectoryWrapper
argument_list|(
name|random
argument_list|,
name|dir
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
name|checkIndexOnClose
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
operator|new
name|FilterDirectory
argument_list|(
name|w
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|Directory
name|getDelegate
parameter_list|()
block|{
comment|// TODO we should port this FilterDirectory to Lucene
return|return
name|w
operator|.
name|getDelegate
argument_list|()
return|;
block|}
block|}
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
literal|3
argument_list|)
condition|)
block|{
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
DECL|method|randomRamDirecoryService
specifier|public
name|DirectoryService
name|randomRamDirecoryService
parameter_list|(
name|ByteBufferCache
name|byteBufferCache
parameter_list|)
block|{
switch|switch
condition|(
name|random
operator|.
name|nextInt
argument_list|(
literal|2
argument_list|)
condition|)
block|{
case|case
literal|0
case|:
return|return
operator|new
name|RamDirectoryService
argument_list|(
name|shardId
argument_list|,
name|indexSettings
argument_list|)
return|;
default|default:
return|return
operator|new
name|ByteBufferDirectoryService
argument_list|(
name|shardId
argument_list|,
name|indexSettings
argument_list|,
name|byteBufferCache
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

