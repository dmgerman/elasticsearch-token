begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.bucket.filters
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
name|filters
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
name|LeafReaderContext
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
name|Weight
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
name|Bits
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
name|io
operator|.
name|stream
operator|.
name|Writeable
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
name|Lucene
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
name|AggregatorBuilder
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
name|LeafBucketCollector
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
name|LeafBucketCollectorBase
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
operator|.
name|Builder
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
name|BucketsAggregator
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
name|Objects
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|FiltersAggregator
specifier|public
class|class
name|FiltersAggregator
extends|extends
name|BucketsAggregator
block|{
DECL|field|FILTERS_FIELD
specifier|public
specifier|static
specifier|final
name|ParseField
name|FILTERS_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"filters"
argument_list|)
decl_stmt|;
DECL|field|OTHER_BUCKET_FIELD
specifier|public
specifier|static
specifier|final
name|ParseField
name|OTHER_BUCKET_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"other_bucket"
argument_list|)
decl_stmt|;
DECL|field|OTHER_BUCKET_KEY_FIELD
specifier|public
specifier|static
specifier|final
name|ParseField
name|OTHER_BUCKET_KEY_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"other_bucket_key"
argument_list|)
decl_stmt|;
DECL|class|KeyedFilter
specifier|public
specifier|static
class|class
name|KeyedFilter
implements|implements
name|Writeable
argument_list|<
name|KeyedFilter
argument_list|>
implements|,
name|ToXContent
block|{
DECL|field|PROTOTYPE
specifier|static
specifier|final
name|KeyedFilter
name|PROTOTYPE
init|=
operator|new
name|KeyedFilter
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
DECL|field|key
specifier|private
specifier|final
name|String
name|key
decl_stmt|;
DECL|field|filter
specifier|private
specifier|final
name|QueryBuilder
argument_list|<
name|?
argument_list|>
name|filter
decl_stmt|;
DECL|method|KeyedFilter
specifier|public
name|KeyedFilter
parameter_list|(
name|String
name|key
parameter_list|,
name|QueryBuilder
argument_list|<
name|?
argument_list|>
name|filter
parameter_list|)
block|{
name|this
operator|.
name|key
operator|=
name|key
expr_stmt|;
name|this
operator|.
name|filter
operator|=
name|filter
expr_stmt|;
block|}
DECL|method|key
specifier|public
name|String
name|key
parameter_list|()
block|{
return|return
name|key
return|;
block|}
DECL|method|filter
specifier|public
name|QueryBuilder
argument_list|<
name|?
argument_list|>
name|filter
parameter_list|()
block|{
return|return
name|filter
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
name|field
argument_list|(
name|key
argument_list|,
name|filter
argument_list|)
expr_stmt|;
return|return
name|builder
return|;
block|}
annotation|@
name|Override
DECL|method|readFrom
specifier|public
name|KeyedFilter
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|key
init|=
name|in
operator|.
name|readString
argument_list|()
decl_stmt|;
name|QueryBuilder
argument_list|<
name|?
argument_list|>
name|filter
init|=
name|in
operator|.
name|readQuery
argument_list|()
decl_stmt|;
return|return
operator|new
name|KeyedFilter
argument_list|(
name|key
argument_list|,
name|filter
argument_list|)
return|;
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
name|key
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeQuery
argument_list|(
name|filter
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|hashCode
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|Objects
operator|.
name|hash
argument_list|(
name|key
argument_list|,
name|filter
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|equals
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
if|if
condition|(
name|obj
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|getClass
argument_list|()
operator|!=
name|obj
operator|.
name|getClass
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
name|KeyedFilter
name|other
init|=
operator|(
name|KeyedFilter
operator|)
name|obj
decl_stmt|;
return|return
name|Objects
operator|.
name|equals
argument_list|(
name|key
argument_list|,
name|other
operator|.
name|key
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|filter
argument_list|,
name|other
operator|.
name|filter
argument_list|)
return|;
block|}
block|}
DECL|field|keys
specifier|private
specifier|final
name|String
index|[]
name|keys
decl_stmt|;
DECL|field|filters
specifier|private
name|Weight
index|[]
name|filters
decl_stmt|;
DECL|field|keyed
specifier|private
specifier|final
name|boolean
name|keyed
decl_stmt|;
DECL|field|showOtherBucket
specifier|private
specifier|final
name|boolean
name|showOtherBucket
decl_stmt|;
DECL|field|otherBucketKey
specifier|private
specifier|final
name|String
name|otherBucketKey
decl_stmt|;
DECL|field|totalNumKeys
specifier|private
specifier|final
name|int
name|totalNumKeys
decl_stmt|;
DECL|method|FiltersAggregator
specifier|public
name|FiltersAggregator
parameter_list|(
name|String
name|name
parameter_list|,
name|AggregatorFactories
name|factories
parameter_list|,
name|String
index|[]
name|keys
parameter_list|,
name|Weight
index|[]
name|filters
parameter_list|,
name|boolean
name|keyed
parameter_list|,
name|String
name|otherBucketKey
parameter_list|,
name|AggregationContext
name|aggregationContext
parameter_list|,
name|Aggregator
name|parent
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
name|super
argument_list|(
name|name
argument_list|,
name|factories
argument_list|,
name|aggregationContext
argument_list|,
name|parent
argument_list|,
name|pipelineAggregators
argument_list|,
name|metaData
argument_list|)
expr_stmt|;
name|this
operator|.
name|keyed
operator|=
name|keyed
expr_stmt|;
name|this
operator|.
name|keys
operator|=
name|keys
expr_stmt|;
name|this
operator|.
name|filters
operator|=
name|filters
expr_stmt|;
name|this
operator|.
name|showOtherBucket
operator|=
name|otherBucketKey
operator|!=
literal|null
expr_stmt|;
name|this
operator|.
name|otherBucketKey
operator|=
name|otherBucketKey
expr_stmt|;
if|if
condition|(
name|showOtherBucket
condition|)
block|{
name|this
operator|.
name|totalNumKeys
operator|=
name|keys
operator|.
name|length
operator|+
literal|1
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|totalNumKeys
operator|=
name|keys
operator|.
name|length
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|getLeafCollector
specifier|public
name|LeafBucketCollector
name|getLeafCollector
parameter_list|(
name|LeafReaderContext
name|ctx
parameter_list|,
specifier|final
name|LeafBucketCollector
name|sub
parameter_list|)
throws|throws
name|IOException
block|{
comment|// no need to provide deleted docs to the filter
specifier|final
name|Bits
index|[]
name|bits
init|=
operator|new
name|Bits
index|[
name|filters
operator|.
name|length
index|]
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
name|filters
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|bits
index|[
name|i
index|]
operator|=
name|Lucene
operator|.
name|asSequentialAccessBits
argument_list|(
name|ctx
operator|.
name|reader
argument_list|()
operator|.
name|maxDoc
argument_list|()
argument_list|,
name|filters
index|[
name|i
index|]
operator|.
name|scorer
argument_list|(
name|ctx
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|LeafBucketCollectorBase
argument_list|(
name|sub
argument_list|,
literal|null
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|void
name|collect
parameter_list|(
name|int
name|doc
parameter_list|,
name|long
name|bucket
parameter_list|)
throws|throws
name|IOException
block|{
name|boolean
name|matched
init|=
literal|false
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
name|bits
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|bits
index|[
name|i
index|]
operator|.
name|get
argument_list|(
name|doc
argument_list|)
condition|)
block|{
name|collectBucket
argument_list|(
name|sub
argument_list|,
name|doc
argument_list|,
name|bucketOrd
argument_list|(
name|bucket
argument_list|,
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|matched
operator|=
literal|true
expr_stmt|;
block|}
block|}
if|if
condition|(
name|showOtherBucket
operator|&&
operator|!
name|matched
condition|)
block|{
name|collectBucket
argument_list|(
name|sub
argument_list|,
name|doc
argument_list|,
name|bucketOrd
argument_list|(
name|bucket
argument_list|,
name|bits
operator|.
name|length
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|;
block|}
annotation|@
name|Override
DECL|method|buildAggregation
specifier|public
name|InternalAggregation
name|buildAggregation
parameter_list|(
name|long
name|owningBucketOrdinal
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|InternalFilters
operator|.
name|InternalBucket
argument_list|>
name|buckets
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|filters
operator|.
name|length
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
name|keys
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|long
name|bucketOrd
init|=
name|bucketOrd
argument_list|(
name|owningBucketOrdinal
argument_list|,
name|i
argument_list|)
decl_stmt|;
name|InternalFilters
operator|.
name|InternalBucket
name|bucket
init|=
operator|new
name|InternalFilters
operator|.
name|InternalBucket
argument_list|(
name|keys
index|[
name|i
index|]
argument_list|,
name|bucketDocCount
argument_list|(
name|bucketOrd
argument_list|)
argument_list|,
name|bucketAggregations
argument_list|(
name|bucketOrd
argument_list|)
argument_list|,
name|keyed
argument_list|)
decl_stmt|;
name|buckets
operator|.
name|add
argument_list|(
name|bucket
argument_list|)
expr_stmt|;
block|}
comment|// other bucket
if|if
condition|(
name|showOtherBucket
condition|)
block|{
name|long
name|bucketOrd
init|=
name|bucketOrd
argument_list|(
name|owningBucketOrdinal
argument_list|,
name|keys
operator|.
name|length
argument_list|)
decl_stmt|;
name|InternalFilters
operator|.
name|InternalBucket
name|bucket
init|=
operator|new
name|InternalFilters
operator|.
name|InternalBucket
argument_list|(
name|otherBucketKey
argument_list|,
name|bucketDocCount
argument_list|(
name|bucketOrd
argument_list|)
argument_list|,
name|bucketAggregations
argument_list|(
name|bucketOrd
argument_list|)
argument_list|,
name|keyed
argument_list|)
decl_stmt|;
name|buckets
operator|.
name|add
argument_list|(
name|bucket
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|InternalFilters
argument_list|(
name|name
argument_list|,
name|buckets
argument_list|,
name|keyed
argument_list|,
name|pipelineAggregators
argument_list|()
argument_list|,
name|metaData
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|buildEmptyAggregation
specifier|public
name|InternalAggregation
name|buildEmptyAggregation
parameter_list|()
block|{
name|InternalAggregations
name|subAggs
init|=
name|buildEmptySubAggregations
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|InternalFilters
operator|.
name|InternalBucket
argument_list|>
name|buckets
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|filters
operator|.
name|length
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
name|keys
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|InternalFilters
operator|.
name|InternalBucket
name|bucket
init|=
operator|new
name|InternalFilters
operator|.
name|InternalBucket
argument_list|(
name|keys
index|[
name|i
index|]
argument_list|,
literal|0
argument_list|,
name|subAggs
argument_list|,
name|keyed
argument_list|)
decl_stmt|;
name|buckets
operator|.
name|add
argument_list|(
name|bucket
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|InternalFilters
argument_list|(
name|name
argument_list|,
name|buckets
argument_list|,
name|keyed
argument_list|,
name|pipelineAggregators
argument_list|()
argument_list|,
name|metaData
argument_list|()
argument_list|)
return|;
block|}
DECL|method|bucketOrd
specifier|final
name|long
name|bucketOrd
parameter_list|(
name|long
name|owningBucketOrdinal
parameter_list|,
name|int
name|filterOrd
parameter_list|)
block|{
return|return
name|owningBucketOrdinal
operator|*
name|totalNumKeys
operator|+
name|filterOrd
return|;
block|}
DECL|class|FiltersAggregatorBuilder
specifier|public
specifier|static
class|class
name|FiltersAggregatorBuilder
extends|extends
name|AggregatorBuilder
argument_list|<
name|FiltersAggregatorBuilder
argument_list|>
block|{
DECL|field|filters
specifier|private
specifier|final
name|List
argument_list|<
name|KeyedFilter
argument_list|>
name|filters
decl_stmt|;
DECL|field|keyed
specifier|private
specifier|final
name|boolean
name|keyed
decl_stmt|;
DECL|field|otherBucket
specifier|private
name|boolean
name|otherBucket
init|=
literal|false
decl_stmt|;
DECL|field|otherBucketKey
specifier|private
name|String
name|otherBucketKey
init|=
literal|"_other_"
decl_stmt|;
comment|/**          * @param name          *            the name of this aggregation          * @param filters          *            the KeyedFilters to use with this aggregation.          */
DECL|method|FiltersAggregatorBuilder
specifier|public
name|FiltersAggregatorBuilder
parameter_list|(
name|String
name|name
parameter_list|,
name|KeyedFilter
modifier|...
name|filters
parameter_list|)
block|{
name|this
argument_list|(
name|name
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|filters
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|FiltersAggregatorBuilder
specifier|private
name|FiltersAggregatorBuilder
parameter_list|(
name|String
name|name
parameter_list|,
name|List
argument_list|<
name|KeyedFilter
argument_list|>
name|filters
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|,
name|InternalFilters
operator|.
name|TYPE
argument_list|)
expr_stmt|;
name|this
operator|.
name|filters
operator|=
name|filters
expr_stmt|;
name|this
operator|.
name|keyed
operator|=
literal|true
expr_stmt|;
block|}
comment|/**          * @param name          *            the name of this aggregation          * @param filters          *            the filters to use with this aggregation          */
DECL|method|FiltersAggregatorBuilder
specifier|public
name|FiltersAggregatorBuilder
parameter_list|(
name|String
name|name
parameter_list|,
name|QueryBuilder
argument_list|<
name|?
argument_list|>
modifier|...
name|filters
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|,
name|InternalFilters
operator|.
name|TYPE
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|KeyedFilter
argument_list|>
name|keyedFilters
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|filters
operator|.
name|length
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
name|filters
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|keyedFilters
operator|.
name|add
argument_list|(
operator|new
name|KeyedFilter
argument_list|(
name|String
operator|.
name|valueOf
argument_list|(
name|i
argument_list|)
argument_list|,
name|filters
index|[
name|i
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|filters
operator|=
name|keyedFilters
expr_stmt|;
name|this
operator|.
name|keyed
operator|=
literal|false
expr_stmt|;
block|}
comment|/**          * Set whether to include a bucket for documents not matching any filter          */
DECL|method|otherBucket
specifier|public
name|FiltersAggregatorBuilder
name|otherBucket
parameter_list|(
name|boolean
name|otherBucket
parameter_list|)
block|{
name|this
operator|.
name|otherBucket
operator|=
name|otherBucket
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**          * Get whether to include a bucket for documents not matching any filter          */
DECL|method|otherBucket
specifier|public
name|boolean
name|otherBucket
parameter_list|()
block|{
return|return
name|otherBucket
return|;
block|}
comment|/**          * Set the key to use for the bucket for documents not matching any          * filter.          */
DECL|method|otherBucketKey
specifier|public
name|FiltersAggregatorBuilder
name|otherBucketKey
parameter_list|(
name|String
name|otherBucketKey
parameter_list|)
block|{
name|this
operator|.
name|otherBucketKey
operator|=
name|otherBucketKey
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**          * Get the key to use for the bucket for documents not matching any          * filter.          */
DECL|method|otherBucketKey
specifier|public
name|String
name|otherBucketKey
parameter_list|()
block|{
return|return
name|otherBucketKey
return|;
block|}
annotation|@
name|Override
DECL|method|doBuild
specifier|protected
name|AggregatorFactory
argument_list|<
name|?
argument_list|>
name|doBuild
parameter_list|(
name|AggregationContext
name|context
parameter_list|,
name|AggregatorFactory
argument_list|<
name|?
argument_list|>
name|parent
parameter_list|,
name|Builder
name|subFactoriesBuilder
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|FiltersAggregatorFactory
argument_list|(
name|name
argument_list|,
name|type
argument_list|,
name|filters
argument_list|,
name|keyed
argument_list|,
name|otherBucket
argument_list|,
name|otherBucketKey
argument_list|,
name|context
argument_list|,
name|parent
argument_list|,
name|subFactoriesBuilder
argument_list|,
name|metaData
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|internalXContent
specifier|protected
name|XContentBuilder
name|internalXContent
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
if|if
condition|(
name|keyed
condition|)
block|{
name|builder
operator|.
name|startObject
argument_list|(
name|FILTERS_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|KeyedFilter
name|keyedFilter
range|:
name|filters
control|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|keyedFilter
operator|.
name|key
argument_list|()
argument_list|,
name|keyedFilter
operator|.
name|filter
argument_list|()
argument_list|)
expr_stmt|;
block|}
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
name|startArray
argument_list|(
name|FILTERS_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|KeyedFilter
name|keyedFilter
range|:
name|filters
control|)
block|{
name|builder
operator|.
name|value
argument_list|(
name|keyedFilter
operator|.
name|filter
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endArray
argument_list|()
expr_stmt|;
block|}
name|builder
operator|.
name|field
argument_list|(
name|OTHER_BUCKET_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|otherBucket
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|OTHER_BUCKET_KEY_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|otherBucketKey
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
DECL|method|doReadFrom
specifier|protected
name|FiltersAggregatorBuilder
name|doReadFrom
parameter_list|(
name|String
name|name
parameter_list|,
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|FiltersAggregatorBuilder
name|factory
decl_stmt|;
if|if
condition|(
name|in
operator|.
name|readBoolean
argument_list|()
condition|)
block|{
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
name|KeyedFilter
argument_list|>
name|filters
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
name|filters
operator|.
name|add
argument_list|(
name|KeyedFilter
operator|.
name|PROTOTYPE
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|factory
operator|=
operator|new
name|FiltersAggregatorBuilder
argument_list|(
name|name
argument_list|,
name|filters
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|int
name|size
init|=
name|in
operator|.
name|readVInt
argument_list|()
decl_stmt|;
name|QueryBuilder
argument_list|<
name|?
argument_list|>
index|[]
name|filters
init|=
operator|new
name|QueryBuilder
argument_list|<
name|?
argument_list|>
index|[
name|size
index|]
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
name|filters
index|[
name|i
index|]
operator|=
name|in
operator|.
name|readQuery
argument_list|()
expr_stmt|;
block|}
name|factory
operator|=
operator|new
name|FiltersAggregatorBuilder
argument_list|(
name|name
argument_list|,
name|filters
argument_list|)
expr_stmt|;
block|}
name|factory
operator|.
name|otherBucket
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
name|factory
operator|.
name|otherBucketKey
operator|=
name|in
operator|.
name|readString
argument_list|()
expr_stmt|;
return|return
name|factory
return|;
block|}
annotation|@
name|Override
DECL|method|doWriteTo
specifier|protected
name|void
name|doWriteTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
name|keyed
argument_list|)
expr_stmt|;
if|if
condition|(
name|keyed
condition|)
block|{
name|out
operator|.
name|writeVInt
argument_list|(
name|filters
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|KeyedFilter
name|keyedFilter
range|:
name|filters
control|)
block|{
name|keyedFilter
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|out
operator|.
name|writeVInt
argument_list|(
name|filters
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|KeyedFilter
name|keyedFilter
range|:
name|filters
control|)
block|{
name|out
operator|.
name|writeQuery
argument_list|(
name|keyedFilter
operator|.
name|filter
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|out
operator|.
name|writeBoolean
argument_list|(
name|otherBucket
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeString
argument_list|(
name|otherBucketKey
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doHashCode
specifier|protected
name|int
name|doHashCode
parameter_list|()
block|{
return|return
name|Objects
operator|.
name|hash
argument_list|(
name|filters
argument_list|,
name|keyed
argument_list|,
name|otherBucket
argument_list|,
name|otherBucketKey
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|doEquals
specifier|protected
name|boolean
name|doEquals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
name|FiltersAggregatorBuilder
name|other
init|=
operator|(
name|FiltersAggregatorBuilder
operator|)
name|obj
decl_stmt|;
return|return
name|Objects
operator|.
name|equals
argument_list|(
name|filters
argument_list|,
name|other
operator|.
name|filters
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|keyed
argument_list|,
name|other
operator|.
name|keyed
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|otherBucket
argument_list|,
name|other
operator|.
name|otherBucket
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|otherBucketKey
argument_list|,
name|other
operator|.
name|otherBucketKey
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

