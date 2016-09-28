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
name|FieldType
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
name|DocValuesType
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
name|spatial
operator|.
name|geopoint
operator|.
name|document
operator|.
name|GeoPointField
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
comment|/**  * Parsing: We handle:  *<p>  * - "field" : "geo_hash"  * - "field" : "lat,lon"  * - "field" : {  * "lat" : 1.1,  * "lon" : 2.1  * }  */
end_comment

begin_class
DECL|class|GeoPointFieldMapper
specifier|public
class|class
name|GeoPointFieldMapper
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
name|GeoPointFieldType
name|FIELD_TYPE
init|=
operator|new
name|LegacyGeoPointFieldType
argument_list|()
decl_stmt|;
static|static
block|{
name|FIELD_TYPE
operator|.
name|setIndexOptions
argument_list|(
name|IndexOptions
operator|.
name|DOCS
argument_list|)
expr_stmt|;
name|FIELD_TYPE
operator|.
name|setTokenized
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|FIELD_TYPE
operator|.
name|setOmitNorms
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|FIELD_TYPE
operator|.
name|setDocValuesType
argument_list|(
name|DocValuesType
operator|.
name|SORTED_NUMERIC
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
name|freeze
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**      * Concrete builder for indexed GeoPointField type      */
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
name|GeoPointFieldMapper
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
name|this
operator|.
name|builder
operator|=
name|this
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|build
specifier|public
name|GeoPointFieldMapper
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
name|fieldType
operator|.
name|setTokenized
argument_list|(
literal|false
argument_list|)
expr_stmt|;
if|if
condition|(
name|context
operator|.
name|indexCreatedVersion
argument_list|()
operator|.
name|before
argument_list|(
name|Version
operator|.
name|V_2_3_0
argument_list|)
condition|)
block|{
name|fieldType
operator|.
name|setNumericPrecisionStep
argument_list|(
name|GeoPointField
operator|.
name|PRECISION_STEP
argument_list|)
expr_stmt|;
name|fieldType
operator|.
name|setNumericType
argument_list|(
name|FieldType
operator|.
name|LegacyNumericType
operator|.
name|LONG
argument_list|)
expr_stmt|;
block|}
name|setupFieldType
argument_list|(
name|context
argument_list|)
expr_stmt|;
return|return
operator|new
name|GeoPointFieldMapper
argument_list|(
name|simpleName
argument_list|,
name|fieldType
argument_list|,
name|defaultFieldType
argument_list|,
name|indexSettings
argument_list|,
name|latMapper
argument_list|,
name|lonMapper
argument_list|,
name|geoHashMapper
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
name|GeoPointFieldMapper
name|build
parameter_list|(
name|BuilderContext
name|context
parameter_list|)
block|{
if|if
condition|(
name|context
operator|.
name|indexCreatedVersion
argument_list|()
operator|.
name|before
argument_list|(
name|Version
operator|.
name|V_2_3_0
argument_list|)
condition|)
block|{
name|fieldType
operator|.
name|setNumericPrecisionStep
argument_list|(
name|GeoPointField
operator|.
name|PRECISION_STEP
argument_list|)
expr_stmt|;
name|fieldType
operator|.
name|setNumericType
argument_list|(
name|FieldType
operator|.
name|LegacyNumericType
operator|.
name|LONG
argument_list|)
expr_stmt|;
block|}
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
DECL|method|GeoPointFieldMapper
specifier|public
name|GeoPointFieldMapper
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
name|latMapper
argument_list|,
name|lonMapper
argument_list|,
name|geoHashMapper
argument_list|,
name|multiFields
argument_list|,
name|ignoreMalformed
argument_list|,
name|copyTo
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|parse
specifier|protected
name|void
name|parse
parameter_list|(
name|ParseContext
name|context
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
comment|// LUCENE WATCH: This will be folded back into Lucene's GeoPointField
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
operator|||
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
name|GeoPointField
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
argument_list|,
name|fieldType
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|super
operator|.
name|parse
argument_list|(
name|context
argument_list|,
name|point
argument_list|,
name|geoHash
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|fieldType
specifier|public
name|LegacyGeoPointFieldType
name|fieldType
parameter_list|()
block|{
return|return
operator|(
name|LegacyGeoPointFieldType
operator|)
name|super
operator|.
name|fieldType
argument_list|()
return|;
block|}
block|}
end_class

end_unit

