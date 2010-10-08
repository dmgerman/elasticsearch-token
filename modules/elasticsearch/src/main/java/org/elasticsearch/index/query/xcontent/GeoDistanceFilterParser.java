begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.query.xcontent
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|xcontent
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
name|lucene
operator|.
name|geo
operator|.
name|GeoDistanceFilter
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
name|settings
operator|.
name|Settings
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
name|AbstractIndexComponent
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
name|Index
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
name|xcontent
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
name|query
operator|.
name|QueryParsingException
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
name|settings
operator|.
name|IndexSettings
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
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|support
operator|.
name|QueryParsers
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  *<pre>  * {  *     "name.lat" : 1.1,  *     "name.lon" : 1.2,  * }  *</pre>  *  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|GeoDistanceFilterParser
specifier|public
class|class
name|GeoDistanceFilterParser
extends|extends
name|AbstractIndexComponent
implements|implements
name|XContentFilterParser
block|{
DECL|field|NAME
specifier|public
specifier|static
specifier|final
name|String
name|NAME
init|=
literal|"geo_distance"
decl_stmt|;
DECL|method|GeoDistanceFilterParser
annotation|@
name|Inject
specifier|public
name|GeoDistanceFilterParser
parameter_list|(
name|Index
name|index
parameter_list|,
annotation|@
name|IndexSettings
name|Settings
name|indexSettings
parameter_list|)
block|{
name|super
argument_list|(
name|index
argument_list|,
name|indexSettings
argument_list|)
expr_stmt|;
block|}
DECL|method|names
annotation|@
name|Override
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
literal|"geoDistance"
block|}
return|;
block|}
DECL|method|parse
annotation|@
name|Override
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
name|double
name|lat
init|=
literal|0
decl_stmt|;
name|double
name|lon
init|=
literal|0
decl_stmt|;
name|String
name|latFieldName
init|=
literal|null
decl_stmt|;
name|String
name|lonFieldName
init|=
literal|null
decl_stmt|;
name|double
name|distance
init|=
literal|0
decl_stmt|;
name|DistanceUnit
name|unit
init|=
literal|null
decl_stmt|;
name|GeoDistance
name|geoDistance
init|=
name|GeoDistance
operator|.
name|ARC
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
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
name|lat
operator|=
name|parser
operator|.
name|doubleValue
argument_list|()
expr_stmt|;
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
name|lon
operator|=
name|parser
operator|.
name|doubleValue
argument_list|()
expr_stmt|;
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
name|END_ARRAY
condition|)
block|{                  }
name|latFieldName
operator|=
name|currentFieldName
operator|+
literal|"."
operator|+
name|GeoPointFieldMapper
operator|.
name|Names
operator|.
name|LAT
expr_stmt|;
name|lonFieldName
operator|=
name|currentFieldName
operator|+
literal|"."
operator|+
name|GeoPointFieldMapper
operator|.
name|Names
operator|.
name|LON
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
name|String
name|currentName
init|=
name|parser
operator|.
name|currentName
argument_list|()
decl_stmt|;
name|latFieldName
operator|=
name|currentFieldName
operator|+
name|GeoPointFieldMapper
operator|.
name|Names
operator|.
name|LAT_SUFFIX
expr_stmt|;
name|lonFieldName
operator|=
name|currentFieldName
operator|+
name|GeoPointFieldMapper
operator|.
name|Names
operator|.
name|LON_SUFFIX
expr_stmt|;
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
name|currentName
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
operator|.
name|isValue
argument_list|()
condition|)
block|{
if|if
condition|(
name|currentName
operator|.
name|equals
argument_list|(
name|GeoPointFieldMapper
operator|.
name|Names
operator|.
name|LAT
argument_list|)
condition|)
block|{
name|lat
operator|=
name|parser
operator|.
name|doubleValue
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|currentName
operator|.
name|equals
argument_list|(
name|GeoPointFieldMapper
operator|.
name|Names
operator|.
name|LON
argument_list|)
condition|)
block|{
name|lon
operator|=
name|parser
operator|.
name|doubleValue
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|currentName
operator|.
name|equals
argument_list|(
name|GeoPointFieldMapper
operator|.
name|Names
operator|.
name|GEOHASH
argument_list|)
condition|)
block|{
name|double
index|[]
name|values
init|=
name|GeoHashUtils
operator|.
name|decode
argument_list|(
name|parser
operator|.
name|text
argument_list|()
argument_list|)
decl_stmt|;
name|lat
operator|=
name|values
index|[
literal|0
index|]
expr_stmt|;
name|lon
operator|=
name|values
index|[
literal|1
index|]
expr_stmt|;
block|}
block|}
block|}
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
literal|"distance"
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
name|VALUE_STRING
condition|)
block|{
name|distance
operator|=
name|DistanceUnit
operator|.
name|parse
argument_list|(
name|parser
operator|.
name|text
argument_list|()
argument_list|,
name|DistanceUnit
operator|.
name|KILOMETERS
argument_list|,
name|DistanceUnit
operator|.
name|MILES
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|distance
operator|=
name|parser
operator|.
name|doubleValue
argument_list|()
expr_stmt|;
block|}
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
name|lat
operator|=
name|parser
operator|.
name|doubleValue
argument_list|()
expr_stmt|;
name|latFieldName
operator|=
name|currentFieldName
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
name|lon
operator|=
name|parser
operator|.
name|doubleValue
argument_list|()
expr_stmt|;
name|lonFieldName
operator|=
name|currentFieldName
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
name|double
index|[]
name|values
init|=
name|GeoHashUtils
operator|.
name|decode
argument_list|(
name|parser
operator|.
name|text
argument_list|()
argument_list|)
decl_stmt|;
name|lat
operator|=
name|values
index|[
literal|0
index|]
expr_stmt|;
name|lon
operator|=
name|values
index|[
literal|1
index|]
expr_stmt|;
name|latFieldName
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
operator|+
name|GeoPointFieldMapper
operator|.
name|Names
operator|.
name|LAT_SUFFIX
expr_stmt|;
name|lonFieldName
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
operator|+
name|GeoPointFieldMapper
operator|.
name|Names
operator|.
name|LON_SUFFIX
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
else|else
block|{
comment|// assume the value is the actual value
name|String
name|value
init|=
name|parser
operator|.
name|text
argument_list|()
decl_stmt|;
name|int
name|comma
init|=
name|value
operator|.
name|indexOf
argument_list|(
literal|','
argument_list|)
decl_stmt|;
if|if
condition|(
name|comma
operator|!=
operator|-
literal|1
condition|)
block|{
name|lat
operator|=
name|Double
operator|.
name|parseDouble
argument_list|(
name|value
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|comma
argument_list|)
operator|.
name|trim
argument_list|()
argument_list|)
expr_stmt|;
name|lon
operator|=
name|Double
operator|.
name|parseDouble
argument_list|(
name|value
operator|.
name|substring
argument_list|(
name|comma
operator|+
literal|1
argument_list|)
operator|.
name|trim
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|double
index|[]
name|values
init|=
name|GeoHashUtils
operator|.
name|decode
argument_list|(
name|value
argument_list|)
decl_stmt|;
name|lat
operator|=
name|values
index|[
literal|0
index|]
expr_stmt|;
name|lon
operator|=
name|values
index|[
literal|1
index|]
expr_stmt|;
block|}
name|latFieldName
operator|=
name|currentFieldName
operator|+
name|GeoPointFieldMapper
operator|.
name|Names
operator|.
name|LAT_SUFFIX
expr_stmt|;
name|lonFieldName
operator|=
name|currentFieldName
operator|+
name|GeoPointFieldMapper
operator|.
name|Names
operator|.
name|LON_SUFFIX
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|unit
operator|!=
literal|null
condition|)
block|{
name|distance
operator|=
name|unit
operator|.
name|toMiles
argument_list|(
name|distance
argument_list|)
expr_stmt|;
block|}
name|MapperService
name|mapperService
init|=
name|parseContext
operator|.
name|mapperService
argument_list|()
decl_stmt|;
name|FieldMapper
name|mapper
init|=
name|mapperService
operator|.
name|smartNameFieldMapper
argument_list|(
name|latFieldName
argument_list|)
decl_stmt|;
if|if
condition|(
name|mapper
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|QueryParsingException
argument_list|(
name|index
argument_list|,
literal|"failed to find lat field ["
operator|+
name|latFieldName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|latFieldName
operator|=
name|mapper
operator|.
name|names
argument_list|()
operator|.
name|indexName
argument_list|()
expr_stmt|;
name|mapper
operator|=
name|mapperService
operator|.
name|smartNameFieldMapper
argument_list|(
name|lonFieldName
argument_list|)
expr_stmt|;
if|if
condition|(
name|mapper
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|QueryParsingException
argument_list|(
name|index
argument_list|,
literal|"failed to find lon field ["
operator|+
name|lonFieldName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|lonFieldName
operator|=
name|mapper
operator|.
name|names
argument_list|()
operator|.
name|indexName
argument_list|()
expr_stmt|;
name|Filter
name|filter
init|=
operator|new
name|GeoDistanceFilter
argument_list|(
name|lat
argument_list|,
name|lon
argument_list|,
name|distance
argument_list|,
name|geoDistance
argument_list|,
name|latFieldName
argument_list|,
name|lonFieldName
argument_list|,
name|mapper
operator|.
name|fieldDataType
argument_list|()
argument_list|,
name|parseContext
operator|.
name|indexCache
argument_list|()
operator|.
name|fieldData
argument_list|()
argument_list|)
decl_stmt|;
name|filter
operator|=
name|wrapSmartNameFilter
argument_list|(
name|filter
argument_list|,
name|parseContext
operator|.
name|smartFieldMappers
argument_list|(
name|latFieldName
argument_list|)
argument_list|,
name|parseContext
argument_list|)
expr_stmt|;
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

