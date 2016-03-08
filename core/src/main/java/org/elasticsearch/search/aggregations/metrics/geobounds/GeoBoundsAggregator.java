begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.metrics.geobounds
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
name|geobounds
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
name|DoubleArray
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

begin_class
DECL|class|GeoBoundsAggregator
specifier|public
specifier|final
class|class
name|GeoBoundsAggregator
extends|extends
name|MetricsAggregator
block|{
DECL|field|WRAP_LONGITUDE_FIELD
specifier|static
specifier|final
name|ParseField
name|WRAP_LONGITUDE_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"wrap_longitude"
argument_list|)
decl_stmt|;
DECL|field|valuesSource
specifier|private
specifier|final
name|ValuesSource
operator|.
name|GeoPoint
name|valuesSource
decl_stmt|;
DECL|field|wrapLongitude
specifier|private
specifier|final
name|boolean
name|wrapLongitude
decl_stmt|;
DECL|field|tops
name|DoubleArray
name|tops
decl_stmt|;
DECL|field|bottoms
name|DoubleArray
name|bottoms
decl_stmt|;
DECL|field|posLefts
name|DoubleArray
name|posLefts
decl_stmt|;
DECL|field|posRights
name|DoubleArray
name|posRights
decl_stmt|;
DECL|field|negLefts
name|DoubleArray
name|negLefts
decl_stmt|;
DECL|field|negRights
name|DoubleArray
name|negRights
decl_stmt|;
DECL|method|GeoBoundsAggregator
specifier|protected
name|GeoBoundsAggregator
parameter_list|(
name|String
name|name
parameter_list|,
name|AggregationContext
name|aggregationContext
parameter_list|,
name|Aggregator
name|parent
parameter_list|,
name|ValuesSource
operator|.
name|GeoPoint
name|valuesSource
parameter_list|,
name|boolean
name|wrapLongitude
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
name|valuesSource
operator|=
name|valuesSource
expr_stmt|;
name|this
operator|.
name|wrapLongitude
operator|=
name|wrapLongitude
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
name|tops
operator|=
name|bigArrays
operator|.
name|newDoubleArray
argument_list|(
literal|1
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|tops
operator|.
name|fill
argument_list|(
literal|0
argument_list|,
name|tops
operator|.
name|size
argument_list|()
argument_list|,
name|Double
operator|.
name|NEGATIVE_INFINITY
argument_list|)
expr_stmt|;
name|bottoms
operator|=
name|bigArrays
operator|.
name|newDoubleArray
argument_list|(
literal|1
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|bottoms
operator|.
name|fill
argument_list|(
literal|0
argument_list|,
name|bottoms
operator|.
name|size
argument_list|()
argument_list|,
name|Double
operator|.
name|POSITIVE_INFINITY
argument_list|)
expr_stmt|;
name|posLefts
operator|=
name|bigArrays
operator|.
name|newDoubleArray
argument_list|(
literal|1
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|posLefts
operator|.
name|fill
argument_list|(
literal|0
argument_list|,
name|posLefts
operator|.
name|size
argument_list|()
argument_list|,
name|Double
operator|.
name|POSITIVE_INFINITY
argument_list|)
expr_stmt|;
name|posRights
operator|=
name|bigArrays
operator|.
name|newDoubleArray
argument_list|(
literal|1
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|posRights
operator|.
name|fill
argument_list|(
literal|0
argument_list|,
name|posRights
operator|.
name|size
argument_list|()
argument_list|,
name|Double
operator|.
name|NEGATIVE_INFINITY
argument_list|)
expr_stmt|;
name|negLefts
operator|=
name|bigArrays
operator|.
name|newDoubleArray
argument_list|(
literal|1
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|negLefts
operator|.
name|fill
argument_list|(
literal|0
argument_list|,
name|negLefts
operator|.
name|size
argument_list|()
argument_list|,
name|Double
operator|.
name|POSITIVE_INFINITY
argument_list|)
expr_stmt|;
name|negRights
operator|=
name|bigArrays
operator|.
name|newDoubleArray
argument_list|(
literal|1
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|negRights
operator|.
name|fill
argument_list|(
literal|0
argument_list|,
name|negRights
operator|.
name|size
argument_list|()
argument_list|,
name|Double
operator|.
name|NEGATIVE_INFINITY
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
if|if
condition|(
name|bucket
operator|>=
name|tops
operator|.
name|size
argument_list|()
condition|)
block|{
name|long
name|from
init|=
name|tops
operator|.
name|size
argument_list|()
decl_stmt|;
name|tops
operator|=
name|bigArrays
operator|.
name|grow
argument_list|(
name|tops
argument_list|,
name|bucket
operator|+
literal|1
argument_list|)
expr_stmt|;
name|tops
operator|.
name|fill
argument_list|(
name|from
argument_list|,
name|tops
operator|.
name|size
argument_list|()
argument_list|,
name|Double
operator|.
name|NEGATIVE_INFINITY
argument_list|)
expr_stmt|;
name|bottoms
operator|=
name|bigArrays
operator|.
name|resize
argument_list|(
name|bottoms
argument_list|,
name|tops
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|bottoms
operator|.
name|fill
argument_list|(
name|from
argument_list|,
name|bottoms
operator|.
name|size
argument_list|()
argument_list|,
name|Double
operator|.
name|POSITIVE_INFINITY
argument_list|)
expr_stmt|;
name|posLefts
operator|=
name|bigArrays
operator|.
name|resize
argument_list|(
name|posLefts
argument_list|,
name|tops
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|posLefts
operator|.
name|fill
argument_list|(
name|from
argument_list|,
name|posLefts
operator|.
name|size
argument_list|()
argument_list|,
name|Double
operator|.
name|POSITIVE_INFINITY
argument_list|)
expr_stmt|;
name|posRights
operator|=
name|bigArrays
operator|.
name|resize
argument_list|(
name|posRights
argument_list|,
name|tops
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|posRights
operator|.
name|fill
argument_list|(
name|from
argument_list|,
name|posRights
operator|.
name|size
argument_list|()
argument_list|,
name|Double
operator|.
name|NEGATIVE_INFINITY
argument_list|)
expr_stmt|;
name|negLefts
operator|=
name|bigArrays
operator|.
name|resize
argument_list|(
name|negLefts
argument_list|,
name|tops
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|negLefts
operator|.
name|fill
argument_list|(
name|from
argument_list|,
name|negLefts
operator|.
name|size
argument_list|()
argument_list|,
name|Double
operator|.
name|POSITIVE_INFINITY
argument_list|)
expr_stmt|;
name|negRights
operator|=
name|bigArrays
operator|.
name|resize
argument_list|(
name|negRights
argument_list|,
name|tops
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|negRights
operator|.
name|fill
argument_list|(
name|from
argument_list|,
name|negRights
operator|.
name|size
argument_list|()
argument_list|,
name|Double
operator|.
name|NEGATIVE_INFINITY
argument_list|)
expr_stmt|;
block|}
name|values
operator|.
name|setDocument
argument_list|(
name|doc
argument_list|)
expr_stmt|;
specifier|final
name|int
name|valuesCount
init|=
name|values
operator|.
name|count
argument_list|()
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
name|valuesCount
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
name|double
name|top
init|=
name|tops
operator|.
name|get
argument_list|(
name|bucket
argument_list|)
decl_stmt|;
if|if
condition|(
name|value
operator|.
name|lat
argument_list|()
operator|>
name|top
condition|)
block|{
name|top
operator|=
name|value
operator|.
name|lat
argument_list|()
expr_stmt|;
block|}
name|double
name|bottom
init|=
name|bottoms
operator|.
name|get
argument_list|(
name|bucket
argument_list|)
decl_stmt|;
if|if
condition|(
name|value
operator|.
name|lat
argument_list|()
operator|<
name|bottom
condition|)
block|{
name|bottom
operator|=
name|value
operator|.
name|lat
argument_list|()
expr_stmt|;
block|}
name|double
name|posLeft
init|=
name|posLefts
operator|.
name|get
argument_list|(
name|bucket
argument_list|)
decl_stmt|;
if|if
condition|(
name|value
operator|.
name|lon
argument_list|()
operator|>=
literal|0
operator|&&
name|value
operator|.
name|lon
argument_list|()
operator|<
name|posLeft
condition|)
block|{
name|posLeft
operator|=
name|value
operator|.
name|lon
argument_list|()
expr_stmt|;
block|}
name|double
name|posRight
init|=
name|posRights
operator|.
name|get
argument_list|(
name|bucket
argument_list|)
decl_stmt|;
if|if
condition|(
name|value
operator|.
name|lon
argument_list|()
operator|>=
literal|0
operator|&&
name|value
operator|.
name|lon
argument_list|()
operator|>
name|posRight
condition|)
block|{
name|posRight
operator|=
name|value
operator|.
name|lon
argument_list|()
expr_stmt|;
block|}
name|double
name|negLeft
init|=
name|negLefts
operator|.
name|get
argument_list|(
name|bucket
argument_list|)
decl_stmt|;
if|if
condition|(
name|value
operator|.
name|lon
argument_list|()
operator|<
literal|0
operator|&&
name|value
operator|.
name|lon
argument_list|()
operator|<
name|negLeft
condition|)
block|{
name|negLeft
operator|=
name|value
operator|.
name|lon
argument_list|()
expr_stmt|;
block|}
name|double
name|negRight
init|=
name|negRights
operator|.
name|get
argument_list|(
name|bucket
argument_list|)
decl_stmt|;
if|if
condition|(
name|value
operator|.
name|lon
argument_list|()
operator|<
literal|0
operator|&&
name|value
operator|.
name|lon
argument_list|()
operator|>
name|negRight
condition|)
block|{
name|negRight
operator|=
name|value
operator|.
name|lon
argument_list|()
expr_stmt|;
block|}
name|tops
operator|.
name|set
argument_list|(
name|bucket
argument_list|,
name|top
argument_list|)
expr_stmt|;
name|bottoms
operator|.
name|set
argument_list|(
name|bucket
argument_list|,
name|bottom
argument_list|)
expr_stmt|;
name|posLefts
operator|.
name|set
argument_list|(
name|bucket
argument_list|,
name|posLeft
argument_list|)
expr_stmt|;
name|posRights
operator|.
name|set
argument_list|(
name|bucket
argument_list|,
name|posRight
argument_list|)
expr_stmt|;
name|negLefts
operator|.
name|set
argument_list|(
name|bucket
argument_list|,
name|negLeft
argument_list|)
expr_stmt|;
name|negRights
operator|.
name|set
argument_list|(
name|bucket
argument_list|,
name|negRight
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
block|{
if|if
condition|(
name|valuesSource
operator|==
literal|null
condition|)
block|{
return|return
name|buildEmptyAggregation
argument_list|()
return|;
block|}
name|double
name|top
init|=
name|tops
operator|.
name|get
argument_list|(
name|owningBucketOrdinal
argument_list|)
decl_stmt|;
name|double
name|bottom
init|=
name|bottoms
operator|.
name|get
argument_list|(
name|owningBucketOrdinal
argument_list|)
decl_stmt|;
name|double
name|posLeft
init|=
name|posLefts
operator|.
name|get
argument_list|(
name|owningBucketOrdinal
argument_list|)
decl_stmt|;
name|double
name|posRight
init|=
name|posRights
operator|.
name|get
argument_list|(
name|owningBucketOrdinal
argument_list|)
decl_stmt|;
name|double
name|negLeft
init|=
name|negLefts
operator|.
name|get
argument_list|(
name|owningBucketOrdinal
argument_list|)
decl_stmt|;
name|double
name|negRight
init|=
name|negRights
operator|.
name|get
argument_list|(
name|owningBucketOrdinal
argument_list|)
decl_stmt|;
return|return
operator|new
name|InternalGeoBounds
argument_list|(
name|name
argument_list|,
name|top
argument_list|,
name|bottom
argument_list|,
name|posLeft
argument_list|,
name|posRight
argument_list|,
name|negLeft
argument_list|,
name|negRight
argument_list|,
name|wrapLongitude
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
name|InternalGeoBounds
argument_list|(
name|name
argument_list|,
name|Double
operator|.
name|NEGATIVE_INFINITY
argument_list|,
name|Double
operator|.
name|POSITIVE_INFINITY
argument_list|,
name|Double
operator|.
name|POSITIVE_INFINITY
argument_list|,
name|Double
operator|.
name|NEGATIVE_INFINITY
argument_list|,
name|Double
operator|.
name|POSITIVE_INFINITY
argument_list|,
name|Double
operator|.
name|NEGATIVE_INFINITY
argument_list|,
name|wrapLongitude
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
name|tops
argument_list|,
name|bottoms
argument_list|,
name|posLefts
argument_list|,
name|posRights
argument_list|,
name|negLefts
argument_list|,
name|negRights
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

