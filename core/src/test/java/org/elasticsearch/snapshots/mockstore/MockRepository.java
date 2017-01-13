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
import|import
name|com
operator|.
name|carrotsearch
operator|.
name|randomizedtesting
operator|.
name|RandomizedContext
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
name|CorruptIndexException
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
name|RepositoryMetaData
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
name|Setting
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
name|Setting
operator|.
name|Property
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
name|xcontent
operator|.
name|NamedXContentRegistry
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
name|plugins
operator|.
name|RepositoryPlugin
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
name|Repository
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
name|IndexId
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
name|org
operator|.
name|elasticsearch
operator|.
name|snapshots
operator|.
name|SnapshotId
import|;
end_import

begin_class
DECL|class|MockRepository
specifier|public
class|class
name|MockRepository
extends|extends
name|FsRepository
block|{
DECL|class|Plugin
specifier|public
specifier|static
class|class
name|Plugin
extends|extends
name|org
operator|.
name|elasticsearch
operator|.
name|plugins
operator|.
name|Plugin
implements|implements
name|RepositoryPlugin
block|{
DECL|field|USERNAME_SETTING
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|String
argument_list|>
name|USERNAME_SETTING
init|=
name|Setting
operator|.
name|simpleString
argument_list|(
literal|"secret.mock.username"
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|)
decl_stmt|;
DECL|field|PASSWORD_SETTING
specifier|public
specifier|static
specifier|final
name|Setting
argument_list|<
name|String
argument_list|>
name|PASSWORD_SETTING
init|=
name|Setting
operator|.
name|simpleString
argument_list|(
literal|"secret.mock.password"
argument_list|,
name|Property
operator|.
name|NodeScope
argument_list|,
name|Property
operator|.
name|Filtered
argument_list|)
decl_stmt|;
annotation|@
name|Override
DECL|method|getRepositories
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|Repository
operator|.
name|Factory
argument_list|>
name|getRepositories
parameter_list|(
name|Environment
name|env
parameter_list|,
name|NamedXContentRegistry
name|namedXContentRegistry
parameter_list|)
block|{
return|return
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"mock"
argument_list|,
parameter_list|(
name|metadata
parameter_list|)
lambda|->
operator|new
name|MockRepository
argument_list|(
name|metadata
argument_list|,
name|env
argument_list|,
name|namedXContentRegistry
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|getSettings
specifier|public
name|List
argument_list|<
name|Setting
argument_list|<
name|?
argument_list|>
argument_list|>
name|getSettings
parameter_list|()
block|{
return|return
name|Arrays
operator|.
name|asList
argument_list|(
name|USERNAME_SETTING
argument_list|,
name|PASSWORD_SETTING
argument_list|)
return|;
block|}
block|}
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
DECL|field|useLuceneCorruptionException
specifier|private
specifier|final
name|boolean
name|useLuceneCorruptionException
decl_stmt|;
DECL|field|maximumNumberOfFailures
specifier|private
specifier|final
name|long
name|maximumNumberOfFailures
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
DECL|method|MockRepository
specifier|public
name|MockRepository
parameter_list|(
name|RepositoryMetaData
name|metadata
parameter_list|,
name|Environment
name|environment
parameter_list|,
name|NamedXContentRegistry
name|namedXContentRegistry
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|overrideSettings
argument_list|(
name|metadata
argument_list|,
name|environment
argument_list|)
argument_list|,
name|environment
argument_list|,
name|namedXContentRegistry
argument_list|)
expr_stmt|;
name|randomControlIOExceptionRate
operator|=
name|metadata
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
name|metadata
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
name|useLuceneCorruptionException
operator|=
name|metadata
operator|.
name|settings
argument_list|()
operator|.
name|getAsBoolean
argument_list|(
literal|"use_lucene_corruption"
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|maximumNumberOfFailures
operator|=
name|metadata
operator|.
name|settings
argument_list|()
operator|.
name|getAsLong
argument_list|(
literal|"max_failure_number"
argument_list|,
literal|100L
argument_list|)
expr_stmt|;
name|blockOnControlFiles
operator|=
name|metadata
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
name|metadata
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
name|metadata
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
name|metadata
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
name|metadata
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
literal|"starting mock repository with random prefix {}"
argument_list|,
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
name|IndexId
argument_list|>
name|indices
parameter_list|,
name|MetaData
name|clusterMetadata
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
name|clusterMetadata
argument_list|)
expr_stmt|;
block|}
DECL|method|overrideSettings
specifier|private
specifier|static
name|RepositoryMetaData
name|overrideSettings
parameter_list|(
name|RepositoryMetaData
name|metadata
parameter_list|,
name|Environment
name|environment
parameter_list|)
block|{
comment|// TODO: use another method of testing not being able to read the test file written by the master...
comment|// this is super duper hacky
if|if
condition|(
name|metadata
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
name|Path
name|location
init|=
name|PathUtils
operator|.
name|get
argument_list|(
name|metadata
operator|.
name|settings
argument_list|()
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
name|Integer
operator|.
name|toString
argument_list|(
name|environment
operator|.
name|hashCode
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
return|return
operator|new
name|RepositoryMetaData
argument_list|(
name|metadata
operator|.
name|name
argument_list|()
argument_list|,
name|metadata
operator|.
name|type
argument_list|()
argument_list|,
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|metadata
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
operator|.
name|toAbsolutePath
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|metadata
return|;
block|}
block|}
DECL|method|incrementAndGetFailureCount
specifier|private
name|long
name|incrementAndGetFailureCount
parameter_list|()
block|{
return|return
name|failureCounter
operator|.
name|incrementAndGet
argument_list|()
return|;
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
DECL|method|blockOnDataFiles
specifier|public
name|boolean
name|blockOnDataFiles
parameter_list|()
block|{
return|return
name|blockOnDataFiles
return|;
block|}
DECL|method|unblockExecution
specifier|public
specifier|synchronized
name|void
name|unblockExecution
parameter_list|()
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
argument_list|()
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
operator|&&
operator|(
name|incrementAndGetFailureCount
argument_list|()
operator|<
name|maximumNumberOfFailures
operator|)
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
if|if
condition|(
name|useLuceneCorruptionException
condition|)
block|{
throw|throw
operator|new
name|CorruptIndexException
argument_list|(
literal|"Random corruption"
argument_list|,
literal|"random file"
argument_list|)
throw|;
block|}
else|else
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Random IOException"
argument_list|)
throw|;
block|}
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
comment|// don't block on the index-N files, as getRepositoryData depends on it
elseif|else
if|if
condition|(
name|blobName
operator|.
name|startsWith
argument_list|(
literal|"index-"
argument_list|)
operator|==
literal|false
condition|)
block|{
if|if
condition|(
name|shouldFail
argument_list|(
name|blobName
argument_list|,
name|randomControlIOExceptionRate
argument_list|)
operator|&&
operator|(
name|incrementAndGetFailureCount
argument_list|()
operator|<
name|maximumNumberOfFailures
operator|)
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
DECL|method|readBlob
specifier|public
name|InputStream
name|readBlob
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
name|readBlob
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
if|if
condition|(
name|RandomizedContext
operator|.
name|current
argument_list|()
operator|.
name|getRandom
argument_list|()
operator|.
name|nextBoolean
argument_list|()
condition|)
block|{
comment|// simulate a non-atomic move, since many blob container implementations
comment|// will not have an atomic move, and we should be able to handle that
name|maybeIOExceptionOrBlock
argument_list|(
name|targetBlob
argument_list|)
expr_stmt|;
name|super
operator|.
name|writeBlob
argument_list|(
name|targetBlob
argument_list|,
name|super
operator|.
name|readBlob
argument_list|(
name|sourceBlob
argument_list|)
argument_list|,
literal|0L
argument_list|)
expr_stmt|;
name|super
operator|.
name|deleteBlob
argument_list|(
name|sourceBlob
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// atomic move since this inherits from FsBlobContainer which provides atomic moves
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
block|}
annotation|@
name|Override
DECL|method|writeBlob
specifier|public
name|void
name|writeBlob
parameter_list|(
name|String
name|blobName
parameter_list|,
name|InputStream
name|inputStream
parameter_list|,
name|long
name|blobSize
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
name|writeBlob
argument_list|(
name|blobName
argument_list|,
name|inputStream
argument_list|,
name|blobSize
argument_list|)
expr_stmt|;
if|if
condition|(
name|RandomizedContext
operator|.
name|current
argument_list|()
operator|.
name|getRandom
argument_list|()
operator|.
name|nextBoolean
argument_list|()
condition|)
block|{
comment|// for network based repositories, the blob may have been written but we may still
comment|// get an error with the client connection, so an IOException here simulates this
name|maybeIOExceptionOrBlock
argument_list|(
name|blobName
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
end_class

end_unit

