begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|analysis
operator|.
name|hunspell
operator|.
name|Dictionary
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|update
operator|.
name|UpdateHelper
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
name|metadata
operator|.
name|MetaDataIndexUpgradeService
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
name|geo
operator|.
name|ShapesAvailability
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
name|AbstractModule
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
name|util
operator|.
name|ExtensionPoint
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
name|query
operator|.
name|functionscore
operator|.
name|FunctionScoreQueryParser
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
name|MoreLikeThisQueryParser
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
name|HunspellService
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
name|cache
operator|.
name|query
operator|.
name|IndicesQueryCache
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
name|cache
operator|.
name|request
operator|.
name|IndicesRequestCache
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
name|cluster
operator|.
name|IndicesClusterStateService
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
name|fielddata
operator|.
name|cache
operator|.
name|IndicesFieldDataCache
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
name|fielddata
operator|.
name|cache
operator|.
name|IndicesFieldDataCacheListener
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
name|flush
operator|.
name|SyncedFlushService
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
name|memory
operator|.
name|IndexingMemoryController
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
name|query
operator|.
name|IndicesQueriesRegistry
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
name|RecoverySettings
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
name|RecoverySource
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
name|RecoveryTarget
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
name|indices
operator|.
name|store
operator|.
name|TransportNodesListShardStoreMetaData
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
name|ttl
operator|.
name|IndicesTTLService
import|;
end_import

begin_comment
comment|/**  * Configures classes and services that are shared by indices on each node.  */
end_comment

begin_class
DECL|class|IndicesModule
specifier|public
class|class
name|IndicesModule
extends|extends
name|AbstractModule
block|{
DECL|field|settings
specifier|private
specifier|final
name|Settings
name|settings
decl_stmt|;
DECL|field|queryParsers
specifier|private
specifier|final
name|ExtensionPoint
operator|.
name|ClassSet
argument_list|<
name|QueryParser
argument_list|>
name|queryParsers
init|=
operator|new
name|ExtensionPoint
operator|.
name|ClassSet
argument_list|<>
argument_list|(
literal|"query_parser"
argument_list|,
name|QueryParser
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|field|hunspellDictionaries
specifier|private
specifier|final
name|ExtensionPoint
operator|.
name|InstanceMap
argument_list|<
name|String
argument_list|,
name|Dictionary
argument_list|>
name|hunspellDictionaries
init|=
operator|new
name|ExtensionPoint
operator|.
name|InstanceMap
argument_list|<>
argument_list|(
literal|"hunspell_dictionary"
argument_list|,
name|String
operator|.
name|class
argument_list|,
name|Dictionary
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|method|IndicesModule
specifier|public
name|IndicesModule
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|this
operator|.
name|settings
operator|=
name|settings
expr_stmt|;
name|registerBuiltinQueryParsers
argument_list|()
expr_stmt|;
block|}
DECL|method|registerBuiltinQueryParsers
specifier|private
name|void
name|registerBuiltinQueryParsers
parameter_list|()
block|{
name|registerQueryParser
argument_list|(
name|MatchQueryParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerQueryParser
argument_list|(
name|MultiMatchQueryParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerQueryParser
argument_list|(
name|NestedQueryParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerQueryParser
argument_list|(
name|HasChildQueryParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerQueryParser
argument_list|(
name|HasParentQueryParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerQueryParser
argument_list|(
name|DisMaxQueryParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerQueryParser
argument_list|(
name|IdsQueryParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerQueryParser
argument_list|(
name|MatchAllQueryParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerQueryParser
argument_list|(
name|QueryStringQueryParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerQueryParser
argument_list|(
name|BoostingQueryParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerQueryParser
argument_list|(
name|BoolQueryParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerQueryParser
argument_list|(
name|TermQueryParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerQueryParser
argument_list|(
name|TermsQueryParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerQueryParser
argument_list|(
name|FuzzyQueryParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerQueryParser
argument_list|(
name|RegexpQueryParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerQueryParser
argument_list|(
name|RangeQueryParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerQueryParser
argument_list|(
name|PrefixQueryParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerQueryParser
argument_list|(
name|WildcardQueryParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerQueryParser
argument_list|(
name|ConstantScoreQueryParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerQueryParser
argument_list|(
name|SpanTermQueryParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerQueryParser
argument_list|(
name|SpanNotQueryParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerQueryParser
argument_list|(
name|SpanWithinQueryParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerQueryParser
argument_list|(
name|SpanContainingQueryParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerQueryParser
argument_list|(
name|FieldMaskingSpanQueryParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerQueryParser
argument_list|(
name|SpanFirstQueryParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerQueryParser
argument_list|(
name|SpanNearQueryParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerQueryParser
argument_list|(
name|SpanOrQueryParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerQueryParser
argument_list|(
name|MoreLikeThisQueryParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerQueryParser
argument_list|(
name|WrapperQueryParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerQueryParser
argument_list|(
name|IndicesQueryParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerQueryParser
argument_list|(
name|CommonTermsQueryParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerQueryParser
argument_list|(
name|SpanMultiTermQueryParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerQueryParser
argument_list|(
name|FunctionScoreQueryParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerQueryParser
argument_list|(
name|SimpleQueryStringParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerQueryParser
argument_list|(
name|TemplateQueryParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerQueryParser
argument_list|(
name|TypeQueryParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerQueryParser
argument_list|(
name|ScriptQueryParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerQueryParser
argument_list|(
name|GeoDistanceQueryParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerQueryParser
argument_list|(
name|GeoDistanceRangeQueryParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerQueryParser
argument_list|(
name|GeoBoundingBoxQueryParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerQueryParser
argument_list|(
name|GeohashCellQuery
operator|.
name|Parser
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerQueryParser
argument_list|(
name|GeoPolygonQueryParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerQueryParser
argument_list|(
name|QueryFilterParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerQueryParser
argument_list|(
name|NotQueryParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerQueryParser
argument_list|(
name|ExistsQueryParser
operator|.
name|class
argument_list|)
expr_stmt|;
name|registerQueryParser
argument_list|(
name|MissingQueryParser
operator|.
name|class
argument_list|)
expr_stmt|;
if|if
condition|(
name|ShapesAvailability
operator|.
name|JTS_AVAILABLE
condition|)
block|{
name|registerQueryParser
argument_list|(
name|GeoShapeQueryParser
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|registerQueryParser
specifier|public
name|void
name|registerQueryParser
parameter_list|(
name|Class
argument_list|<
name|?
extends|extends
name|QueryParser
argument_list|>
name|queryParser
parameter_list|)
block|{
name|queryParsers
operator|.
name|registerExtension
argument_list|(
name|queryParser
argument_list|)
expr_stmt|;
block|}
DECL|method|registerHunspellDictionary
specifier|public
name|void
name|registerHunspellDictionary
parameter_list|(
name|String
name|name
parameter_list|,
name|Dictionary
name|dictionary
parameter_list|)
block|{
name|hunspellDictionaries
operator|.
name|registerExtension
argument_list|(
name|name
argument_list|,
name|dictionary
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|configure
specifier|protected
name|void
name|configure
parameter_list|()
block|{
name|bindQueryParsersExtension
argument_list|()
expr_stmt|;
name|bindHunspellExtension
argument_list|()
expr_stmt|;
name|bind
argument_list|(
name|IndicesLifecycle
operator|.
name|class
argument_list|)
operator|.
name|to
argument_list|(
name|InternalIndicesLifecycle
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|bind
argument_list|(
name|IndicesService
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|bind
argument_list|(
name|RecoverySettings
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|bind
argument_list|(
name|RecoveryTarget
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|bind
argument_list|(
name|RecoverySource
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|bind
argument_list|(
name|IndicesStore
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|bind
argument_list|(
name|IndicesClusterStateService
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|bind
argument_list|(
name|IndexingMemoryController
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|bind
argument_list|(
name|SyncedFlushService
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|bind
argument_list|(
name|IndicesQueryCache
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|bind
argument_list|(
name|IndicesRequestCache
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|bind
argument_list|(
name|IndicesFieldDataCache
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|bind
argument_list|(
name|TransportNodesListShardStoreMetaData
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|bind
argument_list|(
name|IndicesTTLService
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|bind
argument_list|(
name|IndicesWarmer
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|bind
argument_list|(
name|UpdateHelper
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|bind
argument_list|(
name|MetaDataIndexUpgradeService
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|bind
argument_list|(
name|IndicesFieldDataCacheListener
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
block|}
DECL|method|bindQueryParsersExtension
specifier|protected
name|void
name|bindQueryParsersExtension
parameter_list|()
block|{
name|queryParsers
operator|.
name|bind
argument_list|(
name|binder
argument_list|()
argument_list|)
expr_stmt|;
name|bind
argument_list|(
name|IndicesQueriesRegistry
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
block|}
DECL|method|bindHunspellExtension
specifier|protected
name|void
name|bindHunspellExtension
parameter_list|()
block|{
name|hunspellDictionaries
operator|.
name|bind
argument_list|(
name|binder
argument_list|()
argument_list|)
expr_stmt|;
name|bind
argument_list|(
name|HunspellService
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|bind
argument_list|(
name|IndicesAnalysisService
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

