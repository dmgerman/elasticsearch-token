begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.mapper
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
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
name|LatLonDocValuesField
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
name|document
operator|.
name|LatLonPoint
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
name|document
operator|.
name|StoredField
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
name|index
operator|.
name|IndexOptions
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
name|common
operator|.
name|Explicit
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
name|index
operator|.
name|fielddata
operator|.
name|IndexFieldData
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
name|plain
operator|.
name|AbstractLatLonPointDVIndexFieldData
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
name|QueryShardContext
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
name|QueryShardException
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

begin_comment
comment|/**  * Field Mapper for geo_point types.  *  * Uses lucene 6 LatLonPoint encoding  */
end_comment

begin_class
DECL|class|LatLonPointFieldMapper
specifier|public
class|class
name|LatLonPointFieldMapper
extends|extends
name|BaseGeoPointFieldMapper
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
DECL|class|Defaults
specifier|public
specifier|static
class|class
name|Defaults
extends|extends
name|BaseGeoPointFieldMapper
operator|.
name|Defaults
block|{
DECL|field|FIELD_TYPE
specifier|public
specifier|static
specifier|final
name|LatLonPointFieldType
name|FIELD_TYPE
init|=
operator|new
name|LatLonPointFieldType
argument_list|()
decl_stmt|;
static|static
block|{
name|FIELD_TYPE
operator|.
name|setTokenized
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|FIELD_TYPE
operator|.
name|setHasDocValues
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|FIELD_TYPE
operator|.
name|setDimensions
argument_list|(
literal|2
argument_list|,
name|Integer
operator|.
name|BYTES
argument_list|)
expr_stmt|;
name|FIELD_TYPE
operator|.
name|freeze
argument_list|()
expr_stmt|;
block|}
block|}
DECL|class|Builder
specifier|public
specifier|static
class|class
name|Builder
extends|extends
name|BaseGeoPointFieldMapper
operator|.
name|Builder
argument_list|<
name|Builder
argument_list|,
name|LatLonPointFieldMapper
argument_list|>
block|{
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
argument_list|,
name|Defaults
operator|.
name|FIELD_TYPE
argument_list|)
expr_stmt|;
name|builder
operator|=
name|this
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|build
specifier|public
name|LatLonPointFieldMapper
name|build
parameter_list|(
name|BuilderContext
name|context
parameter_list|,
name|String
name|simpleName
parameter_list|,
name|MappedFieldType
name|fieldType
parameter_list|,
name|MappedFieldType
name|defaultFieldType
parameter_list|,
name|Settings
name|indexSettings
parameter_list|,
name|FieldMapper
name|latMapper
parameter_list|,
name|FieldMapper
name|lonMapper
parameter_list|,
name|FieldMapper
name|geoHashMapper
parameter_list|,
name|MultiFields
name|multiFields
parameter_list|,
name|Explicit
argument_list|<
name|Boolean
argument_list|>
name|ignoreMalformed
parameter_list|,
name|CopyTo
name|copyTo
parameter_list|)
block|{
name|setupFieldType
argument_list|(
name|context
argument_list|)
expr_stmt|;
return|return
operator|new
name|LatLonPointFieldMapper
argument_list|(
name|simpleName
argument_list|,
name|fieldType
argument_list|,
name|defaultFieldType
argument_list|,
name|indexSettings
argument_list|,
name|multiFields
argument_list|,
name|ignoreMalformed
argument_list|,
name|copyTo
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|build
specifier|public
name|LatLonPointFieldMapper
name|build
parameter_list|(
name|BuilderContext
name|context
parameter_list|)
block|{
return|return
name|super
operator|.
name|build
argument_list|(
name|context
argument_list|)
return|;
block|}
block|}
DECL|class|TypeParser
specifier|public
specifier|static
class|class
name|TypeParser
extends|extends
name|BaseGeoPointFieldMapper
operator|.
name|TypeParser
block|{
annotation|@
name|Override
DECL|method|parse
specifier|public
name|Mapper
operator|.
name|Builder
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
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
return|return
name|super
operator|.
name|parse
argument_list|(
name|name
argument_list|,
name|node
argument_list|,
name|parserContext
argument_list|)
return|;
block|}
block|}
DECL|method|LatLonPointFieldMapper
specifier|public
name|LatLonPointFieldMapper
parameter_list|(
name|String
name|simpleName
parameter_list|,
name|MappedFieldType
name|fieldType
parameter_list|,
name|MappedFieldType
name|defaultFieldType
parameter_list|,
name|Settings
name|indexSettings
parameter_list|,
name|MultiFields
name|multiFields
parameter_list|,
name|Explicit
argument_list|<
name|Boolean
argument_list|>
name|ignoreMalformed
parameter_list|,
name|CopyTo
name|copyTo
parameter_list|)
block|{
name|super
argument_list|(
name|simpleName
argument_list|,
name|fieldType
argument_list|,
name|defaultFieldType
argument_list|,
name|indexSettings
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|multiFields
argument_list|,
name|ignoreMalformed
argument_list|,
name|copyTo
argument_list|)
expr_stmt|;
block|}
DECL|class|LatLonPointFieldType
specifier|public
specifier|static
class|class
name|LatLonPointFieldType
extends|extends
name|GeoPointFieldType
block|{
DECL|method|LatLonPointFieldType
name|LatLonPointFieldType
parameter_list|()
block|{         }
DECL|method|LatLonPointFieldType
name|LatLonPointFieldType
parameter_list|(
name|LatLonPointFieldType
name|ref
parameter_list|)
block|{
name|super
argument_list|(
name|ref
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|typeName
specifier|public
name|String
name|typeName
parameter_list|()
block|{
return|return
name|CONTENT_TYPE
return|;
block|}
annotation|@
name|Override
DECL|method|clone
specifier|public
name|MappedFieldType
name|clone
parameter_list|()
block|{
return|return
operator|new
name|LatLonPointFieldType
argument_list|(
name|this
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|fielddataBuilder
specifier|public
name|IndexFieldData
operator|.
name|Builder
name|fielddataBuilder
parameter_list|()
block|{
name|failIfNoDocValues
argument_list|()
expr_stmt|;
return|return
operator|new
name|AbstractLatLonPointDVIndexFieldData
operator|.
name|Builder
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|termQuery
specifier|public
name|Query
name|termQuery
parameter_list|(
name|Object
name|value
parameter_list|,
name|QueryShardContext
name|context
parameter_list|)
block|{
throw|throw
operator|new
name|QueryShardException
argument_list|(
name|context
argument_list|,
literal|"Geo fields do not support exact searching, use dedicated geo queries instead: ["
operator|+
name|name
argument_list|()
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
DECL|method|parse
specifier|protected
name|void
name|parse
parameter_list|(
name|ParseContext
name|originalContext
parameter_list|,
name|GeoPoint
name|point
parameter_list|,
name|String
name|geoHash
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Geopoint fields, by default, will not be included in _all
specifier|final
name|ParseContext
name|context
init|=
name|originalContext
operator|.
name|setIncludeInAllDefault
argument_list|(
literal|false
argument_list|)
decl_stmt|;
if|if
condition|(
name|ignoreMalformed
operator|.
name|value
argument_list|()
operator|==
literal|false
condition|)
block|{
if|if
condition|(
name|point
operator|.
name|lat
argument_list|()
operator|>
literal|90.0
operator|||
name|point
operator|.
name|lat
argument_list|()
operator|<
operator|-
literal|90.0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"illegal latitude value ["
operator|+
name|point
operator|.
name|lat
argument_list|()
operator|+
literal|"] for "
operator|+
name|name
argument_list|()
argument_list|)
throw|;
block|}
if|if
condition|(
name|point
operator|.
name|lon
argument_list|()
operator|>
literal|180.0
operator|||
name|point
operator|.
name|lon
argument_list|()
operator|<
operator|-
literal|180
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"illegal longitude value ["
operator|+
name|point
operator|.
name|lon
argument_list|()
operator|+
literal|"] for "
operator|+
name|name
argument_list|()
argument_list|)
throw|;
block|}
block|}
else|else
block|{
name|GeoUtils
operator|.
name|normalizePoint
argument_list|(
name|point
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|fieldType
argument_list|()
operator|.
name|indexOptions
argument_list|()
operator|!=
name|IndexOptions
operator|.
name|NONE
condition|)
block|{
name|context
operator|.
name|doc
argument_list|()
operator|.
name|add
argument_list|(
operator|new
name|LatLonPoint
argument_list|(
name|fieldType
argument_list|()
operator|.
name|name
argument_list|()
argument_list|,
name|point
operator|.
name|lat
argument_list|()
argument_list|,
name|point
operator|.
name|lon
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|fieldType
argument_list|()
operator|.
name|stored
argument_list|()
condition|)
block|{
name|context
operator|.
name|doc
argument_list|()
operator|.
name|add
argument_list|(
operator|new
name|StoredField
argument_list|(
name|fieldType
argument_list|()
operator|.
name|name
argument_list|()
argument_list|,
name|point
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|fieldType
operator|.
name|hasDocValues
argument_list|()
condition|)
block|{
name|context
operator|.
name|doc
argument_list|()
operator|.
name|add
argument_list|(
operator|new
name|LatLonDocValuesField
argument_list|(
name|fieldType
argument_list|()
operator|.
name|name
argument_list|()
argument_list|,
name|point
operator|.
name|lat
argument_list|()
argument_list|,
name|point
operator|.
name|lon
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// if the mapping contains multifields then use the geohash string
if|if
condition|(
name|multiFields
operator|.
name|iterator
argument_list|()
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|multiFields
operator|.
name|parse
argument_list|(
name|this
argument_list|,
name|context
operator|.
name|createExternalValueContext
argument_list|(
name|point
operator|.
name|geohash
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

