begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.bucket.range.geodistance
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
name|range
operator|.
name|geodistance
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
name|index
operator|.
name|SortedNumericDocValues
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
name|GeoDistance
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
name|geo
operator|.
name|GeoDistance
operator|.
name|FixedSourceDistance
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
name|unit
operator|.
name|DistanceUnit
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
name|index
operator|.
name|fielddata
operator|.
name|SortedBinaryDocValues
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
name|SortedNumericDoubleValues
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
name|InternalAggregation
operator|.
name|Type
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
name|range
operator|.
name|InternalRange
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
name|range
operator|.
name|RangeAggregator
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
name|range
operator|.
name|RangeAggregator
operator|.
name|Unmapped
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
name|range
operator|.
name|geodistance
operator|.
name|GeoDistanceParser
operator|.
name|Range
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
name|ValuesSourceAggregatorFactory
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
name|ValuesSourceConfig
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
DECL|class|GeoDistanceRangeAggregatorFactory
specifier|public
class|class
name|GeoDistanceRangeAggregatorFactory
extends|extends
name|ValuesSourceAggregatorFactory
argument_list|<
name|ValuesSource
operator|.
name|GeoPoint
argument_list|,
name|GeoDistanceRangeAggregatorFactory
argument_list|>
block|{
DECL|field|rangeFactory
specifier|private
specifier|final
name|InternalRange
operator|.
name|Factory
argument_list|<
name|InternalGeoDistance
operator|.
name|Bucket
argument_list|,
name|InternalGeoDistance
argument_list|>
name|rangeFactory
init|=
name|InternalGeoDistance
operator|.
name|FACTORY
decl_stmt|;
DECL|field|origin
specifier|private
specifier|final
name|GeoPoint
name|origin
decl_stmt|;
DECL|field|ranges
specifier|private
specifier|final
name|List
argument_list|<
name|Range
argument_list|>
name|ranges
decl_stmt|;
DECL|field|unit
specifier|private
specifier|final
name|DistanceUnit
name|unit
decl_stmt|;
DECL|field|distanceType
specifier|private
specifier|final
name|GeoDistance
name|distanceType
decl_stmt|;
DECL|field|keyed
specifier|private
specifier|final
name|boolean
name|keyed
decl_stmt|;
DECL|method|GeoDistanceRangeAggregatorFactory
specifier|public
name|GeoDistanceRangeAggregatorFactory
parameter_list|(
name|String
name|name
parameter_list|,
name|Type
name|type
parameter_list|,
name|ValuesSourceConfig
argument_list|<
name|ValuesSource
operator|.
name|GeoPoint
argument_list|>
name|config
parameter_list|,
name|GeoPoint
name|origin
parameter_list|,
name|List
argument_list|<
name|Range
argument_list|>
name|ranges
parameter_list|,
name|DistanceUnit
name|unit
parameter_list|,
name|GeoDistance
name|distanceType
parameter_list|,
name|boolean
name|keyed
parameter_list|,
name|AggregationContext
name|context
parameter_list|,
name|AggregatorFactory
argument_list|<
name|?
argument_list|>
name|parent
parameter_list|,
name|AggregatorFactories
operator|.
name|Builder
name|subFactoriesBuilder
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
name|type
argument_list|,
name|config
argument_list|,
name|context
argument_list|,
name|parent
argument_list|,
name|subFactoriesBuilder
argument_list|,
name|metaData
argument_list|)
expr_stmt|;
name|this
operator|.
name|origin
operator|=
name|origin
expr_stmt|;
name|this
operator|.
name|ranges
operator|=
name|ranges
expr_stmt|;
name|this
operator|.
name|unit
operator|=
name|unit
expr_stmt|;
name|this
operator|.
name|distanceType
operator|=
name|distanceType
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
DECL|method|createUnmapped
specifier|protected
name|Aggregator
name|createUnmapped
parameter_list|(
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
return|return
operator|new
name|Unmapped
argument_list|<
name|Range
argument_list|>
argument_list|(
name|name
argument_list|,
name|ranges
argument_list|,
name|keyed
argument_list|,
name|config
operator|.
name|format
argument_list|()
argument_list|,
name|context
argument_list|,
name|parent
argument_list|,
name|rangeFactory
argument_list|,
name|pipelineAggregators
argument_list|,
name|metaData
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|doCreateInternal
specifier|protected
name|Aggregator
name|doCreateInternal
parameter_list|(
specifier|final
name|ValuesSource
operator|.
name|GeoPoint
name|valuesSource
parameter_list|,
name|Aggregator
name|parent
parameter_list|,
name|boolean
name|collectsFromSingleBucket
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
name|DistanceSource
name|distanceSource
init|=
operator|new
name|DistanceSource
argument_list|(
name|valuesSource
argument_list|,
name|distanceType
argument_list|,
name|origin
argument_list|,
name|unit
argument_list|)
decl_stmt|;
return|return
operator|new
name|RangeAggregator
argument_list|(
name|name
argument_list|,
name|factories
argument_list|,
name|distanceSource
argument_list|,
name|config
operator|.
name|format
argument_list|()
argument_list|,
name|rangeFactory
argument_list|,
name|ranges
argument_list|,
name|keyed
argument_list|,
name|context
argument_list|,
name|parent
argument_list|,
name|pipelineAggregators
argument_list|,
name|metaData
argument_list|)
return|;
block|}
DECL|class|DistanceSource
specifier|private
specifier|static
class|class
name|DistanceSource
extends|extends
name|ValuesSource
operator|.
name|Numeric
block|{
DECL|field|source
specifier|private
specifier|final
name|ValuesSource
operator|.
name|GeoPoint
name|source
decl_stmt|;
DECL|field|distanceType
specifier|private
specifier|final
name|GeoDistance
name|distanceType
decl_stmt|;
DECL|field|unit
specifier|private
specifier|final
name|DistanceUnit
name|unit
decl_stmt|;
DECL|field|origin
specifier|private
specifier|final
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|geo
operator|.
name|GeoPoint
name|origin
decl_stmt|;
DECL|method|DistanceSource
specifier|public
name|DistanceSource
parameter_list|(
name|ValuesSource
operator|.
name|GeoPoint
name|source
parameter_list|,
name|GeoDistance
name|distanceType
parameter_list|,
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|geo
operator|.
name|GeoPoint
name|origin
parameter_list|,
name|DistanceUnit
name|unit
parameter_list|)
block|{
name|this
operator|.
name|source
operator|=
name|source
expr_stmt|;
comment|// even if the geo points are unique, there's no guarantee the
comment|// distances are
name|this
operator|.
name|distanceType
operator|=
name|distanceType
expr_stmt|;
name|this
operator|.
name|unit
operator|=
name|unit
expr_stmt|;
name|this
operator|.
name|origin
operator|=
name|origin
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|isFloatingPoint
specifier|public
name|boolean
name|isFloatingPoint
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
DECL|method|longValues
specifier|public
name|SortedNumericDocValues
name|longValues
parameter_list|(
name|LeafReaderContext
name|ctx
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
DECL|method|doubleValues
specifier|public
name|SortedNumericDoubleValues
name|doubleValues
parameter_list|(
name|LeafReaderContext
name|ctx
parameter_list|)
block|{
specifier|final
name|MultiGeoPointValues
name|geoValues
init|=
name|source
operator|.
name|geoPointValues
argument_list|(
name|ctx
argument_list|)
decl_stmt|;
specifier|final
name|FixedSourceDistance
name|distance
init|=
name|distanceType
operator|.
name|fixedSourceDistance
argument_list|(
name|origin
operator|.
name|getLat
argument_list|()
argument_list|,
name|origin
operator|.
name|getLon
argument_list|()
argument_list|,
name|unit
argument_list|)
decl_stmt|;
return|return
name|GeoDistance
operator|.
name|distanceValues
argument_list|(
name|geoValues
argument_list|,
name|distance
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|bytesValues
specifier|public
name|SortedBinaryDocValues
name|bytesValues
parameter_list|(
name|LeafReaderContext
name|ctx
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
block|}
block|}
end_class

end_unit

