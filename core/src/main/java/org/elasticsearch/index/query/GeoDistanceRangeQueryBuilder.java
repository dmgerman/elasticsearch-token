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
name|GeoPointDistanceRangeQuery
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
name|Strings
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
name|GeoDistanceRangeQuery
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
name|Locale
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

begin_import
import|import static
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|GeoUtils
operator|.
name|TOLERANCE
import|;
end_import

begin_class
DECL|class|GeoDistanceRangeQueryBuilder
specifier|public
class|class
name|GeoDistanceRangeQueryBuilder
extends|extends
name|AbstractQueryBuilder
argument_list|<
name|GeoDistanceRangeQueryBuilder
argument_list|>
block|{
DECL|field|NAME
specifier|public
specifier|static
specifier|final
name|String
name|NAME
init|=
literal|"geo_distance_range"
decl_stmt|;
DECL|field|DEFAULT_INCLUDE_LOWER
specifier|public
specifier|static
specifier|final
name|boolean
name|DEFAULT_INCLUDE_LOWER
init|=
literal|true
decl_stmt|;
DECL|field|DEFAULT_INCLUDE_UPPER
specifier|public
specifier|static
specifier|final
name|boolean
name|DEFAULT_INCLUDE_UPPER
init|=
literal|true
decl_stmt|;
DECL|field|DEFAULT_GEO_DISTANCE
specifier|public
specifier|static
specifier|final
name|GeoDistance
name|DEFAULT_GEO_DISTANCE
init|=
name|GeoDistance
operator|.
name|DEFAULT
decl_stmt|;
DECL|field|DEFAULT_UNIT
specifier|public
specifier|static
specifier|final
name|DistanceUnit
name|DEFAULT_UNIT
init|=
name|DistanceUnit
operator|.
name|DEFAULT
decl_stmt|;
DECL|field|DEFAULT_OPTIMIZE_BBOX
specifier|public
specifier|static
specifier|final
name|String
name|DEFAULT_OPTIMIZE_BBOX
init|=
literal|"memory"
decl_stmt|;
DECL|field|fieldName
specifier|private
specifier|final
name|String
name|fieldName
decl_stmt|;
DECL|field|from
specifier|private
name|Object
name|from
decl_stmt|;
DECL|field|to
specifier|private
name|Object
name|to
decl_stmt|;
DECL|field|includeLower
specifier|private
name|boolean
name|includeLower
init|=
name|DEFAULT_INCLUDE_LOWER
decl_stmt|;
DECL|field|includeUpper
specifier|private
name|boolean
name|includeUpper
init|=
name|DEFAULT_INCLUDE_UPPER
decl_stmt|;
DECL|field|point
specifier|private
specifier|final
name|GeoPoint
name|point
decl_stmt|;
DECL|field|geoDistance
specifier|private
name|GeoDistance
name|geoDistance
init|=
name|DEFAULT_GEO_DISTANCE
decl_stmt|;
DECL|field|unit
specifier|private
name|DistanceUnit
name|unit
init|=
name|DEFAULT_UNIT
decl_stmt|;
DECL|field|optimizeBbox
specifier|private
name|String
name|optimizeBbox
init|=
name|DEFAULT_OPTIMIZE_BBOX
decl_stmt|;
DECL|field|validationMethod
specifier|private
name|GeoValidationMethod
name|validationMethod
init|=
name|GeoValidationMethod
operator|.
name|DEFAULT
decl_stmt|;
DECL|field|PROTOTYPE
specifier|static
specifier|final
name|GeoDistanceRangeQueryBuilder
name|PROTOTYPE
init|=
operator|new
name|GeoDistanceRangeQueryBuilder
argument_list|(
literal|"_na_"
argument_list|,
operator|new
name|GeoPoint
argument_list|()
argument_list|)
decl_stmt|;
DECL|method|GeoDistanceRangeQueryBuilder
specifier|public
name|GeoDistanceRangeQueryBuilder
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|GeoPoint
name|point
parameter_list|)
block|{
if|if
condition|(
name|Strings
operator|.
name|isEmpty
argument_list|(
name|fieldName
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"fieldName must not be null"
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
name|IllegalArgumentException
argument_list|(
literal|"point must not be null"
argument_list|)
throw|;
block|}
name|this
operator|.
name|fieldName
operator|=
name|fieldName
expr_stmt|;
name|this
operator|.
name|point
operator|=
name|point
expr_stmt|;
block|}
DECL|method|GeoDistanceRangeQueryBuilder
specifier|public
name|GeoDistanceRangeQueryBuilder
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|double
name|lat
parameter_list|,
name|double
name|lon
parameter_list|)
block|{
name|this
argument_list|(
name|fieldName
argument_list|,
operator|new
name|GeoPoint
argument_list|(
name|lat
argument_list|,
name|lon
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|GeoDistanceRangeQueryBuilder
specifier|public
name|GeoDistanceRangeQueryBuilder
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|String
name|geohash
parameter_list|)
block|{
name|this
argument_list|(
name|fieldName
argument_list|,
name|geohash
operator|==
literal|null
condition|?
literal|null
else|:
operator|new
name|GeoPoint
argument_list|()
operator|.
name|resetFromGeoHash
argument_list|(
name|geohash
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|fieldName
specifier|public
name|String
name|fieldName
parameter_list|()
block|{
return|return
name|fieldName
return|;
block|}
DECL|method|point
specifier|public
name|GeoPoint
name|point
parameter_list|()
block|{
return|return
name|point
return|;
block|}
DECL|method|from
specifier|public
name|GeoDistanceRangeQueryBuilder
name|from
parameter_list|(
name|String
name|from
parameter_list|)
block|{
if|if
condition|(
name|from
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"[from] must not be null"
argument_list|)
throw|;
block|}
name|this
operator|.
name|from
operator|=
name|from
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|from
specifier|public
name|GeoDistanceRangeQueryBuilder
name|from
parameter_list|(
name|Number
name|from
parameter_list|)
block|{
if|if
condition|(
name|from
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"[from] must not be null"
argument_list|)
throw|;
block|}
name|this
operator|.
name|from
operator|=
name|from
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|from
specifier|public
name|Object
name|from
parameter_list|()
block|{
return|return
name|from
return|;
block|}
DECL|method|to
specifier|public
name|GeoDistanceRangeQueryBuilder
name|to
parameter_list|(
name|String
name|to
parameter_list|)
block|{
if|if
condition|(
name|to
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"[to] must not be null"
argument_list|)
throw|;
block|}
name|this
operator|.
name|to
operator|=
name|to
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|to
specifier|public
name|GeoDistanceRangeQueryBuilder
name|to
parameter_list|(
name|Number
name|to
parameter_list|)
block|{
if|if
condition|(
name|to
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"[to] must not be null"
argument_list|)
throw|;
block|}
name|this
operator|.
name|to
operator|=
name|to
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|to
specifier|public
name|Object
name|to
parameter_list|()
block|{
return|return
name|to
return|;
block|}
DECL|method|includeLower
specifier|public
name|GeoDistanceRangeQueryBuilder
name|includeLower
parameter_list|(
name|boolean
name|includeLower
parameter_list|)
block|{
name|this
operator|.
name|includeLower
operator|=
name|includeLower
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|includeLower
specifier|public
name|boolean
name|includeLower
parameter_list|()
block|{
return|return
name|includeLower
return|;
block|}
DECL|method|includeUpper
specifier|public
name|GeoDistanceRangeQueryBuilder
name|includeUpper
parameter_list|(
name|boolean
name|includeUpper
parameter_list|)
block|{
name|this
operator|.
name|includeUpper
operator|=
name|includeUpper
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|includeUpper
specifier|public
name|boolean
name|includeUpper
parameter_list|()
block|{
return|return
name|includeUpper
return|;
block|}
DECL|method|geoDistance
specifier|public
name|GeoDistanceRangeQueryBuilder
name|geoDistance
parameter_list|(
name|GeoDistance
name|geoDistance
parameter_list|)
block|{
if|if
condition|(
name|geoDistance
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"geoDistance calculation mode must not be null"
argument_list|)
throw|;
block|}
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
DECL|method|geoDistance
specifier|public
name|GeoDistance
name|geoDistance
parameter_list|()
block|{
return|return
name|geoDistance
return|;
block|}
DECL|method|unit
specifier|public
name|GeoDistanceRangeQueryBuilder
name|unit
parameter_list|(
name|DistanceUnit
name|unit
parameter_list|)
block|{
if|if
condition|(
name|unit
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"distance unit must not be null"
argument_list|)
throw|;
block|}
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
DECL|method|unit
specifier|public
name|DistanceUnit
name|unit
parameter_list|()
block|{
return|return
name|unit
return|;
block|}
DECL|method|optimizeBbox
specifier|public
name|GeoDistanceRangeQueryBuilder
name|optimizeBbox
parameter_list|(
name|String
name|optimizeBbox
parameter_list|)
block|{
if|if
condition|(
name|optimizeBbox
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"optimizeBbox must not be null"
argument_list|)
throw|;
block|}
switch|switch
condition|(
name|optimizeBbox
condition|)
block|{
case|case
literal|"none"
case|:
case|case
literal|"memory"
case|:
case|case
literal|"indexed"
case|:
break|break;
default|default:
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"optimizeBbox must be one of [none, memory, indexed]"
argument_list|)
throw|;
block|}
name|this
operator|.
name|optimizeBbox
operator|=
name|optimizeBbox
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|optimizeBbox
specifier|public
name|String
name|optimizeBbox
parameter_list|()
block|{
return|return
name|optimizeBbox
return|;
block|}
comment|/** Set validation method for coordinates. */
DECL|method|setValidationMethod
specifier|public
name|GeoDistanceRangeQueryBuilder
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
comment|/** Returns validation method for coordinates. */
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
annotation|@
name|Override
DECL|method|doToQuery
specifier|protected
name|Query
name|doToQuery
parameter_list|(
name|QueryShardContext
name|context
parameter_list|)
throws|throws
name|IOException
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
specifier|final
name|boolean
name|indexCreatedBeforeV2_0
init|=
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
decl_stmt|;
specifier|final
name|boolean
name|indexCreatedBeforeV2_2
init|=
name|context
operator|.
name|indexVersionCreated
argument_list|()
operator|.
name|before
argument_list|(
name|Version
operator|.
name|V_2_2_0
argument_list|)
decl_stmt|;
comment|// validation was not available prior to 2.x, so to support bwc
comment|// percolation queries we only ignore_malformed on 2.x created indexes
if|if
condition|(
operator|!
name|indexCreatedBeforeV2_0
operator|&&
operator|!
name|GeoValidationMethod
operator|.
name|isIgnoreMalformed
argument_list|(
name|validationMethod
argument_list|)
condition|)
block|{
if|if
condition|(
operator|!
name|GeoUtils
operator|.
name|isValidLatitude
argument_list|(
name|point
operator|.
name|lat
argument_list|()
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|QueryShardException
argument_list|(
name|context
argument_list|,
literal|"illegal latitude value [{}] for [{}]"
argument_list|,
name|point
operator|.
name|lat
argument_list|()
argument_list|,
name|NAME
argument_list|)
throw|;
block|}
if|if
condition|(
operator|!
name|GeoUtils
operator|.
name|isValidLongitude
argument_list|(
name|point
operator|.
name|lon
argument_list|()
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|QueryShardException
argument_list|(
name|context
argument_list|,
literal|"illegal longitude value [{}] for [{}]"
argument_list|,
name|point
operator|.
name|lon
argument_list|()
argument_list|,
name|NAME
argument_list|)
throw|;
block|}
block|}
name|GeoPoint
name|point
init|=
operator|new
name|GeoPoint
argument_list|(
name|this
operator|.
name|point
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
name|GeoUtils
operator|.
name|normalizePoint
argument_list|(
name|point
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
name|Double
name|fromValue
decl_stmt|;
name|Double
name|toValue
decl_stmt|;
if|if
condition|(
name|from
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|from
operator|instanceof
name|Number
condition|)
block|{
name|fromValue
operator|=
name|unit
operator|.
name|toMeters
argument_list|(
operator|(
operator|(
name|Number
operator|)
name|from
operator|)
operator|.
name|doubleValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|fromValue
operator|=
name|DistanceUnit
operator|.
name|parse
argument_list|(
operator|(
name|String
operator|)
name|from
argument_list|,
name|unit
argument_list|,
name|DistanceUnit
operator|.
name|DEFAULT
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|indexCreatedBeforeV2_2
operator|==
literal|true
condition|)
block|{
name|fromValue
operator|=
name|geoDistance
operator|.
name|normalize
argument_list|(
name|fromValue
argument_list|,
name|DistanceUnit
operator|.
name|DEFAULT
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|fromValue
operator|=
operator|new
name|Double
argument_list|(
literal|0
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
if|if
condition|(
name|to
operator|instanceof
name|Number
condition|)
block|{
name|toValue
operator|=
name|unit
operator|.
name|toMeters
argument_list|(
operator|(
operator|(
name|Number
operator|)
name|to
operator|)
operator|.
name|doubleValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|toValue
operator|=
name|DistanceUnit
operator|.
name|parse
argument_list|(
operator|(
name|String
operator|)
name|to
argument_list|,
name|unit
argument_list|,
name|DistanceUnit
operator|.
name|DEFAULT
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|indexCreatedBeforeV2_2
operator|==
literal|true
condition|)
block|{
name|toValue
operator|=
name|geoDistance
operator|.
name|normalize
argument_list|(
name|toValue
argument_list|,
name|DistanceUnit
operator|.
name|DEFAULT
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|toValue
operator|=
name|GeoUtils
operator|.
name|maxRadialDistance
argument_list|(
name|point
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|indexCreatedBeforeV2_2
operator|==
literal|true
condition|)
block|{
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
return|return
operator|new
name|GeoDistanceRangeQuery
argument_list|(
name|point
argument_list|,
name|fromValue
argument_list|,
name|toValue
argument_list|,
name|includeLower
argument_list|,
name|includeUpper
argument_list|,
name|geoDistance
argument_list|,
name|geoFieldType
argument_list|,
name|indexFieldData
argument_list|,
name|optimizeBbox
argument_list|)
return|;
block|}
return|return
operator|new
name|GeoPointDistanceRangeQuery
argument_list|(
name|fieldType
operator|.
name|names
argument_list|()
operator|.
name|fullName
argument_list|()
argument_list|,
name|point
operator|.
name|lon
argument_list|()
argument_list|,
name|point
operator|.
name|lat
argument_list|()
argument_list|,
operator|(
name|includeLower
operator|)
condition|?
name|fromValue
else|:
name|fromValue
operator|+
name|TOLERANCE
argument_list|,
operator|(
name|includeUpper
operator|)
condition|?
name|toValue
else|:
name|toValue
operator|-
name|TOLERANCE
argument_list|)
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
name|startArray
argument_list|(
name|fieldName
argument_list|)
operator|.
name|value
argument_list|(
name|point
operator|.
name|lon
argument_list|()
argument_list|)
operator|.
name|value
argument_list|(
name|point
operator|.
name|lat
argument_list|()
argument_list|)
operator|.
name|endArray
argument_list|()
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|GeoDistanceRangeQueryParser
operator|.
name|FROM_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|from
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|GeoDistanceRangeQueryParser
operator|.
name|TO_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|to
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|GeoDistanceRangeQueryParser
operator|.
name|INCLUDE_LOWER_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|includeLower
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|GeoDistanceRangeQueryParser
operator|.
name|INCLUDE_UPPER_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|includeUpper
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|GeoDistanceRangeQueryParser
operator|.
name|UNIT_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|unit
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|GeoDistanceRangeQueryParser
operator|.
name|DISTANCE_TYPE_FIELD
operator|.
name|getPreferredName
argument_list|()
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
name|builder
operator|.
name|field
argument_list|(
name|GeoDistanceRangeQueryParser
operator|.
name|OPTIMIZE_BBOX_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|optimizeBbox
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|GeoDistanceRangeQueryParser
operator|.
name|VALIDATION_METHOD
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|validationMethod
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
DECL|method|doReadFrom
specifier|protected
name|GeoDistanceRangeQueryBuilder
name|doReadFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|GeoDistanceRangeQueryBuilder
name|queryBuilder
init|=
operator|new
name|GeoDistanceRangeQueryBuilder
argument_list|(
name|in
operator|.
name|readString
argument_list|()
argument_list|,
name|in
operator|.
name|readGeoPoint
argument_list|()
argument_list|)
decl_stmt|;
name|queryBuilder
operator|.
name|from
operator|=
name|in
operator|.
name|readGenericValue
argument_list|()
expr_stmt|;
name|queryBuilder
operator|.
name|to
operator|=
name|in
operator|.
name|readGenericValue
argument_list|()
expr_stmt|;
name|queryBuilder
operator|.
name|includeLower
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
name|queryBuilder
operator|.
name|includeUpper
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
name|queryBuilder
operator|.
name|unit
operator|=
name|DistanceUnit
operator|.
name|valueOf
argument_list|(
name|in
operator|.
name|readString
argument_list|()
argument_list|)
expr_stmt|;
name|queryBuilder
operator|.
name|geoDistance
operator|=
name|GeoDistance
operator|.
name|readGeoDistanceFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|queryBuilder
operator|.
name|optimizeBbox
operator|=
name|in
operator|.
name|readString
argument_list|()
expr_stmt|;
name|queryBuilder
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
name|queryBuilder
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
name|point
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeGenericValue
argument_list|(
name|from
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeGenericValue
argument_list|(
name|to
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|includeLower
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|includeUpper
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeString
argument_list|(
name|unit
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
name|geoDistance
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
empty_stmt|;
name|out
operator|.
name|writeString
argument_list|(
name|optimizeBbox
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
DECL|method|doEquals
specifier|protected
name|boolean
name|doEquals
parameter_list|(
name|GeoDistanceRangeQueryBuilder
name|other
parameter_list|)
block|{
return|return
operator|(
operator|(
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
operator|)
operator|&&
operator|(
name|Objects
operator|.
name|equals
argument_list|(
name|point
argument_list|,
name|other
operator|.
name|point
argument_list|)
operator|)
operator|&&
operator|(
name|Objects
operator|.
name|equals
argument_list|(
name|from
argument_list|,
name|other
operator|.
name|from
argument_list|)
operator|)
operator|&&
operator|(
name|Objects
operator|.
name|equals
argument_list|(
name|to
argument_list|,
name|other
operator|.
name|to
argument_list|)
operator|)
operator|&&
operator|(
name|Objects
operator|.
name|equals
argument_list|(
name|includeUpper
argument_list|,
name|other
operator|.
name|includeUpper
argument_list|)
operator|)
operator|&&
operator|(
name|Objects
operator|.
name|equals
argument_list|(
name|includeLower
argument_list|,
name|other
operator|.
name|includeLower
argument_list|)
operator|)
operator|&&
operator|(
name|Objects
operator|.
name|equals
argument_list|(
name|geoDistance
argument_list|,
name|other
operator|.
name|geoDistance
argument_list|)
operator|)
operator|&&
operator|(
name|Objects
operator|.
name|equals
argument_list|(
name|optimizeBbox
argument_list|,
name|other
operator|.
name|optimizeBbox
argument_list|)
operator|)
operator|&&
operator|(
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
operator|)
operator|)
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
name|fieldName
argument_list|,
name|point
argument_list|,
name|from
argument_list|,
name|to
argument_list|,
name|includeUpper
argument_list|,
name|includeLower
argument_list|,
name|geoDistance
argument_list|,
name|optimizeBbox
argument_list|,
name|validationMethod
argument_list|)
return|;
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

