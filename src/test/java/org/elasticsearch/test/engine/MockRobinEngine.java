begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.test.engine
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|test
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
name|AssertingIndexSearcher
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
name|apache
operator|.
name|lucene
operator|.
name|search
operator|.
name|SearcherManager
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
name|engine
operator|.
name|robin
operator|.
name|RobinEngine
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
name|indices
operator|.
name|warmer
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
name|test
operator|.
name|ElasticSearchTestCase
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
name|Random
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

begin_class
DECL|class|MockRobinEngine
specifier|public
specifier|final
class|class
name|MockRobinEngine
extends|extends
name|RobinEngine
implements|implements
name|Engine
block|{
DECL|field|INFLIGHT_ENGINE_SEARCHERS
specifier|public
specifier|static
specifier|final
name|ConcurrentMap
argument_list|<
name|AssertingSearcher
argument_list|,
name|RuntimeException
argument_list|>
name|INFLIGHT_ENGINE_SEARCHERS
init|=
operator|new
name|ConcurrentHashMap
argument_list|<
name|AssertingSearcher
argument_list|,
name|RuntimeException
argument_list|>
argument_list|()
decl_stmt|;
DECL|field|random
specifier|private
specifier|final
name|Random
name|random
decl_stmt|;
annotation|@
name|Inject
DECL|method|MockRobinEngine
specifier|public
name|MockRobinEngine
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
name|IndexSettingsService
name|indexSettingsService
parameter_list|,
name|ShardIndexingService
name|indexingService
parameter_list|,
annotation|@
name|Nullable
name|IndicesWarmer
name|warmer
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
parameter_list|,
name|CodecService
name|codecService
parameter_list|)
throws|throws
name|EngineException
block|{
name|super
argument_list|(
name|shardId
argument_list|,
name|indexSettings
argument_list|,
name|threadPool
argument_list|,
name|indexSettingsService
argument_list|,
name|indexingService
argument_list|,
name|warmer
argument_list|,
name|store
argument_list|,
name|deletionPolicy
argument_list|,
name|translog
argument_list|,
name|mergePolicyProvider
argument_list|,
name|mergeScheduler
argument_list|,
name|analysisService
argument_list|,
name|similarityService
argument_list|,
name|codecService
argument_list|)
expr_stmt|;
specifier|final
name|long
name|seed
init|=
name|indexSettings
operator|.
name|getAsLong
argument_list|(
name|ElasticSearchTestCase
operator|.
name|INDEX_SEED_SETTING
argument_list|,
literal|0l
argument_list|)
decl_stmt|;
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
literal|"Using [{}] for shard [{}] seed: [{}]"
argument_list|,
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|shardId
argument_list|,
name|seed
argument_list|)
expr_stmt|;
block|}
name|random
operator|=
operator|new
name|Random
argument_list|(
name|seed
argument_list|)
expr_stmt|;
block|}
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|ElasticSearchException
block|{
try|try
block|{
name|super
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|logger
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
comment|// log debug if we have pending searchers
for|for
control|(
name|Entry
argument_list|<
name|MockRobinEngine
operator|.
name|AssertingSearcher
argument_list|,
name|RuntimeException
argument_list|>
name|entry
range|:
name|MockRobinEngine
operator|.
name|INFLIGHT_ENGINE_SEARCHERS
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"Unreleased Searchers instance for shard [{}]"
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|,
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|shardId
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
annotation|@
name|Override
DECL|method|newSearcher
specifier|protected
name|Searcher
name|newSearcher
parameter_list|(
name|String
name|source
parameter_list|,
name|IndexSearcher
name|searcher
parameter_list|,
name|SearcherManager
name|manager
parameter_list|)
throws|throws
name|EngineException
block|{
comment|// this executes basic query checks and asserts that weights are normalized only once etc.
specifier|final
name|AssertingIndexSearcher
name|assertingIndexSearcher
init|=
operator|new
name|AssertingIndexSearcher
argument_list|(
name|random
argument_list|,
name|searcher
operator|.
name|getTopReaderContext
argument_list|()
argument_list|)
decl_stmt|;
name|assertingIndexSearcher
operator|.
name|setSimilarity
argument_list|(
name|searcher
operator|.
name|getSimilarity
argument_list|()
argument_list|)
expr_stmt|;
return|return
operator|new
name|AssertingSearcher
argument_list|(
name|super
operator|.
name|newSearcher
argument_list|(
name|source
argument_list|,
name|assertingIndexSearcher
argument_list|,
name|manager
argument_list|)
argument_list|,
name|shardId
argument_list|)
return|;
block|}
DECL|class|AssertingSearcher
specifier|public
specifier|static
specifier|final
class|class
name|AssertingSearcher
implements|implements
name|Searcher
block|{
DECL|field|searcher
specifier|private
specifier|final
name|Searcher
name|searcher
decl_stmt|;
DECL|field|shardId
specifier|private
specifier|final
name|ShardId
name|shardId
decl_stmt|;
DECL|method|AssertingSearcher
specifier|public
name|AssertingSearcher
parameter_list|(
name|Searcher
name|searcher
parameter_list|,
name|ShardId
name|shardId
parameter_list|)
block|{
name|this
operator|.
name|searcher
operator|=
name|searcher
expr_stmt|;
name|this
operator|.
name|shardId
operator|=
name|shardId
expr_stmt|;
name|INFLIGHT_ENGINE_SEARCHERS
operator|.
name|put
argument_list|(
name|this
argument_list|,
operator|new
name|RuntimeException
argument_list|(
literal|"Unreleased Searcher, source ["
operator|+
name|searcher
operator|.
name|source
argument_list|()
operator|+
literal|"]"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|source
specifier|public
name|String
name|source
parameter_list|()
block|{
return|return
name|searcher
operator|.
name|source
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|release
specifier|public
name|boolean
name|release
parameter_list|()
throws|throws
name|ElasticSearchException
block|{
name|RuntimeException
name|remove
init|=
name|INFLIGHT_ENGINE_SEARCHERS
operator|.
name|remove
argument_list|(
name|this
argument_list|)
decl_stmt|;
assert|assert
name|remove
operator|!=
literal|null
operator|:
literal|"Released Searcher more than once, source ["
operator|+
name|searcher
operator|.
name|source
argument_list|()
operator|+
literal|"]"
assert|;
return|return
name|searcher
operator|.
name|release
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|reader
specifier|public
name|IndexReader
name|reader
parameter_list|()
block|{
return|return
name|searcher
operator|.
name|reader
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|searcher
specifier|public
name|IndexSearcher
name|searcher
parameter_list|()
block|{
return|return
name|searcher
operator|.
name|searcher
argument_list|()
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
block|}
block|}
end_class

end_unit

