begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.bucket.histogram
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
name|histogram
package|;
end_package

begin_import
import|import
name|com
operator|.
name|carrotsearch
operator|.
name|hppc
operator|.
name|LongObjectOpenHashMap
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
name|Lists
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
name|CollectionUtil
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|Version
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
name|rounding
operator|.
name|Rounding
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
name|bucket
operator|.
name|BucketStreamContext
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
name|BucketStreams
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
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ListIterator
import|;
end_import

begin_comment
comment|/**  * TODO should be renamed to InternalNumericHistogram (see comment on {@link Histogram})?  */
end_comment

begin_class
DECL|class|InternalHistogram
specifier|public
class|class
name|InternalHistogram
parameter_list|<
name|B
extends|extends
name|InternalHistogram
operator|.
name|Bucket
parameter_list|>
extends|extends
name|InternalAggregation
implements|implements
name|Histogram
block|{
DECL|field|TYPE
specifier|final
specifier|static
name|Type
name|TYPE
init|=
operator|new
name|Type
argument_list|(
literal|"histogram"
argument_list|,
literal|"histo"
argument_list|)
decl_stmt|;
DECL|field|FACTORY
specifier|final
specifier|static
name|Factory
name|FACTORY
init|=
operator|new
name|Factory
argument_list|()
decl_stmt|;
DECL|field|STREAM
specifier|private
specifier|final
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
name|InternalHistogram
name|readResult
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|InternalHistogram
name|histogram
init|=
operator|new
name|InternalHistogram
argument_list|()
decl_stmt|;
name|histogram
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
return|return
name|histogram
return|;
block|}
block|}
decl_stmt|;
DECL|field|BUCKET_STREAM
specifier|private
specifier|final
specifier|static
name|BucketStreams
operator|.
name|Stream
argument_list|<
name|Bucket
argument_list|>
name|BUCKET_STREAM
init|=
operator|new
name|BucketStreams
operator|.
name|Stream
argument_list|<
name|Bucket
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Bucket
name|readResult
parameter_list|(
name|StreamInput
name|in
parameter_list|,
name|BucketStreamContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|Bucket
name|histogram
init|=
operator|new
name|Bucket
argument_list|(
name|context
operator|.
name|keyed
argument_list|()
argument_list|,
name|context
operator|.
name|formatter
argument_list|()
argument_list|)
decl_stmt|;
name|histogram
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
return|return
name|histogram
return|;
block|}
annotation|@
name|Override
specifier|public
name|BucketStreamContext
name|getBucketStreamContext
parameter_list|(
name|Bucket
name|bucket
parameter_list|)
block|{
name|BucketStreamContext
name|context
init|=
operator|new
name|BucketStreamContext
argument_list|()
decl_stmt|;
name|context
operator|.
name|formatter
argument_list|(
name|bucket
operator|.
name|formatter
argument_list|)
expr_stmt|;
name|context
operator|.
name|keyed
argument_list|(
name|bucket
operator|.
name|keyed
argument_list|)
expr_stmt|;
return|return
name|context
return|;
block|}
block|}
decl_stmt|;
DECL|method|registerStream
specifier|public
specifier|static
name|void
name|registerStream
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
name|BucketStreams
operator|.
name|registerStream
argument_list|(
name|BUCKET_STREAM
argument_list|,
name|TYPE
operator|.
name|stream
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|class|Bucket
specifier|public
specifier|static
class|class
name|Bucket
implements|implements
name|Histogram
operator|.
name|Bucket
block|{
DECL|field|key
name|long
name|key
decl_stmt|;
DECL|field|docCount
name|long
name|docCount
decl_stmt|;
DECL|field|aggregations
name|InternalAggregations
name|aggregations
decl_stmt|;
DECL|field|keyed
specifier|private
specifier|transient
specifier|final
name|boolean
name|keyed
decl_stmt|;
DECL|field|formatter
specifier|protected
specifier|transient
specifier|final
annotation|@
name|Nullable
name|ValueFormatter
name|formatter
decl_stmt|;
DECL|method|Bucket
specifier|public
name|Bucket
parameter_list|(
name|boolean
name|keyed
parameter_list|,
annotation|@
name|Nullable
name|ValueFormatter
name|formatter
parameter_list|)
block|{
name|this
operator|.
name|formatter
operator|=
name|formatter
expr_stmt|;
name|this
operator|.
name|keyed
operator|=
name|keyed
expr_stmt|;
block|}
DECL|method|Bucket
specifier|public
name|Bucket
parameter_list|(
name|long
name|key
parameter_list|,
name|long
name|docCount
parameter_list|,
name|boolean
name|keyed
parameter_list|,
annotation|@
name|Nullable
name|ValueFormatter
name|formatter
parameter_list|,
name|InternalAggregations
name|aggregations
parameter_list|)
block|{
name|this
argument_list|(
name|keyed
argument_list|,
name|formatter
argument_list|)
expr_stmt|;
name|this
operator|.
name|key
operator|=
name|key
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
block|}
DECL|method|getFactory
specifier|protected
name|Factory
argument_list|<
name|?
argument_list|>
name|getFactory
parameter_list|()
block|{
return|return
name|FACTORY
return|;
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
name|formatter
operator|!=
literal|null
condition|?
name|formatter
operator|.
name|format
argument_list|(
name|key
argument_list|)
else|:
name|ValueFormatter
operator|.
name|RAW
operator|.
name|format
argument_list|(
name|key
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
name|getKey
argument_list|()
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
name|key
return|;
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
DECL|method|reduce
parameter_list|<
name|B
extends|extends
name|Bucket
parameter_list|>
name|B
name|reduce
parameter_list|(
name|List
argument_list|<
name|B
argument_list|>
name|buckets
parameter_list|,
name|ReduceContext
name|context
parameter_list|)
block|{
name|List
argument_list|<
name|InternalAggregations
argument_list|>
name|aggregations
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
name|long
name|docCount
init|=
literal|0
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
name|aggregations
operator|.
name|add
argument_list|(
operator|(
name|InternalAggregations
operator|)
name|bucket
operator|.
name|getAggregations
argument_list|()
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
name|aggregations
argument_list|,
name|context
argument_list|)
decl_stmt|;
return|return
operator|(
name|B
operator|)
name|getFactory
argument_list|()
operator|.
name|createBucket
argument_list|(
name|key
argument_list|,
name|docCount
argument_list|,
name|aggs
argument_list|,
name|keyed
argument_list|,
name|formatter
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
if|if
condition|(
name|formatter
operator|!=
literal|null
operator|&&
name|formatter
operator|!=
name|ValueFormatter
operator|.
name|RAW
condition|)
block|{
name|Text
name|keyTxt
init|=
operator|new
name|StringText
argument_list|(
name|formatter
operator|.
name|format
argument_list|(
name|key
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|keyed
condition|)
block|{
name|builder
operator|.
name|startObject
argument_list|(
name|keyTxt
operator|.
name|string
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
block|}
name|builder
operator|.
name|field
argument_list|(
name|CommonFields
operator|.
name|KEY_AS_STRING
argument_list|,
name|keyTxt
argument_list|)
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|keyed
condition|)
block|{
name|builder
operator|.
name|startObject
argument_list|(
name|String
operator|.
name|valueOf
argument_list|(
name|getKeyAsNumber
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
block|}
block|}
name|builder
operator|.
name|field
argument_list|(
name|CommonFields
operator|.
name|KEY
argument_list|,
name|key
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|CommonFields
operator|.
name|DOC_COUNT
argument_list|,
name|docCount
argument_list|)
expr_stmt|;
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
name|key
operator|=
name|in
operator|.
name|readLong
argument_list|()
expr_stmt|;
name|docCount
operator|=
name|in
operator|.
name|readVLong
argument_list|()
expr_stmt|;
name|aggregations
operator|=
name|InternalAggregations
operator|.
name|readAggregations
argument_list|(
name|in
argument_list|)
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
name|writeLong
argument_list|(
name|key
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVLong
argument_list|(
name|docCount
argument_list|)
expr_stmt|;
name|aggregations
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|EmptyBucketInfo
specifier|static
class|class
name|EmptyBucketInfo
block|{
DECL|field|rounding
specifier|final
name|Rounding
name|rounding
decl_stmt|;
DECL|field|subAggregations
specifier|final
name|InternalAggregations
name|subAggregations
decl_stmt|;
DECL|field|bounds
specifier|final
name|ExtendedBounds
name|bounds
decl_stmt|;
DECL|method|EmptyBucketInfo
name|EmptyBucketInfo
parameter_list|(
name|Rounding
name|rounding
parameter_list|,
name|InternalAggregations
name|subAggregations
parameter_list|)
block|{
name|this
argument_list|(
name|rounding
argument_list|,
name|subAggregations
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
DECL|method|EmptyBucketInfo
name|EmptyBucketInfo
parameter_list|(
name|Rounding
name|rounding
parameter_list|,
name|InternalAggregations
name|subAggregations
parameter_list|,
name|ExtendedBounds
name|bounds
parameter_list|)
block|{
name|this
operator|.
name|rounding
operator|=
name|rounding
expr_stmt|;
name|this
operator|.
name|subAggregations
operator|=
name|subAggregations
expr_stmt|;
name|this
operator|.
name|bounds
operator|=
name|bounds
expr_stmt|;
block|}
DECL|method|readFrom
specifier|public
specifier|static
name|EmptyBucketInfo
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|Rounding
name|rounding
init|=
name|Rounding
operator|.
name|Streams
operator|.
name|read
argument_list|(
name|in
argument_list|)
decl_stmt|;
name|InternalAggregations
name|aggs
init|=
name|InternalAggregations
operator|.
name|readAggregations
argument_list|(
name|in
argument_list|)
decl_stmt|;
if|if
condition|(
name|in
operator|.
name|getVersion
argument_list|()
operator|.
name|onOrAfter
argument_list|(
name|Version
operator|.
name|V_1_1_0
argument_list|)
condition|)
block|{
if|if
condition|(
name|in
operator|.
name|readBoolean
argument_list|()
condition|)
block|{
return|return
operator|new
name|EmptyBucketInfo
argument_list|(
name|rounding
argument_list|,
name|aggs
argument_list|,
name|ExtendedBounds
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
argument_list|)
return|;
block|}
block|}
return|return
operator|new
name|EmptyBucketInfo
argument_list|(
name|rounding
argument_list|,
name|aggs
argument_list|)
return|;
block|}
DECL|method|writeTo
specifier|public
specifier|static
name|void
name|writeTo
parameter_list|(
name|EmptyBucketInfo
name|info
parameter_list|,
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|Rounding
operator|.
name|Streams
operator|.
name|write
argument_list|(
name|info
operator|.
name|rounding
argument_list|,
name|out
argument_list|)
expr_stmt|;
name|info
operator|.
name|subAggregations
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
if|if
condition|(
name|out
operator|.
name|getVersion
argument_list|()
operator|.
name|onOrAfter
argument_list|(
name|Version
operator|.
name|V_1_1_0
argument_list|)
condition|)
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
name|info
operator|.
name|bounds
operator|!=
literal|null
argument_list|)
expr_stmt|;
if|if
condition|(
name|info
operator|.
name|bounds
operator|!=
literal|null
condition|)
block|{
name|info
operator|.
name|bounds
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
DECL|class|Factory
specifier|static
class|class
name|Factory
parameter_list|<
name|B
extends|extends
name|InternalHistogram
operator|.
name|Bucket
parameter_list|>
block|{
DECL|method|Factory
specifier|protected
name|Factory
parameter_list|()
block|{         }
DECL|method|type
specifier|public
name|String
name|type
parameter_list|()
block|{
return|return
name|TYPE
operator|.
name|name
argument_list|()
return|;
block|}
DECL|method|create
specifier|public
name|InternalHistogram
argument_list|<
name|B
argument_list|>
name|create
parameter_list|(
name|String
name|name
parameter_list|,
name|List
argument_list|<
name|B
argument_list|>
name|buckets
parameter_list|,
name|InternalOrder
name|order
parameter_list|,
name|long
name|minDocCount
parameter_list|,
name|EmptyBucketInfo
name|emptyBucketInfo
parameter_list|,
annotation|@
name|Nullable
name|ValueFormatter
name|formatter
parameter_list|,
name|boolean
name|keyed
parameter_list|)
block|{
return|return
operator|new
name|InternalHistogram
argument_list|<>
argument_list|(
name|name
argument_list|,
name|buckets
argument_list|,
name|order
argument_list|,
name|minDocCount
argument_list|,
name|emptyBucketInfo
argument_list|,
name|formatter
argument_list|,
name|keyed
argument_list|)
return|;
block|}
DECL|method|createBucket
specifier|public
name|B
name|createBucket
parameter_list|(
name|long
name|key
parameter_list|,
name|long
name|docCount
parameter_list|,
name|InternalAggregations
name|aggregations
parameter_list|,
name|boolean
name|keyed
parameter_list|,
annotation|@
name|Nullable
name|ValueFormatter
name|formatter
parameter_list|)
block|{
return|return
operator|(
name|B
operator|)
operator|new
name|Bucket
argument_list|(
name|key
argument_list|,
name|docCount
argument_list|,
name|keyed
argument_list|,
name|formatter
argument_list|,
name|aggregations
argument_list|)
return|;
block|}
block|}
DECL|field|buckets
specifier|protected
name|List
argument_list|<
name|B
argument_list|>
name|buckets
decl_stmt|;
DECL|field|bucketsMap
specifier|private
name|LongObjectOpenHashMap
argument_list|<
name|B
argument_list|>
name|bucketsMap
decl_stmt|;
DECL|field|order
specifier|private
name|InternalOrder
name|order
decl_stmt|;
DECL|field|formatter
specifier|private
annotation|@
name|Nullable
name|ValueFormatter
name|formatter
decl_stmt|;
DECL|field|keyed
specifier|private
name|boolean
name|keyed
decl_stmt|;
DECL|field|minDocCount
specifier|private
name|long
name|minDocCount
decl_stmt|;
DECL|field|emptyBucketInfo
specifier|private
name|EmptyBucketInfo
name|emptyBucketInfo
decl_stmt|;
DECL|method|InternalHistogram
name|InternalHistogram
parameter_list|()
block|{}
comment|// for serialization
DECL|method|InternalHistogram
name|InternalHistogram
parameter_list|(
name|String
name|name
parameter_list|,
name|List
argument_list|<
name|B
argument_list|>
name|buckets
parameter_list|,
name|InternalOrder
name|order
parameter_list|,
name|long
name|minDocCount
parameter_list|,
name|EmptyBucketInfo
name|emptyBucketInfo
parameter_list|,
annotation|@
name|Nullable
name|ValueFormatter
name|formatter
parameter_list|,
name|boolean
name|keyed
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|this
operator|.
name|buckets
operator|=
name|buckets
expr_stmt|;
name|this
operator|.
name|order
operator|=
name|order
expr_stmt|;
assert|assert
operator|(
name|minDocCount
operator|==
literal|0
operator|)
operator|==
operator|(
name|emptyBucketInfo
operator|!=
literal|null
operator|)
assert|;
name|this
operator|.
name|minDocCount
operator|=
name|minDocCount
expr_stmt|;
name|this
operator|.
name|emptyBucketInfo
operator|=
name|emptyBucketInfo
expr_stmt|;
name|this
operator|.
name|formatter
operator|=
name|formatter
expr_stmt|;
name|this
operator|.
name|keyed
operator|=
name|keyed
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
DECL|method|getBuckets
specifier|public
name|List
argument_list|<
name|B
argument_list|>
name|getBuckets
parameter_list|()
block|{
return|return
name|buckets
return|;
block|}
annotation|@
name|Override
DECL|method|getBucketByKey
specifier|public
name|B
name|getBucketByKey
parameter_list|(
name|String
name|key
parameter_list|)
block|{
return|return
name|getBucketByKey
argument_list|(
name|Long
operator|.
name|valueOf
argument_list|(
name|key
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|getBucketByKey
specifier|public
name|B
name|getBucketByKey
parameter_list|(
name|Number
name|key
parameter_list|)
block|{
if|if
condition|(
name|bucketsMap
operator|==
literal|null
condition|)
block|{
name|bucketsMap
operator|=
operator|new
name|LongObjectOpenHashMap
argument_list|<>
argument_list|(
name|buckets
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|B
name|bucket
range|:
name|buckets
control|)
block|{
name|bucketsMap
operator|.
name|put
argument_list|(
name|bucket
operator|.
name|key
argument_list|,
name|bucket
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|bucketsMap
operator|.
name|get
argument_list|(
name|key
operator|.
name|longValue
argument_list|()
argument_list|)
return|;
block|}
DECL|method|getFactory
specifier|protected
name|Factory
argument_list|<
name|B
argument_list|>
name|getFactory
parameter_list|()
block|{
return|return
name|FACTORY
return|;
block|}
annotation|@
name|Override
DECL|method|reduce
specifier|public
name|InternalAggregation
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
name|LongObjectPagedHashMap
argument_list|<
name|List
argument_list|<
name|B
argument_list|>
argument_list|>
name|bucketsByKey
init|=
operator|new
name|LongObjectPagedHashMap
argument_list|<>
argument_list|(
name|reduceContext
operator|.
name|bigArrays
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|InternalAggregation
name|aggregation
range|:
name|aggregations
control|)
block|{
name|InternalHistogram
argument_list|<
name|B
argument_list|>
name|histogram
init|=
operator|(
name|InternalHistogram
operator|)
name|aggregation
decl_stmt|;
for|for
control|(
name|B
name|bucket
range|:
name|histogram
operator|.
name|buckets
control|)
block|{
name|List
argument_list|<
name|B
argument_list|>
name|bucketList
init|=
name|bucketsByKey
operator|.
name|get
argument_list|(
name|bucket
operator|.
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|bucketList
operator|==
literal|null
condition|)
block|{
name|bucketList
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
name|bucketsByKey
operator|.
name|put
argument_list|(
name|bucket
operator|.
name|key
argument_list|,
name|bucketList
argument_list|)
expr_stmt|;
block|}
name|bucketList
operator|.
name|add
argument_list|(
name|bucket
argument_list|)
expr_stmt|;
block|}
block|}
name|List
argument_list|<
name|B
argument_list|>
name|reducedBuckets
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
operator|(
name|int
operator|)
name|bucketsByKey
operator|.
name|size
argument_list|()
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
name|B
argument_list|>
argument_list|>
name|cursor
range|:
name|bucketsByKey
control|)
block|{
name|List
argument_list|<
name|B
argument_list|>
name|sameTermBuckets
init|=
name|cursor
operator|.
name|value
decl_stmt|;
name|B
name|bucket
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
name|bucket
operator|.
name|getDocCount
argument_list|()
operator|>=
name|minDocCount
condition|)
block|{
name|reducedBuckets
operator|.
name|add
argument_list|(
name|bucket
argument_list|)
expr_stmt|;
block|}
block|}
name|bucketsByKey
operator|.
name|close
argument_list|()
expr_stmt|;
comment|// adding empty buckets in needed
if|if
condition|(
name|minDocCount
operator|==
literal|0
condition|)
block|{
name|CollectionUtil
operator|.
name|introSort
argument_list|(
name|reducedBuckets
argument_list|,
name|order
operator|.
name|asc
condition|?
name|InternalOrder
operator|.
name|KEY_ASC
operator|.
name|comparator
argument_list|()
else|:
name|InternalOrder
operator|.
name|KEY_DESC
operator|.
name|comparator
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|B
argument_list|>
name|list
init|=
name|order
operator|.
name|asc
condition|?
name|reducedBuckets
else|:
name|Lists
operator|.
name|reverse
argument_list|(
name|reducedBuckets
argument_list|)
decl_stmt|;
name|B
name|lastBucket
init|=
literal|null
decl_stmt|;
name|ExtendedBounds
name|bounds
init|=
name|emptyBucketInfo
operator|.
name|bounds
decl_stmt|;
name|ListIterator
argument_list|<
name|B
argument_list|>
name|iter
init|=
name|list
operator|.
name|listIterator
argument_list|()
decl_stmt|;
comment|// first adding all the empty buckets *before* the actual data (based on th extended_bounds.min the user requested)
if|if
condition|(
name|bounds
operator|!=
literal|null
condition|)
block|{
name|B
name|firstBucket
init|=
name|iter
operator|.
name|hasNext
argument_list|()
condition|?
name|list
operator|.
name|get
argument_list|(
name|iter
operator|.
name|nextIndex
argument_list|()
argument_list|)
else|:
literal|null
decl_stmt|;
if|if
condition|(
name|firstBucket
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|bounds
operator|.
name|min
operator|!=
literal|null
operator|&&
name|bounds
operator|.
name|max
operator|!=
literal|null
condition|)
block|{
name|long
name|key
init|=
name|bounds
operator|.
name|min
decl_stmt|;
name|long
name|max
init|=
name|bounds
operator|.
name|max
decl_stmt|;
while|while
condition|(
name|key
operator|<=
name|max
condition|)
block|{
name|iter
operator|.
name|add
argument_list|(
name|createBucket
argument_list|(
name|key
argument_list|,
literal|0
argument_list|,
name|emptyBucketInfo
operator|.
name|subAggregations
argument_list|,
name|keyed
argument_list|,
name|formatter
argument_list|)
argument_list|)
expr_stmt|;
name|key
operator|=
name|emptyBucketInfo
operator|.
name|rounding
operator|.
name|nextRoundingValue
argument_list|(
name|key
argument_list|)
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
if|if
condition|(
name|bounds
operator|.
name|min
operator|!=
literal|null
condition|)
block|{
name|long
name|key
init|=
name|bounds
operator|.
name|min
decl_stmt|;
if|if
condition|(
name|key
operator|<
name|firstBucket
operator|.
name|key
condition|)
block|{
while|while
condition|(
name|key
operator|<
name|firstBucket
operator|.
name|key
condition|)
block|{
name|iter
operator|.
name|add
argument_list|(
name|createBucket
argument_list|(
name|key
argument_list|,
literal|0
argument_list|,
name|emptyBucketInfo
operator|.
name|subAggregations
argument_list|,
name|keyed
argument_list|,
name|formatter
argument_list|)
argument_list|)
expr_stmt|;
name|key
operator|=
name|emptyBucketInfo
operator|.
name|rounding
operator|.
name|nextRoundingValue
argument_list|(
name|key
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
comment|// now adding the empty buckets within the actual data,
comment|// e.g. if the data series is [1,2,3,7] there're 3 empty buckets that will be created for 4,5,6
while|while
condition|(
name|iter
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|B
name|nextBucket
init|=
name|list
operator|.
name|get
argument_list|(
name|iter
operator|.
name|nextIndex
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|lastBucket
operator|!=
literal|null
condition|)
block|{
name|long
name|key
init|=
name|emptyBucketInfo
operator|.
name|rounding
operator|.
name|nextRoundingValue
argument_list|(
name|lastBucket
operator|.
name|key
argument_list|)
decl_stmt|;
while|while
condition|(
name|key
operator|<
name|nextBucket
operator|.
name|key
condition|)
block|{
name|iter
operator|.
name|add
argument_list|(
name|createBucket
argument_list|(
name|key
argument_list|,
literal|0
argument_list|,
name|emptyBucketInfo
operator|.
name|subAggregations
argument_list|,
name|keyed
argument_list|,
name|formatter
argument_list|)
argument_list|)
expr_stmt|;
name|key
operator|=
name|emptyBucketInfo
operator|.
name|rounding
operator|.
name|nextRoundingValue
argument_list|(
name|key
argument_list|)
expr_stmt|;
block|}
assert|assert
name|key
operator|==
name|nextBucket
operator|.
name|key
assert|;
block|}
name|lastBucket
operator|=
name|iter
operator|.
name|next
argument_list|()
expr_stmt|;
block|}
comment|// finally, adding the empty buckets *after* the actual data (based on the extended_bounds.max requested by the user)
if|if
condition|(
name|bounds
operator|!=
literal|null
operator|&&
name|lastBucket
operator|!=
literal|null
operator|&&
name|bounds
operator|.
name|max
operator|!=
literal|null
operator|&&
name|bounds
operator|.
name|max
operator|>
name|lastBucket
operator|.
name|key
condition|)
block|{
name|long
name|key
init|=
name|emptyBucketInfo
operator|.
name|rounding
operator|.
name|nextRoundingValue
argument_list|(
name|lastBucket
operator|.
name|key
argument_list|)
decl_stmt|;
name|long
name|max
init|=
name|bounds
operator|.
name|max
decl_stmt|;
while|while
condition|(
name|key
operator|<=
name|max
condition|)
block|{
name|iter
operator|.
name|add
argument_list|(
name|createBucket
argument_list|(
name|key
argument_list|,
literal|0
argument_list|,
name|emptyBucketInfo
operator|.
name|subAggregations
argument_list|,
name|keyed
argument_list|,
name|formatter
argument_list|)
argument_list|)
expr_stmt|;
name|key
operator|=
name|emptyBucketInfo
operator|.
name|rounding
operator|.
name|nextRoundingValue
argument_list|(
name|key
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|order
operator|!=
name|InternalOrder
operator|.
name|KEY_ASC
operator|&&
name|order
operator|!=
name|InternalOrder
operator|.
name|KEY_DESC
condition|)
block|{
name|CollectionUtil
operator|.
name|introSort
argument_list|(
name|reducedBuckets
argument_list|,
name|order
operator|.
name|comparator
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|CollectionUtil
operator|.
name|introSort
argument_list|(
name|reducedBuckets
argument_list|,
name|order
operator|.
name|comparator
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|getFactory
argument_list|()
operator|.
name|create
argument_list|(
name|getName
argument_list|()
argument_list|,
name|reducedBuckets
argument_list|,
name|order
argument_list|,
name|minDocCount
argument_list|,
name|emptyBucketInfo
argument_list|,
name|formatter
argument_list|,
name|keyed
argument_list|)
return|;
block|}
DECL|method|createBucket
specifier|protected
name|B
name|createBucket
parameter_list|(
name|long
name|key
parameter_list|,
name|long
name|docCount
parameter_list|,
name|InternalAggregations
name|aggregations
parameter_list|,
name|boolean
name|keyed
parameter_list|,
annotation|@
name|Nullable
name|ValueFormatter
name|formatter
parameter_list|)
block|{
return|return
operator|(
name|B
operator|)
operator|new
name|InternalHistogram
operator|.
name|Bucket
argument_list|(
name|key
argument_list|,
name|docCount
argument_list|,
name|keyed
argument_list|,
name|formatter
argument_list|,
name|aggregations
argument_list|)
return|;
block|}
DECL|method|createEmptyBucket
specifier|protected
name|B
name|createEmptyBucket
parameter_list|(
name|boolean
name|keyed
parameter_list|,
annotation|@
name|Nullable
name|ValueFormatter
name|formatter
parameter_list|)
block|{
return|return
operator|(
name|B
operator|)
operator|new
name|InternalHistogram
operator|.
name|Bucket
argument_list|(
name|keyed
argument_list|,
name|formatter
argument_list|)
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
name|name
operator|=
name|in
operator|.
name|readString
argument_list|()
expr_stmt|;
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
name|minDocCount
operator|=
name|in
operator|.
name|readVLong
argument_list|()
expr_stmt|;
if|if
condition|(
name|minDocCount
operator|==
literal|0
condition|)
block|{
name|emptyBucketInfo
operator|=
name|EmptyBucketInfo
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
name|formatter
operator|=
name|ValueFormatterStreams
operator|.
name|readOptional
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|keyed
operator|=
name|in
operator|.
name|readBoolean
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
name|B
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
name|B
name|bucket
init|=
name|createEmptyBucket
argument_list|(
name|keyed
argument_list|,
name|formatter
argument_list|)
decl_stmt|;
name|bucket
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|buckets
operator|.
name|add
argument_list|(
name|bucket
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
name|bucketsMap
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
name|out
operator|.
name|writeVLong
argument_list|(
name|minDocCount
argument_list|)
expr_stmt|;
if|if
condition|(
name|minDocCount
operator|==
literal|0
condition|)
block|{
name|EmptyBucketInfo
operator|.
name|writeTo
argument_list|(
name|emptyBucketInfo
argument_list|,
name|out
argument_list|)
expr_stmt|;
block|}
name|ValueFormatterStreams
operator|.
name|writeOptional
argument_list|(
name|formatter
argument_list|,
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|keyed
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
name|B
name|bucket
range|:
name|buckets
control|)
block|{
name|bucket
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
if|if
condition|(
name|keyed
condition|)
block|{
name|builder
operator|.
name|startObject
argument_list|(
name|CommonFields
operator|.
name|BUCKETS
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|builder
operator|.
name|startArray
argument_list|(
name|CommonFields
operator|.
name|BUCKETS
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|B
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
if|if
condition|(
name|keyed
condition|)
block|{
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|builder
operator|.
name|endArray
argument_list|()
expr_stmt|;
block|}
return|return
name|builder
return|;
block|}
block|}
end_class

end_unit

