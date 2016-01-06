begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.query
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
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
name|search
operator|.
name|GeoPointInBBoxQuery
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
name|Query
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|Version
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
name|Numbers
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
name|GeoUtils
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
name|fielddata
operator|.
name|IndexGeoPointFieldData
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
name|MappedFieldType
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
name|geo
operator|.
name|BaseGeoPointFieldMapper
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
name|geo
operator|.
name|GeoPointFieldMapperLegacy
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
name|search
operator|.
name|geo
operator|.
name|InMemoryGeoBoundingBoxQuery
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
name|search
operator|.
name|geo
operator|.
name|IndexedGeoBoundingBoxQuery
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
name|Objects
import|;
end_import

begin_comment
comment|/**  * Creates a Lucene query that will filter for all documents that lie within the specified  * bounding box.  *  * This query can only operate on fields of type geo_point that have latitude and longitude  * enabled.  * */
end_comment

begin_class
DECL|class|GeoBoundingBoxQueryBuilder
specifier|public
class|class
name|GeoBoundingBoxQueryBuilder
extends|extends
name|AbstractQueryBuilder
argument_list|<
name|GeoBoundingBoxQueryBuilder
argument_list|>
block|{
comment|/** Name of the query. */
DECL|field|NAME
specifier|public
specifier|static
specifier|final
name|String
name|NAME
init|=
literal|"geo_bbox"
decl_stmt|;
comment|/** Default type for executing this query (memory as of this writing). */
DECL|field|DEFAULT_TYPE
specifier|public
specifier|static
specifier|final
name|GeoExecType
name|DEFAULT_TYPE
init|=
name|GeoExecType
operator|.
name|MEMORY
decl_stmt|;
comment|/** Needed for serialization. */
DECL|field|PROTOTYPE
specifier|static
specifier|final
name|GeoBoundingBoxQueryBuilder
name|PROTOTYPE
init|=
operator|new
name|GeoBoundingBoxQueryBuilder
argument_list|(
literal|""
argument_list|)
decl_stmt|;
comment|/** Name of field holding geo coordinates to compute the bounding box on.*/
DECL|field|fieldName
specifier|private
specifier|final
name|String
name|fieldName
decl_stmt|;
comment|/** Top left corner coordinates of bounding box. */
DECL|field|topLeft
specifier|private
name|GeoPoint
name|topLeft
init|=
operator|new
name|GeoPoint
argument_list|(
name|Double
operator|.
name|NaN
argument_list|,
name|Double
operator|.
name|NaN
argument_list|)
decl_stmt|;
comment|/** Bottom right corner coordinates of bounding box.*/
DECL|field|bottomRight
specifier|private
name|GeoPoint
name|bottomRight
init|=
operator|new
name|GeoPoint
argument_list|(
name|Double
operator|.
name|NaN
argument_list|,
name|Double
operator|.
name|NaN
argument_list|)
decl_stmt|;
comment|/** How to deal with incorrect coordinates.*/
DECL|field|validationMethod
specifier|private
name|GeoValidationMethod
name|validationMethod
init|=
name|GeoValidationMethod
operator|.
name|DEFAULT
decl_stmt|;
comment|/** How the query should be run. */
DECL|field|type
specifier|private
name|GeoExecType
name|type
init|=
name|DEFAULT_TYPE
decl_stmt|;
comment|/**      * Create new bounding box query.      * @param fieldName name of index field containing geo coordinates to operate on.      * */
DECL|method|GeoBoundingBoxQueryBuilder
specifier|public
name|GeoBoundingBoxQueryBuilder
parameter_list|(
name|String
name|fieldName
parameter_list|)
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
name|IllegalArgumentException
argument_list|(
literal|"Field name must not be empty."
argument_list|)
throw|;
block|}
name|this
operator|.
name|fieldName
operator|=
name|fieldName
expr_stmt|;
block|}
comment|/**      * Adds top left point.      * @param top The top latitude      * @param left The left longitude      * @param bottom The bottom latitude      * @param right The right longitude      */
DECL|method|setCorners
specifier|public
name|GeoBoundingBoxQueryBuilder
name|setCorners
parameter_list|(
name|double
name|top
parameter_list|,
name|double
name|left
parameter_list|,
name|double
name|bottom
parameter_list|,
name|double
name|right
parameter_list|)
block|{
if|if
condition|(
name|GeoValidationMethod
operator|.
name|isIgnoreMalformed
argument_list|(
name|validationMethod
argument_list|)
operator|==
literal|false
condition|)
block|{
if|if
condition|(
name|Numbers
operator|.
name|isValidDouble
argument_list|(
name|top
argument_list|)
operator|==
literal|false
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"top latitude is invalid: "
operator|+
name|top
argument_list|)
throw|;
block|}
if|if
condition|(
name|Numbers
operator|.
name|isValidDouble
argument_list|(
name|left
argument_list|)
operator|==
literal|false
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"left longitude is invalid: "
operator|+
name|left
argument_list|)
throw|;
block|}
if|if
condition|(
name|Numbers
operator|.
name|isValidDouble
argument_list|(
name|bottom
argument_list|)
operator|==
literal|false
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"bottom latitude is invalid: "
operator|+
name|bottom
argument_list|)
throw|;
block|}
if|if
condition|(
name|Numbers
operator|.
name|isValidDouble
argument_list|(
name|right
argument_list|)
operator|==
literal|false
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"right longitude is invalid: "
operator|+
name|right
argument_list|)
throw|;
block|}
comment|// all corners are valid after above checks - make sure they are in the right relation
if|if
condition|(
name|top
operator|<
name|bottom
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"top is below bottom corner: "
operator|+
name|top
operator|+
literal|" vs. "
operator|+
name|bottom
argument_list|)
throw|;
block|}
comment|// we do not check longitudes as the query generation code can deal with flipped left/right values
block|}
name|topLeft
operator|.
name|reset
argument_list|(
name|top
argument_list|,
name|left
argument_list|)
expr_stmt|;
name|bottomRight
operator|.
name|reset
argument_list|(
name|bottom
argument_list|,
name|right
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Adds points.      * @param topLeft topLeft point to add.      * @param bottomRight bottomRight point to add.      * */
DECL|method|setCorners
specifier|public
name|GeoBoundingBoxQueryBuilder
name|setCorners
parameter_list|(
name|GeoPoint
name|topLeft
parameter_list|,
name|GeoPoint
name|bottomRight
parameter_list|)
block|{
return|return
name|setCorners
argument_list|(
name|topLeft
operator|.
name|getLat
argument_list|()
argument_list|,
name|topLeft
operator|.
name|getLon
argument_list|()
argument_list|,
name|bottomRight
operator|.
name|getLat
argument_list|()
argument_list|,
name|bottomRight
operator|.
name|getLon
argument_list|()
argument_list|)
return|;
block|}
comment|/**      * Adds points.      * @param topLeft topLeft point to add as geohash.      * @param bottomRight bottomRight point to add as geohash.      * */
DECL|method|setCorners
specifier|public
name|GeoBoundingBoxQueryBuilder
name|setCorners
parameter_list|(
name|String
name|topLeft
parameter_list|,
name|String
name|bottomRight
parameter_list|)
block|{
return|return
name|setCorners
argument_list|(
name|GeoPoint
operator|.
name|fromGeohash
argument_list|(
name|topLeft
argument_list|)
argument_list|,
name|GeoPoint
operator|.
name|fromGeohash
argument_list|(
name|bottomRight
argument_list|)
argument_list|)
return|;
block|}
comment|/** Returns the top left corner of the bounding box. */
DECL|method|topLeft
specifier|public
name|GeoPoint
name|topLeft
parameter_list|()
block|{
return|return
name|topLeft
return|;
block|}
comment|/** Returns the bottom right corner of the bounding box. */
DECL|method|bottomRight
specifier|public
name|GeoPoint
name|bottomRight
parameter_list|()
block|{
return|return
name|bottomRight
return|;
block|}
comment|/**      * Adds corners in OGC standard bbox/ envelop format.      *      * @param bottomLeft bottom left corner of bounding box.      * @param topRight top right corner of bounding box.      */
DECL|method|setCornersOGC
specifier|public
name|GeoBoundingBoxQueryBuilder
name|setCornersOGC
parameter_list|(
name|GeoPoint
name|bottomLeft
parameter_list|,
name|GeoPoint
name|topRight
parameter_list|)
block|{
return|return
name|setCorners
argument_list|(
name|topRight
operator|.
name|getLat
argument_list|()
argument_list|,
name|bottomLeft
operator|.
name|getLon
argument_list|()
argument_list|,
name|bottomLeft
operator|.
name|getLat
argument_list|()
argument_list|,
name|topRight
operator|.
name|getLon
argument_list|()
argument_list|)
return|;
block|}
comment|/**      * Adds corners in OGC standard bbox/ envelop format.      *      * @param bottomLeft bottom left corner geohash.      * @param topRight top right corner geohash.      */
DECL|method|setCornersOGC
specifier|public
name|GeoBoundingBoxQueryBuilder
name|setCornersOGC
parameter_list|(
name|String
name|bottomLeft
parameter_list|,
name|String
name|topRight
parameter_list|)
block|{
return|return
name|setCornersOGC
argument_list|(
name|GeoPoint
operator|.
name|fromGeohash
argument_list|(
name|bottomLeft
argument_list|)
argument_list|,
name|GeoPoint
operator|.
name|fromGeohash
argument_list|(
name|topRight
argument_list|)
argument_list|)
return|;
block|}
comment|/**      * Specify whether or not to ignore validation errors of bounding boxes.      * Can only be set if coerce set to false, otherwise calling this      * method has no effect.      **/
DECL|method|setValidationMethod
specifier|public
name|GeoBoundingBoxQueryBuilder
name|setValidationMethod
parameter_list|(
name|GeoValidationMethod
name|method
parameter_list|)
block|{
name|this
operator|.
name|validationMethod
operator|=
name|method
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Returns geo coordinate validation method to use.      * */
DECL|method|getValidationMethod
specifier|public
name|GeoValidationMethod
name|getValidationMethod
parameter_list|()
block|{
return|return
name|this
operator|.
name|validationMethod
return|;
block|}
comment|/**      * Sets the type of executing of the geo bounding box. Can be either `memory` or `indexed`. Defaults      * to `memory`.      */
DECL|method|type
specifier|public
name|GeoBoundingBoxQueryBuilder
name|type
parameter_list|(
name|GeoExecType
name|type
parameter_list|)
block|{
if|if
condition|(
name|type
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Type is not allowed to be null."
argument_list|)
throw|;
block|}
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * For BWC: Parse type from type name.      * */
DECL|method|type
specifier|public
name|GeoBoundingBoxQueryBuilder
name|type
parameter_list|(
name|String
name|type
parameter_list|)
block|{
name|this
operator|.
name|type
operator|=
name|GeoExecType
operator|.
name|fromString
argument_list|(
name|type
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/** Returns the execution type of the geo bounding box.*/
DECL|method|type
specifier|public
name|GeoExecType
name|type
parameter_list|()
block|{
return|return
name|type
return|;
block|}
comment|/** Returns the name of the field to base the bounding box computation on. */
DECL|method|fieldName
specifier|public
name|String
name|fieldName
parameter_list|()
block|{
return|return
name|this
operator|.
name|fieldName
return|;
block|}
DECL|method|checkLatLon
name|QueryValidationException
name|checkLatLon
parameter_list|(
name|boolean
name|indexCreatedBeforeV2_0
parameter_list|)
block|{
comment|// validation was not available prior to 2.x, so to support bwc percolation queries we only ignore_malformed on 2.x created indexes
if|if
condition|(
name|GeoValidationMethod
operator|.
name|isIgnoreMalformed
argument_list|(
name|validationMethod
argument_list|)
operator|==
literal|true
operator|||
name|indexCreatedBeforeV2_0
condition|)
block|{
return|return
literal|null
return|;
block|}
name|QueryValidationException
name|validationException
init|=
literal|null
decl_stmt|;
comment|// For everything post 2.0 validate latitude and longitude unless validation was explicitly turned off
if|if
condition|(
name|GeoUtils
operator|.
name|isValidLatitude
argument_list|(
name|topLeft
operator|.
name|getLat
argument_list|()
argument_list|)
operator|==
literal|false
condition|)
block|{
name|validationException
operator|=
name|addValidationError
argument_list|(
literal|"top latitude is invalid: "
operator|+
name|topLeft
operator|.
name|getLat
argument_list|()
argument_list|,
name|validationException
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|GeoUtils
operator|.
name|isValidLongitude
argument_list|(
name|topLeft
operator|.
name|getLon
argument_list|()
argument_list|)
operator|==
literal|false
condition|)
block|{
name|validationException
operator|=
name|addValidationError
argument_list|(
literal|"left longitude is invalid: "
operator|+
name|topLeft
operator|.
name|getLon
argument_list|()
argument_list|,
name|validationException
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|GeoUtils
operator|.
name|isValidLatitude
argument_list|(
name|bottomRight
operator|.
name|getLat
argument_list|()
argument_list|)
operator|==
literal|false
condition|)
block|{
name|validationException
operator|=
name|addValidationError
argument_list|(
literal|"bottom latitude is invalid: "
operator|+
name|bottomRight
operator|.
name|getLat
argument_list|()
argument_list|,
name|validationException
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|GeoUtils
operator|.
name|isValidLongitude
argument_list|(
name|bottomRight
operator|.
name|getLon
argument_list|()
argument_list|)
operator|==
literal|false
condition|)
block|{
name|validationException
operator|=
name|addValidationError
argument_list|(
literal|"right longitude is invalid: "
operator|+
name|bottomRight
operator|.
name|getLon
argument_list|()
argument_list|,
name|validationException
argument_list|)
expr_stmt|;
block|}
return|return
name|validationException
return|;
block|}
annotation|@
name|Override
DECL|method|doToQuery
specifier|public
name|Query
name|doToQuery
parameter_list|(
name|QueryShardContext
name|context
parameter_list|)
block|{
name|MappedFieldType
name|fieldType
init|=
name|context
operator|.
name|fieldMapper
argument_list|(
name|fieldName
argument_list|)
decl_stmt|;
if|if
condition|(
name|fieldType
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|QueryShardException
argument_list|(
name|context
argument_list|,
literal|"failed to find geo_point field ["
operator|+
name|fieldName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
if|if
condition|(
operator|!
operator|(
name|fieldType
operator|instanceof
name|BaseGeoPointFieldMapper
operator|.
name|GeoPointFieldType
operator|)
condition|)
block|{
throw|throw
operator|new
name|QueryShardException
argument_list|(
name|context
argument_list|,
literal|"field ["
operator|+
name|fieldName
operator|+
literal|"] is not a geo_point field"
argument_list|)
throw|;
block|}
name|QueryValidationException
name|exception
init|=
name|checkLatLon
argument_list|(
name|context
operator|.
name|indexVersionCreated
argument_list|()
operator|.
name|before
argument_list|(
name|Version
operator|.
name|V_2_0_0
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|exception
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|QueryShardException
argument_list|(
name|context
argument_list|,
literal|"couldn't validate latitude/ longitude values"
argument_list|,
name|exception
argument_list|)
throw|;
block|}
name|GeoPoint
name|luceneTopLeft
init|=
operator|new
name|GeoPoint
argument_list|(
name|topLeft
argument_list|)
decl_stmt|;
name|GeoPoint
name|luceneBottomRight
init|=
operator|new
name|GeoPoint
argument_list|(
name|bottomRight
argument_list|)
decl_stmt|;
if|if
condition|(
name|GeoValidationMethod
operator|.
name|isCoerce
argument_list|(
name|validationMethod
argument_list|)
condition|)
block|{
comment|// Special case: if the difference between the left and right is 360 and the right is greater than the left, we are asking for
comment|// the complete longitude range so need to set longitude to the complete longditude range
name|double
name|right
init|=
name|luceneBottomRight
operator|.
name|getLon
argument_list|()
decl_stmt|;
name|double
name|left
init|=
name|luceneTopLeft
operator|.
name|getLon
argument_list|()
decl_stmt|;
name|boolean
name|completeLonRange
init|=
operator|(
operator|(
name|right
operator|-
name|left
operator|)
operator|%
literal|360
operator|==
literal|0
operator|&&
name|right
operator|>
name|left
operator|)
decl_stmt|;
name|GeoUtils
operator|.
name|normalizePoint
argument_list|(
name|luceneTopLeft
argument_list|,
literal|true
argument_list|,
operator|!
name|completeLonRange
argument_list|)
expr_stmt|;
name|GeoUtils
operator|.
name|normalizePoint
argument_list|(
name|luceneBottomRight
argument_list|,
literal|true
argument_list|,
operator|!
name|completeLonRange
argument_list|)
expr_stmt|;
if|if
condition|(
name|completeLonRange
condition|)
block|{
name|luceneTopLeft
operator|.
name|resetLon
argument_list|(
operator|-
literal|180
argument_list|)
expr_stmt|;
name|luceneBottomRight
operator|.
name|resetLon
argument_list|(
literal|180
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|context
operator|.
name|indexVersionCreated
argument_list|()
operator|.
name|onOrAfter
argument_list|(
name|Version
operator|.
name|V_2_2_0
argument_list|)
condition|)
block|{
return|return
operator|new
name|GeoPointInBBoxQuery
argument_list|(
name|fieldType
operator|.
name|name
argument_list|()
argument_list|,
name|luceneTopLeft
operator|.
name|lon
argument_list|()
argument_list|,
name|luceneBottomRight
operator|.
name|lat
argument_list|()
argument_list|,
name|luceneBottomRight
operator|.
name|lon
argument_list|()
argument_list|,
name|luceneTopLeft
operator|.
name|lat
argument_list|()
argument_list|)
return|;
block|}
name|Query
name|query
decl_stmt|;
switch|switch
condition|(
name|type
condition|)
block|{
case|case
name|INDEXED
case|:
name|GeoPointFieldMapperLegacy
operator|.
name|GeoPointFieldType
name|geoFieldType
init|=
operator|(
operator|(
name|GeoPointFieldMapperLegacy
operator|.
name|GeoPointFieldType
operator|)
name|fieldType
operator|)
decl_stmt|;
name|query
operator|=
name|IndexedGeoBoundingBoxQuery
operator|.
name|create
argument_list|(
name|luceneTopLeft
argument_list|,
name|luceneBottomRight
argument_list|,
name|geoFieldType
argument_list|)
expr_stmt|;
break|break;
case|case
name|MEMORY
case|:
name|IndexGeoPointFieldData
name|indexFieldData
init|=
name|context
operator|.
name|getForField
argument_list|(
name|fieldType
argument_list|)
decl_stmt|;
name|query
operator|=
operator|new
name|InMemoryGeoBoundingBoxQuery
argument_list|(
name|luceneTopLeft
argument_list|,
name|luceneBottomRight
argument_list|,
name|indexFieldData
argument_list|)
expr_stmt|;
break|break;
default|default:
comment|// Someone extended the type enum w/o adjusting this switch statement.
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"geo bounding box type ["
operator|+
name|type
operator|+
literal|"] not supported."
argument_list|)
throw|;
block|}
return|return
name|query
return|;
block|}
annotation|@
name|Override
DECL|method|doXContent
specifier|protected
name|void
name|doXContent
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
name|NAME
argument_list|)
expr_stmt|;
name|builder
operator|.
name|startObject
argument_list|(
name|fieldName
argument_list|)
expr_stmt|;
name|builder
operator|.
name|array
argument_list|(
name|GeoBoundingBoxQueryParser
operator|.
name|TOP_LEFT_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|topLeft
operator|.
name|getLon
argument_list|()
argument_list|,
name|topLeft
operator|.
name|getLat
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|array
argument_list|(
name|GeoBoundingBoxQueryParser
operator|.
name|BOTTOM_RIGHT_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|bottomRight
operator|.
name|getLon
argument_list|()
argument_list|,
name|bottomRight
operator|.
name|getLat
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|GeoBoundingBoxQueryParser
operator|.
name|VALIDATION_METHOD_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|validationMethod
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|GeoBoundingBoxQueryParser
operator|.
name|TYPE_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|type
argument_list|)
expr_stmt|;
name|printBoostAndQueryName
argument_list|(
name|builder
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doEquals
specifier|protected
name|boolean
name|doEquals
parameter_list|(
name|GeoBoundingBoxQueryBuilder
name|other
parameter_list|)
block|{
return|return
name|Objects
operator|.
name|equals
argument_list|(
name|topLeft
argument_list|,
name|other
operator|.
name|topLeft
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|bottomRight
argument_list|,
name|other
operator|.
name|bottomRight
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|type
argument_list|,
name|other
operator|.
name|type
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|validationMethod
argument_list|,
name|other
operator|.
name|validationMethod
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|fieldName
argument_list|,
name|other
operator|.
name|fieldName
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|doHashCode
specifier|protected
name|int
name|doHashCode
parameter_list|()
block|{
return|return
name|Objects
operator|.
name|hash
argument_list|(
name|topLeft
argument_list|,
name|bottomRight
argument_list|,
name|type
argument_list|,
name|validationMethod
argument_list|,
name|fieldName
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|doReadFrom
specifier|protected
name|GeoBoundingBoxQueryBuilder
name|doReadFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|fieldName
init|=
name|in
operator|.
name|readString
argument_list|()
decl_stmt|;
name|GeoBoundingBoxQueryBuilder
name|geo
init|=
operator|new
name|GeoBoundingBoxQueryBuilder
argument_list|(
name|fieldName
argument_list|)
decl_stmt|;
name|geo
operator|.
name|topLeft
operator|=
name|in
operator|.
name|readGeoPoint
argument_list|()
expr_stmt|;
name|geo
operator|.
name|bottomRight
operator|=
name|in
operator|.
name|readGeoPoint
argument_list|()
expr_stmt|;
name|geo
operator|.
name|type
operator|=
name|GeoExecType
operator|.
name|readTypeFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|geo
operator|.
name|validationMethod
operator|=
name|GeoValidationMethod
operator|.
name|readGeoValidationMethodFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
return|return
name|geo
return|;
block|}
annotation|@
name|Override
DECL|method|doWriteTo
specifier|protected
name|void
name|doWriteTo
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
name|fieldName
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeGeoPoint
argument_list|(
name|topLeft
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeGeoPoint
argument_list|(
name|bottomRight
argument_list|)
expr_stmt|;
name|type
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|validationMethod
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getWriteableName
specifier|public
name|String
name|getWriteableName
parameter_list|()
block|{
return|return
name|NAME
return|;
block|}
block|}
end_class

end_unit

