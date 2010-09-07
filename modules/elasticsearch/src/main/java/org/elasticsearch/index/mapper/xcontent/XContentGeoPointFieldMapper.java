begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.mapper.xcontent
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
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
name|document
operator|.
name|Field
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticSearchIllegalArgumentException
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
name|common
operator|.
name|xcontent
operator|.
name|builder
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
name|common
operator|.
name|xcontent
operator|.
name|support
operator|.
name|XContentMapValues
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
name|FieldMapperListener
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
name|MapperParsingException
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
name|MergeMappingException
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
name|Map
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
name|mapper
operator|.
name|xcontent
operator|.
name|XContentMapperBuilders
operator|.
name|*
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
name|mapper
operator|.
name|xcontent
operator|.
name|XContentTypeParsers
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * Parsing: We handle:  *  * - "field" : "geo_hash"  * - "field" : "lat,lon"  * - "field" : {  * "lat" : 1.1,  * "lon" : 2.1  * }  *  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|XContentGeoPointFieldMapper
specifier|public
class|class
name|XContentGeoPointFieldMapper
implements|implements
name|XContentMapper
block|{
DECL|field|CONTENT_TYPE
specifier|public
specifier|static
specifier|final
name|String
name|CONTENT_TYPE
init|=
literal|"geo_point"
decl_stmt|;
DECL|class|Names
specifier|public
specifier|static
class|class
name|Names
block|{
DECL|field|LAT
specifier|public
specifier|static
specifier|final
name|String
name|LAT
init|=
literal|"lat"
decl_stmt|;
DECL|field|LAT_SUFFIX
specifier|public
specifier|static
specifier|final
name|String
name|LAT_SUFFIX
init|=
literal|"."
operator|+
name|LAT
decl_stmt|;
DECL|field|LON
specifier|public
specifier|static
specifier|final
name|String
name|LON
init|=
literal|"lon"
decl_stmt|;
DECL|field|LON_SUFFIX
specifier|public
specifier|static
specifier|final
name|String
name|LON_SUFFIX
init|=
literal|"."
operator|+
name|LON
decl_stmt|;
DECL|field|GEOHASH
specifier|public
specifier|static
specifier|final
name|String
name|GEOHASH
init|=
literal|"geohash"
decl_stmt|;
DECL|field|GEOHASH_SUFFIX
specifier|public
specifier|static
specifier|final
name|String
name|GEOHASH_SUFFIX
init|=
literal|"."
operator|+
name|GEOHASH
decl_stmt|;
block|}
DECL|class|Defaults
specifier|public
specifier|static
class|class
name|Defaults
block|{
DECL|field|PATH_TYPE
specifier|public
specifier|static
specifier|final
name|ContentPath
operator|.
name|Type
name|PATH_TYPE
init|=
name|ContentPath
operator|.
name|Type
operator|.
name|FULL
decl_stmt|;
DECL|field|STORE
specifier|public
specifier|static
specifier|final
name|Field
operator|.
name|Store
name|STORE
init|=
name|Field
operator|.
name|Store
operator|.
name|NO
decl_stmt|;
block|}
DECL|class|Builder
specifier|public
specifier|static
class|class
name|Builder
extends|extends
name|XContentMapper
operator|.
name|Builder
argument_list|<
name|Builder
argument_list|,
name|XContentGeoPointFieldMapper
argument_list|>
block|{
DECL|field|pathType
specifier|private
name|ContentPath
operator|.
name|Type
name|pathType
init|=
name|Defaults
operator|.
name|PATH_TYPE
decl_stmt|;
DECL|field|enableLatLon
specifier|private
name|boolean
name|enableLatLon
init|=
literal|true
decl_stmt|;
DECL|field|enableGeohash
specifier|private
name|boolean
name|enableGeohash
init|=
literal|false
decl_stmt|;
DECL|field|resolution
specifier|private
name|String
name|resolution
init|=
literal|"64"
decl_stmt|;
DECL|field|precisionStep
specifier|private
name|Integer
name|precisionStep
decl_stmt|;
DECL|field|geohashPrecision
specifier|private
name|int
name|geohashPrecision
init|=
name|GeoHashUtils
operator|.
name|PRECISION
decl_stmt|;
DECL|field|store
specifier|private
name|Field
operator|.
name|Store
name|store
init|=
name|Defaults
operator|.
name|STORE
decl_stmt|;
DECL|method|Builder
specifier|public
name|Builder
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
name|this
operator|.
name|builder
operator|=
name|this
expr_stmt|;
block|}
DECL|method|pathType
specifier|public
name|Builder
name|pathType
parameter_list|(
name|ContentPath
operator|.
name|Type
name|pathType
parameter_list|)
block|{
name|this
operator|.
name|pathType
operator|=
name|pathType
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|enableLatLon
specifier|public
name|Builder
name|enableLatLon
parameter_list|(
name|boolean
name|enableLatLon
parameter_list|)
block|{
name|this
operator|.
name|enableLatLon
operator|=
name|enableLatLon
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|enableGeohash
specifier|public
name|Builder
name|enableGeohash
parameter_list|(
name|boolean
name|enableGeohash
parameter_list|)
block|{
name|this
operator|.
name|enableGeohash
operator|=
name|enableGeohash
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|resolution
specifier|public
name|Builder
name|resolution
parameter_list|(
name|String
name|resolution
parameter_list|)
block|{
name|this
operator|.
name|resolution
operator|=
name|resolution
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|precisionStep
specifier|public
name|Builder
name|precisionStep
parameter_list|(
name|int
name|precisionStep
parameter_list|)
block|{
name|this
operator|.
name|precisionStep
operator|=
name|precisionStep
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|geohashPrecision
specifier|public
name|Builder
name|geohashPrecision
parameter_list|(
name|int
name|geohashPrecision
parameter_list|)
block|{
name|this
operator|.
name|geohashPrecision
operator|=
name|geohashPrecision
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|store
specifier|public
name|Builder
name|store
parameter_list|(
name|Field
operator|.
name|Store
name|store
parameter_list|)
block|{
name|this
operator|.
name|store
operator|=
name|store
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|build
annotation|@
name|Override
specifier|public
name|XContentGeoPointFieldMapper
name|build
parameter_list|(
name|BuilderContext
name|context
parameter_list|)
block|{
name|ContentPath
operator|.
name|Type
name|origPathType
init|=
name|context
operator|.
name|path
argument_list|()
operator|.
name|pathType
argument_list|()
decl_stmt|;
name|context
operator|.
name|path
argument_list|()
operator|.
name|pathType
argument_list|(
name|pathType
argument_list|)
expr_stmt|;
name|XContentNumberFieldMapper
name|latMapper
init|=
literal|null
decl_stmt|;
name|XContentNumberFieldMapper
name|lonMapper
init|=
literal|null
decl_stmt|;
name|XContentStringFieldMapper
name|geohashMapper
init|=
literal|null
decl_stmt|;
name|context
operator|.
name|path
argument_list|()
operator|.
name|add
argument_list|(
name|name
argument_list|)
expr_stmt|;
if|if
condition|(
name|enableLatLon
condition|)
block|{
name|XContentNumberFieldMapper
operator|.
name|Builder
name|latMapperBuilder
decl_stmt|;
name|XContentNumberFieldMapper
operator|.
name|Builder
name|lonMapperBuilder
decl_stmt|;
if|if
condition|(
literal|"32"
operator|.
name|equals
argument_list|(
name|resolution
argument_list|)
condition|)
block|{
name|latMapperBuilder
operator|=
name|floatField
argument_list|(
name|Names
operator|.
name|LAT
argument_list|)
operator|.
name|includeInAll
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|lonMapperBuilder
operator|=
name|floatField
argument_list|(
name|Names
operator|.
name|LON
argument_list|)
operator|.
name|includeInAll
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"64"
operator|.
name|equals
argument_list|(
name|resolution
argument_list|)
condition|)
block|{
name|latMapperBuilder
operator|=
name|doubleField
argument_list|(
name|Names
operator|.
name|LAT
argument_list|)
operator|.
name|includeInAll
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|lonMapperBuilder
operator|=
name|doubleField
argument_list|(
name|Names
operator|.
name|LON
argument_list|)
operator|.
name|includeInAll
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|ElasticSearchIllegalArgumentException
argument_list|(
literal|"Can't handle geo_point resolution ["
operator|+
name|resolution
operator|+
literal|"]"
argument_list|)
throw|;
block|}
if|if
condition|(
name|precisionStep
operator|!=
literal|null
condition|)
block|{
name|latMapperBuilder
operator|.
name|precisionStep
argument_list|(
name|precisionStep
argument_list|)
expr_stmt|;
name|lonMapperBuilder
operator|.
name|precisionStep
argument_list|(
name|precisionStep
argument_list|)
expr_stmt|;
block|}
name|latMapper
operator|=
operator|(
name|XContentNumberFieldMapper
operator|)
name|latMapperBuilder
operator|.
name|includeInAll
argument_list|(
literal|false
argument_list|)
operator|.
name|store
argument_list|(
name|store
argument_list|)
operator|.
name|build
argument_list|(
name|context
argument_list|)
expr_stmt|;
name|lonMapper
operator|=
operator|(
name|XContentNumberFieldMapper
operator|)
name|lonMapperBuilder
operator|.
name|includeInAll
argument_list|(
literal|false
argument_list|)
operator|.
name|store
argument_list|(
name|store
argument_list|)
operator|.
name|build
argument_list|(
name|context
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|enableGeohash
condition|)
block|{
name|geohashMapper
operator|=
name|stringField
argument_list|(
name|Names
operator|.
name|GEOHASH
argument_list|)
operator|.
name|includeInAll
argument_list|(
literal|false
argument_list|)
operator|.
name|build
argument_list|(
name|context
argument_list|)
expr_stmt|;
block|}
name|context
operator|.
name|path
argument_list|()
operator|.
name|remove
argument_list|()
expr_stmt|;
name|context
operator|.
name|path
argument_list|()
operator|.
name|pathType
argument_list|(
name|origPathType
argument_list|)
expr_stmt|;
return|return
operator|new
name|XContentGeoPointFieldMapper
argument_list|(
name|name
argument_list|,
name|pathType
argument_list|,
name|enableLatLon
argument_list|,
name|enableGeohash
argument_list|,
name|resolution
argument_list|,
name|precisionStep
argument_list|,
name|geohashPrecision
argument_list|,
name|latMapper
argument_list|,
name|lonMapper
argument_list|,
name|geohashMapper
argument_list|)
return|;
block|}
block|}
DECL|class|TypeParser
specifier|public
specifier|static
class|class
name|TypeParser
implements|implements
name|XContentTypeParser
block|{
DECL|method|parse
annotation|@
name|Override
specifier|public
name|XContentMapper
operator|.
name|Builder
name|parse
parameter_list|(
name|String
name|name
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|node
parameter_list|,
name|ParserContext
name|parserContext
parameter_list|)
throws|throws
name|MapperParsingException
block|{
name|Builder
name|builder
init|=
operator|new
name|Builder
argument_list|(
name|name
argument_list|)
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|entry
range|:
name|node
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|fieldName
init|=
name|Strings
operator|.
name|toUnderscoreCase
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
decl_stmt|;
name|Object
name|fieldNode
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|fieldName
operator|.
name|equals
argument_list|(
literal|"path"
argument_list|)
condition|)
block|{
name|builder
operator|.
name|pathType
argument_list|(
name|parsePathType
argument_list|(
name|name
argument_list|,
name|fieldNode
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|fieldName
operator|.
name|equals
argument_list|(
literal|"store"
argument_list|)
condition|)
block|{
name|builder
operator|.
name|store
argument_list|(
name|parseStore
argument_list|(
name|name
argument_list|,
name|fieldNode
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|fieldName
operator|.
name|equals
argument_list|(
literal|"lat_lon"
argument_list|)
condition|)
block|{
name|builder
operator|.
name|enableLatLon
argument_list|(
name|XContentMapValues
operator|.
name|nodeBooleanValue
argument_list|(
name|fieldNode
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|fieldName
operator|.
name|equals
argument_list|(
literal|"geohash"
argument_list|)
condition|)
block|{
name|builder
operator|.
name|enableGeohash
argument_list|(
name|XContentMapValues
operator|.
name|nodeBooleanValue
argument_list|(
name|fieldNode
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|fieldName
operator|.
name|equals
argument_list|(
literal|"resolution"
argument_list|)
condition|)
block|{
name|builder
operator|.
name|resolution
argument_list|(
name|Integer
operator|.
name|toString
argument_list|(
name|XContentMapValues
operator|.
name|nodeIntegerValue
argument_list|(
name|fieldNode
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|fieldName
operator|.
name|equals
argument_list|(
literal|"precisionStep"
argument_list|)
condition|)
block|{
name|builder
operator|.
name|precisionStep
argument_list|(
name|XContentMapValues
operator|.
name|nodeIntegerValue
argument_list|(
name|fieldNode
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|fieldName
operator|.
name|equals
argument_list|(
literal|"geohash_precision"
argument_list|)
operator|||
name|fieldName
operator|.
name|equals
argument_list|(
literal|"geohashPrecision"
argument_list|)
condition|)
block|{
name|builder
operator|.
name|geohashPrecision
argument_list|(
name|XContentMapValues
operator|.
name|nodeIntegerValue
argument_list|(
name|fieldNode
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|builder
return|;
block|}
block|}
DECL|field|name
specifier|private
specifier|final
name|String
name|name
decl_stmt|;
DECL|field|pathType
specifier|private
specifier|final
name|ContentPath
operator|.
name|Type
name|pathType
decl_stmt|;
DECL|field|enableLatLon
specifier|private
specifier|final
name|boolean
name|enableLatLon
decl_stmt|;
DECL|field|enableGeohash
specifier|private
specifier|final
name|boolean
name|enableGeohash
decl_stmt|;
DECL|field|resolution
specifier|private
specifier|final
name|String
name|resolution
decl_stmt|;
DECL|field|precisionStep
specifier|private
specifier|final
name|Integer
name|precisionStep
decl_stmt|;
DECL|field|geohashPrecision
specifier|private
specifier|final
name|int
name|geohashPrecision
decl_stmt|;
DECL|field|latMapper
specifier|private
specifier|final
name|XContentNumberFieldMapper
name|latMapper
decl_stmt|;
DECL|field|lonMapper
specifier|private
specifier|final
name|XContentNumberFieldMapper
name|lonMapper
decl_stmt|;
DECL|field|geohashMapper
specifier|private
specifier|final
name|XContentStringFieldMapper
name|geohashMapper
decl_stmt|;
DECL|method|XContentGeoPointFieldMapper
specifier|public
name|XContentGeoPointFieldMapper
parameter_list|(
name|String
name|name
parameter_list|,
name|ContentPath
operator|.
name|Type
name|pathType
parameter_list|,
name|boolean
name|enableLatLon
parameter_list|,
name|boolean
name|enableGeohash
parameter_list|,
name|String
name|resolution
parameter_list|,
name|Integer
name|precisionStep
parameter_list|,
name|int
name|geohashPrecision
parameter_list|,
name|XContentNumberFieldMapper
name|latMapper
parameter_list|,
name|XContentNumberFieldMapper
name|lonMapper
parameter_list|,
name|XContentStringFieldMapper
name|geohashMapper
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
name|this
operator|.
name|pathType
operator|=
name|pathType
expr_stmt|;
name|this
operator|.
name|enableLatLon
operator|=
name|enableLatLon
expr_stmt|;
name|this
operator|.
name|enableGeohash
operator|=
name|enableGeohash
expr_stmt|;
name|this
operator|.
name|resolution
operator|=
name|resolution
expr_stmt|;
name|this
operator|.
name|precisionStep
operator|=
name|precisionStep
expr_stmt|;
name|this
operator|.
name|geohashPrecision
operator|=
name|geohashPrecision
expr_stmt|;
name|this
operator|.
name|latMapper
operator|=
name|latMapper
expr_stmt|;
name|this
operator|.
name|lonMapper
operator|=
name|lonMapper
expr_stmt|;
name|this
operator|.
name|geohashMapper
operator|=
name|geohashMapper
expr_stmt|;
block|}
DECL|method|name
annotation|@
name|Override
specifier|public
name|String
name|name
parameter_list|()
block|{
return|return
name|this
operator|.
name|name
return|;
block|}
DECL|method|parse
annotation|@
name|Override
specifier|public
name|void
name|parse
parameter_list|(
name|ParseContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|ContentPath
operator|.
name|Type
name|origPathType
init|=
name|context
operator|.
name|path
argument_list|()
operator|.
name|pathType
argument_list|()
decl_stmt|;
name|context
operator|.
name|path
argument_list|()
operator|.
name|pathType
argument_list|(
name|pathType
argument_list|)
expr_stmt|;
name|context
operator|.
name|path
argument_list|()
operator|.
name|add
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|boolean
name|added
init|=
literal|false
decl_stmt|;
name|XContentParser
operator|.
name|Token
name|token
init|=
name|context
operator|.
name|parser
argument_list|()
operator|.
name|currentToken
argument_list|()
decl_stmt|;
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
name|String
name|value
init|=
name|context
operator|.
name|parser
argument_list|()
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
name|double
name|lat
init|=
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
decl_stmt|;
name|double
name|lon
init|=
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
decl_stmt|;
name|added
operator|=
literal|true
expr_stmt|;
name|parseLatLon
argument_list|(
name|context
argument_list|,
name|lat
argument_list|,
name|lon
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// geo hash
name|added
operator|=
literal|true
expr_stmt|;
name|parseGeohash
argument_list|(
name|context
argument_list|,
name|value
argument_list|)
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
name|START_OBJECT
condition|)
block|{
name|String
name|currentName
init|=
name|context
operator|.
name|parser
argument_list|()
operator|.
name|currentName
argument_list|()
decl_stmt|;
name|Double
name|lat
init|=
literal|null
decl_stmt|;
name|Double
name|lon
init|=
literal|null
decl_stmt|;
name|String
name|geohash
init|=
literal|null
decl_stmt|;
while|while
condition|(
operator|(
name|token
operator|=
name|context
operator|.
name|parser
argument_list|()
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
name|context
operator|.
name|parser
argument_list|()
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
name|Names
operator|.
name|LAT
argument_list|)
condition|)
block|{
name|lat
operator|=
name|context
operator|.
name|parser
argument_list|()
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
name|Names
operator|.
name|LON
argument_list|)
condition|)
block|{
name|lon
operator|=
name|context
operator|.
name|parser
argument_list|()
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
name|Names
operator|.
name|GEOHASH
argument_list|)
condition|)
block|{
name|geohash
operator|=
name|context
operator|.
name|parser
argument_list|()
operator|.
name|text
argument_list|()
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
name|geohash
operator|!=
literal|null
condition|)
block|{
name|added
operator|=
literal|true
expr_stmt|;
name|parseGeohash
argument_list|(
name|context
argument_list|,
name|geohash
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|lat
operator|!=
literal|null
operator|&&
name|lon
operator|!=
literal|null
condition|)
block|{
name|added
operator|=
literal|true
expr_stmt|;
name|parseLatLon
argument_list|(
name|context
argument_list|,
name|lat
argument_list|,
name|lon
argument_list|)
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
name|START_ARRAY
condition|)
block|{
name|token
operator|=
name|context
operator|.
name|parser
argument_list|()
operator|.
name|nextToken
argument_list|()
expr_stmt|;
name|Double
name|lat
init|=
name|context
operator|.
name|parser
argument_list|()
operator|.
name|doubleValue
argument_list|()
decl_stmt|;
name|token
operator|=
name|context
operator|.
name|parser
argument_list|()
operator|.
name|nextToken
argument_list|()
expr_stmt|;
name|Double
name|lon
init|=
name|context
operator|.
name|parser
argument_list|()
operator|.
name|doubleValue
argument_list|()
decl_stmt|;
name|token
operator|=
name|context
operator|.
name|parser
argument_list|()
operator|.
name|nextToken
argument_list|()
expr_stmt|;
while|while
condition|(
operator|(
name|token
operator|=
name|context
operator|.
name|parser
argument_list|()
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
block|{              }
name|added
operator|=
literal|true
expr_stmt|;
name|parseLatLon
argument_list|(
name|context
argument_list|,
name|lat
argument_list|,
name|lon
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|added
condition|)
block|{
throw|throw
operator|new
name|MapperParsingException
argument_list|(
literal|"failed to find location values for ["
operator|+
name|name
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|context
operator|.
name|path
argument_list|()
operator|.
name|remove
argument_list|()
expr_stmt|;
name|context
operator|.
name|path
argument_list|()
operator|.
name|pathType
argument_list|(
name|origPathType
argument_list|)
expr_stmt|;
block|}
DECL|method|parseLatLon
specifier|private
name|void
name|parseLatLon
parameter_list|(
name|ParseContext
name|context
parameter_list|,
name|Double
name|lat
parameter_list|,
name|Double
name|lon
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|enableLatLon
condition|)
block|{
name|context
operator|.
name|externalValue
argument_list|(
name|lat
argument_list|)
expr_stmt|;
name|latMapper
operator|.
name|parse
argument_list|(
name|context
argument_list|)
expr_stmt|;
name|context
operator|.
name|externalValue
argument_list|(
name|lon
argument_list|)
expr_stmt|;
name|lonMapper
operator|.
name|parse
argument_list|(
name|context
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|enableGeohash
condition|)
block|{
name|context
operator|.
name|externalValue
argument_list|(
name|GeoHashUtils
operator|.
name|encode
argument_list|(
name|lat
argument_list|,
name|lon
argument_list|,
name|geohashPrecision
argument_list|)
argument_list|)
expr_stmt|;
name|geohashMapper
operator|.
name|parse
argument_list|(
name|context
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|parseGeohash
specifier|private
name|void
name|parseGeohash
parameter_list|(
name|ParseContext
name|context
parameter_list|,
name|String
name|geohash
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|enableLatLon
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
name|geohash
argument_list|)
decl_stmt|;
name|context
operator|.
name|externalValue
argument_list|(
name|values
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|latMapper
operator|.
name|parse
argument_list|(
name|context
argument_list|)
expr_stmt|;
name|context
operator|.
name|externalValue
argument_list|(
name|values
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
name|lonMapper
operator|.
name|parse
argument_list|(
name|context
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|enableGeohash
condition|)
block|{
name|context
operator|.
name|externalValue
argument_list|(
name|geohash
argument_list|)
expr_stmt|;
name|geohashMapper
operator|.
name|parse
argument_list|(
name|context
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|merge
annotation|@
name|Override
specifier|public
name|void
name|merge
parameter_list|(
name|XContentMapper
name|mergeWith
parameter_list|,
name|MergeContext
name|mergeContext
parameter_list|)
throws|throws
name|MergeMappingException
block|{
comment|// TODO
block|}
DECL|method|traverse
annotation|@
name|Override
specifier|public
name|void
name|traverse
parameter_list|(
name|FieldMapperListener
name|fieldMapperListener
parameter_list|)
block|{
if|if
condition|(
name|enableLatLon
condition|)
block|{
name|latMapper
operator|.
name|traverse
argument_list|(
name|fieldMapperListener
argument_list|)
expr_stmt|;
name|lonMapper
operator|.
name|traverse
argument_list|(
name|fieldMapperListener
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|enableGeohash
condition|)
block|{
name|geohashMapper
operator|.
name|traverse
argument_list|(
name|fieldMapperListener
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|toXContent
annotation|@
name|Override
specifier|public
name|void
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
name|name
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
name|CONTENT_TYPE
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"path"
argument_list|,
name|pathType
operator|.
name|name
argument_list|()
operator|.
name|toLowerCase
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"lat_lon"
argument_list|,
name|enableLatLon
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"geohash"
argument_list|,
name|enableGeohash
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"resolution"
argument_list|,
name|resolution
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"store"
argument_list|,
name|latMapper
operator|.
name|name
argument_list|()
operator|.
name|toLowerCase
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"geohash_precision"
argument_list|,
name|geohashPrecision
argument_list|)
expr_stmt|;
if|if
condition|(
name|precisionStep
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"precision_step"
argument_list|,
name|precisionStep
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

