begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.mapper.attachment
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
operator|.
name|attachment
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tika
operator|.
name|exception
operator|.
name|TikaException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tika
operator|.
name|metadata
operator|.
name|Metadata
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
name|FastByteArrayInputStream
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
name|*
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
name|DateFieldMapper
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

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
operator|.
name|MapperBuilders
operator|.
name|dateField
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
name|mapper
operator|.
name|MapperBuilders
operator|.
name|stringField
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
name|mapper
operator|.
name|core
operator|.
name|TypeParsers
operator|.
name|parsePathType
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|plugin
operator|.
name|mapper
operator|.
name|attachments
operator|.
name|tika
operator|.
name|TikaInstance
operator|.
name|tika
import|;
end_import

begin_comment
comment|/**  *<pre>  *      field1 : "..."  *</pre>  *<p>Or:  *<pre>  * {  *      file1 : {  *          _content_type : "application/pdf",  *          _content_length : "500000000",  *          _name : "..../something.pdf",  *          content : ""  *      }  * }  *</pre>  *  * _content_length = Specify the maximum amount of characters to extract from the attachment. If not specified, then the default for  *                   tika is 100,000 characters. Caution is required when setting large values as this can cause memory issues.  *  */
end_comment

begin_class
DECL|class|AttachmentMapper
specifier|public
class|class
name|AttachmentMapper
implements|implements
name|Mapper
block|{
DECL|field|CONTENT_TYPE
specifier|public
specifier|static
specifier|final
name|String
name|CONTENT_TYPE
init|=
literal|"attachment"
decl_stmt|;
DECL|class|Defaults
specifier|public
specifier|static
class|class
name|Defaults
block|{
DECL|field|PATH_TYPE
specifier|public
specifier|static
specifier|final
name|ContentPath
operator|.
name|Type
name|PATH_TYPE
init|=
name|ContentPath
operator|.
name|Type
operator|.
name|FULL
decl_stmt|;
block|}
DECL|class|Builder
specifier|public
specifier|static
class|class
name|Builder
extends|extends
name|Mapper
operator|.
name|Builder
argument_list|<
name|Builder
argument_list|,
name|AttachmentMapper
argument_list|>
block|{
DECL|field|pathType
specifier|private
name|ContentPath
operator|.
name|Type
name|pathType
init|=
name|Defaults
operator|.
name|PATH_TYPE
decl_stmt|;
DECL|field|contentBuilder
specifier|private
name|StringFieldMapper
operator|.
name|Builder
name|contentBuilder
decl_stmt|;
DECL|field|titleBuilder
specifier|private
name|StringFieldMapper
operator|.
name|Builder
name|titleBuilder
init|=
name|stringField
argument_list|(
literal|"title"
argument_list|)
decl_stmt|;
DECL|field|authorBuilder
specifier|private
name|StringFieldMapper
operator|.
name|Builder
name|authorBuilder
init|=
name|stringField
argument_list|(
literal|"author"
argument_list|)
decl_stmt|;
DECL|field|keywordsBuilder
specifier|private
name|StringFieldMapper
operator|.
name|Builder
name|keywordsBuilder
init|=
name|stringField
argument_list|(
literal|"keywords"
argument_list|)
decl_stmt|;
DECL|field|dateBuilder
specifier|private
name|DateFieldMapper
operator|.
name|Builder
name|dateBuilder
init|=
name|dateField
argument_list|(
literal|"date"
argument_list|)
decl_stmt|;
DECL|field|contentTypeBuilder
specifier|private
name|StringFieldMapper
operator|.
name|Builder
name|contentTypeBuilder
init|=
name|stringField
argument_list|(
literal|"content_type"
argument_list|)
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
argument_list|)
expr_stmt|;
name|this
operator|.
name|builder
operator|=
name|this
expr_stmt|;
name|this
operator|.
name|contentBuilder
operator|=
name|stringField
argument_list|(
name|name
argument_list|)
expr_stmt|;
block|}
DECL|method|pathType
specifier|public
name|Builder
name|pathType
parameter_list|(
name|ContentPath
operator|.
name|Type
name|pathType
parameter_list|)
block|{
name|this
operator|.
name|pathType
operator|=
name|pathType
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|content
specifier|public
name|Builder
name|content
parameter_list|(
name|StringFieldMapper
operator|.
name|Builder
name|content
parameter_list|)
block|{
name|this
operator|.
name|contentBuilder
operator|=
name|content
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|date
specifier|public
name|Builder
name|date
parameter_list|(
name|DateFieldMapper
operator|.
name|Builder
name|date
parameter_list|)
block|{
name|this
operator|.
name|dateBuilder
operator|=
name|date
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|author
specifier|public
name|Builder
name|author
parameter_list|(
name|StringFieldMapper
operator|.
name|Builder
name|author
parameter_list|)
block|{
name|this
operator|.
name|authorBuilder
operator|=
name|author
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|title
specifier|public
name|Builder
name|title
parameter_list|(
name|StringFieldMapper
operator|.
name|Builder
name|title
parameter_list|)
block|{
name|this
operator|.
name|titleBuilder
operator|=
name|title
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|keywords
specifier|public
name|Builder
name|keywords
parameter_list|(
name|StringFieldMapper
operator|.
name|Builder
name|keywords
parameter_list|)
block|{
name|this
operator|.
name|keywordsBuilder
operator|=
name|keywords
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|contentType
specifier|public
name|Builder
name|contentType
parameter_list|(
name|StringFieldMapper
operator|.
name|Builder
name|contentType
parameter_list|)
block|{
name|this
operator|.
name|contentTypeBuilder
operator|=
name|contentType
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
DECL|method|build
specifier|public
name|AttachmentMapper
name|build
parameter_list|(
name|BuilderContext
name|context
parameter_list|)
block|{
name|ContentPath
operator|.
name|Type
name|origPathType
init|=
name|context
operator|.
name|path
argument_list|()
operator|.
name|pathType
argument_list|()
decl_stmt|;
name|context
operator|.
name|path
argument_list|()
operator|.
name|pathType
argument_list|(
name|pathType
argument_list|)
expr_stmt|;
comment|// create the content mapper under the actual name
name|StringFieldMapper
name|contentMapper
init|=
name|contentBuilder
operator|.
name|build
argument_list|(
name|context
argument_list|)
decl_stmt|;
comment|// create the DC one under the name
name|context
operator|.
name|path
argument_list|()
operator|.
name|add
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|DateFieldMapper
name|dateMapper
init|=
name|dateBuilder
operator|.
name|build
argument_list|(
name|context
argument_list|)
decl_stmt|;
name|StringFieldMapper
name|authorMapper
init|=
name|authorBuilder
operator|.
name|build
argument_list|(
name|context
argument_list|)
decl_stmt|;
name|StringFieldMapper
name|titleMapper
init|=
name|titleBuilder
operator|.
name|build
argument_list|(
name|context
argument_list|)
decl_stmt|;
name|StringFieldMapper
name|keywordsMapper
init|=
name|keywordsBuilder
operator|.
name|build
argument_list|(
name|context
argument_list|)
decl_stmt|;
name|StringFieldMapper
name|contentTypeMapper
init|=
name|contentTypeBuilder
operator|.
name|build
argument_list|(
name|context
argument_list|)
decl_stmt|;
name|context
operator|.
name|path
argument_list|()
operator|.
name|remove
argument_list|()
expr_stmt|;
name|context
operator|.
name|path
argument_list|()
operator|.
name|pathType
argument_list|(
name|origPathType
argument_list|)
expr_stmt|;
return|return
operator|new
name|AttachmentMapper
argument_list|(
name|name
argument_list|,
name|pathType
argument_list|,
name|contentMapper
argument_list|,
name|dateMapper
argument_list|,
name|titleMapper
argument_list|,
name|authorMapper
argument_list|,
name|keywordsMapper
argument_list|,
name|contentTypeMapper
argument_list|)
return|;
block|}
block|}
comment|/**      *<pre>      *  field1 : { type : "attachment" }      *</pre>      * Or:      *<pre>      *  field1 : {      *      type : "attachment",      *      fields : {      *          field1 : {type : "binary"},      *          title : {store : "yes"},      *          date : {store : "yes"}      *      }      * }      *</pre>      *      *      */
DECL|class|TypeParser
specifier|public
specifier|static
class|class
name|TypeParser
implements|implements
name|Mapper
operator|.
name|TypeParser
block|{
annotation|@
name|SuppressWarnings
argument_list|(
block|{
literal|"unchecked"
block|}
argument_list|)
annotation|@
name|Override
DECL|method|parse
specifier|public
name|Mapper
operator|.
name|Builder
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
name|AttachmentMapper
operator|.
name|Builder
name|builder
init|=
operator|new
name|AttachmentMapper
operator|.
name|Builder
argument_list|(
name|name
argument_list|)
decl_stmt|;
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
name|entry
range|:
name|node
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|fieldName
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|Object
name|fieldNode
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|fieldName
operator|.
name|equals
argument_list|(
literal|"path"
argument_list|)
condition|)
block|{
name|builder
operator|.
name|pathType
argument_list|(
name|parsePathType
argument_list|(
name|name
argument_list|,
name|fieldNode
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|fieldName
operator|.
name|equals
argument_list|(
literal|"fields"
argument_list|)
condition|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|fieldsNode
init|=
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|fieldNode
decl_stmt|;
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
name|entry1
range|:
name|fieldsNode
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|propName
init|=
name|entry1
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|Object
name|propNode
init|=
name|entry1
operator|.
name|getValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|name
operator|.
name|equals
argument_list|(
name|propName
argument_list|)
condition|)
block|{
comment|// that is the content
name|builder
operator|.
name|content
argument_list|(
operator|(
name|StringFieldMapper
operator|.
name|Builder
operator|)
name|parserContext
operator|.
name|typeParser
argument_list|(
literal|"string"
argument_list|)
operator|.
name|parse
argument_list|(
name|name
argument_list|,
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|propNode
argument_list|,
name|parserContext
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"date"
operator|.
name|equals
argument_list|(
name|propName
argument_list|)
condition|)
block|{
name|builder
operator|.
name|date
argument_list|(
operator|(
name|DateFieldMapper
operator|.
name|Builder
operator|)
name|parserContext
operator|.
name|typeParser
argument_list|(
literal|"date"
argument_list|)
operator|.
name|parse
argument_list|(
literal|"date"
argument_list|,
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|propNode
argument_list|,
name|parserContext
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"title"
operator|.
name|equals
argument_list|(
name|propName
argument_list|)
condition|)
block|{
name|builder
operator|.
name|title
argument_list|(
operator|(
name|StringFieldMapper
operator|.
name|Builder
operator|)
name|parserContext
operator|.
name|typeParser
argument_list|(
literal|"string"
argument_list|)
operator|.
name|parse
argument_list|(
literal|"title"
argument_list|,
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|propNode
argument_list|,
name|parserContext
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"author"
operator|.
name|equals
argument_list|(
name|propName
argument_list|)
condition|)
block|{
name|builder
operator|.
name|author
argument_list|(
operator|(
name|StringFieldMapper
operator|.
name|Builder
operator|)
name|parserContext
operator|.
name|typeParser
argument_list|(
literal|"string"
argument_list|)
operator|.
name|parse
argument_list|(
literal|"author"
argument_list|,
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|propNode
argument_list|,
name|parserContext
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"keywords"
operator|.
name|equals
argument_list|(
name|propName
argument_list|)
condition|)
block|{
name|builder
operator|.
name|keywords
argument_list|(
operator|(
name|StringFieldMapper
operator|.
name|Builder
operator|)
name|parserContext
operator|.
name|typeParser
argument_list|(
literal|"string"
argument_list|)
operator|.
name|parse
argument_list|(
literal|"keywords"
argument_list|,
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|propNode
argument_list|,
name|parserContext
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"content_type"
operator|.
name|equals
argument_list|(
name|propName
argument_list|)
condition|)
block|{
name|builder
operator|.
name|contentType
argument_list|(
operator|(
name|StringFieldMapper
operator|.
name|Builder
operator|)
name|parserContext
operator|.
name|typeParser
argument_list|(
literal|"string"
argument_list|)
operator|.
name|parse
argument_list|(
literal|"content_type"
argument_list|,
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|propNode
argument_list|,
name|parserContext
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
return|return
name|builder
return|;
block|}
block|}
DECL|field|name
specifier|private
specifier|final
name|String
name|name
decl_stmt|;
DECL|field|pathType
specifier|private
specifier|final
name|ContentPath
operator|.
name|Type
name|pathType
decl_stmt|;
DECL|field|contentMapper
specifier|private
specifier|final
name|StringFieldMapper
name|contentMapper
decl_stmt|;
DECL|field|dateMapper
specifier|private
specifier|final
name|DateFieldMapper
name|dateMapper
decl_stmt|;
DECL|field|authorMapper
specifier|private
specifier|final
name|StringFieldMapper
name|authorMapper
decl_stmt|;
DECL|field|titleMapper
specifier|private
specifier|final
name|StringFieldMapper
name|titleMapper
decl_stmt|;
DECL|field|keywordsMapper
specifier|private
specifier|final
name|StringFieldMapper
name|keywordsMapper
decl_stmt|;
DECL|field|contentTypeMapper
specifier|private
specifier|final
name|StringFieldMapper
name|contentTypeMapper
decl_stmt|;
DECL|method|AttachmentMapper
specifier|public
name|AttachmentMapper
parameter_list|(
name|String
name|name
parameter_list|,
name|ContentPath
operator|.
name|Type
name|pathType
parameter_list|,
name|StringFieldMapper
name|contentMapper
parameter_list|,
name|DateFieldMapper
name|dateMapper
parameter_list|,
name|StringFieldMapper
name|titleMapper
parameter_list|,
name|StringFieldMapper
name|authorMapper
parameter_list|,
name|StringFieldMapper
name|keywordsMapper
parameter_list|,
name|StringFieldMapper
name|contentTypeMapper
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
name|this
operator|.
name|pathType
operator|=
name|pathType
expr_stmt|;
name|this
operator|.
name|contentMapper
operator|=
name|contentMapper
expr_stmt|;
name|this
operator|.
name|dateMapper
operator|=
name|dateMapper
expr_stmt|;
name|this
operator|.
name|titleMapper
operator|=
name|titleMapper
expr_stmt|;
name|this
operator|.
name|authorMapper
operator|=
name|authorMapper
expr_stmt|;
name|this
operator|.
name|keywordsMapper
operator|=
name|keywordsMapper
expr_stmt|;
name|this
operator|.
name|contentTypeMapper
operator|=
name|contentTypeMapper
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|name
specifier|public
name|String
name|name
parameter_list|()
block|{
return|return
name|name
return|;
block|}
annotation|@
name|Override
DECL|method|parse
specifier|public
name|void
name|parse
parameter_list|(
name|ParseContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|byte
index|[]
name|content
init|=
literal|null
decl_stmt|;
name|String
name|contentType
init|=
literal|null
decl_stmt|;
name|int
name|contentLength
init|=
literal|100000
decl_stmt|;
name|String
name|name
init|=
literal|null
decl_stmt|;
name|XContentParser
name|parser
init|=
name|context
operator|.
name|parser
argument_list|()
decl_stmt|;
name|XContentParser
operator|.
name|Token
name|token
init|=
name|parser
operator|.
name|currentToken
argument_list|()
decl_stmt|;
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|VALUE_STRING
condition|)
block|{
name|content
operator|=
name|parser
operator|.
name|binaryValue
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|String
name|currentFieldName
init|=
literal|null
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
name|VALUE_STRING
condition|)
block|{
if|if
condition|(
literal|"content"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|content
operator|=
name|parser
operator|.
name|binaryValue
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"_content_type"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|contentType
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
literal|"_name"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|name
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
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
name|VALUE_NUMBER
condition|)
block|{
if|if
condition|(
literal|"_content_length"
operator|.
name|equals
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|contentLength
operator|=
name|parser
operator|.
name|intValue
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
name|Metadata
name|metadata
init|=
operator|new
name|Metadata
argument_list|()
decl_stmt|;
if|if
condition|(
name|contentType
operator|!=
literal|null
condition|)
block|{
name|metadata
operator|.
name|add
argument_list|(
name|Metadata
operator|.
name|CONTENT_TYPE
argument_list|,
name|contentType
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|name
operator|!=
literal|null
condition|)
block|{
name|metadata
operator|.
name|add
argument_list|(
name|Metadata
operator|.
name|RESOURCE_NAME_KEY
argument_list|,
name|name
argument_list|)
expr_stmt|;
block|}
name|String
name|parsedContent
decl_stmt|;
try|try
block|{
comment|// Set the maximum length of strings returned by the parseToString method, -1 sets no limit
name|parsedContent
operator|=
name|tika
argument_list|()
operator|.
name|parseToString
argument_list|(
operator|new
name|FastByteArrayInputStream
argument_list|(
name|content
argument_list|)
argument_list|,
name|metadata
argument_list|,
name|contentLength
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|TikaException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|MapperParsingException
argument_list|(
literal|"Failed to extract ["
operator|+
name|contentLength
operator|+
literal|"] characters of text for ["
operator|+
name|name
operator|+
literal|"]"
argument_list|,
name|e
argument_list|)
throw|;
block|}
name|context
operator|.
name|externalValue
argument_list|(
name|parsedContent
argument_list|)
expr_stmt|;
name|contentMapper
operator|.
name|parse
argument_list|(
name|context
argument_list|)
expr_stmt|;
name|context
operator|.
name|externalValue
argument_list|(
name|metadata
operator|.
name|get
argument_list|(
name|Metadata
operator|.
name|DATE
argument_list|)
argument_list|)
expr_stmt|;
name|dateMapper
operator|.
name|parse
argument_list|(
name|context
argument_list|)
expr_stmt|;
name|context
operator|.
name|externalValue
argument_list|(
name|metadata
operator|.
name|get
argument_list|(
name|Metadata
operator|.
name|TITLE
argument_list|)
argument_list|)
expr_stmt|;
name|titleMapper
operator|.
name|parse
argument_list|(
name|context
argument_list|)
expr_stmt|;
name|context
operator|.
name|externalValue
argument_list|(
name|metadata
operator|.
name|get
argument_list|(
name|Metadata
operator|.
name|AUTHOR
argument_list|)
argument_list|)
expr_stmt|;
name|authorMapper
operator|.
name|parse
argument_list|(
name|context
argument_list|)
expr_stmt|;
name|context
operator|.
name|externalValue
argument_list|(
name|metadata
operator|.
name|get
argument_list|(
name|Metadata
operator|.
name|KEYWORDS
argument_list|)
argument_list|)
expr_stmt|;
name|keywordsMapper
operator|.
name|parse
argument_list|(
name|context
argument_list|)
expr_stmt|;
name|context
operator|.
name|externalValue
argument_list|(
name|metadata
operator|.
name|get
argument_list|(
name|Metadata
operator|.
name|CONTENT_TYPE
argument_list|)
argument_list|)
expr_stmt|;
name|contentTypeMapper
operator|.
name|parse
argument_list|(
name|context
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|merge
specifier|public
name|void
name|merge
parameter_list|(
name|Mapper
name|mergeWith
parameter_list|,
name|MergeContext
name|mergeContext
parameter_list|)
throws|throws
name|MergeMappingException
block|{
comment|// ignore this for now
block|}
annotation|@
name|Override
DECL|method|traverse
specifier|public
name|void
name|traverse
parameter_list|(
name|FieldMapperListener
name|fieldMapperListener
parameter_list|)
block|{
name|contentMapper
operator|.
name|traverse
argument_list|(
name|fieldMapperListener
argument_list|)
expr_stmt|;
name|dateMapper
operator|.
name|traverse
argument_list|(
name|fieldMapperListener
argument_list|)
expr_stmt|;
name|titleMapper
operator|.
name|traverse
argument_list|(
name|fieldMapperListener
argument_list|)
expr_stmt|;
name|authorMapper
operator|.
name|traverse
argument_list|(
name|fieldMapperListener
argument_list|)
expr_stmt|;
name|keywordsMapper
operator|.
name|traverse
argument_list|(
name|fieldMapperListener
argument_list|)
expr_stmt|;
name|contentTypeMapper
operator|.
name|traverse
argument_list|(
name|fieldMapperListener
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|traverse
specifier|public
name|void
name|traverse
parameter_list|(
name|ObjectMapperListener
name|objectMapperListener
parameter_list|)
block|{     }
annotation|@
name|Override
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
block|{
name|contentMapper
operator|.
name|close
argument_list|()
expr_stmt|;
name|dateMapper
operator|.
name|close
argument_list|()
expr_stmt|;
name|titleMapper
operator|.
name|close
argument_list|()
expr_stmt|;
name|authorMapper
operator|.
name|close
argument_list|()
expr_stmt|;
name|keywordsMapper
operator|.
name|close
argument_list|()
expr_stmt|;
name|contentTypeMapper
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|toXContent
specifier|public
name|XContentBuilder
name|toXContent
parameter_list|(
name|XContentBuilder
name|builder
parameter_list|,
name|Params
name|params
parameter_list|)
throws|throws
name|IOException
block|{
name|builder
operator|.
name|startObject
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
name|CONTENT_TYPE
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"path"
argument_list|,
name|pathType
operator|.
name|name
argument_list|()
operator|.
name|toLowerCase
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|startObject
argument_list|(
literal|"fields"
argument_list|)
expr_stmt|;
name|contentMapper
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
name|authorMapper
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
name|titleMapper
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
name|dateMapper
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
name|keywordsMapper
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
name|contentTypeMapper
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
return|return
name|builder
return|;
block|}
block|}
end_class

end_unit

