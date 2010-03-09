begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.util.json
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|json
package|;
end_package

begin_import
import|import
name|org
operator|.
name|codehaus
operator|.
name|jackson
operator|.
name|*
import|;
end_import

begin_import
import|import
name|org
operator|.
name|codehaus
operator|.
name|jackson
operator|.
name|map
operator|.
name|DeserializationContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|codehaus
operator|.
name|jackson
operator|.
name|map
operator|.
name|ObjectMapper
import|;
end_import

begin_import
import|import
name|org
operator|.
name|codehaus
operator|.
name|jackson
operator|.
name|map
operator|.
name|SerializerProvider
import|;
end_import

begin_import
import|import
name|org
operator|.
name|codehaus
operator|.
name|jackson
operator|.
name|map
operator|.
name|deser
operator|.
name|CustomDeserializerFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|codehaus
operator|.
name|jackson
operator|.
name|map
operator|.
name|deser
operator|.
name|StdDeserializer
import|;
end_import

begin_import
import|import
name|org
operator|.
name|codehaus
operator|.
name|jackson
operator|.
name|map
operator|.
name|deser
operator|.
name|StdDeserializerProvider
import|;
end_import

begin_import
import|import
name|org
operator|.
name|codehaus
operator|.
name|jackson
operator|.
name|map
operator|.
name|ser
operator|.
name|CustomSerializerFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|codehaus
operator|.
name|jackson
operator|.
name|map
operator|.
name|ser
operator|.
name|SerializerBase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|joda
operator|.
name|FormatDateTimeFormatter
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|joda
operator|.
name|Joda
import|;
end_import

begin_import
import|import
name|org
operator|.
name|joda
operator|.
name|time
operator|.
name|DateTime
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
name|Date
import|;
end_import

begin_comment
comment|/**  * A set of helper methods for Jackson.  *  * @author kimchy (Shay Banon)  */
end_comment

begin_class
DECL|class|Jackson
specifier|public
specifier|final
class|class
name|Jackson
block|{
DECL|field|defaultJsonFactory
specifier|private
specifier|static
specifier|final
name|JsonFactory
name|defaultJsonFactory
decl_stmt|;
DECL|field|defaultObjectMapper
specifier|private
specifier|static
specifier|final
name|ObjectMapper
name|defaultObjectMapper
decl_stmt|;
static|static
block|{
name|defaultJsonFactory
operator|=
name|newJsonFactory
argument_list|()
expr_stmt|;
name|defaultObjectMapper
operator|=
name|newObjectMapper
argument_list|()
expr_stmt|;
block|}
DECL|method|defaultJsonFactory
specifier|public
specifier|static
name|JsonFactory
name|defaultJsonFactory
parameter_list|()
block|{
return|return
name|defaultJsonFactory
return|;
block|}
DECL|method|defaultObjectMapper
specifier|public
specifier|static
name|ObjectMapper
name|defaultObjectMapper
parameter_list|()
block|{
return|return
name|defaultObjectMapper
return|;
block|}
DECL|method|newJsonFactory
specifier|public
specifier|static
name|JsonFactory
name|newJsonFactory
parameter_list|()
block|{
name|JsonFactory
name|jsonFactory
init|=
operator|new
name|JsonFactory
argument_list|()
decl_stmt|;
name|jsonFactory
operator|.
name|configure
argument_list|(
name|JsonParser
operator|.
name|Feature
operator|.
name|ALLOW_UNQUOTED_FIELD_NAMES
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|jsonFactory
operator|.
name|configure
argument_list|(
name|JsonGenerator
operator|.
name|Feature
operator|.
name|QUOTE_FIELD_NAMES
argument_list|,
literal|true
argument_list|)
expr_stmt|;
return|return
name|jsonFactory
return|;
block|}
DECL|method|newObjectMapper
specifier|public
specifier|static
name|ObjectMapper
name|newObjectMapper
parameter_list|()
block|{
name|ObjectMapper
name|mapper
init|=
operator|new
name|ObjectMapper
argument_list|()
decl_stmt|;
name|mapper
operator|.
name|configure
argument_list|(
name|JsonParser
operator|.
name|Feature
operator|.
name|ALLOW_UNQUOTED_FIELD_NAMES
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|mapper
operator|.
name|configure
argument_list|(
name|JsonGenerator
operator|.
name|Feature
operator|.
name|QUOTE_FIELD_NAMES
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|CustomSerializerFactory
name|serializerFactory
init|=
operator|new
name|CustomSerializerFactory
argument_list|()
decl_stmt|;
name|serializerFactory
operator|.
name|addSpecificMapping
argument_list|(
name|Date
operator|.
name|class
argument_list|,
operator|new
name|DateSerializer
argument_list|()
argument_list|)
expr_stmt|;
name|serializerFactory
operator|.
name|addSpecificMapping
argument_list|(
name|DateTime
operator|.
name|class
argument_list|,
operator|new
name|DateTimeSerializer
argument_list|()
argument_list|)
expr_stmt|;
name|mapper
operator|.
name|setSerializerFactory
argument_list|(
name|serializerFactory
argument_list|)
expr_stmt|;
name|CustomDeserializerFactory
name|deserializerFactory
init|=
operator|new
name|CustomDeserializerFactory
argument_list|()
decl_stmt|;
name|deserializerFactory
operator|.
name|addSpecificMapping
argument_list|(
name|Date
operator|.
name|class
argument_list|,
operator|new
name|DateDeserializer
argument_list|()
argument_list|)
expr_stmt|;
name|deserializerFactory
operator|.
name|addSpecificMapping
argument_list|(
name|DateTime
operator|.
name|class
argument_list|,
operator|new
name|DateTimeDeserializer
argument_list|()
argument_list|)
expr_stmt|;
name|mapper
operator|.
name|setDeserializerProvider
argument_list|(
operator|new
name|StdDeserializerProvider
argument_list|(
name|deserializerFactory
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|mapper
return|;
block|}
DECL|method|Jackson
specifier|private
name|Jackson
parameter_list|()
block|{      }
DECL|class|DateDeserializer
specifier|public
specifier|static
class|class
name|DateDeserializer
extends|extends
name|StdDeserializer
argument_list|<
name|Date
argument_list|>
block|{
DECL|field|formatter
specifier|private
specifier|final
name|FormatDateTimeFormatter
name|formatter
init|=
name|Joda
operator|.
name|forPattern
argument_list|(
literal|"dateTime"
argument_list|)
decl_stmt|;
DECL|method|DateDeserializer
specifier|public
name|DateDeserializer
parameter_list|()
block|{
name|super
argument_list|(
name|Date
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
DECL|method|deserialize
annotation|@
name|Override
specifier|public
name|Date
name|deserialize
parameter_list|(
name|JsonParser
name|jp
parameter_list|,
name|DeserializationContext
name|ctxt
parameter_list|)
throws|throws
name|IOException
throws|,
name|JsonProcessingException
block|{
name|JsonToken
name|t
init|=
name|jp
operator|.
name|getCurrentToken
argument_list|()
decl_stmt|;
if|if
condition|(
name|t
operator|==
name|JsonToken
operator|.
name|VALUE_STRING
condition|)
block|{
return|return
operator|new
name|Date
argument_list|(
name|formatter
operator|.
name|parser
argument_list|()
operator|.
name|parseMillis
argument_list|(
name|jp
operator|.
name|getText
argument_list|()
argument_list|)
argument_list|)
return|;
block|}
throw|throw
name|ctxt
operator|.
name|mappingException
argument_list|(
name|getValueClass
argument_list|()
argument_list|)
throw|;
block|}
block|}
DECL|class|DateSerializer
specifier|public
specifier|final
specifier|static
class|class
name|DateSerializer
extends|extends
name|SerializerBase
argument_list|<
name|Date
argument_list|>
block|{
DECL|field|formatter
specifier|private
specifier|final
name|FormatDateTimeFormatter
name|formatter
init|=
name|Joda
operator|.
name|forPattern
argument_list|(
literal|"dateTime"
argument_list|)
decl_stmt|;
DECL|method|serialize
annotation|@
name|Override
specifier|public
name|void
name|serialize
parameter_list|(
name|Date
name|value
parameter_list|,
name|JsonGenerator
name|jgen
parameter_list|,
name|SerializerProvider
name|provider
parameter_list|)
throws|throws
name|IOException
throws|,
name|JsonGenerationException
block|{
name|jgen
operator|.
name|writeString
argument_list|(
name|formatter
operator|.
name|parser
argument_list|()
operator|.
name|print
argument_list|(
name|value
operator|.
name|getTime
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|getSchema
annotation|@
name|Override
specifier|public
name|JsonNode
name|getSchema
parameter_list|(
name|SerializerProvider
name|provider
parameter_list|,
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Type
name|typeHint
parameter_list|)
block|{
return|return
name|createSchemaNode
argument_list|(
literal|"string"
argument_list|,
literal|true
argument_list|)
return|;
block|}
block|}
DECL|class|DateTimeDeserializer
specifier|public
specifier|static
class|class
name|DateTimeDeserializer
extends|extends
name|StdDeserializer
argument_list|<
name|DateTime
argument_list|>
block|{
DECL|field|formatter
specifier|private
specifier|final
name|FormatDateTimeFormatter
name|formatter
init|=
name|Joda
operator|.
name|forPattern
argument_list|(
literal|"dateTime"
argument_list|)
decl_stmt|;
DECL|method|DateTimeDeserializer
specifier|public
name|DateTimeDeserializer
parameter_list|()
block|{
name|super
argument_list|(
name|DateTime
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
DECL|method|deserialize
annotation|@
name|Override
specifier|public
name|DateTime
name|deserialize
parameter_list|(
name|JsonParser
name|jp
parameter_list|,
name|DeserializationContext
name|ctxt
parameter_list|)
throws|throws
name|IOException
throws|,
name|JsonProcessingException
block|{
name|JsonToken
name|t
init|=
name|jp
operator|.
name|getCurrentToken
argument_list|()
decl_stmt|;
if|if
condition|(
name|t
operator|==
name|JsonToken
operator|.
name|VALUE_STRING
condition|)
block|{
return|return
name|formatter
operator|.
name|parser
argument_list|()
operator|.
name|parseDateTime
argument_list|(
name|jp
operator|.
name|getText
argument_list|()
argument_list|)
return|;
block|}
throw|throw
name|ctxt
operator|.
name|mappingException
argument_list|(
name|getValueClass
argument_list|()
argument_list|)
throw|;
block|}
block|}
DECL|class|DateTimeSerializer
specifier|public
specifier|final
specifier|static
class|class
name|DateTimeSerializer
extends|extends
name|SerializerBase
argument_list|<
name|DateTime
argument_list|>
block|{
DECL|field|formatter
specifier|private
specifier|final
name|FormatDateTimeFormatter
name|formatter
init|=
name|Joda
operator|.
name|forPattern
argument_list|(
literal|"dateTime"
argument_list|)
decl_stmt|;
DECL|method|serialize
annotation|@
name|Override
specifier|public
name|void
name|serialize
parameter_list|(
name|DateTime
name|value
parameter_list|,
name|JsonGenerator
name|jgen
parameter_list|,
name|SerializerProvider
name|provider
parameter_list|)
throws|throws
name|IOException
throws|,
name|JsonGenerationException
block|{
name|jgen
operator|.
name|writeString
argument_list|(
name|formatter
operator|.
name|printer
argument_list|()
operator|.
name|print
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|getSchema
annotation|@
name|Override
specifier|public
name|JsonNode
name|getSchema
parameter_list|(
name|SerializerProvider
name|provider
parameter_list|,
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Type
name|typeHint
parameter_list|)
block|{
return|return
name|createSchemaNode
argument_list|(
literal|"string"
argument_list|,
literal|true
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

