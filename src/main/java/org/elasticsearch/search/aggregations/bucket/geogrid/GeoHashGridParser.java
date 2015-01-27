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
name|index
operator|.
name|fielddata
operator|.
name|SortingNumericDocValues
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
name|GeoBoundingBoxFilterBuilder
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
name|AggregatorBase
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
name|NonCollectingAggregator
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
name|BucketUtils
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
name|ValuesSourceParser
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
name|Collections
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
comment|/**  * Aggregates Geo information into cells determined by geohashes of a given precision.  * WARNING - for high-precision geohashes it may prove necessary to use a {@link GeoBoundingBoxFilterBuilder}  * aggregation to focus in on a smaller area to avoid generating too many buckets and using too much RAM  */
end_comment

begin_class
DECL|class|GeoHashGridParser
specifier|public
class|class
name|GeoHashGridParser
implements|implements
name|Aggregator
operator|.
name|Parser
block|{
annotation|@
name|Override
DECL|method|type
specifier|public
name|String
name|type
parameter_list|()
block|{
return|return
name|InternalGeoHashGrid
operator|.
name|TYPE
operator|.
name|name
argument_list|()
return|;
block|}
DECL|field|DEFAULT_PRECISION
specifier|public
specifier|static
specifier|final
name|int
name|DEFAULT_PRECISION
init|=
literal|5
decl_stmt|;
DECL|field|DEFAULT_MAX_NUM_CELLS
specifier|public
specifier|static
specifier|final
name|int
name|DEFAULT_MAX_NUM_CELLS
init|=
literal|10000
decl_stmt|;
annotation|@
name|Override
DECL|method|parse
specifier|public
name|AggregatorFactory
name|parse
parameter_list|(
name|String
name|aggregationName
parameter_list|,
name|XContentParser
name|parser
parameter_list|,
name|SearchContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|ValuesSourceParser
name|vsParser
init|=
name|ValuesSourceParser
operator|.
name|geoPoint
argument_list|(
name|aggregationName
argument_list|,
name|InternalGeoHashGrid
operator|.
name|TYPE
argument_list|,
name|context
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|int
name|precision
init|=
name|DEFAULT_PRECISION
decl_stmt|;
name|int
name|requiredSize
init|=
name|DEFAULT_MAX_NUM_CELLS
decl_stmt|;
name|int
name|shardSize
init|=
operator|-
literal|1
decl_stmt|;
name|XContentParser
operator|.
name|Token
name|token
decl_stmt|;
name|String
name|currentFieldName
init|=
literal|null
decl_stmt|;
while|while
condition|(
operator|(
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
operator|)
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|END_OBJECT
condition|)
block|{
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|FIELD_NAME
condition|)
block|{
name|currentFieldName
operator|=
name|parser
operator|.
name|currentName
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|vsParser
operator|.
name|token
argument_list|(
name|currentFieldName
argument_list|,
name|token
argument_list|,
name|parser
argument_list|)
condition|)
block|{
continue|continue;
block|}
elseif|else
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|VALUE_NUMBER
condition|)
block|{
if|if
condition|(
literal|"precision"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|precision
operator|=
name|parser
operator|.
name|intValue
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"size"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|requiredSize
operator|=
name|parser
operator|.
name|intValue
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"shard_size"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
operator|||
literal|"shardSize"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|shardSize
operator|=
name|parser
operator|.
name|intValue
argument_list|()
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|shardSize
operator|==
literal|0
condition|)
block|{
name|shardSize
operator|=
name|Integer
operator|.
name|MAX_VALUE
expr_stmt|;
block|}
if|if
condition|(
name|requiredSize
operator|==
literal|0
condition|)
block|{
name|requiredSize
operator|=
name|Integer
operator|.
name|MAX_VALUE
expr_stmt|;
block|}
if|if
condition|(
name|shardSize
operator|<
literal|0
condition|)
block|{
comment|//Use default heuristic to avoid any wrong-ranking caused by distributed counting
name|shardSize
operator|=
name|BucketUtils
operator|.
name|suggestShardSideQueueSize
argument_list|(
name|requiredSize
argument_list|,
name|context
operator|.
name|numberOfShards
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|shardSize
operator|<
name|requiredSize
condition|)
block|{
name|shardSize
operator|=
name|requiredSize
expr_stmt|;
block|}
return|return
operator|new
name|GeoGridFactory
argument_list|(
name|aggregationName
argument_list|,
name|vsParser
operator|.
name|config
argument_list|()
argument_list|,
name|precision
argument_list|,
name|requiredSize
argument_list|,
name|shardSize
argument_list|)
return|;
block|}
DECL|class|GeoGridFactory
specifier|private
specifier|static
class|class
name|GeoGridFactory
extends|extends
name|ValuesSourceAggregatorFactory
argument_list|<
name|ValuesSource
operator|.
name|GeoPoint
argument_list|>
block|{
DECL|field|precision
specifier|private
name|int
name|precision
decl_stmt|;
DECL|field|requiredSize
specifier|private
name|int
name|requiredSize
decl_stmt|;
DECL|field|shardSize
specifier|private
name|int
name|shardSize
decl_stmt|;
DECL|method|GeoGridFactory
specifier|public
name|GeoGridFactory
parameter_list|(
name|String
name|name
parameter_list|,
name|ValuesSourceConfig
argument_list|<
name|ValuesSource
operator|.
name|GeoPoint
argument_list|>
name|config
parameter_list|,
name|int
name|precision
parameter_list|,
name|int
name|requiredSize
parameter_list|,
name|int
name|shardSize
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|,
name|InternalGeoHashGrid
operator|.
name|TYPE
operator|.
name|name
argument_list|()
argument_list|,
name|config
argument_list|)
expr_stmt|;
name|this
operator|.
name|precision
operator|=
name|precision
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
block|}
annotation|@
name|Override
DECL|method|createUnmapped
specifier|protected
name|Aggregator
name|createUnmapped
parameter_list|(
name|AggregationContext
name|aggregationContext
parameter_list|,
name|Aggregator
name|parent
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
specifier|final
name|InternalAggregation
name|aggregation
init|=
operator|new
name|InternalGeoHashGrid
argument_list|(
name|name
argument_list|,
name|requiredSize
argument_list|,
name|Collections
operator|.
expr|<
name|InternalGeoHashGrid
operator|.
name|Bucket
operator|>
name|emptyList
argument_list|()
argument_list|,
name|metaData
argument_list|)
decl_stmt|;
return|return
operator|new
name|NonCollectingAggregator
argument_list|(
name|name
argument_list|,
name|aggregationContext
argument_list|,
name|parent
argument_list|,
name|metaData
argument_list|)
block|{
specifier|public
name|InternalAggregation
name|buildEmptyAggregation
parameter_list|()
block|{
return|return
name|aggregation
return|;
block|}
block|}
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
name|AggregationContext
name|aggregationContext
parameter_list|,
name|Aggregator
name|parent
parameter_list|,
name|boolean
name|collectsFromSingleBucket
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
if|if
condition|(
name|collectsFromSingleBucket
operator|==
literal|false
condition|)
block|{
return|return
name|asMultiBucketAggregator
argument_list|(
name|this
argument_list|,
name|aggregationContext
argument_list|,
name|parent
argument_list|)
return|;
block|}
name|ValuesSource
operator|.
name|Numeric
name|cellIdSource
init|=
operator|new
name|CellIdSource
argument_list|(
name|valuesSource
argument_list|,
name|precision
argument_list|)
decl_stmt|;
return|return
operator|new
name|GeoHashGridAggregator
argument_list|(
name|name
argument_list|,
name|factories
argument_list|,
name|cellIdSource
argument_list|,
name|requiredSize
argument_list|,
name|shardSize
argument_list|,
name|aggregationContext
argument_list|,
name|parent
argument_list|,
name|metaData
argument_list|)
return|;
block|}
DECL|class|CellValues
specifier|private
specifier|static
class|class
name|CellValues
extends|extends
name|SortingNumericDocValues
block|{
DECL|field|geoValues
specifier|private
name|MultiGeoPointValues
name|geoValues
decl_stmt|;
DECL|field|precision
specifier|private
name|int
name|precision
decl_stmt|;
DECL|method|CellValues
specifier|protected
name|CellValues
parameter_list|(
name|MultiGeoPointValues
name|geoValues
parameter_list|,
name|int
name|precision
parameter_list|)
block|{
name|this
operator|.
name|geoValues
operator|=
name|geoValues
expr_stmt|;
name|this
operator|.
name|precision
operator|=
name|precision
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|setDocument
specifier|public
name|void
name|setDocument
parameter_list|(
name|int
name|docId
parameter_list|)
block|{
name|geoValues
operator|.
name|setDocument
argument_list|(
name|docId
argument_list|)
expr_stmt|;
name|resize
argument_list|(
name|geoValues
operator|.
name|count
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|count
argument_list|()
condition|;
operator|++
name|i
control|)
block|{
name|GeoPoint
name|target
init|=
name|geoValues
operator|.
name|valueAt
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|values
index|[
name|i
index|]
operator|=
name|GeoHashUtils
operator|.
name|encodeAsLong
argument_list|(
name|target
operator|.
name|getLat
argument_list|()
argument_list|,
name|target
operator|.
name|getLon
argument_list|()
argument_list|,
name|precision
argument_list|)
expr_stmt|;
block|}
name|sort
argument_list|()
expr_stmt|;
block|}
block|}
DECL|class|CellIdSource
specifier|private
specifier|static
class|class
name|CellIdSource
extends|extends
name|ValuesSource
operator|.
name|Numeric
block|{
DECL|field|valuesSource
specifier|private
specifier|final
name|ValuesSource
operator|.
name|GeoPoint
name|valuesSource
decl_stmt|;
DECL|field|precision
specifier|private
specifier|final
name|int
name|precision
decl_stmt|;
DECL|method|CellIdSource
specifier|public
name|CellIdSource
parameter_list|(
name|ValuesSource
operator|.
name|GeoPoint
name|valuesSource
parameter_list|,
name|int
name|precision
parameter_list|)
block|{
name|this
operator|.
name|valuesSource
operator|=
name|valuesSource
expr_stmt|;
comment|//different GeoPoints could map to the same or different geohash cells.
name|this
operator|.
name|precision
operator|=
name|precision
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
literal|false
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
return|return
operator|new
name|CellValues
argument_list|(
name|valuesSource
operator|.
name|geoPointValues
argument_list|(
name|ctx
argument_list|)
argument_list|,
name|precision
argument_list|)
return|;
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
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
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
block|}
end_class

end_unit

