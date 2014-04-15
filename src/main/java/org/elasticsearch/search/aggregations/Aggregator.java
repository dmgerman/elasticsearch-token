begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
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
name|ReaderContextAware
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
name|BigArrays
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
name|internal
operator|.
name|SearchContext
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
operator|.
name|Lifetime
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
name|ArrayList
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

begin_class
DECL|class|Aggregator
specifier|public
specifier|abstract
class|class
name|Aggregator
implements|implements
name|Releasable
implements|,
name|ReaderContextAware
block|{
comment|/**      * Defines the nature of the aggregator's aggregation execution when nested in other aggregators and the buckets they create.      */
DECL|enum|BucketAggregationMode
specifier|public
specifier|static
enum|enum
name|BucketAggregationMode
block|{
comment|/**          * In this mode, a new aggregator instance will be created per bucket (created by the parent aggregator)          */
DECL|enum constant|PER_BUCKET
name|PER_BUCKET
block|,
comment|/**          * In this mode, a single aggregator instance will be created per parent aggregator, that will handle the aggregations of all its buckets.          */
DECL|enum constant|MULTI_BUCKETS
name|MULTI_BUCKETS
block|}
DECL|field|name
specifier|protected
specifier|final
name|String
name|name
decl_stmt|;
DECL|field|parent
specifier|protected
specifier|final
name|Aggregator
name|parent
decl_stmt|;
DECL|field|context
specifier|protected
specifier|final
name|AggregationContext
name|context
decl_stmt|;
DECL|field|bigArrays
specifier|protected
specifier|final
name|BigArrays
name|bigArrays
decl_stmt|;
DECL|field|depth
specifier|protected
specifier|final
name|int
name|depth
decl_stmt|;
DECL|field|estimatedBucketCount
specifier|protected
specifier|final
name|long
name|estimatedBucketCount
decl_stmt|;
DECL|field|bucketAggregationMode
specifier|protected
specifier|final
name|BucketAggregationMode
name|bucketAggregationMode
decl_stmt|;
DECL|field|factories
specifier|protected
specifier|final
name|AggregatorFactories
name|factories
decl_stmt|;
DECL|field|subAggregators
specifier|protected
specifier|final
name|Aggregator
index|[]
name|subAggregators
decl_stmt|;
DECL|field|subAggregatorbyName
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|Aggregator
argument_list|>
name|subAggregatorbyName
decl_stmt|;
comment|/**      * Constructs a new Aggregator.      *      * @param name                  The name of the aggregation      * @param bucketAggregationMode The nature of execution as a sub-aggregator (see {@link BucketAggregationMode})      * @param factories             The factories for all the sub-aggregators under this aggregator      * @param estimatedBucketsCount When served as a sub-aggregator, indicate how many buckets the parent aggregator will generate.      * @param context               The aggregation context      * @param parent                The parent aggregator (may be {@code null} for top level aggregators)      */
DECL|method|Aggregator
specifier|protected
name|Aggregator
parameter_list|(
name|String
name|name
parameter_list|,
name|BucketAggregationMode
name|bucketAggregationMode
parameter_list|,
name|AggregatorFactories
name|factories
parameter_list|,
name|long
name|estimatedBucketsCount
parameter_list|,
name|AggregationContext
name|context
parameter_list|,
name|Aggregator
name|parent
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
name|this
operator|.
name|parent
operator|=
name|parent
expr_stmt|;
name|this
operator|.
name|estimatedBucketCount
operator|=
name|estimatedBucketsCount
expr_stmt|;
name|this
operator|.
name|context
operator|=
name|context
expr_stmt|;
name|this
operator|.
name|bigArrays
operator|=
name|context
operator|.
name|bigArrays
argument_list|()
expr_stmt|;
name|this
operator|.
name|depth
operator|=
name|parent
operator|==
literal|null
condition|?
literal|0
else|:
literal|1
operator|+
name|parent
operator|.
name|depth
argument_list|()
expr_stmt|;
name|this
operator|.
name|bucketAggregationMode
operator|=
name|bucketAggregationMode
expr_stmt|;
assert|assert
name|factories
operator|!=
literal|null
operator|:
literal|"sub-factories provided to BucketAggregator must not be null, use AggragatorFactories.EMPTY instead"
assert|;
name|this
operator|.
name|factories
operator|=
name|factories
expr_stmt|;
name|this
operator|.
name|subAggregators
operator|=
name|factories
operator|.
name|createSubAggregators
argument_list|(
name|this
argument_list|,
name|estimatedBucketsCount
argument_list|)
expr_stmt|;
name|context
operator|.
name|searchContext
argument_list|()
operator|.
name|addReleasable
argument_list|(
name|this
argument_list|,
name|Lifetime
operator|.
name|PHASE
argument_list|)
expr_stmt|;
block|}
comment|/**      * @return  The name of the aggregation.      */
DECL|method|name
specifier|public
name|String
name|name
parameter_list|()
block|{
return|return
name|name
return|;
block|}
comment|/** Return the estimated number of buckets. */
DECL|method|estimatedBucketCount
specifier|public
specifier|final
name|long
name|estimatedBucketCount
parameter_list|()
block|{
return|return
name|estimatedBucketCount
return|;
block|}
comment|/** Return the depth of this aggregator in the aggregation tree. */
DECL|method|depth
specifier|public
specifier|final
name|int
name|depth
parameter_list|()
block|{
return|return
name|depth
return|;
block|}
comment|/**      * @return  The parent aggregator of this aggregator. The addAggregation are hierarchical in the sense that some can      *          be composed out of others (more specifically, bucket addAggregation can define other addAggregation that will      *          be aggregated per bucket). This method returns the direct parent aggregator that contains this aggregator, or      *          {@code null} if there is none (meaning, this aggregator is a top level one)      */
DECL|method|parent
specifier|public
name|Aggregator
name|parent
parameter_list|()
block|{
return|return
name|parent
return|;
block|}
DECL|method|subAggregators
specifier|public
name|Aggregator
index|[]
name|subAggregators
parameter_list|()
block|{
return|return
name|subAggregators
return|;
block|}
DECL|method|subAggregator
specifier|public
name|Aggregator
name|subAggregator
parameter_list|(
name|String
name|aggName
parameter_list|)
block|{
if|if
condition|(
name|subAggregatorbyName
operator|==
literal|null
condition|)
block|{
name|subAggregatorbyName
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|(
name|subAggregators
operator|.
name|length
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|subAggregators
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|subAggregatorbyName
operator|.
name|put
argument_list|(
name|subAggregators
index|[
name|i
index|]
operator|.
name|name
argument_list|,
name|subAggregators
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|subAggregatorbyName
operator|.
name|get
argument_list|(
name|aggName
argument_list|)
return|;
block|}
comment|/**      * @return  The current aggregation context.      */
DECL|method|context
specifier|public
name|AggregationContext
name|context
parameter_list|()
block|{
return|return
name|context
return|;
block|}
comment|/**      * @return  The bucket aggregation mode of this aggregator. This mode defines the nature in which the aggregation is executed      * @see     BucketAggregationMode      */
DECL|method|bucketAggregationMode
specifier|public
name|BucketAggregationMode
name|bucketAggregationMode
parameter_list|()
block|{
return|return
name|bucketAggregationMode
return|;
block|}
comment|/**      * @return  Whether this aggregator is in the state where it can collect documents. Some aggregators can do their aggregations without      *          actually collecting documents, for example, an aggregator that computes stats over unmapped fields doesn't need to collect      *          anything as it knows to just return "empty" stats as the aggregation result.      */
DECL|method|shouldCollect
specifier|public
specifier|abstract
name|boolean
name|shouldCollect
parameter_list|()
function_decl|;
comment|/**      * Called during the query phase, to collect& aggregate the given document.      *      * @param doc                   The document to be collected/aggregated      * @param owningBucketOrdinal   The ordinal of the bucket this aggregator belongs to, assuming this aggregator is not a top level aggregator.      *                              Typically, aggregators with {@code #bucketAggregationMode} set to {@link BucketAggregationMode#MULTI_BUCKETS}      *                              will heavily depend on this ordinal. Other aggregators may or may not use it and can see this ordinal as just      *                              an extra information for the aggregation context. For top level aggregators, the ordinal will always be      *                              equal to 0.      * @throws IOException      */
DECL|method|collect
specifier|public
specifier|abstract
name|void
name|collect
parameter_list|(
name|int
name|doc
parameter_list|,
name|long
name|owningBucketOrdinal
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**      * Called after collection of all document is done.      */
DECL|method|postCollection
specifier|public
specifier|final
name|void
name|postCollection
parameter_list|()
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|subAggregators
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|subAggregators
index|[
name|i
index|]
operator|.
name|postCollection
argument_list|()
expr_stmt|;
block|}
name|doPostCollection
argument_list|()
expr_stmt|;
block|}
comment|/** Called upon release of the aggregator. */
annotation|@
name|Override
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
block|{
name|doClose
argument_list|()
expr_stmt|;
block|}
comment|/** Release instance-specific data. */
DECL|method|doClose
specifier|protected
name|void
name|doClose
parameter_list|()
block|{}
comment|/**      * Can be overriden by aggregator implementation to be called back when the collection phase ends.      */
DECL|method|doPostCollection
specifier|protected
name|void
name|doPostCollection
parameter_list|()
block|{     }
comment|/**      * @return  The aggregated& built aggregation      */
DECL|method|buildAggregation
specifier|public
specifier|abstract
name|InternalAggregation
name|buildAggregation
parameter_list|(
name|long
name|owningBucketOrdinal
parameter_list|)
function_decl|;
DECL|method|buildEmptyAggregation
specifier|public
specifier|abstract
name|InternalAggregation
name|buildEmptyAggregation
parameter_list|()
function_decl|;
DECL|method|buildEmptySubAggregations
specifier|protected
specifier|final
name|InternalAggregations
name|buildEmptySubAggregations
parameter_list|()
block|{
name|List
argument_list|<
name|InternalAggregation
argument_list|>
name|aggs
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|Aggregator
name|aggregator
range|:
name|subAggregators
control|)
block|{
name|aggs
operator|.
name|add
argument_list|(
name|aggregator
operator|.
name|buildEmptyAggregation
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|InternalAggregations
argument_list|(
name|aggs
argument_list|)
return|;
block|}
comment|/**      * Parses the aggregation request and creates the appropriate aggregator factory for it.      *      * @see {@link AggregatorFactory}     */
DECL|interface|Parser
specifier|public
specifier|static
interface|interface
name|Parser
block|{
comment|/**          * @return The aggregation type this parser is associated with.          */
DECL|method|type
name|String
name|type
parameter_list|()
function_decl|;
comment|/**          * Returns the aggregator factory with which this parser is associated, may return {@code null} indicating the          * aggregation should be skipped (e.g. when trying to aggregate on unmapped fields).          *          * @param aggregationName   The name of the aggregation          * @param parser            The xcontent parser          * @param context           The search context          * @return                  The resolved aggregator factory or {@code null} in case the aggregation should be skipped          * @throws java.io.IOException      When parsing fails          */
DECL|method|parse
name|AggregatorFactory
name|parse
parameter_list|(
name|String
name|aggregationName
parameter_list|,
name|XContentParser
name|parser
parameter_list|,
name|SearchContext
name|context
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
block|}
end_class

end_unit

