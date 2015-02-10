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
name|com
operator|.
name|google
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
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|IOUtils
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
name|io
operator|.
name|FileSystemUtils
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
name|StreamInput
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
name|common
operator|.
name|util
operator|.
name|concurrent
operator|.
name|FutureUtils
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
name|translog
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
name|indices
operator|.
name|recovery
operator|.
name|RecoveryState
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|RestStatus
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
name|EOFException
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
name|ScheduledFuture
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
DECL|field|threadPool
specifier|private
specifier|final
name|ThreadPool
name|threadPool
decl_stmt|;
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
DECL|field|syncInterval
specifier|private
specifier|final
name|TimeValue
name|syncInterval
decl_stmt|;
DECL|field|flushScheduler
specifier|private
specifier|volatile
name|ScheduledFuture
name|flushScheduler
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
name|ThreadPool
name|threadPool
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
name|threadPool
operator|=
name|threadPool
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
name|componentSettings
operator|.
name|getAsTime
argument_list|(
literal|"wait_for_mapping_update_post_recovery"
argument_list|,
name|TimeValue
operator|.
name|timeValueSeconds
argument_list|(
literal|30
argument_list|)
argument_list|)
expr_stmt|;
name|syncInterval
operator|=
name|componentSettings
operator|.
name|getAsTime
argument_list|(
literal|"sync"
argument_list|,
name|TimeValue
operator|.
name|timeValueSeconds
argument_list|(
literal|5
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|syncInterval
operator|.
name|millis
argument_list|()
operator|>
literal|0
condition|)
block|{
name|this
operator|.
name|indexShard
operator|.
name|translog
argument_list|()
operator|.
name|syncOnEachOperation
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|flushScheduler
operator|=
name|threadPool
operator|.
name|schedule
argument_list|(
name|syncInterval
argument_list|,
name|ThreadPool
operator|.
name|Names
operator|.
name|SAME
argument_list|,
operator|new
name|Sync
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|syncInterval
operator|.
name|millis
argument_list|()
operator|==
literal|0
condition|)
block|{
name|flushScheduler
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|indexShard
operator|.
name|translog
argument_list|()
operator|.
name|syncOnEachOperation
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|flushScheduler
operator|=
literal|null
expr_stmt|;
block|}
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
name|recoveryState
operator|.
name|getIndex
argument_list|()
operator|.
name|startTime
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
expr_stmt|;
name|recoveryState
operator|.
name|setStage
argument_list|(
name|RecoveryState
operator|.
name|Stage
operator|.
name|INDEX
argument_list|)
expr_stmt|;
name|long
name|version
init|=
operator|-
literal|1
decl_stmt|;
name|long
name|translogId
init|=
operator|-
literal|1
decl_stmt|;
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|typesToUpdate
init|=
name|Sets
operator|.
name|newHashSet
argument_list|()
decl_stmt|;
name|SegmentInfos
name|si
init|=
literal|null
decl_stmt|;
name|indexShard
operator|.
name|store
argument_list|()
operator|.
name|incRef
argument_list|()
expr_stmt|;
try|try
block|{
try|try
block|{
name|indexShard
operator|.
name|store
argument_list|()
operator|.
name|failIfCorrupted
argument_list|()
expr_stmt|;
try|try
block|{
name|si
operator|=
name|Lucene
operator|.
name|readSegmentInfos
argument_list|(
name|indexShard
operator|.
name|store
argument_list|()
operator|.
name|directory
argument_list|()
argument_list|)
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
name|indexShard
operator|.
name|store
argument_list|()
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
comment|/**                          * We generate the translog ID before each lucene commit to ensure that                          * we can read the current translog ID safely when we recover. The commits metadata                          * therefor contains always the current / active translog ID.                          */
if|if
condition|(
name|si
operator|.
name|getUserData
argument_list|()
operator|.
name|containsKey
argument_list|(
name|Translog
operator|.
name|TRANSLOG_ID_KEY
argument_list|)
condition|)
block|{
name|translogId
operator|=
name|Long
operator|.
name|parseLong
argument_list|(
name|si
operator|.
name|getUserData
argument_list|()
operator|.
name|get
argument_list|(
name|Translog
operator|.
name|TRANSLOG_ID_KEY
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|translogId
operator|=
name|version
expr_stmt|;
block|}
name|logger
operator|.
name|trace
argument_list|(
literal|"using existing shard data, translog id [{}]"
argument_list|,
name|translogId
argument_list|)
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
name|indexShard
operator|.
name|store
argument_list|()
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
name|recoveryState
operator|.
name|getIndex
argument_list|()
operator|.
name|stopTime
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
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
name|indexShard
operator|.
name|store
argument_list|()
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
specifier|final
name|RecoveryState
operator|.
name|Start
name|stateStart
init|=
name|recoveryState
operator|.
name|getStart
argument_list|()
decl_stmt|;
name|stateStart
operator|.
name|startTime
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
expr_stmt|;
name|recoveryState
operator|.
name|setStage
argument_list|(
name|RecoveryState
operator|.
name|Stage
operator|.
name|START
argument_list|)
expr_stmt|;
if|if
condition|(
name|translogId
operator|==
operator|-
literal|1
condition|)
block|{
comment|// no translog files, bail
name|indexShard
operator|.
name|postRecovery
argument_list|(
literal|"post recovery from gateway, no translog for id ["
operator|+
name|translogId
operator|+
literal|"]"
argument_list|)
expr_stmt|;
comment|// no index, just start the shard and bail
name|stateStart
operator|.
name|stopTime
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
expr_stmt|;
name|stateStart
operator|.
name|checkIndexTime
argument_list|(
name|indexShard
operator|.
name|checkIndexTook
argument_list|()
argument_list|)
expr_stmt|;
return|return;
block|}
specifier|final
name|Translog
name|translog
init|=
name|indexShard
operator|.
name|translog
argument_list|()
decl_stmt|;
specifier|final
name|Path
name|translogName
init|=
name|translog
operator|.
name|getPath
argument_list|(
name|translogId
argument_list|)
decl_stmt|;
name|logger
operator|.
name|trace
argument_list|(
literal|"try recover from translog file {} locations: {}"
argument_list|,
name|translogName
argument_list|,
name|Arrays
operator|.
name|toString
argument_list|(
name|translog
operator|.
name|locations
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|Path
name|recoveringTranslogFile
init|=
literal|null
decl_stmt|;
name|OUTER
label|:
for|for
control|(
name|Path
name|translogLocation
range|:
name|translog
operator|.
name|locations
argument_list|()
control|)
block|{
comment|// we have to support .recovering since it's a leftover from previous version but might still be on the filesystem
comment|// we used to rename the foo into foo.recovering since foo was reused / overwritten but we fixed that in 1.5
for|for
control|(
name|Path
name|recoveryFiles
range|:
name|FileSystemUtils
operator|.
name|files
argument_list|(
name|translogLocation
argument_list|,
name|translogName
operator|.
name|getFileName
argument_list|()
operator|+
literal|"{.recovering,}"
argument_list|)
control|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"Translog file found in {}"
argument_list|,
name|recoveryFiles
argument_list|)
expr_stmt|;
name|recoveringTranslogFile
operator|=
name|recoveryFiles
expr_stmt|;
break|break
name|OUTER
break|;
block|}
name|logger
operator|.
name|trace
argument_list|(
literal|"Translog file NOT found in {} - continue"
argument_list|,
name|translogLocation
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|recoveringTranslogFile
operator|==
literal|null
operator|||
name|Files
operator|.
name|exists
argument_list|(
name|recoveringTranslogFile
argument_list|)
operator|==
literal|false
condition|)
block|{
comment|// no translog to recovery from, start and bail
comment|// no translog files, bail
name|indexShard
operator|.
name|postRecovery
argument_list|(
literal|"post recovery from gateway, no translog"
argument_list|)
expr_stmt|;
comment|// no index, just start the shard and bail
name|stateStart
operator|.
name|stopTime
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
expr_stmt|;
name|stateStart
operator|.
name|checkIndexTime
argument_list|(
literal|0
argument_list|)
expr_stmt|;
return|return;
block|}
comment|// recover from the translog file
name|indexShard
operator|.
name|performRecoveryPrepareForTranslog
argument_list|()
expr_stmt|;
name|stateStart
operator|.
name|stopTime
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
expr_stmt|;
name|stateStart
operator|.
name|checkIndexTime
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|recoveryState
operator|.
name|getTranslog
argument_list|()
operator|.
name|startTime
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
expr_stmt|;
name|recoveryState
operator|.
name|setStage
argument_list|(
name|RecoveryState
operator|.
name|Stage
operator|.
name|TRANSLOG
argument_list|)
expr_stmt|;
name|StreamInput
name|in
init|=
literal|null
decl_stmt|;
try|try
block|{
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
literal|"recovering translog file: {} length: {}"
argument_list|,
name|recoveringTranslogFile
argument_list|,
name|Files
operator|.
name|size
argument_list|(
name|recoveringTranslogFile
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|TranslogStream
name|stream
init|=
name|TranslogStreams
operator|.
name|translogStreamFor
argument_list|(
name|recoveringTranslogFile
argument_list|)
decl_stmt|;
try|try
block|{
name|in
operator|=
name|stream
operator|.
name|openInput
argument_list|(
name|recoveringTranslogFile
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|TruncatedTranslogException
name|e
parameter_list|)
block|{
comment|// file is empty or header has been half-written and should be ignored
name|logger
operator|.
name|trace
argument_list|(
literal|"ignoring truncation exception, the translog is either empty or half-written"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
while|while
condition|(
literal|true
condition|)
block|{
if|if
condition|(
name|in
operator|==
literal|null
condition|)
block|{
break|break;
block|}
name|Translog
operator|.
name|Operation
name|operation
decl_stmt|;
try|try
block|{
if|if
condition|(
name|stream
operator|instanceof
name|LegacyTranslogStream
condition|)
block|{
name|in
operator|.
name|readInt
argument_list|()
expr_stmt|;
comment|// ignored opSize
block|}
name|operation
operator|=
name|stream
operator|.
name|read
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|EOFException
name|e
parameter_list|)
block|{
comment|// ignore, not properly written the last op
name|logger
operator|.
name|trace
argument_list|(
literal|"ignoring translog EOF exception, the last operation was not properly written"
argument_list|,
name|e
argument_list|)
expr_stmt|;
break|break;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// ignore, not properly written last op
name|logger
operator|.
name|trace
argument_list|(
literal|"ignoring translog IO exception, the last operation was not properly written"
argument_list|,
name|e
argument_list|)
expr_stmt|;
break|break;
block|}
try|try
block|{
name|Engine
operator|.
name|IndexingOperation
name|potentialIndexOperation
init|=
name|indexShard
operator|.
name|performRecoveryOperation
argument_list|(
name|operation
argument_list|)
decl_stmt|;
if|if
condition|(
name|potentialIndexOperation
operator|!=
literal|null
operator|&&
name|potentialIndexOperation
operator|.
name|parsedDoc
argument_list|()
operator|.
name|mappingsModified
argument_list|()
condition|)
block|{
if|if
condition|(
operator|!
name|typesToUpdate
operator|.
name|contains
argument_list|(
name|potentialIndexOperation
operator|.
name|docMapper
argument_list|()
operator|.
name|type
argument_list|()
argument_list|)
condition|)
block|{
name|typesToUpdate
operator|.
name|add
argument_list|(
name|potentialIndexOperation
operator|.
name|docMapper
argument_list|()
operator|.
name|type
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|recoveryState
operator|.
name|getTranslog
argument_list|()
operator|.
name|addTranslogOperations
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ElasticsearchException
name|e
parameter_list|)
block|{
if|if
condition|(
name|e
operator|.
name|status
argument_list|()
operator|==
name|RestStatus
operator|.
name|BAD_REQUEST
condition|)
block|{
comment|// mainly for MapperParsingException and Failure to detect xcontent
name|logger
operator|.
name|info
argument_list|(
literal|"ignoring recovery of a corrupt translog entry"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
name|e
throw|;
block|}
block|}
block|}
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|IOUtils
operator|.
name|closeWhileHandlingException
argument_list|(
name|indexShard
operator|.
name|translog
argument_list|()
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IndexShardGatewayRecoveryException
argument_list|(
name|shardId
argument_list|,
literal|"failed to recover shard"
argument_list|,
name|e
argument_list|)
throw|;
block|}
finally|finally
block|{
name|IOUtils
operator|.
name|closeWhileHandlingException
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
name|indexShard
operator|.
name|performRecoveryFinalization
argument_list|(
literal|true
argument_list|)
expr_stmt|;
try|try
block|{
name|Files
operator|.
name|deleteIfExists
argument_list|(
name|recoveringTranslogFile
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"Failed to delete recovering translog file {}"
argument_list|,
name|ex
argument_list|,
name|recoveringTranslogFile
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
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
name|indexShard
operator|.
name|store
argument_list|()
operator|.
name|decRef
argument_list|()
expr_stmt|;
block|}
for|for
control|(
specifier|final
name|String
name|type
range|:
name|typesToUpdate
control|)
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
name|indexService
operator|.
name|mapperService
argument_list|()
operator|.
name|documentMapper
argument_list|(
name|type
argument_list|)
argument_list|,
name|indexService
operator|.
name|indexUUID
argument_list|()
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
name|logger
operator|.
name|debug
argument_list|(
literal|"failed to send mapping update post recovery to master for [{}]"
argument_list|,
name|t
argument_list|,
name|type
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
name|recoveryState
operator|.
name|getTranslog
argument_list|()
operator|.
name|time
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|recoveryState
operator|.
name|getTranslog
argument_list|()
operator|.
name|startTime
argument_list|()
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
name|FutureUtils
operator|.
name|cancel
argument_list|(
name|flushScheduler
argument_list|)
expr_stmt|;
name|cancellableThreads
operator|.
name|cancel
argument_list|(
literal|"closed"
argument_list|)
expr_stmt|;
block|}
DECL|class|Sync
class|class
name|Sync
implements|implements
name|Runnable
block|{
annotation|@
name|Override
DECL|method|run
specifier|public
name|void
name|run
parameter_list|()
block|{
comment|// don't re-schedule  if its closed..., we are done
if|if
condition|(
name|indexShard
operator|.
name|state
argument_list|()
operator|==
name|IndexShardState
operator|.
name|CLOSED
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|indexShard
operator|.
name|state
argument_list|()
operator|==
name|IndexShardState
operator|.
name|STARTED
operator|&&
name|indexShard
operator|.
name|translog
argument_list|()
operator|.
name|syncNeeded
argument_list|()
condition|)
block|{
name|threadPool
operator|.
name|executor
argument_list|(
name|ThreadPool
operator|.
name|Names
operator|.
name|FLUSH
argument_list|)
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
name|indexShard
operator|.
name|translog
argument_list|()
operator|.
name|sync
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
if|if
condition|(
name|indexShard
operator|.
name|state
argument_list|()
operator|==
name|IndexShardState
operator|.
name|STARTED
condition|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"failed to sync translog"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|indexShard
operator|.
name|state
argument_list|()
operator|!=
name|IndexShardState
operator|.
name|CLOSED
condition|)
block|{
name|flushScheduler
operator|=
name|threadPool
operator|.
name|schedule
argument_list|(
name|syncInterval
argument_list|,
name|ThreadPool
operator|.
name|Names
operator|.
name|SAME
argument_list|,
name|Sync
operator|.
name|this
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|flushScheduler
operator|=
name|threadPool
operator|.
name|schedule
argument_list|(
name|syncInterval
argument_list|,
name|ThreadPool
operator|.
name|Names
operator|.
name|SAME
argument_list|,
name|Sync
operator|.
name|this
argument_list|)
expr_stmt|;
block|}
block|}
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

