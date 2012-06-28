begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Charsets
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
name|Maps
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticSearchParseException
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
name|BytesHolder
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
name|collect
operator|.
name|Tuple
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
name|compress
operator|.
name|CompressedStreamInput
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
name|compress
operator|.
name|Compressor
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
name|compress
operator|.
name|CompressorFactory
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
name|io
operator|.
name|stream
operator|.
name|BytesStreamInput
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
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
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
name|Map
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|class|XContentHelper
specifier|public
class|class
name|XContentHelper
block|{
DECL|method|createParser
specifier|public
specifier|static
name|XContentParser
name|createParser
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
throws|throws
name|IOException
block|{
name|Compressor
name|compressor
init|=
name|CompressorFactory
operator|.
name|compressor
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
name|compressor
operator|!=
literal|null
condition|)
block|{
name|CompressedStreamInput
name|compressedInput
init|=
name|compressor
operator|.
name|streamInput
argument_list|(
operator|new
name|BytesStreamInput
argument_list|(
name|data
argument_list|,
name|offset
argument_list|,
name|length
argument_list|,
literal|false
argument_list|)
argument_list|)
decl_stmt|;
name|XContentType
name|contentType
init|=
name|XContentFactory
operator|.
name|xContentType
argument_list|(
name|compressedInput
argument_list|)
decl_stmt|;
name|compressedInput
operator|.
name|resetToBufferStart
argument_list|()
expr_stmt|;
return|return
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|contentType
argument_list|)
operator|.
name|createParser
argument_list|(
name|compressedInput
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|data
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
operator|.
name|createParser
argument_list|(
name|data
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
return|;
block|}
block|}
DECL|method|convertToMap
specifier|public
specifier|static
name|Tuple
argument_list|<
name|XContentType
argument_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|>
name|convertToMap
parameter_list|(
name|byte
index|[]
name|data
parameter_list|,
name|boolean
name|ordered
parameter_list|)
throws|throws
name|ElasticSearchParseException
block|{
return|return
name|convertToMap
argument_list|(
name|data
argument_list|,
literal|0
argument_list|,
name|data
operator|.
name|length
argument_list|,
name|ordered
argument_list|)
return|;
block|}
DECL|method|convertToMap
specifier|public
specifier|static
name|Tuple
argument_list|<
name|XContentType
argument_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|>
name|convertToMap
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
parameter_list|,
name|boolean
name|ordered
parameter_list|)
throws|throws
name|ElasticSearchParseException
block|{
try|try
block|{
name|XContentParser
name|parser
decl_stmt|;
name|XContentType
name|contentType
decl_stmt|;
name|Compressor
name|compressor
init|=
name|CompressorFactory
operator|.
name|compressor
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
name|compressor
operator|!=
literal|null
condition|)
block|{
name|CompressedStreamInput
name|compressedStreamInput
init|=
name|compressor
operator|.
name|streamInput
argument_list|(
operator|new
name|BytesStreamInput
argument_list|(
name|data
argument_list|,
name|offset
argument_list|,
name|length
argument_list|,
literal|false
argument_list|)
argument_list|)
decl_stmt|;
name|contentType
operator|=
name|XContentFactory
operator|.
name|xContentType
argument_list|(
name|compressedStreamInput
argument_list|)
expr_stmt|;
name|compressedStreamInput
operator|.
name|resetToBufferStart
argument_list|()
expr_stmt|;
name|parser
operator|=
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|contentType
argument_list|)
operator|.
name|createParser
argument_list|(
name|compressedStreamInput
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|contentType
operator|=
name|XContentFactory
operator|.
name|xContentType
argument_list|(
name|data
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
expr_stmt|;
name|parser
operator|=
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|contentType
argument_list|)
operator|.
name|createParser
argument_list|(
name|data
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|ordered
condition|)
block|{
return|return
name|Tuple
operator|.
name|tuple
argument_list|(
name|contentType
argument_list|,
name|parser
operator|.
name|mapOrderedAndClose
argument_list|()
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|Tuple
operator|.
name|tuple
argument_list|(
name|contentType
argument_list|,
name|parser
operator|.
name|mapAndClose
argument_list|()
argument_list|)
return|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticSearchParseException
argument_list|(
literal|"Failed to parse content to map"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
DECL|method|convertToJson
specifier|public
specifier|static
name|String
name|convertToJson
parameter_list|(
name|BytesHolder
name|bytes
parameter_list|,
name|boolean
name|reformatJson
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|convertToJson
argument_list|(
name|bytes
operator|.
name|bytes
argument_list|()
argument_list|,
name|bytes
operator|.
name|offset
argument_list|()
argument_list|,
name|bytes
operator|.
name|length
argument_list|()
argument_list|,
name|reformatJson
argument_list|)
return|;
block|}
DECL|method|convertToJson
specifier|public
specifier|static
name|String
name|convertToJson
parameter_list|(
name|BytesHolder
name|bytes
parameter_list|,
name|boolean
name|reformatJson
parameter_list|,
name|boolean
name|prettyPrint
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|convertToJson
argument_list|(
name|bytes
operator|.
name|bytes
argument_list|()
argument_list|,
name|bytes
operator|.
name|offset
argument_list|()
argument_list|,
name|bytes
operator|.
name|length
argument_list|()
argument_list|,
name|reformatJson
argument_list|,
name|prettyPrint
argument_list|)
return|;
block|}
DECL|method|convertToJson
specifier|public
specifier|static
name|String
name|convertToJson
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
parameter_list|,
name|boolean
name|reformatJson
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|convertToJson
argument_list|(
name|data
argument_list|,
name|offset
argument_list|,
name|length
argument_list|,
name|reformatJson
argument_list|,
literal|false
argument_list|)
return|;
block|}
DECL|method|convertToJson
specifier|public
specifier|static
name|String
name|convertToJson
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
parameter_list|,
name|boolean
name|reformatJson
parameter_list|,
name|boolean
name|prettyPrint
parameter_list|)
throws|throws
name|IOException
block|{
name|XContentType
name|xContentType
init|=
name|XContentFactory
operator|.
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
name|xContentType
operator|==
name|XContentType
operator|.
name|JSON
operator|&&
operator|!
name|reformatJson
condition|)
block|{
return|return
operator|new
name|String
argument_list|(
name|data
argument_list|,
name|offset
argument_list|,
name|length
argument_list|,
name|Charsets
operator|.
name|UTF_8
argument_list|)
return|;
block|}
name|XContentParser
name|parser
init|=
literal|null
decl_stmt|;
try|try
block|{
name|parser
operator|=
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|xContentType
argument_list|)
operator|.
name|createParser
argument_list|(
name|data
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
expr_stmt|;
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
name|XContentBuilder
name|builder
init|=
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
decl_stmt|;
if|if
condition|(
name|prettyPrint
condition|)
block|{
name|builder
operator|.
name|prettyPrint
argument_list|()
expr_stmt|;
block|}
name|builder
operator|.
name|copyCurrentStructure
argument_list|(
name|parser
argument_list|)
expr_stmt|;
return|return
name|builder
operator|.
name|string
argument_list|()
return|;
block|}
finally|finally
block|{
if|if
condition|(
name|parser
operator|!=
literal|null
condition|)
block|{
name|parser
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
comment|/**      * Updates the provided changes into the source. If the key exists in the changes, it overrides the one in source      * unless both are Maps, in which case it recuersively updated it.      */
DECL|method|update
specifier|public
specifier|static
name|void
name|update
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|source
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|changes
parameter_list|)
block|{
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
name|changesEntry
range|:
name|changes
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|source
operator|.
name|containsKey
argument_list|(
name|changesEntry
operator|.
name|getKey
argument_list|()
argument_list|)
condition|)
block|{
comment|// safe to copy, change does not exist in source
name|source
operator|.
name|put
argument_list|(
name|changesEntry
operator|.
name|getKey
argument_list|()
argument_list|,
name|changesEntry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|source
operator|.
name|get
argument_list|(
name|changesEntry
operator|.
name|getKey
argument_list|()
argument_list|)
operator|instanceof
name|Map
operator|&&
name|changesEntry
operator|.
name|getValue
argument_list|()
operator|instanceof
name|Map
condition|)
block|{
comment|// recursive merge maps
name|update
argument_list|(
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|source
operator|.
name|get
argument_list|(
name|changesEntry
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|,
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|changesEntry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// update the field
name|source
operator|.
name|put
argument_list|(
name|changesEntry
operator|.
name|getKey
argument_list|()
argument_list|,
name|changesEntry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
comment|/**      * Merges the defaults provided as the second parameter into the content of the first. Only does recursive merge      * for inner maps.      */
annotation|@
name|SuppressWarnings
argument_list|(
block|{
literal|"unchecked"
block|}
argument_list|)
DECL|method|mergeDefaults
specifier|public
specifier|static
name|void
name|mergeDefaults
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|content
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|defaults
parameter_list|)
block|{
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
name|defaultEntry
range|:
name|defaults
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|content
operator|.
name|containsKey
argument_list|(
name|defaultEntry
operator|.
name|getKey
argument_list|()
argument_list|)
condition|)
block|{
comment|// copy it over, it does not exists in the content
name|content
operator|.
name|put
argument_list|(
name|defaultEntry
operator|.
name|getKey
argument_list|()
argument_list|,
name|defaultEntry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// in the content and in the default, only merge compound ones (maps)
if|if
condition|(
name|content
operator|.
name|get
argument_list|(
name|defaultEntry
operator|.
name|getKey
argument_list|()
argument_list|)
operator|instanceof
name|Map
operator|&&
name|defaultEntry
operator|.
name|getValue
argument_list|()
operator|instanceof
name|Map
condition|)
block|{
name|mergeDefaults
argument_list|(
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|content
operator|.
name|get
argument_list|(
name|defaultEntry
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|,
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|defaultEntry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|content
operator|.
name|get
argument_list|(
name|defaultEntry
operator|.
name|getKey
argument_list|()
argument_list|)
operator|instanceof
name|List
operator|&&
name|defaultEntry
operator|.
name|getValue
argument_list|()
operator|instanceof
name|List
condition|)
block|{
name|List
name|defaultList
init|=
operator|(
name|List
operator|)
name|defaultEntry
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|List
name|contentList
init|=
operator|(
name|List
operator|)
name|content
operator|.
name|get
argument_list|(
name|defaultEntry
operator|.
name|getKey
argument_list|()
argument_list|)
decl_stmt|;
name|List
name|mergedList
init|=
operator|new
name|ArrayList
argument_list|()
decl_stmt|;
if|if
condition|(
name|allListValuesAreMapsOfOne
argument_list|(
name|defaultList
argument_list|)
operator|&&
name|allListValuesAreMapsOfOne
argument_list|(
name|contentList
argument_list|)
condition|)
block|{
comment|// all are in the form of [ {"key1" : {}}, {"key2" : {}} ], merge based on keys
name|Map
argument_list|<
name|String
argument_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|>
name|processed
init|=
name|Maps
operator|.
name|newLinkedHashMap
argument_list|()
decl_stmt|;
for|for
control|(
name|Object
name|o
range|:
name|contentList
control|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|map
init|=
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|o
decl_stmt|;
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
name|map
operator|.
name|entrySet
argument_list|()
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
decl_stmt|;
name|processed
operator|.
name|put
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|map
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|Object
name|o
range|:
name|defaultList
control|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|map
init|=
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|o
decl_stmt|;
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
name|map
operator|.
name|entrySet
argument_list|()
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|processed
operator|.
name|containsKey
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
condition|)
block|{
name|mergeDefaults
argument_list|(
name|processed
operator|.
name|get
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|,
name|map
argument_list|)
expr_stmt|;
block|}
block|}
for|for
control|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|map
range|:
name|processed
operator|.
name|values
argument_list|()
control|)
block|{
name|mergedList
operator|.
name|add
argument_list|(
name|map
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
comment|// if both are lists, simply combine them, first the defaults, then the content
name|mergedList
operator|.
name|addAll
argument_list|(
operator|(
name|Collection
operator|)
name|defaultEntry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
name|mergedList
operator|.
name|addAll
argument_list|(
operator|(
name|Collection
operator|)
name|content
operator|.
name|get
argument_list|(
name|defaultEntry
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|content
operator|.
name|put
argument_list|(
name|defaultEntry
operator|.
name|getKey
argument_list|()
argument_list|,
name|mergedList
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
DECL|method|allListValuesAreMapsOfOne
specifier|private
specifier|static
name|boolean
name|allListValuesAreMapsOfOne
parameter_list|(
name|List
name|list
parameter_list|)
block|{
for|for
control|(
name|Object
name|o
range|:
name|list
control|)
block|{
if|if
condition|(
operator|!
operator|(
name|o
operator|instanceof
name|Map
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
operator|(
operator|(
name|Map
operator|)
name|o
operator|)
operator|.
name|size
argument_list|()
operator|!=
literal|1
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
return|return
literal|true
return|;
block|}
DECL|method|copyCurrentStructure
specifier|public
specifier|static
name|void
name|copyCurrentStructure
parameter_list|(
name|XContentGenerator
name|generator
parameter_list|,
name|XContentParser
name|parser
parameter_list|)
throws|throws
name|IOException
block|{
name|XContentParser
operator|.
name|Token
name|t
init|=
name|parser
operator|.
name|currentToken
argument_list|()
decl_stmt|;
comment|// Let's handle field-name separately first
if|if
condition|(
name|t
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|FIELD_NAME
condition|)
block|{
name|generator
operator|.
name|writeFieldName
argument_list|(
name|parser
operator|.
name|currentName
argument_list|()
argument_list|)
expr_stmt|;
name|t
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
expr_stmt|;
comment|// fall-through to copy the associated value
block|}
switch|switch
condition|(
name|t
condition|)
block|{
case|case
name|START_ARRAY
case|:
name|generator
operator|.
name|writeStartArray
argument_list|()
expr_stmt|;
while|while
condition|(
name|parser
operator|.
name|nextToken
argument_list|()
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|END_ARRAY
condition|)
block|{
name|copyCurrentStructure
argument_list|(
name|generator
argument_list|,
name|parser
argument_list|)
expr_stmt|;
block|}
name|generator
operator|.
name|writeEndArray
argument_list|()
expr_stmt|;
break|break;
case|case
name|START_OBJECT
case|:
name|generator
operator|.
name|writeStartObject
argument_list|()
expr_stmt|;
while|while
condition|(
name|parser
operator|.
name|nextToken
argument_list|()
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|END_OBJECT
condition|)
block|{
name|copyCurrentStructure
argument_list|(
name|generator
argument_list|,
name|parser
argument_list|)
expr_stmt|;
block|}
name|generator
operator|.
name|writeEndObject
argument_list|()
expr_stmt|;
break|break;
default|default:
comment|// others are simple:
name|copyCurrentEvent
argument_list|(
name|generator
argument_list|,
name|parser
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|copyCurrentEvent
specifier|public
specifier|static
name|void
name|copyCurrentEvent
parameter_list|(
name|XContentGenerator
name|generator
parameter_list|,
name|XContentParser
name|parser
parameter_list|)
throws|throws
name|IOException
block|{
switch|switch
condition|(
name|parser
operator|.
name|currentToken
argument_list|()
condition|)
block|{
case|case
name|START_OBJECT
case|:
name|generator
operator|.
name|writeStartObject
argument_list|()
expr_stmt|;
break|break;
case|case
name|END_OBJECT
case|:
name|generator
operator|.
name|writeEndObject
argument_list|()
expr_stmt|;
break|break;
case|case
name|START_ARRAY
case|:
name|generator
operator|.
name|writeStartArray
argument_list|()
expr_stmt|;
break|break;
case|case
name|END_ARRAY
case|:
name|generator
operator|.
name|writeEndArray
argument_list|()
expr_stmt|;
break|break;
case|case
name|FIELD_NAME
case|:
name|generator
operator|.
name|writeFieldName
argument_list|(
name|parser
operator|.
name|currentName
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
name|VALUE_STRING
case|:
if|if
condition|(
name|parser
operator|.
name|hasTextCharacters
argument_list|()
condition|)
block|{
name|generator
operator|.
name|writeString
argument_list|(
name|parser
operator|.
name|textCharacters
argument_list|()
argument_list|,
name|parser
operator|.
name|textOffset
argument_list|()
argument_list|,
name|parser
operator|.
name|textLength
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|generator
operator|.
name|writeString
argument_list|(
name|parser
operator|.
name|text
argument_list|()
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
name|VALUE_NUMBER
case|:
switch|switch
condition|(
name|parser
operator|.
name|numberType
argument_list|()
condition|)
block|{
case|case
name|INT
case|:
name|generator
operator|.
name|writeNumber
argument_list|(
name|parser
operator|.
name|intValue
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
name|LONG
case|:
name|generator
operator|.
name|writeNumber
argument_list|(
name|parser
operator|.
name|longValue
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
name|FLOAT
case|:
name|generator
operator|.
name|writeNumber
argument_list|(
name|parser
operator|.
name|floatValue
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
name|DOUBLE
case|:
name|generator
operator|.
name|writeNumber
argument_list|(
name|parser
operator|.
name|doubleValue
argument_list|()
argument_list|)
expr_stmt|;
break|break;
block|}
break|break;
case|case
name|VALUE_BOOLEAN
case|:
name|generator
operator|.
name|writeBoolean
argument_list|(
name|parser
operator|.
name|booleanValue
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
name|VALUE_NULL
case|:
name|generator
operator|.
name|writeNull
argument_list|()
expr_stmt|;
break|break;
case|case
name|VALUE_EMBEDDED_OBJECT
case|:
name|generator
operator|.
name|writeBinary
argument_list|(
name|parser
operator|.
name|binaryValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

