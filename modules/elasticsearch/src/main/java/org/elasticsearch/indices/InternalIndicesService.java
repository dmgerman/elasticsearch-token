begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.indices
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|indices
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
name|search
operator|.
name|FieldCache
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
name|IndexReaderPurgedListener
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
name|ElasticSearchIllegalStateException
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
name|ImmutableMap
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
name|ImmutableSet
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
name|UnmodifiableIterator
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
name|AbstractLifecycleComponent
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
name|inject
operator|.
name|Injector
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
name|Injectors
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
name|ModulesBuilder
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
name|util
operator|.
name|concurrent
operator|.
name|ThreadSafe
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
name|NodeEnvironment
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|gateway
operator|.
name|Gateway
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
name|analysis
operator|.
name|AnalysisModule
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
name|cache
operator|.
name|CacheStats
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
name|cache
operator|.
name|IndexCache
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
name|cache
operator|.
name|IndexCacheModule
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
name|engine
operator|.
name|IndexEngine
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
name|IndexEngineModule
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
name|gateway
operator|.
name|IndexGateway
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
name|gateway
operator|.
name|IndexGatewayModule
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
name|MapperServiceModule
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
name|MergeStats
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
name|percolator
operator|.
name|PercolatorModule
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
name|percolator
operator|.
name|PercolatorService
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
name|query
operator|.
name|IndexQueryParserModule
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
name|service
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
name|service
operator|.
name|InternalIndexService
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
name|IndexSettingsModule
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
name|similarity
operator|.
name|SimilarityModule
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
name|IndexStoreModule
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
name|analysis
operator|.
name|IndicesAnalysisService
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
name|store
operator|.
name|IndicesStore
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
name|IndexPluginsModule
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
name|PluginsService
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
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
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
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|metadata
operator|.
name|IndexMetaData
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
name|common
operator|.
name|collect
operator|.
name|MapBuilder
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
name|common
operator|.
name|collect
operator|.
name|Maps
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
name|common
operator|.
name|collect
operator|.
name|Sets
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
name|common
operator|.
name|settings
operator|.
name|ImmutableSettings
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
annotation|@
name|ThreadSafe
DECL|class|InternalIndicesService
specifier|public
class|class
name|InternalIndicesService
extends|extends
name|AbstractLifecycleComponent
argument_list|<
name|IndicesService
argument_list|>
implements|implements
name|IndicesService
block|{
DECL|field|nodeEnv
specifier|private
specifier|final
name|NodeEnvironment
name|nodeEnv
decl_stmt|;
DECL|field|threadPool
specifier|private
specifier|final
name|ThreadPool
name|threadPool
decl_stmt|;
DECL|field|indicesLifecycle
specifier|private
specifier|final
name|InternalIndicesLifecycle
name|indicesLifecycle
decl_stmt|;
DECL|field|indicesAnalysisService
specifier|private
specifier|final
name|IndicesAnalysisService
name|indicesAnalysisService
decl_stmt|;
DECL|field|indicesStore
specifier|private
specifier|final
name|IndicesStore
name|indicesStore
decl_stmt|;
DECL|field|injector
specifier|private
specifier|final
name|Injector
name|injector
decl_stmt|;
DECL|field|pluginsService
specifier|private
specifier|final
name|PluginsService
name|pluginsService
decl_stmt|;
DECL|field|indicesInjectors
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Injector
argument_list|>
name|indicesInjectors
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Injector
argument_list|>
argument_list|()
decl_stmt|;
DECL|field|indices
specifier|private
specifier|volatile
name|ImmutableMap
argument_list|<
name|String
argument_list|,
name|IndexService
argument_list|>
name|indices
init|=
name|ImmutableMap
operator|.
name|of
argument_list|()
decl_stmt|;
DECL|method|InternalIndicesService
annotation|@
name|Inject
specifier|public
name|InternalIndicesService
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|NodeEnvironment
name|nodeEnv
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|,
name|IndicesLifecycle
name|indicesLifecycle
parameter_list|,
name|IndicesAnalysisService
name|indicesAnalysisService
parameter_list|,
name|IndicesStore
name|indicesStore
parameter_list|,
name|Injector
name|injector
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|nodeEnv
operator|=
name|nodeEnv
expr_stmt|;
name|this
operator|.
name|threadPool
operator|=
name|threadPool
expr_stmt|;
name|this
operator|.
name|indicesLifecycle
operator|=
operator|(
name|InternalIndicesLifecycle
operator|)
name|indicesLifecycle
expr_stmt|;
name|this
operator|.
name|indicesAnalysisService
operator|=
name|indicesAnalysisService
expr_stmt|;
name|this
operator|.
name|indicesStore
operator|=
name|indicesStore
expr_stmt|;
name|this
operator|.
name|injector
operator|=
name|injector
expr_stmt|;
name|this
operator|.
name|pluginsService
operator|=
name|injector
operator|.
name|getInstance
argument_list|(
name|PluginsService
operator|.
name|class
argument_list|)
expr_stmt|;
try|try
block|{
name|FieldCache
operator|.
name|DEFAULT
operator|.
name|getClass
argument_list|()
operator|.
name|getMethod
argument_list|(
literal|"setIndexReaderPurgedListener"
argument_list|,
name|IndexReaderPurgedListener
operator|.
name|class
argument_list|)
expr_stmt|;
comment|// LUCENE MONITOR - This is a hack to eagerly clean caches based on index reader
name|FieldCache
operator|.
name|DEFAULT
operator|.
name|setIndexReaderPurgedListener
argument_list|(
operator|new
name|CacheReaderPurgeListener
argument_list|(
name|this
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|trace
argument_list|(
literal|"eager reader based cache eviction enabled"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NoSuchMethodException
name|e
parameter_list|)
block|{
comment|// no method
name|logger
operator|.
name|warn
argument_list|(
literal|"lucene default FieldCache is used, not enabling eager reader based cache eviction"
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|doStart
annotation|@
name|Override
specifier|protected
name|void
name|doStart
parameter_list|()
throws|throws
name|ElasticSearchException
block|{     }
DECL|method|doStop
annotation|@
name|Override
specifier|protected
name|void
name|doStop
parameter_list|()
throws|throws
name|ElasticSearchException
block|{
name|ImmutableSet
argument_list|<
name|String
argument_list|>
name|indices
init|=
name|ImmutableSet
operator|.
name|copyOf
argument_list|(
name|this
operator|.
name|indices
operator|.
name|keySet
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|CountDownLatch
name|latch
init|=
operator|new
name|CountDownLatch
argument_list|(
name|indices
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
specifier|final
name|String
name|index
range|:
name|indices
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
try|try
block|{
name|deleteIndex
argument_list|(
name|index
argument_list|,
literal|false
argument_list|,
literal|"shutdown"
argument_list|)
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
name|warn
argument_list|(
literal|"failed to delete index on stop ["
operator|+
name|index
operator|+
literal|"]"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
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
try|try
block|{
name|latch
operator|.
name|await
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
comment|// ignore
block|}
block|}
DECL|method|doClose
annotation|@
name|Override
specifier|protected
name|void
name|doClose
parameter_list|()
throws|throws
name|ElasticSearchException
block|{
name|indicesStore
operator|.
name|close
argument_list|()
expr_stmt|;
name|indicesAnalysisService
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
DECL|method|indicesLifecycle
annotation|@
name|Override
specifier|public
name|IndicesLifecycle
name|indicesLifecycle
parameter_list|()
block|{
return|return
name|this
operator|.
name|indicesLifecycle
return|;
block|}
DECL|method|stats
annotation|@
name|Override
specifier|public
name|NodeIndicesStats
name|stats
parameter_list|()
block|{
name|long
name|storeTotalSize
init|=
literal|0
decl_stmt|;
name|long
name|numberOfDocs
init|=
literal|0
decl_stmt|;
name|CacheStats
name|cacheStats
init|=
operator|new
name|CacheStats
argument_list|()
decl_stmt|;
name|MergeStats
name|mergeStats
init|=
operator|new
name|MergeStats
argument_list|()
decl_stmt|;
for|for
control|(
name|IndexService
name|indexService
range|:
name|indices
operator|.
name|values
argument_list|()
control|)
block|{
for|for
control|(
name|IndexShard
name|indexShard
range|:
name|indexService
control|)
block|{
try|try
block|{
name|storeTotalSize
operator|+=
operator|(
operator|(
name|InternalIndexShard
operator|)
name|indexShard
operator|)
operator|.
name|store
argument_list|()
operator|.
name|estimateSize
argument_list|()
operator|.
name|bytes
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
name|Engine
operator|.
name|Searcher
name|searcher
init|=
name|indexShard
operator|.
name|searcher
argument_list|()
decl_stmt|;
try|try
block|{
name|numberOfDocs
operator|+=
name|searcher
operator|.
name|reader
argument_list|()
operator|.
name|numDocs
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
name|searcher
operator|.
name|release
argument_list|()
expr_stmt|;
block|}
block|}
name|mergeStats
operator|.
name|add
argument_list|(
operator|(
operator|(
name|InternalIndexShard
operator|)
name|indexShard
operator|)
operator|.
name|mergeScheduler
argument_list|()
operator|.
name|stats
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|cacheStats
operator|.
name|add
argument_list|(
name|indexService
operator|.
name|cache
argument_list|()
operator|.
name|stats
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|NodeIndicesStats
argument_list|(
operator|new
name|ByteSizeValue
argument_list|(
name|storeTotalSize
argument_list|)
argument_list|,
name|numberOfDocs
argument_list|,
name|cacheStats
argument_list|,
name|mergeStats
argument_list|)
return|;
block|}
comment|/**      * Returns<tt>true</tt> if changes (adding / removing) indices, shards and so on are allowed.      */
DECL|method|changesAllowed
specifier|public
name|boolean
name|changesAllowed
parameter_list|()
block|{
comment|// we check on stop here since we defined stop when we delete the indices
return|return
name|lifecycle
operator|.
name|started
argument_list|()
return|;
block|}
DECL|method|iterator
annotation|@
name|Override
specifier|public
name|UnmodifiableIterator
argument_list|<
name|IndexService
argument_list|>
name|iterator
parameter_list|()
block|{
return|return
name|indices
operator|.
name|values
argument_list|()
operator|.
name|iterator
argument_list|()
return|;
block|}
DECL|method|hasIndex
specifier|public
name|boolean
name|hasIndex
parameter_list|(
name|String
name|index
parameter_list|)
block|{
return|return
name|indices
operator|.
name|containsKey
argument_list|(
name|index
argument_list|)
return|;
block|}
DECL|method|indices
specifier|public
name|Set
argument_list|<
name|String
argument_list|>
name|indices
parameter_list|()
block|{
return|return
name|newHashSet
argument_list|(
name|indices
operator|.
name|keySet
argument_list|()
argument_list|)
return|;
block|}
DECL|method|indexService
specifier|public
name|IndexService
name|indexService
parameter_list|(
name|String
name|index
parameter_list|)
block|{
return|return
name|indices
operator|.
name|get
argument_list|(
name|index
argument_list|)
return|;
block|}
DECL|method|indexServiceSafe
annotation|@
name|Override
specifier|public
name|IndexService
name|indexServiceSafe
parameter_list|(
name|String
name|index
parameter_list|)
throws|throws
name|IndexMissingException
block|{
name|IndexService
name|indexService
init|=
name|indexService
argument_list|(
name|index
argument_list|)
decl_stmt|;
if|if
condition|(
name|indexService
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IndexMissingException
argument_list|(
operator|new
name|Index
argument_list|(
name|index
argument_list|)
argument_list|)
throw|;
block|}
return|return
name|indexService
return|;
block|}
DECL|method|createIndex
specifier|public
specifier|synchronized
name|IndexService
name|createIndex
parameter_list|(
name|String
name|sIndexName
parameter_list|,
name|Settings
name|settings
parameter_list|,
name|String
name|localNodeId
parameter_list|)
throws|throws
name|ElasticSearchException
block|{
if|if
condition|(
operator|!
name|lifecycle
operator|.
name|started
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|ElasticSearchIllegalStateException
argument_list|(
literal|"Can't create an index ["
operator|+
name|sIndexName
operator|+
literal|"], node is closed"
argument_list|)
throw|;
block|}
name|Index
name|index
init|=
operator|new
name|Index
argument_list|(
name|sIndexName
argument_list|)
decl_stmt|;
if|if
condition|(
name|indicesInjectors
operator|.
name|containsKey
argument_list|(
name|index
operator|.
name|name
argument_list|()
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IndexAlreadyExistsException
argument_list|(
name|index
argument_list|)
throw|;
block|}
name|indicesLifecycle
operator|.
name|beforeIndexCreated
argument_list|(
name|index
argument_list|)
expr_stmt|;
name|logger
operator|.
name|debug
argument_list|(
literal|"creating Index [{}], shards [{}]/[{}]"
argument_list|,
name|sIndexName
argument_list|,
name|settings
operator|.
name|get
argument_list|(
name|SETTING_NUMBER_OF_SHARDS
argument_list|)
argument_list|,
name|settings
operator|.
name|get
argument_list|(
name|SETTING_NUMBER_OF_REPLICAS
argument_list|)
argument_list|)
expr_stmt|;
name|Settings
name|indexSettings
init|=
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"settingsType"
argument_list|,
literal|"index"
argument_list|)
operator|.
name|put
argument_list|(
name|this
operator|.
name|settings
argument_list|)
operator|.
name|put
argument_list|(
name|settings
argument_list|)
operator|.
name|classLoader
argument_list|(
name|settings
operator|.
name|getClassLoader
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|ModulesBuilder
name|modules
init|=
operator|new
name|ModulesBuilder
argument_list|()
decl_stmt|;
name|modules
operator|.
name|add
argument_list|(
operator|new
name|IndexNameModule
argument_list|(
name|index
argument_list|)
argument_list|)
expr_stmt|;
name|modules
operator|.
name|add
argument_list|(
operator|new
name|LocalNodeIdModule
argument_list|(
name|localNodeId
argument_list|)
argument_list|)
expr_stmt|;
name|modules
operator|.
name|add
argument_list|(
operator|new
name|IndexSettingsModule
argument_list|(
name|index
argument_list|,
name|indexSettings
argument_list|)
argument_list|)
expr_stmt|;
name|modules
operator|.
name|add
argument_list|(
operator|new
name|IndexPluginsModule
argument_list|(
name|indexSettings
argument_list|,
name|pluginsService
argument_list|)
argument_list|)
expr_stmt|;
name|modules
operator|.
name|add
argument_list|(
operator|new
name|IndexStoreModule
argument_list|(
name|indexSettings
argument_list|)
argument_list|)
expr_stmt|;
name|modules
operator|.
name|add
argument_list|(
operator|new
name|IndexEngineModule
argument_list|(
name|indexSettings
argument_list|)
argument_list|)
expr_stmt|;
name|modules
operator|.
name|add
argument_list|(
operator|new
name|AnalysisModule
argument_list|(
name|indexSettings
argument_list|,
name|indicesAnalysisService
argument_list|)
argument_list|)
expr_stmt|;
name|modules
operator|.
name|add
argument_list|(
operator|new
name|SimilarityModule
argument_list|(
name|indexSettings
argument_list|)
argument_list|)
expr_stmt|;
name|modules
operator|.
name|add
argument_list|(
operator|new
name|IndexCacheModule
argument_list|(
name|indexSettings
argument_list|)
argument_list|)
expr_stmt|;
name|modules
operator|.
name|add
argument_list|(
operator|new
name|IndexQueryParserModule
argument_list|(
name|indexSettings
argument_list|)
argument_list|)
expr_stmt|;
name|modules
operator|.
name|add
argument_list|(
operator|new
name|MapperServiceModule
argument_list|()
argument_list|)
expr_stmt|;
name|modules
operator|.
name|add
argument_list|(
operator|new
name|IndexGatewayModule
argument_list|(
name|indexSettings
argument_list|,
name|injector
operator|.
name|getInstance
argument_list|(
name|Gateway
operator|.
name|class
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|modules
operator|.
name|add
argument_list|(
operator|new
name|IndexModule
argument_list|()
argument_list|)
expr_stmt|;
name|modules
operator|.
name|add
argument_list|(
operator|new
name|PercolatorModule
argument_list|()
argument_list|)
expr_stmt|;
name|Injector
name|indexInjector
init|=
name|modules
operator|.
name|createChildInjector
argument_list|(
name|injector
argument_list|)
decl_stmt|;
name|indicesInjectors
operator|.
name|put
argument_list|(
name|index
operator|.
name|name
argument_list|()
argument_list|,
name|indexInjector
argument_list|)
expr_stmt|;
name|IndexService
name|indexService
init|=
name|indexInjector
operator|.
name|getInstance
argument_list|(
name|IndexService
operator|.
name|class
argument_list|)
decl_stmt|;
name|indicesLifecycle
operator|.
name|afterIndexCreated
argument_list|(
name|indexService
argument_list|)
expr_stmt|;
name|indices
operator|=
name|newMapBuilder
argument_list|(
name|indices
argument_list|)
operator|.
name|put
argument_list|(
name|index
operator|.
name|name
argument_list|()
argument_list|,
name|indexService
argument_list|)
operator|.
name|immutableMap
argument_list|()
expr_stmt|;
return|return
name|indexService
return|;
block|}
DECL|method|cleanIndex
annotation|@
name|Override
specifier|public
specifier|synchronized
name|void
name|cleanIndex
parameter_list|(
name|String
name|index
parameter_list|,
name|String
name|reason
parameter_list|)
throws|throws
name|ElasticSearchException
block|{
name|deleteIndex
argument_list|(
name|index
argument_list|,
literal|false
argument_list|,
name|reason
argument_list|)
expr_stmt|;
block|}
DECL|method|deleteIndex
annotation|@
name|Override
specifier|public
specifier|synchronized
name|void
name|deleteIndex
parameter_list|(
name|String
name|index
parameter_list|,
name|String
name|reason
parameter_list|)
throws|throws
name|ElasticSearchException
block|{
name|deleteIndex
argument_list|(
name|index
argument_list|,
literal|true
argument_list|,
name|reason
argument_list|)
expr_stmt|;
block|}
DECL|method|deleteIndex
specifier|private
name|void
name|deleteIndex
parameter_list|(
name|String
name|index
parameter_list|,
name|boolean
name|delete
parameter_list|,
name|String
name|reason
parameter_list|)
throws|throws
name|ElasticSearchException
block|{
name|Injector
name|indexInjector
decl_stmt|;
name|IndexService
name|indexService
decl_stmt|;
synchronized|synchronized
init|(
name|this
init|)
block|{
name|indexInjector
operator|=
name|indicesInjectors
operator|.
name|remove
argument_list|(
name|index
argument_list|)
expr_stmt|;
if|if
condition|(
name|indexInjector
operator|==
literal|null
condition|)
block|{
if|if
condition|(
operator|!
name|delete
condition|)
block|{
return|return;
block|}
throw|throw
operator|new
name|IndexMissingException
argument_list|(
operator|new
name|Index
argument_list|(
name|index
argument_list|)
argument_list|)
throw|;
block|}
if|if
condition|(
name|delete
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"deleting Index [{}]"
argument_list|,
name|index
argument_list|)
expr_stmt|;
block|}
name|Map
argument_list|<
name|String
argument_list|,
name|IndexService
argument_list|>
name|tmpMap
init|=
name|newHashMap
argument_list|(
name|indices
argument_list|)
decl_stmt|;
name|indexService
operator|=
name|tmpMap
operator|.
name|remove
argument_list|(
name|index
argument_list|)
expr_stmt|;
name|indices
operator|=
name|ImmutableMap
operator|.
name|copyOf
argument_list|(
name|tmpMap
argument_list|)
expr_stmt|;
block|}
name|indicesLifecycle
operator|.
name|beforeIndexClosed
argument_list|(
name|indexService
argument_list|,
name|delete
argument_list|)
expr_stmt|;
for|for
control|(
name|Class
argument_list|<
name|?
extends|extends
name|CloseableIndexComponent
argument_list|>
name|closeable
range|:
name|pluginsService
operator|.
name|indexServices
argument_list|()
control|)
block|{
name|indexInjector
operator|.
name|getInstance
argument_list|(
name|closeable
argument_list|)
operator|.
name|close
argument_list|(
name|delete
argument_list|)
expr_stmt|;
block|}
operator|(
operator|(
name|InternalIndexService
operator|)
name|indexService
operator|)
operator|.
name|close
argument_list|(
name|delete
argument_list|,
name|reason
argument_list|)
expr_stmt|;
name|indexInjector
operator|.
name|getInstance
argument_list|(
name|PercolatorService
operator|.
name|class
argument_list|)
operator|.
name|close
argument_list|()
expr_stmt|;
name|indexInjector
operator|.
name|getInstance
argument_list|(
name|IndexCache
operator|.
name|class
argument_list|)
operator|.
name|close
argument_list|()
expr_stmt|;
name|indexInjector
operator|.
name|getInstance
argument_list|(
name|AnalysisService
operator|.
name|class
argument_list|)
operator|.
name|close
argument_list|()
expr_stmt|;
name|indexInjector
operator|.
name|getInstance
argument_list|(
name|IndexEngine
operator|.
name|class
argument_list|)
operator|.
name|close
argument_list|()
expr_stmt|;
name|indexInjector
operator|.
name|getInstance
argument_list|(
name|IndexServiceManagement
operator|.
name|class
argument_list|)
operator|.
name|close
argument_list|()
expr_stmt|;
name|indexInjector
operator|.
name|getInstance
argument_list|(
name|IndexGateway
operator|.
name|class
argument_list|)
operator|.
name|close
argument_list|(
name|delete
argument_list|)
expr_stmt|;
name|Injectors
operator|.
name|close
argument_list|(
name|injector
argument_list|)
expr_stmt|;
name|indicesLifecycle
operator|.
name|afterIndexClosed
argument_list|(
name|indexService
operator|.
name|index
argument_list|()
argument_list|,
name|delete
argument_list|)
expr_stmt|;
if|if
condition|(
name|delete
condition|)
block|{
name|FileSystemUtils
operator|.
name|deleteRecursively
argument_list|(
name|nodeEnv
operator|.
name|indexLocation
argument_list|(
operator|new
name|Index
argument_list|(
name|index
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|CacheReaderPurgeListener
specifier|private
specifier|static
class|class
name|CacheReaderPurgeListener
implements|implements
name|IndexReaderPurgedListener
block|{
DECL|field|indicesService
specifier|private
specifier|final
name|IndicesService
name|indicesService
decl_stmt|;
DECL|method|CacheReaderPurgeListener
specifier|private
name|CacheReaderPurgeListener
parameter_list|(
name|IndicesService
name|indicesService
parameter_list|)
block|{
name|this
operator|.
name|indicesService
operator|=
name|indicesService
expr_stmt|;
block|}
DECL|method|indexReaderPurged
annotation|@
name|Override
specifier|public
name|void
name|indexReaderPurged
parameter_list|(
name|IndexReader
name|reader
parameter_list|)
block|{
for|for
control|(
name|IndexService
name|indexService
range|:
name|indicesService
control|)
block|{
name|indexService
operator|.
name|cache
argument_list|()
operator|.
name|clear
argument_list|(
name|reader
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

