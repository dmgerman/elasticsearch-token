begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.gateway
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|gateway
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
name|index
operator|.
name|IndexWriterConfig
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
name|SegmentInfos
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
name|elasticsearch
operator|.
name|ExceptionsHelper
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
name|action
operator|.
name|index
operator|.
name|MappingUpdatedAction
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
name|common
operator|.
name|unit
operator|.
name|TimeValue
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
name|CancellableThreads
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
name|IndexService
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
name|EngineException
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
name|mapper
operator|.
name|Mapping
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
name|AbstractIndexShardComponent
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
name|indices
operator|.
name|recovery
operator|.
name|RecoveryState
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
name|TimeUnit
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
comment|/**  *  */
end_comment

begin_class
DECL|class|IndexShardGateway
specifier|public
class|class
name|IndexShardGateway
extends|extends
name|AbstractIndexShardComponent
implements|implements
name|Closeable
block|{
DECL|field|mappingUpdatedAction
specifier|private
specifier|final
name|MappingUpdatedAction
name|mappingUpdatedAction
decl_stmt|;
DECL|field|indexService
specifier|private
specifier|final
name|IndexService
name|indexService
decl_stmt|;
DECL|field|indexShard
specifier|private
specifier|final
name|IndexShard
name|indexShard
decl_stmt|;
DECL|field|waitForMappingUpdatePostRecovery
specifier|private
specifier|final
name|TimeValue
name|waitForMappingUpdatePostRecovery
decl_stmt|;
DECL|field|cancellableThreads
specifier|private
specifier|final
name|CancellableThreads
name|cancellableThreads
init|=
operator|new
name|CancellableThreads
argument_list|()
decl_stmt|;
annotation|@
name|Inject
DECL|method|IndexShardGateway
specifier|public
name|IndexShardGateway
parameter_list|(
name|ShardId
name|shardId
parameter_list|,
annotation|@
name|IndexSettings
name|Settings
name|indexSettings
parameter_list|,
name|MappingUpdatedAction
name|mappingUpdatedAction
parameter_list|,
name|IndexService
name|indexService
parameter_list|,
name|IndexShard
name|indexShard
parameter_list|)
block|{
name|super
argument_list|(
name|shardId
argument_list|,
name|indexSettings
argument_list|)
expr_stmt|;
name|this
operator|.
name|mappingUpdatedAction
operator|=
name|mappingUpdatedAction
expr_stmt|;
name|this
operator|.
name|indexService
operator|=
name|indexService
expr_stmt|;
name|this
operator|.
name|indexShard
operator|=
name|indexShard
expr_stmt|;
name|this
operator|.
name|waitForMappingUpdatePostRecovery
operator|=
name|indexSettings
operator|.
name|getAsTime
argument_list|(
literal|"index.gateway.wait_for_mapping_update_post_recovery"
argument_list|,
name|TimeValue
operator|.
name|timeValueSeconds
argument_list|(
literal|15
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**      * Recovers the state of the shard from the gateway.      */
DECL|method|recover
specifier|public
name|void
name|recover
parameter_list|(
name|boolean
name|indexShouldExists
parameter_list|,
name|RecoveryState
name|recoveryState
parameter_list|)
throws|throws
name|IndexShardGatewayRecoveryException
block|{
name|indexShard
operator|.
name|prepareForIndexRecovery
argument_list|()
expr_stmt|;
name|long
name|version
init|=
operator|-
literal|1
decl_stmt|;
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Mapping
argument_list|>
name|typesToUpdate
decl_stmt|;
name|SegmentInfos
name|si
init|=
literal|null
decl_stmt|;
specifier|final
name|Store
name|store
init|=
name|indexShard
operator|.
name|store
argument_list|()
decl_stmt|;
name|store
operator|.
name|incRef
argument_list|()
expr_stmt|;
try|try
block|{
try|try
block|{
name|store
operator|.
name|failIfCorrupted
argument_list|()
expr_stmt|;
try|try
block|{
name|si
operator|=
name|store
operator|.
name|readLastCommittedSegmentsInfo
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|String
name|files
init|=
literal|"_unknown_"
decl_stmt|;
try|try
block|{
name|files
operator|=
name|Arrays
operator|.
name|toString
argument_list|(
name|store
operator|.
name|directory
argument_list|()
operator|.
name|listAll
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e1
parameter_list|)
block|{
name|files
operator|+=
literal|" (failure="
operator|+
name|ExceptionsHelper
operator|.
name|detailedMessage
argument_list|(
name|e1
argument_list|)
operator|+
literal|")"
expr_stmt|;
block|}
if|if
condition|(
name|indexShouldExists
condition|)
block|{
throw|throw
operator|new
name|IndexShardGatewayRecoveryException
argument_list|(
name|shardId
argument_list|()
argument_list|,
literal|"shard allocated for local recovery (post api), should exist, but doesn't, current files: "
operator|+
name|files
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
if|if
condition|(
name|si
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|indexShouldExists
condition|)
block|{
name|version
operator|=
name|si
operator|.
name|getVersion
argument_list|()
expr_stmt|;
block|}
else|else
block|{
comment|// it exists on the directory, but shouldn't exist on the FS, its a leftover (possibly dangling)
comment|// its a "new index create" API, we have to do something, so better to clean it than use same data
name|logger
operator|.
name|trace
argument_list|(
literal|"cleaning existing shard, shouldn't exists"
argument_list|)
expr_stmt|;
name|IndexWriter
name|writer
init|=
operator|new
name|IndexWriter
argument_list|(
name|store
operator|.
name|directory
argument_list|()
argument_list|,
operator|new
name|IndexWriterConfig
argument_list|(
name|Lucene
operator|.
name|STANDARD_ANALYZER
argument_list|)
operator|.
name|setOpenMode
argument_list|(
name|IndexWriterConfig
operator|.
name|OpenMode
operator|.
name|CREATE
argument_list|)
argument_list|)
decl_stmt|;
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
name|recoveryState
operator|.
name|getTranslog
argument_list|()
operator|.
name|totalOperations
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IndexShardGatewayRecoveryException
argument_list|(
name|shardId
argument_list|()
argument_list|,
literal|"failed to fetch index version after copying it over"
argument_list|,
name|e
argument_list|)
throw|;
block|}
name|recoveryState
operator|.
name|getIndex
argument_list|()
operator|.
name|updateVersion
argument_list|(
name|version
argument_list|)
expr_stmt|;
comment|// since we recover from local, just fill the files and size
try|try
block|{
specifier|final
name|RecoveryState
operator|.
name|Index
name|index
init|=
name|recoveryState
operator|.
name|getIndex
argument_list|()
decl_stmt|;
if|if
condition|(
name|si
operator|!=
literal|null
condition|)
block|{
specifier|final
name|Directory
name|directory
init|=
name|store
operator|.
name|directory
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|name
range|:
name|Lucene
operator|.
name|files
argument_list|(
name|si
argument_list|)
control|)
block|{
name|long
name|length
init|=
name|directory
operator|.
name|fileLength
argument_list|(
name|name
argument_list|)
decl_stmt|;
name|index
operator|.
name|addFileDetail
argument_list|(
name|name
argument_list|,
name|length
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"failed to list file details"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|indexShouldExists
operator|==
literal|false
condition|)
block|{
name|recoveryState
operator|.
name|getTranslog
argument_list|()
operator|.
name|totalOperations
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|recoveryState
operator|.
name|getTranslog
argument_list|()
operator|.
name|totalOperationsOnStart
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
name|typesToUpdate
operator|=
name|indexShard
operator|.
name|performTranslogRecovery
argument_list|()
expr_stmt|;
name|indexShard
operator|.
name|finalizeRecovery
argument_list|()
expr_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Mapping
argument_list|>
name|entry
range|:
name|typesToUpdate
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|validateMappingUpdate
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|indexShard
operator|.
name|postRecovery
argument_list|(
literal|"post recovery from gateway"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|EngineException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IndexShardGatewayRecoveryException
argument_list|(
name|shardId
argument_list|,
literal|"failed to recovery from gateway"
argument_list|,
name|e
argument_list|)
throw|;
block|}
finally|finally
block|{
name|store
operator|.
name|decRef
argument_list|()
expr_stmt|;
block|}
block|}
DECL|method|validateMappingUpdate
specifier|private
name|void
name|validateMappingUpdate
parameter_list|(
specifier|final
name|String
name|type
parameter_list|,
name|Mapping
name|update
parameter_list|)
block|{
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
name|AtomicReference
argument_list|<
name|Throwable
argument_list|>
name|error
init|=
operator|new
name|AtomicReference
argument_list|<>
argument_list|()
decl_stmt|;
name|mappingUpdatedAction
operator|.
name|updateMappingOnMaster
argument_list|(
name|indexService
operator|.
name|index
argument_list|()
operator|.
name|name
argument_list|()
argument_list|,
name|type
argument_list|,
name|update
argument_list|,
name|waitForMappingUpdatePostRecovery
argument_list|,
operator|new
name|MappingUpdatedAction
operator|.
name|MappingUpdateListener
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onMappingUpdate
parameter_list|()
block|{
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|onFailure
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
name|error
operator|.
name|set
argument_list|(
name|t
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
name|cancellableThreads
operator|.
name|execute
argument_list|(
operator|new
name|CancellableThreads
operator|.
name|Interruptable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
throws|throws
name|InterruptedException
block|{
try|try
block|{
if|if
condition|(
name|latch
operator|.
name|await
argument_list|(
name|waitForMappingUpdatePostRecovery
operator|.
name|millis
argument_list|()
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
operator|==
literal|false
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"waited for mapping update on master for [{}], yet timed out"
argument_list|,
name|type
argument_list|)
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|error
operator|.
name|get
argument_list|()
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|IndexShardGatewayRecoveryException
argument_list|(
name|shardId
argument_list|,
literal|"Failed to propagate mappings on master post recovery"
argument_list|,
name|error
operator|.
name|get
argument_list|()
argument_list|)
throw|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"interrupted while waiting for mapping update"
argument_list|)
expr_stmt|;
throw|throw
name|e
throw|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
block|{
name|cancellableThreads
operator|.
name|cancel
argument_list|(
literal|"closed"
argument_list|)
expr_stmt|;
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
literal|"shard_gateway"
return|;
block|}
block|}
end_class

end_unit

