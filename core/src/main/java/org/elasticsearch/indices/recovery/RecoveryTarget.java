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
name|logging
operator|.
name|log4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|logging
operator|.
name|log4j
operator|.
name|message
operator|.
name|ParameterizedMessage
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|logging
operator|.
name|log4j
operator|.
name|util
operator|.
name|Supplier
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
name|apache
operator|.
name|lucene
operator|.
name|index
operator|.
name|IndexFormatTooNewException
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
name|IndexFormatTooOldException
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
name|IOContext
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
name|IndexOutput
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
name|BytesRef
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
name|BytesRefIterator
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
name|UUIDs
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
name|common
operator|.
name|logging
operator|.
name|Loggers
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
name|util
operator|.
name|Callback
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
name|AbstractRefCounted
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
name|shard
operator|.
name|TranslogRecoveryPerformer
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
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
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
name|Map
operator|.
name|Entry
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
name|AtomicBoolean
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

begin_comment
comment|/**  * Represents a recovery where the current node is the target node of the recovery. To track recoveries in a central place, instances of  * this class are created through {@link RecoveriesCollection}.  */
end_comment

begin_class
DECL|class|RecoveryTarget
specifier|public
class|class
name|RecoveryTarget
extends|extends
name|AbstractRefCounted
implements|implements
name|RecoveryTargetHandler
block|{
DECL|field|logger
specifier|private
specifier|final
name|Logger
name|logger
decl_stmt|;
DECL|field|idGenerator
specifier|private
specifier|static
specifier|final
name|AtomicLong
name|idGenerator
init|=
operator|new
name|AtomicLong
argument_list|()
decl_stmt|;
DECL|field|RECOVERY_PREFIX
specifier|private
specifier|final
name|String
name|RECOVERY_PREFIX
init|=
literal|"recovery."
decl_stmt|;
DECL|field|shardId
specifier|private
specifier|final
name|ShardId
name|shardId
decl_stmt|;
DECL|field|recoveryId
specifier|private
specifier|final
name|long
name|recoveryId
decl_stmt|;
DECL|field|indexShard
specifier|private
specifier|final
name|IndexShard
name|indexShard
decl_stmt|;
DECL|field|sourceNode
specifier|private
specifier|final
name|DiscoveryNode
name|sourceNode
decl_stmt|;
DECL|field|tempFilePrefix
specifier|private
specifier|final
name|String
name|tempFilePrefix
decl_stmt|;
DECL|field|store
specifier|private
specifier|final
name|Store
name|store
decl_stmt|;
DECL|field|listener
specifier|private
specifier|final
name|PeerRecoveryTargetService
operator|.
name|RecoveryListener
name|listener
decl_stmt|;
DECL|field|ensureClusterStateVersionCallback
specifier|private
specifier|final
name|Callback
argument_list|<
name|Long
argument_list|>
name|ensureClusterStateVersionCallback
decl_stmt|;
DECL|field|finished
specifier|private
specifier|final
name|AtomicBoolean
name|finished
init|=
operator|new
name|AtomicBoolean
argument_list|()
decl_stmt|;
DECL|field|openIndexOutputs
specifier|private
specifier|final
name|ConcurrentMap
argument_list|<
name|String
argument_list|,
name|IndexOutput
argument_list|>
name|openIndexOutputs
init|=
name|ConcurrentCollections
operator|.
name|newConcurrentMap
argument_list|()
decl_stmt|;
DECL|field|cancellableThreads
specifier|private
specifier|final
name|CancellableThreads
name|cancellableThreads
decl_stmt|;
comment|// last time this status was accessed
DECL|field|lastAccessTime
specifier|private
specifier|volatile
name|long
name|lastAccessTime
init|=
name|System
operator|.
name|nanoTime
argument_list|()
decl_stmt|;
DECL|field|tempFileNames
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|tempFileNames
init|=
name|ConcurrentCollections
operator|.
name|newConcurrentMap
argument_list|()
decl_stmt|;
DECL|method|RecoveryTarget
specifier|private
name|RecoveryTarget
parameter_list|(
name|RecoveryTarget
name|copyFrom
parameter_list|)
block|{
comment|// copy constructor
name|this
argument_list|(
name|copyFrom
operator|.
name|indexShard
argument_list|,
name|copyFrom
operator|.
name|sourceNode
argument_list|,
name|copyFrom
operator|.
name|listener
argument_list|,
name|copyFrom
operator|.
name|cancellableThreads
argument_list|,
name|copyFrom
operator|.
name|recoveryId
argument_list|,
name|copyFrom
operator|.
name|ensureClusterStateVersionCallback
argument_list|)
expr_stmt|;
block|}
DECL|method|RecoveryTarget
specifier|public
name|RecoveryTarget
parameter_list|(
name|IndexShard
name|indexShard
parameter_list|,
name|DiscoveryNode
name|sourceNode
parameter_list|,
name|PeerRecoveryTargetService
operator|.
name|RecoveryListener
name|listener
parameter_list|,
name|Callback
argument_list|<
name|Long
argument_list|>
name|ensureClusterStateVersionCallback
parameter_list|)
block|{
name|this
argument_list|(
name|indexShard
argument_list|,
name|sourceNode
argument_list|,
name|listener
argument_list|,
operator|new
name|CancellableThreads
argument_list|()
argument_list|,
name|idGenerator
operator|.
name|incrementAndGet
argument_list|()
argument_list|,
name|ensureClusterStateVersionCallback
argument_list|)
expr_stmt|;
block|}
comment|/**      * creates a new recovery target object that represents a recovery to the provided indexShard      *      * @param indexShard local shard where we want to recover to      * @param sourceNode source node of the recovery where we recover from      * @param listener called when recovery is completed / failed      * @param ensureClusterStateVersionCallback callback to ensure that the current node is at least on a cluster state with the provided      *                                          version. Necessary for primary relocation so that new primary knows about all other ongoing      *                                          replica recoveries when replicating documents (see {@link RecoverySourceHandler}).      */
DECL|method|RecoveryTarget
specifier|private
name|RecoveryTarget
parameter_list|(
name|IndexShard
name|indexShard
parameter_list|,
name|DiscoveryNode
name|sourceNode
parameter_list|,
name|PeerRecoveryTargetService
operator|.
name|RecoveryListener
name|listener
parameter_list|,
name|CancellableThreads
name|cancellableThreads
parameter_list|,
name|long
name|recoveryId
parameter_list|,
name|Callback
argument_list|<
name|Long
argument_list|>
name|ensureClusterStateVersionCallback
parameter_list|)
block|{
name|super
argument_list|(
literal|"recovery_status"
argument_list|)
expr_stmt|;
name|this
operator|.
name|cancellableThreads
operator|=
name|cancellableThreads
expr_stmt|;
name|this
operator|.
name|recoveryId
operator|=
name|recoveryId
expr_stmt|;
name|this
operator|.
name|listener
operator|=
name|listener
expr_stmt|;
name|this
operator|.
name|logger
operator|=
name|Loggers
operator|.
name|getLogger
argument_list|(
name|getClass
argument_list|()
argument_list|,
name|indexShard
operator|.
name|indexSettings
argument_list|()
operator|.
name|getSettings
argument_list|()
argument_list|,
name|indexShard
operator|.
name|shardId
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|indexShard
operator|=
name|indexShard
expr_stmt|;
name|this
operator|.
name|sourceNode
operator|=
name|sourceNode
expr_stmt|;
name|this
operator|.
name|shardId
operator|=
name|indexShard
operator|.
name|shardId
argument_list|()
expr_stmt|;
name|this
operator|.
name|tempFilePrefix
operator|=
name|RECOVERY_PREFIX
operator|+
name|UUIDs
operator|.
name|base64UUID
argument_list|()
operator|+
literal|"."
expr_stmt|;
name|this
operator|.
name|store
operator|=
name|indexShard
operator|.
name|store
argument_list|()
expr_stmt|;
name|this
operator|.
name|ensureClusterStateVersionCallback
operator|=
name|ensureClusterStateVersionCallback
expr_stmt|;
comment|// make sure the store is not released until we are done.
name|store
operator|.
name|incRef
argument_list|()
expr_stmt|;
name|indexShard
operator|.
name|recoveryStats
argument_list|()
operator|.
name|incCurrentAsTarget
argument_list|()
expr_stmt|;
block|}
DECL|method|recoveryId
specifier|public
name|long
name|recoveryId
parameter_list|()
block|{
return|return
name|recoveryId
return|;
block|}
DECL|method|shardId
specifier|public
name|ShardId
name|shardId
parameter_list|()
block|{
return|return
name|shardId
return|;
block|}
DECL|method|indexShard
specifier|public
name|IndexShard
name|indexShard
parameter_list|()
block|{
name|ensureRefCount
argument_list|()
expr_stmt|;
return|return
name|indexShard
return|;
block|}
DECL|method|sourceNode
specifier|public
name|DiscoveryNode
name|sourceNode
parameter_list|()
block|{
return|return
name|this
operator|.
name|sourceNode
return|;
block|}
DECL|method|state
specifier|public
name|RecoveryState
name|state
parameter_list|()
block|{
return|return
name|indexShard
operator|.
name|recoveryState
argument_list|()
return|;
block|}
DECL|method|CancellableThreads
specifier|public
name|CancellableThreads
name|CancellableThreads
parameter_list|()
block|{
return|return
name|cancellableThreads
return|;
block|}
comment|/** return the last time this RecoveryStatus was used (based on System.nanoTime() */
DECL|method|lastAccessTime
specifier|public
name|long
name|lastAccessTime
parameter_list|()
block|{
return|return
name|lastAccessTime
return|;
block|}
comment|/** sets the lasAccessTime flag to now */
DECL|method|setLastAccessTime
specifier|public
name|void
name|setLastAccessTime
parameter_list|()
block|{
name|lastAccessTime
operator|=
name|System
operator|.
name|nanoTime
argument_list|()
expr_stmt|;
block|}
DECL|method|store
specifier|public
name|Store
name|store
parameter_list|()
block|{
name|ensureRefCount
argument_list|()
expr_stmt|;
return|return
name|store
return|;
block|}
DECL|method|stage
specifier|public
name|RecoveryState
operator|.
name|Stage
name|stage
parameter_list|()
block|{
return|return
name|state
argument_list|()
operator|.
name|getStage
argument_list|()
return|;
block|}
comment|/** renames all temporary files to their true name, potentially overriding existing files */
DECL|method|renameAllTempFiles
specifier|public
name|void
name|renameAllTempFiles
parameter_list|()
throws|throws
name|IOException
block|{
name|ensureRefCount
argument_list|()
expr_stmt|;
name|store
operator|.
name|renameTempFilesSafe
argument_list|(
name|tempFileNames
argument_list|)
expr_stmt|;
block|}
comment|/**      * Closes the current recovery target and returns a      * clone to reset the ongoing recovery.      * Note: the returned target must be canceled, failed or finished      * in order to release all it's reference.      */
DECL|method|resetRecovery
name|RecoveryTarget
name|resetRecovery
parameter_list|()
throws|throws
name|IOException
block|{
name|ensureRefCount
argument_list|()
expr_stmt|;
if|if
condition|(
name|finished
operator|.
name|compareAndSet
argument_list|(
literal|false
argument_list|,
literal|true
argument_list|)
condition|)
block|{
comment|// release the initial reference. recovery files will be cleaned as soon as ref count goes to zero, potentially now
name|decRef
argument_list|()
expr_stmt|;
block|}
name|indexShard
operator|.
name|performRecoveryRestart
argument_list|()
expr_stmt|;
return|return
operator|new
name|RecoveryTarget
argument_list|(
name|this
argument_list|)
return|;
block|}
comment|/**      * cancel the recovery. calling this method will clean temporary files and release the store      * unless this object is in use (in which case it will be cleaned once all ongoing users call      * {@link #decRef()}      *<p>      * if {@link #CancellableThreads()} was used, the threads will be interrupted.      */
DECL|method|cancel
specifier|public
name|void
name|cancel
parameter_list|(
name|String
name|reason
parameter_list|)
block|{
if|if
condition|(
name|finished
operator|.
name|compareAndSet
argument_list|(
literal|false
argument_list|,
literal|true
argument_list|)
condition|)
block|{
try|try
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"recovery canceled (reason: [{}])"
argument_list|,
name|reason
argument_list|)
expr_stmt|;
name|cancellableThreads
operator|.
name|cancel
argument_list|(
name|reason
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
comment|// release the initial reference. recovery files will be cleaned as soon as ref count goes to zero, potentially now
name|decRef
argument_list|()
expr_stmt|;
block|}
block|}
block|}
comment|/**      * fail the recovery and call listener      *      * @param e                exception that encapsulating the failure      * @param sendShardFailure indicates whether to notify the master of the shard failure      */
DECL|method|fail
specifier|public
name|void
name|fail
parameter_list|(
name|RecoveryFailedException
name|e
parameter_list|,
name|boolean
name|sendShardFailure
parameter_list|)
block|{
if|if
condition|(
name|finished
operator|.
name|compareAndSet
argument_list|(
literal|false
argument_list|,
literal|true
argument_list|)
condition|)
block|{
try|try
block|{
name|listener
operator|.
name|onRecoveryFailure
argument_list|(
name|state
argument_list|()
argument_list|,
name|e
argument_list|,
name|sendShardFailure
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
try|try
block|{
name|cancellableThreads
operator|.
name|cancel
argument_list|(
literal|"failed recovery ["
operator|+
name|ExceptionsHelper
operator|.
name|stackTrace
argument_list|(
name|e
argument_list|)
operator|+
literal|"]"
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
comment|// release the initial reference. recovery files will be cleaned as soon as ref count goes to zero, potentially now
name|decRef
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
comment|/** mark the current recovery as done */
DECL|method|markAsDone
specifier|public
name|void
name|markAsDone
parameter_list|()
block|{
if|if
condition|(
name|finished
operator|.
name|compareAndSet
argument_list|(
literal|false
argument_list|,
literal|true
argument_list|)
condition|)
block|{
assert|assert
name|tempFileNames
operator|.
name|isEmpty
argument_list|()
operator|:
literal|"not all temporary files are renamed"
assert|;
try|try
block|{
comment|// this might still throw an exception ie. if the shard is CLOSED due to some other event.
comment|// it's safer to decrement the reference in a try finally here.
name|indexShard
operator|.
name|postRecovery
argument_list|(
literal|"peer recovery done"
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
comment|// release the initial reference. recovery files will be cleaned as soon as ref count goes to zero, potentially now
name|decRef
argument_list|()
expr_stmt|;
block|}
name|listener
operator|.
name|onRecoveryDone
argument_list|(
name|state
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/** Get a temporary name for the provided file name. */
DECL|method|getTempNameForFile
specifier|public
name|String
name|getTempNameForFile
parameter_list|(
name|String
name|origFile
parameter_list|)
block|{
return|return
name|tempFilePrefix
operator|+
name|origFile
return|;
block|}
DECL|method|getOpenIndexOutput
specifier|public
name|IndexOutput
name|getOpenIndexOutput
parameter_list|(
name|String
name|key
parameter_list|)
block|{
name|ensureRefCount
argument_list|()
expr_stmt|;
return|return
name|openIndexOutputs
operator|.
name|get
argument_list|(
name|key
argument_list|)
return|;
block|}
comment|/** remove and {@link org.apache.lucene.store.IndexOutput} for a given file. It is the caller's responsibility to close it */
DECL|method|removeOpenIndexOutputs
specifier|public
name|IndexOutput
name|removeOpenIndexOutputs
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|ensureRefCount
argument_list|()
expr_stmt|;
return|return
name|openIndexOutputs
operator|.
name|remove
argument_list|(
name|name
argument_list|)
return|;
block|}
comment|/**      * Creates an {@link org.apache.lucene.store.IndexOutput} for the given file name. Note that the      * IndexOutput actually point at a temporary file.      *<p>      * Note: You can use {@link #getOpenIndexOutput(String)} with the same filename to retrieve the same IndexOutput      * at a later stage      */
DECL|method|openAndPutIndexOutput
specifier|public
name|IndexOutput
name|openAndPutIndexOutput
parameter_list|(
name|String
name|fileName
parameter_list|,
name|StoreFileMetaData
name|metaData
parameter_list|,
name|Store
name|store
parameter_list|)
throws|throws
name|IOException
block|{
name|ensureRefCount
argument_list|()
expr_stmt|;
name|String
name|tempFileName
init|=
name|getTempNameForFile
argument_list|(
name|fileName
argument_list|)
decl_stmt|;
if|if
condition|(
name|tempFileNames
operator|.
name|containsKey
argument_list|(
name|tempFileName
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"output for file ["
operator|+
name|fileName
operator|+
literal|"] has already been created"
argument_list|)
throw|;
block|}
comment|// add first, before it's created
name|tempFileNames
operator|.
name|put
argument_list|(
name|tempFileName
argument_list|,
name|fileName
argument_list|)
expr_stmt|;
name|IndexOutput
name|indexOutput
init|=
name|store
operator|.
name|createVerifyingOutput
argument_list|(
name|tempFileName
argument_list|,
name|metaData
argument_list|,
name|IOContext
operator|.
name|DEFAULT
argument_list|)
decl_stmt|;
name|openIndexOutputs
operator|.
name|put
argument_list|(
name|fileName
argument_list|,
name|indexOutput
argument_list|)
expr_stmt|;
return|return
name|indexOutput
return|;
block|}
annotation|@
name|Override
DECL|method|closeInternal
specifier|protected
name|void
name|closeInternal
parameter_list|()
block|{
try|try
block|{
comment|// clean open index outputs
name|Iterator
argument_list|<
name|Entry
argument_list|<
name|String
argument_list|,
name|IndexOutput
argument_list|>
argument_list|>
name|iterator
init|=
name|openIndexOutputs
operator|.
name|entrySet
argument_list|()
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|iterator
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|IndexOutput
argument_list|>
name|entry
init|=
name|iterator
operator|.
name|next
argument_list|()
decl_stmt|;
name|logger
operator|.
name|trace
argument_list|(
literal|"closing IndexOutput file [{}]"
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
name|entry
operator|.
name|getValue
argument_list|()
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
name|logger
operator|.
name|debug
argument_list|(
call|(
name|Supplier
argument_list|<
name|?
argument_list|>
call|)
argument_list|()
operator|->
operator|new
name|ParameterizedMessage
argument_list|(
literal|"error while closing recovery output [{}]"
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
name|iterator
operator|.
name|remove
argument_list|()
expr_stmt|;
block|}
comment|// trash temporary files
for|for
control|(
name|String
name|file
range|:
name|tempFileNames
operator|.
name|keySet
argument_list|()
control|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"cleaning temporary file [{}]"
argument_list|,
name|file
argument_list|)
expr_stmt|;
name|store
operator|.
name|deleteQuiet
argument_list|(
name|file
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
comment|// free store. increment happens in constructor
name|store
operator|.
name|decRef
argument_list|()
expr_stmt|;
name|indexShard
operator|.
name|recoveryStats
argument_list|()
operator|.
name|decCurrentAsTarget
argument_list|()
expr_stmt|;
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
name|shardId
operator|+
literal|" ["
operator|+
name|recoveryId
operator|+
literal|"]"
return|;
block|}
DECL|method|ensureRefCount
specifier|private
name|void
name|ensureRefCount
parameter_list|()
block|{
if|if
condition|(
name|refCount
argument_list|()
operator|<=
literal|0
condition|)
block|{
throw|throw
operator|new
name|ElasticsearchException
argument_list|(
literal|"RecoveryStatus is used but it's refcount is 0. Probably a mismatch between incRef/decRef "
operator|+
literal|"calls"
argument_list|)
throw|;
block|}
block|}
comment|/*** Implementation of {@link RecoveryTargetHandler } */
annotation|@
name|Override
DECL|method|prepareForTranslogOperations
specifier|public
name|void
name|prepareForTranslogOperations
parameter_list|(
name|int
name|totalTranslogOps
parameter_list|,
name|long
name|maxUnsafeAutoIdTimestamp
parameter_list|)
throws|throws
name|IOException
block|{
name|state
argument_list|()
operator|.
name|getTranslog
argument_list|()
operator|.
name|totalOperations
argument_list|(
name|totalTranslogOps
argument_list|)
expr_stmt|;
name|indexShard
argument_list|()
operator|.
name|skipTranslogRecovery
argument_list|(
name|maxUnsafeAutoIdTimestamp
argument_list|)
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
specifier|final
name|IndexShard
name|indexShard
init|=
name|indexShard
argument_list|()
decl_stmt|;
name|indexShard
operator|.
name|finalizeRecovery
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getTargetAllocationId
specifier|public
name|String
name|getTargetAllocationId
parameter_list|()
block|{
return|return
name|indexShard
argument_list|()
operator|.
name|routingEntry
argument_list|()
operator|.
name|allocationId
argument_list|()
operator|.
name|getId
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|ensureClusterStateVersion
specifier|public
name|void
name|ensureClusterStateVersion
parameter_list|(
name|long
name|clusterStateVersion
parameter_list|)
block|{
name|ensureClusterStateVersionCallback
operator|.
name|handle
argument_list|(
name|clusterStateVersion
argument_list|)
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
throws|throws
name|TranslogRecoveryPerformer
operator|.
name|BatchOperationException
block|{
specifier|final
name|RecoveryState
operator|.
name|Translog
name|translog
init|=
name|state
argument_list|()
operator|.
name|getTranslog
argument_list|()
decl_stmt|;
name|translog
operator|.
name|totalOperations
argument_list|(
name|totalTranslogOps
argument_list|)
expr_stmt|;
assert|assert
name|indexShard
argument_list|()
operator|.
name|recoveryState
argument_list|()
operator|==
name|state
argument_list|()
assert|;
name|indexShard
argument_list|()
operator|.
name|performBatchRecovery
argument_list|(
name|operations
argument_list|)
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
specifier|final
name|RecoveryState
operator|.
name|Index
name|index
init|=
name|state
argument_list|()
operator|.
name|getIndex
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|phase1ExistingFileNames
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|index
operator|.
name|addFileDetail
argument_list|(
name|phase1ExistingFileNames
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|phase1ExistingFileSizes
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|phase1FileNames
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|index
operator|.
name|addFileDetail
argument_list|(
name|phase1FileNames
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
name|phase1FileSizes
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
name|state
argument_list|()
operator|.
name|getTranslog
argument_list|()
operator|.
name|totalOperations
argument_list|(
name|totalTranslogOps
argument_list|)
expr_stmt|;
name|state
argument_list|()
operator|.
name|getTranslog
argument_list|()
operator|.
name|totalOperationsOnStart
argument_list|(
name|totalTranslogOps
argument_list|)
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
name|state
argument_list|()
operator|.
name|getTranslog
argument_list|()
operator|.
name|totalOperations
argument_list|(
name|totalTranslogOps
argument_list|)
expr_stmt|;
comment|// first, we go and move files that were created with the recovery id suffix to
comment|// the actual names, its ok if we have a corrupted index here, since we have replicas
comment|// to recover from in case of a full cluster shutdown just when this code executes...
name|renameAllTempFiles
argument_list|()
expr_stmt|;
specifier|final
name|Store
name|store
init|=
name|store
argument_list|()
decl_stmt|;
try|try
block|{
name|store
operator|.
name|cleanupAndVerify
argument_list|(
literal|"recovery CleanFilesRequestHandler"
argument_list|,
name|sourceMetaData
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|CorruptIndexException
decl||
name|IndexFormatTooNewException
decl||
name|IndexFormatTooOldException
name|ex
parameter_list|)
block|{
comment|// this is a fatal exception at this stage.
comment|// this means we transferred files from the remote that have not be checksummed and they are
comment|// broken. We have to clean up this shard entirely, remove all files and bubble it up to the
comment|// source shard since this index might be broken there as well? The Source can handle this and checks
comment|// its content on disk if possible.
try|try
block|{
try|try
block|{
name|store
operator|.
name|removeCorruptionMarker
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
name|Lucene
operator|.
name|cleanLuceneIndex
argument_list|(
name|store
operator|.
name|directory
argument_list|()
argument_list|)
expr_stmt|;
comment|// clean up and delete all files
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
name|debug
argument_list|(
literal|"Failed to clean lucene index"
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|ex
operator|.
name|addSuppressed
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
name|RecoveryFailedException
name|rfe
init|=
operator|new
name|RecoveryFailedException
argument_list|(
name|state
argument_list|()
argument_list|,
literal|"failed to clean after recovery"
argument_list|,
name|ex
argument_list|)
decl_stmt|;
name|fail
argument_list|(
name|rfe
argument_list|,
literal|true
argument_list|)
expr_stmt|;
throw|throw
name|rfe
throw|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
name|RecoveryFailedException
name|rfe
init|=
operator|new
name|RecoveryFailedException
argument_list|(
name|state
argument_list|()
argument_list|,
literal|"failed to clean after recovery"
argument_list|,
name|ex
argument_list|)
decl_stmt|;
name|fail
argument_list|(
name|rfe
argument_list|,
literal|true
argument_list|)
expr_stmt|;
throw|throw
name|rfe
throw|;
block|}
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
specifier|final
name|Store
name|store
init|=
name|store
argument_list|()
decl_stmt|;
specifier|final
name|String
name|name
init|=
name|fileMetaData
operator|.
name|name
argument_list|()
decl_stmt|;
name|state
argument_list|()
operator|.
name|getTranslog
argument_list|()
operator|.
name|totalOperations
argument_list|(
name|totalTranslogOps
argument_list|)
expr_stmt|;
specifier|final
name|RecoveryState
operator|.
name|Index
name|indexState
init|=
name|state
argument_list|()
operator|.
name|getIndex
argument_list|()
decl_stmt|;
name|IndexOutput
name|indexOutput
decl_stmt|;
if|if
condition|(
name|position
operator|==
literal|0
condition|)
block|{
name|indexOutput
operator|=
name|openAndPutIndexOutput
argument_list|(
name|name
argument_list|,
name|fileMetaData
argument_list|,
name|store
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|indexOutput
operator|=
name|getOpenIndexOutput
argument_list|(
name|name
argument_list|)
expr_stmt|;
block|}
name|BytesRefIterator
name|iterator
init|=
name|content
operator|.
name|iterator
argument_list|()
decl_stmt|;
name|BytesRef
name|scratch
decl_stmt|;
while|while
condition|(
operator|(
name|scratch
operator|=
name|iterator
operator|.
name|next
argument_list|()
operator|)
operator|!=
literal|null
condition|)
block|{
comment|// we iterate over all pages - this is a 0-copy for all core impls
name|indexOutput
operator|.
name|writeBytes
argument_list|(
name|scratch
operator|.
name|bytes
argument_list|,
name|scratch
operator|.
name|offset
argument_list|,
name|scratch
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
name|indexState
operator|.
name|addRecoveredBytesToFile
argument_list|(
name|name
argument_list|,
name|content
operator|.
name|length
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|indexOutput
operator|.
name|getFilePointer
argument_list|()
operator|>=
name|fileMetaData
operator|.
name|length
argument_list|()
operator|||
name|lastChunk
condition|)
block|{
try|try
block|{
name|Store
operator|.
name|verify
argument_list|(
name|indexOutput
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
comment|// we are done
name|indexOutput
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
specifier|final
name|String
name|temporaryFileName
init|=
name|getTempNameForFile
argument_list|(
name|name
argument_list|)
decl_stmt|;
assert|assert
name|Arrays
operator|.
name|asList
argument_list|(
name|store
operator|.
name|directory
argument_list|()
operator|.
name|listAll
argument_list|()
argument_list|)
operator|.
name|contains
argument_list|(
name|temporaryFileName
argument_list|)
operator|:
literal|"expected: ["
operator|+
name|temporaryFileName
operator|+
literal|"] in "
operator|+
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
assert|;
name|store
operator|.
name|directory
argument_list|()
operator|.
name|sync
argument_list|(
name|Collections
operator|.
name|singleton
argument_list|(
name|temporaryFileName
argument_list|)
argument_list|)
expr_stmt|;
name|IndexOutput
name|remove
init|=
name|removeOpenIndexOutputs
argument_list|(
name|name
argument_list|)
decl_stmt|;
assert|assert
name|remove
operator|==
literal|null
operator|||
name|remove
operator|==
name|indexOutput
assert|;
comment|// remove maybe null if we got finished
block|}
block|}
block|}
end_class

end_unit

