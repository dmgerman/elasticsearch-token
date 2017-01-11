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
name|StreamInput
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
name|StreamOutput
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
name|XContentBuilder
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
name|DocValueFormat
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
name|pipeline
operator|.
name|PipelineAggregator
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
comment|/**  * Result of the {@link TermsAggregator} when the field is some kind of decimal number like a float, double, or distance.  */
end_comment

begin_class
DECL|class|DoubleTerms
specifier|public
class|class
name|DoubleTerms
extends|extends
name|InternalMappedTerms
argument_list|<
name|DoubleTerms
argument_list|,
name|DoubleTerms
operator|.
name|Bucket
argument_list|>
block|{
DECL|field|NAME
specifier|public
specifier|static
specifier|final
name|String
name|NAME
init|=
literal|"dterms"
decl_stmt|;
DECL|class|Bucket
specifier|static
class|class
name|Bucket
extends|extends
name|InternalTerms
operator|.
name|Bucket
argument_list|<
name|Bucket
argument_list|>
block|{
DECL|field|term
specifier|private
specifier|final
name|double
name|term
decl_stmt|;
DECL|method|Bucket
specifier|public
name|Bucket
parameter_list|(
name|double
name|term
parameter_list|,
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
name|DocValueFormat
name|format
parameter_list|)
block|{
name|super
argument_list|(
name|docCount
argument_list|,
name|aggregations
argument_list|,
name|showDocCountError
argument_list|,
name|docCountError
argument_list|,
name|format
argument_list|)
expr_stmt|;
name|this
operator|.
name|term
operator|=
name|term
expr_stmt|;
block|}
comment|/**          * Read from a stream.          */
DECL|method|Bucket
specifier|public
name|Bucket
parameter_list|(
name|StreamInput
name|in
parameter_list|,
name|DocValueFormat
name|format
parameter_list|,
name|boolean
name|showDocCountError
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|in
argument_list|,
name|format
argument_list|,
name|showDocCountError
argument_list|)
expr_stmt|;
name|term
operator|=
name|in
operator|.
name|readDouble
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|writeTermTo
specifier|protected
name|void
name|writeTermTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeDouble
argument_list|(
name|term
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getKeyAsString
specifier|public
name|String
name|getKeyAsString
parameter_list|()
block|{
return|return
name|format
operator|.
name|format
argument_list|(
name|term
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|getKey
specifier|public
name|Object
name|getKey
parameter_list|()
block|{
return|return
name|term
return|;
block|}
annotation|@
name|Override
DECL|method|getKeyAsNumber
specifier|public
name|Number
name|getKeyAsNumber
parameter_list|()
block|{
return|return
name|term
return|;
block|}
annotation|@
name|Override
DECL|method|compareTerm
name|int
name|compareTerm
parameter_list|(
name|Terms
operator|.
name|Bucket
name|other
parameter_list|)
block|{
return|return
name|Double
operator|.
name|compare
argument_list|(
name|term
argument_list|,
operator|(
operator|(
name|Number
operator|)
name|other
operator|.
name|getKey
argument_list|()
operator|)
operator|.
name|doubleValue
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|newBucket
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
block|{
return|return
operator|new
name|Bucket
argument_list|(
name|term
argument_list|,
name|docCount
argument_list|,
name|aggs
argument_list|,
name|showDocCountError
argument_list|,
name|docCountError
argument_list|,
name|format
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|toXContent
specifier|public
name|XContentBuilder
name|toXContent
parameter_list|(
name|XContentBuilder
name|builder
parameter_list|,
name|Params
name|params
parameter_list|)
throws|throws
name|IOException
block|{
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|CommonFields
operator|.
name|KEY
argument_list|,
name|term
argument_list|)
expr_stmt|;
if|if
condition|(
name|format
operator|!=
name|DocValueFormat
operator|.
name|RAW
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|CommonFields
operator|.
name|KEY_AS_STRING
argument_list|,
name|format
operator|.
name|format
argument_list|(
name|term
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|field
argument_list|(
name|CommonFields
operator|.
name|DOC_COUNT
argument_list|,
name|getDocCount
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|showDocCountError
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|InternalTerms
operator|.
name|DOC_COUNT_ERROR_UPPER_BOUND_FIELD_NAME
argument_list|,
name|getDocCountError
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|aggregations
operator|.
name|toXContentInternal
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
return|return
name|builder
return|;
block|}
block|}
DECL|method|DoubleTerms
specifier|public
name|DoubleTerms
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
name|long
name|minDocCount
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
parameter_list|,
name|DocValueFormat
name|format
parameter_list|,
name|int
name|shardSize
parameter_list|,
name|boolean
name|showTermDocCountError
parameter_list|,
name|long
name|otherDocCount
parameter_list|,
name|List
argument_list|<
name|Bucket
argument_list|>
name|buckets
parameter_list|,
name|long
name|docCountError
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|,
name|order
argument_list|,
name|requiredSize
argument_list|,
name|minDocCount
argument_list|,
name|pipelineAggregators
argument_list|,
name|metaData
argument_list|,
name|format
argument_list|,
name|shardSize
argument_list|,
name|showTermDocCountError
argument_list|,
name|otherDocCount
argument_list|,
name|buckets
argument_list|,
name|docCountError
argument_list|)
expr_stmt|;
block|}
comment|/**      * Read from a stream.      */
DECL|method|DoubleTerms
specifier|public
name|DoubleTerms
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|in
argument_list|,
name|Bucket
operator|::
operator|new
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getWriteableName
specifier|public
name|String
name|getWriteableName
parameter_list|()
block|{
return|return
name|NAME
return|;
block|}
annotation|@
name|Override
DECL|method|create
specifier|public
name|DoubleTerms
name|create
parameter_list|(
name|List
argument_list|<
name|Bucket
argument_list|>
name|buckets
parameter_list|)
block|{
return|return
operator|new
name|DoubleTerms
argument_list|(
name|name
argument_list|,
name|order
argument_list|,
name|requiredSize
argument_list|,
name|minDocCount
argument_list|,
name|this
operator|.
name|pipelineAggregators
argument_list|()
argument_list|,
name|metaData
argument_list|,
name|format
argument_list|,
name|shardSize
argument_list|,
name|showTermDocCountError
argument_list|,
name|otherDocCount
argument_list|,
name|buckets
argument_list|,
name|docCountError
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|createBucket
specifier|public
name|Bucket
name|createBucket
parameter_list|(
name|InternalAggregations
name|aggregations
parameter_list|,
name|Bucket
name|prototype
parameter_list|)
block|{
return|return
operator|new
name|Bucket
argument_list|(
name|prototype
operator|.
name|term
argument_list|,
name|prototype
operator|.
name|docCount
argument_list|,
name|aggregations
argument_list|,
name|prototype
operator|.
name|showDocCountError
argument_list|,
name|prototype
operator|.
name|docCountError
argument_list|,
name|prototype
operator|.
name|format
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|create
specifier|protected
name|DoubleTerms
name|create
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
name|long
name|docCountError
parameter_list|,
name|long
name|otherDocCount
parameter_list|)
block|{
return|return
operator|new
name|DoubleTerms
argument_list|(
name|name
argument_list|,
name|order
argument_list|,
name|requiredSize
argument_list|,
name|minDocCount
argument_list|,
name|pipelineAggregators
argument_list|()
argument_list|,
name|getMetaData
argument_list|()
argument_list|,
name|format
argument_list|,
name|shardSize
argument_list|,
name|showTermDocCountError
argument_list|,
name|otherDocCount
argument_list|,
name|buckets
argument_list|,
name|docCountError
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|doXContentBody
specifier|public
name|XContentBuilder
name|doXContentBody
parameter_list|(
name|XContentBuilder
name|builder
parameter_list|,
name|Params
name|params
parameter_list|)
throws|throws
name|IOException
block|{
name|builder
operator|.
name|field
argument_list|(
name|InternalTerms
operator|.
name|DOC_COUNT_ERROR_UPPER_BOUND_FIELD_NAME
argument_list|,
name|docCountError
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|SUM_OF_OTHER_DOC_COUNTS
argument_list|,
name|otherDocCount
argument_list|)
expr_stmt|;
name|builder
operator|.
name|startArray
argument_list|(
name|CommonFields
operator|.
name|BUCKETS
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
name|bucket
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endArray
argument_list|()
expr_stmt|;
return|return
name|builder
return|;
block|}
annotation|@
name|Override
DECL|method|createBucketsArray
specifier|protected
name|Bucket
index|[]
name|createBucketsArray
parameter_list|(
name|int
name|size
parameter_list|)
block|{
return|return
operator|new
name|Bucket
index|[
name|size
index|]
return|;
block|}
annotation|@
name|Override
DECL|method|doReduce
specifier|public
name|InternalAggregation
name|doReduce
parameter_list|(
name|List
argument_list|<
name|InternalAggregation
argument_list|>
name|aggregations
parameter_list|,
name|ReduceContext
name|reduceContext
parameter_list|)
block|{
name|boolean
name|promoteToDouble
init|=
literal|false
decl_stmt|;
for|for
control|(
name|InternalAggregation
name|agg
range|:
name|aggregations
control|)
block|{
if|if
condition|(
name|agg
operator|instanceof
name|LongTerms
operator|&&
operator|(
operator|(
name|LongTerms
operator|)
name|agg
operator|)
operator|.
name|format
operator|==
name|DocValueFormat
operator|.
name|RAW
condition|)
block|{
comment|/**                  * this terms agg mixes longs and doubles, we must promote longs to doubles to make the internal aggs                  * compatible                  */
name|promoteToDouble
operator|=
literal|true
expr_stmt|;
break|break;
block|}
block|}
if|if
condition|(
name|promoteToDouble
operator|==
literal|false
condition|)
block|{
return|return
name|super
operator|.
name|doReduce
argument_list|(
name|aggregations
argument_list|,
name|reduceContext
argument_list|)
return|;
block|}
name|List
argument_list|<
name|InternalAggregation
argument_list|>
name|newAggs
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|InternalAggregation
name|agg
range|:
name|aggregations
control|)
block|{
if|if
condition|(
name|agg
operator|instanceof
name|LongTerms
condition|)
block|{
name|DoubleTerms
name|dTerms
init|=
name|LongTerms
operator|.
name|convertLongTermsToDouble
argument_list|(
operator|(
name|LongTerms
operator|)
name|agg
argument_list|,
name|format
argument_list|)
decl_stmt|;
name|newAggs
operator|.
name|add
argument_list|(
name|dTerms
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|newAggs
operator|.
name|add
argument_list|(
name|agg
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|newAggs
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|doReduce
argument_list|(
name|newAggs
argument_list|,
name|reduceContext
argument_list|)
return|;
block|}
block|}
end_class

end_unit

