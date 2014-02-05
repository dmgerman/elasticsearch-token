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
name|*
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
name|mapper
operator|.
name|FieldMapper
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
name|*
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
name|geopoints
operator|.
name|GeoPointValuesSource
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
name|NumericValuesSource
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
name|String
name|field
init|=
literal|null
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
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|VALUE_STRING
condition|)
block|{
if|if
condition|(
literal|"field"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|field
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
block|}
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
name|ValuesSourceConfig
argument_list|<
name|GeoPointValuesSource
argument_list|>
name|config
init|=
operator|new
name|ValuesSourceConfig
argument_list|<
name|GeoPointValuesSource
argument_list|>
argument_list|(
name|GeoPointValuesSource
operator|.
name|class
argument_list|)
decl_stmt|;
if|if
condition|(
name|field
operator|==
literal|null
condition|)
block|{
return|return
operator|new
name|GeoGridFactory
argument_list|(
name|aggregationName
argument_list|,
name|config
argument_list|,
name|precision
argument_list|,
name|requiredSize
argument_list|,
name|shardSize
argument_list|)
return|;
block|}
name|FieldMapper
argument_list|<
name|?
argument_list|>
name|mapper
init|=
name|context
operator|.
name|smartNameFieldMapper
argument_list|(
name|field
argument_list|)
decl_stmt|;
if|if
condition|(
name|mapper
operator|==
literal|null
condition|)
block|{
name|config
operator|.
name|unmapped
argument_list|(
literal|true
argument_list|)
expr_stmt|;
return|return
operator|new
name|GeoGridFactory
argument_list|(
name|aggregationName
argument_list|,
name|config
argument_list|,
name|precision
argument_list|,
name|requiredSize
argument_list|,
name|shardSize
argument_list|)
return|;
block|}
name|IndexFieldData
argument_list|<
name|?
argument_list|>
name|indexFieldData
init|=
name|context
operator|.
name|fieldData
argument_list|()
operator|.
name|getForField
argument_list|(
name|mapper
argument_list|)
decl_stmt|;
name|config
operator|.
name|fieldContext
argument_list|(
operator|new
name|FieldContext
argument_list|(
name|field
argument_list|,
name|indexFieldData
argument_list|)
argument_list|)
expr_stmt|;
return|return
operator|new
name|GeoGridFactory
argument_list|(
name|aggregationName
argument_list|,
name|config
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
name|ValueSourceAggregatorFactory
argument_list|<
name|GeoPointValuesSource
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
name|GeoPointValuesSource
argument_list|>
name|valueSourceConfig
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
name|valueSourceConfig
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
parameter_list|)
block|{
return|return
operator|new
name|GeoHashGridAggregator
operator|.
name|Unmapped
argument_list|(
name|name
argument_list|,
name|requiredSize
argument_list|,
name|aggregationContext
argument_list|,
name|parent
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|create
specifier|protected
name|Aggregator
name|create
parameter_list|(
specifier|final
name|GeoPointValuesSource
name|valuesSource
parameter_list|,
name|long
name|expectedBucketsCount
parameter_list|,
name|AggregationContext
name|aggregationContext
parameter_list|,
name|Aggregator
name|parent
parameter_list|)
block|{
specifier|final
name|CellValues
name|cellIdValues
init|=
operator|new
name|CellValues
argument_list|(
name|valuesSource
argument_list|,
name|precision
argument_list|)
decl_stmt|;
name|FieldDataSource
operator|.
name|Numeric
name|cellIdSource
init|=
operator|new
name|CellIdSource
argument_list|(
name|cellIdValues
argument_list|,
name|valuesSource
operator|.
name|metaData
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|cellIdSource
operator|.
name|metaData
argument_list|()
operator|.
name|multiValued
argument_list|()
condition|)
block|{
comment|// we need to wrap to ensure uniqueness
name|cellIdSource
operator|=
operator|new
name|FieldDataSource
operator|.
name|Numeric
operator|.
name|SortedAndUnique
argument_list|(
name|cellIdSource
argument_list|)
expr_stmt|;
block|}
specifier|final
name|NumericValuesSource
name|geohashIdSource
init|=
operator|new
name|NumericValuesSource
argument_list|(
name|cellIdSource
argument_list|,
literal|null
argument_list|,
literal|null
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
name|geohashIdSource
argument_list|,
name|requiredSize
argument_list|,
name|shardSize
argument_list|,
name|aggregationContext
argument_list|,
name|parent
argument_list|)
return|;
block|}
DECL|class|CellValues
specifier|private
specifier|static
class|class
name|CellValues
extends|extends
name|LongValues
block|{
DECL|field|geoPointValues
specifier|private
name|GeoPointValuesSource
name|geoPointValues
decl_stmt|;
DECL|field|geoValues
specifier|private
name|GeoPointValues
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
name|GeoPointValuesSource
name|geoPointValues
parameter_list|,
name|int
name|precision
parameter_list|)
block|{
name|super
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|this
operator|.
name|geoPointValues
operator|=
name|geoPointValues
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
name|int
name|setDocument
parameter_list|(
name|int
name|docId
parameter_list|)
block|{
name|geoValues
operator|=
name|geoPointValues
operator|.
name|values
argument_list|()
expr_stmt|;
return|return
name|geoValues
operator|.
name|setDocument
argument_list|(
name|docId
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|nextValue
specifier|public
name|long
name|nextValue
parameter_list|()
block|{
name|GeoPoint
name|target
init|=
name|geoValues
operator|.
name|nextValue
argument_list|()
decl_stmt|;
return|return
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
return|;
block|}
block|}
DECL|class|CellIdSource
specifier|private
specifier|static
class|class
name|CellIdSource
extends|extends
name|FieldDataSource
operator|.
name|Numeric
block|{
DECL|field|values
specifier|private
specifier|final
name|LongValues
name|values
decl_stmt|;
DECL|field|metaData
specifier|private
name|MetaData
name|metaData
decl_stmt|;
DECL|method|CellIdSource
specifier|public
name|CellIdSource
parameter_list|(
name|LongValues
name|values
parameter_list|,
name|MetaData
name|delegate
parameter_list|)
block|{
name|this
operator|.
name|values
operator|=
name|values
expr_stmt|;
comment|//different GeoPoints could map to the same or different geohash cells.
name|this
operator|.
name|metaData
operator|=
name|MetaData
operator|.
name|builder
argument_list|(
name|delegate
argument_list|)
operator|.
name|uniqueness
argument_list|(
name|MetaData
operator|.
name|Uniqueness
operator|.
name|UNKNOWN
argument_list|)
operator|.
name|build
argument_list|()
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
name|LongValues
name|longValues
parameter_list|()
block|{
return|return
name|values
return|;
block|}
annotation|@
name|Override
DECL|method|doubleValues
specifier|public
name|DoubleValues
name|doubleValues
parameter_list|()
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
name|BytesValues
name|bytesValues
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
DECL|method|metaData
specifier|public
name|MetaData
name|metaData
parameter_list|()
block|{
return|return
name|metaData
return|;
block|}
block|}
block|}
block|}
end_class

end_unit

