begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.shard.recovery
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|shard
operator|.
name|recovery
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
name|IndexInput
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticSearchException
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
name|StopWatch
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
name|collect
operator|.
name|Lists
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
name|collect
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
name|component
operator|.
name|AbstractComponent
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
name|unit
operator|.
name|ByteSizeUnit
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
name|unit
operator|.
name|ByteSizeValue
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
name|engine
operator|.
name|Engine
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
name|IllegalIndexShardStateException
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
name|IndexShardClosedException
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
name|service
operator|.
name|InternalIndexShard
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
name|StoreFileMetaData
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
name|translog
operator|.
name|Translog
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
name|threadpool
operator|.
name|ThreadPool
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|transport
operator|.
name|*
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
name|AtomicReference
import|;
end_import

begin_comment
comment|/**  * The source recovery accepts recovery requests from other peer shards and start the recovery process from this  * source shard to the target shard.  *  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|RecoverySource
specifier|public
class|class
name|RecoverySource
extends|extends
name|AbstractComponent
block|{
DECL|class|Actions
specifier|public
specifier|static
class|class
name|Actions
block|{
DECL|field|START_RECOVERY
specifier|public
specifier|static
specifier|final
name|String
name|START_RECOVERY
init|=
literal|"index/shard/recovery/startRecovery"
decl_stmt|;
block|}
DECL|field|threadPool
specifier|private
specifier|final
name|ThreadPool
name|threadPool
decl_stmt|;
DECL|field|transportService
specifier|private
specifier|final
name|TransportService
name|transportService
decl_stmt|;
DECL|field|indicesService
specifier|private
specifier|final
name|IndicesService
name|indicesService
decl_stmt|;
DECL|field|fileChunkSize
specifier|private
specifier|final
name|ByteSizeValue
name|fileChunkSize
decl_stmt|;
DECL|field|compress
specifier|private
specifier|final
name|boolean
name|compress
decl_stmt|;
DECL|field|translogBatchSize
specifier|private
specifier|final
name|int
name|translogBatchSize
decl_stmt|;
DECL|method|RecoverySource
annotation|@
name|Inject
specifier|public
name|RecoverySource
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|,
name|TransportService
name|transportService
parameter_list|,
name|IndicesService
name|indicesService
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|threadPool
operator|=
name|threadPool
expr_stmt|;
name|this
operator|.
name|transportService
operator|=
name|transportService
expr_stmt|;
name|this
operator|.
name|indicesService
operator|=
name|indicesService
expr_stmt|;
name|this
operator|.
name|fileChunkSize
operator|=
name|componentSettings
operator|.
name|getAsBytesSize
argument_list|(
literal|"file_chunk_size"
argument_list|,
operator|new
name|ByteSizeValue
argument_list|(
literal|100
argument_list|,
name|ByteSizeUnit
operator|.
name|KB
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|translogBatchSize
operator|=
name|componentSettings
operator|.
name|getAsInt
argument_list|(
literal|"translog_batch_size"
argument_list|,
literal|100
argument_list|)
expr_stmt|;
name|this
operator|.
name|compress
operator|=
name|componentSettings
operator|.
name|getAsBoolean
argument_list|(
literal|"compress"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|transportService
operator|.
name|registerHandler
argument_list|(
name|Actions
operator|.
name|START_RECOVERY
argument_list|,
operator|new
name|StartRecoveryTransportRequestHandler
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|recover
specifier|private
name|RecoveryResponse
name|recover
parameter_list|(
specifier|final
name|StartRecoveryRequest
name|request
parameter_list|)
block|{
specifier|final
name|InternalIndexShard
name|shard
init|=
operator|(
name|InternalIndexShard
operator|)
name|indicesService
operator|.
name|indexServiceSafe
argument_list|(
name|request
operator|.
name|shardId
argument_list|()
operator|.
name|index
argument_list|()
operator|.
name|name
argument_list|()
argument_list|)
operator|.
name|shardSafe
argument_list|(
name|request
operator|.
name|shardId
argument_list|()
operator|.
name|id
argument_list|()
argument_list|)
decl_stmt|;
name|logger
operator|.
name|trace
argument_list|(
literal|"starting recovery to {}, mark_as_relocated {}"
argument_list|,
name|request
operator|.
name|targetNode
argument_list|()
argument_list|,
name|request
operator|.
name|markAsRelocated
argument_list|()
argument_list|)
expr_stmt|;
specifier|final
name|RecoveryResponse
name|response
init|=
operator|new
name|RecoveryResponse
argument_list|()
decl_stmt|;
name|shard
operator|.
name|recover
argument_list|(
operator|new
name|Engine
operator|.
name|RecoveryHandler
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|phase1
parameter_list|(
specifier|final
name|SnapshotIndexCommit
name|snapshot
parameter_list|)
throws|throws
name|ElasticSearchException
block|{
name|long
name|totalSize
init|=
literal|0
decl_stmt|;
name|long
name|existingTotalSize
init|=
literal|0
decl_stmt|;
try|try
block|{
name|StopWatch
name|stopWatch
init|=
operator|new
name|StopWatch
argument_list|()
operator|.
name|start
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|name
range|:
name|snapshot
operator|.
name|getFiles
argument_list|()
control|)
block|{
name|StoreFileMetaData
name|md
init|=
name|shard
operator|.
name|store
argument_list|()
operator|.
name|metaDataWithMd5
argument_list|(
name|name
argument_list|)
decl_stmt|;
name|boolean
name|useExisting
init|=
literal|false
decl_stmt|;
if|if
condition|(
name|request
operator|.
name|existingFiles
operator|.
name|containsKey
argument_list|(
name|name
argument_list|)
condition|)
block|{
if|if
condition|(
name|md
operator|.
name|md5
argument_list|()
operator|.
name|equals
argument_list|(
name|request
operator|.
name|existingFiles
operator|.
name|get
argument_list|(
name|name
argument_list|)
operator|.
name|md5
argument_list|()
argument_list|)
condition|)
block|{
name|response
operator|.
name|phase1ExistingFileNames
operator|.
name|add
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|response
operator|.
name|phase1ExistingFileSizes
operator|.
name|add
argument_list|(
name|md
operator|.
name|sizeInBytes
argument_list|()
argument_list|)
expr_stmt|;
name|existingTotalSize
operator|+=
name|md
operator|.
name|sizeInBytes
argument_list|()
expr_stmt|;
name|useExisting
operator|=
literal|true
expr_stmt|;
if|if
condition|(
name|logger
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"[{}][{}] recovery [phase1] to {}: not recovering [{}], exists in local store and has md5 [{}]"
argument_list|,
name|request
operator|.
name|shardId
argument_list|()
operator|.
name|index
argument_list|()
operator|.
name|name
argument_list|()
argument_list|,
name|request
operator|.
name|shardId
argument_list|()
operator|.
name|id
argument_list|()
argument_list|,
name|request
operator|.
name|targetNode
argument_list|()
argument_list|,
name|name
argument_list|,
name|md
operator|.
name|md5
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
operator|!
name|useExisting
condition|)
block|{
if|if
condition|(
name|request
operator|.
name|existingFiles
operator|.
name|containsKey
argument_list|(
name|name
argument_list|)
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"[{}][{}] recovery [phase1] to {}: recovering [{}], exists in local store, but has different md5: remote [{}], local [{}]"
argument_list|,
name|request
operator|.
name|shardId
argument_list|()
operator|.
name|index
argument_list|()
operator|.
name|name
argument_list|()
argument_list|,
name|request
operator|.
name|shardId
argument_list|()
operator|.
name|id
argument_list|()
argument_list|,
name|request
operator|.
name|targetNode
argument_list|()
argument_list|,
name|name
argument_list|,
name|request
operator|.
name|existingFiles
operator|.
name|get
argument_list|(
name|name
argument_list|)
operator|.
name|md5
argument_list|()
argument_list|,
name|md
operator|.
name|md5
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"[{}][{}] recovery [phase1] to {}: recovering [{}], does not exists in remote"
argument_list|,
name|request
operator|.
name|shardId
argument_list|()
operator|.
name|index
argument_list|()
operator|.
name|name
argument_list|()
argument_list|,
name|request
operator|.
name|shardId
argument_list|()
operator|.
name|id
argument_list|()
argument_list|,
name|request
operator|.
name|targetNode
argument_list|()
argument_list|,
name|name
argument_list|)
expr_stmt|;
block|}
name|response
operator|.
name|phase1FileNames
operator|.
name|add
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|response
operator|.
name|phase1FileSizes
operator|.
name|add
argument_list|(
name|md
operator|.
name|sizeInBytes
argument_list|()
argument_list|)
expr_stmt|;
name|totalSize
operator|+=
name|md
operator|.
name|sizeInBytes
argument_list|()
expr_stmt|;
block|}
block|}
name|response
operator|.
name|phase1TotalSize
operator|=
name|totalSize
expr_stmt|;
name|response
operator|.
name|phase1ExistingTotalSize
operator|=
name|existingTotalSize
expr_stmt|;
name|logger
operator|.
name|trace
argument_list|(
literal|"[{}][{}] recovery [phase1] to {}: recovering_files [{}] with total_size [{}], reusing_files [{}] with total_size [{}]"
argument_list|,
name|request
operator|.
name|shardId
argument_list|()
operator|.
name|index
argument_list|()
operator|.
name|name
argument_list|()
argument_list|,
name|request
operator|.
name|shardId
argument_list|()
operator|.
name|id
argument_list|()
argument_list|,
name|request
operator|.
name|targetNode
argument_list|()
argument_list|,
name|response
operator|.
name|phase1FileNames
operator|.
name|size
argument_list|()
argument_list|,
operator|new
name|ByteSizeValue
argument_list|(
name|totalSize
argument_list|)
argument_list|,
name|response
operator|.
name|phase1ExistingFileNames
operator|.
name|size
argument_list|()
argument_list|,
operator|new
name|ByteSizeValue
argument_list|(
name|existingTotalSize
argument_list|)
argument_list|)
expr_stmt|;
name|RecoveryFilesInfoRequest
name|recoveryInfoFilesRequest
init|=
operator|new
name|RecoveryFilesInfoRequest
argument_list|(
name|request
operator|.
name|shardId
argument_list|()
argument_list|,
name|response
operator|.
name|phase1FileNames
argument_list|,
name|response
operator|.
name|phase1FileSizes
argument_list|,
name|response
operator|.
name|phase1ExistingFileNames
argument_list|,
name|response
operator|.
name|phase1ExistingFileSizes
argument_list|,
name|response
operator|.
name|phase1TotalSize
argument_list|,
name|response
operator|.
name|phase1ExistingTotalSize
argument_list|)
decl_stmt|;
name|transportService
operator|.
name|submitRequest
argument_list|(
name|request
operator|.
name|targetNode
argument_list|()
argument_list|,
name|RecoveryTarget
operator|.
name|Actions
operator|.
name|FILES_INFO
argument_list|,
name|recoveryInfoFilesRequest
argument_list|,
name|VoidTransportResponseHandler
operator|.
name|INSTANCE
argument_list|)
operator|.
name|txGet
argument_list|()
expr_stmt|;
specifier|final
name|CountDownLatch
name|latch
init|=
operator|new
name|CountDownLatch
argument_list|(
name|response
operator|.
name|phase1FileNames
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|AtomicReference
argument_list|<
name|Exception
argument_list|>
name|lastException
init|=
operator|new
name|AtomicReference
argument_list|<
name|Exception
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
specifier|final
name|String
name|name
range|:
name|response
operator|.
name|phase1FileNames
control|)
block|{
name|threadPool
operator|.
name|cached
argument_list|()
operator|.
name|execute
argument_list|(
operator|new
name|Runnable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|IndexInput
name|indexInput
init|=
literal|null
decl_stmt|;
try|try
block|{
specifier|final
name|int
name|BUFFER_SIZE
init|=
operator|(
name|int
operator|)
name|fileChunkSize
operator|.
name|bytes
argument_list|()
decl_stmt|;
name|byte
index|[]
name|buf
init|=
operator|new
name|byte
index|[
name|BUFFER_SIZE
index|]
decl_stmt|;
name|indexInput
operator|=
name|snapshot
operator|.
name|getDirectory
argument_list|()
operator|.
name|openInput
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|long
name|len
init|=
name|indexInput
operator|.
name|length
argument_list|()
decl_stmt|;
name|long
name|readCount
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|readCount
operator|<
name|len
condition|)
block|{
if|if
condition|(
name|shard
operator|.
name|state
argument_list|()
operator|==
name|IndexShardState
operator|.
name|CLOSED
condition|)
block|{
comment|// check if the shard got closed on us
throw|throw
operator|new
name|IndexShardClosedException
argument_list|(
name|shard
operator|.
name|shardId
argument_list|()
argument_list|)
throw|;
block|}
name|int
name|toRead
init|=
name|readCount
operator|+
name|BUFFER_SIZE
operator|>
name|len
condition|?
call|(
name|int
call|)
argument_list|(
name|len
operator|-
name|readCount
argument_list|)
else|:
name|BUFFER_SIZE
decl_stmt|;
name|long
name|position
init|=
name|indexInput
operator|.
name|getFilePointer
argument_list|()
decl_stmt|;
name|indexInput
operator|.
name|readBytes
argument_list|(
name|buf
argument_list|,
literal|0
argument_list|,
name|toRead
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|transportService
operator|.
name|submitRequest
argument_list|(
name|request
operator|.
name|targetNode
argument_list|()
argument_list|,
name|RecoveryTarget
operator|.
name|Actions
operator|.
name|FILE_CHUNK
argument_list|,
operator|new
name|RecoveryFileChunkRequest
argument_list|(
name|request
operator|.
name|shardId
argument_list|()
argument_list|,
name|name
argument_list|,
name|position
argument_list|,
name|len
argument_list|,
name|buf
argument_list|,
name|toRead
argument_list|)
argument_list|,
name|TransportRequestOptions
operator|.
name|options
argument_list|()
operator|.
name|withCompress
argument_list|(
name|compress
argument_list|)
argument_list|,
name|VoidTransportResponseHandler
operator|.
name|INSTANCE
argument_list|)
operator|.
name|txGet
argument_list|()
expr_stmt|;
name|readCount
operator|+=
name|toRead
expr_stmt|;
block|}
name|indexInput
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|lastException
operator|.
name|set
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|indexInput
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|indexInput
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// ignore
block|}
block|}
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
name|latch
operator|.
name|await
argument_list|()
expr_stmt|;
if|if
condition|(
name|lastException
operator|.
name|get
argument_list|()
operator|!=
literal|null
condition|)
block|{
throw|throw
name|lastException
operator|.
name|get
argument_list|()
throw|;
block|}
comment|// now, set the clean files request
name|Set
argument_list|<
name|String
argument_list|>
name|snapshotFiles
init|=
name|Sets
operator|.
name|newHashSet
argument_list|(
name|snapshot
operator|.
name|getFiles
argument_list|()
argument_list|)
decl_stmt|;
name|transportService
operator|.
name|submitRequest
argument_list|(
name|request
operator|.
name|targetNode
argument_list|()
argument_list|,
name|RecoveryTarget
operator|.
name|Actions
operator|.
name|CLEAN_FILES
argument_list|,
operator|new
name|RecoveryCleanFilesRequest
argument_list|(
name|shard
operator|.
name|shardId
argument_list|()
argument_list|,
name|snapshotFiles
argument_list|)
argument_list|,
name|VoidTransportResponseHandler
operator|.
name|INSTANCE
argument_list|)
operator|.
name|txGet
argument_list|()
expr_stmt|;
name|stopWatch
operator|.
name|stop
argument_list|()
expr_stmt|;
name|logger
operator|.
name|trace
argument_list|(
literal|"[{}][{}] recovery [phase1] to {}: took [{}]"
argument_list|,
name|request
operator|.
name|shardId
argument_list|()
operator|.
name|index
argument_list|()
operator|.
name|name
argument_list|()
argument_list|,
name|request
operator|.
name|shardId
argument_list|()
operator|.
name|id
argument_list|()
argument_list|,
name|request
operator|.
name|targetNode
argument_list|()
argument_list|,
name|stopWatch
operator|.
name|totalTime
argument_list|()
argument_list|)
expr_stmt|;
name|response
operator|.
name|phase1Time
operator|=
name|stopWatch
operator|.
name|totalTime
argument_list|()
operator|.
name|millis
argument_list|()
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
name|RecoverFilesRecoveryException
argument_list|(
name|request
operator|.
name|shardId
argument_list|()
argument_list|,
name|response
operator|.
name|phase1FileNames
operator|.
name|size
argument_list|()
argument_list|,
operator|new
name|ByteSizeValue
argument_list|(
name|totalSize
argument_list|)
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|phase2
parameter_list|(
name|Translog
operator|.
name|Snapshot
name|snapshot
parameter_list|)
throws|throws
name|ElasticSearchException
block|{
if|if
condition|(
name|shard
operator|.
name|state
argument_list|()
operator|==
name|IndexShardState
operator|.
name|CLOSED
condition|)
block|{
throw|throw
operator|new
name|IndexShardClosedException
argument_list|(
name|request
operator|.
name|shardId
argument_list|()
argument_list|)
throw|;
block|}
name|logger
operator|.
name|trace
argument_list|(
literal|"[{}][{}] recovery [phase2] to {}: sending transaction log operations"
argument_list|,
name|request
operator|.
name|shardId
argument_list|()
operator|.
name|index
argument_list|()
operator|.
name|name
argument_list|()
argument_list|,
name|request
operator|.
name|shardId
argument_list|()
operator|.
name|id
argument_list|()
argument_list|,
name|request
operator|.
name|targetNode
argument_list|()
argument_list|)
expr_stmt|;
name|StopWatch
name|stopWatch
init|=
operator|new
name|StopWatch
argument_list|()
operator|.
name|start
argument_list|()
decl_stmt|;
name|transportService
operator|.
name|submitRequest
argument_list|(
name|request
operator|.
name|targetNode
argument_list|()
argument_list|,
name|RecoveryTarget
operator|.
name|Actions
operator|.
name|PREPARE_TRANSLOG
argument_list|,
operator|new
name|RecoveryPrepareForTranslogOperationsRequest
argument_list|(
name|request
operator|.
name|shardId
argument_list|()
argument_list|)
argument_list|,
name|VoidTransportResponseHandler
operator|.
name|INSTANCE
argument_list|)
operator|.
name|txGet
argument_list|()
expr_stmt|;
name|int
name|totalOperations
init|=
name|sendSnapshot
argument_list|(
name|snapshot
argument_list|)
decl_stmt|;
name|stopWatch
operator|.
name|stop
argument_list|()
expr_stmt|;
name|logger
operator|.
name|trace
argument_list|(
literal|"[{}][{}] recovery [phase2] to {}: took [{}]"
argument_list|,
name|request
operator|.
name|shardId
argument_list|()
operator|.
name|index
argument_list|()
operator|.
name|name
argument_list|()
argument_list|,
name|request
operator|.
name|shardId
argument_list|()
operator|.
name|id
argument_list|()
argument_list|,
name|request
operator|.
name|targetNode
argument_list|()
argument_list|,
name|stopWatch
operator|.
name|totalTime
argument_list|()
argument_list|)
expr_stmt|;
name|response
operator|.
name|phase2Time
operator|=
name|stopWatch
operator|.
name|totalTime
argument_list|()
operator|.
name|millis
argument_list|()
expr_stmt|;
name|response
operator|.
name|phase2Operations
operator|=
name|totalOperations
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|phase3
parameter_list|(
name|Translog
operator|.
name|Snapshot
name|snapshot
parameter_list|)
throws|throws
name|ElasticSearchException
block|{
if|if
condition|(
name|shard
operator|.
name|state
argument_list|()
operator|==
name|IndexShardState
operator|.
name|CLOSED
condition|)
block|{
throw|throw
operator|new
name|IndexShardClosedException
argument_list|(
name|request
operator|.
name|shardId
argument_list|()
argument_list|)
throw|;
block|}
name|logger
operator|.
name|trace
argument_list|(
literal|"[{}][{}] recovery [phase3] to {}: sending transaction log operations"
argument_list|,
name|request
operator|.
name|shardId
argument_list|()
operator|.
name|index
argument_list|()
operator|.
name|name
argument_list|()
argument_list|,
name|request
operator|.
name|shardId
argument_list|()
operator|.
name|id
argument_list|()
argument_list|,
name|request
operator|.
name|targetNode
argument_list|()
argument_list|)
expr_stmt|;
name|StopWatch
name|stopWatch
init|=
operator|new
name|StopWatch
argument_list|()
operator|.
name|start
argument_list|()
decl_stmt|;
name|int
name|totalOperations
init|=
name|sendSnapshot
argument_list|(
name|snapshot
argument_list|)
decl_stmt|;
name|transportService
operator|.
name|submitRequest
argument_list|(
name|request
operator|.
name|targetNode
argument_list|()
argument_list|,
name|RecoveryTarget
operator|.
name|Actions
operator|.
name|FINALIZE
argument_list|,
operator|new
name|RecoveryFinalizeRecoveryRequest
argument_list|(
name|request
operator|.
name|shardId
argument_list|()
argument_list|)
argument_list|,
name|VoidTransportResponseHandler
operator|.
name|INSTANCE
argument_list|)
operator|.
name|txGet
argument_list|()
expr_stmt|;
if|if
condition|(
name|request
operator|.
name|markAsRelocated
argument_list|()
condition|)
block|{
comment|// TODO what happens if the recovery process fails afterwards, we need to mark this back to started
try|try
block|{
name|shard
operator|.
name|relocated
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalIndexShardStateException
name|e
parameter_list|)
block|{
comment|// we can ignore this exception since, on the other node, when it moved to phase3
comment|// it will also send shard started, which might cause the index shard we work against
comment|// to move be closed by the time we get to the the relocated method
block|}
block|}
name|stopWatch
operator|.
name|stop
argument_list|()
expr_stmt|;
name|logger
operator|.
name|trace
argument_list|(
literal|"[{}][{}] recovery [phase3] to {}: took [{}]"
argument_list|,
name|request
operator|.
name|shardId
argument_list|()
operator|.
name|index
argument_list|()
operator|.
name|name
argument_list|()
argument_list|,
name|request
operator|.
name|shardId
argument_list|()
operator|.
name|id
argument_list|()
argument_list|,
name|request
operator|.
name|targetNode
argument_list|()
argument_list|,
name|stopWatch
operator|.
name|totalTime
argument_list|()
argument_list|)
expr_stmt|;
name|response
operator|.
name|phase3Time
operator|=
name|stopWatch
operator|.
name|totalTime
argument_list|()
operator|.
name|millis
argument_list|()
expr_stmt|;
name|response
operator|.
name|phase3Operations
operator|=
name|totalOperations
expr_stmt|;
block|}
specifier|private
name|int
name|sendSnapshot
parameter_list|(
name|Translog
operator|.
name|Snapshot
name|snapshot
parameter_list|)
throws|throws
name|ElasticSearchException
block|{
name|int
name|counter
init|=
literal|0
decl_stmt|;
name|int
name|totalOperations
init|=
literal|0
decl_stmt|;
name|List
argument_list|<
name|Translog
operator|.
name|Operation
argument_list|>
name|operations
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
while|while
condition|(
name|snapshot
operator|.
name|hasNext
argument_list|()
condition|)
block|{
if|if
condition|(
name|shard
operator|.
name|state
argument_list|()
operator|==
name|IndexShardState
operator|.
name|CLOSED
condition|)
block|{
throw|throw
operator|new
name|IndexShardClosedException
argument_list|(
name|request
operator|.
name|shardId
argument_list|()
argument_list|)
throw|;
block|}
name|operations
operator|.
name|add
argument_list|(
name|snapshot
operator|.
name|next
argument_list|()
argument_list|)
expr_stmt|;
name|totalOperations
operator|++
expr_stmt|;
if|if
condition|(
operator|++
name|counter
operator|==
name|translogBatchSize
condition|)
block|{
name|RecoveryTranslogOperationsRequest
name|translogOperationsRequest
init|=
operator|new
name|RecoveryTranslogOperationsRequest
argument_list|(
name|request
operator|.
name|shardId
argument_list|()
argument_list|,
name|operations
argument_list|)
decl_stmt|;
name|transportService
operator|.
name|submitRequest
argument_list|(
name|request
operator|.
name|targetNode
argument_list|()
argument_list|,
name|RecoveryTarget
operator|.
name|Actions
operator|.
name|TRANSLOG_OPS
argument_list|,
name|translogOperationsRequest
argument_list|,
name|TransportRequestOptions
operator|.
name|options
argument_list|()
operator|.
name|withCompress
argument_list|(
name|compress
argument_list|)
argument_list|,
name|VoidTransportResponseHandler
operator|.
name|INSTANCE
argument_list|)
operator|.
name|txGet
argument_list|()
expr_stmt|;
name|counter
operator|=
literal|0
expr_stmt|;
name|operations
operator|=
name|Lists
operator|.
name|newArrayList
argument_list|()
expr_stmt|;
block|}
block|}
comment|// send the leftover
if|if
condition|(
operator|!
name|operations
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|RecoveryTranslogOperationsRequest
name|translogOperationsRequest
init|=
operator|new
name|RecoveryTranslogOperationsRequest
argument_list|(
name|request
operator|.
name|shardId
argument_list|()
argument_list|,
name|operations
argument_list|)
decl_stmt|;
name|transportService
operator|.
name|submitRequest
argument_list|(
name|request
operator|.
name|targetNode
argument_list|()
argument_list|,
name|RecoveryTarget
operator|.
name|Actions
operator|.
name|TRANSLOG_OPS
argument_list|,
name|translogOperationsRequest
argument_list|,
name|TransportRequestOptions
operator|.
name|options
argument_list|()
operator|.
name|withCompress
argument_list|(
name|compress
argument_list|)
argument_list|,
name|VoidTransportResponseHandler
operator|.
name|INSTANCE
argument_list|)
operator|.
name|txGet
argument_list|()
expr_stmt|;
block|}
return|return
name|totalOperations
return|;
block|}
block|}
argument_list|)
expr_stmt|;
return|return
name|response
return|;
block|}
DECL|class|StartRecoveryTransportRequestHandler
class|class
name|StartRecoveryTransportRequestHandler
extends|extends
name|BaseTransportRequestHandler
argument_list|<
name|StartRecoveryRequest
argument_list|>
block|{
DECL|method|newInstance
annotation|@
name|Override
specifier|public
name|StartRecoveryRequest
name|newInstance
parameter_list|()
block|{
return|return
operator|new
name|StartRecoveryRequest
argument_list|()
return|;
block|}
DECL|method|messageReceived
annotation|@
name|Override
specifier|public
name|void
name|messageReceived
parameter_list|(
specifier|final
name|StartRecoveryRequest
name|request
parameter_list|,
specifier|final
name|TransportChannel
name|channel
parameter_list|)
throws|throws
name|Exception
block|{
comment|// we don't spawn, but we execute the expensive recovery process on a cached thread pool
name|threadPool
operator|.
name|cached
argument_list|()
operator|.
name|execute
argument_list|(
operator|new
name|Runnable
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
name|RecoveryResponse
name|response
init|=
name|recover
argument_list|(
name|request
argument_list|)
decl_stmt|;
name|channel
operator|.
name|sendResponse
argument_list|(
name|response
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
try|try
block|{
name|channel
operator|.
name|sendResponse
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e1
parameter_list|)
block|{
comment|// ignore
block|}
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
DECL|method|spawn
annotation|@
name|Override
specifier|public
name|boolean
name|spawn
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
block|}
block|}
end_class

end_unit

