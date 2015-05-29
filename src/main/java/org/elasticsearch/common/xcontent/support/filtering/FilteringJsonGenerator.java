begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.xcontent.support.filtering
package|package
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
name|filtering
package|;
end_package

begin_import
import|import
name|com
operator|.
name|fasterxml
operator|.
name|jackson
operator|.
name|core
operator|.
name|Base64Variant
import|;
end_import

begin_import
import|import
name|com
operator|.
name|fasterxml
operator|.
name|jackson
operator|.
name|core
operator|.
name|JsonGenerator
import|;
end_import

begin_import
import|import
name|com
operator|.
name|fasterxml
operator|.
name|jackson
operator|.
name|core
operator|.
name|JsonParser
import|;
end_import

begin_import
import|import
name|com
operator|.
name|fasterxml
operator|.
name|jackson
operator|.
name|core
operator|.
name|SerializableString
import|;
end_import

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
name|ImmutableList
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
name|bytes
operator|.
name|BytesReference
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
name|json
operator|.
name|BaseJsonGenerator
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
name|io
operator|.
name|InputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|OutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|math
operator|.
name|BigDecimal
import|;
end_import

begin_import
import|import
name|java
operator|.
name|math
operator|.
name|BigInteger
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayDeque
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
import|import
name|java
operator|.
name|util
operator|.
name|Queue
import|;
end_import

begin_comment
comment|/**  * A FilteringJsonGenerator uses antpath-like filters to include/exclude fields when writing XContent streams.  *  * When writing a XContent stream, this class instantiates (or reuses) a FilterContext instance for each  * field (or property) that must be generated. This filter context is used to check if the field/property must be  * written according to the current list of XContentFilter filters.  */
end_comment

begin_class
DECL|class|FilteringJsonGenerator
specifier|public
class|class
name|FilteringJsonGenerator
extends|extends
name|BaseJsonGenerator
block|{
comment|/**      * List of previous contexts      * (MAX_CONTEXTS contexts are kept around in order to be reused)      */
DECL|field|contexts
specifier|private
name|Queue
argument_list|<
name|FilterContext
argument_list|>
name|contexts
init|=
operator|new
name|ArrayDeque
argument_list|<>
argument_list|()
decl_stmt|;
DECL|field|MAX_CONTEXTS
specifier|private
specifier|static
specifier|final
name|int
name|MAX_CONTEXTS
init|=
literal|10
decl_stmt|;
comment|/**      * Current filter context      */
DECL|field|context
specifier|private
name|FilterContext
name|context
decl_stmt|;
DECL|method|FilteringJsonGenerator
specifier|public
name|FilteringJsonGenerator
parameter_list|(
name|JsonGenerator
name|generator
parameter_list|,
name|String
index|[]
name|filters
parameter_list|)
block|{
name|super
argument_list|(
name|generator
argument_list|)
expr_stmt|;
name|ImmutableList
operator|.
name|Builder
argument_list|<
name|String
index|[]
argument_list|>
name|builder
init|=
name|ImmutableList
operator|.
name|builder
argument_list|()
decl_stmt|;
if|if
condition|(
name|filters
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|String
name|filter
range|:
name|filters
control|)
block|{
name|String
index|[]
name|matcher
init|=
name|Strings
operator|.
name|delimitedListToStringArray
argument_list|(
name|filter
argument_list|,
literal|"."
argument_list|)
decl_stmt|;
if|if
condition|(
name|matcher
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|add
argument_list|(
name|matcher
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|// Creates a root context that matches all filtering rules
name|this
operator|.
name|context
operator|=
name|get
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
name|builder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**      * Get a new context instance (and reset it if needed)      */
DECL|method|get
specifier|private
name|FilterContext
name|get
parameter_list|(
name|String
name|property
parameter_list|,
name|FilterContext
name|parent
parameter_list|)
block|{
name|FilterContext
name|ctx
init|=
name|contexts
operator|.
name|poll
argument_list|()
decl_stmt|;
if|if
condition|(
name|ctx
operator|==
literal|null
condition|)
block|{
name|ctx
operator|=
operator|new
name|FilterContext
argument_list|(
name|property
argument_list|,
name|parent
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|ctx
operator|.
name|reset
argument_list|(
name|property
argument_list|,
name|parent
argument_list|)
expr_stmt|;
block|}
return|return
name|ctx
return|;
block|}
comment|/**      * Get a new context instance (and reset it if needed)      */
DECL|method|get
specifier|private
name|FilterContext
name|get
parameter_list|(
name|String
name|property
parameter_list|,
name|FilterContext
name|context
parameter_list|,
name|List
argument_list|<
name|String
index|[]
argument_list|>
name|matchings
parameter_list|)
block|{
name|FilterContext
name|ctx
init|=
name|get
argument_list|(
name|property
argument_list|,
name|context
argument_list|)
decl_stmt|;
if|if
condition|(
name|matchings
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|String
index|[]
name|matching
range|:
name|matchings
control|)
block|{
name|ctx
operator|.
name|addMatching
argument_list|(
name|matching
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|ctx
return|;
block|}
comment|/**      * Adds a context instance to the pool in order to reuse it if needed      */
DECL|method|put
specifier|private
name|void
name|put
parameter_list|(
name|FilterContext
name|ctx
parameter_list|)
block|{
if|if
condition|(
name|contexts
operator|.
name|size
argument_list|()
operator|<=
name|MAX_CONTEXTS
condition|)
block|{
name|contexts
operator|.
name|offer
argument_list|(
name|ctx
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|writeStartArray
specifier|public
name|void
name|writeStartArray
parameter_list|()
throws|throws
name|IOException
block|{
name|context
operator|.
name|initArray
argument_list|()
expr_stmt|;
if|if
condition|(
name|context
operator|.
name|include
argument_list|()
condition|)
block|{
name|super
operator|.
name|writeStartArray
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|writeStartArray
specifier|public
name|void
name|writeStartArray
parameter_list|(
name|int
name|size
parameter_list|)
throws|throws
name|IOException
block|{
name|context
operator|.
name|initArray
argument_list|()
expr_stmt|;
if|if
condition|(
name|context
operator|.
name|include
argument_list|()
condition|)
block|{
name|super
operator|.
name|writeStartArray
argument_list|(
name|size
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|writeEndArray
specifier|public
name|void
name|writeEndArray
parameter_list|()
throws|throws
name|IOException
block|{
comment|// Case of array of objects
if|if
condition|(
name|context
operator|.
name|isArrayOfObject
argument_list|()
condition|)
block|{
comment|// Release current context and go one level up
name|FilterContext
name|parent
init|=
name|context
operator|.
name|parent
argument_list|()
decl_stmt|;
name|put
argument_list|(
name|context
argument_list|)
expr_stmt|;
name|context
operator|=
name|parent
expr_stmt|;
block|}
if|if
condition|(
name|context
operator|.
name|include
argument_list|()
condition|)
block|{
name|super
operator|.
name|writeEndArray
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|writeStartObject
specifier|public
name|void
name|writeStartObject
parameter_list|()
throws|throws
name|IOException
block|{
comment|// Case of array of objects
if|if
condition|(
name|context
operator|.
name|isArray
argument_list|()
condition|)
block|{
comment|// Get a context for the anonymous object
name|context
operator|=
name|get
argument_list|(
literal|null
argument_list|,
name|context
argument_list|,
name|context
operator|.
name|matchings
argument_list|()
argument_list|)
expr_stmt|;
name|context
operator|.
name|initArrayOfObject
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|context
operator|.
name|isArrayOfObject
argument_list|()
condition|)
block|{
name|context
operator|.
name|initObject
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|context
operator|.
name|include
argument_list|()
condition|)
block|{
name|super
operator|.
name|writeStartObject
argument_list|()
expr_stmt|;
block|}
name|context
operator|=
name|get
argument_list|(
literal|null
argument_list|,
name|context
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|writeEndObject
specifier|public
name|void
name|writeEndObject
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|context
operator|.
name|isRoot
argument_list|()
condition|)
block|{
comment|// Release current context and go one level up
name|FilterContext
name|parent
init|=
name|context
operator|.
name|parent
argument_list|()
decl_stmt|;
name|put
argument_list|(
name|context
argument_list|)
expr_stmt|;
name|context
operator|=
name|parent
expr_stmt|;
block|}
if|if
condition|(
name|context
operator|.
name|include
argument_list|()
condition|)
block|{
name|super
operator|.
name|writeEndObject
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|writeFieldName
specifier|public
name|void
name|writeFieldName
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|IOException
block|{
name|context
operator|.
name|reset
argument_list|(
name|name
argument_list|)
expr_stmt|;
if|if
condition|(
name|context
operator|.
name|include
argument_list|()
condition|)
block|{
comment|// Ensure that the full path to the field is written
name|context
operator|.
name|writePath
argument_list|(
name|delegate
argument_list|)
expr_stmt|;
name|super
operator|.
name|writeFieldName
argument_list|(
name|name
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|writeFieldName
specifier|public
name|void
name|writeFieldName
parameter_list|(
name|SerializableString
name|name
parameter_list|)
throws|throws
name|IOException
block|{
name|context
operator|.
name|reset
argument_list|(
name|name
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|context
operator|.
name|include
argument_list|()
condition|)
block|{
comment|// Ensure that the full path to the field is written
name|context
operator|.
name|writePath
argument_list|(
name|delegate
argument_list|)
expr_stmt|;
name|super
operator|.
name|writeFieldName
argument_list|(
name|name
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|writeString
specifier|public
name|void
name|writeString
parameter_list|(
name|String
name|text
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|context
operator|.
name|include
argument_list|()
condition|)
block|{
name|super
operator|.
name|writeString
argument_list|(
name|text
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|writeString
specifier|public
name|void
name|writeString
parameter_list|(
name|char
index|[]
name|text
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|len
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|context
operator|.
name|include
argument_list|()
condition|)
block|{
name|super
operator|.
name|writeString
argument_list|(
name|text
argument_list|,
name|offset
argument_list|,
name|len
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|writeString
specifier|public
name|void
name|writeString
parameter_list|(
name|SerializableString
name|text
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|context
operator|.
name|include
argument_list|()
condition|)
block|{
name|super
operator|.
name|writeString
argument_list|(
name|text
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|writeRawUTF8String
specifier|public
name|void
name|writeRawUTF8String
parameter_list|(
name|byte
index|[]
name|text
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|context
operator|.
name|include
argument_list|()
condition|)
block|{
name|super
operator|.
name|writeRawUTF8String
argument_list|(
name|text
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|writeUTF8String
specifier|public
name|void
name|writeUTF8String
parameter_list|(
name|byte
index|[]
name|text
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|context
operator|.
name|include
argument_list|()
condition|)
block|{
name|super
operator|.
name|writeUTF8String
argument_list|(
name|text
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|writeRaw
specifier|public
name|void
name|writeRaw
parameter_list|(
name|String
name|text
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|context
operator|.
name|include
argument_list|()
condition|)
block|{
name|super
operator|.
name|writeRaw
argument_list|(
name|text
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|writeRaw
specifier|public
name|void
name|writeRaw
parameter_list|(
name|String
name|text
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|len
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|context
operator|.
name|include
argument_list|()
condition|)
block|{
name|super
operator|.
name|writeRaw
argument_list|(
name|text
argument_list|,
name|offset
argument_list|,
name|len
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|writeRaw
specifier|public
name|void
name|writeRaw
parameter_list|(
name|SerializableString
name|raw
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|context
operator|.
name|include
argument_list|()
condition|)
block|{
name|super
operator|.
name|writeRaw
argument_list|(
name|raw
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|writeRaw
specifier|public
name|void
name|writeRaw
parameter_list|(
name|char
index|[]
name|text
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|len
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|context
operator|.
name|include
argument_list|()
condition|)
block|{
name|super
operator|.
name|writeRaw
argument_list|(
name|text
argument_list|,
name|offset
argument_list|,
name|len
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|writeRaw
specifier|public
name|void
name|writeRaw
parameter_list|(
name|char
name|c
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|context
operator|.
name|include
argument_list|()
condition|)
block|{
name|super
operator|.
name|writeRaw
argument_list|(
name|c
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|writeRawValue
specifier|public
name|void
name|writeRawValue
parameter_list|(
name|String
name|text
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|context
operator|.
name|include
argument_list|()
condition|)
block|{
name|super
operator|.
name|writeRawValue
argument_list|(
name|text
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|writeRawValue
specifier|public
name|void
name|writeRawValue
parameter_list|(
name|String
name|text
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|len
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|context
operator|.
name|include
argument_list|()
condition|)
block|{
name|super
operator|.
name|writeRawValue
argument_list|(
name|text
argument_list|,
name|offset
argument_list|,
name|len
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|writeRawValue
specifier|public
name|void
name|writeRawValue
parameter_list|(
name|char
index|[]
name|text
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|len
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|context
operator|.
name|include
argument_list|()
condition|)
block|{
name|super
operator|.
name|writeRawValue
argument_list|(
name|text
argument_list|,
name|offset
argument_list|,
name|len
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|writeBinary
specifier|public
name|void
name|writeBinary
parameter_list|(
name|Base64Variant
name|b64variant
parameter_list|,
name|byte
index|[]
name|data
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|len
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|context
operator|.
name|include
argument_list|()
condition|)
block|{
name|super
operator|.
name|writeBinary
argument_list|(
name|b64variant
argument_list|,
name|data
argument_list|,
name|offset
argument_list|,
name|len
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|writeBinary
specifier|public
name|int
name|writeBinary
parameter_list|(
name|Base64Variant
name|b64variant
parameter_list|,
name|InputStream
name|data
parameter_list|,
name|int
name|dataLength
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|context
operator|.
name|include
argument_list|()
condition|)
block|{
return|return
name|super
operator|.
name|writeBinary
argument_list|(
name|b64variant
argument_list|,
name|data
argument_list|,
name|dataLength
argument_list|)
return|;
block|}
return|return
literal|0
return|;
block|}
annotation|@
name|Override
DECL|method|writeNumber
specifier|public
name|void
name|writeNumber
parameter_list|(
name|short
name|v
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|context
operator|.
name|include
argument_list|()
condition|)
block|{
name|super
operator|.
name|writeNumber
argument_list|(
name|v
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|writeNumber
specifier|public
name|void
name|writeNumber
parameter_list|(
name|int
name|v
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|context
operator|.
name|include
argument_list|()
condition|)
block|{
name|super
operator|.
name|writeNumber
argument_list|(
name|v
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|writeNumber
specifier|public
name|void
name|writeNumber
parameter_list|(
name|long
name|v
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|context
operator|.
name|include
argument_list|()
condition|)
block|{
name|super
operator|.
name|writeNumber
argument_list|(
name|v
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|writeNumber
specifier|public
name|void
name|writeNumber
parameter_list|(
name|BigInteger
name|v
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|context
operator|.
name|include
argument_list|()
condition|)
block|{
name|super
operator|.
name|writeNumber
argument_list|(
name|v
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|writeNumber
specifier|public
name|void
name|writeNumber
parameter_list|(
name|double
name|v
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|context
operator|.
name|include
argument_list|()
condition|)
block|{
name|super
operator|.
name|writeNumber
argument_list|(
name|v
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|writeNumber
specifier|public
name|void
name|writeNumber
parameter_list|(
name|float
name|v
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|context
operator|.
name|include
argument_list|()
condition|)
block|{
name|super
operator|.
name|writeNumber
argument_list|(
name|v
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|writeNumber
specifier|public
name|void
name|writeNumber
parameter_list|(
name|BigDecimal
name|v
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|context
operator|.
name|include
argument_list|()
condition|)
block|{
name|super
operator|.
name|writeNumber
argument_list|(
name|v
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|writeNumber
specifier|public
name|void
name|writeNumber
parameter_list|(
name|String
name|encodedValue
parameter_list|)
throws|throws
name|IOException
throws|,
name|UnsupportedOperationException
block|{
if|if
condition|(
name|context
operator|.
name|include
argument_list|()
condition|)
block|{
name|super
operator|.
name|writeNumber
argument_list|(
name|encodedValue
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|writeBoolean
specifier|public
name|void
name|writeBoolean
parameter_list|(
name|boolean
name|state
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|context
operator|.
name|include
argument_list|()
condition|)
block|{
name|super
operator|.
name|writeBoolean
argument_list|(
name|state
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|writeNull
specifier|public
name|void
name|writeNull
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|context
operator|.
name|include
argument_list|()
condition|)
block|{
name|super
operator|.
name|writeNull
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|copyCurrentEvent
specifier|public
name|void
name|copyCurrentEvent
parameter_list|(
name|JsonParser
name|jp
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|context
operator|.
name|include
argument_list|()
condition|)
block|{
name|super
operator|.
name|copyCurrentEvent
argument_list|(
name|jp
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|copyCurrentStructure
specifier|public
name|void
name|copyCurrentStructure
parameter_list|(
name|JsonParser
name|jp
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|context
operator|.
name|include
argument_list|()
condition|)
block|{
name|super
operator|.
name|copyCurrentStructure
argument_list|(
name|jp
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|writeRawValue
specifier|protected
name|void
name|writeRawValue
parameter_list|(
name|byte
index|[]
name|content
parameter_list|,
name|OutputStream
name|bos
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|context
operator|.
name|include
argument_list|()
condition|)
block|{
name|super
operator|.
name|writeRawValue
argument_list|(
name|content
argument_list|,
name|bos
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|writeRawValue
specifier|protected
name|void
name|writeRawValue
parameter_list|(
name|byte
index|[]
name|content
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|,
name|OutputStream
name|bos
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|context
operator|.
name|include
argument_list|()
condition|)
block|{
name|super
operator|.
name|writeRawValue
argument_list|(
name|content
argument_list|,
name|offset
argument_list|,
name|length
argument_list|,
name|bos
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|writeRawValue
specifier|protected
name|void
name|writeRawValue
parameter_list|(
name|InputStream
name|content
parameter_list|,
name|OutputStream
name|bos
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|context
operator|.
name|include
argument_list|()
condition|)
block|{
name|super
operator|.
name|writeRawValue
argument_list|(
name|content
argument_list|,
name|bos
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|writeRawValue
specifier|protected
name|void
name|writeRawValue
parameter_list|(
name|BytesReference
name|content
parameter_list|,
name|OutputStream
name|bos
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|context
operator|.
name|include
argument_list|()
condition|)
block|{
name|super
operator|.
name|writeRawValue
argument_list|(
name|content
argument_list|,
name|bos
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
name|contexts
operator|.
name|clear
argument_list|()
expr_stmt|;
name|super
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit
