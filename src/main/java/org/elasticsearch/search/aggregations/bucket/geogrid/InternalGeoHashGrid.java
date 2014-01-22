begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.bucket.geogrid
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
name|geogrid
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
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|PriorityQueue
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cache
operator|.
name|recycler
operator|.
name|CacheRecycler
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
name|geo
operator|.
name|GeoHashUtils
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
name|geo
operator|.
name|GeoPoint
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
name|recycler
operator|.
name|Recycler
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
name|*
import|;
end_import

begin_comment
comment|/**  * Represents a grid of cells where each cell's location is determined by a geohash.  * All geohashes in a grid are of the same precision and held internally as a single long  * for efficiency's sake.  */
end_comment

begin_class
DECL|class|InternalGeoHashGrid
specifier|public
class|class
name|InternalGeoHashGrid
extends|extends
name|InternalAggregation
implements|implements
name|GeoHashGrid
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
literal|"geohashgrid"
argument_list|,
literal|"ghcells"
argument_list|)
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
name|InternalGeoHashGrid
name|readResult
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|InternalGeoHashGrid
name|buckets
init|=
operator|new
name|InternalGeoHashGrid
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
implements|implements
name|GeoHashGrid
operator|.
name|Bucket
implements|,
name|Comparable
argument_list|<
name|Bucket
argument_list|>
block|{
DECL|field|geohashAsLong
specifier|protected
name|long
name|geohashAsLong
decl_stmt|;
DECL|field|docCount
specifier|protected
name|long
name|docCount
decl_stmt|;
DECL|field|aggregations
specifier|protected
name|InternalAggregations
name|aggregations
decl_stmt|;
DECL|method|Bucket
specifier|public
name|Bucket
parameter_list|(
name|long
name|geohashAsLong
parameter_list|,
name|long
name|docCount
parameter_list|,
name|InternalAggregations
name|aggregations
parameter_list|)
block|{
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
name|geohashAsLong
operator|=
name|geohashAsLong
expr_stmt|;
block|}
DECL|method|getGeoPoint
specifier|public
name|GeoPoint
name|getGeoPoint
parameter_list|()
block|{
return|return
name|GeoHashUtils
operator|.
name|decode
argument_list|(
name|geohashAsLong
argument_list|)
return|;
block|}
DECL|method|getGeoHash
specifier|public
name|String
name|getGeoHash
parameter_list|()
block|{
return|return
name|GeoHashUtils
operator|.
name|toString
argument_list|(
name|geohashAsLong
argument_list|)
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
annotation|@
name|Override
DECL|method|compareTo
specifier|public
name|int
name|compareTo
parameter_list|(
name|Bucket
name|other
parameter_list|)
block|{
if|if
condition|(
name|this
operator|.
name|geohashAsLong
operator|>
name|other
operator|.
name|geohashAsLong
condition|)
block|{
return|return
literal|1
return|;
block|}
if|if
condition|(
name|this
operator|.
name|geohashAsLong
operator|<
name|other
operator|.
name|geohashAsLong
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
return|return
literal|0
return|;
block|}
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
name|CacheRecycler
name|cacheRecycler
parameter_list|)
block|{
if|if
condition|(
name|buckets
operator|.
name|size
argument_list|()
operator|==
literal|1
condition|)
block|{
comment|// we still need to reduce the sub aggs
name|Bucket
name|bucket
init|=
name|buckets
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|bucket
operator|.
name|aggregations
operator|.
name|reduce
argument_list|(
name|cacheRecycler
argument_list|)
expr_stmt|;
return|return
name|bucket
return|;
block|}
name|Bucket
name|reduced
init|=
literal|null
decl_stmt|;
name|List
argument_list|<
name|InternalAggregations
argument_list|>
name|aggregationsList
init|=
operator|new
name|ArrayList
argument_list|<
name|InternalAggregations
argument_list|>
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
if|if
condition|(
name|reduced
operator|==
literal|null
condition|)
block|{
name|reduced
operator|=
name|bucket
expr_stmt|;
block|}
else|else
block|{
name|reduced
operator|.
name|docCount
operator|+=
name|bucket
operator|.
name|docCount
expr_stmt|;
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
name|reduced
operator|.
name|aggregations
operator|=
name|InternalAggregations
operator|.
name|reduce
argument_list|(
name|aggregationsList
argument_list|,
name|cacheRecycler
argument_list|)
expr_stmt|;
return|return
name|reduced
return|;
block|}
annotation|@
name|Override
DECL|method|getInternalKey
specifier|public
name|long
name|getInternalKey
parameter_list|()
block|{
return|return
name|geohashAsLong
return|;
block|}
block|}
DECL|field|requiredSize
specifier|private
name|int
name|requiredSize
decl_stmt|;
DECL|field|buckets
specifier|private
name|Collection
argument_list|<
name|Bucket
argument_list|>
name|buckets
decl_stmt|;
DECL|method|InternalGeoHashGrid
name|InternalGeoHashGrid
parameter_list|()
block|{}
comment|// for serialization
DECL|method|InternalGeoHashGrid
specifier|public
name|InternalGeoHashGrid
parameter_list|(
name|String
name|name
parameter_list|,
name|int
name|requiredSize
parameter_list|,
name|Collection
argument_list|<
name|Bucket
argument_list|>
name|buckets
parameter_list|)
block|{
name|super
argument_list|(
name|name
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
name|buckets
operator|=
name|buckets
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
DECL|class|BucketPriorityQueue
specifier|static
class|class
name|BucketPriorityQueue
extends|extends
name|PriorityQueue
argument_list|<
name|Bucket
argument_list|>
block|{
DECL|method|BucketPriorityQueue
specifier|public
name|BucketPriorityQueue
parameter_list|(
name|int
name|size
parameter_list|)
block|{
name|super
argument_list|(
name|size
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|lessThan
specifier|protected
name|boolean
name|lessThan
parameter_list|(
name|Bucket
name|o1
parameter_list|,
name|Bucket
name|o2
parameter_list|)
block|{
name|long
name|i
init|=
name|o2
operator|.
name|getDocCount
argument_list|()
operator|-
name|o1
operator|.
name|getDocCount
argument_list|()
decl_stmt|;
if|if
condition|(
name|i
operator|==
literal|0
condition|)
block|{
name|i
operator|=
name|o2
operator|.
name|compareTo
argument_list|(
name|o1
argument_list|)
expr_stmt|;
if|if
condition|(
name|i
operator|==
literal|0
condition|)
block|{
name|i
operator|=
name|System
operator|.
name|identityHashCode
argument_list|(
name|o2
argument_list|)
operator|-
name|System
operator|.
name|identityHashCode
argument_list|(
name|o1
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|i
operator|>
literal|0
condition|?
literal|true
else|:
literal|false
return|;
block|}
block|}
annotation|@
name|Override
DECL|method|reduce
specifier|public
name|InternalGeoHashGrid
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
name|InternalGeoHashGrid
name|grid
init|=
operator|(
name|InternalGeoHashGrid
operator|)
name|aggregations
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|grid
operator|.
name|trimExcessEntries
argument_list|(
name|reduceContext
operator|.
name|cacheRecycler
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|grid
return|;
block|}
name|InternalGeoHashGrid
name|reduced
init|=
literal|null
decl_stmt|;
name|Recycler
operator|.
name|V
argument_list|<
name|LongObjectOpenHashMap
argument_list|<
name|List
argument_list|<
name|Bucket
argument_list|>
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
name|InternalGeoHashGrid
name|grid
init|=
operator|(
name|InternalGeoHashGrid
operator|)
name|aggregation
decl_stmt|;
if|if
condition|(
name|reduced
operator|==
literal|null
condition|)
block|{
name|reduced
operator|=
name|grid
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
name|reduceContext
operator|.
name|cacheRecycler
argument_list|()
operator|.
name|longObjectMap
argument_list|(
name|grid
operator|.
name|buckets
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|Bucket
name|bucket
range|:
name|grid
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
name|v
argument_list|()
operator|.
name|get
argument_list|(
name|bucket
operator|.
name|geohashAsLong
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
argument_list|<
name|Bucket
argument_list|>
argument_list|(
name|aggregations
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|buckets
operator|.
name|v
argument_list|()
operator|.
name|put
argument_list|(
name|bucket
operator|.
name|geohashAsLong
argument_list|,
name|existingBuckets
argument_list|)
expr_stmt|;
block|}
name|existingBuckets
operator|.
name|add
argument_list|(
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
name|InternalGeoHashGrid
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
name|Math
operator|.
name|min
argument_list|(
name|requiredSize
argument_list|,
name|buckets
operator|.
name|v
argument_list|()
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
argument_list|)
decl_stmt|;
name|Object
index|[]
name|internalBuckets
init|=
name|buckets
operator|.
name|v
argument_list|()
operator|.
name|values
decl_stmt|;
name|boolean
index|[]
name|states
init|=
name|buckets
operator|.
name|v
argument_list|()
operator|.
name|allocated
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
name|states
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|states
index|[
name|i
index|]
condition|)
block|{
name|List
argument_list|<
name|Bucket
argument_list|>
name|sameCellBuckets
init|=
operator|(
name|List
argument_list|<
name|Bucket
argument_list|>
operator|)
name|internalBuckets
index|[
name|i
index|]
decl_stmt|;
name|ordered
operator|.
name|insertWithOverflow
argument_list|(
name|sameCellBuckets
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|reduce
argument_list|(
name|sameCellBuckets
argument_list|,
name|reduceContext
operator|.
name|cacheRecycler
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|buckets
operator|.
name|release
argument_list|()
expr_stmt|;
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
DECL|method|iterator
specifier|public
name|Iterator
argument_list|<
name|GeoHashGrid
operator|.
name|Bucket
argument_list|>
name|iterator
parameter_list|()
block|{
name|Object
name|o
init|=
name|buckets
operator|.
name|iterator
argument_list|()
decl_stmt|;
return|return
operator|(
name|Iterator
argument_list|<
name|GeoHashGrid
operator|.
name|Bucket
argument_list|>
operator|)
name|o
return|;
block|}
annotation|@
name|Override
DECL|method|getNumberOfBuckets
specifier|public
name|int
name|getNumberOfBuckets
parameter_list|()
block|{
return|return
name|buckets
operator|.
name|size
argument_list|()
return|;
block|}
DECL|method|trimExcessEntries
specifier|protected
name|void
name|trimExcessEntries
parameter_list|(
name|CacheRecycler
name|cacheRecycler
parameter_list|)
block|{
name|int
name|i
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Iterator
argument_list|<
name|Bucket
argument_list|>
name|iter
init|=
name|buckets
operator|.
name|iterator
argument_list|()
init|;
name|iter
operator|.
name|hasNext
argument_list|()
condition|;
control|)
block|{
name|Bucket
name|bucket
init|=
name|iter
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|i
operator|++
operator|>=
name|requiredSize
condition|)
block|{
name|iter
operator|.
name|remove
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|bucket
operator|.
name|aggregations
operator|.
name|reduce
argument_list|(
name|cacheRecycler
argument_list|)
expr_stmt|;
block|}
block|}
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
name|requiredSize
operator|=
name|in
operator|.
name|readVInt
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
name|Bucket
argument_list|>
name|buckets
init|=
operator|new
name|ArrayList
argument_list|<
name|Bucket
argument_list|>
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
name|out
operator|.
name|writeVInt
argument_list|(
name|requiredSize
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
name|geohashAsLong
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
name|getGeoHash
argument_list|()
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

