begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|SortedSetDocValuesField
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
name|DocValuesIndexFieldData
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
name|FieldMapper
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
name|StringFieldType
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
name|internal
operator|.
name|AllFieldMapper
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
comment|/**  * A field mapper for keywords. This mapper accepts strings and indexes them as-is.  */
end_comment

begin_class
DECL|class|KeywordFieldMapper
specifier|public
specifier|final
class|class
name|KeywordFieldMapper
extends|extends
name|FieldMapper
implements|implements
name|AllFieldMapper
operator|.
name|IncludeInAll
block|{
DECL|field|CONTENT_TYPE
specifier|public
specifier|static
specifier|final
name|String
name|CONTENT_TYPE
init|=
literal|"keyword"
decl_stmt|;
DECL|class|Defaults
specifier|public
specifier|static
class|class
name|Defaults
block|{
DECL|field|FIELD_TYPE
specifier|public
specifier|static
specifier|final
name|MappedFieldType
name|FIELD_TYPE
init|=
operator|new
name|KeywordFieldType
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
name|setOmitNorms
argument_list|(
literal|true
argument_list|)
expr_stmt|;
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
name|freeze
argument_list|()
expr_stmt|;
block|}
DECL|field|NULL_VALUE
specifier|public
specifier|static
specifier|final
name|String
name|NULL_VALUE
init|=
literal|null
decl_stmt|;
DECL|field|IGNORE_ABOVE
specifier|public
specifier|static
specifier|final
name|int
name|IGNORE_ABOVE
init|=
name|Integer
operator|.
name|MAX_VALUE
decl_stmt|;
block|}
DECL|class|Builder
specifier|public
specifier|static
class|class
name|Builder
extends|extends
name|FieldMapper
operator|.
name|Builder
argument_list|<
name|Builder
argument_list|,
name|KeywordFieldMapper
argument_list|>
block|{
DECL|field|nullValue
specifier|protected
name|String
name|nullValue
init|=
name|Defaults
operator|.
name|NULL_VALUE
decl_stmt|;
DECL|field|ignoreAbove
specifier|protected
name|int
name|ignoreAbove
init|=
name|Defaults
operator|.
name|IGNORE_ABOVE
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
DECL|method|ignoreAbove
specifier|public
name|Builder
name|ignoreAbove
parameter_list|(
name|int
name|ignoreAbove
parameter_list|)
block|{
if|if
condition|(
name|ignoreAbove
operator|<
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"[ignore_above] must be positive, got "
operator|+
name|ignoreAbove
argument_list|)
throw|;
block|}
name|this
operator|.
name|ignoreAbove
operator|=
name|ignoreAbove
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
DECL|method|indexOptions
specifier|public
name|Builder
name|indexOptions
parameter_list|(
name|IndexOptions
name|indexOptions
parameter_list|)
block|{
if|if
condition|(
name|indexOptions
operator|.
name|compareTo
argument_list|(
name|IndexOptions
operator|.
name|DOCS_AND_FREQS
argument_list|)
operator|>
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"The [keyword] field does not support positions, got [index_options]="
operator|+
name|indexOptionToString
argument_list|(
name|indexOptions
argument_list|)
argument_list|)
throw|;
block|}
return|return
name|super
operator|.
name|indexOptions
argument_list|(
name|indexOptions
argument_list|)
return|;
block|}
DECL|method|eagerGlobalOrdinals
specifier|public
name|Builder
name|eagerGlobalOrdinals
parameter_list|(
name|boolean
name|eagerGlobalOrdinals
parameter_list|)
block|{
name|fieldType
argument_list|()
operator|.
name|setEagerGlobalOrdinals
argument_list|(
name|eagerGlobalOrdinals
argument_list|)
expr_stmt|;
return|return
name|builder
return|;
block|}
annotation|@
name|Override
DECL|method|build
specifier|public
name|KeywordFieldMapper
name|build
parameter_list|(
name|BuilderContext
name|context
parameter_list|)
block|{
name|setupFieldType
argument_list|(
name|context
argument_list|)
expr_stmt|;
name|KeywordFieldMapper
name|fieldMapper
init|=
operator|new
name|KeywordFieldMapper
argument_list|(
name|name
argument_list|,
name|fieldType
argument_list|,
name|defaultFieldType
argument_list|,
name|ignoreAbove
argument_list|,
name|context
operator|.
name|indexSettings
argument_list|()
argument_list|,
name|multiFieldsBuilder
operator|.
name|build
argument_list|(
name|this
argument_list|,
name|context
argument_list|)
argument_list|,
name|copyTo
argument_list|)
decl_stmt|;
return|return
name|fieldMapper
operator|.
name|includeInAll
argument_list|(
name|includeInAll
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
name|KeywordFieldMapper
operator|.
name|Builder
name|builder
init|=
operator|new
name|KeywordFieldMapper
operator|.
name|Builder
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
name|entry
operator|.
name|getKey
argument_list|()
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
if|if
condition|(
name|propNode
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|MapperParsingException
argument_list|(
literal|"Property [null_value] cannot be null."
argument_list|)
throw|;
block|}
name|builder
operator|.
name|nullValue
argument_list|(
name|propNode
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|iterator
operator|.
name|remove
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|propName
operator|.
name|equals
argument_list|(
literal|"ignore_above"
argument_list|)
condition|)
block|{
name|builder
operator|.
name|ignoreAbove
argument_list|(
name|XContentMapValues
operator|.
name|nodeIntegerValue
argument_list|(
name|propNode
argument_list|,
operator|-
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|iterator
operator|.
name|remove
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|propName
operator|.
name|equals
argument_list|(
literal|"norms"
argument_list|)
condition|)
block|{
name|builder
operator|.
name|omitNorms
argument_list|(
name|XContentMapValues
operator|.
name|nodeBooleanValue
argument_list|(
name|propNode
argument_list|)
operator|==
literal|false
argument_list|)
expr_stmt|;
name|iterator
operator|.
name|remove
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|propName
operator|.
name|equals
argument_list|(
literal|"eager_global_ordinals"
argument_list|)
condition|)
block|{
name|builder
operator|.
name|eagerGlobalOrdinals
argument_list|(
name|XContentMapValues
operator|.
name|nodeBooleanValue
argument_list|(
name|propNode
argument_list|)
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
block|}
DECL|class|KeywordFieldType
specifier|public
specifier|static
specifier|final
class|class
name|KeywordFieldType
extends|extends
name|StringFieldType
block|{
DECL|method|KeywordFieldType
specifier|public
name|KeywordFieldType
parameter_list|()
block|{}
DECL|method|KeywordFieldType
specifier|protected
name|KeywordFieldType
parameter_list|(
name|KeywordFieldType
name|ref
parameter_list|)
block|{
name|super
argument_list|(
name|ref
argument_list|)
expr_stmt|;
block|}
DECL|method|clone
specifier|public
name|KeywordFieldType
name|clone
parameter_list|()
block|{
return|return
operator|new
name|KeywordFieldType
argument_list|(
name|this
argument_list|)
return|;
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
DECL|method|nullValueQuery
specifier|public
name|Query
name|nullValueQuery
parameter_list|()
block|{
if|if
condition|(
name|nullValue
argument_list|()
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|termQuery
argument_list|(
name|nullValue
argument_list|()
argument_list|,
literal|null
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
name|DocValuesIndexFieldData
operator|.
name|Builder
argument_list|()
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
comment|// keywords are internally stored as utf8 bytes
name|BytesRef
name|binaryValue
init|=
operator|(
name|BytesRef
operator|)
name|value
decl_stmt|;
return|return
name|binaryValue
operator|.
name|utf8ToString
argument_list|()
return|;
block|}
block|}
DECL|field|includeInAll
specifier|private
name|Boolean
name|includeInAll
decl_stmt|;
DECL|field|ignoreAbove
specifier|private
name|int
name|ignoreAbove
decl_stmt|;
DECL|method|KeywordFieldMapper
specifier|protected
name|KeywordFieldMapper
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
name|int
name|ignoreAbove
parameter_list|,
name|Settings
name|indexSettings
parameter_list|,
name|MultiFields
name|multiFields
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
name|multiFields
argument_list|,
name|copyTo
argument_list|)
expr_stmt|;
assert|assert
name|fieldType
operator|.
name|indexOptions
argument_list|()
operator|.
name|compareTo
argument_list|(
name|IndexOptions
operator|.
name|DOCS_AND_FREQS
argument_list|)
operator|<=
literal|0
assert|;
name|this
operator|.
name|ignoreAbove
operator|=
name|ignoreAbove
expr_stmt|;
block|}
comment|/** Values that have more chars than the return value of this method will      *  be skipped at parsing time. */
comment|// pkg-private for testing
DECL|method|ignoreAbove
name|int
name|ignoreAbove
parameter_list|()
block|{
return|return
name|ignoreAbove
return|;
block|}
annotation|@
name|Override
DECL|method|clone
specifier|protected
name|KeywordFieldMapper
name|clone
parameter_list|()
block|{
return|return
operator|(
name|KeywordFieldMapper
operator|)
name|super
operator|.
name|clone
argument_list|()
return|;
block|}
comment|// pkg-private for testing
DECL|method|includeInAll
name|Boolean
name|includeInAll
parameter_list|()
block|{
return|return
name|includeInAll
return|;
block|}
annotation|@
name|Override
DECL|method|includeInAll
specifier|public
name|KeywordFieldMapper
name|includeInAll
parameter_list|(
name|Boolean
name|includeInAll
parameter_list|)
block|{
if|if
condition|(
name|includeInAll
operator|!=
literal|null
condition|)
block|{
name|KeywordFieldMapper
name|clone
init|=
name|clone
argument_list|()
decl_stmt|;
name|clone
operator|.
name|includeInAll
operator|=
name|includeInAll
expr_stmt|;
return|return
name|clone
return|;
block|}
else|else
block|{
return|return
name|this
return|;
block|}
block|}
annotation|@
name|Override
DECL|method|includeInAllIfNotSet
specifier|public
name|KeywordFieldMapper
name|includeInAllIfNotSet
parameter_list|(
name|Boolean
name|includeInAll
parameter_list|)
block|{
if|if
condition|(
name|includeInAll
operator|!=
literal|null
operator|&&
name|this
operator|.
name|includeInAll
operator|==
literal|null
condition|)
block|{
name|KeywordFieldMapper
name|clone
init|=
name|clone
argument_list|()
decl_stmt|;
name|clone
operator|.
name|includeInAll
operator|=
name|includeInAll
expr_stmt|;
return|return
name|clone
return|;
block|}
else|else
block|{
return|return
name|this
return|;
block|}
block|}
annotation|@
name|Override
DECL|method|unsetIncludeInAll
specifier|public
name|KeywordFieldMapper
name|unsetIncludeInAll
parameter_list|()
block|{
if|if
condition|(
name|includeInAll
operator|!=
literal|null
condition|)
block|{
name|KeywordFieldMapper
name|clone
init|=
name|clone
argument_list|()
decl_stmt|;
name|clone
operator|.
name|includeInAll
operator|=
literal|null
expr_stmt|;
return|return
name|clone
return|;
block|}
else|else
block|{
return|return
name|this
return|;
block|}
block|}
annotation|@
name|Override
DECL|method|parseCreateField
specifier|protected
name|void
name|parseCreateField
parameter_list|(
name|ParseContext
name|context
parameter_list|,
name|List
argument_list|<
name|Field
argument_list|>
name|fields
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|String
name|value
decl_stmt|;
if|if
condition|(
name|context
operator|.
name|externalValueSet
argument_list|()
condition|)
block|{
name|value
operator|=
name|context
operator|.
name|externalValue
argument_list|()
operator|.
name|toString
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|XContentParser
name|parser
init|=
name|context
operator|.
name|parser
argument_list|()
decl_stmt|;
if|if
condition|(
name|parser
operator|.
name|currentToken
argument_list|()
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|VALUE_NULL
condition|)
block|{
name|value
operator|=
name|fieldType
argument_list|()
operator|.
name|nullValueAsString
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|value
operator|=
name|parser
operator|.
name|textOrNull
argument_list|()
expr_stmt|;
block|}
block|}
if|if
condition|(
name|value
operator|==
literal|null
operator|||
name|value
operator|.
name|length
argument_list|()
operator|>
name|ignoreAbove
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|context
operator|.
name|includeInAll
argument_list|(
name|includeInAll
argument_list|,
name|this
argument_list|)
condition|)
block|{
name|context
operator|.
name|allEntries
argument_list|()
operator|.
name|addText
argument_list|(
name|fieldType
argument_list|()
operator|.
name|name
argument_list|()
argument_list|,
name|value
argument_list|,
name|fieldType
argument_list|()
operator|.
name|boost
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// convert to utf8 only once before feeding postings/dv/stored fields
specifier|final
name|BytesRef
name|binaryValue
init|=
operator|new
name|BytesRef
argument_list|(
name|value
argument_list|)
decl_stmt|;
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
name|binaryValue
argument_list|,
name|fieldType
argument_list|()
argument_list|)
decl_stmt|;
name|fields
operator|.
name|add
argument_list|(
name|field
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|fieldType
argument_list|()
operator|.
name|hasDocValues
argument_list|()
condition|)
block|{
name|fields
operator|.
name|add
argument_list|(
operator|new
name|SortedSetDocValuesField
argument_list|(
name|fieldType
argument_list|()
operator|.
name|name
argument_list|()
argument_list|,
name|binaryValue
argument_list|)
argument_list|)
expr_stmt|;
block|}
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
name|this
operator|.
name|includeInAll
operator|=
operator|(
operator|(
name|KeywordFieldMapper
operator|)
name|mergeWith
operator|)
operator|.
name|includeInAll
expr_stmt|;
name|this
operator|.
name|ignoreAbove
operator|=
operator|(
operator|(
name|KeywordFieldMapper
operator|)
name|mergeWith
operator|)
operator|.
name|ignoreAbove
expr_stmt|;
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
name|fieldType
argument_list|()
operator|.
name|nullValue
argument_list|()
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
name|fieldType
argument_list|()
operator|.
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|includeInAll
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"include_in_all"
argument_list|,
name|includeInAll
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|includeDefaults
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"include_in_all"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|includeDefaults
operator|||
name|ignoreAbove
operator|!=
name|Defaults
operator|.
name|IGNORE_ABOVE
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"ignore_above"
argument_list|,
name|ignoreAbove
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

