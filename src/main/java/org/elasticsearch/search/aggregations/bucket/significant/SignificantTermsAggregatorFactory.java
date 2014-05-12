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
name|apache
operator|.
name|lucene
operator|.
name|index
operator|.
name|DocsEnum
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
name|Filter
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
name|util
operator|.
name|BytesRef
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchIllegalArgumentException
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
name|lease
operator|.
name|Releasable
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
name|lucene
operator|.
name|index
operator|.
name|FilterableTermsEnum
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
name|lucene
operator|.
name|index
operator|.
name|FreqTermsEnum
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
name|FieldMapper
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
name|*
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
name|TermsAggregatorFactory
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
name|ValuesSourceAggregatorFactory
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
name|internal
operator|.
name|SearchContext
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

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|SignificantTermsAggregatorFactory
specifier|public
class|class
name|SignificantTermsAggregatorFactory
extends|extends
name|ValuesSourceAggregatorFactory
implements|implements
name|Releasable
block|{
DECL|enum|ExecutionMode
specifier|public
enum|enum
name|ExecutionMode
block|{
DECL|method|MAP
DECL|method|MAP
name|MAP
argument_list|(
operator|new
name|ParseField
argument_list|(
literal|"map"
argument_list|)
argument_list|)
block|{
annotation|@
name|Override
name|Aggregator
name|create
parameter_list|(
name|String
name|name
parameter_list|,
name|AggregatorFactories
name|factories
parameter_list|,
name|ValuesSource
name|valuesSource
parameter_list|,
name|long
name|estimatedBucketCount
parameter_list|,
name|int
name|requiredSize
parameter_list|,
name|int
name|shardSize
parameter_list|,
name|long
name|minDocCount
parameter_list|,
name|long
name|shardMinDocCount
parameter_list|,
name|IncludeExclude
name|includeExclude
parameter_list|,
name|AggregationContext
name|aggregationContext
parameter_list|,
name|Aggregator
name|parent
parameter_list|,
name|SignificantTermsAggregatorFactory
name|termsAggregatorFactory
parameter_list|)
block|{
return|return
operator|new
name|SignificantStringTermsAggregator
argument_list|(
name|name
argument_list|,
name|factories
argument_list|,
name|valuesSource
argument_list|,
name|estimatedBucketCount
argument_list|,
name|requiredSize
argument_list|,
name|shardSize
argument_list|,
name|minDocCount
argument_list|,
name|shardMinDocCount
argument_list|,
name|includeExclude
argument_list|,
name|aggregationContext
argument_list|,
name|parent
argument_list|,
name|termsAggregatorFactory
argument_list|)
return|;
block|}
annotation|@
name|Override
name|boolean
name|needsGlobalOrdinals
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
block|}
block|,
DECL|method|ORDINALS
DECL|method|ORDINALS
name|ORDINALS
argument_list|(
operator|new
name|ParseField
argument_list|(
literal|"ordinals"
argument_list|)
argument_list|)
block|{
annotation|@
name|Override
name|Aggregator
name|create
parameter_list|(
name|String
name|name
parameter_list|,
name|AggregatorFactories
name|factories
parameter_list|,
name|ValuesSource
name|valuesSource
parameter_list|,
name|long
name|estimatedBucketCount
parameter_list|,
name|int
name|requiredSize
parameter_list|,
name|int
name|shardSize
parameter_list|,
name|long
name|minDocCount
parameter_list|,
name|long
name|shardMinDocCount
parameter_list|,
name|IncludeExclude
name|includeExclude
parameter_list|,
name|AggregationContext
name|aggregationContext
parameter_list|,
name|Aggregator
name|parent
parameter_list|,
name|SignificantTermsAggregatorFactory
name|termsAggregatorFactory
parameter_list|)
block|{
if|if
condition|(
name|includeExclude
operator|!=
literal|null
condition|)
block|{
return|return
name|MAP
operator|.
name|create
argument_list|(
name|name
argument_list|,
name|factories
argument_list|,
name|valuesSource
argument_list|,
name|estimatedBucketCount
argument_list|,
name|requiredSize
argument_list|,
name|shardSize
argument_list|,
name|minDocCount
argument_list|,
name|shardMinDocCount
argument_list|,
name|includeExclude
argument_list|,
name|aggregationContext
argument_list|,
name|parent
argument_list|,
name|termsAggregatorFactory
argument_list|)
return|;
block|}
return|return
operator|new
name|SignificantStringTermsAggregator
operator|.
name|WithOrdinals
argument_list|(
name|name
argument_list|,
name|factories
argument_list|,
operator|(
name|ValuesSource
operator|.
name|Bytes
operator|.
name|WithOrdinals
operator|)
name|valuesSource
argument_list|,
name|estimatedBucketCount
argument_list|,
name|requiredSize
argument_list|,
name|shardSize
argument_list|,
name|minDocCount
argument_list|,
name|shardMinDocCount
argument_list|,
name|aggregationContext
argument_list|,
name|parent
argument_list|,
name|termsAggregatorFactory
argument_list|)
return|;
block|}
annotation|@
name|Override
name|boolean
name|needsGlobalOrdinals
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
block|}
block|,
DECL|method|GLOBAL_ORDINALS
DECL|method|GLOBAL_ORDINALS
name|GLOBAL_ORDINALS
argument_list|(
operator|new
name|ParseField
argument_list|(
literal|"global_ordinals"
argument_list|)
argument_list|)
block|{
annotation|@
name|Override
name|Aggregator
name|create
parameter_list|(
name|String
name|name
parameter_list|,
name|AggregatorFactories
name|factories
parameter_list|,
name|ValuesSource
name|valuesSource
parameter_list|,
name|long
name|estimatedBucketCount
parameter_list|,
name|int
name|requiredSize
parameter_list|,
name|int
name|shardSize
parameter_list|,
name|long
name|minDocCount
parameter_list|,
name|long
name|shardMinDocCount
parameter_list|,
name|IncludeExclude
name|includeExclude
parameter_list|,
name|AggregationContext
name|aggregationContext
parameter_list|,
name|Aggregator
name|parent
parameter_list|,
name|SignificantTermsAggregatorFactory
name|termsAggregatorFactory
parameter_list|)
block|{
name|ValuesSource
operator|.
name|Bytes
operator|.
name|WithOrdinals
name|valueSourceWithOrdinals
init|=
operator|(
name|ValuesSource
operator|.
name|Bytes
operator|.
name|WithOrdinals
operator|)
name|valuesSource
decl_stmt|;
name|IndexSearcher
name|indexSearcher
init|=
name|aggregationContext
operator|.
name|searchContext
argument_list|()
operator|.
name|searcher
argument_list|()
decl_stmt|;
name|long
name|maxOrd
init|=
name|valueSourceWithOrdinals
operator|.
name|globalMaxOrd
argument_list|(
name|indexSearcher
argument_list|)
decl_stmt|;
return|return
operator|new
name|GlobalOrdinalsSignificantTermsAggregator
argument_list|(
name|name
argument_list|,
name|factories
argument_list|,
operator|(
name|ValuesSource
operator|.
name|Bytes
operator|.
name|WithOrdinals
operator|.
name|FieldData
operator|)
name|valuesSource
argument_list|,
name|estimatedBucketCount
argument_list|,
name|maxOrd
argument_list|,
name|requiredSize
argument_list|,
name|shardSize
argument_list|,
name|minDocCount
argument_list|,
name|shardMinDocCount
argument_list|,
name|includeExclude
argument_list|,
name|aggregationContext
argument_list|,
name|parent
argument_list|,
name|termsAggregatorFactory
argument_list|)
return|;
block|}
annotation|@
name|Override
name|boolean
name|needsGlobalOrdinals
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
block|}
block|,
DECL|method|GLOBAL_ORDINALS_HASH
DECL|method|GLOBAL_ORDINALS_HASH
name|GLOBAL_ORDINALS_HASH
argument_list|(
operator|new
name|ParseField
argument_list|(
literal|"global_ordinals_hash"
argument_list|)
argument_list|)
block|{
annotation|@
name|Override
name|Aggregator
name|create
parameter_list|(
name|String
name|name
parameter_list|,
name|AggregatorFactories
name|factories
parameter_list|,
name|ValuesSource
name|valuesSource
parameter_list|,
name|long
name|estimatedBucketCount
parameter_list|,
name|int
name|requiredSize
parameter_list|,
name|int
name|shardSize
parameter_list|,
name|long
name|minDocCount
parameter_list|,
name|long
name|shardMinDocCount
parameter_list|,
name|IncludeExclude
name|includeExclude
parameter_list|,
name|AggregationContext
name|aggregationContext
parameter_list|,
name|Aggregator
name|parent
parameter_list|,
name|SignificantTermsAggregatorFactory
name|termsAggregatorFactory
parameter_list|)
block|{
return|return
operator|new
name|GlobalOrdinalsSignificantTermsAggregator
operator|.
name|WithHash
argument_list|(
name|name
argument_list|,
name|factories
argument_list|,
operator|(
name|ValuesSource
operator|.
name|Bytes
operator|.
name|WithOrdinals
operator|.
name|FieldData
operator|)
name|valuesSource
argument_list|,
name|estimatedBucketCount
argument_list|,
name|requiredSize
argument_list|,
name|shardSize
argument_list|,
name|minDocCount
argument_list|,
name|shardMinDocCount
argument_list|,
name|includeExclude
argument_list|,
name|aggregationContext
argument_list|,
name|parent
argument_list|,
name|termsAggregatorFactory
argument_list|)
return|;
block|}
annotation|@
name|Override
name|boolean
name|needsGlobalOrdinals
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
block|}
block|;
DECL|method|fromString
specifier|public
specifier|static
name|ExecutionMode
name|fromString
parameter_list|(
name|String
name|value
parameter_list|)
block|{
for|for
control|(
name|ExecutionMode
name|mode
range|:
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|mode
operator|.
name|parseField
operator|.
name|match
argument_list|(
name|value
argument_list|)
condition|)
block|{
return|return
name|mode
return|;
block|}
block|}
throw|throw
operator|new
name|ElasticsearchIllegalArgumentException
argument_list|(
literal|"Unknown `execution_hint`: ["
operator|+
name|value
operator|+
literal|"], expected any of "
operator|+
name|values
argument_list|()
argument_list|)
throw|;
block|}
DECL|field|parseField
specifier|private
specifier|final
name|ParseField
name|parseField
decl_stmt|;
DECL|method|ExecutionMode
name|ExecutionMode
parameter_list|(
name|ParseField
name|parseField
parameter_list|)
block|{
name|this
operator|.
name|parseField
operator|=
name|parseField
expr_stmt|;
block|}
DECL|method|create
specifier|abstract
name|Aggregator
name|create
parameter_list|(
name|String
name|name
parameter_list|,
name|AggregatorFactories
name|factories
parameter_list|,
name|ValuesSource
name|valuesSource
parameter_list|,
name|long
name|estimatedBucketCount
parameter_list|,
name|int
name|requiredSize
parameter_list|,
name|int
name|shardSize
parameter_list|,
name|long
name|minDocCount
parameter_list|,
name|long
name|shardMinDocCount
parameter_list|,
name|IncludeExclude
name|includeExclude
parameter_list|,
name|AggregationContext
name|aggregationContext
parameter_list|,
name|Aggregator
name|parent
parameter_list|,
name|SignificantTermsAggregatorFactory
name|termsAggregatorFactory
parameter_list|)
function_decl|;
DECL|method|needsGlobalOrdinals
specifier|abstract
name|boolean
name|needsGlobalOrdinals
parameter_list|()
function_decl|;
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|parseField
operator|.
name|getPreferredName
argument_list|()
return|;
block|}
block|}
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
DECL|field|minDocCount
specifier|private
specifier|final
name|long
name|minDocCount
decl_stmt|;
DECL|field|shardMinDocCount
specifier|private
specifier|final
name|long
name|shardMinDocCount
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
DECL|field|indexedFieldName
specifier|private
name|String
name|indexedFieldName
decl_stmt|;
DECL|field|mapper
specifier|private
name|FieldMapper
name|mapper
decl_stmt|;
DECL|field|termsEnum
specifier|private
name|FilterableTermsEnum
name|termsEnum
decl_stmt|;
DECL|field|numberOfAggregatorsCreated
specifier|private
name|int
name|numberOfAggregatorsCreated
init|=
literal|0
decl_stmt|;
DECL|field|filter
specifier|private
name|Filter
name|filter
decl_stmt|;
DECL|method|SignificantTermsAggregatorFactory
specifier|public
name|SignificantTermsAggregatorFactory
parameter_list|(
name|String
name|name
parameter_list|,
name|ValuesSourceConfig
name|valueSourceConfig
parameter_list|,
name|int
name|requiredSize
parameter_list|,
name|int
name|shardSize
parameter_list|,
name|long
name|minDocCount
parameter_list|,
name|long
name|shardMinDocCount
parameter_list|,
name|IncludeExclude
name|includeExclude
parameter_list|,
name|String
name|executionHint
parameter_list|,
name|Filter
name|filter
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|,
name|SignificantStringTerms
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
name|minDocCount
operator|=
name|minDocCount
expr_stmt|;
name|this
operator|.
name|shardMinDocCount
operator|=
name|shardMinDocCount
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
if|if
condition|(
operator|!
name|valueSourceConfig
operator|.
name|unmapped
argument_list|()
condition|)
block|{
name|this
operator|.
name|indexedFieldName
operator|=
name|config
operator|.
name|fieldContext
argument_list|()
operator|.
name|field
argument_list|()
expr_stmt|;
name|mapper
operator|=
name|SearchContext
operator|.
name|current
argument_list|()
operator|.
name|smartNameFieldMapper
argument_list|(
name|indexedFieldName
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|filter
operator|=
name|filter
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
specifier|final
name|InternalAggregation
name|aggregation
init|=
operator|new
name|UnmappedSignificantTerms
argument_list|(
name|name
argument_list|,
name|requiredSize
argument_list|,
name|minDocCount
argument_list|)
decl_stmt|;
return|return
operator|new
name|NonCollectingAggregator
argument_list|(
name|name
argument_list|,
name|aggregationContext
argument_list|,
name|parent
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|InternalAggregation
name|buildEmptyAggregation
parameter_list|()
block|{
return|return
name|aggregation
return|;
block|}
block|}
return|;
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
name|numberOfAggregatorsCreated
operator|++
expr_stmt|;
name|long
name|estimatedBucketCount
init|=
name|TermsAggregatorFactory
operator|.
name|estimatedBucketCount
argument_list|(
name|valuesSource
argument_list|,
name|parent
argument_list|)
decl_stmt|;
if|if
condition|(
name|valuesSource
operator|instanceof
name|ValuesSource
operator|.
name|Bytes
condition|)
block|{
name|ExecutionMode
name|execution
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|executionHint
operator|!=
literal|null
condition|)
block|{
name|execution
operator|=
name|ExecutionMode
operator|.
name|fromString
argument_list|(
name|executionHint
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
operator|(
name|valuesSource
operator|instanceof
name|ValuesSource
operator|.
name|Bytes
operator|.
name|WithOrdinals
operator|)
condition|)
block|{
name|execution
operator|=
name|ExecutionMode
operator|.
name|MAP
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
name|Aggregator
operator|.
name|hasParentBucketAggregator
argument_list|(
name|parent
argument_list|)
condition|)
block|{
name|execution
operator|=
name|ExecutionMode
operator|.
name|GLOBAL_ORDINALS_HASH
expr_stmt|;
block|}
else|else
block|{
name|execution
operator|=
name|ExecutionMode
operator|.
name|GLOBAL_ORDINALS
expr_stmt|;
block|}
block|}
assert|assert
name|execution
operator|!=
literal|null
assert|;
name|valuesSource
operator|.
name|setNeedsGlobalOrdinals
argument_list|(
name|execution
operator|.
name|needsGlobalOrdinals
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|execution
operator|.
name|create
argument_list|(
name|name
argument_list|,
name|factories
argument_list|,
name|valuesSource
argument_list|,
name|estimatedBucketCount
argument_list|,
name|requiredSize
argument_list|,
name|shardSize
argument_list|,
name|minDocCount
argument_list|,
name|shardMinDocCount
argument_list|,
name|includeExclude
argument_list|,
name|aggregationContext
argument_list|,
name|parent
argument_list|,
name|this
argument_list|)
return|;
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
name|ValuesSource
operator|.
name|Numeric
condition|)
block|{
if|if
condition|(
operator|(
operator|(
name|ValuesSource
operator|.
name|Numeric
operator|)
name|valuesSource
operator|)
operator|.
name|isFloatingPoint
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"No support for examining floating point numerics"
argument_list|)
throw|;
block|}
return|return
operator|new
name|SignificantLongTermsAggregator
argument_list|(
name|name
argument_list|,
name|factories
argument_list|,
operator|(
name|ValuesSource
operator|.
name|Numeric
operator|)
name|valuesSource
argument_list|,
name|config
operator|.
name|format
argument_list|()
argument_list|,
name|estimatedBucketCount
argument_list|,
name|requiredSize
argument_list|,
name|shardSize
argument_list|,
name|minDocCount
argument_list|,
name|shardMinDocCount
argument_list|,
name|aggregationContext
argument_list|,
name|parent
argument_list|,
name|this
argument_list|)
return|;
block|}
throw|throw
operator|new
name|AggregationExecutionException
argument_list|(
literal|"sigfnificant_terms aggregation cannot be applied to field ["
operator|+
name|config
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
comment|/**      * Creates the TermsEnum (if not already created) and must be called before any calls to getBackgroundFrequency      * @param context The aggregation context       * @return The number of documents in the index (after an optional filter might have been applied)      */
DECL|method|prepareBackground
specifier|public
name|long
name|prepareBackground
parameter_list|(
name|AggregationContext
name|context
parameter_list|)
block|{
if|if
condition|(
name|termsEnum
operator|!=
literal|null
condition|)
block|{
comment|// already prepared - return
return|return
name|termsEnum
operator|.
name|getNumDocs
argument_list|()
return|;
block|}
name|SearchContext
name|searchContext
init|=
name|context
operator|.
name|searchContext
argument_list|()
decl_stmt|;
name|IndexReader
name|reader
init|=
name|searchContext
operator|.
name|searcher
argument_list|()
operator|.
name|getIndexReader
argument_list|()
decl_stmt|;
try|try
block|{
if|if
condition|(
name|numberOfAggregatorsCreated
operator|==
literal|1
condition|)
block|{
comment|// Setup a termsEnum for sole use by one aggregator
name|termsEnum
operator|=
operator|new
name|FilterableTermsEnum
argument_list|(
name|reader
argument_list|,
name|indexedFieldName
argument_list|,
name|DocsEnum
operator|.
name|FLAG_NONE
argument_list|,
name|filter
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// When we have> 1 agg we have possibility of duplicate term frequency lookups
comment|// and so use a TermsEnum that caches results of all term lookups
name|termsEnum
operator|=
operator|new
name|FreqTermsEnum
argument_list|(
name|reader
argument_list|,
name|indexedFieldName
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|,
name|filter
argument_list|,
name|searchContext
operator|.
name|bigArrays
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticsearchException
argument_list|(
literal|"failed to build terms enumeration"
argument_list|,
name|e
argument_list|)
throw|;
block|}
return|return
name|termsEnum
operator|.
name|getNumDocs
argument_list|()
return|;
block|}
DECL|method|getBackgroundFrequency
specifier|public
name|long
name|getBackgroundFrequency
parameter_list|(
name|BytesRef
name|termBytes
parameter_list|)
block|{
assert|assert
name|termsEnum
operator|!=
literal|null
assert|;
comment|// having failed to find a field in the index we don't expect any calls for frequencies
name|long
name|result
init|=
literal|0
decl_stmt|;
try|try
block|{
if|if
condition|(
name|termsEnum
operator|.
name|seekExact
argument_list|(
name|termBytes
argument_list|)
condition|)
block|{
name|result
operator|=
name|termsEnum
operator|.
name|docFreq
argument_list|()
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticsearchException
argument_list|(
literal|"IOException loading background document frequency info"
argument_list|,
name|e
argument_list|)
throw|;
block|}
return|return
name|result
return|;
block|}
DECL|method|getBackgroundFrequency
specifier|public
name|long
name|getBackgroundFrequency
parameter_list|(
name|long
name|term
parameter_list|)
block|{
name|BytesRef
name|indexedVal
init|=
name|mapper
operator|.
name|indexedValueForSearch
argument_list|(
name|term
argument_list|)
decl_stmt|;
return|return
name|getBackgroundFrequency
argument_list|(
name|indexedVal
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|ElasticsearchException
block|{
try|try
block|{
if|if
condition|(
name|termsEnum
operator|instanceof
name|Releasable
condition|)
block|{
operator|(
operator|(
name|Releasable
operator|)
name|termsEnum
operator|)
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|termsEnum
operator|=
literal|null
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

