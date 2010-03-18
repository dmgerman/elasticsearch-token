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
name|elasticsearch
operator|.
name|util
operator|.
name|concurrent
operator|.
name|NotThreadSafe
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
name|DateTimeZone
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
name|ReadableInstant
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
name|format
operator|.
name|DateTimeFormatter
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
name|format
operator|.
name|ISODateTimeFormat
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
comment|/**  * A helper builder for JSON documents.  *  *<p>Best constructed using {@link #stringJsonBuilder()} or {@link #binaryJsonBuilder()}. When used to create  * source for actions/operations, it is recommended to use {@link #binaryJsonBuilder()}.  *  * @author kimchy (shay.banon)  */
end_comment

begin_class
annotation|@
name|NotThreadSafe
DECL|class|JsonBuilder
specifier|public
specifier|abstract
class|class
name|JsonBuilder
parameter_list|<
name|T
extends|extends
name|JsonBuilder
parameter_list|>
block|{
DECL|field|defaultDatePrinter
specifier|private
specifier|final
specifier|static
name|DateTimeFormatter
name|defaultDatePrinter
init|=
name|ISODateTimeFormat
operator|.
name|dateTime
argument_list|()
operator|.
name|withZone
argument_list|(
name|DateTimeZone
operator|.
name|UTC
argument_list|)
decl_stmt|;
DECL|field|generator
specifier|protected
name|org
operator|.
name|codehaus
operator|.
name|jackson
operator|.
name|JsonGenerator
name|generator
decl_stmt|;
DECL|field|builder
specifier|protected
name|T
name|builder
decl_stmt|;
DECL|method|stringJsonBuilder
specifier|public
specifier|static
name|StringJsonBuilder
name|stringJsonBuilder
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|StringJsonBuilder
operator|.
name|Cached
operator|.
name|cached
argument_list|()
return|;
block|}
DECL|method|jsonBuilder
specifier|public
specifier|static
name|BinaryJsonBuilder
name|jsonBuilder
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|BinaryJsonBuilder
operator|.
name|Cached
operator|.
name|cached
argument_list|()
return|;
block|}
DECL|method|binaryJsonBuilder
specifier|public
specifier|static
name|BinaryJsonBuilder
name|binaryJsonBuilder
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|BinaryJsonBuilder
operator|.
name|Cached
operator|.
name|cached
argument_list|()
return|;
block|}
DECL|method|prettyPrint
specifier|public
name|T
name|prettyPrint
parameter_list|()
block|{
name|generator
operator|.
name|useDefaultPrettyPrinter
argument_list|()
expr_stmt|;
return|return
name|builder
return|;
block|}
DECL|method|startObject
specifier|public
name|T
name|startObject
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|IOException
block|{
name|field
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|startObject
argument_list|()
expr_stmt|;
return|return
name|builder
return|;
block|}
DECL|method|startObject
specifier|public
name|T
name|startObject
parameter_list|()
throws|throws
name|IOException
block|{
name|generator
operator|.
name|writeStartObject
argument_list|()
expr_stmt|;
return|return
name|builder
return|;
block|}
DECL|method|endObject
specifier|public
name|T
name|endObject
parameter_list|()
throws|throws
name|IOException
block|{
name|generator
operator|.
name|writeEndObject
argument_list|()
expr_stmt|;
return|return
name|builder
return|;
block|}
DECL|method|array
specifier|public
name|T
name|array
parameter_list|(
name|String
name|name
parameter_list|,
name|String
modifier|...
name|values
parameter_list|)
throws|throws
name|IOException
block|{
name|startArray
argument_list|(
name|name
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|value
range|:
name|values
control|)
block|{
name|value
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
name|endArray
argument_list|()
expr_stmt|;
return|return
name|builder
return|;
block|}
DECL|method|array
specifier|public
name|T
name|array
parameter_list|(
name|String
name|name
parameter_list|,
name|Object
modifier|...
name|values
parameter_list|)
throws|throws
name|IOException
block|{
name|startArray
argument_list|(
name|name
argument_list|)
expr_stmt|;
for|for
control|(
name|Object
name|value
range|:
name|values
control|)
block|{
name|value
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
name|endArray
argument_list|()
expr_stmt|;
return|return
name|builder
return|;
block|}
DECL|method|startArray
specifier|public
name|T
name|startArray
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|IOException
block|{
name|field
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|startArray
argument_list|()
expr_stmt|;
return|return
name|builder
return|;
block|}
DECL|method|startArray
specifier|public
name|T
name|startArray
parameter_list|()
throws|throws
name|IOException
block|{
name|generator
operator|.
name|writeStartArray
argument_list|()
expr_stmt|;
return|return
name|builder
return|;
block|}
DECL|method|endArray
specifier|public
name|T
name|endArray
parameter_list|()
throws|throws
name|IOException
block|{
name|generator
operator|.
name|writeEndArray
argument_list|()
expr_stmt|;
return|return
name|builder
return|;
block|}
DECL|method|field
specifier|public
name|T
name|field
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|IOException
block|{
name|generator
operator|.
name|writeFieldName
argument_list|(
name|name
argument_list|)
expr_stmt|;
return|return
name|builder
return|;
block|}
DECL|method|field
specifier|public
name|T
name|field
parameter_list|(
name|String
name|name
parameter_list|,
name|char
index|[]
name|value
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
name|generator
operator|.
name|writeFieldName
argument_list|(
name|name
argument_list|)
expr_stmt|;
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
name|generator
operator|.
name|writeNull
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|generator
operator|.
name|writeString
argument_list|(
name|value
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
expr_stmt|;
block|}
return|return
name|builder
return|;
block|}
DECL|method|field
specifier|public
name|T
name|field
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|value
parameter_list|)
throws|throws
name|IOException
block|{
name|generator
operator|.
name|writeFieldName
argument_list|(
name|name
argument_list|)
expr_stmt|;
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
name|generator
operator|.
name|writeNull
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|generator
operator|.
name|writeString
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
return|return
name|builder
return|;
block|}
DECL|method|field
specifier|public
name|T
name|field
parameter_list|(
name|String
name|name
parameter_list|,
name|Integer
name|value
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|field
argument_list|(
name|name
argument_list|,
name|value
operator|.
name|intValue
argument_list|()
argument_list|)
return|;
block|}
DECL|method|field
specifier|public
name|T
name|field
parameter_list|(
name|String
name|name
parameter_list|,
name|int
name|value
parameter_list|)
throws|throws
name|IOException
block|{
name|generator
operator|.
name|writeFieldName
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|generator
operator|.
name|writeNumber
argument_list|(
name|value
argument_list|)
expr_stmt|;
return|return
name|builder
return|;
block|}
DECL|method|field
specifier|public
name|T
name|field
parameter_list|(
name|String
name|name
parameter_list|,
name|Long
name|value
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|field
argument_list|(
name|name
argument_list|,
name|value
operator|.
name|longValue
argument_list|()
argument_list|)
return|;
block|}
DECL|method|field
specifier|public
name|T
name|field
parameter_list|(
name|String
name|name
parameter_list|,
name|long
name|value
parameter_list|)
throws|throws
name|IOException
block|{
name|generator
operator|.
name|writeFieldName
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|generator
operator|.
name|writeNumber
argument_list|(
name|value
argument_list|)
expr_stmt|;
return|return
name|builder
return|;
block|}
DECL|method|field
specifier|public
name|T
name|field
parameter_list|(
name|String
name|name
parameter_list|,
name|Float
name|value
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|field
argument_list|(
name|name
argument_list|,
name|value
operator|.
name|floatValue
argument_list|()
argument_list|)
return|;
block|}
DECL|method|field
specifier|public
name|T
name|field
parameter_list|(
name|String
name|name
parameter_list|,
name|float
name|value
parameter_list|)
throws|throws
name|IOException
block|{
name|generator
operator|.
name|writeFieldName
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|generator
operator|.
name|writeNumber
argument_list|(
name|value
argument_list|)
expr_stmt|;
return|return
name|builder
return|;
block|}
DECL|method|field
specifier|public
name|T
name|field
parameter_list|(
name|String
name|name
parameter_list|,
name|Double
name|value
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|field
argument_list|(
name|name
argument_list|,
name|value
operator|.
name|doubleValue
argument_list|()
argument_list|)
return|;
block|}
DECL|method|field
specifier|public
name|T
name|field
parameter_list|(
name|String
name|name
parameter_list|,
name|double
name|value
parameter_list|)
throws|throws
name|IOException
block|{
name|generator
operator|.
name|writeFieldName
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|generator
operator|.
name|writeNumber
argument_list|(
name|value
argument_list|)
expr_stmt|;
return|return
name|builder
return|;
block|}
DECL|method|field
specifier|public
name|T
name|field
parameter_list|(
name|String
name|name
parameter_list|,
name|Object
name|value
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
name|nullField
argument_list|(
name|name
argument_list|)
expr_stmt|;
return|return
name|builder
return|;
block|}
name|Class
name|type
init|=
name|value
operator|.
name|getClass
argument_list|()
decl_stmt|;
if|if
condition|(
name|type
operator|==
name|String
operator|.
name|class
condition|)
block|{
name|field
argument_list|(
name|name
argument_list|,
operator|(
name|String
operator|)
name|value
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|type
operator|==
name|Float
operator|.
name|class
condition|)
block|{
name|field
argument_list|(
name|name
argument_list|,
operator|(
operator|(
name|Float
operator|)
name|value
operator|)
operator|.
name|floatValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|type
operator|==
name|Double
operator|.
name|class
condition|)
block|{
name|field
argument_list|(
name|name
argument_list|,
operator|(
operator|(
name|Double
operator|)
name|value
operator|)
operator|.
name|doubleValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|type
operator|==
name|Integer
operator|.
name|class
condition|)
block|{
name|field
argument_list|(
name|name
argument_list|,
operator|(
operator|(
name|Integer
operator|)
name|value
operator|)
operator|.
name|intValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|type
operator|==
name|Long
operator|.
name|class
condition|)
block|{
name|field
argument_list|(
name|name
argument_list|,
operator|(
operator|(
name|Long
operator|)
name|value
operator|)
operator|.
name|longValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|type
operator|==
name|Boolean
operator|.
name|class
condition|)
block|{
name|field
argument_list|(
name|name
argument_list|,
operator|(
operator|(
name|Boolean
operator|)
name|value
operator|)
operator|.
name|booleanValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|type
operator|==
name|Date
operator|.
name|class
condition|)
block|{
name|field
argument_list|(
name|name
argument_list|,
operator|(
name|Date
operator|)
name|value
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|type
operator|==
name|byte
index|[]
operator|.
name|class
condition|)
block|{
name|field
argument_list|(
name|name
argument_list|,
operator|(
name|byte
index|[]
operator|)
name|value
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|value
operator|instanceof
name|ReadableInstant
condition|)
block|{
name|field
argument_list|(
name|name
argument_list|,
operator|(
name|ReadableInstant
operator|)
name|value
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|field
argument_list|(
name|name
argument_list|,
name|value
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|builder
return|;
block|}
DECL|method|field
specifier|public
name|T
name|field
parameter_list|(
name|String
name|name
parameter_list|,
name|boolean
name|value
parameter_list|)
throws|throws
name|IOException
block|{
name|generator
operator|.
name|writeFieldName
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|generator
operator|.
name|writeBoolean
argument_list|(
name|value
argument_list|)
expr_stmt|;
return|return
name|builder
return|;
block|}
DECL|method|field
specifier|public
name|T
name|field
parameter_list|(
name|String
name|name
parameter_list|,
name|byte
index|[]
name|value
parameter_list|)
throws|throws
name|IOException
block|{
name|generator
operator|.
name|writeFieldName
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|generator
operator|.
name|writeBinary
argument_list|(
name|value
argument_list|)
expr_stmt|;
return|return
name|builder
return|;
block|}
DECL|method|field
specifier|public
name|T
name|field
parameter_list|(
name|String
name|name
parameter_list|,
name|ReadableInstant
name|date
parameter_list|)
throws|throws
name|IOException
block|{
name|generator
operator|.
name|writeFieldName
argument_list|(
name|name
argument_list|)
expr_stmt|;
return|return
name|value
argument_list|(
name|date
argument_list|)
return|;
block|}
DECL|method|field
specifier|public
name|T
name|field
parameter_list|(
name|String
name|name
parameter_list|,
name|ReadableInstant
name|date
parameter_list|,
name|DateTimeFormatter
name|formatter
parameter_list|)
throws|throws
name|IOException
block|{
name|generator
operator|.
name|writeFieldName
argument_list|(
name|name
argument_list|)
expr_stmt|;
return|return
name|value
argument_list|(
name|date
argument_list|,
name|formatter
argument_list|)
return|;
block|}
DECL|method|field
specifier|public
name|T
name|field
parameter_list|(
name|String
name|name
parameter_list|,
name|Date
name|date
parameter_list|)
throws|throws
name|IOException
block|{
name|generator
operator|.
name|writeFieldName
argument_list|(
name|name
argument_list|)
expr_stmt|;
return|return
name|value
argument_list|(
name|date
argument_list|)
return|;
block|}
DECL|method|field
specifier|public
name|T
name|field
parameter_list|(
name|String
name|name
parameter_list|,
name|Date
name|date
parameter_list|,
name|DateTimeFormatter
name|formatter
parameter_list|)
throws|throws
name|IOException
block|{
name|generator
operator|.
name|writeFieldName
argument_list|(
name|name
argument_list|)
expr_stmt|;
return|return
name|value
argument_list|(
name|date
argument_list|,
name|formatter
argument_list|)
return|;
block|}
DECL|method|nullField
specifier|public
name|T
name|nullField
parameter_list|(
name|String
name|name
parameter_list|)
throws|throws
name|IOException
block|{
name|generator
operator|.
name|writeNullField
argument_list|(
name|name
argument_list|)
expr_stmt|;
return|return
name|builder
return|;
block|}
DECL|method|nullValue
specifier|public
name|T
name|nullValue
parameter_list|()
throws|throws
name|IOException
block|{
name|generator
operator|.
name|writeNull
argument_list|()
expr_stmt|;
return|return
name|builder
return|;
block|}
DECL|method|raw
specifier|public
name|T
name|raw
parameter_list|(
name|String
name|json
parameter_list|)
throws|throws
name|IOException
block|{
name|generator
operator|.
name|writeRaw
argument_list|(
name|json
argument_list|)
expr_stmt|;
return|return
name|builder
return|;
block|}
DECL|method|raw
specifier|public
specifier|abstract
name|T
name|raw
parameter_list|(
name|byte
index|[]
name|json
parameter_list|)
throws|throws
name|IOException
function_decl|;
DECL|method|value
specifier|public
name|T
name|value
parameter_list|(
name|Boolean
name|value
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|value
argument_list|(
name|value
operator|.
name|booleanValue
argument_list|()
argument_list|)
return|;
block|}
DECL|method|value
specifier|public
name|T
name|value
parameter_list|(
name|boolean
name|value
parameter_list|)
throws|throws
name|IOException
block|{
name|generator
operator|.
name|writeBoolean
argument_list|(
name|value
argument_list|)
expr_stmt|;
return|return
name|builder
return|;
block|}
DECL|method|value
specifier|public
name|T
name|value
parameter_list|(
name|ReadableInstant
name|date
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|value
argument_list|(
name|date
argument_list|,
name|defaultDatePrinter
argument_list|)
return|;
block|}
DECL|method|value
specifier|public
name|T
name|value
parameter_list|(
name|ReadableInstant
name|date
parameter_list|,
name|DateTimeFormatter
name|dateTimeFormatter
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|value
argument_list|(
name|dateTimeFormatter
operator|.
name|print
argument_list|(
name|date
argument_list|)
argument_list|)
return|;
block|}
DECL|method|value
specifier|public
name|T
name|value
parameter_list|(
name|Date
name|date
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|value
argument_list|(
name|date
argument_list|,
name|defaultDatePrinter
argument_list|)
return|;
block|}
DECL|method|value
specifier|public
name|T
name|value
parameter_list|(
name|Date
name|date
parameter_list|,
name|DateTimeFormatter
name|dateTimeFormatter
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|value
argument_list|(
name|dateTimeFormatter
operator|.
name|print
argument_list|(
name|date
operator|.
name|getTime
argument_list|()
argument_list|)
argument_list|)
return|;
block|}
DECL|method|value
specifier|public
name|T
name|value
parameter_list|(
name|Integer
name|value
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|value
argument_list|(
name|value
operator|.
name|intValue
argument_list|()
argument_list|)
return|;
block|}
DECL|method|value
specifier|public
name|T
name|value
parameter_list|(
name|int
name|value
parameter_list|)
throws|throws
name|IOException
block|{
name|generator
operator|.
name|writeNumber
argument_list|(
name|value
argument_list|)
expr_stmt|;
return|return
name|builder
return|;
block|}
DECL|method|value
specifier|public
name|T
name|value
parameter_list|(
name|Long
name|value
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|value
argument_list|(
name|value
operator|.
name|longValue
argument_list|()
argument_list|)
return|;
block|}
DECL|method|value
specifier|public
name|T
name|value
parameter_list|(
name|long
name|value
parameter_list|)
throws|throws
name|IOException
block|{
name|generator
operator|.
name|writeNumber
argument_list|(
name|value
argument_list|)
expr_stmt|;
return|return
name|builder
return|;
block|}
DECL|method|value
specifier|public
name|T
name|value
parameter_list|(
name|Float
name|value
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|value
argument_list|(
name|value
operator|.
name|floatValue
argument_list|()
argument_list|)
return|;
block|}
DECL|method|value
specifier|public
name|T
name|value
parameter_list|(
name|float
name|value
parameter_list|)
throws|throws
name|IOException
block|{
name|generator
operator|.
name|writeNumber
argument_list|(
name|value
argument_list|)
expr_stmt|;
return|return
name|builder
return|;
block|}
DECL|method|value
specifier|public
name|T
name|value
parameter_list|(
name|Double
name|value
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|value
argument_list|(
name|value
operator|.
name|doubleValue
argument_list|()
argument_list|)
return|;
block|}
DECL|method|value
specifier|public
name|T
name|value
parameter_list|(
name|double
name|value
parameter_list|)
throws|throws
name|IOException
block|{
name|generator
operator|.
name|writeNumber
argument_list|(
name|value
argument_list|)
expr_stmt|;
return|return
name|builder
return|;
block|}
DECL|method|value
specifier|public
name|T
name|value
parameter_list|(
name|String
name|value
parameter_list|)
throws|throws
name|IOException
block|{
name|generator
operator|.
name|writeString
argument_list|(
name|value
argument_list|)
expr_stmt|;
return|return
name|builder
return|;
block|}
DECL|method|value
specifier|public
name|T
name|value
parameter_list|(
name|byte
index|[]
name|value
parameter_list|)
throws|throws
name|IOException
block|{
name|generator
operator|.
name|writeBinary
argument_list|(
name|value
argument_list|)
expr_stmt|;
return|return
name|builder
return|;
block|}
DECL|method|value
specifier|public
name|T
name|value
parameter_list|(
name|Object
name|value
parameter_list|)
throws|throws
name|IOException
block|{
name|Class
name|type
init|=
name|value
operator|.
name|getClass
argument_list|()
decl_stmt|;
if|if
condition|(
name|type
operator|==
name|String
operator|.
name|class
condition|)
block|{
name|value
argument_list|(
operator|(
name|String
operator|)
name|value
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|type
operator|==
name|Float
operator|.
name|class
condition|)
block|{
name|value
argument_list|(
operator|(
operator|(
name|Float
operator|)
name|value
operator|)
operator|.
name|floatValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|type
operator|==
name|Double
operator|.
name|class
condition|)
block|{
name|value
argument_list|(
operator|(
operator|(
name|Double
operator|)
name|value
operator|)
operator|.
name|doubleValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|type
operator|==
name|Integer
operator|.
name|class
condition|)
block|{
name|value
argument_list|(
operator|(
operator|(
name|Integer
operator|)
name|value
operator|)
operator|.
name|intValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|type
operator|==
name|Long
operator|.
name|class
condition|)
block|{
name|value
argument_list|(
operator|(
operator|(
name|Long
operator|)
name|value
operator|)
operator|.
name|longValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|type
operator|==
name|Boolean
operator|.
name|class
condition|)
block|{
name|value
argument_list|(
operator|(
name|Boolean
operator|)
name|value
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|type
operator|==
name|byte
index|[]
operator|.
name|class
condition|)
block|{
name|value
argument_list|(
operator|(
name|byte
index|[]
operator|)
name|value
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|type
operator|==
name|Date
operator|.
name|class
condition|)
block|{
name|value
argument_list|(
operator|(
name|Date
operator|)
name|value
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|value
operator|instanceof
name|ReadableInstant
condition|)
block|{
name|value
argument_list|(
operator|(
name|ReadableInstant
operator|)
name|value
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Type not allowed ["
operator|+
name|type
operator|+
literal|"]"
argument_list|)
throw|;
block|}
return|return
name|builder
return|;
block|}
DECL|method|flush
specifier|public
name|T
name|flush
parameter_list|()
throws|throws
name|IOException
block|{
name|generator
operator|.
name|flush
argument_list|()
expr_stmt|;
return|return
name|builder
return|;
block|}
DECL|method|reset
specifier|public
specifier|abstract
name|T
name|reset
parameter_list|()
throws|throws
name|IOException
function_decl|;
DECL|method|unsafeBytes
specifier|public
specifier|abstract
name|byte
index|[]
name|unsafeBytes
parameter_list|()
throws|throws
name|IOException
function_decl|;
DECL|method|unsafeBytesLength
specifier|public
specifier|abstract
name|int
name|unsafeBytesLength
parameter_list|()
throws|throws
name|IOException
function_decl|;
DECL|method|copiedBytes
specifier|public
specifier|abstract
name|byte
index|[]
name|copiedBytes
parameter_list|()
throws|throws
name|IOException
function_decl|;
DECL|method|string
specifier|public
specifier|abstract
name|String
name|string
parameter_list|()
throws|throws
name|IOException
function_decl|;
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
block|{
try|try
block|{
name|generator
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// ignore
block|}
block|}
block|}
end_class

end_unit

