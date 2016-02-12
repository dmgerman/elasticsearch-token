begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.indices.recovery
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|indices
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
name|RateLimiter
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
name|node
operator|.
name|DiscoveryNode
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
name|bytes
operator|.
name|BytesReference
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
name|transport
operator|.
name|EmptyTransportResponseHandler
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
name|TransportRequestOptions
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
name|TransportService
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
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicLong
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|function
operator|.
name|Consumer
import|;
end_import

begin_class
DECL|class|RemoteRecoveryTargetHandler
specifier|public
class|class
name|RemoteRecoveryTargetHandler
implements|implements
name|RecoveryTargetHandler
block|{
DECL|field|transportService
specifier|private
specifier|final
name|TransportService
name|transportService
decl_stmt|;
DECL|field|recoveryId
specifier|private
specifier|final
name|long
name|recoveryId
decl_stmt|;
DECL|field|shardId
specifier|private
specifier|final
name|ShardId
name|shardId
decl_stmt|;
DECL|field|targetNode
specifier|private
specifier|final
name|DiscoveryNode
name|targetNode
decl_stmt|;
DECL|field|recoverySettings
specifier|private
specifier|final
name|RecoverySettings
name|recoverySettings
decl_stmt|;
DECL|field|translogOpsRequestOptions
specifier|private
specifier|final
name|TransportRequestOptions
name|translogOpsRequestOptions
decl_stmt|;
DECL|field|fileChunkRequestOptions
specifier|private
specifier|final
name|TransportRequestOptions
name|fileChunkRequestOptions
decl_stmt|;
DECL|field|bytesSinceLastPause
specifier|private
specifier|final
name|AtomicLong
name|bytesSinceLastPause
init|=
operator|new
name|AtomicLong
argument_list|()
decl_stmt|;
DECL|field|onSourceThrottle
specifier|private
specifier|final
name|Consumer
argument_list|<
name|Long
argument_list|>
name|onSourceThrottle
decl_stmt|;
DECL|method|RemoteRecoveryTargetHandler
specifier|public
name|RemoteRecoveryTargetHandler
parameter_list|(
name|long
name|recoveryId
parameter_list|,
name|ShardId
name|shardId
parameter_list|,
name|TransportService
name|transportService
parameter_list|,
name|DiscoveryNode
name|targetNode
parameter_list|,
name|RecoverySettings
name|recoverySettings
parameter_list|,
name|Consumer
argument_list|<
name|Long
argument_list|>
name|onSourceThrottle
parameter_list|)
block|{
name|this
operator|.
name|transportService
operator|=
name|transportService
expr_stmt|;
name|this
operator|.
name|recoveryId
operator|=
name|recoveryId
expr_stmt|;
name|this
operator|.
name|shardId
operator|=
name|shardId
expr_stmt|;
name|this
operator|.
name|targetNode
operator|=
name|targetNode
expr_stmt|;
name|this
operator|.
name|recoverySettings
operator|=
name|recoverySettings
expr_stmt|;
name|this
operator|.
name|onSourceThrottle
operator|=
name|onSourceThrottle
expr_stmt|;
name|this
operator|.
name|translogOpsRequestOptions
operator|=
name|TransportRequestOptions
operator|.
name|builder
argument_list|()
operator|.
name|withCompress
argument_list|(
literal|true
argument_list|)
operator|.
name|withType
argument_list|(
name|TransportRequestOptions
operator|.
name|Type
operator|.
name|RECOVERY
argument_list|)
operator|.
name|withTimeout
argument_list|(
name|recoverySettings
operator|.
name|internalActionLongTimeout
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|this
operator|.
name|fileChunkRequestOptions
operator|=
name|TransportRequestOptions
operator|.
name|builder
argument_list|()
operator|.
name|withCompress
argument_list|(
literal|false
argument_list|)
comment|// lucene files are already compressed and therefore compressing this won't really help much so
comment|// we are saving the cpu for other things
operator|.
name|withType
argument_list|(
name|TransportRequestOptions
operator|.
name|Type
operator|.
name|RECOVERY
argument_list|)
operator|.
name|withTimeout
argument_list|(
name|recoverySettings
operator|.
name|internalActionTimeout
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|prepareForTranslogOperations
specifier|public
name|void
name|prepareForTranslogOperations
parameter_list|(
name|int
name|totalTranslogOps
parameter_list|)
throws|throws
name|IOException
block|{
name|transportService
operator|.
name|submitRequest
argument_list|(
name|targetNode
argument_list|,
name|RecoveryTargetService
operator|.
name|Actions
operator|.
name|PREPARE_TRANSLOG
argument_list|,
operator|new
name|RecoveryPrepareForTranslogOperationsRequest
argument_list|(
name|recoveryId
argument_list|,
name|shardId
argument_list|,
name|totalTranslogOps
argument_list|)
argument_list|,
name|TransportRequestOptions
operator|.
name|builder
argument_list|()
operator|.
name|withTimeout
argument_list|(
name|recoverySettings
operator|.
name|internalActionTimeout
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
argument_list|,
name|EmptyTransportResponseHandler
operator|.
name|INSTANCE_SAME
argument_list|)
operator|.
name|txGet
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|finalizeRecovery
specifier|public
name|void
name|finalizeRecovery
parameter_list|()
block|{
name|transportService
operator|.
name|submitRequest
argument_list|(
name|targetNode
argument_list|,
name|RecoveryTargetService
operator|.
name|Actions
operator|.
name|FINALIZE
argument_list|,
operator|new
name|RecoveryFinalizeRecoveryRequest
argument_list|(
name|recoveryId
argument_list|,
name|shardId
argument_list|)
argument_list|,
name|TransportRequestOptions
operator|.
name|builder
argument_list|()
operator|.
name|withTimeout
argument_list|(
name|recoverySettings
operator|.
name|internalActionLongTimeout
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
argument_list|,
name|EmptyTransportResponseHandler
operator|.
name|INSTANCE_SAME
argument_list|)
operator|.
name|txGet
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|indexTranslogOperations
specifier|public
name|void
name|indexTranslogOperations
parameter_list|(
name|List
argument_list|<
name|Translog
operator|.
name|Operation
argument_list|>
name|operations
parameter_list|,
name|int
name|totalTranslogOps
parameter_list|)
block|{
specifier|final
name|RecoveryTranslogOperationsRequest
name|translogOperationsRequest
init|=
operator|new
name|RecoveryTranslogOperationsRequest
argument_list|(
name|recoveryId
argument_list|,
name|shardId
argument_list|,
name|operations
argument_list|,
name|totalTranslogOps
argument_list|)
decl_stmt|;
name|transportService
operator|.
name|submitRequest
argument_list|(
name|targetNode
argument_list|,
name|RecoveryTargetService
operator|.
name|Actions
operator|.
name|TRANSLOG_OPS
argument_list|,
name|translogOperationsRequest
argument_list|,
name|translogOpsRequestOptions
argument_list|,
name|EmptyTransportResponseHandler
operator|.
name|INSTANCE_SAME
argument_list|)
operator|.
name|txGet
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|receiveFileInfo
specifier|public
name|void
name|receiveFileInfo
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|phase1FileNames
parameter_list|,
name|List
argument_list|<
name|Long
argument_list|>
name|phase1FileSizes
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|phase1ExistingFileNames
parameter_list|,
name|List
argument_list|<
name|Long
argument_list|>
name|phase1ExistingFileSizes
parameter_list|,
name|int
name|totalTranslogOps
parameter_list|)
block|{
name|RecoveryFilesInfoRequest
name|recoveryInfoFilesRequest
init|=
operator|new
name|RecoveryFilesInfoRequest
argument_list|(
name|recoveryId
argument_list|,
name|shardId
argument_list|,
name|phase1FileNames
argument_list|,
name|phase1FileSizes
argument_list|,
name|phase1ExistingFileNames
argument_list|,
name|phase1ExistingFileSizes
argument_list|,
name|totalTranslogOps
argument_list|)
decl_stmt|;
name|transportService
operator|.
name|submitRequest
argument_list|(
name|targetNode
argument_list|,
name|RecoveryTargetService
operator|.
name|Actions
operator|.
name|FILES_INFO
argument_list|,
name|recoveryInfoFilesRequest
argument_list|,
name|TransportRequestOptions
operator|.
name|builder
argument_list|()
operator|.
name|withTimeout
argument_list|(
name|recoverySettings
operator|.
name|internalActionTimeout
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
argument_list|,
name|EmptyTransportResponseHandler
operator|.
name|INSTANCE_SAME
argument_list|)
operator|.
name|txGet
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|cleanFiles
specifier|public
name|void
name|cleanFiles
parameter_list|(
name|int
name|totalTranslogOps
parameter_list|,
name|Store
operator|.
name|MetadataSnapshot
name|sourceMetaData
parameter_list|)
throws|throws
name|IOException
block|{
name|transportService
operator|.
name|submitRequest
argument_list|(
name|targetNode
argument_list|,
name|RecoveryTargetService
operator|.
name|Actions
operator|.
name|CLEAN_FILES
argument_list|,
operator|new
name|RecoveryCleanFilesRequest
argument_list|(
name|recoveryId
argument_list|,
name|shardId
argument_list|,
name|sourceMetaData
argument_list|,
name|totalTranslogOps
argument_list|)
argument_list|,
name|TransportRequestOptions
operator|.
name|builder
argument_list|()
operator|.
name|withTimeout
argument_list|(
name|recoverySettings
operator|.
name|internalActionTimeout
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
argument_list|,
name|EmptyTransportResponseHandler
operator|.
name|INSTANCE_SAME
argument_list|)
operator|.
name|txGet
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|writeFileChunk
specifier|public
name|void
name|writeFileChunk
parameter_list|(
name|StoreFileMetaData
name|fileMetaData
parameter_list|,
name|long
name|position
parameter_list|,
name|BytesReference
name|content
parameter_list|,
name|boolean
name|lastChunk
parameter_list|,
name|int
name|totalTranslogOps
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Pause using the rate limiter, if desired, to throttle the recovery
specifier|final
name|long
name|throttleTimeInNanos
decl_stmt|;
comment|// always fetch the ratelimiter - it might be updated in real-time on the recovery settings
specifier|final
name|RateLimiter
name|rl
init|=
name|recoverySettings
operator|.
name|rateLimiter
argument_list|()
decl_stmt|;
if|if
condition|(
name|rl
operator|!=
literal|null
condition|)
block|{
name|long
name|bytes
init|=
name|bytesSinceLastPause
operator|.
name|addAndGet
argument_list|(
name|content
operator|.
name|length
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|bytes
operator|>
name|rl
operator|.
name|getMinPauseCheckBytes
argument_list|()
condition|)
block|{
comment|// Time to pause
name|bytesSinceLastPause
operator|.
name|addAndGet
argument_list|(
operator|-
name|bytes
argument_list|)
expr_stmt|;
try|try
block|{
name|throttleTimeInNanos
operator|=
name|rl
operator|.
name|pause
argument_list|(
name|bytes
argument_list|)
expr_stmt|;
name|onSourceThrottle
operator|.
name|accept
argument_list|(
name|throttleTimeInNanos
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticsearchException
argument_list|(
literal|"failed to pause recovery"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
else|else
block|{
name|throttleTimeInNanos
operator|=
literal|0
expr_stmt|;
block|}
block|}
else|else
block|{
name|throttleTimeInNanos
operator|=
literal|0
expr_stmt|;
block|}
name|transportService
operator|.
name|submitRequest
argument_list|(
name|targetNode
argument_list|,
name|RecoveryTargetService
operator|.
name|Actions
operator|.
name|FILE_CHUNK
argument_list|,
operator|new
name|RecoveryFileChunkRequest
argument_list|(
name|recoveryId
argument_list|,
name|shardId
argument_list|,
name|fileMetaData
argument_list|,
name|position
argument_list|,
name|content
argument_list|,
name|lastChunk
argument_list|,
name|totalTranslogOps
argument_list|,
comment|/* we send totalOperations with every request since we collect stats on the target and that way we can                                  * see how many translog ops we accumulate while copying files across the network. A future optimization                                  * would be in to restart file copy again (new deltas) if we have too many translog ops are piling up.                                  */
name|throttleTimeInNanos
argument_list|)
argument_list|,
name|fileChunkRequestOptions
argument_list|,
name|EmptyTransportResponseHandler
operator|.
name|INSTANCE_SAME
argument_list|)
operator|.
name|txGet
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

