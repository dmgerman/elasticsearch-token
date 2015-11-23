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
name|search
operator|.
name|aggregations
operator|.
name|AggregationBuilder
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
name|builder
operator|.
name|SearchSourceBuilderException
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
name|Locale
import|;
end_import

begin_comment
comment|/**  * Builder for the {@link GeoDistance} aggregation.  */
end_comment

begin_class
DECL|class|GeoDistanceBuilder
specifier|public
class|class
name|GeoDistanceBuilder
extends|extends
name|AggregationBuilder
argument_list|<
name|GeoDistanceBuilder
argument_list|>
block|{
comment|/**      * A range of values.      */
DECL|class|Range
specifier|public
specifier|static
class|class
name|Range
implements|implements
name|ToXContent
block|{
DECL|field|key
specifier|private
name|String
name|key
decl_stmt|;
DECL|field|from
specifier|private
name|Double
name|from
decl_stmt|;
DECL|field|to
specifier|private
name|Double
name|to
decl_stmt|;
comment|/**          * Create a new range.          * @param key   the identifier of this range          * @param from  the lower bound (inclusive)          * @param to    the upper bound (exclusive)          */
DECL|method|Range
specifier|public
name|Range
parameter_list|(
name|String
name|key
parameter_list|,
name|Double
name|from
parameter_list|,
name|Double
name|to
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
name|from
operator|=
name|from
expr_stmt|;
name|this
operator|.
name|to
operator|=
name|to
expr_stmt|;
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
argument_list|()
expr_stmt|;
if|if
condition|(
name|from
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"from"
argument_list|,
name|from
operator|.
name|doubleValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|to
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"to"
argument_list|,
name|to
operator|.
name|doubleValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|key
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"key"
argument_list|,
name|key
argument_list|)
expr_stmt|;
block|}
return|return
name|builder
operator|.
name|endObject
argument_list|()
return|;
block|}
block|}
DECL|field|field
specifier|private
name|String
name|field
decl_stmt|;
DECL|field|unit
specifier|private
name|DistanceUnit
name|unit
decl_stmt|;
DECL|field|distanceType
specifier|private
name|GeoDistance
name|distanceType
decl_stmt|;
DECL|field|point
specifier|private
name|GeoPoint
name|point
decl_stmt|;
DECL|field|ranges
specifier|private
name|List
argument_list|<
name|Range
argument_list|>
name|ranges
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
comment|/**      * Sole constructor.      */
DECL|method|GeoDistanceBuilder
specifier|public
name|GeoDistanceBuilder
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|,
name|InternalGeoDistance
operator|.
name|TYPE
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**      * Set the field to use to compute distances.      */
DECL|method|field
specifier|public
name|GeoDistanceBuilder
name|field
parameter_list|(
name|String
name|field
parameter_list|)
block|{
name|this
operator|.
name|field
operator|=
name|field
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Set the unit to use for distances, default is kilometers.      */
DECL|method|unit
specifier|public
name|GeoDistanceBuilder
name|unit
parameter_list|(
name|DistanceUnit
name|unit
parameter_list|)
block|{
name|this
operator|.
name|unit
operator|=
name|unit
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Set the {@link GeoDistance distance type} to use, defaults to      * {@link GeoDistance#SLOPPY_ARC}.      */
DECL|method|distanceType
specifier|public
name|GeoDistanceBuilder
name|distanceType
parameter_list|(
name|GeoDistance
name|distanceType
parameter_list|)
block|{
name|this
operator|.
name|distanceType
operator|=
name|distanceType
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Set the point to calculate distances from using a      *<code>lat,lon</code> notation or geohash.      */
DECL|method|point
specifier|public
name|GeoDistanceBuilder
name|point
parameter_list|(
name|String
name|latLon
parameter_list|)
block|{
return|return
name|point
argument_list|(
name|GeoPoint
operator|.
name|parseFromLatLon
argument_list|(
name|latLon
argument_list|)
argument_list|)
return|;
block|}
comment|/**      * Set the point to calculate distances from.      */
DECL|method|point
specifier|public
name|GeoDistanceBuilder
name|point
parameter_list|(
name|GeoPoint
name|point
parameter_list|)
block|{
name|this
operator|.
name|point
operator|=
name|point
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Set the point to calculate distances from using its geohash.      */
DECL|method|geohash
specifier|public
name|GeoDistanceBuilder
name|geohash
parameter_list|(
name|String
name|geohash
parameter_list|)
block|{
if|if
condition|(
name|this
operator|.
name|point
operator|==
literal|null
condition|)
block|{
name|this
operator|.
name|point
operator|=
operator|new
name|GeoPoint
argument_list|()
expr_stmt|;
block|}
name|this
operator|.
name|point
operator|.
name|resetFromGeoHash
argument_list|(
name|geohash
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Set the latitude of the point to calculate distances from.      */
DECL|method|lat
specifier|public
name|GeoDistanceBuilder
name|lat
parameter_list|(
name|double
name|lat
parameter_list|)
block|{
if|if
condition|(
name|this
operator|.
name|point
operator|==
literal|null
condition|)
block|{
name|point
operator|=
operator|new
name|GeoPoint
argument_list|()
expr_stmt|;
block|}
name|point
operator|.
name|resetLat
argument_list|(
name|lat
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Set the longitude of the point to calculate distances from.      */
DECL|method|lon
specifier|public
name|GeoDistanceBuilder
name|lon
parameter_list|(
name|double
name|lon
parameter_list|)
block|{
if|if
condition|(
name|this
operator|.
name|point
operator|==
literal|null
condition|)
block|{
name|point
operator|=
operator|new
name|GeoPoint
argument_list|()
expr_stmt|;
block|}
name|point
operator|.
name|resetLon
argument_list|(
name|lon
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Add a new range to this aggregation.      *      * @param key  the key to use for this range in the response      * @param from the lower bound on the distances, inclusive      * @param to   the upper bound on the distances, exclusive      */
DECL|method|addRange
specifier|public
name|GeoDistanceBuilder
name|addRange
parameter_list|(
name|String
name|key
parameter_list|,
name|double
name|from
parameter_list|,
name|double
name|to
parameter_list|)
block|{
name|ranges
operator|.
name|add
argument_list|(
operator|new
name|Range
argument_list|(
name|key
argument_list|,
name|from
argument_list|,
name|to
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Same as {@link #addRange(String, double, double)} but the key will be      * automatically generated based on<code>from</code> and<code>to</code>.      */
DECL|method|addRange
specifier|public
name|GeoDistanceBuilder
name|addRange
parameter_list|(
name|double
name|from
parameter_list|,
name|double
name|to
parameter_list|)
block|{
return|return
name|addRange
argument_list|(
literal|null
argument_list|,
name|from
argument_list|,
name|to
argument_list|)
return|;
block|}
comment|/**      * Add a new range with no lower bound.      *      * @param key the key to use for this range in the response      * @param to  the upper bound on the distances, exclusive      */
DECL|method|addUnboundedTo
specifier|public
name|GeoDistanceBuilder
name|addUnboundedTo
parameter_list|(
name|String
name|key
parameter_list|,
name|double
name|to
parameter_list|)
block|{
name|ranges
operator|.
name|add
argument_list|(
operator|new
name|Range
argument_list|(
name|key
argument_list|,
literal|null
argument_list|,
name|to
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Same as {@link #addUnboundedTo(String, double)} but the key will be      * computed automatically.      */
DECL|method|addUnboundedTo
specifier|public
name|GeoDistanceBuilder
name|addUnboundedTo
parameter_list|(
name|double
name|to
parameter_list|)
block|{
return|return
name|addUnboundedTo
argument_list|(
literal|null
argument_list|,
name|to
argument_list|)
return|;
block|}
comment|/**      * Add a new range with no upper bound.      *      * @param key  the key to use for this range in the response      * @param from the lower bound on the distances, inclusive      */
DECL|method|addUnboundedFrom
specifier|public
name|GeoDistanceBuilder
name|addUnboundedFrom
parameter_list|(
name|String
name|key
parameter_list|,
name|double
name|from
parameter_list|)
block|{
name|ranges
operator|.
name|add
argument_list|(
operator|new
name|Range
argument_list|(
name|key
argument_list|,
name|from
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Same as {@link #addUnboundedFrom(String, double)} but the key will be      * computed automatically.      */
DECL|method|addUnboundedFrom
specifier|public
name|GeoDistanceBuilder
name|addUnboundedFrom
parameter_list|(
name|double
name|from
parameter_list|)
block|{
return|return
name|addUnboundedFrom
argument_list|(
literal|null
argument_list|,
name|from
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
name|ranges
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|SearchSourceBuilderException
argument_list|(
literal|"at least one range must be defined for geo_distance aggregation ["
operator|+
name|getName
argument_list|()
operator|+
literal|"]"
argument_list|)
throw|;
block|}
if|if
condition|(
name|point
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|SearchSourceBuilderException
argument_list|(
literal|"center point must be defined for geo_distance aggregation ["
operator|+
name|getName
argument_list|()
operator|+
literal|"]"
argument_list|)
throw|;
block|}
if|if
condition|(
name|field
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"field"
argument_list|,
name|field
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|unit
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"unit"
argument_list|,
name|unit
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|distanceType
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"distance_type"
argument_list|,
name|distanceType
operator|.
name|name
argument_list|()
operator|.
name|toLowerCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|startObject
argument_list|(
literal|"center"
argument_list|)
operator|.
name|field
argument_list|(
literal|"lat"
argument_list|,
name|point
operator|.
name|lat
argument_list|()
argument_list|)
operator|.
name|field
argument_list|(
literal|"lon"
argument_list|,
name|point
operator|.
name|lon
argument_list|()
argument_list|)
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|startArray
argument_list|(
literal|"ranges"
argument_list|)
expr_stmt|;
for|for
control|(
name|Range
name|range
range|:
name|ranges
control|)
block|{
name|range
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endArray
argument_list|()
expr_stmt|;
return|return
name|builder
operator|.
name|endObject
argument_list|()
return|;
block|}
block|}
end_class

end_unit

