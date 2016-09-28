begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.bucket.significant
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|bucket
operator|.
name|significant
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|ParseField
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
name|xcontent
operator|.
name|ParseFieldRegistry
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
name|xcontent
operator|.
name|XContentParser
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
name|xcontent
operator|.
name|XContentParser
operator|.
name|Token
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
name|QueryBuilder
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
name|QueryParseContext
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
name|search
operator|.
name|aggregations
operator|.
name|Aggregator
operator|.
name|SubAggCollectionMode
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
name|bucket
operator|.
name|significant
operator|.
name|heuristics
operator|.
name|SignificanceHeuristic
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
name|bucket
operator|.
name|significant
operator|.
name|heuristics
operator|.
name|SignificanceHeuristicParser
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
name|bucket
operator|.
name|terms
operator|.
name|AbstractTermsParser
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
name|bucket
operator|.
name|terms
operator|.
name|TermsAggregator
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
name|bucket
operator|.
name|terms
operator|.
name|TermsAggregator
operator|.
name|BucketCountThresholds
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
name|bucket
operator|.
name|terms
operator|.
name|support
operator|.
name|IncludeExclude
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
name|XContentParseContext
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
name|ValueType
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
name|ValuesSourceType
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

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|SignificantTermsParser
specifier|public
class|class
name|SignificantTermsParser
extends|extends
name|AbstractTermsParser
block|{
DECL|field|significanceHeuristicParserRegistry
specifier|private
specifier|final
name|ParseFieldRegistry
argument_list|<
name|SignificanceHeuristicParser
argument_list|>
name|significanceHeuristicParserRegistry
decl_stmt|;
DECL|field|queriesRegistry
specifier|private
specifier|final
name|IndicesQueriesRegistry
name|queriesRegistry
decl_stmt|;
DECL|method|SignificantTermsParser
specifier|public
name|SignificantTermsParser
parameter_list|(
name|ParseFieldRegistry
argument_list|<
name|SignificanceHeuristicParser
argument_list|>
name|significanceHeuristicParserRegistry
parameter_list|,
name|IndicesQueriesRegistry
name|queriesRegistry
parameter_list|)
block|{
name|this
operator|.
name|significanceHeuristicParserRegistry
operator|=
name|significanceHeuristicParserRegistry
expr_stmt|;
name|this
operator|.
name|queriesRegistry
operator|=
name|queriesRegistry
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doCreateFactory
specifier|protected
name|SignificantTermsAggregationBuilder
name|doCreateFactory
parameter_list|(
name|String
name|aggregationName
parameter_list|,
name|ValuesSourceType
name|valuesSourceType
parameter_list|,
name|ValueType
name|targetValueType
parameter_list|,
name|BucketCountThresholds
name|bucketCountThresholds
parameter_list|,
name|SubAggCollectionMode
name|collectMode
parameter_list|,
name|String
name|executionHint
parameter_list|,
name|IncludeExclude
name|incExc
parameter_list|,
name|Map
argument_list|<
name|ParseField
argument_list|,
name|Object
argument_list|>
name|otherOptions
parameter_list|)
block|{
name|SignificantTermsAggregationBuilder
name|factory
init|=
operator|new
name|SignificantTermsAggregationBuilder
argument_list|(
name|aggregationName
argument_list|,
name|targetValueType
argument_list|)
decl_stmt|;
if|if
condition|(
name|bucketCountThresholds
operator|!=
literal|null
condition|)
block|{
name|factory
operator|.
name|bucketCountThresholds
argument_list|(
name|bucketCountThresholds
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|executionHint
operator|!=
literal|null
condition|)
block|{
name|factory
operator|.
name|executionHint
argument_list|(
name|executionHint
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|incExc
operator|!=
literal|null
condition|)
block|{
name|factory
operator|.
name|includeExclude
argument_list|(
name|incExc
argument_list|)
expr_stmt|;
block|}
name|QueryBuilder
name|backgroundFilter
init|=
operator|(
name|QueryBuilder
operator|)
name|otherOptions
operator|.
name|get
argument_list|(
name|SignificantTermsAggregationBuilder
operator|.
name|BACKGROUND_FILTER
argument_list|)
decl_stmt|;
if|if
condition|(
name|backgroundFilter
operator|!=
literal|null
condition|)
block|{
name|factory
operator|.
name|backgroundFilter
argument_list|(
name|backgroundFilter
argument_list|)
expr_stmt|;
block|}
name|SignificanceHeuristic
name|significanceHeuristic
init|=
operator|(
name|SignificanceHeuristic
operator|)
name|otherOptions
operator|.
name|get
argument_list|(
name|SignificantTermsAggregationBuilder
operator|.
name|HEURISTIC
argument_list|)
decl_stmt|;
if|if
condition|(
name|significanceHeuristic
operator|!=
literal|null
condition|)
block|{
name|factory
operator|.
name|significanceHeuristic
argument_list|(
name|significanceHeuristic
argument_list|)
expr_stmt|;
block|}
return|return
name|factory
return|;
block|}
annotation|@
name|Override
DECL|method|parseSpecial
specifier|public
name|boolean
name|parseSpecial
parameter_list|(
name|String
name|aggregationName
parameter_list|,
name|XContentParseContext
name|context
parameter_list|,
name|Token
name|token
parameter_list|,
name|String
name|currentFieldName
parameter_list|,
name|Map
argument_list|<
name|ParseField
argument_list|,
name|Object
argument_list|>
name|otherOptions
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|START_OBJECT
condition|)
block|{
name|SignificanceHeuristicParser
name|significanceHeuristicParser
init|=
name|significanceHeuristicParserRegistry
operator|.
name|lookupReturningNullIfNotFound
argument_list|(
name|currentFieldName
argument_list|,
name|context
operator|.
name|getParseFieldMatcher
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|significanceHeuristicParser
operator|!=
literal|null
condition|)
block|{
name|SignificanceHeuristic
name|significanceHeuristic
init|=
name|significanceHeuristicParser
operator|.
name|parse
argument_list|(
name|context
argument_list|)
decl_stmt|;
name|otherOptions
operator|.
name|put
argument_list|(
name|SignificantTermsAggregationBuilder
operator|.
name|HEURISTIC
argument_list|,
name|significanceHeuristic
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
elseif|else
if|if
condition|(
name|context
operator|.
name|matchField
argument_list|(
name|currentFieldName
argument_list|,
name|SignificantTermsAggregationBuilder
operator|.
name|BACKGROUND_FILTER
argument_list|)
condition|)
block|{
name|QueryParseContext
name|queryParseContext
init|=
operator|new
name|QueryParseContext
argument_list|(
name|context
operator|.
name|getDefaultScriptLanguage
argument_list|()
argument_list|,
name|queriesRegistry
argument_list|,
name|context
operator|.
name|getParser
argument_list|()
argument_list|,
name|context
operator|.
name|getParseFieldMatcher
argument_list|()
argument_list|)
decl_stmt|;
name|Optional
argument_list|<
name|QueryBuilder
argument_list|>
name|filter
init|=
name|queryParseContext
operator|.
name|parseInnerQueryBuilder
argument_list|()
decl_stmt|;
if|if
condition|(
name|filter
operator|.
name|isPresent
argument_list|()
condition|)
block|{
name|otherOptions
operator|.
name|put
argument_list|(
name|SignificantTermsAggregationBuilder
operator|.
name|BACKGROUND_FILTER
argument_list|,
name|filter
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
annotation|@
name|Override
DECL|method|getDefaultBucketCountThresholds
specifier|protected
name|BucketCountThresholds
name|getDefaultBucketCountThresholds
parameter_list|()
block|{
return|return
operator|new
name|TermsAggregator
operator|.
name|BucketCountThresholds
argument_list|(
name|SignificantTermsAggregationBuilder
operator|.
name|DEFAULT_BUCKET_COUNT_THRESHOLDS
argument_list|)
return|;
block|}
block|}
end_class

end_unit

