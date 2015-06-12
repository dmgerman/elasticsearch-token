begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.xcontent
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|xcontent
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
name|dataformat
operator|.
name|cbor
operator|.
name|CBORConstants
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
name|dataformat
operator|.
name|smile
operator|.
name|SmileConstants
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchParseException
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
name|BytesArray
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
name|cbor
operator|.
name|CborXContent
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
name|JsonXContent
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
name|smile
operator|.
name|SmileXContent
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
name|yaml
operator|.
name|YamlXContent
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
name|util
operator|.
name|Arrays
import|;
end_import

begin_comment
comment|/**  * A one stop to use {@link org.elasticsearch.common.xcontent.XContent} and {@link XContentBuilder}.  */
end_comment

begin_class
DECL|class|XContentFactory
specifier|public
class|class
name|XContentFactory
block|{
DECL|field|GUESS_HEADER_LENGTH
specifier|private
specifier|static
name|int
name|GUESS_HEADER_LENGTH
init|=
literal|20
decl_stmt|;
comment|/**      * Returns a content builder using JSON format ({@link org.elasticsearch.common.xcontent.XContentType#JSON}.      */
DECL|method|jsonBuilder
specifier|public
specifier|static
name|XContentBuilder
name|jsonBuilder
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|contentBuilder
argument_list|(
name|XContentType
operator|.
name|JSON
argument_list|)
return|;
block|}
comment|/**      * Constructs a new json builder that will output the result into the provided output stream.      */
DECL|method|jsonBuilder
specifier|public
specifier|static
name|XContentBuilder
name|jsonBuilder
parameter_list|(
name|OutputStream
name|os
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|XContentBuilder
argument_list|(
name|JsonXContent
operator|.
name|jsonXContent
argument_list|,
name|os
argument_list|)
return|;
block|}
comment|/**      * Returns a content builder using SMILE format ({@link org.elasticsearch.common.xcontent.XContentType#SMILE}.      */
DECL|method|smileBuilder
specifier|public
specifier|static
name|XContentBuilder
name|smileBuilder
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|contentBuilder
argument_list|(
name|XContentType
operator|.
name|SMILE
argument_list|)
return|;
block|}
comment|/**      * Constructs a new json builder that will output the result into the provided output stream.      */
DECL|method|smileBuilder
specifier|public
specifier|static
name|XContentBuilder
name|smileBuilder
parameter_list|(
name|OutputStream
name|os
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|XContentBuilder
argument_list|(
name|SmileXContent
operator|.
name|smileXContent
argument_list|,
name|os
argument_list|)
return|;
block|}
comment|/**      * Returns a content builder using YAML format ({@link org.elasticsearch.common.xcontent.XContentType#YAML}.      */
DECL|method|yamlBuilder
specifier|public
specifier|static
name|XContentBuilder
name|yamlBuilder
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|contentBuilder
argument_list|(
name|XContentType
operator|.
name|YAML
argument_list|)
return|;
block|}
comment|/**      * Constructs a new yaml builder that will output the result into the provided output stream.      */
DECL|method|yamlBuilder
specifier|public
specifier|static
name|XContentBuilder
name|yamlBuilder
parameter_list|(
name|OutputStream
name|os
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|XContentBuilder
argument_list|(
name|YamlXContent
operator|.
name|yamlXContent
argument_list|,
name|os
argument_list|)
return|;
block|}
comment|/**      * Returns a content builder using CBOR format ({@link org.elasticsearch.common.xcontent.XContentType#CBOR}.      */
DECL|method|cborBuilder
specifier|public
specifier|static
name|XContentBuilder
name|cborBuilder
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|contentBuilder
argument_list|(
name|XContentType
operator|.
name|CBOR
argument_list|)
return|;
block|}
comment|/**      * Constructs a new cbor builder that will output the result into the provided output stream.      */
DECL|method|cborBuilder
specifier|public
specifier|static
name|XContentBuilder
name|cborBuilder
parameter_list|(
name|OutputStream
name|os
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|XContentBuilder
argument_list|(
name|CborXContent
operator|.
name|cborXContent
argument_list|,
name|os
argument_list|)
return|;
block|}
comment|/**      * Constructs a xcontent builder that will output the result into the provided output stream.      */
DECL|method|contentBuilder
specifier|public
specifier|static
name|XContentBuilder
name|contentBuilder
parameter_list|(
name|XContentType
name|type
parameter_list|,
name|OutputStream
name|outputStream
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|type
operator|==
name|XContentType
operator|.
name|JSON
condition|)
block|{
return|return
name|jsonBuilder
argument_list|(
name|outputStream
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|type
operator|==
name|XContentType
operator|.
name|SMILE
condition|)
block|{
return|return
name|smileBuilder
argument_list|(
name|outputStream
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|type
operator|==
name|XContentType
operator|.
name|YAML
condition|)
block|{
return|return
name|yamlBuilder
argument_list|(
name|outputStream
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|type
operator|==
name|XContentType
operator|.
name|CBOR
condition|)
block|{
return|return
name|cborBuilder
argument_list|(
name|outputStream
argument_list|)
return|;
block|}
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"No matching content type for "
operator|+
name|type
argument_list|)
throw|;
block|}
comment|/**      * Returns a binary content builder for the provided content type.      */
DECL|method|contentBuilder
specifier|public
specifier|static
name|XContentBuilder
name|contentBuilder
parameter_list|(
name|XContentType
name|type
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|type
operator|==
name|XContentType
operator|.
name|JSON
condition|)
block|{
return|return
name|JsonXContent
operator|.
name|contentBuilder
argument_list|()
return|;
block|}
elseif|else
if|if
condition|(
name|type
operator|==
name|XContentType
operator|.
name|SMILE
condition|)
block|{
return|return
name|SmileXContent
operator|.
name|contentBuilder
argument_list|()
return|;
block|}
elseif|else
if|if
condition|(
name|type
operator|==
name|XContentType
operator|.
name|YAML
condition|)
block|{
return|return
name|YamlXContent
operator|.
name|contentBuilder
argument_list|()
return|;
block|}
elseif|else
if|if
condition|(
name|type
operator|==
name|XContentType
operator|.
name|CBOR
condition|)
block|{
return|return
name|CborXContent
operator|.
name|contentBuilder
argument_list|()
return|;
block|}
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"No matching content type for "
operator|+
name|type
argument_list|)
throw|;
block|}
comment|/**      * Returns the {@link org.elasticsearch.common.xcontent.XContent} for the provided content type.      */
DECL|method|xContent
specifier|public
specifier|static
name|XContent
name|xContent
parameter_list|(
name|XContentType
name|type
parameter_list|)
block|{
return|return
name|type
operator|.
name|xContent
argument_list|()
return|;
block|}
comment|/**      * Guesses the content type based on the provided char sequence.      */
DECL|method|xContentType
specifier|public
specifier|static
name|XContentType
name|xContentType
parameter_list|(
name|CharSequence
name|content
parameter_list|)
block|{
name|int
name|length
init|=
name|content
operator|.
name|length
argument_list|()
operator|<
name|GUESS_HEADER_LENGTH
condition|?
name|content
operator|.
name|length
argument_list|()
else|:
name|GUESS_HEADER_LENGTH
decl_stmt|;
if|if
condition|(
name|length
operator|==
literal|0
condition|)
block|{
return|return
literal|null
return|;
block|}
name|char
name|first
init|=
name|content
operator|.
name|charAt
argument_list|(
literal|0
argument_list|)
decl_stmt|;
if|if
condition|(
name|first
operator|==
literal|'{'
condition|)
block|{
return|return
name|XContentType
operator|.
name|JSON
return|;
block|}
comment|// Should we throw a failure here? Smile idea is to use it in bytes....
if|if
condition|(
name|length
operator|>
literal|2
operator|&&
name|first
operator|==
name|SmileConstants
operator|.
name|HEADER_BYTE_1
operator|&&
name|content
operator|.
name|charAt
argument_list|(
literal|1
argument_list|)
operator|==
name|SmileConstants
operator|.
name|HEADER_BYTE_2
operator|&&
name|content
operator|.
name|charAt
argument_list|(
literal|2
argument_list|)
operator|==
name|SmileConstants
operator|.
name|HEADER_BYTE_3
condition|)
block|{
return|return
name|XContentType
operator|.
name|SMILE
return|;
block|}
if|if
condition|(
name|length
operator|>
literal|2
operator|&&
name|first
operator|==
literal|'-'
operator|&&
name|content
operator|.
name|charAt
argument_list|(
literal|1
argument_list|)
operator|==
literal|'-'
operator|&&
name|content
operator|.
name|charAt
argument_list|(
literal|2
argument_list|)
operator|==
literal|'-'
condition|)
block|{
return|return
name|XContentType
operator|.
name|YAML
return|;
block|}
comment|// CBOR is not supported
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|length
condition|;
name|i
operator|++
control|)
block|{
name|char
name|c
init|=
name|content
operator|.
name|charAt
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|c
operator|==
literal|'{'
condition|)
block|{
return|return
name|XContentType
operator|.
name|JSON
return|;
block|}
if|if
condition|(
name|Character
operator|.
name|isWhitespace
argument_list|(
name|c
argument_list|)
operator|==
literal|false
condition|)
block|{
break|break;
block|}
block|}
return|return
literal|null
return|;
block|}
comment|/**      * Guesses the content (type) based on the provided char sequence.      */
DECL|method|xContent
specifier|public
specifier|static
name|XContent
name|xContent
parameter_list|(
name|CharSequence
name|content
parameter_list|)
block|{
name|XContentType
name|type
init|=
name|xContentType
argument_list|(
name|content
argument_list|)
decl_stmt|;
if|if
condition|(
name|type
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ElasticsearchParseException
argument_list|(
literal|"Failed to derive xcontent from "
operator|+
name|content
argument_list|)
throw|;
block|}
return|return
name|xContent
argument_list|(
name|type
argument_list|)
return|;
block|}
comment|/**      * Guesses the content type based on the provided bytes.      */
DECL|method|xContent
specifier|public
specifier|static
name|XContent
name|xContent
parameter_list|(
name|byte
index|[]
name|data
parameter_list|)
block|{
return|return
name|xContent
argument_list|(
name|data
argument_list|,
literal|0
argument_list|,
name|data
operator|.
name|length
argument_list|)
return|;
block|}
comment|/**      * Guesses the content type based on the provided bytes.      */
DECL|method|xContent
specifier|public
specifier|static
name|XContent
name|xContent
parameter_list|(
name|byte
index|[]
name|data
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
block|{
name|XContentType
name|type
init|=
name|xContentType
argument_list|(
name|data
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
decl_stmt|;
if|if
condition|(
name|type
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ElasticsearchParseException
argument_list|(
literal|"Failed to derive xcontent from (offset="
operator|+
name|offset
operator|+
literal|", length="
operator|+
name|length
operator|+
literal|"): "
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|data
argument_list|)
argument_list|)
throw|;
block|}
return|return
name|xContent
argument_list|(
name|type
argument_list|)
return|;
block|}
comment|/**      * Guesses the content type based on the provided bytes.      */
DECL|method|xContentType
specifier|public
specifier|static
name|XContentType
name|xContentType
parameter_list|(
name|byte
index|[]
name|data
parameter_list|)
block|{
return|return
name|xContentType
argument_list|(
name|data
argument_list|,
literal|0
argument_list|,
name|data
operator|.
name|length
argument_list|)
return|;
block|}
comment|/**      * Guesses the content type based on the provided input stream without consuming it.      */
DECL|method|xContentType
specifier|public
specifier|static
name|XContentType
name|xContentType
parameter_list|(
name|InputStream
name|si
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|si
operator|.
name|markSupported
argument_list|()
operator|==
literal|false
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Cannot guess the xcontent type without mark/reset support on "
operator|+
name|si
operator|.
name|getClass
argument_list|()
argument_list|)
throw|;
block|}
name|si
operator|.
name|mark
argument_list|(
name|GUESS_HEADER_LENGTH
argument_list|)
expr_stmt|;
try|try
block|{
specifier|final
name|int
name|firstInt
init|=
name|si
operator|.
name|read
argument_list|()
decl_stmt|;
comment|// this must be an int since we need to respect the method contract
if|if
condition|(
name|firstInt
operator|==
operator|-
literal|1
condition|)
block|{
return|return
literal|null
return|;
block|}
specifier|final
name|int
name|secondInt
init|=
name|si
operator|.
name|read
argument_list|()
decl_stmt|;
comment|// this must be an int since we need to respect the method contract
if|if
condition|(
name|secondInt
operator|==
operator|-
literal|1
condition|)
block|{
return|return
literal|null
return|;
block|}
specifier|final
name|byte
name|first
init|=
call|(
name|byte
call|)
argument_list|(
literal|0xff
operator|&
name|firstInt
argument_list|)
decl_stmt|;
specifier|final
name|byte
name|second
init|=
call|(
name|byte
call|)
argument_list|(
literal|0xff
operator|&
name|secondInt
argument_list|)
decl_stmt|;
if|if
condition|(
name|first
operator|==
name|SmileConstants
operator|.
name|HEADER_BYTE_1
operator|&&
name|second
operator|==
name|SmileConstants
operator|.
name|HEADER_BYTE_2
condition|)
block|{
name|int
name|third
init|=
name|si
operator|.
name|read
argument_list|()
decl_stmt|;
if|if
condition|(
name|third
operator|==
name|SmileConstants
operator|.
name|HEADER_BYTE_3
condition|)
block|{
return|return
name|XContentType
operator|.
name|SMILE
return|;
block|}
block|}
if|if
condition|(
name|first
operator|==
literal|'{'
operator|||
name|second
operator|==
literal|'{'
condition|)
block|{
return|return
name|XContentType
operator|.
name|JSON
return|;
block|}
if|if
condition|(
name|first
operator|==
literal|'-'
operator|&&
name|second
operator|==
literal|'-'
condition|)
block|{
name|int
name|third
init|=
name|si
operator|.
name|read
argument_list|()
decl_stmt|;
if|if
condition|(
name|third
operator|==
literal|'-'
condition|)
block|{
return|return
name|XContentType
operator|.
name|YAML
return|;
block|}
block|}
comment|// CBOR logic similar to CBORFactory#hasCBORFormat
if|if
condition|(
name|first
operator|==
name|CBORConstants
operator|.
name|BYTE_OBJECT_INDEFINITE
condition|)
block|{
return|return
name|XContentType
operator|.
name|CBOR
return|;
block|}
if|if
condition|(
name|CBORConstants
operator|.
name|hasMajorType
argument_list|(
name|CBORConstants
operator|.
name|MAJOR_TYPE_TAG
argument_list|,
name|first
argument_list|)
condition|)
block|{
comment|// Actually, specific "self-describe tag" is a very good indicator
name|int
name|third
init|=
name|si
operator|.
name|read
argument_list|()
decl_stmt|;
if|if
condition|(
name|third
operator|==
operator|-
literal|1
condition|)
block|{
return|return
literal|null
return|;
block|}
if|if
condition|(
name|first
operator|==
operator|(
name|byte
operator|)
literal|0xD9
operator|&&
name|second
operator|==
operator|(
name|byte
operator|)
literal|0xD9
operator|&&
name|third
operator|==
operator|(
name|byte
operator|)
literal|0xF7
condition|)
block|{
return|return
name|XContentType
operator|.
name|CBOR
return|;
block|}
block|}
comment|// for small objects, some encoders just encode as major type object, we can safely
comment|// say its CBOR since it doesn't contradict SMILE or JSON, and its a last resort
if|if
condition|(
name|CBORConstants
operator|.
name|hasMajorType
argument_list|(
name|CBORConstants
operator|.
name|MAJOR_TYPE_OBJECT
argument_list|,
name|first
argument_list|)
condition|)
block|{
return|return
name|XContentType
operator|.
name|CBOR
return|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|2
init|;
name|i
operator|<
name|GUESS_HEADER_LENGTH
condition|;
name|i
operator|++
control|)
block|{
name|int
name|val
init|=
name|si
operator|.
name|read
argument_list|()
decl_stmt|;
if|if
condition|(
name|val
operator|==
operator|-
literal|1
condition|)
block|{
return|return
literal|null
return|;
block|}
if|if
condition|(
name|val
operator|==
literal|'{'
condition|)
block|{
return|return
name|XContentType
operator|.
name|JSON
return|;
block|}
if|if
condition|(
name|Character
operator|.
name|isWhitespace
argument_list|(
name|val
argument_list|)
operator|==
literal|false
condition|)
block|{
break|break;
block|}
block|}
return|return
literal|null
return|;
block|}
finally|finally
block|{
name|si
operator|.
name|reset
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**      * Guesses the content type based on the provided bytes.      */
DECL|method|xContentType
specifier|public
specifier|static
name|XContentType
name|xContentType
parameter_list|(
name|byte
index|[]
name|data
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
block|{
return|return
name|xContentType
argument_list|(
operator|new
name|BytesArray
argument_list|(
name|data
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
argument_list|)
return|;
block|}
DECL|method|xContent
specifier|public
specifier|static
name|XContent
name|xContent
parameter_list|(
name|BytesReference
name|bytes
parameter_list|)
block|{
name|XContentType
name|type
init|=
name|xContentType
argument_list|(
name|bytes
argument_list|)
decl_stmt|;
if|if
condition|(
name|type
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ElasticsearchParseException
argument_list|(
literal|"Failed to derive xcontent from "
operator|+
name|bytes
argument_list|)
throw|;
block|}
return|return
name|xContent
argument_list|(
name|type
argument_list|)
return|;
block|}
comment|/**      * Guesses the content type based on the provided bytes.      */
DECL|method|xContentType
specifier|public
specifier|static
name|XContentType
name|xContentType
parameter_list|(
name|BytesReference
name|bytes
parameter_list|)
block|{
name|int
name|length
init|=
name|bytes
operator|.
name|length
argument_list|()
decl_stmt|;
if|if
condition|(
name|length
operator|==
literal|0
condition|)
block|{
return|return
literal|null
return|;
block|}
name|byte
name|first
init|=
name|bytes
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
if|if
condition|(
name|first
operator|==
literal|'{'
condition|)
block|{
return|return
name|XContentType
operator|.
name|JSON
return|;
block|}
if|if
condition|(
name|length
operator|>
literal|2
operator|&&
name|first
operator|==
name|SmileConstants
operator|.
name|HEADER_BYTE_1
operator|&&
name|bytes
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|==
name|SmileConstants
operator|.
name|HEADER_BYTE_2
operator|&&
name|bytes
operator|.
name|get
argument_list|(
literal|2
argument_list|)
operator|==
name|SmileConstants
operator|.
name|HEADER_BYTE_3
condition|)
block|{
return|return
name|XContentType
operator|.
name|SMILE
return|;
block|}
if|if
condition|(
name|length
operator|>
literal|2
operator|&&
name|first
operator|==
literal|'-'
operator|&&
name|bytes
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|==
literal|'-'
operator|&&
name|bytes
operator|.
name|get
argument_list|(
literal|2
argument_list|)
operator|==
literal|'-'
condition|)
block|{
return|return
name|XContentType
operator|.
name|YAML
return|;
block|}
comment|// CBOR logic similar to CBORFactory#hasCBORFormat
if|if
condition|(
name|first
operator|==
name|CBORConstants
operator|.
name|BYTE_OBJECT_INDEFINITE
operator|&&
name|length
operator|>
literal|1
condition|)
block|{
return|return
name|XContentType
operator|.
name|CBOR
return|;
block|}
if|if
condition|(
name|CBORConstants
operator|.
name|hasMajorType
argument_list|(
name|CBORConstants
operator|.
name|MAJOR_TYPE_TAG
argument_list|,
name|first
argument_list|)
operator|&&
name|length
operator|>
literal|2
condition|)
block|{
comment|// Actually, specific "self-describe tag" is a very good indicator
if|if
condition|(
name|first
operator|==
operator|(
name|byte
operator|)
literal|0xD9
operator|&&
name|bytes
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|==
operator|(
name|byte
operator|)
literal|0xD9
operator|&&
name|bytes
operator|.
name|get
argument_list|(
literal|2
argument_list|)
operator|==
operator|(
name|byte
operator|)
literal|0xF7
condition|)
block|{
return|return
name|XContentType
operator|.
name|CBOR
return|;
block|}
block|}
comment|// for small objects, some encoders just encode as major type object, we can safely
comment|// say its CBOR since it doesn't contradict SMILE or JSON, and its a last resort
if|if
condition|(
name|CBORConstants
operator|.
name|hasMajorType
argument_list|(
name|CBORConstants
operator|.
name|MAJOR_TYPE_OBJECT
argument_list|,
name|first
argument_list|)
condition|)
block|{
return|return
name|XContentType
operator|.
name|CBOR
return|;
block|}
comment|// a last chance for JSON
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|length
condition|;
name|i
operator|++
control|)
block|{
name|byte
name|b
init|=
name|bytes
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|b
operator|==
literal|'{'
condition|)
block|{
return|return
name|XContentType
operator|.
name|JSON
return|;
block|}
if|if
condition|(
name|Character
operator|.
name|isWhitespace
argument_list|(
name|b
argument_list|)
operator|==
literal|false
condition|)
block|{
break|break;
block|}
block|}
return|return
literal|null
return|;
block|}
block|}
end_class

end_unit
