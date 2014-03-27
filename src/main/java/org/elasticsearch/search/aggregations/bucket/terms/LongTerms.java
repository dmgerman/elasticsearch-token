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
name|text
operator|.
name|StringText
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
name|text
operator|.
name|Text
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
name|LongObjectPagedHashMap
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
name|aggregations
operator|.
name|AggregationStreams
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
name|support
operator|.
name|numeric
operator|.
name|ValueFormatter
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
name|ValueFormatterStreams
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

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|LongTerms
specifier|public
class|class
name|LongTerms
extends|extends
name|InternalTerms
block|{
DECL|field|TYPE
specifier|public
specifier|static
specifier|final
name|Type
name|TYPE
init|=
operator|new
name|Type
argument_list|(
literal|"terms"
argument_list|,
literal|"lterms"
argument_list|)
decl_stmt|;
DECL|field|STREAM
specifier|public
specifier|static
name|AggregationStreams
operator|.
name|Stream
name|STREAM
init|=
operator|new
name|AggregationStreams
operator|.
name|Stream
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|LongTerms
name|readResult
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|LongTerms
name|buckets
init|=
operator|new
name|LongTerms
argument_list|()
decl_stmt|;
name|buckets
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
return|return
name|buckets
return|;
block|}
block|}
decl_stmt|;
DECL|method|registerStreams
specifier|public
specifier|static
name|void
name|registerStreams
parameter_list|()
block|{
name|AggregationStreams
operator|.
name|registerStream
argument_list|(
name|STREAM
argument_list|,
name|TYPE
operator|.
name|stream
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|class|Bucket
specifier|static
class|class
name|Bucket
extends|extends
name|InternalTerms
operator|.
name|Bucket
block|{
DECL|field|term
name|long
name|term
decl_stmt|;
DECL|method|Bucket
specifier|public
name|Bucket
parameter_list|(
name|long
name|term
parameter_list|,
name|long
name|docCount
parameter_list|,
name|InternalAggregations
name|aggregations
parameter_list|)
block|{
name|super
argument_list|(
name|docCount
argument_list|,
name|aggregations
argument_list|)
expr_stmt|;
name|this
operator|.
name|term
operator|=
name|term
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getKey
specifier|public
name|String
name|getKey
parameter_list|()
block|{
return|return
name|String
operator|.
name|valueOf
argument_list|(
name|term
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|getKeyAsText
specifier|public
name|Text
name|getKeyAsText
parameter_list|()
block|{
return|return
operator|new
name|StringText
argument_list|(
name|String
operator|.
name|valueOf
argument_list|(
name|term
argument_list|)
argument_list|)
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
name|Long
operator|.
name|compare
argument_list|(
name|term
argument_list|,
name|other
operator|.
name|getKeyAsNumber
argument_list|()
operator|.
name|longValue
argument_list|()
argument_list|)
return|;
block|}
block|}
DECL|field|valueFormatter
specifier|private
name|ValueFormatter
name|valueFormatter
decl_stmt|;
DECL|method|LongTerms
name|LongTerms
parameter_list|()
block|{}
comment|// for serialization
DECL|method|LongTerms
specifier|public
name|LongTerms
parameter_list|(
name|String
name|name
parameter_list|,
name|InternalOrder
name|order
parameter_list|,
name|ValueFormatter
name|valueFormatter
parameter_list|,
name|int
name|requiredSize
parameter_list|,
name|long
name|minDocCount
parameter_list|,
name|Collection
argument_list|<
name|InternalTerms
operator|.
name|Bucket
argument_list|>
name|buckets
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
name|buckets
argument_list|)
expr_stmt|;
name|this
operator|.
name|valueFormatter
operator|=
name|valueFormatter
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|type
specifier|public
name|Type
name|type
parameter_list|()
block|{
return|return
name|TYPE
return|;
block|}
annotation|@
name|Override
DECL|method|reduce
specifier|public
name|InternalTerms
name|reduce
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
if|if
condition|(
name|aggregations
operator|.
name|size
argument_list|()
operator|==
literal|1
condition|)
block|{
name|InternalTerms
name|terms
init|=
operator|(
name|InternalTerms
operator|)
name|aggregations
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|terms
operator|.
name|trimExcessEntries
argument_list|(
name|reduceContext
operator|.
name|bigArrays
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|terms
return|;
block|}
name|InternalTerms
name|reduced
init|=
literal|null
decl_stmt|;
name|LongObjectPagedHashMap
argument_list|<
name|List
argument_list|<
name|Bucket
argument_list|>
argument_list|>
name|buckets
init|=
literal|null
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
if|if
condition|(
name|terms
operator|instanceof
name|UnmappedTerms
condition|)
block|{
continue|continue;
block|}
if|if
condition|(
name|reduced
operator|==
literal|null
condition|)
block|{
name|reduced
operator|=
name|terms
expr_stmt|;
block|}
if|if
condition|(
name|buckets
operator|==
literal|null
condition|)
block|{
name|buckets
operator|=
operator|new
name|LongObjectPagedHashMap
argument_list|<>
argument_list|(
name|terms
operator|.
name|buckets
operator|.
name|size
argument_list|()
argument_list|,
name|reduceContext
operator|.
name|bigArrays
argument_list|()
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|Terms
operator|.
name|Bucket
name|bucket
range|:
name|terms
operator|.
name|buckets
control|)
block|{
name|List
argument_list|<
name|Bucket
argument_list|>
name|existingBuckets
init|=
name|buckets
operator|.
name|get
argument_list|(
operator|(
operator|(
name|Bucket
operator|)
name|bucket
operator|)
operator|.
name|term
argument_list|)
decl_stmt|;
if|if
condition|(
name|existingBuckets
operator|==
literal|null
condition|)
block|{
name|existingBuckets
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|aggregations
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|buckets
operator|.
name|put
argument_list|(
operator|(
operator|(
name|Bucket
operator|)
name|bucket
operator|)
operator|.
name|term
argument_list|,
name|existingBuckets
argument_list|)
expr_stmt|;
block|}
name|existingBuckets
operator|.
name|add
argument_list|(
operator|(
name|Bucket
operator|)
name|bucket
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|reduced
operator|==
literal|null
condition|)
block|{
comment|// there are only unmapped terms, so we just return the first one (no need to reduce)
return|return
operator|(
name|UnmappedTerms
operator|)
name|aggregations
operator|.
name|get
argument_list|(
literal|0
argument_list|)
return|;
block|}
comment|// TODO: would it be better to sort the backing array buffer of the hppc map directly instead of using a PQ?
specifier|final
name|int
name|size
init|=
operator|(
name|int
operator|)
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
name|LongObjectPagedHashMap
operator|.
name|Cursor
argument_list|<
name|List
argument_list|<
name|LongTerms
operator|.
name|Bucket
argument_list|>
argument_list|>
name|cursor
range|:
name|buckets
control|)
block|{
name|List
argument_list|<
name|LongTerms
operator|.
name|Bucket
argument_list|>
name|sameTermBuckets
init|=
name|cursor
operator|.
name|value
decl_stmt|;
specifier|final
name|InternalTerms
operator|.
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
operator|.
name|bigArrays
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|b
operator|.
name|getDocCount
argument_list|()
operator|>=
name|minDocCount
condition|)
block|{
name|ordered
operator|.
name|insertWithOverflow
argument_list|(
name|b
argument_list|)
expr_stmt|;
block|}
block|}
name|buckets
operator|.
name|release
argument_list|()
expr_stmt|;
name|InternalTerms
operator|.
name|Bucket
index|[]
name|list
init|=
operator|new
name|InternalTerms
operator|.
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
name|reduced
operator|.
name|buckets
operator|=
name|Arrays
operator|.
name|asList
argument_list|(
name|list
argument_list|)
expr_stmt|;
return|return
name|reduced
return|;
block|}
annotation|@
name|Override
DECL|method|readFrom
specifier|public
name|void
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|name
operator|=
name|in
operator|.
name|readString
argument_list|()
expr_stmt|;
name|this
operator|.
name|order
operator|=
name|InternalOrder
operator|.
name|Streams
operator|.
name|readOrder
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|this
operator|.
name|valueFormatter
operator|=
name|ValueFormatterStreams
operator|.
name|readOptional
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|this
operator|.
name|requiredSize
operator|=
name|readSize
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|this
operator|.
name|minDocCount
operator|=
name|in
operator|.
name|readVLong
argument_list|()
expr_stmt|;
name|int
name|size
init|=
name|in
operator|.
name|readVInt
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|InternalTerms
operator|.
name|Bucket
argument_list|>
name|buckets
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|size
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|size
condition|;
name|i
operator|++
control|)
block|{
name|buckets
operator|.
name|add
argument_list|(
operator|new
name|Bucket
argument_list|(
name|in
operator|.
name|readLong
argument_list|()
argument_list|,
name|in
operator|.
name|readVLong
argument_list|()
argument_list|,
name|InternalAggregations
operator|.
name|readAggregations
argument_list|(
name|in
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|buckets
operator|=
name|buckets
expr_stmt|;
name|this
operator|.
name|bucketMap
operator|=
literal|null
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|writeTo
specifier|public
name|void
name|writeTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeString
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|InternalOrder
operator|.
name|Streams
operator|.
name|writeOrder
argument_list|(
name|order
argument_list|,
name|out
argument_list|)
expr_stmt|;
name|ValueFormatterStreams
operator|.
name|writeOptional
argument_list|(
name|valueFormatter
argument_list|,
name|out
argument_list|)
expr_stmt|;
name|writeSize
argument_list|(
name|requiredSize
argument_list|,
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVLong
argument_list|(
name|minDocCount
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|buckets
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|InternalTerms
operator|.
name|Bucket
name|bucket
range|:
name|buckets
control|)
block|{
name|out
operator|.
name|writeLong
argument_list|(
operator|(
operator|(
name|Bucket
operator|)
name|bucket
operator|)
operator|.
name|term
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVLong
argument_list|(
name|bucket
operator|.
name|getDocCount
argument_list|()
argument_list|)
expr_stmt|;
operator|(
operator|(
name|InternalAggregations
operator|)
name|bucket
operator|.
name|getAggregations
argument_list|()
operator|)
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
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
argument_list|(
name|name
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
name|InternalTerms
operator|.
name|Bucket
name|bucket
range|:
name|buckets
control|)
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
operator|(
operator|(
name|Bucket
operator|)
name|bucket
operator|)
operator|.
name|term
argument_list|)
expr_stmt|;
if|if
condition|(
name|valueFormatter
operator|!=
literal|null
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
name|valueFormatter
operator|.
name|format
argument_list|(
operator|(
operator|(
name|Bucket
operator|)
name|bucket
operator|)
operator|.
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
name|bucket
operator|.
name|getDocCount
argument_list|()
argument_list|)
expr_stmt|;
operator|(
operator|(
name|InternalAggregations
operator|)
name|bucket
operator|.
name|getAggregations
argument_list|()
operator|)
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
block|}
name|builder
operator|.
name|endArray
argument_list|()
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
end_class

end_unit

