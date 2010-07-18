begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.service
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|service
package|;
end_package

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
name|Lists
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
name|CloseableIndexComponent
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
name|Module
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
name|gateway
operator|.
name|none
operator|.
name|NoneGateway
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
name|AbstractIndexComponent
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
name|Index
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
name|IndexShardAlreadyExistsException
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
name|IndexShardMissingException
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
name|deletionpolicy
operator|.
name|DeletionPolicyModule
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
name|EngineModule
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
name|IndexShardGatewayModule
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
name|IndexShardGatewayService
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
name|MergePolicyModule
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
name|MergeSchedulerModule
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
name|routing
operator|.
name|OperationRouting
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
name|IndexShardManagement
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
name|IndexShardModule
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
name|IndexStore
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
name|StoreModule
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
name|translog
operator|.
name|TranslogModule
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
name|InternalIndicesLifecycle
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
name|plugins
operator|.
name|ShardsPluginsModule
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|annotation
operator|.
name|Nullable
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

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|InternalIndexService
specifier|public
class|class
name|InternalIndexService
extends|extends
name|AbstractIndexComponent
implements|implements
name|IndexService
block|{
DECL|field|injector
specifier|private
specifier|final
name|Injector
name|injector
decl_stmt|;
DECL|field|indexSettings
specifier|private
specifier|final
name|Settings
name|indexSettings
decl_stmt|;
DECL|field|pluginsService
specifier|private
specifier|final
name|PluginsService
name|pluginsService
decl_stmt|;
DECL|field|indicesLifecycle
specifier|private
specifier|final
name|InternalIndicesLifecycle
name|indicesLifecycle
decl_stmt|;
DECL|field|mapperService
specifier|private
specifier|final
name|MapperService
name|mapperService
decl_stmt|;
DECL|field|queryParserService
specifier|private
specifier|final
name|IndexQueryParserService
name|queryParserService
decl_stmt|;
DECL|field|similarityService
specifier|private
specifier|final
name|SimilarityService
name|similarityService
decl_stmt|;
DECL|field|indexCache
specifier|private
specifier|final
name|IndexCache
name|indexCache
decl_stmt|;
DECL|field|indexEngine
specifier|private
specifier|final
name|IndexEngine
name|indexEngine
decl_stmt|;
DECL|field|indexGateway
specifier|private
specifier|final
name|IndexGateway
name|indexGateway
decl_stmt|;
DECL|field|indexStore
specifier|private
specifier|final
name|IndexStore
name|indexStore
decl_stmt|;
DECL|field|operationRouting
specifier|private
specifier|final
name|OperationRouting
name|operationRouting
decl_stmt|;
DECL|field|shardsInjectors
specifier|private
specifier|volatile
name|ImmutableMap
argument_list|<
name|Integer
argument_list|,
name|Injector
argument_list|>
name|shardsInjectors
init|=
name|ImmutableMap
operator|.
name|of
argument_list|()
decl_stmt|;
DECL|field|shards
specifier|private
specifier|volatile
name|ImmutableMap
argument_list|<
name|Integer
argument_list|,
name|IndexShard
argument_list|>
name|shards
init|=
name|ImmutableMap
operator|.
name|of
argument_list|()
decl_stmt|;
DECL|field|cleanCacheOnIndicesLifecycleListener
specifier|private
specifier|final
name|CleanCacheOnIndicesLifecycleListener
name|cleanCacheOnIndicesLifecycleListener
init|=
operator|new
name|CleanCacheOnIndicesLifecycleListener
argument_list|()
decl_stmt|;
DECL|method|InternalIndexService
annotation|@
name|Inject
specifier|public
name|InternalIndexService
parameter_list|(
name|Injector
name|injector
parameter_list|,
name|Index
name|index
parameter_list|,
annotation|@
name|IndexSettings
name|Settings
name|indexSettings
parameter_list|,
name|MapperService
name|mapperService
parameter_list|,
name|IndexQueryParserService
name|queryParserService
parameter_list|,
name|SimilarityService
name|similarityService
parameter_list|,
name|IndexCache
name|indexCache
parameter_list|,
name|IndexEngine
name|indexEngine
parameter_list|,
name|IndexGateway
name|indexGateway
parameter_list|,
name|IndexStore
name|indexStore
parameter_list|,
name|OperationRouting
name|operationRouting
parameter_list|)
block|{
name|super
argument_list|(
name|index
argument_list|,
name|indexSettings
argument_list|)
expr_stmt|;
name|this
operator|.
name|injector
operator|=
name|injector
expr_stmt|;
name|this
operator|.
name|indexSettings
operator|=
name|indexSettings
expr_stmt|;
name|this
operator|.
name|mapperService
operator|=
name|mapperService
expr_stmt|;
name|this
operator|.
name|queryParserService
operator|=
name|queryParserService
expr_stmt|;
name|this
operator|.
name|similarityService
operator|=
name|similarityService
expr_stmt|;
name|this
operator|.
name|indexCache
operator|=
name|indexCache
expr_stmt|;
name|this
operator|.
name|indexEngine
operator|=
name|indexEngine
expr_stmt|;
name|this
operator|.
name|indexGateway
operator|=
name|indexGateway
expr_stmt|;
name|this
operator|.
name|indexStore
operator|=
name|indexStore
expr_stmt|;
name|this
operator|.
name|operationRouting
operator|=
name|operationRouting
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
name|this
operator|.
name|indicesLifecycle
operator|=
operator|(
name|InternalIndicesLifecycle
operator|)
name|injector
operator|.
name|getInstance
argument_list|(
name|IndicesLifecycle
operator|.
name|class
argument_list|)
expr_stmt|;
name|this
operator|.
name|indicesLifecycle
operator|.
name|addListener
argument_list|(
name|cleanCacheOnIndicesLifecycleListener
argument_list|)
expr_stmt|;
block|}
DECL|method|numberOfShards
annotation|@
name|Override
specifier|public
name|int
name|numberOfShards
parameter_list|()
block|{
return|return
name|shards
operator|.
name|size
argument_list|()
return|;
block|}
DECL|method|iterator
annotation|@
name|Override
specifier|public
name|UnmodifiableIterator
argument_list|<
name|IndexShard
argument_list|>
name|iterator
parameter_list|()
block|{
return|return
name|shards
operator|.
name|values
argument_list|()
operator|.
name|iterator
argument_list|()
return|;
block|}
DECL|method|hasShard
annotation|@
name|Override
specifier|public
name|boolean
name|hasShard
parameter_list|(
name|int
name|shardId
parameter_list|)
block|{
return|return
name|shards
operator|.
name|containsKey
argument_list|(
name|shardId
argument_list|)
return|;
block|}
DECL|method|shard
annotation|@
name|Override
specifier|public
name|IndexShard
name|shard
parameter_list|(
name|int
name|shardId
parameter_list|)
block|{
return|return
name|shards
operator|.
name|get
argument_list|(
name|shardId
argument_list|)
return|;
block|}
DECL|method|shardSafe
annotation|@
name|Override
specifier|public
name|IndexShard
name|shardSafe
parameter_list|(
name|int
name|shardId
parameter_list|)
throws|throws
name|IndexShardMissingException
block|{
name|IndexShard
name|indexShard
init|=
name|shard
argument_list|(
name|shardId
argument_list|)
decl_stmt|;
if|if
condition|(
name|indexShard
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IndexShardMissingException
argument_list|(
operator|new
name|ShardId
argument_list|(
name|index
argument_list|,
name|shardId
argument_list|)
argument_list|)
throw|;
block|}
return|return
name|indexShard
return|;
block|}
DECL|method|shardIds
annotation|@
name|Override
specifier|public
name|Set
argument_list|<
name|Integer
argument_list|>
name|shardIds
parameter_list|()
block|{
return|return
name|newHashSet
argument_list|(
name|shards
operator|.
name|keySet
argument_list|()
argument_list|)
return|;
block|}
DECL|method|injector
annotation|@
name|Override
specifier|public
name|Injector
name|injector
parameter_list|()
block|{
return|return
name|injector
return|;
block|}
DECL|method|gateway
annotation|@
name|Override
specifier|public
name|IndexGateway
name|gateway
parameter_list|()
block|{
return|return
name|indexGateway
return|;
block|}
DECL|method|store
annotation|@
name|Override
specifier|public
name|IndexStore
name|store
parameter_list|()
block|{
return|return
name|indexStore
return|;
block|}
DECL|method|cache
annotation|@
name|Override
specifier|public
name|IndexCache
name|cache
parameter_list|()
block|{
return|return
name|indexCache
return|;
block|}
DECL|method|operationRouting
annotation|@
name|Override
specifier|public
name|OperationRouting
name|operationRouting
parameter_list|()
block|{
return|return
name|operationRouting
return|;
block|}
DECL|method|mapperService
annotation|@
name|Override
specifier|public
name|MapperService
name|mapperService
parameter_list|()
block|{
return|return
name|mapperService
return|;
block|}
DECL|method|queryParserService
annotation|@
name|Override
specifier|public
name|IndexQueryParserService
name|queryParserService
parameter_list|()
block|{
return|return
name|queryParserService
return|;
block|}
DECL|method|similarityService
annotation|@
name|Override
specifier|public
name|SimilarityService
name|similarityService
parameter_list|()
block|{
return|return
name|similarityService
return|;
block|}
DECL|method|indexEngine
annotation|@
name|Override
specifier|public
name|IndexEngine
name|indexEngine
parameter_list|()
block|{
return|return
name|indexEngine
return|;
block|}
DECL|method|close
annotation|@
name|Override
specifier|public
specifier|synchronized
name|void
name|close
parameter_list|(
name|boolean
name|delete
parameter_list|)
block|{
try|try
block|{
for|for
control|(
name|int
name|shardId
range|:
name|shardIds
argument_list|()
control|)
block|{
try|try
block|{
name|deleteShard
argument_list|(
name|shardId
argument_list|,
name|delete
argument_list|,
name|delete
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
literal|"failed to close shard, delete [{}]"
argument_list|,
name|e
argument_list|,
name|delete
argument_list|)
expr_stmt|;
block|}
block|}
block|}
finally|finally
block|{
name|indicesLifecycle
operator|.
name|removeListener
argument_list|(
name|cleanCacheOnIndicesLifecycleListener
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|shardInjector
annotation|@
name|Override
specifier|public
name|Injector
name|shardInjector
parameter_list|(
name|int
name|shardId
parameter_list|)
throws|throws
name|ElasticSearchException
block|{
return|return
name|shardsInjectors
operator|.
name|get
argument_list|(
name|shardId
argument_list|)
return|;
block|}
DECL|method|shardInjectorSafe
annotation|@
name|Override
specifier|public
name|Injector
name|shardInjectorSafe
parameter_list|(
name|int
name|shardId
parameter_list|)
throws|throws
name|IndexShardMissingException
block|{
name|Injector
name|shardInjector
init|=
name|shardInjector
argument_list|(
name|shardId
argument_list|)
decl_stmt|;
if|if
condition|(
name|shardInjector
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IndexShardMissingException
argument_list|(
operator|new
name|ShardId
argument_list|(
name|index
argument_list|,
name|shardId
argument_list|)
argument_list|)
throw|;
block|}
return|return
name|shardInjector
return|;
block|}
DECL|method|createShard
annotation|@
name|Override
specifier|public
specifier|synchronized
name|IndexShard
name|createShard
parameter_list|(
name|int
name|sShardId
parameter_list|)
throws|throws
name|ElasticSearchException
block|{
name|ShardId
name|shardId
init|=
operator|new
name|ShardId
argument_list|(
name|index
argument_list|,
name|sShardId
argument_list|)
decl_stmt|;
if|if
condition|(
name|shardsInjectors
operator|.
name|containsKey
argument_list|(
name|shardId
operator|.
name|id
argument_list|()
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IndexShardAlreadyExistsException
argument_list|(
name|shardId
operator|+
literal|" already exists"
argument_list|)
throw|;
block|}
name|indicesLifecycle
operator|.
name|beforeIndexShardCreated
argument_list|(
name|shardId
argument_list|)
expr_stmt|;
name|logger
operator|.
name|debug
argument_list|(
literal|"creating shard_id [{}]"
argument_list|,
name|shardId
operator|.
name|id
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Module
argument_list|>
name|modules
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
name|modules
operator|.
name|add
argument_list|(
operator|new
name|ShardsPluginsModule
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
name|IndexShardModule
argument_list|(
name|shardId
argument_list|)
argument_list|)
expr_stmt|;
name|modules
operator|.
name|add
argument_list|(
operator|new
name|StoreModule
argument_list|(
name|indexSettings
argument_list|,
name|injector
operator|.
name|getInstance
argument_list|(
name|IndexStore
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
name|DeletionPolicyModule
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
name|MergePolicyModule
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
name|MergeSchedulerModule
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
name|TranslogModule
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
name|EngineModule
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
name|IndexShardGatewayModule
argument_list|(
name|injector
operator|.
name|getInstance
argument_list|(
name|IndexGateway
operator|.
name|class
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|pluginsService
operator|.
name|processModules
argument_list|(
name|modules
argument_list|)
expr_stmt|;
name|Injector
name|shardInjector
init|=
name|injector
operator|.
name|createChildInjector
argument_list|(
name|modules
argument_list|)
decl_stmt|;
name|shardsInjectors
operator|=
name|newMapBuilder
argument_list|(
name|shardsInjectors
argument_list|)
operator|.
name|put
argument_list|(
name|shardId
operator|.
name|id
argument_list|()
argument_list|,
name|shardInjector
argument_list|)
operator|.
name|immutableMap
argument_list|()
expr_stmt|;
name|IndexShard
name|indexShard
init|=
name|shardInjector
operator|.
name|getInstance
argument_list|(
name|IndexShard
operator|.
name|class
argument_list|)
decl_stmt|;
name|indicesLifecycle
operator|.
name|afterIndexShardCreated
argument_list|(
name|indexShard
argument_list|)
expr_stmt|;
name|shards
operator|=
name|newMapBuilder
argument_list|(
name|shards
argument_list|)
operator|.
name|put
argument_list|(
name|shardId
operator|.
name|id
argument_list|()
argument_list|,
name|indexShard
argument_list|)
operator|.
name|immutableMap
argument_list|()
expr_stmt|;
return|return
name|indexShard
return|;
block|}
DECL|method|cleanShard
annotation|@
name|Override
specifier|public
specifier|synchronized
name|void
name|cleanShard
parameter_list|(
name|int
name|shardId
parameter_list|)
throws|throws
name|ElasticSearchException
block|{
name|deleteShard
argument_list|(
name|shardId
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
DECL|method|deleteShard
specifier|private
specifier|synchronized
name|void
name|deleteShard
parameter_list|(
name|int
name|shardId
parameter_list|,
name|boolean
name|delete
parameter_list|,
name|boolean
name|deleteGateway
parameter_list|)
throws|throws
name|ElasticSearchException
block|{
name|Map
argument_list|<
name|Integer
argument_list|,
name|Injector
argument_list|>
name|tmpShardInjectors
init|=
name|newHashMap
argument_list|(
name|shardsInjectors
argument_list|)
decl_stmt|;
name|Injector
name|shardInjector
init|=
name|tmpShardInjectors
operator|.
name|remove
argument_list|(
name|shardId
argument_list|)
decl_stmt|;
if|if
condition|(
name|shardInjector
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
name|IndexShardMissingException
argument_list|(
operator|new
name|ShardId
argument_list|(
name|index
argument_list|,
name|shardId
argument_list|)
argument_list|)
throw|;
block|}
name|shardsInjectors
operator|=
name|ImmutableMap
operator|.
name|copyOf
argument_list|(
name|tmpShardInjectors
argument_list|)
expr_stmt|;
if|if
condition|(
name|delete
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"deleting shard_id [{}]"
argument_list|,
name|shardId
argument_list|)
expr_stmt|;
block|}
name|Map
argument_list|<
name|Integer
argument_list|,
name|IndexShard
argument_list|>
name|tmpShardsMap
init|=
name|newHashMap
argument_list|(
name|shards
argument_list|)
decl_stmt|;
name|IndexShard
name|indexShard
init|=
name|tmpShardsMap
operator|.
name|remove
argument_list|(
name|shardId
argument_list|)
decl_stmt|;
name|shards
operator|=
name|ImmutableMap
operator|.
name|copyOf
argument_list|(
name|tmpShardsMap
argument_list|)
expr_stmt|;
name|ShardId
name|sId
init|=
operator|new
name|ShardId
argument_list|(
name|index
argument_list|,
name|shardId
argument_list|)
decl_stmt|;
name|indicesLifecycle
operator|.
name|beforeIndexShardClosed
argument_list|(
name|sId
argument_list|,
name|indexShard
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
name|shardServices
argument_list|()
control|)
block|{
try|try
block|{
name|shardInjector
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
literal|"failed to clean plugin shard service [{}]"
argument_list|,
name|e
argument_list|,
name|closeable
argument_list|)
expr_stmt|;
block|}
block|}
comment|// close shard actions
if|if
condition|(
name|indexShard
operator|!=
literal|null
condition|)
block|{
name|shardInjector
operator|.
name|getInstance
argument_list|(
name|IndexShardManagement
operator|.
name|class
argument_list|)
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|// this logic is tricky, we want to close the engine so we rollback the changes done to it
comment|// and close the shard so no operations are allowed to it
if|if
condition|(
name|indexShard
operator|!=
literal|null
condition|)
block|{
name|indexShard
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
try|try
block|{
name|shardInjector
operator|.
name|getInstance
argument_list|(
name|Engine
operator|.
name|class
argument_list|)
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
comment|// ignore
block|}
try|try
block|{
comment|// now, we can snapshot to the gateway, it will be only the translog
name|shardInjector
operator|.
name|getInstance
argument_list|(
name|IndexShardGatewayService
operator|.
name|class
argument_list|)
operator|.
name|close
argument_list|(
name|deleteGateway
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// ignore
block|}
try|try
block|{
comment|// now we can close the translog
name|shardInjector
operator|.
name|getInstance
argument_list|(
name|Translog
operator|.
name|class
argument_list|)
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
comment|// ignore
block|}
comment|// call this before we close the store, so we can release resources for it
name|indicesLifecycle
operator|.
name|afterIndexShardClosed
argument_list|(
name|sId
argument_list|,
name|delete
argument_list|)
expr_stmt|;
comment|// if we delete or have no gateway or the store is not persistent, clean the store...
name|Store
name|store
init|=
name|shardInjector
operator|.
name|getInstance
argument_list|(
name|Store
operator|.
name|class
argument_list|)
decl_stmt|;
if|if
condition|(
name|delete
operator|||
name|indexGateway
operator|.
name|type
argument_list|()
operator|.
name|equals
argument_list|(
name|NoneGateway
operator|.
name|TYPE
argument_list|)
operator|||
operator|!
name|indexStore
operator|.
name|persistent
argument_list|()
condition|)
block|{
try|try
block|{
name|store
operator|.
name|fullDelete
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"failed to clean store on shard deletion"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
comment|// and close it
try|try
block|{
name|store
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"failed to close store on shard deletion"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
name|Injectors
operator|.
name|close
argument_list|(
name|injector
argument_list|)
expr_stmt|;
block|}
DECL|class|CleanCacheOnIndicesLifecycleListener
class|class
name|CleanCacheOnIndicesLifecycleListener
extends|extends
name|IndicesLifecycle
operator|.
name|Listener
block|{
DECL|method|beforeIndexShardClosed
annotation|@
name|Override
specifier|public
name|void
name|beforeIndexShardClosed
parameter_list|(
name|ShardId
name|shardId
parameter_list|,
annotation|@
name|Nullable
name|IndexShard
name|indexShard
parameter_list|,
name|boolean
name|delete
parameter_list|)
block|{
name|indexCache
operator|.
name|clearUnreferenced
argument_list|()
expr_stmt|;
block|}
DECL|method|afterIndexShardClosed
annotation|@
name|Override
specifier|public
name|void
name|afterIndexShardClosed
parameter_list|(
name|ShardId
name|shardId
parameter_list|,
name|boolean
name|delete
parameter_list|)
block|{
name|indexCache
operator|.
name|clearUnreferenced
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

