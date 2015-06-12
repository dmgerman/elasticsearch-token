begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.engine
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|engine
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
name|analysis
operator|.
name|Analyzer
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
name|codecs
operator|.
name|Codec
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
name|MergePolicy
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
name|MergeSchedulerConfig
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
name|QueryCache
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
name|QueryCachingPolicy
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
name|similarities
operator|.
name|Similarity
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
name|Nullable
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
name|concurrent
operator|.
name|EsExecutors
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
name|codec
operator|.
name|CodecService
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
name|indexing
operator|.
name|ShardIndexingService
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
name|IndexSettingsService
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
name|translog
operator|.
name|TranslogConfig
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
name|IndicesWarmer
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
name|util
operator|.
name|concurrent
operator|.
name|TimeUnit
import|;
end_import

begin_comment
comment|/*  * Holds all the configuration that is used to create an {@link Engine}.  * Once {@link Engine} has been created with this object, changes to this  * object will affect the {@link Engine} instance.  */
end_comment

begin_class
DECL|class|EngineConfig
specifier|public
specifier|final
class|class
name|EngineConfig
block|{
DECL|field|shardId
specifier|private
specifier|final
name|ShardId
name|shardId
decl_stmt|;
DECL|field|translogRecoveryPerformer
specifier|private
specifier|final
name|TranslogRecoveryPerformer
name|translogRecoveryPerformer
decl_stmt|;
DECL|field|indexingBufferSize
specifier|private
specifier|volatile
name|ByteSizeValue
name|indexingBufferSize
decl_stmt|;
DECL|field|versionMapSize
specifier|private
specifier|volatile
name|ByteSizeValue
name|versionMapSize
decl_stmt|;
DECL|field|versionMapSizeSetting
specifier|private
specifier|volatile
name|String
name|versionMapSizeSetting
decl_stmt|;
DECL|field|indexConcurrency
specifier|private
specifier|final
name|int
name|indexConcurrency
decl_stmt|;
DECL|field|compoundOnFlush
specifier|private
specifier|volatile
name|boolean
name|compoundOnFlush
init|=
literal|true
decl_stmt|;
DECL|field|gcDeletesInMillis
specifier|private
name|long
name|gcDeletesInMillis
init|=
name|DEFAULT_GC_DELETES
operator|.
name|millis
argument_list|()
decl_stmt|;
DECL|field|enableGcDeletes
specifier|private
specifier|volatile
name|boolean
name|enableGcDeletes
init|=
literal|true
decl_stmt|;
DECL|field|codecName
specifier|private
specifier|final
name|String
name|codecName
decl_stmt|;
DECL|field|optimizeAutoGenerateId
specifier|private
specifier|final
name|boolean
name|optimizeAutoGenerateId
decl_stmt|;
DECL|field|threadPool
specifier|private
specifier|final
name|ThreadPool
name|threadPool
decl_stmt|;
DECL|field|indexingService
specifier|private
specifier|final
name|ShardIndexingService
name|indexingService
decl_stmt|;
DECL|field|indexSettingsService
specifier|private
specifier|final
name|IndexSettingsService
name|indexSettingsService
decl_stmt|;
annotation|@
name|Nullable
DECL|field|warmer
specifier|private
specifier|final
name|IndicesWarmer
name|warmer
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
DECL|field|mergePolicy
specifier|private
specifier|final
name|MergePolicy
name|mergePolicy
decl_stmt|;
DECL|field|mergeSchedulerConfig
specifier|private
specifier|final
name|MergeSchedulerConfig
name|mergeSchedulerConfig
decl_stmt|;
DECL|field|analyzer
specifier|private
specifier|final
name|Analyzer
name|analyzer
decl_stmt|;
DECL|field|similarity
specifier|private
specifier|final
name|Similarity
name|similarity
decl_stmt|;
DECL|field|codecService
specifier|private
specifier|final
name|CodecService
name|codecService
decl_stmt|;
DECL|field|failedEngineListener
specifier|private
specifier|final
name|Engine
operator|.
name|FailedEngineListener
name|failedEngineListener
decl_stmt|;
DECL|field|forceNewTranslog
specifier|private
specifier|final
name|boolean
name|forceNewTranslog
decl_stmt|;
DECL|field|filterCache
specifier|private
specifier|final
name|QueryCache
name|filterCache
decl_stmt|;
DECL|field|filterCachingPolicy
specifier|private
specifier|final
name|QueryCachingPolicy
name|filterCachingPolicy
decl_stmt|;
comment|/**      * Index setting for index concurrency / number of threadstates in the indexwriter.      * The default is depending on the number of CPUs in the system. We use a 0.65 the number of CPUs or at least {@value org.apache.lucene.index.IndexWriterConfig#DEFAULT_MAX_THREAD_STATES}      * This setting is<b>not</b> realtime updateable      */
DECL|field|INDEX_CONCURRENCY_SETTING
specifier|public
specifier|static
specifier|final
name|String
name|INDEX_CONCURRENCY_SETTING
init|=
literal|"index.index_concurrency"
decl_stmt|;
comment|/**      * Index setting for compound file on flush. This setting is realtime updateable.      */
DECL|field|INDEX_COMPOUND_ON_FLUSH
specifier|public
specifier|static
specifier|final
name|String
name|INDEX_COMPOUND_ON_FLUSH
init|=
literal|"index.compound_on_flush"
decl_stmt|;
comment|/**      * Setting to control auto generated ID optimizations. Default is<code>true</code> if not present.      * This setting is<b>not</b> realtime updateable.      */
DECL|field|INDEX_OPTIMIZE_AUTOGENERATED_ID_SETTING
specifier|public
specifier|static
specifier|final
name|String
name|INDEX_OPTIMIZE_AUTOGENERATED_ID_SETTING
init|=
literal|"index.optimize_auto_generated_id"
decl_stmt|;
comment|/**      * Index setting to enable / disable deletes garbage collection.      * This setting is realtime updateable      */
DECL|field|INDEX_GC_DELETES_SETTING
specifier|public
specifier|static
specifier|final
name|String
name|INDEX_GC_DELETES_SETTING
init|=
literal|"index.gc_deletes"
decl_stmt|;
comment|/**      * Index setting to control the initial index buffer size.      * This setting is<b>not</b> realtime updateable.      */
DECL|field|INDEX_BUFFER_SIZE_SETTING
specifier|public
specifier|static
specifier|final
name|String
name|INDEX_BUFFER_SIZE_SETTING
init|=
literal|"index.buffer_size"
decl_stmt|;
comment|/**      * Index setting to change the low level lucene codec used for writing new segments.      * This setting is<b>not</b> realtime updateable.      */
DECL|field|INDEX_CODEC_SETTING
specifier|public
specifier|static
specifier|final
name|String
name|INDEX_CODEC_SETTING
init|=
literal|"index.codec"
decl_stmt|;
comment|/**      * The maximum size the version map should grow to before issuing a refresh. Can be an absolute value or a percentage of      * the current index memory buffer (defaults to 25%)      */
DECL|field|INDEX_VERSION_MAP_SIZE
specifier|public
specifier|static
specifier|final
name|String
name|INDEX_VERSION_MAP_SIZE
init|=
literal|"index.version_map_size"
decl_stmt|;
comment|/** if set to true the engine will start even if the translog id in the commit point can not be found */
DECL|field|INDEX_FORCE_NEW_TRANSLOG
specifier|public
specifier|static
specifier|final
name|String
name|INDEX_FORCE_NEW_TRANSLOG
init|=
literal|"index.engine.force_new_translog"
decl_stmt|;
DECL|field|DEFAULT_REFRESH_INTERVAL
specifier|public
specifier|static
specifier|final
name|TimeValue
name|DEFAULT_REFRESH_INTERVAL
init|=
operator|new
name|TimeValue
argument_list|(
literal|1
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
decl_stmt|;
DECL|field|DEFAULT_GC_DELETES
specifier|public
specifier|static
specifier|final
name|TimeValue
name|DEFAULT_GC_DELETES
init|=
name|TimeValue
operator|.
name|timeValueSeconds
argument_list|(
literal|60
argument_list|)
decl_stmt|;
DECL|field|DEFAULT_INDEX_BUFFER_SIZE
specifier|public
specifier|static
specifier|final
name|ByteSizeValue
name|DEFAULT_INDEX_BUFFER_SIZE
init|=
operator|new
name|ByteSizeValue
argument_list|(
literal|64
argument_list|,
name|ByteSizeUnit
operator|.
name|MB
argument_list|)
decl_stmt|;
DECL|field|INACTIVE_SHARD_INDEXING_BUFFER
specifier|public
specifier|static
specifier|final
name|ByteSizeValue
name|INACTIVE_SHARD_INDEXING_BUFFER
init|=
name|ByteSizeValue
operator|.
name|parseBytesSizeValue
argument_list|(
literal|"500kb"
argument_list|,
literal|"INACTIVE_SHARD_INDEXING_BUFFER"
argument_list|)
decl_stmt|;
DECL|field|DEFAULT_VERSION_MAP_SIZE
specifier|public
specifier|static
specifier|final
name|String
name|DEFAULT_VERSION_MAP_SIZE
init|=
literal|"25%"
decl_stmt|;
DECL|field|DEFAULT_CODEC_NAME
specifier|private
specifier|static
specifier|final
name|String
name|DEFAULT_CODEC_NAME
init|=
literal|"default"
decl_stmt|;
DECL|field|translogConfig
specifier|private
name|TranslogConfig
name|translogConfig
decl_stmt|;
comment|/**      * Creates a new {@link org.elasticsearch.index.engine.EngineConfig}      */
DECL|method|EngineConfig
specifier|public
name|EngineConfig
parameter_list|(
name|ShardId
name|shardId
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|,
name|ShardIndexingService
name|indexingService
parameter_list|,
name|IndexSettingsService
name|indexSettingsService
parameter_list|,
name|IndicesWarmer
name|warmer
parameter_list|,
name|Store
name|store
parameter_list|,
name|SnapshotDeletionPolicy
name|deletionPolicy
parameter_list|,
name|MergePolicy
name|mergePolicy
parameter_list|,
name|MergeSchedulerConfig
name|mergeSchedulerConfig
parameter_list|,
name|Analyzer
name|analyzer
parameter_list|,
name|Similarity
name|similarity
parameter_list|,
name|CodecService
name|codecService
parameter_list|,
name|Engine
operator|.
name|FailedEngineListener
name|failedEngineListener
parameter_list|,
name|TranslogRecoveryPerformer
name|translogRecoveryPerformer
parameter_list|,
name|QueryCache
name|filterCache
parameter_list|,
name|QueryCachingPolicy
name|filterCachingPolicy
parameter_list|,
name|TranslogConfig
name|translogConfig
parameter_list|)
block|{
name|this
operator|.
name|shardId
operator|=
name|shardId
expr_stmt|;
name|this
operator|.
name|threadPool
operator|=
name|threadPool
expr_stmt|;
name|this
operator|.
name|indexingService
operator|=
name|indexingService
expr_stmt|;
name|this
operator|.
name|indexSettingsService
operator|=
name|indexSettingsService
expr_stmt|;
name|this
operator|.
name|warmer
operator|=
name|warmer
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
name|mergePolicy
operator|=
name|mergePolicy
expr_stmt|;
name|this
operator|.
name|mergeSchedulerConfig
operator|=
name|mergeSchedulerConfig
expr_stmt|;
name|this
operator|.
name|analyzer
operator|=
name|analyzer
expr_stmt|;
name|this
operator|.
name|similarity
operator|=
name|similarity
expr_stmt|;
name|this
operator|.
name|codecService
operator|=
name|codecService
expr_stmt|;
name|this
operator|.
name|failedEngineListener
operator|=
name|failedEngineListener
expr_stmt|;
name|Settings
name|indexSettings
init|=
name|indexSettingsService
operator|.
name|getSettings
argument_list|()
decl_stmt|;
name|this
operator|.
name|optimizeAutoGenerateId
operator|=
name|indexSettings
operator|.
name|getAsBoolean
argument_list|(
name|EngineConfig
operator|.
name|INDEX_OPTIMIZE_AUTOGENERATED_ID_SETTING
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|this
operator|.
name|compoundOnFlush
operator|=
name|indexSettings
operator|.
name|getAsBoolean
argument_list|(
name|EngineConfig
operator|.
name|INDEX_COMPOUND_ON_FLUSH
argument_list|,
name|compoundOnFlush
argument_list|)
expr_stmt|;
name|this
operator|.
name|indexConcurrency
operator|=
name|indexSettings
operator|.
name|getAsInt
argument_list|(
name|EngineConfig
operator|.
name|INDEX_CONCURRENCY_SETTING
argument_list|,
name|Math
operator|.
name|max
argument_list|(
name|IndexWriterConfig
operator|.
name|DEFAULT_MAX_THREAD_STATES
argument_list|,
call|(
name|int
call|)
argument_list|(
name|EsExecutors
operator|.
name|boundedNumberOfProcessors
argument_list|(
name|indexSettings
argument_list|)
operator|*
literal|0.65
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|codecName
operator|=
name|indexSettings
operator|.
name|get
argument_list|(
name|EngineConfig
operator|.
name|INDEX_CODEC_SETTING
argument_list|,
name|EngineConfig
operator|.
name|DEFAULT_CODEC_NAME
argument_list|)
expr_stmt|;
name|indexingBufferSize
operator|=
name|indexSettings
operator|.
name|getAsBytesSize
argument_list|(
name|INDEX_BUFFER_SIZE_SETTING
argument_list|,
name|DEFAULT_INDEX_BUFFER_SIZE
argument_list|)
expr_stmt|;
name|gcDeletesInMillis
operator|=
name|indexSettings
operator|.
name|getAsTime
argument_list|(
name|INDEX_GC_DELETES_SETTING
argument_list|,
name|EngineConfig
operator|.
name|DEFAULT_GC_DELETES
argument_list|)
operator|.
name|millis
argument_list|()
expr_stmt|;
name|versionMapSizeSetting
operator|=
name|indexSettings
operator|.
name|get
argument_list|(
name|INDEX_VERSION_MAP_SIZE
argument_list|,
name|DEFAULT_VERSION_MAP_SIZE
argument_list|)
expr_stmt|;
name|updateVersionMapSize
argument_list|()
expr_stmt|;
name|this
operator|.
name|translogRecoveryPerformer
operator|=
name|translogRecoveryPerformer
expr_stmt|;
name|this
operator|.
name|forceNewTranslog
operator|=
name|indexSettings
operator|.
name|getAsBoolean
argument_list|(
name|INDEX_FORCE_NEW_TRANSLOG
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|this
operator|.
name|filterCache
operator|=
name|filterCache
expr_stmt|;
name|this
operator|.
name|filterCachingPolicy
operator|=
name|filterCachingPolicy
expr_stmt|;
name|this
operator|.
name|translogConfig
operator|=
name|translogConfig
expr_stmt|;
block|}
comment|/** updates {@link #versionMapSize} based on current setting and {@link #indexingBufferSize} */
DECL|method|updateVersionMapSize
specifier|private
name|void
name|updateVersionMapSize
parameter_list|()
block|{
if|if
condition|(
name|versionMapSizeSetting
operator|.
name|endsWith
argument_list|(
literal|"%"
argument_list|)
condition|)
block|{
name|double
name|percent
init|=
name|Double
operator|.
name|parseDouble
argument_list|(
name|versionMapSizeSetting
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|versionMapSizeSetting
operator|.
name|length
argument_list|()
operator|-
literal|1
argument_list|)
argument_list|)
decl_stmt|;
name|versionMapSize
operator|=
operator|new
name|ByteSizeValue
argument_list|(
call|(
name|long
call|)
argument_list|(
operator|(
operator|(
name|double
operator|)
name|indexingBufferSize
operator|.
name|bytes
argument_list|()
operator|*
operator|(
name|percent
operator|/
literal|100
operator|)
operator|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|versionMapSize
operator|=
name|ByteSizeValue
operator|.
name|parseBytesSizeValue
argument_list|(
name|versionMapSizeSetting
argument_list|,
name|INDEX_VERSION_MAP_SIZE
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      * Settings the version map size that should trigger a refresh. See {@link #INDEX_VERSION_MAP_SIZE} for details.      */
DECL|method|setVersionMapSizeSetting
specifier|public
name|void
name|setVersionMapSizeSetting
parameter_list|(
name|String
name|versionMapSizeSetting
parameter_list|)
block|{
name|this
operator|.
name|versionMapSizeSetting
operator|=
name|versionMapSizeSetting
expr_stmt|;
name|updateVersionMapSize
argument_list|()
expr_stmt|;
block|}
comment|/**      * current setting for the version map size that should trigger a refresh. See {@link #INDEX_VERSION_MAP_SIZE} for details.      */
DECL|method|getVersionMapSizeSetting
specifier|public
name|String
name|getVersionMapSizeSetting
parameter_list|()
block|{
return|return
name|versionMapSizeSetting
return|;
block|}
comment|/** if true the engine will start even if the translog id in the commit point can not be found */
DECL|method|forceNewTranslog
specifier|public
name|boolean
name|forceNewTranslog
parameter_list|()
block|{
return|return
name|forceNewTranslog
return|;
block|}
comment|/**      * returns the size of the version map that should trigger a refresh      */
DECL|method|getVersionMapSize
specifier|public
name|ByteSizeValue
name|getVersionMapSize
parameter_list|()
block|{
return|return
name|versionMapSize
return|;
block|}
comment|/**      * Sets the indexing buffer      */
DECL|method|setIndexingBufferSize
specifier|public
name|void
name|setIndexingBufferSize
parameter_list|(
name|ByteSizeValue
name|indexingBufferSize
parameter_list|)
block|{
name|this
operator|.
name|indexingBufferSize
operator|=
name|indexingBufferSize
expr_stmt|;
name|updateVersionMapSize
argument_list|()
expr_stmt|;
block|}
comment|/**      * Enables / disables gc deletes      *      * @see #isEnableGcDeletes()      */
DECL|method|setEnableGcDeletes
specifier|public
name|void
name|setEnableGcDeletes
parameter_list|(
name|boolean
name|enableGcDeletes
parameter_list|)
block|{
name|this
operator|.
name|enableGcDeletes
operator|=
name|enableGcDeletes
expr_stmt|;
block|}
comment|/**      * Returns the initial index buffer size. This setting is only read on startup and otherwise controlled by {@link org.elasticsearch.indices.memory.IndexingMemoryController}      */
DECL|method|getIndexingBufferSize
specifier|public
name|ByteSizeValue
name|getIndexingBufferSize
parameter_list|()
block|{
return|return
name|indexingBufferSize
return|;
block|}
comment|/**      * Returns the index concurrency that directly translates into the number of thread states used in the engines      * {@code IndexWriter}.      *      * @see org.apache.lucene.index.IndexWriterConfig#getMaxThreadStates()      */
DECL|method|getIndexConcurrency
specifier|public
name|int
name|getIndexConcurrency
parameter_list|()
block|{
return|return
name|indexConcurrency
return|;
block|}
comment|/**      * Returns<code>true</code> iff flushed segments should be written as compound file system. Defaults to<code>true</code>      */
DECL|method|isCompoundOnFlush
specifier|public
name|boolean
name|isCompoundOnFlush
parameter_list|()
block|{
return|return
name|compoundOnFlush
return|;
block|}
comment|/**      * Returns the GC deletes cycle in milliseconds.      */
DECL|method|getGcDeletesInMillis
specifier|public
name|long
name|getGcDeletesInMillis
parameter_list|()
block|{
return|return
name|gcDeletesInMillis
return|;
block|}
comment|/**      * Returns<code>true</code> iff delete garbage collection in the engine should be enabled. This setting is updateable      * in realtime and forces a volatile read. Consumers can safely read this value directly go fetch it's latest value. The default is<code>true</code>      *<p>      *     Engine GC deletion if enabled collects deleted documents from in-memory realtime data structures after a certain amount of      *     time ({@link #getGcDeletesInMillis()} if enabled. Before deletes are GCed they will cause re-adding the document that was deleted      *     to fail.      *</p>      */
DECL|method|isEnableGcDeletes
specifier|public
name|boolean
name|isEnableGcDeletes
parameter_list|()
block|{
return|return
name|enableGcDeletes
return|;
block|}
comment|/**      * Returns the {@link Codec} used in the engines {@link org.apache.lucene.index.IndexWriter}      *<p>      *     Note: this settings is only read on startup.      *</p>      */
DECL|method|getCodec
specifier|public
name|Codec
name|getCodec
parameter_list|()
block|{
return|return
name|codecService
operator|.
name|codec
argument_list|(
name|codecName
argument_list|)
return|;
block|}
comment|/**      * Returns<code>true</code> iff documents with auto-generated IDs are optimized if possible. This mainly means that      * they are simply appended to the index if no update call is necessary.      */
DECL|method|isOptimizeAutoGenerateId
specifier|public
name|boolean
name|isOptimizeAutoGenerateId
parameter_list|()
block|{
return|return
name|optimizeAutoGenerateId
return|;
block|}
comment|/**      * Returns a thread-pool mainly used to get estimated time stamps from {@link org.elasticsearch.threadpool.ThreadPool#estimatedTimeInMillis()} and to schedule      * async force merge calls on the {@link org.elasticsearch.threadpool.ThreadPool.Names#OPTIMIZE} thread-pool      */
DECL|method|getThreadPool
specifier|public
name|ThreadPool
name|getThreadPool
parameter_list|()
block|{
return|return
name|threadPool
return|;
block|}
comment|/**      * Returns a {@link org.elasticsearch.index.indexing.ShardIndexingService} used inside the engine to inform about      * pre and post index and create operations. The operations are used for statistic purposes etc.      *      * @see org.elasticsearch.index.indexing.ShardIndexingService#postCreate(org.elasticsearch.index.engine.Engine.Create)      * @see org.elasticsearch.index.indexing.ShardIndexingService#preCreate(org.elasticsearch.index.engine.Engine.Create)      *      */
DECL|method|getIndexingService
specifier|public
name|ShardIndexingService
name|getIndexingService
parameter_list|()
block|{
return|return
name|indexingService
return|;
block|}
comment|/**      * Returns an {@link org.elasticsearch.indices.IndicesWarmer} used to warm new searchers before they are used for searching.      * Note: This method might retrun<code>null</code>      */
annotation|@
name|Nullable
DECL|method|getWarmer
specifier|public
name|IndicesWarmer
name|getWarmer
parameter_list|()
block|{
return|return
name|warmer
return|;
block|}
comment|/**      * Returns the {@link org.elasticsearch.index.store.Store} instance that provides access to the {@link org.apache.lucene.store.Directory}      * used for the engines {@link org.apache.lucene.index.IndexWriter} to write it's index files to.      *<p>      * Note: In order to use this instance the consumer needs to increment the stores reference before it's used the first time and hold      * it's reference until it's not needed anymore.      *</p>      */
DECL|method|getStore
specifier|public
name|Store
name|getStore
parameter_list|()
block|{
return|return
name|store
return|;
block|}
comment|/**      * Returns a {@link org.elasticsearch.index.deletionpolicy.SnapshotDeletionPolicy} used in the engines      * {@link org.apache.lucene.index.IndexWriter}.      */
DECL|method|getDeletionPolicy
specifier|public
name|SnapshotDeletionPolicy
name|getDeletionPolicy
parameter_list|()
block|{
return|return
name|deletionPolicy
return|;
block|}
comment|/**      * Returns the {@link org.apache.lucene.index.MergePolicy} for the engines {@link org.apache.lucene.index.IndexWriter}      */
DECL|method|getMergePolicy
specifier|public
name|MergePolicy
name|getMergePolicy
parameter_list|()
block|{
return|return
name|mergePolicy
return|;
block|}
comment|/**      * Returns the {@link MergeSchedulerConfig}      */
DECL|method|getMergeSchedulerConfig
specifier|public
name|MergeSchedulerConfig
name|getMergeSchedulerConfig
parameter_list|()
block|{
return|return
name|mergeSchedulerConfig
return|;
block|}
comment|/**      * Returns a listener that should be called on engine failure      */
DECL|method|getFailedEngineListener
specifier|public
name|Engine
operator|.
name|FailedEngineListener
name|getFailedEngineListener
parameter_list|()
block|{
return|return
name|failedEngineListener
return|;
block|}
comment|/**      * Returns the latest index settings directly from the index settings service.      */
DECL|method|getIndexSettings
specifier|public
name|Settings
name|getIndexSettings
parameter_list|()
block|{
return|return
name|indexSettingsService
operator|.
name|getSettings
argument_list|()
return|;
block|}
comment|/**      * Returns the engines shard ID      */
DECL|method|getShardId
specifier|public
name|ShardId
name|getShardId
parameter_list|()
block|{
return|return
name|shardId
return|;
block|}
comment|/**      * Returns the analyzer as the default analyzer in the engines {@link org.apache.lucene.index.IndexWriter}      */
DECL|method|getAnalyzer
specifier|public
name|Analyzer
name|getAnalyzer
parameter_list|()
block|{
return|return
name|analyzer
return|;
block|}
comment|/**      * Returns the {@link org.apache.lucene.search.similarities.Similarity} used for indexing and searching.      */
DECL|method|getSimilarity
specifier|public
name|Similarity
name|getSimilarity
parameter_list|()
block|{
return|return
name|similarity
return|;
block|}
comment|/**      * Sets the GC deletes cycle in milliseconds.      */
DECL|method|setGcDeletesInMillis
specifier|public
name|void
name|setGcDeletesInMillis
parameter_list|(
name|long
name|gcDeletesInMillis
parameter_list|)
block|{
name|this
operator|.
name|gcDeletesInMillis
operator|=
name|gcDeletesInMillis
expr_stmt|;
block|}
comment|/**      * Sets if flushed segments should be written as compound file system. Defaults to<code>true</code>      */
DECL|method|setCompoundOnFlush
specifier|public
name|void
name|setCompoundOnFlush
parameter_list|(
name|boolean
name|compoundOnFlush
parameter_list|)
block|{
name|this
operator|.
name|compoundOnFlush
operator|=
name|compoundOnFlush
expr_stmt|;
block|}
comment|/**      * Returns the {@link org.elasticsearch.index.shard.TranslogRecoveryPerformer} for this engine. This class is used      * to apply transaction log operations to the engine. It encapsulates all the logic to transfer the translog entry into      * an indexing operation.      */
DECL|method|getTranslogRecoveryPerformer
specifier|public
name|TranslogRecoveryPerformer
name|getTranslogRecoveryPerformer
parameter_list|()
block|{
return|return
name|translogRecoveryPerformer
return|;
block|}
comment|/**      * Return the cache to use for filters.      */
DECL|method|getFilterCache
specifier|public
name|QueryCache
name|getFilterCache
parameter_list|()
block|{
return|return
name|filterCache
return|;
block|}
comment|/**      * Return the policy to use when caching filters.      */
DECL|method|getFilterCachingPolicy
specifier|public
name|QueryCachingPolicy
name|getFilterCachingPolicy
parameter_list|()
block|{
return|return
name|filterCachingPolicy
return|;
block|}
comment|/**      * Returns the translog config for this engine      */
DECL|method|getTranslogConfig
specifier|public
name|TranslogConfig
name|getTranslogConfig
parameter_list|()
block|{
return|return
name|translogConfig
return|;
block|}
DECL|method|getIndexSettingsService
name|IndexSettingsService
name|getIndexSettingsService
parameter_list|()
block|{
comment|// for testing
return|return
name|indexSettingsService
return|;
block|}
block|}
end_class

end_unit
