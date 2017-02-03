begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.bucket.adjacency
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
name|adjacency
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
name|ObjectParser
operator|.
name|NamedObjectParser
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
comment|/**  * Aggregation for adjacency matrices.  *   * NOTE! This is an experimental class.  *   * TODO the aggregation produces a sparse response but in the  * computation it uses a non-sparse structure (an array of Bits  * objects). This could be changed to a sparse structure in future.  *   */
end_comment

begin_class
DECL|class|AdjacencyMatrixAggregator
specifier|public
class|class
name|AdjacencyMatrixAggregator
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
DECL|class|KeyedFilter
specifier|protected
specifier|static
class|class
name|KeyedFilter
implements|implements
name|Writeable
implements|,
name|ToXContent
block|{
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
name|filter
decl_stmt|;
DECL|field|PARSER
specifier|public
specifier|static
specifier|final
name|NamedObjectParser
argument_list|<
name|KeyedFilter
argument_list|,
name|QueryParseContext
argument_list|>
name|PARSER
init|=
parameter_list|(
name|XContentParser
name|p
parameter_list|,
name|QueryParseContext
name|c
parameter_list|,
name|String
name|name
parameter_list|)
lambda|->
operator|new
name|KeyedFilter
argument_list|(
name|name
argument_list|,
name|c
operator|.
name|parseInnerQueryBuilder
argument_list|()
argument_list|)
decl_stmt|;
DECL|method|KeyedFilter
specifier|public
name|KeyedFilter
parameter_list|(
name|String
name|key
parameter_list|,
name|QueryBuilder
name|filter
parameter_list|)
block|{
if|if
condition|(
name|key
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"[key] must not be null"
argument_list|)
throw|;
block|}
if|if
condition|(
name|filter
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"[filter] must not be null"
argument_list|)
throw|;
block|}
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
comment|/**          * Read from a stream.          */
DECL|method|KeyedFilter
specifier|public
name|KeyedFilter
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
name|readString
argument_list|()
expr_stmt|;
name|filter
operator|=
name|in
operator|.
name|readNamedWriteable
argument_list|(
name|QueryBuilder
operator|.
name|class
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
name|writeString
argument_list|(
name|key
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeNamedWriteable
argument_list|(
name|filter
argument_list|)
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
DECL|field|totalNumKeys
specifier|private
specifier|final
name|int
name|totalNumKeys
decl_stmt|;
DECL|field|totalNumIntersections
specifier|private
specifier|final
name|int
name|totalNumIntersections
decl_stmt|;
DECL|field|separator
specifier|private
specifier|final
name|String
name|separator
decl_stmt|;
DECL|method|AdjacencyMatrixAggregator
specifier|public
name|AdjacencyMatrixAggregator
parameter_list|(
name|String
name|name
parameter_list|,
name|AggregatorFactories
name|factories
parameter_list|,
name|String
name|separator
parameter_list|,
name|String
index|[]
name|keys
parameter_list|,
name|Weight
index|[]
name|filters
parameter_list|,
name|SearchContext
name|context
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
name|context
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
name|separator
operator|=
name|separator
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
name|totalNumIntersections
operator|=
operator|(
operator|(
name|keys
operator|.
name|length
operator|*
name|keys
operator|.
name|length
operator|)
operator|-
name|keys
operator|.
name|length
operator|)
operator|/
literal|2
expr_stmt|;
name|this
operator|.
name|totalNumKeys
operator|=
name|keys
operator|.
name|length
operator|+
name|totalNumIntersections
expr_stmt|;
block|}
DECL|class|BitsIntersector
specifier|private
specifier|static
class|class
name|BitsIntersector
implements|implements
name|Bits
block|{
DECL|field|a
name|Bits
name|a
decl_stmt|;
DECL|field|b
name|Bits
name|b
decl_stmt|;
DECL|method|BitsIntersector
name|BitsIntersector
parameter_list|(
name|Bits
name|a
parameter_list|,
name|Bits
name|b
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|a
operator|=
name|a
expr_stmt|;
name|this
operator|.
name|b
operator|=
name|b
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|get
specifier|public
name|boolean
name|get
parameter_list|(
name|int
name|index
parameter_list|)
block|{
return|return
name|a
operator|.
name|get
argument_list|(
name|index
argument_list|)
operator|&&
name|b
operator|.
name|get
argument_list|(
name|index
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|length
specifier|public
name|int
name|length
parameter_list|()
block|{
return|return
name|Math
operator|.
name|min
argument_list|(
name|a
operator|.
name|length
argument_list|()
argument_list|,
name|b
operator|.
name|length
argument_list|()
argument_list|)
return|;
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
operator|+
name|totalNumIntersections
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
comment|// Add extra Bits for intersections
name|int
name|pos
init|=
name|filters
operator|.
name|length
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
for|for
control|(
name|int
name|j
init|=
name|i
operator|+
literal|1
init|;
name|j
operator|<
name|filters
operator|.
name|length
condition|;
name|j
operator|++
control|)
block|{
name|bits
index|[
name|pos
operator|++
index|]
operator|=
operator|new
name|BitsIntersector
argument_list|(
name|bits
index|[
name|i
index|]
argument_list|,
name|bits
index|[
name|j
index|]
argument_list|)
expr_stmt|;
block|}
block|}
assert|assert
name|pos
operator|==
name|bits
operator|.
name|length
assert|;
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
block|}
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
comment|// Buckets are ordered into groups - [keyed filters] [key1&key2 intersects]
name|List
argument_list|<
name|InternalAdjacencyMatrix
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
name|int
name|docCount
init|=
name|bucketDocCount
argument_list|(
name|bucketOrd
argument_list|)
decl_stmt|;
comment|// Empty buckets are not returned because this aggregation will commonly be used under a
comment|// a date-histogram where we will look for transactions over time and can expect many
comment|// empty buckets.
if|if
condition|(
name|docCount
operator|>
literal|0
condition|)
block|{
name|InternalAdjacencyMatrix
operator|.
name|InternalBucket
name|bucket
init|=
operator|new
name|InternalAdjacencyMatrix
operator|.
name|InternalBucket
argument_list|(
name|keys
index|[
name|i
index|]
argument_list|,
name|docCount
argument_list|,
name|bucketAggregations
argument_list|(
name|bucketOrd
argument_list|)
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
block|}
name|int
name|pos
init|=
name|keys
operator|.
name|length
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
for|for
control|(
name|int
name|j
init|=
name|i
operator|+
literal|1
init|;
name|j
operator|<
name|keys
operator|.
name|length
condition|;
name|j
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
name|pos
argument_list|)
decl_stmt|;
name|int
name|docCount
init|=
name|bucketDocCount
argument_list|(
name|bucketOrd
argument_list|)
decl_stmt|;
comment|// Empty buckets are not returned due to potential for very sparse matrices
if|if
condition|(
name|docCount
operator|>
literal|0
condition|)
block|{
name|String
name|intersectKey
init|=
name|keys
index|[
name|i
index|]
operator|+
name|separator
operator|+
name|keys
index|[
name|j
index|]
decl_stmt|;
name|InternalAdjacencyMatrix
operator|.
name|InternalBucket
name|bucket
init|=
operator|new
name|InternalAdjacencyMatrix
operator|.
name|InternalBucket
argument_list|(
name|intersectKey
argument_list|,
name|docCount
argument_list|,
name|bucketAggregations
argument_list|(
name|bucketOrd
argument_list|)
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
name|pos
operator|++
expr_stmt|;
block|}
block|}
return|return
operator|new
name|InternalAdjacencyMatrix
argument_list|(
name|name
argument_list|,
name|buckets
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
name|List
argument_list|<
name|InternalAdjacencyMatrix
operator|.
name|InternalBucket
argument_list|>
name|buckets
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
literal|0
argument_list|)
decl_stmt|;
return|return
operator|new
name|InternalAdjacencyMatrix
argument_list|(
name|name
argument_list|,
name|buckets
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
block|}
end_class

end_unit

