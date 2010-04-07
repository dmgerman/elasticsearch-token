begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.engine.robin
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|engine
operator|.
name|robin
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
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
name|apache
operator|.
name|lucene
operator|.
name|index
operator|.
name|IndexReader
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
name|LogMergePolicy
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
name|search
operator|.
name|IndexSearcher
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
name|index
operator|.
name|analysis
operator|.
name|AnalysisService
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
name|SnapshotDeletionPolicy
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
name|merge
operator|.
name|policy
operator|.
name|MergePolicyProvider
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
name|merge
operator|.
name|scheduler
operator|.
name|MergeSchedulerProvider
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
name|similarity
operator|.
name|SimilarityService
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
name|util
operator|.
name|Preconditions
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|SizeUnit
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|SizeValue
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
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
name|util
operator|.
name|concurrent
operator|.
name|resource
operator|.
name|AcquirableResource
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|lucene
operator|.
name|IndexWriters
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|lucene
operator|.
name|ReaderSearcherHolder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|settings
operator|.
name|Settings
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
name|locks
operator|.
name|ReadWriteLock
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
name|locks
operator|.
name|ReentrantReadWriteLock
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|TimeValue
operator|.
name|*
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|concurrent
operator|.
name|resource
operator|.
name|AcquirableResourceFactory
operator|.
name|*
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|lucene
operator|.
name|Lucene
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|RobinEngine
specifier|public
class|class
name|RobinEngine
extends|extends
name|AbstractIndexShardComponent
implements|implements
name|Engine
implements|,
name|ScheduledRefreshableEngine
block|{
DECL|field|ramBufferSize
specifier|private
specifier|final
name|SizeValue
name|ramBufferSize
decl_stmt|;
DECL|field|refreshInterval
specifier|private
specifier|final
name|TimeValue
name|refreshInterval
decl_stmt|;
DECL|field|termIndexInterval
specifier|private
specifier|final
name|int
name|termIndexInterval
decl_stmt|;
DECL|field|rwl
specifier|private
specifier|final
name|ReadWriteLock
name|rwl
init|=
operator|new
name|ReentrantReadWriteLock
argument_list|()
decl_stmt|;
DECL|field|refreshMutex
specifier|private
specifier|final
name|AtomicBoolean
name|refreshMutex
init|=
operator|new
name|AtomicBoolean
argument_list|()
decl_stmt|;
DECL|field|optimizeMutex
specifier|private
specifier|final
name|AtomicBoolean
name|optimizeMutex
init|=
operator|new
name|AtomicBoolean
argument_list|()
decl_stmt|;
DECL|field|store
specifier|private
specifier|final
name|Store
name|store
decl_stmt|;
DECL|field|deletionPolicy
specifier|private
specifier|final
name|SnapshotDeletionPolicy
name|deletionPolicy
decl_stmt|;
DECL|field|translog
specifier|private
specifier|final
name|Translog
name|translog
decl_stmt|;
DECL|field|mergePolicyProvider
specifier|private
specifier|final
name|MergePolicyProvider
name|mergePolicyProvider
decl_stmt|;
DECL|field|mergeScheduler
specifier|private
specifier|final
name|MergeSchedulerProvider
name|mergeScheduler
decl_stmt|;
DECL|field|analysisService
specifier|private
specifier|final
name|AnalysisService
name|analysisService
decl_stmt|;
DECL|field|similarityService
specifier|private
specifier|final
name|SimilarityService
name|similarityService
decl_stmt|;
DECL|field|indexWriter
specifier|private
specifier|volatile
name|IndexWriter
name|indexWriter
decl_stmt|;
DECL|field|nrtResource
specifier|private
specifier|volatile
name|AcquirableResource
argument_list|<
name|ReaderSearcherHolder
argument_list|>
name|nrtResource
decl_stmt|;
DECL|field|closed
specifier|private
specifier|volatile
name|boolean
name|closed
init|=
literal|false
decl_stmt|;
comment|// flag indicating if a dirty operation has occurred since the last refresh
DECL|field|dirty
specifier|private
specifier|volatile
name|boolean
name|dirty
init|=
literal|false
decl_stmt|;
DECL|field|disableFlushCounter
specifier|private
specifier|volatile
name|int
name|disableFlushCounter
init|=
literal|0
decl_stmt|;
DECL|method|RobinEngine
annotation|@
name|Inject
specifier|public
name|RobinEngine
parameter_list|(
name|ShardId
name|shardId
parameter_list|,
annotation|@
name|IndexSettings
name|Settings
name|indexSettings
parameter_list|,
name|Store
name|store
parameter_list|,
name|SnapshotDeletionPolicy
name|deletionPolicy
parameter_list|,
name|Translog
name|translog
parameter_list|,
name|MergePolicyProvider
name|mergePolicyProvider
parameter_list|,
name|MergeSchedulerProvider
name|mergeScheduler
parameter_list|,
name|AnalysisService
name|analysisService
parameter_list|,
name|SimilarityService
name|similarityService
parameter_list|)
throws|throws
name|EngineException
block|{
name|super
argument_list|(
name|shardId
argument_list|,
name|indexSettings
argument_list|)
expr_stmt|;
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|store
argument_list|,
literal|"Store must be provided to the engine"
argument_list|)
expr_stmt|;
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|deletionPolicy
argument_list|,
literal|"Snapshot deletion policy must be provided to the engine"
argument_list|)
expr_stmt|;
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|translog
argument_list|,
literal|"Translog must be provided to the engine"
argument_list|)
expr_stmt|;
name|this
operator|.
name|ramBufferSize
operator|=
name|componentSettings
operator|.
name|getAsSize
argument_list|(
literal|"ram_buffer_size"
argument_list|,
operator|new
name|SizeValue
argument_list|(
literal|64
argument_list|,
name|SizeUnit
operator|.
name|MB
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|refreshInterval
operator|=
name|componentSettings
operator|.
name|getAsTime
argument_list|(
literal|"refresh_interval"
argument_list|,
name|timeValueSeconds
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|termIndexInterval
operator|=
name|componentSettings
operator|.
name|getAsInt
argument_list|(
literal|"term_index_interval"
argument_list|,
name|IndexWriter
operator|.
name|DEFAULT_TERM_INDEX_INTERVAL
argument_list|)
expr_stmt|;
name|this
operator|.
name|store
operator|=
name|store
expr_stmt|;
name|this
operator|.
name|deletionPolicy
operator|=
name|deletionPolicy
expr_stmt|;
name|this
operator|.
name|translog
operator|=
name|translog
expr_stmt|;
name|this
operator|.
name|mergePolicyProvider
operator|=
name|mergePolicyProvider
expr_stmt|;
name|this
operator|.
name|mergeScheduler
operator|=
name|mergeScheduler
expr_stmt|;
name|this
operator|.
name|analysisService
operator|=
name|analysisService
expr_stmt|;
name|this
operator|.
name|similarityService
operator|=
name|similarityService
expr_stmt|;
block|}
DECL|method|start
annotation|@
name|Override
specifier|public
name|void
name|start
parameter_list|()
throws|throws
name|EngineException
block|{
if|if
condition|(
name|indexWriter
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|EngineAlreadyStartedException
argument_list|(
name|shardId
argument_list|)
throw|;
block|}
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
literal|"Starting engine with ram_buffer_size ["
operator|+
name|ramBufferSize
operator|+
literal|"], refresh_interval ["
operator|+
name|refreshInterval
operator|+
literal|"]"
argument_list|)
expr_stmt|;
block|}
name|IndexWriter
name|indexWriter
init|=
literal|null
decl_stmt|;
try|try
block|{
comment|// release locks when started
if|if
condition|(
name|IndexWriter
operator|.
name|isLocked
argument_list|(
name|store
operator|.
name|directory
argument_list|()
argument_list|)
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"Shard is locked, releasing lock"
argument_list|)
expr_stmt|;
name|store
operator|.
name|directory
argument_list|()
operator|.
name|clearLock
argument_list|(
name|IndexWriter
operator|.
name|WRITE_LOCK_NAME
argument_list|)
expr_stmt|;
block|}
name|boolean
name|create
init|=
operator|!
name|IndexReader
operator|.
name|indexExists
argument_list|(
name|store
operator|.
name|directory
argument_list|()
argument_list|)
decl_stmt|;
name|indexWriter
operator|=
operator|new
name|IndexWriter
argument_list|(
name|store
operator|.
name|directory
argument_list|()
argument_list|,
name|analysisService
operator|.
name|defaultIndexAnalyzer
argument_list|()
argument_list|,
name|create
argument_list|,
name|deletionPolicy
argument_list|,
name|IndexWriter
operator|.
name|MaxFieldLength
operator|.
name|UNLIMITED
argument_list|)
expr_stmt|;
name|indexWriter
operator|.
name|setMergeScheduler
argument_list|(
name|mergeScheduler
operator|.
name|newMergeScheduler
argument_list|()
argument_list|)
expr_stmt|;
name|indexWriter
operator|.
name|setMergePolicy
argument_list|(
name|mergePolicyProvider
operator|.
name|newMergePolicy
argument_list|(
name|indexWriter
argument_list|)
argument_list|)
expr_stmt|;
name|indexWriter
operator|.
name|setSimilarity
argument_list|(
name|similarityService
operator|.
name|defaultIndexSimilarity
argument_list|()
argument_list|)
expr_stmt|;
name|indexWriter
operator|.
name|setRAMBufferSizeMB
argument_list|(
name|ramBufferSize
operator|.
name|mbFrac
argument_list|()
argument_list|)
expr_stmt|;
name|indexWriter
operator|.
name|setTermIndexInterval
argument_list|(
name|termIndexInterval
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|safeClose
argument_list|(
name|indexWriter
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|EngineCreationFailureException
argument_list|(
name|shardId
argument_list|,
literal|"Failed to create engine"
argument_list|,
name|e
argument_list|)
throw|;
block|}
name|this
operator|.
name|indexWriter
operator|=
name|indexWriter
expr_stmt|;
try|try
block|{
name|IndexReader
name|indexReader
init|=
name|indexWriter
operator|.
name|getReader
argument_list|()
decl_stmt|;
name|IndexSearcher
name|indexSearcher
init|=
operator|new
name|IndexSearcher
argument_list|(
name|indexReader
argument_list|)
decl_stmt|;
name|indexSearcher
operator|.
name|setSimilarity
argument_list|(
name|similarityService
operator|.
name|defaultSearchSimilarity
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|nrtResource
operator|=
name|newAcquirableResource
argument_list|(
operator|new
name|ReaderSearcherHolder
argument_list|(
name|indexReader
argument_list|,
name|indexSearcher
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
try|try
block|{
name|indexWriter
operator|.
name|rollback
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e1
parameter_list|)
block|{
comment|// ignore
block|}
finally|finally
block|{
try|try
block|{
name|indexWriter
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e1
parameter_list|)
block|{
comment|// ignore
block|}
block|}
throw|throw
operator|new
name|EngineCreationFailureException
argument_list|(
name|shardId
argument_list|,
literal|"Failed to open reader on writer"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
DECL|method|refreshInterval
annotation|@
name|Override
specifier|public
name|TimeValue
name|refreshInterval
parameter_list|()
block|{
return|return
name|refreshInterval
return|;
block|}
DECL|method|create
annotation|@
name|Override
specifier|public
name|void
name|create
parameter_list|(
name|Create
name|create
parameter_list|)
throws|throws
name|EngineException
block|{
name|rwl
operator|.
name|readLock
argument_list|()
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
name|indexWriter
operator|.
name|addDocument
argument_list|(
name|create
operator|.
name|doc
argument_list|()
argument_list|,
name|create
operator|.
name|analyzer
argument_list|()
argument_list|)
expr_stmt|;
name|translog
operator|.
name|add
argument_list|(
operator|new
name|Translog
operator|.
name|Create
argument_list|(
name|create
argument_list|)
argument_list|)
expr_stmt|;
name|dirty
operator|=
literal|true
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
name|CreateFailedEngineException
argument_list|(
name|shardId
argument_list|,
name|create
argument_list|,
name|e
argument_list|)
throw|;
block|}
finally|finally
block|{
name|rwl
operator|.
name|readLock
argument_list|()
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
DECL|method|index
annotation|@
name|Override
specifier|public
name|void
name|index
parameter_list|(
name|Index
name|index
parameter_list|)
throws|throws
name|EngineException
block|{
name|rwl
operator|.
name|readLock
argument_list|()
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
name|indexWriter
operator|.
name|updateDocument
argument_list|(
name|index
operator|.
name|uid
argument_list|()
argument_list|,
name|index
operator|.
name|doc
argument_list|()
argument_list|,
name|index
operator|.
name|analyzer
argument_list|()
argument_list|)
expr_stmt|;
name|translog
operator|.
name|add
argument_list|(
operator|new
name|Translog
operator|.
name|Index
argument_list|(
name|index
argument_list|)
argument_list|)
expr_stmt|;
name|dirty
operator|=
literal|true
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
name|IndexFailedEngineException
argument_list|(
name|shardId
argument_list|,
name|index
argument_list|,
name|e
argument_list|)
throw|;
block|}
finally|finally
block|{
name|rwl
operator|.
name|readLock
argument_list|()
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
DECL|method|delete
annotation|@
name|Override
specifier|public
name|void
name|delete
parameter_list|(
name|Delete
name|delete
parameter_list|)
throws|throws
name|EngineException
block|{
name|rwl
operator|.
name|readLock
argument_list|()
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
name|indexWriter
operator|.
name|deleteDocuments
argument_list|(
name|delete
operator|.
name|uid
argument_list|()
argument_list|)
expr_stmt|;
name|translog
operator|.
name|add
argument_list|(
operator|new
name|Translog
operator|.
name|Delete
argument_list|(
name|delete
argument_list|)
argument_list|)
expr_stmt|;
name|dirty
operator|=
literal|true
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
name|DeleteFailedEngineException
argument_list|(
name|shardId
argument_list|,
name|delete
argument_list|,
name|e
argument_list|)
throw|;
block|}
finally|finally
block|{
name|rwl
operator|.
name|readLock
argument_list|()
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
DECL|method|delete
annotation|@
name|Override
specifier|public
name|void
name|delete
parameter_list|(
name|DeleteByQuery
name|delete
parameter_list|)
throws|throws
name|EngineException
block|{
name|rwl
operator|.
name|readLock
argument_list|()
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
name|indexWriter
operator|.
name|deleteDocuments
argument_list|(
name|delete
operator|.
name|query
argument_list|()
argument_list|)
expr_stmt|;
name|translog
operator|.
name|add
argument_list|(
operator|new
name|Translog
operator|.
name|DeleteByQuery
argument_list|(
name|delete
argument_list|)
argument_list|)
expr_stmt|;
name|dirty
operator|=
literal|true
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
name|DeleteByQueryFailedEngineException
argument_list|(
name|shardId
argument_list|,
name|delete
argument_list|,
name|e
argument_list|)
throw|;
block|}
finally|finally
block|{
name|rwl
operator|.
name|readLock
argument_list|()
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
DECL|method|searcher
annotation|@
name|Override
specifier|public
name|Searcher
name|searcher
parameter_list|()
throws|throws
name|EngineException
block|{
name|AcquirableResource
argument_list|<
name|ReaderSearcherHolder
argument_list|>
name|holder
decl_stmt|;
for|for
control|(
init|;
condition|;
control|)
block|{
name|holder
operator|=
name|this
operator|.
name|nrtResource
expr_stmt|;
if|if
condition|(
name|holder
operator|.
name|acquire
argument_list|()
condition|)
block|{
break|break;
block|}
name|Thread
operator|.
name|yield
argument_list|()
expr_stmt|;
block|}
return|return
operator|new
name|RobinSearchResult
argument_list|(
name|holder
argument_list|)
return|;
block|}
DECL|method|estimateFlushableMemorySize
annotation|@
name|Override
specifier|public
name|SizeValue
name|estimateFlushableMemorySize
parameter_list|()
block|{
name|rwl
operator|.
name|readLock
argument_list|()
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
name|long
name|bytes
init|=
name|IndexWriters
operator|.
name|estimateRamSize
argument_list|(
name|indexWriter
argument_list|)
decl_stmt|;
name|bytes
operator|+=
name|translog
operator|.
name|estimateMemorySize
argument_list|()
operator|.
name|bytes
argument_list|()
expr_stmt|;
return|return
operator|new
name|SizeValue
argument_list|(
name|bytes
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
finally|finally
block|{
name|rwl
operator|.
name|readLock
argument_list|()
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
DECL|method|refresh
annotation|@
name|Override
specifier|public
name|void
name|refresh
parameter_list|(
name|Refresh
name|refresh
parameter_list|)
throws|throws
name|EngineException
block|{
comment|// this engine always acts as if waitForOperations=true
if|if
condition|(
name|refreshMutex
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
if|if
condition|(
name|dirty
condition|)
block|{
name|dirty
operator|=
literal|false
expr_stmt|;
name|AcquirableResource
argument_list|<
name|ReaderSearcherHolder
argument_list|>
name|current
init|=
name|nrtResource
decl_stmt|;
name|IndexReader
name|newReader
init|=
name|current
operator|.
name|resource
argument_list|()
operator|.
name|reader
argument_list|()
operator|.
name|reopen
argument_list|(
literal|true
argument_list|)
decl_stmt|;
if|if
condition|(
name|newReader
operator|!=
name|current
operator|.
name|resource
argument_list|()
operator|.
name|reader
argument_list|()
condition|)
block|{
name|nrtResource
operator|=
name|newAcquirableResource
argument_list|(
operator|new
name|ReaderSearcherHolder
argument_list|(
name|newReader
argument_list|)
argument_list|)
expr_stmt|;
name|current
operator|.
name|markForClose
argument_list|()
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
throw|throw
operator|new
name|RefreshFailedEngineException
argument_list|(
name|shardId
argument_list|,
name|e
argument_list|)
throw|;
block|}
finally|finally
block|{
name|refreshMutex
operator|.
name|set
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|flush
annotation|@
name|Override
specifier|public
name|void
name|flush
parameter_list|(
name|Flush
name|flush
parameter_list|)
throws|throws
name|EngineException
block|{
comment|// check outside the lock as well so we can check without blocking on the write lock
if|if
condition|(
name|disableFlushCounter
operator|>
literal|0
condition|)
block|{
throw|throw
operator|new
name|FlushNotAllowedEngineException
argument_list|(
name|shardId
argument_list|,
literal|"Recovery is in progress, flush is not allowed"
argument_list|)
throw|;
block|}
name|rwl
operator|.
name|writeLock
argument_list|()
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
if|if
condition|(
name|disableFlushCounter
operator|>
literal|0
condition|)
block|{
throw|throw
operator|new
name|FlushNotAllowedEngineException
argument_list|(
name|shardId
argument_list|,
literal|"Recovery is in progress, flush is not allowed"
argument_list|)
throw|;
block|}
try|try
block|{
name|indexWriter
operator|.
name|commit
argument_list|()
expr_stmt|;
name|translog
operator|.
name|newTranslog
argument_list|()
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
name|FlushFailedEngineException
argument_list|(
name|shardId
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
finally|finally
block|{
name|rwl
operator|.
name|writeLock
argument_list|()
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|flush
operator|.
name|refresh
argument_list|()
condition|)
block|{
name|refresh
argument_list|(
operator|new
name|Refresh
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|optimize
annotation|@
name|Override
specifier|public
name|void
name|optimize
parameter_list|(
name|Optimize
name|optimize
parameter_list|)
throws|throws
name|EngineException
block|{
if|if
condition|(
name|optimizeMutex
operator|.
name|compareAndSet
argument_list|(
literal|false
argument_list|,
literal|true
argument_list|)
condition|)
block|{
name|rwl
operator|.
name|readLock
argument_list|()
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
name|int
name|maxNumberOfSegments
init|=
name|optimize
operator|.
name|maxNumSegments
argument_list|()
decl_stmt|;
if|if
condition|(
name|maxNumberOfSegments
operator|==
operator|-
literal|1
condition|)
block|{
comment|// not set, optimize down to half the configured number of segments
if|if
condition|(
name|indexWriter
operator|.
name|getMergePolicy
argument_list|()
operator|instanceof
name|LogMergePolicy
condition|)
block|{
name|maxNumberOfSegments
operator|=
operator|(
operator|(
name|LogMergePolicy
operator|)
name|indexWriter
operator|.
name|getMergePolicy
argument_list|()
operator|)
operator|.
name|getMergeFactor
argument_list|()
operator|/
literal|2
expr_stmt|;
if|if
condition|(
name|maxNumberOfSegments
operator|<
literal|0
condition|)
block|{
name|maxNumberOfSegments
operator|=
literal|1
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|optimize
operator|.
name|onlyExpungeDeletes
argument_list|()
condition|)
block|{
name|indexWriter
operator|.
name|expungeDeletes
argument_list|(
name|optimize
operator|.
name|waitForMerge
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|indexWriter
operator|.
name|optimize
argument_list|(
name|maxNumberOfSegments
argument_list|,
name|optimize
operator|.
name|waitForMerge
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// once we did the optimization, we are "dirty" since we removed deletes potentially which
comment|// affects TermEnum
name|dirty
operator|=
literal|true
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|OptimizeFailedEngineException
argument_list|(
name|shardId
argument_list|,
name|e
argument_list|)
throw|;
block|}
finally|finally
block|{
name|rwl
operator|.
name|readLock
argument_list|()
operator|.
name|unlock
argument_list|()
expr_stmt|;
name|optimizeMutex
operator|.
name|set
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|optimize
operator|.
name|flush
argument_list|()
condition|)
block|{
name|flush
argument_list|(
operator|new
name|Flush
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|optimize
operator|.
name|refresh
argument_list|()
condition|)
block|{
name|refresh
argument_list|(
operator|new
name|Refresh
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|snapshot
annotation|@
name|Override
specifier|public
name|void
name|snapshot
parameter_list|(
name|SnapshotHandler
name|snapshotHandler
parameter_list|)
throws|throws
name|EngineException
block|{
name|SnapshotIndexCommit
name|snapshotIndexCommit
init|=
literal|null
decl_stmt|;
name|Translog
operator|.
name|Snapshot
name|traslogSnapshot
init|=
literal|null
decl_stmt|;
name|rwl
operator|.
name|readLock
argument_list|()
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
name|snapshotIndexCommit
operator|=
name|deletionPolicy
operator|.
name|snapshot
argument_list|()
expr_stmt|;
name|traslogSnapshot
operator|=
name|translog
operator|.
name|snapshot
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
name|snapshotIndexCommit
operator|!=
literal|null
condition|)
name|snapshotIndexCommit
operator|.
name|release
argument_list|()
expr_stmt|;
throw|throw
operator|new
name|SnapshotFailedEngineException
argument_list|(
name|shardId
argument_list|,
name|e
argument_list|)
throw|;
block|}
finally|finally
block|{
name|rwl
operator|.
name|readLock
argument_list|()
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
try|try
block|{
name|snapshotHandler
operator|.
name|snapshot
argument_list|(
name|snapshotIndexCommit
argument_list|,
name|traslogSnapshot
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|snapshotIndexCommit
operator|.
name|release
argument_list|()
expr_stmt|;
name|traslogSnapshot
operator|.
name|release
argument_list|()
expr_stmt|;
block|}
block|}
DECL|method|recover
annotation|@
name|Override
specifier|public
name|void
name|recover
parameter_list|(
name|RecoveryHandler
name|recoveryHandler
parameter_list|)
throws|throws
name|EngineException
block|{
comment|// take a write lock here so it won't happen while a flush is in progress
comment|// this means that next commits will not be allowed once the lock is released
name|rwl
operator|.
name|writeLock
argument_list|()
operator|.
name|lock
argument_list|()
expr_stmt|;
try|try
block|{
name|disableFlushCounter
operator|++
expr_stmt|;
block|}
finally|finally
block|{
name|rwl
operator|.
name|writeLock
argument_list|()
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
name|SnapshotIndexCommit
name|phase1Snapshot
decl_stmt|;
try|try
block|{
name|phase1Snapshot
operator|=
name|deletionPolicy
operator|.
name|snapshot
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
operator|--
name|disableFlushCounter
expr_stmt|;
throw|throw
operator|new
name|RecoveryEngineException
argument_list|(
name|shardId
argument_list|,
literal|1
argument_list|,
literal|"Snapshot failed"
argument_list|,
name|e
argument_list|)
throw|;
block|}
try|try
block|{
name|recoveryHandler
operator|.
name|phase1
argument_list|(
name|phase1Snapshot
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
operator|--
name|disableFlushCounter
expr_stmt|;
name|phase1Snapshot
operator|.
name|release
argument_list|()
expr_stmt|;
throw|throw
operator|new
name|RecoveryEngineException
argument_list|(
name|shardId
argument_list|,
literal|1
argument_list|,
literal|"Execution failed"
argument_list|,
name|e
argument_list|)
throw|;
block|}
name|Translog
operator|.
name|Snapshot
name|phase2Snapshot
decl_stmt|;
try|try
block|{
name|phase2Snapshot
operator|=
name|translog
operator|.
name|snapshot
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
operator|--
name|disableFlushCounter
expr_stmt|;
name|phase1Snapshot
operator|.
name|release
argument_list|()
expr_stmt|;
throw|throw
operator|new
name|RecoveryEngineException
argument_list|(
name|shardId
argument_list|,
literal|2
argument_list|,
literal|"Snapshot failed"
argument_list|,
name|e
argument_list|)
throw|;
block|}
try|try
block|{
name|recoveryHandler
operator|.
name|phase2
argument_list|(
name|phase2Snapshot
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
operator|--
name|disableFlushCounter
expr_stmt|;
name|phase1Snapshot
operator|.
name|release
argument_list|()
expr_stmt|;
name|phase2Snapshot
operator|.
name|release
argument_list|()
expr_stmt|;
throw|throw
operator|new
name|RecoveryEngineException
argument_list|(
name|shardId
argument_list|,
literal|2
argument_list|,
literal|"Execution failed"
argument_list|,
name|e
argument_list|)
throw|;
block|}
name|rwl
operator|.
name|writeLock
argument_list|()
operator|.
name|lock
argument_list|()
expr_stmt|;
name|Translog
operator|.
name|Snapshot
name|phase3Snapshot
decl_stmt|;
try|try
block|{
name|phase3Snapshot
operator|=
name|translog
operator|.
name|snapshot
argument_list|(
name|phase2Snapshot
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
operator|--
name|disableFlushCounter
expr_stmt|;
name|rwl
operator|.
name|writeLock
argument_list|()
operator|.
name|unlock
argument_list|()
expr_stmt|;
name|phase1Snapshot
operator|.
name|release
argument_list|()
expr_stmt|;
name|phase2Snapshot
operator|.
name|release
argument_list|()
expr_stmt|;
throw|throw
operator|new
name|RecoveryEngineException
argument_list|(
name|shardId
argument_list|,
literal|3
argument_list|,
literal|"Snapshot failed"
argument_list|,
name|e
argument_list|)
throw|;
block|}
try|try
block|{
name|recoveryHandler
operator|.
name|phase3
argument_list|(
name|phase3Snapshot
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RecoveryEngineException
argument_list|(
name|shardId
argument_list|,
literal|3
argument_list|,
literal|"Execution failed"
argument_list|,
name|e
argument_list|)
throw|;
block|}
finally|finally
block|{
operator|--
name|disableFlushCounter
expr_stmt|;
name|rwl
operator|.
name|writeLock
argument_list|()
operator|.
name|unlock
argument_list|()
expr_stmt|;
name|phase1Snapshot
operator|.
name|release
argument_list|()
expr_stmt|;
name|phase2Snapshot
operator|.
name|release
argument_list|()
expr_stmt|;
name|phase3Snapshot
operator|.
name|release
argument_list|()
expr_stmt|;
block|}
block|}
DECL|method|close
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|ElasticSearchException
block|{
if|if
condition|(
name|closed
condition|)
block|{
return|return;
block|}
name|closed
operator|=
literal|true
expr_stmt|;
name|rwl
operator|.
name|writeLock
argument_list|()
operator|.
name|lock
argument_list|()
expr_stmt|;
if|if
condition|(
name|nrtResource
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|nrtResource
operator|.
name|forceClose
argument_list|()
expr_stmt|;
block|}
try|try
block|{
if|if
condition|(
name|indexWriter
operator|!=
literal|null
condition|)
block|{
name|indexWriter
operator|.
name|close
argument_list|()
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
name|CloseEngineException
argument_list|(
name|shardId
argument_list|,
literal|"Failed to close engine"
argument_list|,
name|e
argument_list|)
throw|;
block|}
finally|finally
block|{
name|indexWriter
operator|=
literal|null
expr_stmt|;
name|rwl
operator|.
name|writeLock
argument_list|()
operator|.
name|unlock
argument_list|()
expr_stmt|;
block|}
block|}
DECL|class|RobinSearchResult
specifier|private
specifier|static
class|class
name|RobinSearchResult
implements|implements
name|Searcher
block|{
DECL|field|nrtHolder
specifier|private
specifier|final
name|AcquirableResource
argument_list|<
name|ReaderSearcherHolder
argument_list|>
name|nrtHolder
decl_stmt|;
DECL|method|RobinSearchResult
specifier|private
name|RobinSearchResult
parameter_list|(
name|AcquirableResource
argument_list|<
name|ReaderSearcherHolder
argument_list|>
name|nrtHolder
parameter_list|)
block|{
name|this
operator|.
name|nrtHolder
operator|=
name|nrtHolder
expr_stmt|;
block|}
DECL|method|reader
annotation|@
name|Override
specifier|public
name|IndexReader
name|reader
parameter_list|()
block|{
return|return
name|nrtHolder
operator|.
name|resource
argument_list|()
operator|.
name|reader
argument_list|()
return|;
block|}
DECL|method|searcher
annotation|@
name|Override
specifier|public
name|IndexSearcher
name|searcher
parameter_list|()
block|{
return|return
name|nrtHolder
operator|.
name|resource
argument_list|()
operator|.
name|searcher
argument_list|()
return|;
block|}
DECL|method|release
annotation|@
name|Override
specifier|public
name|boolean
name|release
parameter_list|()
throws|throws
name|ElasticSearchException
block|{
name|nrtHolder
operator|.
name|release
argument_list|()
expr_stmt|;
return|return
literal|true
return|;
block|}
block|}
block|}
end_class

end_unit

