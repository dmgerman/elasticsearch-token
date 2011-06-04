begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|GeoPointFieldDataType
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
name|GeoBoundingBoxFilter
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
name|GeoHashUtils
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
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|GeoBoundingBoxFilterParser
specifier|public
class|class
name|GeoBoundingBoxFilterParser
implements|implements
name|FilterParser
block|{
DECL|field|NAME
specifier|public
specifier|static
specifier|final
name|String
name|NAME
init|=
literal|"geo_bbox"
decl_stmt|;
DECL|method|GeoBoundingBoxFilterParser
annotation|@
name|Inject
specifier|public
name|GeoBoundingBoxFilterParser
parameter_list|()
block|{     }
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
literal|"geoBbox"
block|,
literal|"geo_bounding_box"
block|,
literal|"geoBoundingBox"
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
name|boolean
name|cache
init|=
literal|false
decl_stmt|;
name|String
name|fieldName
init|=
literal|null
decl_stmt|;
name|GeoBoundingBoxFilter
operator|.
name|Point
name|topLeft
init|=
operator|new
name|GeoBoundingBoxFilter
operator|.
name|Point
argument_list|()
decl_stmt|;
name|GeoBoundingBoxFilter
operator|.
name|Point
name|bottomRight
init|=
operator|new
name|GeoBoundingBoxFilter
operator|.
name|Point
argument_list|()
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
name|XContentParser
operator|.
name|Token
name|token
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
name|START_OBJECT
condition|)
block|{
name|fieldName
operator|=
name|currentFieldName
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
name|GeoBoundingBoxFilter
operator|.
name|Point
name|point
init|=
literal|null
decl_stmt|;
if|if
condition|(
literal|"top_left"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
operator|||
literal|"topLeft"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|point
operator|=
name|topLeft
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"bottom_right"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
operator|||
literal|"bottomRight"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|point
operator|=
name|bottomRight
expr_stmt|;
block|}
if|if
condition|(
name|point
operator|!=
literal|null
condition|)
block|{
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
name|point
operator|.
name|lon
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
name|point
operator|.
name|lat
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
block|{                              }
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
name|GeoBoundingBoxFilter
operator|.
name|Point
name|point
init|=
literal|null
decl_stmt|;
if|if
condition|(
literal|"top_left"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
operator|||
literal|"topLeft"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|point
operator|=
name|topLeft
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"bottom_right"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
operator|||
literal|"bottomRight"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|point
operator|=
name|bottomRight
expr_stmt|;
block|}
if|if
condition|(
name|point
operator|!=
literal|null
condition|)
block|{
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
name|GeoPointFieldMapper
operator|.
name|Names
operator|.
name|LAT
argument_list|)
condition|)
block|{
name|point
operator|.
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
name|currentFieldName
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
name|point
operator|.
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
name|currentFieldName
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
name|point
operator|.
name|lat
operator|=
name|values
index|[
literal|0
index|]
expr_stmt|;
name|point
operator|.
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
literal|"field"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|fieldName
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|GeoBoundingBoxFilter
operator|.
name|Point
name|point
init|=
literal|null
decl_stmt|;
if|if
condition|(
literal|"top_left"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
operator|||
literal|"topLeft"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|point
operator|=
name|topLeft
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"bottom_right"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
operator|||
literal|"bottomRight"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|point
operator|=
name|bottomRight
expr_stmt|;
block|}
if|if
condition|(
name|point
operator|!=
literal|null
condition|)
block|{
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
name|point
operator|.
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
name|point
operator|.
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
name|point
operator|.
name|lat
operator|=
name|values
index|[
literal|0
index|]
expr_stmt|;
name|point
operator|.
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
name|parser
operator|.
name|booleanValue
argument_list|()
expr_stmt|;
block|}
block|}
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
name|fieldName
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
name|parseContext
operator|.
name|index
argument_list|()
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
name|mapper
operator|.
name|fieldDataType
argument_list|()
operator|!=
name|GeoPointFieldDataType
operator|.
name|TYPE
condition|)
block|{
throw|throw
operator|new
name|QueryParsingException
argument_list|(
name|parseContext
operator|.
name|index
argument_list|()
argument_list|,
literal|"field ["
operator|+
name|fieldName
operator|+
literal|"] is not a geo_point field"
argument_list|)
throw|;
block|}
name|fieldName
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
name|GeoBoundingBoxFilter
argument_list|(
name|topLeft
argument_list|,
name|bottomRight
argument_list|,
name|fieldName
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
if|if
condition|(
name|cache
condition|)
block|{
name|filter
operator|=
name|parseContext
operator|.
name|cacheFilter
argument_list|(
name|filter
argument_list|)
expr_stmt|;
block|}
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
name|fieldName
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

