begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.facet.geodistance
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|facet
operator|.
name|geodistance
package|;
end_package

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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Maps
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
name|FilterBuilder
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
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|facet
operator|.
name|AbstractFacetBuilder
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
comment|/**  * A geo distance builder allowing to create a facet of distances from a specific location including the  * number of hits within each distance range, and aggregated data (like totals of either the distance or  * cusotm value fields).  */
end_comment

begin_class
DECL|class|GeoDistanceFacetBuilder
specifier|public
class|class
name|GeoDistanceFacetBuilder
extends|extends
name|AbstractFacetBuilder
block|{
DECL|field|fieldName
specifier|private
name|String
name|fieldName
decl_stmt|;
DECL|field|valueFieldName
specifier|private
name|String
name|valueFieldName
decl_stmt|;
DECL|field|lat
specifier|private
name|double
name|lat
decl_stmt|;
DECL|field|lon
specifier|private
name|double
name|lon
decl_stmt|;
DECL|field|geohash
specifier|private
name|String
name|geohash
decl_stmt|;
DECL|field|geoDistance
specifier|private
name|GeoDistance
name|geoDistance
decl_stmt|;
DECL|field|unit
specifier|private
name|DistanceUnit
name|unit
decl_stmt|;
DECL|field|params
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|params
decl_stmt|;
DECL|field|valueScript
specifier|private
name|String
name|valueScript
decl_stmt|;
DECL|field|lang
specifier|private
name|String
name|lang
decl_stmt|;
DECL|field|entries
specifier|private
name|List
argument_list|<
name|Entry
argument_list|>
name|entries
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
comment|/**      * Constructs a new geo distance with the provided facet name.      */
DECL|method|GeoDistanceFacetBuilder
specifier|public
name|GeoDistanceFacetBuilder
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|)
expr_stmt|;
block|}
comment|/**      * The geo point field that will be used to extract the document location(s).      */
DECL|method|field
specifier|public
name|GeoDistanceFacetBuilder
name|field
parameter_list|(
name|String
name|fieldName
parameter_list|)
block|{
name|this
operator|.
name|fieldName
operator|=
name|fieldName
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * A custom value field (numeric) that will be used to provide aggregated data for each facet (for example, total).      */
DECL|method|valueField
specifier|public
name|GeoDistanceFacetBuilder
name|valueField
parameter_list|(
name|String
name|valueFieldName
parameter_list|)
block|{
name|this
operator|.
name|valueFieldName
operator|=
name|valueFieldName
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * A custom value script (result is numeric) that will be used to provide aggregated data for each facet (for example, total).      */
DECL|method|valueScript
specifier|public
name|GeoDistanceFacetBuilder
name|valueScript
parameter_list|(
name|String
name|valueScript
parameter_list|)
block|{
name|this
operator|.
name|valueScript
operator|=
name|valueScript
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The language of the {@link #valueScript(String)} script.      */
DECL|method|lang
specifier|public
name|GeoDistanceFacetBuilder
name|lang
parameter_list|(
name|String
name|lang
parameter_list|)
block|{
name|this
operator|.
name|lang
operator|=
name|lang
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Parameters for {@link #valueScript(String)} to improve performance when executing the same script with different parameters.      */
DECL|method|scriptParam
specifier|public
name|GeoDistanceFacetBuilder
name|scriptParam
parameter_list|(
name|String
name|name
parameter_list|,
name|Object
name|value
parameter_list|)
block|{
if|if
condition|(
name|params
operator|==
literal|null
condition|)
block|{
name|params
operator|=
name|Maps
operator|.
name|newHashMap
argument_list|()
expr_stmt|;
block|}
name|params
operator|.
name|put
argument_list|(
name|name
argument_list|,
name|value
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The point to create the range distance facets from.      *      * @param lat latitude.      * @param lon longitude.      */
DECL|method|point
specifier|public
name|GeoDistanceFacetBuilder
name|point
parameter_list|(
name|double
name|lat
parameter_list|,
name|double
name|lon
parameter_list|)
block|{
name|this
operator|.
name|lat
operator|=
name|lat
expr_stmt|;
name|this
operator|.
name|lon
operator|=
name|lon
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The latitude to create the range distance facets from.      */
DECL|method|lat
specifier|public
name|GeoDistanceFacetBuilder
name|lat
parameter_list|(
name|double
name|lat
parameter_list|)
block|{
name|this
operator|.
name|lat
operator|=
name|lat
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The longitude to create the range distance facets from.      */
DECL|method|lon
specifier|public
name|GeoDistanceFacetBuilder
name|lon
parameter_list|(
name|double
name|lon
parameter_list|)
block|{
name|this
operator|.
name|lon
operator|=
name|lon
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The geohash of the geo point to create the range distance facets from.      */
DECL|method|geohash
specifier|public
name|GeoDistanceFacetBuilder
name|geohash
parameter_list|(
name|String
name|geohash
parameter_list|)
block|{
name|this
operator|.
name|geohash
operator|=
name|geohash
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The geo distance type used to compute the distance.      */
DECL|method|geoDistance
specifier|public
name|GeoDistanceFacetBuilder
name|geoDistance
parameter_list|(
name|GeoDistance
name|geoDistance
parameter_list|)
block|{
name|this
operator|.
name|geoDistance
operator|=
name|geoDistance
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Adds a range entry with explicit from and to.      *      * @param from The from distance limit      * @param to   The to distance limit      */
DECL|method|addRange
specifier|public
name|GeoDistanceFacetBuilder
name|addRange
parameter_list|(
name|double
name|from
parameter_list|,
name|double
name|to
parameter_list|)
block|{
name|entries
operator|.
name|add
argument_list|(
operator|new
name|Entry
argument_list|(
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
comment|/**      * Adds a range entry with explicit from and unbounded to.      *      * @param from the from distance limit, to is unbounded.      */
DECL|method|addUnboundedTo
specifier|public
name|GeoDistanceFacetBuilder
name|addUnboundedTo
parameter_list|(
name|double
name|from
parameter_list|)
block|{
name|entries
operator|.
name|add
argument_list|(
operator|new
name|Entry
argument_list|(
name|from
argument_list|,
name|Double
operator|.
name|POSITIVE_INFINITY
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Adds a range entry with explicit to and unbounded from.      *      * @param to the to distance limit, from is unbounded.      */
DECL|method|addUnboundedFrom
specifier|public
name|GeoDistanceFacetBuilder
name|addUnboundedFrom
parameter_list|(
name|double
name|to
parameter_list|)
block|{
name|entries
operator|.
name|add
argument_list|(
operator|new
name|Entry
argument_list|(
name|Double
operator|.
name|NEGATIVE_INFINITY
argument_list|,
name|to
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The distance unit to use. Defaults to {@link org.elasticsearch.common.unit.DistanceUnit#KILOMETERS}      */
DECL|method|unit
specifier|public
name|GeoDistanceFacetBuilder
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
comment|/**      * Marks the facet to run in a global scope, not bounded by any query.      */
DECL|method|global
specifier|public
name|GeoDistanceFacetBuilder
name|global
parameter_list|(
name|boolean
name|global
parameter_list|)
block|{
name|super
operator|.
name|global
argument_list|(
name|global
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Marks the facet to run in a specific scope.      */
annotation|@
name|Override
DECL|method|scope
specifier|public
name|GeoDistanceFacetBuilder
name|scope
parameter_list|(
name|String
name|scope
parameter_list|)
block|{
name|super
operator|.
name|scope
argument_list|(
name|scope
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|facetFilter
specifier|public
name|GeoDistanceFacetBuilder
name|facetFilter
parameter_list|(
name|FilterBuilder
name|filter
parameter_list|)
block|{
name|this
operator|.
name|facetFilter
operator|=
name|filter
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets the nested path the facet will execute on. A match (root object) will then cause all the      * nested objects matching the path to be computed into the facet.      */
DECL|method|nested
specifier|public
name|GeoDistanceFacetBuilder
name|nested
parameter_list|(
name|String
name|nested
parameter_list|)
block|{
name|this
operator|.
name|nested
operator|=
name|nested
expr_stmt|;
return|return
name|this
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
name|fieldName
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|SearchSourceBuilderException
argument_list|(
literal|"field must be set on geo_distance facet for facet ["
operator|+
name|name
operator|+
literal|"]"
argument_list|)
throw|;
block|}
if|if
condition|(
name|entries
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|SearchSourceBuilderException
argument_list|(
literal|"at least one range must be defined for geo_distance facet ["
operator|+
name|name
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|builder
operator|.
name|startObject
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|builder
operator|.
name|startObject
argument_list|(
name|GeoDistanceFacet
operator|.
name|TYPE
argument_list|)
expr_stmt|;
if|if
condition|(
name|geohash
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|fieldName
argument_list|,
name|geohash
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|builder
operator|.
name|startArray
argument_list|(
name|fieldName
argument_list|)
operator|.
name|value
argument_list|(
name|lon
argument_list|)
operator|.
name|value
argument_list|(
name|lat
argument_list|)
operator|.
name|endArray
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|valueFieldName
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"value_field"
argument_list|,
name|valueFieldName
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|valueScript
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"value_script"
argument_list|,
name|valueScript
argument_list|)
expr_stmt|;
if|if
condition|(
name|lang
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"lang"
argument_list|,
name|lang
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|this
operator|.
name|params
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"params"
argument_list|,
name|this
operator|.
name|params
argument_list|)
expr_stmt|;
block|}
block|}
name|builder
operator|.
name|startArray
argument_list|(
literal|"ranges"
argument_list|)
expr_stmt|;
for|for
control|(
name|Entry
name|entry
range|:
name|entries
control|)
block|{
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
if|if
condition|(
operator|!
name|Double
operator|.
name|isInfinite
argument_list|(
name|entry
operator|.
name|from
argument_list|)
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"from"
argument_list|,
name|entry
operator|.
name|from
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|Double
operator|.
name|isInfinite
argument_list|(
name|entry
operator|.
name|to
argument_list|)
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"to"
argument_list|,
name|entry
operator|.
name|to
argument_list|)
expr_stmt|;
block|}
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
name|geoDistance
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
name|geoDistance
operator|.
name|name
argument_list|()
operator|.
name|toLowerCase
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|addFilterFacetAndGlobal
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
DECL|class|Entry
specifier|private
specifier|static
class|class
name|Entry
block|{
DECL|field|from
specifier|final
name|double
name|from
decl_stmt|;
DECL|field|to
specifier|final
name|double
name|to
decl_stmt|;
DECL|method|Entry
specifier|private
name|Entry
parameter_list|(
name|double
name|from
parameter_list|,
name|double
name|to
parameter_list|)
block|{
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
block|}
block|}
end_class

end_unit

