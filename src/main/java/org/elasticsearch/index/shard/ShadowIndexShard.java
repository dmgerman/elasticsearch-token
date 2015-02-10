begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.shard
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|shard
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchIllegalStateException
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
name|ClusterService
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
name|routing
operator|.
name|ShardRouting
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
name|aliases
operator|.
name|IndexAliasesService
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
name|bitset
operator|.
name|ShardBitsetFilterCache
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
name|filter
operator|.
name|ShardFilterCache
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
name|query
operator|.
name|ShardQueryCache
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
name|EngineFactory
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
name|fielddata
operator|.
name|IndexFieldDataService
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
name|fielddata
operator|.
name|ShardFieldData
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
name|get
operator|.
name|ShardGetService
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
name|mapper
operator|.
name|MapperService
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
name|percolator
operator|.
name|PercolatorQueriesRegistry
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
name|stats
operator|.
name|ShardPercolateService
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
name|IndexQueryParserService
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
name|search
operator|.
name|stats
operator|.
name|ShardSearchService
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
name|suggest
operator|.
name|stats
operator|.
name|ShardSuggestService
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
name|termvectors
operator|.
name|ShardTermVectorsService
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
name|index
operator|.
name|warmer
operator|.
name|ShardIndexWarmerService
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
name|IndicesLifecycle
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

begin_comment
comment|/**  * ShadowIndexShard extends {@link IndexShard} to add file synchronization  * from the primary when a flush happens. It also ensures that a replica being  * promoted to a primary causes the shard to fail, kicking off a re-allocation  * of the primary shard.  */
end_comment

begin_class
DECL|class|ShadowIndexShard
specifier|public
specifier|final
class|class
name|ShadowIndexShard
extends|extends
name|IndexShard
block|{
DECL|field|mutex
specifier|private
specifier|final
name|Object
name|mutex
init|=
operator|new
name|Object
argument_list|()
decl_stmt|;
annotation|@
name|Inject
DECL|method|ShadowIndexShard
specifier|public
name|ShadowIndexShard
parameter_list|(
name|ShardId
name|shardId
parameter_list|,
annotation|@
name|IndexSettings
name|Settings
name|indexSettings
parameter_list|,
name|IndexSettingsService
name|indexSettingsService
parameter_list|,
name|IndicesLifecycle
name|indicesLifecycle
parameter_list|,
name|Store
name|store
parameter_list|,
name|MergeSchedulerProvider
name|mergeScheduler
parameter_list|,
name|Translog
name|translog
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|,
name|MapperService
name|mapperService
parameter_list|,
name|IndexQueryParserService
name|queryParserService
parameter_list|,
name|IndexCache
name|indexCache
parameter_list|,
name|IndexAliasesService
name|indexAliasesService
parameter_list|,
name|ShardIndexingService
name|indexingService
parameter_list|,
name|ShardGetService
name|getService
parameter_list|,
name|ShardSearchService
name|searchService
parameter_list|,
name|ShardIndexWarmerService
name|shardWarmerService
parameter_list|,
name|ShardFilterCache
name|shardFilterCache
parameter_list|,
name|ShardFieldData
name|shardFieldData
parameter_list|,
name|PercolatorQueriesRegistry
name|percolatorQueriesRegistry
parameter_list|,
name|ShardPercolateService
name|shardPercolateService
parameter_list|,
name|CodecService
name|codecService
parameter_list|,
name|ShardTermVectorsService
name|termVectorsService
parameter_list|,
name|IndexFieldDataService
name|indexFieldDataService
parameter_list|,
name|IndexService
name|indexService
parameter_list|,
name|ShardSuggestService
name|shardSuggestService
parameter_list|,
name|ShardQueryCache
name|shardQueryCache
parameter_list|,
name|ShardBitsetFilterCache
name|shardBitsetFilterCache
parameter_list|,
annotation|@
name|Nullable
name|IndicesWarmer
name|warmer
parameter_list|,
name|SnapshotDeletionPolicy
name|deletionPolicy
parameter_list|,
name|SimilarityService
name|similarityService
parameter_list|,
name|MergePolicyProvider
name|mergePolicyProvider
parameter_list|,
name|EngineFactory
name|factory
parameter_list|,
name|ClusterService
name|clusterService
parameter_list|)
block|{
name|super
argument_list|(
name|shardId
argument_list|,
name|indexSettings
argument_list|,
name|indexSettingsService
argument_list|,
name|indicesLifecycle
argument_list|,
name|store
argument_list|,
name|mergeScheduler
argument_list|,
name|translog
argument_list|,
name|threadPool
argument_list|,
name|mapperService
argument_list|,
name|queryParserService
argument_list|,
name|indexCache
argument_list|,
name|indexAliasesService
argument_list|,
name|indexingService
argument_list|,
name|getService
argument_list|,
name|searchService
argument_list|,
name|shardWarmerService
argument_list|,
name|shardFilterCache
argument_list|,
name|shardFieldData
argument_list|,
name|percolatorQueriesRegistry
argument_list|,
name|shardPercolateService
argument_list|,
name|codecService
argument_list|,
name|termVectorsService
argument_list|,
name|indexFieldDataService
argument_list|,
name|indexService
argument_list|,
name|shardSuggestService
argument_list|,
name|shardQueryCache
argument_list|,
name|shardBitsetFilterCache
argument_list|,
name|warmer
argument_list|,
name|deletionPolicy
argument_list|,
name|similarityService
argument_list|,
name|mergePolicyProvider
argument_list|,
name|factory
argument_list|,
name|clusterService
argument_list|)
expr_stmt|;
block|}
comment|/**      * In addition to the regular accounting done in      * {@link IndexShard#routingEntry(org.elasticsearch.cluster.routing.ShardRouting)},      * if this shadow replica needs to be promoted to a primary, the shard is      * failed in order to allow a new primary to be re-allocated.      */
annotation|@
name|Override
DECL|method|routingEntry
specifier|public
name|IndexShard
name|routingEntry
parameter_list|(
name|ShardRouting
name|newRouting
parameter_list|)
block|{
if|if
condition|(
name|newRouting
operator|.
name|primary
argument_list|()
operator|==
literal|true
condition|)
block|{
comment|// becoming a primary
throw|throw
operator|new
name|ElasticsearchIllegalStateException
argument_list|(
literal|"can't promote shard to primary"
argument_list|)
throw|;
block|}
return|return
name|super
operator|.
name|routingEntry
argument_list|(
name|newRouting
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|newEngine
specifier|protected
name|Engine
name|newEngine
parameter_list|()
block|{
assert|assert
name|this
operator|.
name|shardRouting
operator|.
name|primary
argument_list|()
operator|==
literal|false
assert|;
return|return
name|engineFactory
operator|.
name|newReadOnlyEngine
argument_list|(
name|config
argument_list|)
return|;
block|}
DECL|method|allowsPrimaryPromotion
specifier|public
name|boolean
name|allowsPrimaryPromotion
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
block|}
end_class

end_unit

