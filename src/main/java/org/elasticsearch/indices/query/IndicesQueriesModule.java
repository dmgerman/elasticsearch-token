begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.indices.query
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|query
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
name|inject
operator|.
name|multibindings
operator|.
name|Multibinder
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
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_class
DECL|class|IndicesQueriesModule
specifier|public
class|class
name|IndicesQueriesModule
extends|extends
name|AbstractModule
block|{
DECL|field|queryParsersClasses
specifier|private
name|Set
argument_list|<
name|Class
argument_list|<
name|QueryParser
argument_list|>
argument_list|>
name|queryParsersClasses
init|=
name|Sets
operator|.
name|newHashSet
argument_list|()
decl_stmt|;
DECL|field|queryParsers
specifier|private
name|Set
argument_list|<
name|QueryParser
argument_list|>
name|queryParsers
init|=
name|Sets
operator|.
name|newHashSet
argument_list|()
decl_stmt|;
DECL|method|addQuery
specifier|public
specifier|synchronized
name|IndicesQueriesModule
name|addQuery
parameter_list|(
name|Class
argument_list|<
name|QueryParser
argument_list|>
name|queryParser
parameter_list|)
block|{
name|queryParsersClasses
operator|.
name|add
argument_list|(
name|queryParser
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|addQuery
specifier|public
specifier|synchronized
name|IndicesQueriesModule
name|addQuery
parameter_list|(
name|QueryParser
name|queryParser
parameter_list|)
block|{
name|queryParsers
operator|.
name|add
argument_list|(
name|queryParser
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
DECL|method|configure
specifier|protected
name|void
name|configure
parameter_list|()
block|{
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
name|Multibinder
argument_list|<
name|QueryParser
argument_list|>
name|qpBinders
init|=
name|Multibinder
operator|.
name|newSetBinder
argument_list|(
name|binder
argument_list|()
argument_list|,
name|QueryParser
operator|.
name|class
argument_list|)
decl_stmt|;
for|for
control|(
name|Class
argument_list|<
name|QueryParser
argument_list|>
name|queryParser
range|:
name|queryParsersClasses
control|)
block|{
name|qpBinders
operator|.
name|addBinding
argument_list|()
operator|.
name|to
argument_list|(
name|queryParser
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
block|}
for|for
control|(
name|QueryParser
name|queryParser
range|:
name|queryParsers
control|)
block|{
name|qpBinders
operator|.
name|addBinding
argument_list|()
operator|.
name|toInstance
argument_list|(
name|queryParser
argument_list|)
expr_stmt|;
block|}
name|qpBinders
operator|.
name|addBinding
argument_list|()
operator|.
name|to
argument_list|(
name|MatchQueryParser
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|qpBinders
operator|.
name|addBinding
argument_list|()
operator|.
name|to
argument_list|(
name|MultiMatchQueryParser
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|qpBinders
operator|.
name|addBinding
argument_list|()
operator|.
name|to
argument_list|(
name|NestedQueryParser
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|qpBinders
operator|.
name|addBinding
argument_list|()
operator|.
name|to
argument_list|(
name|HasChildQueryParser
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|qpBinders
operator|.
name|addBinding
argument_list|()
operator|.
name|to
argument_list|(
name|HasParentQueryParser
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|qpBinders
operator|.
name|addBinding
argument_list|()
operator|.
name|to
argument_list|(
name|DisMaxQueryParser
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|qpBinders
operator|.
name|addBinding
argument_list|()
operator|.
name|to
argument_list|(
name|IdsQueryParser
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|qpBinders
operator|.
name|addBinding
argument_list|()
operator|.
name|to
argument_list|(
name|MatchAllQueryParser
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|qpBinders
operator|.
name|addBinding
argument_list|()
operator|.
name|to
argument_list|(
name|QueryStringQueryParser
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|qpBinders
operator|.
name|addBinding
argument_list|()
operator|.
name|to
argument_list|(
name|BoostingQueryParser
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|qpBinders
operator|.
name|addBinding
argument_list|()
operator|.
name|to
argument_list|(
name|BoolQueryParser
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|qpBinders
operator|.
name|addBinding
argument_list|()
operator|.
name|to
argument_list|(
name|TermQueryParser
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|qpBinders
operator|.
name|addBinding
argument_list|()
operator|.
name|to
argument_list|(
name|TermsQueryParser
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|qpBinders
operator|.
name|addBinding
argument_list|()
operator|.
name|to
argument_list|(
name|FuzzyQueryParser
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|qpBinders
operator|.
name|addBinding
argument_list|()
operator|.
name|to
argument_list|(
name|RegexpQueryParser
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|qpBinders
operator|.
name|addBinding
argument_list|()
operator|.
name|to
argument_list|(
name|RangeQueryParser
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|qpBinders
operator|.
name|addBinding
argument_list|()
operator|.
name|to
argument_list|(
name|PrefixQueryParser
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|qpBinders
operator|.
name|addBinding
argument_list|()
operator|.
name|to
argument_list|(
name|WildcardQueryParser
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|qpBinders
operator|.
name|addBinding
argument_list|()
operator|.
name|to
argument_list|(
name|FilteredQueryParser
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|qpBinders
operator|.
name|addBinding
argument_list|()
operator|.
name|to
argument_list|(
name|ConstantScoreQueryParser
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|qpBinders
operator|.
name|addBinding
argument_list|()
operator|.
name|to
argument_list|(
name|SpanTermQueryParser
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|qpBinders
operator|.
name|addBinding
argument_list|()
operator|.
name|to
argument_list|(
name|SpanNotQueryParser
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|qpBinders
operator|.
name|addBinding
argument_list|()
operator|.
name|to
argument_list|(
name|SpanWithinQueryParser
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|qpBinders
operator|.
name|addBinding
argument_list|()
operator|.
name|to
argument_list|(
name|SpanContainingQueryParser
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|qpBinders
operator|.
name|addBinding
argument_list|()
operator|.
name|to
argument_list|(
name|FieldMaskingSpanQueryParser
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|qpBinders
operator|.
name|addBinding
argument_list|()
operator|.
name|to
argument_list|(
name|SpanFirstQueryParser
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|qpBinders
operator|.
name|addBinding
argument_list|()
operator|.
name|to
argument_list|(
name|SpanNearQueryParser
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|qpBinders
operator|.
name|addBinding
argument_list|()
operator|.
name|to
argument_list|(
name|SpanOrQueryParser
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|qpBinders
operator|.
name|addBinding
argument_list|()
operator|.
name|to
argument_list|(
name|MoreLikeThisQueryParser
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|qpBinders
operator|.
name|addBinding
argument_list|()
operator|.
name|to
argument_list|(
name|WrapperQueryParser
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|qpBinders
operator|.
name|addBinding
argument_list|()
operator|.
name|to
argument_list|(
name|IndicesQueryParser
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|qpBinders
operator|.
name|addBinding
argument_list|()
operator|.
name|to
argument_list|(
name|CommonTermsQueryParser
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|qpBinders
operator|.
name|addBinding
argument_list|()
operator|.
name|to
argument_list|(
name|SpanMultiTermQueryParser
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|qpBinders
operator|.
name|addBinding
argument_list|()
operator|.
name|to
argument_list|(
name|FunctionScoreQueryParser
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|qpBinders
operator|.
name|addBinding
argument_list|()
operator|.
name|to
argument_list|(
name|SimpleQueryStringParser
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|qpBinders
operator|.
name|addBinding
argument_list|()
operator|.
name|to
argument_list|(
name|TemplateQueryParser
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|qpBinders
operator|.
name|addBinding
argument_list|()
operator|.
name|to
argument_list|(
name|TypeQueryParser
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|qpBinders
operator|.
name|addBinding
argument_list|()
operator|.
name|to
argument_list|(
name|LimitQueryParser
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|qpBinders
operator|.
name|addBinding
argument_list|()
operator|.
name|to
argument_list|(
name|TermsQueryParser
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|qpBinders
operator|.
name|addBinding
argument_list|()
operator|.
name|to
argument_list|(
name|ScriptQueryParser
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|qpBinders
operator|.
name|addBinding
argument_list|()
operator|.
name|to
argument_list|(
name|GeoDistanceQueryParser
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|qpBinders
operator|.
name|addBinding
argument_list|()
operator|.
name|to
argument_list|(
name|GeoDistanceRangeQueryParser
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|qpBinders
operator|.
name|addBinding
argument_list|()
operator|.
name|to
argument_list|(
name|GeoBoundingBoxQueryParser
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|qpBinders
operator|.
name|addBinding
argument_list|()
operator|.
name|to
argument_list|(
name|GeohashCellQuery
operator|.
name|Parser
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|qpBinders
operator|.
name|addBinding
argument_list|()
operator|.
name|to
argument_list|(
name|GeoPolygonQueryParser
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|qpBinders
operator|.
name|addBinding
argument_list|()
operator|.
name|to
argument_list|(
name|QueryFilterParser
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|qpBinders
operator|.
name|addBinding
argument_list|()
operator|.
name|to
argument_list|(
name|FQueryFilterParser
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|qpBinders
operator|.
name|addBinding
argument_list|()
operator|.
name|to
argument_list|(
name|AndQueryParser
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|qpBinders
operator|.
name|addBinding
argument_list|()
operator|.
name|to
argument_list|(
name|OrQueryParser
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|qpBinders
operator|.
name|addBinding
argument_list|()
operator|.
name|to
argument_list|(
name|NotQueryParser
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|qpBinders
operator|.
name|addBinding
argument_list|()
operator|.
name|to
argument_list|(
name|ExistsQueryParser
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
name|qpBinders
operator|.
name|addBinding
argument_list|()
operator|.
name|to
argument_list|(
name|MissingQueryParser
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
if|if
condition|(
name|ShapesAvailability
operator|.
name|JTS_AVAILABLE
condition|)
block|{
name|qpBinders
operator|.
name|addBinding
argument_list|()
operator|.
name|to
argument_list|(
name|GeoShapeQueryParser
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

