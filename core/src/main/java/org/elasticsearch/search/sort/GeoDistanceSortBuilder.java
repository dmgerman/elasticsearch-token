begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.sort
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|sort
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchParseException
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
name|QueryBuilder
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
name|Locale
import|;
end_import

begin_comment
comment|/**  * A geo distance based sorting on a geo point like field.  */
end_comment

begin_class
DECL|class|GeoDistanceSortBuilder
specifier|public
class|class
name|GeoDistanceSortBuilder
extends|extends
name|SortBuilder
block|{
DECL|field|fieldName
specifier|final
name|String
name|fieldName
decl_stmt|;
DECL|field|points
specifier|private
specifier|final
name|List
argument_list|<
name|GeoPoint
argument_list|>
name|points
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
DECL|field|geohashes
specifier|private
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|geohashes
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
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
DECL|field|order
specifier|private
name|SortOrder
name|order
decl_stmt|;
DECL|field|sortMode
specifier|private
name|String
name|sortMode
decl_stmt|;
DECL|field|nestedFilter
specifier|private
name|QueryBuilder
name|nestedFilter
decl_stmt|;
DECL|field|nestedPath
specifier|private
name|String
name|nestedPath
decl_stmt|;
DECL|field|coerce
specifier|private
name|Boolean
name|coerce
decl_stmt|;
DECL|field|ignoreMalformed
specifier|private
name|Boolean
name|ignoreMalformed
decl_stmt|;
comment|/**      * Constructs a new distance based sort on a geo point like field.      *      * @param fieldName The geo point like field name.      */
DECL|method|GeoDistanceSortBuilder
specifier|public
name|GeoDistanceSortBuilder
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
block|}
comment|/**      * The point to create the range distance facets from.      *      * @param lat latitude.      * @param lon longitude.      */
DECL|method|point
specifier|public
name|GeoDistanceSortBuilder
name|point
parameter_list|(
name|double
name|lat
parameter_list|,
name|double
name|lon
parameter_list|)
block|{
name|points
operator|.
name|add
argument_list|(
operator|new
name|GeoPoint
argument_list|(
name|lat
argument_list|,
name|lon
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The point to create the range distance facets from.      *      * @param points reference points.      */
DECL|method|points
specifier|public
name|GeoDistanceSortBuilder
name|points
parameter_list|(
name|GeoPoint
modifier|...
name|points
parameter_list|)
block|{
name|this
operator|.
name|points
operator|.
name|addAll
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|points
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The geohash of the geo point to create the range distance facets from.      */
DECL|method|geohashes
specifier|public
name|GeoDistanceSortBuilder
name|geohashes
parameter_list|(
name|String
modifier|...
name|geohashes
parameter_list|)
block|{
name|this
operator|.
name|geohashes
operator|.
name|addAll
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|geohashes
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The geo distance type used to compute the distance.      */
DECL|method|geoDistance
specifier|public
name|GeoDistanceSortBuilder
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
comment|/**      * The distance unit to use. Defaults to {@link org.elasticsearch.common.unit.DistanceUnit#KILOMETERS}      */
DECL|method|unit
specifier|public
name|GeoDistanceSortBuilder
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
comment|/**      * The order of sorting. Defaults to {@link SortOrder#ASC}.      */
annotation|@
name|Override
DECL|method|order
specifier|public
name|GeoDistanceSortBuilder
name|order
parameter_list|(
name|SortOrder
name|order
parameter_list|)
block|{
name|this
operator|.
name|order
operator|=
name|order
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Not relevant.      */
annotation|@
name|Override
DECL|method|missing
specifier|public
name|SortBuilder
name|missing
parameter_list|(
name|Object
name|missing
parameter_list|)
block|{
return|return
name|this
return|;
block|}
comment|/**      * Defines which distance to use for sorting in the case a document contains multiple geo points.      * Possible values: min and max      */
DECL|method|sortMode
specifier|public
name|GeoDistanceSortBuilder
name|sortMode
parameter_list|(
name|String
name|sortMode
parameter_list|)
block|{
name|this
operator|.
name|sortMode
operator|=
name|sortMode
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets the nested filter that the nested objects should match with in order to be taken into account      * for sorting.      */
DECL|method|setNestedFilter
specifier|public
name|GeoDistanceSortBuilder
name|setNestedFilter
parameter_list|(
name|QueryBuilder
name|nestedFilter
parameter_list|)
block|{
name|this
operator|.
name|nestedFilter
operator|=
name|nestedFilter
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets the nested path if sorting occurs on a field that is inside a nested object. By default when sorting on a      * field inside a nested object, the nearest upper nested object is selected as nested path.      */
DECL|method|setNestedPath
specifier|public
name|GeoDistanceSortBuilder
name|setNestedPath
parameter_list|(
name|String
name|nestedPath
parameter_list|)
block|{
name|this
operator|.
name|nestedPath
operator|=
name|nestedPath
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|coerce
specifier|public
name|GeoDistanceSortBuilder
name|coerce
parameter_list|(
name|boolean
name|coerce
parameter_list|)
block|{
name|this
operator|.
name|coerce
operator|=
name|coerce
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|ignoreMalformed
specifier|public
name|GeoDistanceSortBuilder
name|ignoreMalformed
parameter_list|(
name|boolean
name|ignoreMalformed
parameter_list|)
block|{
name|this
operator|.
name|ignoreMalformed
operator|=
name|ignoreMalformed
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
name|builder
operator|.
name|startObject
argument_list|(
literal|"_geo_distance"
argument_list|)
expr_stmt|;
if|if
condition|(
name|geohashes
operator|.
name|size
argument_list|()
operator|==
literal|0
operator|&&
name|points
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
throw|throw
operator|new
name|ElasticsearchParseException
argument_list|(
literal|"No points provided for _geo_distance sort."
argument_list|)
throw|;
block|}
name|builder
operator|.
name|startArray
argument_list|(
name|fieldName
argument_list|)
expr_stmt|;
for|for
control|(
name|GeoPoint
name|point
range|:
name|points
control|)
block|{
name|builder
operator|.
name|value
argument_list|(
name|point
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|String
name|geohash
range|:
name|geohashes
control|)
block|{
name|builder
operator|.
name|value
argument_list|(
name|geohash
argument_list|)
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
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|order
operator|==
name|SortOrder
operator|.
name|DESC
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"reverse"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|sortMode
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"mode"
argument_list|,
name|sortMode
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|nestedPath
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"nested_path"
argument_list|,
name|nestedPath
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|nestedFilter
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"nested_filter"
argument_list|,
name|nestedFilter
argument_list|,
name|params
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|coerce
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"coerce"
argument_list|,
name|coerce
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|ignoreMalformed
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"ignore_malformed"
argument_list|,
name|ignoreMalformed
argument_list|)
expr_stmt|;
block|}
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

