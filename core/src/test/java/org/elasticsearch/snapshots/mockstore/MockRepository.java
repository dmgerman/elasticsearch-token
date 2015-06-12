begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.snapshots.mockstore
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|snapshots
operator|.
name|mockstore
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
name|collect
operator|.
name|ImmutableList
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
name|collect
operator|.
name|ImmutableMap
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
name|ClusterService
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
name|cluster
operator|.
name|metadata
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
name|common
operator|.
name|blobstore
operator|.
name|BlobContainer
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
name|blobstore
operator|.
name|BlobMetaData
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
name|blobstore
operator|.
name|BlobPath
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
name|blobstore
operator|.
name|BlobStore
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
name|index
operator|.
name|snapshots
operator|.
name|IndexShardRepository
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
name|RepositoryName
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
name|RepositorySettings
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
name|fs
operator|.
name|FsRepository
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
name|InputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|OutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|UnsupportedEncodingException
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
name|security
operator|.
name|MessageDigest
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|NoSuchAlgorithmException
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
name|concurrent
operator|.
name|ConcurrentHashMap
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
name|ConcurrentMap
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
name|AtomicLong
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
comment|/**  */
end_comment

begin_class
DECL|class|MockRepository
specifier|public
class|class
name|MockRepository
extends|extends
name|FsRepository
block|{
DECL|field|failureCounter
specifier|private
specifier|final
name|AtomicLong
name|failureCounter
init|=
operator|new
name|AtomicLong
argument_list|()
decl_stmt|;
DECL|method|getFailureCount
specifier|public
name|long
name|getFailureCount
parameter_list|()
block|{
return|return
name|failureCounter
operator|.
name|get
argument_list|()
return|;
block|}
DECL|field|randomControlIOExceptionRate
specifier|private
specifier|final
name|double
name|randomControlIOExceptionRate
decl_stmt|;
DECL|field|randomDataFileIOExceptionRate
specifier|private
specifier|final
name|double
name|randomDataFileIOExceptionRate
decl_stmt|;
DECL|field|waitAfterUnblock
specifier|private
specifier|final
name|long
name|waitAfterUnblock
decl_stmt|;
DECL|field|mockBlobStore
specifier|private
specifier|final
name|MockBlobStore
name|mockBlobStore
decl_stmt|;
DECL|field|randomPrefix
specifier|private
specifier|final
name|String
name|randomPrefix
decl_stmt|;
DECL|field|blockOnInitialization
specifier|private
specifier|volatile
name|boolean
name|blockOnInitialization
decl_stmt|;
DECL|field|blockOnControlFiles
specifier|private
specifier|volatile
name|boolean
name|blockOnControlFiles
decl_stmt|;
DECL|field|blockOnDataFiles
specifier|private
specifier|volatile
name|boolean
name|blockOnDataFiles
decl_stmt|;
DECL|field|blocked
specifier|private
specifier|volatile
name|boolean
name|blocked
init|=
literal|false
decl_stmt|;
annotation|@
name|Inject
DECL|method|MockRepository
specifier|public
name|MockRepository
parameter_list|(
name|RepositoryName
name|name
parameter_list|,
name|RepositorySettings
name|repositorySettings
parameter_list|,
name|IndexShardRepository
name|indexShardRepository
parameter_list|,
name|ClusterService
name|clusterService
parameter_list|,
name|Environment
name|environment
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|name
argument_list|,
name|overrideSettings
argument_list|(
name|repositorySettings
argument_list|,
name|clusterService
argument_list|)
argument_list|,
name|indexShardRepository
argument_list|,
name|environment
argument_list|)
expr_stmt|;
name|randomControlIOExceptionRate
operator|=
name|repositorySettings
operator|.
name|settings
argument_list|()
operator|.
name|getAsDouble
argument_list|(
literal|"random_control_io_exception_rate"
argument_list|,
literal|0.0
argument_list|)
expr_stmt|;
name|randomDataFileIOExceptionRate
operator|=
name|repositorySettings
operator|.
name|settings
argument_list|()
operator|.
name|getAsDouble
argument_list|(
literal|"random_data_file_io_exception_rate"
argument_list|,
literal|0.0
argument_list|)
expr_stmt|;
name|blockOnControlFiles
operator|=
name|repositorySettings
operator|.
name|settings
argument_list|()
operator|.
name|getAsBoolean
argument_list|(
literal|"block_on_control"
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|blockOnDataFiles
operator|=
name|repositorySettings
operator|.
name|settings
argument_list|()
operator|.
name|getAsBoolean
argument_list|(
literal|"block_on_data"
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|blockOnInitialization
operator|=
name|repositorySettings
operator|.
name|settings
argument_list|()
operator|.
name|getAsBoolean
argument_list|(
literal|"block_on_init"
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|randomPrefix
operator|=
name|repositorySettings
operator|.
name|settings
argument_list|()
operator|.
name|get
argument_list|(
literal|"random"
argument_list|,
literal|"default"
argument_list|)
expr_stmt|;
name|waitAfterUnblock
operator|=
name|repositorySettings
operator|.
name|settings
argument_list|()
operator|.
name|getAsLong
argument_list|(
literal|"wait_after_unblock"
argument_list|,
literal|0L
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"starting mock repository with random prefix "
operator|+
name|randomPrefix
argument_list|)
expr_stmt|;
name|mockBlobStore
operator|=
operator|new
name|MockBlobStore
argument_list|(
name|super
operator|.
name|blobStore
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|initializeSnapshot
specifier|public
name|void
name|initializeSnapshot
parameter_list|(
name|SnapshotId
name|snapshotId
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|indices
parameter_list|,
name|MetaData
name|metaData
parameter_list|)
block|{
if|if
condition|(
name|blockOnInitialization
condition|)
block|{
name|blockExecution
argument_list|()
expr_stmt|;
block|}
name|super
operator|.
name|initializeSnapshot
argument_list|(
name|snapshotId
argument_list|,
name|indices
argument_list|,
name|metaData
argument_list|)
expr_stmt|;
block|}
DECL|method|overrideSettings
specifier|private
specifier|static
name|RepositorySettings
name|overrideSettings
parameter_list|(
name|RepositorySettings
name|repositorySettings
parameter_list|,
name|ClusterService
name|clusterService
parameter_list|)
block|{
if|if
condition|(
name|repositorySettings
operator|.
name|settings
argument_list|()
operator|.
name|getAsBoolean
argument_list|(
literal|"localize_location"
argument_list|,
literal|false
argument_list|)
condition|)
block|{
return|return
operator|new
name|RepositorySettings
argument_list|(
name|repositorySettings
operator|.
name|globalSettings
argument_list|()
argument_list|,
name|localizeLocation
argument_list|(
name|repositorySettings
operator|.
name|settings
argument_list|()
argument_list|,
name|clusterService
argument_list|)
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|repositorySettings
return|;
block|}
block|}
DECL|method|localizeLocation
specifier|private
specifier|static
name|Settings
name|localizeLocation
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ClusterService
name|clusterService
parameter_list|)
block|{
name|Path
name|location
init|=
name|PathUtils
operator|.
name|get
argument_list|(
name|settings
operator|.
name|get
argument_list|(
literal|"location"
argument_list|)
argument_list|)
decl_stmt|;
name|location
operator|=
name|location
operator|.
name|resolve
argument_list|(
name|clusterService
operator|.
name|localNode
argument_list|()
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
name|settings
argument_list|)
operator|.
name|put
argument_list|(
literal|"location"
argument_list|,
name|location
operator|.
name|toAbsolutePath
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
DECL|method|addFailure
specifier|private
name|void
name|addFailure
parameter_list|()
block|{
name|failureCounter
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doStop
specifier|protected
name|void
name|doStop
parameter_list|()
block|{
name|unblock
argument_list|()
expr_stmt|;
name|super
operator|.
name|doStop
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|blobStore
specifier|protected
name|BlobStore
name|blobStore
parameter_list|()
block|{
return|return
name|mockBlobStore
return|;
block|}
DECL|method|unblock
specifier|public
name|void
name|unblock
parameter_list|()
block|{
name|unblockExecution
argument_list|()
expr_stmt|;
block|}
DECL|method|blockOnDataFiles
specifier|public
name|void
name|blockOnDataFiles
parameter_list|(
name|boolean
name|blocked
parameter_list|)
block|{
name|blockOnDataFiles
operator|=
name|blocked
expr_stmt|;
block|}
DECL|method|blockOnControlFiles
specifier|public
name|void
name|blockOnControlFiles
parameter_list|(
name|boolean
name|blocked
parameter_list|)
block|{
name|blockOnControlFiles
operator|=
name|blocked
expr_stmt|;
block|}
DECL|method|unblockExecution
specifier|public
specifier|synchronized
name|void
name|unblockExecution
parameter_list|()
block|{
if|if
condition|(
name|blocked
condition|)
block|{
name|blocked
operator|=
literal|false
expr_stmt|;
comment|// Clean blocking flags, so we wouldn't try to block again
name|blockOnDataFiles
operator|=
literal|false
expr_stmt|;
name|blockOnControlFiles
operator|=
literal|false
expr_stmt|;
name|blockOnInitialization
operator|=
literal|false
expr_stmt|;
name|this
operator|.
name|notifyAll
argument_list|()
expr_stmt|;
block|}
block|}
DECL|method|blocked
specifier|public
name|boolean
name|blocked
parameter_list|()
block|{
return|return
name|blocked
return|;
block|}
DECL|method|blockExecution
specifier|private
specifier|synchronized
name|boolean
name|blockExecution
parameter_list|()
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"Blocking execution"
argument_list|)
expr_stmt|;
name|boolean
name|wasBlocked
init|=
literal|false
decl_stmt|;
try|try
block|{
while|while
condition|(
name|blockOnDataFiles
operator|||
name|blockOnControlFiles
operator|||
name|blockOnInitialization
condition|)
block|{
name|blocked
operator|=
literal|true
expr_stmt|;
name|this
operator|.
name|wait
argument_list|()
expr_stmt|;
name|wasBlocked
operator|=
literal|true
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ex
parameter_list|)
block|{
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|interrupt
argument_list|()
expr_stmt|;
block|}
name|logger
operator|.
name|debug
argument_list|(
literal|"Unblocking execution"
argument_list|)
expr_stmt|;
return|return
name|wasBlocked
return|;
block|}
DECL|class|MockBlobStore
specifier|public
class|class
name|MockBlobStore
extends|extends
name|BlobStoreWrapper
block|{
DECL|field|accessCounts
name|ConcurrentMap
argument_list|<
name|String
argument_list|,
name|AtomicLong
argument_list|>
name|accessCounts
init|=
operator|new
name|ConcurrentHashMap
argument_list|<>
argument_list|()
decl_stmt|;
DECL|method|incrementAndGet
specifier|private
name|long
name|incrementAndGet
parameter_list|(
name|String
name|path
parameter_list|)
block|{
name|AtomicLong
name|value
init|=
name|accessCounts
operator|.
name|get
argument_list|(
name|path
argument_list|)
decl_stmt|;
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
name|value
operator|=
name|accessCounts
operator|.
name|putIfAbsent
argument_list|(
name|path
argument_list|,
operator|new
name|AtomicLong
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|value
operator|!=
literal|null
condition|)
block|{
return|return
name|value
operator|.
name|incrementAndGet
argument_list|()
return|;
block|}
return|return
literal|1
return|;
block|}
DECL|method|MockBlobStore
specifier|public
name|MockBlobStore
parameter_list|(
name|BlobStore
name|delegate
parameter_list|)
block|{
name|super
argument_list|(
name|delegate
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|blobContainer
specifier|public
name|BlobContainer
name|blobContainer
parameter_list|(
name|BlobPath
name|path
parameter_list|)
block|{
return|return
operator|new
name|MockBlobContainer
argument_list|(
name|super
operator|.
name|blobContainer
argument_list|(
name|path
argument_list|)
argument_list|)
return|;
block|}
DECL|class|MockBlobContainer
specifier|private
class|class
name|MockBlobContainer
extends|extends
name|BlobContainerWrapper
block|{
DECL|field|digest
specifier|private
name|MessageDigest
name|digest
decl_stmt|;
DECL|method|shouldFail
specifier|private
name|boolean
name|shouldFail
parameter_list|(
name|String
name|blobName
parameter_list|,
name|double
name|probability
parameter_list|)
block|{
if|if
condition|(
name|probability
operator|>
literal|0.0
condition|)
block|{
name|String
name|path
init|=
name|path
argument_list|()
operator|.
name|add
argument_list|(
name|blobName
argument_list|)
operator|.
name|buildAsString
argument_list|(
literal|"/"
argument_list|)
operator|+
literal|"/"
operator|+
name|randomPrefix
decl_stmt|;
name|path
operator|+=
literal|"/"
operator|+
name|incrementAndGet
argument_list|(
name|path
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"checking [{}] [{}]"
argument_list|,
name|path
argument_list|,
name|Math
operator|.
name|abs
argument_list|(
name|hashCode
argument_list|(
name|path
argument_list|)
argument_list|)
operator|<
name|Integer
operator|.
name|MAX_VALUE
operator|*
name|probability
argument_list|)
expr_stmt|;
return|return
name|Math
operator|.
name|abs
argument_list|(
name|hashCode
argument_list|(
name|path
argument_list|)
argument_list|)
operator|<
name|Integer
operator|.
name|MAX_VALUE
operator|*
name|probability
return|;
block|}
else|else
block|{
return|return
literal|false
return|;
block|}
block|}
DECL|method|hashCode
specifier|private
name|int
name|hashCode
parameter_list|(
name|String
name|path
parameter_list|)
block|{
try|try
block|{
name|digest
operator|=
name|MessageDigest
operator|.
name|getInstance
argument_list|(
literal|"MD5"
argument_list|)
expr_stmt|;
name|byte
index|[]
name|bytes
init|=
name|digest
operator|.
name|digest
argument_list|(
name|path
operator|.
name|getBytes
argument_list|(
literal|"UTF-8"
argument_list|)
argument_list|)
decl_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
return|return
operator|(
operator|(
name|bytes
index|[
name|i
operator|++
index|]
operator|&
literal|0xFF
operator|)
operator|<<
literal|24
operator|)
operator||
operator|(
operator|(
name|bytes
index|[
name|i
operator|++
index|]
operator|&
literal|0xFF
operator|)
operator|<<
literal|16
operator|)
operator||
operator|(
operator|(
name|bytes
index|[
name|i
operator|++
index|]
operator|&
literal|0xFF
operator|)
operator|<<
literal|8
operator|)
operator||
operator|(
name|bytes
index|[
name|i
operator|++
index|]
operator|&
literal|0xFF
operator|)
return|;
block|}
catch|catch
parameter_list|(
name|NoSuchAlgorithmException
decl||
name|UnsupportedEncodingException
name|ex
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticsearchException
argument_list|(
literal|"cannot calculate hashcode"
argument_list|,
name|ex
argument_list|)
throw|;
block|}
block|}
DECL|method|maybeIOExceptionOrBlock
specifier|private
name|void
name|maybeIOExceptionOrBlock
parameter_list|(
name|String
name|blobName
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|blobName
operator|.
name|startsWith
argument_list|(
literal|"__"
argument_list|)
condition|)
block|{
if|if
condition|(
name|shouldFail
argument_list|(
name|blobName
argument_list|,
name|randomDataFileIOExceptionRate
argument_list|)
condition|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"throwing random IOException for file [{}] at path [{}]"
argument_list|,
name|blobName
argument_list|,
name|path
argument_list|()
argument_list|)
expr_stmt|;
name|addFailure
argument_list|()
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Random IOException"
argument_list|)
throw|;
block|}
elseif|else
if|if
condition|(
name|blockOnDataFiles
condition|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"blocking I/O operation for file [{}] at path [{}]"
argument_list|,
name|blobName
argument_list|,
name|path
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|blockExecution
argument_list|()
operator|&&
name|waitAfterUnblock
operator|>
literal|0
condition|)
block|{
try|try
block|{
comment|// Delay operation after unblocking
comment|// So, we can start node shutdown while this operation is still running.
name|Thread
operator|.
name|sleep
argument_list|(
name|waitAfterUnblock
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ex
parameter_list|)
block|{
comment|//
block|}
block|}
block|}
block|}
else|else
block|{
if|if
condition|(
name|shouldFail
argument_list|(
name|blobName
argument_list|,
name|randomControlIOExceptionRate
argument_list|)
condition|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"throwing random IOException for file [{}] at path [{}]"
argument_list|,
name|blobName
argument_list|,
name|path
argument_list|()
argument_list|)
expr_stmt|;
name|addFailure
argument_list|()
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Random IOException"
argument_list|)
throw|;
block|}
elseif|else
if|if
condition|(
name|blockOnControlFiles
condition|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"blocking I/O operation for file [{}] at path [{}]"
argument_list|,
name|blobName
argument_list|,
name|path
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|blockExecution
argument_list|()
operator|&&
name|waitAfterUnblock
operator|>
literal|0
condition|)
block|{
try|try
block|{
comment|// Delay operation after unblocking
comment|// So, we can start node shutdown while this operation is still running.
name|Thread
operator|.
name|sleep
argument_list|(
name|waitAfterUnblock
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ex
parameter_list|)
block|{
comment|//
block|}
block|}
block|}
block|}
block|}
DECL|method|MockBlobContainer
specifier|public
name|MockBlobContainer
parameter_list|(
name|BlobContainer
name|delegate
parameter_list|)
block|{
name|super
argument_list|(
name|delegate
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|blobExists
specifier|public
name|boolean
name|blobExists
parameter_list|(
name|String
name|blobName
parameter_list|)
block|{
return|return
name|super
operator|.
name|blobExists
argument_list|(
name|blobName
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|openInput
specifier|public
name|InputStream
name|openInput
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|IOException
block|{
name|maybeIOExceptionOrBlock
argument_list|(
name|name
argument_list|)
expr_stmt|;
return|return
name|super
operator|.
name|openInput
argument_list|(
name|name
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|deleteBlob
specifier|public
name|void
name|deleteBlob
parameter_list|(
name|String
name|blobName
parameter_list|)
throws|throws
name|IOException
block|{
name|maybeIOExceptionOrBlock
argument_list|(
name|blobName
argument_list|)
expr_stmt|;
name|super
operator|.
name|deleteBlob
argument_list|(
name|blobName
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|deleteBlobsByPrefix
specifier|public
name|void
name|deleteBlobsByPrefix
parameter_list|(
name|String
name|blobNamePrefix
parameter_list|)
throws|throws
name|IOException
block|{
name|maybeIOExceptionOrBlock
argument_list|(
name|blobNamePrefix
argument_list|)
expr_stmt|;
name|super
operator|.
name|deleteBlobsByPrefix
argument_list|(
name|blobNamePrefix
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|listBlobs
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|BlobMetaData
argument_list|>
name|listBlobs
parameter_list|()
throws|throws
name|IOException
block|{
name|maybeIOExceptionOrBlock
argument_list|(
literal|""
argument_list|)
expr_stmt|;
return|return
name|super
operator|.
name|listBlobs
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|listBlobsByPrefix
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|BlobMetaData
argument_list|>
name|listBlobsByPrefix
parameter_list|(
name|String
name|blobNamePrefix
parameter_list|)
throws|throws
name|IOException
block|{
name|maybeIOExceptionOrBlock
argument_list|(
name|blobNamePrefix
argument_list|)
expr_stmt|;
return|return
name|super
operator|.
name|listBlobsByPrefix
argument_list|(
name|blobNamePrefix
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|move
specifier|public
name|void
name|move
parameter_list|(
name|String
name|sourceBlob
parameter_list|,
name|String
name|targetBlob
parameter_list|)
throws|throws
name|IOException
block|{
name|maybeIOExceptionOrBlock
argument_list|(
name|targetBlob
argument_list|)
expr_stmt|;
name|super
operator|.
name|move
argument_list|(
name|sourceBlob
argument_list|,
name|targetBlob
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|createOutput
specifier|public
name|OutputStream
name|createOutput
parameter_list|(
name|String
name|blobName
parameter_list|)
throws|throws
name|IOException
block|{
name|maybeIOExceptionOrBlock
argument_list|(
name|blobName
argument_list|)
expr_stmt|;
return|return
name|super
operator|.
name|createOutput
argument_list|(
name|blobName
argument_list|)
return|;
block|}
block|}
block|}
block|}
end_class

end_unit
