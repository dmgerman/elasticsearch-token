begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.bucket.terms
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
name|terms
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticSearchIllegalArgumentException
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
name|AggregationExecutionException
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
name|Aggregator
operator|.
name|BucketAggregationMode
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
name|aggregations
operator|.
name|support
operator|.
name|ValueSourceAggregatorFactory
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
name|ValuesSource
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
name|ValuesSourceConfig
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
name|bytes
operator|.
name|BytesValuesSource
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
name|numeric
operator|.
name|NumericValuesSource
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|TermsAggregatorFactory
specifier|public
class|class
name|TermsAggregatorFactory
extends|extends
name|ValueSourceAggregatorFactory
block|{
DECL|field|EXECUTION_HINT_VALUE_MAP
specifier|public
specifier|static
specifier|final
name|String
name|EXECUTION_HINT_VALUE_MAP
init|=
literal|"map"
decl_stmt|;
DECL|field|EXECUTION_HINT_VALUE_ORDINALS
specifier|public
specifier|static
specifier|final
name|String
name|EXECUTION_HINT_VALUE_ORDINALS
init|=
literal|"ordinals"
decl_stmt|;
DECL|field|order
specifier|private
specifier|final
name|InternalOrder
name|order
decl_stmt|;
DECL|field|requiredSize
specifier|private
specifier|final
name|int
name|requiredSize
decl_stmt|;
DECL|field|shardSize
specifier|private
specifier|final
name|int
name|shardSize
decl_stmt|;
DECL|field|includeExclude
specifier|private
specifier|final
name|IncludeExclude
name|includeExclude
decl_stmt|;
DECL|field|executionHint
specifier|private
specifier|final
name|String
name|executionHint
decl_stmt|;
DECL|method|TermsAggregatorFactory
specifier|public
name|TermsAggregatorFactory
parameter_list|(
name|String
name|name
parameter_list|,
name|ValuesSourceConfig
name|valueSourceConfig
parameter_list|,
name|InternalOrder
name|order
parameter_list|,
name|int
name|requiredSize
parameter_list|,
name|int
name|shardSize
parameter_list|,
name|IncludeExclude
name|includeExclude
parameter_list|,
name|String
name|executionHint
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|,
name|StringTerms
operator|.
name|TYPE
operator|.
name|name
argument_list|()
argument_list|,
name|valueSourceConfig
argument_list|)
expr_stmt|;
name|this
operator|.
name|order
operator|=
name|order
expr_stmt|;
name|this
operator|.
name|requiredSize
operator|=
name|requiredSize
expr_stmt|;
name|this
operator|.
name|shardSize
operator|=
name|shardSize
expr_stmt|;
name|this
operator|.
name|includeExclude
operator|=
name|includeExclude
expr_stmt|;
name|this
operator|.
name|executionHint
operator|=
name|executionHint
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|createUnmapped
specifier|protected
name|Aggregator
name|createUnmapped
parameter_list|(
name|AggregationContext
name|aggregationContext
parameter_list|,
name|Aggregator
name|parent
parameter_list|)
block|{
return|return
operator|new
name|UnmappedTermsAggregator
argument_list|(
name|name
argument_list|,
name|order
argument_list|,
name|requiredSize
argument_list|,
name|aggregationContext
argument_list|,
name|parent
argument_list|)
return|;
block|}
DECL|method|hasParentBucketAggregator
specifier|private
specifier|static
name|boolean
name|hasParentBucketAggregator
parameter_list|(
name|Aggregator
name|parent
parameter_list|)
block|{
if|if
condition|(
name|parent
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
elseif|else
if|if
condition|(
name|parent
operator|.
name|bucketAggregationMode
argument_list|()
operator|==
name|BucketAggregationMode
operator|.
name|PER_BUCKET
condition|)
block|{
return|return
literal|true
return|;
block|}
else|else
block|{
return|return
name|hasParentBucketAggregator
argument_list|(
name|parent
operator|.
name|parent
argument_list|()
argument_list|)
return|;
block|}
block|}
annotation|@
name|Override
DECL|method|create
specifier|protected
name|Aggregator
name|create
parameter_list|(
name|ValuesSource
name|valuesSource
parameter_list|,
name|long
name|expectedBucketsCount
parameter_list|,
name|AggregationContext
name|aggregationContext
parameter_list|,
name|Aggregator
name|parent
parameter_list|)
block|{
name|long
name|estimatedBucketCount
init|=
name|valuesSource
operator|.
name|metaData
argument_list|()
operator|.
name|maxAtomicUniqueValuesCount
argument_list|()
decl_stmt|;
if|if
condition|(
name|estimatedBucketCount
operator|<
literal|0
condition|)
block|{
comment|// there isn't an estimation available.. 50 should be a good start
name|estimatedBucketCount
operator|=
literal|50
expr_stmt|;
block|}
comment|// adding an upper bound on the estimation as some atomic field data in the future (binary doc values) and not
comment|// going to know their exact cardinality and will return upper bounds in AtomicFieldData.getNumberUniqueValues()
comment|// that may be largely over-estimated.. the value chosen here is arbitrary just to play nice with typical CPU cache
comment|//
comment|// Another reason is that it may be faster to resize upon growth than to start directly with the appropriate size.
comment|// And that all values are not necessarily visited by the matches.
name|estimatedBucketCount
operator|=
name|Math
operator|.
name|min
argument_list|(
name|estimatedBucketCount
argument_list|,
literal|512
argument_list|)
expr_stmt|;
if|if
condition|(
name|valuesSource
operator|instanceof
name|BytesValuesSource
condition|)
block|{
if|if
condition|(
name|executionHint
operator|!=
literal|null
operator|&&
operator|!
name|executionHint
operator|.
name|equals
argument_list|(
name|EXECUTION_HINT_VALUE_MAP
argument_list|)
operator|&&
operator|!
name|executionHint
operator|.
name|equals
argument_list|(
name|EXECUTION_HINT_VALUE_ORDINALS
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|ElasticSearchIllegalArgumentException
argument_list|(
literal|"execution_hint can only be '"
operator|+
name|EXECUTION_HINT_VALUE_MAP
operator|+
literal|"' or '"
operator|+
name|EXECUTION_HINT_VALUE_ORDINALS
operator|+
literal|"', not "
operator|+
name|executionHint
argument_list|)
throw|;
block|}
name|String
name|execution
init|=
name|executionHint
decl_stmt|;
if|if
condition|(
operator|!
operator|(
name|valuesSource
operator|instanceof
name|BytesValuesSource
operator|.
name|WithOrdinals
operator|)
condition|)
block|{
name|execution
operator|=
name|EXECUTION_HINT_VALUE_MAP
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|includeExclude
operator|!=
literal|null
condition|)
block|{
name|execution
operator|=
name|EXECUTION_HINT_VALUE_MAP
expr_stmt|;
block|}
if|if
condition|(
name|execution
operator|==
literal|null
condition|)
block|{
if|if
condition|(
operator|(
name|valuesSource
operator|instanceof
name|BytesValuesSource
operator|.
name|WithOrdinals
operator|)
operator|&&
operator|!
name|hasParentBucketAggregator
argument_list|(
name|parent
argument_list|)
condition|)
block|{
name|execution
operator|=
name|EXECUTION_HINT_VALUE_ORDINALS
expr_stmt|;
block|}
else|else
block|{
name|execution
operator|=
name|EXECUTION_HINT_VALUE_MAP
expr_stmt|;
block|}
block|}
assert|assert
name|execution
operator|!=
literal|null
assert|;
if|if
condition|(
name|execution
operator|.
name|equals
argument_list|(
name|EXECUTION_HINT_VALUE_ORDINALS
argument_list|)
condition|)
block|{
assert|assert
name|includeExclude
operator|==
literal|null
assert|;
specifier|final
name|StringTermsAggregator
operator|.
name|WithOrdinals
name|aggregator
init|=
operator|new
name|StringTermsAggregator
operator|.
name|WithOrdinals
argument_list|(
name|name
argument_list|,
name|factories
argument_list|,
operator|(
name|BytesValuesSource
operator|.
name|WithOrdinals
operator|)
name|valuesSource
argument_list|,
name|estimatedBucketCount
argument_list|,
name|order
argument_list|,
name|requiredSize
argument_list|,
name|shardSize
argument_list|,
name|aggregationContext
argument_list|,
name|parent
argument_list|)
decl_stmt|;
name|aggregationContext
operator|.
name|registerReaderContextAware
argument_list|(
name|aggregator
argument_list|)
expr_stmt|;
return|return
name|aggregator
return|;
block|}
else|else
block|{
return|return
operator|new
name|StringTermsAggregator
argument_list|(
name|name
argument_list|,
name|factories
argument_list|,
name|valuesSource
argument_list|,
name|estimatedBucketCount
argument_list|,
name|order
argument_list|,
name|requiredSize
argument_list|,
name|shardSize
argument_list|,
name|includeExclude
argument_list|,
name|aggregationContext
argument_list|,
name|parent
argument_list|)
return|;
block|}
block|}
if|if
condition|(
name|includeExclude
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|AggregationExecutionException
argument_list|(
literal|"Aggregation ["
operator|+
name|name
operator|+
literal|"] cannot support the include/exclude "
operator|+
literal|"settings as it can only be applied to string values"
argument_list|)
throw|;
block|}
if|if
condition|(
name|valuesSource
operator|instanceof
name|NumericValuesSource
condition|)
block|{
if|if
condition|(
operator|(
operator|(
name|NumericValuesSource
operator|)
name|valuesSource
operator|)
operator|.
name|isFloatingPoint
argument_list|()
condition|)
block|{
return|return
operator|new
name|DoubleTermsAggregator
argument_list|(
name|name
argument_list|,
name|factories
argument_list|,
operator|(
name|NumericValuesSource
operator|)
name|valuesSource
argument_list|,
name|estimatedBucketCount
argument_list|,
name|order
argument_list|,
name|requiredSize
argument_list|,
name|shardSize
argument_list|,
name|aggregationContext
argument_list|,
name|parent
argument_list|)
return|;
block|}
return|return
operator|new
name|LongTermsAggregator
argument_list|(
name|name
argument_list|,
name|factories
argument_list|,
operator|(
name|NumericValuesSource
operator|)
name|valuesSource
argument_list|,
name|estimatedBucketCount
argument_list|,
name|order
argument_list|,
name|requiredSize
argument_list|,
name|shardSize
argument_list|,
name|aggregationContext
argument_list|,
name|parent
argument_list|)
return|;
block|}
throw|throw
operator|new
name|AggregationExecutionException
argument_list|(
literal|"terms aggregation cannot be applied to field ["
operator|+
name|valuesSourceConfig
operator|.
name|fieldContext
argument_list|()
operator|.
name|field
argument_list|()
operator|+
literal|"]. It can only be applied to numeric or string fields."
argument_list|)
throw|;
block|}
block|}
end_class

end_unit

