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
name|Iterator
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
comment|/**  *  */
end_comment

begin_class
DECL|class|RecoveryStatus
specifier|public
class|class
name|RecoveryStatus
extends|extends
name|AbstractRefCounted
block|{
DECL|field|logger
specifier|private
specifier|final
name|ESLogger
name|logger
decl_stmt|;
DECL|field|idGenerator
specifier|private
specifier|final
specifier|static
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
name|RecoveryTarget
operator|.
name|RecoveryListener
name|listener
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
DECL|field|legacyChecksums
specifier|private
specifier|final
name|Store
operator|.
name|LegacyChecksums
name|legacyChecksums
init|=
operator|new
name|Store
operator|.
name|LegacyChecksums
argument_list|()
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
DECL|method|RecoveryStatus
specifier|public
name|RecoveryStatus
parameter_list|(
name|IndexShard
name|indexShard
parameter_list|,
name|DiscoveryNode
name|sourceNode
parameter_list|,
name|RecoveryTarget
operator|.
name|RecoveryListener
name|listener
parameter_list|)
block|{
name|super
argument_list|(
literal|"recovery_status"
argument_list|)
expr_stmt|;
name|this
operator|.
name|recoveryId
operator|=
name|idGenerator
operator|.
name|incrementAndGet
argument_list|()
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
name|indexShard
operator|.
name|recoveryState
argument_list|()
operator|.
name|getTimer
argument_list|()
operator|.
name|startTime
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
comment|// make sure the store is not released until we are done.
name|store
operator|.
name|incRef
argument_list|()
expr_stmt|;
block|}
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
DECL|method|legacyChecksums
specifier|public
name|Store
operator|.
name|LegacyChecksums
name|legacyChecksums
parameter_list|()
block|{
return|return
name|legacyChecksums
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
name|renameFilesSafe
argument_list|(
name|tempFileNames
argument_list|)
expr_stmt|;
block|}
comment|/**      * cancel the recovery. calling this method will clean temporary files and release the store      * unless this object is in use (in which case it will be cleaned once all ongoing users call      * {@link #decRef()}      *<p/>      * if {@link #CancellableThreads()} was used, the threads will be interrupted.      */
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
name|e
operator|.
name|getMessage
argument_list|()
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
name|indexShard
operator|.
name|postRecovery
argument_list|(
literal|"peer recovery done"
argument_list|)
expr_stmt|;
comment|// release the initial reference. recovery files will be cleaned as soon as ref count goes to zero, potentially now
name|decRef
argument_list|()
expr_stmt|;
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
comment|/**      * Creates an {@link org.apache.lucene.store.IndexOutput} for the given file name. Note that the      * IndexOutput actually point at a temporary file.      *<p/>      * Note: You can use {@link #getOpenIndexOutput(String)} with the same filename to retrieve the same IndexOutput      * at a later stage      */
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
DECL|method|resetRecovery
specifier|public
name|void
name|resetRecovery
parameter_list|()
throws|throws
name|IOException
block|{
name|cleanOpenFiles
argument_list|()
expr_stmt|;
name|indexShard
argument_list|()
operator|.
name|performRecoveryRestart
argument_list|()
expr_stmt|;
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
name|cleanOpenFiles
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
comment|// free store. increment happens in constructor
name|store
operator|.
name|decRef
argument_list|()
expr_stmt|;
block|}
block|}
DECL|method|cleanOpenFiles
specifier|protected
name|void
name|cleanOpenFiles
parameter_list|()
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
name|Throwable
name|t
parameter_list|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"error while closing recovery output [{}]"
argument_list|,
name|t
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
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
name|legacyChecksums
operator|.
name|clear
argument_list|()
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
literal|"RecoveryStatus is used but it's refcount is 0. Probably a mismatch between incRef/decRef calls"
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

