begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.metrics.tophits
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|metrics
operator|.
name|tophits
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|ScriptContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|SearchScript
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|Aggregator
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|AggregatorFactories
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|AggregatorFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|InternalAggregation
operator|.
name|Type
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|pipeline
operator|.
name|PipelineAggregator
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|support
operator|.
name|AggregationContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|builder
operator|.
name|SearchSourceBuilder
operator|.
name|ScriptField
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|fetch
operator|.
name|StoredFieldsContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|fetch
operator|.
name|subphase
operator|.
name|DocValueFieldsContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|fetch
operator|.
name|subphase
operator|.
name|FetchSourceContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|fetch
operator|.
name|subphase
operator|.
name|ScriptFieldsContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|fetch
operator|.
name|subphase
operator|.
name|highlight
operator|.
name|HighlightBuilder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|internal
operator|.
name|SubSearchContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|sort
operator|.
name|SortAndFormats
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|sort
operator|.
name|SortBuilder
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
name|Collections
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
name|Optional
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
DECL|class|TopHitsAggregatorFactory
specifier|public
class|class
name|TopHitsAggregatorFactory
extends|extends
name|AggregatorFactory
argument_list|<
name|TopHitsAggregatorFactory
argument_list|>
block|{
DECL|field|from
specifier|private
specifier|final
name|int
name|from
decl_stmt|;
DECL|field|size
specifier|private
specifier|final
name|int
name|size
decl_stmt|;
DECL|field|explain
specifier|private
specifier|final
name|boolean
name|explain
decl_stmt|;
DECL|field|version
specifier|private
specifier|final
name|boolean
name|version
decl_stmt|;
DECL|field|trackScores
specifier|private
specifier|final
name|boolean
name|trackScores
decl_stmt|;
DECL|field|sorts
specifier|private
specifier|final
name|List
argument_list|<
name|SortBuilder
argument_list|<
name|?
argument_list|>
argument_list|>
name|sorts
decl_stmt|;
DECL|field|highlightBuilder
specifier|private
specifier|final
name|HighlightBuilder
name|highlightBuilder
decl_stmt|;
DECL|field|storedFieldsContext
specifier|private
specifier|final
name|StoredFieldsContext
name|storedFieldsContext
decl_stmt|;
DECL|field|docValueFields
specifier|private
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|docValueFields
decl_stmt|;
DECL|field|scriptFields
specifier|private
specifier|final
name|List
argument_list|<
name|ScriptFieldsContext
operator|.
name|ScriptField
argument_list|>
name|scriptFields
decl_stmt|;
DECL|field|fetchSourceContext
specifier|private
specifier|final
name|FetchSourceContext
name|fetchSourceContext
decl_stmt|;
DECL|method|TopHitsAggregatorFactory
specifier|public
name|TopHitsAggregatorFactory
parameter_list|(
name|String
name|name
parameter_list|,
name|Type
name|type
parameter_list|,
name|int
name|from
parameter_list|,
name|int
name|size
parameter_list|,
name|boolean
name|explain
parameter_list|,
name|boolean
name|version
parameter_list|,
name|boolean
name|trackScores
parameter_list|,
name|List
argument_list|<
name|SortBuilder
argument_list|<
name|?
argument_list|>
argument_list|>
name|sorts
parameter_list|,
name|HighlightBuilder
name|highlightBuilder
parameter_list|,
name|StoredFieldsContext
name|storedFieldsContext
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|docValueFields
parameter_list|,
name|List
argument_list|<
name|ScriptFieldsContext
operator|.
name|ScriptField
argument_list|>
name|scriptFields
parameter_list|,
name|FetchSourceContext
name|fetchSourceContext
parameter_list|,
name|AggregationContext
name|context
parameter_list|,
name|AggregatorFactory
argument_list|<
name|?
argument_list|>
name|parent
parameter_list|,
name|AggregatorFactories
operator|.
name|Builder
name|subFactories
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|metaData
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|name
argument_list|,
name|type
argument_list|,
name|context
argument_list|,
name|parent
argument_list|,
name|subFactories
argument_list|,
name|metaData
argument_list|)
expr_stmt|;
name|this
operator|.
name|from
operator|=
name|from
expr_stmt|;
name|this
operator|.
name|size
operator|=
name|size
expr_stmt|;
name|this
operator|.
name|explain
operator|=
name|explain
expr_stmt|;
name|this
operator|.
name|version
operator|=
name|version
expr_stmt|;
name|this
operator|.
name|trackScores
operator|=
name|trackScores
expr_stmt|;
name|this
operator|.
name|sorts
operator|=
name|sorts
expr_stmt|;
name|this
operator|.
name|highlightBuilder
operator|=
name|highlightBuilder
expr_stmt|;
name|this
operator|.
name|storedFieldsContext
operator|=
name|storedFieldsContext
expr_stmt|;
name|this
operator|.
name|docValueFields
operator|=
name|docValueFields
expr_stmt|;
name|this
operator|.
name|scriptFields
operator|=
name|scriptFields
expr_stmt|;
name|this
operator|.
name|fetchSourceContext
operator|=
name|fetchSourceContext
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|createInternal
specifier|public
name|Aggregator
name|createInternal
parameter_list|(
name|Aggregator
name|parent
parameter_list|,
name|boolean
name|collectsFromSingleBucket
parameter_list|,
name|List
argument_list|<
name|PipelineAggregator
argument_list|>
name|pipelineAggregators
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|metaData
parameter_list|)
throws|throws
name|IOException
block|{
name|SubSearchContext
name|subSearchContext
init|=
operator|new
name|SubSearchContext
argument_list|(
name|context
operator|.
name|searchContext
argument_list|()
argument_list|)
decl_stmt|;
name|subSearchContext
operator|.
name|parsedQuery
argument_list|(
name|context
operator|.
name|searchContext
argument_list|()
operator|.
name|parsedQuery
argument_list|()
argument_list|)
expr_stmt|;
name|subSearchContext
operator|.
name|explain
argument_list|(
name|explain
argument_list|)
expr_stmt|;
name|subSearchContext
operator|.
name|version
argument_list|(
name|version
argument_list|)
expr_stmt|;
name|subSearchContext
operator|.
name|trackScores
argument_list|(
name|trackScores
argument_list|)
expr_stmt|;
name|subSearchContext
operator|.
name|from
argument_list|(
name|from
argument_list|)
expr_stmt|;
name|subSearchContext
operator|.
name|size
argument_list|(
name|size
argument_list|)
expr_stmt|;
if|if
condition|(
name|sorts
operator|!=
literal|null
condition|)
block|{
name|Optional
argument_list|<
name|SortAndFormats
argument_list|>
name|optionalSort
init|=
name|SortBuilder
operator|.
name|buildSort
argument_list|(
name|sorts
argument_list|,
name|subSearchContext
operator|.
name|getQueryShardContext
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|optionalSort
operator|.
name|isPresent
argument_list|()
condition|)
block|{
name|subSearchContext
operator|.
name|sort
argument_list|(
name|optionalSort
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|storedFieldsContext
operator|!=
literal|null
condition|)
block|{
name|subSearchContext
operator|.
name|storedFieldsContext
argument_list|(
name|storedFieldsContext
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|docValueFields
operator|!=
literal|null
condition|)
block|{
name|subSearchContext
operator|.
name|docValueFieldsContext
argument_list|(
operator|new
name|DocValueFieldsContext
argument_list|(
name|docValueFields
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|scriptFields
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|ScriptFieldsContext
operator|.
name|ScriptField
name|field
range|:
name|scriptFields
control|)
block|{
name|subSearchContext
operator|.
name|scriptFields
argument_list|()
operator|.
name|add
argument_list|(
name|field
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|fetchSourceContext
operator|!=
literal|null
condition|)
block|{
name|subSearchContext
operator|.
name|fetchSourceContext
argument_list|(
name|fetchSourceContext
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|highlightBuilder
operator|!=
literal|null
condition|)
block|{
name|subSearchContext
operator|.
name|highlight
argument_list|(
name|highlightBuilder
operator|.
name|build
argument_list|(
name|context
operator|.
name|searchContext
argument_list|()
operator|.
name|getQueryShardContext
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|TopHitsAggregator
argument_list|(
name|context
operator|.
name|searchContext
argument_list|()
operator|.
name|fetchPhase
argument_list|()
argument_list|,
name|subSearchContext
argument_list|,
name|name
argument_list|,
name|context
argument_list|,
name|parent
argument_list|,
name|pipelineAggregators
argument_list|,
name|metaData
argument_list|)
return|;
block|}
block|}
end_class

end_unit

