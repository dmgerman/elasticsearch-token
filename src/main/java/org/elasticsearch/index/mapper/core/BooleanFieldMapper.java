begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.mapper.core
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
operator|.
name|core
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
name|search
operator|.
name|Filter
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
name|common
operator|.
name|Booleans
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
name|Nullable
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
name|lucene
operator|.
name|Lucene
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
name|lucene
operator|.
name|search
operator|.
name|TermFilter
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
name|codec
operator|.
name|postingsformat
operator|.
name|PostingsFormatProvider
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
name|FieldDataType
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
name|similarity
operator|.
name|SimilarityProvider
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
name|common
operator|.
name|xcontent
operator|.
name|support
operator|.
name|XContentMapValues
operator|.
name|nodeBooleanValue
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
name|booleanField
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
name|parseField
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_comment
comment|// TODO this can be made better, maybe storing a byte for it?
end_comment

begin_class
DECL|class|BooleanFieldMapper
specifier|public
class|class
name|BooleanFieldMapper
extends|extends
name|AbstractFieldMapper
argument_list|<
name|Boolean
argument_list|>
block|{
DECL|field|CONTENT_TYPE
specifier|public
specifier|static
specifier|final
name|String
name|CONTENT_TYPE
init|=
literal|"boolean"
decl_stmt|;
DECL|class|Defaults
specifier|public
specifier|static
class|class
name|Defaults
extends|extends
name|AbstractFieldMapper
operator|.
name|Defaults
block|{
DECL|field|FIELD_TYPE
specifier|public
specifier|static
specifier|final
name|FieldType
name|FIELD_TYPE
init|=
operator|new
name|FieldType
argument_list|(
name|AbstractFieldMapper
operator|.
name|Defaults
operator|.
name|FIELD_TYPE
argument_list|)
decl_stmt|;
static|static
block|{
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
DECL|field|NULL_VALUE
specifier|public
specifier|static
specifier|final
name|Boolean
name|NULL_VALUE
init|=
literal|null
decl_stmt|;
block|}
DECL|class|Values
specifier|public
specifier|static
class|class
name|Values
block|{
DECL|field|TRUE
specifier|public
specifier|final
specifier|static
name|BytesRef
name|TRUE
init|=
operator|new
name|BytesRef
argument_list|(
literal|"T"
argument_list|)
decl_stmt|;
DECL|field|FALSE
specifier|public
specifier|final
specifier|static
name|BytesRef
name|FALSE
init|=
operator|new
name|BytesRef
argument_list|(
literal|"F"
argument_list|)
decl_stmt|;
block|}
DECL|class|Builder
specifier|public
specifier|static
class|class
name|Builder
extends|extends
name|AbstractFieldMapper
operator|.
name|Builder
argument_list|<
name|Builder
argument_list|,
name|BooleanFieldMapper
argument_list|>
block|{
DECL|field|nullValue
specifier|private
name|Boolean
name|nullValue
init|=
name|Defaults
operator|.
name|NULL_VALUE
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
operator|new
name|FieldType
argument_list|(
name|Defaults
operator|.
name|FIELD_TYPE
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|builder
operator|=
name|this
expr_stmt|;
block|}
DECL|method|nullValue
specifier|public
name|Builder
name|nullValue
parameter_list|(
name|boolean
name|nullValue
parameter_list|)
block|{
name|this
operator|.
name|nullValue
operator|=
name|nullValue
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
DECL|method|index
specifier|public
name|Builder
name|index
parameter_list|(
name|boolean
name|index
parameter_list|)
block|{
return|return
name|super
operator|.
name|index
argument_list|(
name|index
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|store
specifier|public
name|Builder
name|store
parameter_list|(
name|boolean
name|store
parameter_list|)
block|{
return|return
name|super
operator|.
name|store
argument_list|(
name|store
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|storeTermVectors
specifier|protected
name|Builder
name|storeTermVectors
parameter_list|(
name|boolean
name|termVectors
parameter_list|)
block|{
return|return
name|super
operator|.
name|storeTermVectors
argument_list|(
name|termVectors
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|storeTermVectorOffsets
specifier|protected
name|Builder
name|storeTermVectorOffsets
parameter_list|(
name|boolean
name|termVectorOffsets
parameter_list|)
block|{
return|return
name|super
operator|.
name|storeTermVectorOffsets
argument_list|(
name|termVectorOffsets
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|storeTermVectorPositions
specifier|protected
name|Builder
name|storeTermVectorPositions
parameter_list|(
name|boolean
name|termVectorPositions
parameter_list|)
block|{
return|return
name|super
operator|.
name|storeTermVectorPositions
argument_list|(
name|termVectorPositions
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|storeTermVectorPayloads
specifier|protected
name|Builder
name|storeTermVectorPayloads
parameter_list|(
name|boolean
name|termVectorPayloads
parameter_list|)
block|{
return|return
name|super
operator|.
name|storeTermVectorPayloads
argument_list|(
name|termVectorPayloads
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|boost
specifier|public
name|Builder
name|boost
parameter_list|(
name|float
name|boost
parameter_list|)
block|{
return|return
name|super
operator|.
name|boost
argument_list|(
name|boost
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|indexName
specifier|public
name|Builder
name|indexName
parameter_list|(
name|String
name|indexName
parameter_list|)
block|{
return|return
name|super
operator|.
name|indexName
argument_list|(
name|indexName
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|similarity
specifier|public
name|Builder
name|similarity
parameter_list|(
name|SimilarityProvider
name|similarity
parameter_list|)
block|{
return|return
name|super
operator|.
name|similarity
argument_list|(
name|similarity
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|build
specifier|public
name|BooleanFieldMapper
name|build
parameter_list|(
name|BuilderContext
name|context
parameter_list|)
block|{
return|return
operator|new
name|BooleanFieldMapper
argument_list|(
name|buildNames
argument_list|(
name|context
argument_list|)
argument_list|,
name|boost
argument_list|,
name|fieldType
argument_list|,
name|nullValue
argument_list|,
name|provider
argument_list|,
name|similarity
argument_list|,
name|fieldDataSettings
argument_list|)
return|;
block|}
block|}
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
name|BooleanFieldMapper
operator|.
name|Builder
name|builder
init|=
name|booleanField
argument_list|(
name|name
argument_list|)
decl_stmt|;
name|parseField
argument_list|(
name|builder
argument_list|,
name|name
argument_list|,
name|node
argument_list|,
name|parserContext
argument_list|)
expr_stmt|;
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
literal|"null_value"
argument_list|)
condition|)
block|{
name|builder
operator|.
name|nullValue
argument_list|(
name|nodeBooleanValue
argument_list|(
name|propNode
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|builder
return|;
block|}
block|}
DECL|field|nullValue
specifier|private
name|Boolean
name|nullValue
decl_stmt|;
DECL|method|BooleanFieldMapper
specifier|protected
name|BooleanFieldMapper
parameter_list|(
name|Names
name|names
parameter_list|,
name|float
name|boost
parameter_list|,
name|FieldType
name|fieldType
parameter_list|,
name|Boolean
name|nullValue
parameter_list|,
name|PostingsFormatProvider
name|provider
parameter_list|,
name|SimilarityProvider
name|similarity
parameter_list|,
annotation|@
name|Nullable
name|Settings
name|fieldDataSettings
parameter_list|)
block|{
name|super
argument_list|(
name|names
argument_list|,
name|boost
argument_list|,
name|fieldType
argument_list|,
name|Lucene
operator|.
name|KEYWORD_ANALYZER
argument_list|,
name|Lucene
operator|.
name|KEYWORD_ANALYZER
argument_list|,
name|provider
argument_list|,
name|similarity
argument_list|,
name|fieldDataSettings
argument_list|)
expr_stmt|;
name|this
operator|.
name|nullValue
operator|=
name|nullValue
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|defaultFieldType
specifier|public
name|FieldType
name|defaultFieldType
parameter_list|()
block|{
return|return
name|Defaults
operator|.
name|FIELD_TYPE
return|;
block|}
annotation|@
name|Override
DECL|method|defaultFieldDataType
specifier|public
name|FieldDataType
name|defaultFieldDataType
parameter_list|()
block|{
comment|// TODO have a special boolean type?
return|return
operator|new
name|FieldDataType
argument_list|(
literal|"string"
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|useTermQueryWithQueryString
specifier|public
name|boolean
name|useTermQueryWithQueryString
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
DECL|method|value
specifier|public
name|Boolean
name|value
parameter_list|(
name|Object
name|value
parameter_list|)
block|{
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
return|return
name|Boolean
operator|.
name|FALSE
return|;
block|}
name|String
name|sValue
init|=
name|value
operator|.
name|toString
argument_list|()
decl_stmt|;
if|if
condition|(
name|sValue
operator|.
name|length
argument_list|()
operator|==
literal|0
condition|)
block|{
return|return
name|Boolean
operator|.
name|FALSE
return|;
block|}
if|if
condition|(
name|sValue
operator|.
name|length
argument_list|()
operator|==
literal|1
operator|&&
name|sValue
operator|.
name|charAt
argument_list|(
literal|0
argument_list|)
operator|==
literal|'F'
condition|)
block|{
return|return
name|Boolean
operator|.
name|FALSE
return|;
block|}
if|if
condition|(
name|Booleans
operator|.
name|parseBoolean
argument_list|(
name|sValue
argument_list|,
literal|false
argument_list|)
condition|)
block|{
return|return
name|Boolean
operator|.
name|TRUE
return|;
block|}
return|return
name|Boolean
operator|.
name|FALSE
return|;
block|}
annotation|@
name|Override
DECL|method|valueForSearch
specifier|public
name|Object
name|valueForSearch
parameter_list|(
name|Object
name|value
parameter_list|)
block|{
return|return
name|value
argument_list|(
name|value
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|indexedValueForSearch
specifier|public
name|BytesRef
name|indexedValueForSearch
parameter_list|(
name|Object
name|value
parameter_list|)
block|{
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
return|return
name|Values
operator|.
name|FALSE
return|;
block|}
if|if
condition|(
name|value
operator|instanceof
name|Boolean
condition|)
block|{
return|return
operator|(
operator|(
name|Boolean
operator|)
name|value
operator|)
condition|?
name|Values
operator|.
name|TRUE
else|:
name|Values
operator|.
name|FALSE
return|;
block|}
name|String
name|sValue
decl_stmt|;
if|if
condition|(
name|value
operator|instanceof
name|BytesRef
condition|)
block|{
name|sValue
operator|=
operator|(
operator|(
name|BytesRef
operator|)
name|value
operator|)
operator|.
name|utf8ToString
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|sValue
operator|=
name|value
operator|.
name|toString
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|sValue
operator|.
name|length
argument_list|()
operator|==
literal|0
condition|)
block|{
return|return
name|Values
operator|.
name|FALSE
return|;
block|}
if|if
condition|(
name|sValue
operator|.
name|length
argument_list|()
operator|==
literal|1
operator|&&
name|sValue
operator|.
name|charAt
argument_list|(
literal|0
argument_list|)
operator|==
literal|'F'
condition|)
block|{
return|return
name|Values
operator|.
name|FALSE
return|;
block|}
if|if
condition|(
name|Booleans
operator|.
name|parseBoolean
argument_list|(
name|sValue
argument_list|,
literal|false
argument_list|)
condition|)
block|{
return|return
name|Values
operator|.
name|TRUE
return|;
block|}
return|return
name|Values
operator|.
name|FALSE
return|;
block|}
annotation|@
name|Override
DECL|method|nullValueFilter
specifier|public
name|Filter
name|nullValueFilter
parameter_list|()
block|{
if|if
condition|(
name|nullValue
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
operator|new
name|TermFilter
argument_list|(
name|names
argument_list|()
operator|.
name|createIndexNameTerm
argument_list|(
name|nullValue
condition|?
name|Values
operator|.
name|TRUE
else|:
name|Values
operator|.
name|FALSE
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|parseCreateField
specifier|protected
name|Field
name|parseCreateField
parameter_list|(
name|ParseContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|fieldType
argument_list|()
operator|.
name|indexed
argument_list|()
operator|&&
operator|!
name|fieldType
argument_list|()
operator|.
name|stored
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
name|XContentParser
operator|.
name|Token
name|token
init|=
name|context
operator|.
name|parser
argument_list|()
operator|.
name|currentToken
argument_list|()
decl_stmt|;
name|String
name|value
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|VALUE_NULL
condition|)
block|{
if|if
condition|(
name|nullValue
operator|!=
literal|null
condition|)
block|{
name|value
operator|=
name|nullValue
condition|?
literal|"T"
else|:
literal|"F"
expr_stmt|;
block|}
block|}
else|else
block|{
name|value
operator|=
name|context
operator|.
name|parser
argument_list|()
operator|.
name|booleanValue
argument_list|()
condition|?
literal|"T"
else|:
literal|"F"
expr_stmt|;
block|}
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
operator|new
name|Field
argument_list|(
name|names
operator|.
name|indexName
argument_list|()
argument_list|,
name|value
argument_list|,
name|fieldType
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|contentType
specifier|protected
name|String
name|contentType
parameter_list|()
block|{
return|return
name|CONTENT_TYPE
return|;
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
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|doXContentBody
argument_list|(
name|builder
argument_list|)
expr_stmt|;
if|if
condition|(
name|nullValue
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"null_value"
argument_list|,
name|nullValue
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

