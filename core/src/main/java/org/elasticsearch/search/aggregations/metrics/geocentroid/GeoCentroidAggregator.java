begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.metrics.geocentroid
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|metrics
operator|.
name|geocentroid
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
name|spatial
operator|.
name|geopoint
operator|.
name|document
operator|.
name|GeoPointField
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
name|lease
operator|.
name|Releasables
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
name|util
operator|.
name|LongArray
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
name|fielddata
operator|.
name|MultiGeoPointValues
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
name|metrics
operator|.
name|MetricsAggregator
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
comment|/**  * A geo metric aggregator that computes a geo-centroid from a {@code geo_point} type field  */
end_comment

begin_class
DECL|class|GeoCentroidAggregator
specifier|public
specifier|final
class|class
name|GeoCentroidAggregator
extends|extends
name|MetricsAggregator
block|{
DECL|field|valuesSource
specifier|private
specifier|final
name|ValuesSource
operator|.
name|GeoPoint
name|valuesSource
decl_stmt|;
DECL|field|centroids
name|LongArray
name|centroids
decl_stmt|;
DECL|field|counts
name|LongArray
name|counts
decl_stmt|;
DECL|method|GeoCentroidAggregator
specifier|protected
name|GeoCentroidAggregator
parameter_list|(
name|String
name|name
parameter_list|,
name|SearchContext
name|context
parameter_list|,
name|Aggregator
name|parent
parameter_list|,
name|ValuesSource
operator|.
name|GeoPoint
name|valuesSource
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
name|valuesSource
operator|=
name|valuesSource
expr_stmt|;
if|if
condition|(
name|valuesSource
operator|!=
literal|null
condition|)
block|{
specifier|final
name|BigArrays
name|bigArrays
init|=
name|context
operator|.
name|bigArrays
argument_list|()
decl_stmt|;
name|centroids
operator|=
name|bigArrays
operator|.
name|newLongArray
argument_list|(
literal|1
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|counts
operator|=
name|bigArrays
operator|.
name|newLongArray
argument_list|(
literal|1
argument_list|,
literal|true
argument_list|)
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
name|LeafBucketCollector
name|sub
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|valuesSource
operator|==
literal|null
condition|)
block|{
return|return
name|LeafBucketCollector
operator|.
name|NO_OP_COLLECTOR
return|;
block|}
specifier|final
name|BigArrays
name|bigArrays
init|=
name|context
operator|.
name|bigArrays
argument_list|()
decl_stmt|;
specifier|final
name|MultiGeoPointValues
name|values
init|=
name|valuesSource
operator|.
name|geoPointValues
argument_list|(
name|ctx
argument_list|)
decl_stmt|;
return|return
operator|new
name|LeafBucketCollectorBase
argument_list|(
name|sub
argument_list|,
name|values
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
name|centroids
operator|=
name|bigArrays
operator|.
name|grow
argument_list|(
name|centroids
argument_list|,
name|bucket
operator|+
literal|1
argument_list|)
expr_stmt|;
name|counts
operator|=
name|bigArrays
operator|.
name|grow
argument_list|(
name|counts
argument_list|,
name|bucket
operator|+
literal|1
argument_list|)
expr_stmt|;
name|values
operator|.
name|setDocument
argument_list|(
name|doc
argument_list|)
expr_stmt|;
specifier|final
name|int
name|valueCount
init|=
name|values
operator|.
name|count
argument_list|()
decl_stmt|;
if|if
condition|(
name|valueCount
operator|>
literal|0
condition|)
block|{
name|double
index|[]
name|pt
init|=
operator|new
name|double
index|[
literal|2
index|]
decl_stmt|;
comment|// get the previously accumulated number of counts
name|long
name|prevCounts
init|=
name|counts
operator|.
name|get
argument_list|(
name|bucket
argument_list|)
decl_stmt|;
comment|// increment by the number of points for this document
name|counts
operator|.
name|increment
argument_list|(
name|bucket
argument_list|,
name|valueCount
argument_list|)
expr_stmt|;
comment|// get the previous GeoPoint if a moving avg was computed
if|if
condition|(
name|prevCounts
operator|>
literal|0
condition|)
block|{
specifier|final
name|long
name|mortonCode
init|=
name|centroids
operator|.
name|get
argument_list|(
name|bucket
argument_list|)
decl_stmt|;
name|pt
index|[
literal|0
index|]
operator|=
name|GeoPointField
operator|.
name|decodeLongitude
argument_list|(
name|mortonCode
argument_list|)
expr_stmt|;
name|pt
index|[
literal|1
index|]
operator|=
name|GeoPointField
operator|.
name|decodeLatitude
argument_list|(
name|mortonCode
argument_list|)
expr_stmt|;
block|}
comment|// update the moving average
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|valueCount
condition|;
operator|++
name|i
control|)
block|{
name|GeoPoint
name|value
init|=
name|values
operator|.
name|valueAt
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|pt
index|[
literal|0
index|]
operator|=
name|pt
index|[
literal|0
index|]
operator|+
operator|(
name|value
operator|.
name|getLon
argument_list|()
operator|-
name|pt
index|[
literal|0
index|]
operator|)
operator|/
operator|++
name|prevCounts
expr_stmt|;
name|pt
index|[
literal|1
index|]
operator|=
name|pt
index|[
literal|1
index|]
operator|+
operator|(
name|value
operator|.
name|getLat
argument_list|()
operator|-
name|pt
index|[
literal|1
index|]
operator|)
operator|/
name|prevCounts
expr_stmt|;
block|}
comment|// TODO: we do not need to interleave the lat and lon bits here
comment|// should we just store them contiguously?
name|centroids
operator|.
name|set
argument_list|(
name|bucket
argument_list|,
name|GeoPointField
operator|.
name|encodeLatLon
argument_list|(
name|pt
index|[
literal|1
index|]
argument_list|,
name|pt
index|[
literal|0
index|]
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
name|bucket
parameter_list|)
block|{
if|if
condition|(
name|valuesSource
operator|==
literal|null
operator|||
name|bucket
operator|>=
name|centroids
operator|.
name|size
argument_list|()
condition|)
block|{
return|return
name|buildEmptyAggregation
argument_list|()
return|;
block|}
specifier|final
name|long
name|bucketCount
init|=
name|counts
operator|.
name|get
argument_list|(
name|bucket
argument_list|)
decl_stmt|;
specifier|final
name|long
name|mortonCode
init|=
name|centroids
operator|.
name|get
argument_list|(
name|bucket
argument_list|)
decl_stmt|;
specifier|final
name|GeoPoint
name|bucketCentroid
init|=
operator|(
name|bucketCount
operator|>
literal|0
operator|)
condition|?
operator|new
name|GeoPoint
argument_list|(
name|GeoPointField
operator|.
name|decodeLatitude
argument_list|(
name|mortonCode
argument_list|)
argument_list|,
name|GeoPointField
operator|.
name|decodeLongitude
argument_list|(
name|mortonCode
argument_list|)
argument_list|)
else|:
literal|null
decl_stmt|;
return|return
operator|new
name|InternalGeoCentroid
argument_list|(
name|name
argument_list|,
name|bucketCentroid
argument_list|,
name|bucketCount
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
return|return
operator|new
name|InternalGeoCentroid
argument_list|(
name|name
argument_list|,
literal|null
argument_list|,
literal|0L
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
DECL|method|doClose
specifier|public
name|void
name|doClose
parameter_list|()
block|{
name|Releasables
operator|.
name|close
argument_list|(
name|centroids
argument_list|,
name|counts
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

