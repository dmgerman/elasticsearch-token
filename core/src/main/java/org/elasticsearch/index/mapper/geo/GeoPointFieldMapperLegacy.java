begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.mapper.geo
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
operator|.
name|geo
package|;
end_package

begin_import
import|import
name|com
operator|.
name|carrotsearch
operator|.
name|hppc
operator|.
name|ObjectHashSet
import|;
end_import

begin_import
import|import
name|com
operator|.
name|carrotsearch
operator|.
name|hppc
operator|.
name|cursors
operator|.
name|ObjectCursor
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
name|Field
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
name|util
operator|.
name|BytesRef
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
name|util
operator|.
name|ByteUtils
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
name|Mapper
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
name|ParseContext
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
name|core
operator|.
name|DoubleFieldMapper
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
name|core
operator|.
name|NumberFieldMapper
operator|.
name|CustomNumericDocValuesField
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
name|core
operator|.
name|StringFieldMapper
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
name|object
operator|.
name|ArrayValueMapperParser
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
name|Iterator
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
DECL|class|GeoPointFieldMapperLegacy
specifier|public
class|class
name|GeoPointFieldMapperLegacy
extends|extends
name|BaseGeoPointFieldMapper
implements|implements
name|ArrayValueMapperParser
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
extends|extends
name|BaseGeoPointFieldMapper
operator|.
name|Names
block|{
DECL|field|COERCE
specifier|public
specifier|static
specifier|final
name|String
name|COERCE
init|=
literal|"coerce"
decl_stmt|;
block|}
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
DECL|field|COERCE
specifier|public
specifier|static
specifier|final
name|Explicit
argument_list|<
name|Boolean
argument_list|>
name|COERCE
init|=
operator|new
name|Explicit
argument_list|(
literal|false
argument_list|,
literal|false
argument_list|)
decl_stmt|;
DECL|field|FIELD_TYPE
specifier|public
specifier|static
specifier|final
name|GeoPointFieldType
name|FIELD_TYPE
init|=
operator|new
name|GeoPointFieldType
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
name|freeze
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**      * Concrete builder for legacy GeoPointField      */
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
name|GeoPointFieldMapperLegacy
argument_list|>
block|{
DECL|field|coerce
specifier|private
name|Boolean
name|coerce
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
DECL|method|coerce
specifier|public
name|Builder
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
name|builder
return|;
block|}
DECL|method|coerce
specifier|protected
name|Explicit
argument_list|<
name|Boolean
argument_list|>
name|coerce
parameter_list|(
name|BuilderContext
name|context
parameter_list|)
block|{
if|if
condition|(
name|coerce
operator|!=
literal|null
condition|)
block|{
return|return
operator|new
name|Explicit
argument_list|<>
argument_list|(
name|coerce
argument_list|,
literal|true
argument_list|)
return|;
block|}
if|if
condition|(
name|context
operator|.
name|indexSettings
argument_list|()
operator|!=
literal|null
condition|)
block|{
return|return
operator|new
name|Explicit
argument_list|<>
argument_list|(
name|context
operator|.
name|indexSettings
argument_list|()
operator|.
name|getAsBoolean
argument_list|(
literal|"index.mapping.coerce"
argument_list|,
name|Defaults
operator|.
name|COERCE
operator|.
name|value
argument_list|()
argument_list|)
argument_list|,
literal|false
argument_list|)
return|;
block|}
return|return
name|Defaults
operator|.
name|COERCE
return|;
block|}
annotation|@
name|Override
DECL|method|build
specifier|public
name|GeoPointFieldMapperLegacy
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
name|DoubleFieldMapper
name|latMapper
parameter_list|,
name|DoubleFieldMapper
name|lonMapper
parameter_list|,
name|StringFieldMapper
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
name|setupFieldType
argument_list|(
name|context
argument_list|)
expr_stmt|;
name|fieldType
operator|.
name|setHasDocValues
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|defaultFieldType
operator|.
name|setHasDocValues
argument_list|(
literal|false
argument_list|)
expr_stmt|;
return|return
operator|new
name|GeoPointFieldMapperLegacy
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
name|coerce
argument_list|(
name|context
argument_list|)
argument_list|,
name|copyTo
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|build
specifier|public
name|GeoPointFieldMapperLegacy
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
DECL|method|parse
specifier|public
specifier|static
name|Builder
name|parse
parameter_list|(
name|Builder
name|builder
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|node
parameter_list|,
name|Mapper
operator|.
name|TypeParser
operator|.
name|ParserContext
name|parserContext
parameter_list|)
throws|throws
name|MapperParsingException
block|{
for|for
control|(
name|Iterator
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|>
name|iterator
init|=
name|node
operator|.
name|entrySet
argument_list|()
operator|.
name|iterator
argument_list|()
init|;
name|iterator
operator|.
name|hasNext
argument_list|()
condition|;
control|)
block|{
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|entry
init|=
name|iterator
operator|.
name|next
argument_list|()
decl_stmt|;
name|String
name|propName
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
name|propNode
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|propName
operator|.
name|equals
argument_list|(
name|Names
operator|.
name|COERCE
argument_list|)
condition|)
block|{
name|builder
operator|.
name|coerce
operator|=
name|XContentMapValues
operator|.
name|nodeBooleanValue
argument_list|(
name|propNode
argument_list|)
expr_stmt|;
name|iterator
operator|.
name|remove
argument_list|()
expr_stmt|;
block|}
block|}
return|return
name|builder
return|;
block|}
comment|/**      * A byte-aligned fixed-length encoding for latitudes and longitudes.      */
DECL|class|Encoding
specifier|public
specifier|static
specifier|final
class|class
name|Encoding
block|{
comment|// With 14 bytes we already have better precision than a double since a double has 11 bits of exponent
DECL|field|MAX_NUM_BYTES
specifier|private
specifier|static
specifier|final
name|int
name|MAX_NUM_BYTES
init|=
literal|14
decl_stmt|;
DECL|field|INSTANCES
specifier|private
specifier|static
specifier|final
name|Encoding
index|[]
name|INSTANCES
decl_stmt|;
static|static
block|{
name|INSTANCES
operator|=
operator|new
name|Encoding
index|[
name|MAX_NUM_BYTES
operator|+
literal|1
index|]
expr_stmt|;
for|for
control|(
name|int
name|numBytes
init|=
literal|2
init|;
name|numBytes
operator|<=
name|MAX_NUM_BYTES
condition|;
name|numBytes
operator|+=
literal|2
control|)
block|{
name|INSTANCES
index|[
name|numBytes
index|]
operator|=
operator|new
name|Encoding
argument_list|(
name|numBytes
argument_list|)
expr_stmt|;
block|}
block|}
comment|/** Get an instance based on the number of bytes that has been used to encode values. */
DECL|method|of
specifier|public
specifier|static
specifier|final
name|Encoding
name|of
parameter_list|(
name|int
name|numBytesPerValue
parameter_list|)
block|{
specifier|final
name|Encoding
name|instance
init|=
name|INSTANCES
index|[
name|numBytesPerValue
index|]
decl_stmt|;
if|if
condition|(
name|instance
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"No encoding for "
operator|+
name|numBytesPerValue
operator|+
literal|" bytes per value"
argument_list|)
throw|;
block|}
return|return
name|instance
return|;
block|}
comment|/** Get an instance based on the expected precision. Here are examples of the number of required bytes per value depending on the          *  expected precision:<ul>          *<li>1km: 4 bytes</li>          *<li>3m: 6 bytes</li>          *<li>1m: 8 bytes</li>          *<li>1cm: 8 bytes</li>          *<li>1mm: 10 bytes</li></ul> */
DECL|method|of
specifier|public
specifier|static
specifier|final
name|Encoding
name|of
parameter_list|(
name|DistanceUnit
operator|.
name|Distance
name|precision
parameter_list|)
block|{
for|for
control|(
name|Encoding
name|encoding
range|:
name|INSTANCES
control|)
block|{
if|if
condition|(
name|encoding
operator|!=
literal|null
operator|&&
name|encoding
operator|.
name|precision
argument_list|()
operator|.
name|compareTo
argument_list|(
name|precision
argument_list|)
operator|<=
literal|0
condition|)
block|{
return|return
name|encoding
return|;
block|}
block|}
return|return
name|INSTANCES
index|[
name|MAX_NUM_BYTES
index|]
return|;
block|}
DECL|field|precision
specifier|private
specifier|final
name|DistanceUnit
operator|.
name|Distance
name|precision
decl_stmt|;
DECL|field|numBytes
specifier|private
specifier|final
name|int
name|numBytes
decl_stmt|;
DECL|field|numBytesPerCoordinate
specifier|private
specifier|final
name|int
name|numBytesPerCoordinate
decl_stmt|;
DECL|field|factor
specifier|private
specifier|final
name|double
name|factor
decl_stmt|;
DECL|method|Encoding
specifier|private
name|Encoding
parameter_list|(
name|int
name|numBytes
parameter_list|)
block|{
assert|assert
name|numBytes
operator|>=
literal|1
operator|&&
name|numBytes
operator|<=
name|MAX_NUM_BYTES
assert|;
assert|assert
operator|(
name|numBytes
operator|&
literal|1
operator|)
operator|==
literal|0
assert|;
comment|// we don't support odd numBytes for the moment
name|this
operator|.
name|numBytes
operator|=
name|numBytes
expr_stmt|;
name|this
operator|.
name|numBytesPerCoordinate
operator|=
name|numBytes
operator|/
literal|2
expr_stmt|;
name|this
operator|.
name|factor
operator|=
name|Math
operator|.
name|pow
argument_list|(
literal|2
argument_list|,
operator|-
name|numBytesPerCoordinate
operator|*
literal|8
operator|+
literal|9
argument_list|)
expr_stmt|;
assert|assert
operator|(
literal|1L
operator|<<
operator|(
name|numBytesPerCoordinate
operator|*
literal|8
operator|-
literal|1
operator|)
operator|)
operator|*
name|factor
operator|>
literal|180
operator|&&
operator|(
literal|1L
operator|<<
operator|(
name|numBytesPerCoordinate
operator|*
literal|8
operator|-
literal|2
operator|)
operator|)
operator|*
name|factor
operator|<
literal|180
operator|:
name|numBytesPerCoordinate
operator|+
literal|" "
operator|+
name|factor
assert|;
if|if
condition|(
name|numBytes
operator|==
name|MAX_NUM_BYTES
condition|)
block|{
comment|// no precision loss compared to a double
name|precision
operator|=
operator|new
name|DistanceUnit
operator|.
name|Distance
argument_list|(
literal|0
argument_list|,
name|DistanceUnit
operator|.
name|DEFAULT
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|precision
operator|=
operator|new
name|DistanceUnit
operator|.
name|Distance
argument_list|(
name|GeoDistance
operator|.
name|PLANE
operator|.
name|calculate
argument_list|(
literal|0
argument_list|,
literal|0
argument_list|,
name|factor
operator|/
literal|2
argument_list|,
name|factor
operator|/
literal|2
argument_list|,
name|DistanceUnit
operator|.
name|DEFAULT
argument_list|)
argument_list|,
comment|// factor/2 because we use Math.round instead of a cast to convert the double to a long
name|DistanceUnit
operator|.
name|DEFAULT
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|precision
specifier|public
name|DistanceUnit
operator|.
name|Distance
name|precision
parameter_list|()
block|{
return|return
name|precision
return|;
block|}
comment|/** The number of bytes required to encode a single geo point. */
DECL|method|numBytes
specifier|public
specifier|final
name|int
name|numBytes
parameter_list|()
block|{
return|return
name|numBytes
return|;
block|}
comment|/** The number of bits required to encode a single coordinate of a geo point. */
DECL|method|numBitsPerCoordinate
specifier|public
name|int
name|numBitsPerCoordinate
parameter_list|()
block|{
return|return
name|numBytesPerCoordinate
operator|<<
literal|3
return|;
block|}
comment|/** Return the bits that encode a latitude/longitude. */
DECL|method|encodeCoordinate
specifier|public
name|long
name|encodeCoordinate
parameter_list|(
name|double
name|lat
parameter_list|)
block|{
return|return
name|Math
operator|.
name|round
argument_list|(
operator|(
name|lat
operator|+
literal|180
operator|)
operator|/
name|factor
argument_list|)
return|;
block|}
comment|/** Decode a sequence of bits into the original coordinate. */
DECL|method|decodeCoordinate
specifier|public
name|double
name|decodeCoordinate
parameter_list|(
name|long
name|bits
parameter_list|)
block|{
return|return
name|bits
operator|*
name|factor
operator|-
literal|180
return|;
block|}
DECL|method|encodeBits
specifier|private
name|void
name|encodeBits
parameter_list|(
name|long
name|bits
parameter_list|,
name|byte
index|[]
name|out
parameter_list|,
name|int
name|offset
parameter_list|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numBytesPerCoordinate
condition|;
operator|++
name|i
control|)
block|{
name|out
index|[
name|offset
operator|++
index|]
operator|=
operator|(
name|byte
operator|)
name|bits
expr_stmt|;
name|bits
operator|>>>=
literal|8
expr_stmt|;
block|}
assert|assert
name|bits
operator|==
literal|0
assert|;
block|}
DECL|method|decodeBits
specifier|private
name|long
name|decodeBits
parameter_list|(
name|byte
index|[]
name|in
parameter_list|,
name|int
name|offset
parameter_list|)
block|{
name|long
name|r
init|=
name|in
index|[
name|offset
operator|++
index|]
operator|&
literal|0xFFL
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|numBytesPerCoordinate
condition|;
operator|++
name|i
control|)
block|{
name|r
operator|=
operator|(
name|in
index|[
name|offset
operator|++
index|]
operator|&
literal|0xFFL
operator|)
operator|<<
operator|(
name|i
operator|*
literal|8
operator|)
expr_stmt|;
block|}
return|return
name|r
return|;
block|}
comment|/** Encode a geo point into a byte-array, over {@link #numBytes()} bytes. */
DECL|method|encode
specifier|public
name|void
name|encode
parameter_list|(
name|double
name|lat
parameter_list|,
name|double
name|lon
parameter_list|,
name|byte
index|[]
name|out
parameter_list|,
name|int
name|offset
parameter_list|)
block|{
name|encodeBits
argument_list|(
name|encodeCoordinate
argument_list|(
name|lat
argument_list|)
argument_list|,
name|out
argument_list|,
name|offset
argument_list|)
expr_stmt|;
name|encodeBits
argument_list|(
name|encodeCoordinate
argument_list|(
name|lon
argument_list|)
argument_list|,
name|out
argument_list|,
name|offset
operator|+
name|numBytesPerCoordinate
argument_list|)
expr_stmt|;
block|}
comment|/** Decode a geo point from a byte-array, reading {@link #numBytes()} bytes. */
DECL|method|decode
specifier|public
name|GeoPoint
name|decode
parameter_list|(
name|byte
index|[]
name|in
parameter_list|,
name|int
name|offset
parameter_list|,
name|GeoPoint
name|out
parameter_list|)
block|{
specifier|final
name|long
name|latBits
init|=
name|decodeBits
argument_list|(
name|in
argument_list|,
name|offset
argument_list|)
decl_stmt|;
specifier|final
name|long
name|lonBits
init|=
name|decodeBits
argument_list|(
name|in
argument_list|,
name|offset
operator|+
name|numBytesPerCoordinate
argument_list|)
decl_stmt|;
return|return
name|decode
argument_list|(
name|latBits
argument_list|,
name|lonBits
argument_list|,
name|out
argument_list|)
return|;
block|}
comment|/** Decode a geo point from the bits of the encoded latitude and longitudes. */
DECL|method|decode
specifier|public
name|GeoPoint
name|decode
parameter_list|(
name|long
name|latBits
parameter_list|,
name|long
name|lonBits
parameter_list|,
name|GeoPoint
name|out
parameter_list|)
block|{
specifier|final
name|double
name|lat
init|=
name|decodeCoordinate
argument_list|(
name|latBits
argument_list|)
decl_stmt|;
specifier|final
name|double
name|lon
init|=
name|decodeCoordinate
argument_list|(
name|lonBits
argument_list|)
decl_stmt|;
return|return
name|out
operator|.
name|reset
argument_list|(
name|lat
argument_list|,
name|lon
argument_list|)
return|;
block|}
block|}
DECL|field|coerce
specifier|protected
name|Explicit
argument_list|<
name|Boolean
argument_list|>
name|coerce
decl_stmt|;
DECL|method|GeoPointFieldMapperLegacy
specifier|public
name|GeoPointFieldMapperLegacy
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
name|DoubleFieldMapper
name|latMapper
parameter_list|,
name|DoubleFieldMapper
name|lonMapper
parameter_list|,
name|StringFieldMapper
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
name|Explicit
argument_list|<
name|Boolean
argument_list|>
name|coerce
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
name|this
operator|.
name|coerce
operator|=
name|coerce
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doMerge
specifier|protected
name|void
name|doMerge
parameter_list|(
name|Mapper
name|mergeWith
parameter_list|,
name|boolean
name|updateAllTypes
parameter_list|)
block|{
name|super
operator|.
name|doMerge
argument_list|(
name|mergeWith
argument_list|,
name|updateAllTypes
argument_list|)
expr_stmt|;
name|GeoPointFieldMapperLegacy
name|gpfmMergeWith
init|=
operator|(
name|GeoPointFieldMapperLegacy
operator|)
name|mergeWith
decl_stmt|;
if|if
condition|(
name|gpfmMergeWith
operator|.
name|coerce
operator|.
name|explicit
argument_list|()
condition|)
block|{
if|if
condition|(
name|coerce
operator|.
name|explicit
argument_list|()
operator|&&
name|coerce
operator|.
name|value
argument_list|()
operator|!=
name|gpfmMergeWith
operator|.
name|coerce
operator|.
name|value
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"mapper ["
operator|+
name|fieldType
argument_list|()
operator|.
name|name
argument_list|()
operator|+
literal|"] has different [coerce]"
argument_list|)
throw|;
block|}
block|}
if|if
condition|(
name|gpfmMergeWith
operator|.
name|coerce
operator|.
name|explicit
argument_list|()
condition|)
block|{
name|this
operator|.
name|coerce
operator|=
name|gpfmMergeWith
operator|.
name|coerce
expr_stmt|;
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
name|boolean
name|validPoint
init|=
literal|false
decl_stmt|;
if|if
condition|(
name|coerce
operator|.
name|value
argument_list|()
operator|==
literal|false
operator|&&
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
name|validPoint
operator|=
literal|true
expr_stmt|;
block|}
if|if
condition|(
name|coerce
operator|.
name|value
argument_list|()
operator|==
literal|true
operator|&&
name|validPoint
operator|==
literal|false
condition|)
block|{
comment|// by setting coerce to false we are assuming all geopoints are already in a valid coordinate system
comment|// thus this extra step can be skipped
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
name|Field
name|field
init|=
operator|new
name|Field
argument_list|(
name|fieldType
argument_list|()
operator|.
name|name
argument_list|()
argument_list|,
name|Double
operator|.
name|toString
argument_list|(
name|point
operator|.
name|lat
argument_list|()
argument_list|)
operator|+
literal|','
operator|+
name|Double
operator|.
name|toString
argument_list|(
name|point
operator|.
name|lon
argument_list|()
argument_list|)
argument_list|,
name|fieldType
argument_list|()
argument_list|)
decl_stmt|;
name|context
operator|.
name|doc
argument_list|()
operator|.
name|add
argument_list|(
name|field
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
if|if
condition|(
name|fieldType
argument_list|()
operator|.
name|hasDocValues
argument_list|()
condition|)
block|{
name|CustomGeoPointDocValuesField
name|field
init|=
operator|(
name|CustomGeoPointDocValuesField
operator|)
name|context
operator|.
name|doc
argument_list|()
operator|.
name|getByKey
argument_list|(
name|fieldType
argument_list|()
operator|.
name|name
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|field
operator|==
literal|null
condition|)
block|{
name|field
operator|=
operator|new
name|CustomGeoPointDocValuesField
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
expr_stmt|;
name|context
operator|.
name|doc
argument_list|()
operator|.
name|addWithKey
argument_list|(
name|fieldType
argument_list|()
operator|.
name|name
argument_list|()
argument_list|,
name|field
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|field
operator|.
name|add
argument_list|(
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
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
DECL|method|doXContentBody
specifier|protected
name|void
name|doXContentBody
parameter_list|(
name|XContentBuilder
name|builder
parameter_list|,
name|boolean
name|includeDefaults
parameter_list|,
name|Params
name|params
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|doXContentBody
argument_list|(
name|builder
argument_list|,
name|includeDefaults
argument_list|,
name|params
argument_list|)
expr_stmt|;
if|if
condition|(
name|includeDefaults
operator|||
name|coerce
operator|.
name|explicit
argument_list|()
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|Names
operator|.
name|COERCE
argument_list|,
name|coerce
operator|.
name|value
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|CustomGeoPointDocValuesField
specifier|public
specifier|static
class|class
name|CustomGeoPointDocValuesField
extends|extends
name|CustomNumericDocValuesField
block|{
DECL|field|points
specifier|private
specifier|final
name|ObjectHashSet
argument_list|<
name|GeoPoint
argument_list|>
name|points
decl_stmt|;
DECL|method|CustomGeoPointDocValuesField
specifier|public
name|CustomGeoPointDocValuesField
parameter_list|(
name|String
name|name
parameter_list|,
name|double
name|lat
parameter_list|,
name|double
name|lon
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|points
operator|=
operator|new
name|ObjectHashSet
argument_list|<>
argument_list|(
literal|2
argument_list|)
expr_stmt|;
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
block|}
DECL|method|add
specifier|public
name|void
name|add
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
block|}
annotation|@
name|Override
DECL|method|binaryValue
specifier|public
name|BytesRef
name|binaryValue
parameter_list|()
block|{
specifier|final
name|byte
index|[]
name|bytes
init|=
operator|new
name|byte
index|[
name|points
operator|.
name|size
argument_list|()
operator|*
literal|16
index|]
decl_stmt|;
name|int
name|off
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Iterator
argument_list|<
name|ObjectCursor
argument_list|<
name|GeoPoint
argument_list|>
argument_list|>
name|it
init|=
name|points
operator|.
name|iterator
argument_list|()
init|;
name|it
operator|.
name|hasNext
argument_list|()
condition|;
control|)
block|{
specifier|final
name|GeoPoint
name|point
init|=
name|it
operator|.
name|next
argument_list|()
operator|.
name|value
decl_stmt|;
name|ByteUtils
operator|.
name|writeDoubleLE
argument_list|(
name|point
operator|.
name|getLat
argument_list|()
argument_list|,
name|bytes
argument_list|,
name|off
argument_list|)
expr_stmt|;
name|ByteUtils
operator|.
name|writeDoubleLE
argument_list|(
name|point
operator|.
name|getLon
argument_list|()
argument_list|,
name|bytes
argument_list|,
name|off
operator|+
literal|8
argument_list|)
expr_stmt|;
name|off
operator|+=
literal|16
expr_stmt|;
block|}
return|return
operator|new
name|BytesRef
argument_list|(
name|bytes
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

