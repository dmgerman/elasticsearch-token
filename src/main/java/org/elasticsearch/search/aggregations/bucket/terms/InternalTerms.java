begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ArrayListMultimap
import|;
end_import

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
name|Maps
import|;
end_import

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
name|Multimap
import|;
end_import

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
name|io
operator|.
name|stream
operator|.
name|Streamable
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
name|ToXContent
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
name|Aggregations
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
name|InternalAggregations
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
name|InternalMultiBucketAggregation
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
name|BucketPriorityQueue
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
name|reducers
operator|.
name|Reducer
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
name|format
operator|.
name|ValueFormatter
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
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
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

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|InternalTerms
specifier|public
specifier|abstract
class|class
name|InternalTerms
extends|extends
name|InternalMultiBucketAggregation
implements|implements
name|Terms
implements|,
name|ToXContent
implements|,
name|Streamable
block|{
DECL|field|DOC_COUNT_ERROR_UPPER_BOUND_FIELD_NAME
specifier|protected
specifier|static
specifier|final
name|String
name|DOC_COUNT_ERROR_UPPER_BOUND_FIELD_NAME
init|=
literal|"doc_count_error_upper_bound"
decl_stmt|;
DECL|field|SUM_OF_OTHER_DOC_COUNTS
specifier|protected
specifier|static
specifier|final
name|String
name|SUM_OF_OTHER_DOC_COUNTS
init|=
literal|"sum_other_doc_count"
decl_stmt|;
DECL|class|Bucket
specifier|public
specifier|static
specifier|abstract
class|class
name|Bucket
extends|extends
name|Terms
operator|.
name|Bucket
block|{
DECL|field|bucketOrd
name|long
name|bucketOrd
decl_stmt|;
DECL|field|docCount
specifier|protected
name|long
name|docCount
decl_stmt|;
DECL|field|docCountError
specifier|protected
name|long
name|docCountError
decl_stmt|;
DECL|field|aggregations
specifier|protected
name|InternalAggregations
name|aggregations
decl_stmt|;
DECL|field|showDocCountError
specifier|protected
name|boolean
name|showDocCountError
decl_stmt|;
DECL|field|formatter
specifier|transient
specifier|final
name|ValueFormatter
name|formatter
decl_stmt|;
DECL|method|Bucket
specifier|protected
name|Bucket
parameter_list|(
annotation|@
name|Nullable
name|ValueFormatter
name|formatter
parameter_list|,
name|boolean
name|showDocCountError
parameter_list|)
block|{
comment|// for serialization
name|this
operator|.
name|showDocCountError
operator|=
name|showDocCountError
expr_stmt|;
name|this
operator|.
name|formatter
operator|=
name|formatter
expr_stmt|;
block|}
DECL|method|Bucket
specifier|protected
name|Bucket
parameter_list|(
name|long
name|docCount
parameter_list|,
name|InternalAggregations
name|aggregations
parameter_list|,
name|boolean
name|showDocCountError
parameter_list|,
name|long
name|docCountError
parameter_list|,
annotation|@
name|Nullable
name|ValueFormatter
name|formatter
parameter_list|)
block|{
name|this
argument_list|(
name|formatter
argument_list|,
name|showDocCountError
argument_list|)
expr_stmt|;
name|this
operator|.
name|docCount
operator|=
name|docCount
expr_stmt|;
name|this
operator|.
name|aggregations
operator|=
name|aggregations
expr_stmt|;
name|this
operator|.
name|docCountError
operator|=
name|docCountError
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getDocCount
specifier|public
name|long
name|getDocCount
parameter_list|()
block|{
return|return
name|docCount
return|;
block|}
annotation|@
name|Override
DECL|method|getDocCountError
specifier|public
name|long
name|getDocCountError
parameter_list|()
block|{
if|if
condition|(
operator|!
name|showDocCountError
condition|)
block|{
throw|throw
operator|new
name|ElasticsearchIllegalStateException
argument_list|(
literal|"show_terms_doc_count_error is false"
argument_list|)
throw|;
block|}
return|return
name|docCountError
return|;
block|}
annotation|@
name|Override
DECL|method|getAggregations
specifier|public
name|Aggregations
name|getAggregations
parameter_list|()
block|{
return|return
name|aggregations
return|;
block|}
DECL|method|newBucket
specifier|abstract
name|Bucket
name|newBucket
parameter_list|(
name|long
name|docCount
parameter_list|,
name|InternalAggregations
name|aggs
parameter_list|,
name|long
name|docCountError
parameter_list|)
function_decl|;
DECL|method|reduce
specifier|public
name|Bucket
name|reduce
parameter_list|(
name|List
argument_list|<
name|?
extends|extends
name|Bucket
argument_list|>
name|buckets
parameter_list|,
name|ReduceContext
name|context
parameter_list|)
block|{
name|long
name|docCount
init|=
literal|0
decl_stmt|;
name|long
name|docCountError
init|=
literal|0
decl_stmt|;
name|List
argument_list|<
name|InternalAggregations
argument_list|>
name|aggregationsList
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|buckets
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|Bucket
name|bucket
range|:
name|buckets
control|)
block|{
name|docCount
operator|+=
name|bucket
operator|.
name|docCount
expr_stmt|;
if|if
condition|(
name|docCountError
operator|!=
operator|-
literal|1
condition|)
block|{
if|if
condition|(
name|bucket
operator|.
name|docCountError
operator|==
operator|-
literal|1
condition|)
block|{
name|docCountError
operator|=
operator|-
literal|1
expr_stmt|;
block|}
else|else
block|{
name|docCountError
operator|+=
name|bucket
operator|.
name|docCountError
expr_stmt|;
block|}
block|}
name|aggregationsList
operator|.
name|add
argument_list|(
name|bucket
operator|.
name|aggregations
argument_list|)
expr_stmt|;
block|}
name|InternalAggregations
name|aggs
init|=
name|InternalAggregations
operator|.
name|reduce
argument_list|(
name|aggregationsList
argument_list|,
name|context
argument_list|)
decl_stmt|;
return|return
name|newBucket
argument_list|(
name|docCount
argument_list|,
name|aggs
argument_list|,
name|docCountError
argument_list|)
return|;
block|}
block|}
DECL|field|order
specifier|protected
name|Terms
operator|.
name|Order
name|order
decl_stmt|;
DECL|field|requiredSize
specifier|protected
name|int
name|requiredSize
decl_stmt|;
DECL|field|shardSize
specifier|protected
name|int
name|shardSize
decl_stmt|;
DECL|field|minDocCount
specifier|protected
name|long
name|minDocCount
decl_stmt|;
DECL|field|buckets
specifier|protected
name|List
argument_list|<
name|Bucket
argument_list|>
name|buckets
decl_stmt|;
DECL|field|bucketMap
specifier|protected
name|Map
argument_list|<
name|String
argument_list|,
name|Bucket
argument_list|>
name|bucketMap
decl_stmt|;
DECL|field|docCountError
specifier|protected
name|long
name|docCountError
decl_stmt|;
DECL|field|showTermDocCountError
specifier|protected
name|boolean
name|showTermDocCountError
decl_stmt|;
DECL|field|otherDocCount
specifier|protected
name|long
name|otherDocCount
decl_stmt|;
DECL|method|InternalTerms
specifier|protected
name|InternalTerms
parameter_list|()
block|{}
comment|// for serialization
DECL|method|InternalTerms
specifier|protected
name|InternalTerms
parameter_list|(
name|String
name|name
parameter_list|,
name|Terms
operator|.
name|Order
name|order
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
name|List
argument_list|<
name|Bucket
argument_list|>
name|buckets
parameter_list|,
name|boolean
name|showTermDocCountError
parameter_list|,
name|long
name|docCountError
parameter_list|,
name|long
name|otherDocCount
parameter_list|,
name|List
argument_list|<
name|Reducer
argument_list|>
name|reducers
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|metaData
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|,
name|reducers
argument_list|,
name|metaData
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
name|minDocCount
operator|=
name|minDocCount
expr_stmt|;
name|this
operator|.
name|buckets
operator|=
name|buckets
expr_stmt|;
name|this
operator|.
name|showTermDocCountError
operator|=
name|showTermDocCountError
expr_stmt|;
name|this
operator|.
name|docCountError
operator|=
name|docCountError
expr_stmt|;
name|this
operator|.
name|otherDocCount
operator|=
name|otherDocCount
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getBuckets
specifier|public
name|List
argument_list|<
name|Terms
operator|.
name|Bucket
argument_list|>
name|getBuckets
parameter_list|()
block|{
name|Object
name|o
init|=
name|buckets
decl_stmt|;
return|return
operator|(
name|List
argument_list|<
name|Terms
operator|.
name|Bucket
argument_list|>
operator|)
name|o
return|;
block|}
annotation|@
name|Override
DECL|method|getBucketByKey
specifier|public
name|Terms
operator|.
name|Bucket
name|getBucketByKey
parameter_list|(
name|String
name|term
parameter_list|)
block|{
if|if
condition|(
name|bucketMap
operator|==
literal|null
condition|)
block|{
name|bucketMap
operator|=
name|Maps
operator|.
name|newHashMapWithExpectedSize
argument_list|(
name|buckets
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Bucket
name|bucket
range|:
name|buckets
control|)
block|{
name|bucketMap
operator|.
name|put
argument_list|(
name|bucket
operator|.
name|getKeyAsString
argument_list|()
argument_list|,
name|bucket
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|bucketMap
operator|.
name|get
argument_list|(
name|term
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|getDocCountError
specifier|public
name|long
name|getDocCountError
parameter_list|()
block|{
return|return
name|docCountError
return|;
block|}
annotation|@
name|Override
DECL|method|getSumOfOtherDocCounts
specifier|public
name|long
name|getSumOfOtherDocCounts
parameter_list|()
block|{
return|return
name|otherDocCount
return|;
block|}
annotation|@
name|Override
DECL|method|doReduce
specifier|public
name|InternalAggregation
name|doReduce
parameter_list|(
name|ReduceContext
name|reduceContext
parameter_list|)
block|{
name|List
argument_list|<
name|InternalAggregation
argument_list|>
name|aggregations
init|=
name|reduceContext
operator|.
name|aggregations
argument_list|()
decl_stmt|;
name|Multimap
argument_list|<
name|Object
argument_list|,
name|InternalTerms
operator|.
name|Bucket
argument_list|>
name|buckets
init|=
name|ArrayListMultimap
operator|.
name|create
argument_list|()
decl_stmt|;
name|long
name|sumDocCountError
init|=
literal|0
decl_stmt|;
name|long
name|otherDocCount
init|=
literal|0
decl_stmt|;
for|for
control|(
name|InternalAggregation
name|aggregation
range|:
name|aggregations
control|)
block|{
name|InternalTerms
name|terms
init|=
operator|(
name|InternalTerms
operator|)
name|aggregation
decl_stmt|;
name|otherDocCount
operator|+=
name|terms
operator|.
name|getSumOfOtherDocCounts
argument_list|()
expr_stmt|;
specifier|final
name|long
name|thisAggDocCountError
decl_stmt|;
if|if
condition|(
name|terms
operator|.
name|buckets
operator|.
name|size
argument_list|()
operator|<
name|this
operator|.
name|shardSize
operator|||
name|this
operator|.
name|order
operator|==
name|InternalOrder
operator|.
name|TERM_ASC
operator|||
name|this
operator|.
name|order
operator|==
name|InternalOrder
operator|.
name|TERM_DESC
condition|)
block|{
name|thisAggDocCountError
operator|=
literal|0
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|InternalOrder
operator|.
name|isCountDesc
argument_list|(
name|this
operator|.
name|order
argument_list|)
condition|)
block|{
name|thisAggDocCountError
operator|=
name|terms
operator|.
name|buckets
operator|.
name|get
argument_list|(
name|terms
operator|.
name|buckets
operator|.
name|size
argument_list|()
operator|-
literal|1
argument_list|)
operator|.
name|docCount
expr_stmt|;
block|}
else|else
block|{
name|thisAggDocCountError
operator|=
operator|-
literal|1
expr_stmt|;
block|}
if|if
condition|(
name|sumDocCountError
operator|!=
operator|-
literal|1
condition|)
block|{
if|if
condition|(
name|thisAggDocCountError
operator|==
operator|-
literal|1
condition|)
block|{
name|sumDocCountError
operator|=
operator|-
literal|1
expr_stmt|;
block|}
else|else
block|{
name|sumDocCountError
operator|+=
name|thisAggDocCountError
expr_stmt|;
block|}
block|}
name|terms
operator|.
name|docCountError
operator|=
name|thisAggDocCountError
expr_stmt|;
for|for
control|(
name|Bucket
name|bucket
range|:
name|terms
operator|.
name|buckets
control|)
block|{
name|bucket
operator|.
name|docCountError
operator|=
name|thisAggDocCountError
expr_stmt|;
name|buckets
operator|.
name|put
argument_list|(
name|bucket
operator|.
name|getKey
argument_list|()
argument_list|,
name|bucket
argument_list|)
expr_stmt|;
block|}
block|}
specifier|final
name|int
name|size
init|=
name|Math
operator|.
name|min
argument_list|(
name|requiredSize
argument_list|,
name|buckets
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
name|BucketPriorityQueue
name|ordered
init|=
operator|new
name|BucketPriorityQueue
argument_list|(
name|size
argument_list|,
name|order
operator|.
name|comparator
argument_list|(
literal|null
argument_list|)
argument_list|)
decl_stmt|;
for|for
control|(
name|Collection
argument_list|<
name|Bucket
argument_list|>
name|l
range|:
name|buckets
operator|.
name|asMap
argument_list|()
operator|.
name|values
argument_list|()
control|)
block|{
name|List
argument_list|<
name|Bucket
argument_list|>
name|sameTermBuckets
init|=
operator|(
name|List
argument_list|<
name|Bucket
argument_list|>
operator|)
name|l
decl_stmt|;
comment|// cast is ok according to javadocs
specifier|final
name|Bucket
name|b
init|=
name|sameTermBuckets
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|reduce
argument_list|(
name|sameTermBuckets
argument_list|,
name|reduceContext
argument_list|)
decl_stmt|;
if|if
condition|(
name|b
operator|.
name|docCountError
operator|!=
operator|-
literal|1
condition|)
block|{
if|if
condition|(
name|sumDocCountError
operator|==
operator|-
literal|1
condition|)
block|{
name|b
operator|.
name|docCountError
operator|=
operator|-
literal|1
expr_stmt|;
block|}
else|else
block|{
name|b
operator|.
name|docCountError
operator|=
name|sumDocCountError
operator|-
name|b
operator|.
name|docCountError
expr_stmt|;
block|}
block|}
if|if
condition|(
name|b
operator|.
name|docCount
operator|>=
name|minDocCount
condition|)
block|{
name|Terms
operator|.
name|Bucket
name|removed
init|=
name|ordered
operator|.
name|insertWithOverflow
argument_list|(
name|b
argument_list|)
decl_stmt|;
if|if
condition|(
name|removed
operator|!=
literal|null
condition|)
block|{
name|otherDocCount
operator|+=
name|removed
operator|.
name|getDocCount
argument_list|()
expr_stmt|;
block|}
block|}
block|}
name|Bucket
index|[]
name|list
init|=
operator|new
name|Bucket
index|[
name|ordered
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
name|ordered
operator|.
name|size
argument_list|()
operator|-
literal|1
init|;
name|i
operator|>=
literal|0
condition|;
name|i
operator|--
control|)
block|{
name|list
index|[
name|i
index|]
operator|=
operator|(
name|Bucket
operator|)
name|ordered
operator|.
name|pop
argument_list|()
expr_stmt|;
block|}
name|long
name|docCountError
decl_stmt|;
if|if
condition|(
name|sumDocCountError
operator|==
operator|-
literal|1
condition|)
block|{
name|docCountError
operator|=
operator|-
literal|1
expr_stmt|;
block|}
else|else
block|{
name|docCountError
operator|=
name|aggregations
operator|.
name|size
argument_list|()
operator|==
literal|1
condition|?
literal|0
else|:
name|sumDocCountError
expr_stmt|;
block|}
return|return
name|newAggregation
argument_list|(
name|name
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|list
argument_list|)
argument_list|,
name|showTermDocCountError
argument_list|,
name|docCountError
argument_list|,
name|otherDocCount
argument_list|,
name|reducers
argument_list|()
argument_list|,
name|getMetaData
argument_list|()
argument_list|)
return|;
block|}
DECL|method|newAggregation
specifier|protected
specifier|abstract
name|InternalTerms
name|newAggregation
parameter_list|(
name|String
name|name
parameter_list|,
name|List
argument_list|<
name|Bucket
argument_list|>
name|buckets
parameter_list|,
name|boolean
name|showTermDocCountError
parameter_list|,
name|long
name|docCountError
parameter_list|,
name|long
name|otherDocCount
parameter_list|,
name|List
argument_list|<
name|Reducer
argument_list|>
name|reducers
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|metaData
parameter_list|)
function_decl|;
block|}
end_class

end_unit

