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
name|XContentParser
operator|.
name|Token
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
name|cache
operator|.
name|filter
operator|.
name|support
operator|.
name|CacheKeyFilter
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
name|GeoPolygonFilter
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
name|wrapSmartNameFilter
import|;
end_import

begin_comment
comment|/**  *<pre>  * {  *     "pin.location" : {  *         "points" : [  *              { "lat" : 12, "lon" : 40},  *              {}  *         ]  *     }  * }  *</pre>  */
end_comment

begin_class
DECL|class|GeoPolygonFilterParser
specifier|public
class|class
name|GeoPolygonFilterParser
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
literal|"geo_polygon"
decl_stmt|;
DECL|field|POINTS
specifier|public
specifier|static
specifier|final
name|String
name|POINTS
init|=
literal|"points"
decl_stmt|;
annotation|@
name|Inject
DECL|method|GeoPolygonFilterParser
specifier|public
name|GeoPolygonFilterParser
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
literal|"geoPolygon"
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
name|boolean
name|cache
init|=
literal|false
decl_stmt|;
name|CacheKeyFilter
operator|.
name|Key
name|cacheKey
init|=
literal|null
decl_stmt|;
name|String
name|fieldName
init|=
literal|null
decl_stmt|;
name|List
argument_list|<
name|GeoPoint
argument_list|>
name|shell
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
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
if|if
condition|(
name|POINTS
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
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
name|Token
operator|.
name|END_ARRAY
condition|)
block|{
name|shell
operator|.
name|add
argument_list|(
name|GeoPoint
operator|.
name|parse
argument_list|(
name|parser
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
else|else
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
literal|"[geo_polygon] filter does not support ["
operator|+
name|currentFieldName
operator|+
literal|"]"
argument_list|)
throw|;
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
name|CacheKeyFilter
operator|.
name|Key
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
throw|throw
operator|new
name|QueryParsingException
argument_list|(
name|parseContext
operator|.
name|index
argument_list|()
argument_list|,
literal|"[geo_polygon] filter does not support ["
operator|+
name|currentFieldName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
block|}
if|if
condition|(
name|shell
operator|.
name|isEmpty
argument_list|()
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
literal|"no points defined for geo_polygon filter"
argument_list|)
throw|;
block|}
else|else
block|{
if|if
condition|(
name|shell
operator|.
name|size
argument_list|()
operator|<
literal|3
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
literal|"to few points defined for geo_polygon filter"
argument_list|)
throw|;
block|}
name|GeoPoint
name|start
init|=
name|shell
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|start
operator|.
name|equals
argument_list|(
name|shell
operator|.
name|get
argument_list|(
name|shell
operator|.
name|size
argument_list|()
operator|-
literal|1
argument_list|)
argument_list|)
condition|)
block|{
name|shell
operator|.
name|add
argument_list|(
name|start
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|shell
operator|.
name|size
argument_list|()
operator|<
literal|4
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
literal|"to few points defined for geo_polygon filter"
argument_list|)
throw|;
block|}
block|}
if|if
condition|(
name|normalizeLat
operator|||
name|normalizeLon
condition|)
block|{
for|for
control|(
name|GeoPoint
name|point
range|:
name|shell
control|)
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
name|IndexGeoPointFieldData
argument_list|<
name|?
argument_list|>
name|indexFieldData
init|=
name|parseContext
operator|.
name|fieldData
argument_list|()
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
name|GeoPolygonFilter
argument_list|(
name|indexFieldData
argument_list|,
name|shell
operator|.
name|toArray
argument_list|(
operator|new
name|GeoPoint
index|[
name|shell
operator|.
name|size
argument_list|()
index|]
argument_list|)
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
argument_list|,
name|cacheKey
argument_list|)
expr_stmt|;
block|}
name|filter
operator|=
name|wrapSmartNameFilter
argument_list|(
name|filter
argument_list|,
name|smartMappers
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

