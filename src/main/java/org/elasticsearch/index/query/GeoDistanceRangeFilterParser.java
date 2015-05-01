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
name|Filter
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
name|QueryCachingPolicy
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
name|inject
operator|.
name|Inject
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
name|lucene
operator|.
name|HashedBytesRef
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
name|mapper
operator|.
name|MapperService
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
name|GeoPointFieldMapper
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
name|GeoDistanceRangeFilter
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
comment|/**  *<pre>  * {  *     "name.lat" : 1.1,  *     "name.lon" : 1.2,  * }  *</pre>  */
end_comment

begin_class
DECL|class|GeoDistanceRangeFilterParser
specifier|public
class|class
name|GeoDistanceRangeFilterParser
extends|extends
name|BaseFilterParserTemp
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
annotation|@
name|Inject
DECL|method|GeoDistanceRangeFilterParser
specifier|public
name|GeoDistanceRangeFilterParser
parameter_list|()
block|{     }
annotation|@
name|Override
DECL|method|names
specifier|public
name|String
index|[]
name|names
parameter_list|()
block|{
return|return
operator|new
name|String
index|[]
block|{
name|NAME
block|,
literal|"geoDistanceRange"
block|}
return|;
block|}
annotation|@
name|Override
DECL|method|parse
specifier|public
name|Filter
name|parse
parameter_list|(
name|QueryParseContext
name|parseContext
parameter_list|)
throws|throws
name|IOException
throws|,
name|QueryParsingException
block|{
name|XContentParser
name|parser
init|=
name|parseContext
operator|.
name|parser
argument_list|()
decl_stmt|;
name|XContentParser
operator|.
name|Token
name|token
decl_stmt|;
name|QueryCachingPolicy
name|cache
init|=
name|parseContext
operator|.
name|autoFilterCachePolicy
argument_list|()
decl_stmt|;
name|HashedBytesRef
name|cacheKey
init|=
literal|null
decl_stmt|;
name|String
name|filterName
init|=
literal|null
decl_stmt|;
name|String
name|currentFieldName
init|=
literal|null
decl_stmt|;
name|GeoPoint
name|point
init|=
operator|new
name|GeoPoint
argument_list|()
decl_stmt|;
name|String
name|fieldName
init|=
literal|null
decl_stmt|;
name|Object
name|vFrom
init|=
literal|null
decl_stmt|;
name|Object
name|vTo
init|=
literal|null
decl_stmt|;
name|boolean
name|includeLower
init|=
literal|true
decl_stmt|;
name|boolean
name|includeUpper
init|=
literal|true
decl_stmt|;
name|DistanceUnit
name|unit
init|=
name|DistanceUnit
operator|.
name|DEFAULT
decl_stmt|;
name|GeoDistance
name|geoDistance
init|=
name|GeoDistance
operator|.
name|DEFAULT
decl_stmt|;
name|String
name|optimizeBbox
init|=
literal|"memory"
decl_stmt|;
name|boolean
name|normalizeLon
init|=
literal|true
decl_stmt|;
name|boolean
name|normalizeLat
init|=
literal|true
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
name|START_ARRAY
condition|)
block|{
name|GeoUtils
operator|.
name|parseGeoPoint
argument_list|(
name|parser
argument_list|,
name|point
argument_list|)
expr_stmt|;
name|fieldName
operator|=
name|currentFieldName
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
name|START_OBJECT
condition|)
block|{
comment|// the json in the format of -> field : { lat : 30, lon : 12 }
name|fieldName
operator|=
name|currentFieldName
expr_stmt|;
name|GeoUtils
operator|.
name|parseGeoPoint
argument_list|(
name|parser
argument_list|,
name|point
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|token
operator|.
name|isValue
argument_list|()
condition|)
block|{
if|if
condition|(
name|currentFieldName
operator|.
name|equals
argument_list|(
literal|"from"
argument_list|)
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
name|VALUE_NULL
condition|)
block|{                     }
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
name|vFrom
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
comment|// a String
block|}
else|else
block|{
name|vFrom
operator|=
name|parser
operator|.
name|numberValue
argument_list|()
expr_stmt|;
comment|// a Number
block|}
block|}
elseif|else
if|if
condition|(
name|currentFieldName
operator|.
name|equals
argument_list|(
literal|"to"
argument_list|)
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
name|VALUE_NULL
condition|)
block|{                     }
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
name|vTo
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
comment|// a String
block|}
else|else
block|{
name|vTo
operator|=
name|parser
operator|.
name|numberValue
argument_list|()
expr_stmt|;
comment|// a Number
block|}
block|}
elseif|else
if|if
condition|(
literal|"include_lower"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
operator|||
literal|"includeLower"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|includeLower
operator|=
name|parser
operator|.
name|booleanValue
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"include_upper"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
operator|||
literal|"includeUpper"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|includeUpper
operator|=
name|parser
operator|.
name|booleanValue
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"gt"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
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
name|VALUE_NULL
condition|)
block|{                     }
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
name|vFrom
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
comment|// a String
block|}
else|else
block|{
name|vFrom
operator|=
name|parser
operator|.
name|numberValue
argument_list|()
expr_stmt|;
comment|// a Number
block|}
name|includeLower
operator|=
literal|false
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"gte"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
operator|||
literal|"ge"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
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
name|VALUE_NULL
condition|)
block|{                     }
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
name|vFrom
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
comment|// a String
block|}
else|else
block|{
name|vFrom
operator|=
name|parser
operator|.
name|numberValue
argument_list|()
expr_stmt|;
comment|// a Number
block|}
name|includeLower
operator|=
literal|true
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"lt"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
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
name|VALUE_NULL
condition|)
block|{                     }
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
name|vTo
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
comment|// a String
block|}
else|else
block|{
name|vTo
operator|=
name|parser
operator|.
name|numberValue
argument_list|()
expr_stmt|;
comment|// a Number
block|}
name|includeUpper
operator|=
literal|false
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"lte"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
operator|||
literal|"le"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
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
name|VALUE_NULL
condition|)
block|{                     }
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
name|vTo
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
comment|// a String
block|}
else|else
block|{
name|vTo
operator|=
name|parser
operator|.
name|numberValue
argument_list|()
expr_stmt|;
comment|// a Number
block|}
name|includeUpper
operator|=
literal|true
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|currentFieldName
operator|.
name|equals
argument_list|(
literal|"unit"
argument_list|)
condition|)
block|{
name|unit
operator|=
name|DistanceUnit
operator|.
name|fromString
argument_list|(
name|parser
operator|.
name|text
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|currentFieldName
operator|.
name|equals
argument_list|(
literal|"distance_type"
argument_list|)
operator|||
name|currentFieldName
operator|.
name|equals
argument_list|(
literal|"distanceType"
argument_list|)
condition|)
block|{
name|geoDistance
operator|=
name|GeoDistance
operator|.
name|fromString
argument_list|(
name|parser
operator|.
name|text
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|currentFieldName
operator|.
name|endsWith
argument_list|(
name|GeoPointFieldMapper
operator|.
name|Names
operator|.
name|LAT_SUFFIX
argument_list|)
condition|)
block|{
name|point
operator|.
name|resetLat
argument_list|(
name|parser
operator|.
name|doubleValue
argument_list|()
argument_list|)
expr_stmt|;
name|fieldName
operator|=
name|currentFieldName
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|currentFieldName
operator|.
name|length
argument_list|()
operator|-
name|GeoPointFieldMapper
operator|.
name|Names
operator|.
name|LAT_SUFFIX
operator|.
name|length
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|currentFieldName
operator|.
name|endsWith
argument_list|(
name|GeoPointFieldMapper
operator|.
name|Names
operator|.
name|LON_SUFFIX
argument_list|)
condition|)
block|{
name|point
operator|.
name|resetLon
argument_list|(
name|parser
operator|.
name|doubleValue
argument_list|()
argument_list|)
expr_stmt|;
name|fieldName
operator|=
name|currentFieldName
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|currentFieldName
operator|.
name|length
argument_list|()
operator|-
name|GeoPointFieldMapper
operator|.
name|Names
operator|.
name|LON_SUFFIX
operator|.
name|length
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|currentFieldName
operator|.
name|endsWith
argument_list|(
name|GeoPointFieldMapper
operator|.
name|Names
operator|.
name|GEOHASH_SUFFIX
argument_list|)
condition|)
block|{
name|GeoHashUtils
operator|.
name|decode
argument_list|(
name|parser
operator|.
name|text
argument_list|()
argument_list|,
name|point
argument_list|)
expr_stmt|;
name|fieldName
operator|=
name|currentFieldName
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|currentFieldName
operator|.
name|length
argument_list|()
operator|-
name|GeoPointFieldMapper
operator|.
name|Names
operator|.
name|GEOHASH_SUFFIX
operator|.
name|length
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"_name"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|filterName
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"_cache"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|cache
operator|=
name|parseContext
operator|.
name|parseFilterCachePolicy
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"_cache_key"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
operator|||
literal|"_cacheKey"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|cacheKey
operator|=
operator|new
name|HashedBytesRef
argument_list|(
name|parser
operator|.
name|text
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"optimize_bbox"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
operator|||
literal|"optimizeBbox"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|optimizeBbox
operator|=
name|parser
operator|.
name|textOrNull
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"normalize"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|normalizeLat
operator|=
name|parser
operator|.
name|booleanValue
argument_list|()
expr_stmt|;
name|normalizeLon
operator|=
name|parser
operator|.
name|booleanValue
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|point
operator|.
name|resetFromString
argument_list|(
name|parser
operator|.
name|text
argument_list|()
argument_list|)
expr_stmt|;
name|fieldName
operator|=
name|currentFieldName
expr_stmt|;
block|}
block|}
block|}
name|Double
name|from
init|=
literal|null
decl_stmt|;
name|Double
name|to
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|vFrom
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|vFrom
operator|instanceof
name|Number
condition|)
block|{
name|from
operator|=
name|unit
operator|.
name|toMeters
argument_list|(
operator|(
operator|(
name|Number
operator|)
name|vFrom
operator|)
operator|.
name|doubleValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|from
operator|=
name|DistanceUnit
operator|.
name|parse
argument_list|(
operator|(
name|String
operator|)
name|vFrom
argument_list|,
name|unit
argument_list|,
name|DistanceUnit
operator|.
name|DEFAULT
argument_list|)
expr_stmt|;
block|}
name|from
operator|=
name|geoDistance
operator|.
name|normalize
argument_list|(
name|from
argument_list|,
name|DistanceUnit
operator|.
name|DEFAULT
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|vTo
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|vTo
operator|instanceof
name|Number
condition|)
block|{
name|to
operator|=
name|unit
operator|.
name|toMeters
argument_list|(
operator|(
operator|(
name|Number
operator|)
name|vTo
operator|)
operator|.
name|doubleValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|to
operator|=
name|DistanceUnit
operator|.
name|parse
argument_list|(
operator|(
name|String
operator|)
name|vTo
argument_list|,
name|unit
argument_list|,
name|DistanceUnit
operator|.
name|DEFAULT
argument_list|)
expr_stmt|;
block|}
name|to
operator|=
name|geoDistance
operator|.
name|normalize
argument_list|(
name|to
argument_list|,
name|DistanceUnit
operator|.
name|DEFAULT
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|normalizeLat
operator|||
name|normalizeLon
condition|)
block|{
name|GeoUtils
operator|.
name|normalizePoint
argument_list|(
name|point
argument_list|,
name|normalizeLat
argument_list|,
name|normalizeLon
argument_list|)
expr_stmt|;
block|}
name|MapperService
operator|.
name|SmartNameFieldMappers
name|smartMappers
init|=
name|parseContext
operator|.
name|smartFieldMappers
argument_list|(
name|fieldName
argument_list|)
decl_stmt|;
if|if
condition|(
name|smartMappers
operator|==
literal|null
operator|||
operator|!
name|smartMappers
operator|.
name|hasMapper
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|QueryParsingException
argument_list|(
name|parseContext
argument_list|,
literal|"failed to find geo_point field ["
operator|+
name|fieldName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|FieldMapper
argument_list|<
name|?
argument_list|>
name|mapper
init|=
name|smartMappers
operator|.
name|mapper
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
operator|(
name|mapper
operator|instanceof
name|GeoPointFieldMapper
operator|)
condition|)
block|{
throw|throw
operator|new
name|QueryParsingException
argument_list|(
name|parseContext
argument_list|,
literal|"field ["
operator|+
name|fieldName
operator|+
literal|"] is not a geo_point field"
argument_list|)
throw|;
block|}
name|GeoPointFieldMapper
name|geoMapper
init|=
operator|(
operator|(
name|GeoPointFieldMapper
operator|)
name|mapper
operator|)
decl_stmt|;
name|IndexGeoPointFieldData
name|indexFieldData
init|=
name|parseContext
operator|.
name|getForField
argument_list|(
name|mapper
argument_list|)
decl_stmt|;
name|Filter
name|filter
init|=
operator|new
name|GeoDistanceRangeFilter
argument_list|(
name|point
argument_list|,
name|from
argument_list|,
name|to
argument_list|,
name|includeLower
argument_list|,
name|includeUpper
argument_list|,
name|geoDistance
argument_list|,
name|geoMapper
argument_list|,
name|indexFieldData
argument_list|,
name|optimizeBbox
argument_list|)
decl_stmt|;
if|if
condition|(
name|cache
operator|!=
literal|null
condition|)
block|{
name|filter
operator|=
name|parseContext
operator|.
name|cacheFilter
argument_list|(
name|filter
argument_list|,
name|cacheKey
argument_list|,
name|cache
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|filterName
operator|!=
literal|null
condition|)
block|{
name|parseContext
operator|.
name|addNamedFilter
argument_list|(
name|filterName
argument_list|,
name|filter
argument_list|)
expr_stmt|;
block|}
return|return
name|filter
return|;
block|}
block|}
end_class

end_unit

